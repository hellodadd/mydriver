package com.droi.account;

import java.lang.reflect.Field;

import android.content.Context;
import android.content.res.Resources;

public final class MyResource {
	
	
	private Resources mResource;
	private String mPackageName;
	private Context mContext;
	
	public MyResource(Context context){
		 mContext = context;
	}
	
	private int baseInfo(String name, String type){
		if(mResource != null){
			return mResource.getIdentifier(name, type, getPackageName());
		}else{
			mResource = mContext.getApplicationContext().getResources();
		}
		
		return mResource.getIdentifier(name, type, getPackageName()); 
	}
	
	private final String getPackageName() {
	    if (mPackageName == null){
	    	mPackageName = mContext.getApplicationContext().getPackageName();
	    }
	    return mPackageName;
	}
	
	private static Object getResourceId(Context context,String name, String type) {
		String className = context.getApplicationContext().getPackageName() +".R";

		try {

			Class<?> cls = Class.forName(className);
	
			for (Class<?> childClass : cls.getClasses()) {
	
				String simple = childClass.getSimpleName();
		
				if (simple.equals(type)) {
		
					for (Field field : childClass.getFields()) {
			
						String fieldName = field.getName();
				
						if (fieldName.equals(name)) {
				
						System.out.println(fieldName);
				
						return field.get(null);
				
						}
			
					}
			
				}
			}

		} catch (Exception e) {

			e.printStackTrace();

		}

		return null;

	}
	
	
	public static int getStyleableId(Context context,String name){
		return (Integer)getResourceId(context, name, "styleable");
	}
	
	public static int[] getStyleableArray(Context context,String name) {
		return (int[])getResourceId(context, name, "styleable");
	}
	
	public final int getDrawable(String name){
		return baseInfo(name, "drawable");
	}
	
	public final int getId(String name){
		return baseInfo(name, "id");
	}
	
	public final int getLayout(String name){
		return baseInfo(name, "layout");
	}
	
	public static final int getString(Context applicationContext, String name){
		Resources resource = applicationContext.getApplicationContext().getResources();
		return resource.getIdentifier(name, "string", applicationContext.getPackageName()); 
	}
	
	public final int getString(String name){
		return baseInfo(name, "string");
	}
	
	public final int getRaw(String name){
		return baseInfo(name, "raw");
	}
	
	public final int getInteger(String name){
		return baseInfo(name, "integer");
	}
	
	public final int getMenu(String name){
		return baseInfo(name, "menu");
	}
	
	public final int getColor(String name){
		return baseInfo(name, "color");
	}
	
	public final int getXml(String name){
		return baseInfo(name, "xml");
	}
	
	public final int getArrray(String name){
		return baseInfo(name, "array");
	}
	
	public final int getDimen(String name){
		return baseInfo(name, "dimen");
	}
	
}
