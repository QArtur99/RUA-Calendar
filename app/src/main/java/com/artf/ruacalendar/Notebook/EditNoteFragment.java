package com.artf.ruacalendar.Notebook;

import android.content.ContentValues;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.artf.ruacalendar.Database.MainDatabaseContract;
import com.artf.ruacalendar.Database.MainDatabaseHelper;
import com.artf.ruacalendar.MainUse;
import com.artf.ruacalendar.R;

/**
 * Created by ART_F on 2016-12-28.
 */

public class EditNoteFragment extends Fragment {

    View.OnClickListener cancelOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Fragment fragment = new NotebookFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment, "visible_fragment");
            ft.addToBackStack(null);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
        }
    };
    private SQLiteDatabase db;
    private long id;
    private EditText createNote;
    private String note;
    private Cursor cursor;
    private SQLiteOpenHelper mainDatabase;
    View.OnClickListener saveOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String note = String.valueOf(createNote.getText());
            updateNote(note);

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
        View rootView = inflater.inflate(R.layout.fragment_edit_note, container, false);
        setHasOptionsMenu(true);

        Bundle bundle = this.getArguments();
        id = bundle.getInt(MainDatabaseContract.Notes._ID, 0);
        try {
            mainDatabase = new MainDatabaseHelper(getActivity());
            db = mainDatabase.getReadableDatabase();
            cursor = db.rawQuery("SELECT " + MainDatabaseContract.Notes.NOTE + " FROM NOTEBOOK WHERE " + MainDatabaseContract.Notes._ID + " = " + id, null);

        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(getActivity(), "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }
        if (cursor.moveToFirst()) {
            note = cursor.getString(cursor.getColumnIndexOrThrow(MainDatabaseContract.Notes.NOTE));
        }

        createNote = (EditText) rootView.findViewById(R.id.editNote);
        createNote.setText(note);

        TextView saveNewNote = (TextView) rootView.findViewById(R.id.saveEditNote);
        saveNewNote.setTransformationMethod(null);
        saveNewNote.setOnClickListener(saveOnClick);

        TextView cancelNewNote = (TextView) rootView.findViewById(R.id.cancelEditNote);
        cancelNewNote.setTransformationMethod(null);
        cancelNewNote.setOnClickListener(cancelOnClick);


        return rootView;

    }

    public void updateNote(String note) {
        mainDatabase = new MainDatabaseHelper(getActivity());
        db = mainDatabase.getWritableDatabase();
        ContentValues taskValues = new ContentValues();
        taskValues.put(MainDatabaseContract.Notes.NOTE, note);
        db.update(MainDatabaseContract.Notes.TABLE_NAME, taskValues, MainDatabaseContract.Notes._ID + "=" + id, null);
        db.close();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.search).setVisible(false);
        MenuItem a = menu.add("delete");
        a.setIcon(R.drawable.ic_delete_forever_white_36dp);
        a.setShowAsAction(1);
        a.setVisible(true);
        a.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mainDatabase = new MainDatabaseHelper(getActivity());
                db = mainDatabase.getWritableDatabase();
                db.delete(MainDatabaseContract.Notes.TABLE_NAME,
                        MainDatabaseContract.Notes._ID + "= ?", new String[]{Long.toString(id)});
                db.close();

                Fragment fragment = new NotebookFragment();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, fragment, "visible_fragment");
                ft.addToBackStack(null);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }
}
