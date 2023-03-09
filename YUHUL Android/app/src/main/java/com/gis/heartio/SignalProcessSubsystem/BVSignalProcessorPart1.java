package com.gis.heartio.SignalProcessSubsystem;

import android.util.Log;

import com.gis.CommonUtils.Constants;
import com.gis.heartio.SignalProcessSubsysII.utilities.Doppler;
import com.gis.heartio.SignalProcessSubsysII.utilities.Tag;
import com.gis.heartio.SignalProcessSubsysII.utilities.Type;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.SystemConfig;
import com.gis.heartio.UIOperationControlSubsystem.MainActivity;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import com.gis.heartio.UIOperationControlSubsystem.UserManagerCommon;

//jaufa, +, 191024
import com.gis.heartio.SignalProcessSubsysII.utilities.Methodoligies;

import uk.me.berndporr.iirj.*;

/**
 * Created by 780797 on 2016/6/20.
 */
public class BVSignalProcessorPart1 {

    private double DOUBLE_LEARN_MAX_IDX_HIGH_COMPARE_RATIO = 0.7;

    //****************************
    // for Short Time FFT
    //****************************
    public double DOUBLE_AMPPSD_NORMALIZE_PERIOD_SEC = 2.0;
    public int mIntAmpPsdNormPeriodSize, mIntAmpPsdNormPeriodSizeCur;
    public double mDoubleAmpPsdMaxCur, mDoubleAmpPsdMaxPeriod, mDoubleAmpPsdMaxPeriodFirst, mDoubleAmpPsdMaxTotal, mDoubleAmpPsdMaxAfterLog;
    public double[] mDoubleAmpPsdMaxNormPeriod;

    public double mDoubleFreqGap;
    private MySTFT mShortTimeFT;

    public double[][] mDoubleBVSpectrumValues; //, mDoubleBVSpectrumValuesOri;
    public double[][] mDoubleBVSpectrumIntegral;
    public double[] mDoubleBVSpectrumIntegralDebug;
    //public double[] mDoubleBVPower, mDoubleBVPowerBeforeSort;
    public double[] mDoubleBVFreqValues;
    public int[] mIntSubSegGainLevels;

    private short[] mShortFeedData;

    public int mIntTotalSubSegMaxSize;
    public int mIntTotalFreqSeqsCnt;
    public int mIntSTFFTNextSubSegIdx, mIntTrySTFFTNextSubSegIdx;
    private int mIntLeftSubSegSize;

    public float mFloatTimeUnitMiniSec;

    //****************************
    // for ECG
    //****************************
    public short[][] mShortECGValues;
    public short[] mShortECGFilteredValue;
    private double ecgThreshold;
    private double mdf_ECG;
    private int ecgRC;
    private boolean ecgSW;

    public ArrayList<ecgResult> mEcgList;
    public Butterworth butterworth = new Butterworth();
    public Butterworth butterworthLow = new Butterworth();
    public Butterworth butterworthHigh = new Butterworth();
    public Butterworth butterworthUSLow = new Butterworth();
    public Butterworth butterworthUSHigh = new Butterworth();
    public Butterworth butterworthUSBS = new Butterworth();
    public ChebyshevI chebyshevUSHigh = new ChebyshevI();
    //****************************
    // for Signal/Noise Learning
    //****************************
    public double mDoubleNoiseBaseLearnedPartL, mDoubleSignalBaseLearnedPartL, mDoubleSNRBaseLearnedPartL;
    public double mDoubleNoiseBaseLearnedPartH, mDoubleSignalBaseLearnedPartH, mDoubleSNRBaseLearnedPartH;

    public double mDoubleMaxIdxCompareBase, mDoubleMaxIdxCompareBaseRatio;
    public boolean mBoolMaxIdxBaseLearned;

    public double mDoubleLearnFreqEndIdxRatio;

    //****************************
    // for MaxIdx
    //****************************
    public int[] mIntArrayMaxIdx_Tri;     //jaufa, 180607, +
    public int[] mIntArrayMaxIdx;
    public double[] mDoubleArraySignalPowerIdx; //jaufa, +, 181102
    public int[] mIntArrayMaxIdx_Hr;     //jaufa, 180607, +
    public int[] mIntArrayMaxIdx_Period;     //jaufa, 180607, +
    public int[] mIntArrayMaxIdx_VPK;     //jaufa, 180607, +
    public int[] mIntArrayHRPeakPosition;   //jaufa, 180805, +
    public int[] mIntArrayHRValleyPosition;   //jaufa, 180805, +
    public int[] mIntArrayHRStartPosition;   //jaufa, 180806, +
    public int[] mIntArrayHREndPosition;   //jaufa, 180806, +
    public int[] mIntArrayVPKPeakPosition;  //jaufa, 181003, +
    public int[] mIntArrayVTIStartPosition;   //jaufa, 180806, +
    public int[] mIntArrayVTIEndPosition;   //jaufa, 180806, +
    public int mIntHRCounts, mIntHRPeriod, mIntHRPeriodCounts, mIntHRVpk;     ///jaufa, 180805, +
    public double mDoubleHrBloodHr, mDoubleHrBloodVpk, mDoubleHrBloodVti, mDoubleHrBloodSV, mDoubleHrBloodCO, mDoubleHrBloodSuccessRatio;     ///jaufa, 181003, +
    public int mIntHRErrCode = 0;     ///jaufa, 181228, +
    public static final int BINARY_ERR_CODE_HR_UNSTABLE = 0x01;
    public static final int BINARY_ERR_CODE_HR_INVALID = 0x02;
    public static final int BINARY_ERR_CODE_VTI_UNSTABLE = 0x04;
    public static final int BINARY_ERR_CODE_VTI_INVALID = 0x08;
    public static final int BINARY_ERR_CODE_Vpk_SMALL = 0x10;
    public static final int BINARY_ERR_CODE_Vpk_INVALID = 0x20;
    public static final int BINARY_ERR_CODE_SNR_SMALL = 0x40;
    public static final int BINARY_ERR_CODE_LENGTH_INVALID = 0x80;
    public static final int BINARY_ERR_CODE_ELECTRICAL_INTEFFERENCE = 0x100;

    // jaufa, +, 181112, Create Vpk Table
    public double[] mDoubleVPKTheoreticalTable;
    public double[] mDoubleVPKExperimentalTable;

    public int[] mIntArrayMaxIdxByMovingAverage;
    public double[] mDoublePsdLogMovingAverage;
    private double[] mDoubleMaxIdxLowWindowData;
    public int mIntMaxIdxNextIdx;


    private int[] mIntMaxIdxBreakLowWindowData;
    private int mIntMaxIdxBreakLowWindowSize, mIntMaxIdxBreakLowWindowSizeCur, mIntMaxIdxBreakLowWindowIdx;

    private int mIntSignalNoiseBasesCnt;

    double[] mDoublemBVSpectrumValuesOneTime, mDoubleBVSpectrumIntegralOneTime;

    //*********************************************
    // For Debug
    //*********************************************
    double[] mDoubleSTFTOneTime;

    //*jaufa, +, 191015, SNR and Audio Amp be showed on Page when Trying Period
    private Methodoligies ma;
    private int mIntTryIdx;
    private double[] mDblsSpectrumSNR;
    private double[] mDblsAudioAMP;
    private double[] mDblsVelocityTable;
    private double[] mDblsVelocityPeak;
    private double mDblSpectrumSNR;
    private double mDblAudioAMP;
    private double mDblVelocityPeak;

    //*/

    public static int lastHR = -1;
    public static int isHRStableCount = 0;
    public static boolean isInverseFreq = false;
    public static int filterLevel = 4;
    public static boolean isFourthLevel = true;

    public BVSignalProcessorPart1() {
        try {
            // ECG
            mShortECGValues = new short[SystemConfig.mIntSystemMaxSubSegSize][SystemConfig.mIntEcgSegCnt];   // 14*125= 1750
            mShortECGFilteredValue = new short[SystemConfig.INT_ECG_DATA_MAX_SIZE];

            mDoubleBVSpectrumValues = new double[SystemConfig.mIntSystemMaxSubSegSize][SystemConfig.mIntSystemMaxTotalFreqSeqsCnt];
            mDoubleBVSpectrumIntegral = new double[SystemConfig.mIntSystemMaxSubSegSize][SystemConfig.mIntSystemMaxTotalFreqSeqsCnt];
            mDoubleBVSpectrumIntegralDebug = new double[SystemConfig.mIntSystemMaxTotalFreqSeqsCnt];
            //if(SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_0_SNSI_GM){
                mDoubleBVFreqValues = new double[SystemConfig.mIntSystemMaxTotalFreqSeqsCnt];
            /*}else {
                mDoubleBVFreqValues = new double[SystemConfig.mIntSystemMaxTotalFreqSeqsCnt];
            }*/
            mIntSubSegGainLevels = new int[SystemConfig.mIntSystemMaxSubSegSize];

            mIntArrayMaxIdx = new int[SystemConfig.mIntSystemMaxSubSegSize];
            mDoubleArraySignalPowerIdx = new double[SystemConfig.mIntSystemMaxSubSegSize]; //jaufa, +, 181102
            mIntArrayMaxIdx_Hr = new int[SystemConfig.mIntSystemMaxSubSegSize]; //jaufa, 180607, +
            mIntArrayMaxIdx_VPK = new int[SystemConfig.mIntSystemMaxSubSegSize]; //jaufa, 180607, +
            mIntArrayMaxIdx_Tri = new int[SystemConfig.mIntSystemMaxSubSegSize]; //jaufa, 180607, +
            mIntArrayMaxIdx_Period = new int[SystemConfig.mIntSystemMaxSubSegSize]; //jaufa, 180607, +
            mIntArrayHRPeakPosition = new int[SystemConfig.INT_HR_RESULT_IDX1_CNT];   //jaufa, 180805, +
            mIntArrayHRValleyPosition = new int[SystemConfig.INT_HR_RESULT_IDX1_CNT];   //jaufa, 180805, +
            mIntArrayHRStartPosition = new int[SystemConfig.INT_HR_RESULT_IDX1_CNT];   //jaufa, 181009, +
            mIntArrayHREndPosition = new int[SystemConfig.INT_HR_RESULT_IDX1_CNT];   //jaufa, 181009, +
            mIntArrayVPKPeakPosition = new int[SystemConfig.INT_HR_RESULT_IDX1_CNT];   //jaufa, 180805, +
            mIntArrayVTIStartPosition = new int[SystemConfig.INT_HR_RESULT_IDX1_CNT];   //jaufa, 180805, +
            mIntArrayVTIEndPosition = new int[SystemConfig.INT_HR_RESULT_IDX1_CNT];   //jaufa, 180805, +

			//*jaufa, 181102, +, 180805, +
            mIntHRCounts=0; mIntHRPeriod=0; mIntHRVpk=0;
            mDoubleHrBloodHr=0; mDoubleHrBloodVpk=0; mDoubleHrBloodVti=0; mDoubleHrBloodSV=0; mDoubleHrBloodCO=0; mDoubleHrBloodSuccessRatio=0;
            mDoubleVPKTheoreticalTable = new double[256];
            mDoubleVPKExperimentalTable = new double[256];
            //*/

            mIntArrayMaxIdxByMovingAverage = new int[SystemConfig.mIntSystemMaxSubSegSize];
            mDoublePsdLogMovingAverage = new double[SystemConfig.mIntSystemMaxSubSegSize];
            mDoubleMaxIdxLowWindowData = new double[SystemConfig.mIntSystemMaxMaxIdxWindowSize];

            mDoubleAmpPsdMaxNormPeriod = new double[SystemConfig.mIntSystemMaxSubSegSize];

            //*jaufa, +, 191015, SNR and Audio Amp be showed on Page when Trying Period
            ma = new Methodoligies();
            mIntTryIdx = 0;
            mDblsSpectrumSNR = new double[64];
            mDblsAudioAMP = new double[64];
            mDblsVelocityPeak = new double[64];
            mDblsVelocityTable = ma.getVelocityCalculateTable();
            mDblSpectrumSNR = 0;
            mDblAudioAMP = 0;
            mDblVelocityPeak = 0;
            //*/


            // ECG
            mEcgList = new ArrayList<>();
            butterworthHigh.highPass(4,500,0.01);
            butterworthLow.lowPass(4,500,150);
            butterworth.bandStop(4,500,60,2);

            // Ultrasound filter
//            butterworthUSLow.lowPass(16,8000,3600);
//            butterworthUSHigh.highPass(16,8000,400);
//            if (isFourthLevel){
//                butterworthUSHigh.highPass(4,8000,400);
//                butterworthUSLow.lowPass(4,8000,3600);
//            }else{
//                butterworthUSHigh.highPass(16,8000,400);
//                butterworthUSLow.lowPass(16,8000,3600);
//            }
            initButterworthFilterForUS();

            butterworthUSBS.bandStop(4,8000,200,200);
            chebyshevUSHigh.highPass(4,8000,150,5);
        } catch (Exception ex1) {
            ex1.printStackTrace();
        }
    }

    public void initButterworthFilterForUS(){
        butterworthUSLow = new Butterworth();
        butterworthUSHigh = new Butterworth();
        if (isFourthLevel){
            butterworthUSHigh.highPass(4,8000,400);
            butterworthUSLow.lowPass(4,8000,3600);
        }else{
            butterworthUSHigh.highPass(16,8000,400);
            butterworthUSLow.lowPass(64,8000,3600);
        }
    }

    public void prepareStart() {
        int iVar, iVar2;

        try {

            SystemConfig.mDoubleNoiseRangeWu = 0;
            SystemConfig.mDoubleNoiseStrengthWu = 0;
            SystemConfig.mDoubleSignalStrengthMaxWu = 0;
            SystemConfig.mDoubleSignalMaxForLearnWuNew = 0;
            SystemConfig.mDoubleSignalMaxForAllWuNew = 0;
            SystemConfig.mDoubleSignalMaxForLearnAfterNormWuNew = 0;
            SystemConfig.mBoolSignalMaxForLearnWuNewOK = false;
            SystemConfig.mDoubleSignalMinForLearnWuNew = Double.POSITIVE_INFINITY;
            SystemConfig.mDoubleSignalMinForAllWuNew = Double.POSITIVE_INFINITY;
            SystemConfig.mDoubleSignalMinForLearnAfterNormWuNew = Double.POSITIVE_INFINITY;
            //SystemConfig.mDoubleSignalMinForAllAfterNormWuNew = Double.POSITIVE_INFINITY;

            mDoubleSTFTOneTime = new double[SystemConfig.mIntFreqIdxsMaxSize];

            //mIntPreMaxIdx = -1;
            //-------------------------------------------------
            //-- for Group Parameter
            //-------------------------------------------------

            mIntTotalSubSegMaxSize = SystemConfig.mIntUltrasoundSamplesMaxSizeForRun / (SystemConfig.mIntSTFTWindowShiftSize);
            mIntLeftSubSegSize = SystemConfig.mIntSTFTWindowSize / SystemConfig.mIntSTFTWindowShiftSize - 1;   //The SubSeg not processed and left to next segment

            mFloatTimeUnitMiniSec = ((float) (SystemConfig.mIntSTFTWindowShiftSize) * 1000) / (float) SystemConfig.mIntUltrasoundSamplerate;
            mIntTotalFreqSeqsCnt = SystemConfig.mIntSTFTWindowSize / 2 + 1;
            mDoubleFreqGap = (double) SystemConfig.mIntUltrasoundSamplerate / SystemConfig.mIntSTFTWindowSize;

            //--------------------------------------------------
            // for Blood Velocity Calculate
            //--------------------------------------------------

            mShortTimeFT = new MySTFT(SystemConfig.mIntSTFTWindowSize, SystemConfig.mIntUltrasoundSamplerate, SystemConfig.mIntSTFTWindowSize, SystemConfig.mStrSTFTWindowType);
            mShortFeedData = new short[SystemConfig.mIntSTFTWindowSize];

            for (iVar = 0; iVar < mIntTotalSubSegMaxSize; iVar++) {
                mIntSubSegGainLevels[iVar] = 0;
                mDoubleAmpPsdMaxNormPeriod[iVar] = 1;
                for (iVar2 = 0; iVar2 < mIntTotalFreqSeqsCnt; iVar2++) {
                    mDoubleBVSpectrumValues[iVar][iVar2] = 0;
                    mDoubleBVSpectrumIntegral[iVar][iVar2] = 0;
                }

                // Cavin move here for reduce for loop 2018/08/09
                mIntArrayMaxIdx[iVar] = 0;
                mIntArrayMaxIdxByMovingAverage[iVar] = 0;
                mDoublePsdLogMovingAverage[iVar] = 0;
            }

            //if(SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_0_SNSI_GM){
                for (iVar = 0; iVar < (mIntTotalFreqSeqsCnt); iVar++) {
                    mDoubleBVFreqValues[iVar] = iVar * mDoubleFreqGap;
                }
            /*}else {
                for (iVar = 0; iVar < mIntTotalFreqSeqsCnt; iVar++) {
                    mDoubleBVFreqValues[iVar] = iVar * mDoubleFreqGap;
                }
            }*/

            mIntSTFFTNextSubSegIdx = 0;
            mIntTrySTFFTNextSubSegIdx = 0;

            mBoolMaxIdxBaseLearned = false;
            mDoubleMaxIdxCompareBase = 0;

            //*********************************
            // for MaxIdx
            //*********************************
            mIntMaxIdxNextIdx = 0;
            /*for (iVar = 0; iVar < mIntTotalSubSegMaxSize; iVar++) {
                mIntArrayMaxIdx[iVar] = 0;
                mIntArrayMaxIdxByMovingAverage[iVar] = 0;
                mDoublePsdLogMovingAverage[iVar] = 0;
            }*/

            mIntAmpPsdNormPeriodSize = (int)((double)(SystemConfig.mIntUltrasoundSamplerate / SystemConfig.mIntSTFTWindowShiftSize) * DOUBLE_AMPPSD_NORMALIZE_PERIOD_SEC);
            mIntAmpPsdNormPeriodSizeCur = 0;
            mDoubleAmpPsdMaxPeriodFirst = Double.NEGATIVE_INFINITY;
            mDoubleAmpPsdMaxPeriod = Double.NEGATIVE_INFINITY;
            mDoubleAmpPsdMaxCur = Double.NEGATIVE_INFINITY;
            mDoubleAmpPsdMaxTotal = Double.NEGATIVE_INFINITY;

            mDoublemBVSpectrumValuesOneTime = new double[mIntTotalFreqSeqsCnt];
            mDoubleBVSpectrumIntegralOneTime = new double[mIntTotalFreqSeqsCnt];

            //mDoublePSDLogMin = Double.POSITIVE_INFINITY;
            //*********************************
            // for Others
            //*********************************
            SystemConfig.mIntStartIdxNoiseLearn = (int) (((double) SystemConfig.mIntUltrasoundSamplerate * SystemConfig.DOUBLE_NOISE_SIGNAL_LEARN_START_SEC)
                    / (double) SystemConfig.mIntSTFTWindowShiftSize) + 1;
            SystemConfig.mIntEndIdxNoiseLearn = (int) (((double) SystemConfig.mIntUltrasoundSamplerate * SystemConfig.DOUBLE_NOISE_SIGNAL_LEARN_END_SEC)
                    / (double) SystemConfig.mIntSTFTWindowShiftSize);

            if ((SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_BE_ONLINE)
                    || (SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_LE_ONLINE)
                    || (SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_OFFLINE)) {
                mIntMaxIdxBreakLowWindowData = new int[SystemConfig.INT_MAXIDX_BREAK_LOW_WINDOWS_SIZE_DEFAULT_ITRI * 2];
            } else if (SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.USCOM_8K) {
                mIntMaxIdxBreakLowWindowData = new int[SystemConfig.INT_MAXIDX_BREAK_LOW_WINDOWS_SIZE_DEFAULT_8K_USCOM * 2];
            }
            mIntMaxIdxBreakLowWindowIdx = 0;
            mIntMaxIdxBreakLowWindowSizeCur = 0;

            //*jaufa, +, 181102
            for (int _var = 0; _var < 256; _var++){
                mDoubleVPKTheoreticalTable[_var] = fVPKTheoreticalTable(_var);
                mDoubleVPKExperimentalTable[_var] = fVPKExperimentalTable((double) _var/100.0);
            }
            //*/

            ecgThreshold = 0;
            mdf_ECG = 0;
            ecgRC = 0;
            ecgSW = true;
            // ECG
            mEcgList.clear();

            // For HR update test
            isHRStableCount = 0;
            lastHR=-1;
        } catch (Exception ex1) {
            Log.i("BloodVelSignalProcessor", "prepareStart: ");
            ex1.printStackTrace();
        }
    }

    public void prepareAfterLearned() {
         mIntMaxIdxNextIdx = mIntSTFFTNextSubSegIdx;

        if (SystemConfig.mIntPart2TestIdx == -1) {
            for (int iVar = 0; iVar < SystemConfig.INT_HR_GROUP_CNT; iVar++) {
                MainActivity.mBVSignalProcessorPart2Array[iVar].prepareAfterLearned();
            }
        } else {
            MainActivity.mBVSignalProcessorPart2Array[SystemConfig.mIntPart2TestIdx].prepareAfterLearned();
        }

    }

    public int getOnlineHR(int samples){
        int HR;
        double[][] tmpSpectrogramValues = new double[129][samples];
        for (int i=samples-1;i>=0;i--){
            for (int j=0;j<129;j++){
                tmpSpectrogramValues[j][samples-1-i] = mDoubleBVSpectrumValues[mIntSTFFTNextSubSegIdx-i][j];
            }
        }

        int width=tmpSpectrogramValues[0].length;
        for (int i=0;i<3;i++) {
            for (int j = 0; j < width; j++) {
                tmpSpectrogramValues[i][j] = 0;
            }
        }

        double [][] o_im5 = Doppler.normal2(tmpSpectrogramValues);
        HR = Doppler.getHRByVFSNSI(o_im5);

        Log.d("BVSP1","HR = "+HR);
        return HR;
    }

    //*************************************************************
    // Inverse FFT output frequency
    //**************************************************************
    private double[] flipFFT(double[] inputArray){
        double[] outputArray = new double[inputArray.length];
        System.arraycopy(inputArray,0,outputArray,0,inputArray.length);
        double temp;
        // swap 0 and 255
        temp=outputArray[0];
        outputArray[0]=outputArray[255];
        outputArray[255]=temp;

        // swap 1~254  ex. [1]+[2]i, [3]+[4]i...,[253]+[254]i
        for (int i=1;i<outputArray.length/2;i+=2){
            temp = outputArray[i];
            outputArray[i]=outputArray[outputArray.length-2-i];
            outputArray[outputArray.length-2-i]=temp;

            temp = outputArray[i+1];
            outputArray[i+1]=outputArray[outputArray.length-1-i];
            outputArray[outputArray.length-1-i]=temp;
        }
        return outputArray;
    }
    //*************************************************************
    // Rewrite inverse frequency offline wave data.
    //**************************************************************
    private void catWaveDataOffline(double[] inputArray, int offset){
        for (int i = 0; i < SystemConfig.mIntSTFTWindowSize; i++) {
            short tmpShortData = ((short)(inputArray[i]*128));
            if (mIntSTFFTNextSubSegIdx+i*2+1<MainActivity.mRawDataProcessor.mByteArrayWavDataOffLine.length){
                MainActivity.mRawDataProcessor.mByteArrayWavDataOffLine[(offset+i)*2] = (byte)(tmpShortData & 0xff);
                MainActivity.mRawDataProcessor.mByteArrayWavDataOffLine[(offset+i)*2+1] = (byte)(tmpShortData>> 8 & 0xff);
            }
        }
    }


    //*************************************************************
    // use one window to find the heart rate signal based on Peak Velocity
    //**************************************************************
    public void processSegment() {
        int iNextDataIdx, iNextDataStartIdx;
        int iVar, iVar2, iSubSegSize, iCurTotalSubSegSize;
        double[] doubleData;
        int iNextDataIdxForGain, iGainLevelMax, iGainLevelMin, iGainLevelCur;
        double doubleGainValueRatio;

        //---------------------------------
        // for Blood Velocity  Calculate
        //----------------------------------
        iCurTotalSubSegSize = (MainActivity.mRawDataProcessor.getUltrasoundCurrSamplesSize()
                - SystemConfig.mIntSTFTWindowSize + SystemConfig.mIntSTFTWindowShiftSize) / SystemConfig.mIntSTFTWindowShiftSize;
        if (SystemConfig.mEnumTryState != SystemConfig.ENUM_TRY_STATE.STATE_TRY) {
            iSubSegSize = iCurTotalSubSegSize - mIntLeftSubSegSize - mIntSTFFTNextSubSegIdx;
            iNextDataStartIdx = mIntSTFFTNextSubSegIdx * SystemConfig.mIntSTFTWindowShiftSize;
        } else {
            iSubSegSize = iCurTotalSubSegSize - mIntLeftSubSegSize - mIntTrySTFFTNextSubSegIdx;
            iNextDataStartIdx = mIntTrySTFFTNextSubSegIdx * SystemConfig.mIntSTFTWindowShiftSize;
        }

        try {
            //----------------------------------------------------------------
            //----- processShortTimeFFT
            //----------------------------------------------------------------
            for (iVar = 0; iVar < iSubSegSize; iVar++) {
                iNextDataIdx = iNextDataStartIdx + iVar * SystemConfig.mIntSTFTWindowShiftSize;
                iNextDataIdx = iNextDataIdx % SystemConfig.mIntUltrasoundSamplesMaxSizeForRun;
                iNextDataIdxForGain = iNextDataIdx;
                iGainLevelMin = Integer.MAX_VALUE;
                iGainLevelMax = Integer.MIN_VALUE;
                //--- 做shortTimeFT 之Data, 其Gain 值可能不同，先找到Data的Gain 值 -----
                for (iVar2 = 0; iVar2 < SystemConfig.mIntSTFTWindowSize; iVar2++) {
                    iGainLevelCur = MainActivity.mRawDataProcessor.mIntUltrasoundDataGainLevel[iNextDataIdxForGain];
                    if (iGainLevelMin > iGainLevelCur) {
                        iGainLevelMin = MainActivity.mRawDataProcessor.mIntUltrasoundDataGainLevel[iNextDataIdxForGain];
                    }
                    if (iGainLevelMax < MainActivity.mRawDataProcessor.mIntUltrasoundDataGainLevel[iNextDataIdxForGain]) {
                        iGainLevelMax = MainActivity.mRawDataProcessor.mIntUltrasoundDataGainLevel[iNextDataIdxForGain];
                    }
                    iNextDataIdxForGain++;
                    if (iNextDataIdxForGain == SystemConfig.mIntUltrasoundSamplesMaxSizeForRun) {
                        iNextDataIdxForGain = 0;
                    }
                }

                doubleGainValueRatio = (double) SystemConfig.mIntGainLevelMapVer1[iGainLevelMax] / (double) SystemConfig.mIntGainLevelMapVer1[iGainLevelMin];
//                Log.d("spp1","doubleGainValueRatio = " + doubleGainValueRatio);

                //--- 將所有要做shortTimeFT 之Data, 將其Gain 值需調到相同, 一律調到最大值 -----
                for (iVar2 = 0; iVar2 < SystemConfig.mIntSTFTWindowSize; iVar2++) {
                    if (MainActivity.mRawDataProcessor.mIntUltrasoundDataGainLevel[iNextDataIdx] == iGainLevelMax) {
                        mShortFeedData[iVar2] = MainActivity.mRawDataProcessor.mShortUltrasoundData[iNextDataIdx];
                    } else {
                        mShortFeedData[iVar2] = (short) ((double) MainActivity.mRawDataProcessor.mShortUltrasoundData[iNextDataIdx]
                                * doubleGainValueRatio);
                    }
                    iNextDataIdx++;
                    // if(SystemConfig.mEnumTryState == SystemConfig.ENUM_TRY_STATE.STATE_TRY)
                    if (iNextDataIdx == SystemConfig.mIntUltrasoundSamplesMaxSizeForRun) {
                        iNextDataIdx = 0;
                    }
                }
                //--- 開始做shortTimeFT ----------
                mShortTimeFT.feedData(mShortFeedData);

                doubleData = mShortTimeFT.getSpectrumAmpOutArrayElement(0);
//                Log.d("BVSPP1","doubleData.length ="+doubleData.length);

                if (BVSignalProcessorPart1.isInverseFreq){
                    double[] tmpDoubleData1 = mShortTimeFT.getSpectrumAmpInTmpValue();
//                    if (mIntSTFFTNextSubSegIdx==0){
//                        for (int i=0;i<tmpDoubleData.length;i++){
//                            Log.d("BVSPP1","tmpDoubleData["+i+"]= "+tmpDoubleData[i]+", mShortFeedData["+i+"]= "+mShortFeedData[i]);
//                        }
//                    }

                    double[] tmpDoubleData = flipFFT(tmpDoubleData1);
                    // DC offset
//                    tmpDoubleData[0] = 0;

                    mShortTimeFT.feedBData(tmpDoubleData);
//                    if (mIntSTFFTNextSubSegIdx==0||mIntSTFFTNextSubSegIdx==1){
//                        for (int i=0;i<tmpDoubleData.length;i++){
//                            Log.d("BVSPP1","tmpDoubleData["+i+"]= "+(short)(tmpDoubleData[i]*128)+", mShortFeedData["+i+"]= "+mShortFeedData[i]);
//                        }
//                    }
                    iNextDataIdx = iNextDataStartIdx + iVar * SystemConfig.mIntSTFTWindowShiftSize;
                    iNextDataIdx = iNextDataIdx % SystemConfig.mIntUltrasoundSamplesMaxSizeForRun;
                    if (!MainActivity.currentFragmentTag.equals(Constants.ITRI_ULTRASOUND_FRAGMENT_TAG)){
                        // write inverse data to wave data
                        catWaveDataOffline(tmpDoubleData,iNextDataIdx);
//                        iNextDataIdx++;
//                        // if(SystemConfig.mEnumTryState == SystemConfig.ENUM_TRY_STATE.STATE_TRY)
//                        if (iNextDataIdx == SystemConfig.mIntUltrasoundSamplesMaxSizeForRun) {
//                            iNextDataIdx = 0;
//                        }
                    }else{
                        for (int i=0;i<tmpDoubleData.length;i++){
                            MainActivity.mRawDataProcessor.mShortUltrasoundDataInverse[iNextDataIdx+i] = (short)(tmpDoubleData[i]*128);
                        }
                        MainActivity.mAudioPlayerController.putMsgForAudioSegmentOnLine(MainActivity.mRawDataProcessor.mIntDataNextIndex-1);
                    }
                }

                if (SystemConfig.isHeartIO2 || (MainActivity.offFrag!=null && MainActivity.offFrag.hasECG)){
                    for (int k=0;k<4;k++){
                        mShortECGFilteredValue[mIntSTFFTNextSubSegIdx*4+k] = (short)butterworth.filter(MainActivity.mRawDataProcessor.mShortEcgData[mIntSTFFTNextSubSegIdx*4+k]);
                        mShortECGFilteredValue[mIntSTFFTNextSubSegIdx*4+k] = (short)butterworthLow.filter(mShortECGFilteredValue[mIntSTFFTNextSubSegIdx*4+k]);

                    }
                    // ECG data copy
//                    System.arraycopy(MainActivity.mRawDataProcessor.mShortEcgData, mIntSTFFTNextSubSegIdx * 4, mShortECGValues[mIntSTFFTNextSubSegIdx], 0, 4);
                    System.arraycopy(mShortECGFilteredValue, mIntSTFFTNextSubSegIdx * 4, mShortECGValues[mIntSTFFTNextSubSegIdx], 0, 4);
                    for (int k=0;k<4;k++){
                        mShortECGFilteredValue[mIntSTFFTNextSubSegIdx*4+k] = (short)butterworthHigh.filter(mShortECGFilteredValue[mIntSTFFTNextSubSegIdx*4+k]);
                        //MainActivity.mRawDataProcessor.mShortEcgData[mIntSTFFTNextSubSegIdx*4+k]= (short)butterworth.filter(MainActivity.mRawDataProcessor.mShortEcgData[mIntSTFFTNextSubSegIdx*4+k]);
                    }

                    processEcgRPeak(mIntSTFFTNextSubSegIdx);

                }

                if (SystemConfig.mTestMode){
                    //* jaufa, +, 191015, Calculate One frame SNR & AudioAmp for Half Sec by 64 elements

                    double ampH = ma.getArrStrengthRangeMean(Type.toDbl(mShortFeedData, 0, mShortFeedData.length-1)
                            , 0.9, 0.98);
                    double ampL = ma.getArrStrengthRangeMean(Type.toDbl(mShortFeedData, 0, mShortFeedData.length-1)
                            , 0.02, 0.1);

                    mDblsAudioAMP[mIntTryIdx] = ampH - ampL;
                    mDblAudioAMP = ma.getArrMean(mDblsAudioAMP, 0 , mDblsAudioAMP.length -1);

                    double signal = ma.getArrStrengthRangeMean(doubleData, 0.9, 0.98);
//                    double noise = ma.getArrStrengthRangeMean(doubleData, 0.02, 0.1);
                    double noise = ma.getArrMean(doubleData, 112, 128);
                    mDblsSpectrumSNR[mIntTryIdx] = signal / noise;
                    mDblSpectrumSNR = ma.getArrMean(mDblsSpectrumSNR, 0 , mDblsSpectrumSNR.length -1);

                    double fVpk = 32 * noise;
                    for(int i = doubleData.length -1 ; i > 0; i--){
                        if((doubleData[i] > fVpk) && (doubleData[i-1] > fVpk)){
                            fVpk = i;
                            break;
                        }
                    }

                    if( fVpk >= 16) {
                        mDblsVelocityPeak[mIntTryIdx] = ma.getArrStrengthGM(doubleData, 2, doubleData.length - 1)[Tag.mIntX];
                        if (mDblsVelocityPeak[mIntTryIdx] > 10 && mDblsVelocityPeak[mIntTryIdx] < 104) {
                            mDblsVelocityPeak[mIntTryIdx] = ma.getArrStrengthGM(doubleData
                                    , (int) mDblsVelocityPeak[mIntTryIdx] - 2, (int) (mDblsVelocityPeak[mIntTryIdx] + 24))[Tag.mIntX];
                        }
                    }else{
                        mDblsVelocityPeak[mIntTryIdx] = fVpk;
                    }
                    mDblVelocityPeak = ma.getArrStrengthRangeMean(mDblsVelocityPeak, 0.2 , 0.8);
                    mDblVelocityPeak = ma.getVelocity(mDblsVelocityTable, (int) mDblVelocityPeak);



                    mIntTryIdx ++;
                    mIntTryIdx %= 64;
                    if ((mIntTryIdx % 8) == 0) {
                        if (MainActivity.currentFragmentTag.equals(Constants.ITRI_ULTRASOUND_FRAGMENT_TAG)){
                            MainActivity.oFrag.updateSNRAmpValue((int)mDblSpectrumSNR, mDblVelocityPeak, (int) (mDblAudioAMP/8));
//                        MainActivity.oFrag.updateSNRAmpValue((int) (mDblSpectrumSNR), (int) (mDblAudioAMP/8));
                        }
                    }

                }
                // Real time update HR.
                if (MainActivity.currentFragmentTag.equals(Constants.ITRI_ULTRASOUND_FRAGMENT_TAG)){
                    int samples=125*6;
                    int updateSamples=125;
                    if (mIntSTFFTNextSubSegIdx>samples && mIntSTFFTNextSubSegIdx%updateSamples==0){
                        int tmpHR = getOnlineHR(samples);
                        if (lastHR!=-1){
                            int hrDiff = Math.abs(tmpHR-lastHR);
                            if (hrDiff<0.1*lastHR){ //判斷是否穩定
                            /* 更新HR穩定標準 2023/02/24 by Doris */
//                            if (hrDiff<15){
                                isHRStableCount++;
                            }else{
                                isHRStableCount=0;
                                Log.d("BVSP1","HR stable count clear.");
                            }
                            if (isHRStableCount>=3){ //連續3次穩定
                                Log.d("BVSP1","HR stable!!!!!!!!!!!");
                            }
                        }
                        MainActivity.oFrag.updateHRValue(tmpHR,(isHRStableCount>=3));
                        lastHR = tmpHR;
                    }
                }


                for (iVar2 = 0; iVar2 < mIntTotalFreqSeqsCnt; iVar2++) {
                    double tmpDoubleData;
                    if (isInverseFreq){
                        tmpDoubleData = doubleData[mIntTotalFreqSeqsCnt-iVar2-1];
                    }else{
                        tmpDoubleData = doubleData[iVar2]; //isInverseFreq = false
                    }
                    mDoubleSTFTOneTime[iVar2] = tmpDoubleData;
                    if (SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_0_SNSI_GM) {
                        //mDoubleBVSpectrumValues[mIntSTFFTNextSubSegIdx][iVar2] = tmpDoubleData * tmpDoubleData
                        //        / (double) SystemConfig.mIntUltrasoundSamplerate / (double) SystemConfig.mIntSTFTWindowSize;
                      //* jaufa, 180809, #
                        mDoubleBVSpectrumValues[mIntSTFFTNextSubSegIdx][iVar2] = tmpDoubleData;
//                        Log.d("mDoubleBVSpectrumValues", String.valueOf(tmpDoubleData));
                      //* jaufa, 180809, # */

                    }else if (SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_1_WU_NEW) {
                        mDoubleBVSpectrumValues[mIntSTFFTNextSubSegIdx][iVar2] = tmpDoubleData * tmpDoubleData
                                / (double) SystemConfig.mIntUltrasoundSamplerate / (double) SystemConfig.mIntSTFTWindowSize;
                        if (SystemConfig.mBoolSignalMaxForLearnWuNewOK){
                            if(SystemConfig.mDoubleSignalMaxForAllWuNew < mDoubleBVSpectrumValues[mIntSTFFTNextSubSegIdx][iVar2]){
                                SystemConfig.mDoubleSignalMaxForAllWuNew = mDoubleBVSpectrumValues[mIntSTFFTNextSubSegIdx][iVar2];
                            }
                            if(SystemConfig.mDoubleSignalMinForAllWuNew > mDoubleBVSpectrumValues[mIntSTFFTNextSubSegIdx][iVar2]){
                                SystemConfig.mDoubleSignalMinForAllWuNew = mDoubleBVSpectrumValues[mIntSTFFTNextSubSegIdx][iVar2];
                            }
                            mDoubleBVSpectrumValues[mIntSTFFTNextSubSegIdx][iVar2] = mDoubleBVSpectrumValues[mIntSTFFTNextSubSegIdx][iVar2] / SystemConfig.mDoubleSignalMaxForLearnWuNew;
                        }else if(mIntSTFFTNextSubSegIdx >= SystemConfig.mIntStartIdxNoiseLearn){
                            if(SystemConfig.mDoubleSignalMaxForLearnWuNew < mDoubleBVSpectrumValues[mIntSTFFTNextSubSegIdx][iVar2]){
                                SystemConfig.mDoubleSignalMaxForLearnWuNew = mDoubleBVSpectrumValues[mIntSTFFTNextSubSegIdx][iVar2];
                            }
                            if (SystemConfig.mDoubleSignalMinForLearnWuNew > mDoubleBVSpectrumValues[mIntSTFFTNextSubSegIdx][iVar2]) {
                                SystemConfig.mDoubleSignalMinForLearnWuNew = mDoubleBVSpectrumValues[mIntSTFFTNextSubSegIdx][iVar2];
                            }
                        }
                    }else if (SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_2_SNR_AMP) {
                        mDoubleBVSpectrumValues[mIntSTFFTNextSubSegIdx][iVar2] = tmpDoubleData;
                    } else {  //if (SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_3_SNR_POWER) {
                        mDoubleBVSpectrumValues[mIntSTFFTNextSubSegIdx][iVar2] = tmpDoubleData * tmpDoubleData;
                    }
                }

                if(MainActivity.mRawDataProcessor.mBoolDCOffsetLearned) {
                    for (iVar2 = 0; iVar2 < mIntTotalFreqSeqsCnt; iVar2++) {
                        if (mDoubleAmpPsdMaxTotal < mDoubleBVSpectrumValues[mIntSTFFTNextSubSegIdx][iVar2]) {
                            mDoubleAmpPsdMaxTotal = mDoubleBVSpectrumValues[mIntSTFFTNextSubSegIdx][iVar2];
                        }
                        if (mDoubleAmpPsdMaxCur < mDoubleBVSpectrumValues[mIntSTFFTNextSubSegIdx][iVar2]) {
                            mDoubleAmpPsdMaxCur = mDoubleBVSpectrumValues[mIntSTFFTNextSubSegIdx][iVar2];
                        }
                    }
                    mIntAmpPsdNormPeriodSizeCur++;
                    if (mIntAmpPsdNormPeriodSizeCur == mIntAmpPsdNormPeriodSize) {
                        mDoubleAmpPsdMaxPeriod = mDoubleAmpPsdMaxCur;
                        if (mDoubleAmpPsdMaxPeriodFirst == Double.NEGATIVE_INFINITY) {
                            mDoubleAmpPsdMaxPeriodFirst = mDoubleAmpPsdMaxCur;
                        }
                        mDoubleAmpPsdMaxCur = Double.NEGATIVE_INFINITY;
                        mIntAmpPsdNormPeriodSizeCur = 0;
                    }
                    if (mDoubleAmpPsdMaxPeriod > 0) {
                        mDoubleAmpPsdMaxNormPeriod[mIntSTFFTNextSubSegIdx] = mDoubleAmpPsdMaxPeriod;
                    }
                }

                mIntSubSegGainLevels[mIntSTFFTNextSubSegIdx] = iGainLevelMax;

                mIntTrySTFFTNextSubSegIdx++;
                mIntSTFFTNextSubSegIdx++;
                if (mIntSTFFTNextSubSegIdx == mIntTotalSubSegMaxSize) {
                    mIntSTFFTNextSubSegIdx = 0;
                }
            }

            //----------------------------------------------------------------
            //----- process NoiseAndSignalBase & MaxIdx
            //----------------------------------------------------------------
            if (!mBoolMaxIdxBaseLearned) {
                if (mIntSTFFTNextSubSegIdx < (SystemConfig.mIntEndIdxNoiseLearn+1)) {
//                    Log.d("mBoolMaxIdxBaseLearned", "mBoolMaxIdxBaseLearned");
//                    會執行這個return
                    return;
                } else {
                    if(SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_0_SNSI_GM){
                        processNoiseAndSignalByWu();    //--- for Algorithm SNSI_GM
                        //*j+,0524  SNR
                        processNoiseAndSignalBaseAndMaxIdxLevel();    //--- for Algorithm SNR_AMP & SNR_PSD
                        //*j+,0524  SNR */

                    }else if(SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_1_WU_NEW){
                        processNormalizeForLearnDataByWuNew();    //--- for Algorithm WU_NEW
                    }else {
                        processNoiseAndSignalBaseAndMaxIdxLevel();    //--- for Algorithm SNR_AMP & SNR_PSD
                    }
                    mBoolMaxIdxBaseLearned = true;
                }
            }
        } catch (Exception ex1) {
            //SystemConfig.mMyEventLogger.appendDebugStr("BVSigProcPart1.procSegCommon.Exception", "");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(), "");
            ex1.printStackTrace();
        }
    }

