package com.smartdeviceny.njts.parser;

import com.smartdeviceny.njts.annotations.JSONObjectSerializer;
import com.smartdeviceny.njts.annotations.Persist;

import java.util.Date;
import java.util.HashMap;

public class DepartureVisionData {
    @Persist
    public String tableTime;  // in 8:23 AM
    @Persist
    public String header = "";
    @Persist
    public String time = "";
    @Persist
    public String to = "";
    @Persist
    public String track = "";
    @Persist
    public String line = "";
    @Persist
    public String status = "";
    @Persist
    public String block_id = "";
    @Persist
    public String station_code = "";
    @Persist
    public Date createTime = new Date(); // time this object was created
    @Persist
    public boolean stale = false;
    @Persist
    public boolean favorite = false;
    @Persist
    public String background;
    @Persist
    public String foreground;
    @Persist
    public int     index;

    @Persist
    public String station_long_name;

    public DepartureVisionData() {
    }

    public DepartureVisionData(HashMap<String, Object> data) {
        time = data.get("time").toString();
        to = data.get("to").toString();
        track = data.get("track").toString();
        line = data.get("line").toString();
        status = data.get("status").toString();
        block_id = data.get("train").toString();
        station_code = data.get("station").toString();
        background = data.get("background").toString();
        foreground = data.get("foreground").toString();
        station_long_name = data.get("station_long_name").toString();

        index  = Integer.parseInt(data.get("index").toString());
        favorite = false;
        header = " " + createTime + " " + to;
        createTime = new Date();
    }


    public DepartureVisionData clone() {
        DepartureVisionData obj = new DepartureVisionData();
        // TODO not sure how to clone strings ..
        obj.time = "" + this.time;
        obj.to = "" + this.to;
        obj.track = "" + this.track;
        obj.line = "" + this.line;
        obj.status = "" + this.status;
        obj.block_id = "" + this.block_id;
        obj.station_code = "" + this.station_code;
        obj.station_long_name =  this.station_long_name;
        obj.favorite = this.favorite;
        obj.createTime = this.createTime;
        obj.header = this.header;

        return obj;
    }

    public String toString() {
       return JSONObjectSerializer.stringify(this);
    }


}
