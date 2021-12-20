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
	 * The maximum number of processing connections at the moment , The default value is 64.
	 * 最大瞬时处理连接数量，默认 64
	 */
	private int maxRequests = DEFAULT_MAX_REQUESTS;
	/**
	 * The maximum number of od connections for each address, The default value is 5.
	 * 每个请求地址最大瞬时处理连接数量,默认5
	 */
	private int maxRequestsPerHost = DEFAULT_MAX_REQUESTS_PER_ROUTE;
	/**
	 * The maximum number of idle connections for each address.
	 * 最大空闲连接梳数量，超出该值后，连接用完后会被关闭，最多只会保留idleConnectionCount个连接数量
	 */
	private int maxIdleConnections = DEFAULT_MAX_CONNECTIONS;
	/**
	 * keep alive Duration. The default value is 5 Minutes.
	 */
	private Duration keepAliveDuration = Duration.ofMinutes(5);

}
