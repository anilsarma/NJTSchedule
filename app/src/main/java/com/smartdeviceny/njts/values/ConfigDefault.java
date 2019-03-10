package com.smartdeviceny.njts.values;

public interface ConfigDefault {

    String START_STATION = "New York Penn Station";
    String STOP_STATION = "New Brunswick";
    String ROUTE = "Northeast Corridor";
    String DELTA_DAYS = "1";
    String DV_STATION = "NY";
    int POLLING_TIME = 30000;

    boolean DEBUG=false;
    boolean TRAIN_NOTIFICTION = true;
    int NOTIFICATION_DELAY = 5;
    String DEPARTURE_VISION = "{'time':0, 'url':'', 'data':''}";
    String DEPARTURE_VISION_HISTORY = "{'time':0, 'history':{}}";


}
