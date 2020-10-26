package com.example.serialPort;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.doorcontrol.MainActivity;

import com.example.utils.Utils;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;

public class SerPort {


    /**
     * 当前串口状态
     */
    private static boolean isopen;

    /**
     * USb权限
     */
    private static final String ACTION_USB_PERMISSION = "cn.wch.wchusbdriver.USB_PERMISSION";

    private static MainActivity mainActivity;


    //A5 A5 BA BA 01 15 EB 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 FF FF FF FF 40 00 00 00 00 00 00 00 00 00 00 00 00 47




    public  void serialPortInit(MainActivity mainActivity){
        this.mainActivity=mainActivity;
        Ch340.driver = new CH34xUARTDriver(
                (UsbManager) mainActivity.getSystemService(Context.USB_SERVICE),mainActivity,
                ACTION_USB_PERMISSION);


        if (!Ch340.driver.UsbFeatureSupported())
        {
            Dialog dialog = new AlertDialog.Builder(mainActivity)
                    .setTitle("提示")
                    .setMessage("您的设备不支持USBhost")
                    .setPositiveButton("确认",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0,
                                                    int arg1) {
                                    System.exit(0);
                                }
                            }).create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
        mainActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        isopen=false;

    }

    public static void serialPortOpen(){
        if(!isopen) {

            int retval = Ch340.driver.ResumeUsbPermission();
            if (retval == 0) {
                //Resume usb device list
                retval = Ch340.driver.ResumeUsbList();
                if (retval == -1)
                {
                    Toast.makeText(mainActivity, "打开设备失败！",
                            Toast.LENGTH_SHORT).show();
                    Ch340.driver.CloseDevice();
                } else if (retval == 0){
                    if (Ch340.driver.mDeviceConnection != null) {
                        if (!Ch340.driver.UartInit()) {//对串口设备进行初始化
                            Toast.makeText(mainActivity, "初始化设备失败！",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(mainActivity, "打开设备成功！",
                                Toast.LENGTH_SHORT).show();
                        isopen = true;
                        mainActivity.button_open.setText("关闭");
                        mainActivity.button_write.setEnabled(true);
                        Ch340.driver.SetConfig(115200,(byte)1,(byte)8,(byte)0,(byte)0);
                        new ReadThread(mainActivity,isopen).start();//开启线程读取串口接收的数据
                    } else {
                        Toast.makeText(mainActivity, "打开设备失败！",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);

                    builder.setTitle("未授权限");
                    builder.setMessage("确认退出吗？");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    });
                    builder.setNegativeButton("返回", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();

                }
            }
        }else {
                mainActivity.button_open.setText("打开");
                mainActivity.button_write.setEnabled(false);
                isopen = false;
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Ch340.driver.CloseDevice();
        }


    }

    public static void writeToSer(byte[] to){


        int retval = Ch340.driver.WriteData(to, to.length);
        mainActivity.text_write.append("向串口发送的数据："+Utils.toHexString(to,to.length)+"-----+"+retval+"\n");

    }

}
  class ReadThread extends Thread {

    private MainActivity mainActivity;
    private boolean isopen;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void run() {
        //接收串口发送的数据
        byte[] buffer = new byte[4096];
        while (true) {

            if (!isopen) {
                break;
            }
            int length = Ch340.driver.ReadData(buffer, 4096);
            if (length > 0) {
                //进行数据解析
                //将解析的数据发送出去
                String recv = Utils.toHexString(buffer, length);
                mainActivity.text_read.append("收到的串口数据：" + recv + "----" + length + "\n");
                String s=Utils.dataAnalyze(buffer);
                mainActivity.text_receive.append("pcs:--"+s+"\n");

                switch (s){
                    case "0xb8":
                    case "0x70":mainActivity.text_receive.append("强制开门事件上报..\n");break;
                    case "0xff":mainActivity.text_receive.append("报警事件上报..\n"); break;
                        default:mainActivity.text_receive.append("刷卡事件上报..\n");
                }


            }
        }
    }

      public ReadThread(MainActivity mainActivity, boolean isopen) {
          this.mainActivity = mainActivity;
          this.isopen = isopen;
      }
  }



