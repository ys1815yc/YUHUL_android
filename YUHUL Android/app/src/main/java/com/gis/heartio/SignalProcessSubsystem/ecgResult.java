package com.gis.heartio.SignalProcessSubsystem;

import java.util.Comparator;

public class ecgResult{
    public int RPeakIndex;
    public double doubleVTI;
    public double doubleVpk;

    public ecgResult(){
        super();
    }

    public int getRPeakIndex() {
        return RPeakIndex;
    }

    public void setRPeakIndex(int index){
        RPeakIndex = index;
    }

    public double getDoubleVTI() {
        return doubleVTI;
    }

    public void setDoubleVTI(double doubleVTI) {
        this.doubleVTI = doubleVTI;
    }

    public double getDoubleVpk() {
        return doubleVpk;
    }

    public void setDoubleVpk(double doubleVpk) {
        this.doubleVpk = doubleVpk;
    }
}
