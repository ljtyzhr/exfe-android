package com.exfe.android;

import org.apache.http.HttpStatus;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.exfe.android.controller.LandingActivity;
import com.exfe.android.controller.SearchPlaceActivity;
import com.exfe.android.model.Model;
import com.exfe.android.model.entity.Response;
import com.flurry.android.FlurryAgent;

public class Activity extends FragmentActivity {

	protected final String TAG = getClass().getSimpleName();

	protected Model mModel = null;

	public Activity() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mModel = ((Application) getApplicationContext()).getModel();

		try {
			PackageInfo pkg = getPackageManager().getPackageInfo(
					getApplication().getPackageName(), 0);
			// String appName =
			// pkg.applicationInfo.loadLabel(getPackageManager())
			// .toString();
			FlurryAgent.setVersionName(pkg.versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, Const.FLURRY_APP_ID);
	}

	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

	/**
	 * @return the model
	 */
	public Model getModel() {
		return this.mModel;
	}

	protected void postInUI(Runnable run) {
		mModel.mHandler.post(run);
	}

	protected Runnable createToastMessage(final String msg) {
		return new Runnable() {

			@Override
			public void run() {
				Toast toast = Toast.makeText(getApplicationContext(), msg,
						Toast.LENGTH_SHORT);
				toast.show();
			}

		};
	}

	protected Runnable hideProgressBar = new Runnable() {

		@Override
		public void run() {
			hideProgressBar();
		}
	};

	protected ProgressDialog mProgressDialog = null;

	protected void showProgressBar(final String title, final String message) {
		postInUI(new Runnable() {

			@Override
			public void run() {
				if (mProgressDialog == null) {
					mProgressDialog = new ProgressDialog(Activity.this);
				} else {
					mProgressDialog.setTitle(title);
					mProgressDialog.setMessage(message);
				}
				if (!mProgressDialog.isShowing()) {
					mProgressDialog.show();
				}
			}
		});
	}

	protected void hideProgressBar() {
		postInUI(new Runnable() {

			@Override
			public void run() {
				if (mProgressDialog != null && mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
			}
		});
	}

	protected Toast mToast = null;

	protected void showToast(String msg) {
		showToast(msg, Toast.LENGTH_SHORT);
	}

	protected void showToast(String msg, int duration) {
		showToast(msg, duration, false);
	}

	protected void showToast(final String msg, final int duration, final boolean showNow) {
		postInUI(new Runnable(){

			@Override
			public void run() {
				if (showNow && mToast != null) {
					mToast.cancel();
				}
				if (mToast == null) {
					mToast = Toast.makeText(Activity.this, msg, duration);
				} else {
					mToast.setText(msg);
					mToast.setDuration(duration);
				}
				mToast.show();
			}});
	}

	public void registGCM() {
		Intent regIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
		regIntent.putExtra(Const.GCM_FIELD_APP,
				PendingIntent.getBroadcast(this, 0, new Intent(), 0));
		regIntent.putExtra(Const.GCM_FIELD_SENDER, Const.PUSH_PROJECT_ID);
		startService(regIntent);
	}

	public void unregistGCM() {
		Intent unregIntent = new Intent(
				"com.google.android.c2dm.intent.UNREGISTER");
		unregIntent.putExtra(Const.GCM_FIELD_APP,
				PendingIntent.getBroadcast(this, 0, new Intent(), 0));
		startService(unregIntent);
	}

	public void signOut() {
		final String deviceToken = mModel.Device().getPushToken();
		final String appKey = mModel.Me().getToken();
		mModel.Me().setToken("");
		mModel.Me().setUserId(0L);
		mModel.Me().setProvider("");
		mModel.Me().setExternalId("");
		mModel.Me().setUsername("");
		mModel.Device().setPushToken("");
		mModel.Crosses().clearCrosses();
		mModel.Crosses().setLastUpdateQuery(null);
		mModel.Me().setProfile(null);

		Runnable run = new Runnable() {

			public void run() {
				FlurryAgent.logEvent("sign_out");
				Response resp = mModel.getServer().signOut(appKey, deviceToken);
				if (resp.getCode() == HttpStatus.SC_OK) {
					mModel.mHandler.post(new Runnable() {

						@Override
						public void run() {
							unregistGCM();
						}
					});
				}
				mModel.mHandler.post(new Runnable() {

					@Override
					public void run() {
						Intent it = new Intent();
						it.setClass(Activity.this, LandingActivity.class);
						it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(it);
						finish();
					}
				});

			}
		};

		new Thread(run).start();
	}
}
