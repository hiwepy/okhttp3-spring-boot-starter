package okhttp3.spring.boot;

import io.micrometer.common.KeyValue;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.okhttp3.DefaultOkHttpObservationConvention;
import io.micrometer.core.instrument.binder.okhttp3.OkHttpConnectionPoolMetrics;
import io.micrometer.core.instrument.binder.okhttp3.OkHttpObservationConvention;
import io.micrometer.core.instrument.binder.okhttp3.OkHttpObservationInterceptor;
import io.micrometer.observation.ObservationRegistry;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OkHttp3MetricsConfigurer implements OKhttp3Configurer {

    protected MeterRegistry registry;
    protected OkHttp3MetricsProperties metricsProperties;

    public OkHttp3MetricsConfigurer(MeterRegistry registry, OkHttp3MetricsProperties metricsProperties) {
        this.registry = registry;
        this.metricsProperties = metricsProperties;
    }

    @Override
    public void configure(ConnectionPool connectionPool) {
        if(metricsProperties.isEnabled()){
            new OkHttpConnectionPoolMetrics(connectionPool).bindTo(registry);
        }
    }

    @Override
    public void configure(OkHttpClient.Builder builder) {
        if(metricsProperties.isEnabled()){
            ObservationRegistry observationRegistry = ObservationRegistry.create();
            OkHttpObservationConvention observationConvention = new DefaultOkHttpObservationConvention("okhttp.request" );

            Function<Request, String> urlMapper = (r) -> {
                return r.url().toString();
            };

            Iterable<KeyValue> extraTags = CollectionUtils.isEmpty(metricsProperties.getExtraTags()) ? Collections.emptyList() : metricsProperties.getExtraTags().entrySet().stream().map(e -> KeyValue.of(e.getKey(), e.getValue())).collect(Collectors.toList());
            Iterable<BiFunction<Request, Response, KeyValue>> contextSpecificTags = CollectionUtils.isEmpty(metricsProperties.getExtraTags()) ? Collections.emptyList() : Collections.emptyList();

            OkHttpObservationInterceptor observationInterceptor = new OkHttpObservationInterceptor(
                    observationRegistry,
                    observationConvention,
                    "okhttp.request",
                    urlMapper,
                    extraTags,
                    contextSpecificTags,
                    metricsProperties.getRequestTagKeys(),
                    metricsProperties.isIncludeHostTag()
            );
            builder.addInterceptor(observationInterceptor);
        }
    }
}
