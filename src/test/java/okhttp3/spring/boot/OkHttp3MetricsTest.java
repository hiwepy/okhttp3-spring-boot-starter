package okhttp3.spring.boot;

import com.google.common.collect.ImmutableMap;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.okhttp3.OkHttpConnectionPoolMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import okhttp3.OkHttpClient;
import okhttp3.metrics.OkHttp3Metrics;
import okhttp3.metrics.OkHttpCacheMetrics;
import okhttp3.metrics.OkHttpDispatcherMetrics;
import okhttp3.spring.boot.actuate.OkHttp3Endpoint;
import okhttp3.spring.boot.actuate.OkHttp3EndpointAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OkHttp3MetricsTest {

    @Test
    void shouldExposeGaugeTimerAndSummaryMetrics() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        AtomicInteger gaugeValue = new AtomicInteger(3);
        String prefix = OkHttp3Metrics.OKHTTP3_METRIC_NAME_PREFIX;
        Gauge.builder(prefix + "connections", gaugeValue, AtomicInteger::get).register(registry);
        Timer timer = Timer.builder(prefix + "latency").publishPercentiles(0.5).publishPercentileHistogram().register(registry);
        timer.record(25, TimeUnit.MILLISECONDS);
        DistributionSummary summary = DistributionSummary.builder(prefix + "bytes")
                .publishPercentiles(0.5).publishPercentileHistogram().register(registry);
        summary.record(128);
        Gauge.builder("unrelated", gaugeValue, AtomicInteger::get).register(registry);

        OkHttp3Endpoint endpoint = new OkHttp3Endpoint(registry);
        Map<String, Object> info = endpoint.okHttp3Metrics();
        Map<String, Object> metrics = endpoint.getMetrics();

        assertTrue(info.containsKey("okhttp3"));
        assertEquals(metrics, info.get("metrics"));
        assertEquals(3.0, metrics.get(prefix + "connections"));
        assertTrue(metrics.keySet().stream().anyMatch(key -> key.startsWith(prefix + "latency")));
        assertTrue(metrics.keySet().stream().anyMatch(key -> key.startsWith(prefix + "bytes")));
        assertFalse(metrics.containsKey("unrelated"));
        assertFalse(endpoint.convertTimerToMap("timer", timer).isEmpty());
        assertFalse(endpoint.convertSummaryToMap("summary", summary).isEmpty());
    }

    @Test
    void shouldCreateAllMetricsBeansAndEndpoint() {
        OkHttp3MetricsAutoConfiguration configuration = new OkHttp3MetricsAutoConfiguration();
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        OkHttpClient.Builder builder = configuration.okhttp3Builder();
        factory.registerSingleton("builder", builder);
        factory.registerSingleton("registry", registry);

        OkHttpClient client = configuration.okhttp3Client(
                factory.getBeanProvider(OkHttpClient.Builder.class),
                factory.getBeanProvider(io.micrometer.core.instrument.MeterRegistry.class));
        DefaultListableBeanFactory clientFactory = new DefaultListableBeanFactory();
        clientFactory.registerSingleton("client", client);

        OkHttpCacheMetrics cacheMetrics = configuration.okHttp3CacheMetrics(clientFactory.getBeanProvider(OkHttpClient.class));
        OkHttpDispatcherMetrics dispatcherMetrics = configuration.okHttp3DispatcherMetrics(clientFactory.getBeanProvider(OkHttpClient.class));
        OkHttp3MetricsProperties properties = new OkHttp3MetricsProperties();
        properties.setExtraTags(ImmutableMap.of("service", "test"));
        OkHttpConnectionPoolMetrics poolMetrics = configuration.okHttp3ConnectionPoolMetrics(
                clientFactory.getBeanProvider(OkHttpClient.class), properties);
        properties.setExtraTags(null);
        assertNotNull(configuration.okHttp3ConnectionPoolMetrics(clientFactory.getBeanProvider(OkHttpClient.class), properties));

        assertNotNull(cacheMetrics);
        assertNotNull(dispatcherMetrics);
        assertNotNull(poolMetrics);
        assertNotNull(new OkHttp3EndpointAutoConfiguration().okHttp3Endpoint(registry));

        client.connectionPool().evictAll();
        client.dispatcher().executorService().shutdownNow();
    }
}
