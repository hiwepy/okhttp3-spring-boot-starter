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

import com.google.common.net.HttpHeaders;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;

/**
 * Implementation of a intercepter to compress http's body using GZIP.
 *
 * @author fujian1115 [at] gmail.com
 */
public class GzipRequestInterceptor implements ApplicationInterceptor {

    private AtomicBoolean enabled = new AtomicBoolean(false);

    public GzipRequestInterceptor(GzipRequestProperties gzipProperties) {
		enabled.set(gzipProperties.isEnabled());
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
	public Response intercept(Interceptor.Chain chain) throws IOException {

		 if (!enabled.get()) {
            return chain.proceed(chain.request());
        }

        Request originalRequest = chain.request();
        RequestBody body = originalRequest.body();
        
        if (body == null || originalRequest.header(HttpHeaders.CONTENT_ENCODING) != null) {
            return chain.proceed(originalRequest);
        }

        Request compressedRequest = originalRequest.newBuilder().header(HttpHeaders.CONTENT_ENCODING, "gzip")
                .method(originalRequest.method(), gzip(body)).build();
        return chain.proceed(compressedRequest);
        
	}

	private RequestBody gzip(final RequestBody body) {
		
		return new RequestBody() {
			
			@Override
			public MediaType contentType() {
				return body.contentType();
			}

			@Override
			public long contentLength() {
				return -1; // We don't know the compressed length in advance!
			}

			@Override
			public void writeTo(BufferedSink sink) throws IOException {
				BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
				body.writeTo(gzipSink);
				gzipSink.close();
			}
			
		};
	}
}