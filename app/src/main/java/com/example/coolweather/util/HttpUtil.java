package com.example.coolweather.util;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {

    // 创建一个OkHttpClient实例用于复用
    private static OkHttpClient client = new OkHttpClient();

    // 连接服务器，并获取数据
    public static void sendOkHttpRequest(String address, Callback callback) {
        try {
            Request request = new Request.Builder().url(address).build();
            client.newCall(request).enqueue(callback);
        } catch (Exception e) {
            e.printStackTrace();
            // 这里可以添加更具体的异常处理，比如回调一个错误处理函数
        }
    }
}
