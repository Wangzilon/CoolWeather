package com.example.coolweather.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather {
    public Basic basic;
    public Now now;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;  //Forecast是单日天气预测信息，forecastList是多日天气预测集合
    public AQI aqi;
    public Suggestion suggestion;
    public String status;  //当前响应数据的状态（不是布尔类型）
}
