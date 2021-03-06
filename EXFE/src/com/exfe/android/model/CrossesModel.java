package com.exfe.android.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;

import com.exfe.android.PrefKeys;
import com.exfe.android.model.entity.Cross;
import com.exfe.android.model.entity.EntityFactory;
import com.exfe.android.model.entity.Response;
import com.j256.ormlite.dao.Dao;

public class CrossesModel {

	public static final int ACTION_TYPE_UPDATE_CROSSES = Model.ACTION_TYPE_CROSSES_BASE + 1;
	public static final int ACTION_TYPE_CROSSES_UPDATE_MILESTONE = Model.ACTION_TYPE_CROSSES_BASE + 2;
	public static final int ACTION_TYPE_REMOVE_CROSS = Model.ACTION_TYPE_CROSSES_BASE + 3;
	
	public static final String FIELD_CHANGE_ID = "change_id";
	public static final String FIELD_CHANGED_ID_LIST = "change_id_list";

	private Model mRoot = null;

	private Date mLastUpdateQuery = null;
	private FetchCrossesTask mTask = null;

	public CrossesModel(Model m) {
		mRoot = m;
	}

	private Dao<Cross, Long> getDao() {
		return mRoot.getHelper().getCrossDao();
	}

	public void addCross(Cross x) {
		List<Cross> xs = new ArrayList<Cross>(1);
		xs.add(x);
		addCrosses(xs);
	}

	public void addCrosses(List<Cross> xs) {

		if (xs != null && !xs.isEmpty()) {
			List<Long> update = new ArrayList<Long>();
			for (Cross x : xs) {
				if (x != null /* && x.getByIdentitiy() != null */) {
					x.saveToDao(mRoot.getHelper());
					update.add(x.getId());
				}
			}
			if (update.size() > 0) {
				mRoot.setChanged();
				Bundle data = new Bundle();
				data.putInt(Model.OBSERVER_FIELD_TYPE,
						ACTION_TYPE_UPDATE_CROSSES);
				long[] value = new long[update.size()];
				for (int i = 0; i < value.length; i++) {
					value[i] = update.get(i);
				}
				data.putLongArray(FIELD_CHANGED_ID_LIST, value);
				mRoot.notifyObservers(data);
			}
		}
	}

	/**
	 * @return the lastUpdateQuery
	 */
	public Date getLastUpdateQuery() {
		if (mLastUpdateQuery == null) {
			long last = mRoot.getDefaultSharedPreference().getLong(
					PrefKeys.LAST_UPDATE_QUERY_TIME, 0);
			if (last > 0) {
				mLastUpdateQuery = new Date(last);
			}
		}
		return mLastUpdateQuery;
	}

	/**
	 * @param lastUpdateQuery
	 *            the lastUpdateQuery to set
	 */
	public void setLastUpdateQuery(Date lastUpdateQuery) {
		Editor edit = mRoot.getDefaultSharedPreference().edit();
		if (lastUpdateQuery != null) {
			edit.putLong(PrefKeys.LAST_UPDATE_QUERY_TIME,
					lastUpdateQuery.getTime());
		} else {
			edit.remove(PrefKeys.LAST_UPDATE_QUERY_TIME);
		}
		edit.commit();
		this.mLastUpdateQuery = lastUpdateQuery;
	}

