package com.chetan.geoserver;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.chetan.geoserver.geoImage.GeoServerImage;
import com.chetan.geoserver.geoJson.GeoServerJson;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnGeoImg = findViewById(R.id.btnGeoImg);
        btnGeoImg.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), GeoServerImage.class));
        });

        Button btnGeoJson = findViewById(R.id.btnGeoJson);
        btnGeoJson.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), GeoServerJson.class));
        });

    }
}