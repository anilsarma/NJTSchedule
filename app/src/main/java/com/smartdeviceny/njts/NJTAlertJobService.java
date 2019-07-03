package com.smartdeviceny.njts;

import android.app.DownloadManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.smartdeviceny.njts.annotations.JSONObjectSerializer;
import com.smartdeviceny.njts.utils.ConfigUtils;
import com.smartdeviceny.njts.utils.DownloadFile;
import com.smartdeviceny.njts.utils.Feed;
import com.smartdeviceny.njts.utils.IDCache;
import com.smartdeviceny.njts.utils.JobID;
import com.smartdeviceny.njts.utils.NotificationGroup;
import com.smartdeviceny.njts.utils.RailAlertDetails;
import com.smartdeviceny.njts.utils.RailDetailsContainer;
import com.smartdeviceny.njts.utils.RssFeedCategorise;
import com.smartdeviceny.njts.utils.RssRailFeedParser;
import com.smartdeviceny.njts.utils.Utils;
import com.smartdeviceny.njts.values.Config;
import com.smartdeviceny.njts.values.ConfigDefault;
import com.smartdeviceny.njts.values.NotificationValues;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class NJTAlertJobService extends JobService {
    SharedPreferences config = null;
    boolean debug = false;
    IDCache idCache;

    @Override
    public boolean onStartJob(JobParameters params) {
        config = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        debug = config.getBoolean(Config.DEBUG, ConfigDefault.DEBUG);
        idCache = new IDCache(getApplicationContext(), "ids");

        if (debug) {
            Utils.notify_user(getApplicationContext(), NotificationGroup.ALERT_CHECK_SERVICE, null, "Alert Checker, in Start", NotificationGroup.ALERT_CHECK_SERVICE.getID() + 1);
        }
        try {
            Date now = new Date();
            if ((now.getHours() >= 4)) {
                downloadAlert();
            }
        } finally {
            scheduleJob(new Date().getTime() + TimeUnit.MINUTES.toMillis(15));
        }

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }


    void scheduleJob(long scheduleTime) {
        Date now = new Date();
        long epoch_time = now.getTime();
        long polling_time = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(Config.ALERT_POLLING_TIME,
                ConfigDefault.ALERT_POLLING_TIME); // config has micros.
        if (scheduleTime > 0) {
            long diff = scheduleTime - epoch_time;
            if (diff > 0) {
                long computed_polling;
                if (diff > TimeUnit.MINUTES.toMillis(60)) {
                    computed_polling = TimeUnit.MINUTES.toMillis(30);
                } else {
                    // TODO:: get the polling time from the config, poll more frequently during
                    // commute hours.
                    if (now.getHours() >= 10 && now.getHours() <= 14) {
                        computed_polling = TimeUnit.MINUTES.toMillis(30);
                    } else {
                        computed_polling = TimeUnit.MINUTES.toMillis(15);
                    }
                }
                polling_time = Math.max(polling_time, computed_polling);
            }
        } else {
            polling_time = Math.max(polling_time, TimeUnit.MINUTES.toMillis(30));
        }

        long diff = Utils.alignTime(polling_time);
        PersistableBundle bundle = new PersistableBundle();
        bundle.putBoolean("periodic", true);
        Utils.scheduleJob(this.getApplicationContext(), JobID.NJTAlertJobService, NJTAlertJobService.class, (int) diff, false, bundle);

    }

    private void downloadAlert() {
        final DownloadFile d = new DownloadFile(getApplicationContext(), new DownloadFile.Callback() {
            @Override
            public boolean downloadComplete(DownloadFile d, long id, String url, File file) {
                config = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                debug = config.getBoolean(Config.DEBUG, ConfigDefault.DEBUG);
                boolean allowNotification = config.getBoolean(Config.TRAIN_NOTIFICTION, ConfigDefault.TRAIN_NOTIFICTION);

                if (debug) {
                    Utils.notify_user(getApplicationContext(), NotificationGroup.ALERT_CHECK_SERVICE, null, "Alert Checker, download complete ",
                            NotificationGroup.ALERT_CHECK_SERVICE.getID() + 2);
                }

                RssRailFeedParser parser = new RssRailFeedParser();
                try {
                    RailDetailsContainer cat = parser.parse(new FileInputStream(file));
                    JSONObject obj = JSONObjectSerializer.marshall(cat);
                    Utils.setConfig(config, Config.ALERT_JSON, obj.toString());
                    Log.d("ALERT", "object ... " + obj.toString());
                    sendAlertsUpdated();
                    //ConfigUtils.setLong(config, Config.LAST_ALERT_TIME, 0);
                    long lastPubTime = config.getLong(Config.LAST_ALERT_TIME, ConfigDefault.LAST_ALERT_TIME);
                    long maxPubTime = lastPubTime;
                    int index = 1;
                    for (RailAlertDetails detail : cat.getTrain()) {
                        if (!DateUtils.isToday(detail.getTime())) {
                            continue;
                        }
                        if (detail.getTimeDate().getTime() > lastPubTime) {
                            maxPubTime = Math.max(maxPubTime, detail.getTimeDate().getTime());
                            if( allowNotification) {
                                Utils.notify_user_big_text(getApplicationContext(), NotificationGroup.ALERT, "Train " + detail.getLong_name(), detail.getTimeDate() + "\n\n" + detail.getAlertText(), NotificationGroup.ALERT.getID() + idCache.getID(detail.getShort_code()));
                            }
                        }
                        index++;
                    }
                    if (maxPubTime > lastPubTime) {
                        // save it
                        ConfigUtils.setLong(config, Config.LAST_ALERT_TIME, maxPubTime);
                    }

                } catch (Throwable e) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);

                    if (allowNotification && debug) {
                        Utils.notify_user_big_text(getApplicationContext(), NotificationGroup.ALERT_CHECK_SERVICE, null, "Alert Checker, download complete, " + "failed parsing \n" + sw, NotificationGroup.ALERT_CHECK_SERVICE.getID() + 3);
                    }
                }
                Utils.delete(file);
                Utils.cleanFiles(file.getParentFile(), "RailAdvisories_feed.xml");
                return false;
            }

            @Override
            public void downloadFailed(DownloadFile d, long id, String url) {
                config = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                debug = config.getBoolean(Config.DEBUG, ConfigDefault.DEBUG);

                if (debug) {
                    Utils.notify_user(getApplicationContext(), NotificationGroup.ALERT_CHECK_SERVICE, null, "Alert Checker, download failed ",
                            NotificationGroup.ALERT_CHECK_SERVICE.getID() + 2);
                }

            }
        });
        try {
            DownloadManager.Request request = d.buildRequest("https://www.njtransit.com/rss/RailAdvisories_feed.xml", "RailAdvisories_feed.xml", "NJ Transit Alerts",
                    DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI, "text/html");
            d.enqueue(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendAlertsUpdated() {
        Intent intent = new Intent(NotificationValues.BROADCAT_ALERT_UPDATED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d("AJOB", "sending " + NotificationValues.BROADCAT_ALERT_UPDATED);
        //Toast.makeText(getApplicationContext(),"sending " + NotificationValues.BROADCAT_DEPARTURE_VISION_UPDATED, Toast.LENGTH_SHORT).show();
    }
}
