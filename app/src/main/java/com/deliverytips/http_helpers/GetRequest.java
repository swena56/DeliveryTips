package com.deliverytips.http_helpers;

import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

/**
 * Created by swena56 on 10/4/2017.
 */

public class GetRequest{

        public static String getRequest(String store_id) {
            StringBuffer stringBuffer = new StringBuffer("");
            BufferedReader bufferedReader = null;
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet();

                HttpPost httpPost = new HttpPost();
                String url = "https://pwr.dominos.com/PWR/Login.aspx?ReturnUrl=RealTimeOrderDetail.aspx?FilterCode=sr_"+store_id+"&FilterDesc=Store-"+store_id;
                URI uri = new URI(url);

                httpGet.setURI(uri);
                httpGet.addHeader(BasicScheme.authenticate(
                        new UsernamePasswordCredentials("user", "password"),
                        HTTP.UTF_8, false));

                HttpResponse httpResponse = httpClient.execute(httpGet);
                InputStream inputStream = httpResponse.getEntity().getContent();
                bufferedReader = new BufferedReader(new InputStreamReader(
                        inputStream));

                String readLine = bufferedReader.readLine();
                while (readLine != null) {
                    stringBuffer.append(readLine);
                    stringBuffer.append("\n");
                    readLine = bufferedReader.readLine();
                }
            } catch (Exception e) {
                // TODO: handle exception
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        // TODO: handle exception
                    }
                }
            }
            return stringBuffer.toString();
        }

}
