package com.artf.ruacalendar.Views;

import com.github.tibolte.agendacalendarview.models.BaseCalendarEvent;
import com.github.tibolte.agendacalendarview.models.CalendarEvent;

import java.util.Calendar;

/**
 * Created by ART_F on 2017-01-28.
 */

public class EventSchedule extends BaseCalendarEvent {
    private int mDrawableId;

    // region Constructors

    public EventSchedule(long id, int color, String title, String description, String location, long dateStart, long dateEnd, int allDay, String duration, int drawableId, int hoursFormat) {
        super(id, color, title, description, location, dateStart, dateEnd, allDay, duration, hoursFormat);
        this.mDrawableId = drawableId;
    }

    public EventSchedule(String title, String description, String location, int color, Calendar startTime, Calendar endTime, boolean allDay, int drawableId) {
        super(title, description, location, color, startTime, endTime, allDay);
        this.mDrawableId = drawableId;
    }

    private EventSchedule(EventSchedule calendarEvent) {
        super(calendarEvent);
        this.mDrawableId = calendarEvent.getDrawableId();
    }


    private int getDrawableId() {
        return mDrawableId;
    }

    public void setDrawableId(int drawableId) {
        this.mDrawableId = drawableId;
    }


    @Override
    public CalendarEvent copy() {
        return new EventSchedule(this);
    }

}