    //----------------------------------------------------------------
    //------Process ECG R peak
    //-----------------------------------------------------------------
    int ringbuff = 1000;   // 2 seconds
    double[] thres_array = new double[ringbuff];
    private final int[] x_derv = new int[4];
    public int Derivative(int data){
        int yd = (data << 1) + x_derv[3] - x_derv[1] - (x_derv[0] << 1);
        yd = yd >> 3;
        for (int i=0;i<3;i++){
            x_derv[i] = x_derv[i+1];
        }
        x_derv[3] = data;
        return yd;
    }

    private int ptr = 0;
    private long sum;
    private final int[] xm = new int[32];

    public double MovingWindowIntegral(int data){
        double ym;
        if (++ptr == 32){
            ptr = 0;
        }
        sum -= xm[ptr];
        sum += data;
        xm[ptr] = data;
        long ly = sum >> 5;

        if (ly > 32400){
            ym = 32400;
        }else{
            ym = ly;
        }
        return (ym);
    }

    int last_data;
    int last_x;
    private void processEcgRPeak(int index){
        for (int j=0;j<4;j++){
            if ((index * 4+j) % ringbuff == 0){
                for (int i=0;i<ringbuff;i++){
                    ecgThreshold = ecgThreshold + thres_array[i];
                }
                ecgThreshold = 1.5 * (ecgThreshold / (double)ringbuff);
            }
//            int ECG = MainActivity.mRawDataProcessor.mShortEcgData[index*4+j];
            int ECG = mShortECGFilteredValue[index*4+j];
            int scal_x = index*4+j;

//            mdf_ECG = Derivative(MainActivity.mRawDataProcessor.mShortEcgData[index*4+j]);
            mdf_ECG = Derivative(mShortECGFilteredValue[index*4+j]);
            mdf_ECG = Math.pow(mdf_ECG,2);
            mdf_ECG = MovingWindowIntegral((int)mdf_ECG);

            thres_array[(index*4+j)%ringbuff] = mdf_ECG;

            if (index * 4 > ringbuff){
                if (mdf_ECG >= ecgThreshold && ecgSW){
//                                Log.d("BVSP1","mdf_ECG = "+mdf_ECG);
//                                Log.d("BVSP1","ecgThreshold = "+ecgThreshold);
                    ecgRC++;
                    if (ecgRC==3){
                        ecgRC = 0;
                        ecgSW = false;
                        last_data = ECG;
                        last_x = scal_x;
                    }
                }else if (mdf_ECG < ecgThreshold && ecgRC < 3 && ecgSW){
                    ecgRC = 0;
                }else if (mdf_ECG > ecgThreshold && !ecgSW) {
                    if (last_data < ECG) {
                        last_data = ECG;
                        last_x = scal_x;
                    }
                }else if (mdf_ECG <= ecgThreshold && !ecgSW) {

                    if (last_data < ECG) {
                        last_data = ECG;
                        last_x = scal_x;
                    }
                    if (ecgRC == 16) {
                        ecgRC = 0;
                        ecgSW = true;
//                                    RRsecond = last_scal_x;
//                                    RS = last_data;
//                        Log.d("BVSP1","R index = "+last_x);
                        ecgResult tmpEcg = new ecgResult();
                        tmpEcg.setRPeakIndex(last_x);
                        mEcgList.add(tmpEcg);
                        last_data = 0;
                        last_x = 0;
                        //Log.d("BVSP1","mEcgList.size()="+mEcgList.size());
                        //po.WriteLine(Convert.ToString(last_scal_x) + '\t' + Convert.ToString(last_data));
                            /*
                              RP.WriteLine(Convert.ToString(last_scal_x) + '\t' + Convert.ToString(last_data));//record ECG Rx data
                              RP.Close();
                              RP = new StreamWriter((File.Open(saveFileDialog3.FileName, FileMode.Append)), System.Text.Encoding.UTF8);
                              */
//                           rrsw = true;
                    }
                    ecgRC++;
                }
            }
        }
    }

    //----------------------------------------------------------------
    //----- process Noise and Signal Learning
    //----------------------------------------------------------------
    private void processNoiseAndSignalBaseAndMaxIdxLevel() {
        int iSignalLearnFreqEndIdx, iMaxIdxsLearnedCnt;
        int[] iMaxIdxsLearned;
        int iMaxIdxsLearnNextIdx;

        processNoiseAndSignalBaseByHuang();

        if (SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_2_SNR_AMP) {
            mDoubleMaxIdxCompareBaseRatio = Math.sqrt(SystemConfig.mDoubleSNRLearned);
            if (mDoubleMaxIdxCompareBaseRatio > SystemConfig.DOUBLE_MAXIDX_REFERENCE_FROM_SNR_BY_NOISE_MULTI) {
                mDoubleMaxIdxCompareBaseRatio = SystemConfig.DOUBLE_MAXIDX_REFERENCE_FROM_SNR_BY_NOISE_MULTI
                        + ((mDoubleMaxIdxCompareBaseRatio - SystemConfig.DOUBLE_MAXIDX_REFERENCE_FROM_SNR_BY_NOISE_MULTI) / SystemConfig.DOUBLE_MAXIDX_REFERENCE_FROM_SNR_BY_DEVIDED_RATIO);
            }
        } else if (SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_3_SNR_POWER) {
            mDoubleMaxIdxCompareBaseRatio = SystemConfig.mDoubleSNRLearned;
            if (mDoubleMaxIdxCompareBaseRatio > (SystemConfig.DOUBLE_MAXIDX_REFERENCE_FROM_SNR_BY_NOISE_MULTI * SystemConfig.DOUBLE_MAXIDX_REFERENCE_FROM_SNR_BY_NOISE_MULTI)) {
                mDoubleMaxIdxCompareBaseRatio =(SystemConfig.DOUBLE_MAXIDX_REFERENCE_FROM_SNR_BY_NOISE_MULTI * SystemConfig.DOUBLE_MAXIDX_REFERENCE_FROM_SNR_BY_NOISE_MULTI)
                        + ((mDoubleMaxIdxCompareBaseRatio - (SystemConfig.DOUBLE_MAXIDX_REFERENCE_FROM_SNR_BY_NOISE_MULTI * SystemConfig.DOUBLE_MAXIDX_REFERENCE_FROM_SNR_BY_NOISE_MULTI))
                        / (SystemConfig.DOUBLE_MAXIDX_REFERENCE_FROM_SNR_BY_DEVIDED_RATIO * SystemConfig.DOUBLE_MAXIDX_REFERENCE_FROM_SNR_BY_DEVIDED_RATIO));
            }
        }
        //*j+,0524 SNRLearned
        else {
            mDoubleMaxIdxCompareBaseRatio = SystemConfig.mDoubleSNRLearned;
        }
        //*j+,0524 SNRLearned  */

        mDoubleMaxIdxCompareBase = mDoubleMaxIdxCompareBaseRatio * SystemConfig.mDoubleNoiseBaseLearned;

        //SystemConfig.mMyEventLogger.appendDebugStr(" mDoubleMaxIdxCompareBaseRatio=", String.format("%.2f", mDoubleMaxIdxCompareBaseRatio));

        iMaxIdxsLearnNextIdx = SystemConfig.mIntStartIdxNoiseLearn;
        iMaxIdxsLearnedCnt = mIntSTFFTNextSubSegIdx - iMaxIdxsLearnNextIdx;
        iMaxIdxsLearned = new int[iMaxIdxsLearnedCnt];
        while (iMaxIdxsLearnNextIdx < mIntSTFFTNextSubSegIdx) {
            iMaxIdxsLearned[iMaxIdxsLearnNextIdx - SystemConfig.mIntStartIdxNoiseLearn] = processOneMaxIdxBySNR(iMaxIdxsLearnNextIdx);
            iMaxIdxsLearnNextIdx++;
        }
        Arrays.sort(iMaxIdxsLearned);
        iSignalLearnFreqEndIdx = iMaxIdxsLearned[iMaxIdxsLearnedCnt - 1];

        mDoubleLearnFreqEndIdxRatio = (double) iSignalLearnFreqEndIdx / (double) (mIntTotalFreqSeqsCnt - 1);

        //SystemConfig.mMyEventLogger.appendDebugStr("LearnFreqEndIdxRatio=", String.format("%.2f", mDoubleLearnFreqEndIdxRatio));

        prepareAfterLearned();
    }


    private void processNoiseAndSignalBaseByHuang() {
        int iStartIdxLowPart, iEndIdxLowPart, iStartIdxHighPart, iEndIdxHighPart, iLearnEndIdx;
        int iDataCntLowPart, iDataCntHighPart;
        int iDataHighPartSignalIdx;
        int iNoiseLowPartNoiseIdx, iNoiseHighPartNoiseIdx, iNoiseHighPartNoiseIdx2;
        int iSignalLowPartSignalIdx, iSignalHighPartSignalIdx;
        double[] doubleDataLowPart, doubleDataHighPart;
        double[] doubleDataLowPartBeforeSort, doubleDataHighPartBeforeSort;
        double[] doubleNoiseLowPart, doubleSignalLowPart;
        double[] doubleNoiseHighPart, doubleSignalHighPart;
        double doubleDataLowPartSignalRef, doubleAccu, doubleAvg;
        int iVar, iVarIdx, iVar2, iVar2Idx, iAccuCnt, iStartIdx;

        try {
            //----   分為LowPart & HighPart分別學習Noise & Signal ---------
            //---- LowPart Idx = 1~64 --------------
            iStartIdxLowPart = 1;
            iEndIdxLowPart = mIntTotalFreqSeqsCnt / 2;
            //---- HighPart Idx = 65~128 ------------
            iStartIdxHighPart = iEndIdxLowPart + 1;
            iEndIdxHighPart = mIntTotalFreqSeqsCnt - 1;
            iDataCntLowPart = iEndIdxLowPart - iStartIdxLowPart + 1;
            iDataCntHighPart = iEndIdxHighPart - iStartIdxHighPart + 1;
            iDataHighPartSignalIdx = (int) ((double) iDataCntHighPart * 0.90);

            doubleDataLowPart = new double[iEndIdxLowPart - iStartIdxLowPart + 1];
            doubleDataHighPart = new double[iEndIdxHighPart - iStartIdxHighPart + 1];
            doubleDataLowPartBeforeSort = new double[iEndIdxLowPart - iStartIdxLowPart + 1];
            doubleDataHighPartBeforeSort = new double[iEndIdxHighPart - iStartIdxHighPart + 1];

            iLearnEndIdx = mIntSTFFTNextSubSegIdx - 10;
            mIntSignalNoiseBasesCnt = iLearnEndIdx - SystemConfig.mIntStartIdxNoiseLearn + 1;
            iNoiseLowPartNoiseIdx = (int) ((double) mIntSignalNoiseBasesCnt * 0.10);
            iNoiseHighPartNoiseIdx2 = (int) ((double) mIntSignalNoiseBasesCnt * 0.50);
            iSignalLowPartSignalIdx = (int) ((double) mIntSignalNoiseBasesCnt - 5);
            iNoiseHighPartNoiseIdx = (int) ((double) mIntSignalNoiseBasesCnt * 0.10);
            iSignalHighPartSignalIdx = (int) ((double) mIntSignalNoiseBasesCnt - 5);
            doubleNoiseLowPart = new double[mIntSignalNoiseBasesCnt];
            doubleNoiseHighPart = new double[mIntSignalNoiseBasesCnt];
            doubleSignalLowPart = new double[mIntSignalNoiseBasesCnt];
            doubleSignalHighPart = new double[mIntSignalNoiseBasesCnt];

            for (iVar = SystemConfig.mIntStartIdxNoiseLearn; iVar <= iLearnEndIdx; iVar++) {
                iVarIdx = iVar - SystemConfig.mIntStartIdxNoiseLearn;
                for (iVar2 = iStartIdxLowPart; iVar2 <= iEndIdxLowPart; iVar2++) {
                    iVar2Idx = iVar2 - iStartIdxLowPart;
                    doubleDataLowPart[iVar2Idx] = mDoubleBVSpectrumValues[iVar][iVar2];
                    doubleDataLowPartBeforeSort[iVar2Idx] = mDoubleBVSpectrumValues[iVar][iVar2];
                }
                Arrays.sort(doubleDataLowPart);

                //--------- LowPart Noise Learning， 取LowPart 的1/4做Noise Learning -----------
                doubleAccu = 0;
                iAccuCnt = 0;
                iStartIdx = (iDataCntLowPart * 3 / 4);
                for (iVar2 = iStartIdx; iVar2 < iDataCntLowPart; iVar2++) {
                    doubleAccu = doubleAccu + doubleDataLowPartBeforeSort[iVar2];
                    iAccuCnt++;
                }
                doubleNoiseLowPart[iVarIdx] = doubleAccu / (double) iAccuCnt;

                //--------- LowPart Signal Learning， 取LowPart 地3強訊號的1/5做Signal Reference 並 Learning-----------
                doubleDataLowPartSignalRef = doubleDataLowPart[iDataCntLowPart - 2] / 5.0;
                doubleAccu = 0;
                iAccuCnt = 0;
                for (iVar2 = 0; iVar2 < iDataCntLowPart; iVar2++) {
                    iVar2Idx = iDataCntLowPart - 1 - iVar2;
                    if (doubleDataLowPart[iVar2Idx] >= doubleDataLowPartSignalRef) {
                        doubleAccu = doubleAccu + doubleDataLowPart[iVar2Idx];
                        iAccuCnt++;
                    } else {
                        break;
                    }
                }
                doubleAvg = doubleAccu / (double) iAccuCnt;
                doubleSignalLowPart[iVarIdx] = doubleAvg;

                //---- HighPart Noise & Signal Learning -------------------------------
                for (iVar2 = iStartIdxHighPart; iVar2 <= iEndIdxHighPart; iVar2++) {
                    iVar2Idx = iVar2 - iStartIdxHighPart;
                    doubleDataHighPart[iVar2Idx] = mDoubleBVSpectrumValues[iVar][iVar2];
                    doubleDataHighPartBeforeSort[iVar2Idx] = mDoubleBVSpectrumValues[iVar][iVar2];
                }
                Arrays.sort(doubleDataHighPart);
                doubleAccu = 0;
                iAccuCnt = 0;
                //---- HighPart Noise Learning，取HighPart 上層1/4 資料做Noise Learning ---
                iStartIdx = iDataCntHighPart * 3 / 4;
                for (iVar2 = iStartIdx ; iVar2 < iDataCntHighPart - 1; iVar2++) {
                    doubleAccu = doubleAccu + doubleDataHighPartBeforeSort[iVar2];
                    iAccuCnt++;
                }
                doubleNoiseHighPart[iVarIdx] = doubleAccu / (double) iAccuCnt;
                //---- HighPart Signal Learning，取HighPart Sort後資料之90%最強點，作為Signal ---
                doubleSignalHighPart[iVarIdx] = doubleDataHighPart[iDataHighPartSignalIdx];
            }

            //--- 對所有時間點LowPart Noise做排序，取最弱10%處，作為LowPart NoiseBase -----
            Arrays.sort(doubleNoiseLowPart);
            mDoubleNoiseBaseLearnedPartL = doubleNoiseLowPart[iNoiseLowPartNoiseIdx];
            //--- 對所有時間點HighPart Noise做排序，取最弱10%處，作為LowPart NoiseBase -----
            Arrays.sort(doubleNoiseHighPart);
            mDoubleNoiseBaseLearnedPartH = doubleNoiseHighPart[iNoiseHighPartNoiseIdx];

            //--- 比較LowPart NoiseBase 及 HighPart NoiseBase ，取小的作為NoiseBase -----
            SystemConfig.mDoubleNoiseBaseLearned = Math.min(mDoubleNoiseBaseLearnedPartH, mDoubleNoiseBaseLearnedPartL);

            //--- 對所有時間點LowPart Signal做排序，取最強90%處，作為LowPart SignalBase -----
            Arrays.sort(doubleSignalLowPart);
            mDoubleSignalBaseLearnedPartL = doubleSignalLowPart[iSignalLowPartSignalIdx];
            //--- 對所有時間點HighPart Signal做排序，取最強90%處，作為HighPart SignalBase -----
            Arrays.sort(doubleSignalHighPart);
            mDoubleSignalBaseLearnedPartH = doubleSignalHighPart[iSignalHighPartSignalIdx];
            //--- 取LowPart SignalBase，作為 SignalBase -----
            SystemConfig.mDoubleSignalBaseLearned = mDoubleSignalBaseLearnedPartL;

            if (SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_2_SNR_AMP) {
                mDoubleSNRBaseLearnedPartL = mDoubleSignalBaseLearnedPartL / mDoubleNoiseBaseLearnedPartL;
                mDoubleSNRBaseLearnedPartH = mDoubleSignalBaseLearnedPartH / mDoubleNoiseBaseLearnedPartH;
                SystemConfig.mDoubleSNRLearned = (SystemConfig.mDoubleSignalBaseLearned / SystemConfig.mDoubleNoiseBaseLearned) - 1;
            } else if (SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_3_SNR_POWER) {
                mDoubleSNRBaseLearnedPartL = Math.sqrt(mDoubleSignalBaseLearnedPartL / mDoubleNoiseBaseLearnedPartL);
                mDoubleSNRBaseLearnedPartH = Math.sqrt(mDoubleSignalBaseLearnedPartH / mDoubleNoiseBaseLearnedPartH);
                SystemConfig.mDoubleSNRLearned = Math.sqrt(SystemConfig.mDoubleSignalBaseLearned / SystemConfig.mDoubleNoiseBaseLearned) - 1;
            }
            //*j+,0524 SNRLearned
            else {
                SystemConfig.mDoubleSNRLearned = (SystemConfig.mDoubleSignalBaseLearned / SystemConfig.mDoubleNoiseBaseLearned) - 1;
            }
            //*j+,0524 SNRLearned  */

            //SystemConfig.mMyEventLogger.appendDebugStr("SNR=", String.format("%.1f", SystemConfig.mDoubleSNRLearned));
            //SystemConfig.mMyEventLogger.appendDebugStr("NoiseBase=", String.valueOf(SystemConfig.mDoubleNoiseBaseLearned));
            //SystemConfig.mMyEventLogger.appendDebugStr("SignalBase=", String.valueOf(SystemConfig.mDoubleSignalBaseLearned));

        } catch (Exception ex1) {
            //SystemConfig.mMyEventLogger.appendDebugStr("processNoiseAndSignalBase.Exception", "");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(), "");
            ex1.printStackTrace();
        }
    }


