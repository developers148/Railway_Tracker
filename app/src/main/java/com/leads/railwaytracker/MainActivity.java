package com.leads.railwaytracker;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.mapbox.mapboxsdk.annotations.BaseMarkerOptions;
import com.mapbox.mapboxsdk.annotations.Marker;
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


import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;


//saydabad jonopoder mor                            overbridge er niche ctg counter gular shamne
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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



    @SuppressLint("LogNotTimber")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_main);
        mapview = findViewById(R.id.mapview);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference().child("stations");
        detailmap = new HashMap<>();
        names = new ArrayList<>();

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
                        destinationpoint = Point.fromLngLat(Double.parseDouble(latlng[0]), Double.parseDouble(latlng[1]));


                       // Log.e("distance", String.valueOf(distance(originpoint.latitude(), originpoint.longitude(), destinationpoint.latitude(), destinationpoint.longitude(), "K")));

                        if (distance(originpoint.latitude(), originpoint.longitude(), destinationpoint.latitude(), destinationpoint.longitude(), "K") <= 5.0000) {

                            GeoJsonSource source = new GeoJsonSource(String.valueOf(i), Feature.fromGeometry(destinationpoint));
                            mapboxMap.getStyle().addSource(source);


                            //add marker on map


                            Log.e("distance", "you are close to " + names.get(i) + " Distance " + distance(originpoint.latitude(), originpoint.longitude(), destinationpoint.latitude(), destinationpoint.longitude(), "K"));

                            getTrainByStation(names.get(i));

                        }
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
    }




    void getTrainByStation(String s) {

        HashMap<String,GeoJsonSource> sources = new HashMap<>();
        database.getReference().child("Trains").child(s).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {




                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String name = ds.getKey();


                    String latlongstr = dataSnapshot.child(name).getValue().toString();

                    double lat = Double.parseDouble(latlongstr.split(",")[0]);
                    double lng = Double.parseDouble(latlongstr.split(",")[1]);




                    if(mapboxMap!=null){
                        mapboxMap.getStyle().addImage(("marker_icon"+name), BitmapFactory.decodeResource(
                                getResources(), R.drawable.red_marker));

                        GeoJsonSource source = new GeoJsonSource("source-id"+name, Feature.fromGeometry(Point.fromLngLat(lng,lat)));

                        mapboxMap.getStyle().addSource(source);

                        SymbolLayer layer = new SymbolLayer("layer-id"+name, "source-id"+name)
                                .withProperties(PropertyFactory.iconImage("marker_icon"+name),
                                PropertyFactory.iconIgnorePlacement(true),
                                PropertyFactory.iconAllowOverlap(true));

                        
                        mapboxMap.getStyle().addLayer(layer);


                        sources.put("source-id"+name,source);

                    }

                    database.getReference().child("Trains").child(s).child(name).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {




                            String latlongstr = dataSnapshot.getValue().toString();

                            double lat = Double.parseDouble(latlongstr.split(",")[0]);
                            double lng = Double.parseDouble(latlongstr.split(",")[1]);


                            sources.get("source-id"+name).setGeoJson(Point.fromLngLat(lng, lat));


                            Log.e("datavaluechanged","train location changed");


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


    void AddMarker(MarkerOptions markerOptions) {

        mapboxMap.addMarker(markerOptions);
    }
    void RemoveMarker(MarkerOptions markerOptions) {

        mapboxMap.removeMarker(markerOptions.getMarker());
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
        Toast.makeText(this, "R.string.user_location_permission_explanation", Toast.LENGTH_LONG).show();
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
            Toast.makeText(this, "R.string.user_location_permission_not_granted", Toast.LENGTH_LONG).show();
        }
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


    private double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        } else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            if (unit.equals("K")) {
                dist = dist * 1.609344;
            } else if (unit.equals("N")) {
                dist = dist * 0.8684;
            }
            return (dist);
        }
    }

}
