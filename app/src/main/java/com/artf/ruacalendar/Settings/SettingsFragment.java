package com.artf.ruacalendar.Settings;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.artf.ruacalendar.Database.MainDatabaseContract;
import com.artf.ruacalendar.Database.MainDatabaseHelper;
import com.artf.ruacalendar.Notifications.OnBootNotificationReceiver;
import com.artf.ruacalendar.R;

/**
 * Created by ART_F on 2017-01-15.
 */

public class SettingsFragment extends Fragment {

    View.OnClickListener syncOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Fragment fragment = new SettingsSyncFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment, "visible_fragment");
            ft.addToBackStack(null);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
        }
    };
    View.OnClickListener goToMail = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String email = "mailto:ruacalendarpro@gmail.com";

            Uri addressUri = Uri.parse(email);
            Intent intent = new Intent(Intent.ACTION_SENDTO, addressUri);

            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
        }
    };
    public RadioButton radioButton;
    private int startView, startViewId, startLoad, startHoursFormat, startNotification;
    private TextView startOfweekDaySelected;
    private TextView startWithSelected;
    private Switch hoursFormat, noitficationsSwitch;
    private int startDay, startDayId;
    private long rowId;
    View.OnClickListener setNotifications = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (noitficationsSwitch.isChecked()) {
                startNotification = 1;
                Intent intent = new Intent();
                intent.setAction(OnBootNotificationReceiver.NOTIFICATIONS_TURN_ON);
                getContext().sendBroadcast(intent);
            } else {
                Intent intent = new Intent();
                intent.setAction(OnBootNotificationReceiver.NOTIFICATIONS_TURN_OFF);
                getContext().sendBroadcast(intent);
                startNotification = 0;
            }
            updateNotifications(startNotification);
        }
    };
    View.OnClickListener setHoursFormat = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (hoursFormat.isChecked()) {
                startHoursFormat = 1;
            } else {
                startHoursFormat = 0;
            }
            updateHoursFormat(startHoursFormat);
        }
    };
    View.OnClickListener startWithOptions = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setView(R.layout.dialog_start_with)
                    .create();
            dialog.show();

            RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.startWithOption);
            radioButton = (RadioButton) dialog.findViewById(startViewId);
            radioButton.setChecked(true);
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                    startViewId = checkedId;
                    radioButton = (RadioButton) dialog.findViewById(checkedId);
                    switch (checkedId) {
                        case R.id.startWithOption1:
                            startLoad = 0;
                            startView = 1;
                            break;
                        case R.id.startWithOption2:
                            startLoad = 22;
                            startView = 2;
                            break;
                        case R.id.startWithOption3:
                            startLoad = 22;
                            startView = 3;
                            break;
                        case R.id.startWithOption4:
                            startLoad = 22;
                            startView = 4;
                            break;
                        case R.id.startWithOption5:
                            startLoad = 33;
                            startView = 5;
                            break;
                    }
                    radioButton.setChecked(true);
                    startWithSelected.setText(" " + radioButton.getText());
                    updateType(startLoad, startViewId, startView);
                    dialog.dismiss();
                }
            });
        }
    };
    View.OnClickListener setStartOfTheWeek = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setView(R.layout.dialog_start_week_day)
                    .create();
            dialog.show();

            RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.startOfweekDay);
            radioButton = (RadioButton) dialog.findViewById(startDayId);
            radioButton.setChecked(true);
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                    startDayId = checkedId;
                    radioButton = (RadioButton) dialog.findViewById(checkedId);
                    switch (checkedId) {
                        case R.id.startOfweekDay1:
                            startDay = 2;
                            break;
                        case R.id.startOfweekDay2:
                            startDay = 3;
                            break;
                        case R.id.startOfweekDay3:
                            startDay = 4;
                            break;
                        case R.id.startOfweekDay4:
                            startDay = 5;
                            break;
                        case R.id.startOfweekDay5:
                            startDay = 6;
                            break;
                        case R.id.startOfweekDay6:
                            startDay = 7;
                            break;
                        case R.id.startOfweekDay7:
                            startDay = 1;
                            break;
                    }
                    radioButton.setChecked(true);
                    startOfweekDaySelected.setText(" " + radioButton.getText());
                    updateDay(startDayId, startDay);
                    dialog.dismiss();
                }
            });
        }
    };

    private void updateNotifications(int startNotification) {
        SQLiteOpenHelper mainDatabase = new MainDatabaseHelper(getActivity());
        SQLiteDatabase db = mainDatabase.getWritableDatabase();
        ContentValues mainValues = new ContentValues();
        mainValues.put(MainDatabaseContract.Settings.NOTIFICATIONS, startNotification);
        db.update(MainDatabaseContract.Settings.TABLE_NAME, mainValues, MainDatabaseContract.Settings._ID + "=" + rowId, null);
        db.close();
    }

    private void updateHoursFormat(int startHoursFormat) {
        SQLiteOpenHelper mainDatabase = new MainDatabaseHelper(getActivity());
        SQLiteDatabase db = mainDatabase.getWritableDatabase();
        ContentValues mainValues = new ContentValues();
        mainValues.put(MainDatabaseContract.Settings.HOURS_FORMAT, startHoursFormat);
        db.update(MainDatabaseContract.Settings.TABLE_NAME, mainValues, MainDatabaseContract.Settings._ID + "=" + rowId, null);
        db.close();
    }

    private void updateType(int startLoad, int startTypeId, int startType) {
        SQLiteOpenHelper mainDatabase = new MainDatabaseHelper(getActivity());
        SQLiteDatabase db = mainDatabase.getWritableDatabase();
        ContentValues mainValues = new ContentValues();
        mainValues.put(MainDatabaseContract.Settings.START_LOAD, startLoad);
        mainValues.put(MainDatabaseContract.Settings.START_VIEW_ID, startTypeId);
        mainValues.put(MainDatabaseContract.Settings.START_VIEW, startType);
        db.update(MainDatabaseContract.Settings.TABLE_NAME, mainValues, MainDatabaseContract.Settings._ID + "=" + rowId, null);
        db.close();
    }

    private void updateDay(int startDayId, int startDay) {
        SQLiteOpenHelper mainDatabase = new MainDatabaseHelper(getActivity());
        SQLiteDatabase db = mainDatabase.getWritableDatabase();
        ContentValues mainValues = new ContentValues();
        mainValues.put(MainDatabaseContract.Settings.START_DAY_ID, startDayId);
        mainValues.put(MainDatabaseContract.Settings.START_DAY, startDay);
        db.update(MainDatabaseContract.Settings.TABLE_NAME, mainValues, MainDatabaseContract.Settings._ID + "=" + rowId, null);
        db.close();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        setHasOptionsMenu(true);

        loadData();

        LinearLayout startWith = (LinearLayout) rootView.findViewById(R.id.startWith);
        startWithSelected = (TextView) rootView.findViewById(R.id.startWithSelected);
        startWith.setOnClickListener(startWithOptions);

        View layout = inflater.inflate(R.layout.dialog_start_with, container, false);
        radioButton = (RadioButton) layout.findViewById(startViewId);
        if(radioButton != null) {
            startWithSelected.setText(" " + radioButton.getText());
        }else{
            radioButton = (RadioButton) layout.findViewById(R.id.startWithOption1);
            startViewId = radioButton.getId();
            startWithSelected.setText(" " + radioButton.getText());
        }

        TextView sync = (TextView) rootView.findViewById(R.id.sync);
        sync.setTransformationMethod(null);
        sync.setOnClickListener(syncOnClick);

        hoursFormat = (Switch) rootView.findViewById(R.id.hoursFormat);
        hoursFormat.setOnClickListener(setHoursFormat);
        if (startHoursFormat > 0) {
            hoursFormat.setChecked(true);
        } else {
            hoursFormat.setChecked(false);
        }

        noitficationsSwitch = (Switch) rootView.findViewById(R.id.notificationsSwitch);
        noitficationsSwitch.setOnClickListener(setNotifications);
        if (startNotification > 0) {
            noitficationsSwitch.setChecked(true);
        } else {
            noitficationsSwitch.setChecked(false);
        }

        LinearLayout startOfTheWeek = (LinearLayout) rootView.findViewById(R.id.startOfTheWeek);
        startOfweekDaySelected = (TextView) rootView.findViewById(R.id.startOfweekDaySelected);
        startOfTheWeek.setOnClickListener(setStartOfTheWeek);

        layout = inflater.inflate(R.layout.dialog_start_week_day, container, false);
        radioButton = (RadioButton) layout.findViewById(startDayId);
        if(radioButton != null) {
            startOfweekDaySelected.setText(" " + radioButton.getText());
        }else{
            radioButton = (RadioButton) layout.findViewById(R.id.startOfweekDay1);
            startDayId = radioButton.getId();
            startOfweekDaySelected.setText(" " + radioButton.getText());
        }

        TextView contactUs = (TextView) rootView.findViewById(R.id.contactUs);
        contactUs.setTransformationMethod(null);
        contactUs.setOnClickListener(goToMail);


        return rootView;
    }

    private void loadData() {
        Cursor cursor = null;
        try {
            SQLiteOpenHelper mainDatabase = new MainDatabaseHelper(getContext());
            SQLiteDatabase db = mainDatabase.getReadableDatabase();
            cursor = db.query(MainDatabaseContract.Settings.TABLE_NAME, MainDatabaseContract.Settings.projection, null, null, null, null, null);

            if (cursor.moveToFirst()) {
                rowId = cursor.getLong(cursor.getColumnIndexOrThrow(MainDatabaseContract.Settings._ID));
                startLoad = cursor.getInt(cursor.getColumnIndexOrThrow(MainDatabaseContract.Settings.START_LOAD));
                startViewId = cursor.getInt(cursor.getColumnIndexOrThrow(MainDatabaseContract.Settings.START_VIEW_ID));
                startView = cursor.getInt(cursor.getColumnIndexOrThrow(MainDatabaseContract.Settings.START_VIEW));
                startHoursFormat = cursor.getInt(cursor.getColumnIndexOrThrow(MainDatabaseContract.Settings.HOURS_FORMAT));
                startNotification = cursor.getInt(cursor.getColumnIndexOrThrow(MainDatabaseContract.Settings.NOTIFICATIONS));
                startDayId = cursor.getInt(cursor.getColumnIndexOrThrow(MainDatabaseContract.Settings.START_DAY_ID));
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.search).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }


}