	public void deleteCrossById(Long crossId){
		// delete the cross
		try {
			getDao().deleteById(crossId);
			
			// notify UI
			mRoot.setChanged();
			Bundle data = new Bundle();
			data.putInt(Model.OBSERVER_FIELD_TYPE, ACTION_TYPE_REMOVE_CROSS);
			data.putLong(FIELD_CHANGE_ID, crossId);
			mRoot.notifyObservers(data);
			// cross: finishactivity
			// crosslist refresh list	
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public void clearCrosses() {
		try {
			getDao().delete(getDao().queryForAll());
			mRoot.setChanged();
			Bundle data = new Bundle();
			data.putInt(Model.OBSERVER_FIELD_TYPE, ACTION_TYPE_UPDATE_CROSSES);
			mRoot.notifyObservers(data);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Collection<Cross> getCrosses() {
		try {
			return getDao().queryForAll();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ArrayList<Cross>();
	}

	public Cross getCrossById(long id) {
		try {
			Cross result = getDao().queryForId(id);
			if (result != null) {
				result.loadFromDao(mRoot.getHelper());
			}
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void saveCross(Cross x) {
		try {
			getDao().createOrUpdate(x);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateLastView(Cross cross) {
		if (cross != null) {
			try {
				cross.setLastViewAt(new Date());
				this.getDao().update(cross);
				mRoot.setChanged();
				Bundle data = new Bundle();
				data.putInt(Model.OBSERVER_FIELD_TYPE,
						ACTION_TYPE_UPDATE_CROSSES);
				mRoot.notifyObservers(data);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	public void fetchCross(final long crossId) {
		fetchCross(crossId, null);
	}

	public void fetchCross(final long crossId, final Date lastUpdate) {
		Runnable run = new Runnable() {

			@Override
			public void run() {
				Response result = mRoot.getServer().getCrossById(crossId, lastUpdate);
				int code = result.getCode();
				@SuppressWarnings("unused")
				int http_category = code % 100;
				mRoot.stopNetworkQuery();
				switch (code) {
				case HttpStatus.SC_OK:
					JSONObject res = result.getResponse();
					JSONObject json = res.optJSONObject("cross");
					if (json != null) {
						Cross cross = (Cross) EntityFactory.create(json);
						List<Cross> xs = new ArrayList<Cross>();
						xs.add(cross);
						addCrosses(xs);
					}
					break;
				case HttpStatus.SC_UNAUTHORIZED:
					// mRoot.signOut();
				case HttpStatus.SC_FORBIDDEN:
					deleteCrossById(crossId);
				case HttpStatus.SC_INTERNAL_SERVER_ERROR:
					// retry
					break;
				default:
					break;
				}
			}
		};
		Thread th = new Thread(run);
		th.start();
	}

	public void freshCrosses() {
		if (mTask == null || mTask.getStatus() == AsyncTask.Status.FINISHED) {
			mTask = new FetchCrossesTask();
			mTask.execute();
		}
	}

	class FetchCrossesTask extends AsyncTask<Integer, Integer, Integer> {

		private Date mLastQueryTime = null;
		private Date mThisQueryTime = null;

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mLastQueryTime = getLastUpdateQuery();
			mRoot.startNetworkQuery();
		}

		@Override
		protected Integer doInBackground(Integer... params) {
			mThisQueryTime = new Date();
			Response result = mRoot.getServer().getNewCrossesAfter(
					mLastQueryTime);
			int code = result.getCode();
			@SuppressWarnings("unused")
			int http_category = code % 100;
			mRoot.stopNetworkQuery();

			if (code == HttpStatus.SC_OK) {
				mRoot.startWaiting();
				List<Cross> xs = new ArrayList<Cross>();
				JSONObject res = result.getResponse();
				JSONArray array = res.optJSONArray("crosses");
				if (array != null) {
					for (int i = 0; i < array.length(); i++) {
						JSONObject json = array.optJSONObject(i);
						if (json != null) {
							Cross cross = (Cross) EntityFactory.create(json);
							xs.add(cross);
						}
					}
				}
				setLastUpdateQuery(mThisQueryTime);
				addCrosses(xs);
				mRoot.stopWaiting();
			}
			return code;
		}

		@Override
		protected void onPostExecute(Integer code) {
			switch (code) {
			case HttpStatus.SC_OK:
				break;
			case HttpStatus.SC_UNAUTHORIZED:
				mRoot.signOut();
			case HttpStatus.SC_INTERNAL_SERVER_ERROR:
				// retry
				break;
			default:
				break;
			}
		}

	}
}
