package com.example.lewis.weather.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.example.lewis.weather.entity.DatabaseLocationObject;

import java.util.ArrayList;
import java.util.List;

public class DatabaseQuery extends DatabaseObject{

    private final String TABLE_NAME = "data";

    private final String KEY_NAME = "_id";

    public DatabaseQuery(Context context) {
        super(context);
    }

    public List<DatabaseLocationObject> getStoredDataLocations(){
        List<DatabaseLocationObject> allLocations = new ArrayList<DatabaseLocationObject>();
        String query = "Select * from data";
        Cursor cursor = this.getDbConnection().rawQuery(query, null);
        if(cursor.moveToFirst()){
            do{
                int id = cursor.getInt(0);
                System.out.println("Response number " + id);
                String storedData = cursor.getString(cursor.getColumnIndexOrThrow("cotent"));
                System.out.println("Response number " + storedData);
                allLocations.add(new DatabaseLocationObject(id, storedData));
            }while (cursor.moveToNext());
        }
        cursor.close();
        return allLocations;
    }

    public int countAllStoredLocations(){
        int total = 0;
        String query = "Select * from data";
        Cursor cursor = this.getDbConnection().rawQuery(query, null);
        if(cursor.moveToFirst()){
            total = cursor.getCount();
        }
        cursor.close();
        return total;
    }

    private boolean isLocationExist(String location){
        String query = "Select * from data where cotent=" + "'"+location+"'";
        Cursor cursor = this.getDbConnection().rawQuery(query, null);
        if(cursor.moveToFirst()){
            return true;
        }
        cursor.close();
        return false;
    }

    public void insertNewLocation(String cityCountry){
        ContentValues values = new ContentValues();
        values.put("cotent", cityCountry);
        if(!isLocationExist(cityCountry)){
            getDbConnection().insert(TABLE_NAME, null, values);
        }
        getDbConnection().close();
    }

    public boolean deleteLocation(int locationId){
        return getDbConnection().delete(TABLE_NAME, KEY_NAME + "=" + locationId, null) > 0;
    }

    public void deleteAllLocationContent(){
        getDbConnection().execSQL("delete from " + TABLE_NAME);
    }
}
