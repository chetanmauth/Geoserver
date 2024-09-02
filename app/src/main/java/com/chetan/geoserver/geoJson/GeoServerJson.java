package com.chetan.geoserver.geoJson;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.chetan.geoserver.R;
import com.chetan.geoserver.volley.VolleyRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.data.Geometry;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.geojson.GeoJsonLineString;
import com.google.maps.android.data.geojson.GeoJsonMultiLineString;
import com.google.maps.android.data.geojson.GeoJsonMultiPoint;
import com.google.maps.android.data.geojson.GeoJsonMultiPolygon;
import com.google.maps.android.data.geojson.GeoJsonPoint;
import com.google.maps.android.data.geojson.GeoJsonPolygon;
import com.google.maps.android.data.geojson.GeoJsonPolygonStyle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GeoServerJson extends AppCompatActivity implements OnMapReadyCallback {

    //Empty geoJson
    private final String emptyGeoJsonString = "{\n" +
            "\"type\": \"FeatureCollection\",\n" +
            "\"name\": \"jsontemplate\",\n" +
            "\"features\": [\n" +
            "{ \"type\": \"Feature\", \"properties\": { \"a1\": \"\", \"a2\": \"\", \"a3\": \"\", \"a4\": \"\"}, \"geometry\": null }\n" +
            "]\n" +
            "}";

    boolean isMapChanged = true;
    private GeoJsonLayer geoJsonLayer;
    private GeoJsonFeature geoJsonFeature = null;
    private String country = "India";
    private String state = "Karnataka";
    private String district = "Dakshina Kannada";
    private GoogleMap mMap;
    private LatLng latLngCenter = new LatLng(15.8497, 74.4977);
    private int density = 130;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geoserver_json);

        initUI();
    }

    private void initUI() {
        SupportMapFragment mapFragment = (SupportMapFragment) this.getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        CardView btnMapStyle = findViewById(R.id.btnMapStyle);
        btnMapStyle.setOnClickListener(v -> {
            if (isMapChanged) {
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                isMapChanged = false;
            } else {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                isMapChanged = true;
            }
        });
    }



    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(15.8497, 74.4977), 10));
        getGeoJsonData();
    }

    //outputFormat=application/json
    private String generateUrl() {

        return "http://34.12.x.x:8080/geoserver/wfs?service=WFS" +
                "&version=1.1.0" +
                "&request=GetFeature" +
                "&typename=coconut:district" +
                "&outputFormat=application/json" +
                "&srsname=EPSG:4326" +
                "&srsFormat=EPSG:4326";
    }

    //fetch the GeoServer data
    private void getGeoJsonData() {


        VolleyRequest request = new VolleyRequest(generateUrl(), response -> {
            if (response != null) {
                fetchGeoData(response);
            } else {
                //TODO show error data not available
            }
        }, error -> {
            //TODO show error data not available
        });
        request.SimpleVolleyGetRequest();
    }

    //fetch the GeoServer data in background
    private void fetchGeoData(Object response) {
        AsyncTask.execute(() -> {

            try {
                JSONObject jsonObject = new JSONObject(response.toString());
                geoJsonLayer = new GeoJsonLayer(mMap, jsonObject);

                for (GeoJsonFeature feature : geoJsonLayer.getFeatures()) {
                    if (feature.hasProperty("District")) {
                        if (feature.getProperty("District").equals(district)) {
                            System.out.println("found data");
                            geoJsonFeature = feature;
                            break;
                        }
                    }
                }

                setGeoJsonOnMap();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    //plotting polygon on map
    private void setGeoJsonOnMap() {
        runOnUiThread(() -> {
            if (geoJsonFeature != null) {
                try {
                    JSONObject jo1 = new JSONObject(emptyGeoJsonString);
                    geoJsonLayer = new GeoJsonLayer(mMap, jo1);
                    geoJsonLayer.addFeature(geoJsonFeature);
                    geoJsonLayer.addLayerToMap();

                    //fetch details
                    showFeatureDetails(geoJsonFeature);

                    GeoJsonPolygonStyle style = geoJsonLayer.getDefaultPolygonStyle();
                    style.setStrokeColor(Color.BLACK);
                    style.setStrokeWidth(3f);

                    //fill tile color
                    style.setFillColor(getColorForDensity(density));
                    System.out.println("density:: " + density);

                    mMap.moveCamera(CameraUpdateFactory
                            .newLatLngBounds(getBoundsFromFeature(geoJsonFeature), 20));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                //TODO show error data not available
            }

        });
    }

    //getting LatLng bounds to fit polygon in screen from selected GeoJsonFeature
    private LatLngBounds getBoundsFromFeature(GeoJsonFeature feature) {

        Geometry geometry = feature.getGeometry();
        List<LatLng> coordinates = new ArrayList<>();

        switch (geometry.getGeometryType()) {
            case "Point":
                coordinates.add(((GeoJsonPoint) geometry).getCoordinates());
                break;
            case "MultiPoint":
                List<GeoJsonPoint> points = ((GeoJsonMultiPoint) geometry).getPoints();
                for (GeoJsonPoint point : points) {
                    coordinates.add(point.getCoordinates());
                }
                break;
            case "LineString":
                coordinates.addAll(((GeoJsonLineString) geometry).getCoordinates());
                break;
            case "MultiLineString":
                List<GeoJsonLineString> lines = ((GeoJsonMultiLineString) geometry).getLineStrings();
                for (GeoJsonLineString line : lines) {
                    coordinates.addAll(line.getCoordinates());
                }
                break;
            case "Polygon":
                List<? extends List<LatLng>> lists =
                        ((GeoJsonPolygon) geometry).getCoordinates();
                for (List<LatLng> list : lists) {
                    coordinates.addAll(list);
                }
                break;
            case "MultiPolygon":
                List<GeoJsonPolygon> polygons = ((GeoJsonMultiPolygon) geometry).getPolygons();
                for (GeoJsonPolygon polygon : polygons) {
                    for (List<LatLng> list : polygon.getCoordinates()) {
                        coordinates.addAll(list);
                    }
                }
                break;
        }
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (int i = 0; i < coordinates.size(); i++) {
            boundsBuilder.include(coordinates.get(i));
        }
        return boundsBuilder.build();
    }

    //setting textviews details
    private void showFeatureDetails(GeoJsonFeature feature) {
        try {

            if (feature.hasProperty("Density (N")) {
                String treesDensity = feature.getProperty("Density (N");

                density = Integer.parseInt(treesDensity);

            }
        } catch (Exception e) {

        }
    }

    //get green color based on density value
    private int getColorForDensity(int density) {
        final int minDensity = 130;
        final int maxDensity = 200;

        final int[] minColor = {20, 220, 80};   // Light Green (RGB:20, 220, 80)
        final int[] maxColor = {20, 90, 40};    // Dark Green (RGB: 20, 90, 40)

        float ratio = (float) (density - minDensity) / (maxDensity - minDensity);

        ratio = Math.max(0, Math.min(1, ratio));

        int[] color = new int[3];
        for (int i = 0; i < 3; i++) {
            color[i] = Math.round(minColor[i] + ratio * (maxColor[i] - minColor[i]));
        }
        //fill transparency
        int alpha = (int) (255 * 0.90);
        return Color.argb(alpha, color[0], color[1], color[2]);
    }
}