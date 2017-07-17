package com.deliverytips;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


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

            //search to see if person exists

            Person person = new Person();
            person._address = editTextAddress.getText().toString();
            person._phone_number =  editTextPhoneNumber.getText().toString();
            Toast.makeText(getContext(),"Person: " + person.getContentValues().toString(), Toast.LENGTH_SHORT).show();

            //save deliveryEvent object
            DeliveryEvent deliveryEvent = new DeliveryEvent();
            deliveryEvent.setPrice( Double.parseDouble( editTextPrice.getText().toString() ));
            deliveryEvent.setTimestampNow();
            deliveryEvent.setPerson(person);
            Toast.makeText(getContext(),"Delivery Event: " + deliveryEvent.getContentValues().toString(), Toast.LENGTH_SHORT).show();

//            MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(getContext());
//            SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();
//            long newRowId = db.insert(person.TABLE_NAME, null, p);

            //Toast.makeText(getContext(),"Saved new Delivery Event with id " + newRowId, Toast.LENGTH_SHORT).show();













        }
    }
}
