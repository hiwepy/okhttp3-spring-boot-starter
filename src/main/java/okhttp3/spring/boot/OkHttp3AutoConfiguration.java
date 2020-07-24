package okhttp3.spring.boot;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;

import okhttp3.CertificatePinner;
import okhttp3.ConnectionPool;
import okhttp3.ConnectionSpec;
import okhttp3.CookieJar;
import okhttp3.Dns;
import okhttp3.EventListener;
import okhttp3.OkHttpClient;
import okhttp3.internal.tls.CertificateChainCleaner;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.spring.boot.ext.RequestHeaderInterceptor;
import okhttp3.spring.boot.ext.RequestHeaderProperties;
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
@EnableConfigurationProperties({ OkHttp3Properties.class, RequestHeaderProperties.class })
public class OkHttp3AutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(OkHttpHostnameVerifier.class)
	public OkHttpHostnameVerifier okhttpHostnameVerifier() {
		return new TrustAllHostnameVerifier();
	}

	@Bean
	@ConditionalOnMissingBean(X509TrustManager.class)
	public X509TrustManager trustManager() {
		return TrustManagerUtils.getAcceptAllTrustManager();
	}
	
	@Bean
	@ConditionalOnMissingBean(SSLSocketFactory.class)
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
	@ConditionalOnMissingBean(CertificateChainCleaner.class)
	public CertificateChainCleaner certificatePinner(X509TrustManager trustManager) {
		return CertificateChainCleaner.get(trustManager);
	}
	
	@Bean
	public RequestHeaderInterceptor headerInterceptor(RequestHeaderProperties headerProperties) {
		return new RequestHeaderInterceptor(headerProperties);
	}
	
	@Bean
	public HttpLoggingInterceptor loggingInterceptor() {
		return new HttpLoggingInterceptor();
	}
	
	@Bean
	@ConditionalOnMissingBean
	public CertificatePinner certificatePinner() {
		return CertificatePinner.DEFAULT;
	}
	
	@Bean
	@ConditionalOnMissingBean
	public CookieJar cookieJar() {
		return CookieJar.NO_COOKIES;
	}
	
	@Bean
	@ConditionalOnMissingBean
	public Dns dns() {
		return Dns.SYSTEM;
	}
	
	@Bean
	@ConditionalOnMissingBean
	public EventListener eventListener() {
		return EventListener.NONE;
	}
	
	@Bean
	@ConditionalOnMissingBean
	public SocketFactory socketFactory() {
		return SocketFactory.getDefault();
	}
	
	@Bean
	public okhttp3.OkHttpClient.Builder okhttp3Builder(
			CertificatePinner certificatePinner,
			@Autowired(required = false) List<ConnectionSpec> connectionSpecs,
			CookieJar cookieJar,
			Dns dns,
			EventListener eventListener,
			OkHttpHostnameVerifier hostnameVerifier,
			SocketFactory socketFactory,
			X509TrustManager trustManager, 
			SSLSocketFactory trustedSSLSocketFactory,
			HttpLoggingInterceptor loggingInterceptor, 
			RequestHeaderInterceptor headerInterceptor,
			OkHttp3Properties properties) throws Exception {

		return new OkHttpClient().newBuilder()
				// Application Interceptors、Network Interceptors : https://segmentfault.com/a/1190000013164260
				.addInterceptor(headerInterceptor)
				.addNetworkInterceptor(loggingInterceptor)
				//.cache(cache)
				.callTimeout(properties.getCallTimeout(), TimeUnit.SECONDS)
				.certificatePinner(certificatePinner)
				.connectionPool(connectionPool(properties))
				.connectionSpecs(connectionSpecs)
				.connectTimeout(properties.getConnectTimeout(), TimeUnit.SECONDS)
				.cookieJar(cookieJar)
				.dns(dns)
				.eventListener(eventListener)
				.followRedirects(properties.isFollowRedirects())
				.followSslRedirects(properties.isFollowSslRedirects())
				.hostnameVerifier(hostnameVerifier)
				.pingInterval(properties.getPingInterval(), TimeUnit.SECONDS)
				.socketFactory(socketFactory)
				.readTimeout(properties.getReadTimeout(), TimeUnit.SECONDS)
				.retryOnConnectionFailure(properties.isRetryOnConnectionFailure())
				.sslSocketFactory(trustedSSLSocketFactory, trustManager)
				.writeTimeout(properties.getWriteTimeout(), TimeUnit.SECONDS);
	}
	
	/**
     * Create a new connection pool with tuning parameters appropriate for a single-user application.
     * The tuning parameters in this pool are subject to change in future OkHttp releases. Currently
     */
    @Bean
    public ConnectionPool connectionPool(OkHttp3Properties properties) {
        return new ConnectionPool(properties.getMaxIdleConnections(), properties.getKeepAliveDuration(), TimeUnit.SECONDS);
    }
	
	@Bean
	@ConditionalOnMissingBean(OkHttpClient.class)
	public OkHttpClient okhttp3Client(okhttp3.OkHttpClient.Builder okhttp3Builder) throws Exception {
		return okhttp3Builder.build();
	}
	
	@Bean
	public OkHttp3ClientHttpRequestFactory okHttp3ClientHttpRequestFactory(OkHttpClient okhttp3Client) {
		return new OkHttp3ClientHttpRequestFactory(okhttp3Client);
	}

	@Bean
	public OkHttp3Template okHttp3Template(OkHttpClient okHttpClient) {
		return new OkHttp3Template(okHttpClient);
	}
	
}
