package okhttp3.spring.boot.actuate;


import static com.codahale.metrics.MetricRegistry.name;

import java.io.IOException;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * okhttp3 metrics interceptor
 * @author linux_china
 */
public class OkHttp3MetricsInterceptor implements Interceptor {
	
	private MetricRegistry registry;

    public OkHttp3MetricsInterceptor(MetricRegistry registry) {
        this.registry = registry;
    }
    
    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        String host = request.url().host();
        Response response;
        final Timer timer = registry.timer(name(OkHttpClient.class, host, request.method()));
        final Timer.Context context = timer.time();
        try {
            response = chain.proceed(request);
        } finally {
            context.stop();
        }
        return response;
    }
    
}