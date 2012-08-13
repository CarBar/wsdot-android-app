/*
 * Copyright (c) 2012 Washington State Department of Transportation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package gov.wa.wsdot.android.wsdot.provider;

import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.Cameras;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.HighwayAlerts;
import gov.wa.wsdot.android.wsdot.provider.WSDOTDatabase.Tables;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

public class WSDOTProvider extends ContentProvider {

	private static final String DEBUG_TAG = "WSDOTProvider";
	private WSDOTDatabase mDb;
	
    private static final UriMatcher sUriMatcher = buildUriMatcher();	

    private static final int CAMERAS = 100;
    private static final int CAMERAS_ID = 101;
    
    private static final int HIGHWAY_ALERTS = 200;
    private static final int HIGHWAY_ALERTS_ID = 201;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WSDOTContract.CONTENT_AUTHORITY;
        
        matcher.addURI(authority, "cameras", CAMERAS);
        matcher.addURI(authority, "cameras/#", CAMERAS_ID);
        matcher.addURI(authority, "highway_alerts", HIGHWAY_ALERTS);
        matcher.addURI(authority, "highway_alerts/#", HIGHWAY_ALERTS_ID);
        
        return matcher;
	}

	@Override
	public boolean onCreate() {
		mDb = new WSDOTDatabase(getContext());
		return true;
	}
	
	@Override
	public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
        case CAMERAS:
            return Cameras.CONTENT_TYPE;
        case CAMERAS_ID:
            return Cameras.CONTENT_ITEM_TYPE;
        case HIGHWAY_ALERTS:
            return HighwayAlerts.CONTENT_TYPE;
        case HIGHWAY_ALERTS_ID:
            return HighwayAlerts.CONTENT_ITEM_TYPE;
        default:
        	throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs,	String sortOrder) {
		
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
    	
    	final int match = sUriMatcher.match(uri);
    	
    	switch (match) {
	    case CAMERAS:
	    	queryBuilder.setTables(WSDOTDatabase.Tables.CAMERAS);
	    	// no filter
	        break;
	    case CAMERAS_ID:
	    	queryBuilder.setTables(WSDOTDatabase.Tables.CAMERAS);
	    	queryBuilder.appendWhere(BaseColumns._ID + "=" + uri.getLastPathSegment());
	        break;
	    case HIGHWAY_ALERTS:
	    	queryBuilder.setTables(WSDOTDatabase.Tables.HIGHWAY_ALERTS);
	    	// no filter
	        break;
	    case HIGHWAY_ALERTS_ID:
	    	queryBuilder.setTables(WSDOTDatabase.Tables.HIGHWAY_ALERTS);
	    	queryBuilder.appendWhere(BaseColumns._ID + "=" + uri.getLastPathSegment());
	        break;	        
	    default:
	    	throw new IllegalArgumentException("Unknown URI " + uri);
	    }
	 
	    Cursor cursor = queryBuilder.query(mDb.getReadableDatabase(),
	            projection, selection, selectionArgs, null, null, sortOrder);
	    cursor.setNotificationUri(getContext().getContentResolver(), uri);
	    
	    return cursor;		
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sUriMatcher.match(uri);
        SQLiteDatabase sqlDB = mDb.getWritableDatabase();
        int rowsAffected = 0;
        String id;
        
        switch (uriType) {
        case CAMERAS:
            rowsAffected = sqlDB.delete(Tables.CAMERAS, selection, selectionArgs);
            break;
        case CAMERAS_ID:
            id = uri.getLastPathSegment();
            if (TextUtils.isEmpty(selection)) {
                rowsAffected = sqlDB.delete(Tables.CAMERAS, BaseColumns._ID + "=" + id, null);
            } else {
                rowsAffected = sqlDB.delete(Tables.CAMERAS,
                        selection + " and " + BaseColumns._ID + "=" + id,
                        selectionArgs);
            }
            break;
        case HIGHWAY_ALERTS:
            rowsAffected = sqlDB.delete(Tables.HIGHWAY_ALERTS, selection, selectionArgs);
            break;
        case HIGHWAY_ALERTS_ID:
            id = uri.getLastPathSegment();
            if (TextUtils.isEmpty(selection)) {
                rowsAffected = sqlDB.delete(Tables.HIGHWAY_ALERTS, BaseColumns._ID + "=" + id, null);
            } else {
                rowsAffected = sqlDB.delete(Tables.HIGHWAY_ALERTS,
                        selection + " and " + BaseColumns._ID + "=" + id,
                        selectionArgs);
            }
            break;            
        default:
            throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsAffected;
	}

	
	
	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
        int uriType = sUriMatcher.match(uri);
        SQLiteDatabase sqlDB = mDb.getWritableDatabase();
        int rowsAdded = 0;
        
        switch(uriType) {
        case CAMERAS:
        	sqlDB.beginTransaction();
            try {
            	for (ContentValues value : values) {
	                sqlDB.insert(Tables.CAMERAS, null, value);
            	}
            	sqlDB.setTransactionSuccessful();
            	rowsAdded = values.length;
            } finally {
                getContext().getContentResolver().notifyChange(uri, null);
                sqlDB.endTransaction();
            }
            
            return rowsAdded;
        case HIGHWAY_ALERTS:
        	sqlDB.beginTransaction();
            try {
            	for (ContentValues value : values) {
	                sqlDB.insert(Tables.HIGHWAY_ALERTS, null, value);
            	}
            	sqlDB.setTransactionSuccessful();
            	rowsAdded = values.length;
            } finally {
                getContext().getContentResolver().notifyChange(uri, null);
                sqlDB.endTransaction();
            }
            
            return rowsAdded;
        default:
    		throw new UnsupportedOperationException("Unknown uri: " + uri);
    	}

	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
        int uriType = sUriMatcher.match(uri);
        SQLiteDatabase sqlDB = mDb.getWritableDatabase();
        
        switch(uriType) {
        case CAMERAS:
            try {
                long rowId = sqlDB.insertOrThrow(Tables.CAMERAS, null, values);
                if (rowId > 0) {
                    Uri newUri = ContentUris.withAppendedId(uri, rowId);
                    getContext().getContentResolver().notifyChange(uri, null);
                    return newUri;
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
            } catch (SQLiteConstraintException e) {
                Log.i(DEBUG_TAG, "Ignoring constraint failure.");
            }
        case HIGHWAY_ALERTS:
            try {
                long rowId = sqlDB.insertOrThrow(Tables.HIGHWAY_ALERTS, null, values);
                if (rowId > 0) {
                    Uri newUri = ContentUris.withAppendedId(uri, rowId);
                    getContext().getContentResolver().notifyChange(uri, null);
                    return newUri;
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
            } catch (SQLiteConstraintException e) {
                Log.i(DEBUG_TAG, "Ignoring constraint failure.");
            }
    	default:
    		throw new UnsupportedOperationException("Unknown uri: " + uri);
    	}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
