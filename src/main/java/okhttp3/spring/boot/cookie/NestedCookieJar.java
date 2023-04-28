package okhttp3.spring.boot.cookie;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class NestedCookieJar implements CookieJar {

    private List<CookieJar> cookieJars;

    public NestedCookieJar(List<CookieJar> cookieJars) {
        this.cookieJars = cookieJars;
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        if (CollectionUtils.isEmpty(cookieJars)) {
            return;
        }
        for (CookieJar cookieJar : cookieJars) {
            try {
                cookieJar.saveFromResponse(url, cookies);
            } catch (Exception e) {
                log.error("saveFromResponse error", e);
            }
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        // 如果没有CookieJar实现，直接返回空
        if (CollectionUtils.isEmpty(cookieJars)) {
            return Collections.emptyList();
        }
        // 从所有CookieJar中获取Cookie
        Map<String, Cookie> cookieMap = new HashMap<>();
        for (CookieJar cookieJar : cookieJars) {
            try {
                // 从CookieJar中获取Cookie
                List<Cookie> cookies = cookieJar.loadForRequest(url);
                // 如果Cookie为空，跳过
                if (CollectionUtils.isEmpty(cookies)) {
                    continue;
                }
                // 移除过期的Cookie
                for (Cookie cookie : cookies) {
                    if (cookie.matches(url) && cookie.expiresAt() >= System.currentTimeMillis()) {
                        cookieMap.put(cookie.name(), cookie);
                    }
                }
            } catch (Exception e) {
                log.error("loadForRequest error", e);
            }
        }
        if (CollectionUtils.isEmpty(cookieMap)) {
            return Collections.emptyList();
        }
        return cookieMap.values().stream().collect(Collectors.toList());
    }

}
