package com.example.coolweather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //getSupportActionBar().hide();  //隐藏自带标题
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if(prefs.getString("weather",null) != null){
                //天气界面显示测试代码
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("weather",null);
                editor.clear();
                editor.commit();
            }
        }
    }