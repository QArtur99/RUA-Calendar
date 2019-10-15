package com.artf.ruacalendar.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.artf.ruacalendar.Database.MainDatabaseContract;

/**
 * Created by ART_F on 2016-05-21.
 */
public class MainDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "RUAcalendar.db"; // the name of our database
    private static final int DB_VERSION = 1; // the version of the database

    public MainDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(MainDatabaseContract.Settings.SQL_CREATE_SETTINGS);
        db.execSQL(MainDatabaseContract.Notes.SQL_CREATE_NOTEBOOK);
        setSettings(db, 11, 2131689661, 1, 0, 1, 2131689653, 1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(MainDatabaseContract.Settings.SQL_DELETE_SETTINGS);
        db.execSQL(MainDatabaseContract.Notes.SQL_DELETE_NOTEBOOK);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    private void setSettings(SQLiteDatabase db, int startLoad, int startViewId, int startView, int hoursFormat, int notifications,int startDayId, int startDay) {
        ContentValues mainValues = new ContentValues();
        mainValues.put(MainDatabaseContract.Settings.START_LOAD, startLoad);
        mainValues.put(MainDatabaseContract.Settings.START_VIEW_ID, startViewId);
        mainValues.put(MainDatabaseContract.Settings.START_VIEW, startView);
        mainValues.put(MainDatabaseContract.Settings.HOURS_FORMAT, hoursFormat);
        mainValues.put(MainDatabaseContract.Settings.NOTIFICATIONS, notifications);
        mainValues.put(MainDatabaseContract.Settings.START_DAY_ID, startDayId);
        mainValues.put(MainDatabaseContract.Settings.START_DAY, startDay);
        db.insert(MainDatabaseContract.Settings.TABLE_NAME, null, mainValues);
    }

}
