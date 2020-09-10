package com.example.rinachangboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // モード選択
        Button button_simple = findViewById(R.id.buttonSimpleMode);
        button_simple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SimpleModeActivity.class);
                intent.putExtra("debug", false);
                startActivity(intent);
            }
        });
        Button button_bluetooth = findViewById(R.id.buttonDebugMode);
        button_bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SimpleModeActivity.class);
                intent.putExtra("debug", true);
                startActivity(intent);
            }
        });
    }
}