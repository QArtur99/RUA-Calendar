package com.artf.ruacalendar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.artf.ruacalendar.Database.MainDatabaseContract.Settings;
import com.artf.ruacalendar.Database.MainDatabaseHelper;

public class MainActivity extends AppCompatActivity {

    private static final int INITIAL_REQUEST = 1337;
    private static final String[] INITIAL_PERMS = {
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.INTERNET,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.VIBRATE,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.ACCESS_NETWORK_STATE,
    };
    private int startLoad;
    private SQLiteDatabase db;
    private Cursor cursor;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        int uiOptions =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        setContentView(R.layout.activity_main);

        // Here, thisActivity is the current activity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkIfAlreadyhavePermission()) {
                requestForSpecificPermission();
            } else {
                loadViewDataByAsyncTask();
            }
        } else {
            loadViewDataByAsyncTask();
        }
    }

    private void loadDataBase() {
        try {
            SQLiteOpenHelper mainDatabase = new MainDatabaseHelper(this);
            db = mainDatabase.getReadableDatabase();
            cursor = db.query(Settings.TABLE_NAME,
                    new String[]{Settings._ID, Settings.START_LOAD}, null, null, null, null, null);

            if (cursor.moveToFirst()) {
                startLoad = cursor.getInt(cursor.getColumnIndexOrThrow(Settings.START_LOAD));
            }

        } catch (SQLiteException e) {
            Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    private void loadViewDataByAsyncTask() {
        loadDataBase();
        AsyncTaskLoader runner = new AsyncTaskLoader(this, startLoad);
        runner.execute();
        mHandler.postDelayed(new Runnable() {
            public void run() {
                runMainUse();
            }
        }, 2000);
    }

    private void runMainUse() {
        Intent goToMainUse = new Intent(MainActivity.this, MainUse.class);
        startActivityForResult(goToMainUse, 1);
    }


    private boolean checkIfAlreadyhavePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestForSpecificPermission() {
        requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case INITIAL_REQUEST:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadViewDataByAsyncTask();
                } else {
                    Toast.makeText(this, "Permissions are neccessary", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cursor != null) {
            cursor.close();
        }
        if (db != null) {
            db.close();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //matches the result code passed from ChildActivity
        if (resultCode == 0) {
            MainActivity.this.finish();
        }
    }
}
