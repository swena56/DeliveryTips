package com.deliverytips.table;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.deliverytips.DeliveryEventDetails;
import com.deliverytips.R;
import com.deliverytips.Settings;
import com.deliverytips.SyncPwr;
import com.deliverytips.table.data.DataFactory;
import com.deliverytips.table.data.DeliveryEvent;

import org.apache.http.client.CookieStore;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.HttpParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.codecrafters.tableview.listeners.SwipeToRefreshListener;
import de.codecrafters.tableview.listeners.TableDataClickListener;
import de.codecrafters.tableview.listeners.TableDataLongClickListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeliveryEventsTable extends Fragment {

    TableDataAdapter tableDataAdapter;
    SharedPreferences sharedPref;
    public String store_id;
    public String username;
    public String password;
    public RequestQueue queue;
    public String url;
    public DefaultHttpClient mDefaultHttpClient;
    public CookieStore cs;
    public RequestQueue mRequestQueue; //for single function
    //public CookieManager cookieManager;
    //public java.net.CookieManager cm;

    ProgressDialog pd;



    public RequestQueue getRequestQueue() {

        if ( this.queue == null ) {
            /*
            DefaultHttpClient httpclient = new DefaultHttpClient();

            HttpClientStack httpStack = new HttpClientStack(httpclient);
            queue = Volley.newRequestQueue(getActivity(), httpStack);
             */

            mDefaultHttpClient = new DefaultHttpClient();

            //cookieManager = CookieManager.getInstance();
            //mDefaultHttpClient.setCookieStore(cm.getCookieStore());
            //cs = mDefaultHttpClient.getCookieStore();
            //mDefaultHttpClient.setCookieStore();
            //CookieManager.getInstance().setCookie("https://pwr.dominos.com/", "ASP.NET_SessionId=51rw25cndjpvfhqkkwohv0hl");
            //BasicClientCookie c = (BasicClientCookie) getCookie(cs, "krekeln_RptParams");

//            c.setValue("+RealTime|RealTime_OrderDetail|sr_1953|0|10/10/2017:10/10/2017||;");
//            cs.addCookie(c);

            final ClientConnectionManager mClientConnectionManager = mDefaultHttpClient.getConnectionManager();
            final HttpParams mHttpParams = mDefaultHttpClient.getParams();
            final ThreadSafeClientConnManager mThreadSafeClientConnManager = new ThreadSafeClientConnManager( mHttpParams, mClientConnectionManager.getSchemeRegistry() );

            mDefaultHttpClient = new DefaultHttpClient( mThreadSafeClientConnManager, mHttpParams );

            final HttpStack httpStack = new HttpClientStack( mDefaultHttpClient );

            this.queue = Volley.newRequestQueue( getContext(), httpStack );
        }

        return this.queue;
    }

    public void single(){


        // Instantiate the cache
        Cache cache = new DiskBasedCache(getActivity().getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        mRequestQueue = new RequestQueue(cache, network);

        // Start the queue
        mRequestQueue.start();

        String url ="https://pwr.Dominos.com/Pwr/";
        url = "https://pwr.dominos.com/PWR/Login.aspx?ReturnUrl=RealTimeOrderDetail.aspx?FilterCode=sr_"+store_id+"&FilterDesc=Store-"+store_id;

        // Formulate the request and handle the response.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Do something with the response
                        Log.d("1", response);

                        //cs = mDefaultHttpClient.getCookieStore();


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                        Log.d("1", error.toString());
                    }
                });

        // Add the request to the RequestQueue.
        mRequestQueue.add(stringRequest);
    }

    public com.android.volley.toolbox.StringRequest getStringRequest(){

        String url = "https://pwr.dominos.com/PWR/Login.aspx?ReturnUrl=RealTimeOrderDetail.aspx?FilterCode=sr_"+store_id+"&FilterDesc=Store-"+store_id;
       // url = "https://pwr.dominos.com/PWR/Login.aspx";
        //                                Map map = new HashMap<String, String>();
        //                                StringRequest postRequest = new StringRequest(Request.Method.POST, url, map, new Response.Listener<String>() {
        //                                    @Override
        //                                    public void onResponse(String response) {
        //                                        Toast.makeText(getActivity()
        //                                                , "Response: " + response, Toast.LENGTH_SHORT).show();
        //                                    }
        //                                });

        com.android.volley.toolbox.StringRequest postRequest = new com.android.volley.toolbox.StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {


                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        Log.d("Response", response);
                        cs = mDefaultHttpClient.getCookieStore();

                        BasicClientCookie c = (BasicClientCookie) getCookie(cs, "ASP.NET_SessionId");
                        cs.getCookies().toString();

                        Log.d("Cookies", cs.getCookies().toString());
                        if( c != null ) {

                            Log.d("ASP.NET_SessionId", c.getValue());
                        }





//                        BasicClientCookie __AntiXsrfToken = (BasicClientCookie) getCookie(cs, "__AntiXsrfToken");
//                        Log.d("__AntiXsrfToken", __AntiXsrfToken.getValue());

//                        BasicClientCookie PwrAuthCookie = (BasicClientCookie) getCookie(cs, "PwrAuthCookie");
//                        Log.d("PwrAuthCookie", PwrAuthCookie.getValue());
                            //PwrAuthCookie

                        if( response != null ){
                          //  MainActivity.createToast(response);
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("txtUsername", username);
                params.put("txtPassword", password);
                params.put("btnLogin", "Sign+In");
                //params.put(username + "_RptParams", "+RealTime|RealTime_OrderDetail|sr_" + store_id + "|0|" + "server_date" + "|Store " + store_id + "|");
                params.put("__AntiXsrfToken", "081d57c9f3144bda95e6f67e16248743");
               // params.put("ReturnUrl", "%2fPWR%2fRealTimeOrderDetail.aspx%3fFilterCode%3dsr_" + store_id);
                //params.put("FilterCode", "sr_" + store_id);
                //params.put("PwrMainGridRowSelect", "1");
                params.put("ASP.NET_SessionId", "gqbodsktyutstjlueurayms4");
                params.put("PwrAuthCookie", "C75D04222C6083453BA2614062EB2F385CBB6550BADD6CC8B32ED7B08D1BFDEDA5D1D924B0BC78FBB33DE24CBFB8B4CB3F0C21A32E75D1749E1377A808FEE8BE47CDEEDCF83315E3CACDBE16B922B61095ED3D9C2FC074CB477F252835C0B6D894A3DFD10F243341576DFF63BF92567F7336D9496F9B59A76F26DE7B9BE2AF2A82D69611CDC8CCBCF9053303BB87E5EFF147ECAC4BC3588554946F5BC645D746E12AD1F9A3F1F1BEFFCEE7BDFF6A5C36087072B3FEA5537CEB2F27232DB143CA6D265246A50C5C6707BC290045EA30C65806897D2659FFBC8C2D25B91113F0DD6400E440D570658F9D9DE43703083942461D20B4CFA048DCC81901626302B33AA7B4291DDAB60F3E5493EB04AC65D9A1B58C1F0C445898CCC77714FFECDB6AE7D55189471D454418E1A1D82EDABB7EC34209EED4B54E18B05CEC94B0377BD3107D2E1AEC");
                //params.put(username + "_CurrentCulture", "en-US");
                //params.put("TimeZoneOffset", "7");

                //params.put("fileDownloadToken","?");
                //params.put("__LASTFOCUS","");
                //params.put("__VIEWSTATE","");
                //params.put("__VIEWSTATEGENERATOR","");
                //params.put("__EVENTTARGET","");
                //params.put("__EVENTARGUMENT","");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();

                params.put("Host", "pwr.dominos.com");
                params.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:55.0) Gecko/20100101 Firefox/55.0");
                //params.put("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
                params.put("Accept", "*/*");
                params.put("Accept-Language", "en-US,en;q=0.8");
                params.put("Accept-Encoding", "gzip, deflate, br");
                params.put("Pragma", "no-cache");
                params.put("Upgrade-Insecure-Requests", "1");
                params.put("Cache-Control", "no-cache");
                params.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");

                // params.put("Content-Length","20");
                params.put("Connection", "keep-alive");

                return params;
            }
        };

        getRequestQueue().add(postRequest);

        Toast.makeText(getContext(), postRequest.getUrl(), Toast.LENGTH_SHORT).show();

        return postRequest;
    }

    public Cookie getCookie(CookieStore cs, String cookieName) {
        Cookie ret = null;

        List<Cookie> l = cs.getCookies();
        for (Cookie c : l) {
            if (c.getName().equals(cookieName)) {
                ret = c;
                break;
            }
        }

        return ret;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_delivery_events_table, container, false);

        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        store_id = sharedPref.getString("store_id", "" );
        username = sharedPref.getString("username", "" );
        password = sharedPref.getString("password", "" );

        final SortableDeliveryEventsTableView carTableView = (SortableDeliveryEventsTableView) rootView.findViewById(R.id.tableView);
        if (carTableView != null) {
            tableDataAdapter = new TableDataAdapter(getContext(), DataFactory.createDeliveryEventsList(getContext()), carTableView);
            carTableView.setDataAdapter(tableDataAdapter);
            carTableView.addDataClickListener(new CarClickListener());
            carTableView.addDataLongClickListener(new CarLongClickListener());
            carTableView.setSwipeToRefreshEnabled(true);
            carTableView.setSwipeToRefreshListener(new SwipeToRefreshListener() {
                @Override
                public void onRefresh(final RefreshIndicator refreshIndicator) {
                    carTableView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //tableDataAdapter.getData();

                            String problem_log = "";
                            if( store_id == "" ){
                                problem_log += "Missing Store Identification Number.\n";
                            }

                            if( username == "" ){
                                problem_log += "Missing Username \n";
                            }

                            if( password == "" ){
                                problem_log += "Missing password \n";
                            }

                            if( store_id == "" || username == "" || password == "" ){
                                Toast.makeText(getContext(), "Error: " + problem_log, Toast.LENGTH_SHORT).show();
                                FragmentManager fm = getActivity().getFragmentManager();
                                fm.beginTransaction().replace(R.id.content_frame, new Settings()).commit();
                            } else {

                                Toast.makeText(getContext(), "Syncing with PWR", Toast.LENGTH_SHORT).show();

                                //pd = ProgressDialog.show(DeliveryEventsTable.th is,"Loading...", true, false);
                                Intent i = new Intent(getActivity(), SyncPwr.class);
                                startActivity(i);

                                tableDataAdapter = new TableDataAdapter(getContext(), DataFactory.createDeliveryEventsList(getContext()), carTableView);
                                tableDataAdapter.notifyDataSetChanged();
                            }

                            refreshIndicator.hide();
                        }
                    }, 3000);
                }
            });
        }

        // Inflate the layout for this fragment
        return rootView;
    }

    private class CarClickListener implements TableDataClickListener<DeliveryEvent> {

        @Override
        public void onDataClicked(final int rowIndex, final DeliveryEvent clickedData) {
            String city = "New Ulm";
            String carString = clickedData.getAddress()  + ", " + city;

            Toast.makeText(getContext(), carString, Toast.LENGTH_SHORT).show();

            Intent i = new Intent(getActivity(), DeliveryEventDetails.class);
            Bundle bundle = new Bundle();
            bundle.putString("ticket_id",clickedData.getTicketID().toString() );
            i.putExtras(bundle);

            startActivity(i);
        }
    }

    private class CarLongClickListener implements TableDataLongClickListener<DeliveryEvent> {

        @Override
        public boolean onDataLongClicked(final int rowIndex, final DeliveryEvent clickedData) {
            final String carString = "Long Click: " + rowIndex + " " + clickedData.getAddress();
            Toast.makeText(getContext(), carString, Toast.LENGTH_SHORT).show();



            //address nav
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://maps.google.co.in/maps?q=" + carString));
            startActivity(intent);
            return true;
        }
    }

}
