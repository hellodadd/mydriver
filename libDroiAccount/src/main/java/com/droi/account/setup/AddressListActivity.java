package com.droi.account.setup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.TextView;

import com.droi.account.DebugUtils;

import com.droi.account.Utils;
import com.droi.account.authenticator.Constants;
import com.droi.account.login.BaseActivity;
import com.droi.account.login.SharedInfo;
import com.droi.account.netutil.HttpOperation;
import com.droi.account.netutil.MD5Util;


public class AddressListActivity extends BaseActivity implements OnItemClickListener{

	private static final String TAG = "AddressListActivity";
	private static final int LIST_ITEM_TYPE_NORMAL = 0;
	private static final int LIST_ITEM_TYPE_ADD = 1;
	private static final int TYPE_COUNT  = 2;
	
	//request code
	private static final int EDIT_ITEM = 1;
	//
	private static final int POPUPMENU_ITEM_MODIFY = 0;
	private static final int POPUPMENU_ITEM_DELETE = 1;
	//
	private static final int LIST_ITEMS_MAX = 6;
	private ListView mListView;
	private ArrayList<AddressInfo> mData = new ArrayList<AddressInfo>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(mMyResources.getLayout("lib_droi_account_layout_my_address"));
		setupView();
		getAddress();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	private void setupView(){
		mListView = (ListView)findViewById(mMyResources.getId("lib_droi_account_listView"));
		mListView.setAdapter(mAdapter);
	}
	
