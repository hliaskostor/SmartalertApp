package com.example.smartalert;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Locale;

public class LoginPage extends AppCompatActivity {
    EditText username, password;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        String language = getIntent().getStringExtra("Language");
        setLocale(language);
        password = findViewById(R.id.password);
        username = findViewById(R.id.email);
        auth = FirebaseAuth.getInstance();
    }

    public void back(View view) {
        Intent intent = new Intent(LoginPage.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void signin(View view) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersCollection = db.collection("users");
        CollectionReference adminCollection = db.collection("admin");
        String enteredUsername = username.getText().toString().trim();
        String enteredPassword = password.getText().toString().trim();
        if (!enteredUsername.isEmpty() && !enteredPassword.isEmpty()) {
            usersCollection.whereEqualTo("username", enteredUsername)
                    .whereEqualTo("password", enteredPassword)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                String userToken = tokenusr();
                                Intent intent = new Intent(LoginPage.this, UserMenu.class);
                                intent.putExtra("username", enteredUsername);
                                intent.putExtra("token", userToken);
                                startActivity(intent);
                                showMessage(getString(R.string.suclogin), getString(R.string.suclogin));
                            } else {
                                adminLog(enteredUsername, enteredPassword);
                            }
                        }
                    });
        } else {
            showMessage(getString(R.string.erlogin), getString(R.string.erlogin));
        }
    }

    private void adminLog(String username, String password) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference adminCollection = db.collection("admin");

        adminCollection.whereEqualTo("username", username)
                .whereEqualTo("password", password)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> adminTask) {
                        if (adminTask.isSuccessful() && !adminTask.getResult().isEmpty()) {
                            String userToken = tokenusr();
                            Intent intent = new Intent(LoginPage.this, AdminMenu.class);
                            intent.putExtra("username", username);
                            intent.putExtra("token", userToken);
                            startActivity(intent);
                            showMessage(getString(R.string.suclogin), getString(R.string.suclogin));
                        } else {
                            showMessage(getString(R.string.erlogin), getString(R.string.erlogin));
                        }
                    }
                });
    }

    private String tokenusr() {
        SharedPreferences sharedPreferences = getSharedPreferences("users", Context.MODE_PRIVATE);
        return sharedPreferences.getString("token", "");
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

        }
    }

    void showMessage(String title, String message) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }
}
