package com.smartdeviceny.njts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.smartdeviceny.njts.utils.JobID;
import com.smartdeviceny.njts.utils.NotificationGroup;
import com.smartdeviceny.njts.utils.Utils;
import com.smartdeviceny.njts.values.Config;
import com.smartdeviceny.njts.values.ConfigDefault;

public class PowerStartServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        boolean debug = config.getBoolean(Config.DEBUG, ConfigDefault.DEBUG);
        if(debug && Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Utils.notify_user(context, NotificationGroup.POWER_SERVICE, "Power Up detected", NotificationGroup.POWER_SERVICE.getID() +2);
        }
        scheduleJob(context.getApplicationContext(), config);
    }


    public static void scheduleJob(Context context, @Nullable SharedPreferences config) {
        if(config ==null) {
            config =  PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        }
        boolean debug = config.getBoolean(Config.DEBUG, ConfigDefault.DEBUG);
        Log.d("PWR", "Scheduling test job.");
        // schedule a job for upgrade check
        long ms_frequency = 1 * 60 * 1000;
        long diff = Utils.alignTime(ms_frequency);
        boolean result = Utils.scheduleJob(context.getApplicationContext(), JobID.UpdateCheckerJobService, UpdateCheckerJobService.class, (int) diff, false, null);
        if(debug) {
            Utils.notify_user(context, NotificationGroup.POWER_SERVICE, "scheduling UpdateChecker " + diff, NotificationGroup.POWER_SERVICE.getID() +1);
        }

        if(!result) {
            Log.e("PWR", "error: Some error while scheduling the job");
            if(debug) {
                Utils.notify_user(context, NotificationGroup.POWER_SERVICE, "scheduling UpdateChecker failed", NotificationGroup.POWER_SERVICE.getID() + 1);
            }
        }
        else {
            Log.d("PWR", "job scheduled " + ms_frequency);
            //Utils.notify_user(context, NotificationGroup.POWER_SERVICE, "system up, scheduling process succedeed", NotificationGroup.POWER_SERVICE.getID() +2);

        }
    }



}