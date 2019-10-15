package com.artf.ruacalendar;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.artf.ruacalendar.Database.MainDatabaseContract.Settings;
import com.artf.ruacalendar.Database.MainDatabaseHelper;
import com.artf.ruacalendar.Event.CreateEventFragment;
import com.artf.ruacalendar.Event.EditEventFragment;
import com.artf.ruacalendar.Notebook.CreateNoteFragment;
import com.artf.ruacalendar.Notebook.EditNoteFragment;
import com.artf.ruacalendar.Notebook.NotebookFragment;
import com.artf.ruacalendar.Notifications.NotificationReceiver;
import com.artf.ruacalendar.Settings.SettingsFragment;
import com.artf.ruacalendar.Settings.SettingsSyncFragment;
import com.artf.ruacalendar.Views.BlocksViewFragment;
import com.artf.ruacalendar.Views.CalendarProFragment;
import com.artf.ruacalendar.Views.EditScheduleFragment;
import com.artf.ruacalendar.Views.ScheduleFragment;
import com.artf.ruacalendar.Views.SearchFragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import me.everything.providers.android.calendar.CalendarProvider;
import me.everything.providers.android.calendar.Reminder;
import me.everything.providers.core.Data;


/**
 * Created by ART_F on 2016-05-13.
 */
public class MainUse extends AppCompatActivity {

