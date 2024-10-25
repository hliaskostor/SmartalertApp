package com.example.smartalert;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Details extends AppCompatActivity {

    int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    double MAX_DISTANCE = 15.0;

    FusedLocationProviderClient fusedLocationClient;
    FirebaseFirestore db;
    SQLiteDatabase dangerDB;
    TextView showName,showSurname, showCity, enterLatitude, enterLongitude, showDisasters, showDatetime;
    ImageView imageViewpic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();
        showCity = findViewById(R.id.showCity);
        enterLatitude = findViewById(R.id.enterLatitude);
        enterLongitude = findViewById(R.id.enterLongitude);
        showDisasters = findViewById(R.id.showDisasters);
        showDatetime = findViewById(R.id.showdatetime);
        imageViewpic=findViewById(R.id.imageViewpic);
        showName=findViewById(R.id.showName);
        showSurname=findViewById(R.id.showSurname);
        dangerDB = openOrCreateDatabase("danger.db", MODE_PRIVATE, null);
        dangerDB.execSQL("CREATE TABLE IF NOT EXISTS high ("+
                "city TEXT, " +
                "latitude TEXT, " +
                " longitude TEXT, " +
                "disaster_type," +
                "timestamp TEXT)");

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String name = extras.getString("name");
            String surname=extras.getString("surname");
            String city = extras.getString("city");
            String latitude = extras.getString("latitude");
            String longitude = extras.getString("longitude");
            String disasterType = extras.getString("disaster_type");
            String timestamp = extras.getString("timestamp");
            String imageUrl = extras.getString("image_url");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Picasso.get().load(imageUrl).into(imageViewpic);
            }
            showName.setText(name);
            showSurname.setText(surname);
            showCity.setText(city);
            enterLatitude.setText(latitude);
            enterLongitude.setText(longitude);
            showDisasters.setText(disasterType);
            showDatetime.setText(timestamp);
        }
        Map<String, String> disasterTranslations = new HashMap<>();
        disasterTranslations.put("Fire", "Πυρκαγιά");
        disasterTranslations.put("Flood", "Πλημμύρα");
        disasterTranslations.put("Earthquake", "Σεισμός");

    }
    public void send(View view) {
        String city = showCity.getText().toString();
        double latitude = Double.parseDouble(enterLatitude.getText().toString());
        double longitude = Double.parseDouble(enterLongitude.getText().toString());
        String disasterType = showDisasters.getText().toString();
        String timestamp = showDatetime.getText().toString();
        sendNotification(city, disasterType, latitude, longitude, timestamp);
    }

    public void reject(View view){
        String name = showName.getText().toString();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("disaster");
        Query query = dbRef.orderByChild("name").equalTo(name);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(Details.this, R.string.successrej, Toast.LENGTH_SHORT).show();
                                    Intent intent=new Intent(Details.this, viewDangers.class);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(Details.this, R.string.deletesub, Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    public void back(View view) {
        Intent intent = new Intent(Details.this, viewDangers.class);
        startActivity(intent);
        finish();
    }

    public void high(View view){
        String city = showCity.getText().toString();
        String disasterType = showDisasters.getText().toString();
        double latitude = Double.parseDouble(enterLatitude.getText().toString());
        double longitude = Double.parseDouble(enterLongitude.getText().toString());
        String timiestamp = showDatetime.getText().toString();
        Map<String, Object> high = new HashMap<>();
        high.put("city", city);
        high.put("disasterType", disasterType);
        high.put("latitude", latitude);
        high.put("longitude", longitude);
        high.put("timestamp", timiestamp);
        db.collection("High")
                .add(high)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(Details.this,R.string.highPrior,Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {

                });
        String insertQuery=String.format(Locale.getDefault(),"INSERT INTO high(city,disaster_type, latitude,longitude,timestamp) " +
                        "VALUES('%s', '%s', '%s','%s', '%s')", showCity.getText().toString(),showDisasters.getText().toString(),
                enterLatitude.getText().toString(),enterLongitude.getText().toString(),showDatetime.getText().toString());
        dangerDB.execSQL(insertQuery);
    }


    private void checkLocation() {
        String city = showCity.getText().toString();
        double latitude = Double.parseDouble(enterLatitude.getText().toString());
        double longitude = Double.parseDouble(enterLongitude.getText().toString());
        String disasterType = showDisasters.getText().toString();
        String timestamp = showDatetime.getText().toString();

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            eventLocation(city, latitude, longitude, disasterType, timestamp);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void eventLocation(String city, double disasterLatitude, double disasterLongitude, String disasterType, String timestamp) {
        try {
            fusedLocationClient.getLastLocation().addOnCompleteListener(this, task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    Location lastKnownLocation = task.getResult();
                    double currentLatitude = lastKnownLocation.getLatitude();
                    double currentLongitude = lastKnownLocation.getLongitude();

                    double distance = eventDistance(currentLatitude, currentLongitude, disasterLatitude, disasterLongitude);

                    if (distance <= MAX_DISTANCE) {
                        sendNotification(city,disasterType,currentLatitude,currentLongitude,timestamp);
                    }
                }
            });
        } catch (SecurityException e) {

        }
    }
    private void sendNotification(String city, String disasterType, double latitude, double longitude, String timestamp) {
        Map<String, String> translate_disaster = new HashMap<>();
        translate_disaster.put("Fire", "Πυρκαγιά");
        translate_disaster.put("Flood", "Πλημμύρα");
        translate_disaster.put("Earthquake", "Σεισμός");

        String disastertranslate = translate_disaster.getOrDefault(disasterType, disasterType);
        String disastertranslateEN = translate_disaster.entrySet().stream()
                .filter(entry -> entry.getValue().equals(disastertranslate))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(disasterType);

        String notificateGR = "";
        if (!disastertranslate.isEmpty()) {
            notificateGR = "Υπάρχει " + disastertranslate + " και ώρα " + timestamp + " στην περιοχή " + city +
                    ".Περιορίστε τις μετακινήσεις σας και ακολουθήστε τις οδηγίες των αρχών.\n";
        }

        String notificateEN = "";
        if (!disastertranslate.isEmpty()) {
            notificateEN = "There is a " + disastertranslateEN + " and time " + timestamp + " in the area of " + city +
                    ".Limit your movements and follow the authorities' instructions.\n";
        }

        String combinedMessage = notificateGR + notificateEN;

        Map<String, Object> notification = new HashMap<>();
        notification.put("city", city);
        notification.put("disasterType", disasterType);
        notification.put("message", combinedMessage);
        notification.put("latitude", latitude);
        notification.put("longitude", longitude);
        notification.put("timestamp", timestamp);

        db.collection("notifications")
                .add(notification)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Details.this, getString(R.string.sendNotify) + " " + MAX_DISTANCE +"km", Toast.LENGTH_SHORT).show();

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Details.this, "Failed to send notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocation();
            } else {
                Toast.makeText(this, "Location access permission is required to send notifications.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
