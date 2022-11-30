package com.gis.heartio.SignalProcessSubsysII.utilities;

public class segObject{
    public double segVTI;   // VTI in this segment
    public double segVpk;   // Vpk in this segment
    public int StartPt;     // HR Segment start point
    public int EndPt;       // HR Segment end point
    public boolean isDiscarded;
    public int segVpkIdx;   // segVpk index
    public int segVTIStartPt;
    public int segVTIEndPt;
    public segObject(int sPt, int ePt, double vpk, double vti){
        this.StartPt = sPt;
        this.EndPt = ePt;
        this.segVpk = vpk;
        this.segVTI = vti;
        this.isDiscarded = true;
    }
}
