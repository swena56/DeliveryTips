package com.deliverytips;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
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

import com.deliverytips.RegisterAppKey.AppKey;
import com.deliverytips.table.data.DeliveryEvent;

import org.apache.http.client.CookieStore;

import java.io.UnsupportedEncodingException;
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
        //setContentView(R.layout.activity_pwr_sync);
        setContentView(R.layout.activity_sync_pwr);

        sharedPref = MainActivity.get().getPreferences(Context.MODE_PRIVATE);

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
        webView.getSettings().setSavePassword(false);
        webView.clearFormData();
        webView.getSettings().getAllowFileAccessFromFileURLs();
        webView.getSettings().getCacheMode();
        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
        webView.enableSlowWholeDocumentDraw();

        String store_id = getIntent().getExtras().getString("store_id");
        loadURL = "https://pwr.dominos.com/PWR/RealTimeOrderDetail.aspx?PrintMode=true&FilterCode=sr_"+store_id+"&FilterDesc=Store-"+store_id;
        //loadURL = "https://pwr.dominos.com/PWR/RealTimeOrderDetail.aspx?FilterCode=sr_"+store_id+"&FilterDesc=Store-"+store_id;

        webView.loadUrl(loadURL);
        CookieManager.getInstance().setAcceptCookie(true);

        text.setText("Loading...please wait");
        webView.findAllAsync("Store Order Detail");
        webView.setFitsSystemWindows(true);
        webView.setFindListener(new WebView.FindListener() {

            @Override
            public void onFindResultReceived(int activeMatchOrdinal, int numberOfMatches, boolean isDoneCounting) {

                if( isDoneCounting && numberOfMatches > 0 ){
                    StartImport();
                    StartImport();
                }
            }
        });

        //stopLoading
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String urlString) {

                injectCredentials(view);
                text.setText(text.getText() + "\nPage finished");
            }

            public void injectCredentials(WebView view){
                String username = getIntent().getExtras().getString("username");
                String password = getIntent().getExtras().getString("password");

                AppKey appKey = new AppKey(MainActivity.get());
                try {

                    if (username != null && password != null ) {

                        byte[] test = Base64.decode(sharedPref.getString("ivs",null),Base64.DEFAULT);
                        byte[] en = Base64.decode(sharedPref.getString("encryption",null),Base64.DEFAULT);

                        String e = appKey.decryptText(String.valueOf(R.string.enc_alias),test,en);
                        //Log.d("Login", e);

                        String javaScript = "javascript:(function() {" +

                                "var element = document.getElementById(\"loginwrapper\");\n" +
                                "element.style='position:absolute;position:fixed !important; height:100%;top:0;botton:0;left:0;right:0;background-color: white';\n" +
                                "document.getElementById(\"txtUsername\").value = \"" + username + "\";\n" +
                                "document.getElementById(\"txtPassword\").value = \"" + e + "\";\n" +
                                "var submit = document.getElementById(\"txtPassword\");\n" +
                                 "document.getElementById(\"btnLogin\").style='position:absolute;position:fixed !important; height:100%;width:100%;top:0;botton:0;left:0;right:0;font-size : 40px;';\n" +

                                //"submit.click();\n" +
                                //"$(\".btnLogin\").click();\n" +
                                "})()";
                        e = null;
                        view.loadUrl(javaScript);
                        javaScript = null;

                    } else {
                        text.setText(text.getText() + "\nInjecting Login Creds...failed");
                    }

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
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
                SaveImport();
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

        SystemClock.sleep(3000);

        SaveImport();

        return true;
    }

    public Boolean SaveImport(){
        if (data.size() == 0 ) {
            text.setText(text.getText() + "\nNo Data\n");
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

