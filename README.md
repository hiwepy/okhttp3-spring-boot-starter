# okhttp3-spring-boot-starter

Spring Boot Starter For Okhttp 3.x


### 说明


 > 基于 okhttp 3.x 的 Spring Boot Starter 实现
 
 部分代码参考了：https://github.com/linux-china/spring-boot-starter-okhttp3

### Maven

``` xml
<dependency>
	<groupId>com.github.hiwepy</groupId>
	<artifactId>okhttp3-spring-boot-starter</artifactId>
	<version>1.0.2.RELEASE</version>
</dependency>
```

### Sample

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

自定义配置，参考如下：
```yaml
okhttp3:
  enabled: true
  connect-timeout: 10
  follow-redirects: false
  follow-ssl-redirects: false
  ping-interval: 0
  read-timeout: 10
  retry-on-connection-failure: false
  write-timeout: 10
```



