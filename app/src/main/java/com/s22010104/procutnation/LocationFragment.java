package com.s22010104.procutnation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class LocationFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private LocationCallback locationCallback;
    private Location lastSearchLocation;

    private static final float MIN_DISTANCE_FOR_UPDATE_METERS = 500; // Only search again if user moves 500m

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                startLocationUpdates();
            } else {
                Toast.makeText(getContext(), "Location permission denied.", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        view.findViewById(R.id.recenterButton).setOnClickListener(v -> {
            lastSearchLocation = null; // Force a new search on next location update
            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.zoomTo(13f));
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        // This callback now runs continuously to provide live updates
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult.getLastLocation() != null) {
                    Location currentLocation = locationResult.getLastLocation();

                    // Animate camera only on the first update or when recentering
                    if (lastSearchLocation == null) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 13f));
                    }

                    // Check if the user has moved a significant distance to trigger a new search
                    if (lastSearchLocation == null || currentLocation.distanceTo(lastSearchLocation) > MIN_DISTANCE_FOR_UPDATE_METERS) {
                        lastSearchLocation = currentLocation; // Update the last search location

                        mMap.clear(); // Clear all old markers
                        Toast.makeText(getContext(), "Finding nearby study spots...", Toast.LENGTH_SHORT).show();

                        // --- UPDATED: Perform three separate searches ---
                        findNearbyPlaces(currentLocation, "library", BitmapDescriptorFactory.HUE_BLUE);
                        findNearbyPlaces(currentLocation, "cafe", BitmapDescriptorFactory.HUE_ORANGE); // Orange is a good substitute for brown
                        findNearbyPlaces(currentLocation, "park", BitmapDescriptorFactory.HUE_GREEN);
                    }
                }
            }
        };

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 20000) // Every 20 seconds
                .setMinUpdateIntervalMillis(10000) // At least 10 seconds apart
                .build();

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    // --- UPDATED: A single, reusable method to find places of a specific type ---
    private void findNearbyPlaces(Location location, String keyword, float markerColor) {
        String apiKey = getString(R.string.google_maps_key);
        // --- FIXED: Changed from a strict 'type' search to a more flexible 'keyword' search ---
        String urlString = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=" + location.getLatitude() + "," + location.getLongitude() +
                "&radius=6000" +
                "&keyword=" + keyword + // Using keyword instead of type
                "&key=" + apiKey;

        new Thread(() -> {
            String data = downloadUrl(urlString);
            if (data != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> parseAndAddMarkers(data, markerColor));
            }
        }).start();
    }

    // --- UPDATED: A single, reusable method to parse the results and add colored markers ---
    private void parseAndAddMarkers(String jsonData, float markerColor) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray results = jsonObject.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                JSONObject place = results.getJSONObject(i);
                JSONObject location = place.getJSONObject("geometry").getJSONObject("location");
                LatLng latLng = new LatLng(location.getDouble("lat"), location.getDouble("lng"));
                String name = place.getString("name");

                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(name)
                        .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PlacesAPI", "Error parsing places for color " + markerColor);
        }
    }

    private String downloadUrl(String strUrl) {
        String data = "";
        try (InputStream iStream = new URL(strUrl).openStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(iStream))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
        } catch (Exception e) {
            Log.e("DownloadUrl", "Error downloading URL: " + e.toString());
        }
        return data;
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop location updates when the fragment is not visible to save battery
        if (fusedLocationProviderClient != null && locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Resume location updates when the fragment becomes visible again
        if (mMap != null) {
            startLocationUpdates();
        }
    }
}