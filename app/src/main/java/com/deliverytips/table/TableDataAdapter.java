package com.deliverytips.table;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.deliverytips.R;
import com.deliverytips.table.data.DeliveryEvent;

import java.text.NumberFormat;
import java.util.List;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.toolkit.LongPressAwareTableDataAdapter;

public class TableDataAdapter extends LongPressAwareTableDataAdapter<DeliveryEvent> {

    private static final int TEXT_SIZE = 15;
    private static final int SMALL_TEXT_SIZE = 13;
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
                //renderedView = renderPhoneNumber(deliveryEvent);
                renderedView = renderTimeAndStatus(deliveryEvent);
                break;
            case 2:
                renderedView = renderAddress(deliveryEvent);
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
        editText.setText(deliveryEvent.getPhoneNumber());
        editText.setPadding(20, 10, 20, 10);
        editText.setTextSize(TEXT_SIZE);
        editText.setSingleLine();
        editText.addTextChangedListener(new CarNameUpdater(deliveryEvent));
        return editText;
    }

    private View renderEditableCatName(final DeliveryEvent deliveryEvent) {
        EditText editText = new EditText(getContext());
        editText.setText(deliveryEvent.getTicketID().toString());
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
        textView.setTextSize(TEXT_SIZE - 1);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        return textView;
    }

    private View renderTicketId(final DeliveryEvent deliveryEvent) {

        String ticket_id = Long.toString( deliveryEvent.getTicketID() );
        String pre = "A";
        String num = "B";
        if( ticket_id.length() > 3 ){
            pre = ticket_id.substring(0,2);
            num = ticket_id.substring(3,ticket_id.length());
        }

        TextView textViewPrice = new TextView(getContext());
        textViewPrice.setText(pre);
        textViewPrice.setPadding(20, 10, 20, 10);
        textViewPrice.setPaintFlags(textViewPrice.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
        textViewPrice.setTextSize(SMALL_TEXT_SIZE);

        TextView textView = new TextView(getContext());
        textView.setText(num);
        textView.setPadding(20, 10, 20, 10);
        textView.setTypeface(null, Typeface.BOLD_ITALIC);
        textView.setPaintFlags(textView.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
        textView.setTextSize(TEXT_SIZE+2);

        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.addView(textViewPrice);
        linearLayout.addView(textView);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        return linearLayout;
    }

    private View renderPrice(final DeliveryEvent deliveryEvent) {
        final String priceString = "$" + PRICE_FORMATTER.format(deliveryEvent.getPrice()) + " / " +
                "$" + PRICE_FORMATTER.format(deliveryEvent.getTip());

        NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        String price = numberFormat.format(deliveryEvent.getPrice());
        String tip = numberFormat.format(deliveryEvent.getTip());

        //20 percent of higher is green
        Double percent = 0.0;

        if( deliveryEvent.getPrice() > 0 ){
            percent = deliveryEvent.getTip() / deliveryEvent.getPrice();
        }

        //percent = Double.parseDouble(numberFormat.format(percent));

        //set price
        TextView textViewPrice = new TextView(getContext());
        textViewPrice.setText(price);
        textViewPrice.setPadding(20, 10, 20, 10);
        textViewPrice.setPaintFlags(textViewPrice.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
        textViewPrice.setTextSize(SMALL_TEXT_SIZE);

        //tip
        TextView textViewTip = new TextView(getContext());
        textViewTip.setText(tip + " ("+percentFormat.format(percent)+")" );
        textViewTip.setPadding(20, 10, 20, 10);
        textViewTip.setTextSize(SMALL_TEXT_SIZE);

//        TextView textViewPercent = new TextView(getContext());
//        textViewPercent.setText();
//        textViewPercent.setPadding(20, 10, 20, 10);
//        textViewPercent.setTextSize(SMALL_TEXT_SIZE);

        LinearLayout linearLayout = new LinearLayout(getContext());
        //linearLayout
        linearLayout.addView(textViewPrice);
        linearLayout.addView(textViewTip);
       // linearLayout.addView(textViewPercent);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        if( ( percent * 100 )>= 20 ){
            textViewTip.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
        } else if( (percent * 100 ) > 10  ) {
            textViewTip.setTextColor(ContextCompat.getColor(getContext(), R.color.orange));
        } else if( (percent * 100 ) < 10 && (percent * 100 ) > 0   ) {
            textViewTip.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
        } else {
            textViewTip.setTextColor(ContextCompat.getColor(getContext(), R.color.black_overlay));
        }

        return linearLayout;
    }

    //time and status
    private View renderTimeAndStatus(final DeliveryEvent deliveryEvent) {

        TextView textView = new TextView(getContext());
        textView.setText(deliveryEvent.getTime());
        textView.setPadding(20, 10, 20, 10);
        textView.setPaintFlags(textView.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
        textView.setTextSize(SMALL_TEXT_SIZE);

        TextView textView2 = new TextView(getContext());
        textView2.setText(deliveryEvent._status);
        textView2.setPadding(20, 10, 20, 10);
        textView2.setPaintFlags(textView.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
        textView2.setTextSize(SMALL_TEXT_SIZE);

        if( deliveryEvent._status.equals("Complete")){
            textView2.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
        } else if( deliveryEvent._status.equals("Oven")) {
            textView2.setTextColor(ContextCompat.getColor(getContext(), R.color.orange));
        } else if( deliveryEvent._status.equals("Abandoned") || deliveryEvent._status.equals("Void")) {
            textView2.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
        } else if( deliveryEvent._status.equals("Future") || deliveryEvent._status.equals("Makeline")) {
            textView2.setTextColor(ContextCompat.getColor(getContext(), R.color.blue));
        }

        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(textView);
        linearLayout.addView(textView2);

        return linearLayout;
    }

    //phone number
    private View renderPhoneNumber(final DeliveryEvent deliveryEvent) {

        String phone_str = "";

        if( deliveryEvent.getPhoneNumber() != null && deliveryEvent.getPhoneNumber().length() > 6){
            phone_str = deliveryEvent.getPhoneNumber().replaceFirst("(\\d{3})(\\d{3})(\\d+)", "($1)-$2-$3");
        }

        final TextView textView = new TextView(getContext());
        textView.setText(phone_str);
        textView.setPadding(20, 10, 20, 10);
        textView.setTextSize(SMALL_TEXT_SIZE);

        //show if the phone number is invalid
        if ( deliveryEvent.getPhoneNumber() != null && ( deliveryEvent.getPhoneNumber().length() > 10 || deliveryEvent.getPhoneNumber().length() < 10 ) ) {
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.table_price_high));
        } else {
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.table_price_low));
        }

        return textView;
    }

    private View renderStatus(final DeliveryEvent deliveryEvent, final ViewGroup parentView) {
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
            deliveryEventToUpdate.setPhoneNumber(s.toString());
        }
    }

}
