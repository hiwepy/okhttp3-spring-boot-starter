package okhttp3.spring.boot;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.beans.factory.ObjectProvider;
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
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.spring.boot.ext.ApplicationInterceptor;
import okhttp3.spring.boot.ext.GzipRequestInterceptor;
import okhttp3.spring.boot.ext.GzipRequestProperties;
import okhttp3.spring.boot.ext.NetworkInterceptor;
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
@EnableConfigurationProperties({ OkHttp3Properties.class, OkHttp3PoolProperties.class, OkHttp3SslProperties.class, 
	GzipRequestProperties.class, RequestHeaderProperties.class })
public class OkHttp3AutoConfiguration {
 
	@Bean
	public RequestHeaderInterceptor headerInterceptor(RequestHeaderProperties headerProperties) {
		return new RequestHeaderInterceptor(headerProperties);
	}
	
	@Bean
	public GzipRequestInterceptor gzipInterceptor(GzipRequestProperties gzipProperties) {
		return new GzipRequestInterceptor(gzipProperties);
	}
	
	@Bean
	public HttpLoggingInterceptor loggingInterceptor() {
		return new HttpLoggingInterceptor();
	}
	
	@Bean
	public okhttp3.OkHttpClient.Builder okhttp3Builder(
			ObjectProvider<CertificatePinner> certificatePinnerProvider,
			ObjectProvider<ConnectionSpec> connectionSpecProvider,
			ObjectProvider<CookieJar> cookieJarProvider,
			ObjectProvider<Dns> dnsProvider,
			ObjectProvider<EventListener> eventListenerProvider,
			ObjectProvider<OkHttpHostnameVerifier> hostnameVerifierProvider,
			ObjectProvider<SocketFactory>  socketFactoryProvider,
			ObjectProvider<X509TrustManager> trustManagerProvider, 
			ObjectProvider<ApplicationInterceptor> applicationInterceptorProvider,
			ObjectProvider<NetworkInterceptor> networkInterceptorProvider,
			HttpLoggingInterceptor loggingInterceptor, 
			OkHttp3Properties properties,
			OkHttp3PoolProperties poolProperties,
			OkHttp3SslProperties sslProperties) throws Exception {
		
		loggingInterceptor.setLevel(properties.getLogLevel());
		
		/**
	     * Create a new connection pool with tuning parameters appropriate for a single-user application.
	     * The tuning parameters in this pool are subject to change in future OkHttp releases. Currently
	     */
    	ConnectionPool connectionPool = new ConnectionPool(poolProperties.getMaxIdleConnections(), poolProperties.getKeepAliveDuration().getSeconds(), TimeUnit.SECONDS);
		
    	okhttp3.OkHttpClient.Builder builder = new OkHttpClient().newBuilder()
				// Application Interceptors、Network Interceptors : https://segmentfault.com/a/1190000013164260
				.addInterceptor(loggingInterceptor)
				.addNetworkInterceptor(loggingInterceptor)
				//.cache(cache)
				.callTimeout(properties.getCallTimeout(), TimeUnit.SECONDS)
				.certificatePinner(certificatePinnerProvider.getIfAvailable(()-> { return CertificatePinner.DEFAULT;} ))
				.connectionPool(connectionPool)
				.connectionSpecs(connectionSpecProvider.stream().collect(Collectors.toList()))
				.connectTimeout(properties.getConnectTimeout(), TimeUnit.SECONDS)
				.cookieJar(cookieJarProvider.getIfAvailable(()-> { return CookieJar.NO_COOKIES;}))
				.dns(dnsProvider.getIfAvailable(()-> { return Dns.SYSTEM;} ))
				.eventListener(eventListenerProvider.getIfAvailable(()-> { return EventListener.NONE;}))
				.followRedirects(properties.isFollowRedirects())
				.followSslRedirects(properties.isFollowSslRedirects())
				.hostnameVerifier(hostnameVerifierProvider.getIfAvailable(()-> { return new TrustAllHostnameVerifier();} ))
				.pingInterval(properties.getPingInterval(), TimeUnit.SECONDS)
				.socketFactory(socketFactoryProvider.getIfAvailable(()-> { return SocketFactory.getDefault();}))
				.readTimeout(properties.getReadTimeout(), TimeUnit.SECONDS)
				.retryOnConnectionFailure(properties.isRetryOnConnectionFailure())
				.writeTimeout(properties.getWriteTimeout(), TimeUnit.SECONDS);

		for (ApplicationInterceptor applicationInterceptor : applicationInterceptorProvider) {
			builder.addInterceptor(applicationInterceptor);
		}
		for (NetworkInterceptor networkInterceptor : networkInterceptorProvider) {
			builder.addNetworkInterceptor(networkInterceptor);
		}
		
		if(sslProperties.isEnabled()) {
			
			X509TrustManager trustManager = trustManagerProvider.getIfAvailable(()-> { return TrustManagerUtils.getAcceptAllTrustManager(); });
			
			/*
			 * 默认信任所有的证书 TODO 最好加上证书认证，主流App都有自己的证书
			 */
			SSLContext sslContext = SSLContextUtils.createSSLContext(sslProperties.getProtocol().name(), null, 
					new TrustManager[] { trustManager },
					new SecureRandom());
			
			SSLSocketFactory trustedSSLSocketFactory = sslContext.getSocketFactory();
			
			builder.sslSocketFactory(trustedSSLSocketFactory, trustManager);
			
		}
		
		return builder;
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
