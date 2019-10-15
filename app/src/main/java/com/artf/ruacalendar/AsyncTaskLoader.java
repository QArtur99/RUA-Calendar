package com.artf.ruacalendar;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.alamkanak.weekview.WeekViewEvent;
import com.artf.ruacalendar.Database.MainDatabaseContract;
import com.artf.ruacalendar.Database.MainDatabaseHelper;
import com.artf.ruacalendar.Views.EventRua;
import com.artf.ruacalendar.Views.EventSchedule;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.github.tibolte.agendacalendarview.models.CalendarEvent;

import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by ART_F on 2017-03-08.
 */

public class AsyncTaskLoader extends AsyncTask<String, String, String> {

    public int startHoursFormat;
    private AlertDialog dialog;
    private List<Event> eventsListCompact = new ArrayList<>();
    private List<CalendarEvent> eventsListSchedule = new ArrayList<>();
    private List<WeekViewEvent> eventsListBlocks = new ArrayList<>();
    private String[] projection;
    private String selection;
    private String[] selectionArgs;
    private Cursor cursor, cursor2;
    private Calendar startTime, endTime;
    private int type;
    private String resp;
    private Handler mHandler = new Handler();
    private Context mContext;
    private Fragment fragment;
    private Bundle bundle;



    public AsyncTaskLoader(Context context, int type, Fragment fragment, Bundle bundle) {
        this.mContext = context;
        this.type = type;
        this.fragment = fragment;
        this.bundle = bundle;
    }

    AsyncTaskLoader(Context context, int type) {
        this.mContext = context;
        this.type = type;
    }

    @Override
    protected String doInBackground(String... params) {
        publishProgress("Sleeping..."); // Calls onProgressUpdate()

        switch (type) {
            case 0:
                getEventsSchedule();
                loadSchedule();
                break;
            case 1:
                getEventsSchedule();
                loadSchedule();
                break;
            case 2:
                getEventsBlocks();
                loadBlocks();
                break;
            case 22:
                getEventsBlocks();
                loadBlocks();
                break;
            case 3:
                getEventsCompact();
                loadCompact();
                break;
            case 33:
                getEventsCompact();
                loadCompact();
                break;
        }

        return resp;
    }

