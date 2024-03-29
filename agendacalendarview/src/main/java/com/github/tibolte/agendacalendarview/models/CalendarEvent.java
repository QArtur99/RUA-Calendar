package com.github.tibolte.agendacalendarview.models;

import java.io.Serializable;
import java.util.Calendar;

public interface CalendarEvent extends Serializable {


    void setPlaceholder(boolean placeholder);

    boolean isPlaceholder();

    public String getLocation();

    public void setLocation(String mLocation);

    long getId();

    void setId(long mId);

    Calendar getStartTime();

    void setStartTime(Calendar mStartTime);

    Calendar getEndTime();

    void setEndTime(Calendar mEndTime);

    String getTitle();

    void setTitle(String mTitle);

    Calendar getInstanceDay();

    void setInstanceDay(Calendar mInstanceDay);

    IDayItem getDayReference();

    void setDayReference(IDayItem mDayReference);

    IWeekItem getWeekReference();

    void setWeekReference(IWeekItem mWeekReference);

    CalendarEvent copy();
}