	private BaseAdapter mAdapter = new BaseAdapter() {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mData.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public int getViewTypeCount() {
			return TYPE_COUNT;
		};
		
		@Override
		public int getItemViewType(int position) {
			
			AddressInfo addressInfo = mData.get(position);
			if("-1".equals(addressInfo.mInfoId)){
				return LIST_ITEM_TYPE_ADD;
			}
			/*
			if(position == getCount() - 1){
				return LIST_ITEM_TYPE_ADD;
			}*/
			return LIST_ITEM_TYPE_NORMAL;
		};
		
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			int type = getItemViewType(position);
			boolean setaddDisable = mData.size() == LIST_ITEMS_MAX;
			if(convertView == null){
				switch(type){
					case LIST_ITEM_TYPE_NORMAL:{
						AddressInfo addressInfo = mData.get(position);
						ViewHolder holder = new ViewHolder();
						holder.index = position;
						convertView = getLayoutInflater().inflate(mMyResources.getLayout("lib_droi_account_layout_address_item"), null);
						holder.name = (TextView)convertView.findViewById(mMyResources.getId("lib_droi_account_name"));
						holder.address = (TextView)convertView.findViewById(mMyResources.getId("lib_droi_account_address"));
						holder.mChangeAdd = (TextView)convertView.findViewById(mMyResources.getId("lib_droi_account_changeAddress"));
						holder.mChangeAdd.setOnClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								clickListItem(v);
							}
						});
						convertView.setTag(holder);
						holder.mChangeAdd.setTag(holder);
						holder.name.setText(addressInfo.mName);
						String addr = EditAddressActivity.getStringFromAddresJson(addressInfo.mAddressDes);
						holder.address.setText(addr);
						setItemsLongClickListener(convertView);
					}
					break;
					case LIST_ITEM_TYPE_ADD:{
						AddAddressHolder holder = new AddAddressHolder();
						convertView = getLayoutInflater().inflate(mMyResources.getLayout("lib_droi_account_layout_address_add"), null);
						View addBtn = holder.addButton = convertView.findViewById(mMyResources.getId("lib_droi_account_add_address"));
						if(setaddDisable){
							addBtn.setEnabled(false);
						}else{
							addBtn.setEnabled(true);
						}
						convertView.findViewById(mMyResources.getId("lib_droi_account_add_address")).setOnClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								clickListItem(v);
							}
						});
						convertView.setTag(holder);
						holder.addButton.setTag(holder);
					}
					break;
				}

			}else{
				switch(type){
					case LIST_ITEM_TYPE_NORMAL:{
						ViewHolder holder = (ViewHolder)convertView.getTag();
						holder.index = position;
						holder.mChangeAdd.setTag(holder);
						
					}
					break;
					case LIST_ITEM_TYPE_ADD:{
						AddAddressHolder holder = (AddAddressHolder)convertView.getTag();
						holder.index = position;
						holder.addButton.setTag(holder);
						if(setaddDisable){
							holder.addButton.setEnabled(false);
						}else{
							holder.addButton.setEnabled(true);
						}
					}
					break;
				}
			}

			return convertView;
		}
		
	};

	private void clickListItem(View v){
		BaseHolder holder = (BaseHolder)v.getTag();
		int position = holder.index;
		if(position > LIST_ITEMS_MAX - 1){
			DebugUtils.i("item ", ""+position);
		}if(position == mData.size() - 1 && position <= LIST_ITEMS_MAX - 2){
			Intent intent = new Intent(this, EditAddressActivity.class);
			intent.putExtra("what", EditAddressActivity.ADD_ADDRESS);
			startActivityForResult(intent, EDIT_ITEM);
		}else{
			Intent intent = new Intent(this, EditAddressActivity.class);
			AddressInfo addressInfo = mData.get(position);
			intent.putExtra("openid", addressInfo.mOpenId);
			intent.putExtra("infoid", addressInfo.mInfoId);
			intent.putExtra("cmobile", addressInfo.mPhone);
			intent.putExtra("caddress", addressInfo.mAddressDes);
			intent.putExtra("cname", addressInfo.mName);
			intent.putExtra("what", EditAddressActivity.UPDATE_ADDRESS);
			startActivityForResult(intent, EDIT_ITEM);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == EDIT_ITEM){
			if(resultCode == RESULT_OK){
				String infoId = data.getStringExtra("infoid");
				if(DebugUtils.DEBUG){
					DebugUtils.i(TAG, "onActivityResult infoId = " + infoId);
				}
				if(TextUtils.isEmpty(infoId)){
					//add a new item
					String delivery_info = data.getStringExtra("delivery_info");
					if(DebugUtils.DEBUG){
						DebugUtils.i(TAG, "onActivityResult delivery_info = " + delivery_info);
					}
					rebuildData(delivery_info);
					mListView.setAdapter(mAdapter);
					mAdapter.notifyDataSetChanged();
				}else{
					//update a item
					String addr = data.getStringExtra("caddress");
					String phone = data.getStringExtra("cmobile");
					String name = data.getStringExtra("cname");
					AddressInfo item = getItemInfo(infoId);
					if(item != null){
					item.mAddressDes = addr;
						item.mPhone = phone;
						item.mName = name;
						mListView.setAdapter(mAdapter);
						mAdapter.notifyDataSetChanged();
					}
				}
				
			}
		}
	}
	
	private void setItemsLongClickListener(View view){
		view.setOnLongClickListener(new View.OnLongClickListener(){
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				handleLongClick(v);
				return true;
			}
		});
	}
	
	private AddressInfo getItemInfo(String infoId){
		if(!TextUtils.isEmpty(infoId)){
			for(int i = 0; i < mData.size(); i++){
				AddressInfo item = mData.get(i);
				if(item.mInfoId.equals(infoId)){
					return item;
				}
			}
		}
		return null;
	}
	
	private void rebuildData(String delivery_info){
		try{
			if(!TextUtils.isEmpty(delivery_info)){
				JSONArray jsonArray = new JSONArray(delivery_info);
				mData.clear();
				for(int i = 0; i < jsonArray.length(); i++){
					JSONObject addressObject = jsonArray.getJSONObject(i);
					String infoid = addressObject.has("infoid") ? addressObject.getString("infoid") : null;
					String openid = addressObject.has("openid") ? addressObject.getString("openid") : null;
					String phone =  addressObject.has("cmobile") ? addressObject.getString("cmobile") : null;
					String address =  addressObject.has("caddress") ? addressObject.getString("caddress") : null;
					String name = addressObject.has("cname") ? addressObject.getString("cname") : null;
					mData.add(new AddressInfo(infoid, openid, name, phone, address));
				}
				mData.add(new AddressInfo("-1", "","","",""));
			}
		}catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void addData(AddressInfo newItem){
		ArrayList<AddressInfo> datas = new ArrayList<AddressInfo>();
		datas.clear();
		for(AddressInfo item : mData){
			if("-1.".equals(item.mInfoId)){
				datas.add(newItem);
				datas.add(item);
			}else{
				datas.add(item);
			}
		}
		mData.clear();
		for(AddressInfo item : datas){
			mData.add(item);
		}
	}
	
	private void removeData(int position){
		ArrayList<AddressInfo> datas = new ArrayList<AddressInfo>();
		datas.clear();
		AddressInfo removeItem = mData.get(position);
		for(AddressInfo item : mData){
			if(removeItem.mInfoId.equals(item.mInfoId)){
			}else{
				datas.add(item);
			}
		}
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "removeData size = " + mData.size() );
			for(int i = 0; i < mData.size(); i++){
				AddressInfo item = mData.get(i);
				DebugUtils.i(TAG, "before remove "+", index = " + i + ", infoId = " + item.mInfoId);
			}
			
		}
		mData.clear();
		for(AddressInfo item : datas){
			mData.add(item);
		}
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "removeData size = " + mData.size() );
			for(int i = 0; i < mData.size(); i++){
				AddressInfo item = mData.get(i);
				DebugUtils.i(TAG, "after remove "+", index = " + i + ", infoId = " + item.mInfoId);
			}
			
		}
	}
	
	private ListPopupWindow createPopupMenu(Context context, final View anchorView){
		final ListPopupWindow listPopupWindow = new ListPopupWindow(context);
		listPopupWindow.setAnchorView(anchorView);
		listPopupWindow.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NOT_NEEDED);
		final ArrayList<String> choices = new ArrayList<String>(2);
		choices.add(getString(mMyResources.getString("lib_droi_account_popup_window_item_modify_address")));
		choices.add(getString(mMyResources.getString("lib_droi_account_popup_window_item_delete_address")));
		
		ListAdapter adapter = new ArrayAdapter<String>(this, mMyResources.getLayout("lib_droi_account_select_dialog_item"), choices);
		
		OnItemClickListener clickListener = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long id) {
				// TODO Auto-generated method stub
				switch(position){
					case POPUPMENU_ITEM_DELETE:
						BaseHolder holder = (BaseHolder)anchorView.getTag();
						int index = holder.index;
						if(index < mData.size() - 1){
							AddressInfo info = mData.get(index);
							if(DebugUtils.DEBUG){
								DebugUtils.i(TAG, "popwindow delete = " + info);
							}
							if(info != null){
								deleteAddress(info.mInfoId, index);
							}
						}
						break;
					case POPUPMENU_ITEM_MODIFY:
						clickListItem(anchorView);
						break;
				}
				
				listPopupWindow.dismiss();
			}
			
		};
		listPopupWindow.setAdapter(adapter);
		listPopupWindow.setOnItemClickListener(clickListener);
		return listPopupWindow;
	}
	
	private void handleLongClick(View v){
		ListPopupWindow popupWindow = createPopupMenu(this, v);
		popupWindow.show();
	}
	
	
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}
	
	private class ViewHolder extends BaseHolder{
		public TextView name;
		public TextView address;
		public TextView mChangeAdd;
	}


	private class AddAddressHolder extends BaseHolder{
		
		public View addButton;
	}
	
	private class BaseHolder {
		public int index;
	}
	
	private void getAddress(){

		SharedInfo sharedInfo = SharedInfo.getInstance();
		String token = sharedInfo.getAccessToken(getApplicationContext());
		String openId = sharedInfo.getOpenId(getApplicationContext());
		showProgressbar(getString(mMyResources.getString("lib_droi_account_msg_on_process")));
		new GetDeliveryInfoTask(openId, token, "getinfo").execute();
	}
	
	private void deleteAddress(String infoId, int position){
		if(DebugUtils.DEBUG){
			DebugUtils.i(TAG, "deleteAddress infoId = " + infoId + ", position = " + position);
		}
		SharedInfo sharedInfo = SharedInfo.getInstance();
		String token = sharedInfo.getAccessToken(getApplicationContext());
		String openId = sharedInfo.getOpenId(getApplicationContext());
		showProgressbar(getString(mMyResources.getString("lib_droi_account_msg_on_process")));
		new GetDeliveryInfoTask(openId, token, "delete", infoId, position).execute();
	}
	
	private class GetDeliveryInfoTask extends AsyncTask<Void, Void, String>{

		private final String mOpendId;
		private final String mToken;
		private final String mActionType;
		private final String mInfoId;
		private final int mPosition;
		
		public GetDeliveryInfoTask(String openId, String token, String actionType){
			this(openId, token, actionType, null, -1);
		}
		
		public GetDeliveryInfoTask(String openId, String token, String actionType, String infoId, int position){
			mOpendId = openId;
			mToken = token;
			mActionType = actionType;
			if(DebugUtils.DEBUG){
				DebugUtils.i(TAG, "openId : " + mOpendId + ", token : " + mToken);
			}
			mInfoId = infoId;
			mPosition = position;
		}
		
		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub
			final Map<String, String> userParams = new HashMap<String, String>();
			String signString = MD5Util.md5(mOpendId + mToken + mActionType + Constants.SIGNKEY);
			userParams.put(Constants.JSON_S_OPENID, mOpendId);
			userParams.put(Constants.JSON_S_TOKEN, mToken);
			userParams.put("actiontype", mActionType);
			userParams.put("sign", signString);
			if(!TextUtils.isEmpty(mInfoId)){
				userParams.put("infoid", mInfoId);
			}
			String result = null;
			try{
				result = HttpOperation.postRequest(Constants.DELIVERY_INFO, userParams);
			}catch (Exception e){
	            e.printStackTrace();
	        }
			return result;
		}
		
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			//cname infoid  cmobile caddress
			super.onPostExecute(result);
			dismissProgressbar();
			if(DebugUtils.DEBUG){
				DebugUtils.i(TAG, "result : " + result);
			}
			if (!TextUtils.isEmpty(result)){
        		try {
					JSONObject jsonObject = new JSONObject(result);
					int rs = jsonObject.getInt("result");
					if(rs == 0){
						if(mActionType.equals("getinfo")){
							String delivery_info = jsonObject.has("delivery_info") ? jsonObject.getString("delivery_info") : null;
							if(DebugUtils.DEBUG){
								DebugUtils.i(TAG, "delivery_info : " + delivery_info);
							}
							mData.clear();
							if(!TextUtils.isEmpty(delivery_info) && !"{}".equals(delivery_info)){
								JSONArray jsonArray = new JSONArray(delivery_info);
								for(int i = 0; i < jsonArray.length(); i++){
									JSONObject addressObject = jsonArray.getJSONObject(i);
									String infoid = addressObject.has("infoid") ? addressObject.getString("infoid") : null;
									String openid = addressObject.has("openid") ? addressObject.getString("openid") : null;
									String phone =  addressObject.has("cmobile") ? addressObject.getString("cmobile") : null;
									String address =  addressObject.has("caddress") ? addressObject.getString("caddress") : null;
									String name = addressObject.has("cname") ? addressObject.getString("cname") : null;
									if(infoid != null){
									    mData.add(new AddressInfo(infoid, openid, name, phone, address));
									}
								}
							}
							mData.add(new AddressInfo("-1", "","","",""));
							mAdapter.notifyDataSetChanged();
						}else if(mActionType.equals("delete")){
							removeData(mPosition);
							mListView.setAdapter(mAdapter);
							mAdapter.notifyDataSetChanged();
						}
					}else{
						String desc  = jsonObject.has("desc") ? jsonObject.getString("desc") : 
							getResources().getString(mMyResources.getString("lib_droi_account_get_address_failed"));
                    	Utils.showMessage(getApplicationContext(), desc);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				String desc  = getResources().getString(mMyResources.getString("lib_droi_account_get_address_failed"));
            	Utils.showMessage(getApplicationContext(), desc);
			}
			
		}
		
		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
			dismissProgressbar();
		}
		
	}

	public void onBack(View view){
		onBackPressed();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		intent.putExtra("address", (mData.size() > 1));
		setResult(RESULT_CANCELED, intent);
		super.onBackPressed();
	}
}
