package com.artf.ruacalendar.Notifications;

/**
 * Created by ART_F on 2017-02-11.
 */

public class NotificationObject {
    public String titleOfNotification;
    public String asEmail;
    public int timeValue;
    public int notificationType;


public NotificationObject(String asEmail, String titleOfNotification, int timeValue, int notificationType){
    this.asEmail = asEmail;
    this.titleOfNotification = titleOfNotification;
    this.timeValue = timeValue;
    this.notificationType = notificationType;
}

    @Override
    public String toString() {
        return titleOfNotification + asEmail;
    }
}
