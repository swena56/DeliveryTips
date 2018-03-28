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
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.Volley;
import com.deliverytips.DeliveryEventDetails;
import com.deliverytips.MainActivity;
import com.deliverytips.MyDatabaseHelper;
import com.deliverytips.R;
import com.deliverytips.Settings;
import com.deliverytips.SyncPwrLogin;
import com.deliverytips.fragments.PwrSyncFragment;
import com.deliverytips.fragments.SummaryActivity;
import com.deliverytips.table.data.DataFactory;
import com.deliverytips.table.data.DeliveryEvent;

import org.apache.http.client.CookieStore;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.codecrafters.tableview.listeners.SwipeToRefreshListener;
import de.codecrafters.tableview.listeners.TableDataClickListener;
import de.codecrafters.tableview.listeners.TableDataLongClickListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeliveryEventsTable extends Fragment {

    public TableDataAdapter tableDataAdapter;
    SharedPreferences sharedPref;
    public SortableDeliveryEventsTableView carTableView;

    public static int SYNC_INTERVAL = 5;
    public String store_id;
    public String username;
    public String password;
    public String address;
    public RequestQueue queue;
    public String url;
    public DefaultHttpClient mDefaultHttpClient;
    public CookieStore cs;
    public CheckBox auto_hide;
    public String city_state;
    public RequestQueue mRequestQueue; //for single function
    public NumberFormat numberFormat;
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
    Timer timer;
    TimerTask doAsynchronousTask;
    private static DeliveryEventsTable _instance;

    public static DeliveryEventsTable get() {
        return _instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_delivery_events_table, container, false);

        _instance = this;

        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        store_id = sharedPref.getString("store_id", "" );

        numberFormat = NumberFormat.getNumberInstance(Locale.US);
        numDeleliveries = (TextView) rootView.findViewById(R.id.textViewNumEvents);
        totalTips = (TextView) rootView.findViewById(R.id.textViewTotalTips);
        averageTips = (TextView) rootView.findViewById(R.id.textViewAvgTips);

        //Launch Summary Activity, when small details layout is clicked
        LinearLayout linearLayoutSummary = (LinearLayout) rootView.findViewById(R.id.linearLayoutSummary);
        linearLayoutSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getContext(), "Viewing Summary Details", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getActivity(), SummaryActivity.class);
                startActivity(i);
            }
        });

        //hide complete checkbox
        auto_hide = (CheckBox) rootView.findViewById(R.id.checkboxHideComplete);
        auto_hide.setChecked(sharedPref.getBoolean("hide_complete",false));
        auto_hide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //tableDataAdapter.getFilter();

                //Save driver to preferences
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("hide_complete", isChecked);
                editor.commit();

                tableDataAdapter = new TableDataAdapter(getContext(), DataFactory.createDeliveryEventsList(getContext(), selectedItem,null, isChecked ), carTableView );
                carTableView.setDataAdapter(tableDataAdapter);
            }
        });

        //set last sync date from shared preferences
        TextView sync_date = (TextView) rootView.findViewById(R.id.textViewSyncDate);
        sync_date.setText(sharedPref.getString("last_sync_date","never"));

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

                //Toast.makeText(getContext(),"Selected Driver: " + selectedItem,Toast.LENGTH_SHORT ).show();

                //Save driver to preferences
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("selected_driver", selectedItem);
                editor.commit();

                //Set Dealer Stats
                Map<String,String> map =  DataFactory.GetDriverStats(getContext(),selectedItem);

                numDeleliveries.setText(commify(map.get("size")) + " ( $"+ commify(map.get("total_price")) +" )" );
                totalTips.setText("$" + commify(map.get("total_tip")));
                averageTips.setText("$" + commify(map.get("avg_tip")));


                tableDataAdapter = new TableDataAdapter(getContext(), DataFactory.createDeliveryEventsList(getContext(),selectedItem,null, auto_hide.isChecked()  ), carTableView);
                carTableView.setDataAdapter(tableDataAdapter);

                //tableDataAdapter.getData();
            } // to close the onItemSelected

            public void onNothingSelected(AdapterView<?> parent)
            {
                Toast.makeText(getContext(),"No Driver Filter",Toast.LENGTH_SHORT ).show();
                //selectedItem = null;
            }
        });

        //search
        SearchView searchInput = (SearchView) rootView.findViewById(R.id.editTextSearch);
        searchInput.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // do something when the focus of the query text field changes
                Toast.makeText(getContext(),"Search Selected",Toast.LENGTH_SHORT ).show();
            }
        });

        //search listener
        searchInput.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                // do something on text submit
                Toast.makeText(getContext(),"Search: "+query,Toast.LENGTH_SHORT ).show();
                //tableDataAdapter.
                tableDataAdapter = new TableDataAdapter(getContext(), DataFactory.createDeliveryEventsList(getContext(), selectedItem, query,auto_hide.isChecked()), carTableView);
                carTableView.setDataAdapter(tableDataAdapter);
                return false;
            }


            @Override
            public boolean onQueryTextChange(String newText) {
                // do something when text changes
                updateTable(newText);
                return false;
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
            tableDataAdapter = new TableDataAdapter(getContext(), DataFactory.createDeliveryEventsList(getContext(), selectedItem,null,auto_hide.isChecked()), carTableView);
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
                            sync(false);
                            refreshIndicator.hide();
                        }
                    }, 3000);
                }
            });
        }

        //check if auto sync check box is checked
        if( sharedPref.getBoolean("auto_sync", false) ) {
            //create_sync_timer();
        }

        // Inflate the layout for this fragment
        return rootView;
    }

    public void updateTable(String newText){

        if( newText.equals("") ){

            //Toast.makeText(getContext(),"Clear Search",Toast.LENGTH_SHORT ).show();
            tableDataAdapter = new TableDataAdapter(getContext(), DataFactory.createDeliveryEventsList(getContext(), selectedItem,null,auto_hide.isChecked()), carTableView);
            carTableView.setDataAdapter(tableDataAdapter);

        } else {
            //Toast.makeText(getContext(),"Search Change: "+newText,Toast.LENGTH_SHORT ).show();
            tableDataAdapter = new TableDataAdapter(getContext(), DataFactory.createDeliveryEventsList(getContext(), selectedItem,newText, auto_hide.isChecked()), carTableView);
            carTableView.setDataAdapter(tableDataAdapter);
        }
    }

    public String commify(String num ){

        if( num == null ){
            return "0";
        }

        if( num.contains(".") ){
            double number = Double.parseDouble(num);
            return numberFormat.format(number);
        } else {
            int number = Integer.parseInt(num);
            return numberFormat.format(number);
        }
    }

    public void create_sync_timer(){
        final Handler handler = new Handler();
        timer = new Timer();
        doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {

                            if( sharedPref.getBoolean("auto_sync", false) ) {

                                SimpleDateFormat s = new SimpleDateFormat("MM/dd hh:mm");
                                String format = s.format(new Date());
                                String last_update = sharedPref.getString("last_sync_date", null);

                                long second = 1000l;
                                long minute = 60l * second;
                                long hour = 60l * minute;

                                Date date1 = s.parse(last_update);
                                Date date2 = s.parse(format.toString());

                                long diff = date2.getTime() - date1.getTime();

                                String diff_str = String.format("%02d", diff / hour) + ":" + String.format("%02d", (diff % hour) / minute) + ":" + String.format("%02d", (diff % minute) / second);

                                //only sync when auto sync is enabled
                                if ((diff / hour) > 1 || (diff / minute) >= SYNC_INTERVAL) {
                                    Toast.makeText(getContext(), "Syncing with PWR", Toast.LENGTH_SHORT).show();
                                    sync(true);
                                } else {
                                    //Toast.makeText(getContext(), "Waiting to sync: " + diff_str, Toast.LENGTH_SHORT).show();
                                }
                            }

                        } catch (Exception e) {
                        }
                    }
                });
            }
        };
        int sleep_time = 50000;

        timer.schedule(doAsynchronousTask, 0, sleep_time);
    }

    public void sync(Boolean updateFragment){

        if( updateFragment ) {
            MainActivity.get().fm.beginTransaction().replace(R.id.content_frame_stats, new PwrSyncFragment()).addToBackStack("sync").commit();
            return;
        }
        store_id = sharedPref.getString("store_id", null );
        username = sharedPref.getString("username", null );
        password = sharedPref.getString("password", null );
        address = sharedPref.getString("address", null );
        
        String problem_log = "";
        if( store_id == null ){
            problem_log += "Missing Store Identification Number.\n";
        }

        if( username == null ){
            problem_log += "Missing Username \n";
        }

        if( password == null ){
            problem_log += "Missing password \n";
        }

        if( store_id == null || username == null || password == null ){
            Toast.makeText(getContext(), "Error: " + problem_log, Toast.LENGTH_SHORT).show();
            FragmentManager fm = getActivity().getFragmentManager();
            fm.beginTransaction().replace(R.id.content_frame, new Settings()).commit();
        } else {

            Toast.makeText(getContext(), "Syncing with PWR", Toast.LENGTH_SHORT).show();

            //pd = ProgressDialog.show(DeliveryEventsTable.th is,"Loading...", true, false);
            Intent i = new Intent(getActivity(), SyncPwrLogin.class);
            i.putExtra("store_id", store_id);
            i.putExtra("username", username);
            i.putExtra("password", password);
            startActivity(i);


            //DataFactory.getDeliveryDataFromServer(store_id,"user","password");
            tableDataAdapter = new TableDataAdapter(getContext(), DataFactory.createDeliveryEventsList(getContext(), selectedItem, null, auto_hide.isChecked()), carTableView);
            tableDataAdapter.notifyDataSetChanged();

            SharedPreferences.Editor editor = sharedPref.edit();
            SimpleDateFormat s = new SimpleDateFormat("MM/dd hh:mm");
            String format = s.format(new Date());
            editor.putString("last_sync_date", format.toString());
            editor.commit();
        }
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

            Log.d("Prepare", cursor.toString());
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
            //currently need a way to get state and city from settings
            String address = sharedPref.getString("address", null );
            String carString = clickedData.getAddress() + address;
            Toast.makeText(getContext(), carString, Toast.LENGTH_SHORT).show();

            Intent i = new Intent(getActivity(), DeliveryEventDetails.class);
            Bundle bundle = new Bundle();
            bundle.putString("ticket_id", clickedData.getTicketID().toString());
            bundle.putString("address", carString);
            i.putExtras(bundle);

            startActivity(i);
        }
    }

    public static boolean openMap(Context context, String address) {

        String URL = null;
        try {
            URL = "https://www.google.com/maps/dir/?api=1&travelmode=driving&dir_action=navigate&destination="+ URLEncoder.encode(" " +address, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Uri location = Uri.parse(URL);
        Intent intent = new Intent(Intent.ACTION_VIEW, location);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
            return true;
        }
        return false;
    }

    private class CarLongClickListener implements TableDataLongClickListener<DeliveryEvent> {

        @Override
        public boolean onDataLongClicked(final int rowIndex, final DeliveryEvent clickedData) {

            //address nav
            String address =  clickedData.getAddress() + "," + sharedPref.getString("address","");
            openMap(getContext(),address);
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

    @Override
    public void onPause() {
        super.onPause();
        //timer.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
        //create_sync_timer();
    }

    public void toggleLoginButton(){

    }
}
