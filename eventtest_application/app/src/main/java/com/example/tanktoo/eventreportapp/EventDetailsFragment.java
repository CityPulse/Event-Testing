package com.example.tanktoo.eventreportapp;


import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;
import java.util.UUID;


/**
 * A simple {@link Fragment} subclass.
 */
public class EventDetailsFragment extends Fragment implements View.OnClickListener{

    View view;
    MapView mapView;
    private Event event;
    private GoogleMap googleMap;

    public EventDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_event_details, container, false);

        event = ((MainActivity)getActivity()).getSelectedEvent();
        if(event == null) {
            ((MainActivity) getActivity()).backToMain();
            return view;
        }

        mapView = (MapView) view.findViewById(R.id.details_miniMapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();

        try{
            MapsInitializer.initialize((getActivity().getApplicationContext()));
        } catch(Exception e){
            e.printStackTrace();
        }

        LatLng eventLocation = new LatLng(event.getLatitude(), event.getLongitude());

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                googleMap = map;
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(event.getLocation(), 14));
                googleMap.addMarker(new MarkerOptions().position(event.getLocation()));
            }
        });

        Button deleteButton = (Button) view.findViewById(R.id.details_frag_button_change);
        deleteButton.setOnClickListener(this);
        Button changeButton = (Button) view.findViewById(R.id.details_frag_button_delete);
        changeButton.setOnClickListener(this);

        //check if its auto generated event, no changes allowed then! (hide buttons)
        if(this.event.getEventSourceType().equals(EventSourceType.SENSOR)){
            deleteButton.setVisibility(View.GONE);
            changeButton.setVisibility(View.GONE);
        }
        TextView textView_id = (TextView) view.findViewById(R.id.details_frag_textView_id_value);
        textView_id.setText(event.getIdentifier());
        TextView textView_type = (TextView) view.findViewById(R.id.details_frag_textView_type_value);
        textView_type.setText(event.getType());
        TextView textView_class = (TextView) view.findViewById(R.id.details_frag_textView_class_value);
        textView_class.setText(event.getEventClass());
        Spinner spinner_level = (Spinner) view.findViewById(R.id.details_frag_spinner_level);
        spinner_level.setSelection(event.getLevel()-1);
        TextView textView_source = (TextView) view.findViewById(R.id.details_frag_textView_source_value);
        textView_source.setText(event.getSource());
        TextView textView_time = (TextView) view.findViewById(R.id.details_frag_textView_time_value);
        textView_time.setText(event.getStringDate());

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
    public void onClick(View v) {
        EventParser eventParser = new EventParser();

        switch (v.getId()) {
            case  R.id.details_frag_button_change: {
                // do something for button 1 click
                Spinner spinner_level = (Spinner) view.findViewById(R.id.details_frag_spinner_level);
                this.event.setLevel(Integer.parseInt(spinner_level.getSelectedItem().toString()));

                //send message
                ((MainActivity)getActivity()).sendMessage(eventParser.parseEvent(event));
                Snackbar.make(view, "Event changed", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                ((MainActivity) getActivity()).backToMain();
                break;
            }

            case R.id.details_frag_button_delete: {
                // do something for button 2 click
                this.event.setLevel(0);     //set level to 0 for deletion of event
                ((MainActivity)getActivity()).sendMessage(eventParser.parseEvent(event));
                Snackbar.make(view, "Event deleted", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                ((MainActivity) getActivity()).backToMain();
                break;
            }

            //.... etc
        }
    }
}
