package com.deliverytips;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.deliverytips.RegisterAppKey.AppKey;
import com.deliverytips.fragments.Import;
import com.deliverytips.http_helpers.GetRequest;
import com.deliverytips.table.DeliveryEventsTable;
import com.deliverytips.table.data.DeliveryEvent;
import com.pddstudio.preferences.encrypted.EncryptedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;


public class Settings extends Fragment {

    EditText editTextStoreId;
    EditText editTextUsername;
    EditText editTextPassword;
    EditText editTextAddress;
    Button saveButton;
    Button resetDBButton;
    CheckBox enableAutoSubmitCheckbox;
    AppKey appKey;
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
        enableAutoSubmitCheckbox = (CheckBox) rootView.findViewById(R.id.checkBoxEnableAutoSubmit);
        editTextStoreId.setText( sharedPref.getString("store_id", null) );
        editTextUsername.setText( sharedPref.getString("username", null) );

        //store id change detection
        editTextStoreId.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("store_id", editTextStoreId.getText().toString());
                    editor.commit();

            }
        });



        //set auto sync
        enableAutoSubmitCheckbox.setChecked(sharedPref.getBoolean("auto_sync",false));

        //load password into memory
        String ivs = sharedPref.getString("ivs",null);
        String encryption = sharedPref.getString("ivs",null);
        byte[] test = (ivs != null) ? Base64.decode(ivs,Base64.DEFAULT) :  new byte[]{};
        byte[] en = (encryption != null) ? Base64.decode(encryption,Base64.DEFAULT) : new byte[]{};

        appKey = new AppKey(MainActivity.get());
        editTextPassword.setText( sharedPref.getString("password", null) );
        try {

            String e = appKey.decryptText(String.valueOf(R.string.enc_alias),test,en);
            editTextPassword.setText( e );
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

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
                  String results = GetRequest.getRequest("1953");
                  Toast.makeText(MainActivity.get(),results, Toast.LENGTH_LONG).show();

                  EncryptedPreferences encryptedPreferences = new EncryptedPreferences.Builder(MainActivity.get()).withEncryptionPassword(String.valueOf(R.string.enc_alias)).build();

                  encryptedPreferences.edit()
                          .putString("password", editTextPassword.getText().toString())
                          .apply();

                  SharedPreferences.Editor editor = sharedPref.edit();

                  try {

                      SecureRandom random = new SecureRandom();
                      //random.setSeed(R.string.enc_alias);
                      String seed = random.toString();
                      editor.putString("key",seed);

                      String encrypted = appKey.encryptText(String.valueOf(R.string.enc_alias), editTextPassword.getText().toString());

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

        //set shared preferences to run auto sync
        enableAutoSubmitCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //save to Shared Preferences
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("auto_sync", isChecked);
                editor.commit();

                //message to user
                Toast.makeText(MainActivity.get(),"Auto Sync is "+((isChecked) ? "(ON)":"(OFF)") +" Unimplemented.", Toast.LENGTH_SHORT).show();
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

    //fetch data from server
    private void getData(){
        String url = "https://pwr-deliveries.ddns.net/delivery/1953";

        RequestQueue mRequestQueue;

        // Instantiate the cache
        Cache cache = new DiskBasedCache(MainActivity.get().getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        mRequestQueue = new RequestQueue(cache, network);

        // Start the queue
        mRequestQueue.start();

        // Formulate the request and handle the response.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Do something with the response
                        Log.d("http",response.toString());
                        String in = response.toString();
                        try {
                            JSONObject reader = new JSONObject(in);
                            JSONArray contacts = reader.getJSONArray("results");

                            //reader.get
                            int le = contacts.length();
                            Log.d("http",le + "");
//
//
                            for (int i = 0; i < contacts.length(); i++) {
                                JSONObject c = contacts.getJSONObject(i);
                                Log.d("http", c.toString());
                                String order_id = c.getString("order_id");
                                String address = c.getString("address");
                                String timestamp = c.getString("timestamp");
                                String source = c.getString("source");
                                String service = c.getString("service");
                                String status = c.getString("status");
                                String description = c.getString("description");
                                String price = c.getString("price");
                                String phone = c.getString("phone");

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                        Log.d("http",error.toString());
                    }
                }){

            //This is for Headers If You Needed
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                //params.put("Content-Type", "application/json; charset=UTF-8");
                //params.put("token", ACCESS_TOKEN);
                params.put("php-auth-user", "user");
                params.put("php-auth-pw", "password");
                return params;
            }

            //Pass Your Parameters here
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("php-auth-user", "user");
                params.put("php-auth-pw", "password");
                return params;
            }
        };


        // Add the request to the RequestQueue.
        mRequestQueue.add(stringRequest);


    }
}
