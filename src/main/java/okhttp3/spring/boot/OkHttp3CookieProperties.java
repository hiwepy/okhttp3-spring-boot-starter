package okhttp3.spring.boot;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(OkHttp3CookieProperties.PREFIX)
@Data
public class OkHttp3CookieProperties {
	
	public static final String PREFIX = "okhttp3.cookie";

	/**
	 * he maximum size of the cache
	 */
	private long maximumSize = 10_000;

	/**
	 * the length of time after an entry is created that it should be automatically removed
	 */
	private Duration expireAfterWrite = Duration.ofMinutes(30);

	/**
	 * the length of time after an entry is created that it should be automatically removed
	 */
	private Duration expireAfterAccess = Duration.ofMinutes(30);

}