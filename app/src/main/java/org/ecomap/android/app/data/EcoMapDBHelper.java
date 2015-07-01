package org.ecomap.android.app.data;

import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.ecomap.android.app.data.EcoMapContract.PhotosEntry;
import org.ecomap.android.app.data.EcoMapContract.ProblemsEntry;
import org.ecomap.android.app.data.EcoMapContract.ResourcesEntry;

/**
 * Manages a local database for weather data.
 */
public class EcoMapDBHelper extends SQLiteOpenHelper {
    //
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
                ProblemsEntry.COLUMN_STATUS + " TEXT NOT NULL, " +
                ProblemsEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                ProblemsEntry.COLUMN_USER_NAME + " TEXT NOT NULL, " +
                ProblemsEntry.COLUMN_PROBLEM_TYPE_ID + " INTEGER NOT NULL, " +
                ProblemsEntry.COLUMN_SEVERITY + " INTEGER NOT NULL, " +
                ProblemsEntry.COLUMN_VOTES_NUMBER + " INTEGER NOT NULL, " +
                ProblemsEntry.COLUMN_DATE + " TEXT NOT NULL, " +
                ProblemsEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
                ProblemsEntry.COLUMN_LATITUDE + " TEXT NOT NULL, " +
                ProblemsEntry.COLUMN_LONGTITUDE + " TEXT NOT NULL, " +
                ProblemsEntry.COLUMN_PROPOSAL + " TEXT NOT NULL, " +
                ProblemsEntry.COLUMN_REGION_ID + " INTEGER NOT NULL, " +
                ProblemsEntry.COLUMN_COMMENTS_NUMBER + " INTEGER NOT NULL " +
                " );";

        final String SQL_CREATE_PHOTOS_TABLE = "CREATE TABLE " + PhotosEntry.TABLE_NAME + " (" +
                PhotosEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PhotosEntry.COLUMN_PROBLEM_ID + " INTEGER NOT NULL, " +
                PhotosEntry.COLUMN_LINK + " TEXT NOT NULL, " +
                PhotosEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
                PhotosEntry.COLUMN_BLOB + " BLOB NOT NULL, " +

                //setting up PROBLEM_ID as foreign key
                " FOREIGN KEY (" + PhotosEntry.COLUMN_PROBLEM_ID + ") REFERENCES " +
                ProblemsEntry.TABLE_NAME + " (" + ProblemsEntry._ID + " );";

        final String SQL_CREATE_RESOURCES_TABLE = "CREATE TABLE " + ResourcesEntry.TABLE_NAME + " (" +
                ResourcesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ResourcesEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                ResourcesEntry.COLUMN_CONTENT + " TEXT NOT NULL" +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_PROBLEMS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_PHOTOS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_RESOURCES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }
}
