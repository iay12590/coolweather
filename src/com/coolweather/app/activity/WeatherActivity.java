package com.coolweather.app.activity;

import com.coolweather.app.R;
import com.coolweather.app.service.AutoUpdateService;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.HttpUtil.HttpCallbackListener;
import com.coolweather.app.util.Utility;

import android.R.string;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity implements OnClickListener{
	
	private LinearLayout weatherInfoLayout;
	
	/**
	 * 用于显示城市名
	 */
	private TextView cityNameText;
	
	/**
	 * 用于显示发布时间
	 */
	private TextView publishText;
	
	/**
	 * 用于显示天气描述信息
	 */
	private TextView weatherDespText;
	
	/**
	 * 用于显示气温1
	 */
	private TextView temp1Text;
	
	/**
	 * 用于显示气温2
	 */
	private TextView temp2Text;
	
	/**
	 * 用于显示当前日期
	 */
	private TextView currentDateText;
	
	/**
	 * 切换城市
	 */
	
	private Button switchCity;
	
	/**
	 * 更新天气
	 */
	
	private Button refreshWeather; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		
		//初始化控件
		
		weatherInfoLayout = (LinearLayout)findViewById(R.id.weather_info_layout);
		cityNameText = (TextView)findViewById(R.id.city_name);
		publishText = (TextView)findViewById(R.id.publish_text);
		temp1Text = (TextView)findViewById(R.id.temp1);
		temp2Text = (TextView)findViewById(R.id.temp2);
		weatherDespText = (TextView)findViewById(R.id.weather_desp);
		currentDateText =(TextView)findViewById(R.id.current_date);
		
		String countyCode = getIntent().getStringExtra("county_code");
		
		//Log.d("WeatherActivity", countyCode);
		
		if(!TextUtils.isEmpty(countyCode)){
			
			//countyCode不为空，查询天气
			publishText.setText("同步中......");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		}else{
			//没有县级代号直接显示本地天气
			showWeather();
		}
		
		
		switchCity = (Button)findViewById(R.id.home);
		refreshWeather = (Button)findViewById(R.id.refresh_weather);
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
	}
	/**
	 * 查询县级代号天气
	 */
	private void queryWeatherCode(String countyCode){
		String address = "http://www.weather.com.cn/data/list3/city"+countyCode+".xml";
		queryFromServer(address, "countyCode");
	}
	
	/**
	 * 查询天气代号所对应的天气
	 */
	
	private void queryweatherInfo(String weatherCode){
		//String address = "http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
		//String address2 = "http://www.weather.com.cn/data/list3/city020104.xml";
		
		String address = "http://weather.123.duba.net/static/weather_info/"+weatherCode+".html";
		queryFromServer(address, "weatherCode");
	}
	
	/**
	 * 根据传入的地址和类型去向服务器查询天气代号或者天气信息
	 */
	private void queryFromServer(final String address,final String type){
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				if("countyCode".equals(type)){
					if(!TextUtils.isEmpty(response)){
						String[] array = response.split("\\|");
						if(array!=null&&array.length==2){
							String weatherCode = array[1];
							queryweatherInfo(weatherCode);
						}
					}
				}else if("weatherCode".equals(type)){
					//处理服务器返回的天气信息
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					runOnUiThread(new Runnable() {
						public void run() {
							showWeather();
						}
					});
				}
				
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					public void run() {
						publishText.setText("同步失败");
					}
				});
				
			}
		});
	}
	
	
	/**
	 * 查询本地天气
	 */
	private void showWeather(){
		SharedPreferences pres = PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(pres.getString("city_name", ""));
		temp1Text.setText(pres.getString("temp1", ""));
		temp2Text.setText(pres.getString("temp2", ""));
		weatherDespText.setText(pres.getString("weather_desp", ""));
		publishText.setText(pres.getString("publish_time", "")+"发布");
		currentDateText.setText(pres.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		
		Intent intent = new Intent(this,AutoUpdateService.class);
		startService(intent);
	}
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.home:
			Intent intent = new Intent(this,ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather:
			publishText.setText("同步中....");
			
			Log.d("WeatherActivity","test11");
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			
			Log.d("WeatherActivity","test22");
			
			String weatherCode = prefs.getString("weather_code", "yyy");
			
			Log.d("WeatherActivity",weatherCode);
			
			if(!TextUtils.isEmpty(weatherCode)){
				queryweatherInfo(weatherCode);
				Log.d("WeatherActivity","test");
			}
			break;
		default:
			break;
		}
		
	}

	/**
	 * 
	 */
}
