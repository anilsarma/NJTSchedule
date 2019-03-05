package com.smartdeviceny.njts;

import android.app.DownloadManager;

import java.util.ArrayList;

public class DownloadDataTable {

    class Data {
        int status;
        int id;

        Data(int status, int id) {
            this.status = status;
            this.id = id;
        }

        public int getStatus() {
            return status;
        }

        public int getId() {
            return id;
        }
    }
    public int index =0;
    ArrayList<Data> data = new ArrayList<>();

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int size() {
        return data.size();
    }
    public int getStatus() {
        return data.get(index).getStatus();
    }
    public int getID() {
        return data.get(index).getId();
    }

    public void add(int status, int id) {
        data.add(new Data(status, id));
    }



}
