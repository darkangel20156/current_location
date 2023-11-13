package com.example.current_location;

import static android.content.ContentValues.TAG;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


public class MainActivity extends AppCompatActivity {
    private String locationIsNear = null;
    private String locationUser = null;

    // Firebase
//    private FirebaseDatabase mFirebaseDatabase;
//    private DatabaseReference mDatabaseReference;

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseFirestore mFirebaseFirestore = FirebaseFirestore.getInstance();

        CollectionReference collectionRef = mFirebaseFirestore.collection("locations");

        collectionRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Thêm trường mới cho mỗi tài liệu
                        document.getReference().update("locationIsNear", "true");
                    }
                })
                .addOnFailureListener(e -> {
                    // Xử lý lỗi
                });

        // Check for location permissions
        if (checkLocationPermissions()) {
            // Permission already granted, start location updates
            startLocationUpdates();
        } else {
            // Permission not granted, request it
            requestLocationPermissions();
        }
    }

    private boolean checkLocationPermissions() {
        for (String permission : LOCATION_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this, LOCATION_PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    private void startLocationUpdates() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Handle location updates
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                locationUser = String.valueOf(latitude)  + " & " + String.valueOf(longitude);

//                locationUser = latitude + "" + ", " + longitude + "";
                Log.d(TAG, "User is at: " + locationUser);
                // Use latitude and longitude as needed
                updateLocationInfo(latitude, longitude);

                checkLocation(latitude, longitude);
                Log.d(TAG, "is near?: " + locationIsNear);
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

        // Check for network provider availability
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // Use the network provider for location updates
            try {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // If the network provider is not available, use the GPS provider
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        } else {
            // Handle the case when neither network nor GPS providers are available
            Toast.makeText(this, "Location providers are not available", Toast.LENGTH_SHORT).show();
        }
    }
    private void updateLocationInfo(double latitude, double longitude) {
        // Update your UI or perform any action with the new location information
        String locationInfo = "Latitude: " + latitude + "\nLongitude: " + longitude;
        TextView locationTextView = findViewById(R.id.locationTextView);
        locationTextView.setText(locationInfo);

        // Firebase
//        mDatabaseReference.child("locationUser").setValue(locationInfo);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (checkPermissionGranted(grantResults)) {
                // Permission granted, start location updates
                startLocationUpdates();
            } else {
                // Permission denied, show a message or handle it accordingly
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean checkPermissionGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void checkLocation(double xPosition, double yPosition) {
        double radius = 10.0;
        double distance = sqrt(pow(xPosition - 21.004010169142187 ,2) + pow(yPosition - 105.84266667946878 ,2) );

        System.out.print("xin chao" + distance);
        Log.d(TAG, "radius " + radius);
        Log.d(TAG, "distance " + distance);
        if (distance <= radius) {
            locationIsNear = "Yes";
            Toast.makeText(this, "User is near hear", Toast.LENGTH_LONG).show();
//            mDatabaseReference.child("locationIsNear").setValue("Yes");
        }
        else {
            locationIsNear = "No";
            Toast.makeText(this, "User is not near hear", Toast.LENGTH_LONG).show();
//            mDatabaseReference.child("locationIsNear").setValue("No");
        }
    }
}
