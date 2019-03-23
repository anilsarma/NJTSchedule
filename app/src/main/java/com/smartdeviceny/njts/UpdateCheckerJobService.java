package com.smartdeviceny.njts;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.smartdeviceny.njts.annotations.JSONObjectSerializer;
import com.smartdeviceny.njts.parser.DepartureVisionData;
import com.smartdeviceny.njts.parser.DepartureVisionParser;
import com.smartdeviceny.njts.parser.DepartureVisionWrapper;
import com.smartdeviceny.njts.parser.Route;
import com.smartdeviceny.njts.utils.IDGenerator;
import com.smartdeviceny.njts.utils.JobID;
import com.smartdeviceny.njts.utils.NotificationGroup;
import com.smartdeviceny.njts.utils.SQLWrapper;
import com.smartdeviceny.njts.utils.Utils;
import com.smartdeviceny.njts.values.Config;
import com.smartdeviceny.njts.values.ConfigDefault;
import com.smartdeviceny.njts.values.NotificationValues;

import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UpdateCheckerJobService extends JobService {

    public UpdateCheckerJobService() {
        super();
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean debug = config.getBoolean(Config.DEBUG, ConfigDefault.DEBUG);
        if (debug) {
            Utils.notify_user(getApplicationContext(), NotificationGroup.UPDATE_CHECK_SERVICE, null, "Job Checker, in Start", NotificationGroup.UPDATE_CHECK_SERVICE.getID() + 1);
        }
        long nextTime = -1;
        try {
            PersistableBundle bundle = jobParameters.getExtras();
            if (bundle == null || bundle.getBoolean("periodic", false)) {
                oneTimeCheck();
            }
            nextTime = periodicCheck(jobParameters);
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            nextTime = new Date().getTime() + TimeUnit.SECONDS.toMillis(30);
            if (debug) {
                Utils.notify_user_big_text(getApplicationContext(), NotificationGroup.UPDATE_CHECK_SERVICE, null, "Job Checker, in Start Exception \n" + sw.toString(),
                        NotificationGroup.UPDATE_CHECK_SERVICE.getID() + 100);
            }

        } finally {
            scheduleJob(nextTime);
            jobFinished(jobParameters, true);
        }
        return false; // let the system know we have no job running ..
    }

    HashMap<String, HistoryData> getHistory() {
        HashMap<String, HistoryData> code = new HashMap<>();
        try {
            SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            JSONObject json = new JSONObject(Utils.getConfig(config, Config.DEPARTURE_VISION_HISTORY, ConfigDefault.DEPARTURE_VISION_HISTORY));
            long time = json.getLong("time");
            Date dataTime = new Date(time);
            Date now = new Date();
            if ((now.getTime() - dataTime.getTime()) > TimeUnit.HOURS.toMillis(6)) {
                return code;
            }
            JSONObject history = (JSONObject) json.get("history");
            Iterator<String> keys = history.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject data = history.getJSONObject(key);
                Date dt = new Date(data.getLong("time"));
                String message = data.getString("message");
                String subject = data.getString("subject");

                HistoryData entry = new HistoryData(dt, message, subject);
                // drop old entries, more than 6 hours.
                if ((now.getTime() - dt.getTime()) > TimeUnit.HOURS.toMillis(6)) {
                    continue;
                }
                code.put(key, entry);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return code;
    }

    class HistoryData {
        public Date date;
        public String msg;
        public String subject;

        public HistoryData(Date date, String msg, String subject) {
            this.date = date;
            this.msg = msg;
            this.subject = msg;
        }
    }

    ;

    void save(HashMap<String, HistoryData> history) {
        JSONObject container = new JSONObject();
        JSONObject hist = new JSONObject();
        for (Map.Entry<String, HistoryData> e : history.entrySet()) {
            JSONObject data = new JSONObject();
            try {
                data.put("time", e.getValue().date.getTime());
                data.put("message", e.getValue().msg);
                data.put("subject", e.getValue().subject);
                hist.put(e.getKey(), data);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        try {
            container.put("time", (long) new Date().getTime());
            container.put("history", hist);
            SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Utils.setConfig(config, Config.DEPARTURE_VISION_HISTORY, container.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    long periodicCheck(JobParameters jobParameters) {
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean debug = config.getBoolean(Config.DEBUG, ConfigDefault.DEBUG);

        long nextTime = -1;
        HashMap<String, DepartureVisionData> dv = new HashMap<>();
        if (!config.getBoolean(Config.TRAIN_NOTIFICTION, ConfigDefault.TRAIN_NOTIFICTION)) {
            if (debug) {
                Log.d("UPD", "Job Checker, train notification turned off next in 10 minutes");
            }
            return TimeUnit.MINUTES.toMillis(10);
        }
        try {
            //JSONObject json = new JSONObject(Utils.getConfig(config, Config.DEPARTURE_VISION, ConfigDefault.DEPARTURE_VISION));
            JSONObject json = new JSONObject(Utils.getConfig(config, Config.DEPARTURE_VISION, ConfigDefault.DEPARTURE_VISION));
            DepartureVisionWrapper wrapper =(DepartureVisionWrapper) JSONObjectSerializer.unmarshall(DepartureVisionWrapper.class, json);
//

            String code = wrapper.code;
            long time =wrapper.time.getTime();
            {
                for ( DepartureVisionData v : wrapper.entries) {
                    Date dt = new Date(time);
                    v.createTime = Utils.makeDate(Utils.getTodayYYYYMMDD(dt), v.tableTime, "yyyyMMdd HH:mm a");
                    dv.put(v.block_id, v);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("UPD", e.toString());

        }
        if (isSystemServiceRunning()) {
            //
            long lastTime = config.getLong(Config.LAST_UPDATE_CHECK, ConfigDefault.LAST_UPDATE_CHECK);
            Date now = new Date();
            Date last = new Date(lastTime);
            long diff = now.getTime() - last.getTime();
            if (diff > TimeUnit.HOURS.toMillis(2)) {

                oneTimeCheck();
            }
            if (debug) {
                Utils.notify_user_big_text(getApplicationContext(), NotificationGroup.UPDATE_CHECK_SERVICE, null,
                        "Job Checker, System Service running, last check:" + last + " diff:" + TimeUnit.MILLISECONDS.toMinutes(diff) + " minutes ago",
                        NotificationGroup.UPDATE_CHECK_SERVICE.getID() + 101);
            }
        } else {
            if (debug) {
                Utils.notify_user(getApplicationContext(), NotificationGroup.UPDATE_CHECK_SERVICE, null, "Job Checker, System Service not running",
                        NotificationGroup.UPDATE_CHECK_SERVICE.getID() + 1);
            }
        }
        {
            HashMap<String, HistoryData> history = getHistory();
            try (SQLWrapper wrapper = new SQLWrapper(getApplicationContext(), "rails_checker_db.sql")) {
                wrapper.open();
                String startStation = wrapper.getConfig().getString(Config.START_STATION, ConfigDefault.START_STATION);
                String stopStation = wrapper.getConfig().getString(Config.STOP_STATION, ConfigDefault.STOP_STATION);
                // use the ID instead of the Block id so that we don't have an infinite number of notifications.
                IDGenerator ID = new IDGenerator(1);
                // do that for all routes configured in the system.
                long earliestEvent = updateCurrentRoutes(wrapper, history, dv, startStation, stopStation);
                if (earliestEvent > 0) {
                    nextTime = (nextTime == -1) ? earliestEvent : nextTime;
                    nextTime = Math.min(nextTime, earliestEvent);
                }
                earliestEvent = updateCurrentRoutes(wrapper, history, dv, stopStation, startStation);
                if (earliestEvent > 0) {
                    nextTime = (nextTime == -1) ? earliestEvent : nextTime;
                    nextTime = Math.min(nextTime, earliestEvent);
                }

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } finally {
                save(history);
            }
        }
        return nextTime;
    }

    long updateCurrentRoutes(SQLWrapper wrapper, HashMap<String, HistoryData> history, HashMap<String, DepartureVisionData> dv, String startStation, String stopStation) {
        ArrayList<Route> routes = wrapper.getRoutes(startStation, stopStation, null, null);
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Date now = new Date();
        long nextTime = -1;
        for (Route info : routes) {
            String key = info.station_code + "." + info.block_id;
            String message = "";
            if (!info.favorite) {
                continue;
            }
            boolean inFuture = info.departure_time_as_date.getTime() > now.getTime();
            if (inFuture) {
                if (nextTime <= 0) {
                    nextTime = info.departure_time_as_date.getTime();
                }
                nextTime = Math.min(info.departure_time_as_date.getTime(), nextTime);
            }
            if (history.keySet().contains(key)) {
                message = history.get(key).msg;
                long diff = now.getTime() - history.get(key).date.getTime();
                int duration = config.getInt(Config.NOTIFICATION_DELAY, ConfigDefault.NOTIFICATION_DELAY);
                if (diff > TimeUnit.MINUTES.toMillis(duration)) {
                    message = ""; // force publish.
                }
            }
            long diff = (info.departure_time_as_date.getTime() - now.getTime());


            if (diff > 0 && diff < TimeUnit.MINUTES.toMillis(60)) {
                String msg =
                        info.route_name + "\n" + info.from + " \u279F " + info.to
                        + "\nTravel time " + TimeUnit.MILLISECONDS.toMinutes(info.arrival_time_as_date.getTime() - info.departure_time_as_date.getTime()) + " minutes"
                        ;

                String subject = "#" + info.block_id + " " + info.station_code + " " + Utils.formatPrintableTime(info.departure_time_as_date, null);
                DepartureVisionData entry = dv.get(info.block_id);
                if (entry != null && entry.station_code.equals(info.station_code)) {
                    // make sure we don't use stale data.
                    if (Utils.getTodayYYYYMMDD(now).equals(Utils.getTodayYYYYMMDD(entry.createTime))) {
                        if (!entry.track.isEmpty()) {
                            msg += "\nTrack " + entry.track;
                            subject += " Track " + entry.track;
                        }
                        msg += " " + entry.status;
                        // filter status that contains time.
                        if (entry.status.toLowerCase().contains(" min") && entry.status.contains(" in ")) {
                            subject += " '" + entry.status + "'";
                        } else {
                            subject += " " + entry.status;
                        }
                    }
                }
                    //str.append(msg + "\n");

                if (!message.equals(msg)) {
                    boolean skip = false;
                    // some times the track info gets cleared, retain the old value
                    if( message.contains("Track") && !msg.contains("Track")) {
                        skip = true;
                    }
                    if(!skip) {
                        int id = Integer.parseInt(info.block_id) * 10 + info.station_code.getBytes()[0] * 10 + info.station_code.getBytes()[1];
                        Utils.notify_user_big_text(getApplicationContext(), NotificationGroup.UPCOMING, subject, msg, NotificationGroup.UPCOMING.getID() + 1000 + id); // no more than 5
                        history.put(key, new HistoryData(now, subject, msg));
                    }
                }

            } else {
                // clear any pending notifications, if diff is negative i.e in the past.
                if (-diff > TimeUnit.MINUTES.toMillis(15)) {
                    int id =  Integer.parseInt(info.block_id) *10 +   info.station_code.getBytes()[0] *10 + info.station_code.getBytes()[1];
                    final NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.cancel(NotificationGroup.UPCOMING.getID() + 1000 + id);
                }
            }
        }
        return nextTime;
    }

    boolean isSystemServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> processes = activityManager.getRunningServices(100);
        StringBuffer str = new StringBuffer();
        str.append("[");
        for (ActivityManager.RunningServiceInfo info : processes) {
            str.append(info.service.getClassName() + "\n");
        }

        str.append("]");
//        Utils.notify_user_big_text(getApplicationContext(), NotificationGroup.UPDATE_CHECK_SERVICE, "running list " + str.toString(),
//                NotificationGroup.UPDATE_CHECK_SERVICE.getID() + 2);


        for (ActivityManager.RunningServiceInfo info : processes) {
            if (info.service.getClassName().equals(SystemService.class.getName())) {
                return true;
            }
        }
        return false;
    }

    void scheduleJob(long scheduleTime) {
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean debug = config.getBoolean(Config.DEBUG, ConfigDefault.DEBUG);

        Date now = new Date();
        long epoch_time = now.getTime();
        long polling_time = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(Config.POLLING_TIME, ConfigDefault.POLLING_TIME); // config has micros.
        if (scheduleTime > 0) {
            // get the complicated schedule time, if the time is far far away
            //
            long diff = scheduleTime - epoch_time;
            if (diff > 0) {
                long computed_polling;
                if (diff > TimeUnit.MINUTES.toMillis(60)) {
                    computed_polling = TimeUnit.MINUTES.toMillis(30);
                } else  if (diff > TimeUnit.MINUTES.toMillis(30)) {
                    computed_polling = TimeUnit.MINUTES.toMillis(15);
                } else if (diff > TimeUnit.MINUTES.toMillis(10)) {
                    computed_polling = TimeUnit.MINUTES.toMillis(5);
                } else {
                    computed_polling = TimeUnit.MINUTES.toMillis(1);
                }
                polling_time = Math.max(polling_time, computed_polling);
            }
        } else {
            polling_time = Math.max(polling_time, TimeUnit.MINUTES.toMillis(30));
        }
        long diff = Utils.alignTime(polling_time);
        Log.d("UPDJOB", "Next Wake up time in diff:" + diff + " " + TimeUnit.MILLISECONDS.toMinutes(diff) + " (minutes)" + TimeUnit.MILLISECONDS.toSeconds(
                diff % (60000)) + " seconds" + " Polling:" + TimeUnit.MILLISECONDS.toMinutes(polling_time) + " (minutes)" + TimeUnit.MILLISECONDS.toSeconds(
                polling_time % (60000)) + " seconds" + " Schedule Time:" + new Date(scheduleTime) + " (" + scheduleTime + ")");
        PersistableBundle bundle = new PersistableBundle();
        bundle.putBoolean("periodic", true);

        //jobScheduler.cancelAll();
        Utils.scheduleJob(this.getApplicationContext(), JobID.UpdateCheckerJobService, UpdateCheckerJobService.class, (int) diff, false, bundle);
//        if(debug) {
//            JobScheduler jobScheduler = getApplicationContext().getSystemService(JobScheduler.class);
//            List<JobInfo> jobs = jobScheduler.getAllPendingJobs();
//            StringBuffer buffer = new StringBuffer();
//            for(JobInfo info:jobs) {
//                buffer.append(info.getId() + " " + info.getService().getClassName() +  " time:" + info.getIntervalMillis() + " \n");
//            }
//            Utils.notify_user_big_text(getApplicationContext(), NotificationGroup.UPDATE_CHECK_SERVICE, null,
//                    "Job Checker scheduled jobs \n" + buffer.toString(),
//                    NotificationGroup.UPDATE_CHECK_SERVICE.getID() + 200);
//        }
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d("UPDJOB", "onStopJob - done");
        return false;
    }

    void oneTimeCheck() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        boolean isMobile = activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
        Log.d("UPDJOB", "onStartJob - periodic job. " + isConnected + " wifi:" + isWiFi + " isMobile:" + isMobile);
        sendCheckForUpdate();
    }

    private void sendCheckForUpdate() {
        Intent intent = new Intent(NotificationValues.BROADCAT_CHECK_FOR_UPDATE_NO_FILTER);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
