package com.example.coolweather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //getSupportActionBar().hide();  //隐藏自带标题

        //从缓存器中获取数据
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //判断是否有天气数据，若有，则直接显示天气界面
        if (prefs.getString("weather", null) != null) {

            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();

//                //天气界面显示测试代码
//                SharedPreferences.Editor editor = prefs.edit();
//                editor.putString("weather",null);
//                editor.clear();
//                editor.commit();
        }
    }
}