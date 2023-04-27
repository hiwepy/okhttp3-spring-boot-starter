package okhttp3.spring.boot;

import java.net.Proxy;
import java.net.ProxySelector;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import okhttp3.*;
import okhttp3.spring.boot.cookie.CaffeineCacheCookieJar;
import okhttp3.spring.boot.cookie.NestedCookieJar;
import okhttp3.spring.boot.ext.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;

import okhttp3.internal.tls.OkHostnameVerifier;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.spring.boot.ssl.SSLContexts;
import okhttp3.spring.boot.ssl.TrustManagerUtils;
import org.springframework.util.CollectionUtils;

/**
 * OkHttp Client Ini
 */
@Configuration
@ConditionalOnClass(okhttp3.OkHttpClient.class)
@EnableConfigurationProperties({ OkHttp3Properties.class, OkHttp3PoolProperties.class, OkHttp3SslProperties.class,
		OkHttp3CookieProperties.class, GzipRequestProperties.class, RequestHeaderProperties.class , OkHttp3MetricsProperties.class})
public class OkHttp3AutoConfiguration {

	@Bean
	public RequestHeaderInterceptor headerInterceptor(RequestHeaderProperties headerProperties) {
		return new RequestHeaderInterceptor(headerProperties);
	}

	@Bean
	public RequestRetryIntercepter requestRetryIntercepter(OkHttp3Properties properties) {
		return new RequestRetryIntercepter(properties.getRetryMaxAttempts(), properties.getRetryInterval());
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
	public Dispatcher dispatcher(OkHttp3PoolProperties properties) {
		Dispatcher dispatcher = new Dispatcher();
		dispatcher.setMaxRequests(Math.max(properties.getMaxRequests(), OkHttp3PoolProperties.DEFAULT_MAX_REQUESTS));
		dispatcher.setMaxRequestsPerHost(Math.max(properties.getMaxRequestsPerHost(), OkHttp3PoolProperties.DEFAULT_MAX_REQUESTS_PER_ROUTE));
		return dispatcher;
	}

	@Bean
	@ConditionalOnMissingBean
	public CookieJar cookieJar(OkHttp3CookieProperties properties) {
		return new CaffeineCacheCookieJar(properties.getMaximumSize(), properties.getExpireAfterWrite(), properties.getExpireAfterAccess());
	}

	@Bean
	public okhttp3.OkHttpClient.Builder okhttp3Builder(
			ObjectProvider<Authenticator> authenticatorProvider,
			ObjectProvider<CertificatePinner> certificatePinnerProvider,
			ObjectProvider<Cache> cacheProvider,
			ObjectProvider<ConnectionSpec> connectionSpecProvider,
			ObjectProvider<CookieJar> cookieJarProvider,
			ObjectProvider<Dns> dnsProvider,
            ObjectProvider<Dispatcher> dispatcherProvider,
			ObjectProvider<EventListener> eventListenerProvider,
			ObjectProvider<HostnameVerifier> hostnameVerifierProvider,
			ObjectProvider<ProxyAuthenticator> proxyAuthenticatorProvider,
			ObjectProvider<Proxy> proxyProvider,
			ObjectProvider<ProxySelector> proxySelectorProvider,
			ObjectProvider<SocketFactory>  socketFactoryProvider,
			ObjectProvider<X509TrustManager> trustManagerProvider,
			ObjectProvider<RequestInterceptor> requestInterceptorProvider,
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
		/**
		 * get connectionSpecs
		 */
		List<ConnectionSpec> connectionSpecs = connectionSpecProvider.stream().collect(Collectors.toList());
		if(CollectionUtils.isEmpty(connectionSpecs)){
			connectionSpecs = Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT);
		}
		/**
		 * get cookieJar
		 */
		List<CookieJar> cookieJars = cookieJarProvider.stream().collect(Collectors.toList());
		/**
		 *  Create a new OkHttpClient Builder with configuration.
		 */
		okhttp3.OkHttpClient.Builder builder = new OkHttpClient().newBuilder()
				// Application Interceptors、Network Interceptors : https://segmentfault.com/a/1190000013164260
				.authenticator(authenticatorProvider.getIfAvailable(() -> Authenticator.NONE))
				.addInterceptor(loggingInterceptor)
				.addNetworkInterceptor(loggingInterceptor)
				.cache(cacheProvider.getIfAvailable())
				.callTimeout(properties.getCallTimeout())
				.certificatePinner(certificatePinnerProvider.getIfAvailable(() -> CertificatePinner.DEFAULT))
				.connectionPool(connectionPool)
				.connectTimeout(properties.getConnectTimeout())
				.connectionSpecs(connectionSpecs)
				.cookieJar(CollectionUtils.isEmpty(cookieJars) ? CookieJar.NO_COOKIES: new NestedCookieJar(cookieJars))
				.dns(dnsProvider.getIfAvailable(() -> Dns.SYSTEM))
				.dispatcher(dispatcherProvider.getIfAvailable(() -> new Dispatcher()))
				.eventListener(eventListenerProvider.getIfAvailable(() -> EventListener.NONE))
				.followRedirects(properties.isFollowRedirects())
				.followSslRedirects(properties.isFollowSslRedirects())
				.hostnameVerifier(hostnameVerifierProvider.getIfAvailable(() -> OkHostnameVerifier.INSTANCE))
				.protocols(properties.getProtocols())
				.proxy(proxyProvider.getIfAvailable())
				.proxySelector(proxySelectorProvider.getIfAvailable(() -> ProxySelector.getDefault()))
				.proxyAuthenticator(proxyAuthenticatorProvider.getIfAvailable(() -> ProxyAuthenticator.NONE))
				.pingInterval(properties.getPingInterval())
				.readTimeout(properties.getReadTimeout())
				.retryOnConnectionFailure(properties.isRetryOnConnectionFailure())
				.socketFactory(socketFactoryProvider.getIfAvailable(() -> SocketFactory.getDefault()))
				.writeTimeout(properties.getWriteTimeout());

		for (RequestInterceptor requestInterceptor : requestInterceptorProvider) {
			builder.addInterceptor(requestInterceptor);
		}
		for (NetworkInterceptor networkInterceptor : networkInterceptorProvider) {
			builder.addNetworkInterceptor(networkInterceptor);
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
	public OkHttpClient okhttp3Client(okhttp3.OkHttpClient.Builder okhttp3Builder,
									  OkHttp3MetricsProperties metricsProperties) throws Exception {
		OkHttpClient okhttp3Client = okhttp3Builder.build();
		/*if (metricsProperties.isEnabled()) {
			return InstrumentedOkHttpClients.create(registry, okhttp3Client);
		}*/
		return okhttp3Client;
	}

	@Bean
	public OkHttp3ClientHttpRequestFactory okHttp3ClientHttpRequestFactory(OkHttpClient okhttp3Client) {
		return new OkHttp3ClientHttpRequestFactory(okhttp3Client);
	}

	@Bean
	public OkHttp3Template okHttp3Template(OkHttpClient okhttp3Client,
										  ObjectProvider<ObjectMapper> objectMapperProvider) {

		ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(() -> {
			ObjectMapper objectMapperDef = new ObjectMapper();
			objectMapperDef.setSerializationInclusion(JsonInclude.Include.NON_NULL);
			objectMapperDef.enable(MapperFeature.USE_GETTERS_AS_SETTERS);
			objectMapperDef.enable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS);
			objectMapperDef.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
			objectMapperDef.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			return objectMapperDef;
		});

		return new OkHttp3Template(okhttp3Client, objectMapper);
	}

}
