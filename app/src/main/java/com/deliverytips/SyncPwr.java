package com.deliverytips;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.client.CookieStore;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


public class SyncPwr extends AppCompatActivity {

    TextView text;
    public CookieStore cs;
    public SharedPreferences sharedPref;
    public WebView webView;
    public  WebSettings webSettings;
    public EditText editText;
    public Boolean loaded = false;
    public Button saveImportButton;
    public CheckBox enableAutoSubmitCheckbox;
    //javascript parser
    public List<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
    public Boolean doneParsing = false;
  //  public SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_pwr);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //sharedPref = getPreferences(Context.MODE_PRIVATE);
        sharedPref = getSharedPreferences("", Context.MODE_PRIVATE);
        text = (TextView) findViewById(R.id.log_text);
        editText = (EditText) findViewById(R.id.EDIT_TEXT);
        webView = (WebView) findViewById(R.id.webview);
        saveImportButton = (Button) findViewById(R.id.buttonSubmitImport);
        enableAutoSubmitCheckbox = (CheckBox) findViewById(R.id.checkBoxEnableAutoSubmit);

        boolean autoSaveSettings = sharedPref.getBoolean("auto_sync",false);
        enableAutoSubmitCheckbox.setChecked(autoSaveSettings);


        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.clearFormData();
        webView.getSettings().getAllowFileAccessFromFileURLs();
        webView.getSettings().getCacheMode();
        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
        String store_id = getIntent().getExtras().getString("store_id");
        webView.loadUrl("https://pwr.dominos.com/PWR/RealTimeOrderDetail.aspx?PrintMode=true&FilterCode=sr_"+store_id+"&FilterDesc=Store-"+store_id);
        CookieManager.getInstance().setAcceptCookie(true);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String urlString) {

                text.setText("Initial Page has finished loadeding.");
                text.setText(text.getText() + "\nWait until the PWR table HTML is generated.\nThen submit the results by pushing the pink button.");
                text.setText(text.getText() + "\nResults can vary depending on connection speed.");

                //enable option to submit, manually
                saveImportButton.setEnabled(true);

                Boolean auto_sync = sharedPref.getBoolean("auto_sync",false);

                if( auto_sync ){

                    //wait 8 seconds, then start import
                    SystemClock.sleep(8000);

                    SaveImport();
                }

                String username = getIntent().getExtras().getString("username");
                String password = getIntent().getExtras().getString("password");

                //add username and password to forum, then click
                if( username != null && password != null ){
                    String javaScript ="javascript:(function() {" +
                            "document.getElementById(\"txtUsername\").value = \""+username+"\";\n" +
                            "document.getElementById(\"txtPassword\").value = \""+password+"\";\n" +
                            "document.getElementById(\"btnLogin\").click();\n" +
                            "})()";
                    webView.loadUrl(javaScript);
                }

                //do work to automatically detect when the page completes generating html
                //then run javascript parser
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        webView.requestFocus();

        //finished loading
        loaded = true;

        //set shared preferences to run auto sync
        enableAutoSubmitCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //save to Shared Preferences
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("auto_sync", isChecked);
                editor.commit();

                //message to user
                Toast.makeText(getApplicationContext(),"Auto Sync is "+((isChecked) ? "(ON)":"(OFF)") +" Unimplemented.", Toast.LENGTH_SHORT).show();
            }
        });

        //load two buttons for importing, round fab, and square Submit button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Parsing....", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                SaveImport();
                }

        });

        saveImportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Parsing....", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                SaveImport();
            }
        });
    }

    protected Boolean SaveImport(){

        Toast.makeText(getApplicationContext(),"Starting Import", Toast.LENGTH_SHORT).show();

        text.setText(text.getText() + "\nStarting Import, please wait");
        webView.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");

        while( !doneParsing ){
            SystemClock.sleep(1000);
        }

        if( data.size() == 0 ){

            Toast.makeText(getApplicationContext(),"No Import data detected, or login required!", Toast.LENGTH_SHORT).show();
            return false;
        }

        text.setText(text.getText() + "\nNumber of entries detected: " + data.size() + "\n");

        for (ArrayList<String> row : data){

            String address = row.get(4);
            if( address.contains("&nbsp;") || address == "" ){
                address = null;
            }

            String phone = row.get(3);
            if( phone.contains("&nbsp;") || phone == "" ){
                phone = null;
            }

            Number price = 0;
            NumberFormat format = NumberFormat.getCurrencyInstance();
            try {
                price = format.parse(row.get(5));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //Create Delivery event
            DeliveryEvent deliveryEvent = new DeliveryEvent();
            deliveryEvent._price = price.doubleValue();
            deliveryEvent._status = row.get(6);
            deliveryEvent._service = row.get(8);
            //deliveryEvent._type = row.get(7);
            deliveryEvent._timestamp = row.get(2);
            deliveryEvent._driver = row.get(10);
            deliveryEvent._phone = phone;
            deliveryEvent._csr = row.get(9);
            deliveryEvent._description = row.get(11);
            deliveryEvent._street = address;
            deliveryEvent._full_name = row.get(1);

            //parse delivery number
            String[] arr = row.get(1).split("#");
            String ticket_id = "";
            Long order_number;
            if( arr.length > 0 ){
                order_number = Long.parseLong( arr[1] );
                deliveryEvent.setOrderNumber(order_number);
                ticket_id = arr[1];
            }


            MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(getApplicationContext());
            SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();

            //check if already exists
//                    Cursor cursor = db.rawQuery("SELECT "+
//                            DeliveryEvent.COLUMN_NAME_ORDER_NUMBER + ", " +
//                            DeliveryEvent.COLUMN_NAME_TIP +
//                            " FROM " + DeliveryEvent.TABLE_NAME + " WHERE " + DeliveryEvent.COLUMN_NAME_ORDER_NUMBER + " = ? ", new String[] {ticket_id});

            String[] whereArgs ={ticket_id};
            Cursor cursor = db.rawQuery("SELECT "+ DeliveryEvent.COLUMN_NAME_ORDER_NUMBER +"," +
                    DeliveryEvent.COLUMN_NAME_TIP + "," +
                    DeliveryEvent.COLUMN_NAME_NOTES +
                    " FROM "+ com.deliverytips.DeliveryEvent.TABLE_NAME
                    +" WHERE "+ DeliveryEvent.COLUMN_NAME_ORDER_NUMBER+" = ?",whereArgs);

            if(cursor.getCount() <= 0){
                text.setText(text.getText() + "\n\n(NEW) " + row);
                db.insert(deliveryEvent.TABLE_NAME, null, deliveryEvent.getContentValues());
            } else {
                text.setText(text.getText() + "\n\n(UPDATE) " + row);

                int tip_index = cursor.getColumnIndexOrThrow(DeliveryEvent.COLUMN_NAME_TIP);
                //deliveryEvent._notes = cursor.getString(3);

                ContentValues contentValues = deliveryEvent.getContentValues();
                contentValues.remove(DeliveryEvent.COLUMN_NAME_TIP);
                contentValues.remove(DeliveryEvent.COLUMN_NAME_DRIVER);

                        db.update(
                                DeliveryEvent.TABLE_NAME,
                                contentValues,
                                DeliveryEvent.COLUMN_NAME_ORDER_NUMBER + "= ?",
                                new String[] {ticket_id});
            }

            cursor.close();
            db.close();

            //Toast.makeText(getApplicationContext(),"Data Import Complete", Toast.LENGTH_SHORT).show();
            text.setText(text.getText() + "\nData Import Complete");

            this.finish();
        }

        return true;
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    public class MyJavaScriptInterface
    {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html)
        {
            // process the html as needed by the app
            String[] lines = html.split("\\n");
            for (String line : lines)
            {
                if( line.contains("<td class=\"dxgv\" align=\"center\">") ) {
                    Log.i("S", line);

                    List<String> row = new ArrayList<String>();
                    String[] values = line.split("</td>");
                    if( values.length >= 11) {

                        for (String cell : values) {

                            String[] cut_str = cell.split(">");
                            if (cut_str.length > 0) {
                                row.add(cut_str[1]);
                            }
                        }
                    }

                    data.add((ArrayList<String>) row);
                }
                //text.setText(text.getText() + "\n" + line);
            }

            doneParsing = true;
            //text.setText(text.getText() + "\nNumber of entries detected: " + numLines);
        }
    }
}

