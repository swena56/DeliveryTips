package com.deliverytips.table;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.Volley;
import com.deliverytips.DeliveryEventDetails;
import com.deliverytips.MyDatabaseHelper;
import com.deliverytips.R;
import com.deliverytips.Settings;
import com.deliverytips.SyncPwr;
import com.deliverytips.table.data.DataFactory;
import com.deliverytips.table.data.DeliveryEvent;

import org.apache.http.client.CookieStore;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.codecrafters.tableview.listeners.SwipeToRefreshListener;
import de.codecrafters.tableview.listeners.TableDataClickListener;
import de.codecrafters.tableview.listeners.TableDataLongClickListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeliveryEventsTable extends Fragment {

    TableDataAdapter tableDataAdapter;
    SharedPreferences sharedPref;
    public SortableDeliveryEventsTableView carTableView;
    public String store_id;
    public String username;
    public String password;
    public String address;
    public RequestQueue queue;
    public String url;
    public DefaultHttpClient mDefaultHttpClient;
    public CookieStore cs;
    public String city_state;
    public RequestQueue mRequestQueue; //for single function
    //public CookieManager cookieManager;
    //public java.net.CookieManager cm;

    ProgressDialog pd;

    public String selectedItem;
    TextView numDeleliveries;
    TextView totalTips;
    TextView averageTips;

    //select driver list pull down
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_delivery_events_table, container, false);

        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        store_id = sharedPref.getString("store_id", "" );
        username = sharedPref.getString("username", "" );
        password = sharedPref.getString("password", "" );
        address = sharedPref.getString("address", "" );

        numDeleliveries = (TextView) rootView.findViewById(R.id.textViewNumEvents);
        totalTips = (TextView) rootView.findViewById(R.id.textViewTotalTips);
        averageTips = (TextView) rootView.findViewById(R.id.textViewAvgTips);

        Spinner spinner = (Spinner) rootView.findViewById(R.id.select_driver);
        List<String> driver_list = prepareListData();

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, driver_list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        city_state = sharedPref.getString("address", String.valueOf(R.string.default_city).toString());
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                selectedItem = parent.getItemAtPosition(position).toString();

                Toast.makeText(getContext(),"Selected Driver: " + selectedItem,Toast.LENGTH_SHORT ).show();

                //Save driver to preferences
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("selected_driver", selectedItem);
                editor.commit();

                //Set Dealer Stats
                Map<String,String> map =  DataFactory.GetDriverStats(getContext(),selectedItem);
                numDeleliveries.setText(map.get("size") + "   ( $"+map.get("total_price") +" ) " );
                totalTips.setText("$" + map.get("total_tip"));
                averageTips.setText("$" + map.get("avg_tip"));

                tableDataAdapter = new TableDataAdapter(getContext(), DataFactory.createDeliveryEventsList(getContext(),selectedItem), carTableView);
                carTableView.setDataAdapter(tableDataAdapter);
                //tableDataAdapter.getData();
            } // to close the onItemSelected

            public void onNothingSelected(AdapterView<?> parent)
            {
                Toast.makeText(getContext(),"No Driver Filter",Toast.LENGTH_SHORT ).show();
                //selectedItem = null;
            }
        });

        String sel_driver = sharedPref.getString("selected_driver",null);
        if( sel_driver != null && driver_list.contains(sel_driver)){
            spinner.setSelection(driver_list.indexOf(sel_driver));

            //Query Statistics
            Map<String,String> map =  DataFactory.GetDriverStats(getContext(),sel_driver);

            //set number of deliveries
            numDeleliveries.setText(map.get("size"));

            totalTips.setText(map.get("size"));
        }

        carTableView = (SortableDeliveryEventsTableView) rootView.findViewById(R.id.tableView);
        if (carTableView != null) {
            tableDataAdapter = new TableDataAdapter(getContext(), DataFactory.createDeliveryEventsList(getContext(), selectedItem), carTableView);
            carTableView.setDataAdapter(tableDataAdapter);
            carTableView.addDataClickListener(new CarClickListener());
            carTableView.addDataLongClickListener(new CarLongClickListener());
            carTableView.setSwipeToRefreshEnabled(true);
            carTableView.setSwipeToRefreshListener(new SwipeToRefreshListener() {
                @Override
                public void onRefresh(final RefreshIndicator refreshIndicator) {
                    carTableView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //tableDataAdapter.getData();

                            String problem_log = "";
                            if( store_id == "" ){
                                problem_log += "Missing Store Identification Number.\n";
                            }

                            if( username == "" ){
                                problem_log += "Missing Username \n";
                            }

                            if( password == "" ){
                                problem_log += "Missing password \n";
                            }

                            if( store_id == "" || username == "" || password == "" ){
                                Toast.makeText(getContext(), "Error: " + problem_log, Toast.LENGTH_SHORT).show();
                                FragmentManager fm = getActivity().getFragmentManager();
                                fm.beginTransaction().replace(R.id.content_frame, new Settings()).commit();
                            } else {

                                Toast.makeText(getContext(), "Syncing with PWR", Toast.LENGTH_SHORT).show();

                                //pd = ProgressDialog.show(DeliveryEventsTable.th is,"Loading...", true, false);
                                Intent i = new Intent(getActivity(), SyncPwr.class);
                                i.putExtra("store_id",store_id);
                                i.putExtra("username",username);
                                i.putExtra("password",password);
                                startActivity(i);

                                tableDataAdapter = new TableDataAdapter(getContext(), DataFactory.createDeliveryEventsList(getContext(), selectedItem), carTableView);
                                tableDataAdapter.notifyDataSetChanged();
                            }

                            refreshIndicator.hide();
                        }
                    }, 3000);
                }
            });
        }

        // Inflate the layout for this fragment
        return rootView;
    }

    private List<String> prepareListData() {

        MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(getContext());
        SQLiteDatabase db = myDatabaseHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + DeliveryEvent.COLUMN_NAME_DRIVER + " FROM " + DeliveryEvent.TABLE_NAME +
                        " WHERE " + DeliveryEvent.COLUMN_NAME_DRIVER + " IS NOT NULL " +
                        " GROUP BY  " + DeliveryEvent.COLUMN_NAME_DRIVER,
                null);
        List<String> selectedDriver = new ArrayList<String>();
        selectedDriver.add("No Driver Selected.");
        while( cursor.moveToNext() ){
            String driver = cursor.getString(cursor.getColumnIndex(DeliveryEvent.COLUMN_NAME_DRIVER));
            if( driver != null && driver != "" && driver.length() > 0 && !driver.equals(" ()") ){
                selectedDriver.add(driver);
            }
        }
        cursor.close();
        db.close();
        return selectedDriver;
    }

    private class CarClickListener implements TableDataClickListener<DeliveryEvent> {

        @Override
        public void onDataClicked(final int rowIndex, final DeliveryEvent clickedData) {

            if( clickedData.getAddress() != null ) {

                //currently need a way to get state and city from settings
                String carString = clickedData.getAddress() + " New Ulm, MN";

                Toast.makeText(getContext(), carString, Toast.LENGTH_SHORT).show();

                Intent i = new Intent(getActivity(), DeliveryEventDetails.class);
                Bundle bundle = new Bundle();
                bundle.putString("ticket_id", clickedData.getTicketID().toString());
                bundle.putString("address", carString);
                i.putExtras(bundle);

                startActivity(i);
            }
        }
    }

    private class CarLongClickListener implements TableDataLongClickListener<DeliveryEvent> {

        @Override
        public boolean onDataLongClicked(final int rowIndex, final DeliveryEvent clickedData) {
            final String carString = "Long Click: " + rowIndex + " " + clickedData.getAddress();
            Toast.makeText(getContext(), carString, Toast.LENGTH_SHORT).show();

            //address nav
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://maps.google.co.in/maps?q=" + carString));
            startActivity(intent);
            return true;
        }
    }

    public class ExpandableListAdapter extends BaseExpandableListAdapter {

        private Context _context;
        private List<String> _listDataHeader; // header titles
        // child data in format of header title, child title
        private HashMap<String, List<String>> _listDataChild;

        public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                     HashMap<String, List<String>> listChildData) {
            this._context = context;
            this._listDataHeader = listDataHeader;
            this._listDataChild = listChildData;
        }

        @Override
        public Object getChild(int groupPosition, int childPosititon) {
            return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                    .get(childPosititon);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, final int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {

            final String childText = (String) getChild(groupPosition, childPosition);

            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.list_item, null);
            }

            TextView txtListChild = (TextView) convertView
                    .findViewById(R.id.textViewSelectDriver);

            txtListChild.setText(childText);
            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                    .size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return this._listDataHeader.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return this._listDataHeader.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            String headerTitle = (String) getGroup(groupPosition);
            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.list_group, null);
            }

            TextView lblListHeader = (TextView) convertView
                    .findViewById(R.id.select_driver_group);
            lblListHeader.setTypeface(null, Typeface.BOLD);
            lblListHeader.setText(headerTitle);

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
    public RequestQueue getRequestQueue() {

        if ( this.queue == null ) {

            mDefaultHttpClient = new DefaultHttpClient();

            final ClientConnectionManager mClientConnectionManager = mDefaultHttpClient.getConnectionManager();
            final HttpParams mHttpParams = mDefaultHttpClient.getParams();
            final ThreadSafeClientConnManager mThreadSafeClientConnManager = new ThreadSafeClientConnManager( mHttpParams, mClientConnectionManager.getSchemeRegistry() );

            mDefaultHttpClient = new DefaultHttpClient( mThreadSafeClientConnManager, mHttpParams );

            final HttpStack httpStack = new HttpClientStack( mDefaultHttpClient );

            this.queue = Volley.newRequestQueue( getContext(), httpStack );
        }

        return this.queue;
    }
}
