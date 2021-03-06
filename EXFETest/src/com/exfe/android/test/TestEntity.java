package com.exfe.android.test;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.text.TextUtils;

import com.exfe.android.controller.LandingActivity;
import com.exfe.android.debug.Log;
import com.exfe.android.model.Model;
import com.exfe.android.model.entity.Cross;
import com.exfe.android.model.entity.CrossTime;
import com.exfe.android.model.entity.EFTime;
import com.exfe.android.model.entity.Entity;
import com.exfe.android.model.entity.EntityFactory;
import com.exfe.android.model.entity.Invitation;
import com.exfe.android.model.entity.Response;
import com.exfe.android.model.entity.User;
import com.exfe.android.util.Tool;

/**
 * @author stony
 * 
 */
public class TestEntity extends
		ActivityInstrumentationTestCase2<LandingActivity> {
	public static final String TAG = TestEntity.class.getSimpleName();

	private Model mModel = null;
	private LandingActivity mActivity = null;

	/**
	 * @param name
	 */
	public TestEntity() {
		super(LandingActivity.class);
		Log.d(TAG, "init of TestServer");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.test.ActivityInstrumentationTestCase2#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		Log.d(TAG, "setup of TestServer");
		mActivity = getActivity();
		mModel = mActivity.getModel();
		// target context's resouce.
		// getInstrumentation().getTargetContext().getResources()
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.test.ActivityInstrumentationTestCase2#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public final void testCross() {
		String crossStr = Helpler.getFromAssets(this, "user_39_crosses.js");
		assertNotNull("test file user39.crosses.js is missing", crossStr);

		Response response = new Response(crossStr, null, null);

		JSONObject res = response.getResponse();
		JSONArray array = res.optJSONArray("crosses");
		if (array != null) {
			for (int i = 0; i < array.length(); i++) {
				JSONObject json = array.optJSONObject(i);
				if (json != null) {
					Cross cross = (Cross) EntityFactory.create(json);
					Log.d(TAG, "%s", json.toString());
					assertTrue(cross.getId() > 0);
					Log.d(TAG, "%s", cross.toJSON().toString());
				}
			}
		}
	}

	public final void testCrossTime() {
		Tool.NOW = new Date(112, 3, 4, 14, 8, 0);
		CrossTime c = null;

		Tool.TIME_ZONE = "+08:00 CST";
		c = new CrossTime(new EFTime("", "2012-04-04", "", "06:08:00",
				"+08:00 CST"), "2012-04-04 14:08:00", CrossTime.MARK_FORMAT);
		assertEquals("2:08PM on Wed, Apr 4", c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("", "2012-04-04", "", "06:08:00",
				"+08:00 CST"), "2012-04-04 2:08:00 pm abc",
				CrossTime.MARK_ORIGINAL);
		assertEquals("2012-04-04 2:08:00 pm abc", c.getLongLocalTimeSring(null));

		// Time_word (at) Time Date_word (on) Date
		c = new CrossTime(new EFTime("This Week", "", "", "", "+08:00 CST"),
				"This week", CrossTime.MARK_FORMAT);
		assertEquals("This Week", c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("", "2012-04-04", "", "", "+08:00 CST"),
				"2012 4 4", CrossTime.MARK_FORMAT);
		assertEquals("Wed, Apr 4", c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("", "", "Dinner", "", "+08:00 CST"),
				"dinner", CrossTime.MARK_FORMAT);
		assertEquals("Dinner", c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("", "", "", "06:08:00", "+08:00 CST"),
				"14:08:00", CrossTime.MARK_FORMAT);
		assertEquals("2:08PM", c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("This Week", "2012-04-04", "", "",
				"+08:00 CST"), "This week 2012 04 04", CrossTime.MARK_FORMAT);
		assertEquals("This Week on Wed, Apr 4", c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("This Week", "", "Dinner", "",
				"+08:00 CST"), "dinner this week", CrossTime.MARK_FORMAT);
		assertEquals("Dinner This Week", c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("This Week", "", "", "06:08:00",
				"+08:00 CST"), "14:08 this week", CrossTime.MARK_FORMAT);
		assertEquals("2:08PM This Week", c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("", "2012-04-04", "Dinner", "",
				"+08:00 CST"), "dinner 2012-04-04", CrossTime.MARK_FORMAT);
		assertEquals("Dinner on Wed, Apr 4", c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("", "2012-04-04", "", "06:08:00",
				"+08:00 CST"), "2012 04 04 14:08", CrossTime.MARK_FORMAT);
		assertEquals("2:08PM on Wed, Apr 4", c.getLongLocalTimeSring(null));
		c = new CrossTime(
				new EFTime("", "", "Dinner", "06:08:00", "+08:00 CST"),
				"dinner at 14:08", CrossTime.MARK_FORMAT);
		assertEquals("Dinner at 2:08PM", c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("This Week", "2012-04-04", "Dinner", "",
				"+08:00 CST"), "dinner this week 2012-04-04",
				CrossTime.MARK_FORMAT);
		assertEquals("Dinner This Week on Wed, Apr 4",
				c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("This Week", "2012-04-04", "", "06:08:00",
				"+08:00 CST"), "14:08 this week 2012-04-04",
				CrossTime.MARK_FORMAT);
		assertEquals("2:08PM This Week on Wed, Apr 4",
				c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("This Week", "", "Dinner", "06:08:00",
				"+08:00 CST"), "dinner 14:08 this week", CrossTime.MARK_FORMAT);
		assertEquals("Dinner at 2:08PM This Week",
				c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("This Week", "2012-04-04", "Dinner",
				"06:08:00", "+08:00 CST"), "dinner 14:08 this week 2012-4-4",
				CrossTime.MARK_FORMAT);
		assertEquals("Dinner at 2:08PM This Week on Wed, Apr 4",
				c.getLongLocalTimeSring(null));

		// different target zone format string ??
		c = new CrossTime(new EFTime("", "2012-04-04", "", "06:08:00",
				"+08:00 CST"), "2012-04-04 14:8:00", CrossTime.MARK_FORMAT);
		assertEquals("2:08PM on Wed, Apr 4", c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("", "2012-04-04", "", "06:08:00",
				"+08:00 CST"), "2012-04-04 14:8:00", CrossTime.MARK_FORMAT);
		Tool.TIME_ZONE = "";
		assertEquals("2:08PM on Wed, Apr 4", c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("", "2012-04-04", "", "06:08:00",
				"+08:00 CST"), "2012-04-04 14:8:00", CrossTime.MARK_FORMAT);
		Tool.TIME_ZONE = "+08:00 PST";
		assertEquals("2:08PM on Wed, Apr 4", c.getLongLocalTimeSring(null));

		// if Origin, use CrossTime zone
		c = new CrossTime(new EFTime("", "2012-04-04", "", "06:08:00",
				"+08:00 CST"), "2012-04-04 14:8:00", CrossTime.MARK_FORMAT);
		Tool.TIME_ZONE = "+09:00 PST";
		assertEquals("3:08PM +09:00 PST on Wed, Apr 4",
				c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("", "2012-04-04", "", "06:08:00",
				"+08:00 CST"), "2012-04-04 14:8:00 abc",
				CrossTime.MARK_ORIGINAL);
		assertEquals("2012-04-04 14:8:00 abc +08:00 CST",
				c.getLongLocalTimeSring(null));

		// Time_word (at) Time Zone Date_word (on) Date
		// Only show Zone with Time_word or Time
		Tool.TIME_ZONE = "+09:00 PST";
		c = new CrossTime(new EFTime("This Week", "", "", "", "+08:00 CST"),
				"this week", CrossTime.MARK_FORMAT);
		assertEquals("This Week", c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("", "2012-04-04", "", "", "+08:00 CST"),
				"2012-04-04", CrossTime.MARK_FORMAT);
		assertEquals("Wed, Apr 4", c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("", "", "Dinner", "", "+08:00 CST"),
				"dinner", CrossTime.MARK_FORMAT);
		assertEquals("Dinner +08:00 CST", c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("", "", "", "06:08:00", "+08:00 CST"),
				"14:08", CrossTime.MARK_FORMAT);
		assertEquals("3:08PM +09:00 PST", c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("This Week", "2012-04-04", "", "",
				"+08:00 CST"), "this week 2012 4 4", CrossTime.MARK_FORMAT);
		assertEquals("This Week on Wed, Apr 4", c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("This Week", "", "Dinner", "",
				"+08:00 CST"), "dinner this week", CrossTime.MARK_FORMAT);
		assertEquals("Dinner +08:00 CST This Week",
				c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("This Week", "", "", "06:08:00",
				"+08:00 CST"), "14:08 this week", CrossTime.MARK_FORMAT);
		assertEquals("3:08PM +09:00 PST This Week",
				c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("", "2012-04-04", "Dinner", "",
				"+08:00 CST"), "dinner 2012-04-04", CrossTime.MARK_FORMAT);
		assertEquals("Dinner +08:00 CST on Wed, Apr 4",
				c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("", "2012-04-04", "", "06:08:00",
				"+08:00 CST"), "2012-04-04 14:08", CrossTime.MARK_FORMAT);
		assertEquals("3:08PM +09:00 PST on Wed, Apr 4",
				c.getLongLocalTimeSring(null));
		c = new CrossTime(
				new EFTime("", "", "Dinner", "06:08:00", "+08:00 CST"),
				"dinner 14:08", CrossTime.MARK_FORMAT);
		assertEquals("Dinner at 3:08PM +09:00 PST",
				c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("This Week", "2012-04-04", "Dinner", "",
				"+08:00 CST"), "dinner this week 2012-04-04",
				CrossTime.MARK_FORMAT);
		assertEquals("Dinner +08:00 CST This Week on Wed, Apr 4",
				c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("This Week", "2012-04-04", "", "06:08:00",
				"+08:00 CST"), "14:08 this week 2012 04 04",
				CrossTime.MARK_FORMAT);
		assertEquals("3:08PM +09:00 PST This Week on Wed, Apr 4",
				c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("This Week", "", "Dinner", "06:08:00",
				"+08:00 CST"), "14:08 dinner this week", CrossTime.MARK_FORMAT);
		assertEquals("Dinner at 3:08PM +09:00 PST This Week",
				c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("This Week", "2012-04-04", "Dinner",
				"06:08:00", "+08:00 CST"), "14:08 dinner this week 2012 04 04",
				CrossTime.MARK_FORMAT);
		assertEquals("Dinner at 3:08PM +09:00 PST This Week on Wed, Apr 4",
				c.getLongLocalTimeSring(null));

		// different target zone format
		Tool.TIME_ZONE = "+09:00";
		c = new CrossTime(new EFTime("", "2012-04-04", "", "06:08:00",
				"+08:00 CST"), "2012-04-04 14:8:00", CrossTime.MARK_FORMAT);
		assertEquals("3:08PM +09:00 on Wed, Apr 4",
				c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("", "2012-04-04", "", "06:08:00",
				"+08:00 CST"), "2012-04-04 14:8:00", CrossTime.MARK_FORMAT);
		Tool.TIME_ZONE = "";
		assertEquals("2:08PM on Wed, Apr 4", c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("", "2012-04-04", "", "06:08:00",
				"+08:00 CST"), "2012-04-04 14:8:00", CrossTime.MARK_FORMAT);
		Tool.TIME_ZONE = "+09:00 PST";
		assertEquals("3:08PM +09:00 PST on Wed, Apr 4",
				c.getLongLocalTimeSring(null));

		// different year
		// Time_word (at) Time Date_word (on) Date
		c = new CrossTime(new EFTime("", "2011-04-04", "", "", "+08:00 CST"),
				"2011-04-04", CrossTime.MARK_FORMAT);
		Tool.TIME_ZONE = "+08:00 CST";
		assertEquals("Mon, Apr 4, 2011", c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("This Week", "2011-04-04", "", "",
				"+08:00 CST"), "this week 2011-04-04", CrossTime.MARK_FORMAT);
		assertEquals("This Week on Mon, Apr 4, 2011",
				c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("", "2011-04-04", "Dinner", "",
				"+08:00 CST"), "dinner 2011-04-04", CrossTime.MARK_FORMAT);
		assertEquals("Dinner on Mon, Apr 4, 2011",
				c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("", "2011-04-04", "", "06:08:00",
				"+08:00 CST"), "2011-04-04 14:08", CrossTime.MARK_FORMAT);
		assertEquals("2:08PM on Mon, Apr 4, 2011",
				c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("This Week", "2011-04-04", "Dinner", "",
				"+08:00 CST"), "2011-04-04 dinner this week",
				CrossTime.MARK_FORMAT);
		assertEquals("Dinner This Week on Mon, Apr 4, 2011",
				c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("This Week", "2011-04-04", "", "06:08:00",
				"+08:00 CST"), "this week 2011-04-04 14:8:00",
				CrossTime.MARK_FORMAT);
		assertEquals("2:08PM This Week on Mon, Apr 4, 2011",
				c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("This Week", "2011-04-04", "Dinner",
				"06:08:00", "+08:00 CST"), "14:08 this week 2011 04 04",
				CrossTime.MARK_FORMAT);
		assertEquals("Dinner at 2:08PM This Week on Mon, Apr 4, 2011",
				c.getLongLocalTimeSring(null));

		// Time Zone & Different year
		c = new CrossTime(new EFTime("This Week", "2011-12-31", "Dinner",
				"06:08:00", "+08:00 CST"), "Dinner 14:08 this week 2011 12 31",
				CrossTime.MARK_FORMAT);
		assertEquals("Dinner at 2:08PM This Week on Sat, Dec 31, 2011",
				c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("This Week", "2011-12-31", "Breakfast",
				"23:08:00", "-08:00 PST"), "Breakfast 15:08 this week 2011 12 31",
				CrossTime.MARK_FORMAT);
		assertEquals("Breakfast at 7:08AM +08:00 CST This Week on Sun, Jan 1",
				c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("This Week", "2012-01-01", "Dinner",
				"06:08:00", "+08:00 CST"), "Dinner 14:08 this week 2012 01 01",
				CrossTime.MARK_FORMAT);
		Tool.TIME_ZONE = "-08:00 PST";
		assertEquals(
				"Dinner at 10:08PM -08:00 PST This Week on Sat, Dec 31, 2011",
				c.getLongLocalTimeSring(null));
		c = new CrossTime(new EFTime("This Week", "2012-01-01", "Dinner", "",
				"+08:00 CST"), "Dinner this week 2012 01 01",
				CrossTime.MARK_FORMAT);
		assertEquals("Dinner +08:00 CST This Week on Sun, Jan 1",
				c.getLongLocalTimeSring(null));
	}

	public final void testToolXRelativeTime() {

		Calendar base = new GregorianCalendar(2012, 8 - 1, 13, 14, 0, 0);
		Tool.NOW = base.getTime();
		Resources res = mActivity.getResources();

		assertEquals("29 days ago", Tool.getXRelativeString(
				new GregorianCalendar(2012, 7 - 1, 15, 16, 10, 0).getTime(),
				res));
		assertEquals("The day before yesterday",
				Tool.getXRelativeString(new GregorianCalendar(2012, 8 - 1, 11,
						0, 10, 0).getTime(), res));
		assertEquals("Yesterday",
				Tool.getXRelativeString(new GregorianCalendar(2012, 8 - 1, 12,
						0, 10, 0).getTime(), res));
		assertEquals("Yesterday",
				Tool.getXRelativeString(new GregorianCalendar(2012, 8 - 1, 12,
						15, 0, 0).getTime(), res));
		assertEquals("14 hours ago",
				Tool.getXRelativeString(new GregorianCalendar(2012, 8 - 1, 13,
						0, 10, 0).getTime(), res));
		assertEquals("2 hours ago", Tool.getXRelativeString(
				new GregorianCalendar(2012, 8 - 1, 13, 12, 10, 0).getTime(),
				res));
		assertEquals("1 hour ago", Tool.getXRelativeString(
				new GregorianCalendar(2012, 8 - 1, 13, 12, 50, 0).getTime(),
				res));
		assertEquals("Just now", Tool.getXRelativeString(new GregorianCalendar(
				2012, 8 - 1, 13, 13, 10, 0).getTime(), res));
		assertEquals("Now", Tool.getXRelativeString(new GregorianCalendar(2012,
				8 - 1, 13, 13, 50, 0).getTime(), res));
		assertEquals("Now", Tool.getXRelativeString(new GregorianCalendar(2012,
				8 - 1, 13, 14, 0, 0).getTime(), res));
		assertEquals("In 20 minutes", Tool.getXRelativeString(
				new GregorianCalendar(2012, 8 - 1, 13, 14, 20, 0).getTime(),
				res));
		assertEquals("In 1 hour", Tool.getXRelativeString(
				new GregorianCalendar(2012, 8 - 1, 13, 15, 30, 0).getTime(),
				res));
		assertEquals("In 2 hours", Tool.getXRelativeString(
				new GregorianCalendar(2012, 8 - 1, 13, 15, 50, 0).getTime(),
				res));
		assertEquals("Today", Tool.getXRelativeString(new GregorianCalendar(
				2012, 8 - 1, 13, 23, 50, 0).getTime(), new GregorianCalendar(
				2012, 8 - 1, 13, 4, 0, 0).getTime(), res));
		assertEquals("Tomorrow", Tool.getXRelativeString(new GregorianCalendar(
				2012, 8 - 1, 14, 4, 10, 0).getTime(), res));
		assertEquals("The day after tomorrow",
				Tool.getXRelativeString(new GregorianCalendar(2012, 8 - 1, 15,
						0, 10, 0).getTime(), res));
		assertEquals("Thursday", Tool.getXRelativeString(new GregorianCalendar(
				2012, 8 - 1, 16, 14, 0, 0).getTime(), res));
		assertEquals("Next Sunday",
				Tool.getXRelativeString(new GregorianCalendar(2012, 8 - 1, 19,
						14, 0, 0).getTime(), res));
		assertEquals("Next Monday",
				Tool.getXRelativeString(new GregorianCalendar(2012, 8 - 1, 20,
						12, 0, 0).getTime(), res));
		assertEquals("Next Saturday",
				Tool.getXRelativeString(new GregorianCalendar(2012, 8 - 1, 25,
						12, 0, 0).getTime(), res));
		assertEquals("In 15 days", Tool.getXRelativeString(
				new GregorianCalendar(2012, 8 - 1, 28, 1, 0, 0).getTime(), res));
		assertEquals("In 1 month", Tool.getXRelativeString(
				new GregorianCalendar(2012, 9 - 1, 13, 1, 0, 0).getTime(), res));
		assertEquals("In 2 months",
				Tool.getXRelativeString(new GregorianCalendar(2012, 10 - 1, 12,
						1, 0, 0).getTime(), res));
		assertEquals("In 11 months", Tool.getXRelativeString(
				new GregorianCalendar(2013, 8 - 1, 1, 1, 0, 0).getTime(), res));
		assertEquals("In 1 year", Tool.getXRelativeString(
				new GregorianCalendar(2013, 8 - 1, 13, 1, 0, 0).getTime(), res));
		assertEquals("In 1 year and 1 month", Tool.getXRelativeString(
				new GregorianCalendar(2013, 9 - 1, 12, 1, 0, 0).getTime(), res));
		assertEquals("In 1 year and 2 months",
				Tool.getXRelativeString(new GregorianCalendar(2013, 10 - 1, 12,
						1, 0, 0).getTime(), res));
	}

	public final void testEntityParseJSON() {
		Entity e = new Cross();
		assertNotNull("abc", e.toJSON());
		printInfo(e, null);

		User u = new User();
		printInfo(u, null);
		assertNotNull("abc", u.toJSON());
	}

	public void printInfo(Entity e, Class<? extends Entity> clz) {
		if (clz == null) {
			clz = e.getClass();
		}
		Log.d(TAG, "=====%s Start=====", clz.getName());

		@SuppressWarnings("rawtypes")
		Class s = clz.getSuperclass();
		if (!clz.equals(Entity.class)) {
			printInfo(e, s);
		}

		Field[] flds = clz.getDeclaredFields();
		for (Field f : flds) {
			Log.d(TAG, "parseJSON: field name:%s, type:%s, modifier: %d",
					f.getName(), f.getType(), f.getModifiers());
		}

		/*
		 * Method[] mths = clz.getMethods(); for(Method m: mths){ Log.d(TAG,
		 * "parseJSON: method name:%s, returntype:%s,  modifier: %d",
		 * m.getName(), m.getReturnType(), m.getModifiers()); }
		 */
		Log.d(TAG, "=====%s End=====", clz.getName());
	}

	public void testIsJson() {

		String data = "{\"meta\":{\"code\":200},\"response\":{\"device_token\":null,\"identity_id\":\"100\"}}";
		Pattern p1 = Pattern.compile("^\\{.*\\}$");
		Matcher m = p1.matcher(data);
		if (m.find()) {
			Log.d("RegEx", "match: %s", m.group());
		}

		assertEquals(true, Pattern.matches("\\{.*?\\}", data));
		// assertEquals(true, Pattern.matches("^\\{", data));
		assertEquals(true, Pattern.matches("^\\{.*", data));
		assertEquals(true, Pattern.matches("^\\{.*\\}", data));
		// assertEquals(true, Pattern.matches("\\}$", data));
		assertEquals(true, Pattern.matches(".*\\}$", data));
		assertEquals(true, Pattern.matches("\\{.*\\}$", data));
		assertEquals(true, Pattern.matches("^\\{.*\\}$", data));
	}

	public final void testModifyCross() {
		assertFalse("Missing token", TextUtils.isEmpty(mModel.Me().getToken()));

		Collection<Cross> list = mModel.Crosses().getCrosses();
		Iterator<Cross> it = list.iterator();
		assertTrue(it.hasNext());
		Cross cross = it.next();
		cross.loadFromDao(mModel.getHelper());
		Collection<Invitation> invs = cross.getExfee().getInvitations();
		Iterator<Invitation> it2 = invs.iterator();
		assertTrue(it.hasNext());
		Invitation inv = null;
		while (it2.hasNext()) {
			Invitation i = it2.next();
			if (i.isHost() != true) {
				inv = i;
				break;
			}
		}
		assertNotNull(inv);

		try {
			Invitation invNew = inv.cloneSelf();
			int oldMates = inv.getMates();
			inv.setMates(oldMates + 2);
			mModel.getHelper().getInvitationDao().update(inv);
			Invitation invSaved1 = mModel.getHelper().getInvitationDao()
					.queryForId(inv.getId());
			assertEquals(oldMates + 2, invSaved1.getMates());

			invNew.setMates(oldMates + 1);
			Date d = invNew.getUpdateAt();
			d.setTime(d.getTime() + 10 * 60 * 1000);
			invNew.setUpdateAt(d);
			invNew.setExfee(cross.getExfee());
			assertEquals(oldMates + 1, invNew.getMates());
			int ret = mModel.getHelper().getInvitationDao().update(invNew);
			// invNew.saveToDao(mModel.getHelper());

			Invitation invSaved2 = mModel.getHelper().getInvitationDao()
					.queryForId(invNew.getId());
			assertEquals(oldMates + 1, invSaved2.getMates());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertFalse(e.getMessage(), true);
		}

	}
}
