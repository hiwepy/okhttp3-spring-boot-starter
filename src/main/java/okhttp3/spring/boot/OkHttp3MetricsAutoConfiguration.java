package okhttp3.spring.boot;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.okhttp3.OkHttpConnectionPoolMetrics;
import io.micrometer.core.instrument.binder.okhttp3.OkHttpObservationInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.metrics.InstrumentedOkHttpClients;
import okhttp3.metrics.OkHttp3Metrics;
import okhttp3.metrics.OkHttpCacheMetrics;
import okhttp3.metrics.OkHttpDispatcherMetrics;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * OkHttp Client Metrics Ini
 * @author wandl
 */
@AutoConfigureAfter(MetricsAutoConfiguration.class)
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ MeterRegistry.class, OkHttpClient.class, OkHttpConnectionPoolMetrics.class , OkHttpObservationInterceptor.class  })
@ConditionalOnBean(MeterRegistry.class)
@EnableConfigurationProperties({ OkHttp3MetricsProperties.class })
public class OkHttp3MetricsAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public okhttp3.OkHttpClient.Builder okhttp3Builder(){
		return new okhttp3.OkHttpClient.Builder();
	}

	@Bean
	public OkHttpClient okhttp3Client(ObjectProvider<okhttp3.OkHttpClient.Builder> okhttp3BuilderProvider,
									  ObjectProvider<MeterRegistry> meterRegistryProvider){
		OkHttpClient okhttp3Client = okhttp3BuilderProvider.getObject().build();
		return InstrumentedOkHttpClients.create(meterRegistryProvider.getObject(), okhttp3Client);
	}

	@Bean
	public OkHttpCacheMetrics okHttp3CacheMetrics(ObjectProvider<OkHttpClient> okhttp3ClientProvider) {
		return new OkHttpCacheMetrics(okhttp3ClientProvider.getObject(), OkHttp3Metrics.OKHTTP3_POOL_METRIC_NAME_PREFIX);
	}

	@Bean
	public OkHttpDispatcherMetrics okHttp3DispatcherMetrics(ObjectProvider<OkHttpClient> okhttp3ClientProvider) {
		return new OkHttpDispatcherMetrics(okhttp3ClientProvider.getObject(), OkHttp3Metrics.OKHTTP3_POOL_METRIC_NAME_PREFIX);
	}

	@Bean
	public OkHttpConnectionPoolMetrics okHttp3ConnectionPoolMetrics(ObjectProvider<OkHttpClient> okhttp3ClientProvider,
																	OkHttp3MetricsProperties metricsProperties){
		Iterable<Tag> extraTags = CollectionUtils.isEmpty(metricsProperties.getExtraTags()) ? new ArrayList<>()  : metricsProperties.getExtraTags()
				.entrySet().stream().map(e -> Tag.of(e.getKey(), e.getValue())).collect(Collectors.toList());
		return new OkHttpConnectionPoolMetrics(okhttp3ClientProvider.getObject().connectionPool(), OkHttp3Metrics.OKHTTP3_POOL_METRIC_NAME_PREFIX , extraTags);
	}

}
