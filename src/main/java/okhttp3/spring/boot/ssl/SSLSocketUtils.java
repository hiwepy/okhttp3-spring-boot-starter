package okhttp3.spring.boot.ssl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.net.ssl.SSLSocket;

/*
 * General utilities for SSLSocket.
 * @since 3.4
 */
public class SSLSocketUtils {
    private SSLSocketUtils() {
        // Not instantiable
    }

    /*
     * Enable the HTTPS endpoint identification algorithm on an SSLSocket.
     * @param socket the SSL socket
     * @return {@code true} on success (this is only supported on Java 1.7+)
     */
    public static boolean enableEndpointNameVerification(SSLSocket socket) {
        try {
            Class<?> cls = Class.forName("javax.net.ssl.SSLParameters");
            Method setEndpointIdentificationAlgorithm = cls.getDeclaredMethod("setEndpointIdentificationAlgorithm", String.class);
            Method getSSLParameters = SSLSocket.class.getDeclaredMethod("getSSLParameters");
            Method setSSLParameters = SSLSocket.class.getDeclaredMethod("setSSLParameters", cls);
            if (setEndpointIdentificationAlgorithm != null && getSSLParameters != null && setSSLParameters != null) {
                Object sslParams = getSSLParameters.invoke(socket);
                if (sslParams != null) {
                    setEndpointIdentificationAlgorithm.invoke(sslParams, "HTTPS");
                    setSSLParameters.invoke(socket, sslParams);
                    return true;
                }
            }
        } catch (SecurityException e) { // Ignored
        } catch (ClassNotFoundException e) { // Ignored
        } catch (NoSuchMethodException e) { // Ignored
        } catch (IllegalArgumentException e) { // Ignored
        } catch (IllegalAccessException e) { // Ignored
        } catch (InvocationTargetException e) { // Ignored
        }
        return false;
    }
}
