package com.droi.account;

import java.io.File;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class DroiAccountProvider extends ContentProvider {

	private static String CONTENT_PACKAGE;
	private static final String DATABASE_NAME = "droi_account.db";
	private static String DATABASE_PATH;
	private static final int DATABASE_VERSION = 2;
	private static final String TABLE_NAME = "droidata";
	private static final String TABLE_STATISTICS = "statistics";
	private static UriMatcher sMatcher;
	private static final int ITEM = 1;
	private static final int ITEM_ID = 2;
	private static final int ITEM_STATISTICS = 3;
	private static final int ITEM_STATISTICS_ID = 4;
	
	
	private static final String AUTHORITY_SUFFIX = ".droidatabase";
	
	private static String AUTHORITY ;
	
	public static Uri CONTENT_URI;
	
	public static Uri CONTENT_STATISTICS_URI ;
	
	public final static String _ID = "_id";
	public final static String TB_ITEM_SAHRED_DATA = "shared_data";
	
	public static final String TB_STATISTICS_ITEM_PACKAGENAME = "packagename";
	
	public static final String TB_STATISTICS_ITEM_INTERFACE = "interface";
	
	public static final String TB_STATISTICS_ITEM_COUNTS = "count";
	
	private static final String STR_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS  " + TABLE_NAME
			+ "( " + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + ", "
			+ TB_ITEM_SAHRED_DATA + " text" 
			+ ");";
	private static final String STR_CREATE_TABLE_STATISTICS = "CREATE TABLE IF NOT EXISTS " + TABLE_STATISTICS
			+"( " + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + ", "
			+ TB_STATISTICS_ITEM_PACKAGENAME + " text,"
			+ TB_STATISTICS_ITEM_INTERFACE + " text,"
			+ TB_STATISTICS_ITEM_COUNTS + " INTEGER"
			+");";
	
	
	
	private SQLiteDatabase mSqldb;
	private static DatabaseHelper mDbHelper;
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
	    private static DatabaseHelper mInstance = null; 
	    public static synchronized DatabaseHelper getInstance(Context context) { 
	        if(mInstance == null){
	            mInstance = new DatabaseHelper(context);
	        }
	        return mInstance;
	    }
	    
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			try{
				db.execSQL(STR_CREATE_TABLE);
				db.execSQL(STR_CREATE_TABLE_STATISTICS);
			}catch(Exception e){
			    e.printStackTrace();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATISTICS);
			onCreate(db);
		}
		
		public void deleteDatabase(Context context){
		    context.deleteDatabase(DATABASE_NAME);
		}
		
		public void createDatabase(Context context){
		    SQLiteDatabase db = context.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
            try{
                db.execSQL(STR_CREATE_TABLE);
                db.execSQL(STR_CREATE_TABLE_STATISTICS);
            }catch(Exception e){
                e.printStackTrace();
            }
            db.close();
		}
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int count;
		switch (sMatcher.match(uri)) {
		case ITEM:{
			count = db.delete(TABLE_NAME, selection, selectionArgs);
		}
			break;
		case ITEM_ID:{
			String noteId = uri.getPathSegments().get(1);
			count = db.delete(TABLE_NAME, _ID + "=" + noteId + (!TextUtils.isEmpty(selection) ? " AND (" 
			        + selection + ')' : ""), selectionArgs);
		}
			break;
		case ITEM_STATISTICS:{
			count = db.delete(TABLE_STATISTICS, selection, selectionArgs);
		}
			break;
		case ITEM_STATISTICS_ID:{
			String noteId = uri.getPathSegments().get(1);
			count = db.delete(TABLE_STATISTICS, _ID + "=" + noteId + (!TextUtils.isEmpty(selection) ? " AND (" 
			        + selection + ')' : ""), selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unnown URI:" + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
	
		return count;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		mSqldb = mDbHelper.getWritableDatabase();
		long rowId = -1;
		Uri contentUri = null;
		switch (sMatcher.match(uri)) {
			case ITEM:{
				rowId = mSqldb.insert(TABLE_NAME, null, values);
				contentUri = CONTENT_URI;
			}
				break;
			case ITEM_STATISTICS:{
				rowId = mSqldb.insert(TABLE_STATISTICS, null, values);
				contentUri = CONTENT_STATISTICS_URI;
			}
				break;
			default:
				throw new IllegalArgumentException("Unnown URI:" + uri);
		}
		
		if (rowId > 0) {
			Uri rowUri = ContentUris.withAppendedId(contentUri, rowId);
			getContext().getContentResolver().notifyChange(rowUri, null);
			return rowUri;
		}
		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		initData();
        mDbHelper = DatabaseHelper.getInstance(getContext());
        return (mDbHelper == null) ? false : true;
	}

	private void initData(){
		if(CONTENT_PACKAGE == null){
			CONTENT_PACKAGE = getContext().getApplicationContext().getPackageName();
			AUTHORITY = CONTENT_PACKAGE + AUTHORITY_SUFFIX;
			DATABASE_PATH = "/data/data/" + CONTENT_PACKAGE + "/databases";
			sMatcher = new UriMatcher(UriMatcher.NO_MATCH);
			sMatcher.addURI(AUTHORITY, TABLE_NAME, ITEM);
			sMatcher.addURI(AUTHORITY, TABLE_NAME + "/#", ITEM_ID);
			sMatcher.addURI(AUTHORITY, TABLE_STATISTICS, ITEM_STATISTICS);
			sMatcher.addURI(AUTHORITY, TABLE_STATISTICS + "/#", ITEM_STATISTICS_ID);
			CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/"+TABLE_NAME);
			CONTENT_STATISTICS_URI = Uri.parse("content://" + AUTHORITY + "/"+TABLE_STATISTICS);
		}
	}
	
	public static void initData(Context context){
		if(CONTENT_PACKAGE == null){
			CONTENT_PACKAGE = context.getApplicationContext().getPackageName();
			AUTHORITY = CONTENT_PACKAGE + AUTHORITY_SUFFIX;
			DATABASE_PATH = "/data/data/" + CONTENT_PACKAGE + "/databases";
			sMatcher = new UriMatcher(UriMatcher.NO_MATCH);
			sMatcher.addURI(AUTHORITY, TABLE_NAME, ITEM);
			sMatcher.addURI(AUTHORITY, TABLE_NAME + "/#", ITEM_ID);
			sMatcher.addURI(AUTHORITY, TABLE_STATISTICS, ITEM_STATISTICS);
			sMatcher.addURI(AUTHORITY, TABLE_STATISTICS + "/#", ITEM_STATISTICS_ID);
			CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/"+TABLE_NAME);
			CONTENT_STATISTICS_URI = Uri.parse("content://" + AUTHORITY + "/"+TABLE_STATISTICS);
		}
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		switch (sMatcher.match(uri)) {
			case ITEM:{
				qb.setTables(TABLE_NAME);
			}
				break;
			case ITEM_STATISTICS:{
				qb.setTables(TABLE_STATISTICS);
			}
				break;
			default:
				throw new IllegalArgumentException("Unnown URI:" + uri);
		}
	
		
		Cursor c = qb.query(db, projection, selection, null, null, null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		SQLiteDatabase db = mDbHelper.getWritableDatabase();	
		int count = 0;
		switch (sMatcher.match(uri)) {
		case ITEM:{
			count = db.update(TABLE_NAME, values, selection, selectionArgs);
		}
			break;
		case ITEM_ID:{
			long switch_id = ContentUris.parseId(uri);
			String where = "_ID=" + switch_id;
			where += !TextUtils.isEmpty(selection) ? " and (" + selection + ")" : "";
			count = db.update(TABLE_NAME, values, where, selectionArgs);
		}
			break;
		case ITEM_STATISTICS:{
			count = db.update(TABLE_STATISTICS, values, selection, selectionArgs);
		}
		break;
		case ITEM_STATISTICS_ID:{
			long switch_id = ContentUris.parseId(uri);
			String where = "_ID=" + switch_id;
			where += !TextUtils.isEmpty(selection) ? " and (" + selection + ")" : "";
			count = db.update(TABLE_STATISTICS, values, where, selectionArgs);
		}
		break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	public static boolean isDBExit(){
	    SQLiteDatabase checkDB = null;
	    String myPath = DATABASE_PATH + "/" +  DATABASE_NAME;
	    File dbfile = new File(myPath);
	    if(!dbfile.exists()){
	        
	        return false;
	    }
	    
	    try{
	        checkDB = SQLiteDatabase.openDatabase(myPath, 
	                null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
	    }catch(SQLiteException e){
	        e.printStackTrace();
	    }

	    if(checkDB != null){
	        checkDB.close();
	    }
	    return checkDB != null ? true : false;
	}
	
    public static boolean createDatabase(Context context){
        if(mDbHelper != null){
            mDbHelper.createDatabase(context);
            return true;
        }
        return false;
	}
	
}
