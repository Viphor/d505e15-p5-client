package com.d505e15;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import com.d505e15.gps.R;
import com.d505e15.gps.UpdateRoadTask;

import java.util.ArrayList;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;


public class ShowMapActivity extends AppCompatActivity {
    private static ShowMapActivity localActivity;

    public static ShowMapActivity getActivity() {
        return localActivity;
    }




    ArrayList<OverlayItem> anotherOverlayItemArray;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        localActivity = this;

        setContentView(R.layout.activity_show_map);
        MapView map = (MapView) findViewById(R.id.mapView);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        MapController mapController = new MapController(map);
        mapController.setZoom(10);



        GeoPoint geoPoint01 = new GeoPoint(39.9877396, 116.4491);
        GeoPoint geoPoint02 = new GeoPoint(39.9854851, 116.450905);
        GeoPoint geoPoint03 = new GeoPoint(39.9869347, 116.459579);
        GeoPoint geoPoint04 = new GeoPoint(39.9796066, 116.470894);
        GeoPoint geoPoint05 = new GeoPoint(39.9829674, 116.476654);
        GeoPoint geoPoint06 = new GeoPoint(39.978981, 116.483788);
        mapController.setCenter(geoPoint01);
        PathOverlay myPath = new PathOverlay(Color.BLACK, this);
        myPath.addPoint(geoPoint01);
        myPath.addPoint(geoPoint02);
        myPath.addPoint(geoPoint03);
        myPath.addPoint(geoPoint04);
        myPath.addPoint(geoPoint05);
        myPath.addPoint(geoPoint06);
        map.getOverlays().add(myPath);
        
    }
}




