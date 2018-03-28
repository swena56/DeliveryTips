package com.deliverytips.http_helpers;

import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.deliverytips.MainActivity;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by swena56 on 10/4/2017.
 */

class AuthRequest extends JsonObjectRequest {

    public AuthRequest(int method, String url, JSONObject jsonRequest,
                       Response.Listener<JSONObject> listener,
                       Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
    }

    public AuthRequest(String url, JSONObject jsonRequest,
                       Response.Listener<JSONObject> listener,
                       Response.ErrorListener errorListener) {
        super(url, jsonRequest, listener, errorListener);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return createBasicAuthHeader("user", "password");
    }

    Map<String, String> createBasicAuthHeader(String username, String password) {
        Map<String, String> headerMap = new HashMap<String, String>();

        String credentials = username + ":" + password;
        String encodedCredentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        headerMap.put("Authorization", "Basic " + encodedCredentials);

        return headerMap;
    }
}


public class GetRequest{

        public static String simpleRequest(){

            final String mTextView = "";

            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(MainActivity.get());
            String url ="http://www.google.com";
            RequestQueue mRequestQueue;
            return "data";
        }

        public static String getRequest(String store_id) {



                //new AuthRequest()
                //AuthRequest authRequest = new AuthRequest(Request.Method.GET,"",new Response.Listener<JSONObject>());

                //AuthRequest authRequest = new AuthRequest("GET",)
                URL imageUrl = null;
                try {
                    imageUrl = new URL("https://pwr-deliveries.ddns.net/delivery/1953");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }




                return "";
    //            String basicAuth = "Basic " + new String(Base64.encode("user:pass".getBytes(),Base64.NO_WRAP ));
    //
    //            HttpURLConnection conn = null;
    //            try {
    //                conn = (HttpURLConnection) imageUrl
    //                        .openConnection();
    //            } catch (IOException e) {
    //                e.printStackTrace();
    //            }
    //            conn.setRequestProperty("Authorization",basicAuth);
    //            conn.setConnectTimeout(30000);
    //            conn.setReadTimeout(30000);
    //            conn.setInstanceFollowRedirects(true);
    //            InputStream is = null;
    //            try {
    //                is = conn.getInputStream();
    //            } catch (IOException e) {
    //                e.printStackTrace();
    //            }
    //
    //            Log.d("http",is.toString());
    //
    //            return is.toString();

    //            StringBuffer stringBuffer = new StringBuffer("");
    //            BufferedReader bufferedReader = null;
    //
    //            Log.d("http","test");
    //            try {
    //                HttpClient httpClient = new DefaultHttpClient();
    //                HttpGet httpGet = new HttpGet();
    //
    //                HttpPost httpPost = new HttpPost();
    //                String url = "https://pwr.dominos.com/PWR/Login.aspx?ReturnUrl=RealTimeOrderDetail.aspx?FilterCode=sr_"+store_id+"&FilterDesc=Store-"+store_id;
    //                url = "https://pwr-deliveries.ddns.net/delivery/"+store_id;
    //                URI uri = new URI(url);
    //
    //                httpGet.setURI(uri);
    //                httpGet.addHeader(BasicScheme.authenticate(
    //                        new UsernamePasswordCredentials("user", "password"),
    //                        HTTP.UTF_8, false));
    //
    //                HttpResponse httpResponse = httpClient.execute(httpGet);
    //                Log.d("http",httpResponse.toString());
    //
    //                InputStream inputStream = httpResponse.getEntity().getContent();
    //                bufferedReader = new BufferedReader(new InputStreamReader(
    //                        inputStream));
    //
    //                String readLine = bufferedReader.readLine();
    //                while (readLine != null) {
    //                    stringBuffer.append(readLine);
    //                    stringBuffer.append("\n");
    //                    readLine = bufferedReader.readLine();
    //                }
    //            } catch (Exception e) {
    //                // TODO: handle exception
    //                Log.d("http",e.toString());
    //            } finally {
    //                if (bufferedReader != null) {
    //                    try {
    //                        bufferedReader.close();
    //                    } catch (IOException e) {
    //                        // TODO: handle exception
    //                    }
    //                }
    //            }
    //            Log.d("http",stringBuffer.toString());
    //            return stringBuffer.toString();
        }

}
