package com.smartdeviceny.njts;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.smartdeviceny.njts.utils.JobID;
import com.smartdeviceny.njts.utils.NotificationChannels;
import com.smartdeviceny.njts.utils.NotificationGroup;
import com.smartdeviceny.njts.utils.Utils;

import java.util.Date;

public class PowerStartServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        schdeulJob(context.getApplicationContext());
    }


    public static void schdeulJob(Context context) {
        Log.d("PWR", "Scheduling test job.");
        // schedule a job for upgrade check
        long ms_frequency = 1 * 60 * 1000;
        long diff = Utils.alignTime(ms_frequency);
        boolean result = Utils.scheduleJob(context.getApplicationContext(), JobID.UpdateCheckerJobService, UpdateCheckerJobService.class, (int) diff, false, null);
        //Utils.notify_user(context, NotificationGroup.POWER_SERVICE, "system up, scheduling process " + diff, NotificationGroup.POWER_SERVICE.getID() +1);

        if(!result) {
            Log.e("PWR", "error: Some error while scheduling the job");
            //Utils.notify_user(context, NotificationGroup.POWER_SERVICE, "system up, scheduling process failed", NotificationGroup.POWER_SERVICE.getID() +1);
        }
        else {
            Log.d("PWR", "job scheduled " + ms_frequency);
            //Utils.notify_user(context, NotificationGroup.POWER_SERVICE, "system up, scheduling process succedeed", NotificationGroup.POWER_SERVICE.getID() +2);

        }
    }



}