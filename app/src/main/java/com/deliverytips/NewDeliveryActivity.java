package com.deliverytips;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NewDeliveryActivity extends AppCompatActivity {


    SurfaceView cameraView;
    TextView textView;

    EditText editTextPhoneNumber;
    EditText editTextAddress;
    EditText editTextPrice;
    EditText editTextOrderNumber;
    Button buttonSave;
    Context context;

    CameraSource cameraSource;
    final int RequestCameraPermissionID = 1001;
    public DeliveryEvent de;

    Map<String, String> id_matches;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestCameraPermissionID: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
            break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_delivery);


        Toast.makeText(getApplicationContext(), "Scanning for text with Camera", Toast.LENGTH_SHORT).show();
        cameraView = (SurfaceView) findViewById(R.id.surfaceView);
        textView = (TextView) findViewById(R.id.textDetect);

        editTextPhoneNumber = ( EditText ) findViewById(R.id.editText5);
        editTextAddress = ( EditText ) findViewById(R.id.editTextAddress);
        editTextPrice = ( EditText ) findViewById(R.id.editTextPrice);
        editTextOrderNumber = ( EditText ) findViewById(R.id.editTextOrderNumber);
        buttonSave = ( Button ) findViewById(R.id.buttonSave);

        context = getBaseContext();
        final Activity activity = this;
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(context);
                SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();

                String phone = editTextPhoneNumber.getText().toString();

                //phone = phone.replaceAll("\\s+","");
                phone = phone.replaceAll("[^a-zA-Z0-9]", "");

                //search to see if person exists
                Cursor cursor = db.query(
                        Person.TABLE_NAME,
                        new String[] {
                                Person.COLUMN_NAME_ID,
                                Person.COLUMN_NAME_PHONE_NUMBER,
                                Person.COLUMN_NAME_ADDRESS
                        },
                        Person.COLUMN_NAME_PHONE_NUMBER + "=" + phone, null, null, null, null);

                Person person = new Person();

                if( cursor.getCount() > 0 ) {

                    while (cursor.moveToNext()) {
                        person = new Person(cursor);
                        //Toast.makeText(getContext(),person._phone_number, Toast.LENGTH_SHORT).show();
                    }
                } else {

                    //Insert a person
                    person = new Person();
                    person._address = editTextAddress.getText().toString();
                    person._phone_number =  phone;
                    person._id = db.insert(person.TABLE_NAME, null, person.getContentValues());

                    Toast.makeText(context,"Person: ( " + person._id + " ) " + person.getContentValues().toString(), Toast.LENGTH_SHORT).show();
                }

                if( person.isValidPerson()) {

                    //save deliveryEvent object
                    DeliveryEvent deliveryEvent = new DeliveryEvent();
                    deliveryEvent.setPrice(Double.parseDouble(editTextPrice.getText().toString()));
                    deliveryEvent.setTimestampNow();
                    deliveryEvent.setOrderNumber(Long.parseLong(editTextOrderNumber.getText().toString()));
                    deliveryEvent.setPerson(person);

                    //TODO check if delivery order already exists
                    cursor = db.query(
                        DeliveryEvent.TABLE_NAME,
                        new String[] {
                                DeliveryEvent.COLUMN_NAME_ORDER_NUMBER
                        },
                        DeliveryEvent.COLUMN_NAME_ORDER_NUMBER + "=" + editTextOrderNumber.getText().toString(), null, null, null, null);

                    if( cursor.getCount() == 0 ) {
                        db.insert(deliveryEvent.TABLE_NAME, null, deliveryEvent.getContentValues());
                        Toast.makeText(context, "Delivery Event: " + deliveryEvent.getContentValues().toString(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Delivery Event: Already Exists", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Invalid Person", Toast.LENGTH_SHORT).show();
                }

                //setContentView(R.layout.activity_main);
                activity.finish();
            }
        });

        //matches dataset
        id_matches = new HashMap<String, String>();

        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (!textRecognizer.isOperational()) {
            Log.w("New Delivery Activity", "Detector dependencies are not yet available");
        } else {

            cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setRequestedFps(2.0f)
                    .setAutoFocusEnabled(true)
                    .build();

            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {

                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(NewDeliveryActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    RequestCameraPermissionID);
                            return;
                        }
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    cameraSource.stop();
                }
            });

            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {

                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if(items.size() != 0)
                    {
                        textView.post(new Runnable() {
                            @Override
                            public void run() {

                                StringBuilder stringBuilder = new StringBuilder();
                                for(int i =0;i<items.size();++i)
                                {

                                    TextBlock item = items.valueAt(i);

                                    String text = item.getValue().toLowerCase();

                                    //could the text be a phone number
                                    if( text.matches(".*(\\d+).*") ) {

                                        if (validatePhoneNumber(text)) {

                                            //might need to strip the phone number
                                            Log.w("Detected Phone", text);
                                            editTextPhoneNumber.setText(text);
                                        } else {


                                            if (text.matches("(.)*(\\d{6})(.)*")) {

                                                String order = text;
                                                order = order.replaceAll("[^0-9]", "");

                                                if (order.length() == 6) {
                                                    Log.w("Detected Order", order);
                                                    editTextOrderNumber.setText(order);
                                                }

                                            }
                                            //detect zip
//                                            if (text.matches("(.)*(\\d{5})(.)*")) {
//
//                                                String zip = text;
//                                                zip = zip.replaceAll("[^0-9]", "");
//
//                                                if (zip.length() == 5) {
//                                                    Log.w("Detected Zip", zip);
//                                                    editTextAddress.setText(zip);
//                                                }
//                                            }
                                        }
                                    }

                                    if( text.matches(".*\\d+\\.\\d{2}.*") ){
                                        String price = text;
                                        price = price.replaceAll("[^0-9\\.]", "");
                                        editTextPrice.setText(price);
                                        //Log.w("Detected price", price);
                                    }


                                    //detect street
                                    if( text.matches(".*[ ][avenue|lane|road|boulevard|drive|street|ave|dr|rd|blvd|ln|st][ ].*") ) {
                                        if (text.matches(".*\\d+[ ].*[avenue|lane|road|boulevard|drive|street|ave|dr|rd|blvd|ln|st][ ].*")) {

                                            editTextAddress.setText(text);
                                        }
                                    }

                                            Log.w("New Delivery Activity", "Scanning: " + text);
                                    //stringBuilder.append(text);
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private static boolean validatePhoneNumber(String phoneNo) {
        //validate phone numbers of format "1234567890"
        if (phoneNo.matches("\\d{10}")) return true;
            //validating phone number with -, . or spaces
        else if(phoneNo.matches("\\d{3}[-\\.\\s]\\d{3}[-\\.\\s]\\d{4}")) return true;
            //validating phone number with extension length from 3 to 5
        else if(phoneNo.matches("\\d{3}-\\d{3}-\\d{4}\\s(x|(ext))\\d{3,5}")) return true;
        else if(phoneNo.matches("\\(\\d{3}\\) \\d{3}-\\d{4}")) return true;
            //validating phone number where area code is in braces ()
        else if(phoneNo.matches("\\(\\d{3}\\)-\\d{3}-\\d{4}")) return true;
            //return false if nothing matches the input
        else return false;
    }

    private static boolean check_string(String search_word, String test){

        if( test.toLowerCase().indexOf(search_word) != -1 ){
            return true;
        } else {
            return false;
        }
    }
}
