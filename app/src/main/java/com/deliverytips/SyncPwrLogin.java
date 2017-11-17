package com.deliverytips;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
        webView.addJavascriptInterface(new LoginJavasriptInterface(), "HTMLOUT");

        String store_id = getIntent().getExtras().getString("store_id");
        //loadURL = "https://pwr.dominos.com/PWR/RealTimeOrderDetail.aspx?PrintMode=true&FilterCode=sr_"+store_id+"&FilterDesc=Store-"+store_id;
        //loadURL = "https://pwr.dominos.com/PWR/RealTimeOrderDetail.aspx?FilterCode=sr_"+store_id+"&FilterDesc=Store-"+store_id;
        loadURL = "https://pwr.dominos.com/PWR/Login.aspx";
        webView.loadUrl(loadURL);
        CookieManager.getInstance().setAcceptCookie(true);

        text.setText("Loading... may require login click, followed by cancel button.");

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String urlString) {

                String username = getIntent().getExtras().getString("username");
                String password = getIntent().getExtras().getString("password");

                int progress = webView.getProgress();
                while (progress < 100) {
                    text.setText(text.getText() + "\nProgress: " + progress);
                    SystemClock.sleep(1000);
                    progress = webView.getProgress();
                }

                text.setText(text.getText() + "\nDone Loading");
                if (username != null && password != null ) {

                    String javaScript = "javascript:(function() {" +
                            "document.getElementById(\"txtUsername\").value = \"" + username + "\";\n" +
                            "document.getElementById(\"txtPassword\").value = \"" + password + "\";\n" +
                            "var submit = document.getElementById(\"txtPassword\");\n" +
                            "submit.click();\n"+
                            //"$(\".btnLogin\").click();\n" +
                            "})()";

                    webView.loadUrl(javaScript);

                    //wait 5 seconds
                    SystemClock.sleep(5000);

                    StopAll();

                    saveImportButton.setEnabled(true);
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
                webView.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>");

                //StopAll();
            }

        });

        saveImportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Parsing....", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                //webView.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>");
                StopAll();

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
        Intent i2 = new Intent(MainActivity.get(), SyncPwr.class);
        i2.putExtra("store_id",getIntent().getExtras().getString("store_id"));
        startActivity(i2);
    }

    public class LoginJavasriptInterface
    {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html)
        {
            // process the html as needed by the app
            String[] lines = html.split("\\n");
            for (String line : lines)
            {
                if( line.contains("<input name=\"txtUsername\"")){
                    needsLogin = true;
                    text.setText(text.getText() + "\nNeeds Login");
                    Log.d("Parsing","Needs working");
                }
            }
            doneParsing = true;
        }
    }
}

