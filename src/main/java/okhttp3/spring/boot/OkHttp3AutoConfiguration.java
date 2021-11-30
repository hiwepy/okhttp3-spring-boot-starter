package okhttp3.spring.boot;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;

import okhttp3.Cache;
import okhttp3.CertificatePinner;
import okhttp3.ConnectionPool;
import okhttp3.CookieJar;
import okhttp3.Dns;
import okhttp3.EventListener;
import okhttp3.OkHttpClient;
import okhttp3.internal.tls.OkHostnameVerifier;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.spring.boot.ext.GzipRequestInterceptor;
import okhttp3.spring.boot.ext.GzipRequestProperties;
import okhttp3.spring.boot.ext.NetworkInterceptor;
import okhttp3.spring.boot.ext.RequestHeaderInterceptor;
import okhttp3.spring.boot.ext.RequestHeaderProperties;
import okhttp3.spring.boot.ext.RequestInterceptor;
import okhttp3.spring.boot.ext.RequestRetryIntercepter;
import okhttp3.spring.boot.ssl.SSLContexts;
import okhttp3.spring.boot.ssl.TrustManagerUtils;

/**
 * 
 */
@Configuration
@ConditionalOnClass(okhttp3.OkHttpClient.class)
@EnableConfigurationProperties({ OkHttp3Properties.class, OkHttp3PoolProperties.class, OkHttp3SslProperties.class, 
	GzipRequestProperties.class, RequestHeaderProperties.class })
public class OkHttp3AutoConfiguration {
 
	@Bean
	public RequestHeaderInterceptor headerInterceptor(RequestHeaderProperties headerProperties) {
		return new RequestHeaderInterceptor(headerProperties);
	}
	
	@Bean
	public RequestRetryIntercepter requestRetryIntercepter(OkHttp3Properties properties) {
		return new RequestRetryIntercepter(properties.getMaxRetry(), properties.getRetryInterval());
	}
	
	@Bean
	public GzipRequestInterceptor gzipInterceptor(GzipRequestProperties gzipProperties) {
		return new GzipRequestInterceptor(gzipProperties);
	}
	
	@Bean
	public HttpLoggingInterceptor loggingInterceptor(OkHttp3Properties properties) {
		HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
		loggingInterceptor.setLevel(properties.getLogLevel());
		return loggingInterceptor;
	}
	
	@Bean
	public okhttp3.OkHttpClient.Builder okhttp3Builder(
			ObjectProvider<CertificatePinner> certificatePinnerProvider,
			ObjectProvider<Cache> cacheProvider,
			ObjectProvider<CookieJar> cookieJarProvider,
			ObjectProvider<Dns> dnsProvider,
			ObjectProvider<EventListener> eventListenerProvider,
			ObjectProvider<HostnameVerifier> hostnameVerifierProvider,
			ObjectProvider<SocketFactory>  socketFactoryProvider,
			ObjectProvider<X509TrustManager> trustManagerProvider, 
			ObjectProvider<RequestInterceptor> applicationInterceptorProvider,
			ObjectProvider<NetworkInterceptor> networkInterceptorProvider,
			HttpLoggingInterceptor loggingInterceptor, 
			OkHttp3Properties properties,
			OkHttp3PoolProperties poolProperties,
			OkHttp3SslProperties sslProperties) throws Exception {
		
		/**
	     * Create a new connection pool with tuning parameters appropriate for a single-user application.
	     * The tuning parameters in this pool are subject to change in future OkHttp releases. Currently
	     */
    	ConnectionPool connectionPool = new ConnectionPool(poolProperties.getMaxIdleConnections(), poolProperties.getKeepAliveDuration().getSeconds(), TimeUnit.SECONDS);
    	
    	okhttp3.OkHttpClient.Builder builder = new OkHttpClient().newBuilder()
				// Application Interceptors、Network Interceptors : https://segmentfault.com/a/1190000013164260
				.addInterceptor(loggingInterceptor)
				.addNetworkInterceptor(loggingInterceptor)
				.callTimeout(properties.getCallTimeout())
				.certificatePinner(certificatePinnerProvider.getIfAvailable(()-> { return CertificatePinner.DEFAULT;} ))
				.connectionPool(connectionPool)
				.connectTimeout(properties.getConnectTimeout())
				.cookieJar(cookieJarProvider.getIfAvailable(()-> { return CookieJar.NO_COOKIES;}))
				.dns(dnsProvider.getIfAvailable(()-> { return Dns.SYSTEM;} ))
				.eventListener(eventListenerProvider.getIfAvailable(()-> { return EventListener.NONE;}))
				.followRedirects(properties.isFollowRedirects())
				.followSslRedirects(properties.isFollowSslRedirects()) 
				.hostnameVerifier(hostnameVerifierProvider.getIfAvailable(()-> { return OkHostnameVerifier.INSTANCE;} ))
				.pingInterval(properties.getPingInterval())
				.protocols(properties.getProtocols())
				.socketFactory(socketFactoryProvider.getIfAvailable(()-> { return SocketFactory.getDefault();}))
				.readTimeout(properties.getReadTimeout())
				.retryOnConnectionFailure(properties.isRetryOnConnectionFailure())
				.writeTimeout(properties.getWriteTimeout());

		for (RequestInterceptor applicationInterceptor : applicationInterceptorProvider) {
			builder.addInterceptor(applicationInterceptor);
		}
		for (NetworkInterceptor networkInterceptor : networkInterceptorProvider) {
			builder.addNetworkInterceptor(networkInterceptor);
		}
		
		Cache cache = cacheProvider.getIfAvailable();
		if(Objects.nonNull(cache)) {
			builder.cache(cache);
		}
		
		if(sslProperties.isEnabled()) {
			
			X509TrustManager trustManager = trustManagerProvider.getIfAvailable(()-> { return TrustManagerUtils.getAcceptAllTrustManager(); });
			
			SSLContext sslContext = SSLContexts.createSSLContext(sslProperties.getProtocol().name(), null, trustManager);
			
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
