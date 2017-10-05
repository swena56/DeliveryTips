package com.deliverytips.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.deliverytips.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class CTR extends Fragment {

    SurfaceView cameraView;
    TextView textView;
    //CameraSource cameraSource;
    final int RequestCameraPermissionID = 1001;

    public CTR() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ctr, container, false);
    }

}
