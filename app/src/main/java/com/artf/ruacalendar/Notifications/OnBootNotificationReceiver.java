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
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.widget.Toast;

import com.artf.ruacalendar.Database.MainDatabaseContract;
import com.artf.ruacalendar.Database.MainDatabaseHelper;

import java.util.Calendar;

import me.everything.providers.android.calendar.CalendarProvider;
import me.everything.providers.android.calendar.Event;
import me.everything.providers.android.calendar.Reminder;
import me.everything.providers.core.Data;

//import java.util.Calendar;

/**
 * Created by ART_F on 2017-01-08.
 */
public class OnBootNotificationReceiver extends WakefulBroadcastReceiver {
    public static final String NOTIFICATIONS_TURN_ON = "com.artf.ruacalendar.notificationsTurnOn";
    public static final String NOTIFICATIONS_TURN_OFF = "com.artf.ruacalendar.notificationsTurnOff";
    private static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    private Context context;
    private Intent intent;
    private int startHoursFormat, notificationState;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        this.intent = intent;
        doTask();
    }

    public void doTask() {
        new Runnable() {
            @Override
            public void run() {
                loadData(context);
                if (notificationState > 0) {
                    if (BOOT_COMPLETED.equals(intent.getAction()) || NOTIFICATIONS_TURN_ON.equals(intent.getAction())) {
                        setNotification(context);
                    }
                }
                if (notificationState == 0) {
                    if (NOTIFICATIONS_TURN_OFF.equals(intent.getAction())) {
                        turnoff(context);
                    }
                }
            }
        };
    }

    private void setNotification(Context context) {
        Calendar calendar = Calendar.getInstance();

        String[] projection = new String[]{Events._ID, Events.TITLE, Events.DTSTART, Events.DTEND};
        String selection = Events.DTSTART + " >=?";
        String[] selectionArgs = new String[]{"" + calendar.getTimeInMillis()};
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
        Intent intent = new Intent(context, NotificationReceiver.class);
        Cursor cursor = context.getContentResolver().query(Events.CONTENT_URI, projection, selection, selectionArgs, "dtstart DESC, dtend DESC");
        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndex(Events._ID));
                String title = cursor.getString(cursor.getColumnIndex(Events.TITLE));
                String timeStart = cursor.getString(cursor.getColumnIndex(Events.DTSTART));
                intent.putExtra("title", title);
                intent.putExtra("id", id);
                intent.putExtra("date", timeStart);
                intent.putExtra("hoursFormat", startHoursFormat);


                String[] projection2 = new String[]{Reminders._ID, Reminders.EVENT_ID, Reminders.MINUTES, Reminders.METHOD};
                String selection2 = Reminders.EVENT_ID + "=?";
                String[] selectionArgs2 = new String[]{"" + id};
                Cursor cursor2 = context.getContentResolver().query(Reminders.CONTENT_URI, projection2, selection2, selectionArgs2, null);
                if (cursor2.moveToFirst()) {
                    do {
                        int minutes = cursor2.getInt(cursor2.getColumnIndex(Reminders.MINUTES));
                        String reminderId = cursor2.getString(cursor2.getColumnIndex(Reminders._ID));
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, Integer.parseInt(reminderId), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        alarmManager.set(AlarmManager.RTC_WAKEUP, (Long.parseLong(timeStart) - minutes * 60000), pendingIntent);
                    } while (cursor2.moveToNext());
                }
                cursor2.close();
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void loadData(Context mContext) {
        Cursor cursor = null;
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
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void turnoff(Context context) {
        CalendarProvider provider = new CalendarProvider(context);
        Data<me.everything.providers.android.calendar.Calendar> calendars = provider.getCalendars();
        for (me.everything.providers.android.calendar.Calendar calendar : calendars.getList()) {
            Data<Event> events = provider.getEvents(calendar.id);
            for (Event event1 : events.getList()) {
                Data<Reminder> reminders = provider.getReminders(event1.id);
                for (Reminder reminders1 : reminders.getList()) {
                    turnOffNotification(context, reminders1.id);
                }
            }
        }
    }

    public void turnOffNotification(Context context, long id) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) id, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}