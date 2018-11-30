/*
 * Copyright (c) 2018, vindell (https://github.com/vindell).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package okhttp3.spring.boot.actuate;


import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.health.HealthIndicatorAutoConfiguration;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.codahale.metrics.MetricRegistry;

import okhttp3.OkHttpClient;
import okhttp3.spring.boot.OkHttp3AutoConfiguration;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for {@link OkHttp3Endpoint}.
 * @author 		： <a href="https://github.com/vindell">vindell</a>
 */
@Configuration
@ConditionalOnClass({OkHttpClient.class, MetricRegistry.class, HealthIndicator.class, EndpointAutoConfiguration.class})
@ConditionalOnEnabledHealthIndicator("okhttp3")
@AutoConfigureBefore(HealthIndicatorAutoConfiguration.class)
@AutoConfigureAfter(OkHttp3AutoConfiguration.class)
public class OkHttp3EndpointAutoConfiguration {
	
	@Bean
	@ConditionalOnMissingBean
	public MetricRegistry registry() {
		return new MetricRegistry();
	}
	
	@Bean
	@ConditionalOnMissingBean
    public OkHttp3MetricsInterceptor okHttp3MetricsInterceptor(MetricRegistry registry) {
        return new OkHttp3MetricsInterceptor(registry);
    }

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnEnabledEndpoint
    public OkHttp3Endpoint okHttp3Endpoint(MetricRegistry registry) {
        return new OkHttp3Endpoint(registry);
	}

}
