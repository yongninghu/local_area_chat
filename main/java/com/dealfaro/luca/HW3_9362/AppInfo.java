package com.dealfaro.luca.HW3_9362;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.preference.PreferenceManager;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by craighu on 5/3/2015.
 */
public class AppInfo {

    public static final String PREF_USERID = "crg_hu";

    private static AppInfo instance = null;

    protected AppInfo() {
        // Exists only to defeat instantiation.
    }

    // Here are some values we want to keep global.
    public String userid;

    public static List<String> chatIDList = new ArrayList<String>();

    public static AppInfo getInstance(Context context) {
        if (instance == null) {
            instance = new AppInfo();
            // Creates a userid, if I don't have one.
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            instance.userid = settings.getString(PREF_USERID, null);
            if (instance.userid == null) {
                // We need to create a userid.
                SecureRandom random = new SecureRandom();
                instance.userid = new BigInteger(130, random).toString(32);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(PREF_USERID, instance.userid);
                editor.commit();
            }
        }
        return instance;
    }

    public Location lastLocation;

    //a shared location listener for all activity.
    public LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // Do something with the location you receive.
            lastLocation = location;
            /*

            */
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };
}