/*
 * Copyright (c) 2018, hiwepy (https://github.com/hiwepy).
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
package okhttp3.spring.boot;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.binder.MeterBinder;
import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.spring.boot.metrics.MetricNames;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

/**
 * OkHttp3 Metrics
 */
public class OkHttp3Metrics implements MeterBinder, ApplicationListener<ApplicationStartedEvent> {

	/**
	 * Prefix used for all OkHttp3 metric names.
	 */
	public static final String OKHTTP3_METRIC_NAME_PREFIX = "okhttp3";
	/**
	 * Prefix used for all OkHttp3 Event metric names.
	 */
	public static final String OKHTTP3_EVENT_METRIC_NAME_PREFIX = "okhttp3.event";
	/**
	 * Prefix used for all OkHttp3 Request metric names.
	 */
	public static final String OKHTTP3_REQUEST_METRIC_NAME_PREFIX = "okhttp3.requests";

	/**
	 * dispatcher
	 */
	public static final String METRIC_NAME_DISPATCHER_MAX_REQUESTS 			= OKHTTP3_METRIC_NAME_PREFIX + ".dispatcher.max.requests";
	public static final String METRIC_NAME_DISPATCHER_MAX_REQUESTS_PERHOST 	= OKHTTP3_METRIC_NAME_PREFIX + ".dispatcher.max.requests.perhost";
	public static final String METRIC_NAME_DISPATCHER_QUEUED_CALLS_COUNT		= OKHTTP3_METRIC_NAME_PREFIX + ".dispatcher.queued.calls.count";
	public static final String METRIC_NAME_DISPATCHER_RUNNING_CALLS_COUNT		= OKHTTP3_METRIC_NAME_PREFIX + ".dispatcher.running.calls.count";

	/**
	 * http cache
	 */
	public static final String METRIC_NAME_CACHE_REQUEST_COUNT 		= OKHTTP3_METRIC_NAME_PREFIX + ".cache.request.count";
	public static final String METRIC_NAME_CACHE_HIT_COUNT 		= OKHTTP3_METRIC_NAME_PREFIX + ".cache.hit.count";
	public static final String METRIC_NAME_CACHE_NETWORK_COUNT 		= OKHTTP3_METRIC_NAME_PREFIX + ".cache.network.count";
	public static final String METRIC_NAME_CACHE_WRITE_SUCCESS_COUNT 		= OKHTTP3_METRIC_NAME_PREFIX + ".cache.write.success.count";
	public static final String METRIC_NAME_CACHE_WRITE_ABORT_COUNT 		= OKHTTP3_METRIC_NAME_PREFIX + ".cache.write.abort.count";
	public static final String METRIC_NAME_CACHE_CURRENT_SIZE 	= OKHTTP3_METRIC_NAME_PREFIX + ".cache.current.size";
	public static final String METRIC_NAME_CACHE_MAX_SIZE 	= OKHTTP3_METRIC_NAME_PREFIX + ".cache.max.size";

	/**
	 * connection pool
	 */
	public static final String METRIC_NAME_CONNECTION_POOL_CONNECTION_COUNT		= OKHTTP3_METRIC_NAME_PREFIX + ".connection.pool.connection.count";
	public static final String METRIC_NAME_CONNECTION_POOL_IDLE_CONNECTION_COUNT		= OKHTTP3_METRIC_NAME_PREFIX + ".connection.pool.idle.connection.count";

	/**
	 * timeout
	 */
	public static final String METRIC_NAME_CALL_TIMEOUT_COUNT 			= OKHTTP3_METRIC_NAME_PREFIX + ".call.timeout.count";
	public static final String METRIC_NAME_CONNECT_TIMEOUT_COUNT 		= OKHTTP3_METRIC_NAME_PREFIX + ".connect.timeout.count";
	public static final String METRIC_NAME_READ_TIMEOUT_COUNT 			= OKHTTP3_METRIC_NAME_PREFIX + ".read.timeout.count";
	public static final String METRIC_NAME_WRITE_TIMEOUT_COUNT 		= OKHTTP3_METRIC_NAME_PREFIX + ".write.timeout.count";
	public static final String METRIC_NAME_PING_FAIL_COUNT 			= OKHTTP3_METRIC_NAME_PREFIX + ".ping.fail.count";

	/**
	 * network
	 */
	public static final String METRIC_NAME_NETWORK_REQUESTS_SUBMITTED 			= OKHTTP3_METRIC_NAME_PREFIX + ".network.requests.submitted";
	public static final String METRIC_NAME_NETWORK_REQUESTS_RUNNING 			= OKHTTP3_METRIC_NAME_PREFIX + ".network.requests.running";
	public static final String METRIC_NAME_NETWORK_REQUESTS_COMPLETED 			= OKHTTP3_METRIC_NAME_PREFIX + ".network.requests.completed";
	public static final String METRIC_NAME_NETWORK_REQUESTS_DURATION 			= OKHTTP3_METRIC_NAME_PREFIX + ".network.requests.duration";

	private OkHttpClient okhttp3Client;
	private Iterable<Tag> tags;

	public OkHttp3Metrics(OkHttpClient okhttp3Client) {
		this(okhttp3Client, Collections.emptyList());
	}

