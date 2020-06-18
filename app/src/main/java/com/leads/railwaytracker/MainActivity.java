package com.leads.railwaytracker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;


public class MainActivity extends AppCompatActivity implements PermissionsListener {

    MapView mapview;
    MapboxMap mapboxMap;
    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    private PermissionsManager permissionsManager;
    private LocationChangeListeningActivityLocationCallback callback;
    private LocationEngine locationEngine;
    static int finalint = 1;
    int gotvalue = 1;
    FirebaseDatabase database;
    DatabaseReference reference;
    HashMap<String, String> detailmap;
    Point originpoint;
    Point destinationpoint;
    ArrayList<String> names;
    TextInputEditText srcedt;
    ListView listView;

    CardView contactTv,aboutus;
    NavigationView view;
    ConstraintLayout layout;
    PopupWindow popupWindow,popupWindow1;


    @SuppressLint("LogNotTimber")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_main);


        mapview = findViewById(R.id.mapview);
        srcedt = findViewById(R.id.searchedt);


        view = findViewById(R.id.navigationview);
        layout = findViewById(R.id.mainlayout);

        contactTv = findViewById(R.id.contact);
        aboutus = findViewById(R.id.about);


        database = FirebaseDatabase.getInstance();
        reference = database.getReference().child("stations");
        detailmap = new HashMap<>();
        names = new ArrayList<>();
        trainName = new ArrayList<>();
        listView = findViewById(R.id.listview);
        listView.setVisibility(View.INVISIBLE);




        contactTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
                View vi = inflater.inflate(R.layout.contact_us,null);
                popupWindow = new PopupWindow(vi, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


                popupWindow.showAtLocation(layout, Gravity.CENTER,0,0);
            }
        });








       



        aboutus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
                View vi = inflater.inflate(R.layout.about_us,null);
                 popupWindow1= new PopupWindow(vi, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


                popupWindow1.showAtLocation(layout, Gravity.CENTER,0,0);
            }
        });

        int result = ContextCompat.checkSelfPermission(this,ACCESS_FINE_LOCATION);
        if(result == PackageManager.PERMISSION_GRANTED){

        }else {
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 121);
        }


        reference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> iterable = dataSnapshot.getChildren();
                for (DataSnapshot snapshot : iterable) {
                    detailmap.put(snapshot.getKey(), snapshot.getValue().toString());
                    names.add(snapshot.getKey());
                }
                Log.e("detailmap", detailmap.toString());

                if (gotvalue == 1) {
                    for (int i = 0; i < detailmap.size(); i++) {
                        String[] latlng = detailmap.get(names.get(i)).split(",");

                        Log.e("crush",latlng[0]+"    "+latlng[1]);


                        destinationpoint = Point.fromLngLat(Double.parseDouble(latlng[0]), Double.parseDouble(latlng[1]));






                        // Log.e("distance", String.valueOf(distance(originpoint.latitude(), originpoint.longitude(), destinationpoint.latitude(), destinationpoint.longitude(), "K")));




                        GeoJsonSource source = new GeoJsonSource(String.valueOf(i), Feature.fromGeometry(destinationpoint));
                        mapboxMap.getStyle().addSource(source);


                        //add marker on map

                        getTrainByStation(names.get(i));



                    }

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        mapview.onCreate(savedInstanceState);
        mapview.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapbox) {
                mapboxMap = mapbox;


                Style.Builder style = new Style.Builder().fromUri(Style.MAPBOX_STREETS)
                        .withImage("ORIGIN_ICON_ID", Objects.requireNonNull(BitmapUtils.getBitmapFromDrawable(
                                getResources().getDrawable(R.drawable.red_marker))));


                mapboxMap.setStyle(style, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        // Toast.makeText(MainActivity.this, "MapLoaded Successfully", Toast.LENGTH_SHORT).show();
                        callback = new LocationChangeListeningActivityLocationCallback(MainActivity.this, style);
                        enableLocationComponent(style);


                    }
                });
            }
        });


        srcedt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                listView.setVisibility(View.VISIBLE);
                String word = s.toString();
                ArrayList <String> finallist = new ArrayList<>();
                if (!word.isEmpty()){

                    for(int i = 0;i<trainName.size();i++){
                        if(trainName.get(i).toLowerCase().contains(s.toString().toLowerCase())){
                            finallist.add(trainName.get(i));
                        }
                    }
                    if(finallist.size()!=0){
                        listView.setAdapter(new ListviewAdapter(MainActivity.this,finallist));
                    }else {
                        listView.setAdapter(null);
                        listView.setVisibility(View.INVISIBLE);
                    }

                }else {
                    listView.setAdapter(null);
                    listView.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    ArrayList<String> trainName;

    void getTrainByStation(String s) {

        HashMap<String, GeoJsonSource> sources = new HashMap<>();
        database.getReference().child("Trains").child(s).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String name = ds.getKey();


                    String latlongstr = dataSnapshot.child(name).getValue().toString();

                    trainName.add(name);
                    Log.e("name",name);

                    double lat = Double.parseDouble(latlongstr.split(",")[0]);
                    double lng = Double.parseDouble(latlongstr.split(",")[1]);


                    if (mapboxMap != null) {
                        mapboxMap.getStyle().addImage(("marker_icon" + s + name), BitmapFactory.decodeResource(
                                getResources(), R.drawable.red_marker));

                        GeoJsonSource source = new GeoJsonSource("source-id" + s + name, Feature.fromGeometry(Point.fromLngLat(lng, lat)));

                        mapboxMap.getStyle().addSource(source);

                        SymbolLayer layer = new SymbolLayer("layer-id" +s + name, "source-id" +s + name)
                                .withProperties(PropertyFactory.textField(name),
                                        PropertyFactory.iconImage("marker_icon" +s + name),
                                        PropertyFactory.iconIgnorePlacement(true),
                                        PropertyFactory.iconAllowOverlap(true));



                        mapboxMap.getStyle().addLayer(layer);


                        sources.put("source-id" +s + name, source);

                    }

                    database.getReference().child("Trains").child(s).child(name).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                            String latlongstr = dataSnapshot.getValue().toString();

                            double lat = Double.parseDouble(latlongstr.split(",")[0]);
                            double lng = Double.parseDouble(latlongstr.split(",")[1]);


                            sources.get("source-id" + s  + name).setGeoJson(Point.fromLngLat(lng, lat));


                            Log.e("datavaluechanged", "train location changed");


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    /**
     * Initialize the Maps SDK's LocationComponent
     */
    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            // Set the LocationComponent activation options
            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(this, loadedMapStyle)
                            .useDefaultLocationEngine(false)
                            .build();

            // Activate with the LocationComponentActivationOptions object
            locationComponent.activateLocationComponent(locationComponentActivationOptions);

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            initLocationEngine();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    /**
     * Set up the LocationEngine and the parameters for querying the device's location
     */
    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine.requestLocationUpdates(request, callback, getMainLooper());

        locationEngine.getLastLocation(callback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, "Explanation needed", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {




            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(this, "Location Permission Not Granted.", Toast.LENGTH_LONG).show();
        }

        Intent mStartActivity = new Intent(MainActivity.this, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(MainActivity.this, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)MainActivity.this.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);

    }

    private class LocationChangeListeningActivityLocationCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<MainActivity> activityWeakReference;
        Style style;

        LocationChangeListeningActivityLocationCallback(MainActivity activity, Style style) {
            this.activityWeakReference = new WeakReference<>(activity);
            this.style = style;
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location has changed.
         *
         * @param result the LocationEngineResult object which has the last known location within it.
         */
        @SuppressLint("LogNotTimber")
        @Override
        public void onSuccess(LocationEngineResult result) {
            MainActivity activity = activityWeakReference.get();

            if (activity != null) {
                Location location = result.getLastLocation();

                if (location == null) {
                    return;
                }
                // Create a Toast which displays the new location's coordinates
                //Toast.makeText(activity,"new Location"+String.valueOf(result.getLastLocation().getLatitude())+String.valueOf(result.getLastLocation().getLongitude()),Toast.LENGTH_SHORT).show();
                Log.e("new Location", String.valueOf(result.getLastLocation().getLatitude()) + result.getLastLocation().getLongitude());
                //activity.databaseReference.child("loc").setValue(result.getLastLocation().getLatitude()+","+result.getLastLocation().getLongitude());
                // Pass the new location to the Maps SDK's LocationComponent


                if (finalint == 1) {
                    originpoint = Point.fromLngLat(result.getLastLocation().getLatitude(), result.getLastLocation().getLongitude());
                    Log.e("origin latitude", String.valueOf(originpoint.latitude()));
                    Log.e("origin longitude", String.valueOf(originpoint.longitude()));

                    CameraPosition position = new CameraPosition.Builder()
                            .target(new LatLng(result.getLastLocation().getLatitude(), result.getLastLocation().getLongitude())) // Sets the new camera position
                            .zoom(12) // Sets the zoom
                            .bearing(180) // Rotate the camera
                            .tilt(30) // Set the camera tilt
                            .build(); // Creates a CameraPosition from the builder

                    activity.mapboxMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(position), 3000);
                    finalint++;
                }


                if (activity.mapboxMap != null && result.getLastLocation() != null) {
                    activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                }
            }
        }

        @Override
        public void onFailure(@NonNull Exception exception) {
            MainActivity activity = activityWeakReference.get();
            if (activity != null) {
                Toast.makeText(activity, exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }




    public class ListviewAdapter extends ArrayAdapter<String> {
        public ListviewAdapter(Context context, ArrayList<String> users) {
            super(context, 0, users);
        }


        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


            final String model = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.itemlayout, parent, false);
            }
            // Lookup view for data population

            TextView tvHome = convertView.findViewById(R.id.itemid);
            // Populate the data into the template view using the data object

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    srcedt.setText(model);
                    DatabaseReference tempref = FirebaseDatabase.getInstance().getReference().child("Trains");
                    tempref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                String name = ds.getKey();

                                tempref.child(name).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            String train = ds.getKey();

                                            if(train.toLowerCase().contains(model.toLowerCase())){
                                                String value = ds.getValue().toString();
                                                Log.e("value",value);
                                                String [] latlng = value.split(",");

                                                double lat = Double.parseDouble(latlng[0]);
                                                double lon = Double.parseDouble(latlng[1]);

                                                CameraPosition position = new CameraPosition.Builder()
                                                        .target(new LatLng(lat, lon))
                                                        .zoom(10)
                                                        .tilt(20)
                                                        .bearing(180)
                                                        .build();

                                                mapboxMap.animateCamera(CameraUpdateFactory
                                                        .newCameraPosition(position), 5000);
                                                listView.setVisibility(View.INVISIBLE);


                                                InputMethodManager imm = (InputMethodManager) MainActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                                                //Find the currently focused view, so we can grab the correct window token from it.
                                                View view = MainActivity.this.getCurrentFocus();
                                                //If no view currently has focus, create a new one, just so we can grab a window token from it
                                                if (view == null) {
                                                    view = new View(MainActivity.this);
                                                }
                                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);



                                            }

                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            });

            tvHome.setText(model);
            // Return the completed view to render on screen
            return convertView;
        }
    }

    @Override
    public void onBackPressed() {
        if(popupWindow.isShowing()){
            popupWindow.dismiss();
        }else if(popupWindow1.isShowing()){
            popupWindow1.dismiss();
        }else {
            super.onBackPressed();
        }
    }
}
