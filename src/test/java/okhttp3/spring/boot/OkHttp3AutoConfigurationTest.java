package okhttp3.spring.boot;

import okhttp3.OkHttpClient;
import okhttp3.extension.interceptor.RequestHeaderInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * OkHttp 高并发默认配置与自动装配测试。
 */
class OkHttp3AutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OkHttp3AutoConfiguration.class));

    @Test
    void shouldCreateSharedHighConcurrencyClientWithProductionDefaults() {
        contextRunner.run(context -> {
            OkHttpClient client = context.getBean(OkHttpClient.class);
            try {
                assertEquals(2_000, client.connectTimeoutMillis());
                assertEquals(10_000, client.writeTimeoutMillis());
                assertEquals(120_000, client.readTimeoutMillis());
                assertEquals(128, client.dispatcher().getMaxRequests());
                assertEquals(64, client.dispatcher().getMaxRequestsPerHost());
                assertTrue(client.retryOnConnectionFailure());
                assertEquals(1, client.interceptors().size());
                assertTrue(client.interceptors().get(0) instanceof RequestHeaderInterceptor);
            } finally {
                client.dispatcher().cancelAll();
                client.connectionPool().evictAll();
                client.dispatcher().executorService().shutdown();
            }
        });
    }

    @Test
    void springFactoriesShouldContainThreeNonBlankAutoConfigurations() throws IOException {
        Properties factories = PropertiesLoaderUtils.loadAllProperties("META-INF/spring.factories");
        String value = factories.getProperty("org.springframework.boot.autoconfigure.EnableAutoConfiguration");
        String[] classNames = Arrays.stream(value.split(","))
                .map(String::trim)
                .toArray(String[]::new);

        assertEquals(3, classNames.length);
        assertTrue(Arrays.stream(classNames).noneMatch(String::isEmpty));
        assertTrue(Arrays.asList(classNames).contains(
                "okhttp3.spring.boot.actuate.OkHttp3EndpointAutoConfiguration"));
    }

    @Test
    void shouldKeepPrimarySharedClientWhenProviderHasDedicatedClient() {
        contextRunner.withUserConfiguration(DedicatedProviderClientConfiguration.class).run(context -> {
            assertEquals(2, context.getBeansOfType(OkHttpClient.class).size());
            assertEquals(context.getBean("okhttp3Client"), context.getBean(OkHttpClient.class));
            assertEquals(context.getBean("providerOkHttpClient"),
                    context.getBean("providerOkHttpClient", OkHttpClient.class));
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class DedicatedProviderClientConfiguration {

        @Bean
        OkHttpClient providerOkHttpClient() {
            return new OkHttpClient();
        }
    }
}
