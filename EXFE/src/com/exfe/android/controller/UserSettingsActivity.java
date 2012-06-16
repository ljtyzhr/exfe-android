package com.exfe.android.controller;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.exfe.android.Activity;
import com.exfe.android.R;
import com.exfe.android.debug.Log;
import com.exfe.android.model.MeModel;
import com.exfe.android.model.Model;
import com.exfe.android.model.entity.EntityFactory;
import com.exfe.android.model.entity.Identity;
import com.exfe.android.model.entity.Response;
import com.exfe.android.model.entity.User;
import com.exfe.android.task.GetProfileTask;
import com.exfe.android.util.ImageCache;
import com.exfe.android.view.SeperatedBaseAdapter;

public class UserSettingsActivity extends Activity implements Observer {

	private User mMe = null;
	private ImageView mAvatar = null;
	private TextView mName = null;
	private ListView mList = null;
	private TextView mAttend = null;
	private IdentityAdpater mIdentityAdapter = null;
	private IdentityAdpater mDeviceAdapter = null;
	private SeperatedBaseAdapter mAdapter = null;
	private SeperatedBaseAdapter.SeperatedSectionFactory mFactory = new SeperatedBaseAdapter.SeperatedSectionFactory() {

		@Override
		public boolean hasHeader(SeperatedBaseAdapter container,
				BaseAdapter adapter, int index) {
			return false;
		}

		@Override
		public boolean hasFooter(SeperatedBaseAdapter container,
				BaseAdapter adapter, int index) {
			if (index == 0) {
				return true;
			} else if (index == 1) {
				return true;
			}
			return false;
		}

		@Override
		public View getHeader(SeperatedBaseAdapter container,
				BaseAdapter adapter, int index) {
			View v = null;
			LayoutInflater mInflater = (LayoutInflater) container.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = mInflater.inflate(R.layout.listitem_add_identity, null);

			return v;
		}

		@Override
		public View getFoot(SeperatedBaseAdapter container,
				BaseAdapter adapter, int index) {
			View v = null;
			LayoutInflater mInflater = (LayoutInflater) container.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			if (index == 0) {
				v = mInflater.inflate(R.layout.listitem_add_identity, null);
			} else {
				v = mInflater.inflate(R.layout.profile_foot, null);

				View btnSignOut = v.findViewById(R.id.btn_sign_out);
				btnSignOut.setOnClickListener(mLogout);
			}

			return v;
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mModel.addObserver(this);
		ImageCache.getInst().addObserver(this);
		setContentView(R.layout.scr_user_settings);

		View btnBack = findViewById(R.id.btn_back);
		btnBack.setOnClickListener(mBackClick);

		mAvatar = (ImageView) findViewById(R.id.user_avatar);
		if (mAvatar != null) {
			mAvatar.setOnClickListener(mShowDrawable);
		}
		mName = (TextView) findViewById(R.id.user_name);
		mList = (ListView) findViewById(R.id.user_list);
		mAttend = (TextView) findViewById(R.id.user_cross_count);
		int[] reses = { R.layout.listitem_identity,
				R.layout.listitem_identity_device };
		mIdentityAdapter = new IdentityAdpater(this, reses,
				new ArrayList<Identity>());
		mDeviceAdapter = new IdentityAdpater(this, reses,
				new ArrayList<Identity>());
		mAdapter = new SeperatedBaseAdapter(this, mFactory);
		mAdapter.addAdapter(mIdentityAdapter);
		mAdapter.addAdapter(mDeviceAdapter);
		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(mItemClick);
		mMe = mModel.Me().getProfile();
		loadUser(mMe);
		loadIdentities(mMe);
		mModel.Me().fetchProfile();
	}

	View.OnClickListener mLogout = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			final String deviceToken = mModel.Me().getDeviceToken();
			mModel.Me().setToken("");
			mModel.Me().setUserId(0);
			mModel.Me().setProvider("");
			mModel.Me().setExternalId("");
			mModel.Me().setUsername("");
			mModel.Me().setDeviceToken("");
			mModel.Crosses().clearCrosses();
			mModel.Crosses().setLastUpdateQuery(null);
			// mModel.Me().setProfile(null);

			Runnable run = new Runnable() {
				public void run() {
					mModel.getServer().signOut(deviceToken);
				}
			};
			new Thread(run).start();

			Intent it = new Intent();
			it.setClass(v.getContext(), PortalActivity.class);
			startActivity(it);
		}
	};

	View.OnClickListener mBackClick = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			finish();
		}
	};

	View.OnClickListener mShowDrawable = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v instanceof ImageView) {
				Drawable d = ((ImageView) v).getDrawable();
				ImageView iv = new ImageView(v.getContext());
				iv.setImageDrawable(d);
				final PopupWindow popup = new PopupWindow(iv,
						WindowManager.LayoutParams.MATCH_PARENT,
						WindowManager.LayoutParams.MATCH_PARENT, true);
				popup.setContentView(iv);
				popup.setOutsideTouchable(false);
				popup.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
				popup.showAtLocation(v, Gravity.CENTER, 0, 0);
				iv.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						popup.dismiss();
					}
				});
			}
		}
	};

	AdapterView.OnItemClickListener mItemClick = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> listView, View item,
				int position, long id) {
			// TODO Auto-generated method stub
			if (id == Identity.NO_ID) {
				Log.d(TAG, "add Identity");
			} else {
				Identity identity = (Identity) listView.getAdapter().getItem(
						position);
			}
		}
	};

	protected void loadUser(User user) {
		if (user != null) {
			// set avatar
			String avatar_file_name = user.getAvatarFilename();
			boolean flag = false;

			if (!TextUtils.isEmpty(avatar_file_name)) {
				Bitmap bm = ImageCache.getInst().getImageFrom(avatar_file_name);
				if (bm != null) {
					mAvatar.setImageBitmap(bm);
					flag = true;
				}
			}
			if (flag == false) {
				mAvatar.setImageResource(R.drawable.default_avatar);
			}

			// set name
			String name = user.getName();
			mName.setText(name);
			// mLongName.setText();
			CharSequence styledText = Html.fromHtml(String.format(
					getResources().getString(R.string.x_attended),
					user.getCrossQuantity()));
			mAttend.setText(styledText);
		}
	}

	protected void loadIdentities(User user) {
		if (user != null) {
			mIdentityAdapter.setNotifyOnChange(false);
			mDeviceAdapter.setNotifyOnChange(false);
			mIdentityAdapter.clear();
			mDeviceAdapter.clear();
			for (Identity id : user.getIdentities()) {
				if (id.isDeviceToken()) {
					mDeviceAdapter.add(id);
				} else {
					mIdentityAdapter.add(id);
				}
			}
			mIdentityAdapter.setNotifyOnChange(true);
			mDeviceAdapter.setNotifyOnChange(true);
			mIdentityAdapter.notifyDataSetChanged();
			// mDeviceAdapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		mModel.deleteObserver(this);
		ImageCache.getInst().deleteObserver(this);
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub
		if (observable instanceof ImageCache) {
			if (mMe.getAvatarFilename().equals(data)) {
				loadUser(mMe);
			}
		} else if (observable instanceof Model) {
			Bundle bundle = (Bundle) data;
			int type = bundle.getInt(Model.OBSERVER_FIELD_TYPE);
			switch (type) {
			case MeModel.ACTION_TYPE_UPDATE_MY_PROFILE:
				mMe = mModel.Me().getProfile();
				loadUser(mMe);
				loadIdentities(mMe);
				break;
			}
		}
	}

	public static class IdentityAdpater extends ArrayAdapter<Identity> {
		public static class ViewHolder {
			ImageView icon;
			TextView main;
			TextView alt;
			WeakReference<View> root;
		}

		private int[] mResource;
		private int[] mDropDownResource;
		private LayoutInflater mInflater;

		public IdentityAdpater(Context context, int[] resource,
				List<Identity> objects) {
			super(context, resource[0], objects);
			// TODO Auto-generated constructor stub
			init(context, resource);
		}

		private void init(Context context, int[] resource) {
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mResource = resource;
			mDropDownResource = resource;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
		 * android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return createViewFromResource(position, convertView, parent,
					mResource);
		}

		private View createViewFromResource(int position, View convertView,
				ViewGroup parent, int[] resource) {
			View view;
			ViewHolder holder;
			int type = getItemViewType(position);
			if (convertView == null) {
				view = mInflater.inflate(resource[type], parent, false);
				holder = creatViewHolder(view);
				view.setTag(holder);
			} else {
				view = convertView;
				Object obj = view.getTag();
				if (obj == null) {
					holder = creatViewHolder(view);
					view.setTag(holder);
				} else {
					holder = (ViewHolder) obj;
				}
			}

			ImageView icon = holder.icon;
			TextView main = holder.main;
			TextView alt = holder.alt;
			View root = holder.root.get();

			Identity id = getItem(position);

			if (icon != null) {
				boolean flag = false;
				if (type == 1) {
					icon.setImageResource(R.drawable.device_phone);
					flag = true;
				} else {
					if (!TextUtils.isEmpty(id.getAvatarFilename())) {
						Bitmap bm = ImageCache.getInst().getImageFrom(
								id.getAvatarFilename());
						if (bm != null) {
							icon.setImageBitmap(bm);
							flag = true;
						}
					}
				}
				if (flag == false) {
					icon.setImageResource(R.drawable.default_avatar);
				}
			}

			if (main != null) {
				main.setText(id.getExternalUsername());
			}

			if (alt != null) {
				alt.setText(id.getExternalId());
			}
			return view;
		}

		private ViewHolder creatViewHolder(View view) {
			ViewHolder holder = new ViewHolder();
			holder.icon = (ImageView) view.findViewById(R.id.identity_icon);
			holder.main = (TextView) view.findViewById(R.id.identity_main);
			holder.alt = (TextView) view.findViewById(R.id.identity_alt);
			holder.root = new WeakReference<View>(
					view.findViewById(R.id.list_identity_root));
			return holder;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ArrayAdapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position) {
			Identity id = getItem(position);
			if (id != null) {
				return id.getId();
			} else {
				return Identity.NO_ID;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.BaseAdapter#hasStableIds()
		 */
		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.BaseAdapter#getItemViewType(int)
		 */
		@Override
		public int getItemViewType(int position) {
			Identity id = null;
			if (position < super.getCount()) {
				id = getItem(position);

			}

			if (id != null && id.isDeviceToken()) {
				return 1;
			}

			return 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.BaseAdapter#getViewTypeCount()
		 */
		@Override
		public int getViewTypeCount() {
			return 2;
		}

	}
}