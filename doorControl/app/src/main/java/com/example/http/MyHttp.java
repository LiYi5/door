package com.example.http;

import android.os.Build;
import android.os.Message;

import androidx.annotation.RequiresApi;

import com.example.doorcontrol.MainActivity;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.doorcontrol.MainActivity.JSON;

public class MyHttp {

    public  OkHttpClient client = new OkHttpClient();

    public  JSONObject json=new JSONObject();

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public  void getDataFromGet(final String url){
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                   get(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

//    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//    public void getDataFromPost(String s) throws JSONException {
//
//        json.put("xx",s);
//        new Thread(){
//            @Override
//            public void run() {
//                super.run();
//                try {
//                    post("http://jsonplaceholder.typicode.com/posts",String.valueOf(json));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();
//
//    }

    /*
                http get
             */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public   String get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }



    /*
        http Post
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public  String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }


}
