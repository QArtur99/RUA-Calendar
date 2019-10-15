package com.artf.ruacalendar.Notebook;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.artf.ruacalendar.Database.MainDatabaseContract.Notes;
import com.artf.ruacalendar.MainUse;
import com.artf.ruacalendar.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by ART_F on 2016-12-28.
 */

class AdapterNotebook extends CursorAdapter {
    private int startHoursFormat;


    AdapterNotebook(Context context, Cursor cursor, int startHoursFormat) {
        super(context, cursor, 0);
        this.startHoursFormat = startHoursFormat;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = LayoutInflater.from(context).inflate(R.layout.fragment_notebook_part, parent, false);
        LinearLayout noteRow = (LinearLayout) v.findViewById(R.id.noteRow);
        GradientDrawable timeColorLayout = (GradientDrawable) noteRow.getBackground();
        timeColorLayout.setColor(ContextCompat.getColor(context, R.color.coloreNote));
        return v;

    }

    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {
        TextView noteText = (TextView) view.findViewById(R.id.noteText);
        noteText.setGravity(Gravity.START | Gravity.TOP);

        String body = cursor.getString(cursor.getColumnIndexOrThrow(Notes.NOTE));
        noteText.setText(body);

        TextView timeText = (TextView) view.findViewById(R.id.dateText);
        timeText.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
        long getTime = cursor.getLong(cursor.getColumnIndexOrThrow(Notes.TIME_IN_MILLIS));

        Calendar calendar = Calendar.getInstance();
        DateFormat time = new SimpleDateFormat("   EEE dd MMM yyyy  HH:mm");
        calendar.setTimeInMillis(getTime);
        String CREATE_DATE = context.getResources().getText(R.string.createDate).toString();
        if(startHoursFormat > 0) {
            timeText.setText(CREATE_DATE + time.format(calendar.getTime()));
        }else{
            int timeFormat = calendar.get(Calendar.HOUR_OF_DAY);
            if(timeFormat > 11) {
                calendar.add(Calendar.HOUR, -12);
                timeText.setText(CREATE_DATE + time.format(calendar.getTime()) + " PM");
            }else{
                timeText.setText(CREATE_DATE + time.format(calendar.getTime()) + " AM");
            }
        }

        long id = cursor.getLong(cursor.getColumnIndexOrThrow(Notes._ID));
        view.setId((int) id);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = view.getId();
                Bundle bundle = new Bundle();
                bundle.putInt(Notes._ID, id);
                Fragment fragment = new EditNoteFragment();
                FragmentTransaction ft = ((MainUse) context).getSupportFragmentManager().beginTransaction();
                fragment.setArguments(bundle);
                ft.replace(R.id.content_frame, fragment, "visible_fragment");
                ft.addToBackStack(null);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
            }
        });

    }

}