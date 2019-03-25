package com.smartdeviceny.njts.utils;

import android.app.NotificationManager;

public enum NotificationGroup {
    UPDATE {
        public NotificationChannels getChannel() { return NotificationChannels.NJTS;}
        public int  getID() {
            return 1000;
        }
        public String getGroupID() {
            return "NJTS";
        }
        public String getName() {
            return "NJTS";
        }
        public String getDescription() {
            return "Updates";
        }

    },
    UPCOMING {
        public NotificationChannels getChannel() { return NotificationChannels.NJTS;}
        public int  getID() {
            return 2000;
        }
        public String getGroupID() {
            return "NJTS";
        }
        public String getName() {
            return "Upcoming Trains";
        }
        public String getDescription() {
            return "Upcoming Trains";
        }
    },
    DEPARTURE_VISION {
        public NotificationChannels getChannel() { return NotificationChannels.DEBUG;}
        public int  getID() {
            return 3000;
        }
        public String getGroupID() {
            return "NJTS";
        }
        public String getName() {
            return "NJTS";
        }
        public String getDescription() {
            return "Departure Vision - Debug";
        }
    },
    POWER_SERVICE {
        public NotificationChannels getChannel() { return NotificationChannels.DEBUG;}
        public int  getID() {
            return 4000;
        }
        public String getGroupID() {
            return "NJTS";
        }
        public String getName() {
            return "Debug Service";
        }
        public String getDescription() {
            return "Power Service - Debug";
        }
    },
    DATABASE_UPGRADE {
        public NotificationChannels getChannel() { return NotificationChannels.NJTS;}
        public int  getID() {
            return 5000;
        }
        public String getGroupID() {
            return "NJTS";
        }
        public String getName() {
            return "Schedule Database";
        }
        public String getDescription() {
            return "Schedule Database";
        }
    },
    UPDATE_CHECK_SERVICE {
        public NotificationChannels getChannel() { return NotificationChannels.DEBUG;}
        public int  getID() {
            return 6000;
        }
        public String getGroupID() {
            return "NJTS";
        }
        public String getName() {
            return "Update Check";
        }
        public String getDescription() {
            return  "Update Check - Debug";
        }
    };
    abstract public NotificationChannels getChannel();
    abstract public int  getID();
    public String getUniqueID() { return getChannel().getUniqueID() + "." + getGroupID() + "." + getID();}
    abstract public String getGroupID();
    abstract public String getName();
    abstract public String getDescription();

}
