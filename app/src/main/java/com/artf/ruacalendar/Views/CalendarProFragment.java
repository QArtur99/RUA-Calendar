package com.artf.ruacalendar.Views;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.artf.ruacalendar.Database.MainDatabaseContract;
import com.artf.ruacalendar.Database.MainDatabaseHelper;
import com.artf.ruacalendar.R;
import com.artf.ruacalendar.ViewAdapters.AdapterCalendarPro;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarProFragment extends Fragment {

    private List<Event> eventsListCompact = new ArrayList<>();
    private Cursor cursor;
    private ArrayList<Event> mutableBookings = new ArrayList<>();
    private ArrayAdapter adapter;
    private TextView titleToolbar;
    private ImageView arrow;
    private int startHoursFormat, startDay;
    private Calendar currentCalender;
    private SimpleDateFormat dateFormatForMonth = new SimpleDateFormat("MMM - yyyy", Locale.getDefault());
    private boolean shouldShow = false;
    private CompactCalendarView compactCalendarView;
    View.OnClickListener showCalendar = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!compactCalendarView.isAnimating()) {
                if (shouldShow) {
                    compactCalendarView.showCalendar();
                    arrow.setBackgroundResource(R.drawable.arrow_up);
                    Date a2 = currentCalender.getTime();
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_calendar_pro, container, false);


        loadData();
        LinearLayout titleRow = (LinearLayout) getActivity().findViewById(R.id.titleToolbar);
        arrow = (ImageView) getActivity().findViewById(R.id.arrowTittle);
        titleToolbar = (TextView) getActivity().findViewById(R.id.toolbar_title);
        arrow.setBackgroundResource(R.drawable.arrow_up);
        titleRow.setOnClickListener(showCalendar);
        currentCalender = Calendar.getInstance();
        RelativeLayout main = (RelativeLayout) rootView.findViewById((R.id.main_content));
        ListView bookingsListView = (ListView) rootView.findViewById(R.id.bookings_listview);


        adapter = new AdapterCalendarPro(getContext(), mutableBookings, CalendarProFragment.this, startHoursFormat);
        bookingsListView.setAdapter(adapter);
        compactCalendarView = (CompactCalendarView) rootView.findViewById(R.id.compactcalendar_view);

        compactCalendarView.setUseThreeLetterAbbreviation(false);
        compactCalendarView.setFirstDayOfWeek(startDay);

        loadEventsList();

        compactCalendarView.addEvents(eventsListCompact);
        compactCalendarView.invalidate();
        onDayClick(currentCalender.getTime());


        // show days from other months as greyed out days
        compactCalendarView.displayOtherMonthDays(true);


        //set title on calendar scroll
        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                currentCalender.setTime(dateClicked);
                titleToolbar.setText(dateFormatForMonth.format(dateClicked));
                List<Event> bookingsFromMap = compactCalendarView.getEvents(dateClicked);
                if (bookingsFromMap != null) {
                    mutableBookings.clear();
                    for (Event booking : bookingsFromMap) {
                        mutableBookings.add(booking);
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                titleToolbar.setText(dateFormatForMonth.format(firstDayOfNewMonth));
                onDayClick(firstDayOfNewMonth);
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
        return rootView;
    }

    private void loadEventsList() {
        FileInputStream fis = null;
        try {
            String FILENAME_COMPACT = "RUAcalendar_COMPACT";
            fis = getContext().openFileInput(FILENAME_COMPACT);
            InputStream buffer = new BufferedInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(buffer);
            eventsListCompact = (List<Event>) ois.readObject();
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
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onDayClick(Date dateClicked) {
        currentCalender.setTime(dateClicked);
        titleToolbar.setText(dateFormatForMonth.format(dateClicked));
        List<Event> bookingsFromMap = compactCalendarView.getEvents(dateClicked);
        if (bookingsFromMap != null) {
            mutableBookings.clear();
            for (Event booking : bookingsFromMap) {
                mutableBookings.add(booking);
            }
            adapter.notifyDataSetChanged();
        }
    }

    public void deleteEventCompactCalendar(Long id) {
        List<Event> bookingsFromMap = compactCalendarView.getEvents(currentCalender.getTime());
        if (bookingsFromMap != null) {
            for (Event booking : bookingsFromMap) {
                EventRua eventRua = (EventRua) booking.getData();
                long cc = eventRua.eventId;
                if (cc == id) {
                    compactCalendarView.removeEvent(booking, true);
                    onDayClick(currentCalender.getTime());
                    break;
                }
            }
        }
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
        }finally {
            if(cursor !=null) {
                cursor.close();
            }
        }
    }

}