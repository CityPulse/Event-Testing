package com.example.tanktoo.eventreportapp;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


/**
 * A simple {@link Fragment} subclass.
 */
public class ReportFragment extends Fragment implements View.OnClickListener{

    View view;
    MapView mapView;
    private GoogleMap googleMap;
    private LatLng eventLocation;
    SharedPreferences sharedPreferences;

    public ReportFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_report, container, false);

        MainActivity activity = ((MainActivity)getActivity());
        sharedPreferences = activity.getSharedPreferences(activity.getPreferences(), Context.MODE_PRIVATE);

        Button upButton = (Button) view.findViewById(R.id.test_button);
        upButton.setOnClickListener(this);

        eventLocation = ((MainActivity)getActivity()).getEventLocation();

        mapView = (MapView) view.findViewById(R.id.miniMapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();

        try{
            MapsInitializer.initialize((getActivity().getApplicationContext()));
        } catch(Exception e){
            e.printStackTrace();
        }

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                googleMap = map;
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLocation, 14));
                googleMap.addMarker(new MarkerOptions().position(eventLocation));
            }
        });

        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        if(mapView != null)
            mapView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mapView != null)
            mapView.onDestroy();
    }

    @Override
    public void onClick(View arg0) {

        Spinner typeSpinner = (Spinner) view.findViewById(R.id.spinner_event);
        Spinner levelSpinner = (Spinner) view.findViewById(R.id.spinner_level);

        Event event = new Event();
        event.setLatitude(this.eventLocation.latitude);
        event.setLongitude(this.eventLocation.longitude);
        event.setDate(Calendar.getInstance().getTime());
        event.setSource("USER_" +  sharedPreferences.getString("username", "testuser"));
        event.setLevel(Integer.parseInt(levelSpinner.getSelectedItem().toString()));
        event.setEventClass(typeSpinner.getSelectedItem().toString());
        event.setType("ec:TransportationEvent");

        //UUID must not start with a number
        String uuid = UUID.randomUUID().toString();
        while(Character.isDigit(uuid.charAt(0)))
            uuid = UUID.randomUUID().toString();

        event.setIdentifier("sao:" + uuid);

        EventParser eventParser = new EventParser();

        System.out.println("Send event: " + eventParser.parseEvent(event));

        ((MainActivity)getActivity()).sendNewEventMessage(eventParser.parseEvent(event));
        Snackbar.make(view, "Event sent", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        ((MainActivity) getActivity()).backToMain();
    }

}