    private int processOneMaxIdxBySNR(int iMaxIdxNextIdx) {
        int iVar, iCnt, iIdxThis, iIdxStart, iIdxEnd;
        int iLowWindowIdx, iLowWindowIdxNew;
        double doubleDiffLowWindowAccu;
        double doubleCompVal;
        double doubleLowWindowOldData;
        int iCurMaxIdxWindowSize;
        double doubleFirstLowAvgAmp;
        double doubleCompareBase;
        boolean boolHigherPartWindow, boolFound;

        try {
            doubleCompareBase = mDoubleMaxIdxCompareBase;
            //doubleCompareBase = doubleCompareBase * (double) SystemConfig.mIntGainLevelMap[mIntSubSegGainLevels[iMaxIdxNextIdx]]
            //        / (double) SystemConfig.mIntGainLevelMap[SystemConfig.mIntNoiseLearnedGainLevel];

            // calculate first doubleDiffMax and Window data
            //iIdxThis = SystemConfig.mBVSignalProcessorPart1.mIntTotalFreqSeqsCnt - 1 - SystemConfig.mIntMaxIdxDiffMaxModeWindowSize;
            iIdxThis = MainActivity.mBVSignalProcessorPart1.mIntTotalFreqSeqsCnt -1;
            //------------------------------------------------------
            // first LowWindow
            //------------------------------------------------------
            iIdxStart = iIdxThis - SystemConfig.mIntMaxIdxWindowSizeBySNR;
            iIdxEnd = iIdxThis - 1;
            iLowWindowIdx = SystemConfig.mIntMaxIdxWindowSizeBySNR - 1;
            doubleDiffLowWindowAccu = 0;
            for (iVar = iIdxStart; iVar <= iIdxEnd; iVar++) {
                mDoubleMaxIdxLowWindowData[iLowWindowIdx] = mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar];
                doubleDiffLowWindowAccu = doubleDiffLowWindowAccu + mDoubleMaxIdxLowWindowData[iLowWindowIdx];
                iLowWindowIdx--;
            }
            doubleFirstLowAvgAmp = doubleDiffLowWindowAccu / (double) (iIdxEnd - iIdxStart + 1);
            //------------------------------------------------------
            // calculate first difference value
            //------------------------------------------------------
            doubleCompVal = doubleFirstLowAvgAmp;
            if (doubleCompVal < 0) {
                doubleCompVal = 0;
            }
            //mIntArrayBloodVelocityMaxIdx[mIntBloodVelocityMaxSelectNextIdx] = iIdxThis;

            if ((doubleCompVal >= doubleCompareBase)
                    && (mDoubleBVSpectrumValues[iMaxIdxNextIdx][iIdxThis] >= doubleCompareBase)) {
                //&& (SystemConfig.mBVSignalProcessorPart1.mDoubleBVSpectrumValues[iMaxIdxNextIdx][iIdxThis] <= SystemConfig.mDoubleAmpSignalTooMaxLearned)) {
                mIntArrayMaxIdx[iMaxIdxNextIdx] = iIdxThis;
                boolFound = true;
            } else {
                mIntArrayMaxIdx[iMaxIdxNextIdx] = 0;   // not found
                boolFound = false;
            }

            iLowWindowIdx = 0;

            iCurMaxIdxWindowSize = SystemConfig.mIntMaxIdxWindowSizeBySNR;

            //---------------------------------------------------------------------
            // calculate continuous doubleDiffMax
            //---------------------------------------------------------------------

            //iCnt = iIdxThis-SystemConfig.mIntMaxIdxWindowSizeBySNR-1;
            boolHigherPartWindow = true;
            iCnt = iIdxThis - 2;
            for (iVar = 0; iVar < iCnt - 1; iVar++) {
                iIdxThis--;
                if (iIdxThis < (SystemConfig.mIntMaxIdxWindowSizeBySNR + 1)) {    // LowerPart < WindowSize
                    doubleDiffLowWindowAccu = doubleDiffLowWindowAccu - mDoubleMaxIdxLowWindowData[iLowWindowIdx];

                    iCurMaxIdxWindowSize--;
                    boolHigherPartWindow = false;

                } else {
                    boolHigherPartWindow = true;
                }
                //-------------------------------------------------
                // new Low Window Data
                //-------------------------------------------------
                if (boolHigherPartWindow) {
                    iLowWindowIdxNew = iIdxThis - SystemConfig.mIntMaxIdxWindowSizeBySNR;
                    doubleLowWindowOldData = mDoubleMaxIdxLowWindowData[iLowWindowIdx];
                    mDoubleMaxIdxLowWindowData[iLowWindowIdx] = mDoubleBVSpectrumValues[iMaxIdxNextIdx][iLowWindowIdxNew];
                    doubleDiffLowWindowAccu = doubleDiffLowWindowAccu + mDoubleMaxIdxLowWindowData[iLowWindowIdx] - doubleLowWindowOldData;
                } else {
                    //doubleCompareBase =  SystemConfig.mDoubleNoiseBaseLearned + SystemConfig.mSystemConfig.mBVSignalProcessorPart1.mDoubleMaxIdxCompareBase *3;
                }
                //-------------------------------------------------
                // new Difference between Low Window and High Window
                //-------------------------------------------------
                doubleCompVal = doubleDiffLowWindowAccu / (double) iCurMaxIdxWindowSize;
                if (doubleCompVal < 0) {
                    doubleCompVal = 0;
                }

                if (!boolFound) {
                    if ((doubleCompVal > doubleCompareBase) &&
                            (mDoubleBVSpectrumValues[iMaxIdxNextIdx][iIdxThis] >= doubleCompareBase)) {
                        boolFound = true;
                        mIntArrayMaxIdx[iMaxIdxNextIdx] = iIdxThis;
                    }
                }

                //-------------------------------------------------
                iLowWindowIdx++;
                if (iLowWindowIdx >= iCurMaxIdxWindowSize) {
                    iLowWindowIdx = 0;
                }
            }

            //mIntPreMaxIdx = mIntArrayMaxIdx[iMaxIdxNextIdx];
            return mIntArrayMaxIdx[iMaxIdxNextIdx];
        } catch (Exception ex1) {
            //SystemConfig.mMyEventLogger.appendDebugStr("processOneMaxIdxBySNR.Exception()", "");
//            return mIntArrayMaxIdx[iMaxIdxNextIdx];
            return 0;
        }
    }


    public int getCurSubSegSize() {
        if (SystemConfig.mEnumTryState != SystemConfig.ENUM_TRY_STATE.STATE_TRY) {
            //Log.d("BVSPP1","SystemConfig.mEnumTryState != SystemConfig.ENUM_TRY_STATE.STATE_TRY mIntSTFFTNextSubSegIdx="+mIntSTFFTNextSubSegIdx);
            return mIntSTFFTNextSubSegIdx;
        } else {
            //Log.d("BVSPP1","mIntTrySTFFTNextSubSegIdx="+mIntTrySTFFTNextSubSegIdx);
            return mIntTrySTFFTNextSubSegIdx;
        }
    }

    private boolean checkBreakLowNeeded(int iStartIdx, int iEndIdx) {
        int iVar, iBreakLowCntAccu, iDiff, iBreakLowSizeDefault;
        int iBreakLowCompare, iGap;

        processMaxIdxByMovingAverage(11, iStartIdx - 11, iEndIdx + 11);

        iBreakLowCntAccu = 0;
        for (iVar = iStartIdx; iVar <= iEndIdx; iVar++) {
            if (mIntArrayMaxIdxByMovingAverage[iVar] <= 10) {
                iBreakLowCompare = mIntArrayMaxIdxByMovingAverage[iVar];
            } else {
                iBreakLowCompare = mIntArrayMaxIdxByMovingAverage[iVar] / 2;    //-- 比平均值小的，就有發生BreakLow機率---
            }
            iDiff = mIntArrayMaxIdx[iVar] - mIntArrayMaxIdxByMovingAverage[iVar];
            if (iDiff <= 0) {
                iDiff = -iDiff;
            }
            if (iDiff > iBreakLowCompare) {
                iBreakLowCntAccu++;
            }
        }
        //SystemConfig.mMyEventLogger.appendDebugStr("BreakLowCntAccu= ", String.valueOf(iBreakLowCntAccu));

        if ((SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_BE_ONLINE)
                || (SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_LE_ONLINE)
                || (SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_OFFLINE)) {
            iBreakLowSizeDefault = SystemConfig.INT_MAXIDX_BREAK_LOW_WINDOWS_SIZE_DEFAULT_ITRI;
        } else {  //if (SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.USCOM_8K) {
            iBreakLowSizeDefault = SystemConfig.INT_MAXIDX_BREAK_LOW_WINDOWS_SIZE_DEFAULT_8K_USCOM;
        }

        if (iBreakLowCntAccu <= 20) {     //---可能 BreakLow 發生20次，則判斷無BreakLow發生
            mIntMaxIdxBreakLowWindowSize = 0;
            return false;
        } else if (iBreakLowCntAccu <= 60) {    //---可能 BreakLow 發生次數<=60次，則判斷有輕微BreakLow發生，BreakLow Window Size取小一點
            iGap = 40 / iBreakLowSizeDefault;   //---當iBreakLowCntAccu==60時，取BreakLowWindowSize=iBreakLowSizeDefault，推算當iBreakLowCntAccu<60時，每增加一個BreakLowWindowSize所需之Gap
            if (iGap < 1) {
                iGap = 1;
            }
            mIntMaxIdxBreakLowWindowSize = 2 + (iBreakLowCntAccu - 20) / iGap;
            if (mIntMaxIdxBreakLowWindowSize > iBreakLowSizeDefault) {  //---最大Break Low Window Size為iBreakLowSizeDefault
                mIntMaxIdxBreakLowWindowSize = iBreakLowSizeDefault;
            }
            return true;
        } else {        //---當iBreakLowCntAccu>60時，BreakLowWindowSize可以比iBreakLowSizeDefault大
            mIntMaxIdxBreakLowWindowSize = iBreakLowSizeDefault + (iBreakLowCntAccu - 60) / 4;
            if (mIntMaxIdxBreakLowWindowSize > (iBreakLowSizeDefault + iBreakLowSizeDefault / 2)) {
                mIntMaxIdxBreakLowWindowSize = iBreakLowSizeDefault + iBreakLowSizeDefault / 2;
            }
            return true;
        }
    }

    private void processOneMaxIdxBreakLowModify(int iMaxIdxNextIdx) {
        int iVar, iIdx, iMaxValue, iNearValue, iNearCnt, iAngNearValue;
        boolean boolModifyToNear = false;

        mIntMaxIdxBreakLowWindowData[mIntMaxIdxBreakLowWindowIdx] = mIntArrayMaxIdx[iMaxIdxNextIdx];
        if (mIntMaxIdxBreakLowWindowSizeCur < mIntMaxIdxBreakLowWindowSize) {
            mIntMaxIdxBreakLowWindowSizeCur++;
        } else {
            iIdx = mIntMaxIdxBreakLowWindowIdx + 1;
            if (iIdx == mIntMaxIdxBreakLowWindowSize) {
                iIdx = 0;
            }

            iMaxValue = mIntMaxIdxBreakLowWindowData[iIdx];
            for (iVar = 0; iVar < mIntMaxIdxBreakLowWindowSize - 2; iVar++) {
                iIdx++;
                if (iIdx == mIntMaxIdxBreakLowWindowSize) {
                    iIdx = 0;
                }
                if (iMaxValue < mIntMaxIdxBreakLowWindowData[iIdx]) {
                    iMaxValue = mIntMaxIdxBreakLowWindowData[iIdx];
                }
            }

            if (mIntArrayMaxIdx[iMaxIdxNextIdx] < iMaxValue) {
                mIntArrayMaxIdx[iMaxIdxNextIdx] = iMaxValue;
            }
        }

        mIntMaxIdxBreakLowWindowIdx++;
        if (mIntMaxIdxBreakLowWindowIdx == mIntMaxIdxBreakLowWindowSize) {
            mIntMaxIdxBreakLowWindowIdx = 0;
        }
    }


    public void copyToMaxIdxForVTI() {
        int iVar;

        for (iVar = 0; iVar < mIntMaxIdxNextIdx; iVar++) {
            mIntArrayMaxIdxByMovingAverage[iVar] = mIntArrayMaxIdx[iVar];
        }
    }

    public void processMaxIdxByMovingAverage(int iSmoothPoints, int iStartIdx, int iEndIdx) {
        int iVar, iWindowSize, iCurWindowSize, iWindowNextIdx, iAverageNextIdx, iWindowAccu, iOldData;
        int[] iWindowData;

        iWindowSize = iSmoothPoints;
        iCurWindowSize = 0;
        iWindowNextIdx = 0;
        iAverageNextIdx = iStartIdx + iSmoothPoints / 2;
        iWindowAccu = 0;
        iWindowData = new int[iWindowSize];
        for (iVar = iStartIdx; iVar <= iEndIdx; iVar++) {
            mIntArrayMaxIdxByMovingAverage[iVar] = mIntArrayMaxIdx[iVar];
            if (iCurWindowSize < iWindowSize) {
                iWindowData[iWindowNextIdx] = mIntArrayMaxIdx[iVar];
                iWindowAccu = iWindowAccu + iWindowData[iWindowNextIdx];
                iCurWindowSize++;
                if (iCurWindowSize == iWindowSize) {
                    mIntArrayMaxIdxByMovingAverage[iAverageNextIdx] = iWindowAccu / iWindowSize;
                    iAverageNextIdx++;
                }
            } else {
                iOldData = iWindowData[iWindowNextIdx];
                iWindowData[iWindowNextIdx] = mIntArrayMaxIdx[iVar];
                iWindowAccu = iWindowAccu + iWindowData[iWindowNextIdx] - iOldData;
                mIntArrayMaxIdxByMovingAverage[iAverageNextIdx] = iWindowAccu / iWindowSize;
                iAverageNextIdx++;
            }
            iWindowNextIdx++;
            if (iWindowNextIdx == iWindowSize) {
                iWindowNextIdx = 0;
            }
        }
        for (iVar = iStartIdx; iVar <= iEndIdx; iVar++) {
            mIntArrayMaxIdx[iVar] =  mIntArrayMaxIdxByMovingAverage[iVar];
        }
    }


    public void processMaxIdxByMovingAverageForSingleVpk() {
        double doubleSingleVpkAccu;
        int iSingleVpkCnt, iSingleVpkStart, iSingleVpkEnd, iEndCompare;
        int iVar, iSingleVpk10Rest, iSingleVpk10;
        int[] iArrayMaxIdxForVTI;

        try {
            iArrayMaxIdxForVTI = new int[mIntMaxIdxNextIdx];
            for (iVar = 0; iVar < mIntMaxIdxNextIdx; iVar++) {
                iArrayMaxIdxForVTI[iVar] = mIntArrayMaxIdxByMovingAverage[iVar];
            }
            Arrays.sort(iArrayMaxIdxForVTI);

            iSingleVpkStart = (int) ((double) iArrayMaxIdxForVTI.length * 0.25);
            iSingleVpkEnd = (int) ((double) iArrayMaxIdxForVTI.length * 0.75);
            iSingleVpkCnt = iSingleVpkEnd - iSingleVpkStart + 1;
            doubleSingleVpkAccu = 0;

            for (iVar = iSingleVpkStart; iVar <= iSingleVpkEnd; iVar++) {
                doubleSingleVpkAccu = doubleSingleVpkAccu + mDoubleBVFreqValues[iArrayMaxIdxForVTI[iVar]];
            }
            SystemConfig.mDoubleSingleVpkAvg = doubleSingleVpkAccu / (double) iSingleVpkCnt;
            iSingleVpk10 = (int) ((SystemConfig.mDoubleSingleVpkAvg * (double) 10) / mDoubleFreqGap);
            iSingleVpk10Rest = iSingleVpk10 % 10;
            if (iSingleVpk10Rest >= 5) {
                SystemConfig.mIntSingleVpkAvg = (iSingleVpk10 / 10) + 1;
            } else {
                SystemConfig.mIntSingleVpkAvg = (iSingleVpk10 / 10);
            }

            for (iVar = 0; iVar < mIntMaxIdxNextIdx; iVar++) {
                mIntArrayMaxIdxByMovingAverage[iVar] = SystemConfig.mIntSingleVpkAvg;
            }
        } catch (Exception ex1) {
            //SystemConfig.mMyEventLogger.appendDebugStr("Part1.procMaxIdxByMovingForSingleVpk.Exception", "");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(), "");
            ex1.printStackTrace();
        }
    }

    public void processAllSegmentMaxIdxBySNR() {
        int iVar;

        for (iVar = SystemConfig.mIntStartIdxNoiseLearn; iVar <= SystemConfig.mIntEndIdxNoiseLearn; iVar++) {
                processOneMaxIdxBySNR(iVar);
        }
        SystemConfig.mBoolMaxIdxBreakLow = checkBreakLowNeeded(SystemConfig.mIntStartIdxNoiseLearn, SystemConfig.mIntEndIdxNoiseLearn-1);


        for (iVar = SystemConfig.mIntEndIdxNoiseLearn+1 ; iVar < mIntSTFFTNextSubSegIdx; iVar++) {
            processOneMaxIdxBySNR(iVar);
            if (SystemConfig.mBoolMaxIdxBreakLow) {
                processOneMaxIdxBreakLowModify(iVar);
            }
        }
        mIntMaxIdxNextIdx = mIntSTFFTNextSubSegIdx;
    }


    private void processNormalizeForLearnDataByWuNew(){
        int iLearnEndIdx, iVar, iVar2;
        double doubleSignalStrengthMax;
        double[] doubleArrayDebug;

        processNoiseAndSignalByWu();

        iLearnEndIdx = mIntSTFFTNextSubSegIdx - 1;

        SystemConfig.mBoolSignalMaxForLearnWuNewOK = true;
        SystemConfig.mDoubleSignalMaxForAllWuNew = SystemConfig.mDoubleSignalMaxForLearnWuNew;
        SystemConfig.mDoubleSignalMinForAllWuNew = SystemConfig.mDoubleSignalMinForLearnWuNew;

        for (iVar = SystemConfig.mIntStartIdxNoiseLearn; iVar <= iLearnEndIdx; iVar++) {
            for (iVar2 = 0; iVar2 < mIntTotalFreqSeqsCnt; iVar2++) {
                mDoubleBVSpectrumValues[iVar][iVar2] = mDoubleBVSpectrumValues[iVar][iVar2] / SystemConfig.mDoubleSignalMaxForLearnWuNew;
                if(SystemConfig.mDoubleSignalMaxForLearnAfterNormWuNew <  mDoubleBVSpectrumValues[iVar][iVar2]) {
                    SystemConfig.mDoubleSignalMaxForLearnAfterNormWuNew =  mDoubleBVSpectrumValues[iVar][iVar2];
                }
                if(SystemConfig.mDoubleSignalMinForLearnAfterNormWuNew >  mDoubleBVSpectrumValues[iVar][iVar2]) {
                    SystemConfig.mDoubleSignalMinForLearnAfterNormWuNew =  mDoubleBVSpectrumValues[iVar][iVar2];
                }
            }
        }
        if((SystemConfig.mEnumPlotterGrayModeForWuNew == SystemConfig.ENUM_PLOTTER_GRAY_MODE_FOR_WU_NEW.WU_NEW_DIFF_MAX_NOISE)
                || (SystemConfig.mEnumPlotterGrayModeForWuNew == SystemConfig.ENUM_PLOTTER_GRAY_MODE_FOR_WU_NEW.WU_NEW_RATIO_MAX_NOISE)) {
            SystemConfig.mDoubleSignalMinForLearnAfterNormWuNew = SystemConfig.mDoubleNoiseStrengthWu / SystemConfig.mDoubleSignalMaxForLearnWuNew;
        }
    }


    private void processNormalizeForAllDataFromLearnByWuNew(){
        int iLearnEndIdx, iVar, iVar2;
        double doubleLearnToAllRatio;

        processNoiseAndSignalByWu();

        doubleLearnToAllRatio = SystemConfig.mDoubleSignalMaxForLearnWuNew / SystemConfig.mDoubleSignalMaxForAllWuNew;
        iLearnEndIdx = mIntSTFFTNextSubSegIdx - 1;
        for (iVar = SystemConfig.mIntStartIdxNoiseLearn; iVar <= iLearnEndIdx; iVar++) {
            for (iVar2 = 0; iVar2 < mIntTotalFreqSeqsCnt; iVar2++) {
                mDoubleBVSpectrumValues[iVar][iVar2] = mDoubleBVSpectrumValues[iVar][iVar2] * doubleLearnToAllRatio;
                //doubleArrayDebug[iVar2] = mDoubleBVSpectrumValues[iVar][iVar2];
                if(SystemConfig.mDoubleSignalMaxForLearnAfterNormWuNew <  mDoubleBVSpectrumValues[iVar][iVar2]) {
                    SystemConfig.mDoubleSignalMaxForLearnAfterNormWuNew =  mDoubleBVSpectrumValues[iVar][iVar2];
                }
                //if(SystemConfig.mDoubleSignalMinForLearnAfterNormWuNew >  mDoubleBVSpectrumValues[iVar][iVar2]) {
                //    SystemConfig.mDoubleSignalMinForLearnAfterNormWuNew =  mDoubleBVSpectrumValues[iVar][iVar2];
                //}
            }
        }
        SystemConfig.mDoubleSignalMinForLearnAfterNormWuNew = SystemConfig.mDoubleNoiseStrengthWu/SystemConfig.mDoubleSignalMaxForAllWuNew;
    }


    private void processNoiseAndSignalByWu() {
        int iLearnEndIdx, iVar, iVar2;
        int iHeavyCenterIdx, iNoiseWuStartIdx;
        double doubleNoiseRangeWuAccu, doubleNoiseMax, doubleNoiseMin;
        double doubleNoiseStrengthWuAccu;
        double doubleSignalStrengthMaxWuAccu;
        double doubleSignalStrengthMaxWu;

        iLearnEndIdx = mIntSTFFTNextSubSegIdx - 1;
        doubleNoiseRangeWuAccu = 0;
        doubleNoiseStrengthWuAccu = 0;
        doubleSignalStrengthMaxWuAccu = 0;
        for (iVar = SystemConfig.mIntStartIdxNoiseLearn; iVar <= iLearnEndIdx; iVar++) {
            //--- calculate Signal Max. ---
            doubleSignalStrengthMaxWu = 0;
            for(iVar2 = 1; iVar2 < mIntTotalFreqSeqsCnt; iVar2++){
                if(doubleSignalStrengthMaxWu < mDoubleBVSpectrumValues [iVar][iVar2]){
                    doubleSignalStrengthMaxWu = mDoubleBVSpectrumValues [iVar][iVar2];
                }
            }
            if(SystemConfig.mDoubleSignalStrengthMaxWu < doubleSignalStrengthMaxWu) {
                SystemConfig.mDoubleSignalStrengthMaxWu = doubleSignalStrengthMaxWu;
            }
            doubleSignalStrengthMaxWuAccu = doubleSignalStrengthMaxWuAccu + doubleSignalStrengthMaxWu;
            //--- calculate Noise  ---
            iHeavyCenterIdx = getHeavyCenterIdx(iVar);
            iNoiseWuStartIdx = iHeavyCenterIdx + (int) ((double)(mIntTotalFreqSeqsCnt - 1 - iHeavyCenterIdx) * 2.0/3.0);
            doubleNoiseMax = Double.NEGATIVE_INFINITY;
            doubleNoiseMin = Double.POSITIVE_INFINITY;
            for(iVar2 = iNoiseWuStartIdx; iVar2 < mIntTotalFreqSeqsCnt; iVar2++){
                if(doubleNoiseMax < mDoubleBVSpectrumValues [iVar][iVar2]){
                    doubleNoiseMax = mDoubleBVSpectrumValues [iVar][iVar2];
                }
                if(doubleNoiseMin > mDoubleBVSpectrumValues [iVar][iVar2]){
                    doubleNoiseMin = mDoubleBVSpectrumValues [iVar][iVar2];
                }
            }
//            Log.d("doubleNoiseMax", String.valueOf(doubleNoiseMax));
//            Log.d("doubleNoiseMin", String.valueOf(doubleNoiseMin));
            doubleNoiseRangeWuAccu = doubleNoiseRangeWuAccu + (doubleNoiseMax - doubleNoiseMin);
            doubleNoiseStrengthWuAccu = doubleNoiseStrengthWuAccu + (doubleNoiseMax + doubleNoiseMin) / 2.0;
        }
        /* 將畫圖參數設定為固定常數 2023/02/22 by Doris */
        SystemConfig.mDoubleNoiseRangeWu = 6.989132738914301E-11;
        SystemConfig.mDoubleNoiseStrengthWu = 5.4366277970330474E-11;
//        SystemConfig.mDoubleNoiseRangeWu = doubleNoiseRangeWuAccu / (double)(iLearnEndIdx-SystemConfig.mIntStartIdxNoiseLearn+1);
//        SystemConfig.mDoubleNoiseStrengthWu = doubleNoiseStrengthWuAccu / (double)(iLearnEndIdx-SystemConfig.mIntStartIdxNoiseLearn+1);
        SystemConfig.mDoubleSignalStrengthWu = doubleSignalStrengthMaxWuAccu / (double)(iLearnEndIdx-SystemConfig.mIntStartIdxNoiseLearn+1);
        SystemConfig.mDoubleSNRLearnedWu = SystemConfig.mDoubleSignalStrengthWu / SystemConfig.mDoubleNoiseStrengthWu;

//        Log.d("mDoubleNoiseRangeWu", String.valueOf(SystemConfig.mDoubleNoiseRangeWu));
//        Log.d("mDoubleNoiseRangeWu0", String.valueOf(doubleNoiseRangeWuAccu / (double)(iLearnEndIdx-SystemConfig.mIntStartIdxNoiseLearn+1)));
//        Log.d("mDoubleNoiseStrengthWu", String.valueOf(SystemConfig.mDoubleNoiseStrengthWu));
//        Log.d("mDoubleNoiseStrengthWu0", String.valueOf(doubleNoiseStrengthWuAccu / (double)(iLearnEndIdx-SystemConfig.mIntStartIdxNoiseLearn+1)));
    }



    private int getHeavyCenterIdx(int iSubSegIdx) {
        int iVar, iTorqueCenter;
        double doubleTorqueAccu, doubleAreaAccu, doubleTorqueCenter, doubleRest;

        doubleTorqueAccu = 0;
        for(iVar = 1 ; iVar < mIntTotalFreqSeqsCnt  ; iVar++ ) {
            doubleTorqueAccu = doubleTorqueAccu + mDoubleBVSpectrumValues [iSubSegIdx][iVar]*iVar;
        }

        doubleAreaAccu = 0;
        for(iVar = 1 ; iVar < mIntTotalFreqSeqsCnt  ; iVar++ ) {
            doubleAreaAccu = doubleAreaAccu + mDoubleBVSpectrumValues [iSubSegIdx][iVar];
        }
        doubleTorqueCenter = doubleTorqueAccu / doubleAreaAccu;
        iTorqueCenter = (int) doubleTorqueCenter;
        doubleRest = doubleTorqueCenter % 1.0;
        if(doubleRest >= 0.5){
            iTorqueCenter++;
        }

        return iTorqueCenter;
    }



    public void transWaveBySimilarMapping(int[] isOut, int[] _mIntInputArray_Input, int iStart, int iPatternWidth, int iCompareLength){
        //for (iVar = SystemConfig.mIntStartIdxNoiseLearn ; iVar < mIntSTFFTNextSubSegIdx - 250; iVar++) {
        int _iWaveCompare_Sum, _iWaveIdx_Init , _iWaveIdx_Cur ;
        int _iWaveCompareSumMax_T0 = Integer.MIN_VALUE;
        _iWaveIdx_Init = iStart;
        for (int _iVar = iStart; _iVar < iStart + iCompareLength; _iVar++) {
            _iWaveCompare_Sum = 0;
            _iWaveIdx_Cur = _iWaveIdx_Init;
            for (int _iPeriodIdx = _iVar; _iPeriodIdx < _iVar + iCompareLength; _iPeriodIdx++) {
                _iWaveCompare_Sum += Math.abs(_mIntInputArray_Input[_iWaveIdx_Cur] - _mIntInputArray_Input[_iPeriodIdx]);
                //_iWaveCompare_Sum += _mIntInputArray_Input[_iWaveInitIdx_Cur] * _mIntInputArray_Input[_iPeriodIdx];
                //if(_mIntInputArray_Input[_iWaveInitIdx_Cur] > _mIntInputArray_Input[_iPeriodIdx]){
                //_iWaveCompare_Sum += _mIntInputArray_Input[_iWaveInitIdx_Cur] - _mIntInputArray_Input[_iPeriodIdx];
                //    _iWaveCompare_Sum +=_mIntInputArray_Input[_iWaveInitIdx_Cur] - _mIntInputArray_Input[_iPeriodIdx];
                //}
                _iWaveIdx_Cur++;
            }
            isOut[_iVar] = _iWaveCompare_Sum;
        }
    }

    public void transWaveByArrayCopy(int[] _mIntArray_Output, int[] _mIntInputArray_Input, int _iStart, int _iEnd) {
        for (int _iVar=_iStart; _iVar<_iEnd; _iVar++){
            _mIntArray_Output[_iVar]= _mIntInputArray_Input[_iVar];
        }
    }
    public void transWaveByMovingIntegrate(int[] _mIntArray_Output, int[] _mIntInputArray_Input, int _iStart, int _iEnd, int _iIntegrateWindows){
        //for (iVar = SystemConfig.mIntStartIdxNoiseLearn ; iVar < mIntSTFFTNextSubSegIdx - 250; iVar++) {
        int _iWaveIntegrate_Sum, _iWaveIdx_Init , _iWaveIdx_Cur ;
        int _iWaveIntegrateSumMax = Integer.MIN_VALUE, _iWaveIntegrate_Max = Integer.MIN_VALUE;
        int [] _mIntArray = new int[_iEnd];
        _iWaveIdx_Init = _iStart;
        for (int _iVar = _iStart; _iVar < _iEnd - _iIntegrateWindows; _iVar++) {
            _iWaveIntegrate_Sum = 0;
            _iWaveIdx_Cur = _iWaveIdx_Init;
            for (int _iPeriodIdx = _iVar; _iPeriodIdx < _iVar + _iIntegrateWindows; _iPeriodIdx++) {
                _iWaveIntegrate_Sum += _mIntInputArray_Input[_iPeriodIdx];
            }

            if (_iWaveIntegrateSumMax<_iWaveIntegrate_Sum){
                _iWaveIntegrateSumMax=_iWaveIntegrate_Sum;
            }
            _mIntArray[_iVar] = _iWaveIntegrate_Sum;
        }
        for (int _iVar = _iStart; _iVar < _iEnd; _iVar++) {
            _mIntArray_Output[_iVar] = (int) ((double)_mIntArray[_iVar] / _iWaveIntegrateSumMax * 100);
        }
    }
    public void transWaveByNormalize(int[] _mIntArray_Output, int[] _mIntArray_Input, int _iStart, int _iEnd, int _iWindows){
        //for (iVar = SystemConfig.mIntStartIdxNoiseLearn ; iVar < mIntSTFFTNextSubSegIdx - 250; iVar++) {
        int _iWave_Max=Integer.MIN_VALUE , _iWaveIdx , _iWaveIdx_Nums ;
        float _iWaveIntegrate_Mean;
        _iWaveIdx = 0;
        // initial

        for (int _iVar = _iStart; _iVar < _iEnd; _iVar++) {
            if(_iWaveIdx%_iWindows ==0){
                int _iEnd2;
                _iWave_Max = Integer.MIN_VALUE;
                _iEnd2 = _iVar+_iWindows;
                if (_iEnd2>_iEnd) _iEnd2 = _iEnd;
                for (int _iVar2 = _iVar; _iVar2 < _iEnd2; _iVar2++){
                    if (_iWave_Max<_mIntArray_Input[_iVar2])
                        _iWave_Max=_mIntArray_Input[_iVar2];
                }

            }
            _iWaveIdx++;
            _mIntArray_Output[_iVar] = (int) ( 100 * ((float)_mIntArray_Input[_iVar] /_iWave_Max));
        }
    }

    public void transWaveByMovingVti100(int[] _mIntArray_Output, int[] _mIntArray_Input, int _iStart, int _iEnd, int _iHalfFilterWindows){
        //for (iVar = SystemConfig.mIntStartIdxNoiseLearn ; iVar < mIntSTFFTNextSubSegIdx - 250; iVar++) {
        int _iWaveIntegrate_Sum, _iWaveIdx , _iWaveIdx_Nums ;
        int _iWaveIntegrateSumMax = Integer.MIN_VALUE, _iWaveIntegrate_Max = Integer.MIN_VALUE;
        int [] _mIntArray = new int[_iEnd];
        //_mIntArray_Output = new int[_mIntArray_Input.length];
        _iWaveIdx = _iStart;
        _iWaveIntegrate_Sum = 0;
        _iWaveIdx_Nums = 2*_iHalfFilterWindows +1;
        // initial
        if (_iStart < _iHalfFilterWindows){
            _iStart += (_iHalfFilterWindows-_iStart);
        }

        for (int _iVar = _iStart-_iHalfFilterWindows; _iVar <= _iStart + _iHalfFilterWindows; _iVar++) {
            _iWaveIntegrate_Sum += (int)(fVPKCali(_mIntArray_Input[_iVar]) * 100) ;
        }
        _iWaveIdx = _iStart;
        //_mIntArray[_iWaveIdx] = _iWaveIntegrate_Sum / _iWaveIdx_Nums;
        _mIntArray[_iWaveIdx] = _iWaveIntegrate_Sum;
        for (int _iVar = _iWaveIdx+1; _iVar < _iEnd; _iVar++) {
            _iWaveIntegrate_Sum = _iWaveIntegrate_Sum -
                    (int)(fVPKCali(_mIntArray_Input[_iVar-_iHalfFilterWindows]) * 100) +
                    (int)(fVPKCali(_mIntArray_Input[_iVar + _iHalfFilterWindows]) * 100);
        //    _mIntArray[_iVar] = _iWaveIntegrate_Sum / _iWaveIdx_Nums;
            _mIntArray[_iVar] = _iWaveIntegrate_Sum;
        }
        for (int _iVar = _iStart; _iVar < _iEnd; _iVar++) {
            _mIntArray_Output[_iVar]= _mIntArray[_iVar];
        }
    }


    public void transDoubleWaveByMovingAverage(double[] _mDoubleArray_Output, double[] _mDoubleInputArray_Input, int _iStart, int _iEnd, int _iHalfFilterWindows){
        //for (iVar = SystemConfig.mIntStartIdxNoiseLearn ; iVar < mIntSTFFTNextSubSegIdx - 250; iVar++) {
        double _fWaveIntegrate_Sum;
        int _iWaveIdx , _iWaveIdx_Nums ;
        double _fWaveIntegrateSumMax = Double.MIN_VALUE, _fWaveIntegrate_Max = Double.MIN_VALUE;
        double [] _DoubleArray = new double[_iEnd+1];
        _iWaveIdx = 0;
        _fWaveIntegrate_Sum = 0;
        _iWaveIdx_Nums = 2 * _iHalfFilterWindows +1;
        // initial

        for (int _iVar = _iStart; _iVar <= _iEnd; _iVar++) {
            if ((_iVar - _iHalfFilterWindows) <= _iStart){
                _fWaveIntegrate_Sum=0;
                for (int _iVar1 = _iStart; _iVar1 <= _iVar + _iHalfFilterWindows; _iVar1++) {
                    _fWaveIntegrate_Sum += _mDoubleInputArray_Input[_iVar1];
                }
                _iWaveIdx ++;
                _iWaveIdx_Nums = _iWaveIdx + _iHalfFilterWindows;
            }else if ((_iVar + _iHalfFilterWindows) > _iEnd){
                _iWaveIdx --;
                _fWaveIntegrate_Sum=0;
                for (int _iVar1 = (_iVar - _iHalfFilterWindows); _iVar1 <= _iEnd; _iVar1 ++) {
                    _fWaveIntegrate_Sum += _mDoubleInputArray_Input[_iVar1];
                }
                _iWaveIdx_Nums = _iWaveIdx + _iHalfFilterWindows;
            }else{
                _fWaveIntegrate_Sum = _fWaveIntegrate_Sum - _mDoubleInputArray_Input[_iVar-_iHalfFilterWindows-1] + _mDoubleInputArray_Input[_iVar + _iHalfFilterWindows];
                _iWaveIdx_Nums = 2 * _iHalfFilterWindows +1;
            }
            _DoubleArray[_iVar] = _fWaveIntegrate_Sum / (float)_iWaveIdx_Nums;
        }
        for (int _iVar = _iStart; _iVar <= _iEnd; _iVar++) {
            _mDoubleArray_Output[_iVar]= _DoubleArray[_iVar];
        }

    }

    public void transWaveByBinary(int[] isOut, int[] isIn, int iStart, int iEnd, int iHalfWnd){
        transWaveByMovingAverage(isOut, isIn, iStart,iEnd, iHalfWnd);
        for (int iVar = iStart; iVar < iEnd; iVar++){
            if (isIn[iVar] > isOut[iVar]){
                isOut[iVar] = 1;
            }else{
                isOut[iVar] = 0;
            }
        }
    }

    public void transWaveByMovingAverage(int[] _mIntArray_Output, int[] _mIntInputArray_Input, int _iStart, int _iEnd, int _iHalfFilterWindows){
        //for (iVar = SystemConfig.mIntStartIdxNoiseLearn ; iVar < mIntSTFFTNextSubSegIdx - 250; iVar++) {
        int _iWaveIntegrate_Sum, _iWaveIdx , _iWaveIdx_Nums ;
        int _iWaveIntegrateSumMax = Integer.MIN_VALUE, _iWaveIntegrate_Max = Integer.MIN_VALUE;
        int [] _mIntArray = new int[_iEnd];
        _iWaveIdx = _iStart;
        _iWaveIntegrate_Sum = 0;
        _iWaveIdx_Nums = 2*_iHalfFilterWindows +1;
        // initial
        if (_iStart < _iHalfFilterWindows){
            _iStart += (_iHalfFilterWindows-_iStart);
        }

        for (int _iVar = _iStart-_iHalfFilterWindows; _iVar <= _iStart + _iHalfFilterWindows; _iVar++) {
            _iWaveIntegrate_Sum += _mIntInputArray_Input[_iVar];
        }
        _iWaveIdx = _iStart;
        _mIntArray[_iWaveIdx] = _iWaveIntegrate_Sum / _iWaveIdx_Nums;
        for (int _iVar = _iWaveIdx+1; _iVar < _iEnd; _iVar++) {
            _iWaveIntegrate_Sum = _iWaveIntegrate_Sum - _mIntInputArray_Input[_iVar-_iHalfFilterWindows-1] + _mIntInputArray_Input[_iVar + _iHalfFilterWindows];
            _mIntArray[_iVar] = _iWaveIntegrate_Sum / _iWaveIdx_Nums;
        }
        for (int _iVar = _iStart; _iVar < _iEnd; _iVar++) {
            _mIntArray_Output[_iVar]= _mIntArray[_iVar];
        }

    }

    public void transWaveByZeroOneMapping(int[] _mIntArrayWaveZeroOne_Output, int[] _mIntInputArray_Input, int _start, int _end, int _iNoiseWidth, int _iNoiseAmp) {
        boolean _bPeakSearch = false;
        int _iValleyAnyNums = 0;
        int _iValleyExtremeNums = 0;
        int[] _mIntArrayAnyValleyPosition = new int[300];
        int[] _mIntArrayAnyValleyValue = new int[300];
        int[] _mIntArrayAnyPeakPosition = new int[300];
        int _iPositionIdx_Cur, _iPositionValue_Cur;
        int _iPositionIdx_Pre = _start, _iPositionValue_Pre = _mIntInputArray_Input[_start];
        int _iWaveCondition =0 ;
        //  find Valley & Peak
        for (int _iVar = _start; _iVar < _end; _iVar ++) {
            _iPositionIdx_Cur = _iVar;
            _iPositionValue_Cur = _mIntInputArray_Input[_iPositionIdx_Cur];
            // bPeakSearch == false ; search Valley
            // bPeakSearch == true ; search Peak
            if (_iPositionIdx_Cur!=_iPositionIdx_Pre) {
                if((Math.abs(_iPositionValue_Cur-_iPositionValue_Pre)< _iNoiseAmp)||(_iPositionIdx_Cur-_iPositionValue_Pre)<_iNoiseWidth){
                    if (((_iPositionValue_Cur < _iPositionValue_Pre) && (!_bPeakSearch)) ||
                            ((_iPositionValue_Cur > _iPositionValue_Pre) && (_bPeakSearch))){
                        _iPositionValue_Pre = _iPositionValue_Cur;
                        _iPositionIdx_Pre = _iPositionIdx_Cur;
                    }
                }else{
                    _iWaveCondition = Integer.rotateLeft(_iWaveCondition, 1);
                    if (((_iPositionValue_Cur > _iPositionValue_Pre) && (!_bPeakSearch)) ||    //upward
                            ((_iPositionValue_Cur < _iPositionValue_Pre) && (_bPeakSearch))) { //downward
                        _iWaveCondition += 1;
                    }else{
                        _iPositionValue_Pre = _iPositionValue_Cur;
                        _iPositionIdx_Pre = _iPositionIdx_Cur;
                    }
                    if ((_iWaveCondition & 0b11111) == 0b01111) {
                        if(!_bPeakSearch){
                            _mIntArrayAnyValleyPosition[_iValleyAnyNums] =_iPositionIdx_Pre;
                        }else{
                            _mIntArrayAnyPeakPosition[_iValleyAnyNums] =_iPositionIdx_Pre;
                            _iValleyAnyNums ++;
                        }
                        _iPositionValue_Pre = _iPositionValue_Cur;
                        _iPositionIdx_Pre = _iPositionIdx_Cur;
                        _bPeakSearch = !_bPeakSearch;
                        _iWaveCondition = 0xb11111;
                    }
                }
                if (!_bPeakSearch) {
                    _mIntArrayWaveZeroOne_Output[_iVar] = 0;
                } else {
                    _mIntArrayWaveZeroOne_Output[_iVar] = 1;
                }
            }
        }
    }
    public void substractTwomIntArray(int[] _mIntArray_R, int[] _mIntArray_I1,int[] _mIntArray_I2, double _iI2Times){
        for (int _iVar = 0; _iVar < SystemConfig.mIntSystemMaxSubSegSize; _iVar ++) {
            _mIntArray_R[_iVar] = _mIntArray_I1[_iVar]  - (int) (_iI2Times * _mIntArray_I2[_iVar]) ;
            //if(_mIntArray_R[_iVar] < 0){
            //    _mIntArray_R[_iVar] = 0;
            //}
        }

    }

    private void vFindVPKPosition(int[] _arIntVPK, int[] _arIntVTIStart, int[] _arIntVTIEnd, int[] _arIntMaxIdx, int _intCounts){
        int _iStart, _iEnd, _iValue, _iIDX;
        for (int _iVar=0; _iVar< _intCounts; _iVar++) {
            // step1: find vpk by strong
            _iStart = _arIntVTIStart[_iVar];
            _iEnd = _arIntVTIEnd[_iVar];
            _iValue = Integer.MIN_VALUE;
            _iIDX = _iStart;

            for (int _iVar1 = _iStart; _iVar1<= _iEnd; _iVar1++){
                if (_iValue < _arIntMaxIdx[_iVar1]){
                    _iValue=_arIntMaxIdx[_iVar1];
                    _iIDX = _iVar1;
                }
            }
            _arIntVPK[_iVar] = _iIDX;
        }

    }

