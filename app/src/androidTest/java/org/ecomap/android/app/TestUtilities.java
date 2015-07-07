package org.ecomap.android.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import org.ecomap.android.app.data.EcoMapContract;
import org.ecomap.android.app.data.EcoMapDBHelper;
import org.ecomap.android.app.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

/**
 * Created by yura on 7/7/15.
 */
public class TestUtilities extends AndroidTestCase{
    static final int TEST_COMMENTS_NUMBER = 1;
    static final String TEST_TITLE = "Problem 1";
    static final String TEST_CONTENT = "Problem 1 content";
    static final long TEST_DATE = 1419033600L;  // December 20th, 2014
    static final double TEST_LATITUDE = 50.231;
    static final double TEST_LONGITUDE = 30.2134;
    static final int TEST_PROBLEM_TYPE_ID = 1;
    static final String TEST_PROPOSAL = "Problem 1 proposal";
    static final int TEST_REGION_ID = 1;
    static final int TEST_SEVERITY = 1;
    static final String TEST_STATUS = "UNSOLVED";
    static final String TEST_USER_NAME = "Ivan Ivanenko";
    static final int TEST_VOTES_NUMBER = 1;

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    /*
        Students: Use this to create some default weather values for your database tests.
     */
    static ContentValues createProblemValues() {
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(EcoMapContract.ProblemsEntry.COLUMN_COMMENTS_NUMBER, TEST_COMMENTS_NUMBER);
        weatherValues.put(EcoMapContract.ProblemsEntry.COLUMN_TITLE, TEST_TITLE);
        weatherValues.put(EcoMapContract.ProblemsEntry.COLUMN_CONTENT, TEST_CONTENT);
        weatherValues.put(EcoMapContract.ProblemsEntry.COLUMN_DATE, TEST_DATE);
        weatherValues.put(EcoMapContract.ProblemsEntry.COLUMN_LATITUDE, TEST_LATITUDE);
        weatherValues.put(EcoMapContract.ProblemsEntry.COLUMN_LONGTITUDE, TEST_LONGITUDE);
        weatherValues.put(EcoMapContract.ProblemsEntry.COLUMN_PROBLEM_TYPE_ID, TEST_PROBLEM_TYPE_ID);
        weatherValues.put(EcoMapContract.ProblemsEntry.COLUMN_PROPOSAL, TEST_PROPOSAL);
        weatherValues.put(EcoMapContract.ProblemsEntry.COLUMN_REGION_ID, TEST_REGION_ID);
        weatherValues.put(EcoMapContract.ProblemsEntry.COLUMN_SEVERITY, TEST_SEVERITY);
        weatherValues.put(EcoMapContract.ProblemsEntry.COLUMN_STATUS, TEST_STATUS);
        weatherValues.put(EcoMapContract.ProblemsEntry.COLUMN_USER_NAME, TEST_USER_NAME);
        weatherValues.put(EcoMapContract.ProblemsEntry.COLUMN_VOTES_NUMBER, TEST_VOTES_NUMBER);
        return weatherValues;
    }

    /*
        Students: You can uncomment this function once you have finished creating the
        LocationEntry part of the EcoMapContract as well as the WeatherDbHelper.
     */
    static long insertProblemsValues(Context context) {
        // insert our test records into the database
        EcoMapDBHelper dbHelper = new EcoMapDBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createProblemValues();

        long problemRowId;
        problemRowId = db.insert(EcoMapContract.ProblemsEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert North Pole Location Values", problemRowId != -1);

        return problemRowId;
    }

    /*
        Students: The functions we provide inside of TestProvider use this utility class to test
        the ContentObserver callbacks using the PollingCheck class that we grabbed from the Android
        CTS tests.

        Note that this only tests that the onChange function is called; it does not test that the
        correct Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
