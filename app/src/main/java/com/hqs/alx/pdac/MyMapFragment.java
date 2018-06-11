package com.hqs.alx.pdac;


import android.location.Location;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyMapFragment extends Fragment implements OnMapReadyCallback{

    //the color for the polygon filling
    private static final int COLOR_BLACK_ARGB = 0x80000000;
    //the polyline pattern
    private static final PatternItem DOT = new Dot();
    private static final PatternItem GAP = new Gap(2);
    // Create a stroke pattern of a gap followed by a dot.
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);

    View view;
    MapView myMapView;
    GoogleMap myGooglMap;
    ArrayList<LatLng> polylineValues;
    PolylineOptions polylineOptions;
    Polyline polyline1;
    boolean polygonCreated = false;

    public MyMapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_my_map, container, false);

        //LatLang values of polygon dots
        polylineValues = new ArrayList<>();

        //creating the map
        myMapView = (MapView) view.findViewById(R.id.myMapView);
        myMapView.onCreate(null);
        myMapView.onResume();
        myMapView.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        MapsInitializer.initialize(getActivity());

        myGooglMap = googleMap;
        myGooglMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        setMarkers();

    }

    //this method put markers on the map
    private void setMarkers(){
        myGooglMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                return false;
            }
        });

        polylineOptions = new PolylineOptions();

        myGooglMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //if polygone wasnt created yet, it will place markers untill we press "make polygon" from the menu
                if(!polygonCreated){
                    polylineOptions = new PolylineOptions();
                    myGooglMap.addMarker(new MarkerOptions().position(latLng).title("" + polylineValues.size()).snippet("lat: " +latLng.latitude + "\n lng: " + latLng.longitude));
                    polylineValues.add(latLng);
                    polylineOptions.addAll(polylineValues);
                    polyline1 = myGooglMap.addPolyline(polylineOptions);
                    //if polygon was already created, it will will place a new marker and check if its inside polygone, if not, it will calculate the closest point
                }else{
                    myGooglMap.addMarker(new MarkerOptions().position(latLng).title("0").snippet("lat: " +latLng.latitude + "\n lng: " + latLng.longitude));
                    checkIfInsidePoly(latLng, polylineValues);
                }

            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.my_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            //here we create the polygone
            case R.id.createPolygon:
                Polygon polygon = myGooglMap.addPolygon(new PolygonOptions().addAll(polylineValues));
                polygon.setFillColor(COLOR_BLACK_ARGB);
                polygonCreated = true;
                Toast.makeText(getActivity(), "Create Polygon", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //this method finds the cosest point from a marker to polygone
    private void findDistance(LatLng latLngToCheck, ArrayList<LatLng> polylineValues) {

        float[] distance = new float[1];
        double minDistance = -1;
        LatLng newLatLang = null;


        for (int i = 0; i < polylineValues.size(); i++) {

            LatLng start = polylineValues.get(i);
            int nextPoint = i + 1;
            if (nextPoint >= polylineValues.size()) {
                nextPoint = 0;
            }

            LatLng end = polylineValues.get(nextPoint);

            // calculating the incline and the meeting point with Y of the line between "start" and "end"
            double m = (start.latitude - end.latitude) / (start.longitude - end.longitude);
            double b = ((start.longitude * end.latitude) - (end.longitude * start.latitude)) / (start.longitude - end.longitude);

            //finding the meeting point of the line (start to end) and the point we are checking
            double mylong = (latLngToCheck.longitude + (latLngToCheck.latitude * m) - (b * m)) / (m * m + 1);
            double mylat = m * mylong + b;

            //distance from meeting point to start of the line
            double distanceToStart = Math.sqrt((mylong - start.longitude) * (mylong - start.longitude) +
                    (mylat - start.latitude) * (mylat - start.latitude));
            //distance from meeting point to end of the line
            double distanceToEnd = Math.sqrt((mylong - end.longitude) * (mylong - end.longitude) +
                    (mylat - end.latitude) * (mylat - end.latitude));
            //distance from starting point to ending of the line
            double distanceStartEnd = Math.sqrt((start.latitude - end.latitude) * (start.latitude - end.latitude) +
                    (start.longitude - end.longitude) * (start.longitude - end.longitude));
            //distance from the point we are checking to the meeting point
            double minDistanceFromCheckPoint = Math.sqrt((latLngToCheck.longitude - mylong) * (latLngToCheck.longitude - mylong) +
                    (latLngToCheck.latitude - mylat) * (latLngToCheck.latitude - mylat));

            //finding the minimum distance from the checking point
            if (distanceToStart < distanceStartEnd && distanceToEnd < distanceStartEnd) {
                if (minDistanceFromCheckPoint < minDistance || minDistance == -1) {
                    minDistance = minDistanceFromCheckPoint;
                    newLatLang = new LatLng(mylat, mylong);
                }
            }
        }
        //placing the clossest meeting point on the map and adding polyline + calculating the distance with "Location.distanceBetween"
        myGooglMap.addMarker(new MarkerOptions().position(newLatLang));
        Polyline polyline = myGooglMap.addPolyline(new PolylineOptions().add(newLatLang).add(latLngToCheck));
        polyline.setPattern(PATTERN_POLYLINE_DOTTED);
        Location.distanceBetween(newLatLang.latitude, newLatLang.longitude, latLngToCheck.latitude, latLngToCheck.longitude, distance);
        float resultToShow = distance[0] / 1000;
        Toast.makeText(getActivity(), "Shortest Distnace: " + resultToShow, Toast.LENGTH_SHORT).show();
    }

    // a method to check if the point to check is inside poly or not
    private void checkIfInsidePoly(LatLng latLngToCheck, ArrayList<LatLng> values){

        double count = 0;
        double maxLat = -1;
        double minLat = values.get(0).latitude;
        double maxLong = -1;
        double minLong =  values.get(0).longitude;

        //creating a hypotetic square to check if the point we are checking are outside of polygone for sure
        for (int i = 0; i < values.size(); i++) {

            if(values.get(i).latitude > maxLat || maxLat == -1){
                maxLat = values.get(i).latitude;
            }
            if(values.get(i).latitude < minLat ){
                minLat = values.get(i).latitude;
            }

            if(values.get(i).longitude > maxLong || maxLong == -1){
                maxLong = values.get(i).longitude;
            }
            if(values.get(i).longitude < minLong ){
                minLong = values.get(i).longitude;
            }
        }

        //if the point to check is far from polygone (latitude>maxLat, longtitude>maxLog..etc..,
        // no need to do all the calculation because the point is outside the poly for sure
        if(latLngToCheck.latitude > maxLat || latLngToCheck.latitude < minLat || latLngToCheck.longitude > maxLong || latLngToCheck.longitude < minLong){
            Toast.makeText(getActivity(), "This point is not inside the polygon!", Toast.LENGTH_SHORT).show();
        }else{
            //the point to check might be inside or outside
            /*
            a way to check if the point inside polygon is to check how many meeting points there are between our point to polygon sides if creating a line from
            the point a far point (increasing its X  - longtitude
             */
            for (int i = 0; i < values.size(); i++) {

                LatLng start = values.get(i);
                int nextPoint = i + 1;
                if (nextPoint >= values.size()) {
                    nextPoint = 0;
                }
                LatLng end = values.get(nextPoint);

                // calculating the incline and the meeting point with Y of the line between "start" and "end"
                double m = (start.latitude - end.latitude) / (start.longitude - end.longitude);
                double b = ((start.longitude * end.latitude) - (end.longitude * start.latitude)) / (start.longitude - end.longitude);


                double hypotB = latLngToCheck.latitude;
                // m of the hypotetic line will always be 1 because the longtitude doesnt change

                double meetingPointLong = (latLngToCheck.latitude - b) / m;

                LatLng meetingPoint = new LatLng(latLngToCheck.latitude, meetingPointLong);

                if(meetingPointLong < maxLong && meetingPointLong > latLngToCheck.longitude){
                    if(meetingPoint.latitude < start.latitude && meetingPoint.latitude < end.latitude){
                        break;
                    }else{
                        count++;
                        myGooglMap.addMarker(new MarkerOptions().position(meetingPoint));
                        Polyline polyline = myGooglMap.addPolyline(new PolylineOptions().add(meetingPoint).add(latLngToCheck));
                        polyline.setPattern(PATTERN_POLYLINE_DOTTED);
                    }
                }
            }

            //if the number of meeting points is even - the point is outside of polygon
            if(count % 2 == 0){
                Toast.makeText(getActivity(), "not inside the polygon!", Toast.LENGTH_SHORT).show();
                Log.d("MyPoint ", " not inside poly");
                findDistance(latLngToCheck, values);
            // if the number of meeting points is not even - the point is inside the polygon
            }else{
                Toast.makeText(getActivity(), "inside the polygon!", Toast.LENGTH_SHORT).show();
                Log.d("MyPoint ", " inside poly");
            }
        }



    }
}
