package okhttp3.spring.boot;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
@ConfigurationProperties(OkHttp3MetricsProperties.PREFIX)
@Data
public class OkHttp3MetricsProperties {

	public static final String PREFIX = "okhttp3.metrics";

	/**
	 * Whether Enable OkHttp3 Metrics.
	 */
	private boolean enabled = false;

	/**
	 * Extra tags for metrics.
	 */
	private Map<String, String > extraTags = new LinkedHashMap<>(16);

	/**
	 * Whether include host tag.
	 */
	boolean includeHostTag;

	/**
	 * The tag keys to request.
	 */
	List<String> requestTagKeys;

}
