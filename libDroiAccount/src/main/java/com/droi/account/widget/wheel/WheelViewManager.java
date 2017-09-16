package com.droi.account.widget.wheel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.droi.account.DebugUtils;
import com.droi.account.Utils;
import com.droi.account.widget.wheel.adapters.ArrayWheelAdapter;

public class WheelViewManager implements OnWheelChangedListener{
	private static final String TAG = "WheelViewManager";
	/**
	 * 把全国的省市区的信息以json的格式保存，解析完成后赋值为null
	 */
	private JSONObject mJsonObj;
	/**
	 * 省的WheelView控件
	 */
	private final WheelView mProvince;
	/**
	 * 市的WheelView控件
	 */
	private final WheelView mCity;
	/**
	 * 区的WheelView控件
	 */
	private final WheelView mArea;

	/**
	 * 所有省
	 */
	private String[] mProvinceDatas;
	/**
	 * key - 省 value - 市s
	 */
	private Map<String, String[]> mCitisDatasMap = new HashMap<String, String[]>();
	/**
	 * key - 市 values - 区s
	 */
	private Map<String, String[]> mAreaDatasMap = new HashMap<String, String[]>();

	/**
	 * 当前省的名称
	 */
	private String mCurrentProviceName;
	/**
	 * 当前市的名称
	 */
	private String mCurrentCityName;
	/**
	 * 当前区的名称
	 */
	private String mCurrentAreaName ="";
	
	private Context mContext;
	
	public WheelViewManager(Context context, WheelView provice, WheelView city, WheelView area){
		mContext = context.getApplicationContext();
		mProvince = provice;
		mCity = city;
		mArea = area;
		initJsonData("lib_droi_account_droi_city.json");
		initDatas();

		mProvince.setViewAdapter(new ArrayWheelAdapter<String>(mContext, mProvinceDatas));
		// 添加change事件
		mProvince.addChangingListener(this);
		// 添加change事件
		mCity.addChangingListener(this);
		// 添加change事件
		mArea.addChangingListener(this);

		mProvince.setVisibleItems(5);
		mCity.setVisibleItems(5);
		mArea.setVisibleItems(5);
		updateCities();
		updateAreas();
	}
	
	public WheelViewManager(Context context, WheelView province, WheelView city, WheelView area, 
			String initProvince, String initCity, String initArea){
		mContext = context.getApplicationContext();
		mProvince = province;
		mCity = city;
		mArea = area;
		initJsonData("lib_droi_account_droi_city.json");
		initDatas();

		mProvince.setViewAdapter(new ArrayWheelAdapter<String>(mContext, mProvinceDatas));
		// 添加change事件
		mProvince.addChangingListener(this);
		// 添加change事件
		mCity.addChangingListener(this);
		// 添加change事件
		mArea.addChangingListener(this);

		mProvince.setVisibleItems(5);
		mCity.setVisibleItems(5);
		mArea.setVisibleItems(5);
		updateCities(initProvince, initCity, initArea);
	}
	
	private void setInitState(String province, String city, String area){
		List<String> provincelist = Arrays.asList(mProvinceDatas);
		int provinceIndex = provincelist.indexOf(province);
		
		String[] citiesDatas = mCitisDatasMap.get(province);
		if(citiesDatas != null){
			List<String> citylist = Arrays.asList(citiesDatas);
			int cityIndex = citylist.indexOf(city);
			
			String[] areaDatas = mAreaDatasMap.get(city);
			if(areaDatas != null){
				List<String> arealist = Arrays.asList(areaDatas);
				int areaIndex = arealist.indexOf(area);
				
			}
		}

	}
	
	/**
	 * 根据当前的市，更新区WheelView的信息
	 */
	private void updateAreas(){
		int pCurrent = mCity.getCurrentItem();
		mCurrentCityName = mCitisDatasMap.get(mCurrentProviceName)[pCurrent];
		String[] areas = mAreaDatasMap.get(mCurrentCityName);
		if (areas == null){
			areas = new String[] { "" };
		}
		mArea.setViewAdapter(new ArrayWheelAdapter<String>(mContext, areas));
		mArea.setCurrentItem(0);
		mCurrentAreaName = areas[0];
	}

	/**
	 * 根据当前的市，更新区WheelView的信息
	 */
	private void updateAreas(String area){
		
		int pCurrent = mCity.getCurrentItem();
		mCurrentCityName = mCitisDatasMap.get(mCurrentProviceName)[pCurrent];
		String[] areas = mAreaDatasMap.get(mCurrentCityName);
		int index = 0;
		if (areas == null){
			areas = new String[] { "" };
		}else if(!TextUtils.isEmpty(area)){
			List<String> arealist = Arrays.asList(areas);
			index = arealist.indexOf(area);
		}
		
		mArea.setViewAdapter(new ArrayWheelAdapter<String>(mContext, areas));
		mArea.setCurrentItem(index);
	}
	
	/**
	 * 根据当前的省，更新市WheelView的信息
	 */
	private void updateCities(String province, String city, String area)	{
		if(!TextUtils.isEmpty(province)){
			List<String> provincelist = Arrays.asList(mProvinceDatas);
			int provinceIndex = provincelist.indexOf(province);
			mProvince.setCurrentItem(provinceIndex);
		}
		int pCurrent = mProvince.getCurrentItem();
		mCurrentProviceName = mProvinceDatas[pCurrent];
		String[] cities = mCitisDatasMap.get(mCurrentProviceName);
		int cityIndex = 0;
		if (cities == null){
			cities = new String[] {""};
		}else if(!TextUtils.isEmpty(city)){
			List<String> citylist = Arrays.asList(cities);
			cityIndex = citylist.indexOf(city);
		}
		mCity.setViewAdapter(new ArrayWheelAdapter<String>(mContext, cities));
		mCity.setCurrentItem(cityIndex);
		updateAreas(area);
	}
	
