package com.artf.ruacalendar.ViewAdapters;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.artf.ruacalendar.Event.EditEventFragment;
import com.artf.ruacalendar.MainUse;
import com.artf.ruacalendar.R;
import com.artf.ruacalendar.Views.CalendarProFragment;
import com.artf.ruacalendar.Views.EventRua;
import com.github.sundeepk.compactcalendarview.domain.Event;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by ART_F on 2017-01-21.
 */

public class AdapterCalendarPro extends ArrayAdapter<Event> {

    public long eventId;
    private int color;
    private int accessLevelCalendar;
    private String titleDialog, titleCalendar;
    private Long timeStartDialog;
    private Long timeEndDialog;
    private Long calendarId;
    private int allDay;
    private String duration, rrule;
    private int hourFormat;
    private Calendar startDialogCalendar = Calendar.getInstance();
    private Calendar endDialogCalendar = Calendar.getInstance();
    private DateFormat dateDialog = new SimpleDateFormat("EEE dd MMM  HH:mm - ");
    private DateFormat dateDialog2 = new SimpleDateFormat("EEE dd MMM  HH:mm");
    private DateFormat timeDialog = new SimpleDateFormat("HH:mm");
    private CalendarProFragment calendarProFragment;
    private View.OnClickListener showDialog = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            eventId = v.getId();
            final AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setView(R.layout.dialog_event)
                    .create();
            dialog.show();
            String[] projection = new String[]{Events._ID, Events.CALENDAR_ID, Events.TITLE, Events.DTSTART, Events.DTEND, Events.ALL_DAY, Events.ACCESS_LEVEL, Events.RRULE, Events.DURATION};
            String selection = "_id=?";
            String[] selectionArgs = new String[]{String.valueOf(eventId)};
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Cursor cursor = getContext().getContentResolver().query(Events.CONTENT_URI, projection, selection, selectionArgs, "dtstart DESC, dtend DESC");
            if (cursor.moveToFirst()) {

                calendarId = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.CALENDAR_ID));
                titleDialog = cursor.getString(cursor.getColumnIndex(Events.TITLE));
                timeStartDialog = cursor.getLong(cursor.getColumnIndex(Events.DTSTART));
                timeEndDialog = cursor.getLong(cursor.getColumnIndex(Events.DTEND));
                allDay = cursor.getInt(cursor.getColumnIndex(CalendarContract.Events.ALL_DAY));
                int accessLevel = cursor.getInt(cursor.getColumnIndex(Events.ACCESS_LEVEL));
                duration = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.DURATION));
                rrule = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.RRULE));
            }

            if (rrule != null) {
                int durationNum = Integer.parseInt(duration.replaceAll("[^\\d.]", ""));
                timeEndDialog = (durationNum * 1000) + timeStartDialog;
            }



            projection = new String[]{"_id", CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CalendarContract.Calendars.SYNC_EVENTS, CalendarContract.Calendars.CALENDAR_COLOR, CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL};
            selection = "_id=?";
            selectionArgs = new String[]{String.valueOf(calendarId)};
            cursor = getContext().getContentResolver().query(CalendarContract.Calendars.CONTENT_URI, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                color = cursor.getInt(cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_COLOR));
                titleCalendar = cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME));
                accessLevelCalendar = cursor.getInt(cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL));
                cursor.close();
            }
            TextView calendarTextView = (TextView) dialog.findViewById(R.id.titleCalendar);
            calendarTextView.setText(titleCalendar);


            startDialogCalendar.setTimeInMillis(timeStartDialog);
            endDialogCalendar.setTimeInMillis(timeEndDialog);
            if (allDay > 0) {
                setToMidnight(startDialogCalendar);
                setToMidnight(endDialogCalendar);
            }

            TextView titleEvent = (TextView) dialog.findViewById(R.id.titleEvent);
            titleEvent.setBackgroundColor(color);
            titleEvent.setText(titleDialog);

            TextView startTime = (TextView) dialog.findViewById(R.id.startTime);
            TextView endTime = (TextView) dialog.findViewById(R.id.endTime);

            if (hourFormat > 0) {
                startTime.setText(String.valueOf(dateDialog.format(startDialogCalendar.getTime())));
                endTime.setText(String.valueOf(timeDialog.format(endDialogCalendar.getTime())));
            } else {
                int time = startDialogCalendar.get(Calendar.HOUR_OF_DAY);
                if (time > 11) {
                    startDialogCalendar.add(Calendar.HOUR, -12);
                    startTime.setText(String.valueOf(dateDialog2.format(startDialogCalendar.getTime())) + " PM - ");
                } else {
                    startTime.setText(String.valueOf(dateDialog2.format(startDialogCalendar.getTime())) + " AM - ");
                }

                time = endDialogCalendar.get(Calendar.HOUR_OF_DAY);
                if (time > 11) {
                    endDialogCalendar.add(Calendar.HOUR, -12);
                    endTime.setText(String.valueOf(timeDialog.format(endDialogCalendar.getTime())) + " PM");
                } else {
                    endTime.setText(String.valueOf(timeDialog.format(endDialogCalendar.getTime())) + " AM");
                }
            }


            ImageView cancelEvent = (ImageView) dialog.findViewById(R.id.cancelEvent);
            cancelEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            ImageView goToEdit = (ImageView) dialog.findViewById(R.id.goToEditEvent);
            goToEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putLong("Event_id", eventId);
                    Fragment fragment = new EditEventFragment();
                    FragmentTransaction ft = ((MainUse) getContext()).getSupportFragmentManager().beginTransaction();
                    fragment.setArguments(bundle);
                    ft.replace(R.id.content_frame, fragment, "visible_fragment");
                    ft.addToBackStack(null);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    ft.commit();
                    dialog.dismiss();
                }
            });
            ImageView deleteEvent = (ImageView) dialog.findViewById(R.id.deleteEvent);
            deleteEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContentResolver cr = getContext().getContentResolver();
                    ContentValues values = new ContentValues();
                    values.put(CalendarContract.Events.DELETED, 1);
                    Uri deleteUri = null;
                    deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
                    cr.update(deleteUri, values, null, null);
                    cr.delete(deleteUri, null, null);
                    calendarProFragment.deleteEventCompactCalendar(eventId);
                    ((MainUse) getContext()).turnOff(eventId);
                    dialog.dismiss();

                }
            });
            if (accessLevelCalendar != 700) {
                goToEdit.setVisibility(View.INVISIBLE);
                deleteEvent.setVisibility(View.INVISIBLE);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) cancelEvent.getLayoutParams();
                params.removeRule(RelativeLayout.ALIGN_PARENT_START);
                params.addRule(RelativeLayout.ALIGN_PARENT_END);
                cancelEvent.setLayoutParams(params);
            }


            dialog.show();


        }
    };
    private ArrayList<Event> objects;


    public AdapterCalendarPro(Context context, ArrayList<Event> objects, CalendarProFragment f, int hourFormat) {
        super(context, R.layout.fragment_calendarpro_part, objects);
        this.objects = objects;
        this.hourFormat = hourFormat;
        this.calendarProFragment = f;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.fragment_calendarpro_part, null);
        }
        TextView timeStartTV = (TextView) v.findViewById(R.id.timeStartText);
        TextView timeEndTV = (TextView) v.findViewById(R.id.timeEndText);
        TextView titleTextView = (TextView) v.findViewById(R.id.nameTextCalendar);
        LinearLayout lineColor = (LinearLayout) v.findViewById(R.id.mainLine);
        RelativeLayout timeColor = (RelativeLayout) v.findViewById(R.id.rowView2);

        DateFormat time = new SimpleDateFormat("HH:mm");
        Calendar calendar = Calendar.getInstance();
        Event i = objects.get(position);
        i.getData().getClass();
        EventRua eventRua = (EventRua) i.getData();
        int allDay = eventRua.isAllday;
        if (hourFormat > 0) {
            if (allDay > 0) {
                calendar.setTimeInMillis(eventRua.timeStart);
                setToMidnight(calendar);
                timeStartTV.setText(String.valueOf(time.format(calendar.getTime())));
                timeEndTV.setText(String.valueOf(time.format(calendar.getTime())));
            } else {
                calendar.setTimeInMillis(eventRua.timeStart);
                timeStartTV.setText(String.valueOf(time.format(calendar.getTime())));

                calendar.setTimeInMillis(eventRua.timeEnd);
                timeEndTV.setText(String.valueOf(time.format(calendar.getTime())));
            }
        } else {
            if (allDay > 0) {
                calendar.setTimeInMillis(eventRua.timeStart);
                setToMidnight(calendar);
                timeStartTV.setText(String.valueOf(time.format(calendar.getTime())) + " AM");
                timeEndTV.setText(String.valueOf(time.format(calendar.getTime())) + " AM");
            } else {
                calendar.setTimeInMillis(eventRua.timeStart);
                int timeFormat = calendar.get(Calendar.HOUR_OF_DAY);
                if (timeFormat > 11) {
                    calendar.add(Calendar.HOUR, -12);
                    timeStartTV.setText(String.valueOf(time.format(calendar.getTime())) + " PM");
                } else {
                    timeStartTV.setText(String.valueOf(time.format(calendar.getTime())) + " AM");
                }
                calendar.setTimeInMillis(eventRua.timeStart);
                timeFormat = calendar.get(Calendar.HOUR_OF_DAY);
                if (timeFormat > 11) {
                    calendar.add(Calendar.HOUR, -12);
                    timeEndTV.setText(String.valueOf(time.format(calendar.getTime())) + " PM");
                } else {
                    timeEndTV.setText(String.valueOf(time.format(calendar.getTime())) + " AM");
                }
            }
        }
        titleTextView.setText(eventRua.title);

        GradientDrawable rowLinearColor = (GradientDrawable) lineColor.getBackground();
        rowLinearColor.setColor(i.getColor());
        GradientDrawable timeColorLayout = (GradientDrawable) timeColor.getBackground();
        timeColorLayout.setColor(i.getColor());
        v.setOnClickListener(showDialog);

        long a = eventRua.eventId;
        v.setId((int) a);

        return v;

    }

    private void setToMidnight(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

}
