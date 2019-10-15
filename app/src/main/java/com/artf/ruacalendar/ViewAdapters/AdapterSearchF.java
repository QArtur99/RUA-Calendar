package com.artf.ruacalendar.ViewAdapters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.artf.ruacalendar.MainUse;
import com.artf.ruacalendar.R;
import com.artf.ruacalendar.Views.EditScheduleFragment;
import com.artf.ruacalendar.Views.SearchObject;

import java.util.ArrayList;

/**
 * Created by ART_F on 2017-03-12.
 */

public class AdapterSearchF extends ArrayAdapter<SearchObject> {

    private long eventId;
    private View.OnClickListener goToView = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            eventId = v.getId();

            Bundle bundle = new Bundle();
            bundle.putLong("Event_id", eventId);
            Fragment fragment = new EditScheduleFragment();
            FragmentTransaction ft = ((MainUse) getContext()).getSupportFragmentManager().beginTransaction();
            fragment.setArguments(bundle);
            ft.replace(R.id.content_frame, fragment, "visible_fragment");
            ft.addToBackStack(null);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
        }
    };


    private ArrayList<SearchObject> searchObjects;

    public AdapterSearchF(Context context, ArrayList<SearchObject> searchObjects) {
        super(context, R.layout.fragment_search_part, searchObjects);
        this.searchObjects = searchObjects;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.fragment_search_part, null);
        }
        TextView searchTitle = (TextView) v.findViewById(R.id.searchTitle);

        String titleObject = searchObjects.get(position).title;
        eventId = searchObjects.get(position).eventId;
        searchTitle.setText(titleObject);
        v.setId((int) eventId);
        v.setOnClickListener(goToView);
        return v;
    }
}