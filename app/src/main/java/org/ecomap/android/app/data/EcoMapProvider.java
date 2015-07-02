package org.ecomap.android.app.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class EcoMapProvider extends ContentProvider {

    //TODO: db-man! please, add here work with Uri

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private EcoMapDBHelper mOpenHelper;

    static final int PROBLEMS = 100;
    static final int PHOTOS_WITH_PROBLEMS = 102;
    static final int RESOURCES = 103;

    private static final SQLiteQueryBuilder sPhotoByProblemQueryBuilder;

    static{
        sPhotoByProblemQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //weather INNER JOIN location ON weather.location_id = location._id
        sPhotoByProblemQueryBuilder.setTables(
                EcoMapContract.PhotosEntry.TABLE_NAME + " INNER JOIN " +
                        EcoMapContract.ProblemsEntry.TABLE_NAME +
                        " ON " + EcoMapContract.PhotosEntry.TABLE_NAME +
                        "." + EcoMapContract.PhotosEntry.COLUMN_PROBLEM_ID +
                        " = " + EcoMapContract.ProblemsEntry.TABLE_NAME +
                        "." + EcoMapContract.ProblemsEntry._ID);
    }

    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = EcoMapContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, EcoMapContract.PATH_PROBLEMS, PROBLEMS);
        matcher.addURI(authority, EcoMapContract.PATH_PHOTOS, PHOTOS_WITH_PROBLEMS);
        matcher.addURI(authority, EcoMapContract.PATH_RESOURCES, RESOURCES);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new EcoMapDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "weather"
            case PROBLEMS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        EcoMapContract.ProblemsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "location"
            case PHOTOS_WITH_PROBLEMS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        EcoMapContract.PhotosEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case RESOURCES: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        EcoMapContract.ResourcesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PROBLEMS:
                return EcoMapContract.ProblemsEntry.CONTENT_TYPE;
            case PHOTOS_WITH_PROBLEMS:
                return EcoMapContract.PhotosEntry.CONTENT_TYPE;
            case RESOURCES:
                return EcoMapContract.ResourcesEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case PROBLEMS: {
                normalizeDate(values);
                long _id = db.insert(EcoMapContract.ProblemsEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = EcoMapContract.ProblemsEntry.buildProblemsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case RESOURCES: {
                long _id = db.insert(EcoMapContract.ResourcesEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = EcoMapContract.ResourcesEntry.buildResourcesUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case PROBLEMS:
                rowsDeleted = db.delete(
                        EcoMapContract.ProblemsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case RESOURCES:
                rowsDeleted = db.delete(
                        EcoMapContract.ResourcesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    private void normalizeDate(ContentValues values) {
        // normalize the date value
        if (values.containsKey(EcoMapContract.ProblemsEntry.COLUMN_DATE)) {
            long dateValue = values.getAsLong(EcoMapContract.ProblemsEntry.COLUMN_DATE);
         //TODO    values.put(EcoMapContract.ProblemsEntry.COLUMN_DATE, EcoMapContract.normalizeDate(dateValue));
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case PROBLEMS:
                normalizeDate(values);
                rowsUpdated = db.update(EcoMapContract.ProblemsEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;

            case RESOURCES:
                rowsUpdated = db.update(EcoMapContract.ResourcesEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PROBLEMS:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(EcoMapContract.ProblemsEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
