package com.deliverytips;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.deliverytips.RegisterAppKey.AppKey;
import com.deliverytips.fragments.Import;
import com.deliverytips.table.DeliveryEventsTable;
import com.deliverytips.table.data.DeliveryEvent;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;


public class Settings extends Fragment {

    EditText editTextStoreId;
    EditText editTextUsername;
    EditText editTextPassword;
    EditText editTextAddress;
    Button saveButton;
    Button resetDBButton;
    View rootView;

    SharedPreferences sharedPref;
    Fragment _this;


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        MainActivity.get().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        _this = this;

        rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        editTextStoreId = (EditText) rootView.findViewById(R.id.editTextStoreId);
        editTextUsername = (EditText) rootView.findViewById(R.id.editTextUsername);
        editTextPassword = (EditText) rootView.findViewById(R.id.editTextPassword);
        editTextAddress = (EditText) rootView.findViewById(R.id.editTextCity);
        saveButton = (Button) rootView.findViewById(R.id.buttonSaveSettings);
        resetDBButton = (Button) rootView.findViewById(R.id.buttonClearDB);
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        editTextStoreId.setText( sharedPref.getString("store_id", null) );
        editTextUsername.setText( sharedPref.getString("username", null) );
        editTextPassword.setText( sharedPref.getString("password", null) );
        editTextAddress.setText( sharedPref.getString("address", null) );

        Button import_button = (Button) rootView.findViewById(R.id.buttonImport);

        import_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getFragmentManager();
                fm.beginTransaction().replace(R.id.content_frame, new Import()).commit();
            }
        });


        saveButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {

                  SharedPreferences.Editor editor = sharedPref.edit();

                  AppKey appKey = new AppKey(MainActivity.get());
                  try {

                      SecureRandom random = new SecureRandom();
                      random.setSeed(R.string.enc_alias);
                      String seed = random.toString();
                      editor.putString("key",seed);

                      String encrypted = appKey.encryptText(seed, editTextPassword.getText().toString());

                      editor.putString("password", encrypted);
                      editor.putString("ivs", Base64.encodeToString(appKey.GetIvs(),Base64.DEFAULT).toString());
                      editor.putString("encryption",Base64.encodeToString(appKey.GetEncryption(),Base64.DEFAULT).toString());
                      editor.putString("store_id", editTextStoreId.getText().toString());
                      editor.putString("username", editTextUsername.getText().toString());
                      editor.putString("address", editTextAddress.getText().toString());
                      editor.commit();
                      Toast.makeText(MainActivity.get(),"Saved Settings", Toast.LENGTH_SHORT).show();
                  } catch (UnsupportedEncodingException e) {

                      Toast.makeText(MainActivity.get(),"Failed to Save Settings. " + e.toString(), Toast.LENGTH_SHORT).show();
                      e.printStackTrace();
                  }

                  //destory fragment
                  FragmentManager fm = getActivity().getFragmentManager();
                  fm.beginTransaction().replace(R.id.content_frame, new DeliveryEventsTable()).commit();
              }
        });

        resetDBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(getContext());
                SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();
                db.execSQL("DROP TABLE IF EXISTS " + DeliveryEvent.TABLE_NAME);
                db.execSQL("DROP TABLE IF EXISTS " + Person.TABLE_NAME);

                db.execSQL(myDatabaseHelper.CREATE_DELIVERY_EVENT);
                db.execSQL(myDatabaseHelper.CREATE_PERSON_TABLE);

                Toast.makeText(getActivity(),"DB Reset", Toast.LENGTH_SHORT).show();

                FragmentManager fm = getActivity().getFragmentManager();
                fm.beginTransaction().replace(R.id.content_frame, new DeliveryEventsTable()).commit();
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }
}
