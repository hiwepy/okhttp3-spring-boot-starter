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
package okhttp3.spring.boot.cache;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 *  持久化Cookie，运行时缓存了Cookie，当App退出的时候Cookie就不存在了
 * @author 		： <a href="https://github.com/hiwepy">hiwepy</a>
 */
public class PersistenceCookieJar implements CookieJar {
    
	List<Cookie> cache = new ArrayList<>();

    /*
     * Http请求结束，Response中有Cookie时候回调
     */
    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        // 内存中缓存Cookie
        cache.addAll(cookies);
    }

    /*
     * Http发送请求前回调，Request中设置Cookie
     */
    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        // 过期的Cookie
        List<Cookie> invalidCookies = new ArrayList<>();
        // 有效的Cookie
        List<Cookie> validCookies = new ArrayList<>();
        for (Cookie cookie : cache) {
            if (cookie.expiresAt() < System.currentTimeMillis()) {
                //判断是否过期
                invalidCookies.add(cookie);
            } else if (cookie.matches(url)) {
                //匹配Cookie对应url
                validCookies.add(cookie);
            }
        }
        // 缓存中移除过期的Cookie
        cache.removeAll(invalidCookies);
        // 返回List<Cookie>让Request进行设置
        return validCookies;
    }
    
}
