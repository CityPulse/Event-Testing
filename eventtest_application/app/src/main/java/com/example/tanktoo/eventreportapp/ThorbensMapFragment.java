package com.example.tanktoo.eventreportapp;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.MapFragment;

/**
 * A simple {@link MapFragment} subclass.
 */
public class ThorbensMapFragment extends MapFragment {

    View view;

    public ThorbensMapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = super.onCreateView(inflater, container, savedInstanceState);
        System.out.println("##### mein olles map fragment #####");
        return view;
    }

}
