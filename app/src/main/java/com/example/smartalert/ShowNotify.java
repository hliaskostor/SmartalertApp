package com.example.smartalert;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ShowNotify extends AppCompatActivity {

    int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    double MAX_DISTANCE = 15.0;

    LinearLayout notificationLayout;
    FirebaseFirestore db;
    FusedLocationProviderClient fusedLocationClient;
    LocationCallback locationCallback;
    TextView showDanger;
    List<String> notificationList;
    SQLiteDatabase dangerDB;
    String username;
    Set<String> notificationSet;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_notify);
        db = FirebaseFirestore.getInstance();
        username = getIntent().getStringExtra("username");
        dangerDB = openOrCreateDatabase("danger.db", MODE_PRIVATE, null);
        notificationLayout = findViewById(R.id.notificationLayout);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        showDanger = findViewById(R.id.showDanger);
      notificationList=new ArrayList<>();
        notificationSet = new HashSet<>();
        findLocation();
        dangerDB.execSQL("CREATE TABLE IF NOT EXISTS messages (username TEXT, message TEXT)");

    }

    public void back(View view) {
        Intent intent = new Intent(ShowNotify.this, UserMenu.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    private void findLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                fireData(location.getLatitude(), location.getLongitude());
                            } else {
                            }
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        }
                    });
        } catch (SecurityException e) {

        }
    }

    private void fireData(double latitude, double longitude) {
        db.collection("notifications")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String city = documentSnapshot.getString("city");
                        String disasterType = documentSnapshot.getString("disasterType");
                        String messageText = documentSnapshot.getString("message");
                        Double notificationLat = documentSnapshot.getDouble("latitude");
                        Double notificationLong = documentSnapshot.getDouble("longitude");
                        String timestamp = documentSnapshot.getString("timestamp");
                        if (city != null && disasterType != null && notificationLat != null && notificationLong != null && messageText != null && timestamp != null) {
                            double distance = eventDistance(latitude, longitude, notificationLat, notificationLong);
                            if (distance <= MAX_DISTANCE) {
                                String newNotification = city + ":" + messageText;
                                String same = city + ":" + disasterType + ":" + timestamp;
                                if (!notificationSet.contains(same) && !notificationList.contains(newNotification)) {
                                    notificationSet.add(same);
                                    addNotification(city, messageText);
                                }
                            }
                        }
                    }

                    updateNotification();
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private double eventDistance(double userlat, double userlon, double loclat, double loclon) {
        final double R = 6371.0;

        double latDistance = Math.toRadians(loclat - userlat);
        double lonDistance = Math.toRadians(loclon - userlon);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(userlat)) * Math.cos(Math.toRadians(loclat))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;
        return distance;
    }
    private void addNotification(String city, String messageText) {
        String newNotification = city + ":" + messageText;
        if (!notificationSet.contains(newNotification)) {
            notificationSet.add(newNotification);
            Cursor cursor = dangerDB.rawQuery("SELECT COUNT(*) FROM messages WHERE username = ? AND message = ?",
                    new String[]{username, newNotification});
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst() && cursor.getInt(0) == 0) {
                        notificationList.add(newNotification);
                        String insertQuery = "INSERT INTO messages (username,message) VALUES (?,?)";
                        dangerDB.execSQL(insertQuery, new String[]{username, newNotification});
                    }
                } finally {
                    cursor.close();
                }
            }
        }
    }



    private void updateNotification() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String notification : notificationList) {
            stringBuilder.append(notification).append("\n\n");
        }
        showDanger.setText(stringBuilder.toString());
    }
    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    fireData(latitude, longitude);
                }
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestLocationUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                findLocation();
            }
        }
    }
}
