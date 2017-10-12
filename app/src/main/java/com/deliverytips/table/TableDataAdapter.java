package com.deliverytips.table;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.deliverytips.R;
import com.deliverytips.table.data.DeliveryEvent;

import java.text.NumberFormat;
import java.util.List;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.toolkit.LongPressAwareTableDataAdapter;

public class TableDataAdapter extends LongPressAwareTableDataAdapter<DeliveryEvent> {

    private static final int TEXT_SIZE = 13;
    private static final int SMALL_TEXT_SIZE = 8;
    private static final NumberFormat PRICE_FORMATTER = NumberFormat.getNumberInstance();


    public TableDataAdapter(final Context context, final List<DeliveryEvent> data, final TableView<DeliveryEvent> tableView) {
        super(context, data, tableView);
    }

    @Override
    public View getDefaultCellView(int rowIndex, int columnIndex, ViewGroup parentView) {
        final DeliveryEvent deliveryEvent = getRowData(rowIndex);
        View renderedView = null;

        switch (columnIndex) {
            case 0:
                renderedView = renderTicketId(deliveryEvent);
                break;
            case 1:
                renderedView = renderCatName(deliveryEvent);
                break;
            case 2:
                renderedView = renderPower(deliveryEvent);
                break;
            case 3:
                renderedView = renderPrice(deliveryEvent);
                break;
            case 4:
                renderedView = renderPrice(deliveryEvent);
                break;
        }

        return renderedView;
    }



    @Override
    public View getLongPressCellView(int rowIndex, int columnIndex, ViewGroup parentView) {
        final DeliveryEvent deliveryEvent = getRowData(rowIndex);
        View renderedView = null;

        switch (columnIndex) {
            case 0:
                renderedView = renderTicketId(deliveryEvent);
                break;
//            case 1:
//                renderedView = renderEditableCatName(deliveryEvent);
//                break;
            case 2:
                renderedView = renderAddress(deliveryEvent);
                break;
            default:
                renderedView = getDefaultCellView(rowIndex, columnIndex, parentView);
        }

        return renderedView;
    }

    private View renderMapsIntent(final DeliveryEvent deliveryEvent) {
        final EditText editText = new EditText(getContext());
        editText.setText(deliveryEvent.getName());
        editText.setPadding(20, 10, 20, 10);
        editText.setTextSize(TEXT_SIZE);
        editText.setSingleLine();
        editText.addTextChangedListener(new CarNameUpdater(deliveryEvent));
        return editText;
    }

    private View renderEditableCatName(final DeliveryEvent deliveryEvent) {
        final EditText editText = new EditText(getContext());
        editText.setText(deliveryEvent.getName());
        editText.setPadding(20, 10, 20, 10);
        editText.setTextSize(TEXT_SIZE);
        editText.setSingleLine();
        editText.addTextChangedListener(new CarNameUpdater(deliveryEvent));
        return editText;
    }

    private View renderAddress(final DeliveryEvent deliveryEvent) {

        TextView textView = new TextView(getContext());
        textView.setText(deliveryEvent.getAddress());
        textView.setPadding(20, 10, 20, 10);
        textView.setTextSize(TEXT_SIZE);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        return textView;
    }

    private View renderTicketId(final DeliveryEvent deliveryEvent) {
        return renderString(
                Long.toString( deliveryEvent.getTicketID() )
        );
    }

    private View renderPrice(final DeliveryEvent deliveryEvent) {
        final String priceString = "$" + PRICE_FORMATTER.format(deliveryEvent.getPrice());

        TextView textView = new TextView(getContext());
        textView.setText(priceString);
        textView.setPadding(20, 10, 20, 10);
        textView.setTextSize(TEXT_SIZE);

        if (deliveryEvent.getPrice() < 10) {
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.table_price_high));
        } else if (deliveryEvent.getPrice()<=10) {
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.table_price_low));
        }

        return textView;
    }

    private View renderPower(final DeliveryEvent deliveryEvent) {
        TextView textView = new TextView(getContext());
        textView.setText(deliveryEvent.getAddress());
        textView.setPadding(20, 10, 20, 10);
        textView.setTextSize(TEXT_SIZE);
        return textView;
    }

    //phone number
    private View renderCatName(final DeliveryEvent deliveryEvent) {
        //TODO renmae variables

        String phone_str = deliveryEvent.getName().replaceFirst("(\\d{3})(\\d{3})(\\d+)", "($1)-$2-$3");

        final TextView textView = new TextView(getContext());
        textView.setText(phone_str);
        textView.setPadding(20, 10, 20, 10);
        textView.setTextSize(TEXT_SIZE);

        //show if the phone number is invalid
        if (deliveryEvent.getName().length() > 10 || deliveryEvent.getName().length() < 10 ) {
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.table_price_high));
        } else {
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.table_price_low));
        }

        return textView;
    }

    private View renderProducerLogo(final DeliveryEvent deliveryEvent, final ViewGroup parentView) {
        final View view = getLayoutInflater().inflate(R.layout.table_cell_image, parentView, false);
        final ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        imageView.setImageResource(deliveryEvent.getProducer().getLogo());
        return view;
    }

    private View renderString(final String value) {
        final TextView textView = new TextView(getContext());
        textView.setText(value);
        textView.setPadding(20, 10, 20, 10);
        textView.setTextSize(TEXT_SIZE);
        return textView;
    }

    private static class CarNameUpdater implements TextWatcher {

        private DeliveryEvent deliveryEventToUpdate;

        public CarNameUpdater(DeliveryEvent deliveryEventToUpdate) {
            this.deliveryEventToUpdate = deliveryEventToUpdate;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // no used
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // not used
        }

        @Override
        public void afterTextChanged(Editable s) {
            deliveryEventToUpdate.setName(s.toString());
        }
    }

}