	public OkHttp3Metrics(OkHttpClient okhttp3Client, Iterable<Tag> tags) {
		this.okhttp3Client = okhttp3Client;
		this.tags = tags;
	}

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		this.bindTo(event.getApplicationContext().getBean(MeterRegistry.class));
	}

	@Override
	public void bindTo(MeterRegistry registry) {
		registerDispatcher(registry);
		registerHttpCache(registry);
		registerConnectionPool(registry);
		registerOkHttpClient(registry);
	}

	private void registerDispatcher(MeterRegistry registry) {
		Dispatcher dispatcher = okhttp3Client.dispatcher();
		bindCounter(registry, METRIC_NAME_DISPATCHER_MAX_REQUESTS, "max requests of dispatcher ", dispatcher, Dispatcher::getMaxRequests);
		bindCounter(registry, METRIC_NAME_DISPATCHER_MAX_REQUESTS_PERHOST, "max requests of dispatcher by per host ", dispatcher, Dispatcher::getMaxRequestsPerHost);
		bindGauge(registry, METRIC_NAME_DISPATCHER_QUEUED_CALLS_COUNT, "Total number of queued calls ", dispatcher, Dispatcher::queuedCallsCount);
		bindGauge(registry, METRIC_NAME_DISPATCHER_RUNNING_CALLS_COUNT, "Total number of running calls ", dispatcher, Dispatcher::runningCallsCount);
	}

	private void registerHttpCache(MeterRegistry registry) {
		Cache cache = okhttp3Client.cache();
		bindGauge(registry, METRIC_NAME_CACHE_REQUEST_COUNT, "Total number of cache request ", cache, Cache::requestCount);
		bindGauge(registry, METRIC_NAME_CACHE_HIT_COUNT, "Total number of cache hit ", cache, Cache::hitCount);
		bindGauge(registry, METRIC_NAME_CACHE_NETWORK_COUNT, "Total number of cache network ", cache, Cache::networkCount);
		bindGauge(registry, METRIC_NAME_CACHE_WRITE_SUCCESS_COUNT, "Total number of cache write success ", cache, Cache::writeSuccessCount);
		bindGauge(registry, METRIC_NAME_CACHE_WRITE_ABORT_COUNT, "Total number of cache write abort ", cache, Cache::writeAbortCount);
		bindGauge(registry, METRIC_NAME_CACHE_CURRENT_SIZE, "Total number of current cache size ", cache, (c) -> {
			try {
				return c.size();
			} catch (IOException e) {
				return 0;
			}
		});
		bindGauge(registry, METRIC_NAME_CACHE_MAX_SIZE, "Total number of cache max size ", cache, Cache::maxSize);
	}

	private void registerConnectionPool(MeterRegistry registry) {
		ConnectionPool connectionPool = okhttp3Client.connectionPool();
		bindGauge(registry, METRIC_NAME_CONNECTION_POOL_CONNECTION_COUNT, "Total number of connection pool connection count ", connectionPool, ConnectionPool::connectionCount);
		bindGauge(registry, METRIC_NAME_CONNECTION_POOL_IDLE_CONNECTION_COUNT, "Total number of connection pool idle connection count ", connectionPool, ConnectionPool::idleConnectionCount);
	}

	private void registerOkHttpClient(MeterRegistry registry) {
		bindGauge(registry, METRIC_NAME_CALL_TIMEOUT_COUNT, "Total number of call timeout ", okhttp3Client, OkHttpClient::callTimeoutMillis);
		bindGauge(registry, METRIC_NAME_CONNECT_TIMEOUT_COUNT, "Total number of connect timeout ", okhttp3Client, OkHttpClient::connectTimeoutMillis);
		bindGauge(registry, METRIC_NAME_READ_TIMEOUT_COUNT, "Total number of read timeout", okhttp3Client, OkHttpClient::readTimeoutMillis);
		bindGauge(registry, METRIC_NAME_WRITE_TIMEOUT_COUNT, "Total number of write timeout ", okhttp3Client, OkHttpClient::writeTimeoutMillis);
		bindGauge(registry, METRIC_NAME_PING_FAIL_COUNT, "Total number of ping fail ", okhttp3Client, OkHttpClient::pingIntervalMillis);
	}

	private <T> void bindTimer(MeterRegistry registry, String name, String desc, T metricsHandler,
						   ToLongFunction<T> countFunc, ToDoubleFunction<T> consumer) {
		FunctionTimer.builder(name, metricsHandler, countFunc, consumer, TimeUnit.SECONDS)
				.description(desc)
				.tags(tags)
				.register(registry);
	}

	private <T> void bindGauge(MeterRegistry registry, String name, String desc, T metricResult,
								   ToDoubleFunction<T> consumer) {
		Gauge.builder(name, metricResult, consumer)
				.description(desc)
				.tags(tags)
				.register(registry);
	}
	
	private <T> void bindTimeGauge(MeterRegistry registry, String name, String desc, T metricResult,
							   ToDoubleFunction<T> consumer) {
		TimeGauge.builder(name, metricResult, TimeUnit.SECONDS, consumer)
				.description(desc)
				.tags(tags)
				.register(registry);
	}

	private <T> void bindCounter(MeterRegistry registry, String name, String desc, T metricsHandler,
							 ToDoubleFunction<T> consumer) {
		FunctionCounter.builder(name, metricsHandler, consumer)
				.description(desc)
				.tags(tags)
				.register(registry);
	}

}
