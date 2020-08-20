package de.androidnewcomer.weginsbuero;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUESTCODE_PERMISSIONS = 222;
    private MapView mapView;
    private WegHandler handler = new WegHandler();
    private MyLocationNewOverlay myLocationOverlay;

    private class WegHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            zeigeWeg();
        }
    }

    private void zeigeWeg() {
        List<Location> weg = WegAufzeichnungsService.weg;
        if(!weg.isEmpty()) {
            mapView.getOverlayManager().clear();
            if(weg.size()>1) {
                PathOverlay overlay = new PathOverlay(Color.BLUE);
                for(int i=0; i<weg.size(); i++) {
                    GeoPoint point = new GeoPoint(weg.get(i));
                    overlay.addPoint(point);
                }
                mapView.getOverlayManager().add(overlay);
            }
            mapView.getOverlays().add(myLocationOverlay);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.stop).setOnClickListener(this);

        mapView =  findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(16);
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this),mapView);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)  {
            findViewById(R.id.start).setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE}, REQUESTCODE_PERMISSIONS);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        zeigeWeg();
        WegAufzeichnungsService.updateHandler = handler;
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.start) {
            startService(new Intent(this, WegAufzeichnungsService.class));
            WegAufzeichnungsService.updateHandler = handler;
        }
        if(view.getId()==R.id.stop) {
            stopService(new Intent(this, WegAufzeichnungsService.class));
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode==REQUESTCODE_PERMISSIONS && permissions.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
            findViewById(R.id.start).setEnabled(true);
        }
    }
}