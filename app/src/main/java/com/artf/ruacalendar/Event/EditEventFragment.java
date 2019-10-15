package com.artf.ruacalendar.Event;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
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
import android.support.v7.app.AlertDialog;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.artf.ruacalendar.Database.MainDatabaseContract;
import com.artf.ruacalendar.Database.MainDatabaseHelper;
import com.artf.ruacalendar.MainUse;
import com.artf.ruacalendar.Notifications.NotificationObject;
import com.artf.ruacalendar.Notifications.CalendarChangedReceiver;
import com.artf.ruacalendar.R;
import com.example.art_f.recurrenedialog.EventRecurrence;
import com.example.art_f.recurrenedialog.EventRecurrenceFormatter;
import com.example.art_f.recurrenedialog.RecurrencePickerDialog;
import com.tokenautocomplete.FilteredArrayAdapter;
import com.tokenautocomplete.TokenCompleteTextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import me.everything.providers.android.calendar.Attendee;
import me.everything.providers.android.calendar.CalendarProvider;
import me.everything.providers.android.calendar.Event;
import me.everything.providers.android.calendar.Reminder;
import me.everything.providers.core.Data;

public class EditEventFragment extends Fragment implements TokenCompleteTextView.TokenListener<Person> {

    TextView noNotification, tenMinutesBefore, fifTeenMinutesBefore, thirtyMinutesBefore, sixtyMinutesBefore, custom;
    TextView minuteBefore, hourBefore, dayBefore, weekBefore, asNotification, asEmail, doneCustomNotification;
    EditText timeCustomEditText;
    Boolean customNotificationBoolean, asMethodNotification;
    int customTime;
    AlertDialog standardNotificationDialog;
    ArrayList<NotificationObject> notificationsList = new ArrayList<>();
    ListView listViewCreate;
    TextView addNotification;
    int posNotificationList;
    ListView listViewNotifications;
    ArrayAdapter notificationAdapter;
    View rootView;
    String titleOfCustomNotification;
    View.OnClickListener getStandardNotificationDialogItems = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TextView titleId = (TextView) standardNotificationDialog.findViewById(v.getId());
            String title = titleId.getText().toString().toLowerCase();
            switch (v.getId()) {
                case R.id.noNotifications:
                    if (v.isSelected()) {
                    } else {
                        if (notificationsList.size() > 0) {
                            turnOffStandardSelectors(v);
                            removeNotification();
                        } else {
                            standardNotificationDialog.dismiss();
                        }
                    }
                    break;
                case R.id.tenMinutesBefore:
                    if (v.isSelected()) {
                        v.setSelected(false);
                        removeNotification();
                    } else {
                        turnOffStandardSelectors(v);
                        addNotification("", title, 10, 0);
                    }
                    break;
                case R.id.fifTeenMinutesBefore:
                    if (v.isSelected()) {
                        v.setSelected(false);
                        removeNotification();
                    } else {
                        turnOffStandardSelectors(v);
                        addNotification("", title, 15, 0);
                    }
                    break;
                case R.id.thirtyMinutesBefore:
                    if (v.isSelected()) {
                        v.setSelected(false);
                        removeNotification();
                    } else {
                        turnOffStandardSelectors(v);
                        addNotification("", title, 30, 0);
                    }
                    break;
                case R.id.sixtyMinutesBefore:
                    if (v.isSelected()) {
                        v.setSelected(false);
                        removeNotification();
                    } else {
                        turnOffStandardSelectors(v);
                        addNotification("", title, 60, 0);
                    }
                    break;
            }
        }
    };
    View.OnLongClickListener goToLocation = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            String address = location.getText().toString();

            Uri addressUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
            Intent intent = new Intent(Intent.ACTION_VIEW, addressUri);

            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            }

            return false;
        }
    };
    View.OnClickListener standardNotification = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            posNotificationList = -1;
            buildStandardDialog();
        }
    };
    ListView.OnItemClickListener notificationEdit = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            posNotificationList = position;
            if (notificationsList.get(position).notificationType == 0) {
                buildStandardDialog();
            } else {
                view.setOnClickListener(customNotificationDialog);
            }
            view.callOnClick();
        }
    };
    private AlertDialog dialogCustom;
    View.OnClickListener getCustomNotificationDialogItems = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TextView title = (TextView) dialogCustom.findViewById(v.getId());
            titleOfCustomNotification = " " + title.getText().toString().toLowerCase();
            switch (v.getId()) {
                case R.id.minuteBefore:
                    if (v.isSelected()) {
                        v.setSelected(false);
                        customNotificationBoolean = false;
                    } else {
                        turnOffCustomSelectors(v);
                        customTime = 1;
                    }
                    break;
                case R.id.houBefore:
                    if (v.isSelected()) {
                        v.setSelected(false);
                        customNotificationBoolean = false;
                    } else {
                        turnOffCustomSelectors(v);
                        customTime = 60;
                    }
                    break;
                case R.id.dayBefore:
                    if (v.isSelected()) {
                        v.setSelected(false);
                        customNotificationBoolean = false;
                    } else {
                        turnOffCustomSelectors(v);
                        customTime = 24 * 60;
                    }
                    break;
                case R.id.weekBefore:
                    if (v.isSelected()) {
                        v.setSelected(false);
                        customNotificationBoolean = false;
                    } else {
                        turnOffCustomSelectors(v);
                        customTime = 7 * 24 * 60;
                    }
                    break;
                case R.id.asNotification:
                    if (v.isSelected()) {
                        v.setSelected(false);
                        asMethodNotification = false;
                    } else {
                        turnOffCustomMethodSelectors(v);
                    }
                    break;
                case R.id.asEmail:
                    if (v.isSelected()) {
                        v.setSelected(false);
                        asMethodNotification = false;
                    } else {
                        turnOffCustomMethodSelectors(v);
                    }
                    break;
            }
        }
    };
    View.OnClickListener customNotificationDialog = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dialogCustom = new AlertDialog.Builder(getActivity())
                    .setView(R.layout.dialog_custom_notifications)
                    .create();
            dialogCustom.show();

            timeCustomEditText = (EditText) dialogCustom.findViewById(R.id.timeCustomEditText);
            minuteBefore = (TextView) dialogCustom.findViewById(R.id.minuteBefore);
            hourBefore = (TextView) dialogCustom.findViewById(R.id.houBefore);
            dayBefore = (TextView) dialogCustom.findViewById(R.id.dayBefore);
            weekBefore = (TextView) dialogCustom.findViewById(R.id.weekBefore);
            asNotification = (TextView) dialogCustom.findViewById(R.id.asNotification);
            asEmail = (TextView) dialogCustom.findViewById(R.id.asEmail);
            doneCustomNotification = (TextView) dialogCustom.findViewById(R.id.doneCustomNotification);

            minuteBefore.setOnClickListener(getCustomNotificationDialogItems);
            hourBefore.setOnClickListener(getCustomNotificationDialogItems);
            dayBefore.setOnClickListener(getCustomNotificationDialogItems);
            weekBefore.setOnClickListener(getCustomNotificationDialogItems);
            asNotification.setOnClickListener(getCustomNotificationDialogItems);
            asEmail.setOnClickListener(getCustomNotificationDialogItems);

            minuteBefore.setSelected(true);
            customNotificationBoolean = true;
            asMethodNotification = true;
            titleOfCustomNotification = " " + minuteBefore.getText().toString().toLowerCase();
            customTime = 1;


            if (posNotificationList >= 0) {
                timeCustomEditText.setText(String.valueOf(notificationsList.get(posNotificationList).timeValue));
            }

            if (posNotificationList >= 0 && notificationsList.get(posNotificationList).asEmail.contains(asEmail.getText().toString())) {
                asEmail.setSelected(true);
            } else {
                asNotification.setSelected(true);
            }

            doneCustomNotification.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (customNotificationBoolean && asMethodNotification && timeCustomEditText.length() > 0) {
                        if (posNotificationList >= 0) {
                            int time = customTime * Integer.parseInt(timeCustomEditText.getText().toString());
                            if (!asEmail.isSelected()) {
                                notificationsList.get(posNotificationList).asEmail = "";
                            } else {
                                notificationsList.get(posNotificationList).asEmail = " " + asEmail.getText().toString();
                            }
                            notificationsList.get(posNotificationList).titleOfNotification = timeCustomEditText.getText().toString() + titleOfCustomNotification;
                            notificationsList.get(posNotificationList).timeValue = time;
                            notificationsList.get(posNotificationList).notificationType = 1;
                            notificationAdapter.notifyDataSetChanged();
                            listViewNotifications.setAdapter(notificationAdapter);
                            standardNotificationDialog.dismiss();
                            dialogCustom.dismiss();
                        } else {
                            int time = customTime * Integer.parseInt(timeCustomEditText.getText().toString());
                            if (!asEmail.isSelected()) {
                                addNotification("", timeCustomEditText.getText() + titleOfCustomNotification, time, 1);
                            } else {
                                addNotification(" " + asEmail.getText().toString(), timeCustomEditText.getText() + titleOfCustomNotification, time, 1);
                            }
                            dialogCustom.dismiss();
                        }
                    } else {
                        if (posNotificationList >= 0) {
                            removeNotification();
                            dialogCustom.dismiss();
                        } else {
                            standardNotificationDialog.dismiss();
                            dialogCustom.dismiss();
                        }
                    }

                }
            });


        }
    };
    private Long eventId;
    private int startHoursFormat, startView;
    private ContactsCompletionView completionView;
    private MyCalendar myCalendars[];
    private List<MyCalendar> temp = new ArrayList<>();
    private Cursor cursor;
    private Button dateStart, timeStart, dateEnd, timeEnd;
    private Calendar beginTime, endTime;
    private int yearStart, monthStart, dayStart, yearEnd, monthEnd, dayEnd;
    private int hourStart, minuteStart, hourEnd, minuteEnd;
    private DateFormat date, time;
    private SimpleDateFormat nameOfDay;
    private EditText eventTitle, description, location;
    private Switch allDaySwitch;
    View.OnClickListener addOnClickAllDay = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (allDaySwitch.isChecked()) {
                timeStart.setVisibility(View.INVISIBLE);
                timeEnd.setVisibility(View.INVISIBLE);
            } else {
                timeStart.setVisibility(View.VISIBLE);
                timeEnd.setVisibility(View.VISIBLE);
            }
        }
    };
    View.OnClickListener addOnClickDateStart = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DatePickerDialog dpd = new DatePickerDialog(getActivity(),
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            yearStart = year;
                            monthStart = monthOfYear;
                            dayStart = dayOfMonth;

                            Calendar calendar = Calendar.getInstance();
                            calendar.set(Calendar.YEAR, yearStart);
                            calendar.set(Calendar.MONTH, monthStart);
                            calendar.set(Calendar.DAY_OF_MONTH, dayStart);
                            dateStart.setText(String.valueOf(nameOfDay.format(calendar.getTime())) + " " + String.valueOf(date.format(calendar.getTime())));
                            if (allDaySwitch.isChecked()) {
                                dateEnd.setText(String.valueOf(nameOfDay.format(calendar.getTime())) + " " + String.valueOf(date.format(calendar.getTime())));
                            }
                        }
                    }, yearStart, monthStart, dayStart);
            dpd.show();
        }

    };
    View.OnClickListener addOnClickDateEnd = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!allDaySwitch.isChecked()) {
                DatePickerDialog dpd = new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                yearEnd = year;
                                monthEnd = monthOfYear;
                                dayEnd = dayOfMonth;

                                Calendar calendar = Calendar.getInstance();
                                calendar.set(Calendar.YEAR, yearEnd);
                                calendar.set(Calendar.MONTH, monthEnd);
                                calendar.set(Calendar.DAY_OF_MONTH, dayEnd);
                                dateEnd.setText(String.valueOf(nameOfDay.format(calendar.getTime())) + " " + String.valueOf(date.format(calendar.getTime())));
                            }
                        }, yearEnd, monthEnd, dayEnd);
                dpd.show();
            }
        }

    };
    private Boolean calendarType = false;
    View.OnClickListener addOnClickTimeStart = new View.OnClickListener() {
        @Override
        public void onClick(View v) {


            // Launch Time Picker Dialog
            TimePickerDialog tpd = new TimePickerDialog(getActivity(),
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {
                            // Display Selected time in textbox
                            hourStart = hourOfDay;
                            minuteStart = minute;

                            Calendar calendar = Calendar.getInstance();
                            calendar.set(Calendar.HOUR_OF_DAY, hourStart);
                            calendar.set(Calendar.MINUTE, minuteStart);
                            if (startHoursFormat > 0) {
                                timeStart.setText(String.valueOf(time.format(calendar.getTime())));
                            } else {
                                if (hourStart > 11) {
                                    calendar.add(Calendar.HOUR, -12);
                                    timeStart.setText(String.valueOf(time.format(calendar.getTime())) + " PM");
                                } else {
                                    timeStart.setText(String.valueOf(time.format(calendar.getTime())) + " AM");
                                }
                            }
                        }
                    }, hourStart, minuteStart, calendarType);
            tpd.show();
        }

    };
    View.OnClickListener addOnClickTimeEnd = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            // Launch Time Picker Dialog
            TimePickerDialog tpd = new TimePickerDialog(getActivity(),
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {
                            // Display Selected time in textbox
                            hourEnd = hourOfDay;
                            minuteEnd = minute;

                            Calendar calendar = Calendar.getInstance();
                            calendar.set(Calendar.HOUR_OF_DAY, hourEnd);
                            calendar.set(Calendar.MINUTE, minuteEnd);
                            if (startHoursFormat > 0) {
                                timeEnd.setText(String.valueOf(time.format(calendar.getTime())));
                            } else {
                                if (hourStart > 11) {
                                    calendar.add(Calendar.HOUR, -12);
                                    timeEnd.setText(String.valueOf(time.format(calendar.getTime())) + " PM");
                                } else {
                                    timeEnd.setText(String.valueOf(time.format(calendar.getTime())) + " AM");
                                }
                            }
                        }
                    }, hourEnd, minuteEnd, calendarType);
            tpd.show();
        }

    };
    private int select;
    private long calendarId;
    AdapterView.OnItemSelectedListener chooseCorrectCalendar = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> p_parent, View p_view,
                                   int p_pos, long p_id) {
            String mySelectedCalendarId = myCalendars[(int) p_id].id;
            calendarId = Integer.parseInt(mySelectedCalendarId);
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };
    private TextView recurrence;
    private String recurrenceRule;
    View.OnClickListener addOnClickSaveNewEvent = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            beginTime.set(yearStart, monthStart, dayStart, hourStart, minuteStart);
            long startMillis = beginTime.getTimeInMillis();
            endTime.set(yearEnd, monthEnd, dayEnd, hourEnd, minuteEnd);
            long endMillis = endTime.getTimeInMillis();

            String getEvent = eventTitle.getText().toString();
            String getLocation = location.getText().toString();
            String getDescription = description.getText().toString();
            if (allDaySwitch.isChecked()) {
                Calendar time = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                time.setTimeInMillis(startMillis);
                setToMidnight(time);
                startMillis = time.getTimeInMillis();
                endMillis = startMillis;
            }

            updateEvent(eventId, calendarId, startMillis, endMillis, getEvent, allDaySwitch.isChecked(), getLocation, getDescription, recurrenceRule);
            if (!notificationsList.isEmpty()) {
                addNotifications(eventId);
            }
            if (completionView.getObjects() != null) {
                addAttendees(eventId);
            }

            ((MainUse) getContext()).selectItem2(startView, 1);
            ((MainUse) getContext()).hideKeyboardFrom(getActivity());

        }
    };
    View.OnClickListener recurrenceDialog = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final RecurrencePickerDialog recurrencePickerDialog = new RecurrencePickerDialog();

            if (recurrenceRule != null && recurrenceRule.length() > 0) {
                Bundle bundle = new Bundle();
                bundle.putString(RecurrencePickerDialog.BUNDLE_RRULE, recurrenceRule);
                recurrencePickerDialog.setArguments(bundle);
            }

            recurrencePickerDialog.setOnRecurrenceSetListener(new RecurrencePickerDialog.OnRecurrenceSetListener() {
                @Override
                public void onRecurrenceSet(String rrule) {
                    recurrenceRule = rrule;

                    if (recurrenceRule != null && recurrenceRule.length() > 0) {
                        EventRecurrence recurrenceEvent = new EventRecurrence();

                        recurrenceEvent.setStartDate(new Time("" + new Date().getTime()));
                        recurrenceEvent.parse(rrule);
                        String srt = EventRecurrenceFormatter.getRepeatString(getContext(), getResources(), recurrenceEvent, true);
                        recurrence.setText(srt);
                    } else {
                        recurrence.setText(R.string.recurrence);
                    }
                }
            });
            recurrencePickerDialog.show(getActivity().getSupportFragmentManager(), "recurrencePicker");
        }
    };

    public void addAttendees(long eventId) {
        ContentResolver cr = getActivity().getContentResolver();

        CalendarProvider provider = new CalendarProvider(getContext());
        Data<Attendee> attendees = provider.getAttendees(eventId);
        Cursor cursor = attendees.getCursor();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    long attendeeId = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Attendees._ID));
                    Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Attendees.CONTENT_URI, attendeeId);
                    cr.delete(deleteUri, null, null);
                } while (cursor.moveToNext());
            }
        }

        for (Person person : completionView.getObjects()) {
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Attendees.ATTENDEE_EMAIL, person.getEmail());
            values.put(CalendarContract.Attendees.EVENT_ID, eventId);
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Uri uri = cr.insert(CalendarContract.Attendees.CONTENT_URI, values);
        }
    }

    public void addNotifications(long eventID) {
        ContentResolver cr = getActivity().getContentResolver();

        ((MainUse) getContext()).turnOff(eventID);
        for (NotificationObject notificationObject : notificationsList) {
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Reminders.MINUTES, notificationObject.timeValue);
            values.put(CalendarContract.Reminders.EVENT_ID, eventID);
            if (!(notificationObject.asEmail.length() > 2)) {
                values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            } else {
                values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_EMAIL);
            }
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Uri uri = cr.insert(CalendarContract.Reminders.CONTENT_URI, values);
            Intent intent = new Intent(getContext(), CalendarChangedReceiver.class);
            intent.putExtra("Event_id", String.valueOf(eventID));
            intent.setAction(CalendarChangedReceiver.TAG);
            getContext().sendBroadcast(intent);
        }
    }

    public void buildStandardDialog() {
        standardNotificationDialog = new AlertDialog.Builder(getActivity())
                .setView(R.layout.dialog_standard_notifications)
                .create();
        standardNotificationDialog.show();

        noNotification = (TextView) standardNotificationDialog.findViewById(R.id.noNotifications);
        tenMinutesBefore = (TextView) standardNotificationDialog.findViewById(R.id.tenMinutesBefore);
        fifTeenMinutesBefore = (TextView) standardNotificationDialog.findViewById(R.id.fifTeenMinutesBefore);
        thirtyMinutesBefore = (TextView) standardNotificationDialog.findViewById(R.id.thirtyMinutesBefore);
        sixtyMinutesBefore = (TextView) standardNotificationDialog.findViewById(R.id.sixtyMinutesBefore);
        custom = (TextView) standardNotificationDialog.findViewById(R.id.customTime);

        noNotification.setOnClickListener(getStandardNotificationDialogItems);
        tenMinutesBefore.setOnClickListener(getStandardNotificationDialogItems);
        fifTeenMinutesBefore.setOnClickListener(getStandardNotificationDialogItems);
        thirtyMinutesBefore.setOnClickListener(getStandardNotificationDialogItems);
        sixtyMinutesBefore.setOnClickListener(getStandardNotificationDialogItems);
        custom.setOnClickListener(customNotificationDialog);
    }

    public void turnOffCustomSelectors(View v) {
        minuteBefore.setSelected(false);
        hourBefore.setSelected(false);
        dayBefore.setSelected(false);
        weekBefore.setSelected(false);
        v.setSelected(true);
    }

    public void turnOffCustomMethodSelectors(View v) {
        asNotification.setSelected(false);
        asEmail.setSelected(false);
        v.setSelected(true);
        asMethodNotification = true;
    }

    public void removeNotification() {
        if (notificationsList.size() > 0) {
            notificationsList.remove(posNotificationList);
            setSizeOfNotificationListView();
            notificationAdapter.notifyDataSetChanged();
            standardNotificationDialog.dismiss();
        }
        if (notificationsList.size() == 4) {
            RelativeLayout row = (RelativeLayout) rootView.findViewById(R.id.addNotificationRow);
            row.setVisibility(View.VISIBLE);
        }
    }

    public void addNotification(String asEmailTitle, String title, int time, int type) {
        notificationsList.add(new NotificationObject(asEmailTitle, title, time, type));
        setSizeOfNotificationListView();
        notificationAdapter.notifyDataSetChanged();
        if (standardNotificationDialog != null) {
            standardNotificationDialog.dismiss();
        }
        if (notificationsList.size() == 5) {
            RelativeLayout row = (RelativeLayout) rootView.findViewById(R.id.addNotificationRow);
            row.setVisibility(View.GONE);
        }
    }

    public void setSizeOfNotificationListView() {
        ViewGroup.LayoutParams params = listViewNotifications.getLayoutParams();
        int size = notificationsList.size();
        float scale = getResources().getDisplayMetrics().density;
        int height5 = (int) (size * 44);
        params.height = (int) (height5 * scale);
        listViewNotifications.setLayoutParams(params);
    }

    public void turnOffStandardSelectors(View v) {
        noNotification.setSelected(false);
        tenMinutesBefore.setSelected(false);
        fifTeenMinutesBefore.setSelected(false);
        thirtyMinutesBefore.setSelected(false);
        sixtyMinutesBefore.setSelected(false);
        v.setSelected(true);
        customNotificationBoolean = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_create_event, container, false);
