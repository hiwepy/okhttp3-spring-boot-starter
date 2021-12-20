package okhttp3.spring.boot;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(OkHttp3PoolProperties.PREFIX)
@Data
public class OkHttp3PoolProperties {

	public static final String PREFIX = "okhttp3.pool";

	/**
	 * The maximum number of processing connections at the moment , The default value is 64.
	 * 最大瞬时处理连接数量，默认 64
	 */
	private int maxRequests = 64;
	/**
	 * The maximum number of processing connections at the moment for each address, The default value is 64.
	 * 每个请求地址最大瞬时处理连接数量,默认5
	 */
	private int maxRequestsPerHost = 5;
	/**
	 * The maximum number of idle connections for each address.
	 * 最大空闲连接梳数量，超出该值后，连接用完后会被关闭，最多只会保留idleConnectionCount个连接数量
	 */
	private int maxIdleConnections = 5;
	/**
	 * keep alive Duration. 5The default value is  Minutes.
	 */
	private Duration keepAliveDuration = Duration.ofMinutes(5);



}