// jaufa, *, 181105, Find Strongest VTI, Find Valley then Calculate VTI and compare
    private void vFindHRVTIStartEndPosition(int[] _arIntHRStart, int[] _arIntHREnd, int[] _arIntVTIStart, int[] _arIntVTIEnd, int[] _arIntHRValley, int[] _arIntMaxIdxHR, int[] _arIntMaxIdxVTI, int _intHRPeriodCounts){
        Arrays.fill(_arIntHRStart,0);
        Arrays.fill(_arIntHREnd,0);
        Arrays.fill(_arIntVTIStart,0);
        Arrays.fill(_arIntVTIEnd,0);

        for (int _iVar=0; _iVar< _intHRPeriodCounts; _iVar++) {
            int _iHRStart = _arIntHRValley[_iVar];
            int _iHREnd = _arIntHRValley[_iVar+1]-1;
            int _iCompareVTIThis;
            int _iCompareVTIMax = Integer.MIN_VALUE;
            int _iCompareVTIStart;
            int _iCompareVTIEnd;
            boolean _bCompareVTIOK = false;
            int _iCaPeakIdx = 0;
            int _iCompareCondition;
            //int _iCompareLowest = Integer.MAX_VALUE, _iCompareLowestIdx = 0;
            int _iValueNow, _iValuePre, _iValueDiff;//, _iValueIdxNow=0, _iValueIdxPre=0;

            // For HR Start & End Position:
            _arIntHRStart[_iVar]=_iHRStart;
            _arIntHREnd[_iVar]=_iHREnd;

            int _iVTICnts = 10, _iValleyLasting=0, _iAmpDiff = 0;
            int[] _iArrThisVTIStart = new int[_iVTICnts], _iArrThisVTIEnd = new int[_iVTICnts], _iArrThisVTIArea = new int[_iVTICnts];

            //Step 1: Find Valley  from HR array
            _iCompareCondition = 0x00FF;
            _iValuePre = _arIntMaxIdxHR[_iHRStart];
            //_iCompareLowest = Integer.MAX_VALUE;
            _iCompareVTIStart = _iHRStart;
            _iCompareVTIEnd = _iHREnd;
            for (int _iVar1 = _iHRStart; _iVar1 <= _iHREnd; _iVar1++){
                _iCompareCondition &= 0x00FF;
                //Step 1.1: Find Next Vally
                //if (_iCompareLowest >= _arIntMaxIdxHR[_iVar1]){
                //    _iCompareLowest = _arIntMaxIdxHR[_iVar1];
                //    _iCompareLowestIdx = _iVar1;
                //}
                _iValueNow = _arIntMaxIdxHR[_iVar1];
                _iValueDiff = Math.abs(_iValueNow-_iValuePre);
                if (_iValueDiff >= 0){    // Amplitude determine: move to "0b0111"
                    _iCompareCondition = Integer.rotateLeft(_iCompareCondition, 1);
                    if(_arIntMaxIdxHR[_iVar1] >= _arIntMaxIdxHR[_iVar1 - 1]) {
                        //if ((_iValueNow-_iCompareLowest) >= 2) {
                        _iCompareCondition += 1;
                        //}
                    }
                    if (((_iCompareCondition & 0b1111) == 0b0110)) {    //雜訊
                    //    if (_arIntMaxIdxHR[_iVar1 - 3 - _iValleyLasting] <= _arIntMaxIdxHR[_iVar1]) {
                        if (_arIntMaxIdxHR[_iVar1 - 3] < _arIntMaxIdxHR[_iVar1]) {
                            _iCompareCondition = 0b0111;
                        } else {
                            _iValleyLasting = 0;
                        }
                    }
                    if (((_iCompareCondition & 0b1111) == 0b0111)) {
                    //_iCompareCondition = 0b111;
                        _iCompareVTIEnd = _iVar1-3-_iValleyLasting;
                        if (((_arIntMaxIdxHR[_iVar1]-_arIntMaxIdxHR[_iCompareVTIEnd]) > 6)
								|| ((_arIntMaxIdxHR[_iVar1] <= 3 && _arIntMaxIdxHR[_iCompareVTIEnd] <= 3))) {
                            _bCompareVTIOK = true;
                            _iValleyLasting = 0;
                        }else{
                            _iValleyLasting ++;
                            _iCompareCondition = 0b0011;
                        }
                    }

                _iValuePre=_iValueNow;

                    if (((_iCompareCondition & 0b111) == 0b010)) { //雜訊
                        if ((_arIntMaxIdxHR[_iVar1 - 2] < _arIntMaxIdxHR[_iVar1]) ||
						(_arIntMaxIdxHR[_iVar1 - 2] <= 3 &&  _arIntMaxIdxHR[_iVar1] <= 3)){
                            _iCompareCondition = 0b011;
                        } else {
                            _iValleyLasting = 0;
                        }
                    }



                //}//else if ((_iCompareCondition & 0b1111) == 0b1111){ //如果都是爬升
                 //   _iCompareLowestIdx = _iVar1;
                }
                if (_iVar1 == _iHREnd){
                    //_iCompareCondition = 0b1111;
                    _iCompareVTIEnd = _iHREnd;
                    _bCompareVTIOK = true;
                }

                if (_bCompareVTIOK) {
                    _bCompareVTIOK = false;
                    _iCompareVTIThis = 0;
                    for (int _iVar2 = _iCompareVTIStart; _iVar2 <= _iCompareVTIEnd; _iVar2++) {
                        _iCompareVTIThis += _arIntMaxIdxVTI[_iVar2];
                    }
                    if (_iCompareVTIMax <= _iCompareVTIThis) {
                        _iCompareVTIMax = _iCompareVTIThis;
                        _arIntVTIStart[_iVar] = _iCompareVTIStart;
                        _arIntVTIEnd[_iVar] = _iCompareVTIEnd;
                    }
                    _iCompareVTIStart = _iCompareVTIEnd + 1;
                }

            }
        }
    }


    private void vFindHRVTIStartEndPosition_1105(int[] _arIntHRStart, int[] _arIntHREnd, int[] _arIntVTIStart, int[] _arIntVTIEnd, int[] _arIntHRValley, int[] _arIntMaxIdxHR, int[] _arIntMaxIdxVTI, int _intHRPeriodCounts){
        Arrays.fill(_arIntHRStart,0);
        Arrays.fill(_arIntHREnd,0);
        Arrays.fill(_arIntVTIStart,0);
        Arrays.fill(_arIntVTIEnd,0);

        for (int _iVar=0; _iVar< _intHRPeriodCounts; _iVar++) {
            int _iHRStart = _arIntHRValley[_iVar];
            int _iHREnd = _arIntHRValley[_iVar+1]-1;
            int _iComparePeak;
            int _iComparePeakIdx = 0;
            int _iCompareCondition;
            int _iCompareLowest, _iCompareLowestIdx = 0;
            int _iValueNow, _iValuePre, _iValueDiff;//, _iValueIdxNow=0, _iValueIdxPre=0;

            // For HR Start & End Position:
            _arIntHRStart[_iVar]=_iHRStart;
            _arIntHREnd[_iVar]=_iHREnd;

            int _iVTICnts = 10, _iValleyCnts=0;
            int[] _iArrThisVTIStart = new int[_iVTICnts], _iArrThisVTIEnd = new int[_iVTICnts], _iArrThisVTIArea = new int[_iVTICnts];

            //Step 1: Find Vpk Peak from VTI array
            _iComparePeak = Integer.MIN_VALUE;
            for (int _iVar1 = _iHRStart; _iVar1<= _iHREnd; _iVar1++){
                if(_iComparePeak<=_arIntMaxIdxVTI[_iVar1]){
                    _iComparePeak=_arIntMaxIdxVTI[_iVar1];
                    _iComparePeakIdx = _iVar1;
                }
            }

            //Step 2: Find Vpk Start from HR array
            _iCompareCondition = 0xFFFF;
            _iValuePre = _arIntMaxIdxHR[_iComparePeakIdx];
            _iCompareLowest = Integer.MAX_VALUE;
            for (int _iVar1 = _iComparePeakIdx; _iVar1 >= _iHRStart; _iVar1--){
                if (_iCompareLowest >= _arIntMaxIdxHR[_iVar1]){
                        _iCompareLowest = _arIntMaxIdxHR[_iVar1];
                        _iCompareLowestIdx = _iVar1;
                }
                _iValueNow = _arIntMaxIdxHR[_iVar1];
                _iValueDiff = Math.abs(_iValueNow-_iValuePre);
                if (_iValueDiff > 0){
                    _iCompareCondition = Integer.rotateLeft(_iCompareCondition, 1);
                    if(_iValueNow >= _iValuePre){
                        //if ((_iValueNow-_iCompareLowest) >= 2) {
                            _iCompareCondition += 1;
                        //}
                    }
                    if (((_iCompareCondition & 0b111) == 0b011)) {
                        break;
                    }else if ((_iCompareCondition & 0b111) == 0b111){ //如果都是爬升
                        _iCompareLowest = Integer.MAX_VALUE;
                    }else if (((_iCompareCondition & 0b111) == 0b101)){ //雜訊
                    //   _iCompareCondition = 0xFFFF;
                    }
                    _iValuePre=_iValueNow;
                }
            }
            _arIntVTIStart[_iVar]=_iCompareLowestIdx;

            //Step 3: Find Vpk End from HR array
            _iCompareCondition = 0xFFFF;
            _iValuePre = _arIntMaxIdxHR[_iComparePeakIdx];
            _iCompareLowest = Integer.MAX_VALUE;
            for (int _iVar1 = _iComparePeakIdx; _iVar1 <= _iHREnd; _iVar1++){
                if (_iCompareLowest >= _arIntMaxIdxHR[_iVar1]){
                    _iCompareLowest = _arIntMaxIdxHR[_iVar1];
                    _iCompareLowestIdx = _iVar1;
                }
                _iValueNow = _arIntMaxIdxHR[_iVar1];
                _iValueDiff = Math.abs(_iValueNow-_iValuePre);
                if (_iValueDiff > 0){
                    _iCompareCondition = Integer.rotateLeft(_iCompareCondition, 1);
                    if(_iValueNow >= _iValuePre){
                        //if ((_iValueNow-_iCompareLowest) >= 2) {
                        _iCompareCondition += 1;
                        //}
                    }
                    if (((_iCompareCondition & 0b1111) == 0b0111)) {
                        break;
                    }else if ((_iCompareCondition & 0b1111) == 0b1111){ //如果都是爬升
                        _iCompareLowest = Integer.MAX_VALUE;
                    }else if (((_iCompareCondition & 0b1010) == 0b1010)){ //雜訊
                           _iCompareCondition = 0x01110;
                    }
                    _iValuePre=_iValueNow;
                }
            }
            _arIntVTIEnd[_iVar]=_iCompareLowestIdx;
        }
    }

    private void __vFindHRVTIStartEndPosition(int[] _arIntHRStart, int[] _arIntHREnd, int[] _arIntVTIStart, int[] _arIntVTIEnd, int[] _arIntHRValley, int[] _arIntMaxIdxHR, int[] _arIntMaxIdxVTI, int _intHRPeriodCounts){
        Arrays.fill(_arIntHRStart,0);
        Arrays.fill(_arIntHREnd,0);
        Arrays.fill(_arIntVTIStart,0);
        Arrays.fill(_arIntVTIEnd,0);


        for (int _iVar=0; _iVar< _intHRPeriodCounts; _iVar++) {
            int _iHRStart = _arIntHRValley[_iVar];
            int _iHREnd = _arIntHRValley[_iVar+1]-1;
            int _iComparePeak = _iHRStart;
            int _iCompareValue = Integer.MIN_VALUE;
            int _iCompareCondition;
            int _iCompareLowest, _iCompareLowestIdx = 0;
            int _iValueNow, _iValuePre, _iValueDiff;//, _iValueIdxNow=0, _iValueIdxPre=0;

            // For HR Start & End Position:
            _arIntHRStart[_iVar]=_iHRStart;
            _arIntHREnd[_iVar]=_iHREnd;

            int _iVTICnts = 10, _iValleyCnts=0;
            int[] _iArrThisVTIStart = new int[_iVTICnts], _iArrThisVTIEnd = new int[_iVTICnts], _iArrThisVTIArea = new int[_iVTICnts];

            // step1: find Valley and Calculate VTI Area
            for (int _iVar1 = _iHRStart; _iVar1<= _iHREnd; _iVar1++){
                if (_iValleyCnts < _iVTICnts){          //0,1,2,3,4
                    _iArrThisVTIStart[_iValleyCnts] = _iVar1;
                    // Search Valley
                    _iCompareCondition = 0xFFFF;
                    _iValuePre = _arIntMaxIdxHR[_iVar1];
                    _iCompareLowest = Integer.MAX_VALUE;
                    for (int _iVar2 = _iVar1; _iVar2 <= _iHREnd; _iVar2++){
                        if (_iCompareLowest >= _arIntMaxIdxHR[_iVar2]){
                            _iCompareLowest = _arIntMaxIdxHR[_iVar2];
                            _iCompareLowestIdx = _iVar2;
                        }
                        _iValueNow = _arIntMaxIdxHR[_iVar2];
                        _iValueDiff = Math.abs(_iValueNow-_iValuePre);
                        if (_iValueDiff > 0){
                            _iCompareCondition = Integer.rotateLeft(_iCompareCondition, 1);
                            if(_iValueNow >= _iValuePre){
                                if ((_iValueNow-_iCompareLowest) >= 4) {
                                    _iCompareCondition += 1;
                                }
                            }
                            if (((_iCompareCondition & 0b1111) == 0b0111)) {
                                _iCompareLowest = Integer.MAX_VALUE;
                                _iCompareCondition = 0xFFFF;
                                _iArrThisVTIEnd[_iValleyCnts] = _iCompareLowestIdx;
                                _iValleyCnts ++;
                                _iVar1=_iCompareLowestIdx;
                                break;
                            }else if ((_iCompareCondition & 0b1111) == 0b1111){ //如果都是爬升
                                _iCompareLowest = Integer.MAX_VALUE;
                            }else if ((_iCompareCondition & 0b1111) == 0b1101){ //雜訊
                                _iCompareCondition = 0b1111;
                            }
                            _iValuePre=_iValueNow;
                        }
                        if (_iVar2 == _iHREnd){
                            _iArrThisVTIEnd[_iValleyCnts] = _iHREnd;
                        }
                    }
                }else{
                    break;
                }
            }

            int _iVTIAreaMax = Integer.MIN_VALUE, _iVTIAreaMaxIdx=0;
            for (int _iVar1 = 0; _iVar1<_iValleyCnts; _iVar1++){
                for (int _iVar2 = _iArrThisVTIStart[_iVar1]; _iVar2 <= _iArrThisVTIEnd[_iVar1]; _iVar2++){
                    _iArrThisVTIArea[_iVar1] += _arIntMaxIdxHR[_iVar2];
                }
                if (_iVTIAreaMax <= _iArrThisVTIArea[_iVar1]){
                    _iVTIAreaMax = _iArrThisVTIArea[_iVar1];
                    _iVTIAreaMaxIdx = _iVar1;
                }
            }

            _arIntVTIStart[_iVar]=_iArrThisVTIStart[_iVTIAreaMaxIdx];
            _arIntVTIEnd[_iVar]=_iArrThisVTIEnd[_iVTIAreaMaxIdx];
            _arIntVTIEnd[_iVar]=_iArrThisVTIEnd[_iVTIAreaMaxIdx];
        }
    }
    private void vFindHRVallyPositionByBeforePeak(int[] _arValleyPosition, int[] _arPeakPosition_Input, int[] _arInputArray_Input, int _iNums, int _iSearchFeatureLength){
        int _iExtremeMin;
        int _iExtremePosition = 0;
        /*
        for (int _iVar = 0; _iVar < _iNums; _iVar++){
            int _iSearchStart = _arPeakPosition_Input[_iVar]-_iSearchFeatureLength;
            int _iSearchEnd = _arPeakPosition_Input[_iVar];
            int _iCompareA, _iCompareB;
            int _iWaveCondition = 0xFF;
            _iCompareA = _arInputArray_Input[_iVar];
            _iCompareB = _iCompareA;
            _iExtremePosition =  _iSearchEnd;
            for (int _iVar2 = _iSearchEnd; _iVar2 >= _iSearchStart; _iVar2--){
                _iCompareA = _arInputArray_Input[_iVar2];
                _iWaveCondition = Integer.rotateLeft(_iWaveCondition, 1);
                if ((_iCompareA > _iCompareB)) { //upward
                    _iWaveCondition += 1;
                }
                _iCompareB = _iCompareA;
                if ((_iWaveCondition & 0b111111) == 0b011111) {
                    _iExtremePosition= _iVar2+2;
                    break;
                }
            }
            _arValleyPosition[_iVar]=_iExtremePosition;


        }
        //*/

        ///*
        for (int _iVar=0; _iVar<_iNums; _iVar++){
            int _iSearchEnd = _arPeakPosition_Input[_iVar]-_iSearchFeatureLength;
            int _iSearchStart = _arPeakPosition_Input[_iVar];
            if (_iSearchEnd<0) _iSearchEnd=0;
            _iExtremeMin = Integer.MAX_VALUE;
            for (int _iVar2=_iSearchStart; _iVar2 >= _iSearchEnd; _iVar2--) {
                if (_iExtremeMin > _arInputArray_Input[_iVar2]) {
                    _iExtremeMin = _arInputArray_Input[_iVar2];
                    _iExtremePosition = _iVar2;
                }
            }
            _arValleyPosition[_iVar]= _iExtremePosition;
        }
        //*/
    }

    public void vFindHRDeEchoByDeMaxPeak(int[] _mIntArrayDeEcho_IO, int[] _mIntHrPeakPosition_I, int _iHRCount, int _iDeEchoHalfWidth){
        for (int _iVar = 0; _iVar < _iHRCount; _iVar ++) {
            int _iStart = _mIntHrPeakPosition_I[_iVar] - _iDeEchoHalfWidth;
            int _iEnd = _mIntHrPeakPosition_I[_iVar] + _iDeEchoHalfWidth;
            for (int _iVar2=_iStart; _iVar2<=_iEnd; _iVar2++) {
                _mIntArrayDeEcho_IO[_iVar2]=0;
            }
        }
    }
    public double fOneHRVtiCm(int _iStart, int _iEnd, int[] _intArrayMaxIdx){
        double _fResult = 0;
        for (int _iVar=_iStart; _iVar<= _iEnd; _iVar++){
            _fResult += fVPKCali(_intArrayMaxIdx[_iVar]);
        }
        return _fResult * SystemConfig.mDoubleSubSegTimesliceMsec * 100 / 1000;
    }


    public double fSimilarCnt(double[] _doubleArrayMaxIdx, int _iStart, int _iEnd, double _fCompareDiff){
        double _fResult, _doubleDiffValue;
        int _iSimilarCnt = (_iEnd-_iStart +1), _iSimilarIdx, _iSimilarCntMax;
        int[] _iArraySimilarStart = new int [_iSimilarCnt];
        int[] _iArraySimilarEnd = new int [_iSimilarCnt];
        int[] _iArraySimilarCnt = new int [_iSimilarCnt];
        _iSimilarIdx = 0;
        Arrays.sort(_doubleArrayMaxIdx);
        for (int _iVar= _iStart; _iVar <= _iEnd; _iVar++) {
            for (int _iVar1 = _iVar; _iVar1 <= _iEnd; _iVar1++) {
                _doubleDiffValue = Math.abs(_doubleArrayMaxIdx[_iVar]-_doubleArrayMaxIdx[_iVar1]);
                _iArraySimilarStart[_iSimilarIdx]=_iVar;
                if (_doubleDiffValue <= _doubleArrayMaxIdx[_iVar1] * _fCompareDiff) {
                    _iArraySimilarEnd[_iSimilarIdx]=_iVar1;
                    _iArraySimilarCnt[_iSimilarIdx] = _iVar1 - _iVar +1;
                }else{
                    break;
                }
            }
            _iSimilarIdx ++;
        }
        _iSimilarCntMax = _iArraySimilarCnt[0];
        _iSimilarIdx = 0;
        for (int _iVar= 0; _iVar < _iSimilarCnt; _iVar++) {
            if (_iSimilarCntMax <= _iArraySimilarCnt[_iVar]){
                _iSimilarCntMax = _iArraySimilarCnt[_iVar];
                _iSimilarIdx = _iVar;
            }
        }

        _fResult = 0;
        _iSimilarCnt = _iArraySimilarEnd[_iSimilarIdx] - _iArraySimilarStart[_iSimilarIdx] +1;

        for (int _iVar = _iArraySimilarStart[_iSimilarIdx]; _iVar <= _iArraySimilarEnd[_iSimilarIdx]; _iVar++) {
            _fResult += _doubleArrayMaxIdx[_iVar];
        }
        _fResult = _fResult / _iSimilarCnt;
        return _fResult;
    }

    public double fSimilarMaxCnt(double[] _doubleArrayMaxIdx, int _iMaxOrder, double _fCompareDiff){
        double _fResult = 0, _doubleDiffValue;
        int[] _iSimilarEndIdx;
        if (_iMaxOrder > _doubleArrayMaxIdx.length)
            _iMaxOrder = _doubleArrayMaxIdx.length;
        _iSimilarEndIdx = new int[_iMaxOrder];
        for (int _iVar = _doubleArrayMaxIdx.length - 1; _iVar >= _doubleArrayMaxIdx.length - _iMaxOrder; _iVar--) {
            _fCompareDiff = _doubleArrayMaxIdx[_iVar] * _fCompareDiff;
            for (int _iVar1 = _iVar; _iVar1 >= 0; _iVar1--) {
                _doubleDiffValue = Math.abs(_doubleArrayMaxIdx[_iVar]-_doubleArrayMaxIdx[_iVar1]);
                if (_doubleDiffValue <= _fCompareDiff) {
                    _iSimilarEndIdx[_doubleArrayMaxIdx.length - _iVar - 1] = _iVar1;
                }else{
                    break;
                }
            }
        }

        int _iMaxStartIdx = 0, _iMaxCount = Integer.MIN_VALUE, _iSimilarCnt;
        for (int _iVar = 0; _iVar < _iMaxOrder; _iVar++) {
            _iSimilarCnt = (_doubleArrayMaxIdx.length - _iVar - _iSimilarEndIdx[_iVar]) +1;
            if (_iMaxCount <= _iSimilarCnt) {
                _iMaxCount = _iSimilarCnt;
                _iMaxStartIdx = _iVar;
            }
        }

        if (_iMaxCount > 3) _iMaxCount=3;
        for (int _iVar = _doubleArrayMaxIdx.length - _iMaxStartIdx - 1; _iVar >= _doubleArrayMaxIdx.length - _iMaxStartIdx - _iMaxCount; _iVar--) {
            _fResult += _doubleArrayMaxIdx[_iVar];
        }
        _fResult = _fResult / _iMaxCount;
        return _fResult;
    }


    public double fMeanHRVtiFixRange(int[] _intArrHRAccepted, int[] _intArrayHRStartPosition, int[] _intArrayHREndPosition, int[] _intArrayVtiPeriod, int _iHRPeriodMean, int _iCnt){
        double[] _doubleArrayVti = new double[_iCnt];
        int[] _intArrayVtiLength = new int[_iCnt];
        int[] _intArrayVtiSelIdx = new int[_iCnt];
        double _doubleVtiRaw=0, _doubleVtiCm = 0;
        double _fVti=0, _fResult;
        int _iHRPeriod=0, _iVtiCounts = 0;

        for (int _iVar=0; _iVar< _iCnt; _iVar++) {
            _iHRPeriod = _intArrayHREndPosition[_iVar] - _intArrayHRStartPosition[_iVar];
            // step 1: limit VTI range
            //if (_intArrHRAccepted[_iVar]==0x01 && (Math.abs(_iHRPeriodMean-_iHRPeriod) < (_iHRPeriodMean* 0.15)) ){       // HR Limited
            if (_intArrHRAccepted[_iVar]==0x01){       // HR Limited
                int _VtiMax = Integer.MIN_VALUE;
                for (int _iVar1 = _intArrayHRStartPosition[_iVar]; _iVar1<= _intArrayHREndPosition[_iVar]; _iVar1++){
                    if (_VtiMax <= _intArrayVtiPeriod[_iVar1]){
                        _VtiMax = _intArrayVtiPeriod[_iVar1];
                    }
                }
                _iVtiCounts++;
                _intArrayVtiSelIdx[_iVar]=1;
                _doubleArrayVti[_iVar] = (double) _VtiMax/100.0;
            }else{
                _intArrayVtiSelIdx[_iVar]=0;
                _intArrHRAccepted[_iVar]=0x00;
            }
        }

        double[] _doubleArrayVtiSel = new double[_iVtiCounts];
        if ( _iVtiCounts >=2 ){
            _fResult = 0;
            int _iCntTmp = 0;
            for (int _iVar=0; _iVar< _iCnt; _iVar++) {
                if (_intArrayVtiSelIdx[_iVar]==1){
                    _doubleArrayVtiSel[_iCntTmp]= _doubleArrayVti[_iVar];
                    _fResult += _doubleArrayVti[_iVar];
                    _iCntTmp ++;
                }
            }
            if (_iVtiCounts <= 3){
                _fResult /= _iVtiCounts;
            }else {
                Arrays.sort(_doubleArrayVtiSel);

                _fResult = fSimilarMaxCnt(_doubleArrayVtiSel, 3, 0.15);
            }
        }else{
            _fResult = -1;
        }

        int iHRPeriodUpper = 0;
        int iHRPeriodLower = 0;
        int iHRPeriodHalf = _iVtiCounts/2;

        if (_iVtiCounts<=3){
            mIntHRErrCode |= BINARY_ERR_CODE_VTI_INVALID;
        }else {
            Arrays.sort(_doubleArrayVtiSel);
            for (int _iVar = 0; _iVar < iHRPeriodHalf; _iVar++) {
                iHRPeriodLower += _doubleArrayVtiSel[_iVar];
            }

            for (int _iVar = (_iVtiCounts - iHRPeriodHalf); _iVar < _iVtiCounts; _iVar++) {
                iHRPeriodUpper += _doubleArrayVtiSel[_iVar];
            }

            if ((iHRPeriodUpper - iHRPeriodLower) > iHRPeriodUpper / 2) {
                mIntHRErrCode |= BINARY_ERR_CODE_VTI_UNSTABLE;
            }
        }

        return _fResult;
    }


    public double fMeanHRVti(int[] _intArrayHRStartPosition, int[] _intArrayHREndPosition, int[] _intArrayVTIStartPosition, int[] _intArrayVTIEndPosition, int[] _intArrayMaxIdx, int _iCnt){
        double[] _doubleArrayVti = new double[_iCnt];
        int[] _intArrayVtiLength = new int[_iCnt];
        int[] _intArrayVtiSelIdx = new int[_iCnt];
        double _doubleVtiRaw=0, _doubleVtiCm = 0;
        double _fVti=0, _fResult;
        int _iHRPeriod, _iVTIPeriod, _iVtiCounts = 0;

        for (int _iVar=0; _iVar< _iCnt; _iVar++) {
            _iHRPeriod = _intArrayHREndPosition[_iVar] - _intArrayHRStartPosition[_iVar];
            _iVTIPeriod = _intArrayVTIEndPosition[_iVar] - _intArrayVTIStartPosition[_iVar];
            _doubleArrayVti[_iVar] = fOneHRVtiCm(_intArrayVTIStartPosition[_iVar], _intArrayVTIEndPosition[_iVar], _intArrayMaxIdx);
            _intArrayVtiLength[_iVar] = _iVTIPeriod;
            // step 1: limit VTI range
            if (_iVTIPeriod > (_iHRPeriod* 0.2) && _iVTIPeriod < (_iHRPeriod* 0.7)){
                _iVtiCounts++;
                _intArrayVtiSelIdx[_iVar]=1;
            }else{
                _intArrayVtiSelIdx[_iVar]=0;
            }
        }
        double[] _doubleArrayVtiSel = new double[_iVtiCounts];
        _iVtiCounts = 0;

        for (int _iVar=0; _iVar< _iCnt; _iVar++) {
            if (_intArrayVtiSelIdx[_iVar]==1){
                _doubleArrayVtiSel[_iVtiCounts]= _intArrayVtiLength[_iVar];
                _iVtiCounts ++;
            }
        }

        if ( _iVtiCounts >=2 ){
            _fResult = 0;
            if (_iVtiCounts <= 3){
                for (int _iVar=0; _iVar< _iCnt; _iVar++) {
                    if (_intArrayVtiSelIdx[_iVar]==1){
                        _fResult += _doubleArrayVti[_iVar] ;
                    }
                }
                _fResult /= _iVtiCounts;
            }else {
                double _fVtiLengthMean = fSimilarCnt(_doubleArrayVtiSel, 0, _iVtiCounts-1, 0.15);
                _iVtiCounts = 0;
                for (int _iVar=0; _iVar< _iCnt; _iVar++) {
                    if (_intArrayVtiSelIdx[_iVar]==1 && Math.abs(_fVtiLengthMean - _intArrayVtiLength[_iVar]) <= _fVtiLengthMean * 0.3){
                        _fResult += _doubleArrayVti[_iVar] ;
                        _iVtiCounts ++;
                    } else {
                        _intArrayVtiSelIdx[_iVar] = 0;
                    }
                }

                _doubleArrayVtiSel = new double[_iVtiCounts];
                _iVtiCounts = 0;
                for (int _iVar=0; _iVar< _iCnt; _iVar++) {
                    if (_intArrayVtiSelIdx[_iVar]==1){
                        _doubleArrayVtiSel[_iVtiCounts]= _doubleArrayVti[_iVar];
                        _iVtiCounts ++;
                    }
                }
                if (_iVtiCounts < 2) {
                    _fResult = -1;
                }else if (_iVtiCounts < 4) {
                    for (int _iVar=0; _iVar< _iCnt; _iVar++) {
                        _fResult += _doubleArrayVtiSel[_iVar];
                    }
                    _fResult /= _iVtiCounts;
                } else {
                    Arrays.sort(_doubleArrayVtiSel);
                    _fResult = (_doubleArrayVtiSel[_iVtiCounts-2] + _doubleArrayVtiSel[_iVtiCounts-3] + _doubleArrayVtiSel[_iVtiCounts-4]) /3.0;
                }
            }
            double fHRVtiUpper = 0;
            double fHRVtiLower = 0;
            int iHRVtiHalf = _iVtiCounts/2;

            for (int _iVar = 0; _iVar< iHRVtiHalf; _iVar++) {
                fHRVtiLower += _doubleArrayVtiSel[_iVar];
            }

            for (int _iVar = (_iVtiCounts-iHRVtiHalf) ; _iVar< _iVtiCounts; _iVar++) {
                fHRVtiUpper += _doubleArrayVtiSel[_iVar];
            }

            if ((fHRVtiUpper-fHRVtiLower) > fHRVtiUpper/3){
                _fResult = -1;
            }

        }else{
            _fResult = -1;
        }
        return _fResult;
    }

    public double fMeanHRVpk(int[] _intArrHRAccept, double[] _fArrSignalSNR, int iSnrCompare,int[] _intArrayVpkIdx, int[] _intArrayMaxIdx, int _iCnt){
        double[] _fArrayVpkValue = new double[_iCnt];
        double _fResult, _fDiffValue=0;
        int _iVpkIdx, _iStart, _iEnd, _iSimilarCnt, _iSimilarIdx, _iSimilarCntMax;
        int[] _iArraySimilarCnt, _iArraySimilarStart, _iArraySimilarEnd;
        double _fVpkCali;
        int _iVpkCnt = 0;

        for (int _iVar=0; _iVar< _iCnt; _iVar++) {
            _iVpkIdx = _intArrayVpkIdx[_iVar];
            _fVpkCali = fVPKCali(_intArrayMaxIdx[_iVpkIdx]);
            if (_intArrHRAccept[_iVar] == 0 || _fArrSignalSNR[_iVpkIdx] < iSnrCompare || (_fVpkCali > 2)){
                _intArrHRAccept[_iVar] = 0;
                _fArrayVpkValue[_iVar] = 0;
            }else {
                _fArrayVpkValue[_iVar] = _fVpkCali;
                _iVpkCnt++;
            }
        }

        if ( _iVpkCnt >=3 ){
            Arrays.sort(_fArrayVpkValue);
            //_fResult = fSimilarMaxCnt(_fArrayVpkValue, (int)(_iCnt*2 /3), (_iCnt-1),0.15);
            _fResult = fSimilarMaxCnt(_fArrayVpkValue,2,0.1);
        }else{
            mIntHRErrCode |= BINARY_ERR_CODE_Vpk_INVALID;
            _fResult = -1;
        }

        return _fResult;
    }

    public int iMeanHRPeriod(int[] _iArrHRAccepted, int _iPreHRPeriod,int[] _intArrayMaxIdx,int[] _iArrVpkPosition,int[] _iArrayHRPStart ,int[] _iArrayHRPEnd, int _iHRCount){
        _iHRCount = _iHRCount-1;
        int[] _iArrayHRPeriod = new int[_iHRCount], _iArrayHRPeriodTmp = new int[_iHRCount];
        int[] _iArrayHRStart = new int[_iHRCount];
        int[] _iArrayHREnd = new int[_iHRCount];
        int[] _iArrayHRLength = new int[_iHRCount];
        for (int _iVar=0; _iVar< _iHRCount; _iVar++) {
            //if(_iArrHRAcceptedDel[_iVar] == 1 && mDoubleArraySignalPowerIdx[_iArrVpkPosition[_iVar]] >= 100)
            int _iVpkIdx = _iArrVpkPosition[_iVar];
            double _fVpkCali = fVPKCali(_intArrayMaxIdx[_iVpkIdx]);
            //if (_iArrHRAccepted[_iVar] == 0) {
                _iArrayHRPeriod[_iVar] = 0;
            //}else {
                _iArrayHRPeriod[_iVar] = _iArrayHRPEnd[_iVar] - _iArrayHRPStart[_iVar];
            //}
            _iArrayHRPeriodTmp[_iVar] = _iArrayHRPeriod[_iVar];
        }

        int iValidHr = 0;
        int iValidHrPeriodMean = 0;
        for (int _iVar = 0; _iVar < _iHRCount; _iVar++) {
            if(_iArrHRAccepted[_iVar] == 0 || (_iArrayHRPeriodTmp[_iVar] - _iPreHRPeriod) >= _iPreHRPeriod * 0.2){
                _iArrayHRPeriodTmp[_iVar] = 0;
                _iArrHRAccepted[_iVar] = 0;
            }else{
                iValidHr ++;
                iValidHrPeriodMean += _iArrayHRPeriodTmp[_iVar];
            }
        }
        iValidHrPeriodMean = iValidHrPeriodMean / iValidHr;

        Arrays.sort(_iArrayHRPeriodTmp);
        for (int _iVar = 0; _iVar< _iHRCount; _iVar++) {
            _iArrayHRStart[_iVar] = _iVar;
            _iArrayHREnd[_iVar] = _iVar;
            _iArrayHRLength[_iVar] = 0;
            for (int _iVar1 = _iVar; _iVar1 < _iHRCount; _iVar1++) {
                if ((_iArrayHRPeriodTmp[_iVar1] - _iArrayHRPeriodTmp[_iVar]) <= _iArrayHRPeriodTmp[_iVar1] * 0.1) {
                    _iArrayHREnd[_iVar] = _iVar1;
                    _iArrayHRLength[_iVar] += _iArrayHRPeriodTmp[_iVar1];
                }else{
                    break;
                }
            }
        }

        int _iLengthMaxVal = Integer.MIN_VALUE;
        int _iLengthMaxIdx = 0;
        for (int _iVar = 0; _iVar< _iHRCount; _iVar++) {
            if (_iLengthMaxVal <= _iArrayHRLength[_iVar]){
                _iLengthMaxVal = _iArrayHRLength[_iVar];
                _iLengthMaxIdx = _iVar;
            }
        }
        float _fResult = _iLengthMaxVal / (_iArrayHREnd[_iLengthMaxIdx]-_iLengthMaxIdx +1 );


        int iHRPeriodUpper = 0;
        int iHRPeriodLower = 0;
        int iHRPeriodHalf = _iHRCount/2;

        for (int _iVar = 0; _iVar< iHRPeriodHalf; _iVar++) {
            iHRPeriodLower += _iArrayHRPeriodTmp[_iVar];
        }

        for (int _iVar = (_iHRCount-iHRPeriodHalf) ; _iVar< _iHRCount; _iVar++) {
            iHRPeriodUpper += _iArrayHRPeriodTmp[_iVar];
        }
        // move to outside
        //if ((iHRPeriodUpper-iHRPeriodLower) > iHRPeriodUpper / 2){
        //    mIntHRErrCode |= BINARY_ERR_CODE_HR_UNSTABLE;
        //}

        return (int)_fResult;//iValidHrPeriodMean;
    }

    public int iMeanHRPeriod190219(int[] _iArrHRAccepted, int[] _intArrayMaxIdx,int[] _iArrVpkPosition,int[] _iArrayHRPStart ,int[] _iArrayHRPEnd, int _iHRCount){
        _iHRCount = _iHRCount-1;
        int[] _iArrayHRPeriod = new int[_iHRCount], _iArrayHRPeriodTmp = new int[_iHRCount];
        int[] _iArrayHRStart = new int[_iHRCount];
        int[] _iArrayHREnd = new int[_iHRCount];
        int[] _iArrayHRLength = new int[_iHRCount];
        for (int _iVar=0; _iVar< _iHRCount; _iVar++) {
            //if(_iArrHRAcceptedDel[_iVar] == 1 && mDoubleArraySignalPowerIdx[_iArrVpkPosition[_iVar]] >= 100)
            int _iVpkIdx = _iArrVpkPosition[_iVar];
            double _fVpkCali = fVPKCali(_intArrayMaxIdx[_iVpkIdx]);
            if (_iArrHRAccepted[_iVar] == 0) {
                _iArrayHRPeriod[_iVar] = 0;
            }else {
                _iArrayHRPeriod[_iVar] = _iArrayHRPEnd[_iVar] - _iArrayHRPStart[_iVar];
            }
            _iArrayHRPeriodTmp[_iVar] = _iArrayHRPeriod[_iVar];
        }
        Arrays.sort(_iArrayHRPeriod);
        for (int _iVar = 0; _iVar< _iHRCount; _iVar++) {
            _iArrayHRStart[_iVar] = _iVar;
            _iArrayHREnd[_iVar] = _iVar;
            _iArrayHRLength[_iVar] = 0;
            for (int _iVar1 = _iVar; _iVar1 < _iHRCount; _iVar1++) {
                if ((_iArrayHRPeriod[_iVar1] - _iArrayHRPeriod[_iVar]) <= _iArrayHRPeriod[_iVar1] * 0.15) {
                    _iArrayHREnd[_iVar] = _iVar1;
                    _iArrayHRLength[_iVar] += _iArrayHRPeriod[_iVar1];
                }else{
                    break;
                }
            }
        }
        int _iLengthMaxVal = Integer.MIN_VALUE;
        int _iLengthMaxIdx = 0;
        for (int _iVar = 0; _iVar< _iHRCount; _iVar++) {
            if (_iLengthMaxVal <= _iArrayHRLength[_iVar]){
                _iLengthMaxVal = _iArrayHRLength[_iVar];
                _iLengthMaxIdx = _iVar;
            }
        }
        float _fResult = _iLengthMaxVal / (_iArrayHREnd[_iLengthMaxIdx]-_iLengthMaxIdx +1 );

        for (int _iVar=0; _iVar< _iHRCount; _iVar++) {
            if (Math.abs(_iArrayHRPeriodTmp[_iVar] - _fResult) > _fResult * 0.15)
                _iArrHRAccepted[_iVar] = 0;
        }

        int iHRPeriodUpper = 0;
        int iHRPeriodLower = 0;
        int iHRPeriodHalf = _iHRCount/2;

        for (int _iVar = 0; _iVar< iHRPeriodHalf; _iVar++) {
            iHRPeriodLower += _iArrayHRPeriod[_iVar];
        }

        for (int _iVar = (_iHRCount-iHRPeriodHalf) ; _iVar< _iHRCount; _iVar++) {
            iHRPeriodUpper += _iArrayHRPeriod[_iVar];
        }

        //if ((iHRPeriodUpper-iHRPeriodLower) > iHRPeriodUpper/3){
        //    mIntHRErrCode |= BINARY_ERR_CODE_HR_UNSTABLE;
        //}

        return (int) _fResult;
    }

    public int iFindHRPeakByFixLength(int[] _mIntArrayPosition_Output, int[] _mIntInputArray_Input, int _iStart, int _iEnd, int _iHRPeriodLength, int _iSearchFeatureLength){
        int _iExtremeMax = Integer.MIN_VALUE;
        int _iExtremePosition;
        int _iExtremeCounts = 0;

        _mIntArrayPosition_Output[0]=_iStart;
        _iExtremePosition = _iStart;
        _iExtremeCounts ++;

        for (int _iVar = _iStart; _iVar < _iEnd; _iVar ++) {
            int _iSearchStart = _iVar +_iHRPeriodLength - _iSearchFeatureLength;
            int _iSearchEnd = _iVar +_iHRPeriodLength +_iSearchFeatureLength;
            if (_iSearchStart<0) _iSearchStart=0;
            if (_iSearchEnd > _iEnd) _iSearchEnd=_iEnd;
            if(_iSearchStart > _iSearchEnd) break;
            _iExtremeMax = Integer.MIN_VALUE;
            for (int _iVar2=_iSearchStart; _iVar2<=_iSearchEnd; _iVar2++) {
                if (_iExtremeMax < _mIntInputArray_Input[_iVar2]) {
                    _iExtremeMax = _mIntInputArray_Input[_iVar2];
                    _iExtremePosition = _iVar2;
                }
            }
            _mIntArrayPosition_Output[_iExtremeCounts]=_iExtremePosition;
            _iExtremeCounts++;
            _iVar = _iExtremePosition;

        }
        return _iExtremeCounts;
    }


    public int findWaveValleyPeakPosition(int[] _mIntArrayValleyPosition_Output, int[] _mIntArrayPeakPosition_Output, int[] _mIntInputArray_Input, int[] _mIntInputArray_Input2, int _start, int _end, int _iNoiseWidth, int _iNoiseAmp){
        boolean _bPeakSearch = false;
        int _iValleyAnyNums = 0;
        int _iValleyExtremeNums;
        int[] _mIntArrayAnyValleyPosition = new int[300];
        int[] _mIntArrayAnyPeakPosition = new int[300];
        boolean[] _mBoolArrayAnyTruly = new boolean[300];
        int _iPositionIdx_Cur, _iPositionValue_Cur;
        int _iPositionIdx_Pre = _start, _iPositionValue_Pre = _mIntInputArray_Input[_start];
        int _iWaveCondition =0 ;
        //  find Valley & Peak
        for (int _iVar = _start; _iVar < _end; _iVar ++) {
            _iPositionIdx_Cur = _iVar;
            _iPositionValue_Cur = _mIntInputArray_Input[_iPositionIdx_Cur];
            // bPeakSearch == false ; search Valley
            // bPeakSearch == true ; search Peak
            if (_iPositionIdx_Cur!=_iPositionIdx_Pre) {
                if((Math.abs(_iPositionValue_Cur-_iPositionValue_Pre)< _iNoiseAmp)||(_iPositionIdx_Cur-_iPositionValue_Pre)<_iNoiseWidth){
                    if (((_iPositionValue_Cur < _iPositionValue_Pre) && (!_bPeakSearch)) ||
                            ((_iPositionValue_Cur > _iPositionValue_Pre) && (_bPeakSearch))){
                        _iPositionValue_Pre = _iPositionValue_Cur;
                        _iPositionIdx_Pre = _iPositionIdx_Cur;
                    }
                }else{
                    _iWaveCondition = Integer.rotateLeft(_iWaveCondition, 1);
                    if (((_iPositionValue_Cur > _iPositionValue_Pre) && (!_bPeakSearch)) ||    //upward
                            ((_iPositionValue_Cur < _iPositionValue_Pre) && (_bPeakSearch))) { //downward
                        _iWaveCondition += 1;
                    }else{
                        _iPositionValue_Pre = _iPositionValue_Cur;
                        _iPositionIdx_Pre = _iPositionIdx_Cur;
                    }
                    if ((_iWaveCondition & 0b111) == 0b011) {
                        if(!_bPeakSearch){
                            _mIntArrayAnyValleyPosition[_iValleyAnyNums] =_iPositionIdx_Pre;
                        }else{
                            _mIntArrayAnyPeakPosition[_iValleyAnyNums] =_iPositionIdx_Pre;
                            _iValleyAnyNums ++;
                        }
                        _iPositionValue_Pre = _iPositionValue_Cur;
                        _iPositionIdx_Pre = _iPositionIdx_Cur;
                        _bPeakSearch = !_bPeakSearch;
                        _iWaveCondition = 0xb11111;
                    }
                }
            }
        }


        //  find  ExtremeValley
        //_mIntArrayValleyPosition_Output[0]=_mIntArrayAnyValleyPosition[0];
        _mBoolArrayAnyTruly[0]= true;
        _iValleyExtremeNums=1;

        // First Run: 取較大波峰
        for (int _iVar = 1; _iVar < _iValleyAnyNums; _iVar ++) {
            int _iCompareValue_Max;
            int _iCompareNums = 5;
            int _iCompareStart, _iCompareEnd=0;
            int _iCompareValue=0, _iCompareValue_Min =Integer.MAX_VALUE;
            _iCompareStart=_iVar;
            for (int _iVar2 = _iCompareStart; _iVar2 < _iValleyAnyNums; _iVar2 ++) {
                _iCompareEnd = _iVar2;

                if((_iCompareEnd-_iCompareStart)>=5){
                    break;
                }

                if((_mIntArrayAnyPeakPosition[_iCompareEnd]-_mIntArrayAnyPeakPosition[_iCompareStart])>=250){
                    break;
                }
            }

            _iCompareValue_Max =Integer.MIN_VALUE;
            _iCompareNums = 0;
            for (int _iVar2 = _iCompareStart; _iVar2 < (_iCompareEnd+1); _iVar2 ++) {
                _iCompareValue += _mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iVar2]];
                _iCompareNums ++;
                if (_iCompareValue_Max < _mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iVar2]]){
                    _iCompareValue_Max = _mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iVar2]];
                }
            }
            //_iCompareValue = (_iCompareValue)/(_iCompareNums+1);     //最值做加權
            _iCompareValue = (_iCompareValue + _iCompareValue_Max)/(_iCompareNums+1);     //最值做加權
            // (_mIntArrayAnyValleyValue[_iVar] > _iCompareValue)
            if ((_mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iVar]] > _iCompareValue_Max * 0.5)){
                _mBoolArrayAnyTruly[_iVar]=true;
            }

            //if (Math.abs(_mIntArrayAnyValleyValue[_iVar] - _iCompareValue) < 6){ // 非常相似，可能是沒有雙波峰或更多
            //    _mIntArrayValleyPosition_Output[_iValleyExtremeNums]=_mIntArrayAnyValleyPosition[_iVar];
            //    _iValleyExtremeNums++;
            //}else if ((_mIntArrayAnyValleyValue[_iVar] > _iCompareValue)||(_mIntArrayAnyValleyValue[_iVar]> _iCompareValue_Max*0.8)){
            //    _mIntArrayValleyPosition_Output[_iValleyExtremeNums]=_mIntArrayAnyValleyPosition[_iVar];
            //    _iValleyExtremeNums++;
            //}

        }

        //* Second Run  去除峰峰過窄 <  250 ms about : 30 pts


        //* Second Run  去除峰峰過窄 <  250 ms about : 30 pts  */
        int _iPreTrulyIdx = 0;
        for (int _iVar = 1; _iVar < _iValleyAnyNums; _iVar ++) {
            if (_mBoolArrayAnyTruly[_iVar]) {
                if ((_mIntArrayAnyPeakPosition[_iVar]-_mIntArrayAnyPeakPosition[_iPreTrulyIdx]) <= 30){
                    if(_mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iVar]]>_mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iPreTrulyIdx]]) {
                        _mBoolArrayAnyTruly[_iPreTrulyIdx] = false;
                        _iPreTrulyIdx = _iVar;
                    }else{
                        _mBoolArrayAnyTruly[_iVar] = false;
                    }
                }else{
                    _iPreTrulyIdx = _iVar;
                }
            }
        }


        //* Third Run  前後峰比對
        //先找第二真實點
        int _iCountTruly = 0;
        for (int _iVar = 0; _iVar < _iValleyAnyNums; _iVar ++) {
            if (_mBoolArrayAnyTruly[_iVar]){
                _iCountTruly++;
                if(_iCountTruly==2){
                    _iCountTruly = _iVar;
                    break;
                }
            }
        }


        for (int _iVar = _iCountTruly; _iVar < _iValleyAnyNums; _iVar ++) {
            int _iWidth = 8;
            int _iBeforeIdx, _iAfterIdx;
            int[] _arIBeAfIdx = new int[2];
            int _iBeforeValue, _iAfterValue, _iCurrentValue, _iBeforeAfterMeanValue;
            int _iBeforeValueR, _iAfterValueR, _iCurrentValueR, _iBeforeAfterMeanValueR=0;
            int _iBeforeAfterSpan, _iBeforeSpan, _iAfterSpan;
            int _iValleyPeakWidthAfter, _iValleyPeakWidthCurrent, _iValleyPeakWidthBefore;
            double _doubleCurrentSlope, _doubleBeforeSlope, _doubleAfterSlope, _doubleSlopeMean;

            searchPeakBeforeAndAfter(_arIBeAfIdx, _iVar,_iValleyAnyNums , _mBoolArrayAnyTruly);
            _iBeforeIdx=_arIBeAfIdx[0];
            _iAfterIdx=_arIBeAfIdx[1];
            if(_mBoolArrayAnyTruly[_iVar]) {
//                for (int _iVar2 = -1; _iVar2 <= 1; _iVar2++) {
                    //_iBeforeValue += _mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iBeforeIdx] + _iVar2];
                    //_iCurrentValue += _mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iVar] + _iVar2];
                    //_iAfterValue += _mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iAfterIdx] + _iVar2];
                    //_iBeforeAfterMeanValue = (_iAfterValue+_iBeforeValue)/2;
//                }

                //_iBeforeValue = _mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iBeforeIdx]]
                //                    -_mIntInputArray_Input[_mIntArrayAnyValleyPosition[_iBeforeIdx]];
                //_iCurrentValue = _mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iVar]]
                //        -_mIntInputArray_Input[_mIntArrayAnyValleyPosition[_iVar]];
                //_iAfterValue = _mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iAfterIdx]]
                //        -_mIntInputArray_Input[_mIntArrayAnyValleyPosition[_iAfterIdx]];

                //_iBeforeValueR = _mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iBeforeIdx]]
                //        -_mIntInputArray_Input[_mIntArrayAnyValleyPosition[_iBeforeIdx+1]];
                //_iCurrentValueR = _mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iVar]]
                 //       -_mIntInputArray_Input[_mIntArrayAnyValleyPosition[_iVar+1]];
                //_iAfterValueR = _mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iAfterIdx]]
                //        -_mIntInputArray_Input[_mIntArrayAnyValleyPosition[_iAfterIdx+1]];

                _iBeforeValue = _mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iBeforeIdx]]
                        -_mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iBeforeIdx]-_iWidth];
                _iCurrentValue = _mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iVar]]
                        -_mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iVar]-_iWidth];
                _iAfterValue = _mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iAfterIdx]]
                        -_mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iAfterIdx]-_iWidth];

                _iBeforeValueR = _mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iBeforeIdx]]
                        -_mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iBeforeIdx]+_iWidth];
                _iCurrentValueR = _mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iVar]]
                        -_mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iVar]+_iWidth];
                _iAfterValueR = _mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iAfterIdx]]
                        -_mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iAfterIdx]+_iWidth];





                _iBeforeAfterMeanValue = (_iAfterValue+_iBeforeValue)/2;

                _iBeforeAfterSpan=(_mIntArrayAnyPeakPosition[_iAfterIdx] - _mIntArrayAnyPeakPosition[_iBeforeIdx]);
                _iBeforeSpan=(_mIntArrayAnyPeakPosition[_iVar] - _mIntArrayAnyPeakPosition[_iBeforeIdx]);
                _iAfterSpan=(_mIntArrayAnyPeakPosition[_iAfterIdx] - _mIntArrayAnyPeakPosition[_iVar]);

                //_doubleBeforeSlope = (double) _iBeforeValue
                //        /(_mIntArrayAnyPeakPosition[_iBeforeIdx]-_mIntArrayAnyValleyPosition[_iBeforeIdx])
                //                + (double)_iBeforeValueR
                //        /(_mIntArrayAnyValleyPosition[_iBeforeIdx+1]-_mIntArrayAnyPeakPosition[_iBeforeIdx]);
                //_doubleCurrentSlope = (double) _iCurrentValue
                //        /(_mIntArrayAnyPeakPosition[_iVar]-_mIntArrayAnyValleyPosition[_iVar])
                //                + (double) _iCurrentValueR
                //        /(_mIntArrayAnyValleyPosition[_iVar+1]-_mIntArrayAnyPeakPosition[_iVar]);
                //_doubleAfterSlope = (double) _iAfterValue
                //        /(_mIntArrayAnyPeakPosition[_iAfterIdx]-_mIntArrayAnyValleyPosition[_iAfterIdx])
                //                + (double) _iAfterValueR
                //        /(_mIntArrayAnyValleyPosition[_iAfterIdx+1]-_mIntArrayAnyPeakPosition[_iAfterIdx]);


                _doubleBeforeSlope = (double) _iBeforeValue/(_iWidth)
                                + (double)_iBeforeValueR /(_iWidth);
                _doubleCurrentSlope = (double) _iCurrentValue /(_iWidth)
                                + (double) _iCurrentValueR /(_iWidth);
                _doubleAfterSlope = (double) _iAfterValue /(_iWidth)
                                + (double) _iAfterValueR /(_iWidth);
                _doubleSlopeMean = (_doubleBeforeSlope+_doubleAfterSlope)/2;

                _iValleyPeakWidthBefore = _mIntArrayAnyPeakPosition[_iBeforeIdx]-_mIntArrayAnyValleyPosition[_iBeforeIdx];
                _iValleyPeakWidthCurrent = _mIntArrayAnyPeakPosition[_iVar]-_mIntArrayAnyValleyPosition[_iVar];
                _iValleyPeakWidthAfter = _mIntArrayAnyPeakPosition[_iAfterIdx]-_mIntArrayAnyValleyPosition[_iAfterIdx];



                for (int _iVar2 = -1; _iVar2 <= 1; _iVar2++) {
                    _iBeforeValue += _mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iBeforeIdx] + _iVar2];
                    _iCurrentValue += _mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iVar] + _iVar2];
                    _iAfterValue += _mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iAfterIdx] + _iVar2];
                }
                _iBeforeAfterMeanValue = (_iAfterValue+_iBeforeValue)/2;


                if ((_iBeforeAfterSpan > 250)
                        ||(_iBeforeAfterSpan > 6* (_iBeforeSpan))
                        ||(_iBeforeAfterSpan > 6* (_iAfterSpan))
                        ) {       // 2sec

                } else {
                    if ((Math.abs(_iAfterValue - _iBeforeValue) < _iBeforeAfterMeanValue * 0.5)
                            && (_iCurrentValue < _iBeforeValue * 0.8)
                            && (_iCurrentValue < _iAfterValue * 0.8)
                            ) {
                        _mBoolArrayAnyTruly[_iVar] = false;
                    } else if ((Math.abs(_doubleAfterSlope - _doubleBeforeSlope) < _doubleSlopeMean * 0.4)
                            && (_doubleCurrentSlope < _doubleAfterSlope * 0.8)
                            && (_doubleCurrentSlope < _doubleBeforeSlope * 0.8)
                            ) {
                        _mBoolArrayAnyTruly[_iVar] = false;
                    } else if ((_iValleyPeakWidthCurrent < _iValleyPeakWidthBefore * 0.8)
                            && (_iValleyPeakWidthCurrent < _iValleyPeakWidthAfter * 0.8)
                            ) {
                        _mBoolArrayAnyTruly[_iVar] = false;
                    }
                }
            }
        }
        //* Third Run  前後峰比對  */

        /* Third Run
        for (int _iVar = 1; _iVar < _iValleyAnyNums; _iVar ++) {
            if(true) break;
            // 0:BeforeIdx, 1:AfterIdx
            int[] _arAnBIdx = new int[2];
            int _iSlopeBefore_R, _iSlopeAfter_R, _iSlopeCur_R;
            int _iSlopeBefore_L, _iSlopeAfter_L, _iSlopeCur_L;
            int _iHeightBefore, _iHeightAfter, _iHeightCur;
            if(false){break;}
            searchValleyNext(_iVar, _arAnBIdx, _iValleyAnyNums, _mIntArrayAnyValleyTruly);
            if (((_mIntArrayAnyValleyPosition[_arAnBIdx[1]] - _mIntArrayAnyValleyPosition[_arAnBIdx[0]]) < 250)) {       // 2sec
                int _iTmp;
                _iSlopeBefore_R =    (_mIntInputArray_Input[_mIntArrayAnyValleyPosition[_arAnBIdx[0]] + 6 ]
                        - _mIntInputArray_Input[_mIntArrayAnyValleyPosition[_arAnBIdx[0]]]);
                _iSlopeAfter_R = (_mIntInputArray_Input[_mIntArrayAnyValleyPosition[_arAnBIdx[1]] + 6 ]
                        - _mIntInputArray_Input[_mIntArrayAnyValleyPosition[_arAnBIdx[1]]]);
                _iSlopeCur_R = (_mIntInputArray_Input[_mIntArrayAnyValleyPosition[_iVar] + 6 ]
                        - _mIntInputArray_Input[_mIntArrayAnyValleyPosition[_iVar]]);
                _iTmp = _arAnBIdx[0];
                if (_iTmp==0){
                    _iSlopeBefore_L = _iSlopeBefore_R;
                }else {
                    _iTmp = _mIntArrayAnyValleyPosition[_arAnBIdx[0]] - 6;
                    if(_iTmp<0) _iTmp =0;
                    _iSlopeBefore_L = (_mIntInputArray_Input[_iTmp]
                            - _mIntInputArray_Input[_mIntArrayAnyValleyPosition[_arAnBIdx[0]]]);
                }

//               _iSlopeAfter_L = (_mIntInputArray_Input[_mIntArrayAnyValleyPosition[_arAnBIdx[1]] - 6 ]
//                        - _mIntInputArray_Input[_mIntArrayAnyValleyPosition[_arAnBIdx[1]]]);
                _iTmp = _mIntArrayAnyValleyPosition[_arAnBIdx[1]] - 6;
                if(_iTmp<0) _iTmp =0;
                _iSlopeAfter_L = (_mIntInputArray_Input[_iTmp ]
                        - _mIntInputArray_Input[_mIntArrayAnyValleyPosition[_arAnBIdx[1]]]);
                _iTmp = _mIntArrayAnyValleyPosition[_iVar] - 6 ;
                if(_iTmp<0) _iTmp =0;
                _iSlopeCur_L = (_mIntInputArray_Input[ _iTmp ]
                        - _mIntInputArray_Input[_mIntArrayAnyValleyPosition[_iVar]]);



                _iHeightBefore = _mIntArrayAnyPeakValue[_arAnBIdx[0] ];
                _iHeightAfter = _mIntArrayAnyPeakValue[_arAnBIdx[1] ];
                _iHeightCur = _mIntArrayAnyPeakValue[_iVar];
                //if ((_iSlopeAfter_R > _iSlopeCur_R    && _iSlopeBefore_R> _iSlopeCur_R)//){
                //        && (_arAnBIdx[1] - _arAnBIdx[0] < 3 )){
                //    _mIntArrayAnyValleyTruly[_iVar]=false;
                //}
                //if ((_iSlopeAfter_L > _iSlopeCur_L    && _iSlopeBefore_L> _iSlopeCur_L)//){
                //        && (_arAnBIdx[1] - _arAnBIdx[0] < 3 )){
                //    _mIntArrayAnyValleyTruly[_iVar]=false;
                //}

            }
        }
        //* Third Run  */



        _iValleyExtremeNums=0;
        for (int _iVar = 0; _iVar < _iValleyAnyNums; _iVar ++) {
            if(_mBoolArrayAnyTruly[_iVar]) {
                _mIntArrayPeakPosition_Output[_iValleyExtremeNums] = _mIntArrayAnyPeakPosition[_iVar];
                _mIntArrayValleyPosition_Output[_iValleyExtremeNums] = _mIntArrayAnyValleyPosition[_iVar];
                _iValleyExtremeNums++;
            }
        }


        return _iValleyExtremeNums;

    }


    public int iFindFirstHRPeriodByPeakMode(int[] _mIntInputArray_Input, int _start, int _end, int _iNoiseWidth, int _iNoiseAmp){
        boolean _bPeakSearch = true;
        int _iValleyAnyNums = 0;
        int _iValleyExtremeNums;
        int _iValleyPeriod;
        int[] _mIntArrayAnyValleyPosition = new int[100];
        int[] _mIntArrayAnyValleyValue = new int[100];
        int[] _mIntArrayAnyPeakPosition = new int[100];
        boolean[] _mIntArrayAnyValleyTruly = new boolean[100];
        int _iPositionIdx_Cur, _iPositionValue_Cur;
        int _iPositionIdx_Pre = _start, _iPositionValue_Pre = _mIntInputArray_Input[_start];
        int _iWaveCondition =0 ;
        //  find Valley & Peak
        for (int _iVar = _start; _iVar < _end; _iVar ++) {
            _iPositionIdx_Cur = _iVar;
            _iPositionValue_Cur = _mIntInputArray_Input[_iPositionIdx_Cur];
            // bPeakSearch == false ; search Valley
            // bPeakSearch == true ; search Peak
            if (_iPositionIdx_Cur!=_iPositionIdx_Pre) {
                if((Math.abs(_iPositionValue_Cur-_iPositionValue_Pre)< _iNoiseAmp)||(_iPositionIdx_Cur-_iPositionValue_Pre)<_iNoiseWidth){
                    if (((_iPositionValue_Cur < _iPositionValue_Pre) && (!_bPeakSearch)) ||
                            ((_iPositionValue_Cur > _iPositionValue_Pre) && (_bPeakSearch))){
                        _iPositionValue_Pre = _iPositionValue_Cur;
                        _iPositionIdx_Pre = _iPositionIdx_Cur;
                    }
                }else{
                    _iWaveCondition = Integer.rotateLeft(_iWaveCondition, 1);
                    if (((_iPositionValue_Cur > _iPositionValue_Pre) && (!_bPeakSearch)) ||    //upward
                            ((_iPositionValue_Cur < _iPositionValue_Pre) && (_bPeakSearch))) { //downward
                        _iWaveCondition += 1;
                    }else{
                        _iPositionValue_Pre = _iPositionValue_Cur;
                        _iPositionIdx_Pre = _iPositionIdx_Cur;
                    }
                    if ((_iWaveCondition & 0b111) == 0b011) {
                        if(!_bPeakSearch){
                            _mIntArrayAnyValleyPosition[_iValleyAnyNums] =_iPositionIdx_Pre;
                            _iValleyAnyNums ++;
                        }else{
                            _mIntArrayAnyPeakPosition[_iValleyAnyNums] =_iPositionIdx_Pre;
                        }
                        _iPositionValue_Pre = _iPositionValue_Cur;
                        _iPositionIdx_Pre = _iPositionIdx_Cur;
                        _bPeakSearch = !_bPeakSearch;
                        _iWaveCondition = 0xb111;
                    }
                }
            }
        }
        for (int _iVar = 1; _iVar < _iValleyAnyNums; _iVar ++) {
            _mIntArrayAnyValleyValue[_iVar] = (_mIntInputArray_Input[_mIntArrayAnyPeakPosition[0]]-_mIntInputArray_Input[_mIntArrayAnyValleyPosition[0]]);
        }


        //  find  ExtremeValley
        //_mIntArrayValleyPosition_Output[0]=_mIntArrayAnyValleyPosition[0];
        _mIntArrayAnyValleyTruly[0]= true;
        int _iPreExtreme = 0;
        _iValleyExtremeNums=1;

        // First Run
        //找基準點
        int _iCompareValue_Max=Integer.MIN_VALUE;
        int _iCompareNums = 5;
        int _iCompareStart, _iCompareEnd;
        int _iCompareValue=0, _iCompareValue_Min =Integer.MAX_VALUE;
        for (int _iVar = 1; _iVar < _iValleyAnyNums; _iVar ++) {
            if (_iVar==1) {     // 只做一次
                _iCompareValue_Max =Integer.MIN_VALUE;
                _iCompareStart = _iVar;
                _iCompareEnd = 6;
                if (_iCompareEnd>_iValleyAnyNums) {
                    _iCompareEnd = _iValleyAnyNums;
                }
                _iCompareNums = 0;
                for (int _iVar2 = _iCompareStart; _iVar2 < _iCompareEnd; _iVar2 ++) {
                    _iCompareValue += _mIntArrayAnyValleyValue[_iVar2];
                    _iCompareNums ++;
                    if (_iCompareValue_Max < _mIntArrayAnyValleyValue[_iVar2]){
                        _iCompareValue_Max = _mIntArrayAnyValleyValue[_iVar2];
                    }
                }
                _iCompareValue = (_iCompareValue + _iCompareValue_Max)/(_iCompareNums+1);     //最值做加權

            }

            if ((_mIntArrayAnyValleyValue[_iVar] > _iCompareValue)||(_mIntArrayAnyValleyValue[_iVar] > _iCompareValue_Max * 0.5)){
                _mIntArrayAnyValleyTruly[_iVar] = true;
                _iValleyExtremeNums++;
                _iPreExtreme = _iVar;
                //_iValleyPeriod = _mIntArrayAnyValleyPosition[_iVar]-_mIntArrayAnyValleyPosition[0];
//                    return _iValleyPeriod;
            }

        }

//Second Run
        int _iValleyExtremeNums2 =0;
        _iValleyPeriod=0;
        _iPositionIdx_Pre = 0;
        int _iPositionIdx_Middle = 0;
        for (int _iVar = 1; _iVar < _iValleyAnyNums; _iVar ++) {
            if (_mIntArrayAnyValleyTruly[_iVar]){
                if (_iValleyExtremeNums2 > 0){      //先看2個波...
                    int _SlopePre = _mIntInputArray_Input[_mIntArrayAnyValleyPosition[_iPositionIdx_Pre]+5]
                            - _mIntInputArray_Input[_mIntArrayAnyValleyPosition[_iPositionIdx_Pre]];
                    int _SlopeCur = _mIntInputArray_Input[_mIntArrayAnyValleyPosition[_iVar]+5]
                            - _mIntInputArray_Input[_mIntArrayAnyValleyPosition[_iVar]];
                    int _SpanPre = _mIntArrayAnyValleyPosition[_iPositionIdx_Pre]
                            - _mIntArrayAnyValleyPosition[0];
                    int _SpanCur = _mIntArrayAnyValleyPosition[_iVar]
                            - _mIntArrayAnyValleyPosition[_iPositionIdx_Pre];
                    if(Math.abs(_mIntArrayAnyValleyValue[_iPositionIdx_Pre]-_mIntArrayAnyValleyValue[_iVar]) <= Math.abs(_mIntArrayAnyValleyValue[_iPositionIdx_Pre]+_mIntArrayAnyValleyValue[_iVar]) / 2* 0.2 ) {
                        _iValleyPeriod = _mIntArrayAnyValleyPosition[_iPositionIdx_Pre]-_mIntArrayAnyValleyPosition[0]; // 兩個波相似, 取前者
//                    }else if (_mIntInputArray_Input[_mIntArrayAnyValleyPosition[_iPositionIdx_Pre]] < _mIntInputArray_Input[_mIntArrayAnyValleyPosition[_iVar]] * 0.9
//                            && _mIntInputArray_Input[_mIntArrayAnyValleyPosition[_iPositionIdx_Pre]] < _mIntInputArray_Input[_mIntArrayAnyValleyPosition[0]] * 0.9
//                            ){  //原訊號比較，若前訊號較小，取後者
//                        _iValleyPeriod = _mIntArrayAnyValleyPosition[_iVar]-_mIntArrayAnyValleyPosition[0];
                    }else if (_mIntArrayAnyValleyValue[_iVar] > _mIntArrayAnyValleyValue[_iPositionIdx_Pre] * 1.1){  //後者振幅較大， 取者後者
                        _iValleyPeriod = _mIntArrayAnyValleyPosition[_iPositionIdx_Pre]-_mIntArrayAnyValleyPosition[0];
                    } else if (Math.abs(_SpanPre-_SpanCur) >= ((Math.abs(_SpanPre + _SpanCur)/2) * 0.2)){  //表示兩者間距不一樣，取後者
                        _iValleyPeriod = _mIntArrayAnyValleyPosition[_iVar]-_mIntArrayAnyValleyPosition[0];
                    } else if (Math.abs(_SlopePre-_SlopeCur) >= ((Math.abs(_SlopePre + _SlopeCur)/2) * 0.3)){  //表示兩者斜率不一樣，取後者
                        _iValleyPeriod = _mIntArrayAnyValleyPosition[_iVar]-_mIntArrayAnyValleyPosition[0];
                    }else if (_iPositionIdx_Pre != (_iVar-_iPositionIdx_Pre)){
                        _iValleyPeriod = _mIntArrayAnyValleyPosition[_iVar]-_mIntArrayAnyValleyPosition[0];     // 兩個波型的特徵不同, 取後者
                    }else{
                        _iValleyPeriod = _mIntArrayAnyValleyPosition[_iPositionIdx_Pre] - _mIntArrayAnyValleyPosition[0];     // 預設, 取前者
                        //if (_iValleyExtremeNums2 > 1) {   // 再看第3個波
                        //    if ((Math.abs(_SlopePre-_SlopeCur) >= ((Math.abs(_SlopePre + _SlopeCur)/2) * 0.2)))
                        //    _iValleyPeriod = _mIntArrayAnyValleyPosition[_iPositionIdx_Pre] - _mIntArrayAnyValleyPosition[0];     // 預設, 取前者
                        //}
                        //_iValleyExtremeNums2 ++;
                        //_iPositionIdx_Middle = _iVar;
                    }
                    break;
                }else {
                    _iPositionIdx_Pre = _iVar;
                    _iValleyExtremeNums2++;
                    if (_iValleyExtremeNums == 2){      // 只有乙個波, 無需比對
                        _iValleyPeriod = _mIntArrayAnyValleyPosition[_iVar]-_mIntArrayAnyValleyPosition[0];
                        break;
                    }
                }
            }
        }

        if (_iValleyPeriod == 0) _iValleyPeriod = 125;
        return _iValleyPeriod;

    }

    public int[] isSimilarVote(int[] isCh){
        int iLen = isCh.length;
        int[] isSimilarCnt = new int[isCh.length];
        int[] isResult = new int[3]; //int[0] = Heart Count, int[1] = PeriodMean, int[2] = NoneZeroMean
        Arrays.sort(isCh);
        int iSum = 0;
        int iSumCnt = 0;
        for (int iVar = 0; iVar < iLen; iVar++){
            int iCnt = 0;
            if (isCh[iVar] != 0) {
                iSum = iSum + isCh[iVar];
                iSumCnt ++;
            }
            for (int iVar1 = iVar; iVar1 < iLen; iVar1++){
                if ((isCh[iVar1] == 0) || (isCh[iVar1] - isCh[iVar]) > (int)((float)isCh[iVar] * 0.2)){
                    break;
                }
                iCnt++;
                isSimilarCnt[iVar] = iCnt;
            }
        }
        int iPos = 0;
        // 找最相似區段
        for (int iVar = 0; iVar < iLen; iVar++){
            if (isSimilarCnt[iVar] > isSimilarCnt[iPos]){
                iPos = iVar;
            }
        }

        //求平均
        int iMean = 0;
        for (int iVar = iPos; iVar < iPos + isSimilarCnt[iPos]; iVar++){
            iMean += isCh[iVar];
        }
        if (isSimilarCnt[iPos] == 0) {
            iMean = 0;
        }else {
            iMean = iMean / isSimilarCnt[iPos];
        }

        if (iSumCnt !=0){
            iSum = iSum/iSumCnt;
        }
        isResult[0] = isSimilarCnt[iPos];
        isResult[1] = iMean;
        isResult[2] = iSum;
        return isResult;
    }

    public int iFindPeriodByPeak(int[] isCh, int iWnd){
        int iLen = isCh.length;
        int iStart = 0;
        int iCnt = 10;       //  the maximal cnts of peak is 5
        int[] isPeak = new int[iCnt];
        int[] isPeakPos = new int[iCnt];
        int[] isValley = new int[iCnt];
        int[] isValleyPos = new int[iCnt];

        boolean bPeak = false;
        int iPeakCnt = 0;

        int iPreValleyVal = Integer.MAX_VALUE;
        int iPreValleyPos = 0;
        int iPrePeakVal = Integer.MIN_VALUE;
        int iPrePeakPos = 0;

        int iThisValleyVal;
        int iThisValleyPos = 0;
        int iThisPeakVal = Integer.MIN_VALUE;
        int iThisPeakPos = 0;


        for (int iVar = 0; iVar < iLen; iVar++){
            int iWndStart = iVar;
            int iWndEnd = iWndStart + iWnd;
            if (iWndEnd > iLen) iWndEnd = iLen;

            if (!bPeak){
                iPrePeakVal = Integer.MIN_VALUE;
                iThisValleyVal = Integer.MAX_VALUE;
                // Search this section Minimal Value
                for(int iVar1 = iWndStart; iVar1 < iWndEnd; iVar1++){
                    iVar = iVar1;
                    if (isCh[iVar1] < iThisValleyVal) {
                        iThisValleyVal = isCh[iVar1];
                        iThisValleyPos = iVar1;
                    }
                }
                // if iThisVally is  the Minimal, search again
                if (iThisValleyVal < iPreValleyVal) {
                    iPreValleyVal = iThisValleyVal;
                    iPreValleyPos = iThisValleyPos;
                }else{
                    bPeak = true;
                    iThisValleyVal = iPreValleyVal;
                    iThisValleyPos = iPreValleyPos;
                    iVar = iThisValleyPos;
                    //final
                    isValley[iPeakCnt] = iThisValleyVal;
                    isValleyPos[iPeakCnt] = iThisValleyPos;
                }
            }else{
                iPreValleyVal = Integer.MAX_VALUE;
                iThisPeakVal = Integer.MIN_VALUE;
                // Search this section Minimal Value
                for(int iVar1 = iWndStart; iVar1 < iWndEnd; iVar1++){
                    iVar = iVar1;
                    if (isCh[iVar1] > iThisPeakVal) {
                        iThisPeakVal = isCh[iVar1];
                        iThisPeakPos = iVar1;
                    }
                }
                // if iThisPeak is  the Maximal, search again
                if (iThisPeakVal > iPrePeakVal) {
                    iPrePeakVal = iThisPeakVal;
                    iPrePeakPos = iThisPeakPos;
                }else{
                    bPeak = false;
                    iThisPeakVal = iPrePeakVal;
                    iThisPeakPos = iPrePeakPos;
                    iVar = iPrePeakPos;

                    //final
                    isPeak[iPeakCnt] = iThisPeakVal;
                    isPeakPos[iPeakCnt] = iThisPeakPos;
                    iPeakCnt++;
                    if(iPeakCnt == iCnt) break;
                }
            }
        }

/* Offset
        for (int iVar = 0; iVar < iPeakCnt; iVar++){
            isPeak[iVar] = isPeak[iVar] - isValley[iVar];
        }
//* Offset */

// Find First Peak
        for(int iVar = 0; iVar < iPeakCnt; iVar++){
            if(iVar == 0){
                iThisPeakVal = isPeak[iVar];
                iThisPeakPos = isPeakPos[iVar];
            }else{
                if (isPeak[iVar] > (int)((float) iThisPeakVal) * 1.1){
                    iThisPeakVal = isPeak[iVar];
                    iThisPeakPos = isPeakPos[iVar];
                }
            }
        }

/* j,
        int iPeakMaxVal = Integer.MIN_VALUE;
        int iValleyMinVal = Integer.MAX_VALUE;

        for(int iVar = 0; iVar < iPeakCnt; iVar++){
            if (isPeak[iVar] > iPeakMaxVal){
                iPeakMaxVal = isPeak[iVar];
            }
            if (isValley[iVar] < iValleyMinVal){
                iValleyMinVal = isValley[iVar];
            }
        }

        if ((iThisPeakPos < 40) || (iThisPeakPos > 250) || ((iPeakMaxVal - iValleyMinVal) < 30)){
            iThisPeakPos = 0;                       // Fail
        }
//* j,
//*/

        return iThisPeakPos;
    }


    public int iFindFirstHRPeriodByMixed(int[] isMaxIdx_Raw, int[] isMaxIdx_Bin, int iStart, int iPatternWidth, int iCompareLength) {
        int[] isPatternBinary = new int[iCompareLength];
        int[] isPatternAnalog = new int[iCompareLength];
        int[] isPatternWave = new int[iCompareLength];
        int iPeriod;


    // binary
        for (int iVar = 0; iVar < iCompareLength; iVar++) {
            int bOneCnt = 0;
            for (int iVar1 = 0; iVar1 < iPatternWidth; iVar1++) {
                if (((isMaxIdx_Bin[iStart + iVar + iVar1] ^ isMaxIdx_Bin[iStart + iVar1]) & 0x01) == 0x00) {
                    bOneCnt++;

                }
            }
            isPatternBinary[iVar] = bOneCnt;
        }

        // Analog
        int iAmpMax = Integer.MIN_VALUE;
        for (int iVar = 0; iVar < iCompareLength; iVar++) {
            int iAnalogSum = 0;
            for (int iVar1 = 0; iVar1 < iPatternWidth; iVar1++) {
                iAnalogSum += Math.abs(isMaxIdx_Raw[iStart + iVar + iVar1] - isMaxIdx_Raw[iStart + iVar1]);
            }
            if (iAmpMax < iAnalogSum){
                iAmpMax = iAnalogSum;
            }
            isPatternAnalog[iVar] = iAnalogSum;
        }

        //Mixed
        double fGain = 1.0 * ((double)iPatternWidth) / ((double)iAmpMax);
        for (int iVar = 0; iVar < iCompareLength; iVar++) {
            isPatternAnalog[iVar] = 0 - (int)((double)isPatternAnalog[iVar] * fGain);
            isPatternWave[iVar] = isPatternBinary[iVar] + isPatternAnalog[iVar];
        }

        //int[] iP = new int[3];
        //iP[0] = iFindPeriodByPeak(isPatternBinary,30);
        //iP[1] = iFindPeriodByPeak(isPatternAnalog, 30);
        //iP[2] = iFindPeriodByPeak(isPatternWave, 30);
        //Arrays.sort(iP);
        iPeriod = iFindPeriodByPeak(isPatternWave, 30);    // 125/sec, 約240bpm

        double fCompareHalf;
        double fPreHalf = 0;
        double fAfterHalf = 0;
        double fCompareTwo;
        double fFirst = 0;
        double fSecond = 0;

    //* 190322, Analog 前後比較, 好像作用不大??
        for (int iVar = iStart; iVar < iStart + iPeriod; iVar++) {
            fFirst += isMaxIdx_Raw[iVar];
            fSecond += isMaxIdx_Raw[iVar + iPeriod];
        }
        fCompareTwo = fFirst / fSecond;
        if ((isPatternWave[iPeriod] > 50) && iPeriod < 125 && (fCompareTwo > 1.3 || fCompareTwo < 0.7)) {    // 前後波差異大於 30%
            iPeriod = iPeriod * 2;
        }


        int iHalfCnt = iPeriod/ 2;
        for (int iVar = iStart; iVar < iStart +iHalfCnt; iVar++) {
            fPreHalf += isMaxIdx_Raw[iVar];
            fAfterHalf += isMaxIdx_Raw[iVar + iHalfCnt];
        }
        fCompareHalf = fPreHalf / fAfterHalf;
        if ((isPatternWave[iPeriod] > 50) && iPeriod > 125 && (fCompareHalf < 1.1 && fCompareHalf > 0.9)) { // 前後半波差異小於 10%
            iPeriod = iPeriod / 2;
        }
        //*/

        if (((isPatternWave[iPeriod] < 50) || iPeriod < 40 || iPeriod > 250)) {
            iPeriod = 0;
        }
        return iPeriod;

    /* 190322, Binary
        int iHalfCnt = iPeriod/ 2;
        for (int iVar = iStart; iVar < iHalfCnt; iVar++) {
            //fCompareHalf += (int) (isMaxIdx_Bin[iVar] ^ isMaxIdx_Bin[iVar + iHalfCnt]) & 0x01;
            fAfterHalf += (int) (isMaxIdx_Bin[iVar]);
            fPreHalf += (int) (isMaxIdx_Bin[iVar + iHalfCnt]);
        }
        //fCompareHalf = fCompareHalf / (double) iHalfCnt;
        fCompareHalf = fAfterHalf / fPreHalf;
        if (iPeriod > 125 && (fCompareHalf < 1.15 && fCompareHalf > 0.85)) { // 前後半波差異小於 15%
            iPeriod = iPeriod / 2;
        }
        // */

    }

    public int iFindFirstHRPeriodByBinary(int[] isMaxIdx_Raw, int[] isMaxIdx_Bin, int iStart, int iPatternWidth, int iCompareLength) {
        int[] isPatternWave = new int[iCompareLength];
        int iPeriod;
        int iOnesCnt = 0;
        int iOnesContinueMax = Integer.MIN_VALUE;
        int iOnesContinueCnt = 0;

        /*j, Comparison SUM(1) and Continue to determine the Heart Quality
        for (int iVar = iStart; iVar < iStart + iPatternWidth; iVar++){
            if (isMaxIdx_Bin[iVar] == 1) {
                iOnesCnt++;
                if(iVar > 0 && isMaxIdx_Bin[iVar-1]==1){
                    iOnesContinueCnt ++;
                    if (iOnesContinueCnt > iOnesContinueMax) {
                        iOnesContinueMax = iOnesContinueCnt;
                    }
                } else {
                    iOnesContinueCnt = 0;
                }
            }
        }
        //*j, Comparison SUM(1) and Continue to determine the Heart Quality
        //*/


        for (int iVar = 0; iVar < iCompareLength; iVar++) {
            int bOneCnt = 0;
            for (int iVar1 = 0; iVar1 < iPatternWidth; iVar1++) {
                if (((isMaxIdx_Bin[iStart + iVar + iVar1] ^ isMaxIdx_Bin[iStart + iVar1]) & 0x01) == 0x00) {
                    bOneCnt++;
                }
            }
            isPatternWave[iVar] = bOneCnt;
        }


        iPeriod = iFindPeriodByPeak(isPatternWave, 30);    // 125/sec, 約240bpm

        double fCompareHalf;
        double fPreHalf = 0;
        double fAfterHalf = 0;
        double fCompareTwo;
        double fFirst = 0;
        double fSecond = 0;

        for (int iVar = iStart; iVar < iStart + iPeriod; iVar++) {
            fFirst += isMaxIdx_Raw[iVar];
            fSecond += isMaxIdx_Raw[iVar + iPeriod];
        }


        fCompareTwo = fFirst / fSecond;
        if (iPeriod < 125 && (fCompareTwo > 1.3 || fCompareTwo < 0.7)) {    // 前後波差異大於 30%
            iPeriod = iPeriod * 2;
        }

        for (int iVar = iStart; iVar < iStart + iPeriod / 2; iVar++) {
            fPreHalf += isMaxIdx_Raw[iVar];
            fAfterHalf += isMaxIdx_Raw[iVar + iPeriod / 2];
        }
        fCompareHalf = fPreHalf / fAfterHalf;
        if (iPeriod > 125 && (fCompareHalf < 1.05 && fCompareHalf > 0.95)) { // 前後半波差異小於 5%
            iPeriod = iPeriod / 2;
        }

        return iPeriod;
    }

    public int iFindFirstHRPeriod(int[] _mIntInputArray_Input, int[] _mIntInputArray_Raw, int _start, int _end, int _iNoiseWidth, int _iNoiseAmp){
        boolean _bPeakSearch = false;
        int _iValleyAnyNums = 0;
        int _iValleyExtremeNums;
        int _iValleyPeriod;
        int[] _mIntArrayAnyValleyPosition = new int[100];
        int[] _mIntArrayAnyValleyValue = new int[100];
        int[] _mIntArrayAnyPeakPosition = new int[100];
        boolean[] _mIntArrayAnyValleyTruly = new boolean[100];
        int _iPositionIdx_Cur, _iPositionValue_Cur;
        int _iPositionIdx_Pre = _start, _iPositionValue_Pre = _mIntInputArray_Input[_start];
        int _iWaveCondition =0 ;
        //  find Valley & Peak
        for (int _iVar = _start; _iVar < _end; _iVar ++) {
            _iPositionIdx_Cur = _iVar;
            _iPositionValue_Cur = _mIntInputArray_Input[_iPositionIdx_Cur];
            // bPeakSearch == false ; search Valley
            // bPeakSearch == true ; search Peak
            if (_iPositionIdx_Cur!=_iPositionIdx_Pre) {
                if((Math.abs(_iPositionValue_Cur-_iPositionValue_Pre)< _iNoiseAmp)||(_iPositionIdx_Cur-_iPositionValue_Pre)<_iNoiseWidth){
                    if (((_iPositionValue_Cur < _iPositionValue_Pre) && (!_bPeakSearch)) ||
                            ((_iPositionValue_Cur > _iPositionValue_Pre) && (_bPeakSearch))){
                        _iPositionValue_Pre = _iPositionValue_Cur;
                        _iPositionIdx_Pre = _iPositionIdx_Cur;
                    }
                }else{
                    _iWaveCondition = Integer.rotateLeft(_iWaveCondition, 1);
                    if (((_iPositionValue_Cur > _iPositionValue_Pre) && (!_bPeakSearch)) ||    //upward
                            ((_iPositionValue_Cur < _iPositionValue_Pre) && (_bPeakSearch))) { //downward
                        _iWaveCondition += 1;
                    }else{
                        _iPositionValue_Pre = _iPositionValue_Cur;
                        _iPositionIdx_Pre = _iPositionIdx_Cur;
                    }
                    if ((_iWaveCondition & 0b111) == 0b011) {
                        if(!_bPeakSearch){
                            _mIntArrayAnyValleyPosition[_iValleyAnyNums] =_iPositionIdx_Pre;
                        }else{
                            _mIntArrayAnyPeakPosition[_iValleyAnyNums] =_iPositionIdx_Pre;
                            _iValleyAnyNums ++;
                        }
                        _iPositionValue_Pre = _iPositionValue_Cur;
                        _iPositionIdx_Pre = _iPositionIdx_Cur;
                        _bPeakSearch = !_bPeakSearch;
                        _iWaveCondition = 0xb111;
                    }
                }
            }
        }
        _mIntArrayAnyValleyValue[0] = (_mIntInputArray_Input[_mIntArrayAnyPeakPosition[0]]-_mIntInputArray_Input[_mIntArrayAnyValleyPosition[0]]);
        for (int _iVar = 1; _iVar < _iValleyAnyNums; _iVar ++) {
//            _mIntArrayAnyValleyValue[_iVar]=(_mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iVar-1]]
//                                                    - _mIntInputArray_Input[_mIntArrayAnyValleyPosition[_iVar]]);
            _mIntArrayAnyValleyValue[_iVar]=(_mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iVar-1]]
                    +_mIntInputArray_Input[_mIntArrayAnyPeakPosition[_iVar]]
                    - 2*_mIntInputArray_Input[_mIntArrayAnyValleyPosition[_iVar]]);
        }


        //  find  ExtremeValley
        //_mIntArrayValleyPosition_Output[0]=_mIntArrayAnyValleyPosition[0];
        _mIntArrayAnyValleyTruly[0]= true;
        int _iPreExtreme = 0;
        _iValleyExtremeNums=1;

        // First Run
        //找基準點
        int _iCompareValue_Max=Integer.MIN_VALUE;
        int _iCompareNums = 5;
        int _iCompareStart, _iCompareEnd;
        int _iCompareValue=0, _iCompareValue_Min =Integer.MAX_VALUE;
        for (int _iVar = 1; _iVar < _iValleyAnyNums; _iVar ++) {
            if (_iVar==1) {     // 只做一次
                _iCompareValue_Max =Integer.MIN_VALUE;
                _iCompareStart = _iVar;
                _iCompareEnd = 6;
                if (_iCompareEnd>_iValleyAnyNums) {
                    _iCompareEnd = _iValleyAnyNums;
                }
                _iCompareNums = 0;
                for (int _iVar2 = _iCompareStart; _iVar2 < _iCompareEnd; _iVar2 ++) {
                    _iCompareValue += _mIntArrayAnyValleyValue[_iVar2];
                    _iCompareNums ++;
                    if (_iCompareValue_Max < _mIntArrayAnyValleyValue[_iVar2]){
                        _iCompareValue_Max = _mIntArrayAnyValleyValue[_iVar2];
                    }
                }
                _iCompareValue = (_iCompareValue + _iCompareValue_Max)/(_iCompareNums+1);     //最值做加權

            }

            if ((_mIntArrayAnyValleyValue[_iVar] > _iCompareValue)||(_mIntArrayAnyValleyValue[_iVar] > _iCompareValue_Max * 0.5)){
                _mIntArrayAnyValleyTruly[_iVar] = true;
                _iValleyExtremeNums++;
            }

        }

