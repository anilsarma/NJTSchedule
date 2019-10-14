package com.smartdeviceny.njts.utils;

import com.smartdeviceny.njts.annotations.Persist;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StopDetails {
    final public static DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
    final public static DateFormat printFormat = new SimpleDateFormat("hh:mm a");

    public String from;
    public String to;
    public String block_id;
    public String route_name;
    public String arrival_time;
    //public String de;


    @Persist
    public String stop_name;
    @Persist
    public String departure_time;
    Date   departure_time_as_date;
    Date   arrival_time_as_date;
    public StopDetails(String stop, String departure_time, String arrival_time) {
        stop_name = stop;
        this.departure_time = departure_time;
        this.arrival_time = arrival_time;
        try {
            this.departure_time_as_date = dateFormat.parse(departure_time);
            this.arrival_time_as_date = dateFormat.parse(arrival_time);
        } catch(Exception e) {
            e.printStackTrace();
            this.departure_time_as_date = new Date();
            this.arrival_time_as_date = new Date();
        }
    }

    public String getStop_name() {
        return stop_name;
    }

    public String getDeparture_time() {
        return departure_time;
    }

    public Date getTime() {
        return departure_time_as_date;
    }
    public String getPrintableDepartureTime() {
        return printFormat.format(departure_time_as_date);
    }
    public String getPrintableArrivalTime() {
        return printFormat.format(arrival_time_as_date);
    }
}
