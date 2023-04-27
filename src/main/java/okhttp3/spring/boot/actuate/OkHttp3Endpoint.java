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
package okhttp3.spring.boot.actuate;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Collectors;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import okhttp3.spring.boot.OkHttp3Metrics;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

/**
 * {@link Endpoint} to expose OkHttp3 Metrics.
 * @author 		： <a href="https://github.com/hiwepy">hiwepy</a>
 */
@Endpoint(id = "okhttp3")
public class OkHttp3Endpoint {
	
	private MeterRegistry registry;

    public OkHttp3Endpoint(MeterRegistry registry) {
        this.registry = registry;
    }
    
    @ReadOperation
    public Map<String, Object> okHttp3Metrics() {
    	 Map<String, Object> info = new HashMap<>();
         info.put("okhttp3", "http://square.github.io/okhttp/");
         info.put("metrics", getMetrics());
		return info;
	}
    
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        //gauge
        registry.getMeters().stream().filter(meter -> meter.getId().getName()
                .startsWith(OkHttp3Metrics.OKHTTP3_METRIC_NAME_PREFIX) && meter.getId().getType().equals(Gauge.class)

        ).collect(Collectors.toList());


        SortedMap<String, Gauge> gauges = registry.getGauges((name, metric) -> name.startsWith("okhttp3.OkHttpClient."));
        for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
            metrics.put(entry.getKey(), entry.getValue().getValue());
        }
        //timer
        SortedMap<String, com.codahale.metrics.Timer> timers = registry.getTimers((name, metric) -> name.startsWith("okhttp3.OkHttpClient."));
        for (Map.Entry<String, Timer> entry : timers.entrySet()) {
            metrics.putAll(convertTimerToMap(entry.getKey(), entry.getValue()));
        }
        return metrics;
    }

    public Map<String, Object> convertTimerToMap(String name, Timer timer) {
        Map<String, Object> map = new HashMap<>();
        map.put(name + ".count", timer.getCount());
        map.put(name + ".oneMinuteRate", timer.getOneMinuteRate());
        map.put(name + ".fiveMinuteRate", timer.getFiveMinuteRate());
        map.put(name + ".fifteenMinuteRate", timer.getFifteenMinuteRate());
        map.put(name + ".meanRate", timer.getMeanRate());
        HistogramSnapshot snapshot = timer.takeSnapshot();
        map.put(name + ".snapshot.mean", snapshot.mean());
        map.put(name + ".snapshot.max", snapshot.max());
        map.put(name + ".snapshot.min", snapshot.());
        map.put(name + ".snapshot.median", snapshot.getMedian());
        map.put(name + ".snapshot.stdDev", snapshot.getStdDev());
        map.put(name + ".snapshot.75thPercentile", snapshot.get75thPercentile());
        map.put(name + ".snapshot.95thPercentile", snapshot.get95thPercentile());
        map.put(name + ".snapshot.98thPercentile", snapshot.get98thPercentile());
        map.put(name + ".snapshot.99thPercentile", snapshot.get99thPercentile());
        map.put(name + ".snapshot.999thPercentile", snapshot.get999thPercentile());
        return map;
    }
	
	

	 
}
