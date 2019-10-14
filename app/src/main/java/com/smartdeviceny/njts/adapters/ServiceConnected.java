package com.smartdeviceny.njts.adapters;

import com.smartdeviceny.njts.SystemService;

public interface ServiceConnected {
    void onSystemServiceConnected(SystemService systemService);
    void onDepartureVisionUpdated(SystemService systemService);
    void onAlertsUpdated(SystemService systemService);
    void onTimerEvent(SystemService systemService);
    void configChanged(SystemService systemService);
}
