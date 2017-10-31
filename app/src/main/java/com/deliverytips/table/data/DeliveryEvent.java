package com.deliverytips.table.data;


/**
 * Data object representing a car.
 *
 * @author ISchwarz
 */
public class DeliveryEvent implements Chargable {

    private long ticket_id;
    //private final DeliveryEventsProducer producer;  // for adding a image in the column
    private String Address;
    private double price;
    private double tip;
    private String name;

    public DeliveryEvent(final long ticket_id, final String name, final String Address, final double price, final double tip) {
        this.ticket_id = ticket_id;
        this.name = name;
        this.Address = Address;
        this.price = price;
        this.tip = tip;
        //this.producer = null;
    }

    public DeliveryEventsProducer getProducer() {
        return   null;//      new DeliveryEventsProducer(R.mipmap, "round");
    }


    public Long getTicketID() {
        return ticket_id;
    }

    public String getName() {

        if( name == null ){
            return "";
        }
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getAddress(){

        if( this.Address == null ){
            return "";
        } else {
            return this.Address;
        }
    }

    public void setAddress(final String address) {
        this.Address = address;
    }

    public double getPrice() {
        return price;
    }
    public double getTip() {
        return tip;
    }

    @Override
    public String toString() {
        return name;
    }
}
