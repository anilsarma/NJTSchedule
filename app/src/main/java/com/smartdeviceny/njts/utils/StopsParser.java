package com.smartdeviceny.njts.utils;

import java.util.ArrayList;
import java.util.HashMap;

public class StopsParser {

    public static  ArrayList<StopDetails>  parse(ArrayList<HashMap<String, Object>> sqlresult) {
        ArrayList<StopDetails> details = new ArrayList<>();
        for(HashMap<String, Object> row: sqlresult) {
            try {
                details.add(new StopDetails(row.get("stop_name").toString(), row.get("arrival_time").toString(), row.get("departure_time").toString()));
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
        return details;
    }
}
