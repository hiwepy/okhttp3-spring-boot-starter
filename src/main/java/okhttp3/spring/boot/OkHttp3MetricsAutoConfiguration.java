package okhttp3.spring.boot;

import io.micrometer.common.KeyValue;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.okhttp3.*;
import io.micrometer.observation.ObservationRegistry;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.spring.boot.metrics.OkHttp3MetricsInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * OkHttp Client Ini
 */
@AutoConfigureAfter(MetricsAutoConfiguration.class)
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ MeterRegistry.class, OkHttpClient.class, OkHttpConnectionPoolMetrics.class , OkHttpObservationInterceptor.class  })
@ConditionalOnProperty(prefix = OkHttp3MetricsProperties.PREFIX, value = "enabled", havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties({ OkHttp3MetricsProperties.class })
public class OkHttp3MetricsAutoConfiguration {

	@Bean
	public OkHttp3Metrics okhttp3Metrics(OkHttpClient okhttp3Client) {
		return new OkHttp3Metrics(okhttp3Client);
	}

	@Bean
	public OkHttpConnectionPoolMetrics okHttp3ConnectionPoolMetrics(OkHttpClient okhttp3Client, OkHttp3MetricsProperties metricsProperties){
		Iterable<Tag> extraTags = CollectionUtils.isEmpty(metricsProperties.getExtraTags()) ? Collections.emptyList() : metricsProperties.getExtraTags().entrySet().stream().map(e -> Tag.of(e.getKey(), e.getValue())).collect(Collectors.toList());
		return new OkHttpConnectionPoolMetrics(okhttp3Client.connectionPool(), OkHttp3PoolProperties.PREFIX , extraTags);
	}

	@Bean
	public OkHttpMetricsEventListener okHttp3MetricsEventListener(MeterRegistry meterRegistry,
																  ObjectProvider<OKhttp3MetricsSpecificTagHandler> specificTagHandlerObjectProvider,
																  OkHttp3MetricsProperties metricsProperties) {
		Iterable<Tag> extraTags = CollectionUtils.isEmpty(metricsProperties.getExtraTags()) ? Collections.emptyList() : metricsProperties.getExtraTags().entrySet().stream().map(e -> Tag.of(e.getKey(), e.getValue())).collect(Collectors.toList());
		Iterable<BiFunction<Request, Response, KeyValue>> contextSpecificTags = specificTagHandlerObjectProvider.stream().map(specificTagHandler -> specificTagHandler.getHandler()).collect(Collectors.toList());
		// 默认也支持URI_PATTERN header支持uri tag
		Function<Request, String> urlMapper = (r) -> {
			return r.url().encodedPath().toString();
		};
		return OkHttpMetricsEventListener.builder(meterRegistry, OkHttp3Metrics.OKHTTP3_EVENT_METRIC_NAME_PREFIX)
				.tags(extraTags)
				.requestTagKeys(metricsProperties.getRequestTagKeys())
				.includeHostTag(metricsProperties.isIncludeHostTag())
				.uriMapper(urlMapper)
				.build();
	}

	@Bean
	public OkHttp3MetricsInterceptor okHttp3MetricsInterceptor(MeterRegistry meterRegistry) {
		return new OkHttp3MetricsInterceptor(meterRegistry);
	}

	@Autowired
	void bindMetricsRegistryToOkHttpInterceptors(OkHttpClient okhttp3Client,
												 OkHttp3MetricsProperties metricsProperties,
												 ObjectProvider<OKhttp3MetricsSpecificTagHandler> specificTagHandlerObjectProvider,
												 ObservationRegistry observationRegistry) {

		OkHttpObservationConvention observationConvention = new DefaultOkHttpObservationConvention(OkHttp3Properties.PREFIX);

		Function<Request, String> urlMapper = (request) -> {
			return request.url().toString();
		};

		Iterable<KeyValue> extraTags = CollectionUtils.isEmpty(metricsProperties.getExtraTags()) ? Collections.emptyList() : metricsProperties.getExtraTags().entrySet().stream().map(e -> KeyValue.of(e.getKey(), e.getValue())).collect(Collectors.toList());
		Iterable<BiFunction<Request, Response, KeyValue>> contextSpecificTags = specificTagHandlerObjectProvider.stream().map(specificTagHandler -> specificTagHandler.getHandler()).collect(Collectors.toList());

		OkHttpObservationInterceptor observationInterceptor = new OkHttpObservationInterceptor(
				observationRegistry,
				observationConvention,
				OkHttp3Metrics.OKHTTP3_REQUEST_METRIC_NAME_PREFIX,
				urlMapper,
				extraTags,
				contextSpecificTags,
				metricsProperties.getRequestTagKeys(),
				metricsProperties.isIncludeHostTag()
		);

		okhttp3Client.networkInterceptors().add(0, observationInterceptor);
	}

}
