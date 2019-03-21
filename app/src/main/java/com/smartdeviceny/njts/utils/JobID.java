package com.smartdeviceny.njts.utils;

import com.smartdeviceny.njts.NJTAlertJobService;

public enum JobID {
    DepartureVisionJobService {
        public int getID() { return 1000; }
    },
    UpdateCheckerJobService {
        public int getID() { return 1001; }
    },
    NJTAlertJobService {
        @Override
        public int getID() {
            return 1002;
        }
    }
    ;

    abstract public int getID();
}
