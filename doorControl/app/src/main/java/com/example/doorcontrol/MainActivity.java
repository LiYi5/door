package com.example.doorcontrol;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.serialPort.Ch340;
import com.example.serialPort.SerPort;
import com.example.utils.Utils;
import com.example.webSocket.MyWebSocket;

import okhttp3.MediaType;

public class MainActivity extends AppCompatActivity {
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public TextView text_write;
    public TextView text_read;
    public TextView text_receive;
    public EditText edit_text;

          //A5 A5 BA BA 01 15 EB 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 FF FF FF FF 40 00 00 00 00 00 00 00 00 00 00 00 00 47
    private  byte[] to_s={(byte)0xa5,(byte)0xa5,(byte)0xba,(byte)0xba,(byte)0x01,(byte)0x15,(byte)0xeb,(byte)0x00,(byte)0x00,(byte)0x00,
            (byte)0x00,(byte)0x00,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0x00,(byte)0x00,(byte)0x00,
            (byte)0x00,(byte)0x00,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0x40,(byte)0x00,(byte)0x00,
            (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
            (byte)0x47};

    public Button button_open,button_write,button_websocket,clear;

    public Button btnClose,btnClose1,button_websocket1;

    public MyWebSocket mSocket,mSocket1;

    public SerPort serPort=new SerPort();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text_read=findViewById(R.id.text_read);
        text_write=findViewById(R.id.text_write);
        text_receive=findViewById(R.id.text_receive);
        edit_text=findViewById(R.id.edit_text);

        button_open=findViewById(R.id.button_open);
        button_write=findViewById(R.id.button_write);
        button_websocket=findViewById(R.id.button_websocket);
        button_websocket1=findViewById(R.id.button_websocket1);
        btnClose =  findViewById(R.id.button_websocketclose);
        btnClose1=findViewById(R.id.button_websocketclose1);
        clear=findViewById(R.id.button_clear);

        /*
         * 设置按钮状态及按钮监听
         */
        button_write.setEnabled(false);

        // 检测是否连接上USB线
        //serPort.serialPortInit(this);

        /*
         * 打开串口按钮监听类
         */
        button_open.setOnClickListener(new  View.OnClickListener(){

            @Override
            public void onClick(View v) {
                SerPort.serialPortOpen();
            }
        });

        /*
         * 通过按钮向串口发送数据
         */

        button_write.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                int retval = Ch340.driver.WriteData(to_s, to_s.length);
                text_write.append("向串口发送的数据："+Utils.toHexString(to_s,to_s.length)+"-----+"+retval+"\n");
                if (retval < 0)
                    Toast.makeText(MainActivity.this, "写入失败！",
                            Toast.LENGTH_SHORT).show();
            }
        });

        // websocket初始化
        WebSocketInit();
    }

    public  void output(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text_receive.setText(text_receive.getText().toString() + "\n" + text);
            }
        });
    }

    private void WebSocketInit(){
        //小门禁
        button_websocket.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                text_receive.append("小门禁websocket连接！\n");
                //"ws://106.13.133.229:6005/ws?deviceId=1"
                mSocket=new MyWebSocket(MainActivity.this);
                mSocket.start("ws://106.13.133.229:6005/ws?deviceId=21");

            }
        });

        //背夹
        button_websocket1.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                text_receive.append("背夹websocket连接！\n");
                //"ws://106.13.133.229:6005/ws?deviceId=1"
                mSocket1=new MyWebSocket(MainActivity.this);
                mSocket1.start("ws://106.13.133.229:6005/ws?deviceId=22");
            }
        });

        //小门禁
        btnClose.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                output("closeing");
                Utils.closeConnect(mSocket.webSocket);
            }
        });

        //背夹
        btnClose1.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                output("closeing");
                Utils.closeConnect(mSocket1.webSocket);
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text_write.setText("");
                text_read.setText("");
                text_receive.setText("");

            }
        });
    }


    @Override
    protected void onDestroy() {
        Ch340.driver.CloseDevice();
        super.onDestroy();
        if(mSocket!=null){
            Utils.closeConnect(mSocket.webSocket);
        }
        if(mSocket1!=null) {
            Utils.closeConnect(mSocket1.webSocket);
        }
    }

}
