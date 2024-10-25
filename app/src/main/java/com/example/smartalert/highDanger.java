package com.example.smartalert;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class highDanger extends AppCompatActivity {
    SQLiteDatabase dangerDB;
    TextView highText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_danger);
        highText = findViewById(R.id.highText);
        StringBuilder messageStringBuilder;
        try {
            dangerDB = openOrCreateDatabase("danger.db", MODE_PRIVATE, null);
            Cursor cursor = dangerDB.rawQuery("SELECT * FROM high;", null);
            messageStringBuilder = new StringBuilder();
            if (cursor != null && cursor.moveToFirst()) {
                int cityShow = cursor.getColumnIndex("city");
                int disasterType = cursor.getColumnIndex("disaster_type");
                int timestamp = cursor.getColumnIndex("timestamp");
                do {
                    String city = cursor.getString(cityShow);
                    String disaster = cursor.getString(disasterType);
                    String datetime = cursor.getString(timestamp);
                    messageStringBuilder.append(getString(R.string.cityHigh)).append(city).append("\n");
                    messageStringBuilder.append(getString(R.string.disasterHigh)).append(disaster).append("\n");
                    messageStringBuilder.append(getString(R.string.timestampHigh)).append(datetime).append("\n\n");
                } while (cursor.moveToNext());
                cursor.close();
            } else {
                Toast.makeText(highDanger.this,R.string.notFound,Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(highDanger.this,R.string.notFound,Toast.LENGTH_SHORT).show();
            return;
        }
        highText.setText(messageStringBuilder.toString());
    }


    public void back(View view){
        Intent intent= new Intent(highDanger.this, AdminMenu.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
