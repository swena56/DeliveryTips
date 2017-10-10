package com.deliverytips.table.data;


/**
 * Data object representing a car producer.
 *
 * @author ISchwarz
 */
public class DeliveryEventsProducer {

    private final String name;
    private final int logoRes;

    public DeliveryEventsProducer(final int logoRes, final String name) {
        this.logoRes = logoRes;
        this.name = name;
    }

    public int getLogo() {
        return logoRes;
    }

    public String getName() {
        return name;
    }
}
