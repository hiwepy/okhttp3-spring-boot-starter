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
package okhttp3.spring.boot;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * OkHttp3 常规请求模板
 * @author ： <a href="https://github.com/hiwepy">wandl</a>
 */
public class OkHttp3Template implements InitializingBean {

	protected OkHttpClient okHttpClient;
	protected String baseUrl;

	public OkHttp3Template() {
	}

	public OkHttp3Template(OkHttpClient okHttpClient) {
		this.okHttpClient = okHttpClient;
	}

	public OkHttp3Template(OkHttpClient okHttpClient, String baseUrl) {
		this.okHttpClient = okHttpClient;
		this.baseUrl = baseUrl;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// 请求编码，默认：UTF-8
		// String charset = properties.getProperty(HTTP_CHARSET, "UTF-8");
		if (okHttpClient == null) {
			// 1.创建OkHttpClient对象
			okHttpClient = new OkHttpClient().newBuilder().connectTimeout(5000, TimeUnit.MILLISECONDS)
					// .hostnameVerifier(okhttpHostnameVerifier)
					// .followRedirects(properties.isFollowRedirects())
					// .followSslRedirects(properties.isFollowSslRedirects())
					.pingInterval(1, TimeUnit.MILLISECONDS).readTimeout(3000, TimeUnit.MILLISECONDS)
					.retryOnConnectionFailure(true)
					// .sslSocketFactory(trustedSSLSocketFactory, trustManager)
					.writeTimeout(3, TimeUnit.SECONDS)
					// Application Interceptors、Network Interceptors :
					// https://segmentfault.com/a/1190000013164260
					// .addNetworkInterceptor(loggingInterceptor)
					// .addInterceptor(headerInterceptor)
					.build();
		}
	}

	public void post(String url, Function<FormBody.Builder, RequestBody> body,
			final Function<Response, Boolean> success) {
		this.post(url, body, success, (call, e) -> {
			return false;
		});
	}

	public void post(String url, Function<FormBody.Builder, RequestBody> body,
			final Function<Response, Boolean> success, final BiFunction<Call, IOException, Boolean> failure) {
		
		// 1.创建RequestBody对象
		RequestBody formBody = body.apply(new FormBody.Builder());
		// 2.创建Request对象，设置一个url地址,设置请求方式。
		Request request = new Request.Builder().url(this.joinPath(url)).post(formBody).build();
		// 3.创建一个call对象,参数就是Request请求对象
		Call call = okHttpClient.newCall(request);
		// 4.请求加入调度，重写回调方法
		call.enqueue(new Callback() {
			
			// 请求失败执行的方法
			@Override
			public void onFailure(Call call, IOException e) {
				failure.apply(call, e);
			}

			// 请求成功执行的方法
			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (response.isSuccessful()) {
					success.apply(response);
				}
			}
			
		});
	}

	public <T> void get(String url, final Function<Response, T> success) {
		this.get(url, null, success, (call, e) -> {
			return null;
		});
	}

	public <T> void get(String url, Map<String, Object> params, final Function<Response, T> success) {
		this.get(url, params, success, (call, e) -> {
			return null;
		});
	}

	public <T> void get(String url, Map<String, Object> params, final Function<Response, T> success,
			final BiFunction<Call, IOException, Boolean> failure) {
		this.get(url, params, new HashMap<String, Object>(), success, failure);
	}

	public <T> void get(String url, Map<String, Object> params, Map<String, Object> headers,
			final Function<Response, T> success, final BiFunction<Call, IOException, Boolean> failure) {

		// 1.创建Request对象，设置一个url地址,设置请求方式。
		Request.Builder builder = new Request.Builder()
				.url(CollectionUtils.isEmpty(params) ? this.joinPath(url) : this.getGetHttpURL( this.joinPath(url) , params)).get();
		for (Entry<String, Object> entry : headers.entrySet()) {
			builder.addHeader(entry.getKey(), String.valueOf(entry.getValue()));
		}
		// 2.创建一个call对象,参数就是Request请求对象
		Call call = okHttpClient.newCall(builder.build());
		// 3.请求加入调度，重写回调方法
		call.enqueue(new Callback() {
			
			// 请求失败执行的方法
			@Override
			public void onFailure(Call call, IOException e) {
				failure.apply(call, e);
			}

			// 请求成功执行的方法
			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (response.isSuccessful()) {
					success.apply(response);
				}
			}
		});
	}

	protected String getGetHttpURL(String httpUrl, Map<String, Object> httpArg) {
		StringBuffer paramsBuffer = new StringBuffer();
		if (httpArg != null && httpArg.size() > 0) {
			Set<String> set = httpArg.keySet();
			Iterator<String> it = set.iterator();
			int count = 0;
			while (it.hasNext()) {
				String key = it.next();
				Object value = httpArg.get(key);
				if (count > 0) {
					paramsBuffer.append("&");
				}
				// 内容转码由调用者实现
				// paramsBuffer.append(key + "=" + URLEncoder.encode(value, "UTF-8"));
				paramsBuffer.append(key + "=" + value);
				count++;
			}
		}
		String params = paramsBuffer.toString();
		if (params != null && !"".equals(params.trim())) {
			if (httpUrl.indexOf("?") == -1) {
				httpUrl = httpUrl + "?" + params;
			} else if (httpUrl.indexOf("?") == httpUrl.length() - 1) {
				httpUrl = httpUrl + params;
			} else {
				httpUrl = httpUrl + "&" + params;
			}
		}
		return httpUrl;
	}

	/**
	 * 字符串拼接
	 * @param url
	 * @return
	 */
	protected String joinPath(String url) {
		if (StringUtils.hasText(baseUrl)) {
			String address;
			if (!baseUrl.endsWith("/")) {
				address = baseUrl + "/" + url;
			}
			address = baseUrl + url;
			return address;
		}
		return url;
	}

}
