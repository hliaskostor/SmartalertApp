package com.example.smartalert;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Submit extends AppCompatActivity implements LocationListener {
    String[] disasterTypes = {"Fire", "Flood", "Earthquake"};
    int LOCATION_PERMISSION_REQUEST_CODE = 123;
    int RESULT_LOAD_IMG = 1;
    LocationManager locManager;
    TextView cityText, enterLatitude, enterLongitude, showuser;
    Geocoder geocoder;
    Button photoButton, submitButton;
    ImageView imageView2;
    Spinner spinner;
    EditText yourNameEditText, yourSurnameEditText;
    FirebaseFirestore db;
    String username;
    String userToken;
    SQLiteDatabase dangerDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit);
        Locale currentLocale = getResources().getConfiguration().locale;
        photoButton = findViewById(R.id.photoButton);
        showuser = findViewById(R.id.showuser);
        spinner = findViewById(R.id.spinner);
        yourNameEditText = findViewById(R.id.your_name);
        yourSurnameEditText = findViewById(R.id.your_surname);
        username = getIntent().getStringExtra("username");
        userToken = getIntent().getStringExtra("token");
        showuser.setText(username);
        submitButton = findViewById(R.id.submitButton);
        imageView2 = findViewById(R.id.imageView2);
        cityText = findViewById(R.id.cityText);
        enterLatitude = findViewById(R.id.enterLatitude);
        enterLongitude = findViewById(R.id.enterLongitude);

        db = FirebaseFirestore.getInstance();
        geocoder = new Geocoder(this, Locale.getDefault());
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(this, "Please enable GPS", Toast.LENGTH_SHORT).show();
            } else {
                locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            }
        }
        if (currentLocale != null && currentLocale.getLanguage().equals("el")) {
            disasterTypes = new String[]{"Πυρκαγιά", "Πλημμύρα", "Σεισμός"};
        } else {
            disasterTypes = new String[]{"Fire", "Flood", "Earthquake"};
        }
        ArrayAdapter<String> aa = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, disasterTypes);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(aa);
        dangerDB = openOrCreateDatabase("danger.db", MODE_PRIVATE, null);
        dangerDB.execSQL("CREATE TABLE IF NOT EXISTS disasters (" +
                "username TEXT, " +
                "name TEXT, " +
                "surname TEXT, " +
                "danger TEXT, " +
                "city TEXT, " +
                "latitude TEXT," +
                "longitude TEXT," +
                "image BLOB," +
                "timestamp TEXT)");
    }

    public void choose(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
    }

    public void back(View view) {
        Intent intent = new Intent(this, UserMenu.class);
        intent.putExtra("username", showuser.getText().toString());
        intent.putExtra("token", userToken);
        startActivity(intent);
    }

    public void submit(View view) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("disaster");
        Calendar calendar = Calendar.getInstance();
        String name = yourNameEditText.getText().toString().trim();
        String surname = yourSurnameEditText.getText().toString().trim();
        String disasterType = spinner.getSelectedItem().toString();
        String city = cityText.getText().toString().trim();
        String key = databaseRef.push().getKey();
        String newlatitude = enterLatitude.getText().toString().trim();
        String newlongitude = enterLongitude.getText().toString().trim();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String timestamp = dateFormat.format(calendar.getTime());
        Drawable drawable = imageView2.getDrawable();
        Bitmap bitmap = null;
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        }

        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("Images");
        String imageName = "image_" + System.currentTimeMillis() + ".png";
        StorageReference imageRef = storageRef.child(imageName);
        imageRef.putBytes(imageBytes)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        Map<String, Object> disaster = new HashMap<>();
                        disaster.put("username", username);
                        disaster.put("name", name);
                        disaster.put("surname", surname);
                        disaster.put("disaster_type", disasterType);
                        disaster.put("city", city);
                        disaster.put("longitude", newlongitude);
                        disaster.put("latitude", newlatitude);
                        disaster.put("timestamp", timestamp);
                        disaster.put("image_url", imageUrl);
                        disaster.put("user_token", userToken);
                        databaseRef.child(key).setValue(disaster).addOnCompleteListener(sql2 -> {
                            if (sql2.isSuccessful()) {
                                String insertQuery = String.format(Locale.getDefault(),
                                        "INSERT INTO disasters (username,name, surname, danger, city,latitude,longitude, image,timestamp) " +
                                                "VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', ?,'%s')",
                                        username, name, surname, disasterType, city, newlatitude, newlongitude, imageUrl, timestamp);
                                SQLiteStatement totalDisasters = dangerDB.compileStatement(insertQuery);
                                totalDisasters.bindBlob(1, imageBytes);
                                totalDisasters.executeInsert();
                                Toast.makeText(Submit.this, R.string.submit_success, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Submit.this, R.string.failed_submit, Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                });
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && data != null) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imageView2.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();

            }


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                } else {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                }
            }  ;
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        long timestamp = location.getTime();
        Date date = new Date(timestamp);
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String cityName = address.getLocality();
                String latitudeText = String.valueOf(latitude);
                String longitudeText = String.valueOf(longitude);
                cityText.setText(cityName);
                enterLatitude.setText(latitudeText);
                enterLongitude.setText(longitudeText);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        locManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
    }
}
