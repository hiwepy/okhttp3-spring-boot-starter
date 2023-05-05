# okhttp3-spring-boot-starter

Spring Boot Starter For Okhttp 3.x

### 组件简介

 > 基于 okhttp 3.x 的 Spring Boot Starter 实现
 
- 部分代码参考了：https://github.com/linux-china/spring-boot-starter-okhttp3

### 使用说明

##### 1、Spring Boot 项目添加 Maven 依赖

``` xml
<dependency>
	<groupId>com.github.hiwepy</groupId>
	<artifactId>okhttp3-spring-boot-starter</artifactId>
	<version>1.1.2.RELEASE</version>
</dependency>
```

##### 2、在`application.yml`文件中增加如下配置

```yaml
################################################################################################################
###okhttp3基本配置：
################################################################################################################
okhttp3:
  # 连接超时时间，默认 10s
  connect-timeout: 5s
  # 读取超时时间，默认 10s
  read-timeout: 30s
  # 写入超时时间，默认 10s
  write-timeout: 30s
  # 连接失败后是否重试
  retry-on-connection-failure: false
  # 打印日志级别：NONE、BASIC、HEADERS、BODY
  log-level: HEADERS
  pool:
    # 最大空闲连接梳数量，超出该值后，连接用完后会被关闭，最多只会保留idleConnectionCount个连接数量
    max-idle-connections: 48
    # 最大瞬时处理连接数量
    max-requests: 128
    # 每个请求地址最大瞬时处理连接数量
    max-requests-per-host: 24
```

##### 3、使用示例

```java

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
	
	@Autowired
	private OkHttpClient okHttpClient;
	
	@PostConstruct
	public void test() throws IOException {
		
		//调用ok的get请求
       	Request request = new Request.Builder()
                .get()
                .url(url)
                .build();
       	//同步请求方式
	   	Response theResponse = okHttpClient.newCall(newRequest).execute();
	   	// 解析响应内容
	   	ResponseBody body = theResponse.body();
	   	// 响应头信息
	   	Headers headers = theResponse.headers();
	   	// 响应类型
	   	MediaType mediaType = body.contentType();
	   	// 成功状态
		if( theResponse.isSuccessful()) {
			// do something
		} 
		
	}
	
	
	public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);
	}

}
```

##### 4、集成 Prometheus 监控

项目中引入 micrometer-prometheus、okhttp3-metrics-prometheus 依赖，可实现 `OkHttp` 组件的指标采集

```xml
<dependency>
	<groupId>io.micrometer</groupId>
	<artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
<dependency>
    <groupId>com.github.hiwepy</groupId>
    <artifactId>okhttp3-metrics-prometheus</artifactId>
</dependency>
```

可采集到如下指标：

