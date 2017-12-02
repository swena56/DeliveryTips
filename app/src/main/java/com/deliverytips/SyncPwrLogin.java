package com.deliverytips;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
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

import com.deliverytips.table.data.DeliveryEvent;

import org.apache.http.client.CookieStore;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


public class SyncPwrLogin extends AppCompatActivity {

    TextView text;
    public CookieStore cs;
    public SharedPreferences sharedPref;
    public WebView webView;
    public  WebSettings webSettings;
    public EditText editText;
    public Boolean loaded = false;
    public Button saveImportButton;
    public Button cancelImportButton;
    public CheckBox enableAutoSubmitCheckbox;
    //javascript parser
    public List<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
    public Boolean doneParsing = false;
    public Boolean needsLogin = false;
    public String loadURL;
    public Boolean stopProcessing = false;
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
        cancelImportButton = (Button) findViewById(R.id.buttonImportCancel);
        enableAutoSubmitCheckbox = (CheckBox) findViewById(R.id.checkBoxEnableAutoSubmit);

        boolean autoSaveSettings = sharedPref.getBoolean("auto_sync",false);
        enableAutoSubmitCheckbox.setChecked(autoSaveSettings);

        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.clearFormData();
        webView.getSettings().getAllowFileAccessFromFileURLs();
        webView.getSettings().getCacheMode();
        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");

        //getWindow().requestFeature(Window.FEATURE_PROGRESS);
        webView.enableSlowWholeDocumentDraw();

        String store_id = getIntent().getExtras().getString("store_id");
        loadURL = "https://pwr.dominos.com/PWR/RealTimeOrderDetail.aspx?PrintMode=true&FilterCode=sr_"+store_id+"&FilterDesc=Store-"+store_id;
        loadURL = "https://pwr.dominos.com/PWR/RealTimeOrderDetail.aspx?FilterCode=sr_"+store_id+"&FilterDesc=Store-"+store_id;
        //loadURL = "https://pwr.dominos.com/PWR/Login.aspx";
        webView.loadUrl(loadURL);
        CookieManager.getInstance().setAcceptCookie(true);

