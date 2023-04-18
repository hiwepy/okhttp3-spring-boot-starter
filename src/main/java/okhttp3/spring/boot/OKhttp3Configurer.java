package okhttp3.spring.boot;

import okhttp3.ConnectionPool;

public interface OKhttp3Configurer {

    void configure(ConnectionPool connectionPool);

    void configure(okhttp3.OkHttpClient.Builder builder);

}
