package com.smartdeviceny.njts.parser;

import java.util.Date;
import java.util.HashMap;

public class DepartureVisionData {
    public String tableTime;  // in 8:23 AM
    public String header = "";
    public String time = "";
    public String to = "";
    public String track = "";
    public String line = "";
    public String status = "";
    public String block_id = "";
    public String station_code = "";
    public Date createTime = new Date(); // time this object was created
    public boolean stale = false;
    public boolean favorite = false;

    public String background;
    public String foreground;
    public int     index;

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
        obj.favorite = this.favorite;
        obj.createTime = this.createTime;
        obj.header = this.header;

        return obj;
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("time=" + time);
        str.append(" to=" + to);
        str.append(" track=" + track);
        str.append(" line=" + line);
        str.append(" status=" + status);
        str.append(" block_id=" + block_id);
        str.append(" station=" + station_code);
        str.append(" favorite=" + favorite);
        str.append(" createTime=" + createTime);
        str.append(" header=" + header);
        return str.toString();
    }


}
