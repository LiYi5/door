package com.example.webSocket;

import com.example.doorcontrol.MainActivity;
import com.example.serialPort.SerPort;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MyWebSocket {


    private MainActivity mainActivity;

    public WebSocket webSocket;
    public String url;

    //背夹数据
    byte[] sendToBack={(byte)0xa5,(byte)0xa5,(byte)0xba,(byte)0xba,(byte)0x01,(byte)0x15,(byte)0xeb,(byte)0x00,(byte)0x00,(byte)0x00,
            (byte)0x00,(byte)0x00,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0x00,(byte)0x00,(byte)0x00,
            (byte)0x00,(byte)0x00,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0x40,(byte)0x00,(byte)0x00,
            (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
            (byte)0x47};


    //小门禁
    byte[] sendToDoor={(byte)0xa5,(byte)0xa5,(byte)0xba,(byte)0xba,(byte)0x01,(byte)0x15,(byte)0xeb,(byte)0x00,(byte)0x00,(byte)0x00,
            (byte)0x00,(byte)0x00,(byte)0x11,(byte)0x11,(byte)0x11,(byte)0x11,(byte)0x00,(byte)0x00,(byte)0x00,
            (byte)0x00,(byte)0x00,(byte)0x11,(byte)0x11,(byte)0x11,(byte)0x11,(byte)0x40,(byte)0x00,(byte)0x00,
            (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
            (byte)0x47};

    public void start(String url) {
        this.url=url;
        OkHttpClient mOkHttpClient = new OkHttpClient.Builder()
                .readTimeout(3, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(3, TimeUnit.SECONDS)//设置写的超时时间
                .connectTimeout(3, TimeUnit.SECONDS)//设置连接超时时间
                .build();
        //"ws://106.13.133.229:6005/ws?deviceId=1"
        Request request = new Request.Builder().url(url).build();

        EchoWebSocketListener socketListener = new EchoWebSocketListener();
        mOkHttpClient.newWebSocket(request, socketListener);
        mOkHttpClient.dispatcher().executorService().shutdown();

    }

    private final class EchoWebSocketListener extends WebSocketListener {

        @Override
        public void onOpen(okhttp3.WebSocket webSocket1, Response response) {
            super.onOpen(webSocket1, response);
            webSocket=webSocket1;
//            String openid = "1";
//            //连接成功后，发送登录信息
//            String message = "{\"type\":\"login\",\"user_id\":\""+openid+"\"}";
//            boolean send = mSocket.send("hello!");
//            output("连接成功！"+send);

        }

        @Override
        public void onMessage(okhttp3.WebSocket webSocket, ByteString bytes) {
            super.onMessage(webSocket, bytes);
            mainActivity.output("receive bytes:" + bytes.hex());
        }

        @Override
        public void onMessage(okhttp3.WebSocket webSocket, String text) {
            super.onMessage(webSocket, text);
            mainActivity.output("receive string:" +text+"\n");

            if(text.length()>0){
                if("ws://106.13.133.229:6005/ws?deviceId=21".equals(url)){
                mainActivity.output("向小门禁发送\n");
                    SerPort.writeToSer(sendToDoor);
                }else if("ws://106.13.133.229:6005/ws?deviceId=22".equals(url)){
//                    向背夹发送
                    mainActivity.output("向背夹发送\n");
                    SerPort.writeToSer(sendToBack);

                }

            }
        }

        @Override
        public void onClosed(okhttp3.WebSocket webSocket, int code, String reason) {
            super.onClosed(webSocket, code, reason);
            mainActivity.output("closed:" + reason);
        }

        @Override
        public void onClosing(okhttp3.WebSocket webSocket, int code, String reason) {
            super.onClosing(webSocket, code, reason);
            mainActivity.output("closing:" + reason);
        }

        @Override
        public void onFailure(okhttp3.WebSocket webSocket, Throwable t, Response response) {
            super.onFailure(webSocket, t, response);
            mainActivity.output("failure:" + t.getMessage());
        }
    }



    public MyWebSocket(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
}