//Second Run
        int _iValleyExtremeNums2 =0;
        _iValleyPeriod=0;
        _iPositionIdx_Pre = 0;
        int _iPositionValue_Max = Integer.MIN_VALUE;
        int _iPositionIdx_Max = 0;
        for (int _iVar = 1; _iVar < _iValleyAnyNums; _iVar ++) {
            if (_mIntArrayAnyValleyTruly[_iVar]){
                if (_iPositionValue_Max<_mIntArrayAnyValleyValue[_iVar]){
                    _iPositionValue_Max=_mIntArrayAnyValleyValue[_iVar];
                    _iPositionIdx_Max=_iVar;
                }
                _iValleyExtremeNums2++;
                if (_iValleyExtremeNums == 2){      // 只有乙個波, 無需比對
                    _iValleyPeriod = _mIntArrayAnyValleyPosition[_iVar]-_mIntArrayAnyValleyPosition[0];
                    break;
                }
                if (_iValleyExtremeNums2 == 2){      //先看2個波...
                    double _SlopePre = _mIntInputArray_Input[_mIntArrayAnyValleyPosition[_iPositionIdx_Pre]+8]
                            - _mIntInputArray_Input[_mIntArrayAnyValleyPosition[_iPositionIdx_Pre]];
                    double _SlopeCur = _mIntInputArray_Input[_mIntArrayAnyValleyPosition[_iVar]+8]
                            - _mIntInputArray_Input[_mIntArrayAnyValleyPosition[_iVar]];
                    double _SpanPre = _mIntArrayAnyValleyPosition[_iPositionIdx_Pre]
                            - _mIntArrayAnyValleyPosition[0];
                    double _SpanCur = _mIntArrayAnyValleyPosition[_iVar]
                            - _mIntArrayAnyValleyPosition[_iPositionIdx_Pre];
                    double _SumRawPre, _SumRawCur;

                    _SumRawPre = iWaveByPeriodSum(_mIntInputArray_Raw,_mIntArrayAnyValleyPosition[0] ,_mIntArrayAnyValleyPosition[_iPositionIdx_Pre]);
                    _SumRawCur = iWaveByPeriodSum(_mIntInputArray_Raw,_mIntArrayAnyValleyPosition[_iPositionIdx_Pre] ,_mIntArrayAnyValleyPosition[_iVar]);

                    if (_mIntArrayAnyValleyValue[_iVar] > _mIntArrayAnyValleyValue[_iPositionIdx_Pre] * 1.1){  //後者振幅較大， 取者後者
                        _iValleyPeriod = _mIntArrayAnyValleyPosition[_iVar]-_mIntArrayAnyValleyPosition[0];
                        break;
                    } else if (_mIntArrayAnyValleyValue[_iVar] > _mIntArrayAnyValleyValue[_iPositionIdx_Pre] * 0.8 ){  //後者振幅差不多， 開始比較
                        if(Math.abs(_SpanPre-_SpanCur) >= ((Math.abs(_SpanPre + _SpanCur)/2) * 0.2)){  //表示兩者間距不一樣，取後者
                            _iValleyPeriod = _mIntArrayAnyValleyPosition[_iVar]-_mIntArrayAnyValleyPosition[0];
                            break;
                            //    } else if(Math.abs(_SlopeCur-_SlopePre) > (_SlopeCur+_SlopePre) /2 * 0.2){  //表示兩者斜率不一樣，取後者
                            //        _iValleyPeriod = _mIntArrayAnyValleyPosition[_iVar]-_mIntArrayAnyValleyPosition[0];
                            //        break;
                        } else if(/*Math.abs*/(_SumRawCur-_SumRawPre) > (_SumRawCur+_SumRawPre)/2 * 0.4){   //表示兩者RawData Sum值不一樣，取後者
                            _iValleyPeriod = _mIntArrayAnyValleyPosition[_iVar]-_mIntArrayAnyValleyPosition[0];
                            break;
                        }
                        else {
                            _iValleyPeriod = _mIntArrayAnyValleyPosition[_iPositionIdx_Pre] - _mIntArrayAnyValleyPosition[0];     // 預設, 取前者
                            break;
                        }
                    } else if(_iValleyExtremeNums==3) {   //僅有兩個波, 取大者
                        _iValleyPeriod = _mIntArrayAnyValleyPosition[_iPositionIdx_Max] - _mIntArrayAnyValleyPosition[0];     // 預設, 取者
                        break;
                    }
                }else if (_iValleyExtremeNums2 == 3) {    //三個波, 取最者
                    _iValleyPeriod = _mIntArrayAnyValleyPosition[_iPositionIdx_Max] - _mIntArrayAnyValleyPosition[0];     // 預設, 取最大者
                    break;
                }
                _iPositionIdx_Pre = _iVar;
            }
        }

        if (_iValleyPeriod == 0) _iValleyPeriod = 125;
        return _iValleyPeriod;

    }

    private void searchPeakBeforeAndAfter(int[] _arIBeAfIdx, int _iVar, int _iTerminal, boolean[] _mIntArrayValleyTruly){
        if(_mIntArrayValleyTruly[_iVar]) {
            _arIBeAfIdx[0] = _iVar;
            _arIBeAfIdx[1] = _iVar;
            for (int _iVar2 = _iVar - 1; _iVar2 >= 0; _iVar2--) {
                if (_mIntArrayValleyTruly[_iVar2]){
                    _arIBeAfIdx[0]=_iVar2;
                    break;
                }
            }
            for (int _iVar2 = _iVar + 1; _iVar2 < _iTerminal; _iVar2++) {
                if (_mIntArrayValleyTruly[_iVar2]){
                    _arIBeAfIdx[1]=_iVar2;
                    break;
                }
            }
        }
    }
    private void transWaveByFrEnergy(int[] _mIntArray_Output, double[][] _mInt2Array_Input, int _start, int _end, int _low, int _high) {
        double _doubleFrEnergy_Max = Double.MIN_VALUE;
        double _doubleFrEnergy;
        double[] _doubleArrayFrEnergy = new double[_end];
        for (int _iVar = _start; _iVar < _end; _iVar ++) {
            _doubleFrEnergy=0;
            for (int _iVar2 = _low; _iVar2 < _high; _iVar2++) {
                _doubleFrEnergy += Math.pow(_mInt2Array_Input[_iVar][_iVar2],0.5);
            }
            _doubleArrayFrEnergy[_iVar] = _doubleFrEnergy;
            if ( _doubleFrEnergy_Max < _doubleFrEnergy) {
                _doubleFrEnergy_Max = _doubleFrEnergy;
            }
        }
        for (int _iVar = _start; _iVar < _end; _iVar ++) {
            _mIntArray_Output[_iVar] = (int)((_doubleArrayFrEnergy[_iVar]/_doubleFrEnergy_Max)*100);
        }
    }
