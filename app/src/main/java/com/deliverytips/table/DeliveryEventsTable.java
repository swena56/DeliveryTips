package com.deliverytips.table;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.deliverytips.R;
import com.deliverytips.table.data.DataFactory;
import com.deliverytips.table.data.DeliveryEvent;

import de.codecrafters.tableview.listeners.SwipeToRefreshListener;
import de.codecrafters.tableview.listeners.TableDataClickListener;
import de.codecrafters.tableview.listeners.TableDataLongClickListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeliveryEventsTable extends Fragment {

    TableDataAdapter tableDataAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_delivery_events_table, container, false);

        final SortableDeliveryEventsTableView carTableView = (SortableDeliveryEventsTableView) rootView.findViewById(R.id.tableView);
        if (carTableView != null) {
            tableDataAdapter = new TableDataAdapter(getContext(), DataFactory.createDeliveryEventsList(getContext()), carTableView);
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
                            //final DeliveryEvent randomDeliveryEvent = getRandomCar();
                            //tableDataAdapter.getData();
                            tableDataAdapter = new TableDataAdapter(getContext(), DataFactory.createDeliveryEventsList(getContext()), carTableView);
                            tableDataAdapter.notifyDataSetChanged();
                            refreshIndicator.hide();
                            Toast.makeText(getContext(), "Syncing with PWR... unimplemented.", Toast.LENGTH_SHORT).show();
                        }
                    }, 3000);
                }
            });
        }

        // Inflate the layout for this fragment
        return rootView;
    }

    private class CarClickListener implements TableDataClickListener<DeliveryEvent> {

        @Override
        public void onDataClicked(final int rowIndex, final DeliveryEvent clickedData) {
            final String carString = "Click: " + clickedData.getAddress() + " " + clickedData.getName();
            Toast.makeText(getContext(), carString, Toast.LENGTH_SHORT).show();
        }
    }

    private class CarLongClickListener implements TableDataLongClickListener<DeliveryEvent> {

        @Override
        public boolean onDataLongClicked(final int rowIndex, final DeliveryEvent clickedData) {
            final String carString = "Long Click: " + clickedData.getProducer().getName() + " " + clickedData.getName();
            Toast.makeText(getContext(), carString, Toast.LENGTH_SHORT).show();
            return true;
        }
    }

}
