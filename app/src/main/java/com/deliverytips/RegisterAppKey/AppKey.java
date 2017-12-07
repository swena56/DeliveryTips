package com.deliverytips.RegisterAppKey;

import android.app.Activity;
import android.content.Context;
import android.util.Base64;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.deliverytips.R;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by swena56 on 12/5/2017.
 *
 * ## Manually generate a strong key pair, via linux command shell.
 *
 * ## First generate the RSA key
 * openssl genrsa -des3 -out private.pem 2048
 *
 * ## Generate the public key
 * openssl rsa -in private.pem -outform PEM -pubout -out public.pem
 *
 */

public class AppKey {

    private String keyUrl;
    private String key;

    private EnCryptor encryptor;
    private DeCryptor decryptor;
    private Activity activity;

    public AppKey(Activity activity){

        this.activity = activity;

        //initalize encryption and decryption
        encryptor = new EnCryptor();

        try {
            decryptor = new DeCryptor();
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException |
                IOException e) {
            e.printStackTrace();
        }



        //check if app key is set
        //SharedPreferences sharedPreferences = activity.getSharedPreferences();
        //sharedPreferences.getString("","");

        //Encrypting and decrypting is only allowed when the application key is valid
    }


    //basic encrypting and decrypting
    public String decryptText(String alias, byte[] ivs, byte[] encryption  ) throws UnsupportedEncodingException {
        byte[] bytes;

        if( this.activity != null && alias != null  ) {
            if (ivs != null && ivs.length > 0) {
                try {
                    //bytes = decryptor.decryptData(this.activity.getString(R.string.enc_alias), encryptor.getEncryption(), ivs).getBytes("UTF-8");

                    //return Base64.decode(bytes,Base64.DEFAULT).toString();
                    if(encryption == null){
                        encryption = encryptor.getEncryption();
                    }

                    return decryptor.decryptData(alias, encryption, ivs);

                } catch (UnrecoverableEntryException | NoSuchAlgorithmException |
                        KeyStoreException | NoSuchPaddingException | NoSuchProviderException |
                        IOException | InvalidKeyException e) {
                } catch (IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                }
            }
        }

        return Base64.encodeToString(this.activity.getString(R.string.failed_encryption).getBytes("UTF-8"), Base64.DEFAULT);
    }

    public String encryptText(String alias,String text) throws UnsupportedEncodingException {

        if( this.activity != null && alias != null ) {
            try {

                byte[] encryptedText = encryptor
                        .encryptText(alias, text);
                //return encryptedText.toString();
                return Base64.encodeToString(encryptedText, Base64.DEFAULT);
            } catch (UnrecoverableEntryException | NoSuchAlgorithmException | NoSuchProviderException |
                    KeyStoreException | IOException | NoSuchPaddingException | InvalidKeyException e) {
            } catch (InvalidAlgorithmParameterException | SignatureException |
                    IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
            }
        }

        return Base64.encodeToString(this.activity.getString(R.string.failed_encryption).getBytes("UTF-8"), Base64.DEFAULT);
    }


    private Boolean IsValidKey(){

        if( this.key != null ){

            //has the key expired?
            return true;
        }

        return false;
    }

    public String getAppKey(){
        return null;
    }

    public String EncodeIvs(byte[] ivs){
        return Base64.encodeToString(ivs, Base64.DEFAULT);
    }

    public byte[] DecodeIvs(String encoded_ivs ){

        if( encoded_ivs != null ){
            return Base64.decode(encoded_ivs, Base64.DEFAULT);
        }

        return null;
    }

    public String GetEncodedIvs(){
        return Base64.encodeToString(encryptor.getIv(), Base64.DEFAULT);
    }

    public byte[] GetIvs(){
        return encryptor.getIv();
    }

    public byte[] GetEncryption(){
        return encryptor.getEncryption();
    }

    private void getAppPublicKey(Context context){

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);

        if( keyUrl != null ) {

            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.GET, keyUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Display the first 500 characters of the response string.
                            //mTextView.setText("Response is: "+ response.substring(0,500));
                        }
                    }, new Response.ErrorListener() {


                @Override
                public void onErrorResponse(VolleyError error) {
                    // mTextView.setText("That didn't work!");
                }
            });
            // Add the request to the RequestQueue.
            queue.add(stringRequest);
        }
    }
}
