package com.example.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import com.example.coolweather.json.Weather;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public int onStartCommand(Intent intent, int flag, int startId) {
        // 更新对应数据
        updateWeather();
        updateBingPic();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8 * 60 * 60 * 1000;  //8小时毫秒数
        long triggerAtTime = System.currentTimeMillis() + anHour;
        // 使用Intent启动另一个服务
        Intent i = new Intent(this, AutoUpdateService.class);
        // 对Intent进行封装，使得Intent不会立即执行，而是需要满足一定条件
        PendingIntent pi = PendingIntent.getService(this,0,i,PendingIntent.FLAG_IMMUTABLE);
        // 取消原来的Intent
        manager.cancel(pi);
        // 定时执行新的Intent，第一次参数为AlarmManager，第二个参数为PendingIntent，第三个参数为时间间隔
        // ELAPSED_REALTIME_WAKEUP表示基于系统时间的闹钟
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flag, startId);
    }

    //更新缓存器内容
    private void updateWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            Weather oldWeather = Utility.handleWeatherResponse(weatherString);  //将字符串换成Weather类实例
            String oldWeatherId = oldWeather.basic.weather_id;

            String weatherUrl = "http://guolin.tech/api/weather?cityid=" + oldWeatherId + "&&key=39755a213347";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseText = response.body().string();
                    Weather newWeather = Utility.handleWeatherResponse(responseText);
                    if (newWeather != null && "ok".equals(newWeather.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather", responseText);
                        editor.apply();
                    }
                }
            });
        }
    }

    //更新缓存器中背景图片
    private void updateBingPic() {
        String requestBingPic = "https://cn.bing.com/HPImageArchive.aspx?format=js&idx=5&n=1";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String bingPicResponse = response.body().string();
                String bingPic = Utility.handleBingPicResponse(bingPicResponse);
                //更新缓存器
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();

            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }
        });
    }
}