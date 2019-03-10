package com.smartdeviceny.njts.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.smartdeviceny.njts.parser.Route;
import com.smartdeviceny.njts.values.Config;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SQLWrapper implements Closeable {
    SQLiteLocalDatabase sql;
    Context context;
    SharedPreferences config;

    public SQLWrapper(Context context) {
        this.context = context;
        config = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    @Override
    public void close() throws IOException {
        if(sql !=null) {
            sql.close();
        }
    }

    public void setSql(SQLiteLocalDatabase sql) {
        this.sql = sql;
    }

    public SharedPreferences getConfig() {
        return config;
    }
    public SQLiteLocalDatabase getSql() {
        return sql;
    }

    public void open() {
        File f = new File(context.getApplicationContext().getApplicationInfo().dataDir + File.separator + "rails_db.sql");
        if (f.exists()) {
            if (sql != null) {
                try {
                    sql.close();
                } catch (Exception e) {

                }
            }
            sql = new SQLiteLocalDatabase(context.getApplicationContext(), f.getName(), null);
            sql.opendatabase();
        }
    }

    public String getStationCode(String station) {
        if (sql == null) {
            return "NY";
        }
        String value = SqlUtils.getStationCode(sql.getReadableDatabase(), station);
        Log.d("SVC", "looking up station code " + station + "=" + value);
        if (value == null || value.isEmpty()) {
            return "NY";
        }
        return value;
    }

    public HashSet<String> getFavortes() {
        HashSet<String> favorites = new HashSet<>();
        if (config != null) {
            Set<String> tmp = config.getStringSet(Config.FAVORITES, favorites);
            favorites = new HashSet<>(tmp); // re-init the data structures
        }
        return favorites;
    }

    public ArrayList<Route> getRoutes(String from, String to, @Nullable Integer date, @Nullable Integer delta) {
        ArrayList<Route> r = new ArrayList<>();
        SQLiteDatabase db = null;
        if (sql == null) {
            return r;
        }
        if (delta == null) {
            delta = new Integer(2);
        }

        if (date == null) {
            date = Integer.parseInt(Utils.getLocaDate(0));
        }
        try {
            db = sql.getReadableDatabase();
        } catch (Exception e) {
            return r;
        }

        HashSet<String> favorites = getFavortes();
        String station_code = getStationCode(from);
        delta = Math.max(1, delta);
        for (int i = -delta; i < delta; i++) {
            try {
                Date stDate = DateFormatCont.yyyyMMddFmt.parse("" + date);
                stDate = Utils.adddays(stDate, i);
                ArrayList<HashMap<String, Object>> routes = Utils.parseCursor(SQLHelper.getRoutes(db, from, to, Integer.parseInt(DateFormatCont.yyyyMMddFmt.format(stDate))));
                for (HashMap<String, Object> rt : routes) {
                    r.add(new Route(station_code, DateFormatCont.yyyyMMddFmt.format(stDate), from, to, rt, favorites.contains(rt.get("block_id").toString())));
                }
            } catch (Exception e) {

            }
        }
        return r;
    }

}
