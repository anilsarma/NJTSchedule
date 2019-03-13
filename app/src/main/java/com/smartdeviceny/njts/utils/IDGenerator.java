package com.smartdeviceny.njts.utils;

public class IDGenerator {
    int id;
    public  IDGenerator(int inital) {
        this.id = inital;
    }

    public int getNext() {
        return id++;
    }

    public void setId(int id) {
        this.id = id;
    }
}
