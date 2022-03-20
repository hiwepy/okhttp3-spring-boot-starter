package okhttp3.spring.boot;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(OkHttp3PoolProperties.PREFIX)
@Data
public class OkHttp3PoolProperties {

	public static final String PREFIX = "okhttp3.pool";

	/**
	 * Default value for max number od connections.
	 */
	public static final int DEFAULT_MAX_CONNECTIONS = 200;

	/**
	 * Default value for max number od connections.
	 */
	public static final int DEFAULT_MAX_REQUESTS = 64;

	/**
	 * Default value for max number od connections per route.
	 */
	public static final int DEFAULT_MAX_REQUESTS_PER_ROUTE = 5;

	/**
	 * The maximum number of requests to execute concurrently. Above this requests queue in memory, waiting for the running calls to complete.
	 * If more than maxRequests requests are in flight when this is invoked, those requests will remain in flight.
	 * 设置并发执行的最大请求数。上面是内存中的请求队列，等待正在运行的调用完成。
	 */
	private int maxRequests = DEFAULT_MAX_REQUESTS;
	/**
	 * The maximum number of requests for each host to execute concurrently.
	 * This limits requests by the URL's host name.
	 * Note that concurrent requests to a single IP address may still exceed this limit: multiple hostnames may share an IP address or be routed through the same HTTP proxy.
	 * If more than maxRequestsPerHost requests are in flight when this is invoked, those requests will remain in flight.
	 * WebSocket connections to hosts do not count against this limit.
	 * 设置每个主机并发执行的最大请求数。这会根据URL的主机名限制请求。
	 */
	private int maxRequestsPerHost = DEFAULT_MAX_REQUESTS_PER_ROUTE;

	/**
	 * The maximum number of idle connections for each address.
	 */
	private int maxIdleConnections = DEFAULT_MAX_CONNECTIONS;
	/**
	 * keep alive Duration. The default value is 5 Minutes.
	 */
	private Duration keepAliveDuration = Duration.ofMinutes(5);

}
