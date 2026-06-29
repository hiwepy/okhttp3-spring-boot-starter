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
import java.io.InterruptedIOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
public class RequestRetryIntercepter implements RequestInterceptor {
	
	private int maxRetry; // 最大重试次数，假如设置为3次重试的话，则最大可能请求4次（默认1次+3次重试）
    private long retryInterval;// 重试的间隔
    private AtomicBoolean enabled = new AtomicBoolean(false);
    private final Cache<String, Integer> cache;
    private final String RETRY_ID_TMP = "retryId-%s";
    
    public RequestRetryIntercepter(int maxRetry, long retryInterval) {
        this.maxRetry = maxRetry;
        this.retryInterval = retryInterval;
        this.enabled.set(maxRetry > 0);
        this.cache = CacheBuilder.newBuilder()
        		.initialCapacity(10)
        		.removalListener(new RemovalListener<String, Integer>() {

					@Override
					public void onRemoval(RemovalNotification<String, Integer> notification) {
						log.debug("Remove Cache : {}", notification.getKey());
					}
        			
				})
        		.expireAfterAccess(1, TimeUnit.HOURS)
        		.build();
    }

    @SuppressWarnings("resource")
	@Override
    public Response intercept(Chain chain) throws IOException {
    	
    	// 1、检查retry是否开启
    	if (!enabled.get()) {
            return chain.proceed(chain.request());
        }
    	
    	Request request = chain.request();
        Response response = doRequest(chain, request);
        String retryId = String.format(RETRY_ID_TMP, request.hashCode());
        Integer retryNum = cache.getIfPresent(retryId);  
        if(Objects.isNull(retryNum)) {
        	retryNum = 0;
        }
        
        // 2、执行重试
        while ((response == null || !response.isSuccessful()) && retryNum < maxRetry) {
            log.info("intercept Request is not successful - {}", retryNum);
            final long nextInterval = getRetryInterval();
        	try {
                log.info("Wait for {}", nextInterval);
                Thread.sleep(nextInterval);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InterruptedIOException();
            }
        	retryNum ++;
        	cache.put(retryId, retryNum);
            // retry the request
            response = doRequest(chain, request);
        }
        // 3、失败超过重试或者重试成功，则移除
        if(response.isSuccessful()) {
        	cache.invalidate(retryId);
        }
        return response;
    }
    
    private Response doRequest(Chain chain, Request request) {
        Response response = null;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
        }
        return response;
    }
    
    /**
     * retry间隔时间
     */
    public long getRetryInterval() {
        return this.retryInterval;
    }
    
}