        text.setText("Loading...please wait");
        webView.findAllAsync("Store Order Detail");
        webView.setFindListener(new WebView.FindListener() {

            @Override
            public void onFindResultReceived(int activeMatchOrdinal, int numberOfMatches, boolean isDoneCounting) {

                if( isDoneCounting && numberOfMatches > 0 ){
                    text.setText(text.getText() + "\nReal: " + numberOfMatches + ", " + isDoneCounting);
                    text.setText(text.getText() + "\nGenerating html");

                    StartImport();
                    launchParser();
                    saveImportButton.setEnabled(true);
                }
            }

            public void launchParser(){

                boolean autoSaveSettings = sharedPref.getBoolean("auto_sync",false);

                StopAll();

                Intent i2 = new Intent(MainActivity.get(), SyncPwr.class);
                i2.putExtra("store_id",getIntent().getExtras().getString("store_id"));
                i2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i2);
            }
        });

        //stopLoading
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                //text.setText(text.getText() + "\nPage started");
            }

            @Override
            public void onPageFinished(WebView view, String urlString) {

                injectCredentials(view);
                text.setText(text.getText() + "\nPage finished");
                text.setText(text.getText() + "\nContent Height: "+view.getContentHeight());
            }

            public void injectCredentials(WebView view){
                String username = getIntent().getExtras().getString("username");
                String password = getIntent().getExtras().getString("password");

                if (username != null && password != null ) {

                    text.setText(text.getText() + "\nInjecting Login Creds");
                    String javaScript = "javascript:(function() {" +
                            "document.getElementById(\"txtUsername\").value = \"" + username + "\";\n" +
                            "document.getElementById(\"txtPassword\").value = \"" + password + "\";\n" +
                            "var submit = document.getElementById(\"txtPassword\");\n" +
                            //"submit.click();\n" +
                            //"$(\".btnLogin\").click();\n" +
                            "})()";

                    view.loadUrl(javaScript);
                } else {
                    text.setText(text.getText() + "\nInjecting Login Creds...failed");
                }
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

                stopProcessing = true;

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("auto_sync", true);
                editor.commit();

//                StopAll();
//
//                Intent i2 = new Intent(MainActivity.get(), SyncPwr.class);
//                i2.putExtra("store_id",getIntent().getExtras().getString("store_id"));
//                startActivity(i2);
            }

        });

        saveImportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Parsing....", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                //webView.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>");
                stopProcessing = true;

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("auto_sync", true);
                editor.commit();

                SaveImport();
                StopAll();
                Intent i2 = new Intent(MainActivity.get(), SyncPwr.class);
                i2.putExtra("store_id",getIntent().getExtras().getString("store_id"));
                startActivity(i2);

            }
        });

        cancelImportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Cancel", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                stopProcessing = true;
                StopAll();
            }
        });
    }

    public void StopAll(){
        stopProcessing = true;
        this.finish();
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
            }

            doneParsing = true;
        }
    }

    protected Boolean StartImport() {

        Toast.makeText(getApplicationContext(), "Starting Import", Toast.LENGTH_SHORT).show();

        text.setText(text.getText() + "\nStarting Import, please wait");
        webView.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");

        while (!doneParsing) {
            if( stopProcessing){
                break;
            } else {
                SystemClock.sleep(1000);
            }
        }

        SaveImport();

        return true;
    }

    public Boolean SaveImport(){
        if (data.size() == 0 ) {
            //Toast.makeText(getApplicationContext(), "No Data available", Toast.LENGTH_SHORT).show();
            return false;
        }


        text.setText(text.getText() + "\nNumber of entries detected: " + data.size() + "\n");

        for (ArrayList<String> row : data) {

            String address = row.get(4);
            if (address.contains("&nbsp;") || address == "") {
                address = null;
            }

            String phone = row.get(3);
            if (phone.contains("&nbsp;") || phone == "") {
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
            deliveryEvent._timestamp = row.get(2);
            deliveryEvent._driver = row.get(10);
            deliveryEvent._phone_number = phone;
            deliveryEvent._csr = row.get(9);
            deliveryEvent._description = row.get(11);
            deliveryEvent._street = address;
            deliveryEvent._full_name = row.get(1);

            //parse delivery number
            String[] arr = row.get(1).split("#");
            String ticket_id = "";
            Long order_number;
            if (arr.length > 0) {
                order_number = Long.parseLong(arr[1]);
                deliveryEvent._order_number = order_number;
                ticket_id = arr[1];
            }

            MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(getApplicationContext());
            SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();

            String[] whereArgs = {ticket_id};
            Cursor cursor = db.rawQuery("SELECT " + DeliveryEvent.COLUMN_NAME_ORDER_NUMBER + "," +
                    DeliveryEvent.COLUMN_NAME_TIP + "," +
                    DeliveryEvent.COLUMN_NAME_NOTES +
                    " FROM " + DeliveryEvent.TABLE_NAME
                    + " WHERE " + DeliveryEvent.COLUMN_NAME_ORDER_NUMBER + " = ?", whereArgs);

            int recordCount = cursor.getCount();
            cursor.close();

            if (recordCount <= 0) {
                text.setText(text.getText() + "\n\n(NEW) " + row);
                Log.d("INSERTING",ticket_id);
                db.insert(deliveryEvent.TABLE_NAME, null, deliveryEvent.getContentValues());
            } else {
                text.setText(text.getText() + "\n\n(UPDATE) " + row);
                Log.d("UPDATING",ticket_id);
                ContentValues cv = deliveryEvent.getContentValues();
                cv.remove(DeliveryEvent.COLUMN_NAME_TIP);
                cv.remove(DeliveryEvent.COLUMN_NAME_NOTES);
                db.update(deliveryEvent.TABLE_NAME,cv,deliveryEvent.COLUMN_NAME_ORDER_NUMBER + "= ?",whereArgs );
            }

            db.close();

            //Toast.makeText(getApplicationContext(),"Data Import Complete", Toast.LENGTH_SHORT).show();
            text.setText(text.getText() + "\nData Import Complete");

            //logout
            //webView.loadUrl("javascript:PWRLogout()");

                   this.finish();
        }

        return true;
    }
}

