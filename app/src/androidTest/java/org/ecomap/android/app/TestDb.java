package org.ecomap.android.app;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import org.ecomap.android.app.data.EcoMapContract;
import org.ecomap.android.app.data.EcoMapDBHelper;

import java.util.HashSet;

/**
 * Created by yura on 7/7/15.
 */
public class TestDb extends AndroidTestCase {

    void deleteTheDatabase() {
        mContext.deleteDatabase(EcoMapDBHelper.DATABASE_NAME);
    }

    public void setUp() {
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(EcoMapContract.ProblemsEntry.TABLE_NAME);
        tableNameHashSet.add(EcoMapContract.ResourcesEntry.TABLE_NAME);

        mContext.deleteDatabase(EcoMapDBHelper.DATABASE_NAME);
        SQLiteDatabase db = new EcoMapDBHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without both the location entry and weather entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + EcoMapContract.ProblemsEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> problemsColumnHashSet = new HashSet<String>();
        problemsColumnHashSet.add(EcoMapContract.ProblemsEntry._ID);
        problemsColumnHashSet.add(EcoMapContract.ProblemsEntry.COLUMN_COMMENTS_NUMBER);
        problemsColumnHashSet.add(EcoMapContract.ProblemsEntry.COLUMN_TITLE);
        problemsColumnHashSet.add(EcoMapContract.ProblemsEntry.COLUMN_CONTENT);
        problemsColumnHashSet.add(EcoMapContract.ProblemsEntry.COLUMN_DATE);
        problemsColumnHashSet.add(EcoMapContract.ProblemsEntry.COLUMN_LATITUDE);
        problemsColumnHashSet.add(EcoMapContract.ProblemsEntry.COLUMN_LONGTITUDE);
        problemsColumnHashSet.add(EcoMapContract.ProblemsEntry.COLUMN_PROBLEM_TYPE_ID);
        problemsColumnHashSet.add(EcoMapContract.ProblemsEntry.COLUMN_PROPOSAL);
        problemsColumnHashSet.add(EcoMapContract.ProblemsEntry.COLUMN_REGION_ID);
        problemsColumnHashSet.add(EcoMapContract.ProblemsEntry.COLUMN_SEVERITY);
        problemsColumnHashSet.add(EcoMapContract.ProblemsEntry.COLUMN_STATUS);
        problemsColumnHashSet.add(EcoMapContract.ProblemsEntry.COLUMN_USER_FIRST_NAME);
        problemsColumnHashSet.add(EcoMapContract.ProblemsEntry.COLUMN_USER_LAST_NAME);
        problemsColumnHashSet.add(EcoMapContract.ProblemsEntry.COLUMN_NUMBER_OF_VOTES);


        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            problemsColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                problemsColumnHashSet.isEmpty());
        db.close();
    }

    public void testProblemsTable() {

        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        EcoMapDBHelper dbHelper = new EcoMapDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create ContentValues of what you want to insert
        // (you can use the createNorthPoleLocationValues if you wish)
        ContentValues testValues = TestUtilities.createProblemValues();

        // Third Step: Insert ContentValues into database and get a row ID back
        long locationRowId;
        locationRowId = db.insert(EcoMapContract.ProblemsEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                EcoMapContract.ProblemsEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue( "Error: No Records returned from location query", cursor.moveToFirst() );

        // Fifth Step: Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("Error: Location Query Validation Failed",
                cursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from location query",
                cursor.moveToNext() );

        // Sixth Step: Close Cursor and Database
        cursor.close();
        db.close();
    }

}