package com.artf.ruacalendar.Event;

/**
 * Created by ART_F on 2017-01-17.
 */

class MyCalendar {

    public String name;
    public String id;

    MyCalendar(String _name, String _id) {
        name = _name;
        id = _id;
    }

    @Override
    public String toString() {
        return name;
    }
}

