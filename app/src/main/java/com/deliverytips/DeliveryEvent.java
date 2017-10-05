package com.deliverytips;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

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
    static String COLUMN_NAME_ORDER_NUMBER = "order_number";
    static String COLUMN_NAME_TIMESTAMP = "timestamp";
    static String COLUMN_NAME_PRICE = "price";
    static String COLUMN_NAME_CUSTOMER_ID = "customer_id";

    //private variables
    long _id;
    long _order_number;
    String _timestamp = "0000-00-00 00:00:00";
    Double _price = 0.00;
    Person _person = null;
    long _person_id;

    //TODO does not handle null strings
    public String toString(){

        //return "delivery event to string - unimplemented";
        return _timestamp + " " + _price + " " + _person.toString();
    }

    // Empty constructor
    public DeliveryEvent(){

    }

    public DeliveryEvent(long _order_number, Context context){

    }

    DeliveryEvent(Cursor cursor){
        if( cursor != null ){
            this._id = Long.parseLong( cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_ID)));
            this._order_number = Long.parseLong( cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_ORDER_NUMBER)));
            this._price = Double.parseDouble(cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_PRICE)));
            this._timestamp = cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_TIMESTAMP));
            this._person_id = Long.parseLong(cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_CUSTOMER_ID)));
        }
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

    public void setOrderNumber(long orderNumber){
        this._order_number = orderNumber;
    }

    //TODO change so DeliveryEvent only uses person_id not the person obj
    public void setPerson( Person person ){
        _person = person;
    }

    public void setPersonId( long id ){
        _person_id = id;
    }

    public String getTimestamp(){
        return _timestamp;
    }

    public ContentValues getContentValues(){
        ContentValues c = new ContentValues();
        c.put(this.COLUMN_NAME_PRICE, this._price);
        c.put(this.COLUMN_NAME_ORDER_NUMBER, this._order_number);
        c.put(this.COLUMN_NAME_TIMESTAMP, this._timestamp);

       if( this._person != null  ){
           c.put(this.COLUMN_NAME_CUSTOMER_ID, this._person._id);
       } else {
           c.put(this.COLUMN_NAME_CUSTOMER_ID, this._person_id);
       }

        return c;
    }

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
