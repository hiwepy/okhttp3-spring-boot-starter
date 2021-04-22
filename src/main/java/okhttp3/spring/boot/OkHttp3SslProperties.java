package okhttp3.spring.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

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
		SSLv2("SSLv2"),
		SSLv3("SSLv3"),
		/**
		 * TLS：(Transport Layer Security，传输层安全协议)，用于两个应用程序之间提供保密性和数据完整性。该协议由两层组成：TLS记录协议和TLS握手协议。
		 */
		TLS("TLS"),
		TLSv1("TLSv1"),
		TLSv2("TLSv2");

		private final String protocol;

		Protocol(String protocol) {
			this.protocol = protocol;
		}

		public String value() {
			return protocol;
		}
		
	}
	
}