package com.example.smartalert;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_menu);
    }

    public void disaster(View view){
        Intent intent = new Intent(this, viewDangers.class);
        startActivity(intent);

    }
    public void dangers(View view){
        Intent intent=new Intent(this, highDanger.class);
        startActivity(intent);
    }
public void logout(View view){
    Intent intent=new Intent(this, LoginPage.class);
    startActivity(intent);
    finish();
    Toast.makeText(this,R.string.logout,Toast.LENGTH_SHORT).show();
}

}