//        ((MainUse)getContext()).createAdView(rootView);
//        AdView adView = (AdView) rootView.findViewById(R.id.adView);
//        View adViewSpace = (View) rootView.findViewById(R.id.space);
//        ((MainUse) getContext()).createAdViewKey(rootView, adView, adViewSpace);

        setHasOptionsMenu(true);


        loadData();
        if (startHoursFormat > 0) {
            calendarType = true;
        } else {
            calendarType = false;
        }


        Bundle bundle = this.getArguments();
        if (bundle != null) {
            eventId = bundle.getLong("Event_id", 0);
        }

        CalendarProvider provider = new CalendarProvider(getContext());
        Event event = provider.getEvent(eventId);
        Data<Reminder> reminders = provider.getReminders(eventId);
        Data<Attendee> attendees = provider.getAttendees(eventId);

        listViewNotifications = (ListView) rootView.findViewById(R.id.listViewNotification);
        notificationAdapter = new ArrayAdapter(getActivity(), R.layout.list_view_notifications, notificationsList);
        listViewNotifications.setAdapter(notificationAdapter);
        listViewNotifications.setOnItemClickListener(notificationEdit);


        try {

            if (event.calendarId != 0) {
                calendarId = event.calendarId;
            } else {
                calendarId = 0;
            }

            String[] projection = new String[]{"_id", Calendars.IS_PRIMARY, Calendars.CALENDAR_DISPLAY_NAME, Calendars.CALENDAR_ACCESS_LEVEL};
            String selection = CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL + "=?";
            String[] selectionArgs = new String[]{"700"};
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return rootView;
            }
            cursor = getContext().getContentResolver().query(Calendars.CONTENT_URI, projection, selection, selectionArgs, null);
            if (cursor.moveToFirst()) {
                do {
                    int CALENDAR_ACCESS_LEVEL = cursor.getInt(cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL));
                    String calName = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME));
                    String calId = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
                    if (CALENDAR_ACCESS_LEVEL == 700) {
                        temp.add(new MyCalendar(calName, calId));
                    }
                }
                while (cursor.moveToNext());

            }
        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(getActivity(), "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }
        int size = temp.size();
        myCalendars = new MyCalendar[size];
        temp.toArray(myCalendars);

        ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(), R.layout.spinner_item, myCalendars);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        date = new SimpleDateFormat("dd MMM yyyy");
        time = new SimpleDateFormat("HH:mm");
        nameOfDay = new SimpleDateFormat("EEE");

        Calendar calendarStart = Calendar.getInstance();
        Calendar calendarEnd = Calendar.getInstance();
        calendarStart.setTimeInMillis(event.dTStart);
        if ((event.dTend - event.dTStart) <= 0) {
            int durationNum = Integer.parseInt(event.duration.replaceAll("[^\\d.]", ""));
            calendarEnd.setTimeInMillis(event.dTStart + (durationNum * 1000));
        } else {
            calendarEnd.setTimeInMillis(event.dTend);
        }
        beginTime = (Calendar) calendarStart.clone();
        endTime = (Calendar) calendarStart.clone();
        yearStart = calendarStart.get(Calendar.YEAR);
        monthStart = calendarStart.get(Calendar.MONTH);
        dayStart = calendarStart.get(Calendar.DAY_OF_MONTH);
        hourStart = calendarStart.get(Calendar.HOUR_OF_DAY);
        minuteStart = calendarStart.get(Calendar.MINUTE);
        yearEnd = calendarEnd.get(Calendar.YEAR);
        monthEnd = calendarEnd.get(Calendar.MONTH);
        dayEnd = calendarEnd.get(Calendar.DAY_OF_MONTH);
        hourEnd = calendarEnd.get(Calendar.HOUR_OF_DAY);
        minuteEnd = calendarEnd.get(Calendar.MINUTE);


        eventTitle = (EditText) rootView.findViewById(R.id.createEvent);
        eventTitle.setText(event.title);

        dateStart = (Button) rootView.findViewById(R.id.datePickerStart);
        timeStart = (Button) rootView.findViewById(R.id.timePickerStart);
        dateStart.setTransformationMethod(null);
        timeStart.setTransformationMethod(null);
        dateStart.setText(String.valueOf(nameOfDay.format(calendarStart.getTime())) + " " + String.valueOf(date.format(calendarStart.getTime())));
        timeStart.setText(String.valueOf(time.format(calendarStart.getTime())));
        dateStart.setOnClickListener(addOnClickDateStart);
        timeStart.setOnClickListener(addOnClickTimeStart);

        dateEnd = (Button) rootView.findViewById(R.id.datePickerEnd);
        timeEnd = (Button) rootView.findViewById(R.id.timePickerEnd);
        dateEnd.setTransformationMethod(null);
        timeEnd.setTransformationMethod(null);
        dateEnd.setText("" + String.valueOf(nameOfDay.format(calendarEnd.getTime())) + " " + String.valueOf(date.format(calendarEnd.getTime())));
        timeEnd.setText(String.valueOf(time.format(calendarEnd.getTime())));
        dateEnd.setOnClickListener(addOnClickDateEnd);
        timeEnd.setOnClickListener(addOnClickTimeEnd);


        if (startHoursFormat > 0) {
            timeStart.setText(String.valueOf(time.format(calendarStart.getTime())));
            timeEnd.setText(String.valueOf(time.format(calendarEnd.getTime())));
        } else {
            if (hourStart > 11) {
                calendarStart.add(Calendar.HOUR, -12);
                timeStart.setText(String.valueOf(time.format(calendarStart.getTime())) + " PM");
            } else {
                timeStart.setText(String.valueOf(time.format(calendarStart.getTime())) + " AM");
            }
            if (hourEnd > 11) {
                calendarEnd.add(Calendar.HOUR, -12);
                timeEnd.setText(String.valueOf(time.format(calendarEnd.getTime())) + " PM");
            } else {
                timeEnd.setText(String.valueOf(time.format(calendarEnd.getTime())) + " AM");
            }
        }


        allDaySwitch = (Switch) rootView.findViewById(R.id.allDaySwitch);
        allDaySwitch.setOnClickListener(addOnClickAllDay);
        if (event.allDay) {
            allDaySwitch.setChecked(true);
            allDaySwitch.callOnClick();
        } else {
            allDaySwitch.setChecked(false);
            allDaySwitch.callOnClick();
        }


        Spinner chooseCalendar = (Spinner) rootView.findViewById(R.id.chooseCalendar);
        chooseCalendar.setAdapter(arrayAdapter);
        int calendarListSize = myCalendars.length;
        int i = 0;
        while (calendarListSize > i) {
            if (calendarId == Integer.parseInt(myCalendars[i].id)) {
                select = i;
                break;
            }
            i++;
        }
        chooseCalendar.setSelection(select);
        chooseCalendar.setOnItemSelectedListener(chooseCorrectCalendar);


        listViewCreate = (ListView) rootView.findViewById(R.id.listViewNotification);
        addNotification = (TextView) rootView.findViewById(R.id.addNotification);
        addNotification.setOnClickListener(standardNotification);


        String minutesBefore = getResources().getString(R.string.minutesBefore).toLowerCase();
        for (Reminder reminder : reminders.getList()) {
            addNotification("", reminder.minutes + " " + minutesBefore, reminder.minutes, 0);
        }

        recurrence = (TextView) rootView.findViewById(R.id.recurrence);
        recurrence.setOnClickListener(recurrenceDialog);
        recurrenceRule = event.rRule;
        if (recurrenceRule != null && recurrenceRule.length() > 0) {
            EventRecurrence recurrenceEvent = new EventRecurrence();
            recurrenceEvent.setStartDate(new Time("" + new Date().getTime()));
            recurrenceEvent.parse(recurrenceRule);
            String srt = EventRecurrenceFormatter.getRepeatString(getContext(), getResources(), recurrenceEvent, true);
            recurrence.setText(srt);
        } else {
            recurrence.setText(R.string.recurrence);
        }

        Person[] people = new Person[]{};
        ArrayAdapter<Person> adapter = new FilteredArrayAdapter<Person>(getActivity(), R.layout.person_layout, people) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {

                    LayoutInflater l = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                    convertView = l.inflate(R.layout.person_layout, parent, false);
                }

                Person p = getItem(position);
                ((TextView) convertView.findViewById(R.id.name)).setText(p.getName());
                ((TextView) convertView.findViewById(R.id.email)).setText(p.getEmail());

                return convertView;
            }

            @Override
            protected boolean keepObject(Person person, String mask) {
                mask = mask.toLowerCase();
                return person.getName().toLowerCase().startsWith(mask) || person.getEmail().toLowerCase().startsWith(mask);
            }
        };

        completionView = (ContactsCompletionView) rootView.findViewById(R.id.searchView);
        completionView.setAdapter(adapter);
        completionView.setTokenListener(this);
        completionView.setTokenClickStyle(TokenCompleteTextView.TokenClickStyle.Select);

        Cursor cursor = attendees.getCursor();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                people = new Person[cursor.getCount()];
                int attendeeId = 0;
                do {
                    people[attendeeId] = new Person("", cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Attendees.ATTENDEE_EMAIL)));
                    completionView.addObject(people[attendeeId]);
                    attendeeId++;
                } while (cursor.moveToNext());
            }
        }

        location = (EditText) rootView.findViewById(R.id.locationPicker);
        location.setOnLongClickListener(goToLocation);
        location.setText(event.eventLocation);

        description = (EditText) rootView.findViewById(R.id.createEventDescription);
        description.setText(event.description);

        TextView saveNewEvent = (TextView) rootView.findViewById(R.id.saveNewEvent);
        saveNewEvent.setTransformationMethod(null);
        saveNewEvent.setOnClickListener(addOnClickSaveNewEvent);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.search).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void updateEvent(long eventId, long calID, long startMillis, long endMillis, String title, Boolean allDay, String location, String description, String recurrenceRule) {

        TimeZone tz = TimeZone.getDefault();
        ContentResolver cr = getContext().getContentResolver();
        ContentValues values = new ContentValues();
        values.put(Events.DTSTART, startMillis);

        if (recurrenceRule != null) {
            long duration = (endMillis - startMillis) / 1000;
            if (duration <= 0) {
                values.put(Events.DURATION, "P3600S");
                values.put(Events.DTEND, "");
            } else {
                values.put(Events.DURATION, "P" + duration + "S");
                values.put(Events.DTEND, "");
            }
        } else {
            values.put(Events.DURATION, "");
            values.put(Events.DTEND, endMillis);
        }
        values.put(Events.TITLE, title);
        values.put(Events.ALL_DAY, allDay);
        values.put(Events.EVENT_LOCATION, location);
        values.put(Events.DESCRIPTION, description);
        values.put(Events.CALENDAR_ID, calID);
        values.put(Events.EVENT_TIMEZONE, tz.getID());

        values.put(Events.RRULE, recurrenceRule);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        String selection;
        String[] selectionArgs;
        selection = "_id=?";
        selectionArgs = new String[]{String.valueOf(eventId)};
        cr.update(Events.CONTENT_URI, values, selection, selectionArgs);
    }

    private void updateTokenConfirmation() {
        StringBuilder sb = new StringBuilder("Current tokens:\n");
        for (Object token : completionView.getObjects()) {
            sb.append(token.toString());
            sb.append("\n");
        }
//
//        ((TextView)findViewById(R.id.tokens)).setText(sb);
    }

    private void setToMidnight(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void loadData() {
        try {
            SQLiteOpenHelper mainDatabase = new MainDatabaseHelper(getContext());
            SQLiteDatabase db = mainDatabase.getReadableDatabase();
            cursor = db.query(MainDatabaseContract.Settings.TABLE_NAME, MainDatabaseContract.Settings.projection, null, null, null, null, null);

            if (cursor.moveToFirst()) {
                startHoursFormat = cursor.getInt(cursor.getColumnIndexOrThrow(MainDatabaseContract.Settings.HOURS_FORMAT));
                startView = cursor.getInt(cursor.getColumnIndexOrThrow(MainDatabaseContract.Settings.START_VIEW));
            }

        } catch (SQLiteException e) {
            Toast.makeText(getContext(), "Database unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTokenAdded(Person token) {
//        ((TextView)findViewById(R.id.lastEvent)).setText("Added: " + token);
        updateTokenConfirmation();
    }

    @Override
    public void onTokenRemoved(Person token) {
//        ((TextView)findViewById(R.id.lastEvent)).setText("Removed: " + token);
        updateTokenConfirmation();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cursor != null) {
            cursor.close();
        }
    }
}
