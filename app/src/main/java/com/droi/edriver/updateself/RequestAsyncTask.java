package com.droi.edriver.updateself;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.droi.edriver.net.DESUtil;
import com.droi.edriver.net.Header;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;

public class RequestAsyncTask extends AsyncTask<Object, Object, String> {
	private Context mContext;

	private Handler mHandler;

	private int mMsgWhat;

	private boolean mStarting = false;

	private int mMessageCode = 0;

	public static String ENCODE_DECODE_KEY = "x_s0_s22";

	public static final String SelfUpdateUrl = "http://update-market.tt286.com:2520";
	//public static final String SelfUpdateUrl = "http://101.95.97.178:2520";

	/**
	 * 根据新市场协议中6.2.1.3 getApkUpdateReq 自更新请求定�?
	 */
	public static final int SelfUpdateMsgCode = 103001;

	public static String mAppId = ""; // 应用程序标识�?
	public static String mChId = ""; // 通道标识�?

	public RequestAsyncTask(Context context, Handler handler, int msgWhat, String appid, String chnid) {
		mContext = context;
		mHandler = handler;
		mMsgWhat = msgWhat;
		mAppId = appid;
		mChId = chnid;
	}

	protected String doInBackground(Object... params) {
		String result = "";
		String url = "";
		String contents = "";

		url = (String) params[0];

		mMessageCode = (Integer) params[1];

		contents = buildToJSONData(mContext, mMessageCode);// 103001
		Log.i("gchk", contents);
		try {
			result = accessNetworkByPost(url, contents);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	protected void onCancelled() {
		mStarting = false;
		super.onCancelled();
	}

	protected void onPostExecute(String result) {
		Log.i("gchk", "onPostExecute = " + result);
		HashMap<String, Object> map = null;
		int msgCode = -1;
		if (!TextUtils.isEmpty(result)) {
			map = new ApkCheckSelfUpdateCodec().splitMySelfData(result);
			if (map == null || map.size() == 0) {
				msgCode = -1;
			} else if (map.containsKey("errorCode")) {
				msgCode = Integer.valueOf(map.get("errorCode").toString());
			}
		}
		try {
			Message msg = new Message();
			msg.what = mMsgWhat;
			msg.arg1 = msgCode;
			msg.obj = map;
			mHandler.sendMessage(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onPostExecute(result);
	}

	public void startRun() {
		if (!mStarting) {
			mStarting = true;
			execute(SelfUpdateUrl, SelfUpdateMsgCode);
		}
	}

	private String buildToJSONData(Context context, int msgCode) {
		String result = "";
		String body = "";

		JSONObject jsObject = new JSONObject();

		if (context == null)
			return result;

		TerminalInfo terminalInfo = TerminalInfo.generateTerminalInfo(context, mAppId, mChId);
		body = terminalInfo.toString();
		try {
			jsObject.put("head", buildHeadData(msgCode));
			jsObject.put("body", body);
			result = jsObject.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	private String buildHeadData(int msgCode) {
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

	private String accessNetworkByPost(String urlString, String contents) throws IOException {
		String line = "";
		DataOutputStream out = null;
		URL postUrl;

		BufferedInputStream bis = null;
		ByteArrayBuffer baf = null;
		boolean isPress = false;
		HttpURLConnection connection = null;

		try {
			byte[] encrypted = DESUtil.encrypt(contents.getBytes("utf-8"), ENCODE_DECODE_KEY.getBytes());
			postUrl = new URL(urlString);
			connection = (HttpURLConnection) postUrl.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setConnectTimeout(10000);
			connection.setReadTimeout(20000);
			connection.setRequestMethod("POST");
			connection.setInstanceFollowRedirects(true);
			connection.setRequestProperty("contentType", "utf-8");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length", "" + encrypted.length);

			out = new DataOutputStream(connection.getOutputStream());
			out.write(encrypted);
			out.flush();
			out.close();

			bis = new BufferedInputStream(connection.getInputStream());
			baf = new ByteArrayBuffer(1024);

			isPress = Boolean.valueOf(connection.getHeaderField("isPress"));

			int current = 0;

			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}
			if (baf.length() > 0) {
				byte unCompressByte[];
				byte[] decrypted;
				if (isPress) {
					Log.e("shuaiqingDe@@@@", "compress length:" + baf.length());
					unCompressByte = ZipUtil.uncompress(baf.toByteArray());
					Log.e("shuaiqingDe@@@@", "length:" + unCompressByte.length);
					decrypted = DESUtil.decrypt(unCompressByte, ENCODE_DECODE_KEY.getBytes());
				} else
					decrypted = DESUtil.decrypt(baf.toByteArray(), ENCODE_DECODE_KEY.getBytes());
				line = new String(decrypted);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null)
				connection.disconnect();
			if (bis != null)
				bis.close();
			if (baf != null)
				baf.clear();
		}
		return line.trim();
	}
}
