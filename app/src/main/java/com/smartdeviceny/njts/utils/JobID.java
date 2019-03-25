package com.smartdeviceny.njts.utils;

public enum JobID {
    DepartureVisionJobService {
        public int getID() { return 1000; }
    },
    UpdateCheckerJobService {
        public int getID() { return 1001; }
    };
    abstract public int getID();
}
