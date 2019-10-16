package com.smartdeviceny;

import androidx.lifecycle.MutableLiveData;

public class SharedLiveData {
    private static SharedLiveData instance  = new SharedLiveData();

    MutableLiveData<String> onComunicate = new MutableLiveData<>();
    MutableLiveData<String> dataBaseCheckComplete = new MutableLiveData<>();

    public static SharedLiveData instance() {
        return instance;
    }

    public MutableLiveData<String> getComunicate() {
        if(onComunicate == null) {
            onComunicate = new MutableLiveData<>();
        }
        return onComunicate;
    }

    public MutableLiveData<String> getDataBaseCheckComplete() {
        if(dataBaseCheckComplete == null) {
            dataBaseCheckComplete = new MutableLiveData<>();
        }
        return dataBaseCheckComplete;
    }
}
