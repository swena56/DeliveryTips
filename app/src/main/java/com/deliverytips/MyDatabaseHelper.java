package com.deliverytips;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Andrew Swenson on 7/15/2017.
 *
 * Used for creating and updating the Database
 * TODO what db datatype to use for timestamp
 * TODO setup Person DB
 * TODO setup Locations DB
 * TODO loadup form for creating new Delivery Event
 */

public class MyDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "DB";

    private static final int DATABASE_VERSION = 15;

    // Database creation sql statement
    public static final String CREATE_DELIVERY_EVENT = "CREATE TABLE IF NOT EXISTS " + DeliveryEvent.TABLE_NAME +
            " ( " +
                DeliveryEvent.COLUMN_NAME_ID + " integer primary key, " +
                DeliveryEvent.COLUMN_NAME_ORDER_NUMBER + " integer, " +
                DeliveryEvent.COLUMN_NAME_TIMESTAMP + " text, " +
                DeliveryEvent.COLUMN_NAME_STREET + " text, " +
                DeliveryEvent.COLUMN_NAME_FULL_NAME + " text, " +
                DeliveryEvent.COLUMN_NAME_CSR + "  text, " +
                DeliveryEvent.COLUMN_NAME_DRIVER + "  text, " +
                DeliveryEvent.COLUMN_NAME_PHONE_NUMBER + "  text, " +
                DeliveryEvent.COLUMN_NAME_PRICE + "  double, " +
                DeliveryEvent.COLUMN_NAME_TIP + "  double, " +
                DeliveryEvent.COLUMN_NAME_DESCRIPTION + " text, " +
                DeliveryEvent.COLUMN_NAME_NOTES + " text " +
               // DeliveryEvent.COLUMN_NAME_CUSTOMER_ID + "  integer" +
            ");";


    public static final String CREATE_PERSON_TABLE = "CREATE TABLE IF NOT EXISTS " + Person.TABLE_NAME +
            "(" +  Person.COLUMN_NAME_ID + " integer primary key , " +
            Person.COLUMN_NAME_PHONE_NUMBER + " text, " +
            Person.COLUMN_NAME_ADDRESS + " text );";




    //DeliveryEvent.
    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //is the db created
    public int databaseExists(){
        //this.getDatabaseName().
        //DATABASE_NAME

        return 1;
    }

    public int insertDeliveryEvent( DeliveryEvent deliveryEvent){

        return 1 ;
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase database) {

        database.execSQL(CREATE_DELIVERY_EVENT);
        database.execSQL(CREATE_PERSON_TABLE);
    }

    // Method is called during an upgrade of the database,
    @Override
    public void onUpgrade(SQLiteDatabase database,int oldVersion,int newVersion){
        Log.w(MyDatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");

        database.execSQL("DROP TABLE IF EXISTS " + DeliveryEvent.TABLE_NAME);
        database.execSQL("DROP TABLE IF EXISTS " + Person.TABLE_NAME);
        onCreate(database);
    }
}