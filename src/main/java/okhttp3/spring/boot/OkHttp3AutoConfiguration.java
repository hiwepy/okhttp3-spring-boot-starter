package okhttp3.spring.boot;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import okhttp3.OkHttpClient;
import okhttp3.internal.tls.CertificateChainCleaner;
import okhttp3.spring.boot.ssl.OkHttpHostnameVerifier;
import okhttp3.spring.boot.ssl.SSLContextUtils;
import okhttp3.spring.boot.ssl.TrustAllHostnameVerifier;
import okhttp3.spring.boot.ssl.TrustManagerUtils;

/**
 * 
 */
@Configuration
@ConditionalOnClass(okhttp3.OkHttpClient.class)
@ConditionalOnProperty(prefix = OkHttp3Properties.PREFIX, value = "enabled", havingValue = "true")
@EnableConfigurationProperties({ OkHttp3Properties.class })
public class OkHttp3AutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public OkHttpHostnameVerifier okhttpHostnameVerifier() {
		return new TrustAllHostnameVerifier();
	}

	@Bean
	@ConditionalOnMissingBean
	public X509TrustManager trustManager() {
		return TrustManagerUtils.getAcceptAllTrustManager();
	}
	
	@Bean
	@ConditionalOnMissingBean
	public SSLSocketFactory trustedSSLSocketFactory(X509TrustManager trustManager) throws IOException {
		/*
		 * 默认信任所有的证书 TODO 最好加上证书认证，主流App都有自己的证书
		 */
		SSLContext sslContext = SSLContextUtils.createSSLContext("TLS", null, 
				new TrustManager[] { trustManager },
				new SecureRandom());

		return sslContext.getSocketFactory();
	}
	
	@Bean
	@ConditionalOnMissingBean
	public CertificateChainCleaner certificatePinner(X509TrustManager trustManager) {
		return CertificateChainCleaner.get(trustManager);
	}
	
	@Bean
	public OkHttpClient okHttpClient(OkHttpHostnameVerifier okhttpHostnameVerifier, 
			X509TrustManager trustManager,
			SSLSocketFactory trustedSSLSocketFactory,
			OkHttp3Properties properties) throws Exception {

		
		OkHttpClient client = new OkHttpClient().newBuilder()
				.connectTimeout(properties.getConnectTimeout(), TimeUnit.SECONDS)
				.hostnameVerifier(okhttpHostnameVerifier)
				.followRedirects(properties.isFollowRedirects())
				.followSslRedirects(properties.isFollowSslRedirects())
				.pingInterval(properties.getPingInterval(), TimeUnit.SECONDS)
				.readTimeout(properties.getReadTimeout(), TimeUnit.SECONDS)
				.retryOnConnectionFailure(properties.isRetryOnConnectionFailure())
				.sslSocketFactory(trustedSSLSocketFactory, trustManager)
				.writeTimeout(properties.getWriteTimeout(), TimeUnit.SECONDS)
				.build();

		return client;
	}

}
