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
package okhttp3.spring.boot.ext;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Http Request Header 配置
 * @author ： <a href="https://github.com/vindell">vindell</a>
 */
@ConfigurationProperties(RequestHeaderProperties.PREFIX)
public class RequestHeaderProperties {

	public static final String PREFIX = "okhttp3.header";
	
	public static final String DEFAULT_ACCEPT = "*/*";
	public static final String DEFAULT_ACCEPT_CHARSET = "utf-8, iso-8859-1;q=0.5";
	public static final String DEFAULT_ACCEPT_ENCODING = "gzip, deflate, br";
	public static final String DEFAULT_CONNECTION = "keep-alive";
	public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:63.0) Gecko/20100101 Firefox/63.0";

	/**
	 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept
	 */
	private String Accept = DEFAULT_ACCEPT;
	/**
	 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept-Charset
	 */
	private String AcceptCharset = DEFAULT_ACCEPT_CHARSET;
	/**
	 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept-Encoding
	 */
	private String AcceptEncoding = DEFAULT_ACCEPT_ENCODING;
	/**
	 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept-Language
	 */
	private String AcceptLanguage = "*";
	/**
	 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept-Ranges
	 */
	private String AcceptRanges = "";
	/**
	 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Authorization
	 */
	private String Authorization;
	/**
	 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Connection
	 */
	private String Connection = DEFAULT_CONNECTION;
	/**
	 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Host
	 */
	private String Host;
	/**
	 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Origin
	 */
	private String Origin = "";
	/**
	 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Proxy-Authorization
	 */
	private String ProxyAuthorization;
	/**
	 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Proxy-Authenticate
	 */
	private String ProxyAuthenticate;
	/**
	 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Referer
	 */
	private String Referer;
	/**
	 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/User-Agent
	 */
	private String UserAgent = DEFAULT_USER_AGENT;

	public String getAccept() {
		return Accept;
	}

	public void setAccept(String accept) {
		Accept = accept;
	}

	public String getAcceptCharset() {
		return AcceptCharset;
	}

	public void setAcceptCharset(String acceptCharset) {
		AcceptCharset = acceptCharset;
	}

	public String getAcceptEncoding() {
		return AcceptEncoding;
	}

	public void setAcceptEncoding(String acceptEncoding) {
		AcceptEncoding = acceptEncoding;
	}

	public String getAcceptLanguage() {
		return AcceptLanguage;
	}

	public void setAcceptLanguage(String acceptLanguage) {
		AcceptLanguage = acceptLanguage;
	}

	public String getAcceptRanges() {
		return AcceptRanges;
	}

	public void setAcceptRanges(String acceptRanges) {
		AcceptRanges = acceptRanges;
	}

	public String getAuthorization() {
		return Authorization;
	}

	public void setAuthorization(String authorization) {
		Authorization = authorization;
	}

	public String getConnection() {
		return Connection;
	}

	public void setConnection(String connection) {
		Connection = connection;
	}

	public String getHost() {
		return Host;
	}

	public void setHost(String host) {
		Host = host;
	}

	public String getOrigin() {
		return Origin;
	}

	public void setOrigin(String origin) {
		Origin = origin;
	}

	public String getProxyAuthorization() {
		return ProxyAuthorization;
	}

	public void setProxyAuthorization(String proxyAuthorization) {
		ProxyAuthorization = proxyAuthorization;
	}

	public String getProxyAuthenticate() {
		return ProxyAuthenticate;
	}

	public void setProxyAuthenticate(String proxyAuthenticate) {
		ProxyAuthenticate = proxyAuthenticate;
	}

	public String getReferer() {
		return Referer;
	}

	public void setReferer(String referer) {
		Referer = referer;
	}

	public String getUserAgent() {
		return UserAgent;
	}

	public void setUserAgent(String userAgent) {
		UserAgent = userAgent;
	}

}