//MainActivity.java
//Hosts the MainActivityFragment on a phone and both the
//MainActivityFragment and SettingsActivityFragment on a tablet

package com.example.mohammad.flagquiz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    //keys for reading data from SharedPreferences
    public static final String CHOICES = "pref_numberOfChoices";
    public static final String REGIONS = "pref_regionsToInclude";

    private boolean phoneDevice = true;//used to force portrait mode
    private boolean preferencesChanged = true;//did preferences change?
    private Vibrator vibrator;// vibrator for the reset item in the menu

    //configure the MainActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        //set default values in the app's SharedPreferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        //register listener for SharedPreferences changes
        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(preferencesChangedListener);
        //determine screen size
        int screenSize = getResources().getConfiguration().screenLayout&
                Configuration.SCREENLAYOUT_SIZE_MASK;

        //if device is a tablet, set phoneDevice to false
        if(screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE ||
                screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            phoneDevice = false;//not a phone-sized device
        }

        //if running on phone-sized device, allow only portrait orientation
        if(phoneDevice){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    //called after onCreate completes execution
    @Override
    protected void onStart() {
        super.onStart();

        //now that the default preferences have been set,
        //initialize MainActivityFragment and start the quiz
        if(preferencesChanged){
            MainActivityFragment quizFragment = (MainActivityFragment)
                    getSupportFragmentManager().findFragmentById(R.id.quizFragment);
            quizFragment.updateGuessRows(PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.updateRegions(PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.resetQuiz();
            preferencesChanged = false;
        }
    }

    //show menu if app is running on a phone or a portrait-oriented tablet
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //get the device's current orientation
        int orientation = getResources().getConfiguration().orientation;

        //display the app's menu only in portrait orientation
        if(orientation == Configuration.ORIENTATION_PORTRAIT){
            //inflate the menu
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }
        else
            return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                Intent preferenceIntent = new Intent(this,SettingsActivity.class);
                startActivity(preferenceIntent);
                return true;

            case R.id.action_reset_quiz:
                MainActivityFragment quizFragment = (MainActivityFragment)
                        getSupportFragmentManager().findFragmentById(R.id.quizFragment);
                quizFragment.resetQuiz();
                Toast.makeText(MainActivity.this,
                        R.string.restarting_quiz_2, Toast.LENGTH_SHORT).show();
                vibrator.vibrate(100);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //listener for changes to the app's SharedPreferences
    private OnSharedPreferenceChangeListener preferencesChangedListener =
            new OnSharedPreferenceChangeListener() {
                //called when the user changes the app's preferences
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    preferencesChanged = true;// user changed app settings
                    MainActivityFragment quizFragment = (MainActivityFragment)
                            getSupportFragmentManager().findFragmentById(R.id.quizFragment);

                    if(key.equals(CHOICES)){// # of choices to display changed
                        quizFragment.updateGuessRows(sharedPreferences);
                        quizFragment.resetQuiz();
                    }
                    else if(key.equals(REGIONS)){// regions to include changed
                        Set<String> regions = sharedPreferences.getStringSet(REGIONS, null);

                        if(regions != null && regions.size() > 0){
                            quizFragment.updateGuessRows(sharedPreferences);
                            quizFragment.resetQuiz();
                        }
                        else {
                            //must select one region--set North America as default
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            regions.add(getString(R.string.default_region));
                            editor.putStringSet(REGIONS, regions);
                            editor.apply();
                            Toast.makeText(MainActivity.this,
                                    R.string.default_region_message, Toast.LENGTH_SHORT).show();
                        }
                    }
                    Toast.makeText(MainActivity.this,
                            R.string.restarting_quiz, Toast.LENGTH_SHORT).show();
                }
            };
}
