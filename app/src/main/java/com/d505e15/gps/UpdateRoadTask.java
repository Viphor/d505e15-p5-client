package com.d505e15.gps;

import android.os.AsyncTask;
import android.widget.Toast;

import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

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

import com.d505e15.ShowMapActivity;
import com.d505e15.gps.R;
import java.util.ArrayList;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;
/**
 * Created by Michael on 09-12-2015.
 */

public class UpdateRoadTask extends AsyncTask<Object, Void, Road> {

    protected Road doInBackground(Object... params) {
        @SuppressWarnings("unchecked")
        ArrayList<GeoPoint> waypoints = (ArrayList<GeoPoint>)params[0];
        RoadManager roadManager = new OSRMRoadManager();


        return roadManager.getRoad(waypoints);
    }

    protected void onPostExecute(Road result, MapView map) {
       Road road = result;

        // showing distance and duration of the road
        Toast.makeText(ShowMapActivity.getActivity(), "distance="+road.mLength, Toast.LENGTH_LONG).show();
        Toast.makeText(ShowMapActivity.getActivity(), "durée="+road.mDuration, Toast.LENGTH_LONG).show();

        if(road.mStatus != Road.STATUS_OK)
            Toast.makeText(ShowMapActivity.getActivity(), "Error when loading the road - status="+road.mStatus, Toast.LENGTH_SHORT).show();
        Polyline roadOverlay = RoadManager.buildRoadOverlay(road,ShowMapActivity.getActivity());

        map.getOverlays().add(roadOverlay);
        map.invalidate();
        //updateUIWithRoad(result);
    }
}
