package com.e.hw2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FlingAnimation;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener {

    private GoogleMap mMap;
    boolean buttons_shown;
    Sensor accelerometer;
    SensorManager sensorManager;
    boolean recording;
    JSONObject markers;
    JSONArray markerArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        markerArray = new JSONArray();
        markers = new JSONObject();
        StringBuilder stringBuilder;
        String response = "";
        File file = new File(getFilesDir(),"markersArray");
        if(file.exists()){
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            stringBuilder = new StringBuilder();
            String line = bufferedReader.readLine();
            while (line != null){
                stringBuilder.append(line).append("\n");
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            response = stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            markers = new JSONObject(response);
            markerArray = markers.getJSONArray("markerArray");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("eo",response);
    }
        else {

        }
    }


    public void saveJSON() throws JSONException {
        markers.put("markerArray",markerArray);
        File file = new File(getFilesDir(),"markersArray");
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(markers.toString());
            bufferedWriter.close();
            Log.e("eo",markers.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(0,0)));
        final FloatingActionButton record = findViewById(R.id.button_record);
        final FloatingActionButton stop = findViewById(R.id.button_stop);
        final ConstraintLayout buttons = findViewById(R.id.menu_slide);
        final ImageButton zoomIn = findViewById(R.id.button_zoom_in);
        buttons_shown = false;
        recording = false;
        for(int i = 0; i<markerArray.length(); i=i+2)
        {
            LatLng latLng = null;
            try {
                latLng = new LatLng(markerArray.getDouble(i),markerArray.getDouble(i+1));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mMap.addMarker(new MarkerOptions().position(latLng).title("Position:("+String.format("%.2f",latLng.latitude)+","+String.format("%.2f",latLng.longitude)+")"));
        }


        //todo animations
        //todo json saving
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.addMarker(new MarkerOptions().position(latLng).title("Position:("+String.format("%.2f",latLng.latitude)+","+String.format("%.2f",latLng.longitude)+")"));
                try {
                    markerArray.put(latLng.latitude);
                    markerArray.put(latLng.longitude);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    saveJSON();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        ImageButton zoomOut = findViewById(R.id.button_zoom_out);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer,SensorManager.SENSOR_DELAY_NORMAL);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(buttons_shown == false) {
                    FlingAnimation fling = new FlingAnimation(buttons, DynamicAnimation.SCROLL_X);
                    fling.setStartVelocity(1000)
                            .setMinValue(0)
                            .setFriction(1.1f)
                            .start();
                    buttons_shown = true;
                }
                return false;
            }
        });

        Button clear = findViewById(R.id.button_clear);

        zoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMap.getCameraPosition().target, mMap.getCameraPosition().zoom-1));
            }
        });

        zoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMap.getCameraPosition().target, mMap.getCameraPosition().zoom+1));
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();
                markerArray = new JSONArray();
                try {
                    saveJSON();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recording = !recording;
                TextView acc = findViewById(R.id.accelerationText);
                if(recording){
                    acc.setVisibility(View.VISIBLE);
                } else acc.setVisibility(View.INVISIBLE);
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttons_shown == true){
                    FlingAnimation fling = new FlingAnimation(buttons, DynamicAnimation.SCROLL_X);
                    fling.setStartVelocity(-1000)
                            .setMinValue(0)
                            .setFriction(1.1f)
                            .start();
                    buttons_shown = false;
                    TextView acc = findViewById(R.id.accelerationText);
                    acc.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        TextView acceleration = findViewById(R.id.accelerationText);
        float accX = event.values[0];
        float accY = event.values[1];
        acceleration.setText("Acceleration: \n x: "+accX + "\n y: "+accY);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}