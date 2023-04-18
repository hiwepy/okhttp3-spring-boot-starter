package okhttp3.spring.boot;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.okhttp3.OkHttpConnectionPoolMetrics;
import io.micrometer.core.instrument.binder.okhttp3.OkHttpObservationInterceptor;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OkHttp Client Ini
 */
@Configuration
@ConditionalOnClass({ OkHttpClient.class, OkHttpConnectionPoolMetrics.class , OkHttpObservationInterceptor.class  })
@ConditionalOnProperty(prefix = OkHttp3MetricsProperties.PREFIX, value = "enabled", havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties({ OkHttp3MetricsProperties.class })
public class OkHttp3MetricsAutoConfiguration {

	@Bean
	public OkHttp3MetricsConfigurer okHttp3MetricsConfigurer(ObjectProvider<MeterRegistry> registryProvider,
															 OkHttp3MetricsProperties metricsProperties) {
		return new OkHttp3MetricsConfigurer(registryProvider.getIfAvailable(), metricsProperties);
	}

}
