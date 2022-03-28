package okhttp3.spring.boot.ext;

import okhttp3.Authenticator;

public interface ProxyAuthenticator extends Authenticator {

    /** An authenticator that knows no credentials and makes no attempt to authenticate. */
    ProxyAuthenticator NONE = (route, response) -> null;

}
