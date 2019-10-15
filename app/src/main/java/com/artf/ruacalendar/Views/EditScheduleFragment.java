package com.artf.ruacalendar.Views;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.artf.ruacalendar.AsyncTaskLoader;
import com.artf.ruacalendar.Event.EditEventFragment;
import com.artf.ruacalendar.Database.MainDatabaseContract;
import com.artf.ruacalendar.Database.MainDatabaseHelper;
import com.artf.ruacalendar.MainUse;
import com.artf.ruacalendar.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by ART_F on 2017-01-31.
 */

public class EditScheduleFragment extends Fragment {

    View.OnClickListener cancelOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Fragment fragment = new ScheduleFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment, "visible_fragment");
            ft.addToBackStack(null);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
        }
    };

    private int startHoursFormat;
    private int color;
    private String titleDialog, titleCalendar;
    private Long timeStartDialog;
    private Long timeEndDialog;
    private Long calendarId;
    private int allDay;
    private Calendar startDialogCalendar = Calendar.getInstance();
    private Calendar endDialogCalendar = Calendar.getInstance();
    private DateFormat dateDialog = new SimpleDateFormat("EEE dd MMM  HH:mm - ");
    private DateFormat dateDialog2 = new SimpleDateFormat("EEE dd MMM  HH:mm");
    private DateFormat timeDialog = new SimpleDateFormat("HH:mm");
    private int accessLevelCalendar;
    private long eventId;
    View.OnClickListener editOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Bundle bundle = new Bundle();
            bundle.putLong("Event_id", eventId);
            Fragment fragment = new EditEventFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            fragment.setArguments(bundle);
            ft.replace(R.id.content_frame, fragment, "visible_fragment");
            ft.addToBackStack(null);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
        }
    };

    private String duration, rrule;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_schedule_event_view, container, false);
        setHasOptionsMenu(true);
        loadData();

        Bundle bundle = this.getArguments();
        eventId = bundle.getLong("Event_id", 0);

        try {

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
                return rootView;
            }
            Cursor cursor2 = getActivity().getContentResolver().query(Events.CONTENT_URI, projection, selection, selectionArgs, "dtstart DESC, dtend DESC");
            if (cursor2 != null && cursor2.moveToFirst()) {

                calendarId = cursor2.getLong(cursor2.getColumnIndex(Events.CALENDAR_ID));
                titleDialog = cursor2.getString(cursor2.getColumnIndex(Events.TITLE));
                timeStartDialog = cursor2.getLong(cursor2.getColumnIndex(Events.DTSTART));
                timeEndDialog = cursor2.getLong(cursor2.getColumnIndex(Events.DTEND));
                allDay = cursor2.getInt(cursor2.getColumnIndex(Events.ALL_DAY));
                duration = cursor2.getString(cursor2.getColumnIndex(CalendarContract.Events.DURATION));
                rrule = cursor2.getString(cursor2.getColumnIndex(CalendarContract.Events.RRULE));
            }

            if (rrule != null) {
                int durationNum = Integer.parseInt(duration.replaceAll("[^\\d.]", ""));
                timeEndDialog = (durationNum * 1000) + timeStartDialog;
            }

            projection = new String[]{"_id", Calendars.CALENDAR_DISPLAY_NAME, Calendars.SYNC_EVENTS, Calendars.CALENDAR_COLOR, Calendars.CALENDAR_ACCESS_LEVEL};
            selection = "_id=?";
            selectionArgs = new String[]{String.valueOf(calendarId)};
            cursor2 = getActivity().getContentResolver().query(Calendars.CONTENT_URI, projection, selection, selectionArgs, null);
            if (cursor2 != null && cursor2.moveToFirst()) {
                color = cursor2.getInt(cursor2.getColumnIndexOrThrow(Calendars.CALENDAR_COLOR));
                titleCalendar = cursor2.getString(cursor2.getColumnIndex(Calendars.CALENDAR_DISPLAY_NAME));
                accessLevelCalendar = cursor2.getInt(cursor2.getColumnIndexOrThrow(Calendars.CALENDAR_ACCESS_LEVEL));
                cursor2.close();
            }
            LinearLayout rowTitle = (LinearLayout) rootView.findViewById(R.id.rowTitleEvent);
            rowTitle.setBackgroundColor(color);
            TextView calendarTextView = (TextView) rootView.findViewById(R.id.titleCalendar);
            calendarTextView.setText(titleCalendar);


            startDialogCalendar.setTimeInMillis(timeStartDialog);
            endDialogCalendar.setTimeInMillis(timeEndDialog);
            if (allDay > 0) {
                setToMidnight(startDialogCalendar);
                setToMidnight(endDialogCalendar);
            }

            ImageView arrow = (ImageView) getActivity().findViewById(R.id.arrowTittle);
            TextView titleToolbar = (TextView) rootView.findViewById(R.id.titleEvent);
            arrow.setBackgroundResource(R.color.colorPrimary);
            titleToolbar.setText(titleDialog);

            TextView startTime = (TextView) rootView.findViewById(R.id.startTime);
            TextView endTime = (TextView) rootView.findViewById(R.id.endTime);

            if (startHoursFormat > 0) {
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


        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(getActivity(), "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }


        TextView saveNewNote = (TextView) rootView.findViewById(R.id.saveEditNote);
        saveNewNote.setBackgroundColor(color);
        saveNewNote.setTransformationMethod(null);
        saveNewNote.setOnClickListener(editOnClick);

        TextView cancelNewNote = (TextView) rootView.findViewById(R.id.cancelEditNote);
        cancelNewNote.setBackgroundColor(color);
        cancelNewNote.setTransformationMethod(null);
        cancelNewNote.setOnClickListener(cancelOnClick);
        if (accessLevelCalendar != 700) {
            LinearLayout aa = (LinearLayout) rootView.findViewById(R.id.optionRow);
            aa.removeView(saveNewNote);
        }

        return rootView;

    }

    private void setToMidnight(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.search).setVisible(false);
        if (accessLevelCalendar == 700) {
            MenuItem a = menu.add("delete");
            a.setIcon(R.drawable.ic_delete_forever_white_36dp);
            a.setShowAsAction(1);
            a.setVisible(true);
            a.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    ContentResolver cr = getContext().getContentResolver();
                    ContentValues values = new ContentValues();
                    values.put(CalendarContract.Events.DELETED, 1);
                    Uri deleteUri = null;
                    deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
                    cr.update(deleteUri, values, null, null);
                    cr.delete(deleteUri, null, null);
                    ((MainUse) getContext()).turnOff(eventId);

                    AsyncTaskLoader runner = new AsyncTaskLoader(getContext(), 1, new ScheduleFragment(), Bundle.EMPTY);
                    runner.execute();
                    return true;
                }
            });
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void loadData() {
        Cursor cursor = null;
        try {
            SQLiteOpenHelper mainDatabase = new MainDatabaseHelper(getContext());
            SQLiteDatabase db = mainDatabase.getReadableDatabase();
            cursor = db.query(MainDatabaseContract.Settings.TABLE_NAME, MainDatabaseContract.Settings.projection, null, null, null, null, null);

            if (cursor.moveToFirst()) {
                startHoursFormat = cursor.getInt(cursor.getColumnIndexOrThrow(MainDatabaseContract.Settings.HOURS_FORMAT));
            }

        } catch (SQLiteException e) {
            Toast.makeText(getContext(), "Database unavailable", Toast.LENGTH_SHORT).show();
        }finally {
            if(cursor !=null) {
                cursor.close();
            }
        }
    }
}
