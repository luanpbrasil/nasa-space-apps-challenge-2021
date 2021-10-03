package com.example.nasaapp;

import android.os.Bundle;
import android.util.Log;
import android.view.Choreographer;
import android.view.View;
import android.view.Menu;
import android.widget.FrameLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import gov.nasa.worldwind.Navigator;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layer.BackgroundLayer;
import gov.nasa.worldwind.layer.BlueMarbleLandsatLayer;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.PlacemarkAttributes;

public class MainActivity extends AppCompatActivity implements Choreographer.FrameCallback {

    private AppBarConfiguration mAppBarConfiguration;

    private WorldWindow wwd;
    private static final int NUM_PLACES = 10;
    private long lastFrameTimeNanos;
    private double cameraDegreesPerSecond = 0.41;
    protected boolean activityPaused;
    private RenderableLayer placemarksLayer;

    private int playPause = 1;

    Map<String, Placemark> placemarkHashMap = new HashMap<String, Placemark>();
    ArrayList<String> keys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(playPause == 1){
                    fab.setImageResource(android.R.drawable.ic_media_play);
                    playPause = 0;
                }
                else{
                    fab.setImageResource(android.R.drawable.ic_media_pause);
                    playPause = 1;
                }

            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //-------------------------------------------------------------------------------------------------------------
        // Create a WorldWindow (a GLSurfaceView)...
        wwd = new WorldWindow(getApplicationContext());
        // ... and add some map layers
        wwd.getLayers().addLayer(new BackgroundLayer());
        wwd.getLayers().addLayer(new BlueMarbleLandsatLayer());
        // Add the WorldWindow view object to the layout that was reserved for the globe.
        FrameLayout globeLayout = (FrameLayout) findViewById(R.id.globe);
        globeLayout.addView(wwd);


        // Create a Renderable layer for the placemarks and add it to the WorldWindow
        placemarksLayer = new RenderableLayer("Placemarks");
        wwd.getLayers().addLayer(placemarksLayer);

        startPlacemarks();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop running the animation when this activity is paused.
        this.activityPaused = true;
        this.lastFrameTimeNanos = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume the earth rotation animation
        this.activityPaused = false;
        this.lastFrameTimeNanos = 0;
        Choreographer.getInstance().postFrameCallback(this);
    }
    @Override
    public void doFrame(long frameTimeNanos) {
        if (this.lastFrameTimeNanos != 0) {
            // Compute the frame duration in seconds.
            double frameDurationSeconds = (frameTimeNanos - this.lastFrameTimeNanos) * 1.0e-9;
            if(playPause != 0) {
                double cameraDegrees = (frameDurationSeconds * this.cameraDegreesPerSecond);

                // Move the navigator to simulate the Earth's rotation about its axis.
                Navigator navigator = wwd.getNavigator();
                navigator.setLongitude(navigator.getLongitude() - cameraDegrees);
            }

            // Redraw the WorldWindow to display the above changes.
            wwd.requestRedraw();
        }

        if (!this.activityPaused) { // stop animating when this Activity is paused
            Choreographer.getInstance().postFrameCallback(this);
        }

        if(playPause == 0){
            return;
        }
        keys = new ArrayList<String>(placemarkHashMap.keySet());

        try {

            for (int i = 0; i < keys.size(); i++) {

                double lat = placemarkHashMap.get(keys.get(i)).getPosition().latitude + 0.5;
                double lon = placemarkHashMap.get(keys.get(i)).getPosition().longitude + 0.5;
                double alt = placemarkHashMap.get(keys.get(i)).getPosition().altitude + 0.5;

                Position posAux = new Position(lat, lon, alt);

                placemarkHashMap.get(keys.get(i)).moveTo(wwd.getGlobe(), posAux);

            }
            this.lastFrameTimeNanos = frameTimeNanos;

        } catch(NullPointerException e){
            Log.e("MainActivity.java", "Keys array is null");
        }

    }

    public void startPlacemarks(){
        // Create some placemarks at a known locations
        Placemark origin = new Placemark(Position.fromDegrees(0, 0, 1e5),
                PlacemarkAttributes.createWithImageAndLeader(ImageSource.fromResource(R.drawable.point)),
                "Origin");
        placemarkHashMap.put("origin", origin);
        Placemark northPole = new Placemark(Position.fromDegrees(90, 0, 1e5),
                PlacemarkAttributes.createWithImageAndLeader(ImageSource.fromResource(R.drawable.point)),
                "North Pole");
        placemarkHashMap.put("North Pole", northPole);
        Placemark southPole = new Placemark(Position.fromDegrees(-90, 0, 0),
                PlacemarkAttributes.createWithImage(ImageSource.fromResource(R.drawable.point)),
                "South Pole");
        placemarkHashMap.put("South Pole", southPole);
        Placemark antiMeridian = new Placemark(Position.fromDegrees(0, 180, 0),
                PlacemarkAttributes.createWithImage(ImageSource.fromResource(R.drawable.point)),
                "Anti-meridian");
        placemarkHashMap.put("Anti-meridian", antiMeridian);




        keys = new ArrayList<String>(placemarkHashMap.keySet());

        for(int i = 0; i < keys.size(); i++){
            placemarksLayer.addRenderable(placemarkHashMap.get(keys.get(i)));

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}