```markdown
# HELP okhttp3_pool_dispatcher_running_calls_count Total number of running calls
# TYPE okhttp3_pool_dispatcher_running_calls_count gauge
okhttp3_pool_dispatcher_running_calls_count{application="app-test",} 1.0
# HELP okhttp3_pool_dispatcher_queued_calls_count Total number of queued calls
# TYPE okhttp3_pool_dispatcher_queued_calls_count gauge
okhttp3_pool_dispatcher_queued_calls_count{application="app-test",} 0.0
# HELP okhttp3_pool_dispatcher_max_requests_perhost_total max requests of dispatcher by per host
# TYPE okhttp3_pool_dispatcher_max_requests_perhost_total counter
okhttp3_pool_dispatcher_max_requests_perhost_total{application="app-test",} 5.0
# HELP okhttp3_network_requests_completed_total
# TYPE okhttp3_network_requests_completed_total counter
okhttp3_network_requests_completed_total{application="app-test",} 410.0
# HELP okhttp3_requests_body_bytes_max
# TYPE okhttp3_requests_body_bytes_max gauge
okhttp3_requests_body_bytes_max{application="app-test",} 0.0
# HELP okhttp3_requests_body_bytes
# TYPE okhttp3_requests_body_bytes summary
okhttp3_requests_body_bytes_count{application="app-test",} 0.0
okhttp3_requests_body_bytes_sum{application="app-test",} 0.0
# HELP okhttp3_connections_started_total
# TYPE okhttp3_connections_started_total counter
okhttp3_connections_started_total{application="app-test",} 4.0
# HELP okhttp3_connections_released_total
# TYPE okhttp3_connections_released_total counter
okhttp3_connections_released_total{application="app-test",} 410.0
# HELP okhttp3_responses_failed_total
# TYPE okhttp3_responses_failed_total counter
okhttp3_responses_failed_total{application="app-test",} 0.0
# HELP okhttp3_responses_headers_end_total
# TYPE okhttp3_responses_headers_end_total counter
okhttp3_responses_headers_end_total{application="app-test",} 410.0
# HELP okhttp3_requests_body_started_total
# TYPE okhttp3_requests_body_started_total counter
okhttp3_requests_body_started_total{application="app-test",} 0.0
# HELP okhttp3_connections_end_total
# TYPE okhttp3_connections_end_total counter
okhttp3_connections_end_total{application="app-test",} 4.0
# HELP okhttp3_dns_duration_seconds
# TYPE okhttp3_dns_duration_seconds summary
okhttp3_dns_duration_seconds_count{application="app-test",} 0.0
okhttp3_dns_duration_seconds_sum{application="app-test",} 0.0
# HELP okhttp3_dns_duration_seconds_max
# TYPE okhttp3_dns_duration_seconds_max gauge
okhttp3_dns_duration_seconds_max{application="app-test",} 0.0
# HELP okhttp3_requests_failed_total
# TYPE okhttp3_requests_failed_total counter
okhttp3_requests_failed_total{application="app-test",} 0.0
# HELP okhttp3_pool_dispatcher_max_requests_total max requests of dispatcher
# TYPE okhttp3_pool_dispatcher_max_requests_total counter
okhttp3_pool_dispatcher_max_requests_total{application="app-test",} 64.0
# HELP okhttp3_connections_acquired_total
# TYPE okhttp3_connections_acquired_total counter
okhttp3_connections_acquired_total{application="app-test",} 410.0
# HELP okhttp3_calls_failed_total
# TYPE okhttp3_calls_failed_total counter
okhttp3_calls_failed_total{application="app-test",} 0.0
# HELP okhttp3_pool_connection_count_connections The state of connections in the OkHttp connection pool
# TYPE okhttp3_pool_connection_count_connections gauge
okhttp3_pool_connection_count_connections{application="app-test",state="active",} 0.0
okhttp3_pool_connection_count_connections{application="app-test",state="idle",} 2.0
# HELP okhttp3_dns_end_total
# TYPE okhttp3_dns_end_total counter
okhttp3_dns_end_total{application="app-test",} 4.0
# HELP okhttp3_connections_failed_total
# TYPE okhttp3_connections_failed_total counter
okhttp3_connections_failed_total{application="app-test",} 0.0
# HELP okhttp3_requests_seconds_max Timer of OkHttp operation
# TYPE okhttp3_requests_seconds_max gauge
okhttp3_requests_seconds_max{application="app-test",method="GET",status="302",target_host="baidu.com",target_port="443",target_scheme="https",uri="/",} 0.4752953
# HELP okhttp3_requests_seconds Timer of OkHttp operation
# TYPE okhttp3_requests_seconds summary
okhttp3_requests_seconds_count{application="app-test",method="GET",status="302",target_host="baidu.com",target_port="443",target_scheme="https",uri="/",} 205.0
okhttp3_requests_seconds_sum{application="app-test",method="GET",status="302",target_host="baidu.com",target_port="443",target_scheme="https",uri="/",} 8.9529481
# HELP okhttp3_calls_duration_seconds
# TYPE okhttp3_calls_duration_seconds summary
okhttp3_calls_duration_seconds_count{application="app-test",} 0.0
okhttp3_calls_duration_seconds_sum{application="app-test",} 0.0
# HELP okhttp3_calls_duration_seconds_max
# TYPE okhttp3_calls_duration_seconds_max gauge
okhttp3_calls_duration_seconds_max{application="app-test",} 0.0
# HELP okhttp3_responses_body_bytes_max
# TYPE okhttp3_responses_body_bytes_max gauge
okhttp3_responses_body_bytes_max{application="app-test",} 1142.0
# HELP okhttp3_responses_body_bytes
# TYPE okhttp3_responses_body_bytes summary
okhttp3_responses_body_bytes_count{application="app-test",} 410.0
okhttp3_responses_body_bytes_sum{application="app-test",} 234110.0
# HELP okhttp3_responses_body_end_total
# TYPE okhttp3_responses_body_end_total counter
okhttp3_responses_body_end_total{application="app-test",} 410.0
# HELP okhttp3_calls_started_total
# TYPE okhttp3_calls_started_total counter
okhttp3_calls_started_total{application="app-test",} 205.0
# HELP okhttp3_network_requests_submitted_total
# TYPE okhttp3_network_requests_submitted_total counter
okhttp3_network_requests_submitted_total{application="app-test",} 431.0
# HELP okhttp3_requests_body_end_total
# TYPE okhttp3_requests_body_end_total counter
okhttp3_requests_body_end_total{application="app-test",} 0.0
# HELP okhttp3_requests_headers_end_total
# TYPE okhttp3_requests_headers_end_total counter
okhttp3_requests_headers_end_total{application="app-test",} 431.0
# HELP okhttp3_requests_headers_started_total
# TYPE okhttp3_requests_headers_started_total counter
okhttp3_requests_headers_started_total{application="app-test",} 434.0
# HELP okhttp3_responses_headers_started_total
# TYPE okhttp3_responses_headers_started_total counter
okhttp3_responses_headers_started_total{application="app-test",} 434.0
# HELP okhttp3_responses_body_started_total
# TYPE okhttp3_responses_body_started_total counter
okhttp3_responses_body_started_total{application="app-test",} 433.0
# HELP okhttp3_network_requests_duration_seconds
# TYPE okhttp3_network_requests_duration_seconds histogram
okhttp3_network_requests_duration_seconds{application="app-test",quantile="0.5",} 0.031981568
okhttp3_network_requests_duration_seconds{application="app-test",quantile="0.75",} 0.041418752
okhttp3_network_requests_duration_seconds{application="app-test",quantile="0.95",} 0.047710208
okhttp3_network_requests_duration_seconds{application="app-test",quantile="0.98",} 0.051904512
okhttp3_network_requests_duration_seconds{application="app-test",quantile="0.99",} 0.056098816
okhttp3_network_requests_duration_seconds{application="app-test",quantile="0.999",} 0.318242816
okhttp3_network_requests_duration_seconds_bucket{application="app-test",le="0.1",} 432.0
okhttp3_network_requests_duration_seconds_bucket{application="app-test",le="+Inf",} 433.0
okhttp3_network_requests_duration_seconds_count{application="app-test",} 433.0
okhttp3_network_requests_duration_seconds_sum{application="app-test",} 11.691
# HELP okhttp3_network_requests_duration_seconds_max
# TYPE okhttp3_network_requests_duration_seconds_max gauge
okhttp3_network_requests_duration_seconds_max{application="app-test",} 0.307
# HELP okhttp3_connections_duration_seconds
# TYPE okhttp3_connections_duration_seconds summary
okhttp3_connections_duration_seconds_count{application="app-test",} 0.0
okhttp3_connections_duration_seconds_sum{application="app-test",} 0.0
# HELP okhttp3_connections_duration_seconds_max
# TYPE okhttp3_connections_duration_seconds_max gauge
okhttp3_connections_duration_seconds_max{application="app-test",} 0.0
# HELP okhttp3_network_requests_running_total
# TYPE okhttp3_network_requests_running_total counter
okhttp3_network_requests_running_total{application="app-test",} 434.0
# HELP okhttp3_calls_end_total
# TYPE okhttp3_calls_end_total counter
okhttp3_calls_end_total{application="app-test",} 216.0
# HELP okhttp3_dns_started_total
# TYPE okhttp3_dns_started_total counter
okhttp3_dns_started_total{application="app-test",} 4.0
```

## Jeebiz 技术社区

Jeebiz 技术社区 **微信公共号**、**小程序**，欢迎关注反馈意见和一起交流，关注公众号回复「Jeebiz」拉你入群。

|公共号|小程序|
|---|---|
| ![](https://raw.githubusercontent.com/hiwepy/static/main/images/qrcode_for_gh_1d965ea2dfd1_344.jpg)| ![](https://raw.githubusercontent.com/hiwepy/static/main/images/gh_09d7d00da63e_344.jpg)|