//jaufa+ check:  one spectrum normalize divide by mDoubleAmpPsdMaxNormPeriod[iMaxIdxNextIdx]
    private int iWaveByPeriodSum(int[] _mIntArrayInput, int _iStart, int _iEnd){
        int _iSum=0;
        for (int _iVar = _iStart; _iVar <= _iEnd; _iVar++){
            _iSum += _mIntArrayInput[_iVar];
        }
        return _iSum;
    }
    private int iFindPeriodLowestPosition(int[] _mIntArrayInput, int _iStart, int _iEnd, int _iPulseAndMinusNums){
        int [] _mIntArrayResult= new int[_iEnd-_iStart+1];
        int  _iIdx = 0;
        int _iMin = Integer.MAX_VALUE;
        int _iMinIdx = 0;
        _mIntArrayResult[_iIdx] = iWaveByPeriodSum(_mIntArrayInput, _iStart-_iPulseAndMinusNums, _iStart+_iPulseAndMinusNums);
        _iIdx ++;
        for (int _iVar = _iStart+1; _iVar <= _iEnd; _iVar++){
            _mIntArrayResult[_iIdx] = _mIntArrayResult[_iIdx-1]-_mIntArrayInput[_iVar-_iPulseAndMinusNums]+_mIntArrayInput[_iVar+_iPulseAndMinusNums];
            _iIdx ++;
        }
        for (int _iVar = 0; _iVar < _iIdx; _iVar++){
            if (_iMin>_mIntArrayResult[_iVar]){
                _iMin = _mIntArrayResult[_iVar];
                _iMinIdx = _iVar;
            }
        }
        return (_iMinIdx+_iStart);
    }

    private int iFindPeriodHighestPosition(int[] _mIntArrayInput, int _iStart, int _iEnd, int _iHalfWindows){
        int [] _mIntArrayResult= new int[_iEnd-_iStart+1];
        int  _iIdx = 0;
        int _iMax = Integer.MIN_VALUE;
        int _iMaxIdx = 0;
        _mIntArrayResult[_iIdx] = iWaveByPeriodSum(_mIntArrayInput, _iStart-_iHalfWindows, _iStart+_iHalfWindows);
        _iIdx ++;
        for (int _iVar = _iStart+1; _iVar <= _iEnd; _iVar++){
            _mIntArrayResult[_iIdx] = _mIntArrayResult[_iIdx-1]-_mIntArrayInput[_iVar-_iHalfWindows]+_mIntArrayInput[_iVar+_iHalfWindows];
            _iIdx ++;
        }
        for (int _iVar = 0; _iVar < _iIdx; _iVar++){
            if (_iMax < _mIntArrayResult[_iVar]){
                _iMax = _mIntArrayResult[_iVar];
                _iMaxIdx = _iVar;
            }
        }
        return (_iMaxIdx+_iStart);
    }

    public double fHRPeriodToBPM(int _iPeriod){
        if(_iPeriod <= 0){
            mIntHRErrCode |= BINARY_ERR_CODE_HR_INVALID;
            return -1;
        }else {
            return (double) SystemConfig.mIntUltrasoundSamplerate
                    / SystemConfig.mIntSTFTWindowShiftSize
                    / _iPeriod * 60.0;
        }
    }

    public double fVPKTheoreticalTable(int _iRawValue){
        double _fFr,_fVpk = 0;
        if (SystemConfig.mIntCalibrationAdjustType == SystemConfig.INT_CALIBRATION_TYPE_0_NO_CALI) {
            _fFr = (double) SystemConfig.mIntUltrasoundSamplerate / 2 / SystemConfig.mIntFreqIdxsMaxSize * _iRawValue;
            _fVpk = (double) _fFr * SystemConfig.DOUBLE_ULTRASOUND_SPEED_FOR_BODY_METER_PERSEC
                    / SystemConfig.mDoubleSensorWaveFreq ;
           // _fVpk = _fVpk / UserManagerCommon.mDoubleCosineUserAngle ;
        } else if (SystemConfig.mIntCalibrationAdjustType == SystemConfig.INT_CALIBRATION_TYPE_1_TABLE) {
            //_fVpk = adjustValueFromTable(doubleVpk);
        }
        return _fVpk;
    }

    public double fVPKExperimentalTable(double _iTheoreticalValue){
        double _fExperial;
        if(_iTheoreticalValue < 0.35){
            _fExperial = (0.27/0.35) * _iTheoreticalValue + 0;
        }else if(_iTheoreticalValue > 1.54){
            _fExperial = (2.66 - 1.59) / (2.54 - 1.54) * (_iTheoreticalValue - 1.54) + 1.59;
        }else {
            _fExperial = 0.2523 * Math.pow(_iTheoreticalValue, 2) + 0.6283 * _iTheoreticalValue + 0.014;
        }
        if (_fExperial < 0) {
            _fExperial = 0;
        } else if (_fExperial > 2.46) {
            _fExperial = 2.46;
        }
        return _fExperial;
    }

    public double fVPKCali(int _iRawValue){
        double _fVpk;
        if (_iRawValue < 0)
            _iRawValue = 0;
        else if (_iRawValue > 255)
            _iRawValue = 255;
        _fVpk = mDoubleVPKTheoreticalTable[_iRawValue];
        _fVpk = mDoubleVPKExperimentalTable[(int)(_fVpk *100.0)];
        return _fVpk;
    }

    public int intFindVpkMax(int[] _iArrInput) {
        int _iMax = Integer.MIN_VALUE;
        int _iLength = Array.getLength(_iArrInput);
        for (int _var=0; _var<_iLength; _var++){
            if (_iMax <= _iArrInput[_var]){
                _iMax = _iArrInput[_var];
            }
        }
        return _iMax;

    }

    public void vIntArrayDoubleToIntNormalize(int[] _iArrOutput, double[] _dArrInput, int _iMaxValue){
        int _iLength = Array.getLength(_dArrInput);
        int[] _iArrayDeal = new int[_iLength];
        double _doubleMaxDeal = Double.MIN_VALUE;
        for (int _var=0; _var<_iLength; _var++){
            if (_doubleMaxDeal < _dArrInput[_var]){
                _doubleMaxDeal = _dArrInput[_var];
            }
        }
        for (int _var=0; _var<_iLength; _var++){
            _iArrOutput[_var] =  (int)(_dArrInput[_var] / _doubleMaxDeal * _iMaxValue);
        }
        //_iArrOutput = _iArrayDeal;

    }
    public void processAllSegmentMaxIdxBySnSiGm() {
        double[] mDoubleArrayFrEnergy = new double[SystemConfig.mIntSystemMaxSubSegSize];

        //jDoubleArrayLowFlowEnergy = new double[SystemConfig.mIntSystemMaxSubSegSize];

        try {
//            for (iVar = SystemConfig.mIntEndIdxNoiseLearn +1 ; iVar < mIntSTFFTNextSubSegIdx; iVar++) {
            for (int iVar = SystemConfig.mIntStartIdxNoiseLearn; iVar < mIntSTFFTNextSubSegIdx; iVar++) {
                processOneMaxIdxBySnSiGm(iVar);
            }
            // jaufa,  Single Mode 不做GM & SNSI
            if (SystemConfig.mIntSingleVpkEnabled == SystemConfig.INT_SINGLE_VPK_ENABLED_YES){
                mIntMaxIdxNextIdx = mIntSTFFTNextSubSegIdx;
                return;
            }


            int iPeriodIdx = 0, iPeriodNum = 0;
            int iWaveGoldenIdx = 0, iWaveGoldenIdx_Peak = 0, iWaveGoldenIdx_Cur = 0;
            int iWaveCompareSum = 0;
//            for (iVar = SystemConfig.mIntEndIdxNoiseLearn +1 ; iVar < mIntSTFFTNextSubSegIdx - 250; iVar++) {
//                iWaveGoldenIdx = SystemConfig.mIntEndIdxNoiseLearn +1;
            int iPeakCompareWindow = 4;
            int iPeakCompareAmp = 2;

            int iValleyIdx = 0, iValleyValue = 0;
            int iPeakIdx = 0, iPeakValue = 0;
            //int iValleyIdx = 0, iValleyIdx_Pre=0, iValleyIdx_Cur = 0;
            int iValleyStatus = 0; //01: Valley; 10: Peak; up: 11, down:00
            int iPeakStatus = 0; //01: Valley; 10: Peak; up: 11, down:00
            int iPeriodValue_Cur = Integer.MAX_VALUE, iPeriodValue_Pre = 0;
            int iPeriodIdx_Cur = 0, iPeriodIdx_Pre = 0;
            boolean bPeakSearch= false;
            /* Filter
            for (iVar = SystemConfig.mIntStartIdxNoiseLearn + FILTER_TAP_NUM_HALF +1 ; iVar < mIntSTFFTNextSubSegIdx -FILTER_TAP_NUM_HALF; iVar++) {
                //mIntArrayMaxIdx_Tri[iVar] = filterZero2SixHz(iVar, mIntArrayMaxIdx_Hr);
                //for (iVar = SystemConfig.mIntEndIdxNoiseLearn + FILTER_TAP_NUM_2_HALF +1 ; iVar < mIntSTFFTNextSubSegIdx -FILTER_TAP_NUM_2_HALF; iVar++) {
                //    mIntArrayMaxIdx[iVar] = filterZero2TenHz(iVar, mIntArrayMaxIdx_Tri);
                //    mIntArrayMaxIdx_Hr[iVar]=mIntArrayMaxIdx[iVar];
                //for (iVar = SystemConfig.mIntEndIdxNoiseLearn + 2 ; iVar < mIntSTFFTNextSubSegIdx - 2; iVar++) {
                //mIntArrayMaxIdx[iVar]=filterSomeMiddleMeans(mIntArrayMaxIdx,iVar,5,1);
                //jDoubleArrayLowFlowEnergy[iVar]=processOneMaxIdxByFindLowFlow(iVar);
            }
            //*/
            //int iPeriodNums = 0;
            int _iHRPeriod=0;
            //int[] mIntArrayValleyPosition=new int[100]; //[0][]:Positions, //[1][]:Value
            //int[] mIntArrayPeakPosition=new int[100]; //[0][]:Positions, //[1][]:Value
            int iMappingStart;
            mIntArrayMaxIdx_Period = new int[SystemConfig.mIntSystemMaxSubSegSize];
            //int [] mIntArrayMaxIdx_Tmp0 =  new int[SystemConfig.mIntSystemMaxSubSegSize];
            //int [] mIntArrayMaxIdx_Tmp1 =  new int[SystemConfig.mIntSystemMaxSubSegSize];
            //mIntArrayMaxIdx_Tri = new int[SystemConfig.mIntSystemMaxSubSegSize];
            //mIntArrayMaxIdx_Hr = new int[SystemConfig.mIntSystemMaxSubSegSize];
            //transWaveByNormalize(mIntArrayMaxIdx_Hr, mIntArrayMaxIdx_Hr, SystemConfig.mIntStartIdxNoiseLearn, mIntSTFFTNextSubSegIdx-20, 250);
            //transWaveByMovingAverage(mIntArrayMaxIdx_Hr, mIntArrayMaxIdx_Hr, SystemConfig.mIntStartIdxNoiseLearn, mIntSTFFTNextSubSegIdx-20, 3);
            //transWaveByFrEnergy(mIntArrayMaxIdx_Hr, mDoubleBVSpectrumValues,SystemConfig.mIntStartIdxNoiseLearn +125, mIntSTFFTNextSubSegIdx, 2, 126);

            //iMappingStart = iFindPeriodLowestPosition(mIntArrayMaxIdx_Hr, (SystemConfig.mIntStartIdxNoiseLearn + 125), (SystemConfig.mIntStartIdxNoiseLearn + 125 + 250),10);
            mIntHRErrCode = 0;

            transWaveByMovingAverage(mIntArrayMaxIdx, mIntArrayMaxIdx, SystemConfig.mIntStartIdxNoiseLearn, mIntSTFFTNextSubSegIdx-5, 5);


//* j, 190304, +, Note: Amplitude -> Binary (Mix) (Determine the Width )
            int[] isMaxIdx_Bin = new int[mIntSTFFTNextSubSegIdx];
            int[] isHrPeriod = new int[10];
            transWaveByMovingAverage(mIntArrayMaxIdx_Hr, mIntArrayMaxIdx_Hr, SystemConfig.mIntStartIdxNoiseLearn, mIntSTFFTNextSubSegIdx-10, 10);
            transWaveByBinary(isMaxIdx_Bin, mIntArrayMaxIdx_Hr, SystemConfig.mIntStartIdxNoiseLearn, mIntSTFFTNextSubSegIdx - 20, 20);
            for (int iVar = 0; iVar < 10; iVar++){
                int iStart =  SystemConfig.mIntStartIdxNoiseLearn + iVar * 100;
                int iPatternWidth = 200;
                int iCompareLength = 300;
//                isHrPeriod[iVar] = iFindFirstHRPeriodByBinary(mIntArrayMaxIdx_Hr, isMaxIdx_Bin, iStart, iPatternWidth, iCompareLength);
                isHrPeriod[iVar] = iFindFirstHRPeriodByMixed(mIntArrayMaxIdx_Hr, isMaxIdx_Bin, iStart, iPatternWidth, iCompareLength);
            }

            int[] iCheck = isSimilarVote(isHrPeriod);
            mIntHRPeriod = iCheck[1];
            double fUnstable = 0;
            if (iCheck[1] > 0){
                fUnstable = ((float) iCheck[2] / (float) iCheck[1]);
            }

            if ((iCheck[0] < 4) || (fUnstable < 0.7) || (fUnstable > 1.43)){
                mIntHRErrCode |= BINARY_ERR_CODE_HR_UNSTABLE;
            }
            /*
            if (((mIntHRPeriod ==0 )||(iCheck[0] < 2))){
                mIntHRErrCode |= BINARY_ERR_CODE_HR_INVALID;
                mIntHRPeriod = 125;  //default
            }
             */


            //* j, 190304, +, Note: Amplitude -> Binary (Determine the Width )
// */

/* j, 190304, Note: Amplitude
            int[] isHrPeriod = new int[10];
            int[] isMaxIdx_Amp = new int[mIntSTFFTNextSubSegIdx];
            int iStart =  SystemConfig.mIntStartIdxNoiseLearn + iVar * 100;
            int iPatternWidth = 200;
            int iCompareLength = 300;
            transWaveByMovingAverage(mIntArrayMaxIdx_Hr, mIntArrayMaxIdx_Hr, SystemConfig.mIntStartIdxNoiseLearn, mIntSTFFTNextSubSegIdx-10, 10);
            iMappingStart = SystemConfig.mIntStartIdxNoiseLearn;
            for (int iVar = 0; iVar <10; iVar++){
                int iStart =  SystemConfig.mIntStartIdxNoiseLearn + iVar * 100;
                int PatternWidth = 200;
                int iCompareLength = 300;
                transWaveBySimilarMapping(isMaxIdx_Amp, mIntArrayMaxIdx_Hr, iStart, iPatternWidth,iCompareLength);
                isHrPeriod[iVar] = iFindFirstHRPeriod(isMaxIdx_Amp, iStart, iCompareLength,6,4);

            }

            iMappingStart = SystemConfig.mIntStartIdxNoiseLearn + 250;
            transWaveBySimilarMapping(mIntArrayMaxIdx_Period, mIntArrayMaxIdx_Hr, iMappingStart, iMappingStart+500,250);
            _arIntHrPeriod[1] = iFindFirstHRPeriod(mIntArrayMaxIdx_Period, mIntArrayMaxIdx_Hr,iMappingStart, iMappingStart+250,6,4);

            iMappingStart = SystemConfig.mIntStartIdxNoiseLearn + 550;
            transWaveBySimilarMapping(mIntArrayMaxIdx_Period, mIntArrayMaxIdx_Hr, iMappingStart, iMappingStart+500,250);
            _arIntHrPeriod[2] = iFindFirstHRPeriod(mIntArrayMaxIdx_Period, mIntArrayMaxIdx_Hr,iMappingStart, iMappingStart+250,6,4);

            Arrays.sort(_arIntHrPeriod);

            //mIntHRPeriod = _arIntHrPeriod[1];

//            mIntHRPeriod = (_arIntHrPeriod[1] + _arIntHrPeriod[2]) /2;
            mIntHRPeriod = _arIntHrPeriod[1];

            if ((_arIntHrPeriod[2]-_arIntHrPeriod[0]) > _arIntHrPeriod[2] * 0.3){
                mIntHRErrCode |= BINARY_ERR_CODE_HR_UNSTABLE;
            }

//* j, 190304, -, Note: Amplitude -> Binary (Determine the Width )
// */

            iMappingStart = iFindPeriodHighestPosition(mIntArrayMaxIdx_Hr, (SystemConfig.mIntStartIdxNoiseLearn + 250), (SystemConfig.mIntStartIdxNoiseLearn + 250 + mIntHRPeriod),2);
            mIntHRCounts = iFindHRPeakByFixLength(mIntArrayHRPeakPosition, mIntArrayMaxIdx_Hr, iMappingStart, mIntSTFFTNextSubSegIdx, mIntHRPeriod, (mIntHRPeriod/2));
            mIntHRPeriodCounts = mIntHRCounts - 1;
            //vFindHRDeEchoByDeMaxPeak(mIntArrayMaxIdx, mIntArrayHRPeakPosition,  mIntHRCounts, 3); /*(mIntHRPeriod/20)*/
            //transWaveByMovingAverage(mIntArrayMaxIdx, mIntArrayMaxIdx, SystemConfig.mIntStartIdxNoiseLearn, mIntSTFFTNextSubSegIdx-3, 3);

            //transWaveByMovingAverage(mIntArrayMaxIdx_Tri, mIntArrayMaxIdx, SystemConfig.mIntStartIdxNoiseLearn, mIntSTFFTNextSubSegIdx-mIntHRPeriod/5, mIntHRPeriod/5);

            vFindHRVallyPositionByBeforePeak(mIntArrayHRValleyPosition, mIntArrayHRPeakPosition, mIntArrayMaxIdx_Hr, mIntHRCounts,(int)(mIntHRPeriod * 0.5));

            //transWaveByMovingAverage(mIntArrayMaxIdx_Hr, mIntArrayMaxIdx, SystemConfig.mIntStartIdxNoiseLearn, mIntSTFFTNextSubSegIdx-10, 10);
            //transWaveByMovingAverage(mIntArrayMaxIdx_Hr, mIntArrayMaxIdx_Hr, SystemConfig.mIntStartIdxNoiseLearn, mIntSTFFTNextSubSegIdx-mIntHRPeriod/20, mIntHRPeriod/20);

            // jaufa, +, test
            //transWaveByMovingAverage(mIntArrayMaxIdx, mIntArrayMaxIdx, SystemConfig.mIntStartIdxNoiseLearn, mIntSTFFTNextSubSegIdx-5, 5);
            //int _iVpkMax = intFindVpkMax(mIntArrayMaxIdx);
            //vIntArrayDoubleToIntNormalize(mIntArrayMaxIdx, mDoubleArraySignalPowerIdx, _iVpkMax);
            //vIntArrayDoubleToIntNormalize(mIntArrayMaxIdx_Hr, mDoubleArraySignalPowerIdx, 128);
            //transWaveByMovingAverage(mIntArrayMaxIdx_Hr, mIntArrayMaxIdx_Hr, SystemConfig.mIntStartIdxNoiseLearn, mIntSTFFTNextSubSegIdx-3, 3);

            //vIntArrayDoubleToIntNormalize(mIntArrayMaxIdx_Hr, mDoubleArraySignalPowerIdx, 128);


            transWaveByMovingAverage(mIntArrayMaxIdx_Tri, mIntArrayMaxIdx_Tri, SystemConfig.mIntStartIdxNoiseLearn, mIntSTFFTNextSubSegIdx-5, 5);
            vFindHRVTIStartEndPosition(mIntArrayHRStartPosition, mIntArrayHREndPosition, mIntArrayVTIStartPosition, mIntArrayVTIEndPosition, mIntArrayHRValleyPosition, mIntArrayMaxIdx_Tri, mIntArrayMaxIdx, mIntHRPeriodCounts);
            vFindVPKPosition(mIntArrayVPKPeakPosition, mIntArrayVTIStartPosition, mIntArrayVTIEndPosition, mIntArrayMaxIdx, mIntHRPeriodCounts);
            int[] intArrayHRAccepted = new int[mIntHRPeriodCounts];
            Arrays.fill(intArrayHRAccepted, 1);
            int intHRAcceptedCnt = 0;

            //* jaufa, 1228, +

            mDoubleHrBloodVpk = fMeanHRVpk(intArrayHRAccepted, mDoubleArraySignalPowerIdx, 50, mIntArrayVPKPeakPosition, mIntArrayMaxIdx, mIntHRPeriodCounts);
            int iTmp = 0;
            iTmp = iMeanHRPeriod(intArrayHRAccepted, mIntHRPeriod, mIntArrayMaxIdx,mIntArrayVPKPeakPosition, mIntArrayHRStartPosition, mIntArrayHREndPosition,mIntHRCounts);
            mDoubleHrBloodHr = fHRPeriodToBPM(mIntHRPeriod);

            transWaveByMovingVti100(mIntArrayMaxIdx_Tri, mIntArrayMaxIdx, SystemConfig.mIntStartIdxNoiseLearn, mIntSTFFTNextSubSegIdx - (int) (mIntHRPeriod * 0.15),
                        (int) (mIntHRPeriod * 0.15));

            mDoubleHrBloodVti = fMeanHRVtiFixRange(intArrayHRAccepted, mIntArrayHRStartPosition, mIntArrayHREndPosition, mIntArrayMaxIdx_Tri, mIntHRPeriod, mIntHRPeriodCounts);
                //mDoubleHrBloodVti =  fMeanHRVti(mIntArrayHRStartPosition, mIntArrayHREndPosition, mIntArrayVTIStartPosition, mIntArrayVTIEndPosition, mIntArrayMaxIdx, mIntHRPeriodCounts);


                // jaufa, +, test
                // vIntArrayDoubleToIntNormalize(mIntArrayMaxIdx, mDoubleArraySignalPowerIdx, 128);
                // transWaveByMovingAverage(mIntArrayMaxIdx, mIntArrayMaxIdx, SystemConfig.mIntStartIdxNoiseLearn, mIntSTFFTNextSubSegIdx-5, 5);
                //mIntArrayMaxIdx = mIntArrayMaxIdx_Tri;


                double doubleRadius = UserManagerCommon.mDoubleUserPulmDiameterCm / 2.0;
                double doubleCSArea = doubleRadius * doubleRadius * Math.PI;
                mDoubleHrBloodSV = mDoubleHrBloodVti * doubleCSArea;


                mDoubleHrBloodCO = mDoubleHrBloodSV * mDoubleHrBloodHr / 1000.0;

                int _iHRAcceptedCnt = 0;
                for (int _iVar = 0; _iVar < mIntHRPeriodCounts; _iVar++) {
                    if (intArrayHRAccepted[_iVar] == 0x01) {
                        _iHRAcceptedCnt++;
                    }
                }

                //mDoubleHrBloodSuccessRatio = (double)_iHRAcceptedCnt / (double)mIntHRPeriodCounts;
                //mDoubleHrBloodSuccessRatio = _iHRAcceptedCnt;
                mDoubleHrBloodSuccessRatio = (mDoubleHrBloodHr / 60.0 * (double) _iHRAcceptedCnt) / 10.0;

            //iPeriodNums = findWaveValleyPosition(mIntArrayValleyPosition, mIntArrayMaxIdx_Period, iMappingStart, mIntSTFFTNextSubSegIdx - 250, 8, 8);

            //iPeriodNums = findWaveValleyPeakPosition(mIntArrayValleyPosition, mIntArrayPeakPosition, mIntArrayMaxIdx_Hr, mIntArrayMaxIdx, SystemConfig.mIntStartIdxNoiseLearn, mIntSTFFTNextSubSegIdx, 8, 8);

            //iPeriodNums = findWavePeakPosition(mIntArrayValleyPosition, mIntArrayMaxIdx_Hr, iMappingStart, mIntSTFFTNextSubSegIdx, 8, 8);
            //transWaveByZeroOneMapping(mIntArrayMaxIdx_Tri, mIntArrayMaxIdx_Period, (SystemConfig.mIntStartIdxNoiseLearn + 125), mIntSTFFTNextSubSegIdx,6, 6);

            for (int iVar =0; iVar<mIntHRCounts; iVar++){
               mIntArrayMaxIdx_Period[mIntArrayVTIStartPosition[iVar]] = 129;
               mIntArrayMaxIdx_Period[mIntArrayVTIEndPosition[iVar]] = 64;
                //for (int _iVar2 = mIntArrayValleyPosition[iVar]-6;_iVar2 <= mIntArrayValleyPosition[iVar]+6; _iVar2 ++){
                //    mIntArrayMaxIdx[_iVar2]=0;
                //   mIntArrayMaxIdx_Hr[_iVar2]=mIntArrayMaxIdx[_iVar2];
               //}
            }
/*
            for (int iVar =iMappingStart; iVar<mIntSTFFTNextSubSegIdx-20; iVar++){
                if(((iVar-iMappingStart)%_iHRPeriod)==0){
                    mIntArrayMaxIdx_Period[iVar] = 129;
                }
            }
*/
            //transWaveByArrayCopy(mIntArrayMaxIdx_Hr, mIntArrayMaxIdx_Tmp0, 0, mIntSTFFTNextSubSegIdx);
            //transWaveBySimilarMapping(mIntArrayMaxIdx_Period, mIntArrayMaxIdx_Tri, (SystemConfig.mIntStartIdxNoiseLearn + 125), mIntSTFFTNextSubSegIdx,250);
            //transWaveByZeroOneMapping(mIntArrayMaxIdx_Tri, mIntArrayMaxIdx_Period, (SystemConfig.mIntStartIdxNoiseLearn + 125), mIntSTFFTNextSubSegIdx,6, 6);
            //transWaveBySimilarMapping(mIntArrayMaxIdx_Period,mIntArrayMaxIdx_Hr, iMappingStart, mIntSTFFTNextSubSegIdx,250);
            //substractTwomIntArray(mIntArrayMaxIdx_Period, mIntArrayMaxIdx_Period, mIntArrayMaxIdx_Tri, 0.5);

            //iPeriodNums = findWaveValleyPosition(mIntArrayValleyPosition, mIntArrayMaxIdx_Period, iMappingStart, mIntSTFFTNextSubSegIdx - 250, 8, 8);
              //iPeriodNums = findWavePeakPosition(mIntArrayValleyPosition, mIntArrayMaxIdx_Tri, SystemConfig.mIntStartIdxNoiseLearn +125, mIntSTFFTNextSubSegIdx - 250, 4, 6);

            mIntMaxIdxNextIdx = mIntSTFFTNextSubSegIdx;
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("procAllSegMaxIdxGm.Exception","");
            ex1.printStackTrace();
        }
    }
