package com.deliverytips;

import android.content.ContentValues;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Andrew Swenson on 7/15/2017.
 *
 * Purpose This class holds on to db details and actions

 */

public class DeliveryEvent {

    static String TABLE_NAME = "DeliveryEvents";
    static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    //column names
    static String COLUMN_NAME_ID = "id";
    static String COLUMN_NAME_TIMESTAMP = "timestamp";
    static String COLUMN_NAME_PRICE = "price";
    static String COLUMN_NAME_CUSTOMER_ID = "customer_id";

    //private variables
    int _id;
    String _timestamp = "0000-00-00 00:00:00";
    Double _price = 0.00;
    Person _person = null;

    //TODO does not handle null strings
    public String toString(){

        //return "delivery event to string - unimplemented";
        return _timestamp + " " + _price + " " + _person.toString();
    }

    // Empty constructor
    public DeliveryEvent(){

    }

    public DeliveryEvent( Double price, Person Person){

    }

    // set time stamp
    public void setTimestamp( String timestamp ){
        _timestamp = timestamp;
    }

    public void setTimestampNow(){
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        String millisInString  = dateFormat.format(new Date());
        _timestamp = millisInString;
    }

    //TODO negative check?
    public void setPrice(Double price){

        if( price == null ){
            _price = 0.00;
        }

        //_price = Double.parseDouble(price);
        _price = price;
    }

    public void setPerson( Person person ){
        _person = person;
    }


    public ContentValues getContentValues(){

        ContentValues c = new ContentValues();

        c.put("customer_id", this._person.id);
        c.put("timestamp", this._timestamp);
        c.put("price", this._price);

        return c;
    }
    public String getTimestamp(){
        return _timestamp;
    }

    //public Content

    public boolean isValid(){

        if( _timestamp == null ){
            return false;
        }

        if( _person == null ){
            return false;
        }

        if( _price == null ){
            return false;
        }

        return true;
    }



}
