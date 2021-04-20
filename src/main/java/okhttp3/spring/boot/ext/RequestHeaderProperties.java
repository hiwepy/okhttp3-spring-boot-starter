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
package okhttp3.spring.boot.ext;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Http Request Header 配置
 * @author ： <a href="https://github.com/hiwepy">hiwepy</a>
 */
@ConfigurationProperties(RequestHeaderProperties.PREFIX)
@Data
public class RequestHeaderProperties {

	public static final String PREFIX = "okhttp3.header";
	
	public static final String DEFAULT_ACCEPT = "*/*";
	public static final String DEFAULT_ACCEPT_CHARSET = "utf-8, iso-8859-1;q=0.5";
	public static final String DEFAULT_ACCEPT_ENCODING = "gzip, deflate, br";
	public static final String DEFAULT_CONNECTION = "keep-alive";
	public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:63.0) Gecko/20100101 Firefox/63.0";

	/** Whether Enable OkHttp3 Header . */
	private boolean enabled = false;
	
	/**
	 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept
	 */
	private String accept = DEFAULT_ACCEPT;
	/**
	 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept-Charset
	 */
	private String acceptCharset = DEFAULT_ACCEPT_CHARSET;
	/**
	 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept-Encoding
	 */
	private String acceptEncoding = DEFAULT_ACCEPT_ENCODING;
	/**
	 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept-Language
	 */
	private String acceptLanguage = "*";
	/**
	 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept-Ranges
	 */
	private String acceptRanges = "";
	/**
	 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Authorization
	 */
	private String authorization;
	/**
	 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Connection
	 */
	private String connection = DEFAULT_CONNECTION;
	/**
	 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Host
	 */
	private String host;
	/**
	 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Origin
	 */
	private String origin = "";
	/**
	 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Proxy-Authorization
	 */
	private String proxyAuthorization;
	/**
	 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Proxy-Authenticate
	 */
	private String proxyAuthenticate;
	/**
	 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Referer
	 */
	private String referer;
	/**
	 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/User-Agent
	 */
	private String userAgent = DEFAULT_USER_AGENT;

}