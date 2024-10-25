package com.example.smartalert;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TotalMessages extends AppCompatActivity {

    SQLiteDatabase dangerDB;
    TextView totalMessagesTextView;
    TextView messTextView;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_messages);
        totalMessagesTextView = findViewById(R.id.totalMessagesTextView);
        messTextView = findViewById(R.id.messΤextView);
        dangerDB = openOrCreateDatabase("danger.db", MODE_PRIVATE, null);
        username = getIntent().getStringExtra("username");
        if (findTable(dangerDB, "messages")) {
            int totalMessages = allMessages(username);
            if (totalMessages > 0) {
                totalMessagesTextView.setText(getString(R.string.totalmessages) + " " + totalMessages);

                Cursor cursor = null;
                try {
                    cursor = dangerDB.rawQuery("SELECT message FROM messages WHERE username = ?", new String[]{username});
                    StringBuilder messageStringBuilder = new StringBuilder();
                    if (cursor != null && cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndex("message");
                        do {
                            String message = cursor.getString(columnIndex);
                            messageStringBuilder.append(message).append("\n\n");
                        } while (cursor.moveToNext());
                        messTextView.setText(messageStringBuilder.toString());
                    } else {
                        messTextView.setText(R.string.notFound);
                    }
                } catch (SQLiteException e) {
                    Toast.makeText(this, "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            } else {
                Toast.makeText(TotalMessages.this, R.string.notFound, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(TotalMessages.this, R.string.notFound, Toast.LENGTH_SHORT).show();
        }
    }
    private boolean findTable(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{tableName});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }



    public void back(View view) {
        Intent intent = new Intent(TotalMessages.this, UserMenu.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private int allMessages(String username) {
        int totalMessages = 0;
        Cursor cursor = dangerDB.rawQuery("SELECT COUNT(*) FROM messages WHERE username = ?", new String[]{username});
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    totalMessages = cursor.getInt(0);
                }
            } finally {
                cursor.close();
            }
        }
        return totalMessages;
    }
}
