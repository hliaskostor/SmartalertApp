package com.example.smartalert;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class viewDangers extends AppCompatActivity {

    ListView listView;
    DatabaseReference dbRef;
    List<String> disasterList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_dangers);

        listView = findViewById(R.id.listView);
        dbRef = FirebaseDatabase.getInstance().getReference("disaster");
        disasterList = new ArrayList<>();

        listDisasters();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String disasterInfo = disasterList.get(position);
                String[] infoParts = disasterInfo.split(" \\| ");
                String disasterType = infoParts[0];
                String city = infoParts[1];
                String timestamp = infoParts[2];
                disasterDetails(disasterType, city, timestamp);
            }
        });
    }
    public void back(View view){
        Intent intent=new Intent(viewDangers.this, AdminMenu.class);
        startActivity(intent);
        finish();
    }
    private void listDisasters() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                disasterList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String disasterType = snapshot.child("disaster_type").getValue(String.class);
                    String city = snapshot.child("city").getValue(String.class);
                    String timestamp = snapshot.child("timestamp").getValue(String.class);
                    if (disasterType != null && city != null && timestamp != null) {
                        int userCount = totalSubmissions(dataSnapshot, disasterType, city);

                        Locale currentLocale = getResources().getConfiguration().locale;
                        String displayText;
                        if (currentLocale != null && currentLocale.getLanguage().equals("el")) {
                            displayText = disasterType + " | " + city + " | " + timestamp + " | Χρήστες: " + userCount;
                        } else {
                            displayText = disasterType + " | " + city + " | " + timestamp + " | Users: " + userCount;
                        }

                        disasterList.add(displayText);
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(viewDangers.this, android.R.layout.simple_list_item_1, disasterList);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase Database", "Error fetching disasters: ", databaseError.toException());
                Toast.makeText(viewDangers.this, "Error fetching disasters. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private int totalSubmissions(DataSnapshot dataSnapshot, String disasterType, String city) {
        int count = 0;
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            String showDisaster = snapshot.child("disaster_type").getValue(String.class);
            String showCity = snapshot.child("city").getValue(String.class);
            if (showDisaster != null && showCity != null &&
                    showDisaster.equals(disasterType) && showCity.equals(city)) {
                count++;
            }
        }
        return count;
    }

    private void disasterDetails(String disasterType, String city, String timestamp) {
        Intent intent = new Intent(viewDangers.this, Details.class);
        dbRef.orderByChild("disaster_type")
                .equalTo(disasterType)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String disasterCity = snapshot.child("city").getValue(String.class);
                            String disasterTimestamp = snapshot.child("timestamp").getValue(String.class);
                            if (disasterCity != null && disasterTimestamp != null &&
                                    disasterCity.equals(city) && disasterTimestamp.equals(timestamp)) {
                                String name = snapshot.child("name").getValue(String.class);
                                String surname = snapshot.child("surname").getValue(String.class);
                                String latitude = snapshot.child("latitude").getValue(String.class);
                                String longitude = snapshot.child("longitude").getValue(String.class);
                                String imageUrl = snapshot.child("image_url").getValue(String.class);
                                String userToken = snapshot.child("user_token").getValue(String.class);
                                intent.putExtra("name", name);
                                intent.putExtra("surname", surname);
                                intent.putExtra("city", city);
                                intent.putExtra("latitude", latitude);
                                intent.putExtra("longitude", longitude);
                                intent.putExtra("disaster_type", disasterType);
                                intent.putExtra("image_url", imageUrl);
                                intent.putExtra("user_token", userToken);
                                intent.putExtra("timestamp", timestamp);
                                startActivity(intent);
                                return;
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("Realtime Database", "Error getting disaster details: ", databaseError.toException());
                        Toast.makeText(viewDangers.this, "Error fetching disaster details. Please try again later.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
