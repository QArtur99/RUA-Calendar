package com.artf.ruacalendar.Database;

import android.provider.BaseColumns;

/**
 * Created by ART_F on 2017-03-14.
 */

public final class MainDatabaseContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private MainDatabaseContract() {
    }

    /* Inner class that defines the table contents */
    public static class Settings implements BaseColumns {

        public static final String TABLE_NAME = "SETTINGS";
        public static final String START_LOAD = "startLoad";
        public static final String START_VIEW_ID = "startViewId";
        public static final String START_VIEW = "startView";
        public static final String HOURS_FORMAT = "hoursFormat";
        public static final String NOTIFICATIONS = "notifications";
        public static final String START_DAY_ID = "startDayId";
        public static final String START_DAY = "startDay";

        static final String SQL_CREATE_SETTINGS =
                "CREATE TABLE " + TABLE_NAME + " ("
                        + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + START_LOAD + " TEXT,"
                        + START_VIEW_ID + " TEXT,"
                        + START_VIEW + " TEXT,"
                        + HOURS_FORMAT + " TEXT,"
                        + NOTIFICATIONS + " TEXT,"
                        + START_DAY_ID + " TEXT,"
                        + START_DAY + " TEXT"
                        + ");";

        static final String SQL_DELETE_SETTINGS =
                "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static final String[] projection = {
                _ID,
                START_LOAD,
                START_VIEW_ID,
                START_VIEW,
                HOURS_FORMAT,
                NOTIFICATIONS,
                START_DAY_ID,
                START_DAY
        };

    }

    public static class Notes implements BaseColumns {
        public static final String TABLE_NAME = "NOTEBOOK";
        public static final String NOTE = "note";
        public static final String TIME_IN_MILLIS = "timeInMillis";


        static final String SQL_CREATE_NOTEBOOK =
                "CREATE TABLE " + TABLE_NAME + " ("
                        + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + NOTE + " TEXT,"
                        + TIME_IN_MILLIS + " TEXT"
                        + ");";

        static final String SQL_DELETE_NOTEBOOK =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

}
