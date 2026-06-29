/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package okhttp3.spring.boot.ssl;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

/*
 * {@link javax.net.ssl.SSLContext} factory methods.
 *
 * @since 4.4
 */
public class SSLContexts {

    /*
     * Creates default factory based on the standard JSSE trust material
     * ({@code cacerts} file in the security properties directory). System properties
     * are not taken into consideration.
     *
     * @return the default SSL socket factory
     */
    public static SSLContext createDefault() throws SSLInitializationException {
        try {
            final SSLContext sslContext = SSLContext.getInstance(SSLContextBuilder.TLS);
            sslContext.init(null, null, null);
            return sslContext;
        } catch (final NoSuchAlgorithmException ex) {
            throw new SSLInitializationException(ex.getMessage(), ex);
        } catch (final KeyManagementException ex) {
            throw new SSLInitializationException(ex.getMessage(), ex);
        }
    }

    /*
     * Creates default SSL context based on system properties. This method obtains
     * default SSL context by calling {@code SSLContext.getInstance("Default")}.
     * Please note that {@code Default} algorithm is supported as of Java 6.
     * This method will fall back onto {@link #createDefault()} when
     * {@code Default} algorithm is not available.
     *
     * @return default system SSL context
     */
    public static SSLContext createSystemDefault() throws SSLInitializationException {
        try {
            return SSLContext.getDefault();
        } catch (final NoSuchAlgorithmException ex) {
            return createDefault();
        }
    }

	/*
	 * Create and initialise an SSLContext.
	 * 
	 * @param protocol 		the protocol used to instatiate the context
	 * @param keyManager 	the key manager, may be {@code null}
	 * @param trustManager 	the trust manager, may be {@code null}
	 * @return the initialised context.
	 * @throws IOException this is used to wrap any {@link GeneralSecurityException} that occurs
	 */
	public static SSLContext createSSLContext(String protocol, KeyManager keyManager, TrustManager trustManager)
			throws IOException {
		return createSSLContext(protocol, keyManager == null ? null : new KeyManager[] { keyManager },
				trustManager == null ? null : new TrustManager[] { trustManager });
	}
 
	/*
	 * Create and initialise an SSLContext.
	 * 
	 * @param protocol 	the protocol used to instatiate the context
	 * @param keyManagers the array of key managers, may be {@code null} but array entries must not be {@code null}
	 * @param trustManagers the array of trust managers, may be {@code null} but array entries
	 *            must not be {@code null}
	 * @return the initialised context.
	 * @throws IOException this is used to wrap any {@link GeneralSecurityException} that occurs
	 */
	public static SSLContext createSSLContext(String protocol, KeyManager[] keyManagers, TrustManager[] trustManagers)
			throws IOException {
		SSLContext ctx;
		try {
			/*
			 * HttpClient使用SSLSocketFactory来创建SSL连接。SSLSocketFactory允许高度定制。
			 * 它可以使用javax.net.ssl.SSLContext的实例作为参数，并使用它来创建定制SSL连接。
			 */
			ctx = SSLContexts.custom().setProtocol(protocol).build();
			// 使用TrustManager来初始化该上下文,TrustManager只是被SSL的Socket所使用
			ctx.init(keyManagers, trustManagers, /* SecureRandom */ null);
		} catch (GeneralSecurityException e) {
			IOException ioe = new IOException("Could not initialize SSL context");
			ioe.initCause(e);
			throw ioe;
		}
		return ctx;
	}
	
    /**
     * Create and initialise an SSLContext.
     * @param protocol the protocol used to instatiate the context
     * @param keyManagers the array of key managers, may be {@code null} but array entries must not be {@code null}
     * @param trustManagers the array of trust managers, may be {@code null} but array entries must not be {@code null}
     * @param secureRandom This class provides a cryptographically strong random number generator (RNG). 
     * @return the initialised context.
     * @throws IOException this is used to wrap any {@link GeneralSecurityException} that occurs
     */
    public static SSLContext createSSLContext(String protocol, KeyManager[] keyManagers, TrustManager[] trustManagers,
    		SecureRandom secureRandom)
        throws IOException {
        SSLContext ctx;
        try {
            ctx = SSLContexts.custom().setProtocol(protocol).build();
            ctx.init(keyManagers, trustManagers, secureRandom);
        } catch (GeneralSecurityException e) {
            IOException ioe = new IOException("Could not initialize SSL context");
            ioe.initCause(e);
            throw ioe;
        }
        return ctx;
    }
    

	public static SSLContext createSSLContext(KeyStore keystore, TrustStrategy trustStrategy) throws IOException {
		// 初始化证书
		SSLContext ctx;
		try {
			ctx = SSLContexts.custom().loadTrustMaterial(keystore, trustStrategy).build();
		} catch (GeneralSecurityException e) {
			IOException ioe = new IOException("Could not initialize SSL context");
			ioe.initCause(e);
			throw ioe;
		}
		return ctx;
	}

	public static SSLContext createSSLContext(String protocol, File keystore, String storePassword,
			TrustStrategy trustStrategy) throws IOException {
		// 初始化证书
		SSLContext ctx;
		try {
			ctx = SSLContexts.custom().setProtocol(protocol)
					.loadTrustMaterial(keystore, storePassword.toCharArray(), trustStrategy).build();
		} catch (GeneralSecurityException e) {
			IOException ioe = new IOException("Could not initialize SSL context");
			ioe.initCause(e);
			throw ioe;
		}
		return ctx;
	}
	
    /*
     * Creates custom SSL context.
     *
     * @return default system SSL context
     */
    public static SSLContextBuilder custom() {
        return SSLContextBuilder.create();
    }
   

}
