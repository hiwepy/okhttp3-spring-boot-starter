package okhttp3.spring.boot;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * https://blog.csdn.net/fomin_zhu/article/details/85990363
 * https://www.jianshu.com/p/57a91bd2b68f
 */
public class OkHttp3WebSoket_Test {

    @Test
    public void testWebsocket(){

       // 1、 配置OkHttpClient

        OkHttpClient mClient = new OkHttpClient.Builder()
                .readTimeout(3, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(3, TimeUnit.SECONDS)//设置写的超时时间
                .connectTimeout(3, TimeUnit.SECONDS)//设置连接超时时间
                .build();
// 2、使用Url，构建WebSocket请求（一般是后端接口返回连接的Url地址）
        //连接地址
        String url = "ws://xxxxx";
//构建一个连接请求对象
        Request request = new Request.Builder().get().url(url).build();

        // 3、发起连接，配置回调。
        /*
        onOpen()，连接成功
        onMessage(String text)，收到字符串类型的消息，一般我们都是使用这个
        onMessage(ByteString bytes)，收到字节数组类型消息，我这里没有用到
        onClosed()，连接关闭
        onFailure()，连接失败，一般都是在这里发起重连操作
*/
        //开始连接
        WebSocket websocket = mClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);
                //连接成功...
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
                //收到消息...（一般是这里处理json）
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                super.onMessage(webSocket, bytes);
                //收到消息...（一般很少这种消息）
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                super.onClosed(webSocket, code, reason);
                //连接关闭...
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable throwable, Response response) {
                super.onFailure(webSocket, throwable, response);
                //连接失败...
            }
        });

       // 使用WebSocket对象发送消息，msg为消息内容（一般是json，当然你也可以使用其他的，例如xml等），send方法会马上返回发送结果。

        //发送消息
        boolean isSendSuccess = websocket.send("msg");



    }

}
