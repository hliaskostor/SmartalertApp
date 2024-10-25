package com.example.smartalert;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    LocationManager loc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loc=(LocationManager) getSystemService(LOCATION_SERVICE);

    }

    public void login(View view) {
        Intent intent = new Intent(MainActivity.this, LoginPage.class);
        intent.putExtra("Language", getCurrentLanguage());
        startActivity(intent);
    }
    public void change(View view) {
        toggleLanguage();
        restartActivity();
    }
    private void toggleLanguage() {
        Configuration configuration = getResources().getConfiguration();
        if (configuration.locale.getLanguage().equals("el")) {
            setLocale("en");
        } else {
            setLocale("el");

        }
    }
    public void register(View view){
        Intent intent = new Intent(this, CreateUser.class);
        startActivity(intent);
    }


    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
        getSharedPreferences( "Change", Context.MODE_PRIVATE)
                .edit()
                .putString("My_Lang", languageCode)
                .apply();
    }

    private String getCurrentLanguage() {
        Configuration configuration = getResources().getConfiguration();
        return configuration.locale.getLanguage();
    }

    private void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}
