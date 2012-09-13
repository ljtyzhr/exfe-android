package com.exfe.android.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.http.HttpStatus;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.bitmapfun.util.ImageFetcher;
import com.example.android.bitmapfun.util.ImageWorker;
import com.exfe.android.Activity;
import com.exfe.android.Const;
import com.exfe.android.Fragment;
import com.exfe.android.R;
import com.exfe.android.model.entity.EntityFactory;
import com.exfe.android.model.entity.Identity;
import com.exfe.android.model.entity.Provider;
import com.exfe.android.model.entity.Response;
import com.exfe.android.util.Tool;

public class LoginFragment extends Fragment implements Observer {

	private static final int QUERY_ID_PROFILE = 1;

	public static final int UI_MODE_SIGN_IN = 0;
	public static final int UI_MODE_SIGN_UP = 1;

	private Fragment.ActivityCallBack mCallBack;
	private ImageWorker mImageWorker = null;

	private TextView tvWelcome = null;
	private EditText etIdentity = null;
	private EditText etPassword = null;
	private EditText etUsername = null;
	private View btnSignIn = null;
	private View btnSetupNew = null;
	private View btnForgetPwd = null;
	private View btnShowPwd = null;
	private View groupUserName = null;
	private ProgressBar pbIndicator = null;
	private TextView tvHint = null;
	private ImageView ivAvatar = null;

	private String mKeyword = null;
	private String mRegFlag = null;
	private int mUiMode = UI_MODE_SIGN_IN;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mModel.addObserver(this);

