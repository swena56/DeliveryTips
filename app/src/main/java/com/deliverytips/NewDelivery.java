package com.deliverytips;


import android.app.Fragment;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.deliverytips.table.data.DeliveryEvent;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewDelivery extends Fragment implements View.OnClickListener {

    Button saveButton;
    EditText editTextPhoneNumber;
    EditText editTextAddress;
    EditText editTextPrice;

    public NewDelivery() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_new_delivery, container, false);

        saveButton = (Button) rootView.findViewById(R.id.buttonSave);
        saveButton.setOnClickListener(this);

        editTextPhoneNumber = (EditText) rootView.findViewById(R.id.editText5);
        editTextAddress = (EditText) rootView.findViewById(R.id.editTextAddress);
        editTextPrice = (EditText) rootView.findViewById(R.id.editTextPrice);

        return rootView;
    }

    @Override
    public void onClick(View view) {
        if( view.getId() == R.id.buttonSave ){

            MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(getContext());
            SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();

            DeliveryEvent deliveryEvent = new DeliveryEvent();
                deliveryEvent._price = Double.parseDouble(editTextPrice.getText().toString());
                db.insert(deliveryEvent.TABLE_NAME, null, deliveryEvent.getContentValues());
                Toast.makeText(getContext(), "Delivery Event: " + deliveryEvent.getContentValues().toString(), Toast.LENGTH_SHORT).show();

            //search to see if person exists
//            Cursor cursor = db.query(
//                    Person.TABLE_NAME,
//                    new String[] {
//                            Person.COLUMN_NAME_ID,
//                            Person.COLUMN_NAME_PHONE_NUMBER,
//                            Person.COLUMN_NAME_ADDRESS
//                    },
//                    Person.COLUMN_NAME_PHONE_NUMBER + "=" + editTextPhoneNumber.getText().toString(), null, null, null, null);
//
//            Person person = new Person();
//
//            if( cursor.getCount() > 0 ) {
//
//                while (cursor.moveToNext()) {
//                    person = new Person(cursor);
//                    //Toast.makeText(getContext(),person._phone_number, Toast.LENGTH_SHORT).show();
//                }
//            } else {
//
//                //Insert a person
//                person = new Person();
//                person._address = editTextAddress.getText().toString();
//                person._phone_number =  editTextPhoneNumber.getText().toString();
//                person._id = db.insert(person.TABLE_NAME, null, person.getContentValues());
//
//                Toast.makeText(getContext(),"Person: ( " + person._id + " ) " + person.getContentValues().toString(), Toast.LENGTH_SHORT).show();
//            }
//
//            if( person.isValidPerson()) {
//
//                //save deliveryEvent object
//                DeliveryEvent deliveryEvent = new DeliveryEvent();
//                deliveryEvent.setPrice(Double.parseDouble(editTextPrice.getText().toString()));
//                deliveryEvent.setTimestampNow();
//                deliveryEvent.setPerson(person);
//                db.insert(deliveryEvent.TABLE_NAME, null, deliveryEvent.getContentValues());
//                Toast.makeText(getContext(), "Delivery Event: " + deliveryEvent.getContentValues().toString(), Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(getContext(), "Invalid Person", Toast.LENGTH_SHORT).show();
//            }

            //destory fragment
            getFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
        }
    }
}
