package okhttp3.spring.boot;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * https://www.cnblogs.com/lujiango/p/11771319.html
 */
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
	 * The maximum number of requests to execute concurrently. Above this requests queue in
	 * memory, waiting for the running calls to complete.
	 *
	 * If more than {@code maxRequests} requests are in flight when this is invoked, those requests
	 * will remain in flight.
	 *
	 * 当前okhttpclient实例最大的并发请求数;
	 * 默认：64，默认的64一般满足不了业务需要。这个值一般要大于maxRequestPerHost，如果这个值小于maxRequestPerHost会导致，请求单个主机的并发不可能超过maxRequest.
	 */
	private int maxRequests = DEFAULT_MAX_REQUESTS;
	/**
	 * The maximum number of requests for each host to execute concurrently. This limits requests
	 * by the URL's host name. Note that concurrent requests to a single IP address may still exceed
	 * this limit: multiple hostnames may share an IP address or be routed through the same HTTP
	 * proxy.
	 *
	 * If more than {@code maxRequestsPerHost} requests are in flight when this is invoked, those
	 * requests will remain in flight.
	 *
	 * WebSocket connections to hosts do not count against this limit.
	 *
	 * 单个主机最大请求并发数，这里的主机指被请求方主机，一般可以理解对调用方有限流作用。注意：websocket请求不受这个限制。
	 *
	 * 默认：4，一般建议与maxRequest保持一致。
	 *
	 * 这个值设置，有如下几个场景考虑：
	 *
	 * （1）如果被调用方的并发能力只能支持200，那这个值最好不要超过200，否则对调用方有压力；
	 *
	 * （2）如果当前okhttpclient实例只对一个调用方发起调用，那这个值与maxRequests保持一致；
	 *
	 * （3）如果当前okhttpclient实例在一个事务中对n个调用方发起调用，n * maxReuestPerHost要接近maxRequest
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
