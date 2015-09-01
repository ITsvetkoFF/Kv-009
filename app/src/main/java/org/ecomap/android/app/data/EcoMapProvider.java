package org.ecomap.android.app.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

public class EcoMapProvider extends ContentProvider {

    //TODO: db-man! please, add here work with Uri

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private EcoMapDBHelper mOpenHelper;

    private static final int PROBLEMS = 100;
    private static final int RESOURCES = 103;
    private static final int PENDING_PROBLEMS = 102;
    private static final int REVISIONS = 104;

    private static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = EcoMapContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, EcoMapContract.PATH_PROBLEMS, PROBLEMS);
        matcher.addURI(authority, EcoMapContract.PATH_RESOURCES, RESOURCES);
        matcher.addURI(authority, EcoMapContract.PATH_PENDING_PROBLEMS, PENDING_PROBLEMS);
        matcher.addURI(authority, EcoMapContract.PATH_REVISIONS, REVISIONS);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new EcoMapDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "problems"
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
            case REVISIONS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        EcoMapContract.RevisionsEntry.TABLE_NAME,
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

        Context c = getContext();
        if (c != null) {
            ContentResolver cr = c.getContentResolver();
            retCursor.setNotificationUri(cr, uri);
        }

        return retCursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PROBLEMS:
                return EcoMapContract.ProblemsEntry.CONTENT_TYPE;
            case PENDING_PROBLEMS:
                return EcoMapContract.PendingProblemsEntry.CONTENT_TYPE;
            case RESOURCES:
                return EcoMapContract.ResourcesEntry.CONTENT_TYPE;
            case REVISIONS:
                return EcoMapContract.RevisionsEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }


    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case PROBLEMS: {
                long _id = db.insert(EcoMapContract.ProblemsEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = EcoMapContract.ProblemsEntry.buildProblemsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case PENDING_PROBLEMS: {
                long _id = db.insert(EcoMapContract.PendingProblemsEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = EcoMapContract.PendingProblemsEntry.buildPendingProblemsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case RESOURCES: {
                long _id = db.insert(EcoMapContract.ResourcesEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = EcoMapContract.ResourcesEntry.buildResourcesUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case REVISIONS: {
                long _id = db.insert(EcoMapContract.RevisionsEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = EcoMapContract.ResourcesEntry.buildResourcesUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        Context c = getContext();
        if (c != null) {
            ContentResolver cr = c.getContentResolver();
            cr.notifyChange(uri, null);
        }

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        switch (match) {
            case PROBLEMS:
                rowsDeleted = db.delete(
                        EcoMapContract.ProblemsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PENDING_PROBLEMS:
                rowsDeleted = db.delete(
                        EcoMapContract.PendingProblemsEntry.TABLE_NAME, selection, selectionArgs);
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

            Context c = getContext();
            if (c != null) {
                ContentResolver cr = c.getContentResolver();
                cr.notifyChange(uri, null);
            }

        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case PROBLEMS:
                rowsUpdated = db.update(EcoMapContract.ProblemsEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;

            case RESOURCES:
                rowsUpdated = db.update(EcoMapContract.ResourcesEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;

            case REVISIONS:
                rowsUpdated = db.update(EcoMapContract.RevisionsEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {

            Context c = getContext();
            if (c != null) {
                ContentResolver cr = c.getContentResolver();
                cr.notifyChange(uri, null);
            }

        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PROBLEMS:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        String str = String.valueOf(value.get(EcoMapContract.ProblemsEntry.COLUMN_PROBLEM_ID));
                        long _id = db.update(EcoMapContract.ProblemsEntry.TABLE_NAME, value, "problem_id = ?", new String[]{str});
                        //long _id = db.insert(EcoMapContract.ProblemsEntry.TABLE_NAME, null, value);

                        if (_id != -1 && _id != 0) {
                            returnCount++;
                        } else {
                            db.insert(EcoMapContract.ProblemsEntry.TABLE_NAME, null, value);
                        }

                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                Context c = getContext();
                if (c != null) {
                    ContentResolver cr = c.getContentResolver();
                    cr.notifyChange(uri, null);
                }

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
