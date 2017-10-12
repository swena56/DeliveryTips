package com.deliverytips;

import android.content.Context;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.TextView;

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

        sharedPref = getPreferences(Context.MODE_PRIVATE);
        text = (TextView) findViewById(R.id.log_text);
        editText = (EditText) findViewById(R.id.EDIT_TEXT);
        webView = (WebView) findViewById(R.id.webview);

        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.clearFormData();
        webView.getSettings().getAllowFileAccessFromFileURLs();
        webView.getSettings().getCacheMode();
        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
        webView.loadUrl("https://pwr.dominos.com/PWR/RealTimeOrderDetail.aspx?FilterCode=sr_1953&FilterDesc=Store-1953");
        CookieManager.getInstance().setAcceptCookie(true);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String urlString) {

                text.setText("Page Loaded");
                text.setText(text.getText() + "\nWaiting until HTML is generated, then\n submit the results by pushing the pink button.");

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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Parsing....", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

               //Toast.makeText(getApplicationContext(),"Starting Import", Toast.LENGTH_SHORT).show();
                text.setText(text.getText() + "\nStarting Import, please wait");
                webView.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");

                while( !doneParsing ){
                    SystemClock.sleep(1000);
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
                    //deliveryEvent._type = row.get(7);
                    deliveryEvent._timestamp = row.get(2);
                    deliveryEvent._driver = row.get(8);
                    deliveryEvent._phone = phone;
                    deliveryEvent._csr = row.get(10);
                    deliveryEvent._description = row.get(11);
                    deliveryEvent._street = address;
                    deliveryEvent._full_name = row.get(1);

                    //parse delivery number
                    String[] arr = row.get(1).split("#");
                    Long order_number;
                    if( arr.length > 0 ){
                        order_number = Long.parseLong( arr[1] );
                        deliveryEvent.setOrderNumber(order_number);
                    }

                    MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(getApplicationContext());
                    SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();

                    //check if already exists
                    Cursor cursor = db.rawQuery("SELECT "+DeliveryEvent.COLUMN_NAME_ORDER_NUMBER+" FROM " + DeliveryEvent.TABLE_NAME + " WHERE " + DeliveryEvent.COLUMN_NAME_ORDER_NUMBER + " = " + deliveryEvent._order_number, null);
                    if(cursor.getCount() <= 0){
                        text.setText(text.getText() + "\n\n(NEW) " + row);
                        db.insert(deliveryEvent.TABLE_NAME, null, deliveryEvent.getContentValues());
                    } else {
                        text.setText(text.getText() + "\n\n(UPDATE) - not implemented " + row);
                        //db.insert(deliveryEvent.TABLE_NAME, null, deliveryEvent.getContentValues());
                    }

                    cursor.close();
                    db.close();

                    //Toast.makeText(getApplicationContext(),"Data Import Complete", Toast.LENGTH_SHORT).show();
                    text.setText(text.getText() + "\nData Import Complete");

                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                }
                }

        });
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

