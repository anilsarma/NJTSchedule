package com.smartdeviceny.njts.parser;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Route {
    final DateFormat dateTim24HrFmt = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
    //final DateFormatCont time24HFmt = new SimpleDateFormat("HH:mm:ss");
    final DateFormat dateFmt = new SimpleDateFormat("yyyyMMdd");
    public String station_code;
    public String departure_time;
    public String arrival_time;
    public String block_id;
    public String route_name;
    public String trip_id;

    public String date;
    public String header;
    public String from;
    public String to;
    public boolean favorite = false;

    public Date date_as_date;
    public Date departure_time_as_date;
    public Date arrival_time_as_date;

    public Route(String station_code, String date, String from, String to, HashMap<String, Object> data, boolean isfavorite) {
        this.station_code = station_code;
        departure_time = data.get("departure_time").toString();
        arrival_time = data.get("destination_time").toString();
        block_id = data.get("block_id").toString();
        route_name = data.get("route_long_name").toString();
        trip_id = data.get("trip_id").toString();
        this.from = from;
        this.to = to;


        this.favorite = isfavorite;
        try {
            // remember hours are more than 24 hrs here to represent the next day.
            this.departure_time_as_date = dateTim24HrFmt.parse(date + " " + departure_time);
            this.arrival_time_as_date = dateTim24HrFmt.parse(date + " " + arrival_time);

            this.date = dateFmt.format(departure_time_as_date);
            this.date_as_date = dateFmt.parse(this.date);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.date = "" + this.date;
        this.header = this.date + " " + from + " => " + to;
    }


    public Date getDate(String time) throws ParseException {
        DateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        Date tm = dateTimeFormat.parse(date + " " + time);
        return tm;
    }


    @Override
    public String toString() {
        return "Route@" + block_id + " Time:" + departure_time;
    }
}