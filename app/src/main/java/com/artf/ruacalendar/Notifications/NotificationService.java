package com.artf.ruacalendar.Notifications;

/**
 * Created by ART_F on 2016-12-13.
 */

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.artf.ruacalendar.MainUse;
import com.artf.ruacalendar.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class NotificationService extends IntentService {
    public NotificationService() {
        super("NotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM, HH:mm");
        Calendar dateCalendar = Calendar.getInstance();

        String title = intent.getStringExtra("title");
        String date = intent.getStringExtra("date");
        String  notification_id = intent.getStringExtra("id");
        int hoursFormat = intent.getIntExtra("hoursFormat", 0);
        dateCalendar.setTimeInMillis(Long.parseLong(date));

        String time;
        if (hoursFormat > 0) {
            time = String.valueOf(dateFormat.format(dateCalendar.getTime()));
        } else {
            int hourOfDay = dateCalendar.get(Calendar.HOUR_OF_DAY);
            if (hourOfDay > 11) {
                dateCalendar.add(Calendar.HOUR, -12);
                time = String.valueOf(dateFormat.format(dateCalendar.getTime())) + " PM";
            } else {
                time = String.valueOf(dateFormat.format(dateCalendar.getTime())) + " AM";
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            showText(title,notification_id, time);
        } else {
            showText2(title,notification_id, time);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void showText(final String title, String notification_id, String time) {
        Intent intent = new Intent(this, MainUse.class);
        intent.putExtra("Event_id", notification_id);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainUse.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setShowWhen(false)
                .setSmallIcon(R.drawable.ic_date_range_white_24dp)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(pendingIntent)
                .setContentText(time)
                .build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }

    private void showText2(final String title,String notification_id, String time) {
        Intent intent = new Intent(this, MainUse.class);
        intent.putExtra("Event_id", notification_id);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainUse.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(pendingIntent)
                .setContentText(time)
                .build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }
}