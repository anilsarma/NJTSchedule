package com.smartdeviceny.njts;

import androidx.annotation.NonNull;

import com.smartdeviceny.njts.annotations.JSONObjectSerializer;
import com.smartdeviceny.njts.annotations.Persist;

import java.util.HashMap;

class TestObjectHash {

    @Persist
    HashMap<String, String> tos = new HashMap<>();
    @Persist
    HashMap<String, TestObject> too = new HashMap<>();
    public TestObjectHash() {

    }

    @NonNull
    @Override
    public String toString() {
        return JSONObjectSerializer.stringify(this);
    }
}
