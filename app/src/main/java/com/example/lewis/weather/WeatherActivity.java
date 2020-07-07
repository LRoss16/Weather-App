package com.example.lewis.weather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.example.lewis.weather.adapters.RecyclerViewAdapter;
import com.example.lewis.weather.database.DatabaseQuery;
import com.example.lewis.weather.entity.WeatherObject;
import com.example.lewis.weather.helpers.CustomSharedPreference;
import com.example.lewis.weather.helpers.Helper;
import com.example.lewis.weather.json.FiveDaysForecast;
import com.example.lewis.weather.json.FiveWeathers;
import com.example.lewis.weather.json.Forecast;
import com.example.lewis.weather.json.LocationMapObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
;

public class WeatherActivity extends AppCompatActivity implements LocationListener {

    private static final String TAG = WeatherActivity.class.getSimpleName();

    private RecyclerView recyclerView;

    private RecyclerViewAdapter recyclerViewAdapter;

    private TextView cityCountry;

    private TextView currentDate;

    private ImageView weatherImage;

    private String cardinalDirection;



    private TextView windResult;

    private TextView Direction;

    private TextView Info;

    private TextView Result;

     private TextView sunRise;

     private TextView sunSet;

     private TextView minTemp;

     private TextView maxTemp;

    private TextView humidityResult;

    private RequestQueue queue;

    private LocationMapObject locationMapObject;

    private LocationManager locationManager;

    private Location location;

    private final int REQUEST_LOCATION = 200;

    private CustomSharedPreference sharedPreference;

    private String isLocationSaved;

    private DatabaseQuery query;

    private String apiUrl;

    private FiveDaysForecast fiveDaysForecast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        queue = Volley.newRequestQueue(this);
        query = new DatabaseQuery(WeatherActivity.this);
        sharedPreference = new CustomSharedPreference(WeatherActivity.this);
        isLocationSaved = sharedPreference.getLocationInPreference();

        cityCountry = findViewById(R.id.city_country);
        currentDate = findViewById(R.id.current_date);
        weatherImage = findViewById(R.id.weather_icon);
        Result = findViewById(R.id.weather_result);
        Info = findViewById(R.id.weather_information);
        windResult = findViewById(R.id.wind_result);
        Direction = findViewById(R.id.wind_direction);
        humidityResult = findViewById(R.id.humidity_result);
        sunRise = findViewById(R.id.sunrise_result);
        sunSet = findViewById(R.id.sunset_result);
        minTemp = findViewById(R.id.min_temp);
        maxTemp = findViewById(R.id.max_temp);

        locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(WeatherActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            if (isLocationSaved.equals("")) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 2, this);
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    apiUrl = "http://api.openweathermap.org/data/2.5/weather?lat=" + location.getLatitude() + "&lon=" + location.getLongitude() + "&APPID=" + Helper.API_KEY + "&units=metric";
                    makeJsonObject(apiUrl);
                }
            } else {

                String storedCityName = sharedPreference.getLocationInPreference();

                System.out.println("Stored city " + storedCityName);
                String[] city = storedCityName.split(",");
                if (!TextUtils.isEmpty(city[0])) {
                    System.out.println("Stored city " + city[0]);
                    String url = "http://api.openweathermap.org/data/2.5/weather?q=" + city[0] + "&APPID=" + Helper.API_KEY + "&units=metric";
                    makeJsonObject(url);
                }
            }
        }

        ImageButton addLocation = findViewById(R.id.add_location);
        addLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addLocationIntent = new Intent(WeatherActivity.this, AddLocationActivity.class);
                startActivity(addLocationIntent);
            }
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(WeatherActivity.this, 4);
        List<WeatherObject> dailyWeather = new ArrayList<>();


        recyclerView = findViewById(R.id.weather_daily_list);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);
    }


    private void makeJsonObject(final String apiUrl) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, apiUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response " + response);
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                locationMapObject = gson.fromJson(response, LocationMapObject.class);
                if (null == locationMapObject) {
                    Toast.makeText(getApplicationContext(), "Error nothing found", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Data recievd", Toast.LENGTH_LONG).show();

                    String city = locationMapObject.getName() + ", " + locationMapObject.getSys().getCountry();
                    String todayDate = getTodayDateInStringFormat();
                    Long tempVal = Math.round(Math.floor(Double.parseDouble(locationMapObject.getMain().getTemp())));
                    String weatherTemp = String.valueOf(tempVal) + "°";
                    String weatherDescription = Helper.capitalizeFirstLetter(locationMapObject.getWeather().get(0).getDescription());
                    String windSpeed = locationMapObject.getWind().getSpeed();
                    convertDegreeToCardinalDirection();
                    String windDirection = cardinalDirection;
                    String humidityValue = locationMapObject.getMain().getHumudity();
                   String riseTime = locationMapObject.getSys().getSunrise();
                   String setTime = locationMapObject.getSys().getSunset();
                   String minimum = locationMapObject.getMain().getTemp_min();
                   String maximum = locationMapObject.getMain().getTemp_max();


                    //save location in database
                    if (apiUrl.contains("lat")) {
                        query.insertNewLocation(locationMapObject.getName());
                    }
                    // populate View data
                    cityCountry.setText(String.valueOf(city));
                    currentDate.setText(todayDate);
                    Result.setText((weatherTemp));
                    Info.setText(weatherDescription);
                    windResult.setText((windSpeed) + " km/h");
                    Direction.setText((windDirection) + " direction");
                    humidityResult.setText((humidityValue) + " %");
                   sunRise.setText(UnixTime(Long.parseLong(riseTime)));
                   sunSet.setText(UnixTime(Long.parseLong(setTime)));
                   minTemp.setText("min temp " + minimum + "°");
                   maxTemp.setText("max temp " + maximum + "°");



                    fiveDaysApiJsonObjectCall(locationMapObject.getName());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error " + error.getMessage());
            }
        });
        queue.add(stringRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //make api call
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 2, this);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        apiUrl = "http://api.openweathermap.org/data/2.5/weather?lat=" + location.getLatitude() + "&lon=" + location.getLongitude() + "&APPID=" + Helper.API_KEY + "&units=metric";
                        makeJsonObject(apiUrl);
                    } else {
                        apiUrl = "http://api.openweathermap.org/data/2.5/weather?lat=51.5074&lon=0.1278&APPID=" + Helper.API_KEY + "&units=metric";
                        makeJsonObject(apiUrl);
                    }
                }
            } else {
                Toast.makeText(WeatherActivity.this, getString(R.string.permission_notice), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            showGPSDisabledAlertToUser();
        }
    }

    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("You can enable GPS on your settings page", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(callGPSSettingIntent);
                    }
                });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private String getTodayDateInStringFormat() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("E, d MMMM", Locale.getDefault());
        return df.format(c.getTime());
    }

    private void fiveDaysApiJsonObjectCall(String city) {
        String apiUrl = "http://api.openweathermap.org/data/2.5/forecast?q=" + city + "&APPID=" + Helper.API_KEY + "&units=metric";
        final List<WeatherObject> daysOfTheWeek = new ArrayList<>();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, apiUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response 5 days" + response);
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                Forecast forecast = gson.fromJson(response, Forecast.class);
                if (null == forecast) {
                    Toast.makeText(getApplicationContext(), "Can't display anything", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Data recieved", Toast.LENGTH_LONG).show();

                    int[] everyday = new int[]{0, 0, 0, 0, 0, 0, 0};

                    List<FiveWeathers> weatherInfo = forecast.getList();
                    if (null != weatherInfo) {
                        for (int i = 0; i < weatherInfo.size(); i++) {
                            String time = weatherInfo.get(i).getDt_txt();
                            String shortDay = convertTimeToDay(time);
                            String temp = weatherInfo.get(i).getMain().getTemp();
                            String tempMin = weatherInfo.get(i).getMain().getTemp_min();

                            if (convertTimeToDay(time).equals("Mon") && everyday[0] < 1) {
                                daysOfTheWeek.add(new WeatherObject(shortDay, R.drawable.small_weather_icon, temp, tempMin));
                                everyday[0] = 1;
                            }
                            if (convertTimeToDay(time).equals("Tue") && everyday[1] < 1) {
                                daysOfTheWeek.add(new WeatherObject(shortDay, R.drawable.small_weather_icon, temp, tempMin));
                                everyday[1] = 1;
                            }
                            if (convertTimeToDay(time).equals("Wed") && everyday[2] < 1) {
                                daysOfTheWeek.add(new WeatherObject(shortDay, R.drawable.small_weather_icon, temp, tempMin));
                                everyday[2] = 1;
                            }
                            if (convertTimeToDay(time).equals("Thu") && everyday[3] < 1) {
                                daysOfTheWeek.add(new WeatherObject(shortDay, R.drawable.small_weather_icon, temp, tempMin));
                                everyday[3] = 1;
                            }
                            if (convertTimeToDay(time).equals("Fri") && everyday[4] < 1) {
                                daysOfTheWeek.add(new WeatherObject(shortDay, R.drawable.small_weather_icon, temp, tempMin));
                                everyday[4] = 1;
                            }
                            if (convertTimeToDay(time).equals("Sat") && everyday[5] < 1) {
                                daysOfTheWeek.add(new WeatherObject(shortDay, R.drawable.small_weather_icon, temp, tempMin));
                                everyday[5] = 1;
                            }
                            if (convertTimeToDay(time).equals("Sun") && everyday[6] < 1) {
                                daysOfTheWeek.add(new WeatherObject(shortDay, R.drawable.small_weather_icon, temp, tempMin));
                                everyday[6] = 1;
                            }


                            recyclerView = findViewById(R.id.weather_daily_list);
                            recyclerViewAdapter = new RecyclerViewAdapter(WeatherActivity.this, daysOfTheWeek);
                            recyclerView.setAdapter(recyclerViewAdapter);


                        }
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error " + error.getMessage());
            }
        });
        queue.add(stringRequest);
    }

    private String convertTimeToDay(String time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:SSSS", Locale.getDefault());
        String days = "";
        try {
            Date date = format.parse(time);
            System.out.println("Our time " + date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            days = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault());
            System.out.println("Our time " + days);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return days;
    }

    public void convertDegreeToCardinalDirection() {

        if ((locationMapObject.getWind().getDeg() >= 348.75) && (locationMapObject.getWind().getDeg() <= 360) ||
                (locationMapObject.getWind().getDeg() >= 0) && (locationMapObject.getWind().getDeg() <= 11.25)) {
            cardinalDirection = "N";
        } else if ((locationMapObject.getWind().getDeg() >= 11.25) && (locationMapObject.getWind().getDeg() <= 33.75)) {
            cardinalDirection = "NNE";
        } else if ((locationMapObject.getWind().getDeg() >= 33.75) && (locationMapObject.getWind().getDeg() <= 56.25)) {
            cardinalDirection = "NE";
        } else if ((locationMapObject.getWind().getDeg() >= 56.25) && (locationMapObject.getWind().getDeg() <= 78.75)) {
            cardinalDirection = "ENE";
        } else if ((locationMapObject.getWind().getDeg() >= 78.75) && (locationMapObject.getWind().getDeg() <= 101.25)) {
            cardinalDirection = "E";
        } else if ((locationMapObject.getWind().getDeg() >= 101.25) && (locationMapObject.getWind().getDeg() <= 123.75)) {
            cardinalDirection = "ESE";
        } else if ((locationMapObject.getWind().getDeg() >= 123.75) && (locationMapObject.getWind().getDeg() <= 146.25)) {
            cardinalDirection = "SE";
        } else if ((locationMapObject.getWind().getDeg() >= 146.25) && (locationMapObject.getWind().getDeg() <= 168.75)) {
            cardinalDirection = "SSE";
        } else if ((locationMapObject.getWind().getDeg() >= 168.75) && (locationMapObject.getWind().getDeg() <= 191.25)) {
            cardinalDirection = "S";
        } else if ((locationMapObject.getWind().getDeg() >= 191.25) && (locationMapObject.getWind().getDeg() <= 213.75)) {
            cardinalDirection = "SSW";
        } else if ((locationMapObject.getWind().getDeg() >= 213.75) && (locationMapObject.getWind().getDeg() <= 236.25)) {
            cardinalDirection = "SW";
        } else if ((locationMapObject.getWind().getDeg() >= 236.25) && (locationMapObject.getWind().getDeg() <= 258.75)) {
            cardinalDirection = "WSW";
        } else if ((locationMapObject.getWind().getDeg() >= 258.75) && (locationMapObject.getWind().getDeg() <= 281.25)) {
            cardinalDirection = "W";
        } else if ((locationMapObject.getWind().getDeg() >= 281.25) && (locationMapObject.getWind().getDeg() <= 303.75)) {
            cardinalDirection = "WNW";
        } else if ((locationMapObject.getWind().getDeg() >= 303.75) && (locationMapObject.getWind().getDeg() <= 326.25)) {
            cardinalDirection = "NW";
        } else if ((locationMapObject.getWind().getDeg() >= 326.25) && (locationMapObject.getWind().getDeg() <= 348.75)) {
            cardinalDirection = "NNW";
        } else {
            cardinalDirection = "?";
        }
    }

    private String UnixTime(long timex) {

        Date date = new Date(timex *1000L);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }


}



