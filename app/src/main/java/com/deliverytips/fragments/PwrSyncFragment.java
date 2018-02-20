package com.deliverytips.fragments;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.deliverytips.MainActivity;
import com.deliverytips.MyDatabaseHelper;
import com.deliverytips.R;
import com.deliverytips.table.DeliveryEventsTable;
import com.deliverytips.table.data.DeliveryEvent;
import com.pddstudio.preferences.encrypted.EncryptedPreferences;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class PwrSyncFragment extends Fragment {


    public WebView webView;
    public List<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
    public boolean needsLogin = false;
    public SharedPreferences sharedPref;
    public static int SYNC_INTERVAL = 5;
    public Timer timer;
    public TimerTask doAsynchronousTask;
    public View view;
    public PwrSyncFragment() {
        // Required empty public constructor
    }


    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_pwr_sync, container, false);

        sharedPref = MainActivity.get().getPreferences(Context.MODE_PRIVATE);
        webView = (WebView) view.findViewById(R.id.syncWebView);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.clearFormData();
        //webView.getSettings().getAllowFileAccessFromFileURLs();
        //webView.enableSlowWholeDocumentDraw();
        webView.getSettings().getCacheMode();
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");

        String store_id = sharedPref.getString("store_id", null);
        String loadURL = "https://pwr.dominos.com/PWR/RealTimeOrderDetail.aspx?PrintMode=true&FilterCode=sr_"+store_id+"&FilterDesc=Store-"+store_id;
        webView.loadUrl(loadURL);
        //webView.findAllAsync("Store Order Detail");
        webView.findAllAsync("User name:");
        webView.setFitsSystemWindows(true);
        CookieManager.getInstance().setAcceptCookie(true);

        //stopLoading
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String urlString) {
                injectCredentials(view);
            }

            public void injectCredentials(WebView view){
                SharedPreferences sharedPref = MainActivity.get().getPreferences(Context.MODE_PRIVATE);
                String username = sharedPref.getString("username",null);
                String password = sharedPref.getString("password",null);

                if (username != null && password != null  ) {

                    String ivs = sharedPref.getString("ivs",null);
                    String encryption = sharedPref.getString("encryption",null);

                    EncryptedPreferences encryptedPreferences = new EncryptedPreferences.Builder(MainActivity.get()).withEncryptionPassword(String.valueOf(R.string.enc_alias)).build();
                    encryptedPreferences.getString("password", null);

                    webView.clearFormData();

                    if( encryptedPreferences.getString("password", null) != null ) {

                        String javaScript = "javascript:(function() {" +

                                //"document.getElementById(\"btnLogin\").style='position:absolute;position:fixed !important; height:100%;width:100%;top:0;botton:0;left:0;right:0;font-size : 40px;';\n" +
                                "var element = document.getElementById(\"loginwrapper\");\n" +
                                "element.style='position:absolute;position:fixed !important; height:100%;top:0;botton:0;left:0;right:0;background-color: white';\n" +
                                "document.getElementById(\"txtUsername\").value = \"" + username + "\";\n" +
                                "document.getElementById(\"txtPassword\").value = \"" + encryptedPreferences.getString("password", null) + "\";\n" +
                                "var submit = document.getElementById(\"txtPassword\");\n" +
                                //"submit.click();\n" +
                                //"$(\".btnLogin\").click();\n" +
                                "})()";

                        view.loadUrl(javaScript);
                        javaScript = null;
                    }

                }
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        view.setVisibility(View.GONE);

        webView.setFindListener(new WebView.FindListener() {

            @Override
            public void onFindResultReceived(int activeMatchOrdinal, int numberOfMatches, boolean isDoneCounting) {

                if( isDoneCounting && numberOfMatches > 0 ){
                    view.setVisibility(View.VISIBLE);
                }
            }
        });

        startTimer();

        webView.requestFocus();

        // Inflate the layout for this fragment

        return view;
    }

    public void startTimer(){

        final Handler handler = new Handler();
        timer = new Timer();
        doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {

                            SimpleDateFormat s = new SimpleDateFormat("MM/dd hh:mm");
                            String format = s.format(new Date());
                            String last_update = sharedPref.getString("last_sync_date", null);

                            long second = 1000l;
                            long minute = 60l * second;
                            long hour = 60l * minute;

                            Date date1 = s.parse(last_update);
                            Date date2 = s.parse(format.toString());

                            long diff = date2.getTime() - date1.getTime();

                            String diff_str = String.format("%02d", diff / hour) + ":" + String.format("%02d", (diff % hour) / minute) + ":" + String.format("%02d", (diff % minute) / second);

                            //only sync when auto sync is enabled
                            if ((diff / hour) > 1 || (diff / minute) >= SYNC_INTERVAL) {
                                //webView.refreshDrawableState();
                                //SystemClock.sleep(10000);
                                //Toast.makeText(getContext(), "Syncing with PWR", Toast.LENGTH_SHORT).show();
                                webView.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
                                webView.reload();
                            } else {
                                //webView.refreshDrawableState();
                                //Toast.makeText(getContext(), "Waiting to sync: " + diff_str, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                        }
                    }
                });
            }
        };

        int sleep_time = 50000;

        timer.schedule(doAsynchronousTask, 0, sleep_time);
    }

    public class MyJavaScriptInterface
    {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html)
        {
            data.clear();

            // process the html as needed by the app
            String[] lines = html.split("\\n");
            for (String line : lines)
            {
                //Log.i("S", line);
//                if( line.contains("<input name=\"txtUsername\"")){
//                    needsLogin = true;
//                    MainActivity.get().createToast("Detected Login");
//                    Log.d("Detected Login","Detected Login");
//                    view.setVisibility(View.VISIBLE);
//                    break;
//                }

                if( line.contains("<td class=\"dxgv\" align=\"center\">") ) {
                    //Log.i("S", line);

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

            if (data.size() > 0) {

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
                    String date;

                    if (arr.length > 0) {
                        order_number = Long.parseLong(arr[1]);
                        deliveryEvent._order_number = order_number;
                        ticket_id = arr[1];
                        date = arr[0];
                    }

                    //String date = sharedPref.getString("date", null);

                    MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(getActivity());
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
                        Log.d("INSERTING", ticket_id);
                        db.insert(deliveryEvent.TABLE_NAME, null, deliveryEvent.getContentValues());
                    } else {
                        Log.d("UPDATING", ticket_id);
                        ContentValues cv = deliveryEvent.getContentValues();
                        cv.remove(DeliveryEvent.COLUMN_NAME_TIP);
                        cv.remove(DeliveryEvent.COLUMN_NAME_NOTES);
                        db.update(deliveryEvent.TABLE_NAME, cv, deliveryEvent.COLUMN_NAME_ORDER_NUMBER + "= ?", whereArgs);
                    }

                    db.close();

                    //logout
                    //webView.loadUrl("javascript:PWRLogout()");

                    SharedPreferences.Editor editor = sharedPref.edit();
                    SimpleDateFormat s = new SimpleDateFormat("MM/dd hh:mm");
                    editor.putString("last_sync_date", s.format(new Date()).toString());
                    editor.commit();

                    MainActivity.get().fm.beginTransaction().replace(R.id.content_frame, new DeliveryEventsTable()).commit();

                   // view.setVisibility(View.GONE);
                    //update delivery table
                    //DeliveryEventsTable.get().updateTable("");
                    //DeliveryEventsTable.get().tableDataAdapter.notifyDataSetChanged();
                    //DeliveryEventsTable.get().
                }
            }
        }
    }
}
