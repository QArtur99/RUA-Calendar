package com.artf.ruacalendar.Views;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.artf.ruacalendar.R;
import com.github.tibolte.agendacalendarview.AgendaCalendarView;
import com.github.tibolte.agendacalendarview.CalendarManager;
import com.github.tibolte.agendacalendarview.CalendarPickerController;
import com.github.tibolte.agendacalendarview.models.BaseCalendarEvent;
import com.github.tibolte.agendacalendarview.models.CalendarEvent;
import com.github.tibolte.agendacalendarview.models.DayItem;
import com.github.tibolte.agendacalendarview.models.IDayItem;
import com.github.tibolte.agendacalendarview.models.WeekItem;
import com.github.tibolte.agendacalendarview.render.DefaultEventRenderer;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


/**
 * Created by ART_F on 2017-01-28.
 */

public class ScheduleFragment extends Fragment implements CalendarPickerController {

    private TextView titleToolbar;
    private List<CalendarEvent> eventList = new ArrayList<>();
    private SimpleDateFormat dateFormatForMonth = new SimpleDateFormat("MMM - yyyy", Locale.getDefault());
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_schedule, container, false);


        AgendaCalendarView mAgendaCalendarView = (AgendaCalendarView) rootView.findViewById(R.id.agenda_calendar_view);
        titleToolbar = (TextView) getActivity().findViewById(R.id.toolbar_title);

        Calendar minDate = Calendar.getInstance();
        Calendar maxDate = Calendar.getInstance();
        minDate.add(Calendar.MONTH, -2);
        minDate.set(Calendar.DAY_OF_MONTH, 1);
        maxDate.add(Calendar.YEAR, 1);

        loadEventsList();


        mAgendaCalendarView.enableCalenderView(false);
        CalendarManager calendarManager = CalendarManager.getInstance(getContext().getApplicationContext());
        calendarManager.buildCal(minDate, maxDate, Locale.getDefault(), new DayItem(), new WeekItem());
        calendarManager.loadEvents(eventList, new BaseCalendarEvent());
        List readyEvents = calendarManager.getEvents();
        List readyDays = calendarManager.getDays();
        List readyWeeks = calendarManager.getWeeks();
        mAgendaCalendarView.init(Locale.getDefault(), readyWeeks, readyDays, readyEvents, this);
        mAgendaCalendarView.addEventRenderer(new DefaultEventRenderer());

        return rootView;
    }

    private void loadEventsList() {
        FileInputStream fis = null;
        try {
            String FILENAME_SCHEDULE = "RUAcalendar_Schedule";
            fis = getContext().openFileInput(FILENAME_SCHEDULE);
            InputStream buffer = new BufferedInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(buffer);
            eventList = (List<CalendarEvent>) ois.readObject();
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
    public void onDaySelected(IDayItem dayItem) {
        Log.d("++++++++++", String.format("Selected day: %s", dayItem));
    }

    @Override
    public void onEventSelected(final CalendarEvent event) {
        if (event.getStartTime() != null) {
            long id = event.getId();
            Bundle bundle = new Bundle();
            bundle.putLong("Event_id", id);
            Fragment fragment = new EditScheduleFragment();
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            fragment.setArguments(bundle);
            ft.replace(R.id.content_frame, fragment, "visible_fragment");
            ft.addToBackStack(null);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
        }
    }

    @Override
    public void onScrollToDate(Calendar calendar) {
        titleToolbar.setText(dateFormatForMonth.format(calendar.getTime()));
    }
}
