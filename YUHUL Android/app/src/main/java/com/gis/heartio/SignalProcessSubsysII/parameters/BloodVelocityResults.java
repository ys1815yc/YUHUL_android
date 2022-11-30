package com.gis.heartio.SignalProcessSubsysII.parameters;

public class BloodVelocityResults {
	public double mDblHR;
	public double mDblPeriod;
	public double mDblVTI;
	public double mDblSV;
	public double mDblCO;
	public double mDblVPK;
	public double mDblDia;
	public double mDblTmpValue;
	
// Signal Conditions
	public int mIntDataLoss;
	public double mDblHeartRateStability;
	public double mDblVTIStability;
	public double mDblSignalStrength;
	public double mDblWhiteNoiseStrength;
	public double mDblMeanNoiseStrength;
	public double mDblSignalWhiteNoiseRatio;
	public double mDblSignalMeanNoiseRatio;
	public double mDblAudioAmplitude;

	public void setBVInit() {
		mDblHR = 0;
		mDblPeriod = 0;
		mDblVTI = 0;
		mDblSV = 0;
		mDblCO = 0;
		mDblVPK = 0;
		mDblDia = 0;

/* Signal Conditions
		public int mIntDataLoss;
		public double mDblHeartRateStability;
		public double mDblVTIStability;
		public double mDblSignalStrength;
		public double mDblWhiteNoiseStrength;
		public double mDblMeanNoiseStrength;
		public double mDblSignalWhiteNoiseRatio;
		public double mDblSignalMeanNoiseRatio;
		public double mDblAudioAmplitude;
//* Signal Conditions */
    }

}

