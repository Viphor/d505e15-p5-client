package com.d505e15;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import com.d505e15.gps.R;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;


public class ShowMapActivity extends AppCompatActivity {
    private static ShowMapActivity localActivity;

    public static ShowMapActivity getActivity() {
        return localActivity;
    }

    public List<GeoPoint> gpsList = new ArrayList<GeoPoint>();


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
        mapController.setZoom(15);

        gpsList.add(new GeoPoint(39.9877396, 116.4491));
        gpsList.add(new GeoPoint(39.9854851, 116.450905));
        gpsList.add(new GeoPoint(39.9869347, 116.459579));
        gpsList.add(new GeoPoint(39.9796066, 116.470894));
        gpsList.add(new GeoPoint(39.9829674, 116.476654));
        gpsList.add(new GeoPoint(39.978981, 116.483788));

        PathOverlay myPath = new PathOverlay(Color.BLACK, this);
        myPath.getPaint().setStrokeWidth(5);
        for(GeoPoint geoPoint : gpsList) {
            myPath.addPoint(geoPoint);

        }

        mapController.animateTo(gpsList.get(0));
        map.getOverlays().add(myPath);
    }
}




