package com.exfe.android.model;

import java.util.Observable;
import java.util.Random;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.exfe.android.Application;
import com.exfe.android.db.DatabaseHelper;
import com.exfe.android.net.ServerAPI2;
import com.j256.ormlite.android.apptools.OpenHelperManager;

public class Model extends Observable {

	private static final String TAG = Model.class.getSimpleName();

	public static final String OBSERVER_FIELD_TYPE = "ACTION_TYPE";

	public static final int ACTION_TYPE_NOTIFICATION_BASE = 100;
	public static final int ACTION_TYPE_SIGN_OUT = Model.ACTION_TYPE_NOTIFICATION_BASE + 1;
	public static final int ACTION_TYPE_START_NETWROK_QUERY = Model.ACTION_TYPE_NOTIFICATION_BASE + 2;
	public static final int ACTION_TYPE_STOP_NETWROK_QUERY = Model.ACTION_TYPE_NOTIFICATION_BASE + 3;

	public static final int ACTION_TYPE_CROSSES_BASE = 200;
	public static final int ACTION_TYPE_CONVERSATION_BASE = 300;
	public static final int ACTION_TYPE_ME_BASE = 400;
	public static final int ACTION_TYPE_DEVICE_BASE = 500;

	// public Looper mLooper = Looper.getMainLooper();
	public Handler mHandler = new Handler();

	private CrossesModel mCrosses = null;
	private MeModel mMe = null;
	private DeviceModel mDevice = null;
	private ConversationModel mConversation = null;
	private CacheModel mImageCache = null;
	private Application mAppContext = null;
	private DatabaseHelper mDBHelper = null;

	private boolean mAutoNotification = true;

	public Model(Application appContext) {
		super();
		if (appContext == null) {
			throw new IllegalArgumentException("appConext should not be null.");
		}
		mAppContext = appContext;
	}

	// Server API v1
	// @Deprecated
	// public ServerAPI1 getServerv1() {
	// return new ServerAPI1(this);
	// }

	// Server API v2
	public ServerAPI2 getServer() {
		return new ServerAPI2(this);
	}

	public SharedPreferences getDefaultSharedPreference() {
		return getAppContext().getSharedPreferences("model",
				Context.MODE_PRIVATE);
	}

	public NotificationManager getNotificationManager() {
		NotificationManager mNotificationManager = (NotificationManager) getAppContext()
				.getSystemService(Context.NOTIFICATION_SERVICE);
		return mNotificationManager;
	}

	public Application getAppContext() {
		return mAppContext;
	}

	public void setAppContext(Application appContext) {
		this.mAppContext = appContext;
	}

	public void setChanged() {
		if (this.mAutoNotification == true) {
			super.setChanged();
		}
	}

	/**
	 * @return the autoNotification
	 */
	public boolean isAutoNotification() {
		return mAutoNotification;
	}

	/**
	 * @param autoNotification
	 *            the autoNotification to set
	 */
	public void setAutoNotification(boolean autoNotification) {
		this.mAutoNotification = autoNotification;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observable#notifyObservers()
	 */
	@Override
	public void notifyObservers() {
		// TODO Auto-generated method stub
		super.notifyObservers();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observable#notifyObservers(java.lang.Object)
	 */
	@Override
	public void notifyObservers(Object data) {
		// TODO Auto-generated method stub
		super.notifyObservers(data);
	}

	// Extend Model
	public CrossesModel Crosses() {
		if (mCrosses == null) {
			synchronized (CrossesModel.class) {
				if (mCrosses == null) {
					mCrosses = new CrossesModel(this);
				}
			}
		}
		return mCrosses;
	}

	public ConversationModel Conversations() {
		if (mConversation == null) {
			synchronized (ConversationModel.class) {
				if (mConversation == null) {
					mConversation = new ConversationModel(this);
				}
			}
		}
		return mConversation;
	}

	public MeModel Me() {
		if (mMe == null) {
			synchronized (MeModel.class) {
				if (mMe == null) {
					mMe = new MeModel(this);
				}
			}
		}
		return mMe;
	}
	
	public DeviceModel Device() {
		if (mDevice == null) {
			synchronized (DeviceModel.class) {
				if (mDevice == null) {
					mDevice = new DeviceModel(this);
				}
			}
		}
		return mDevice;
	}

	public CacheModel ImageCache() {
		if (mImageCache == null) {
			synchronized (CacheModel.class) {
				if (mImageCache == null) {
					mImageCache = new CacheModel(this);
				}
			}
		}
		return mImageCache;
	}

	public DatabaseHelper getHelper() {
		if (mDBHelper == null) {
			mDBHelper = OpenHelperManager.getHelper(getAppContext(),
					DatabaseHelper.class);
		}
		return mDBHelper;
	}

	public void releaseHelper() {
		if (mDBHelper != null) {
			OpenHelperManager.releaseHelper();
			mDBHelper = null;
		}
	}

	public String generateUDID() {
		// try use ANDROID_ID
		String Android_Id = Settings.Secure.getString(mAppContext.getContentResolver(),
				Settings.Secure.ANDROID_ID);

		// For 2.2 (Froyo) bug, use other way:getDeviceId
		// http://blog.csdn.net/zhjp4295216/article/details/5769564
		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.FROYO
				&& "9774D56D682E549C".equalsIgnoreCase(Android_Id)) {
			final TelephonyManager tm = (TelephonyManager) mAppContext
					.getSystemService(Context.TELEPHONY_SERVICE);
			final String tmDevice = tm.getDeviceId(); // IMEI for GSM, MEID for
														// CDMA
			Android_Id = String.format("2.2_%s", tmDevice);
		}

		// For emulator (except 2.2/Froyo)
		if ("google_sdk".equals(Build.PRODUCT) || "sdk".equals(Build.PRODUCT)
				|| "generic".equals(Build.BRAND)) {
			StringBuilder sb = new StringBuilder("emu_");
			Random r = new Random();
			while (sb.length() < 16) {
				sb.append(r.nextInt(10000));
			}
			Android_Id = sb.toString();
		}

		return Android_Id;
	}

	public String getDeviceName() {
		return Build.MODEL;
	}

	public String getDeviceId() {
		return Settings.Secure.getString(mAppContext.getContentResolver(),
				Settings.Secure.ANDROID_ID);
	}
	
	public void signOut(){
		setChanged();
		Bundle data = new Bundle();
		data.putInt(Model.OBSERVER_FIELD_TYPE, ACTION_TYPE_SIGN_OUT);
		notifyObservers(data);
	}
	
	public void startNetworkQuery(){
		setChanged();
		Bundle data = new Bundle();
		data.putInt(Model.OBSERVER_FIELD_TYPE, ACTION_TYPE_START_NETWROK_QUERY);
		notifyObservers(data);
	}
	
	public void stopNetworkQuery(){
		setChanged();
		Bundle data = new Bundle();
		data.putInt(Model.OBSERVER_FIELD_TYPE, ACTION_TYPE_STOP_NETWROK_QUERY);
		notifyObservers(data);
	}
	
	public void startWaiting(){
		setChanged();
		Bundle data = new Bundle();
		data.putInt(Model.OBSERVER_FIELD_TYPE, ACTION_TYPE_START_NETWROK_QUERY);
		notifyObservers(data);
	}
	
	public void stopWaiting(){
		setChanged();
		Bundle data = new Bundle();
		data.putInt(Model.OBSERVER_FIELD_TYPE, ACTION_TYPE_STOP_NETWROK_QUERY);
		notifyObservers(data);
	}

}
