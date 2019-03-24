package com.smartdeviceny.njts.utils;

import com.smartdeviceny.njts.annotations.Persist;

import java.util.ArrayList;
import java.util.Comparator;

public class RailDetailsContainer {
    @Persist
    private ArrayList<RailAlertDetails> trains = new ArrayList<>();
    private ArrayList<RailAlertDetails> construction = new ArrayList<>();
    private ArrayList<RailAlertDetails> service = new ArrayList<>();


    public RailDetailsContainer() {}

    public void addTrain(RailAlertDetails entry) {
        this.trains.add(entry);
    }
    public void addConstruction(RailAlertDetails entry) {
        this.construction.add(entry);
    }


    public void addService(RailAlertDetails entry) {
        this.service.add(entry);
    }


    final public ArrayList<RailAlertDetails> getTrain() {
        return trains;
    }

    final public ArrayList<RailAlertDetails> getConstruction() {
        return construction;
    }

    final public ArrayList<RailAlertDetails> getService() {
        return service;
    }

    public void setConstruction(ArrayList<RailAlertDetails> construction) {
        this.construction = construction;
    }

    public void setService(ArrayList<RailAlertDetails> service) {
        this.service = service;
    }

    public void setTrain(ArrayList<RailAlertDetails> trains) {
        this.trains = trains;
    }

    public void sort() {
        trains.sort(Comparator.comparingLong(RailAlertDetails::getTime));
        construction.sort(Comparator.comparingLong(RailAlertDetails::getTime));
        service.sort(Comparator.comparingLong(RailAlertDetails::getTime));
    }

}
