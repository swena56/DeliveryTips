package com.deliverytips.table;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.deliverytips.R;
import com.deliverytips.table.data.DeliveryEvent;

import de.codecrafters.tableview.SortableTableView;
import de.codecrafters.tableview.model.TableColumnWeightModel;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import de.codecrafters.tableview.toolkit.SortStateViewProviders;
import de.codecrafters.tableview.toolkit.TableDataRowBackgroundProviders;


/**
 * An extension of the {@link SortableTableView} that handles {@link DeliveryEvent}s.
 *
 * @author ISchwarz
 */
public class SortableDeliveryEventsTableView extends SortableTableView<DeliveryEvent> {

    public SortableDeliveryEventsTableView(final Context context) {
        this(context, null);
    }

    public SortableDeliveryEventsTableView(final Context context, final AttributeSet attributes) {
        this(context, attributes, android.R.attr.listViewStyle);
    }

    public SortableDeliveryEventsTableView(final Context context, final AttributeSet attributes, final int styleAttributes) {
        super(context, attributes, styleAttributes);

        final SimpleTableHeaderAdapter simpleTableHeaderAdapter = new SimpleTableHeaderAdapter(
                context, R.string.ticket_id_column, R.string.status_column, R.string.address_column, R.string.price);
        simpleTableHeaderAdapter.setTextColor(ContextCompat.getColor(context, R.color.table_header_text));
        setHeaderAdapter(simpleTableHeaderAdapter);

        final int rowColorEven = ContextCompat.getColor(context, R.color.table_data_row_even);
        final int rowColorOdd = ContextCompat.getColor(context, R.color.table_data_row_odd);
        setDataRowBackgroundProvider(TableDataRowBackgroundProviders.alternatingRowColors(rowColorEven, rowColorOdd));
        setHeaderSortStateViewProvider(SortStateViewProviders.brightArrows());

        final TableColumnWeightModel tableColumnWeightModel = new TableColumnWeightModel(4);
        tableColumnWeightModel.setColumnWeight(0, 3);
        tableColumnWeightModel.setColumnWeight(1, 3);
        tableColumnWeightModel.setColumnWeight(2, 5);
        tableColumnWeightModel.setColumnWeight(3, 3);
        setColumnModel(tableColumnWeightModel);

        setColumnComparator(0, DeliveryEventComparators.getTicketComparator());

        //set header color
        //https://material.io/guidelines/style/color.html#color-color-palette
        setHeaderBackgroundColor(Color.parseColor("#1E88E5"));

        setColumnComparator(1, DeliveryEventComparators.getCarNameComparator());
        setColumnComparator(2, DeliveryEventComparators.getCarPowerComparator());
        setColumnComparator(3, DeliveryEventComparators.getCarPriceComparator());
    }

}
