package com.droi.edriver.net;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.droi.edriver.app.WatchApplication;
import com.droi.edriver.bean.UserInfo;
import com.droi.edriver.tools.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GetDataFromVolly {
	public static final String TAG = "GetDataFromVolly";
	private static RequestQueue mQueue = WatchApplication.getInstance().getRequestQueue();

	private GetDataFromVolly() {
	}

	public static void execute(final int msgCode, final Handler handler, HashMap<String, String> params, String url, String tag) {
		ErrorListener mErrorListener = new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError volleyError) {
				Log.i(TAG, "request error: " + volleyError);
				Message msg = new Message();
				msg.what = NetMsgCode.MSG_FAIL;
				msg.arg1 = msgCode;
				handler.sendMessage(msg);
			}
		};
		MyRequest request = new MyRequest(msgCode, handler, params, url, mErrorListener);
		request.setTag(tag);
		request.setRetryPolicy(new DefaultRetryPolicy(4000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		mQueue.add(request);
	}

	private static class MyRequest extends Request<String> {
		private byte[] content;
		private Handler handler;
		private int msgCode;
		private HashMap<String, String> params;

		public MyRequest(int msgCode, Handler handler, HashMap<String, String> params, String url, ErrorListener errorListener) {
			super(Method.POST, url, errorListener);
			this.handler = handler;
			this.msgCode = msgCode;
			this.params = params;
			getContent();
		}

		private String buildHeadData() {
			String result = "";
			UUID uuid = UUID.randomUUID();
			Header header = new Header();
			header.setBasicVer((byte) 1);
			header.setLength(84);
			header.setType((byte) 1);
			header.setReserved((short) 0);
			header.setFirstTransaction(uuid.getMostSignificantBits());
			header.setSecondTransaction(uuid.getLeastSignificantBits());
			header.setMessageCode(msgCode);
			result = header.toString();
			return result;
		}

		private String buildBodyData() {
			JSONObject jsonObjBody = new JSONObject();
			try {
				if (params != null && params.size() > 0) {
					for (String key : params.keySet()) {
						jsonObjBody.put(key, params.get(key));
					}
				}
				return jsonObjBody.toString();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return "";
		}

		private void getContent() {
			JSONObject jsObject = new JSONObject();
			try {
				jsObject.put("head", buildHeadData());
				jsObject.put("body", buildBodyData());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			Log.i(TAG, "content:" + Tools.formatJson(jsObject.toString()));
			try {
				content = DESUtil.encrypt(jsObject.toString().getBytes("utf-8"), NetMsgCode.ENCODE_DECODE_KEY.getBytes());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public Map<String, String> getHeaders() throws AuthFailureError {
			Map<String, String> map = new HashMap<>();
			map.put("contentType", "utf-8");
			map.put("Content-Type", "application/x-www-form-urlencoded");
			map.put("Content-Length", "" + content.length);
			map.put("Connection", "close");
			return map;
		}

		@Override
		protected void deliverResponse(String result) {
			Message msg = new Message();
			msg.what = NetMsgCode.MSG_SUCCESS;
			int code = getSUCCESS(result);
			msg.arg1 = msgCode;
			Log.i(TAG, "result:" + Tools.formatJson(result));
			if (TextUtils.isEmpty(result) || result.equals("zero") || (code == -1 || code == 1)) {
				msg.what = NetMsgCode.MSG_FAIL;
				handler.sendMessage(msg);
			} else {
				switch (msgCode) {
					case NetMsgCode.UPDATE_USER_INFO:
						break;
					case NetMsgCode.GET_USER_INFO:
						JSONObject body = getBodyJson(result);
						if (body != null) {
							msg.obj = UserInfo.getInstanceByJSON(getJSONObject(body, NetMsgCode.KeyUserInfo.JSONNAME));
						}
						Log.i(TAG, "msg.obj = " + msg.obj);
						if (msg.obj != null) {
							Tools.setUserInfo((UserInfo) msg.obj);
						}
						break;
					default:
						break;
				}
				handler.sendMessage(msg);
			}
		}

		@Override
		protected Response<String> parseNetworkResponse(NetworkResponse response) {
			try {
				byte[] decrypted = DESUtil.decrypt(response.data, NetMsgCode.ENCODE_DECODE_KEY.getBytes());
				return Response.success(new String(decrypted), HttpHeaderParser.parseCacheHeaders(response));
			} catch (Exception e) {
				e.printStackTrace();
				return Response.error(new ParseError(e));
			}
		}

		@Override
		public byte[] getBody() throws AuthFailureError {
			return content;
		}
	}

	private static int getSUCCESS(String result) {
		int netResult = -1;
		JSONObject bodyObject = getBodyJson(result);
		if (bodyObject == null) {
			return netResult;
		} else {
			netResult = bodyObject.optInt("result", -1);
			return netResult;
		}
	}

	private static JSONObject getBodyJson(String jsonStr) {
		try {
			JSONObject object = new JSONObject(jsonStr);
			String body = object.optString("body");
			return new JSONObject(body);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static JSONObject getJSONObject(JSONObject body, String key) {
		try {
			String value = body.optString(key);
			return new JSONObject(value);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
}
