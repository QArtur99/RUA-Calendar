package com.artf.ruacalendar.Notebook;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.artf.ruacalendar.Database.MainDatabaseContract;
import com.artf.ruacalendar.Database.MainDatabaseHelper;
import com.artf.ruacalendar.R;


/**
 * Created by ART_F on 2016-12-27.
 */

public class NotebookFragment extends Fragment {


    View.OnClickListener addOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Fragment fragment = new CreateNoteFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment, "visible_fragment");
            ft.addToBackStack(null);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
        }
    };
    int startHoursFormat;
    private Cursor cursor;
    private SQLiteDatabase db;
    private SQLiteOpenHelper mainDatabase;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_notebook, container, false);
        setHasOptionsMenu(true);
        loadData();

        ListView listView = (ListView) rootView.findViewById(R.id.listView);
        TextView addNote = (TextView) rootView.findViewById(R.id.addNote);
        addNote.setTransformationMethod(null);
        addNote.setOnClickListener(addOnClick);

        loadNotes(listView);

        return rootView;
    }

    private void loadNotes(ListView listView) {
        try {
            mainDatabase = new MainDatabaseHelper(getActivity());
            db = mainDatabase.getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM NOTEBOOK", null);

            AdapterNotebook listAdapter = new AdapterNotebook(getActivity(),
                    cursor,
                    startHoursFormat
            );

            listView.setAdapter(listAdapter);
        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(getActivity(), "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void loadData() {
        try {
            mainDatabase = new MainDatabaseHelper(getContext());
            db = mainDatabase.getReadableDatabase();
            cursor = db.query(MainDatabaseContract.Settings.TABLE_NAME, MainDatabaseContract.Settings.projection, null, null, null, null, null);

            if (cursor.moveToFirst()) {
                startHoursFormat = cursor.getInt(cursor.getColumnIndexOrThrow(MainDatabaseContract.Settings.HOURS_FORMAT));
            }

        } catch (SQLiteException e) {
            Toast.makeText(getContext(), "Database unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.search).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }
}
