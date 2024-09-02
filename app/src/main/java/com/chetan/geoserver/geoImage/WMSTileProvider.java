package com.chetan.geoserver.geoImage;


import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WMSTileProvider implements TileProvider {
    private final String baseUrl;
    private final int tileSize;

    public WMSTileProvider(String baseUrl, int tileSize) {
        this.baseUrl = baseUrl;
        this.tileSize = tileSize;
    }

    private static double tileXToLon(int x, int zoom) {
        return x / Math.pow(2, zoom) * 360.0 - 180;
    }

    private static double tileYToLat(int y, int zoom) {
        double n = Math.PI - 2.0 * Math.PI * y / Math.pow(2, zoom);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }

    //For "EPSG_4326"
    private String getBoundingBoxEPSG_3857(int x, int y, int zoom) {

        double initialResolution = 2 * Math.PI * 6378137 / tileSize;
        double originShift = 2 * Math.PI * 6378137 / 2.0;

        double resolution = initialResolution / Math.pow(2, zoom);
        double minX = x * tileSize * resolution - originShift;
        double maxY = originShift - y * tileSize * resolution;
        double maxX = (x + 1) * tileSize * resolution - originShift;
        double minY = originShift - (y + 1) * tileSize * resolution;
        return minX + "," + minY + "," + maxX + "," + maxY;
    }

    @Nullable
    @Override
    public Tile getTile(int x, int y, int zoom) {

        String bbox;

        //get bounding accordingly
        bbox = getBoundingBoxEPSG_3857(x, y, zoom);
        //bbox = getBoundingBoxEPSG_4326(x, y, zoom);

        String url = baseUrl + "&WIDTH=" + tileSize
                + "&HEIGHT=" + tileSize
                + "&BBOX=" + bbox;

        Log.d("WMSTileProvider", "getTile: " + url);
        byte[] tileData = downloadTile(url);
        return tileData == null ? TileProvider.NO_TILE : new Tile(tileSize, tileSize, tileData);
    }

    private byte[] downloadTile(String url) {
        HttpURLConnection connection = null;

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             InputStream ignored = new URL(url).openStream()) {

            URL urlObj = new URL(url);
            connection = (HttpURLConnection) urlObj.openConnection();

            // Set timeouts in milliseconds
            connection.setConnectTimeout(5000); // 5 seconds for connection timeout
            connection.setReadTimeout(5000);    // 5 seconds for read timeout

            connection.setRequestMethod("GET");
            connection.connect();

            byte[] buffer = new byte[8192]; // Increase buffer size for efficiency
            int bytesRead;

            while ((bytesRead = connection.getInputStream().read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();

        } catch (Exception e) {
            Log.e("WMSTileProvider", "Error downloading tile: " + e.getMessage());
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    //For "EPSG_3857"
    private String getBoundingBoxEPSG_4326(int x, int y, int zoom) {
        double minLon = tileXToLon(x, zoom);
        double maxLon = tileXToLon(x + 1, zoom);
        double minLat = tileYToLat(y + 1, zoom);
        double maxLat = tileYToLat(y, zoom);
        return minLon + "," + minLat + "," + maxLon + "," + maxLat;
    }
}