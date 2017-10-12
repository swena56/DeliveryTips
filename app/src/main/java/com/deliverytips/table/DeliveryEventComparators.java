package com.deliverytips.table;

import com.deliverytips.table.data.DeliveryEvent;

import java.util.Comparator;


/**
 * A collection of {@link Comparator}s for {@link DeliveryEvent} objects.
 *
 * @author ISchwarz
 */
public final class DeliveryEventComparators {

    private DeliveryEventComparators() {
        //no instance
    }


    public static Comparator<DeliveryEvent> getCarPowerComparator() {
        return new NameComparator();
    }

    public static Comparator<DeliveryEvent> getCarNameComparator() {
        return new NameComparator();
    }

    public static Comparator<DeliveryEvent> getCarPriceComparator() {
        return new PriceComparator();
    }

    public static Comparator<DeliveryEvent> getTicketComparator() {
        return new TicketComparator();
    }

    private static class TicketComparator implements Comparator<DeliveryEvent> {

        @Override
        public int compare(final DeliveryEvent deliveryEvent1, final DeliveryEvent deliveryEvent2) {
            if (deliveryEvent1.getTicketID() < deliveryEvent2.getTicketID()) return -1;
            if (deliveryEvent1.getTicketID() > deliveryEvent2.getTicketID()) return 1;
            return 0;
        }
    }

    private static class AddressComparator implements Comparator<DeliveryEvent> {

        @Override
        public int compare(final DeliveryEvent deliveryEvent1, final DeliveryEvent deliveryEvent2) {
            return deliveryEvent1.getAddress().compareTo(deliveryEvent2.getAddress());
        }
    }

    private static class NameComparator implements Comparator<DeliveryEvent> {

        @Override
        public int compare(final DeliveryEvent deliveryEvent1, final DeliveryEvent deliveryEvent2) {
            return deliveryEvent1.getName().compareTo(deliveryEvent2.getName());
        }
    }

    private static class PriceComparator implements Comparator<DeliveryEvent> {

        @Override
        public int compare(final DeliveryEvent deliveryEvent1, final DeliveryEvent deliveryEvent2) {
            if (deliveryEvent1.getPrice() < deliveryEvent2.getPrice()) return -1;
            if (deliveryEvent1.getPrice() > deliveryEvent2.getPrice()) return 1;
            return 0;
        }
    }

}
