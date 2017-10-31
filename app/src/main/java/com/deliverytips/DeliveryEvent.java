package com.deliverytips;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.deliverytips.table.data.Chargable;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Andrew Swenson on 7/15/2017.
 *
 * Purpose This class holds on to db details and actions

 */

public class DeliveryEvent implements Chargable {

    public static String TABLE_NAME = "DeliveryEvents";
    static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    //column names and class variables
    public static String COLUMN_NAME_ID = "id";
    public long _id;

    public static String COLUMN_NAME_ORDER_NUMBER = "order_number";
    public long _order_number;

    public static String COLUMN_NAME_TIMESTAMP = "timestamp";
    public String _timestamp = "0000-00-00 00:00:00";

    public static String COLUMN_NAME_PHONE_NUMBER = "phone_number";
    public String _phone = null;

    public static String COLUMN_NAME_FULL_NAME = "full_name";
    public String _full_name = null;

    public static String COLUMN_NAME_STREET = "street";
    public String _street = null;

    public static String COLUMN_NAME_PRICE = "price";
    public Double _price = 0.00;

    public static String COLUMN_NAME_STATUS = "status";
    public String _status = null;

    public static String COLUMN_NAME_SOURCE = "source";
    public String _source = null;

    public static String COLUMN_NAME_SERVICE_METHOD = "service_method";
    public String _service = "";

    public static String COLUMN_NAME_CSR = "csr";
    public String _csr = "";

    public static String COLUMN_NAME_DRIVER = "driver";
    public String _driver = "";

    public static String COLUMN_NAME_DESCRIPTION = "description";
    public String _description = "";

    public static String COLUMN_NAME_NOTES = "notes";
    public String _notes = "";

    public static String COLUMN_NAME_TIP = "tip";
    public Double _tip = 0.00;

    public static String COLUMN_NAME_CUSTOMER_ID = "customer_id";

    //public Person _person = null;
    public long _person_id;

    //TODO does not handle null strings
    public String toString(){

        //return "delivery event to string - unimplemented";
        return _timestamp + " " + _price;
    }

    // Empty constructor
    public DeliveryEvent(){

    }

    public DeliveryEvent(long _order_number, Context context){

    }

    public DeliveryEvent(Cursor cursor){
        if( cursor != null ){
            this._id = Long.parseLong( cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_ID)));
            this._order_number = Long.parseLong( cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_ORDER_NUMBER)));
            this._service = cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_SERVICE_METHOD));
            this._price = Double.parseDouble(cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_PRICE)));
            this._tip = Double.parseDouble(cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_TIP)));
            this._timestamp = cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_TIMESTAMP));
            this._phone = cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_PHONE_NUMBER));
            this._street = cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_STREET));

            //add full_name
            if( cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_FULL_NAME) != -1) {
                this._full_name = cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_FULL_NAME));
            }

            //add driver
            if( cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_DRIVER) != -1) {
                this._driver = cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_DRIVER));
            }

            //add descescription
            if( cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_DESCRIPTION) != -1) {
                this._description = cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_DESCRIPTION));
            }

            //add notes
            if( cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_NOTES) != -1) {
                this._notes = cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_NOTES));
            }

            //add service method
//            if( cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_SERVICE_METHOD) != -1) {
//                this._service = cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_SERVICE_METHOD));
//            }

            //this._person_id = Long.parseLong(cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_CUSTOMER_ID)));
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
    /*
    public void setPerson( Person person ){
        _person = person;
    }
    */

    public void setPersonId( long id ){
        _person_id = id;
    }

    public String getTimestamp(){
        return _timestamp;
    }

    public ContentValues getContentValues(){
        ContentValues c = new ContentValues();
        c.put(this.COLUMN_NAME_PRICE, this._price);
        c.put(this.COLUMN_NAME_TIP, this._tip);
        c.put(this.COLUMN_NAME_SERVICE_METHOD, this._service);
        c.put(this.COLUMN_NAME_ORDER_NUMBER, this._order_number);
        c.put(this.COLUMN_NAME_TIMESTAMP, this._timestamp);
        c.put(this.COLUMN_NAME_FULL_NAME, this._full_name);
        c.put(this.COLUMN_NAME_CSR, this._csr);
        c.put(this.COLUMN_NAME_DESCRIPTION, this._description);
        c.put(this.COLUMN_NAME_PHONE_NUMBER, this._phone);
        c.put(this.COLUMN_NAME_STREET, this._street);
        c.put(this.COLUMN_NAME_NOTES, this._notes);
        c.put(this.COLUMN_NAME_DRIVER, this._driver);

//       if( this._person != null  ){
//           c.put(this.COLUMN_NAME_CUSTOMER_ID, this._person._id);
//       } else {
//           c.put(this.COLUMN_NAME_CUSTOMER_ID, this._person_id);
//       }

        return c;
    }

    public boolean isValid(){

        if( _timestamp == null ){
            return false;
        }

//        if( _person == null ){
//            return false;
//        }

        if( _price == null ){
            return false;
        }

        return true;
    }

    @Override
    public double getPrice() {
        return 0;
    }
}
