package com.example.coolweather.json;

import com.google.gson.annotations.SerializedName;

public class Basic {
    @SerializedName("city")
    public String cityName;
    public Update update;
    @SerializedName("id")
    public String weather_id;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