/*
    Take 3 middle values from 5 data
*/
private int filterSomeMiddleMeans(int[] mData, int iCurrentIdx, int iTotalNums, int iWipeExtremeNums){
    //int [] mCalculateData = new int[iTotalNums];
    int iCalIdx;
    int iHalfNums = iTotalNums /2;
    int[] mCalculateData = Arrays.copyOfRange(mData, (iCurrentIdx-iHalfNums), (iCurrentIdx+iHalfNums));
    double dResult=0;
    //for (int iVar = iCurrentIdx - iHalfNums ; iVar <= iCurrentIdx + iHalfNums; iVar++) {
    //    mCalculateData[iCalIdx] = mData[iCurrentIdx];
    //    iCalIdx++ ;
    //}
    Arrays.sort(mCalculateData);
    iCalIdx=0;
    for (int iVar =  iWipeExtremeNums ; iVar < iTotalNums - iWipeExtremeNums; iVar++) {
        dResult += mCalculateData[iVar];
        iCalIdx++ ;
    }
    dResult = dResult/iCalIdx;
    return (int) dResult;
}

    private double filterSomeMiddleMeans(double[] mData, int iCurrentIdx, int iTotalNums, int iWipeExtremeNums){
        //int [] mCalculateData = new int[iTotalNums];
        int iCalIdx;
        int iHalfNums = iTotalNums /2;
        double[] mCalculateData = Arrays.copyOfRange(mData, (iCurrentIdx-iHalfNums), (iCurrentIdx+iHalfNums));
        double dResult=0;
        //for (int iVar = iCurrentIdx - iHalfNums ; iVar <= iCurrentIdx + iHalfNums; iVar++) {
        //    mCalculateData[iCalIdx] = mData[iCurrentIdx];
        //    iCalIdx++ ;
        //}
        Arrays.sort(mCalculateData);
        iCalIdx=0;
        for (int iVar =  iWipeExtremeNums ; iVar < iTotalNums - iWipeExtremeNums; iVar++) {
            dResult += mCalculateData[iVar];
            iCalIdx++ ;
        }
        dResult = dResult/iCalIdx;
        return dResult;
    }


/*
FIR filter designed with
http://t-filter.appspot.com
sampling frequency: 125 Hz
element: 29
* 0 Hz - 6 Hz gain = 1 desired ripple = 5 dB actual ripple = 3.2193776381925714 dB
* 12 Hz - 62.5 Hz gain = 0 desired attenuation = -40 dB actual attenuation = -42.19324252415652 dB
*/

//#define FILTER_TAP_NUM 29
    public static int FILTER_TAP_NUM  = 29;
    public static int FILTER_TAP_NUM_HALF  = 14;
    double[] mFilter_taps = {
                 -0.0070901937886763515, -0.007826235210629025, -0.010181274973141776, -0.010984889360697062, -0.009083857442502485,
                -0.003423920030922521,   0.006695488248999375,  0.02140421505869316,  0.040127092411659945, 0.06156577419575633,
                0.0838030361748037, 0.10453313406086986, 0.12143230852103903,  0.1324814068080008, 0.13632526014991048,
                0.1324814068080008, 0.12143230852103903, 0.10453313406086986, 0.0838030361748037, 0.06156577419575633,
                0.040127092411659945, 0.02140421505869316,  0.006695488248999375, -0.003423920030922521,  -0.009083857442502485,
                -0.010984889360697062, -0.010181274973141776, -0.007826235210629025, -0.0070901937886763515
        };

/*
FIR filter designed with
http://t-filter.appspot.com
sampling frequency: 125 Hz
* 0 Hz - 10 Hz gain = 1 desired ripple = 5 dB actual ripple = 3.96156352073567 dB
* 16 Hz - 62.5 Hz gain = 0 desired attenuation = -40 dB actual attenuation = -40.441469634829375 dB
*/
    public static int FILTER_TAP_NUM_2  = 23;
    public static int FILTER_TAP_NUM_2_HALF  = 11;
    double[] mFilter_taps_2 = {
            -0.012434853888681258, -0.02122228662119973, -0.03217426586180514, -0.03886198187870229, -0.03594063205606867,
            -0.01909717675666841, 0.013101288053774583, 0.0579584320189748, 0.10861614746694227, 0.1554618011667011,
            0.18858705150953298, 0.20053633425786052, 0.18858705150953298, 0.1554618011667011, 0.10861614746694227,
            0.0579584320189748, 0.013101288053774583, -0.01909717675666841, -0.03594063205606867, -0.03886198187870229,
            -0.03217426586180514, -0.02122228662119973, -0.012434853888681258
    };


    private int filterZero2SixHz(int iCurrentVar, int[] pDataArray){
        int iFilterIdx=0;
        double dResult=0;
        for (int iVar = iCurrentVar - FILTER_TAP_NUM_HALF ; iVar <= iCurrentVar + FILTER_TAP_NUM_HALF; iVar++) {
            dResult += (double) pDataArray[iVar] * mFilter_taps[iFilterIdx];
            iFilterIdx++ ;
        }
        return (int) dResult;
    }

    private int filterZero2TenHz(int iCurrentVar, int[] pDataArray){
        int iFilterIdx=0;
        double dResult=0;
        for (int iVar = iCurrentVar - FILTER_TAP_NUM_2_HALF; iVar <= iCurrentVar + FILTER_TAP_NUM_2_HALF; iVar++) {
            dResult += (double) pDataArray[iVar] * mFilter_taps_2[iFilterIdx];
            iFilterIdx++ ;
        }
        return (int) dResult;
    }

    private int processOneMaxIdxBySnSiGm(int iMaxIdxNextIdx) {
        int iVar, iSignalStartIdx, iSignalEndIdx, iSignalMaxIdx, iNoiseStartIdx, iNoiseEndIdx;
        int _iMaxCrossIdx, iMaxCrossIdx_Vti, iMaxCrossIdx_Hr = 0, iMaxCrossIdx_GM, iMaxCrossIdx_Tri;
        double doubleSignalEnd, doubleSignalStart, doubleSignalMax, doubleNoiseEnd, doubleNoiseStart;
        double doubleM1, doubleB1, doubleM2, doubleB2, doubleZeroPointA, doubleZeroPointB;
        int iMaxIdx, iCrossIdx, iStart, iZeroPointA, iZeroPointB;
        double doubleMax, doubleMaxOneTime, doubleCross, doubleCrossIdx, doubleDistance, doubleAccu;
        double doublePSDMax, doublePSDSignal, doublePSDNoise, doubleSignalAccu, doubleNoiseAccu, doubleSNR;
//jaufa+ check:  one spectrum normalize divide by mDoubleAmpPsdMaxNormPeriod[iMaxIdxNextIdx]
        try {
            //* jaufa: standard
            double _doubleOneNoiseMax;
            double _doubleOneSignalMax;
            double _doubleOneSignalMean;
            double _doubleOneNoiseMean = 0;
            double _doubleFindPeak;
            boolean _boolFindPeak = false;
            int _intFindPeakVar;
            double _doubleBVSpectrumValuesOneTimeVerticalFilter;
            double _doubleBVSpectrumValuesOneTimeHorizontalFilter = 0;






            //for (iVar = 0; iVar < mIntTotalFreqSeqsCnt; iVar++) {
            //    mDoublemBVSpectrumValuesOneTime[iVar] =   mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar];
            //}

            //* jaufa Filter
            for (iVar = 0; iVar < mIntTotalFreqSeqsCnt; iVar++) {
                if(iVar > 1 && iVar < mIntTotalFreqSeqsCnt) {
                    if (iMaxIdxNextIdx > 2 && iMaxIdxNextIdx < SystemConfig.mIntSystemMaxSubSegSize-3) {
                        _doubleBVSpectrumValuesOneTimeHorizontalFilter =
                                        0.3435736661374695 * mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar]
                                        + 0.2745078276838308 * mDoubleBVSpectrumValues[iMaxIdxNextIdx - 1][iVar]
                                        + 0.2745078276838308 * mDoubleBVSpectrumValues[iMaxIdxNextIdx + 1][iVar]
                                        + 0.1363897237013295 * mDoubleBVSpectrumValues[iMaxIdxNextIdx - 2][iVar]
                                        + 0.1363897237013295 * mDoubleBVSpectrumValues[iMaxIdxNextIdx + 2][iVar]
                                        +  0.02893626643605756  * mDoubleBVSpectrumValues[iMaxIdxNextIdx - 3][iVar]
                                        +  0.02893626643605756  * mDoubleBVSpectrumValues[iMaxIdxNextIdx + 3][iVar];
                    } else {
                        _doubleBVSpectrumValuesOneTimeHorizontalFilter = mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar];
                    }
                }
                if(iVar > 2 && iVar < mIntTotalFreqSeqsCnt-3) {
                    _doubleBVSpectrumValuesOneTimeVerticalFilter =
                            0.3435736661374695 * mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar]
                                    +    0.2745078276838308 * mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar-1]
                                    +    0.2745078276838308 * mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar+1]
                                    +    0.1363897237013295 * mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar-2]
                                    +    0.1363897237013295 * mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar+2]
                                    +    0.02893626643605756  * mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar-3]
                                    +    0.02893626643605756 * mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar+3];
                }else {
                    _doubleBVSpectrumValuesOneTimeVerticalFilter = mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar];
                }
                mDoublemBVSpectrumValuesOneTime[iVar]= 0.4 * _doubleBVSpectrumValuesOneTimeHorizontalFilter+
                        0.6 * _doubleBVSpectrumValuesOneTimeVerticalFilter;
            }
            //* jaufa Filter  */


//* jaufa, add Wu center of gravity
            // jaufa, +, 191031
            SystemConfig.mDoubleNoiseBaseLearned = SystemConfig.mDoubleNoiseStrengthWu;
            SystemConfig.mDoubleSignalBaseLearned = SystemConfig.mDoubleSignalStrengthWu;
            SystemConfig.mDoubleSNRLearned = SystemConfig.mDoubleSNRLearnedWu;


            double dSignalStrengthMaxWu = Double.MIN_VALUE;
            int iHeavyCenterIdx;

            for(int iVar2 = 1 ; iVar2 < mIntTotalFreqSeqsCnt  ; iVar2++ ){
                if(dSignalStrengthMaxWu < mDoubleBVSpectrumValues [iMaxIdxNextIdx][iVar2]){
                    dSignalStrengthMaxWu = mDoubleBVSpectrumValues [iMaxIdxNextIdx][iVar2];
                }
            }
            //--- calculate Noise  ---
            iHeavyCenterIdx = getHeavyCenterIdx(iMaxIdxNextIdx);
            int iNoiseWuStartIdx = iHeavyCenterIdx + (int) ((double)(mIntTotalFreqSeqsCnt - 1 - iHeavyCenterIdx) * 2.0/3.0);
            double dNoiseMax = Double.NEGATIVE_INFINITY;
            double dNoiseMin = Double.POSITIVE_INFINITY;
            for(int iVar2 = iNoiseWuStartIdx ; iVar2 < mIntTotalFreqSeqsCnt  ; iVar2++ ){
                if(dNoiseMax < mDoubleBVSpectrumValues [iMaxIdxNextIdx][iVar2]){
                    dNoiseMax = mDoubleBVSpectrumValues [iMaxIdxNextIdx][iVar2];
                }
                if(dNoiseMin > mDoubleBVSpectrumValues [iMaxIdxNextIdx][iVar2]){
                    dNoiseMin = mDoubleBVSpectrumValues [iMaxIdxNextIdx][iVar2];
                }
            }
            double dNoiseStrengthWud  = (dNoiseMax + dNoiseMin) / 2.0;

