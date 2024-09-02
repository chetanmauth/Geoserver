
package com.chetan.geoserver.geoImage;


import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.chetan.geoserver.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlayOptions;

public class GeoServerImage extends AppCompatActivity implements OnMapReadyCallback {

    //FORMAT=image/png
    //use the geoserver url
    String url = "http://34.12.x.x:8080/geoserver/wms?REQUEST=GetMap&SERVICE=WMS&VERSION=1.3.0&FORMAT=image/png&STYLES=style&TRANSPARENT=true&LAYERS=layer&CQL_FILTER=location= 'brix.tif'&FORMAT_OPTIONS=dpi:200&srs=EPSG:3857";
    boolean  isMapChanged = false;
    private LatLng latLngCenter;
    private final float ZOOM_LEVEL = 10;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geoserver_image);
        initUI();

        CardView btnMapStyle = findViewById(R.id.btnMapStyle);
        btnMapStyle.setOnClickListener(v -> {
            if (isMapChanged) {
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                isMapChanged = false;
            } else {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                isMapChanged = true;
            }
            Toast.makeText(this, "Map Style Changed", Toast.LENGTH_SHORT).show();
        });
        latLngCenter = new LatLng(27.5154, 80.1791);
    }

    private void initUI() {
        SupportMapFragment mapFragment = (SupportMapFragment) this.getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        setMapData(url);
    }

    /**
     * set the map data
     */
    private void setMapData(String url) {

        if (mMap != null) {
            mMap.clear();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngCenter, ZOOM_LEVEL));
            WMSTileProvider tileProvider = new WMSTileProvider(url, 256);
            mMap.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider));
        }
    }
}