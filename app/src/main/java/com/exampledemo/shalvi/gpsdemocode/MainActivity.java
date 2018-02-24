package com.exampledemo.shalvi.gpsdemocode;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.exampledemo.shalvi.gpsdemocode.helper.SqlHelper;
import com.exampledemo.shalvi.gpsdemocode.model.TrckerData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static final String TAG = "MainActivity";
    private TextView mLatitudeTextView;
    private TextView mLongitudeTextView;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    SqlHelper db;
    Handler handler;
    private LocationManager mLocationManager;
    final static int REQUEST_LOCATION = 199;
    private LocationRequest mLocationRequest;
    private com.google.android.gms.location.LocationListener listener;
    TextView speed_text;
    Dialog Addtagdialog;
    ArrayList<String> permissionsToRequest;
    ArrayList<String> permissionsRejected = new ArrayList<>();
    Double latitude_data, longitude_data;
    int speed;
    String time;
    private GoogleApiClient googleApiClient;
    private LocationManager locationManager;
    private final static int ALL_PERMISSIONS_RESULT = 101;
    TextView tvTime;
    String data;
    Switch s_traking;
    int  timeIntervel;
    private static final String FILENAME = "myFile.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler();
        mLatitudeTextView = (TextView) findViewById((R.id.latitude_textview));
        mLongitudeTextView = (TextView) findViewById((R.id.longitude_textview));
        tvTime = (TextView) findViewById(R.id.tvTime);
        speed_text = (TextView) findViewById(R.id.speed_text);
        s_traking = (Switch) findViewById(R.id.s_traking);

        db = new SqlHelper(this);
        //Eable and Disable Track System
        s_traking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b == true) {
                    turnONSwitch();
                } else {
                    turnOff();
                }
            }
        });
        speed_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSpeed();
            }
        });
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(MainActivity.this, "Gps already enabled", Toast.LENGTH_SHORT).show();
            s_traking.setChecked(true);

        }
        //checkLocation(); //check whether location service is enable or not in your  phone
    }

    public void addSpeed() {
        Addtagdialog = new Dialog(MainActivity.this);
        Addtagdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Addtagdialog.setContentView(R.layout.addspeed);
        Addtagdialog.setCancelable(false);
        Addtagdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Addtagdialog.getWindow()
                .getAttributes().windowAnimations = R.style.DialogAnimation;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        final EditText speedData = (EditText) Addtagdialog.findViewById(R.id.speed_editText);
        Button Ok = (Button) Addtagdialog.findViewById(R.id.ok);
        Button cancle= (Button) Addtagdialog.findViewById(R.id.cancle);
        Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (speedData.getText().equals(null) || speedData.getText().toString().isEmpty()) {

                } else {
                    calculateSpeed(Integer.valueOf(speedData.getText().toString()));
                    Addtagdialog.dismiss();
                }
            }
        });
        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Addtagdialog.dismiss();
            }
        });
        lp.copyFrom(Addtagdialog.getWindow().getAttributes());
        Addtagdialog.getWindow().setAttributes(lp);
        Addtagdialog.show();


    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }


        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLocation == null) {
            //startLocationUpdates(1000);
            calculateSpeed(80);

        }
        if (mLocation != null) {
            calculateSpeed(80);
      } else {
            Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show();
        }
    }
//-------------------------calculate Speed----------------------------------
    private void calculateSpeed(int Speed) {
        if (Speed >= 80) {
            startLocationUpdates(300000);
            timeIntervel=300000;
            speed=Speed;
            Toast.makeText(MainActivity.this, "Captured in 3 second", Toast.LENGTH_SHORT).show();

        } else if (Speed < 80 && Speed >= 60) {
            startLocationUpdates(500000);
            timeIntervel=500000;
            Toast.makeText(MainActivity.this, "Captured in 5 second", Toast.LENGTH_SHORT).show();


        } else if (Speed < 60 && Speed >= 30) {
            startLocationUpdates(120000);
            timeIntervel=120000;
            Toast.makeText(MainActivity.this, "Captured in  2 min", Toast.LENGTH_SHORT).show();


        } else if (Speed < 30) {
            startLocationUpdates(300000);
            timeIntervel=300000;
            Toast.makeText(MainActivity.this, "Captured in 5 min", Toast.LENGTH_SHORT).show();


        }
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    protected void startLocationUpdates(long capturedTime) {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

                .setInterval(capturedTime);

        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
        Log.d("reque", "--->>>>");
    }

    @Override
    public void onLocationChanged(Location location) {

        String msg = location.getTime() +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude())+timeIntervel;
        mLatitudeTextView.setText(String.valueOf(location.getLatitude()));
        mLongitudeTextView.setText(String.valueOf(location.getLongitude()));
        tvTime.setText(DateFormat.getTimeInstance().format(location.getTime()));
        writeToFile(msg);
        addData(location.getLatitude(),location.getLongitude(),DateFormat.getTimeInstance().format(location.getTime()));

    }

    private boolean checkLocation() {
        if (!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    //=======================  Gps Enable Method===================
    void turnONSwitch() {

        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(MainActivity.this, "Gps not enabled", Toast.LENGTH_SHORT).show();
            enableLoc();

        } else {

            Toast.makeText(MainActivity.this, "Gps already enabled", Toast.LENGTH_SHORT).show();
        }
    }
    //========================== Gps Disable Method================

    private void turnOff() {
        LocationManager loc = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (loc.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "GPS off", Toast.LENGTH_LONG).show();
            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(myIntent);
            googleApiClient = null;
        }
    }

    private void enableLoc() {

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {

                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            googleApiClient.connect();
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {

                            Log.d("Location error", "Location error " + connectionResult.getErrorCode());
                        }
                    }).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(MainActivity.this, REQUEST_LOCATION);
                                //  getLoc();


                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                    }
                }
            });
        }

    }

    //=============================   check Permission for 4.4 and above==========================
    private boolean hasPermission(String permission) {
        if (canAskPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canAskPermission() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSIONS_RESULT:
                Log.d(TAG, "onRequestPermissionsResult");
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toArray(
                                                        new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);

                                            }
                                        }
                                    });
                            return;
                        }
                    }
                } else {
                    Log.d(TAG, "No rejected permissions.");

                    // getLocation();
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
    ///=========================== insert Data in Sqlite=============================

    public void addData(double latitude, double longitude, String time) {

        if (latitude == 0 || longitude == 0 || time == null || speed == 0) {
            Toast.makeText(MainActivity.this, "data is not avalible", Toast.LENGTH_SHORT).show();
        } else {
            db.addTrackingData(new TrckerData(latitude, longitude, time, speed));
        }
    }

    //=========================== insert in file================================

    public void insertFile(String msg){
        data=msg;
        try {
            FileOutputStream fOut = openFileOutput(FILENAME,MODE_WORLD_READABLE);
            fOut.write(data.getBytes());

            fOut.close();
            Toast.makeText(getBaseContext(),"file saved"+data,Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    private void writeToFile(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(FILENAME, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);

            outputStreamWriter.close();
            //readFromFile();
        }
        catch (IOException e) {
            Log.e(TAG, "File write failed: " + e.toString());
        }

    }

    private String readFromFile() {

        String ret = "";

        try {
            InputStream inputStream = openFileInput(FILENAME);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "Can not read file: " + e.toString());
        }

        return ret;
    }
}

