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

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;

/**
 * 请求头拦截器：动态增加请求头
 * @author 		： <a href="https://github.com/hiwepy">hiwepy</a>
 */
@Slf4j
public class RequestHeaderInterceptor implements RequestInterceptor  {

    private AtomicBoolean enabled = new AtomicBoolean(false);

	private RequestHeaderProperties headerProperties;
	
	public RequestHeaderInterceptor(RequestHeaderProperties headerProperties) {
		this.headerProperties = headerProperties;
		enabled.set(this.headerProperties.isEnabled());
	}
	
	public void enable() {
        enabled.set(true);
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public void disable() {
        enabled.set(false);
    }
    
	@Override
	public Response intercept(Chain chain) throws IOException {
		
		if (!enabled.get()) {
            return chain.proceed(chain.request());
        }
	 
		Request originalRequest = chain.request();
		Builder builder = originalRequest.newBuilder();
		builder = this.setHeader(originalRequest, builder, HttpHeaders.ACCEPT, headerProperties.getAccept());
		builder = this.setHeader(originalRequest, builder, HttpHeaders.ACCEPT_CHARSET, headerProperties.getAcceptCharset());
		builder = this.setHeader(originalRequest, builder, HttpHeaders.ACCEPT_ENCODING, headerProperties.getAcceptEncoding());
		builder = this.setHeader(originalRequest, builder, HttpHeaders.ACCEPT_LANGUAGE, headerProperties.getAcceptLanguage());
		builder = this.setHeader(originalRequest, builder, HttpHeaders.ACCEPT_RANGES, headerProperties.getAcceptRanges());
		builder = this.setHeader(originalRequest, builder, HttpHeaders.AUTHORIZATION, headerProperties.getAuthorization());
		builder = this.setHeader(originalRequest, builder, HttpHeaders.CONNECTION, headerProperties.getConnection());
		builder = this.setHeader(originalRequest, builder, HttpHeaders.HOST, headerProperties.getHost());
		builder = this.setHeader(originalRequest, builder, HttpHeaders.ORIGIN, headerProperties.getOrigin());
		builder = this.setHeader(originalRequest, builder, HttpHeaders.PROXY_AUTHENTICATE, headerProperties.getProxyAuthenticate());
		builder = this.setHeader(originalRequest, builder, HttpHeaders.PROXY_AUTHORIZATION, headerProperties.getProxyAuthorization());
		builder = this.setHeader(originalRequest, builder, HttpHeaders.REFERER, headerProperties.getReferer());
		builder = this.setHeader(originalRequest, builder, HttpHeaders.USER_AGENT, headerProperties.getUserAgent());
		return chain.proceed(builder.build());
	}

	protected Builder setHeader(Request request, okhttp3.Request.Builder builder, String key, String value) {
		if(StringUtils.hasText(value)) {
			boolean match = request.headers().names().stream().anyMatch(item -> item.equalsIgnoreCase(key));
			if(!match) {
				if(log.isDebugEnabled()){
					log.debug("Set HTTP HEADER: {}:{}.", key, value);
				}
				return builder.header(key, value);
			}
		}
		return builder;
	}
	
}
