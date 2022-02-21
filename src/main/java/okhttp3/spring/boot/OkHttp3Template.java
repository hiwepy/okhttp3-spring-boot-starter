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
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * OkHttp3 常规请求模板
 *
 * @author ： <a href="https://github.com/hiwepy">wandl</a>
 */
@Slf4j
public class OkHttp3Template implements InitializingBean {

	public final static String APPLICATION_JSON_VALUE = "application/json";
	public final static String APPLICATION_JSON_UTF8_VALUE = "application/json;charset=UTF-8";
	public final static MediaType APPLICATION_JSON = MediaType.parse(APPLICATION_JSON_VALUE);
	public final static MediaType APPLICATION_JSON_UTF8 = MediaType.parse(APPLICATION_JSON_UTF8_VALUE);

	public static int TRY_MAX = 5;

	protected OkHttpClient okhttp3Client;
	protected ObjectMapper objectMapper;
	protected String baseUrl;

	public OkHttp3Template() {
	}

	public OkHttp3Template(OkHttpClient okhttp3Client, ObjectMapper objectMapper) {
		this.okhttp3Client = okhttp3Client;
		this.objectMapper = objectMapper;
	}

	public OkHttp3Template(OkHttpClient okhttp3Client, ObjectMapper objectMapper, String baseUrl) {
		this.okhttp3Client = okhttp3Client;
		this.objectMapper = objectMapper;
		this.baseUrl = baseUrl;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// 请求编码，默认：UTF-8
		// String charset = properties.getProperty(HTTP_CHARSET, "UTF-8");
		if (okhttp3Client == null) {
			// 1.创建OkHttpClient对象
			okhttp3Client = new OkHttpClient().newBuilder().connectTimeout(5000, TimeUnit.MILLISECONDS)
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
		Call call = okhttp3Client.newCall(request);
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
	
	public <T extends Okhttp3Response> T post(
			String url,
			Class<T> rtClass,
			Map<String, Object> headers,
			Map<String, Object> params,
			Object bodyObj,
			Function<Response, T> success,
			BiFunction<Call, IOException, Boolean> failure) {
		long startTime = System.currentTimeMillis();

		Request.Builder builder = null;
		
		HttpUrl httpUrl = this.getHttpUrl(this.joinPath(url), params);
		log.info("OkHttp3 >> Request Url : {}", httpUrl.query());
		if(Objects.nonNull(bodyObj)) {
			String bodyStr = objectMapper.writeValueAsString(bodyObj);
			log.info("OkHttp3 >> Request Body : {}", bodyStr);
			RequestBody requestBody = RequestBody.create(APPLICATION_JSON_UTF8, bodyStr);
			builder = new Request.Builder()
					.url(httpUrl)
					.post(requestBody);
		} else {
			builder = new Request.Builder()
					.url(httpUrl)
					.post();
		}
		
		
		if(Objects.nonNull(headers)) {
			log.info("OkHttp3 >> Request Headers : {}", headers);
			for (Entry<String, Object> entry : headers.entrySet()) {
				builder.addHeader(entry.getKey(), String.valueOf(entry.getValue()));
			}
		}
		
		// 2.创建一个call对象,参数就是Request请求对象
		T res = null;
		try {
			try(Response response = this.syncRequest(startTime, builder.build());) {
				if (response.isSuccessful()) {
					String body = response.body().string();
					log.info("OkHttp3 Request Success : url : {}, method : {}, params : {}, code : {}, body : {} , use time : {} ", method, url, params, response.code(), body , System.currentTimeMillis() - startTime);
					res = this.readValue(body, rtClass);
	            } else {
	            	log.error("OkHttp3 Request Failure : url : {}, params : {}, code : {}, message : {}, use time : {} ", url, params, response.code(), response.message(), System.currentTimeMillis() - startTime);
	            	res = BeanUtils.instantiateClass(rtClass);
				}
				res.setCode(response.code());
			}
		} catch (Exception e) {
			log.error("OkHttp3 Request Error : url : {}, params : {}, use time : {} ,  {}", url, params, e.getMessage(), System.currentTimeMillis() - startTime);
			res = BeanUtils.instantiateClass(rtClass);
			res.setCode(500);
		}
		return res;
	}
	
	
	protected <T extends Okhttp3Response> void post(String url,
			Map<String, Object> headers, 
			Map<String, Object> params, 
			Class<T> cls, 
			Consumer<T> consumer) {
		long startTime = System.currentTimeMillis();
		
		
		
		
		this.asyncRequest(url, params, (response) -> {
			if (response.isSuccessful()) {
				try {
					String body = response.body().string();
					T res = this.readValue(body, cls);
					res.setCode(response.code());
					if (res.isSuccess()) {
						log.info("OkHttp3 {} >> Success, url : {}, params : {}, Code : {}, Body : {}", address.getOpt(),
								url, params, res.getCode(), body);
					} else {
						log.error("OkHttp3 {} >> Failure, url : {}, params : {}, Code : {}", address.getOpt(), url,
								params, res.getCode());
					}
					consumer.accept(res);
				} catch (IOException e) {
					log.error("OkHttp3 {} >> Response Parse Error : {}", address.getOpt(), e.getMessage());
					T res = BeanUtils.instantiateClass(cls);
					consumer.accept(res);
				}
			} else {
				T res = BeanUtils.instantiateClass(cls);
				res.setCode(response.code());
				consumer.accept(res);
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

	public <T> void get(String url, 
			Map<String, Object> params,
			Function<Response, T> success,
			final BiFunction<Call, IOException, Boolean> failure) {
		this.get(url, params, null, success, failure);
	}

	public <T extends Okhttp3Response> T get(
			String url,
			Class<T> rtClass,
			Map<String, Object> headers,
			Map<String, Object> params,
			Object bodyObj,
			Function<Response, T> success,
			BiFunction<Call, IOException, Boolean> failure) {
		long startTime = System.currentTimeMillis();
		HttpUrl httpUrl = this.getHttpUrl(this.joinPath(url), params);
		log.info("OkHttp3 >> Request Url : {}", httpUrl.query());
		Request.Builder builder = builder = new Request.Builder().url(httpUrl).get();
		
		
		if(Objects.nonNull(headers)) {
			log.info("OkHttp3 >> Request Headers : {}", headers);
			for (Entry<String, Object> entry : headers.entrySet()) {
				builder.addHeader(entry.getKey(), String.valueOf(entry.getValue()));
			}
		}
		// 2.创建一个call对象,参数就是Request请求对象
		T res = null;
		try {
			try(Response response = this.syncRequest(startTime, builder.build());) {
				if (response.isSuccessful()) {
					String body = response.body().string();
					log.info("OkHttp3 Request Success : url : {}, method : {}, params : {}, code : {}, body : {} , use time : {} ", method, url, params, response.code(), body , System.currentTimeMillis() - startTime);
					res = this.readValue(body, rtClass);
	            } else {
	            	log.error("OkHttp3 Request Failure : url : {}, params : {}, code : {}, message : {}, use time : {} ", url, params, response.code(), response.message(), System.currentTimeMillis() - startTime);
	            	res = BeanUtils.instantiateClass(rtClass);
				}
				res.setCode(response.code());
			}
		} catch (Exception e) {
			log.error("OkHttp3 Request Error : url : {}, params : {}, use time : {} ,  {}", url, params, e.getMessage(), System.currentTimeMillis() - startTime);
			res = BeanUtils.instantiateClass(rtClass);
			res.setCode(500);
		}
		return res;
	}
	
	public <T extends Okhttp3Response> void asyncGet(
			String url,
			Class<T> rtClass,
			Map<String, Object> headers,
			Map<String, Object> params,
			Consumer<T> success,
			BiFunction<Call, IOException, Boolean> failure, 
			Consumer<Exception> error) {
		long startTime = System.currentTimeMillis();
		// 1.创建Request对象，设置一个url地址,设置请求方式。
		HttpUrl httpUrl = this.getHttpUrl(this.joinPath(url), params); 
		Request.Builder builder = new Request.Builder().url(httpUrl).get();
		for (Entry<String, Object> entry : headers.entrySet()) {
			builder.addHeader(entry.getKey(), String.valueOf(entry.getValue()));
		}
		// 2.创建一个call对象,参数就是Request请求对象
		this.asyncRequest(startTime, builder.build(), (response) -> {
			if (response.isSuccessful()) {
				try {
					String body = response.body().string();
					T res = this.readValue(body, rtClass);
					res.setCode(response.code());
					if (res.isSuccess()) {
						log.info("OkHttp3 Response Success, url : {}, params : {}, Code : {}, Body : {}", 
								url, params, res.getCode(), body);
					} else {
						log.error("OkHttp3 Response Failure, url : {}, params : {}, Code : {}", url,
								params, res.getCode());
					}
					success.accept(res);
				} catch (IOException e) {
					log.error("OkHttp3 Response Parse Error : {}", e.getMessage());
					T res = BeanUtils.instantiateClass(rtClass);
					success.accept(res);
				}
			} else {
				T res = BeanUtils.instantiateClass(rtClass);
				res.setCode(response.code());
				success.accept(res);
			}
		}, failure, error);

 
	}
	
	
	public <T extends Okhttp3Response> T syncRequest(
			long startTime,
			HttpUrl httpUrl,
			Class<T> rtClass,
			Map<String, Object> headers,
			Map<String, Object> params,
			Function<Response, T> success,
			BiFunction<Call, IOException, Boolean> failure) {
		log.info("OkHttp3 >> Request Url : {}", httpUrl.query());
		Request.Builder builder = builder = new Request.Builder().url(httpUrl).get();
		if(Objects.nonNull(headers)) {
			log.info("OkHttp3 >> Request Headers : {}", headers);
			for (Entry<String, Object> entry : headers.entrySet()) {
				builder.addHeader(entry.getKey(), String.valueOf(entry.getValue()));
			}
		}
		// 2.创建一个call对象,参数就是Request请求对象
		T res = null;
		try {
			try(Response response = okhttp3Client.newCall(builder.build()).execute();) {
				if (response.isSuccessful()) {
					String body = response.body().string();
					log.info("OkHttp3 >> Request Success : url : {}, method : {}, params : {}, code : {}, body : {} , use time : {} ", method, url, params, response.code(), body , System.currentTimeMillis() - startTime);
					res = this.readValue(body, rtClass);
	            } else {
	            	log.error("OkHttp3 >> Request Failure : url : {}, params : {}, code : {}, message : {}, use time : {} ", url, params, response.code(), response.message(), System.currentTimeMillis() - startTime);
	            	res = BeanUtils.instantiateClass(rtClass);
				}
				res.setCode(response.code());
			}
		} catch (Exception e) {
			log.error("OkHttp3 Request Error : url : {}, params : {}, use time : {} ,  {}", url, params, e.getMessage(), System.currentTimeMillis() - startTime);
			res = BeanUtils.instantiateClass(rtClass);
			res.setCode(500);
		}
		return res;
	}
	
	
	protected void asyncRequest(long startTime, 
			Request request,
			Consumer<Response> success,
			BiFunction<Call, IOException, Boolean> failure, 
			Consumer<Exception> error) {
		try {

			okhttp3Client.newCall(request).enqueue(new Callback() {

				@Override
				public void onFailure(Call call, IOException e) {
					log.error("OkHttp3 Async Request Failure : url : {}, message : {}, use time : {} ",
							request.url().toString(), e.getMessage(), System.currentTimeMillis() - startTime);
					if (Objects.nonNull(failure)) {
						failure.apply(call, e);
					}
				}

				@Override
				public void onResponse(Call call, Response response) {
					if (response.isSuccessful()) {
						log.info("OkHttp3 Async Request Success : url : {}, code : {}, message : {} , use time : {} ",
								request.url().toString(), response.code(), response.message(),
								System.currentTimeMillis() - startTime);
						if (Objects.nonNull(success)) {
							success.accept(response);
						}
					} else {
						log.error("OkHttp3 Async Request Failure : url : {}, code : {}, message : {}, use time : {} ",
								request.url().toString(), response.code(), response.message(),
								System.currentTimeMillis() - startTime);
					}
				}

			});
		} catch (Exception e) {
			log.error("OkHttp3 Async Request Error : url : {}, message : {} , use time : {} ", request.url().toString(),
					e.getMessage(), System.currentTimeMillis() - startTime);
			if (Objects.nonNull(error)) {
				error.accept(e);
			}
		}
	}
	

	protected HttpUrl getHttpUrl(String httpUrl, Map<String, Object> params) {
		HttpUrl.Builder urlBuilder = HttpUrl.parse(httpUrl).newBuilder();
		if (CollectionUtils.isEmpty(params)) {
			return urlBuilder.build();
		}
		if (!CollectionUtils.isEmpty(params)) {
			log.info("OkHttp3 >> Request Params : {}", params);
			Iterator<Entry<String, Object>> it = params.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, Object> entry = it.next();
				urlBuilder.addQueryParameter(entry.getKey(),
						Objects.isNull(entry.getValue()) ? "" : entry.getValue().toString());
			}
		}
		return urlBuilder.build();
	}

	/**
	 * 字符串拼接
	 *
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

	public <T> T readValue(String json, Class<T> cls) {
		try {
			return objectMapper.readValue(json, cls);
		} catch (Exception e) {
			log.error(e.getMessage());
			return BeanUtils.instantiateClass(cls);
		}
	}

}
