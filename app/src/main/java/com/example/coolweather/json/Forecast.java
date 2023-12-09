package com.example.coolweather.json;

import com.google.gson.annotations.SerializedName;

public class Forecast {
    //这里只定义单日预测天气实体类
    public String date;
    @SerializedName("cond")
    public More more;
    @SerializedName("tmp")
    public Temperature temperature;
    public class More{
        @SerializedName("txt_d")
        public String info;
    }
    public class Temperature{
        public String max;
        public String min;
    }
}
