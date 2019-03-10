package com.smartdeviceny.njts.utils;

import android.app.NotificationManager;

public enum NotificationChannels {
    NJTS {
        public String getUniqueID() {
            return "NJTS";
        }

        public String getName() {
            return "NJTSChannel";
        }

        public String getDescription() {
            return "Notification channel for the NJ Transit Schedule application.";
        }

        public int getImportance() {
            return NotificationManager.IMPORTANCE_LOW;
        }
    },
    DEBUG {
        public String getUniqueID() {
            return "DEBUG";
        }

        public String getName() {
            return "NJTS Debug Channel";
        }

        public String getDescription() {
            return "Debug Notification channel for the NJ Transit Schedule application.";
        }

        public int getImportance() {
            return NotificationManager.IMPORTANCE_LOW;
        }
    },
    TRAINS {
        public String getUniqueID() {
            return "TRAINS";
        }

        public String getName() {
            return "NJTS Train Channel";
        }

        public String getDescription() {
            return "Notification channel for the NJ Transit Upcoming trains.";
        }

        public int getImportance() {
            return NotificationManager.IMPORTANCE_LOW;
        }
    };
    abstract public String getUniqueID();

    abstract public String getName();

    abstract public String getDescription();

    abstract public int getImportance();
}
