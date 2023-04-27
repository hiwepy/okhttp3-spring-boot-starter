# okhttp3-spring-boot-starter

Spring Boot Starter For Okhttp 3.x

### 组件简介

 > 基于 okhttp 3.x 的 Spring Boot Starter 实现
 
- 部分代码参考了：https://github.com/linux-china/spring-boot-starter-okhttp3
- 整合了：https://github.com/raskasa/metrics-okhttp

### 使用说明

##### 1、Spring Boot 项目添加 Maven 依赖

``` xml
<dependency>
	<groupId>com.github.hiwepy</groupId>
	<artifactId>okhttp3-spring-boot-starter</artifactId>
	<version>${project.version}</version>
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

## Jeebiz 技术社区

Jeebiz 技术社区 **微信公共号**、**小程序**，欢迎关注反馈意见和一起交流，关注公众号回复「Jeebiz」拉你入群。

|公共号|小程序|
|---|---|
| ![](https://raw.githubusercontent.com/hiwepy/static/main/images/qrcode_for_gh_1d965ea2dfd1_344.jpg)| ![](https://raw.githubusercontent.com/hiwepy/static/main/images/gh_09d7d00da63e_344.jpg)|

