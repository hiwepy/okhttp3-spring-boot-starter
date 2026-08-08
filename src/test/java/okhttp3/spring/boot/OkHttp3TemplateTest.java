package okhttp3.spring.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OkHttp3TemplateTest {

    private OkHttpClient client;
    private OkHttp3Template template;

    @BeforeEach
    void setUp() {
        client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    if (request.url().encodedPath().contains("failure")) {
                        throw new IOException("simulated failure");
                    }
                    int code = request.url().encodedPath().contains("server-error") ? 500 : 200;
                    String body = request.url().encodedPath().contains("invalid-json") ? "invalid" : "\"ok\"";
                    return new Response.Builder()
                            .request(request)
                            .protocol(okhttp3.Protocol.HTTP_1_1)
                            .code(code)
                            .message(code == 200 ? "OK" : "Server Error")
                            .body(ResponseBody.create(body, OkHttp3Template.APPLICATION_JSON))
                            .build();
                })
                .build();
        template = new OkHttp3Template(client, new ObjectMapper(), "http://localhost/");
    }

    @AfterEach
    void tearDown() {
        client.dispatcher().cancelAll();
        client.connectionPool().evictAll();
        client.dispatcher().executorService().shutdownNow();
    }

    @Test
    void shouldBuildUrlsRequestsAndAllHttpMethods() throws Exception {
        HttpUrl url = template.getHttpUrl("http://localhost/path", Map.of("a", 1, "empty", ""));
        assertEquals("1", url.queryParameter("a"));
        assertEquals("", url.queryParameter("empty"));
        assertEquals("http://localhost/path", template.getHttpUrl("http://localhost/path", null).toString());
        assertEquals("http://localhost/path", new OkHttp3Template(client, new ObjectMapper()).joinPath("http://localhost/path"));
        assertEquals("http://localhost/path", template.joinPath("path"));
        assertEquals("http://localhostpath", new OkHttp3Template(client, new ObjectMapper(), "http://localhost").joinPath("path"));

        for (OkHttp3Template.HttpMethod method : OkHttp3Template.HttpMethod.values()) {
            Request request = template.createRequestBuilder(
                    HttpUrl.get("http://localhost/method"), method, Map.of("X-Test", "yes"),
                    supportsBody(method) ? Map.of("value", 1) : null).build();
            assertEquals("yes", request.header("X-Test"));
            assertNotNull(method.getName());
            assertNotNull(method.apply(new Request.Builder().url("http://localhost/apply")));
        }
        assertNull(OkHttp3Template.HttpMethod.getByName(1));
    }

    @Test
    void shouldExecuteTypedAndRawSyncOverloads() throws Exception {
        assertEquals("ok", template.get("ok", String.class));
        assertEquals("ok", template.get("ok", Map.of("q", "v"), String.class));
        assertEquals("ok", template.get("ok", Map.of("X-Test", "yes"), Map.of("q", "v"), String.class));
        assertEquals("ok", template.post("ok", String.class));
        assertEquals("ok", template.post("ok", Map.of("body", true), String.class));
        assertEquals("ok", template.post("ok", Map.of("X-Test", "yes"), Map.of("body", true), String.class));
        assertEquals("ok", template.post("ok", Map.of("X-Test", "yes"), Map.of("q", "v"), String.class));
        assertEquals("ok", template.post("ok", Map.of("X-Test", "yes"), Map.of("q", "v"), Map.of("body", true), String.class));
        assertNull(template.doRequest("ok", OkHttp3Template.HttpMethod.GET, null, null, null, Void.TYPE));
        assertEquals("", template.get("server-error", String.class));
        assertEquals("", template.get("invalid-json", String.class));
        assertEquals("", template.get("failure", String.class));

        try (Response response = template.doRequest("ok", OkHttp3Template.HttpMethod.GET)) {
            assertEquals(200, response.code());
        }
        try (Response response = template.doRequest("server-error", OkHttp3Template.HttpMethod.GET, Map.of("q", "v"))) {
            assertEquals(500, response.code());
        }
        try (Response response = template.doRequest("ok", OkHttp3Template.HttpMethod.POST,
                Map.of("X-Test", "yes"), Map.of("q", "v"), Map.of("body", true))) {
            assertEquals("yes", response.request().header("X-Test"));
        }
        assertNull(template.doRequest(System.currentTimeMillis(), "failure", OkHttp3Template.HttpMethod.GET,
                null, null, null));
    }

    @Test
    void shouldExecuteTypedAsyncSuccessErrorVoidAndFailurePaths() throws Exception {
        CountDownLatch successLatch = new CountDownLatch(3);
        AtomicReference<String> successful = new AtomicReference<>();
        AtomicReference<String> unsuccessful = new AtomicReference<>();
        AtomicBoolean voidCalled = new AtomicBoolean();

        template.doAsyncRequest("ok", OkHttp3Template.HttpMethod.GET, value -> {
            successful.set(value);
            successLatch.countDown();
        }, String.class);
        template.doAsyncRequest("server-error", OkHttp3Template.HttpMethod.GET, null, value -> {
            unsuccessful.set(value);
            successLatch.countDown();
        }, null, String.class);
        template.doAsyncRequest("ok", OkHttp3Template.HttpMethod.GET, null, null, null, value -> {
            voidCalled.set(true);
            successLatch.countDown();
        }, null, Void.TYPE);

        assertTrue(successLatch.await(5, TimeUnit.SECONDS));
        assertEquals("ok", successful.get());
        assertEquals("", unsuccessful.get());
        assertTrue(voidCalled.get());

        CountDownLatch failureLatch = new CountDownLatch(1);
        AtomicReference<IOException> failure = new AtomicReference<>();
        template.doAsyncRequest("failure", OkHttp3Template.HttpMethod.GET, null, null,
                ignored -> { }, (call, error) -> {
                    failure.set(error);
                    failureLatch.countDown();
                    return true;
                }, String.class);
        assertTrue(failureLatch.await(5, TimeUnit.SECONDS));
        assertEquals("simulated failure", failure.get().getMessage());
    }

    @Test
    void shouldExecuteLowLevelAsyncCallbacksAndHandleJsonFallback() throws Exception {
        CountDownLatch responseLatch = new CountDownLatch(1);
        AtomicReference<Call> actualCall = new AtomicReference<>();
        template.doAsyncRequest(System.currentTimeMillis(), "ok", OkHttp3Template.HttpMethod.GET,
                Map.of("X-Test", "yes"), Map.of("q", "v"), null,
                (call, response) -> {
                    actualCall.set(call);
                    response.close();
                    responseLatch.countDown();
                    return true;
                }, null);
        assertTrue(responseLatch.await(5, TimeUnit.SECONDS));
        assertNotNull(actualCall.get());
        assertEquals("ok", template.readValue("\"ok\"", String.class));
        assertEquals("", template.readValue("invalid", String.class));
    }

    @Test
    void shouldCreateDefaultClientWhenNoneWasProvided() throws Exception {
        OkHttp3Template defaultTemplate = new OkHttp3Template();
        defaultTemplate.afterPropertiesSet();
        assertNotNull(defaultTemplate.okhttp3Client);
        assertTrue(defaultTemplate.okhttp3Client.retryOnConnectionFailure());
        assertFalse(defaultTemplate.okhttp3Client.dispatcher().executorService().isShutdown());
        defaultTemplate.okhttp3Client.connectionPool().evictAll();
        defaultTemplate.okhttp3Client.dispatcher().executorService().shutdownNow();
    }

    private boolean supportsBody(OkHttp3Template.HttpMethod method) {
        return method == OkHttp3Template.HttpMethod.POST
                || method == OkHttp3Template.HttpMethod.PUT
                || method == OkHttp3Template.HttpMethod.PATCH
                || method == OkHttp3Template.HttpMethod.DELETE;
    }
}
