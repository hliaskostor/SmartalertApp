package com.example.smartalert;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class UserMenu extends AppCompatActivity {
    static final int LOCATION_PERMISSION_REQUEST_CODE = 123;

    LocationManager locationManager;
    LocationListener locationListener;
    TextView welcome,usernameShow;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_menu);
        String username = getIntent().getStringExtra("username");
        welcome= findViewById(R.id.welcomeuser);
        usernameShow=findViewById(R.id.usernameShow);
        usernameShow.setText(username);

        token = getIntent().getStringExtra("token");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }

    }
    public void total(View view){
        Intent intent=new Intent(UserMenu.this,TotalMessages.class);
        intent.putExtra("username", usernameShow.getText().toString());
        startActivity(intent);
    }
    public void logout(View view){
        Intent intent=new Intent(this, LoginPage.class);
        startActivity(intent);
        finish();
        Toast.makeText(this,R.string.logout,Toast.LENGTH_SHORT).show();
    }

    public void submit(View view) {
        Intent intent = new Intent(UserMenu.this, Submit.class);
        intent.putExtra("username", usernameShow.getText().toString());
        intent.putExtra("token", token);
        startActivity(intent);
    }

    public void view(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation != null) {
            double latitude = lastKnownLocation.getLatitude();
            double longitude = lastKnownLocation.getLongitude();
            Intent intent = new Intent(UserMenu.this, ShowNotify.class);
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);
            intent.putExtra("username", usernameShow.getText().toString());
            startActivity(intent);
        } else {
            Toast.makeText(this,R.string.enableGPS , Toast.LENGTH_SHORT).show();
        }
    }

}
