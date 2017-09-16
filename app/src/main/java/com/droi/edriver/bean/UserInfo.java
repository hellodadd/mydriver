package com.droi.edriver.bean;

import com.droi.edriver.net.NetMsgCode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by ZhuQichao on 2016/3/15.
 */
public class UserInfo implements Serializable {

	private String openid;
	private String nickName;
	private int sex;//0男，1女
	private String birth;//生日：yyyy-MM-dd
	private String car;

	public UserInfo() {
	}

	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public String getBirth() {
		return birth;
	}

	public void setBirth(String birth) {
		this.birth = birth;
	}

	public String getCar() {
		return car;
	}

	public void setCar(String car) {
		this.car = car;
	}

	public HashMap<String, String> getUpdateParam() {
		HashMap<String, String> param = new HashMap<>();
		param.put(NetMsgCode.KeyUserInfo.OPENID, openid + "");
		param.put(NetMsgCode.KeyUserInfo.NAME, nickName + "");
		param.put(NetMsgCode.KeyUserInfo.SEX, sex + "");
		param.put(NetMsgCode.KeyUserInfo.BIRTH, birth + "");
		param.put(NetMsgCode.KeyUserInfo.CAR, car + "");
		return param;
	}

	public static UserInfo getInstanceByJSON(JSONObject result) {
		if (result == null) {
			return null;
		}
		UserInfo value = new UserInfo();
		try {
			value.openid = result.getString(NetMsgCode.KeyUserInfo.OPENID);
			value.nickName = result.getString(NetMsgCode.KeyUserInfo.NAME);
			value.sex = result.getInt(NetMsgCode.KeyUserInfo.SEX);
			value.birth = result.getString(NetMsgCode.KeyUserInfo.BIRTH);
			value.car = result.getString(NetMsgCode.KeyUserInfo.CAR);
			return value;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		UserInfo userInfo = (UserInfo) o;

		if (sex != userInfo.sex) return false;
		if (!birth.equals(userInfo.birth)) return false;
		if (!openid.equals(userInfo.openid)) return false;
		if (!nickName.equals(userInfo.nickName)) return false;
		return car.equals(userInfo.car);
	}

	@Override
	public int hashCode() {
		int result = openid.hashCode();
		result = 31 * result + nickName.hashCode();
		result = 31 * result + sex;
		result = 31 * result + birth.hashCode();
		result = 31 * result + car.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "UserInfo{" +
				"openid='" + openid + '\'' +
				", nickName='" + nickName + '\'' +
				", sex=" + sex +
				", birth=" + birth +
				", car='" + car + '\'' +
				'}';
	}
}
