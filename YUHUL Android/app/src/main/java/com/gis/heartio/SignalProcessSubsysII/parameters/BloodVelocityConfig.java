package com.gis.heartio.SignalProcessSubsysII.parameters;
import com.gis.heartio.SignalProcessSubsysII.utilities.Tag;


public class BloodVelocityConfig {
//	public String mStrProcedureMode = Tag.STR_PROCEDURE_MODE_WU; 
//	public String mStrProcedureMode = Tag.STR_PROCEDURE_MODE_JF; 
	public int mIntProcedureMode = Tag.INT_PROCEDURE_MODE_PWR;

	//******************************************************************
    //************** For Calculate  Constant Parameters*********
    //******************************************************************
	public static final double DOUBLE_ULTRASOUND_CAPTURE_LENGTH_SEC = 14.0;   
	public static final int INTEGER_ULTRASOUND_CAPTURE_LENGTH_PTS = 112000;   

	public static final int INTEGER_ULTRASOUND_SAMPLERATE = 8000;
	public static final int INTEGER_STFFT_SAMPLERATE = 125;	// 8000 / 64
	public static final int INTEGER_ULTRASOUND_MAXVALUE = 4096;
       
	public static final int  INTEGER_STFFT_WINDOW_SIZE = 256;   
	public static final int  INTEGER_STFFT_WINDOWS_SHIFESIZE = 64;
	public static final int  INTEGER_STFFT_SUBSEQ_FREQCNT = 129;
	public static final int  INTEGER_STFFT_SUBSEQs_TOTALCNT = 1747; // (8000 * 14 -256) / 64
    
	public static final double DOUBLE_ULTRASOUND_SPEED_FOR_BODY_METER_PERSEC = 1540;
	public static final double DOUBLE_ULTRASOUND_SENSOR_WAVE_FREQ = 2500000 ; //2.5 Mega
	
    public int mIntSTFTWindowSize = INTEGER_STFFT_WINDOW_SIZE;   
    public int mIntSTFTWindowShiftSize = INTEGER_STFFT_WINDOWS_SHIFESIZE;
	public int mIntSubSeqFreqCnt = INTEGER_STFFT_SUBSEQ_FREQCNT;
	public int mIntSubSeqsTotalCnt = INTEGER_STFFT_SUBSEQs_TOTALCNT; // (8000 * 14 -256) / 64
    
	public int mIntUltrasoundSamplesMaxSizeForRun = INTEGER_ULTRASOUND_CAPTURE_LENGTH_PTS; 
	public double mDblUltrasoundSamplesTotalSec = DOUBLE_ULTRASOUND_CAPTURE_LENGTH_SEC; 
	public int mDblUltrasoundSamplesTotalPts = INTEGER_ULTRASOUND_CAPTURE_LENGTH_PTS; 
	public int mIntUltrasoundSamplerate = INTEGER_ULTRASOUND_SAMPLERATE; 
	public int mIntSTFFTSamplerate = INTEGER_STFFT_SAMPLERATE; 

	public int mIntUltrasoundSampleMaxValue = INTEGER_ULTRASOUND_MAXVALUE;
    
	//******************************************************************
    //************** Wav & Spectrum Dat  *********
    //******************************************************************
    public int[] mIntArrayUltrasoundData;
    public double[] mDblArrayUltrasoundData;
	public double[][] mDoubleBVSpectrumOrg;
	public double[][] mDoubleBVSpectrumImg;
	public double[][] mDoubleBVSpectrumFilter;
	//******************************************************************
    //************** Profile Data  *********
    //******************************************************************
	public int[] mIntArrayVTIMaxIdx;
	

	//******************************************************************
    //************** User Data   *********
    //******************************************************************
    public double mDblUserPulmonaryDiameter;

    //**********************************************************************************
    // ***************** Signal Conditions   *************************************
    //**********************************************************************************
    public int mIntDataLoss;

    //**********************************************************************************
    // ***************** Noise & Signal Learning   *************************************
    //**********************************************************************************
    public static final double DOUBLE_CALCULATE_LENGTH_SEC = 8.0;   
    public static final int INTEGER_CALCULATE_LENGTH_PTS = 1000;	// 8.0 * 125   

    public static final double DOUBLE_SIGNAL_SAMPLE_LENGTH_SEC = 2.0;   // for DC offset
    public static final double DOUBLE_SIGNAL_COMPARE_LENGTH_SEC = 2.0;   // for DC offset
    public static final double DOUBLE_NOISE_SIGNAL_LEARN_START_SEC = 1.0;   // for DC offset
    public static final int INTEGER_NOISE_SIGNAL_LEARN_START_IDX = 125;   // for DC offset

    public static final int INTEGER_SIGNAL_LEARN_START_IDX = 0;   // for DC offset
    public static final int INTEGER_SIGNAL_LEARN_END_IDX = 64;   // for DC offset
    public static final int INTEGER_NOISE_LEARN_START_IDX = 98;   // for DC offset
    public static final int INTEGER_NOISE_LEARN_END_IDX = 128;   // for DC offset
    
    public static final double DOUBLE_NOISE_SIGNAL_LEARN_END_SEC = 3.0;
  
    public static int mIntStartIdxNoiseLearn, mIntEndIdxNoiseLearn, mIntLengthNoiseLearn;
    public static double mDoubleNoiseBaseLearned, mDoubleSignalBaseLearned, mDoubleSNRLearned;
    public static double mDoubleNoiseStrengthWu, mDoubleNoiseRangeWu, mDoubleSignalStrengthMaxWu;
 

    

//    public BloodVelocityConfig() {
//
//    }
    
}
