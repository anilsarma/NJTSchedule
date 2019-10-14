package com.smartdeviceny.njts;

import android.app.ActivityManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.smartdeviceny.njts.utils.JobID;
import com.smartdeviceny.njts.utils.Utils;
import com.smartdeviceny.njts.values.Config;
import com.smartdeviceny.njts.values.ConfigDefault;
import com.smartdeviceny.njts.values.NotificationValues;

import java.util.Date;

public class DepartureVisionJobService extends JobService {

    public DepartureVisionJobService() {
        super();
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        //Log.d("JOB", "onStartJob - periodic job.");
//        List<ActivityManager.RunningServiceInfo> processes;
//        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//        processes = activityManager.getRunningServices(100);
//        Utils.notify_user(getApplicationContext(),  NotificationGroup.DEPARTURE_VISION, "departure vision, scheduling process " + processes.size(),
//                NotificationGroup.DEPARTURE_VISION.getID() + 1);
//
//        int i = NotificationGroup.DEPARTURE_VISION.getID() + 2;
//        for (ActivityManager.RunningServiceInfo info : processes) {
//            Utils.notify_user_big_text(getApplicationContext(), NotificationGroup.DEPARTURE_VISION,
//                    info.process + " " + info.pid + " " + info.service.getClassName(), i++);
//        }
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if(activeNetwork!=null) {
                boolean isConnected = activeNetwork.isConnectedOrConnecting();
                boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
                boolean isMobile = activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;

                Log.d("JOB", "DepartureVisionJobService - isConnected:" + isConnected + " wifi:" + isWiFi + " isMobile:" + isMobile + " ");
                if (isConnected) {
                    sendDepartureVisionPings();
                    // Utils.notify_user(this.getApplicationContext(), "NJTS", "NJTS", "Ping Sent " + new Date(), 1);
                }
            } else {
                sendFakeDepartureVisionPings(); // notify the systems that are awake;
            }
            sendTimerEvent();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            int polling_time = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(Config.POLLING_TIME, ConfigDefault.POLLING_TIME);
            polling_time = Math.max(10000, polling_time);
            long diff = Utils.alignTime(polling_time);

            Utils.scheduleJob(this.getApplicationContext(), JobID.DepartureVisionJobService, DepartureVisionJobService.class, (int) diff, false, null);

            jobFinished(jobParameters, true);
            //Log.d("JOB", "onStartJob - periodic job, complete " + time);
            return false; // let the system know we have no job running ..
        }
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d("JOB", "onStopJob - done");
        return false;
    }

    public void sendTimerEvent() {
        Intent intent = new Intent(NotificationValues.BROADCAT_PERIODIC_TIMER);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        //Log.d("JOB", "sending " + NotificationValues.BROADCAT_PERIODIC_TIMER);
    }
    public void sendDepartureVisionPings() {
        Intent intent = new Intent(NotificationValues.BROADCAT_SEND_DEPARTURE_VISION_PING);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        //Log.d("JOB", "sending " + NotificationValues.BROADCAT_SEND_DEPARTURE_VISION_PING);
    }

    public void sendFakeDepartureVisionPings() {
        Intent intent = new Intent(NotificationValues.BROADCAT_DEPARTURE_VISION_UPDATED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        //Log.d("JOB", "sending " + NotificationValues.BROADCAT_SEND_DEPARTURE_VISION_PING);
    }
}
