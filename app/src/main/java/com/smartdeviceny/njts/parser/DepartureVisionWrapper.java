package com.smartdeviceny.njts.parser;

import com.smartdeviceny.njts.annotations.Persist;

import java.util.ArrayList;
import java.util.Date;

public class DepartureVisionWrapper {
    @Persist
    public Date time;
    @Persist
    public String url;
    @Persist
    public String code;
    @Persist
    public ArrayList<DepartureVisionData> entries = new ArrayList<>();



}
