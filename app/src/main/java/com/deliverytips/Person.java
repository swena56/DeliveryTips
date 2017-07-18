package com.deliverytips;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by Andrew Swenson on 7/15/2017.
 */

public class Person  {

    static String TABLE_NAME = "Person";

    //column names
    static String COLUMN_NAME_ID = "id";
    static String COLUMN_NAME_ADDRESS= "address";
    static String COLUMN_NAME_PHONE_NUMBER= "phone_number";

    public long _id = 0;
    public String _first_name;
    public String _last_name;
    public String _address;
    public String _phone_number;


    Person(){

    }

    Person(long person_id){
        //search for person with matching id
    }

    Person(Cursor cursor){
        if( cursor != null ){
            this._id = Long.parseLong( cursor.getString(cursor.getColumnIndex(Person.COLUMN_NAME_ID)));
            this._address = cursor.getString(cursor.getColumnIndex(Person.COLUMN_NAME_ADDRESS));
            this._phone_number = cursor.getString(cursor.getColumnIndex(Person.COLUMN_NAME_PHONE_NUMBER));
        }
    }

    public String toString(){

        String string = "error - invalid person";

        if( isValidPerson() ){
            string = _first_name + " " + _last_name + " " + _phone_number;


        }

        return string;
    }

    public Person setCursor(Cursor cursor){

        if( cursor != null ){
            this._id = Long.parseLong( cursor.getString(cursor.getColumnIndex(Person.COLUMN_NAME_ID)));
            this._address = cursor.getString(cursor.getColumnIndex(Person.COLUMN_NAME_ADDRESS));
            this._phone_number = cursor.getString(cursor.getColumnIndex(Person.COLUMN_NAME_PHONE_NUMBER));
            return this;
        }

        return null;
    }

    public ContentValues getContentValues(){
        ContentValues c = new ContentValues();
        c.put(this.COLUMN_NAME_PHONE_NUMBER, this._phone_number);
        c.put(this.COLUMN_NAME_ADDRESS, this._address);

        return c;
    }

    public boolean isValidPerson(){

        if( _phone_number == null ){
            return false;
        }

        return true;
    }
}
