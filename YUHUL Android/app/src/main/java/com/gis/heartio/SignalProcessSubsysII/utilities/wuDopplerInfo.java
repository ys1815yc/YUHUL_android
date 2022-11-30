package com.gis.heartio.SignalProcessSubsysII.utilities;

import java.util.ArrayList;

public class wuDopplerInfo {
    public double[] freqArray;
    public double[] angleArray;
    public double[] vpkArray;
    public ArrayList<segObject> segList;
    public double HRLength;
    public boolean isSNRFail;
    public boolean isSegFail;
    public boolean isHRFail;
    public double HR;
    public double Vpk;
    public double VTI;

    public wuDopplerInfo(){
        freqArray = new double[1750];
        angleArray = new double[1750];
        vpkArray = new double[1750];
        segList = new ArrayList<>();
        HRLength = 0;
    }
}

