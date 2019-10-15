package com.artf.ruacalendar.Settings;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Calendars;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Switch;

import com.artf.ruacalendar.R;

/**
 * Created by ART_F on 2017-01-20.
 */

class AdapterSync extends CursorAdapter {
    private long calendarId;


    AdapterSync(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.fragment_settings_sync_part, parent, false);

    }

    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {

        final Switch syncSwitch = (Switch) view.findViewById(R.id.syncPart);

        String calendar = cursor.getString(cursor.getColumnIndexOrThrow(Calendars.CALENDAR_DISPLAY_NAME));
        syncSwitch.setText(calendar);
        int sync = cursor.getInt(cursor.getColumnIndexOrThrow(Calendars.SYNC_EVENTS));
        if (sync == 1) {
            syncSwitch.setChecked(true);
        }
        if (sync == 0) {
            syncSwitch.setChecked(false);
        }

        long id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
        view.setId((int) id);
        syncSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarId = view.getId();
                if (syncSwitch.isChecked()) {
                    ContentValues values = new ContentValues();
                    values.put(Calendars.SYNC_EVENTS, 1);
                    Uri updateUri = ContentUris.withAppendedId(Calendars.CONTENT_URI, calendarId);
                    int rows = context.getContentResolver().update(updateUri, values, null, null);
                } else {
                    ContentValues values = new ContentValues();
                    values.put(Calendars.SYNC_EVENTS, 0);
                    Uri updateUri = ContentUris.withAppendedId(Calendars.CONTENT_URI, calendarId);
                    int rows = context.getContentResolver().update(updateUri, values, null, null);
                }
            }
        });
    }
}
