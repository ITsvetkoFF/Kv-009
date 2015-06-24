package org.ecomap.android.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.ecomap.android.app.data.EcoMapContract.ProblemsEntry;

/**
 * Manages a local database for weather data.
 */
public class EcoMapDBHelper extends SQLiteOpenHelper {

    //TODO: db-man! please write here a creation of db

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "ecomap.db";

    public EcoMapDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        // Create a table to hold problems.
        final String SQL_CREATE_PROBLEMS_TABLE = "CREATE TABLE " + ProblemsEntry.TABLE_NAME + " (" +
                ProblemsEntry._ID + " INTEGER PRIMARY KEY," +
                ProblemsEntry.COLUMN_DATE + " TEXT NOT NULL " +
                //add colums
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_PROBLEMS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }
}
