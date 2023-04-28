package okhttp3.spring.boot.ssl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/*
 * TrustManager utilities for generating TrustManagers.
 * 
 * @since 3.0
 */
public final class TrustManagerUtils {

	private static final X509Certificate[] EMPTY_X509CERTIFICATE_ARRAY = new X509Certificate[] {};

	// 用于解决javax.net.ssl.SSLPeerUnverifiedException: peer not authenticated
	private static class TrustManager implements X509TrustManager {

		private final boolean checkServerValidity;

		TrustManager(boolean checkServerValidity) {
			this.checkServerValidity = checkServerValidity;
		}

		/*
		 * Never generates a CertificateException.
		 */
		public void checkClientTrusted(X509Certificate[] certificates, String authType) {
		}

		public void checkServerTrusted(X509Certificate[] certificates, String authType) throws CertificateException {
			if (checkServerValidity) {
				for (X509Certificate certificate : certificates) {
					certificate.checkValidity();
				}
			}
		}

		/*
		 * @return an empty array of certificates
		 */
		public X509Certificate[] getAcceptedIssuers() {
			return EMPTY_X509CERTIFICATE_ARRAY;
		}
	}
	
	private static class TrustHostnameVerifier implements HostnameVerifier {
		
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
		
	}

	// 创建TrustManager()
	private static final X509TrustManager ACCEPT_ALL = new TrustManager(false);

	private static final X509TrustManager CHECK_SERVER_VALIDITY = new TrustManager(true);

	private static final HostnameVerifier ACCEPT_ALL_VERIFIER = new TrustHostnameVerifier();
	
	/*
	 * Generate a HostnameVerifier that performs no checks.
	 *
	 * @return the HostnameVerifier
	 */
	public static HostnameVerifier getAcceptAllHostnameVerifier() {
		return ACCEPT_ALL_VERIFIER;
	}
	
	/*
	 * Generate a TrustManager that performs no checks.
	 *
	 * @return the TrustManager
	 */
	public static X509TrustManager getAcceptAllTrustManager() {
		return ACCEPT_ALL;
	}

	/*
	 * Generate a TrustManager that checks server certificates for validity, but
	 * otherwise performs no checks.
	 *
	 * @return the validating TrustManager
	 */
	public static X509TrustManager getValidateServerCertificateTrustManager() {
		return CHECK_SERVER_VALIDITY;
	}

	/*
	 * Return the default TrustManager provided by the JVM.
	 * <p>
	 * This should be the same as the default used by
	 * {@link javax.net.ssl.SSLContext#init(javax.net.ssl.KeyManager[], javax.net.ssl.TrustManager[], java.security.SecureRandom)
	 * SSLContext#init(KeyManager[], TrustManager[], SecureRandom)} when the
	 * TrustManager parameter is set to {@code null}
	 * 
	 * @param keyStore the KeyStore to use, may be {@code null}
	 * @return the default TrustManager
	 * @throws GeneralSecurityException if an error occurs
	 */
	public static X509TrustManager getDefaultTrustManager(KeyStore keyStore) throws GeneralSecurityException {
		String defaultAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
		TrustManagerFactory instance = TrustManagerFactory.getInstance(defaultAlgorithm);
		instance.init(keyStore);
		return (X509TrustManager) instance.getTrustManagers()[0];
	}
	
	


}
