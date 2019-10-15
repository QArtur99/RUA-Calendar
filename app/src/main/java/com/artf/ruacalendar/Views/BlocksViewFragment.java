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
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.artf.ruacalendar.Database.MainDatabaseContract;
import com.artf.ruacalendar.Database.MainDatabaseHelper;
import com.artf.ruacalendar.Event.CreateEventFragment;
import com.artf.ruacalendar.Event.EditEventFragment;
import com.artf.ruacalendar.MainUse;
import com.artf.ruacalendar.R;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by ART_F on 2017-01-18.
 */

public class BlocksViewFragment extends Fragment implements WeekView.EventClickListener, MonthLoader.MonthChangeListener, WeekView.EventLongPressListener, WeekView.EmptyViewLongPressListener {
    private static final int TYPE_DAY_VIEW = 1;
    private static final int TYPE_THREE_DAY_VIEW = 2;
    private static final int TYPE_WEEK_VIEW = 3;
    private List<WeekViewEvent> eventsListBlocks = new ArrayList<>();
    private TextView titleToolbar;
    private ImageView arrow;
    private Cursor cursor, cursor2;
    private Date a2;
    private String titleDialog, titleCalendar;
    private Long timeStartDialog;
    private Long timeEndDialog;
    private Long calendarId;
    private int allDay;
    private String duration, rrule;
    private Calendar startDialogCalendar = Calendar.getInstance();
    private Calendar endDialogCalendar = Calendar.getInstance();
    private DateFormat dateDialog = new SimpleDateFormat("EEE dd MMM  HH:mm - ");
    private DateFormat dateDialog2 = new SimpleDateFormat("EEE dd MMM  HH:mm");
    private DateFormat timeDialog = new SimpleDateFormat("HH:mm");
    private Calendar dateTime = Calendar.getInstance();
    private int accessLevelCalendar;
    private int startHoursFormat, startDay;
    private WeekView mWeekView;
    private List<Event> eventsListCompact = new ArrayList<>();
    private Calendar currentCalender = Calendar.getInstance(Locale.getDefault());
    private SimpleDateFormat dateFormatForMonth = new SimpleDateFormat("MMM - yyyy", Locale.getDefault());
    private boolean shouldShow = false;
    private CompactCalendarView compactCalendarView;
    View.OnClickListener showCalendar = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!compactCalendarView.isAnimating()) {
                if (shouldShow) {
                    currentCalender = mWeekView.getFirstVisibleDay();
                    compactCalendarView.setCurrentDate(currentCalender.getTime());
                    compactCalendarView.showCalendar();
                    arrow.setBackgroundResource(R.drawable.arrow_up);
                    a2 = currentCalender.getTime();
                    onDayClick(a2);
                } else {
                    compactCalendarView.hideCalendar();
                    arrow.setBackgroundResource(R.drawable.arrow_down);
                }
                shouldShow = !shouldShow;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_view, container, false);


        loadData();

        LinearLayout titleRow = (LinearLayout) getActivity().findViewById(R.id.titleToolbar);
        arrow = (ImageView) getActivity().findViewById(R.id.arrowTittle);
        titleToolbar = (TextView) getActivity().findViewById(R.id.toolbar_title);
        arrow.setBackgroundResource(R.drawable.arrow_down);

        compactCalendarView = (CompactCalendarView) rootView.findViewById(R.id.compactcalendar_view);
        compactCalendarView.hideCalendar2();
        shouldShow = !shouldShow;
        compactCalendarView.displayOtherMonthDays(true);

        mWeekView = (WeekView) rootView.findViewById(R.id.weekView);

        titleRow.setOnClickListener(showCalendar);

        Bundle bundle = this.getArguments();
        int viewType = bundle.getInt("viewType", 0);

        loadView(viewType);
        loadEventsList();

        compactCalendarView.addEvents(eventsListCompact);
        compactCalendarView.invalidate();
        a2 = currentCalender.getTime();
        onDayClick(a2);
        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                titleToolbar.setText(dateFormatForMonth.format(dateClicked));
                currentCalender.setTime(dateClicked);
                mWeekView.goToDate(currentCalender);


            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                titleToolbar.setText(dateFormatForMonth.format(firstDayOfNewMonth));
                currentCalender.setTime(firstDayOfNewMonth);
                mWeekView.goToDate(currentCalender);
            }
        });
        compactCalendarView.setAnimationListener(new CompactCalendarView.CompactCalendarAnimationListener() {
            @Override
            public void onOpened() {
            }

            @Override
            public void onClosed() {
            }
        });

        compactCalendarView.setUseThreeLetterAbbreviation(false);
        compactCalendarView.setFirstDayOfWeek(startDay);

        // Show a toast message about the touched event.
        mWeekView.setOnEventClickListener(this);

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        mWeekView.setMonthChangeListener(this);

        // Set long press listener for events.
        mWeekView.setEventLongPressListener(this);

        // Set long press listener for empty view
        mWeekView.setEmptyViewLongPressListener(this);

        // Set up a date time interpreter to interpret how the date and time will be formatted in
        // the week view. This is optional.

        mWeekView.setScrollListener(new WeekView.ScrollListener() {
            @Override
            public void onFirstVisibleDayChanged(Calendar newFirstVisibleDay, Calendar oldFirstVisibleDay) {
                titleToolbar.setText(dateFormatForMonth.format(newFirstVisibleDay.getTime()));
                compactCalendarView.setCurrentDate(newFirstVisibleDay.getTime());
            }
        });
        mWeekView.setFirstDayOfWeek(startDay);
        mWeekView.goToHour(currentCalender.get(Calendar.HOUR_OF_DAY));
        return rootView;
    }

    private void loadView(int viewType) {
        int mWeekViewType;
        switch (viewType) {
            case TYPE_DAY_VIEW:
                mWeekViewType = TYPE_DAY_VIEW;
                mWeekView.setNumberOfVisibleDays(1);
                mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
                mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                setupDateTimeInterpreter(false);
                break;
            case TYPE_THREE_DAY_VIEW:
                mWeekViewType = TYPE_THREE_DAY_VIEW;
                mWeekView.setNumberOfVisibleDays(3);
                mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
                mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                setupDateTimeInterpreter(false);
                break;
            case TYPE_WEEK_VIEW:
                mWeekViewType = TYPE_WEEK_VIEW;
                mWeekView.setNumberOfVisibleDays(7);
                mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
                mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
                mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
                setupDateTimeInterpreter(true);
                break;
        }
    }

    private void loadEventsList() {
        FileInputStream fis = null;
        try {
            String FILENAME_BLOCKS = "RUAcalendar_BLOCKS";
            fis = getContext().openFileInput(FILENAME_BLOCKS);
            InputStream buffer = new BufferedInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(buffer);
            eventsListCompact = (List<Event>) ois.readObject();
            eventsListBlocks = (List<WeekViewEvent>) ois.readObject();

        } catch (FileNotFoundException e) {
            //null
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cursor != null) {
            cursor.close();
        }
        if (cursor2 != null) {
            cursor2.close();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        titleToolbar.setText(dateFormatForMonth.format(compactCalendarView.getFirstDayOfCurrentMonth()));
    }

    public void onDayClick(Date dateClicked) {
        titleToolbar.setText(dateFormatForMonth.format(dateClicked));
    }

    @Override
    public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {

        Calendar startOfMonth = Calendar.getInstance();
        startOfMonth.setTimeZone(TimeZone.getDefault());
        startOfMonth.set(Calendar.YEAR, newYear);
        startOfMonth.set(Calendar.MONTH, newMonth - 1);
        startOfMonth.set(Calendar.DAY_OF_MONTH, 1);
        startOfMonth.set(Calendar.HOUR_OF_DAY, 0);
        startOfMonth.set(Calendar.MINUTE, 0);
        startOfMonth.set(Calendar.SECOND, 0);
        startOfMonth.set(Calendar.MILLISECOND, 0);
        Calendar endOfMonth = (Calendar) startOfMonth.clone();
        endOfMonth.set(Calendar.DAY_OF_MONTH, endOfMonth.getMaximum(Calendar.DAY_OF_MONTH));
        endOfMonth.set(Calendar.HOUR_OF_DAY, 23);
        endOfMonth.set(Calendar.MINUTE, 59);
        endOfMonth.set(Calendar.SECOND, 59);

        ArrayList<WeekViewEvent> events2 = new ArrayList<>();
        ;
        for (WeekViewEvent event : eventsListBlocks) {
            if (event.getEndTime().getTimeInMillis() > startOfMonth.getTimeInMillis() &&
                    event.getStartTime().getTimeInMillis() < endOfMonth.getTimeInMillis()) {
                events2.add(event);
            }
        }
        return events2;
    }

    private void setToMidnight(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void setupDateTimeInterpreter(final boolean shortDate) {
        mWeekView.setDateTimeInterpreter(new DateTimeInterpreter() {
            @Override
            public String interpretDate(Calendar date) {
                SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
                String weekday = weekdayNameFormat.format(date.getTime());
                SimpleDateFormat format = new SimpleDateFormat(" M-d", Locale.getDefault());

                // All android api level do not have a standard way of getting the first letter of
                // the week day name. Hence we get the first char programmatically.
                // Details: http://stackoverflow.com/questions/16959502/get-one-letter-abbreviation-of-week-day-of-a-date-in-java#answer-16959657
                if (shortDate)
                    weekday = String.valueOf(weekday.charAt(0));
                return weekday.toUpperCase() + format.format(date.getTime());
            }

            @Override
            public String interpretTime(int hour) {
                if (startHoursFormat > 0) {
                    if (hour == 24) hour = 0;
                    if (hour == 0) hour = 0;
                    return hour + ":00";
                } else {
                    return hour > 11 ? (hour - 12) + " PM" : (hour == 0 ? "12 AM" : hour + " AM");
                }
            }
        });
    }

    protected String getEventTitle(Calendar time) {
        return String.format("Event of %02d:%02d %s/%d", time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), time.get(Calendar.MONTH) + 1, time.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public void onEventClick(final WeekViewEvent event, final RectF eventRect) {
        final long eventId = event.getId();


        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(R.layout.dialog_event)
                .create();
        dialog.show();
        String[] projection = new String[]{"_id", CalendarContract.Events.CALENDAR_ID, "title", "dtstart", "dtend", CalendarContract.Events.ALL_DAY, CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.RRULE, CalendarContract.Events.DURATION};
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
        cursor2 = getActivity().getContentResolver().query(CalendarContract.Events.CONTENT_URI, projection, selection, selectionArgs, "dtstart DESC, dtend DESC");
        if (cursor2 != null && cursor2.moveToFirst()) {

            calendarId = cursor2.getLong(cursor2.getColumnIndex(CalendarContract.Events.CALENDAR_ID));
            titleDialog = cursor2.getString(cursor2.getColumnIndex("title"));
            timeStartDialog = cursor2.getLong(cursor2.getColumnIndex("dtstart"));
            timeEndDialog = cursor2.getLong(cursor2.getColumnIndex("dtend"));
            allDay = cursor2.getInt(cursor2.getColumnIndex(CalendarContract.Events.ALL_DAY));
            duration = cursor2.getString(cursor2.getColumnIndex(CalendarContract.Events.DURATION));
            rrule = cursor2.getString(cursor2.getColumnIndex(CalendarContract.Events.RRULE));
        }

        if (rrule != null) {
            int durationNum = Integer.parseInt(duration.replaceAll("[^\\d.]", ""));
            timeEndDialog = (durationNum * 1000) + timeStartDialog;
        }
        projection = new String[]{"_id", CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CalendarContract.Calendars.SYNC_EVENTS, CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL};
        selection = "_id=?";
        selectionArgs = new String[]{String.valueOf(calendarId)};
        cursor2 = getActivity().getContentResolver().query(CalendarContract.Calendars.CONTENT_URI, projection, selection, selectionArgs, null);
        if (cursor2 != null && cursor2.moveToFirst()) {

            titleCalendar = cursor2.getString(cursor2.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME));
            accessLevelCalendar = cursor2.getInt(cursor2.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL));
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
        titleEvent.setBackgroundColor(event.getColor());
        titleEvent.setText(titleDialog);

        TextView startTime = (TextView) dialog.findViewById(R.id.startTime);
        TextView endTime = (TextView) dialog.findViewById(R.id.endTime);

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
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                fragment.setArguments(bundle);
                ft.replace(R.id.content_frame, fragment, "visible_fragment");
                ft.addToBackStack(null);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
                dialog.dismiss();
            }
        });
        final ImageView deleteEvent = (ImageView) dialog.findViewById(R.id.deleteEvent);
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
                ((MainUse) getContext()).turnOff(eventId);

                deleteEventCompactCalendar(eventId, timeStartDialog);
                for (Iterator<WeekViewEvent> iterator = eventsListBlocks.iterator(); iterator.hasNext(); ) {
                    WeekViewEvent value = iterator.next();
                    if (value.getId() == eventId) {
                        iterator.remove();
                    }
                }

                onMonthChange(startDialogCalendar.get(Calendar.YEAR), startDialogCalendar.get(Calendar.MONTH));
                mWeekView.notifyDatasetChanged();

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

    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
        Toast.makeText(getActivity(), "Long pressed event: " + event.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEmptyViewLongPress(Calendar time) {
        Fragment fragment = new CreateEventFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment, "visible_fragment");
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    private void loadData() {
        try {
            SQLiteOpenHelper mainDatabase = new MainDatabaseHelper(getContext());
            SQLiteDatabase db = mainDatabase.getReadableDatabase();
            cursor = db.query(MainDatabaseContract.Settings.TABLE_NAME, MainDatabaseContract.Settings.projection, null, null, null, null, null);

            if (cursor.moveToFirst()) {
                startHoursFormat = cursor.getInt(cursor.getColumnIndexOrThrow(MainDatabaseContract.Settings.HOURS_FORMAT));
                startDay = cursor.getInt(cursor.getColumnIndexOrThrow(MainDatabaseContract.Settings.START_DAY));
            }

        } catch (SQLiteException e) {
            Toast.makeText(getContext(), "Database unavailable", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }

    private void deleteEventCompactCalendar(Long id, Long date) {
        dateTime.setTimeInMillis(date);
        setToMidnight(dateTime);
        List<Event> bookingsFromMap = compactCalendarView.getEvents(dateTime.getTime());
        if (bookingsFromMap != null) {
            for (Event booking : bookingsFromMap) {
                EventRua eventRua = (EventRua) booking.getData();
                long cc = eventRua.eventId;
                if (cc == id) {
                    compactCalendarView.removeEvent(booking, true);
                    break;
                }
            }
        }
    }
}
