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
package okhttp3.spring.boot.cookie;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *  持久化Cookie，运行时缓存了Cookie，当App退出的时候Cookie就不存在了
 * @author 		： <a href="https://github.com/hiwepy">hiwepy</a>
 */
@Slf4j
public class CaffeineCacheCookieJar implements CookieJar {

    protected Cache<String, List<Cookie>> cookieCache;

    public CaffeineCacheCookieJar(long maximumSize, Duration expireAfterWrite, Duration expireAfterAccess) {
        this.cookieCache = Caffeine.newBuilder()
                .initialCapacity(10)
                .maximumSize(maximumSize)
                .removalListener(new RemovalListener<String, List<Cookie>>() {

                    @Override
                    public void onRemoval(@Nullable String host, @Nullable List<Cookie> value, @NonNull RemovalCause cause) {
                        log.debug("Remove Cookie Cache : {}", host);
                    }

                })
                .expireAfterWrite(expireAfterWrite)
                .expireAfterAccess(expireAfterAccess)
                .build();
    }


    /*
     * Http请求结束，Response中有Cookie时候回调
     */
    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        cookieCache.put(url.host(), cookies);
    }

    /*
     * Http发送请求前回调，Request中设置Cookie
     */
    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        // 从缓存中获取Cookie
        List<Cookie> cookies = cookieCache.getIfPresent(url.host());
        if (Objects.nonNull(cookies)) {
            List<Cookie> newCookies = new ArrayList<>();
            // 移除过期的Cookie
            for (Cookie cookie : cookies) {
                if (cookie.expiresAt() >= System.currentTimeMillis()) {
                    newCookies.add(cookie);
                }
            }
            // 更新缓存缓存中的Cookie
            cookieCache.put(url.host(), newCookies);
            return newCookies;
        }
        return Collections.emptyList();
    }

}
