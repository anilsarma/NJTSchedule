package com.smartdeviceny.njts;


import android.support.annotation.NonNull;

import com.smartdeviceny.njts.annotations.JSONObjectSerializer;
import com.smartdeviceny.njts.annotations.Persist;

import java.util.ArrayList;
import java.util.Date;



class TestObject {
    @Persist
    Date date;

    @Persist
    Double class_double = new Double(100);

    @Persist
    double primitive_double;

    @Persist
    Integer class_integer;

    @Persist
    int primitive_integer = 12;

   // @Persist
    ArrayList<Integer> ids = new ArrayList<Integer>();
    @Persist
    ArrayList<TestObject> tos = new ArrayList<TestObject>();

    public TestObject() {

    }

    @NonNull
    @Override
    public String toString() {
        return JSONObjectSerializer.stringify(this);
    }
}
