package com.example.h.ruhungry;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.internal.ConstructorConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by h on 11/18/2016.
 */

public class DiningTransitionHelper implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, ResultCallback {
    private ArrayList<Geofence> mGeofences;
    private PendingIntent mGeofencePendingIntent;
    private Context mContext;
    private GoogleApiClient mGoogleApiClient;

    private static final String TAG="DiningTransitionHelper";

    public DiningTransitionHelper(Context context ){
        mContext=context;
        mGeofences=new ArrayList<>();
        for (Map.Entry<String, LatLng> entry:Constants.RU_DINING_HALLS.entrySet()){
            mGeofences.add(new Geofence.Builder()
                    .setRequestId(entry.getKey())

                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            Constants.GEOFENCE_RADIUS_IN_METERS
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                    .setLoiteringDelay(Constants.LOITERING_DELAY)
                    .build());
        }

        mGoogleApiClient=new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


    }
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL|GeofencingRequest.INITIAL_TRIGGER_EXIT);
        builder.addGeofences(mGeofences);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        /*if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }*/
        Intent intent = new Intent(mContext, DiningHallTransitionIntentService.class);
        mGeofencePendingIntent=PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }



    public void start(){
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "GoogleApi Connected!");
        try {

            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object.
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        }catch (SecurityException e){
            Toast.makeText(mContext,"Turn on Location",Toast.LENGTH_SHORT);
            e.printStackTrace();
        }


    }

    public void end(){
        mGoogleApiClient.disconnect();
        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient,getGeofencePendingIntent());
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "GoogleApi Failed to Connect!");
    }

    @Override
    public void onResult(@NonNull Result result) {
        if (result.getStatus().isSuccess()) {
            Log.d(TAG,"Geofences added successfully");
        }else{
            Log.d(TAG,"Geofences failed to add!");
        }

    }
}
