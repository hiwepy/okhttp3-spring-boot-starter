package okhttp3.spring.boot.metrics;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.*;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.spring.boot.OkHttp3Metrics;
import okhttp3.spring.boot.metrics.MetricNames;
import org.springframework.core.annotation.Order;

/**
 * An {@link Interceptor} that monitors the number of submitted, running, and completed network
 * requests. Also, keeps a {@link Timer} for the request duration.
 */
@Order(Integer.MIN_VALUE)
public class OkHttp3MetricsInterceptor implements Interceptor {
	
	private MeterRegistry registry;
    private final Counter submitted;
    private final Counter running;
    private final Counter completed;
    private final Timer duration;

    public OkHttp3MetricsInterceptor(MeterRegistry registry) {
        this.registry = registry;
        this.submitted = registry.counter(OkHttp3Metrics.METRIC_NAME_NETWORK_REQUESTS_SUBMITTED);
        this.running = registry.counter(OkHttp3Metrics.METRIC_NAME_NETWORK_REQUESTS_RUNNING);
        this.completed = registry.counter(OkHttp3Metrics.METRIC_NAME_NETWORK_REQUESTS_COMPLETED);
        this.duration = registry.timer(OkHttp3Metrics.METRIC_NAME_NETWORK_REQUESTS_DURATION);
    }
    
    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        // 记录请求开始时间
        long start = System.currentTimeMillis();
        // 一次请求计数 +1
        submitted.increment();
        // 当前正在运行的请求数 +1
        running.increment();
        // 获取请求
        Request request = chain.request();
        // 获取本次请求对应的度量指标名称
        String metric = MetricNames.name(OkHttp3Metrics.OKHTTP3_REQUEST_METRIC_NAME_PREFIX, request.url().host(), request.method());
        // 获取度量指标
        Timer timer = registry.timer(metric);
        Response response;
        try {
            response = chain.proceed(request);
        } finally {
            // 记录本次请求耗时
            timer.record(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
            duration.record(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
            // 当前正在运行的请求数 -1
            running.increment(-1);
            // 当前已完成的请求数 +1
            completed.increment();
        }
        return response;
    }
    
}