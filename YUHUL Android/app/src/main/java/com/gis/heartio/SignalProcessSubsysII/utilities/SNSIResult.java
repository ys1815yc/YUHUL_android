package com.gis.heartio.SignalProcessSubsysII.utilities;

public class SNSIResult {
    public double[][] p_out;
    public double[][] NIPC;
    public double[] vf;
    public double[] vfbw;
    public double[] m_center;
    public double[] SNR_pf;
    public SNSIResult(double[][] p_in){
        int width = p_in[0].length;
        int depth = p_in.length;
        p_out = new double[depth][width];
        NIPC = new double[depth][width];
        vf = new double[width];
        vfbw = new double[width];
        m_center = new double[width];
        SNR_pf = new double[width];
    }
}
