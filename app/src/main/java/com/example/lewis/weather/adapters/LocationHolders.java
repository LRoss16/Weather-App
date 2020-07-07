package com.example.lewis.weather.adapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.lewis.weather.R;
import com.example.lewis.weather.entity.LocationObject;

import java.util.List;

public class LocationHolders extends RecyclerView.ViewHolder{

    private static final String TAG = LocationHolders.class.getSimpleName();

    public TextView locationCity;

    public TextView weatherInformation;

    public TextView deleteText;

    public RadioButton selectableRadioButton;

    public LocationHolders(final View itemView, final List<LocationObject> locationObject) {
        super(itemView);
        locationCity = itemView.findViewById(R.id.city_location);
        weatherInformation = itemView.findViewById(R.id.temp_info);
        selectableRadioButton = itemView.findViewById(R.id.radio_button);
        deleteText =itemView.findViewById(R.id.delete_row);
        deleteText.setTextColor(Color.RED);

    }
}
