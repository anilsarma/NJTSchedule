package com.smartdeviceny.njts;

import android.app.ActivityManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.smartdeviceny.njts.parser.DepartureVisionData;
import com.smartdeviceny.njts.parser.DepartureVisionParser;
import com.smartdeviceny.njts.parser.Route;
import com.smartdeviceny.njts.utils.JobID;
import com.smartdeviceny.njts.utils.NotificationGroup;
import com.smartdeviceny.njts.utils.SQLWrapper;
import com.smartdeviceny.njts.utils.Utils;
import com.smartdeviceny.njts.values.Config;
import com.smartdeviceny.njts.values.ConfigDefault;
import com.smartdeviceny.njts.values.NotificationValues;

import org.json.JSONObject;
import org.jsoup.Jsoup;

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
        //Utils.notify_user(getApplicationContext(), NotificationGroup.UPDATE_CHECK_SERVICE, "Job Checker, in Start", NotificationGroup.UPDATE_CHECK_SERVICE.getID() + 1);
        try {
            PersistableBundle bundle = jobParameters.getExtras();
            if (bundle == null || bundle.getBoolean("periodic", false)) {
                oneTimeCheck();
            }
            periodicCheck(jobParameters);
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            scheduleJob();
            jobFinished(jobParameters, true);
        }
        return false; // let the system know we have no job running ..
    }

    HashMap<String, Date> getHistory() {
        HashMap<String, Date> code = new HashMap<>();
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
                long value = history.getLong(key);

                Date dt = new Date(value);
                // drop old entries, more than 6 hours.
                if ((now.getTime() - dt.getTime()) > TimeUnit.HOURS.toMillis(6)) {
                    continue;
                }
                code.put(key, dt);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return code;
    }

    void save(HashMap<String, Date> history) {
        JSONObject container = new JSONObject();
        JSONObject hist = new JSONObject();
        for (Map.Entry<String, Date> e : history.entrySet()) {
            try {
                hist.put(e.getKey(), e.getValue().getTime());
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

    void periodicCheck(JobParameters jobParameters) {
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean debug = config.getBoolean(Config.DEBUG, ConfigDefault.DEBUG);

        HashMap<String, DepartureVisionData> dv = new HashMap<>();
        if (!config.getBoolean(Config.TRAIN_NOTIFICTION, ConfigDefault.TRAIN_NOTIFICTION)) {
            return;
        }
        try {
            JSONObject json = new JSONObject(Utils.getConfig(config, Config.DEPARTURE_VISION, ConfigDefault.DEPARTURE_VISION));
            String data = (String) json.get("data");
            String code = (String) json.get("code");
            long time = json.getLong("time");

            if (data.length() > 0) {
                data = Utils.decodeToString(data);
                DepartureVisionParser parser = new DepartureVisionParser();
                dv = parser.parseDepartureVision(code, Jsoup.parse(data));
                for (Map.Entry<String, DepartureVisionData> entry : dv.entrySet()) {
                    DepartureVisionData v = entry.getValue();
                    Date dt = new Date(time);
                    v.createTime = Utils.makeDate(Utils.getTodayYYYYMMDD(dt), v.tableTime, "yyyyMMdd HH:mm a");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("UPD", e.toString());

        }
        if (isSystemServiceRunning()) {

            long lastTime = config.getLong(Config.LAST_UPDATE_CHECK, ConfigDefault.LAST_UPDATE_CHECK);
            Date now = new Date();
            Date last = new Date(lastTime);
            long diff = now.getTime() - last.getTime();
            if (diff > TimeUnit.HOURS.toMillis(2)) {

                oneTimeCheck();
            }
            if (debug) {
                Utils.notify_user_big_text(getApplicationContext(), NotificationGroup.UPDATE_CHECK_SERVICE,
                        "Job Checker, System Service running, last check:" + last + " diff:" + TimeUnit.MILLISECONDS.toMinutes(diff) + " minutes ago",
                        NotificationGroup.UPDATE_CHECK_SERVICE.getID() + 1);
            }
        } else {
            if (debug) {
                Utils.notify_user(getApplicationContext(), NotificationGroup.UPDATE_CHECK_SERVICE, "Job Checker, System Service not running",
                        NotificationGroup.UPDATE_CHECK_SERVICE.getID() + 1);
            }
        }
        {

            HashMap<String, Date> history = getHistory();
            try (SQLWrapper wrapper = new SQLWrapper(getApplicationContext())) {
                wrapper.open();
                String startStation = wrapper.getConfig().getString(Config.START_STATION, ConfigDefault.START_STATION);
                String stopStation = wrapper.getConfig().getString(Config.STOP_STATION, ConfigDefault.STOP_STATION);
                int ID = 1; // use the ID instead of the Block id so that we don't have an infinite number of notifications.
                // do that for all routes configured in the system.
                ID = updateCurrentRoutes(ID, wrapper, history, dv, startStation, stopStation);
                ID = updateCurrentRoutes(ID, wrapper, history, dv, stopStation, startStation);

            } catch (Exception e) {

            } finally {
                save(history);
            }
        }
    }

    int updateCurrentRoutes(int ID, SQLWrapper wrapper, HashMap<String, Date> history, HashMap<String, DepartureVisionData> dv, String startStation, String stopStation) {
        ArrayList<Route> routes = wrapper.getRoutes(startStation, stopStation, null, null);
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Date now = new Date();

        for (Route info : routes) {
            String key = startStation + "." + info.block_id;
            if (!info.favorite) {
                continue;
            }

            if (history.keySet().contains(key)) {
                long diff = now.getTime() - history.get(key).getTime();
                int duration = config.getInt(Config.NOTIFICATION_DELAY, ConfigDefault.NOTIFICATION_DELAY);
                if (diff < TimeUnit.MINUTES.toMillis(duration)) {
                    Log.d("UPD", key + " Filtered by time " + TimeUnit.MILLISECONDS.toMinutes(diff) + " minutes, period:" + duration + " minutes");
                    continue;
                }
            }
            long diff = (info.departure_time_as_date.getTime() - now.getTime());


            if (diff > 0 && diff < TimeUnit.MINUTES.toMillis(60)) {
                String msg = info.block_id + " departs " + Utils.formatPrintableTime(info.departure_time_as_date, null) + " from " + info.station_code;
                DepartureVisionData entry = dv.get(info.block_id);
                if (entry != null) {
                    // make sure we don't use stale data.
                    if (Utils.getTodayYYYYMMDD(now).equals(Utils.getTodayYYYYMMDD(entry.createTime))) {
                        if (!entry.track.isEmpty()) {
                            msg += " Track " + entry.track;
                        }
                        msg += " " + entry.status;
                    }
                }
                //str.append(msg + "\n");
                Utils.notify_user_big_text(getApplicationContext(), NotificationGroup.UPCOMING, msg, NotificationGroup.UPCOMING.getID() + 10000 + (ID++ % 5)); // no more than 5
                history.put(key, now);
            }
        }
        return ID;
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

    void scheduleJob() {
        int polling_time = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(Config.POLLING_TIME, ConfigDefault.POLLING_TIME);
        polling_time = Math.max(10000, polling_time);
        Date now = new Date();
        long epoch_time = now.getTime();
        epoch_time += polling_time; // next polling time
        epoch_time = (epoch_time / polling_time) * polling_time; // to the next clock time.
        long diff = epoch_time - now.getTime();

        PersistableBundle bundle = new PersistableBundle();
        bundle.putBoolean("periodic", true);
        Utils.scheduleJob(this.getApplicationContext(), JobID.UpdateCheckerJobService, UpdateCheckerJobService.class, (int) diff, false, bundle);
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

    public void sendCheckForUpdate() {
        Intent intent = new Intent(NotificationValues.BROADCAT_CHECK_FOR_UPDATE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
