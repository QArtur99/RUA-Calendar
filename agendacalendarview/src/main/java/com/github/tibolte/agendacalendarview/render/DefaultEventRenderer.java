package com.github.tibolte.agendacalendarview.render;

import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.tibolte.agendacalendarview.R;
import com.github.tibolte.agendacalendarview.models.BaseCalendarEvent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Class helping to inflate our default layout in the AgendaAdapter
 */
public class DefaultEventRenderer extends EventRenderer<BaseCalendarEvent> {

    // region class - EventRenderer

    @Override
    public void render(@NonNull View view, @NonNull BaseCalendarEvent event) {
        TextView txtTitle = (TextView) view.findViewById(R.id.view_agenda_event_title);
//        TextView txtLocation = (TextView) view.findViewById(R.id.view_agenda_event_location);
        LinearLayout descriptionContainer = (LinearLayout) view.findViewById(R.id.view_agenda_event_description_container);
//        LinearLayout locationContainer = (LinearLayout) view.findViewById(R.id.view_agenda_event_location_container);
        RelativeLayout timeColor = (RelativeLayout) view.findViewById(R.id.rowView2);
        TextView timeStartTV = (TextView) view.findViewById(R.id.timeStartText);
        TextView timeEndTV = (TextView) view.findViewById(R.id.timeEndText);
        DateFormat time = new SimpleDateFormat("HH:mm");
        Calendar calendar = Calendar.getInstance();
        if(event.getHourFormat() > 0) {
            if (event.isAllDay()) {
                calendar = event.getStartTime();
                setToMidnight(calendar);
                timeStartTV.setText(String.valueOf(time.format(calendar.getTime())));
                timeEndTV.setText(String.valueOf(time.format(calendar.getTime())));
            } else {
                if (event.getStartTime() != null) {
                    calendar = event.getStartTime();
                    timeStartTV.setText(String.valueOf(time.format(calendar.getTime())));
                    calendar = event.getEndTime();
                    timeEndTV.setText(String.valueOf(time.format(calendar.getTime())));
                } else {
                    timeColor.setVisibility(View.INVISIBLE);
                }

            }
        }else{
            if (event.isAllDay()) {
                calendar = event.getStartTime();
                setToMidnight(calendar);
                timeStartTV.setText(String.valueOf(time.format(calendar.getTime())) + " AM");
                timeEndTV.setText(String.valueOf(time.format(calendar.getTime())) + " AM");
            } else {
                if (event.getStartTime() != null) {
                    calendar = event.getStartTime();
                    int timeFormat = calendar.get(Calendar.HOUR_OF_DAY);
                    if(timeFormat > 11){
                        calendar.add(Calendar.HOUR, -12);
                        timeStartTV.setText(String.valueOf(time.format(calendar.getTime())) + " PM");
                    }else{
                        timeStartTV.setText(String.valueOf(time.format(calendar.getTime())) + " AM");
                    }
                    calendar = event.getEndTime();
                    timeFormat = calendar.get(Calendar.HOUR_OF_DAY);
                    if(timeFormat > 11){
                        calendar.add(Calendar.HOUR, -12);
                        timeEndTV.setText(String.valueOf(time.format(calendar.getTime())) + " PM");
                    }else{
                        timeEndTV.setText(String.valueOf(time.format(calendar.getTime())) + " AM");
                    }
                } else {
                    timeColor.setVisibility(View.INVISIBLE);
                }

            }
        }
        descriptionContainer.setVisibility(View.VISIBLE);
        txtTitle.setTextColor(view.getResources().getColor(android.R.color.black));


        txtTitle.setText(event.getTitle());
//        txtLocation.setText(event.getLocation());
//        if (event.getLocation().length() > 0) {
//            locationContainer.setVisibility(View.VISIBLE);
//            txtLocation.setText(event.getLocation());
//        } else {
//            locationContainer.setVisibility(View.GONE);
//        }
//
//        if (event.getTitle().equals(view.getResources().getString(R.string.agenda_event_no_events))) {
//            txtTitle.setTextColor(view.getResources().getColor(android.R.color.black));
//        } else {
//            txtTitle.setTextColor(view.getResources().getColor(R.color.theme_text_icons));
//        }
//        descriptionContainer.setBackgroundColor(event.getColor());
        GradientDrawable bgShapeA = (GradientDrawable) descriptionContainer.getBackground();
        bgShapeA.setColor(event.getColor());
        GradientDrawable bgShape = (GradientDrawable) timeColor.getBackground();
        bgShape.setColor(event.getColor());
//        txtLocation.setTextColor(view.getResources().getColor(R.color.theme_text_icons));
    }

    @Override
    public int getEventLayout() {
        return R.layout.view_agenda_event;
    }

    // endregion
    private void setToMidnight(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
}
