package com.example.coolweather.util;

import android.text.TextUtils;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.json.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.function.Function;

public class Utility {

    // 处理省级数据
    public static boolean handleProvinceResponse(String response) {
        return handleGenericResponse(response, jsonArray -> {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject provinceObject = jsonArray.getJSONObject(i);
                Province province = new Province();
                province.setProvinceCode(provinceObject.getInt("id"));
                province.setProvinceName(provinceObject.getString("name"));
                province.save();
            }
        });
    }

    // 处理市级数据
    public static boolean handleCityResponse(String response, int provinceCode) {
        return handleGenericResponse(response, jsonArray -> {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject cityObject = jsonArray.getJSONObject(i);
                City city = new City();
                city.setCityCode(cityObject.getInt("id"));
                city.setCityName(cityObject.getString("name"));
                city.setProvinceCode(provinceCode);
                city.save();
            }
        });
    }

    // 处理县级数据
    public static boolean handleCountyResponse(String response, int cityCode) {
        return handleGenericResponse(response, jsonArray -> {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject countyObject = jsonArray.getJSONObject(i);
                County county = new County();
                county.setCountyName(countyObject.getString("name"));
                county.setWeather_id(countyObject.getString("weather_id"));
                county.setCityCode(cityCode);
                county.save();
            }
        });
    }

    // 处理某日某县天气信息
    public static Weather handleWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, Weather.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 用于处理通用的JSON响应
    private static boolean handleGenericResponse(String response, ResponseHandler handler) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray jsonArray = new JSONArray(response);
                handler.handleResponse(jsonArray);
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // 定义函数式接口用于处理响应
    @FunctionalInterface
    private interface ResponseHandler {
        void handleResponse(JSONArray jsonArray) throws JSONException;
    }

    //处理背景图片
    public static String handleBingPicResponse(String response) {
        //处理返回的JSON数据，获取背景图片
        //try catch用于捕捉异常
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("images");
            JSONObject jsonObject1 = jsonArray.getJSONObject(0);
            String url = jsonObject1.getString("url");
            String bingPic = "https://cn.bing.com" + url;
            return bingPic;
        }catch (Exception e){
            throw new RuntimeException(e);

        }
    }
}
