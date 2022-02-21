package okhttp3.spring.boot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 响应结果
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = false)
public class Okhttp3Response {

	/**
	 * 响应状态码，200表示成功，非200表示失败
	 */
	@JsonProperty("code")
	protected int code;

	public boolean isSuccess() {
		return code == 200;
	}

}
