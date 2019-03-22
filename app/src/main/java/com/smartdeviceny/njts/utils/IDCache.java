package com.smartdeviceny.njts.utils;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

public class IDCache {
    CacheManager cache;
    File file;
    JSONObject dict = new JSONObject();
    int id = -1;
    static Object lock = new Object();

    /**
     *
     * @param context
     * @param cacheDir the cache directory name "short name"
     */
    public IDCache(Context context, String cacheDir) {
        cache = new CacheManager(context, cacheDir, null);
        file = cache.getCachedFile("ID");
        if(file.exists()) {
            try {
                dict = new JSONObject(Utils.convertStreamToString(new FileInputStream(file)));
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        if(lock == null) {
            lock = new Object();
        }
    }

    public int getID(String name) {
        if( dict.has(name)) {
            try {
                return dict.getInt(name);
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
        if(id==-1) {
            Iterator<String> keys = dict.keys();

            id = 0;
           while( keys.hasNext() ) {
               String key = keys.next();
                try { id = Math.max(id, dict.getInt(key)); }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            id ++;
            try {
                dict.put(name, id);
                cache.saveCache(dict.toString(), file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return id;

    }
}
