package com.artf.ruacalendar.Settings;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.provider.CalendarContract.Calendars;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.artf.ruacalendar.R;


/**
 * Created by ART_F on 2017-01-19.
 */

public class SettingsSyncFragment extends Fragment {
    private Cursor cursor;
    private ListView sync;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings_sync, container, false);
        setHasOptionsMenu(true);

        sync = (ListView) rootView.findViewById(R.id.syncListView);
        setSync();
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.search).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setSync() {
        try {
            String[] projection = new String[]{Calendars._ID, Calendars.CALENDAR_DISPLAY_NAME, Calendars.SYNC_EVENTS};
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
            cursor = getActivity().getContentResolver().query(Calendars.CONTENT_URI, projection, null, null, null);    //all calendars
            AdapterSync listAdapter = new AdapterSync(getActivity(),
                    cursor
            );
            sync.setAdapter(listAdapter);

        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(getActivity(), "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
