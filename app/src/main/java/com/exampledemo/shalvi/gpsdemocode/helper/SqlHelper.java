package com.exampledemo.shalvi.gpsdemocode.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.exampledemo.shalvi.gpsdemocode.model.TrckerData;

/**
 * Created by Windows on 2/24/2018.
 */

public class SqlHelper extends SQLiteOpenHelper {
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "trackerDetails";

    // Contacts table name
    private static final String TABLE_NAME = "Tracking_Details";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_LATITUDE= "latitude";
    private static final String KEY_LONGITUTDE = "longitude";
    private static final String KEY_TIME = "time";
    private static final String KEY_SPEED="speed";

    public SqlHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public SqlHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_BRAND_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_LATITUDE + " DOUBLE,"
                + KEY_LONGITUTDE + " DOUBLE," + KEY_TIME + " TIME,"+KEY_SPEED+",INT" + ")";
        db.execSQL(CREATE_BRAND_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void addTrackingData(TrckerData trackData) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_ID, trackData.getVechile_id()); // Vechile Id

        values.put(KEY_LONGITUTDE,trackData.getLatiutude()); // Latitude

        values.put(KEY_LONGITUTDE,trackData.getLongitude());//Longitutde
        values.put(KEY_TIME,trackData.getTime());
        values.put(KEY_SPEED, trackData.getSpeed());//Speed
        // Inserting Row
        db.insert(TABLE_NAME, null, values);
        db.close(); // Closing database connection

    }


}
