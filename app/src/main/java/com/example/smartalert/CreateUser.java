package com.example.smartalert;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateUser extends AppCompatActivity {
    EditText email, password, username;
    FirebaseAuth auth;
    FirebaseUser user;
    SQLiteDatabase dangerDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);
        username = findViewById(R.id.editTextText5);
        password = findViewById(R.id.editTextTextPassword3);
        email = findViewById(R.id.editTextTextEmailAddress);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        String language = getIntent().getStringExtra("Language");
        setLocale(language);
        dangerDB = openOrCreateDatabase("danger.db", MODE_PRIVATE, null);

        dangerDB.execSQL("CREATE TABLE IF NOT EXISTS users (" +
                "email TEXT, " +
                "username TEXT, " +
                " password TEXT)");
    }


    public void create(View view) {
        if (!email.getText().toString().isEmpty() &&
                !password.getText().toString().isEmpty() &&
                !username.getText().toString().isEmpty()) {
            auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                user = auth.getCurrentUser();
                                saveUserToken(user.getUid());
                                updateUser(user, username.getText().toString());
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                DocumentReference usersRef = db.collection("users").document(user.getUid());
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("username", username.getText().toString());
                                userData.put("password", password.getText().toString());
                                usersRef.set(userData, SetOptions.merge());
                                String insertQuery = String.format(Locale.getDefault(),
                                        "INSERT INTO users (username,email,password) " +
                                                "VALUES ('%s', '%s', '%s')",
                                        username.getText().toString(), email.getText().toString(), password.getText().toString());
                                dangerDB.execSQL(insertQuery);



                                if (task.getException() != null) {
                                    showMessage(getString(R.string.success_message), task.getException().getLocalizedMessage());
                                } else {
                                    showMessage(getString(R.string.success_message), getString(R.string.success_message));
                                    Intent intent = new Intent(CreateUser.this, LoginPage.class);
                                    startActivity(intent);
                                }
                            } else {
                                showMessage(getString(R.string.error_message), task.getException().getLocalizedMessage());
                            }
                        }
                    });
        }
    }

    public void back(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void saveUserToken(String userId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                @Override
                public void onComplete(@NonNull Task<GetTokenResult> task) {
                    if (task.isSuccessful()) {
                        String token = task.getResult().getToken();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        DocumentReference userRef = db.collection("users").document(userId);
                        userRef.update("token", token);
                    } else {
                    }
                }
            });
        }
    }



    private void updateUser(FirebaseUser user, String username) {
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();
        user.updateProfile(request);
    }

    public void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null);
        builder.create().show();
    }

    private void setLocale(String languageCode) {
        if (languageCode != null) {
            Locale locale = new Locale(languageCode);
            Locale.setDefault(locale);

            Configuration configuration = new Configuration();
            configuration.locale = locale;

            getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
            getSharedPreferences("Change", MODE_PRIVATE)
                    .edit()
                    .putString("My_Lang", languageCode)
                    .apply();

            restartActivity();
        }
    }

    private void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}
