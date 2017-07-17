package com.deliverytips;

import android.content.ContentValues;

/**
 * Created by Andrew Swenson on 7/15/2017.
 */

public class Person  {



    static String TABLE_NAME = "Person";

    //column names
    static String COLUMN_NAME_ID = "id";
    static String COLUMN_NAME_ADDRESS= "address";
    static String COLUMN_NAME_PHONE_NUMBER= "phone_number";




    int id = 0;
    public String _first_name;
    public String _last_name;
    public String _address;
    public String _phone_number;


    Person(){

    }

    public String toString(){

        String string = "error - invalid person";

        if( isValidPerson() ){
            string = _first_name + " " + _last_name + " " + _phone_number;


        }

        return string;
    }

    public ContentValues getContentValues(){
        ContentValues c = new ContentValues();
        c.put(this.COLUMN_NAME_PHONE_NUMBER, this._phone_number);
        c.put(this.COLUMN_NAME_ADDRESS, this._address);

        return c;
    }

    public boolean isValidPerson(){

        if( _first_name == null || _last_name == null ){
            return false;
        }

        if( _phone_number == null ){
            return false;
        }



        return true;
    }
}
