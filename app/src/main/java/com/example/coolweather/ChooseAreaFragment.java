package com.example.coolweather;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private int currentLevel;  //当前的访问状态，对应于LEVEL_PROVINCE，LEVEL_CITY，LEVEL_COUNTY
    private TextView titleText;
    private Button back_btn;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private ProgressDialog progressDialog;  //进度加载对话框

    //当前的省
    private Province currentProvince;
    //当前的市
    private City currentCity;
    //所有的省
    private List<Province> provinceList;
    //所有的市
    private List<City> cityList;
    //所有的县
    private List<County> countyList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);

        //获取控件
        back_btn = (Button) view.findViewById(R.id.back_btn);
        listView = (ListView) view.findViewById(R.id.list_view);
        titleText = (TextView) view.findViewById(R.id.title_text);

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        requireActivity().getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event.getTargetState() == Lifecycle.State.CREATED) {
                    //视图加载之初，检索省级数据并显示
                    queryProvince();
                    //添加点击事件
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            //如果列表显示的是省级数据，则点击某省对应的市级数据
                            if (currentLevel == LEVEL_PROVINCE) {
                                currentProvince = provinceList.get(i);
                                queryCity();
                                //如果列表显示的是市级数据，则点击某省对应的县级数据
                            } else if (currentLevel == LEVEL_CITY) {
                                currentCity = cityList.get(i);
                                queryCounty();
                            }
                            //如果列表显示的是县级数据，则点击某县显示对应的天气数据
                            else if (currentLevel == LEVEL_COUNTY) {
                                String weatherId = countyList.get(i).getWeather_id();
                                //通过Intent向天气界面传递数据
                                Intent intent = new Intent(getActivity(), WeatherActivity.class);
                                intent.putExtra("weather_id", weatherId);
                                startActivity(intent);  //启动WeatherActivity
                                getActivity().finish(); //关闭当前活动
                            }
                        }
                    });

                    //添加点击返回键事件
                    back_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //如果当前显示市级数据，则点击返回键显示省级数据
                            if (currentLevel == LEVEL_CITY) {
                                queryProvince();
                            } else if (currentLevel == LEVEL_COUNTY) {
                                queryCity();
                            }
                        }
                    });
                    requireActivity().getLifecycle().removeObserver(this);
                }
            }
        });
    }

    //从服务器获取数据
    public void queryFromService(String address, final String type) {
        //显示进度对话框
        showProgressDialog();
        //调用函数，访问服务器
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                //判断是哪种数据，然后调用相应的数据处理方法
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, currentProvince.getProvinceCode());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, currentCity.getCityCode());
                }
                //数据存储成功后，读取数据库，并显示在界面上
                if (result) {
                    //涉及到UI界面，切换回到主线程进行相应的处理
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //关闭进度对话框
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvince();
                            } else if ("city".equals(type)) {
                                queryCity();
                            } else if ("county".equals(type)) {
                                queryCounty();
                            }
                        }
                    });
                }
            }
        });
    }

    /*
    1.读取所有信息，并显示在UI上
    2.如果数据库有信息，则读取数据库
    3.如果数据库无信息，则连接服务器读取数据库并存储，再读取数据库
     */

    /*获取省级数据*/
    public void queryProvince() {
        titleText.setText("中国");
        back_btn.setVisibility(View.GONE);  //显示省级数据时，不显示返回键
        //读取数据库，并判断是否为空
        provinceList = LitePal.findAll(Province.class);
        //如果数据库有数据，直接读取并显示
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            //页面显示数据
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromService(address, "province");
        }
    }

    /*获取市级数据*/
    public void queryCity() {
        titleText.setText(currentProvince.getProvinceName());
        back_btn.setVisibility(View.VISIBLE);  //显示市级数据时，显示返回键
        //优先读取数据库，并判断是否为空  SQL:select * from City where provinceCode = currentProvince.provinceCode
        cityList = LitePal.where("provinceCode=?", String.valueOf(currentProvince.getProvinceCode())).find(City.class);
        //判断数据库是否为空，如果数据库有数据，直接读取并显示
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            //页面显示
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            //更新当前数据访问的状态
            currentLevel = LEVEL_CITY;
        } else {
            //数据库为空，则去访问服务器
            String address = "http://guolin.tech/api/china/" + currentProvince.getProvinceCode();
            queryFromService(address, "city");
        }
    }

    /*获取县级数据*/
    public void queryCounty() {
        titleText.setText(currentCity.getCityName());
        back_btn.setVisibility(View.VISIBLE);  //显示县级数据时，显示返回键
        //优先读取数据库，并判断是否为空  SQL:select * from County where cityCode = currentCity.cityCode
        countyList = LitePal.where("cityCode=?", String.valueOf(currentCity.getCityCode())).find(County.class);
        //判断数据库是否为空，如果数据库有数据，直接读取并显示
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            //页面显示
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            //更新当前数据访问的状态
            currentLevel = LEVEL_COUNTY;
        } else {
            //数据库为空，则去访问服务器
            String address = "http://guolin.tech/api/china/" + currentProvince.getProvinceCode() + "/" + currentCity.getCityCode();
            queryFromService(address, "county");
        }
    }

    //加载时显示进度对话框
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);  //用户点击对话框外部，对话框无法取消
        }
        progressDialog.show();
    }

    //关闭进度对话框
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}