//*/


            _doubleOneSignalMax = 0;
            _doubleOneNoiseMax = 0;
            _doubleOneSignalMean = 0;
            for (iVar = 0; iVar < mIntTotalFreqSeqsCnt-3; iVar++) {
                if((iVar < 24) &&  (iVar >= 8)) {
                    _doubleOneSignalMean = _doubleOneSignalMean + Math.pow(mDoublemBVSpectrumValuesOneTime[iVar],2);
                }
                if (_doubleOneSignalMax <= mDoublemBVSpectrumValuesOneTime[iVar]){
                    _doubleOneSignalMax = mDoublemBVSpectrumValuesOneTime[iVar];
                }
                if(iVar >= mIntTotalFreqSeqsCnt-16){
                    if (_doubleOneNoiseMax<=mDoublemBVSpectrumValuesOneTime[iVar]){
                        _doubleOneNoiseMax=mDoublemBVSpectrumValuesOneTime[iVar];
                    }
                }
                if(iVar >= mIntTotalFreqSeqsCnt-16 && iVar < mIntTotalFreqSeqsCnt){
                    _doubleOneNoiseMean += Math.pow(mDoublemBVSpectrumValuesOneTime[iVar],2);
                }
            }
            _doubleOneSignalMean = Math.pow(_doubleOneSignalMean/16, 0.5);
            _doubleOneNoiseMean= Math.pow(_doubleOneNoiseMean/16, 0.5);

            //* jaufa ----   Integration -----------------------
            mDoubleBVSpectrumIntegralOneTime = new double[mIntTotalFreqSeqsCnt];

            double doubleIntegral = 0.0;
            double doubleIntegralWeight = 0.0;
            int _intIntegralCross = 0; //jaufa+

            for (iVar = 0; iVar < mIntTotalFreqSeqsCnt; iVar++) {
                doubleIntegral = doubleIntegral + mDoublemBVSpectrumValuesOneTime[iVar];
                //mDoubleBVSpectrumIntegralOneTime_Pre[iVar] = doubleIntegral;
                mDoubleBVSpectrumIntegralOneTime[iVar] = doubleIntegral;
            }
            //* jaufa ----   Integration ------------------- */


            double jDoubleCompareNoiseMult = 0;
            _intFindPeakVar=0;
            double _doubleMulti = (SystemConfig.mDoubleSNRLearned / 16);
            double _doubleFindNoiseStart =   6;
            double _doubleFindSignalEnd =   20;
            double _doubleFindSignalTri =   20;
            if (SystemConfig.mIntSingleVpkEnabled == SystemConfig.INT_SINGLE_VPK_ENABLED_YES) {
                //* jaufa,  SingleVPK Mode
                if (_doubleMulti > 20)
                    _doubleMulti = 20;
                else if (_doubleMulti < 10)
                    _doubleMulti = 10;
//1
                _doubleFindSignalEnd = _doubleMulti * _doubleOneNoiseMean;
                _doubleFindNoiseStart = 6 * _doubleOneNoiseMean;
                _doubleFindPeak = _doubleMulti * 2 * (SystemConfig.mDoubleNoiseBaseLearned);
//2
                _doubleFindSignalEnd = _doubleMulti * 2 * _doubleOneNoiseMean;
                _doubleFindNoiseStart = 4 * _doubleOneNoiseMean;

                _doubleFindPeak = _doubleMulti * (_doubleOneNoiseMean);
//3
                _doubleMulti = (_doubleOneSignalMean/_doubleOneNoiseMean) / 10;
                //_doubleMulti = (SystemConfig.mDoubleSNRLearned) / 10;
                if (_doubleMulti > 50)
                    _doubleMulti = 50;
                else if (_doubleMulti < 10)
                    _doubleMulti = 10;
                _doubleFindSignalEnd = 30 * (SystemConfig.mDoubleNoiseBaseLearned);
                //_doubleFindSignalEnd = 30 * (_doubleOneNoiseMean);
                _doubleFindNoiseStart = 8  * (SystemConfig.mDoubleNoiseBaseLearned);

                _doubleFindPeak = _doubleMulti * (_doubleOneNoiseMean);

            }else{
                _doubleFindSignalEnd = 16 * dNoiseStrengthWud; //8 * SystemConfig.mDoubleNoiseBaseLearned;//50 * _doubleOneNoiseMean;
                //_doubleFindSignalEnd =   50 * _doubleOneNoiseMean;
                _doubleFindNoiseStart = 4 * SystemConfig.mDoubleNoiseBaseLearned; //20 * (SystemConfig.mDoubleNoiseBaseLearned);
                _doubleFindPeak = 16 * SystemConfig.mDoubleNoiseBaseLearned;//100 * (SystemConfig.mDoubleNoiseBaseLearned);

                //_doubleFindSignalTri = 50 * _doubleOneNoiseMean;
                //_doubleFindSignalTri = (SystemConfig.mDoubleSNRLearned / 100) * (SystemConfig.mDoubleNoiseBaseLearned);

                _doubleFindSignalTri = 8 * dNoiseStrengthWud;
                //_doubleFindSignalTri =   100 * _doubleOneNoiseMean;

            }
            //_doubleFindPeak = 20  * (_doubleOneNoiseMean);
            int _iFindCondition = 0x00;
            mDoublemBVSpectrumValuesOneTime[0] = _doubleFindPeak;
            iNoiseStartIdx = 0x00;
            _intFindPeakVar = 0x00;
            iSignalEndIdx = 0x00;
            iMaxCrossIdx_Tri = 0x00;
            for (iVar = mIntTotalFreqSeqsCnt-1; iVar >= 0; iVar--) {
                //jaufa+ note: method using max_hold (No significant differences)
                //if (_intFindPeakVar > 0){
                //    mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar] = _doubleFindPeak;   //最大值設為 _doubleFindPeak
                //}else
                if (((_iFindCondition & 0b0001) == 0b0000) && (mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar]>= _doubleFindPeak)){
                    _iFindCondition |= 0b0001;
                    iMaxCrossIdx_Hr = iVar;
                }
                if (((_iFindCondition & 0b0010) == 0b0000) && (mDoublemBVSpectrumValuesOneTime[iVar]>= _doubleFindNoiseStart)){
                    _iFindCondition |= 0b0010;
                    iNoiseStartIdx = iVar;
                }
                if (((_iFindCondition & 0b0100) == 0b0000) && (mDoublemBVSpectrumValuesOneTime[iVar]>= _doubleFindSignalEnd)){
                    _iFindCondition |= 0b0100;
                    iSignalEndIdx = iVar;
                }
                if (((_iFindCondition & 0b1000) == 0b0000) && (mDoublemBVSpectrumValuesOneTime[iVar]>= _doubleFindSignalTri)){
                    _iFindCondition |= 0b1000;
                    iMaxCrossIdx_Tri = iVar;
                }
            }

            //* jaufa GM
            //iNoiseEndIdx= (iNoiseStartIdx + (mIntTotalFreqSeqsCnt - 1)) / 2;
            //iNoiseEndIdx= iNoiseStartIdx +  (iNoiseStartIdx - iSignalEndIdx);
            iNoiseEndIdx= iNoiseStartIdx;
            /*
            if (SystemConfig.mIntSingleVpkEnabled == SystemConfig.INT_SINGLE_VPK_ENABLED_YES) {
                transDoubleWaveByMovingAverage(mDoubleBVSpectrumIntegralOneTime, mDoubleBVSpectrumIntegralOneTime, 0,128, 3);
                iNoiseEndIdx = iSignalEndIdx + 16;
                iSignalEndIdx = iSignalEndIdx - 16;
                if (iSignalEndIdx<0)
                    iSignalEndIdx = 0;
                if(iNoiseEndIdx>128)
                    iNoiseEndIdx = 128;
            }
             */
            if (iNoiseEndIdx >= mIntTotalFreqSeqsCnt){
                iNoiseEndIdx = mIntTotalFreqSeqsCnt - 1;
            }

            iSignalMaxIdx = 0;
            iMaxCrossIdx_GM = iSignalEndIdx;
            if (iMaxCrossIdx_GM > 0) {
                double _doubleArea; //jaufa+
                double _doubleAreaMax; //jaufa+
                double _doublePoint2SignalX;//jaufa+
                double _doublePoint2SignalY;//jaufa+
                double _doublePoint2NoiseX;//jaufa+
                double _doublePoint2NoiseY;//jaufa+

                //*  jaufa+ : Loop 1
                doubleNoiseEnd =  mDoubleBVSpectrumIntegralOneTime[iNoiseEndIdx];
                doubleNoiseStart = mDoubleBVSpectrumIntegralOneTime[iNoiseStartIdx];
                doubleSignalEnd =  mDoubleBVSpectrumIntegralOneTime[iSignalEndIdx];
                _doubleAreaMax = 0.0;
                if (iSignalEndIdx > iNoiseEndIdx){
                    iMaxCrossIdx_GM = iNoiseEndIdx;
                }else {
                    for (iVar = iSignalEndIdx; iVar <= iNoiseEndIdx; iVar++) {   // jaufa, +, iSignalEndIdx~iNoiseEndIdx全做, 做到尾
                        _doublePoint2SignalX = iSignalEndIdx - iVar;
                        _doublePoint2SignalY = doubleSignalEnd - mDoubleBVSpectrumIntegralOneTime[iVar];
                        _doublePoint2NoiseX = iNoiseEndIdx - iVar;
                        _doublePoint2NoiseY = doubleNoiseEnd - mDoubleBVSpectrumIntegralOneTime[iVar];
                        _doubleArea = _doublePoint2SignalX * _doublePoint2NoiseY - _doublePoint2SignalY * _doublePoint2NoiseX;
                        if (_doubleAreaMax < _doubleArea) {
                            _doubleAreaMax = _doubleArea;
                            iMaxCrossIdx_GM = iVar;
                        }
                    }
                }
            }
            // jaufa GM */

            if (SystemConfig.mIntSingleVpkEnabled == SystemConfig.INT_SINGLE_VPK_ENABLED_YES) {
                int iSingleVpkCompare = 128 - iSignalEndIdx;
                int iSingleVpkIdx = 0;
                if (iSingleVpkCompare < 64) {
                    iSingleVpkCompare = 16;
                } else {
                    iSingleVpkCompare = 16 + (iSingleVpkCompare-60)  ;
                }
                //_doubleFindPeak = iSingleVpkCompare * (SystemConfig.mDoubleNoiseBaseLearned);
                _doubleFindPeak = iSingleVpkCompare * (_doubleOneNoiseMean);
                for (iVar = mIntTotalFreqSeqsCnt-1; iVar >= 0; iVar--) {
                    if ((mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar] >= _doubleFindPeak)){
                        iSingleVpkIdx = iVar;
                        break;
                    }
                }

                mIntArrayMaxIdx[iMaxIdxNextIdx] = iSingleVpkIdx;        // just use a fixed level
                //mIntArrayMaxIdx[iMaxIdxNextIdx] = iSignalEndIdx;        // just use a fixed level
                //mIntArrayMaxIdx[iMaxIdxNextIdx] = _intFindPeakVar;        // just use a fixed level
                //mIntArrayMaxIdx[iMaxIdxNextIdx] = iMaxCrossIdx_GM;

                mIntArrayMaxIdx_Hr[iMaxIdxNextIdx]= _intFindPeakVar;
                mIntArrayMaxIdx_Tri[iMaxIdxNextIdx] = iMaxCrossIdx_Tri;
            } else {
                //mIntArrayMaxIdx_Tri[iMaxIdxNextIdx] = iMaxCrossIdx_GM;
                //mIntArrayMaxIdx_Hr[iMaxIdxNextIdx]= iMaxCrossIdx_Vti;
                iMaxCrossIdx_Vti = iMaxCrossIdx_GM;
                if (iMaxCrossIdx_Vti >= iMaxCrossIdx_Hr){
                //iMaxCrossIdx_Vti = (iMaxCrossIdx_Vti +iMaxCrossIdx_Hr) /2;
                    iMaxCrossIdx_Vti = iMaxCrossIdx_Hr;
                }
                /*jaufa, 180821, DeEcho
                if (_doubleOneNoiseMean > 20 * SystemConfig.mDoubleNoiseBaseLearned){
                    iMaxCrossIdx_Vti = 0;
                    //iMaxCrossIdx_Hr = 0;
                }
                //*jaufa, 180821, DeEcho */

                mIntArrayMaxIdx[iMaxIdxNextIdx] = iMaxCrossIdx_GM;
                //mIntArrayMaxIdx[iMaxIdxNextIdx] = iMaxCrossIdx_Hr;
                //mIntArrayMaxIdx[iMaxIdxNextIdx] = iMaxCrossIdx_Vti;
                //mIntArrayMaxIdx[iMaxIdxNextIdx] = iMaxCrossIdx_Tri;
                //mIntArrayMaxIdx_Tri[iMaxIdxNextIdx] = iMaxCrossIdx_Tri;
                mIntArrayMaxIdx_Tri[iMaxIdxNextIdx] = iMaxCrossIdx_GM;
                //mIntArrayMaxIdx[iMaxIdxNextIdx]=iNoiseStartIdx;
                //mIntArrayMaxIdx[iMaxIdxNextIdx]=iMaxCrossIdx_Hr;
                mIntArrayMaxIdx_Hr[iMaxIdxNextIdx] = iMaxCrossIdx_Hr;
                //mIntArrayMaxIdx_Hr[iMaxIdxNextIdx] = iMaxCrossIdx_Tri;
            }
            mDoubleArraySignalPowerIdx[iMaxIdxNextIdx] = _doubleOneSignalMean/_doubleOneNoiseMean;
            //mIntArrayMaxIdx[iMaxIdxNextIdx]=iMaxCrossIdx_Tri;
            return mIntArrayMaxIdx[iMaxIdxNextIdx];
        } catch (Exception ex1) {
            //SystemConfig.mMyEventLogger.appendDebugStr("OneMaxIdxBySnSiGm.Exception()", "");
            ex1.printStackTrace();
            return mIntArrayMaxIdx[iMaxIdxNextIdx];
        }
    }

    private int processOneMaxIdxBySnSiGm190201(int iMaxIdxNextIdx) {
        int iVar, iSignalStartIdx, iSignalEndIdx, iSignalMaxIdx, iNoiseStartIdx, iNoiseEndIdx;
        int _iMaxCrossIdx, iMaxCrossIdx_Vti=0 , iMaxCrossIdx_Hr, iMaxCrossIdx_GM, iMaxCrossIdx_Tri;
        double doubleSignalEnd, doubleSignalStart, doubleSignalMax, doubleNoiseEnd, doubleNoiseStart;
        double doubleM1, doubleB1, doubleM2, doubleB2, doubleZeroPointA, doubleZeroPointB;
        int iMaxIdx, iCrossIdx, iStart, iZeroPointA, iZeroPointB;
        double doubleMax, doubleMaxOneTime, doubleCross, doubleCrossIdx, doubleDistance, doubleAccu;
        double doublePSDMax, doublePSDSignal, doublePSDNoise, doubleSignalAccu, doubleNoiseAccu, doubleSNR;
//jaufa+ check:  one spectrum normalize divide by mDoubleAmpPsdMaxNormPeriod[iMaxIdxNextIdx]
        try {
            //* jaufa: standard
            double _doubleOneNoiseMax;
            double _doubleOneSignalMax;
            double _doubleOneSignalMean;
            double _doubleOneNoiseMean = 0;
            double _doubleFindPeak;
            boolean _boolFindPeak = false;
            int _intFindPeakVar;
            double _doubleBVSpectrumValuesOneTimeVerticalFilter = 0;
            double _doubleBVSpectrumValuesOneTimeHorizontalFilter = 0;



            // jaufa, +, 191031
            SystemConfig.mDoubleNoiseBaseLearned = SystemConfig.mDoubleNoiseStrengthWu;
            SystemConfig.mDoubleSignalBaseLearned = SystemConfig.mDoubleSignalStrengthWu;
            SystemConfig.mDoubleSNRLearned = SystemConfig.mDoubleSNRLearnedWu;



            //for (iVar = 0; iVar < mIntTotalFreqSeqsCnt; iVar++) {
            //    mDoublemBVSpectrumValuesOneTime[iVar] =   mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar];
            //}

                          //* jaufa Filter
            for (iVar = 0; iVar < mIntTotalFreqSeqsCnt; iVar++) {
                if(iVar > 1 && iVar < mIntTotalFreqSeqsCnt) {
                    if (iMaxIdxNextIdx > 2 && iMaxIdxNextIdx < SystemConfig.mIntSystemMaxSubSegSize-3) {
                        _doubleBVSpectrumValuesOneTimeHorizontalFilter =
                              0.3435736661374695 * mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar]
                                        + 0.2745078276838308 * mDoubleBVSpectrumValues[iMaxIdxNextIdx - 1][iVar]
                                        + 0.2745078276838308 * mDoubleBVSpectrumValues[iMaxIdxNextIdx + 1][iVar]
                                        + 0.1363897237013295 * mDoubleBVSpectrumValues[iMaxIdxNextIdx - 2][iVar]
                                        + 0.1363897237013295 * mDoubleBVSpectrumValues[iMaxIdxNextIdx + 2][iVar]
                                        +  0.02893626643605756  * mDoubleBVSpectrumValues[iMaxIdxNextIdx - 3][iVar]
                                        +  0.02893626643605756  * mDoubleBVSpectrumValues[iMaxIdxNextIdx + 3][iVar];
                    } else {
                        _doubleBVSpectrumValuesOneTimeHorizontalFilter = mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar];
                    }
                }
                if(iVar > 2 && iVar < mIntTotalFreqSeqsCnt-3) {
                    _doubleBVSpectrumValuesOneTimeVerticalFilter =
                            0.3435736661374695 * mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar]
                            +    0.2745078276838308 * mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar-1]
                            +    0.2745078276838308 * mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar+1]
                            +    0.1363897237013295 * mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar-2]
                            +    0.1363897237013295 * mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar+2]
                            +    0.02893626643605756  * mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar-3]
                            +    0.02893626643605756 * mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar+3];

                }else {
                    _doubleBVSpectrumValuesOneTimeVerticalFilter = mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar];
                }
                mDoublemBVSpectrumValuesOneTime[iVar]= 0.4 * _doubleBVSpectrumValuesOneTimeHorizontalFilter+
                        0.6 * _doubleBVSpectrumValuesOneTimeVerticalFilter;
            }
                //* jaufa Filter  */

            _doubleOneSignalMax = 0;
            _doubleOneNoiseMax = 0;
            _doubleOneSignalMean = 0;
            for (iVar = 0; iVar < mIntTotalFreqSeqsCnt-3; iVar++) {
                if((iVar < 24) &&  (iVar >= 8)) {
                    _doubleOneSignalMean = _doubleOneSignalMean + Math.pow(mDoublemBVSpectrumValuesOneTime[iVar],2);
                }
                if (_doubleOneSignalMax <= mDoublemBVSpectrumValuesOneTime[iVar]){
                    _doubleOneSignalMax = mDoublemBVSpectrumValuesOneTime[iVar];
                }
                if(iVar >= mIntTotalFreqSeqsCnt-16){
                    if (_doubleOneNoiseMax<=mDoublemBVSpectrumValuesOneTime[iVar]){
                        _doubleOneNoiseMax=mDoublemBVSpectrumValuesOneTime[iVar];
                    }
                }
                if(iVar >= mIntTotalFreqSeqsCnt-16 && iVar < mIntTotalFreqSeqsCnt){
                    _doubleOneNoiseMean += Math.pow(mDoublemBVSpectrumValuesOneTime[iVar],2);
                }
            }
            _doubleOneSignalMean = Math.pow(_doubleOneSignalMean/16, 0.5);
            _doubleOneNoiseMean= Math.pow(_doubleOneNoiseMean/16, 0.5);

            //* jaufa ----   Integration -----------------------
            mDoubleBVSpectrumIntegralOneTime = new double[mIntTotalFreqSeqsCnt];

            Double doubleIntegral = 0.0;
            Double doubleIntegralWeight = 0.0;
            int _intIntegralCross = 0; //jaufa+

            for (iVar = 0; iVar < mIntTotalFreqSeqsCnt; iVar++) {
                doubleIntegral = doubleIntegral + mDoublemBVSpectrumValuesOneTime[iVar];
                //mDoubleBVSpectrumIntegralOneTime_Pre[iVar] = doubleIntegral;
                mDoubleBVSpectrumIntegralOneTime[iVar] = doubleIntegral;
            }
            //* jaufa ----   Integration ------------------- */


            double jDoubleCompareNoiseMult = 0;
            _intFindPeakVar=0;
             double _doubleMulti = (SystemConfig.mDoubleSNRLearned / 16);
             double _doubleFindNoiseStart;
             double _doubleFindSignalEnd;
             double _doubleFindSignalTri =   20;
            if (SystemConfig.mIntSingleVpkEnabled == SystemConfig.INT_SINGLE_VPK_ENABLED_YES) {
                //* jaufa,  SingleVPK Mode
                if (_doubleMulti > 20)
                    _doubleMulti = 20;
                else if (_doubleMulti < 10)
                    _doubleMulti = 10;
//1
                _doubleFindSignalEnd = _doubleMulti * _doubleOneNoiseMean;
                _doubleFindNoiseStart = 6 * _doubleOneNoiseMean;
                _doubleFindPeak = _doubleMulti * 2 * (SystemConfig.mDoubleNoiseBaseLearned);
//2
                _doubleFindSignalEnd = _doubleMulti * 2 * _doubleOneNoiseMean;
                _doubleFindNoiseStart = 4 * _doubleOneNoiseMean;

                _doubleFindPeak = _doubleMulti * (_doubleOneNoiseMean);
//3
                _doubleMulti = (_doubleOneSignalMean/_doubleOneNoiseMean) / 10;
                //_doubleMulti = (SystemConfig.mDoubleSNRLearned) / 10;
                if (_doubleMulti > 50)
                    _doubleMulti = 50;
                else if (_doubleMulti < 10)
                    _doubleMulti = 10;
                _doubleFindSignalEnd = 30 * (SystemConfig.mDoubleNoiseBaseLearned);
                //_doubleFindSignalEnd = 30 * (_doubleOneNoiseMean);
                _doubleFindNoiseStart = 8  * (SystemConfig.mDoubleNoiseBaseLearned);

                _doubleFindPeak = _doubleMulti * (_doubleOneNoiseMean);

            }else{
                _doubleFindSignalEnd = 4 * SystemConfig.mDoubleNoiseBaseLearned;//50 * _doubleOneNoiseMean;
                _doubleFindNoiseStart = SystemConfig.mDoubleNoiseBaseLearned; //20 * (SystemConfig.mDoubleNoiseBaseLearned);
                _doubleFindPeak = 2 * SystemConfig.mDoubleNoiseBaseLearned;//100 * (SystemConfig.mDoubleNoiseBaseLearned);
                //_doubleFindSignalTri = 50 * _doubleOneNoiseMean;
                //_doubleFindSignalTri = (SystemConfig.mDoubleSNRLearned / 100) * (SystemConfig.mDoubleNoiseBaseLearned);
                _doubleFindSignalTri = 4 * (SystemConfig.mDoubleNoiseBaseLearned);
                //_doubleFindSignalTri =   100 * _doubleOneNoiseMean;

            }
            //_doubleFindPeak = 20  * (_doubleOneNoiseMean);
            int _iFindCondition = 0x00;
            mDoublemBVSpectrumValuesOneTime[0] = _doubleFindPeak;
            iNoiseStartIdx = 0x00;
            _intFindPeakVar = 0x00;
            iSignalEndIdx = 0x00;
            iMaxCrossIdx_Tri = 0x00;
            for (iVar = mIntTotalFreqSeqsCnt-1; iVar >= 0; iVar--) {
                //jaufa+ note: method using max_hold (No significant differences)
                //if (_intFindPeakVar > 0){
                //    mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar] = _doubleFindPeak;   //最大值設為 _doubleFindPeak
                //}else
                if (((_iFindCondition & 0b0001) == 0b0000) && (mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar]>= _doubleFindPeak)){
                    _iFindCondition |= 0b0001;
                    _intFindPeakVar = iVar;
                }
                if (((_iFindCondition & 0b0010) == 0b0000) && (mDoublemBVSpectrumValuesOneTime[iVar]>= _doubleFindNoiseStart)){
                    _iFindCondition |= 0b0010;
                    iNoiseStartIdx = iVar;
                }
                if (((_iFindCondition & 0b0100) == 0b0000) && (mDoublemBVSpectrumValuesOneTime[iVar]>= _doubleFindSignalEnd)){
                    _iFindCondition |= 0b0100;
                    iSignalEndIdx = iVar;
                }
                if (((_iFindCondition & 0b1000) == 0b0000) && (mDoublemBVSpectrumValuesOneTime[iVar]>= _doubleFindSignalTri)){
                    _iFindCondition |= 0b1000;
                    iMaxCrossIdx_Tri = iVar;
                }
            }
                //* jaufa GM
                //iNoiseEndIdx= (iNoiseStartIdx + (mIntTotalFreqSeqsCnt - 1)) / 2;
                //iNoiseEndIdx= iNoiseStartIdx +  (iNoiseStartIdx - iSignalEndIdx);
                iNoiseEndIdx= iNoiseStartIdx;
            /*
                if (SystemConfig.mIntSingleVpkEnabled == SystemConfig.INT_SINGLE_VPK_ENABLED_YES) {
                    transDoubleWaveByMovingAverage(mDoubleBVSpectrumIntegralOneTime, mDoubleBVSpectrumIntegralOneTime, 0,128, 3);
                    iNoiseEndIdx = iSignalEndIdx + 16;
                    iSignalEndIdx = iSignalEndIdx - 16;
                    if (iSignalEndIdx<0)
                        iSignalEndIdx = 0;
                    if(iNoiseEndIdx>128)
                        iNoiseEndIdx = 128;
                }
                */
                if (iNoiseEndIdx >= mIntTotalFreqSeqsCnt){
                    iNoiseEndIdx = mIntTotalFreqSeqsCnt - 1;
                }

                iSignalMaxIdx = 0;

                iMaxCrossIdx_GM = iSignalEndIdx;
                if (iMaxCrossIdx_GM > 0) {
                    double _doubleArea; //jaufa+
                    double _doubleAreaMax; //jaufa+
                    double _doublePoint2SignalX;//jaufa+
                    double _doublePoint2SignalY;//jaufa+
                    double _doublePoint2NoiseX;//jaufa+
                    double _doublePoint2NoiseY;//jaufa+
                    //*  jaufa+ : Loop 1
                    doubleNoiseEnd =  mDoubleBVSpectrumIntegralOneTime[iNoiseEndIdx];
                    doubleNoiseStart = mDoubleBVSpectrumIntegralOneTime[iNoiseStartIdx];
                    doubleSignalEnd =  mDoubleBVSpectrumIntegralOneTime[iSignalEndIdx];
                    _doubleAreaMax = 0.0;
                    for (iVar = iSignalEndIdx; iVar <= iNoiseEndIdx; iVar++) {   // jaufa, +, iSignalEndIdx~iNoiseEndIdx全做, 做到尾
                        _doublePoint2SignalX = iSignalEndIdx - iVar;
                        _doublePoint2SignalY = doubleSignalEnd - mDoubleBVSpectrumIntegralOneTime[iVar];
                        _doublePoint2NoiseX = iNoiseEndIdx - iVar;
                        _doublePoint2NoiseY = doubleNoiseEnd - mDoubleBVSpectrumIntegralOneTime[iVar];
                        _doubleArea = _doublePoint2SignalX * _doublePoint2NoiseY - _doublePoint2SignalY * _doublePoint2NoiseX;
                        if (_doubleAreaMax < _doubleArea) {
                            _doubleAreaMax = _doubleArea;
                            iMaxCrossIdx_GM = iVar;
                        }
                    }
                }
            // jaufa GM */

            if (SystemConfig.mIntSingleVpkEnabled == SystemConfig.INT_SINGLE_VPK_ENABLED_YES) {
                int iSingleVpkCompare = 128 - iSignalEndIdx;
                int iSingleVpkIdx = 0;
                if (iSingleVpkCompare < 64) {
                    iSingleVpkCompare = 16;
                } else {
                    iSingleVpkCompare = 16 + (iSingleVpkCompare-60)  ;
                }
                //_doubleFindPeak = iSingleVpkCompare * (SystemConfig.mDoubleNoiseBaseLearned);
                _doubleFindPeak = iSingleVpkCompare * (_doubleOneNoiseMean);
                for (iVar = mIntTotalFreqSeqsCnt-1; iVar >= 0; iVar--) {
                    if ((mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar] >= _doubleFindPeak)){
                        iSingleVpkIdx = iVar;
                        break;
                    }
                }

                mIntArrayMaxIdx[iMaxIdxNextIdx] = iSingleVpkIdx;        // just use a fixed level
                //mIntArrayMaxIdx[iMaxIdxNextIdx] = iSignalEndIdx;        // just use a fixed level
                //mIntArrayMaxIdx[iMaxIdxNextIdx] = _intFindPeakVar;        // just use a fixed level
                //mIntArrayMaxIdx[iMaxIdxNextIdx] = iMaxCrossIdx_GM;

                mIntArrayMaxIdx_Hr[iMaxIdxNextIdx]= _intFindPeakVar;
            } else {
                //mIntArrayMaxIdx_Tri[iMaxIdxNextIdx] = iMaxCrossIdx_GM;
                mIntArrayMaxIdx_Hr[iMaxIdxNextIdx]= _intFindPeakVar;
                //mIntArrayMaxIdx_Hr[iMaxIdxNextIdx]= iMaxCrossIdx_Vti;
                iMaxCrossIdx_Vti = iMaxCrossIdx_GM;
                //if (iMaxCrossIdx_Vti>= iMaxCrossIdx_Hr){
                    //iMaxCrossIdx_Vti = (iMaxCrossIdx_Vti +iMaxCrossIdx_Hr) /2;
                //    iMaxCrossIdx_Vti = iMaxCrossIdx_Hr;
                //}
                /*jaufa, 180821, DeEcho
                if (_doubleOneNoiseMean > 20 * SystemConfig.mDoubleNoiseBaseLearned){
                    iMaxCrossIdx_Vti = 0;
                    //iMaxCrossIdx_Hr = 0;
                }
                //*jaufa, 180821, DeEcho */

                mIntArrayMaxIdx[iMaxIdxNextIdx] = iMaxCrossIdx_GM;
                //mIntArrayMaxIdx[iMaxIdxNextIdx] = iMaxCrossIdx_Tri;
                //mIntArrayMaxIdx[iMaxIdxNextIdx]=iNoiseStartIdx;
                //mIntArrayMaxIdx[iMaxIdxNextIdx]=iMaxCrossIdx_Hr;
            }
            mIntArrayMaxIdx_Tri[iMaxIdxNextIdx] = iMaxCrossIdx_Tri;
            mDoubleArraySignalPowerIdx[iMaxIdxNextIdx] = _doubleOneSignalMean/_doubleOneNoiseMean;
            //mIntArrayMaxIdx[iMaxIdxNextIdx]=iMaxCrossIdx_Tri;
            return mIntArrayMaxIdx[iMaxIdxNextIdx];
        } catch (Exception ex1) {
            //SystemConfig.mMyEventLogger.appendDebugStr("OneMaxIdxBySnSiGm.Exception()", "");
            ex1.printStackTrace();
            return mIntArrayMaxIdx[iMaxIdxNextIdx];
        }
    }

    private int processOneMaxIdxBySnSiGm181218(int iMaxIdxNextIdx) {
        int iVar, iSignalStartIdx, iSignalEndIdx, iSignalMaxIdx, iNoiseStartIdx, iNoiseEndIdx;
        int _iMaxCrossIdx, iMaxCrossIdx_Vti=0 , iMaxCrossIdx_Hr, iMaxCrossIdx_GM, iMaxCrossIdx_Tri;
        double doubleSignalEnd, doubleSignalStart, doubleSignalMax, doubleNoiseEnd, doubleNoiseStart;
        double doubleM1, doubleB1, doubleM2, doubleB2, doubleZeroPointA, doubleZeroPointB;
        int iMaxIdx, iCrossIdx, iStart, iZeroPointA, iZeroPointB;
        double doubleMax, doubleMaxOneTime, doubleCross, doubleCrossIdx, doubleDistance, doubleAccu;
        double doublePSDMax, doublePSDSignal, doublePSDNoise, doubleSignalAccu, doubleNoiseAccu, doubleSNR;
//jaufa+ check:  one spectrum normalize divide by mDoubleAmpPsdMaxNormPeriod[iMaxIdxNextIdx]
        try {
            //* jaufa: standard
            double _doubleOneNoiseMax;
            double _doubleOneSignalMax;
            double _doubleOneSignalMean;
            double _doubleOneNoiseMean = 0;
            double _doubleFindPeak;
            boolean _boolFindPeak = false;
            int _intFindPeakVar;
            double _doubleBVSpectrumValuesOneTimeVerticalFilter = 0;
            double _doubleBVSpectrumValuesOneTimeHorizontalFilter = 0;
               /* jaufa Filter
            for (iVar = 0; iVar < mIntTotalFreqSeqsCnt; iVar++) {
                if(iVar > 1 && iVar < mIntTotalFreqSeqsCnt) {
                    if (iMaxIdxNextIdx > 2 && iMaxIdxNextIdx < SystemConfig.mIntSystemMaxSubSegSize-3) {
                        _doubleBVSpectrumValuesOneTimeHorizontalFilter =
                              0.3435736661374695 * mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar]
                                        + 0.2745078276838308 * mDoubleBVSpectrumValues[iMaxIdxNextIdx - 1][iVar]
                                        + 0.2745078276838308 * mDoubleBVSpectrumValues[iMaxIdxNextIdx + 1][iVar]
                                        + 0.1363897237013295 * mDoubleBVSpectrumValues[iMaxIdxNextIdx - 2][iVar]
                                        + 0.1363897237013295 * mDoubleBVSpectrumValues[iMaxIdxNextIdx + 2][iVar]
                                        +  0.02893626643605756  * mDoubleBVSpectrumValues[iMaxIdxNextIdx - 3][iVar]
                                        +  0.02893626643605756  * mDoubleBVSpectrumValues[iMaxIdxNextIdx + 3][iVar];
                    } else {
                        _doubleBVSpectrumValuesOneTimeHorizontalFilter = mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar];
                    }
                }
                if(iVar > 2 && iVar < mIntTotalFreqSeqsCnt-3) {
                    _doubleBVSpectrumValuesOneTimeVerticalFilter =
                            0.3435736661374695 * mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar]
                            +    0.2745078276838308 * mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar-1]
                            +    0.2745078276838308 * mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar+1]
                            +    0.1363897237013295 * mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar-2]
                            +    0.1363897237013295 * mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar+2]
                            +    0.02893626643605756  * mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar-3]
                            +    0.02893626643605756 * mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar+3];

                }else {
                    _doubleBVSpectrumValuesOneTimeVerticalFilter = mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar];
                }
                mDoublemBVSpectrumValuesOneTime[iVar]= 0.4 * _doubleBVSpectrumValuesOneTimeHorizontalFilter+
                        0.6 * _doubleBVSpectrumValuesOneTimeVerticalFilter;
            }
                //* jaufa Filter  */


            for (iVar = 0; iVar < mIntTotalFreqSeqsCnt; iVar++) {
                mDoublemBVSpectrumValuesOneTime[iVar] =   mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar];
            }

            _doubleOneSignalMax=0;
            _doubleOneNoiseMax=0;
            _doubleOneSignalMean=0;
            for (iVar = 0; iVar < mIntTotalFreqSeqsCnt-3; iVar++) {
                if((iVar < 24) &&  (iVar >= 8)) {
                    _doubleOneSignalMean = _doubleOneSignalMean + Math.pow(mDoublemBVSpectrumValuesOneTime[iVar],2);
                }
                if (_doubleOneSignalMax<=mDoublemBVSpectrumValuesOneTime[iVar]){
                    _doubleOneSignalMax=mDoublemBVSpectrumValuesOneTime[iVar];
                }
                if(iVar >= mIntTotalFreqSeqsCnt-16){
                    if (_doubleOneNoiseMax<=mDoublemBVSpectrumValuesOneTime[iVar]){
                        _doubleOneNoiseMax=mDoublemBVSpectrumValuesOneTime[iVar];
                    }
                }
                if(iVar >= mIntTotalFreqSeqsCnt-16 && iVar < mIntTotalFreqSeqsCnt){
                    _doubleOneNoiseMean += Math.pow(mDoublemBVSpectrumValuesOneTime[iVar],2);
                }
            }
            _doubleOneSignalMean = Math.pow(_doubleOneSignalMean/16, 0.5);
            _doubleOneNoiseMean= Math.pow(_doubleOneNoiseMean/16, 0.5);

            //* jaufa ----   Integration -----------------------
            mDoubleBVSpectrumIntegralOneTime = new double[mIntTotalFreqSeqsCnt];

            double doubleIntegral = 0.0;
            double doubleIntegralWeight = 0.0;
            int _intIntegralCross = 0; //jaufa+

            for (iVar = 0; iVar < mIntTotalFreqSeqsCnt; iVar++) {
                doubleIntegral = doubleIntegral + mDoublemBVSpectrumValuesOneTime[iVar];
                //mDoubleBVSpectrumIntegralOneTime_Pre[iVar] = doubleIntegral;
                mDoubleBVSpectrumIntegralOneTime[iVar] = doubleIntegral;
            }
            //* jaufa ----   Integration ------------------- */


            double jDoubleCompareNoiseMult = 0;
            _intFindPeakVar=0;
            double _doubleMulti = (SystemConfig.mDoubleSNRLearned / 16);
            double _doubleFindNoiseStart =   6;
            double _doubleFindSignalEnd =   20;
            double _doubleFindSignalTri =   20;
            if (SystemConfig.mIntSingleVpkEnabled == SystemConfig.INT_SINGLE_VPK_ENABLED_YES) {
                //* jaufa,  SingleVPK Mode
                //if (SystemConfig.mDoubleSNRLearned < 200){
                //    mIntArrayMaxIdx[iMaxIdxNextIdx] = 0;
                //    return mIntArrayMaxIdx[iMaxIdxNextIdx];
                //} else {
                    //_doubleFindSignalEnd = 100 * (SystemConfig.mDoubleNoiseBaseLearned);    //_doubleOneNoiseMean;
                    //_doubleFindNoiseStart = 100 * (SystemConfig.mDoubleNoiseBaseLearned);   //_doubleOneNoiseMean;
                    _doubleFindSignalEnd = 50 * (_doubleOneNoiseMean);    //_doubleOneNoiseMean;
                    _doubleFindNoiseStart = 50 * (_doubleOneNoiseMean);   //_doubleOneNoiseMean;
                    //_doubleFindSignalEnd =  (SystemConfig.mDoubleSNRLearned/3) * (SystemConfig.mDoubleNoiseBaseLearned);    //_doubleOneNoiseMean;
                    //_doubleFindNoiseStart = (SystemConfig.mDoubleSNRLearned/3) * (SystemConfig.mDoubleNoiseBaseLearned);   //_doubleOneNoiseMean;
                    _doubleFindPeak = 100 * (SystemConfig.mDoubleNoiseBaseLearned);
                //}
            }else{
                _doubleFindSignalEnd = 50 * _doubleOneNoiseMean;
                _doubleFindNoiseStart = 20 * (SystemConfig.mDoubleNoiseBaseLearned);
                _doubleFindPeak = 100 * (SystemConfig.mDoubleNoiseBaseLearned);
                //_doubleFindSignalTri = 50 * _doubleOneNoiseMean;
                //_doubleFindSignalTri = (SystemConfig.mDoubleSNRLearned / 100) * (SystemConfig.mDoubleNoiseBaseLearned);
                _doubleFindPeak = 100 * (SystemConfig.mDoubleNoiseBaseLearned);
                //_doubleFindSignalTri =   100 * _doubleOneNoiseMean;
            }
            //_doubleFindPeak = 20  * (_doubleOneNoiseMean);
            int _iFindCondition = 0x00;
            mDoublemBVSpectrumValuesOneTime[0] = _doubleFindPeak;
            iNoiseStartIdx = 0x00;
            _intFindPeakVar = 0x00;
            iSignalEndIdx = 0x00;
            iMaxCrossIdx_Tri = 0x00;
            for (iVar = mIntTotalFreqSeqsCnt-1; iVar >= 0; iVar--) {
                //jaufa+ note: method using max_hold (No significant differences)
                //if (_intFindPeakVar > 0){
                //    mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar] = _doubleFindPeak;   //最大值設為 _doubleFindPeak
                //}else
                if (((_iFindCondition & 0b0001) == 0b0000) && (mDoubleBVSpectrumValues[iMaxIdxNextIdx][iVar]>= _doubleFindPeak)){
                    _iFindCondition |= 0b0001;
                    _intFindPeakVar = iVar;
                }
                if (((_iFindCondition & 0b0010) == 0b0000) && (mDoublemBVSpectrumValuesOneTime[iVar]>= _doubleFindNoiseStart)){
                    _iFindCondition |= 0b0010;
                    iNoiseStartIdx = iVar;
                }
                if (((_iFindCondition & 0b0100) == 0b0000) && (mDoublemBVSpectrumValuesOneTime[iVar]>= _doubleFindSignalEnd)){
                    _iFindCondition |= 0b0100;
                    iSignalEndIdx = iVar;
                }
                if (((_iFindCondition & 0b1000) == 0b0000) && (mDoublemBVSpectrumValuesOneTime[iVar]>= _doubleFindSignalTri)){
                    _iFindCondition |= 0b1000;
                    iMaxCrossIdx_Tri = iVar;
                }
            }

            if (SystemConfig.mIntSingleVpkEnabled == SystemConfig.INT_SINGLE_VPK_ENABLED_YES) {
                //* jaufa SNSI
                //transDoubleWaveByMovingAverage(mDoubleBVSpectrumIntegralOneTime, mDoubleBVSpectrumIntegralOneTime, 0,128, 3);

                double _fSNSILengthX =  iNoiseStartIdx - iSignalEndIdx;
                double _fSNSILengthY =  mDoubleBVSpectrumIntegralOneTime[iNoiseStartIdx]
                        - mDoubleBVSpectrumIntegralOneTime[iSignalEndIdx];
                double _fSNSITargetSlop = _fSNSILengthY / _fSNSILengthX;

                int iMaxCrossIdx_SNSI = 0;


                for (iVar = iSignalEndIdx; iVar <= iNoiseStartIdx; iVar++) {   // jaufa, +, iSignalEndIdx~iNoiseEndIdx全做, 做到尾
                    int iVar1 = iVar -2;
                    if (iVar1 < 0) iVar1 = 0;
                    int iVar2 = iVar +2;
                    if (iVar2 >= mIntTotalFreqSeqsCnt) iVar2 = mIntTotalFreqSeqsCnt-1;
                    double _fSNSINowSlop =
                            (mDoubleBVSpectrumIntegralOneTime[iVar2] - mDoubleBVSpectrumIntegralOneTime[iVar1])
                            / (float)(iVar2-iVar1);
                    iMaxCrossIdx_SNSI = iVar;
                    if (_fSNSINowSlop < (_fSNSITargetSlop)) {
                        break;
                    }
                }

                mIntArrayMaxIdx[iMaxIdxNextIdx] = iMaxCrossIdx_SNSI;        // just use a fixed level

                mIntArrayMaxIdx_Hr[iMaxIdxNextIdx]= _intFindPeakVar;
            } else {
                //* jaufa GM

                //iNoiseEndIdx= (iNoiseStartIdx + (mIntTotalFreqSeqsCnt - 1)) / 2;
                //iNoiseEndIdx= iNoiseStartIdx +  (iNoiseStartIdx - iSignalEndIdx);
                iNoiseEndIdx= iNoiseStartIdx;
                if (SystemConfig.mIntSingleVpkEnabled == SystemConfig.INT_SINGLE_VPK_ENABLED_YES) {
                    iNoiseEndIdx = iSignalEndIdx + 16;
                }
                if (iNoiseEndIdx >= mIntTotalFreqSeqsCnt){
                    iNoiseEndIdx = mIntTotalFreqSeqsCnt - 1;
                }

                iSignalMaxIdx = 0;

                iMaxCrossIdx_GM = iSignalEndIdx;
                if (iMaxCrossIdx_GM > 0) {
                    double _doubleArea; //jaufa+
                    double _doubleAreaMax; //jaufa+
                    double _doublePoint2SignalX;//jaufa+
                    double _doublePoint2SignalY;//jaufa+
                    double _doublePoint2NoiseX;//jaufa+
                    double _doublePoint2NoiseY;//jaufa+
                    //*  jaufa+ : Loop 1
                    doubleNoiseEnd =  mDoubleBVSpectrumIntegralOneTime[iNoiseEndIdx];
                    doubleNoiseStart = mDoubleBVSpectrumIntegralOneTime[iNoiseStartIdx];
                    doubleSignalEnd =  mDoubleBVSpectrumIntegralOneTime[iSignalEndIdx];
                    _doubleAreaMax = 0.0;
                    for (iVar = iSignalEndIdx; iVar <= iNoiseEndIdx; iVar++) {   // jaufa, +, iSignalEndIdx~iNoiseEndIdx全做, 做到尾
                        _doublePoint2SignalX = iSignalEndIdx - iVar;
                        _doublePoint2SignalY = doubleSignalEnd - mDoubleBVSpectrumIntegralOneTime[iVar];
                        _doublePoint2NoiseX = iNoiseEndIdx - iVar;
                        _doublePoint2NoiseY = doubleNoiseEnd - mDoubleBVSpectrumIntegralOneTime[iVar];
                        _doubleArea = _doublePoint2SignalX * _doublePoint2NoiseY - _doublePoint2SignalY * _doublePoint2NoiseX;
                        if (_doubleAreaMax < _doubleArea) {
                            _doubleAreaMax = _doubleArea;
                            iMaxCrossIdx_GM = iVar;
                        }
                    }
                }
                // jaufa GM */
                //mIntArrayMaxIdx_Tri[iMaxIdxNextIdx] = iMaxCrossIdx_GM;
                mIntArrayMaxIdx_Hr[iMaxIdxNextIdx]= _intFindPeakVar;
                //mIntArrayMaxIdx_Hr[iMaxIdxNextIdx]= iMaxCrossIdx_Vti;
                iMaxCrossIdx_Vti = iMaxCrossIdx_GM;
                //if (iMaxCrossIdx_Vti>= iMaxCrossIdx_Hr){
                //iMaxCrossIdx_Vti = (iMaxCrossIdx_Vti +iMaxCrossIdx_Hr) /2;
                //    iMaxCrossIdx_Vti = iMaxCrossIdx_Hr;
                //}
                /*jaufa, 180821, DeEcho
                if (_doubleOneNoiseMean > 20 * SystemConfig.mDoubleNoiseBaseLearned){
                    iMaxCrossIdx_Vti = 0;
                    //iMaxCrossIdx_Hr = 0;
                }
                //*jaufa, 180821, DeEcho */

                mIntArrayMaxIdx[iMaxIdxNextIdx] = iMaxCrossIdx_GM;
                //mIntArrayMaxIdx[iMaxIdxNextIdx] = iMaxCrossIdx_Tri;
                //mIntArrayMaxIdx[iMaxIdxNextIdx]=iNoiseStartIdx;
                //mIntArrayMaxIdx[iMaxIdxNextIdx]=iMaxCrossIdx_Hr;
            }
            mIntArrayMaxIdx_Tri[iMaxIdxNextIdx] = iMaxCrossIdx_Tri;
            mDoubleArraySignalPowerIdx[iMaxIdxNextIdx] = _doubleOneSignalMean/_doubleOneNoiseMean;
            //mIntArrayMaxIdx[iMaxIdxNextIdx]=iMaxCrossIdx_Tri;
            return mIntArrayMaxIdx[iMaxIdxNextIdx];
        } catch (Exception ex1) {
            //SystemConfig.mMyEventLogger.appendDebugStr("OneMaxIdxBySnSiGm.Exception()", "");
            ex1.printStackTrace();
            return mIntArrayMaxIdx[iMaxIdxNextIdx];
        }
    }


    public void processAllSegmentMaxIdxByWuNew() {
        if(SystemConfig.mEnumUltrasoundUIState == SystemConfig.ENUM_UI_STATE.ULTRASOUND_UI_STATE_ONLINE){
            processNormalizeForAllDataFromLearnByWuNew();
        }


        //---------- JF code here -------------------
        processAllSegmentMaxIdxBySnSiGm();

    }

}