	/**
	 * 根据当前的省，更新市WheelView的信息
	 */
	private void updateCities()	{
		int pCurrent = mProvince.getCurrentItem();
		mCurrentProviceName = mProvinceDatas[pCurrent];
		String[] cities = mCitisDatasMap.get(mCurrentProviceName);
		if (cities == null){
			cities = new String[] {""};
		}
		mCity.setViewAdapter(new ArrayWheelAdapter<String>(mContext, cities));
		mCity.setCurrentItem(0);
		updateAreas();
	}

	/**
	 * 解析整个Json对象，完成后释放Json对象的内存
	 */
	private void initDatas() {
		try	{
			JSONArray jsonArray = mJsonObj.getJSONArray("citylist");
			mProvinceDatas = new String[jsonArray.length()];
			for (int i = 0; i < jsonArray.length(); i++){
				JSONObject jsonP = jsonArray.getJSONObject(i);// 每个省的json对象
				String province = jsonP.getString("p");// 省名字
				mProvinceDatas[i] = province;
				JSONArray jsonCs = null;
				try	{
					/**
					 * Throws JSONException if the mapping doesn't exist or is
					 * not a JSONArray.
					 */
					jsonCs = jsonP.getJSONArray("c");
				} catch (Exception e1)	{
					continue;
				}
				String[] mCitiesDatas = new String[jsonCs.length()];
				for (int j = 0; j < jsonCs.length(); j++) {
					JSONObject jsonCity = jsonCs.getJSONObject(j);
					String city = jsonCity.getString("n");// 市名字
					mCitiesDatas[j] = city;
					JSONArray jsonAreas = null;
					try	{
						/**
						 * Throws JSONException if the mapping doesn't exist or
						 * is not a JSONArray.
						 */
						jsonAreas = jsonCity.getJSONArray("a");
					} catch (Exception e)	{
						continue;
					}

					String[] mAreasDatas = new String[jsonAreas.length()];// 当前市的所有区
					//logMsg(city+": " +jsonAreas.toString());
					for (int k = 0; k < jsonAreas.length(); k++){
						String area = jsonAreas.getJSONObject(k).getString("s");// 区域的名称
						mAreasDatas[k] = area;
					}
					mAreaDatasMap.put(city, mAreasDatas);
				}
				mCitisDatasMap.put(province, mCitiesDatas);
			}

		} catch (JSONException e){
			e.printStackTrace();
		}
		mJsonObj = null;
	}

	/**
	 * 从assert文件夹中读取省市区的json文件，然后转化为json对象
	 */
	private void initJsonData()	{
		try	{
			StringBuffer sb = new StringBuffer();
			InputStream is = mContext.getAssets().open("city.json");
			int len = -1;
			byte[] buf = new byte[1024];
			while ((len = is.read(buf)) != -1)	{
				sb.append(new String(buf, 0, len, "gbk"));
			}
			is.close();
			mJsonObj = new JSONObject(sb.toString());
		} catch (IOException e)	{
			e.printStackTrace();
		} catch (JSONException e){
			e.printStackTrace();
		}
	}

	private void initJsonData(String fileName){
		BufferedReader reader = null;
		try	{
			StringBuffer sb = new StringBuffer();
			InputStream is = mContext.getApplicationContext().getAssets().open(fileName);
			InputStreamReader inputStreamReader = new InputStreamReader(is, "gbk");
			reader = new BufferedReader(inputStreamReader);
			String tempString = null;
			while((tempString = reader.readLine()) != null){
				sb.append(tempString);
			}
			is.close();
			mJsonObj = new JSONObject(sb.toString());
		} catch (IOException e)	{
			e.printStackTrace();
		} catch (JSONException e){
			e.printStackTrace();
		}
		if(reader != null){
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "initJsonData mJsonObj = " + mJsonObj);
		}
	}
	
	@Override
	public void onChanged(WheelView wheel, int oldValue, int newValue) {
		// TODO Auto-generated method stub
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "oldValue = " + oldValue + ", newValue = " + newValue);
		}
		if (wheel == mProvince) {
			updateCities();
			wheel.updateTextColor(mCurrentProviceName);
		} else if (wheel == mCity) {
			updateAreas();
			wheel.updateTextColor(mCurrentCityName);
		} else if (wheel == mArea) {
			mCurrentAreaName = mAreaDatasMap.get(mCurrentCityName)[newValue];
			wheel.updateTextColor(mCurrentAreaName);
		}
		
	}
	
	public String getSelectedInfo(){
		String info = "";
		if(!TextUtils.isEmpty(mCurrentProviceName)){
			info += mCurrentProviceName;
		}
		if(!TextUtils.isEmpty(mCurrentCityName)){
			info += ("-"+mCurrentCityName);
		}
		if(!TextUtils.isEmpty(mCurrentAreaName)){
			info += ("-"+mCurrentAreaName);
		}else{
			
		}
		return info;
	}
	
	private void showMessage(String msg){
		Utils.showMessage(mContext, msg);
	}

}
