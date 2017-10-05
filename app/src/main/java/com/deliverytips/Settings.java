package com.deliverytips;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class Settings extends Fragment {

    EditText editTextStoreId;
    EditText editTextUsername;
    EditText editTextPassword;
    Button saveButton;
    View rootView;

    SharedPreferences sharedPref;
    Fragment _this;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        _this = this;
        rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        editTextStoreId = (EditText) rootView.findViewById(R.id.editTextStoreId);
        editTextUsername = (EditText) rootView.findViewById(R.id.editTextUsername);
        editTextPassword = (EditText) rootView.findViewById(R.id.editTextPassword);
        saveButton = (Button) rootView.findViewById(R.id.buttonSaveSettings);
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

        editTextStoreId.setText( sharedPref.getString("store_id", null) );
        editTextUsername.setText( sharedPref.getString("username", null) );
        editTextPassword.setText( sharedPref.getString("password", null) );

        saveButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {

                  SharedPreferences.Editor editor = sharedPref.edit();

                  editor.putString("store_id", editTextStoreId.getText().toString());
                  editor.putString("username", editTextUsername.getText().toString());
                  editor.putString("password", editTextPassword.getText().toString());
                  editor.commit();
                  Toast.makeText(getActivity(),"Saved Settings", Toast.LENGTH_SHORT).show();

                  //destory fragment
                  FragmentManager fm = getActivity().getFragmentManager();
                  fm.beginTransaction().replace(R.id.content_frame, new DashboardTable()).commit();
              }
        });

        // Inflate the layout for this fragment
        return rootView;
    }


}
