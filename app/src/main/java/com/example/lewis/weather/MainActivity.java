package com.example.lewis.weather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.google.common.collect.Lists;
import com.google.common.math.IntMath;
import com.example.lewis.weather.entity.ListJsonObject;
import com.example.lewis.weather.helpers.CustomApplication;
import com.example.lewis.weather.helpers.CustomSharedPreference;
import com.example.lewis.weather.helpers.Helper;

import java.io.InputStream;
import java.math.RoundingMode;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private boolean mFirstUse = false;
    private static final String FIRST_TIME = "first_time";
    private final int SPLASH_DISPLAY_LENGTH = 4000;

    private CustomSharedPreference customSharedPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.hide();
        }

        customSharedPreference = new CustomSharedPreference(MainActivity.this);
        if(!customSharedPreference.getDataSourceIfPresent()){
            PrepareDataSource mDataSource = new PrepareDataSource();
            mDataSource.execute();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {


                if (!FirstUse()) {

                    Intent x = new Intent(MainActivity.this, WelcomeSlider.class);
                    startActivity(x);

                    markAppUsed();
                } else {
                    Intent startActivityIntent = new Intent(MainActivity.this, WeatherActivity.class);
                    startActivity(startActivityIntent);
                    MainActivity.this.finish();
                }
            }
        },SPLASH_DISPLAY_LENGTH);
            }






    private boolean FirstUse() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mFirstUse = sharedPreferences.getBoolean(FIRST_TIME, false);
        return mFirstUse;
    }

    private void markAppUsed() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mFirstUse = true;
        sharedPreferences.edit().putBoolean(FIRST_TIME, mFirstUse).apply();
    }

    private class PrepareDataSource extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... voids) {
            InputStream stream = ((CustomApplication)getApplication()).getJsonStream();
            List<ListJsonObject> storeSourceData = ((CustomApplication)getApplication()).readStream(stream);
            // store data in shared reference
            int partitionSize = IntMath.divide(storeSourceData.size(), 2, RoundingMode.UP);
            List<List<ListJsonObject>> partitions = Lists.partition(storeSourceData, partitionSize);
            List<ListJsonObject> firstListObject = partitions.get(0);
            List<ListJsonObject> secondListObject = partitions.get(1);

            customSharedPreference.setDataFromSharedPreferences(Helper.STORED_DATA_FIRST, firstListObject);
            customSharedPreference.setDataFromSharedPreferences(Helper.STORED_DATA_SECOND, secondListObject);
            customSharedPreference.setDataSourceIfPresent(true);

            return null;
        }
    }


}
