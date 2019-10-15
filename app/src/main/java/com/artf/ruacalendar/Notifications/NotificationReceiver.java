package com.artf.ruacalendar.Notifications;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.artf.ruacalendar.Notifications.NotificationService;

public class NotificationReceiver extends WakefulBroadcastReceiver {

    public NotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String date = intent.getStringExtra("date");
        String notification_id = intent.getStringExtra("id");
        int hoursFormat = intent.getIntExtra("hoursFormat", 0);
        Intent notification = new Intent(context, NotificationService.class);
        notification.putExtra("title", title);
        notification.putExtra("date", date);
        notification.putExtra("id", notification_id);
        notification.putExtra("hoursFormat", hoursFormat);
        context.startService(notification);
    }
}

