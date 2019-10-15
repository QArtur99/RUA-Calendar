package com.artf.ruacalendar.Views;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.artf.ruacalendar.R;
import com.artf.ruacalendar.ViewAdapters.AdapterSearchF;

import java.util.ArrayList;
import java.util.List;

import me.everything.providers.android.calendar.Calendar;
import me.everything.providers.android.calendar.CalendarProvider;
import me.everything.providers.android.calendar.Event;
import me.everything.providers.core.Data;


public class SearchFragment extends Fragment {

    private String search;
    private List<SearchObject> searchObjectList = new ArrayList<>();
    private ArrayList<SearchObject> searchObjectListView = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.listView);

        Bundle bundle = this.getArguments();
        search = bundle.getString("search");

        loadEvents();
        ArrayAdapter adapter = new AdapterSearchF(getContext(), searchObjectListView);
        listView.setAdapter(adapter);
        return rootView;
    }

    public void loadEvents() {
        CalendarProvider provider = new CalendarProvider(getContext());
        Data<Calendar> calendars = provider.getCalendars();
        for (Calendar calendar : calendars.getList()) {
            if (calendar.syncEvents == 1) {
                Data<Event> events = provider.getEvents(calendar.id);
                for (Event event : events.getList()) {
                    SearchObject searchObject = new SearchObject(event.id, event.title);
                    searchObjectList.add(searchObject);
                }
            }
        }

        for (SearchObject searchObject : searchObjectList) {
            if (searchObject.title.toLowerCase().contains((search).toLowerCase())) {
                searchObjectListView.add(searchObject);
            }
        }
    }

}