    private void loadCompact() {
        FileOutputStream fos = null;
        try {
            String FILENAME_COMPACT = "RUAcalendar_COMPACT";
            mContext.deleteFile(FILENAME_COMPACT);
            fos = mContext.openFileOutput(FILENAME_COMPACT, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(eventsListCompact);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadBlocks() {
        FileOutputStream fos = null;
        try {
            String FILENAME_BLOCKS = "RUAcalendar_BLOCKS";
            mContext.deleteFile(FILENAME_BLOCKS);
            fos = mContext.openFileOutput(FILENAME_BLOCKS, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(eventsListCompact);
            oos.writeObject(eventsListBlocks);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadSchedule() {
        FileOutputStream fos = null;
         try {
            String FILENAME_SCHEDULE = "RUAcalendar_Schedule";
            mContext.deleteFile(FILENAME_SCHEDULE);
            fos = mContext.openFileOutput(FILENAME_SCHEDULE, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(eventsListSchedule);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (type != 0 && type < 5) {
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    dialog.dismiss();
                    FragmentTransaction ft = ((MainUse) mContext).getSupportFragmentManager().beginTransaction();
                    fragment.setArguments(bundle);
                    ft.replace(R.id.content_frame, fragment, "visible_fragment");
                    ft.addToBackStack(null);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    ft.commit();
                }
            }, 200);
        }

    }

    @Override
    protected void onPreExecute() {
        if (type != 0 && type < 5) {

            dialog = new AlertDialog.Builder(mContext)
                    .setView(R.layout.dialog_progress)
                    .create();
            dialog.show();
            dialog.setCanceledOnTouchOutside(false);
            DisplayMetrics displaymetrics = new DisplayMetrics();
            dialog.getWindow().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int height = (int) (displaymetrics.widthPixels * 0.25);
            dialog.getWindow().setLayout(height, height);
        }
    }

    @Override
    protected void onProgressUpdate(String... text) {
        // Things to be done while execution of long running operation is in
        // progress. For example updating ProgessDialog
    }

    private void getEventsCompact() {
        try {
            projection = new String[]{"_id", Calendars.SYNC_EVENTS, Calendars.CALENDAR_COLOR};
            selection = Calendars.SYNC_EVENTS + "=?";
            selectionArgs = new String[]{"1"};
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            cursor = mContext.getContentResolver().query(Calendars.CONTENT_URI, projection, selection, selectionArgs, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        int color = cursor.getInt(cursor.getColumnIndexOrThrow(Calendars.CALENDAR_COLOR));
                        Long calendarId = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));

                        projection = new String[]{"_id", "title", "dtstart", "dtend", Events.ALL_DAY, Events.DELETED, Events.RRULE, Events.DURATION, Events.RDATE};
                        selection = "calendar_id=?";
                        selectionArgs = new String[]{calendarId.toString()};
                        cursor2 = mContext.getContentResolver().query(Events.CONTENT_URI, projection, selection, selectionArgs, "dtstart DESC, dtend DESC");
                        if (cursor2 != null) {
                            if (cursor2.moveToFirst()) {
                                do {
                                    int deleted = cursor2.getInt(cursor2.getColumnIndex(Events.DELETED));
                                    if (deleted > 0) {
                                    } else {
                                        Long eventId = cursor2.getLong(cursor2.getColumnIndex("_id"));
                                        String title = cursor2.getString(cursor2.getColumnIndex("title"));
                                        Long timeStart = cursor2.getLong(cursor2.getColumnIndex("dtstart"));
                                        Long timeEnd = cursor2.getLong(cursor2.getColumnIndex("dtend"));
                                        String rrule = cursor2.getString(cursor2.getColumnIndex(Events.RRULE));
                                        String date = cursor2.getString(cursor2.getColumnIndex(Events.RDATE));
                                        String duration = cursor2.getString(cursor2.getColumnIndex(Events.DURATION));
                                        int allDay = cursor2.getInt(cursor2.getColumnIndex(Events.ALL_DAY));
                                        if (rrule != null) {
                                            RecurrenceRule rule = new RecurrenceRule(rrule);
                                            DateTime start = new DateTime(timeStart);
                                            RecurrenceRuleIterator it = rule.iterator(start);
                                            int maxInstances = 100; // limit instances for rules that recur forever
                                            while (it.hasNext() && (!rule.isInfinite() || maxInstances-- > 0)) {
                                                DateTime nextInstance = it.nextDateTime();
                                                long recurenceStartTime = nextInstance.getTimestamp();
                                                int durationNum = Integer.parseInt(duration.replaceAll("[^\\d.]", ""));
                                                EventRua aaa = new EventRua(calendarId, eventId, title, timeStart, (timeStart + (durationNum * 1000)), allDay);
                                                eventsListCompact.add(new Event(color, recurenceStartTime, aaa));
                                            }
                                        } else {
                                            EventRua aa = new EventRua(calendarId, eventId, title, timeStart, timeEnd, allDay);
                                            eventsListCompact.add(new Event(color, timeStart, aa));
                                        }
                                    }
                                } while (cursor2.moveToNext());
                            }
                            cursor2.close();
                        }
                    } while (cursor.moveToNext());
                }
            }
        } catch (InvalidRecurrenceRuleException e) {
            e.printStackTrace();
        } finally {
            if(cursor !=null) {
                cursor.close();
            }
        }
    }

    private void getEventsSchedule() {
        loadData();
        try {
            projection = new String[]{"_id", Calendars.SYNC_EVENTS, Calendars.CALENDAR_COLOR};
            selection = CalendarContract.Calendars.SYNC_EVENTS + "=?";
            selectionArgs = new String[]{"1"};
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            cursor = mContext.getContentResolver().query(Calendars.CONTENT_URI, projection, selection, selectionArgs, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        int color = cursor.getInt(cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_COLOR));
                        Long calendarId = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));

                        projection = new String[]{"_id", "title", "dtstart", "dtend", Events.ALL_DAY, Events.DELETED, Events.RRULE, Events.DURATION, Events.RDATE};
                        selection = "calendar_id=?";
                        selectionArgs = new String[]{calendarId.toString()};
                        cursor2 = mContext.getContentResolver().query(Events.CONTENT_URI, projection, selection, selectionArgs, "dtstart DESC, dtend DESC");
                        if (cursor2 != null) {
                            if (cursor2.moveToFirst()) {
                                do {
                                    int deleted = cursor2.getInt(cursor2.getColumnIndex(Events.DELETED));
                                    if (deleted > 0) {
                                    } else {
                                        Long eventId = cursor2.getLong(cursor2.getColumnIndex("_id"));
                                        String title = cursor2.getString(cursor2.getColumnIndex("title"));
                                        Long timeStart = cursor2.getLong(cursor2.getColumnIndex("dtstart"));
                                        Long timeEnd = cursor2.getLong(cursor2.getColumnIndex("dtend"));
                                        String rrule = cursor2.getString(cursor2.getColumnIndex(Events.RRULE));
                                        String date = cursor2.getString(cursor2.getColumnIndex(Events.RDATE));
                                        String duration = cursor2.getString(cursor2.getColumnIndex(Events.DURATION));
                                        int allDay = cursor2.getInt(cursor2.getColumnIndex(Events.ALL_DAY));
                                        EventSchedule aa;
                                        if (rrule != null) {
                                            RecurrenceRule rule = new RecurrenceRule(rrule);
                                            DateTime start = new DateTime(timeStart);
                                            RecurrenceRuleIterator it = rule.iterator(start);
                                            int maxInstances = 100; // limit instances for rules that recur forever
                                            while (it.hasNext() && (!rule.isInfinite() || maxInstances-- > 0)) {
                                                DateTime nextInstance = it.nextDateTime();
                                                long recurenceStartTime = nextInstance.getTimestamp();
                                                int durationNum = Integer.parseInt(duration.replaceAll("[^\\d.]", ""));
                                                aa = new EventSchedule(eventId, color, title, "", "", recurenceStartTime, (timeStart + (durationNum * 1000)), 0, null, 0, startHoursFormat);
                                                if (allDay > 0) {
                                                    aa.setAllDay(true);
                                                }
                                                eventsListSchedule.add(aa);
                                            }
                                        } else {
                                            if (allDay > 0) {
                                                aa = new EventSchedule(eventId, color, title, "", "", timeStart, timeStart, 0, null, 0, startHoursFormat);
                                                aa.setAllDay(true);
                                            } else {
                                                if (timeEnd - timeStart <= 0) {
                                                    startTime = Calendar.getInstance();
                                                    startTime.setTimeInMillis(timeStart);
                                                    endTime = (Calendar) startTime.clone();
                                                    endTime.add(Calendar.MINUTE, 15);
                                                    aa = new EventSchedule(eventId, color, title, "", "", timeStart, endTime.getTimeInMillis(), 0, null, 0, startHoursFormat);
                                                } else {
                                                    startTime = Calendar.getInstance();
                                                    endTime = Calendar.getInstance();
                                                    startTime.setTimeInMillis(timeStart);
                                                    startTime.setTimeZone(TimeZone.getDefault());
                                                    endTime.setTimeInMillis(timeEnd);
                                                    endTime.setTimeZone(TimeZone.getDefault());
                                                    aa = new EventSchedule(eventId, color, title, "", "", startTime.getTimeInMillis(), endTime.getTimeInMillis(), 0, null, 0, startHoursFormat);
                                                }
                                            }
                                            eventsListSchedule.add(aa);
                                        }
                                    }
                                }
                                while (cursor2.moveToNext());

                            }
                            cursor2.close();
                        }
                    } while (cursor.moveToNext());
                }
            }
        } catch (InvalidRecurrenceRuleException e) {
            e.printStackTrace();
        } finally {
            if(cursor !=null) {
                cursor.close();
            }
        }
    }

    private void getEventsBlocks() {
        try {
            projection = new String[]{"_id", Calendars.SYNC_EVENTS, Calendars.CALENDAR_COLOR};
            selection = Calendars.SYNC_EVENTS + "=?";
            selectionArgs = new String[]{"1"};
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            cursor = mContext.getContentResolver().query(Calendars.CONTENT_URI, projection, selection, selectionArgs, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        int color = cursor.getInt(cursor.getColumnIndexOrThrow(Calendars.CALENDAR_COLOR));
                        Long calendarId = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));

                        projection = new String[]{"_id", "title", "dtstart", "dtend", Events.ALL_DAY, Events.DELETED, Events.RRULE, Events.DURATION, Events.RDATE};
                        selection = "calendar_id=?";
                        selectionArgs = new String[]{calendarId.toString()};
                        cursor2 = mContext.getContentResolver().query(Events.CONTENT_URI, projection, selection, selectionArgs, "dtstart DESC, dtend DESC");
                        if (cursor2 != null) {
                            if (cursor2.moveToFirst()) {
                                do {
                                    int deleted = cursor2.getInt(cursor2.getColumnIndex(Events.DELETED));
                                    if (deleted > 0) {
                                    } else {

                                        Long eventId = cursor2.getLong(cursor2.getColumnIndex("_id"));
                                        String title = cursor2.getString(cursor2.getColumnIndex("title"));
                                        Long timeStart = cursor2.getLong(cursor2.getColumnIndex("dtstart"));
                                        Long timeEnd = cursor2.getLong(cursor2.getColumnIndex("dtend"));
                                        String rrule = cursor2.getString(cursor2.getColumnIndex(Events.RRULE));
                                        String date = cursor2.getString(cursor2.getColumnIndex(Events.RDATE));
                                        String duration = cursor2.getString(cursor2.getColumnIndex(Events.DURATION));
                                        int allDay = cursor2.getInt(cursor2.getColumnIndex(Events.ALL_DAY));

                                        WeekViewEvent event;
                                        if (rrule != null) {
                                            RecurrenceRule rule = new RecurrenceRule(rrule);
                                            DateTime start = new DateTime(timeStart);
                                            RecurrenceRuleIterator it = rule.iterator(start);
                                            int maxInstances = 100; // limit instances for rules that recur forever

                                            EventRua aaa = new EventRua(calendarId, eventId, null, null, null, allDay);

                                            while (it.hasNext() && (!rule.isInfinite() || maxInstances-- > 0)) {
                                                DateTime nextInstance = it.nextDateTime();

                                                long recurenceStartTime = nextInstance.getTimestamp();
                                                eventsListCompact.add(new Event(color, recurenceStartTime, aaa));

                                                startTime = Calendar.getInstance();
                                                endTime = Calendar.getInstance();
                                                startTime.setTimeInMillis(recurenceStartTime);
                                                startTime.setTimeZone(TimeZone.getDefault());
                                                int durationNum = Integer.parseInt(duration.replaceAll("[^\\d.]", ""));
                                                endTime.setTimeInMillis(recurenceStartTime + (durationNum * 1000));
                                                endTime.setTimeZone(TimeZone.getDefault());

                                                event = new WeekViewEvent(eventId, title, null, startTime, endTime);
                                                if (allDay > 0) {
                                                    event.setAllDay(true);
                                                }
                                                event.setColor(color);
                                                eventsListBlocks.add(event);

                                            }
                                        } else {
                                            EventRua aa = new EventRua(calendarId, eventId, null, null, null, allDay);
                                            eventsListCompact.add(new Event(color, timeStart, aa));
                                            if (allDay > 0) {
                                                startTime = Calendar.getInstance();
                                                startTime.setTimeInMillis(timeStart);
                                                endTime = (Calendar) startTime.clone();
                                                endTime.add(Calendar.SECOND, 1);
                                                event = new WeekViewEvent(eventId, title, null, startTime, endTime);
                                                event.setAllDay(true);
                                            } else {
                                                if (timeEnd - timeStart <= 0) {
                                                    startTime = Calendar.getInstance();
                                                    startTime.setTimeInMillis(timeStart);
                                                    endTime = (Calendar) startTime.clone();
                                                    endTime.add(Calendar.MINUTE, 15);
                                                    event = new WeekViewEvent(eventId, title, null, startTime, endTime);
                                                } else {
                                                    startTime = Calendar.getInstance();
                                                    endTime = Calendar.getInstance();
                                                    startTime.setTimeInMillis(timeStart);
                                                    startTime.setTimeZone(TimeZone.getDefault());
                                                    endTime.setTimeInMillis(timeEnd);
                                                    endTime.setTimeZone(TimeZone.getDefault());
                                                    event = new WeekViewEvent(eventId, title, null, startTime, endTime);
                                                }
                                            }
                                            event.setColor(color);
                                            eventsListBlocks.add(event);
                                        }
                                    }
                                }
                                while (cursor2.moveToNext());
                            }
                            cursor2.close();
                        }
                    } while (cursor.moveToNext());
                }
            }
        } catch (InvalidRecurrenceRuleException e) {
            e.printStackTrace();
        } finally {
            if(cursor !=null) {
                cursor.close();
            }
        }
    }

    private void loadData() {
        try {
            SQLiteOpenHelper mainDatabase = new MainDatabaseHelper(mContext);
            SQLiteDatabase db = mainDatabase.getReadableDatabase();
            cursor = db.query(MainDatabaseContract.Settings.TABLE_NAME, MainDatabaseContract.Settings.projection, null, null, null, null, null);

            if (cursor.moveToFirst()) {
                startHoursFormat = cursor.getInt(cursor.getColumnIndexOrThrow(MainDatabaseContract.Settings.HOURS_FORMAT));
            }

        } catch (SQLiteException e) {
            Toast.makeText(mContext, "Database unavailable", Toast.LENGTH_SHORT).show();
        }finally {
            if(cursor !=null) {
                cursor.close();
            }
        }
    }

}