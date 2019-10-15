package com.artf.ruacalendar.Notifications;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.CalendarAlerts;
import android.provider.CalendarContract.Reminders;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.widget.Toast;

import com.artf.ruacalendar.Database.MainDatabaseContract;
import com.artf.ruacalendar.Database.MainDatabaseHelper;
import com.artf.ruacalendar.Notifications.NotificationReceiver;

import me.everything.providers.android.calendar.CalendarProvider;
import me.everything.providers.android.calendar.Event;

/**
 * Created by ART_F on 2017-02-13.
 */

public class CalendarChangedReceiver extends WakefulBroadcastReceiver {
    public static final String TAG = "com.artf.ruacalendar.editNotifications";
    private static final String EVENT_REMINDER_APP_ACTION = "android.intent.action.EVENT_REMINDER";
    private int startHoursFormat, notificationState;
    private Cursor cursor;

    @Override
    public void onReceive(Context context, Intent intent) {
        loadData(context);
        if (notificationState > 0) {
            if (TAG.equals(intent.getAction())) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    if (extras.containsKey("Event_id")) {
                        String eventId = extras.getString("Event_id");
                        turnOnNotification(context, eventId);
                    }
                }
            } else {
                if (EVENT_REMINDER_APP_ACTION.equals(intent.getAction())) {

                    Uri uri = intent.getData();
                    String alertTime = uri.getLastPathSegment();
                    String[] projection = new String[]{CalendarAlerts.EVENT_ID};
                    String selection = CalendarAlerts.ALARM_TIME + "=?";
                    String[] selectionArgs = new String[]{alertTime};
                    Cursor cursor = context.getContentResolver().query(CalendarAlerts.CONTENT_URI_BY_INSTANCE, projection, selection, selectionArgs, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        String eventId = cursor.getString(cursor.getColumnIndex(CalendarAlerts.EVENT_ID));
                        turnOnNotification(context, eventId);
                        cursor.close();
                    }
                }
            }
        }
    }

    private void turnOnNotification(Context context, String id) {
        CalendarProvider provider = new CalendarProvider(context);

        Event event = provider.getEvent(Long.parseLong(id));
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("title", event.title);
        intent.putExtra("id", id);
        intent.putExtra("date", String.valueOf(event.dTStart));
        intent.putExtra("hoursFormat", startHoursFormat);


        String[] projection = new String[]{Reminders._ID, Reminders.MINUTES, Reminders.METHOD, Reminders.EVENT_ID};
        String selection = Reminders.EVENT_ID + "=?";
        String[] selectionArgs = new String[]{id};
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        cursor = context.getContentResolver().query(Reminders.CONTENT_URI, projection, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                long reminderId = cursor.getLong(cursor.getColumnIndexOrThrow(Reminders._ID));
                int minutes = cursor.getInt(cursor.getColumnIndexOrThrow(Reminders.MINUTES));
                int methodValue = cursor.getInt(cursor.getColumnIndexOrThrow(Reminders.METHOD));

                if (methodValue == 2) {
                    //empty
                } else {
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) reminderId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC_WAKEUP, (event.dTStart - (minutes * 60000)), pendingIntent);
                }
            } while (cursor.moveToNext());
        }
    }

    private void loadData(Context mContext) {
        try {
            SQLiteOpenHelper mainDatabase = new MainDatabaseHelper(mContext);
            SQLiteDatabase db = mainDatabase.getReadableDatabase();
            cursor = db.query(MainDatabaseContract.Settings.TABLE_NAME, MainDatabaseContract.Settings.projection, null, null, null, null, null);

            if (cursor.moveToFirst()) {
                startHoursFormat = cursor.getInt(cursor.getColumnIndexOrThrow(MainDatabaseContract.Settings.HOURS_FORMAT));
                notificationState = cursor.getInt(cursor.getColumnIndexOrThrow(MainDatabaseContract.Settings.NOTIFICATIONS));
            }

        } catch (SQLiteException e) {
            Toast.makeText(mContext, "Database unavailable", Toast.LENGTH_SHORT).show();
        }finally {
            if(cursor !=null) {
                cursor.close();
            }
        }
    }

}