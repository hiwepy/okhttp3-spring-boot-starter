package okhttp3.spring.boot;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(OkHttp3SslProperties.PREFIX)
@Data
public class OkHttp3SslProperties {
	
	public static final String PREFIX = "okhttp3.ssl";

	/** Whether Enable OkHttp3 SSL. */
	private boolean enabled = false;
	
	private Protocol protocol = Protocol.TLS;
	
	public enum Protocol {

		/**
		 * SSL：（Secure Socket Layer，安全套接字层），位于可靠的面向连接的网络层协议和应用层协议之间的一种协议层。SSL通过互相认证、使用数字签名确保完整性、使用加密确保私密性，以实现客户端和服务器之间的安全通讯。该协议由两层组成：SSL记录协议和SSL握手协议。
		 */
		SSL("SSL"),
		/**
		 * TLS：(Transport Layer Security，传输层安全协议)，用于两个应用程序之间提供保密性和数据完整性。该协议由两层组成：TLS记录协议和TLS握手协议。
		 */
		TLS("TLS"),
		TLSv1("TLSv1"),
		/**
		 * SECURITY (audit 2026-08): TLSv1.1 and below have known weaknesses
		 * (BEAST, POODLE, etc.) and must be opted into explicitly. The Java
		 * standard name is {@code TLSv1.2}; previously the enum exposed
		 * {@code TLSv2} (not a real protocol name) and {@code SSLv2/SSLv3},
		 * which were removed.
		 */
		TLSv1_2("TLSv1.2"),
		TLSv1_3("TLSv1.3");

		private final String protocol;

		Protocol(String protocol) {
			this.protocol = protocol;
		}

		public String value() {
			return protocol;
		}

	}
	
}