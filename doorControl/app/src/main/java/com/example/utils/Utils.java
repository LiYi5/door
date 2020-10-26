package com.example.utils;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.http.MyHttp;

public class Utils {



    /**
     * 断开连接
     */
    public  static void closeConnect(okhttp3.WebSocket webSocket) {
        try {
            if (null != webSocket) {
                webSocket.close(1000,"");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            webSocket = null;
        }
    }

    //对收到的数据进行拆帧处理
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public  static  String dataAnalyze(byte[] bytes){
        int pdlLength=0;//payload的长度
        int x=0;//每一帧下标
        String pdlAnd="";
        String sendAnd="";
        String recAnd="";
        String pcsAnd="";
        String pc="";
        byte[]  pcs=new byte[1];

        if (bytes[5 + x] != 0) {
            //负载长度
            byte[] b1 = new byte[1];
            b1[0] = bytes[5 + x];
            //每一帧payload长度
            pdlLength=b1[0];
            //pdlLength= Integer.parseInt(toHexString(b1, 1).replace(" ",""), 16);
            //处理每一帧数据
            byte[] dataByte = new byte[pdlLength+18];
            //将第一帧的数据拷贝进新的byte
            System.arraycopy(bytes, x, dataByte, 0, pdlLength + 18);


            //对每一帧进行校验处理
            //帧头判断0xa5 0xa5 0xba 0xba
            if ((dataByte[0] == (byte)0xa5) && (dataByte[1] == (byte)0xa5) &&
                    (dataByte[2] == (byte)0xba) && (dataByte[3] == (byte)0xba)) {

                //负载长度校验
                byte[] lcs=new byte[1];
                lcs[0]=dataByte[6+x];
                //PDL长度校验结果
                pdlAnd = And(toHexString(b1, 1).trim(), toHexString(lcs, 1).trim());

                //SendID校验
                byte[] sendId=new byte[4];
                byte[]  sics=new byte[1];
                //取出senID数据
                System.arraycopy(dataByte, 7+x, sendId, 0, 4);
                //取出SICS数据
                sics[0]=dataByte[11+x];
                //获取SendID自加结果
                String sendIdAnd = byteAcc(sendId);
                //与SICS(发送方ID校验)进行相加
                sendAnd = And(sendIdAnd.replace("0x", ""), toHexString(sics, 1).trim());

                //RecID校验
                byte[] recId=new byte[4];
                byte[]  rics=new byte[1];
                //取出RecID数据
                System.arraycopy(dataByte, 12+x, recId, 0, 4);
                //取出RICS数据
                rics[0]=dataByte[16+x];
                //sendID相加结果，返回16进制
                String recIdAnd=byteAcc(recId);
                //与SICS(发送方ID校验)进行相加
                recAnd = And(recIdAnd.replace("0x", ""), toHexString(rics, 1).trim());

                //payLoad校验
                //取出payload数据
                byte[] payLoad=new byte[pdlLength];
                System.arraycopy(dataByte, 17+x, payLoad, 0, pdlLength);
                pcs[0]=dataByte[17+x+pdlLength];
                //得到payLoad数据的自加结果
                if(pdlLength!=1) {
                    String payLoadAnd = byteAcc(payLoad);
                    //与PCS进行校验
                    pcsAnd = And(payLoadAnd.replace("0x", ""), toHexString(pcs, 1).trim());
                }
                //测试用
                pc="0x"+toHexString(pcs,1).trim();
//
//            if((pcsAnd.equals("0x00"))&&(recAnd.equals("0x00"))&&(sendAnd.equals("0x00"))&&(pdlAnd.equals("0x00"))){
//                ////更新下一帧的起始下标
//                x += (pdlLength + 18);
//            }


                switch (pc){
                    //小门禁强制开门回复
                    case "0xb8":new MyHttp().getDataFromGet("http://106.13.133.229:6005/test/OpenDoorFeedback?deviceId=21&success=true");break;
                    //背夹强制开门回复
                    case "0x70":new MyHttp().getDataFromGet("http://106.13.133.229:6005/test/OpenDoorFeedback?deviceId=22&success=true");break;
                    //报警事件上报
                    case "0xff":new MyHttp().getDataFromGet("http://106.13.133.229:6005/home/AddMsg?msg=报警事件"); break;
                    //刷卡事件上报
                    default:new MyHttp().getDataFromGet("http://106.13.133.229:6005/test/OpenDoorRecordAdd?cardNumber="+Utils.toHexString(payLoad,payLoad.length));
                }


//                    return ""+toHexString(payLoad,payLoad.length);
            }
                else {

                    return "error";
                }

        }
            else {
                return "error";
            }
        return  pc;
    }




    //进行byte数组的累加运算
    public  static   String byteAcc(byte[] b1){
        //将byte转成数组
        String[] s = toHexString(b1, b1.length).split(" ");
        String and=And(s[0],s[1]);
        for (int i = 2; i <b1.length; i++) {
            and=And(and.substring(and.length()-2).trim(),s[i]);

        }
        if(and.length()<2){
            and="0"+and;
        }
        return  "0x"+and.substring(and.length()-2).trim();


    }



    public static   String And(String strHex_X,String strHex_Y){
        //将x、y转成二进制形式
        String anotherBinary=Integer.toBinaryString(Integer.valueOf(strHex_X,16));
        String thisBinary=Integer.toBinaryString(Integer.valueOf(strHex_Y,16));
        StringBuilder result = new StringBuilder();

        int a=0;
        int b=0;
        int sum=0;
        int carry=0;
        //判断是否为8位二进制，否则左补零
        if(anotherBinary.length() != 8){
            for (int i = anotherBinary.length(); i <8; i++) {
                anotherBinary = "0"+anotherBinary;
            }
        }
        if(thisBinary.length() != 8){
            for (int i = thisBinary.length(); i <8; i++) {
                thisBinary = "0"+thisBinary;
            }
        }


        //二进制加法
        for(int i=anotherBinary.length()-1;i>=0;i--){
            a=anotherBinary.charAt(i)-'0';
            b=thisBinary.charAt(i)-'0';
            sum=a+b+carry;
            //进位
            if(sum>=2){
                result.append(sum-2) ;
                carry=1;
            }else {
                result.append( sum);
                carry=0;
            }

        }

        if(carry==1){
            result.append("1");
        }

        String s = Integer.toHexString(Integer.parseInt(result.reverse().toString(), 2));


        if(s.length()<2){
            s="0"+s;
        }
        return "0x"+s.substring(s.length()-2);

    }


    //将16进制byte类型转成字符串类型（字符串不包含0x）
    public static String toHexString(byte[] arg, int length) {
        String result ="";
        if (arg != null) {
            for (int i = 0; i < length; i++) {
                result = result
                        + (Integer.toHexString(
                        arg[i] < 0 ? arg[i] + 256 : arg[i]).length() == 1 ? "0"
                        + Integer.toHexString(arg[i] < 0 ? arg[i] + 256
                        : arg[i])
                        : Integer.toHexString(arg[i] < 0 ? arg[i] + 256
                        : arg[i])) + " ";
            }
            return result;
        }
        return "";
    }


    public static  String convertHexToString(String hex) {

        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for (int i = 0; i < hex.length() - 1; i += 2) {

            //grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            //convert the decimal to character
            sb.append((char) decimal);

            temp.append(decimal);
        }

        return temp.toString();
    }


    //将字符串转成byte数组
    public static byte[] toByteArray2(String arg) {
        if (arg != null) {
            char[] NewArray = new char[1000];
            char[] array = arg.toCharArray();
            int length = 0;

            for (int i = 0; i < array.length; i++) {
                if (array[i] != ' ') {
                    NewArray[length] = array[i];
                    length++;
                }
            }

            byte[] byteArray = new byte[length];
            for (int i = 0; i < length; i++) {
                byteArray[i] = (byte)NewArray[i];
            }
            return byteArray;

        }
        return new byte[] {};
    }



    //将两个16进制的字符数数字进行异或运算，以16进制字符串形式返回出去。
    public static String xor(String strHex_X,String strHex_Y){
        //将x、y转成二进制形式
        String anotherBinary=Integer.toBinaryString(Integer.valueOf(strHex_X,16));
        String thisBinary=Integer.toBinaryString(Integer.valueOf(strHex_Y,16));
        String result = "";
        //判断是否为8位二进制，否则左补零
        if(anotherBinary.length() != 8){
            for (int i = anotherBinary.length(); i <8; i++) {
                anotherBinary = "0" + anotherBinary;
            }
        }
        if(thisBinary.length() != 8){
            for (int i = thisBinary.length(); i <8; i++) {
                thisBinary = "0" + thisBinary;
            }
        }
        //异或运算
        for(int i=0;i<anotherBinary.length();i++){
            //如果相同位置数相同，则补0，否则补1
            if(thisBinary.charAt(i)==anotherBinary.charAt(i))
                result += "0";
            else{
                result += "1";
            }

        }
        //Integer.parseInt(result, 2):将2进展result转为10进制数
        //Integer.toHexString：将10进制数字转为16进制字符串
        return "0x"+Integer.toHexString(Integer.parseInt(result, 2));
    }

    //将字符串转成byte数组
    public static byte[] toByteArray(String arg) {
        if (arg != null) {
            char[] NewArray = new char[1000];
            char[] array = arg.toCharArray();
            int length = 0;
            for (int i = 0; i < array.length; i++) {
                if (array[i] != ' ') {
                    NewArray[length] = array[i];
                    length++;
                }
            }
            int EvenLength = (length % 2 == 0) ? length : length + 1;
            if (EvenLength != 0) {
                int[] data = new int[EvenLength];
                data[EvenLength - 1] = 0;
                for (int i = 0; i < length; i++) {
                    if (NewArray[i] >= '0' && NewArray[i] <= '9') {
                        data[i] = NewArray[i] - '0';
                    } else if (NewArray[i] >= 'a' && NewArray[i] <= 'f') {
                        data[i] = NewArray[i] - 'a' + 10;
                    } else if (NewArray[i] >= 'A' && NewArray[i] <= 'F') {
                        data[i] = NewArray[i] - 'A' + 10;
                    }
                }
                byte[] byteArray = new byte[EvenLength / 2];
                for (int i = 0; i < EvenLength / 2; i++) {
                    byteArray[i] = (byte) (data[i * 2] * 16 + data[i * 2 + 1]);
                }
                return byteArray;
            }
        }
        return new byte[] {};
    }


}
