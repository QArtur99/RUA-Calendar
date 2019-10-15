package com.artf.ruacalendar.Views;

import java.io.Serializable;

/**
 * Created by ART_F on 2017-01-21.
 */

public class EventRua implements Serializable {
    public String title;
    Long calendarId;
    public Long eventId;
    public Long timeStart;
    public Long timeEnd;
    public int isAllday;

    public EventRua(Long calendarId, Long eventId, String title, Long timeStart, Long timeEnd, int isAllday)
    {
        this.title = title;
        this.calendarId = calendarId;
        this.eventId = eventId;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.isAllday = isAllday;
    }

    @Override
    public String toString() {
        return title;
    }
}