    Cursor cursor;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    private int type = 0;
    private Runnable mPendingRunnable;
    private Handler mHandler = new Handler();
    private TextView titleToolbar;
    private Calendar rightNow = Calendar.getInstance();
    private Bundle bundle;
    private ActionBar ab;
    private ImageView arrow;
    private int starView;
    private long eventId = 0;
    private Toolbar toolbar;
    private Fragment fragment = null;
    private int currentPosition;
    private int yearValue;
    private int monthValue;
    private int dayValue;
    private int currentYearValue = rightNow.get(Calendar.YEAR);
    private int currentMonthValue = rightNow.get(Calendar.MONTH);
    private int currentDayValue = rightNow.get(Calendar.DATE);
    private View adViewSpace, borderLine;
    private Boolean showAd = false;

    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }

    public static void hideKeyboardFrom(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.use_main);
        getDataValue();


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        titleToolbar = (TextView) findViewById(R.id.toolbar_title);
        arrow = (ImageView) findViewById(R.id.arrowTittle);
        setSupportActionBar(toolbar);
        ab = getSupportActionBar();
        arrow = (ImageView) findViewById(R.id.arrowTittle);

        //Display the correct fragment.
        if (savedInstanceState != null) {
            getSupportFragmentManager().getFragment(savedInstanceState, "myfragment");
            currentPosition = savedInstanceState.getInt("position");
            setActionBarTitle(currentPosition);
        } else {
            onNewIntent(getIntent());
            if (eventId == 0) {
                loadDataBase();
                setActionBarTitle(currentPosition);
                setSupportActionBar(toolbar);
            }
        }

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);

                //Check to see which item was being clicked and perform appropriate action
                type = 0;
                switch (menuItem.getItemId()) {
                    case R.id.schedule:
                        type = 1;
                        fragment = new ScheduleFragment();
                        break;
                    case R.id.day:
                        type = 2;
                        bundle = new Bundle();
                        bundle.putInt("viewType", 1);
                        fragment = new BlocksViewFragment();

                        break;
                    case R.id.threeDays:
                        type = 2;
                        bundle = new Bundle();
                        bundle.putInt("viewType", 2);
                        fragment = new BlocksViewFragment();
                        break;
                    case R.id.week:
                        type = 2;
                        bundle = new Bundle();
                        bundle.putInt("viewType", 3);
                        fragment = new BlocksViewFragment();
                        break;
                    case R.id.calendar:
                        type = 3;
                        fragment = new CalendarProFragment();
                        break;
                    case R.id.createEvent:
                        fragment = new CreateEventFragment();
                        break;
                    case R.id.createNote:
                        fragment = new NotebookFragment();
                        break;
                    case R.id.settings:
                        fragment = new SettingsFragment();
                        break;
                    case R.id.help:
                        goToMail();
                        return true;
                    // For rest of the options we just show a toast on click
                    default:
                        Toast.makeText(getApplicationContext(), "Somethings Wrong", Toast.LENGTH_SHORT).show();
                        return true;
                }
                drawerLayout.closeDrawers();
                mPendingRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (type != 0) {
                            AsyncTaskLoader runner = new AsyncTaskLoader(MainUse.this, type, fragment, bundle);
                            runner.execute();
                        }
                        if (type == 0) {
                            fragment.setArguments(bundle);
                            callFragment(fragment);
                        }
                    }
                };
                return true;
            }
        });

        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();

                if (mPendingRunnable != null) {
                    mHandler.post(mPendingRunnable);
                    mPendingRunnable = null;
                }

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }


        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.addDrawerListener(drawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        drawerToggle.syncState();
        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        FragmentManager fragMan = getSupportFragmentManager();
                        Fragment fragment = fragMan.findFragmentByTag("visible_fragment");
                        if (fragment instanceof ScheduleFragment) {
                            arrow.setVisibility(View.GONE);
                            hamburgerView();
                            currentPosition = 1;
                        }
                        if (fragment instanceof BlocksViewFragment) {
                            arrow.setVisibility(View.VISIBLE);
                            hamburgerView();
                            currentPosition = 2;
                        }
                        if (fragment instanceof EditEventFragment) {
                            backButton();
                            currentPosition = 11;
                        }
                        if (fragment instanceof EditScheduleFragment) {
                            arrow.setVisibility(View.GONE);
                            hamburgerView();
                            currentPosition = 12;
                        }
                        if (fragment instanceof CalendarProFragment) {
                            arrow.setVisibility(View.VISIBLE);
                            hamburgerView();
                            currentPosition = 5;
                        }
                        if (fragment instanceof CreateEventFragment) {
                            backButton();
                            currentPosition = 6;
                        }
                        if (fragment instanceof NotebookFragment) {
                            arrow.setVisibility(View.GONE);
                            hamburgerView();
                            currentPosition = 7;
                        }
                        if (fragment instanceof SettingsFragment) {
                            backButton();
                            currentPosition = 8;
                        }
                        if (fragment instanceof SettingsSyncFragment) {
                            backButton();
                            currentPosition = 81;
                        }
                        if (fragment instanceof CreateNoteFragment) {
                            backButton();
                            currentPosition = 41;
                        }
                        if (fragment instanceof EditNoteFragment) {
                            backButton();
                            currentPosition = 42;
                        }
                        if (fragment instanceof SearchFragment) {
                            arrow.setVisibility(View.GONE);
                            hamburgerView();
                            currentPosition = 900;
                        }

                        setActionBarTitle(currentPosition);
                        if (currentPosition <= 8) {
                            navigationView.getMenu().getItem(currentPosition - 1).setChecked(true);
                        } else {
                            navigationView.getMenu().getItem(1).setChecked(false);
                        }
                    }
                }
        );
        super.setResult(0);
    }

    private void hamburgerView() {
        ab.setDisplayHomeAsUpEnabled(false);
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerToggle.setToolbarNavigationClickListener(null);
    }

    private void backButton() {
        arrow.setVisibility(View.GONE);
        drawerToggle.setDrawerIndicatorEnabled(false);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        drawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSupportNavigateUp();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        //This method is called when the up button is pressed. Just the pop back stack.
        getSupportFragmentManager().popBackStack();
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("position", currentPosition);
    }

    private void loadDataBase() {
        SQLiteOpenHelper mainDatabase = new MainDatabaseHelper(this);
        SQLiteDatabase db = mainDatabase.getReadableDatabase();
        try {
            cursor = db.query(Settings.TABLE_NAME,
                    new String[]{Settings._ID, Settings.START_VIEW}, null, null, null, null, null);

            if (cursor.moveToFirst()) {
                starView = cursor.getInt(cursor.getColumnIndexOrThrow(Settings.START_VIEW));
            }
            selectItem2(starView, 0);

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

    public void selectItem2(int view, int condition) {
        type = 0;
        bundle = new Bundle();
        switch (view) {
            case 1:
                type = 1;
                currentPosition = 1;
                fragment = new ScheduleFragment();
                break;
            case 2:
                type = 2;
                currentPosition = 2;
                bundle.putInt("viewType", 1);
                fragment = new BlocksViewFragment();

                break;
            case 3:
                type = 2;
                currentPosition = 2;
                bundle.putInt("viewType", 2);
                fragment = new BlocksViewFragment();
                break;
            case 4:
                type = 2;
                currentPosition = 2;
                bundle.putInt("viewType", 3);
                fragment = new BlocksViewFragment();
                break;
            case 5:
                type = 3;
                currentPosition = 5;
                fragment = new CalendarProFragment();
                break;
            default:
                Toast.makeText(getApplicationContext(), "Somethings Wrong", Toast.LENGTH_SHORT).show();
                return;
        }
        if (condition != 0) {
            AsyncTaskLoader runner = new AsyncTaskLoader(MainUse.this, type, fragment, bundle);
            runner.execute();
        } else {
            fragment.setArguments(bundle);
            callFragment(fragment);
        }
    }

    public void setActionBarTitle(int position) {
        String title;
        switch (position) {
            case 1:
                title = getDateTitle();
                break;
            case 11:
                title = getResources().getString(R.string.editEvent);
                break;
            case 12:
                title = getResources().getString(R.string.event);
                break;
            case 2:
                title = getDateTitle();
                break;
            case 5:
                title = getDateTitle();
                break;
            case 6:
                title = getResources().getString(R.string.createEvent);
                break;
            case 7:
                title = getResources().getString(R.string.note);
                break;
            case 8:
                title = getResources().getString(R.string.settings);
                break;
            case 81:
                title = getResources().getString(R.string.sync);
                break;
            case 41:
                title = getResources().getString(R.string.noteCreate);
                break;
            case 42:
                title = getResources().getString(R.string.noteEdit);
                break;
            case 900:
                title = getResources().getString(R.string.search);
                break;
            default:
                title = getDateTitle();
                break;
        }
        ab.setTitle("");
        toolbar.setTitle("");
        titleToolbar.setText(title);
    }

    private String getDateTitle() {
        String title;
        DateFormat date2 = new SimpleDateFormat("MMM - yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, yearValue);
        calendar.set(Calendar.MONTH, monthValue);
        calendar.set(Calendar.DAY_OF_MONTH, dayValue);
        title = String.valueOf(date2.format(calendar.getTime()));
        return title;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater m = getMenuInflater();
        m.inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                callSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    public void callSearch(String query) {
        currentPosition = 900;
        fragment = new SearchFragment();
        bundle = new Bundle();
        bundle.putString("search", query);
        fragment.setArguments(bundle);
        callFragment(fragment);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.search:
                drawerToggle.setDrawerIndicatorEnabled(false);
                return true;
            case R.id.createEvent:
                callFragment(new CreateEventFragment());
                return true;
            case R.id.createNote:
                callFragment(new NotebookFragment());
                return true;
            case R.id.synchronize:
                AsyncTaskLoader runner = new AsyncTaskLoader(MainUse.this, 1, new ScheduleFragment(), Bundle.EMPTY);
                runner.execute();
                return true;
            case R.id.settings:
                callFragment(new SettingsFragment());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void callFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment, "visible_fragment");
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    public void getDataValue() {
        Intent intent = getIntent();
        yearValue = intent.getIntExtra("year", currentYearValue);
        monthValue = intent.getIntExtra("month", currentMonthValue);
        dayValue = intent.getIntExtra("day", currentDayValue);
    }

    @Override
    public void onBackPressed() {
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag("visible_fragment");
        if (currentPosition == 1 || currentPosition == 2 || currentPosition == 5 || currentPosition == 12) {
            fragment.onDestroy();
            MainUse.this.finish();
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        }
    }

    public void goToMail() {
        String email = "mailto:ruacalendarpro@gmail.com";

        Uri addressUri = Uri.parse(email);
        Intent intent = new Intent(Intent.ACTION_SENDTO, addressUri);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("Event_id")) {
                currentPosition = 1;
                eventId = Long.parseLong(extras.getString("Event_id"));
                Bundle bundle = new Bundle();
                bundle.putLong("Event_id", eventId);
                Fragment fragment = new EditScheduleFragment();
                fragment.setArguments(bundle);
                callFragment(fragment);
            }
        }
    }

    public void turnOff(long eventId) {
        ContentResolver cr = getContentResolver();
        CalendarProvider provider = new CalendarProvider(this);
        Data<Reminder> reminders = provider.getReminders(eventId);
        Cursor cursor = reminders.getCursor();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    long reminderId = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Reminders._ID));
                    Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Reminders.CONTENT_URI, reminderId);
                    cr.delete(deleteUri, null, null);
                    turnOffNotification(this, reminderId);
                } while (cursor.moveToNext());
            }
        }
    }

    public void turnOffNotification(Context context, long id) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) id, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }


    public void openAppInGooglePlay(View v) {
//        final String appPackageName = "com.artf.ruacalendar";
//        try {
//            this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
//        } catch (android.content.ActivityNotFoundException e) {
//            this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
//        }
    }
}