		mImageWorker = new ImageFetcher(mModel.getAppContext(), getResources()
				.getDimensionPixelSize(R.dimen.small_avatar_width),
				getResources().getDimensionPixelSize(
						R.dimen.small_avatar_height));
		mImageWorker.setImageCache(mModel.ImageCache().ImageCache());
		mImageWorker.setImageFadeIn(false);
		// mImageWorker.setLoadingImage(resId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_login, container, false);
		return v;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.ListFragment#onViewCreated(android.view.View,
	 * android.os.Bundle)
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {

		View v = null;

		if (TextUtils.isEmpty(mKeyword)) {
			mKeyword = getResources().getString(R.string.key_word_exfee);
		}

		v = view.findViewById(R.id.welcome);
		if (v != null) {
			tvWelcome = (TextView) v;
			CharSequence text = Tool.highlightKeyword(tvWelcome.getText(),
					mKeyword,
					new ForegroundColorSpan(Color.rgb(0xDB, 0xEA, 0xF9)));
			tvWelcome.setText(text);
		}

		v = view.findViewById(R.id.btn_login_way_2);
		if (v != null) {
			v.setOnClickListener(clickListener);
			v.setTag(Provider.STR_TWITTER);
		}

		v = view.findViewById(R.id.input_indentity);
		if (v != null) {
			etIdentity = (EditText) v;
			InputMethodManager imm = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(etIdentity, 0);
		}
		v = view.findViewById(R.id.input_password);
		if (v != null) {
			etPassword = (EditText) v;
			etPassword.setOnEditorActionListener(editorAction);
		}

		v = view.findViewById(R.id.input_username_group);
		if (v != null) {
			groupUserName = (ViewGroup) v;
		}

		v = view.findViewById(R.id.input_username);
		if (v != null) {
			etUsername = (EditText) v;
		}

		v = view.findViewById(R.id.btn_sign_in);
		if (v != null) {
			btnSignIn = v;
			btnSignIn.setOnClickListener(clickListener);
		}

		v = view.findViewById(R.id.btn_setup_new);
		if (v != null) {
			btnSetupNew = v;
			btnSetupNew.setOnClickListener(clickListener);
		}

		v = view.findViewById(R.id.forget_password);
		if (v != null) {
			btnForgetPwd = v;
			btnForgetPwd.setOnClickListener(clickListener);
		}

		v = view.findViewById(R.id.show_password);
		if (v != null) {
			btnShowPwd = v;
			btnShowPwd.setOnClickListener(clickListener);
		}

		v = view.findViewById(R.id.indicator);
		if (v != null) {
			pbIndicator = (ProgressBar) v;
		}

		v = view.findViewById(R.id.label_hint);
		if (v != null) {
			tvHint = (TextView) v;
		}

		v = view.findViewById(R.id.input_indentity_avatar);
		if (v != null) {
			ivAvatar = (ImageView) v;
		}

		setUiMode(UI_MODE_SIGN_IN);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			getLoaderManager().initLoader(QUERY_ID_PROFILE, null,
					queryLoaderHandler);
		} else {
			// TODO: should handle version related code in a nice way.
			String defAccount = null;
			AccountManager accManager = AccountManager.get(getActivity());
			Account[] accounts = accManager
					.getAccountsByType(Const.ACCOUNT_TYPE_GOOGLE);
			if (accounts != null && accounts.length > 0) {
				defAccount = accounts[0].name;
			}
			if (!TextUtils.isEmpty(defAccount)) {
				if (etIdentity != null) {
					etIdentity.setText(defAccount);
					etIdentity.setSelection(defAccount.length());
					etIdentity.setOnFocusChangeListener(focusChangeListener);
					etIdentity.requestFocus();
				}
			}
		}

	}

	View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {

			if (!hasFocus) {
				final TextView tv = (TextView) v;
				final String external_name = tv.getText().toString();

				Runnable run = new Runnable() {

					@Override
					public void run() {
						mRegFlag = "";
						Response resp = getModel().getServer()
								.getRegistrationFlag(external_name,
										Provider.STR_EMAIL);
						if (resp.getCode() == HttpStatus.SC_OK) {
							mRegFlag = resp.getResponse().optString(
									"registration_flag");

							if ("SIGN_IN".equalsIgnoreCase(mRegFlag)) {
								final Identity ident = (Identity) EntityFactory
										.create(resp.getResponse()
												.optJSONObject("identity"));
								if (ident != null) {
									try {
										getModel().getHelper().getIdentityDao()
												.update(ident);
									} catch (SQLException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									tv.post(new Runnable() {

										@Override
										public void run() {
											setUiMode(UI_MODE_SIGN_IN);

											mImageWorker.loadImage(
													ident.getAvatarFilename(),
													ivAvatar);
										}
									});
								}
							} else if ("SIGN_UP".equalsIgnoreCase(mRegFlag)) {
								tv.post(new Runnable() {

									@Override
									public void run() {
										setUiMode(UI_MODE_SIGN_UP);
										// mImageWorker.loadImage(num,
										// ivAvatar);
									}
								});

							}
						}
					}
				};
				new Thread(run).start();
			} else {

				getActivity()
						.getWindow()
						.setSoftInputMode(
								WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

			}
		}
	};

	LoaderManager.LoaderCallbacks<Cursor> queryLoaderHandler = new LoaderManager.LoaderCallbacks<Cursor>() {
		final String[] PROJECTION = {
				ContactsContract.CommonDataKinds.Email.ADDRESS,
				ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
				ContactsContract.CommonDataKinds.Photo.PHOTO };
		static final int ADDRESS = 0;
		static final int IS_PRIMARY = 1;

		@SuppressLint("NewApi")
		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			if (id == QUERY_ID_PROFILE) {

				return new CursorLoader(
						getActivity(),
						Uri.withAppendedPath(
								ContactsContract.Profile.CONTENT_URI,
								ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
						PROJECTION,
						ContactsContract.Contacts.Data.MIMETYPE + " = ?",
						new String[] { ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE },
						ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
			}

			return null;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
			String defAccount = "";

			List<String> emails = new ArrayList<String>();
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				if (cursor.getInt(IS_PRIMARY) > 0) {
					emails.add(cursor.getString(ADDRESS));
				}
				cursor.moveToNext();
			}
			cursor.close();
			if (!emails.isEmpty()) {
				defAccount = emails.get(0);
			}

			AccountManager accManager = AccountManager.get(getActivity());
			Account[] accounts = accManager
					.getAccountsByType(Const.ACCOUNT_TYPE_GOOGLE);
			if (accounts != null && accounts.length > 0) {
				defAccount = accounts[0].name;
			}
			if (!TextUtils.isEmpty(defAccount)) {
				if (etIdentity != null) {
					etIdentity.setText(defAccount);
					etIdentity.setSelection(defAccount.length());
					etIdentity.setOnFocusChangeListener(focusChangeListener);
					etIdentity.requestFocus();
					// InputMethodManager imm = (InputMethodManager) etIdentity
					// .getContext().getSystemService(
					// Context.INPUT_METHOD_SERVICE);
					// imm.showSoftInput(etIdentity,
					// InputMethodManager.SHOW_IMPLICIT);
				}
			}

		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			// TODO Auto-generated method stub

		}

	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(android.app.Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		if (activity instanceof Fragment.ActivityCallBack) {
			mCallBack = (Fragment.ActivityCallBack) activity;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onDetach()
	 */
	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		mCallBack = null;
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	public void onDestroy() {
		mModel.deleteObserver(this);
		super.onDestroy();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub

	}

	protected boolean isShowPwd() {
		if (etPassword != null) {
			return etPassword.getTransformationMethod() != null;
		}
		return false;
	}

	protected void setShowPwd(boolean show) {
		if (etPassword != null) {
			if (show) {
				etPassword.setTransformationMethod(null);
			} else {
				etPassword
						.setTransformationMethod(new PasswordTransformationMethod());
			}
			if (etPassword.hasFocus()) {
				etPassword.setSelection(etPassword.getText().length());
			}
		}
	}

	protected int getUiMode() {
		return mUiMode;
	}

	private CharSequence highligntKeyword(int textid) {
		CharSequence text = Tool.highlightKeyword(
				getResources().getString(textid), mKeyword,
				new ForegroundColorSpan(Color.rgb(0x72, 0x97, 0xBF)));
		return text;
	}

	protected void setUiMode(int mode) {
		mUiMode = mode;
		if (mode == UI_MODE_SIGN_UP) {
			if (groupUserName != null) {
				groupUserName.setVisibility(View.VISIBLE);
			}
			if (btnSetupNew != null) {
				btnSetupNew.setVisibility(View.VISIBLE);
			}
			if (btnSignIn != null) {
				btnSignIn.setVisibility(View.GONE);
			}
			if (btnShowPwd != null) {
				btnShowPwd.setVisibility(View.VISIBLE);
			}
			if (btnForgetPwd != null) {
				btnForgetPwd.setVisibility(View.GONE);
			}
			if (etPassword != null) {
				etPassword
						.setHint(highligntKeyword(R.string.set_exfe_password));
			}
		} else {
			if (groupUserName != null) {
				if (groupUserName.hasFocus() && etPassword != null) {
					etPassword.requestFocus();
					etPassword.setSelection(etPassword.getText().length());
				}
				groupUserName.setVisibility(View.GONE);
			}
			if (isShowPwd()) {
				setShowPwd(false);
			}

			if (etPassword != null) {
				etPassword
						.setHint(highligntKeyword(R.string.enter_exfe_password));
			}
			if (btnSetupNew != null) {
				btnSetupNew.setVisibility(View.GONE);
			}
			if (btnSignIn != null) {
				btnSignIn.setVisibility(View.VISIBLE);
			}
			if (btnShowPwd != null) {
				btnShowPwd.setVisibility(View.GONE);
			}
			if (btnForgetPwd != null) {
				btnForgetPwd.setVisibility(View.VISIBLE);
			}

		}
	}

	private EditText.OnEditorActionListener editorAction = new EditText.OnEditorActionListener() {

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_GO
					|| actionId == EditorInfo.IME_ACTION_DONE
					|| (event.getAction() == KeyEvent.ACTION_DOWN && event
							.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
				if (btnSignIn != null) {
					btnSignIn.performClick();
				}
				return true;
			}
			return false;
		}
	};

	private View.OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			switch (id) {
			case R.id.btn_sign_in:
				if (!TextUtils.isEmpty(etPassword.getText())
						&& !TextUtils.isEmpty(mRegFlag)) {
					InputMethodManager imm = (InputMethodManager) getActivity()
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(etPassword.getWindowToken(), 0);

					new doLoginTask().execute(etIdentity.getText().toString()
							.trim(), Provider.STR_EMAIL, etPassword.getText()
							.toString());
				}
				break;
			case R.id.btn_login_way_1:
			case R.id.btn_login_way_2:
				String tag = (String) v.getTag();
				if (mCallBack != null) {
					Bundle param = new Bundle();
					param.putInt("action",
							LandingActivity.ACTIVITY_RESULT_SIGNIN);
					param.putString("provider", tag);
					mCallBack.onSwitch(LoginFragment.this, param);
				}
				break;
			case R.id.btn_setup_new:
				if (!TextUtils.isEmpty(etPassword.getText())
						&& !TextUtils.isEmpty(mRegFlag)) {
					InputMethodManager imm = (InputMethodManager) getActivity()
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(etPassword.getWindowToken(), 0);

					new doLoginTask()
							.execute(etIdentity.getText().toString().trim(),
									Provider.STR_EMAIL, etPassword.getText()
											.toString(), etUsername.getText()
											.toString().trim());
				}

				break;
			case R.id.forget_password:
				break;
			case R.id.show_password:
				setShowPwd(isShowPwd());
				break;
			default:
				break;
			}
		}
	};

	private class doLoginTask extends AsyncTask<String, Integer, Response> {

		String external_id = null;
		String password = null;
		String provider = null;
		String name = null;

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			btnSignIn.setEnabled(false);
			btnSetupNew.setEnabled(false);
			pbIndicator.setVisibility(View.VISIBLE);
			tvHint.setText("");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Response doInBackground(String... params) {
			external_id = params[0];
			provider = params[1];
			password = params[2];

			if (params.length == 3) {
				return mModel.getServer().signIn(external_id, provider,
						password);
			} else {
				name = params[3];
				return mModel.getServer().setUpNewUser(external_id, provider,
						password, name);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Response result) {
			super.onPostExecute(result);
			if (result != null) {
				int code;
				code = result.getCode();
				switch (code) {
				case HttpStatus.SC_OK:
					JSONObject resp = result.getResponse();
					String token = resp.optString("token");
					long user_id = resp.optLong("user_id");

					mModel.Me().setUsername(external_id);
					mModel.Me().setProvider(provider);
					mModel.Me().setToken(token);
					mModel.Me().setUserId(user_id);
					mModel.Me().fetchProfile();
					mModel.Me().setExternalId("");

					((Activity) getActivity()).registGCM();

					if (mCallBack != null) {
						Bundle param = new Bundle();
						param.putInt(LandingActivity.FIELD_ACTION,
								LandingActivity.ACTIVITY_RESULT_CROSS);
						mCallBack.onSwitch(LoginFragment.this, param);
					}
					break;
				case HttpStatus.SC_NOT_FOUND:
					tvHint.setText(R.string.login_sign_in_fail_hint);
					break;
				default:
					break;
				}

			}
			pbIndicator.setVisibility(View.GONE);
			btnSignIn.setEnabled(true);
			btnSetupNew.setEnabled(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onCancelled()
		 */
		@Override
		protected void onCancelled() {
			super.onCancelled();
			pbIndicator.setVisibility(View.GONE);
			btnSignIn.setEnabled(true);
			btnSetupNew.setEnabled(true);
		}

	}

}