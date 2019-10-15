package com.artf.ruacalendar.Views;

/**
 * Created by ART_F on 2017-03-11.
 */

public class SearchObject {

    public long eventId;
    public String title;

    SearchObject(long eventId, String title) {
        this.title = title;
        this.eventId = eventId;
    }

    @Override
    public String toString() {
        return title;
    }
}