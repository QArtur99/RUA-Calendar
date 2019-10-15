package com.artf.ruacalendar.Notebook;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.artf.ruacalendar.Database.MainDatabaseContract.Notes;
import com.artf.ruacalendar.Database.MainDatabaseHelper;
import com.artf.ruacalendar.MainUse;
import com.artf.ruacalendar.R;

import java.util.Calendar;

/**
 * Created by ART_F on 2016-12-27.
 */

public class CreateNoteFragment extends Fragment {

    private EditText createNote;
    View.OnClickListener saveOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String note = String.valueOf(createNote.getText());
            insertNote(note);

            Fragment fragment = new NotebookFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment, "visible_fragment");
            ft.addToBackStack(null);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
            MainUse.hideKeyboardFrom(getActivity());
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_note, container, false);
        setHasOptionsMenu(true);

        createNote = (EditText) rootView.findViewById(R.id.enterNote);

        TextView saveNewNote = (TextView) rootView.findViewById(R.id.saveNewNote);
        saveNewNote.setTransformationMethod(null);
        saveNewNote.setOnClickListener(saveOnClick);

        return rootView;
    }

    public void insertNote(String note) {
        Calendar calendar = Calendar.getInstance();
        SQLiteOpenHelper mainDatabase = new MainDatabaseHelper(getActivity());
        SQLiteDatabase db = mainDatabase.getWritableDatabase();
        ContentValues taskValues = new ContentValues();
        taskValues.put(Notes.NOTE, note);
        taskValues.put(Notes.TIME_IN_MILLIS, calendar.getTimeInMillis());
        db.insert(Notes.TABLE_NAME, null, taskValues);
        db.close();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.search).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

}
