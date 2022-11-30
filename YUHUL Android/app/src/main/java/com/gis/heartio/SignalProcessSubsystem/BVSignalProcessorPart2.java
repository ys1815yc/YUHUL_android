package com.gis.heartio.SignalProcessSubsystem;

import android.util.Log;

import com.gis.heartio.SupportSubsystem.SystemConfig;

import com.gis.heartio.UIOperationControlSubsystem.MainActivity;
import com.gis.heartio.UIOperationControlSubsystem.UserManagerCommon;

import java.util.Arrays;


/**
 * Created by 780797 on 2016/6/20.
 */
public class BVSignalProcessorPart2 {

    public int[] mIntArrayHRPeakBottomStates;

    public int mIntGroupIdx;
    public double mDoubleHRPeakIntegralWindowMsec;
    public double mDoubleHRPeakFeatureWindowMsec;
    public double mDoubleHRPeakHalfIntegralWindowMsec;
    public double mDoubleHRPeakHalfFeatureWindowMsec;
    public double mDoubleVpkPeakS1IntegralWindowMsec;
    public double mDoubleVpkPeakS1FeatureWindowMsec;
    //public double mDoubleVpkPeakS2IntegralWindowMsec;
    //public double mDoubleVpkPeakS2FeatureWindowMsec;
    public double mDoublePhantomPeakIntegralWindowMsec;
    public double mDoublePhantomPeakFeatureWindowMsec;
    public double mDoubleHRBottomIntegralWindowMsec;
    public double mDoubleHRBottomFeatureWindowMsec;
    public double mDoubleVTIStartIntegralWindowMsec;
    public double mDoubleVTIStartFeatureWindowMsec;
    public double mDoubleVTIEndIntegralWindowMsec;
    public double mDoubleVTIEndFeatureWindowMsec;
    public double mDoubleVTIStartEndIntegralWindowMsec;
    public double mDoubleVTIStartEndFeatureWindowMsec;
    //public double mDoubleHRBottomSelectCompareRatio;
    //public double mDoubleVpkToStartHumanBodySignalCompareRatio;

    public int mIntHRAreaAccuAll, mIntHRAreaVariAccuAll;
    public double mDoubleHRAreaAverageAll;
    public double mDoubleHRAreaVariAccuAll, mDoubleHRAreaVariAverageAll, mDoubleHRAreaVariAverageAllRatio;

    //public double mDoubleUltrasoundSpeedForBodyMeterSec;
    //public double mDoubleUltrasoundWaveFreq; //2.5 Mega

    //*****************************
    // for Heart Rate Calculate
    //*****************************
    public boolean mBoolHRResultInPart2;
    public double   mDoubleHRPeriodMsec, mDoubleHRAverageValue, mDoubleHRAverageValueAll, mIntHRAverageSubSeg;
    public int mIntHRAccuCntUsed, mIntHRAccuUsed, mIntHRAccuCntUsedByReselectVpk, mIntHRAccuCntUsedByReselectVti;
    public int mIntHRAccuCntAll, mIntHRAccuAll;
    public double mDoubleHRVarianceAccuAll, mDoubleHRVarianceAccuUsed;
    public double mDoubleHRVarianceAll, mDoubleHRVarianceAllRatio, mDoubleHRVarianceUsed, mDoubleHRVarianceUsedRatio;
    public double mDoubleVTIVarianceAll, mDoubleVTIVarianceAllRatio, mDoubleVTIVarianceUsed, mDoubleVTIVarianceUsedRatio;
    public double mDoublePhantomYNCntRatio;
    public int mIntPhantomPeakCntAccuAll, mIntPhantomPeakCntAccuUsed;
    public double mDoublePhantomPeakCntAverageUsed;

    private int mIntHRSelectNextIdx;

    public enum FREQ_AMP_SUM_MUL_HR_STATE_ENUM {MUL_HR_STATE_LEARN_FIND_FIRST_MULPEAK, MUL_HR_STATE_LEARN_REF_VAL, MUL_HR_STATE_LEARN_FAILED, MUL_HR_STATE_FIND_MIN_LOW, MUL_HR_STATE_FIND_MIN_HIGH}
    private FREQ_AMP_SUM_MUL_HR_STATE_ENUM mEnumMulHRProcState;
    public int[][] mIntArrayHRResult = null;
    public int mIntHRResultNextIdx;
    public double[][] mDoubleArrayHRBloodResult = null;

    private double mDoubleHRWindowCurAccuValue;
    private int mIntHRWindowCurSize;
    private int mIntHRWindowNextIdx;
    private double mDoubleHRWindowMinValue;
    private int mIntHRRefNextIdx;
    //private double mDoubleMulHRRefMinLowValue, mDoubleMulHRRefMinHighValue;
    private boolean mBoolHRRefFailed;

    public double[] mDoubleSumFreqAmpMulValues;

    //****************************
    // for Blood Velocity
    //****************************

    public int[] mIntArrayVTIMaxIdx;
    //public int[] mIntArrayBloodPeakEvaluatedMaxIdx;
    //public ENUM_CANDI_PEAK_AND_VPK_STATE [] mEnumCandiPeakAndVpkState;
    public int[] mIntCandiPeakAndVpkState;
    public double[] mDoubleVpkMeterValues;

    private int mIntCandiPeakShortWindowSize , mIntBloodVelocityPeakSelectLongWindowSize;
    private int mIntCandiPeakNextIdx;
    // if (mIntCurBloodVelocityPeakSelectWindowSize <  mIntBloodVelocityPeakSelectWindowSize) -> don't have candidate( mIntCurBloodVelocityPeakIdxInWindo)
    // when mDoubleCurBloodVelocityPeakValue arise then change mIntCurCandiPeakIdxInWindow
    private int mIntCurCandiPeakSelectShortWindowSize , mIntCurBloodVelocityPeakSelectLongWindowSize;
    private int mIntCurCandiPeakIdxInWindow;    // 0 : no candidate , not 0 : candidate Idx
    private int mIntPeakVelocityLastIdx;

    public double mDoubleCurFreqAmpSumPeakReference;
    public double mDoubleFreqAmpSumPeakReference;

    private int mIntVpkAccuCnt;
    private double mDoubleVpkMeterAccuValue;
    public double mDoubleVpkMeterAverage;
    public double mDoubleVpkMeterAverageOri, mDoubleVpkMeterAverageAfterUserAngle ,  mDoubleVpkMeterAverageAfterAngleAfterCali;

    //*Jaufa Define
    public double mDoubleVpkRawAverageOri;
    //Jaufa Define  */

    private int mIntCurCandiPeakRefCnt;
    public int mIntPeakFreqAccumulateReference;
    public int mIntCurCandiPeakAccumulateReference;

    //****************************
    // for VTI (Velocity Time Integration)
    //****************************
    public double[] mDoubleVTICmValues;
    public int[] mIntVTIValues;
    public int mIntVTIAverage, mIntVTIVarianceRatio100;
    public double mDoubleVtiCmAverage;
    public double mDoubleVtiCmAverageOri, mDoubleVtiCmAverageAfterUserAngle, mDoubleVtiCmAverageAfterAngleAfterCali;

    //private int mIntPhantomYNCntAccu;
    private boolean mBoolIsPhantom;

    //****************************
    // for Stroke Volume Calculation
    //****************************
    public double[] mDoubleStrokeVolumeValues;
    public double mDoubleStrokeVolumeMax;
    public double mDoubleStrokeVolumeAverage;
    public double mDoubleStrokeVolumeAverageOri, mDoubleStrokeVolumeAverageAfterUserAngle, mDoubleStrokeVolumeAverageAfterAngleAfterCali;
    public double mDoubleCOAverage;
    public double mDoubleCOAverageOri, mDoubleCOAverageAfterUserAngle, mDoubleCOAverageAfterAngleAfterCali;

    //*********************************
    // for Blood Pressure
    //*********************************
    public double[] mDoubleSumAmpValues;
    double mDoubleMaxSumAmp;

    //*********************************************
    // for Debug Message
    //*********************************************
    //DecimalFormat mFormatDebugMsg;
    //String mStrDebugMsg;
    //Boolean mBoolDebugMsg;

    //*********************************************
    // for Data Integrator
    //*********************************************
    public BVSignalFeaturePeakHR mHRPeakFeature;
    public BVSignalFeatureBottomHR mHRBottomFeature;
    public BVSignalFeatureBottomVTIStart mVTIStartFeature;
    public BVSignalFeatureBottomVTIEnd mVTIEndFeature;
    public BVSignalFeaturePhantom mPhantomPeakFeature;

    public BVSignalProcessorPart2(int iGroupIdx) {

        mIntGroupIdx = iGroupIdx;

        try {

            mIntArrayHRResult = new int[SystemConfig.INT_HR_RESULT_IDX1_CNT][SystemConfig.INT_HR_RESULT_IDX2_CNT];

            mDoubleArrayHRBloodResult = new double[SystemConfig.DOUBLE_HR_BLOOD_RESULT_IDX1_CNT][SystemConfig.DOUBLE_HR_BLOOD_RESULT_IDX2_CNT];

            mDoubleSumFreqAmpMulValues = new double[SystemConfig.mIntSystemMaxSubSegSize];

            mIntArrayVTIMaxIdx = new int[SystemConfig.mIntSystemMaxSubSegSize];
            //mEnumCandiPeakAndVpkState = new ENUM_CANDI_PEAK_AND_VPK_STATE[SystemConfig.mIntSystemMaxSubSegSize];
            mIntCandiPeakAndVpkState = new int[SystemConfig.mIntSystemMaxSubSegSize];
            mDoubleVpkMeterValues = new double[SystemConfig.mIntSystemMaxSubSegSize];
            mIntArrayHRPeakBottomStates = new int[SystemConfig.mIntSystemMaxSubSegSize];

            mDoubleVTICmValues = new double[SystemConfig.mIntSystemMaxSubSegSize];
            mDoubleSumAmpValues = new double[SystemConfig.mIntSystemMaxSubSegSize];
            mDoubleStrokeVolumeValues = new double[SystemConfig.mIntSystemMaxSubSegSize];

            mHRPeakFeature = new BVSignalFeaturePeakHR(this);
            mHRPeakFeature.setFeatureReStartWindowGapRatio(1.0);
            mHRBottomFeature = new BVSignalFeatureBottomHR(this);
            mHRBottomFeature.setFeatureReStartWindowGapRatioAfterHigh(0);
            mHRBottomFeature.setFeatureReStartWindowGapRatioAfterLow(0);

            mVTIStartFeature = new BVSignalFeatureBottomVTIStart(this);
            mVTIStartFeature.setFeatureReStartWindowGapRatio(2.0);
            mVTIStartFeature.setFeatureComparePrePeakRatio(0);

            mVTIEndFeature = new BVSignalFeatureBottomVTIEnd(this);
            mVTIEndFeature.setFeatureReStartWindowGapRatio(1.0);
            mVTIEndFeature.setFeatureComparePrePeakRatio(0);

            mPhantomPeakFeature = new BVSignalFeaturePhantom(this);
            mPhantomPeakFeature.setFeatureReStartWindowGapRatioAfterHigh(0);
            mPhantomPeakFeature.setFeatureReStartWindowGapRatioAfterLow(0);
            mPhantomPeakFeature.setFeatureComparePrePeakRatio(0);

        }catch(Exception ex1 ){
            //SystemConfig.mMyEventLogger.appendDebugStr("BVSignalProcessPart2.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }

    }


    public void prepareStart() {
        int iVar , iVar2;

        try {

            //-------------------------------------------------
            //-- for Group Parameter
            //-------------------------------------------------
            if ((SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_BE_ONLINE)
                    || (SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_LE_ONLINE)
                    || (SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_OFFLINE)
                    || (SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_44K)) {
                mDoubleHRPeakIntegralWindowMsec = SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx]
                        * SystemConfig.DOUBLE_HR_PEAK_INTEGRAL_WINDOW_MSEC_RATIO_MODE_PEAK_ITRI[mIntGroupIdx];
                mDoubleHRPeakFeatureWindowMsec = SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx]
                        * SystemConfig.DOUBLE_HR_PEAK_FEATURE_WINDOW_MSEC_RATIO_MODE_PEAK_ITRI[mIntGroupIdx];
                mDoubleHRBottomIntegralWindowMsec = SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx]
                        * SystemConfig.DOUBLE_HR_BOTTOM_INTEGRAL_WINDOW_MSEC_RATIO_MODE_PEAK_ITRI[mIntGroupIdx];
                mDoubleHRBottomFeatureWindowMsec = SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx]
                        * SystemConfig.DOUBLE_HR_BOTTOM_FEATURE_WINDOW_MSEC_RATIO_MODE_PEAK_ITRI[mIntGroupIdx];

                mDoubleVpkPeakS1IntegralWindowMsec = SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx]
                                * SystemConfig.DOUBLE_VPK_PEAK_S1_INTEGRAL_WINDOW_MSEC_RATIO_ITRI[mIntGroupIdx];
                mDoubleVpkPeakS1FeatureWindowMsec = SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx]
                                * SystemConfig.DOUBLE_VPK_PEAK_S1_FEATURE_WINDOW_MSEC_RATIO_ITRI[mIntGroupIdx];
                 mDoublePhantomPeakIntegralWindowMsec = SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx]
                                * SystemConfig.DOUBLE_PHANTOM_PEAK_INTEGRAL_WINDOW_MSEC_RATIO_ITRI[mIntGroupIdx];
                mDoublePhantomPeakFeatureWindowMsec = SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx]
                                * SystemConfig.DOUBLE_PHANTOM_PEAK_FEATURE_WINDOW_MSEC_RATIO_ITRI[mIntGroupIdx];
                mDoubleVTIStartIntegralWindowMsec = SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx]
                                * SystemConfig.DOUBLE_VTI_START_INTEGRAL_WINDOW_MSEC_RATIO_ITRI[mIntGroupIdx];
                mDoubleVTIStartFeatureWindowMsec = SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx]
                                * SystemConfig.DOUBLE_VTI_START_FEATURE_WINDOW_MSEC_RATIO_ITRI[mIntGroupIdx];
                mDoubleVTIEndIntegralWindowMsec = SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx]
                        * SystemConfig.DOUBLE_VTI_END_INTEGRAL_WINDOW_MSEC_RATIO_ITRI[mIntGroupIdx];
                mDoubleVTIEndFeatureWindowMsec = SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx]
                        * SystemConfig.DOUBLE_VTI_END_FEATURE_WINDOW_MSEC_RATIO_ITRI[mIntGroupIdx];
                //mDoubleVTIStartEndIntegralWindowMsec = SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx]
                //        * SystemConfig.DOUBLE_VTI_START_END_INTEGRAL_WINDOW_MSEC_RATIO_ITRI[mIntGroupIdx];
                //mDoubleVTIStartEndFeatureWindowMsec = SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx]
                //        * SystemConfig.DOUBLE_VTI_START_END_FEATURE_WINDOW_MSEC_RATIO_ITRI[mIntGroupIdx];
            } else {
                mDoubleHRPeakIntegralWindowMsec = SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx]
                                * SystemConfig.DOUBLE_HR_PEAK_INTEGRAL_WINDOW_MSEC_RATIO_USCOM[mIntGroupIdx];
                mDoubleHRPeakFeatureWindowMsec = SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx]
                                * SystemConfig.DOUBLE_HR_PEAK_FEATURE_WINDOW_MSEC_RATIO_USCOM[mIntGroupIdx];
                //mDoubleHRPeakHalfIntegralWindowMsec = SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx]
                //        * SystemConfig.DOUBLE_HR_PEAK_HALF_INTEGRAL_WINDOW_MSEC_RATIO_USCOM[mIntGroupIdx];
                //mDoubleHRPeakHalfFeatureWindowMsec = SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx]
                //        * SystemConfig.DOUBLE_HR_PEAK_HALF_FEATURE_WINDOW_MSEC_RATIO_USCOM[mIntGroupIdx];
                mDoubleVpkPeakS1IntegralWindowMsec = SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx]
                                * SystemConfig.DOUBLE_VPK_PEAK_S1_INTEGRAL_WINDOW_MSEC_RATIO_USCOM[mIntGroupIdx];
                 mDoubleVpkPeakS1FeatureWindowMsec = SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx]
                                * SystemConfig.DOUBLE_VPK_PEAK_S1_FEATURE_WINDOW_MSEC_RATIO_USCOM[mIntGroupIdx];
                mDoublePhantomPeakIntegralWindowMsec = SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx]
                                * SystemConfig.DOUBLE_PHANTOM_PEAK_INTEGRAL_WINDOW_MSEC_RATIO_USCOM[mIntGroupIdx];
                mDoublePhantomPeakFeatureWindowMsec = SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx]
                                * SystemConfig.DOUBLE_PHANTOM_PEAK_FEATURE_WINDOW_MSEC_RATIO_USCOM[mIntGroupIdx];
                mDoubleHRBottomIntegralWindowMsec = SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx]
                                * SystemConfig.DOUBLE_HR_BOTTOM_INTEGRAL_WINDOW_MSEC_RATIO_USCOM[mIntGroupIdx];
                mDoubleHRBottomFeatureWindowMsec = SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx]
                                * SystemConfig.DOUBLE_HR_BOTTOM_FEATURE_WINDOW_MSEC_RATIO_USCOM[mIntGroupIdx];
                mDoubleVTIStartIntegralWindowMsec = SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx]
                                * SystemConfig.DOUBLE_VTI_START_INTEGRAL_WINDOW_MSEC_RATIO_USCOM[mIntGroupIdx];
                mDoubleVTIStartFeatureWindowMsec = SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx]
                                * SystemConfig.DOUBLE_VTI_START_FEATURE_WINDOW_MSEC_RATIO_USCOM[mIntGroupIdx];
                mDoubleVTIEndIntegralWindowMsec = SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx]
                        * SystemConfig.DOUBLE_VTI_END_INTEGRAL_WINDOW_MSEC_RATIO_USCOM[mIntGroupIdx];
                mDoubleVTIEndFeatureWindowMsec = SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx]
                        * SystemConfig.DOUBLE_VTI_END_FEATURE_WINDOW_MSEC_RATIO_USCOM[mIntGroupIdx];
            }

            //mIntPhantomYNCntAccu = 0;

            SystemConfig.mIntRawPacketCntsOnLineForRun = SystemConfig.mIntRawPacketCntsOnLine;

            //--------------------------------------------
            // for HR Calculate
            //--------------------------------------------

            mDoubleHRPeriodMsec = 0;

            mDoubleHRAverageValue = 0;
            mBoolHRResultInPart2 = true;

            mIntCurCandiPeakRefCnt = 0;
            mIntPeakFreqAccumulateReference = 0;
            mIntCurCandiPeakAccumulateReference = 0;

            mEnumMulHRProcState = FREQ_AMP_SUM_MUL_HR_STATE_ENUM.MUL_HR_STATE_LEARN_FIND_FIRST_MULPEAK;

            mDoubleHRWindowCurAccuValue = 0;
            mIntHRWindowCurSize = 0;
            mIntHRWindowNextIdx = 0;

            mIntHRRefNextIdx = 0;

            mIntHRSelectNextIdx = 0;

            mIntHRResultNextIdx = 0;

            mBoolHRRefFailed = false;
            for (iVar = 0; iVar < MainActivity.mBVSignalProcessorPart1.mIntTotalSubSegMaxSize; iVar++) {
                mDoubleSumFreqAmpMulValues[iVar] = 0;
            }

            //--------------------------------------------
            // for Peak Blood Velocity
            //--------------------------------------------
            mIntCurCandiPeakSelectShortWindowSize = 0;
            mIntCurBloodVelocityPeakSelectLongWindowSize = 0;
            mIntCandiPeakNextIdx = 0;
            mIntCurBloodVelocityPeakSelectLongWindowSize = 0;
            mIntCurCandiPeakIdxInWindow = 0;

            for (iVar = 0; iVar < MainActivity.mBVSignalProcessorPart1.mIntTotalSubSegMaxSize; iVar++) {
                mIntArrayVTIMaxIdx[iVar] = 0;
                mIntCandiPeakAndVpkState[iVar] = 0;
                mDoubleVpkMeterValues[iVar] = 0;
            }

            mIntPeakVelocityLastIdx = -1;
            mIntVpkAccuCnt = 0;
            mDoubleVpkMeterAccuValue = 0;
            mDoubleVpkMeterAverage = 0;
            mDoubleVpkMeterAverageOri = 0;
            mDoubleVpkMeterAverageAfterUserAngle = 0;
            mDoubleVpkMeterAverageAfterAngleAfterCali = 0;

            mDoubleCurFreqAmpSumPeakReference = 0;
            mDoubleFreqAmpSumPeakReference = 0;
            for (iVar = 0; iVar < MainActivity.mBVSignalProcessorPart1.mIntTotalSubSegMaxSize; iVar++) {
                mIntArrayHRPeakBottomStates[iVar] = 0;
            }

            //--------------------------------------------
            // for VTI Calculate
            //--------------------------------------------

            for (iVar = 0; iVar < MainActivity.mBVSignalProcessorPart1.mIntTotalSubSegMaxSize; iVar++) {
                mDoubleVTICmValues[iVar] = 0;
            }

            mDoubleVtiCmAverage = 0;
            mDoubleVtiCmAverageOri = 0;
            mDoubleVtiCmAverageAfterUserAngle = 0;
            mDoubleVtiCmAverageAfterAngleAfterCali = 0;

            //--------------------------------------------
            // for Stroke Calculate
            //--------------------------------------------
            for (iVar = 0; iVar < MainActivity.mBVSignalProcessorPart1.mIntTotalSubSegMaxSize; iVar++) {
                mDoubleStrokeVolumeValues[iVar] = 0;
            }

            mDoubleStrokeVolumeAverage = 0;
            mDoubleStrokeVolumeAverageOri = 0;
            mDoubleStrokeVolumeAverageAfterUserAngle = 0;
            mDoubleStrokeVolumeAverageAfterAngleAfterCali = 0;
            mDoubleStrokeVolumeMax = 0; // will be replaced
            //mDoubleStrokeVolumeMin = STROKE_VOLUME_MIN_START; //will be replaced

            mDoubleCOAverage = 0;
            mDoubleCOAverageOri = 0;
            mDoubleCOAverageAfterUserAngle = 0;
            mDoubleCOAverageAfterAngleAfterCali = 0;

            //*********************************
            // for Blood Pressure and Calibration
            //*********************************
            mDoubleMaxSumAmp = -1;

            for (iVar = 0; iVar < MainActivity.mBVSignalProcessorPart1.mIntTotalSubSegMaxSize; iVar++) {
                mDoubleSumAmpValues[iVar] = 0;
            }
            //*********************************
            // for Others
            //*********************************

            mHRPeakFeature.prepareStart();
            mHRBottomFeature.prepareStart();
            mVTIStartFeature.prepareStart();
            mVTIEndFeature.prepareStart();
        }catch(Exception ex1){
            //Log.i("BloodVelSignalProcessor", "prepareStart: ");
            ex1.printStackTrace();
        }
    }

    public void prepareAfterLearned() {
        int iVar , iVar2;

        mIntHRSelectNextIdx = SystemConfig.mIntEndIdxNoiseLearn;

        mIntCandiPeakNextIdx = SystemConfig.mIntEndIdxNoiseLearn;
    }



    //*************************************************************
    // use one window to find the heart rate signal based on Peak Velocity
    //**************************************************************
    public void processAllSegment(){
        //int  iTotalSubSegSize;

        try {

            if (MainActivity.mRawDataProcessor.mEnumUltrasoundOneDataReceiveState == RawDataProcessor.ENUM_RAW_DATA_ONE_DATA_RX_STATE.RECEIVE_STATE_END) {

                prepareStart();

                mHRPeakFeature.prepareStart();
                mHRPeakFeature.processAllSegmentIntegral();
                mHRPeakFeature.processAllSegmentFeature();

                mHRBottomFeature.prepareStart();
                mHRBottomFeature.processAllSegmentIntegral();
                mHRBottomFeature.processAllSegmentFeature();

                mPhantomPeakFeature.prepareStart();
                mPhantomPeakFeature.processAllSegmentIntegral();
                mPhantomPeakFeature.processAllSegmentFeatureStage0();
                mPhantomPeakFeature.checkDiffSumPeakBottomFromStage0();
                mPhantomPeakFeature.prepareStartStage1();
                mPhantomPeakFeature.processAllSegmentFeatureStage1();

                mVTIStartFeature.prepareStart();
                mVTIStartFeature.processAllSegmentIntegral();
                mVTIStartFeature.processAllSegmentFeature();

                mVTIEndFeature.prepareStart();
                mVTIEndFeature.processAllSegmentIntegral();
                mVTIEndFeature.processAllSegmentFeature();

                processAllSegmentHRByPeakMode();
				
                processResultBloodSignal();

            }
        }catch(Exception ex1){
            ex1.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugStr("Part2.processAllSegment.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }

//jaufa, N1, 181014
    public void processAllSegmentOne(){
        //int  iTotalSubSegSize;

        try {

            if (MainActivity.mRawDataProcessor.mEnumUltrasoundOneDataReceiveState == RawDataProcessor.ENUM_RAW_DATA_ONE_DATA_RX_STATE.RECEIVE_STATE_END) {

                prepareStart();

                mHRPeakFeature.prepareStart();
                mHRPeakFeature.processAllSegmentIntegral();
                mHRPeakFeature.processAllSegmentFeature();

                mHRBottomFeature.prepareStart();
                mHRBottomFeature.processAllSegmentIntegral();
                mHRBottomFeature.processAllSegmentFeature();

                mPhantomPeakFeature.prepareStart();
                mPhantomPeakFeature.processAllSegmentIntegral();
                mPhantomPeakFeature.processAllSegmentFeatureStage0();
                mPhantomPeakFeature.checkDiffSumPeakBottomFromStage0();
                mPhantomPeakFeature.prepareStartStage1();
                mPhantomPeakFeature.processAllSegmentFeatureStage1();

                mVTIStartFeature.prepareStart();
                mVTIStartFeature.processAllSegmentIntegral();
                mVTIStartFeature.processAllSegmentFeature();

                mVTIEndFeature.prepareStart();
                mVTIEndFeature.processAllSegmentIntegral();
                mVTIEndFeature.processAllSegmentFeature();

//                processAllSegmentHRByPeakMode();
//                processResultBloodSignal();

            }
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("Part2.processAllSegment.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }
	

    /* jaufa, N1, 181002, One by Jaufa
    public int getVTIValueOne(int _iVTIArea){
        int iVTIAccuCur = 0;
        for (iVar2 = iStart; iVar2 <= iEnd; iVar2++) {
            iFreqIdx = mIntArrayVTIMaxIdx[iVar2];
            if (iFreqIdx == -1) {
                iFreqIdx = 0;
            }
            iVTIAccuCur = iVTIAccuCur + iFreqIdx;
            doubleCurFreqAccu = doubleCurFreqAccu + SystemConfig.mBVSignalProcessorPart1.mDoubleBVFreqValues[iFreqIdx];
        }
        doubleCurVTICm = doubleCurFreqAccu * SystemConfig.DOUBLE_ULTRASOUND_SPEED_FOR_BODY_METER_PERSEC / 2.0 / SystemConfig.mDoubleSensorWaveFreq;
        doubleCurVTICm = doubleCurVTICm * 100.0; // 1 M = 100 cm
        doubleCurVTICm = (doubleCurVTICm * SystemConfig.mDoubleSubSegTimesliceMsec) / 1000.0;

        mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_VALUE_IDX] = iVTIAccuCur;
        return _iArea;
    }
    //*/

    //* jaufa, N1, 181002, One by Jaufa
    public int getVTIAreaOne(int _iStart, int _iEnd, int[] _arIntMaxIdxVTI){
        int _iArea=0;
        for (int _Var = _iStart; _Var<= _iEnd; _Var++){
            _iArea += _arIntMaxIdxVTI[_Var];
        }
        return _iArea;
    }
    //*/

    /*
            if (SystemConfig.mIntCalibrationAdjustType == SystemConfig.INT_CALIBRATION_TYPE_0_NO_CALI) {
        return doubleVpk;
    } else if (SystemConfig.mIntCalibrationAdjustType == SystemConfig.INT_CALIBRATION_TYPE_1_TABLE) {
        return adjustValueFromTable(doubleVpk);
    } else {
        return (doubleVpk * 2.0);
    }

    // */
//* jaufa, 180805, One by Jaufa
    // Cavin, 200828 modified to wu's algorithm
    public void processAllSegmentHRnVTIOne() {
        int iLoopEndIdx, iTmp;
        int iHRStartIdx, iHREndIdx, iVTIStartIdx, iVTIEndIdx;
        try {
            //iLoopEndIdx = MainActivity.mBVSignalProcessorPart1.mIntHRPeriodCounts;
            iLoopEndIdx = SystemConfig.mDopplerInfo.segList.size();
            mIntHRResultNextIdx=0;
            mIntHRSelectNextIdx=1;

            while (mIntHRResultNextIdx < (iLoopEndIdx)) {
                mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_DISCARDED_IDX]
                        = SystemConfig.INT_HR_RESULT_DISCARDED_STATE_YES;
//                mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_VTI_START_END_PRE_SEL_IDX]
//                        = SystemConfig.INT_HR_RESULT_DISCARDED_STATE_YES;

                //Heart Rate Contents
//                iHRStartIdx = MainActivity.mBVSignalProcessorPart1.mIntArrayHRValleyPosition[mIntHRResultNextIdx];
//                iHREndIdx = MainActivity.mBVSignalProcessorPart1.mIntArrayHRValleyPosition[mIntHRSelectNextIdx]-1;
                iHRStartIdx = SystemConfig.mDopplerInfo.segList.get(mIntHRResultNextIdx).StartPt;
                iHREndIdx = SystemConfig.mDopplerInfo.segList.get(mIntHRResultNextIdx).EndPt;
                mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_START_IDX]
                        = iHRStartIdx; //---心跳起點
                mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_END_IDX]
                        = iHREndIdx ; //---心跳終點
                mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_INTERVAL_IDX]
                        = iHREndIdx - iHRStartIdx;

                //*與繪圖有關
                setHRPeriod(mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_START_IDX]
                        , mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_END_IDX]);
                setPeakBottomStatesForHRBottomS2(mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_START_IDX]);
                setPeakBottomStatesForHRStart(mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_START_IDX]);
                setPeakBottomStatesForHREnd(mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_END_IDX]);
                //*與繪圖有關*/

                //VTI Contents
//                iVTIStartIdx = MainActivity.mBVSignalProcessorPart1.mIntArrayVTIStartPosition[mIntHRResultNextIdx];
//                iVTIEndIdx = MainActivity.mBVSignalProcessorPart1.mIntArrayVTIEndPosition[mIntHRResultNextIdx];

                iVTIStartIdx = SystemConfig.mDopplerInfo.segList.get(mIntHRResultNextIdx).segVTIStartPt;
                iVTIEndIdx = SystemConfig.mDopplerInfo.segList.get(mIntHRResultNextIdx).segVTIEndPt;

                mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_VTI_START_IDX]
                        = iVTIStartIdx;
                mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_VTI_END_IDX]
                        = iVTIEndIdx;
//                mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_VTI_START_END_PRE_SEL_IDX]
//                        = iVTIStartIdx;
                mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_VTI_INTERVAL_IDX]
                        = iVTIEndIdx - iVTIStartIdx;

                //VTI MaxIdx Contents
                for (int _Var1=iHRStartIdx; _Var1<=iHREndIdx; _Var1++) {
                    if (_Var1>=iVTIStartIdx && _Var1<=iVTIEndIdx) {
                        mIntArrayVTIMaxIdx[_Var1] = MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx[_Var1];
                    }else {
                        mIntArrayVTIMaxIdx[_Var1] = 0;
                    }
                }


                //VPK Contents
                iTmp=MainActivity.mBVSignalProcessorPart1.mIntArrayVPKPeakPosition[mIntHRResultNextIdx];
                mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_VPK_IDX]
                        = iTmp;
//                Log.d("BVSPP2","VPK index = "+ iTmp);   // index 1750
                iTmp = MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx[iTmp];
                mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_VPK_VALUE_IDX]
                        = iTmp;
//                Log.d("BVSPP2","VPK value = "+ iTmp);   // freq 129
                //*與繪圖有關
                setPeakBottomStatesForVpkPeakS2(mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_VPK_IDX]);
                setPeakBottomStatesForVTIStartS2(mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_VTI_START_IDX]);
                setPeakBottomStatesForVTIEndS2(mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_VTI_END_IDX]);
                //*/

                //  processResultStrokeVolume();
                // processResultCardiacOutput();

                mIntHRResultNextIdx++;
                mIntHRSelectNextIdx++;
            }
        }catch(Exception ex1){
            ex1.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugStr("processAllSegmentHRByPeakMode.Exception","");
        }
    }


    public void processAllSegmentHRByPeakMode() {
        int iHRCurStartIdx, iHRPreStartIdx, iLoopEndIdx;

        if (mIntHRResultNextIdx >= SystemConfig.INT_HR_RESULT_IDX1_CNT) {
            return;
        }

        try {
            if (mHRPeakFeature.mIntFindPeakLast == 0) {
                return;
            }

            iHRCurStartIdx = 0;
            iHRPreStartIdx = 0;
            iLoopEndIdx = mHRPeakFeature.mIntFindPeakLast;
            while (mIntHRSelectNextIdx <= iLoopEndIdx) {
                if (checkPeakBottomStateForHRBottomS1(mIntHRSelectNextIdx)) {    //--- Bottom 點為心跳起點
                        iHRCurStartIdx = mIntHRSelectNextIdx;
                }
                if ((checkPeakBottomStateForHRPeak(mIntHRSelectNextIdx) && (iHRCurStartIdx > 0))) {
                    if (iHRPreStartIdx == 0) {              //--- 第1個Bottom + Peak點尚無法決定心跳起點及終點
                        iHRPreStartIdx = iHRCurStartIdx;
                    }
                    if (iHRPreStartIdx != iHRCurStartIdx) {              //--- 第1個Bottom + Peak點尚無法決定心跳起點及終點
                        mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_START_IDX] = iHRPreStartIdx; //---心跳起點
                        mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_END_IDX] = iHRCurStartIdx - 1; //---心跳終點
                        mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_INTERVAL_IDX] = iHRCurStartIdx - iHRPreStartIdx;

                        setPeakBottomStatesForHRBottomS2(mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_START_IDX]);
                        setPeakBottomStatesForHRStart(mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_START_IDX]);
                        setPeakBottomStatesForHREnd(mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_END_IDX]);

                        iHRPreStartIdx = iHRCurStartIdx;

                        mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_AREA_IDX] =
                                calculateHRArea(mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_START_IDX], mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_END_IDX]);

                        mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_PHANTOM_PEAK_CNT_IDX] =
                                calculatePhantomCnt(mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_START_IDX], mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_END_IDX]);
                        //if(mIntArrayHRResult[mIntHRResultNextIdx][SystemConfig.INT_HR_RESULT_PHANTOM_PEAK_CNT_IDX] == 1){
                        //    iPhantomYNCntAccu ++;
                        //}else{
                        //    mIntPhantomYNCntAccu --;
                        //}

                        mIntHRResultNextIdx++;
                    }
                }
                mIntHRSelectNextIdx++;
            }
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("processAllSegmentHRByPeakMode.Exception","");
        }
    }


    private int calculateHRArea(int iHRStart, int iHREnd){
        int iVar, iHRArea=0;

        for(iVar = iHRStart ; iVar <= iHREnd ; iVar++){
            //iHRArea = iHRArea + SystemConfig.mBVSignalProcessorPart1.mIntArrayMaxIdx[iVar];
            iHRArea = iHRArea + MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdxByMovingAverage[iVar];
        }

        return iHRArea;
    }


     private int calculatePhantomCnt(int iHRStart, int iHREnd){
        int iVar, iPhantomPeakCnt=0;

        for(iVar = iHRStart ; iVar <= iHREnd ; iVar++){
            if(checkPeakBottomStateForPhantomPeak(iVar)){
                iPhantomPeakCnt ++;
            }
        }

        return iPhantomPeakCnt;
    }

/*
    public void processResultBloodSignalOne() {
        //if(SystemConfig.mBoolDebugMode == true){
        //    for (iDebug = 0 ; iDebug < 64 ; iDebug++){
        //        doubleDebug = SystemConfig.mSystemConfig.mBVSignalProcessorPart1.mDoubleBloodVelocityAmpValues[0][iDebug];
        //        Log.i("mDoubleData-STFT" , String.valueOf(iDebug) + " = " + String.format("%.10f", doubleDebug));
        //    }
        //}

        try {

            //------------------------
            // Process for HR Result
            //------------------------

            if(!processResultHROne()){
                mBoolHRResultInPart2 = false;
                return;
            }

            //------------------------
            // Process for VTI Result
            //------------------------

            processResultVpkAndVTIStartEnd();

            if(!processResultVTIandVpk()){
                return;
            }

            processResultStrokeVolume();

            processResultCardiacOutput();

        } catch (Exception ex1) {
            Log.i("Application Error", "in processBloodSignal function");
        }
    }
//*/

//*jaufa, N1, 181002
    public void processResultBloodSignalByNoSelOne() {
        try {

            /*
            if(!processResultHR()){
                mBoolHRResultInPart2 = false;
                return;
            }

            //------------------------
            // Process for VTI Result
            //------------------------

            processResultVpkAndVTIStartEnd();

            if(!processResultVTIandVpk()){
                return;
            }

            processResultStrokeVolume();

            processResultCardiacOutput();
//*/


//jaufa, +
            //setHRPeriodAllDiscarded(true);
            //setVTIAllDiscarded(true);

            //processAllSegmentHRnVTIOne();
            // HR Result
            mDoubleArrayHRBloodResult[SystemConfig.DOUBLE_HR_BLOOD_RESULT_ORI_STATE][SystemConfig.DOUBLE_HR_BLOOD_RESULT_HR_IDX]
                    =  MainActivity.mBVSignalProcessorPart1.mDoubleHrBloodHr;

            //VPK
            mDoubleArrayHRBloodResult[SystemConfig.DOUBLE_HR_BLOOD_RESULT_ORI_STATE][SystemConfig.DOUBLE_HR_BLOOD_RESULT_VPK_IDX]
                    =  MainActivity.mBVSignalProcessorPart1.mDoubleHrBloodVpk;

            mDoubleArrayHRBloodResult[SystemConfig.DOUBLE_HR_BLOOD_RESULT_ORI_STATE][SystemConfig.DOUBLE_HR_BLOOD_RESULT_VTI_IDX]
                    =  MainActivity.mBVSignalProcessorPart1.mDoubleHrBloodVti;

            mDoubleArrayHRBloodResult[SystemConfig.DOUBLE_HR_BLOOD_RESULT_ORI_STATE][SystemConfig.DOUBLE_HR_BLOOD_RESULT_SV_IDX]
                    =  MainActivity.mBVSignalProcessorPart1.mDoubleHrBloodSV;

            mDoubleArrayHRBloodResult[SystemConfig.DOUBLE_HR_BLOOD_RESULT_ORI_STATE][SystemConfig.DOUBLE_HR_BLOOD_RESULT_CO_IDX]
                    =  MainActivity.mBVSignalProcessorPart1.mDoubleHrBloodCO;

            //------------------------
            // Process for HR Result
            //------------------------
        } catch (Exception ex1) {
            Log.i("Application Error", "in processBloodSignal function");
        }
    }
//*/

  public void processResultBloodSignal() {
         //if(SystemConfig.mBoolDebugMode == true){
        //    for (iDebug = 0 ; iDebug < 64 ; iDebug++){
        //        doubleDebug = SystemConfig.mSystemConfig.mBVSignalProcessorPart1.mDoubleBloodVelocityAmpValues[0][iDebug];
        //        Log.i("mDoubleData-STFT" , String.valueOf(iDebug) + " = " + String.format("%.10f", doubleDebug));
        //    }
        //}

        try {

            //------------------------
            // Process for HR Result
            //------------------------

            if(!processResultHR()){
                mBoolHRResultInPart2 = false;
                return;
            }

            //------------------------
            // Process for VTI Result
            //------------------------

            processResultVpkAndVTIStartEnd();

            if(!processResultVTIandVpk()){
                return;
            }

            processResultStrokeVolume();

            processResultCardiacOutput();

        } catch (Exception ex1) {
            Log.i("Application Error", "in processBloodSignal function");
        }
    }

    public void processResultBloodSignalByRecalculate() {

        try {
            if (!processResultHRByCalculate()) {
                //mDoubleHRAverageValue = -1;
                //mDoubleVpkMeterAverage = -1;
                //mDoubleVtiCmAverage = -1;
                //mDoubleStrokeVolumeAverage = -1;
                //mDoubleCOAverage = -1;
                mBoolHRResultInPart2 = false;
                return;
            }

            if (!processResultVTIandVpk()) {
                mDoubleVpkMeterAverage = -1;
                mDoubleVtiCmAverage = -1;
                mDoubleStrokeVolumeAverage = -1;
                mDoubleCOAverage = -1;
                return;
            }
            processResultStrokeVolume();
            processResultCardiacOutput();

            mDoubleArrayHRBloodResult[SystemConfig.DOUBLE_HR_BLOOD_RESULT_SEL_STATE][SystemConfig.DOUBLE_HR_BLOOD_RESULT_HR_IDX]
                    = mDoubleHRAverageValue;
            mDoubleArrayHRBloodResult[SystemConfig.DOUBLE_HR_BLOOD_RESULT_SEL_STATE][SystemConfig.DOUBLE_HR_BLOOD_RESULT_VPK_IDX]
                    = mDoubleVpkMeterAverage;
            mDoubleArrayHRBloodResult[SystemConfig.DOUBLE_HR_BLOOD_RESULT_SEL_STATE][SystemConfig.DOUBLE_HR_BLOOD_RESULT_VTI_IDX]
                    = mDoubleVtiCmAverage;
            mDoubleArrayHRBloodResult[SystemConfig.DOUBLE_HR_BLOOD_RESULT_SEL_STATE][SystemConfig.DOUBLE_HR_BLOOD_RESULT_SV_IDX]
                    = mDoubleStrokeVolumeAverage;
            mDoubleArrayHRBloodResult[SystemConfig.DOUBLE_HR_BLOOD_RESULT_SEL_STATE][SystemConfig.DOUBLE_HR_BLOOD_RESULT_CO_IDX]
                    = mDoubleCOAverage;

        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("ByRecalculate.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            ex1.printStackTrace();
        }

    }

//* jaufa, 180805, + For One Group
    public boolean processResultHROne(){
        int iVar;
        int[] iArrayMulHRResultInterval, iArrayPhantomPeakCnt;
        int iHRDiff;
        int iHRAccu, iHRAccuCnt, iHRAverage;
        double doubleDiff, doubleHRIntervalMSec;
        int iPeakCntNumberForMax, iPeakCntNumberAccuCntForMax, iPeakCntNumberCur, iPeakCntNumberAccuCntCur;

        try {
            if(mIntHRResultNextIdx == 0){
                return false;
            }

            //--- Phantom HR period requirement ---??-----------------------------
            for (iVar = 0; iVar < mIntHRResultNextIdx; iVar++) {
                setHRPeriod(mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_START_IDX]
                        , mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_END_IDX]);
            }

            iArrayMulHRResultInterval = new int[mIntHRResultNextIdx];
            for (iVar = 0; iVar < mIntHRResultNextIdx; iVar++) {
                iArrayMulHRResultInterval[iVar] = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_INTERVAL_IDX];
            }
            Arrays.sort(iArrayMulHRResultInterval);

            iHRAccu = 0;
            iHRAccuCnt = 0;
            for (iVar = 0; iVar < mIntHRResultNextIdx; iVar++) {
                doubleHRIntervalMSec = (double)iArrayMulHRResultInterval[iVar] * (double)SystemConfig.INT_BLOOD_VELOCITY_STFT_WINDOW_SHIFT_SIZE_64 * 1000.0 /8000.0;
                if((doubleHRIntervalMSec <= (SystemConfig.DOUBLE_HR_PERIOD_MSEC_MAX[mIntGroupIdx] * 1.1))
                        && (doubleHRIntervalMSec >= (SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx] * SystemConfig.DOUBLE_HR_PEAK_FEATURE_WINDOW_MSEC_RATIO_MODE_PEAK_ITRI[mIntGroupIdx]))){
                    iHRAccu = iHRAccu + iArrayMulHRResultInterval[iVar];
                    iHRAccuCnt++;
                }
            }
            if(iHRAccuCnt < 2){
                return false;
            }else {
                iHRAverage = iHRAccu / iHRAccuCnt;
            }

            //-----------------------------------
            mIntHRAccuAll = 0;
            mIntHRAccuCntAll = 0;
            mDoubleHRVarianceAccuAll = 0;
            mDoubleHRVarianceAccuUsed = 0;
            mIntHRAccuUsed = 0;
            mIntHRAccuCntUsed = 0;
            mIntHRAreaAccuAll = 0;
            mIntHRAreaVariAccuAll = 0;
            mIntPhantomPeakCntAccuAll = 0;
            mIntPhantomPeakCntAccuUsed = 0;
            for (iVar = 0; iVar < mIntHRResultNextIdx; iVar++) {
                iHRDiff = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_INTERVAL_IDX] - iHRAverage;
                if (iHRDiff < 0) {
                    iHRDiff = -iHRDiff;
                }
                mDoubleHRVarianceAccuAll = mDoubleHRVarianceAccuAll + iHRDiff * iHRDiff;
                mIntHRAccuCntAll++;

                mIntHRAreaAccuAll = mIntHRAreaAccuAll + mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_AREA_IDX];

                //mIntPhantomPeakCntAccuAll = mIntPhantomPeakCntAccuAll + mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_PHANTOM_PEAK_CNT_IDX];

                doubleHRIntervalMSec = (double) mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_INTERVAL_IDX] * (double)SystemConfig.INT_BLOOD_VELOCITY_STFT_WINDOW_SHIFT_SIZE_64 * 1000.0 /8000.0;
                if((doubleHRIntervalMSec <= (SystemConfig.DOUBLE_HR_PERIOD_MSEC_MAX[mIntGroupIdx] * 1.1))
                        && (doubleHRIntervalMSec >= (SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx] * SystemConfig.DOUBLE_HR_PEAK_FEATURE_WINDOW_MSEC_RATIO_MODE_PEAK_ITRI[mIntGroupIdx]))){
                    mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_DISCARDED_IDX] = SystemConfig.INT_HR_RESULT_DISCARDED_STATE_NO;
                    setHRPeriodDiscarded(mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_START_IDX]
                            , mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_END_IDX], false);

                    mIntHRAccuUsed = mIntHRAccuUsed + mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_INTERVAL_IDX];
                    mDoubleHRVarianceAccuUsed = mDoubleHRVarianceAccuUsed + iHRDiff * iHRDiff;
                    mIntPhantomPeakCntAccuUsed = mIntPhantomPeakCntAccuUsed + mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_PHANTOM_PEAK_CNT_IDX];
                    mIntHRAccuCntUsed++;
                } else {
                    mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_DISCARDED_IDX] = SystemConfig.INT_HR_RESULT_DISCARDED_STATE_YES;
                    setHRPeriodDiscarded(mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_START_IDX]
                            , mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_END_IDX], true);
                }
            }

            mIntHRAverageSubSeg =  mIntHRAccuUsed / mIntHRAccuCntUsed;
            mDoubleHRPeriodMsec = ((double) mIntHRAccuUsed * SystemConfig.mDoubleSubSegTimesliceMsec) / (double) mIntHRAccuCntUsed;
            mDoubleHRAverageValue = (double) 60000 / mDoubleHRPeriodMsec;

            mDoubleHRVarianceUsed = Math.sqrt((mDoubleHRVarianceAccuUsed) / (double) mIntHRAccuCntUsed);
            mDoubleHRVarianceUsedRatio = mDoubleHRVarianceUsed / iHRAverage;
            mDoubleHRVarianceAll = Math.sqrt(mDoubleHRVarianceAccuAll / (double) mIntHRAccuCntAll);
            mDoubleHRVarianceAllRatio = mDoubleHRVarianceAll / (double) iHRAverage;

            mDoubleHRAreaAverageAll = (double) mIntHRAreaAccuAll / (double) mIntHRAccuCntAll;
            //mDoublePhantomPeakCntAverageAll = (double) mIntPhantomPeakCntAccuAll / (double) mIntHRAccuCntAll;

            mDoubleHRAreaVariAccuAll = 0;
            for (iVar = 0; iVar < mIntHRResultNextIdx; iVar++) {
                doubleDiff = (double) mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_AREA_IDX] - mDoubleHRAreaAverageAll;
                mDoubleHRAreaVariAccuAll = mDoubleHRAreaVariAccuAll + doubleDiff * doubleDiff;
            }
            mDoubleHRAreaVariAverageAll = Math.sqrt(mDoubleHRAreaVariAccuAll / (double) mIntHRAccuCntUsed);
            mDoubleHRAreaVariAverageAllRatio = mDoubleHRAreaVariAverageAll / mDoubleHRAreaAverageAll;
            mDoublePhantomPeakCntAverageUsed = (double) mIntPhantomPeakCntAccuUsed / (double) mIntHRAccuCntUsed;

            return true;
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("processResultHR.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            mDoubleHRAverageValue = -1;
            mDoubleVpkMeterAverage = -1;
            mDoubleVtiCmAverage = -1;
            mDoubleStrokeVolumeAverage = -1;
            mDoubleCOAverage = -1;
            mDoubleHRVarianceAll = Double.MAX_VALUE;
            mDoubleHRVarianceUsed = Double.MAX_VALUE;
            return false;
        }
    }

    public boolean processResultHR(){
        int iVar;
        int[] iArrayMulHRResultInterval, iArrayPhantomPeakCnt;
        int iHRDiff;
        int iHRAccu, iHRAccuCnt, iHRAverage;
        double doubleDiff, doubleHRIntervalMSec;
        int iPeakCntNumberForMax, iPeakCntNumberAccuCntForMax, iPeakCntNumberCur, iPeakCntNumberAccuCntCur;

        try {

            if(mIntHRResultNextIdx == 0){
                return false;
            }

            //--- Phantom HR period requirement --------------------------------
            for (iVar = 0; iVar < mIntHRResultNextIdx; iVar++) {
                setHRPeriod(mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_START_IDX]
                        , mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_END_IDX]);
            }

            iArrayMulHRResultInterval = new int[mIntHRResultNextIdx];
            for (iVar = 0; iVar < mIntHRResultNextIdx; iVar++) {
                iArrayMulHRResultInterval[iVar] = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_INTERVAL_IDX];
            }
            Arrays.sort(iArrayMulHRResultInterval);

            iHRAccu = 0;
            iHRAccuCnt = 0;
            for (iVar = 0; iVar < mIntHRResultNextIdx; iVar++) {
                doubleHRIntervalMSec = (double)iArrayMulHRResultInterval[iVar] * (double)SystemConfig.INT_BLOOD_VELOCITY_STFT_WINDOW_SHIFT_SIZE_64 * 1000.0 /8000.0;
                if((doubleHRIntervalMSec <= (SystemConfig.DOUBLE_HR_PERIOD_MSEC_MAX[mIntGroupIdx] * 1.2))
                          && (doubleHRIntervalMSec >= (SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx] * SystemConfig.DOUBLE_HR_PEAK_FEATURE_WINDOW_MSEC_RATIO_MODE_PEAK_ITRI[mIntGroupIdx] *0.8))){
                    iHRAccu = iHRAccu + iArrayMulHRResultInterval[iVar];
                    iHRAccuCnt++;
                }
            }
            if(iHRAccuCnt < 2){
                return false;
            }else {
                iHRAverage = iHRAccu / iHRAccuCnt;
            }

            //-----------------------------------
            mIntHRAccuAll = 0;
            mIntHRAccuCntAll = 0;
            mDoubleHRVarianceAccuAll = 0;
            mDoubleHRVarianceAccuUsed = 0;
            mIntHRAccuUsed = 0;
            mIntHRAccuCntUsed = 0;
            mIntHRAreaAccuAll = 0;
            mIntHRAreaVariAccuAll = 0;
            mIntPhantomPeakCntAccuAll = 0;
            mIntPhantomPeakCntAccuUsed = 0;
            for (iVar = 0; iVar < mIntHRResultNextIdx; iVar++) {
                iHRDiff = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_INTERVAL_IDX] - iHRAverage;
                if (iHRDiff < 0) {
                    iHRDiff = -iHRDiff;
                }
                mDoubleHRVarianceAccuAll = mDoubleHRVarianceAccuAll + iHRDiff * iHRDiff;
                mIntHRAccuCntAll++;

                mIntHRAreaAccuAll = mIntHRAreaAccuAll + mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_AREA_IDX];

                mIntPhantomPeakCntAccuAll = mIntPhantomPeakCntAccuAll + mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_PHANTOM_PEAK_CNT_IDX];

                doubleHRIntervalMSec = (double) mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_INTERVAL_IDX] * (double)SystemConfig.INT_BLOOD_VELOCITY_STFT_WINDOW_SHIFT_SIZE_64 * 1000.0 /8000.0;
                if((doubleHRIntervalMSec <= (SystemConfig.DOUBLE_HR_PERIOD_MSEC_MAX[mIntGroupIdx] * 1.1))
                          && (doubleHRIntervalMSec >= (SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx] * SystemConfig.DOUBLE_HR_PEAK_FEATURE_WINDOW_MSEC_RATIO_MODE_PEAK_ITRI[mIntGroupIdx]))){
                    mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_DISCARDED_IDX] = SystemConfig.INT_HR_RESULT_DISCARDED_STATE_NO;
                    setHRPeriodDiscarded(mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_START_IDX]
                            , mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_END_IDX], false);

                    mIntHRAccuUsed = mIntHRAccuUsed + mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_INTERVAL_IDX];
                    mDoubleHRVarianceAccuUsed = mDoubleHRVarianceAccuUsed + iHRDiff * iHRDiff;
                    mIntPhantomPeakCntAccuUsed = mIntPhantomPeakCntAccuUsed + mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_PHANTOM_PEAK_CNT_IDX];
                    mIntHRAccuCntUsed++;
                } else {
                    mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_DISCARDED_IDX] = SystemConfig.INT_HR_RESULT_DISCARDED_STATE_YES;
                    setHRPeriodDiscarded(mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_START_IDX]
                            , mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_END_IDX], true);
                }
            }

            mIntHRAverageSubSeg =  mIntHRAccuUsed / mIntHRAccuCntUsed;
            mDoubleHRPeriodMsec = ((double) mIntHRAccuUsed * SystemConfig.mDoubleSubSegTimesliceMsec) / (double) mIntHRAccuCntUsed;
            mDoubleHRAverageValue = (double) 60000 / mDoubleHRPeriodMsec;

            mDoubleHRVarianceUsed = Math.sqrt((mDoubleHRVarianceAccuUsed) / (double) mIntHRAccuCntUsed);
            mDoubleHRVarianceUsedRatio = mDoubleHRVarianceUsed / iHRAverage;
            mDoubleHRVarianceAll = Math.sqrt(mDoubleHRVarianceAccuAll / (double) mIntHRAccuCntAll);
            mDoubleHRVarianceAllRatio = mDoubleHRVarianceAll / (double) iHRAverage;

            mDoubleHRAreaAverageAll = (double) mIntHRAreaAccuAll / (double) mIntHRAccuCntAll;
            //mDoublePhantomPeakCntAverageAll = (double) mIntPhantomPeakCntAccuAll / (double) mIntHRAccuCntAll;

            mDoubleHRAreaVariAccuAll = 0;
            for (iVar = 0; iVar < mIntHRResultNextIdx; iVar++) {
                doubleDiff = (double) mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_AREA_IDX] - mDoubleHRAreaAverageAll;
                mDoubleHRAreaVariAccuAll = mDoubleHRAreaVariAccuAll + doubleDiff * doubleDiff;
            }
            mDoubleHRAreaVariAverageAll = Math.sqrt(mDoubleHRAreaVariAccuAll / (double) mIntHRAccuCntUsed);
            mDoubleHRAreaVariAverageAllRatio = mDoubleHRAreaVariAverageAll / mDoubleHRAreaAverageAll;
            mDoublePhantomPeakCntAverageUsed = (double) mIntPhantomPeakCntAccuUsed / (double) mIntHRAccuCntUsed;

            return true;
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("processResultHR.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            mDoubleHRAverageValue = -1;
            mDoubleVpkMeterAverage = -1;
            mDoubleVtiCmAverage = -1;
            mDoubleStrokeVolumeAverage = -1;
            mDoubleCOAverage = -1;
            mDoubleHRVarianceAll = Double.MAX_VALUE;
            mDoubleHRVarianceUsed = Double.MAX_VALUE;
            return false;
        }
    }



    public boolean processResultHR_20180501(){
        int iVar;
        int[] iArrayMulHRResultInterval, iArrayPhantomPeakCnt;
        int iHRDiff;
        int iHRAccu, iHRAccuCnt, iHRAverage;
        double doubleDiff, doubleHRIntervalMSec;
        int iPeakCntNumberForMax, iPeakCntNumberAccuCntForMax, iPeakCntNumberCur, iPeakCntNumberAccuCntCur;

        try {

            if(mIntHRResultNextIdx == 0){
                return false;
            }

            //--- Phantom Peak Number requirement --------------------------------
            iArrayPhantomPeakCnt = new int[mIntHRResultNextIdx];
            for (iVar = 0; iVar < mIntHRResultNextIdx; iVar++) {
                iArrayPhantomPeakCnt[iVar] = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_PHANTOM_PEAK_CNT_IDX];
            }
            Arrays.sort(iArrayPhantomPeakCnt);
            iPeakCntNumberForMax = -1;
            iPeakCntNumberAccuCntForMax = 0;
            iPeakCntNumberCur = iArrayPhantomPeakCnt[0];
            iPeakCntNumberAccuCntCur = 1;
            for (iVar = 1; iVar < mIntHRResultNextIdx; iVar++) {
                if(iArrayPhantomPeakCnt[iVar] != iPeakCntNumberCur) {
                    if (iPeakCntNumberAccuCntForMax < iPeakCntNumberAccuCntCur) {
                        iPeakCntNumberForMax = iPeakCntNumberCur;
                        iPeakCntNumberAccuCntForMax = iPeakCntNumberAccuCntCur;
                        iPeakCntNumberCur = iArrayPhantomPeakCnt[iVar];
                        iPeakCntNumberAccuCntCur = 1;
                    }
                }else{
                    iPeakCntNumberAccuCntCur ++;
                }
            }
            if(iPeakCntNumberAccuCntForMax < (mIntHRResultNextIdx * 3 /4)){
                return false;
            }

            //--- Phantom HR period requirement --------------------------------
            for (iVar = 0; iVar < mIntHRResultNextIdx; iVar++) {
                setHRPeriod(mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_START_IDX]
                        , mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_END_IDX]);
            }

            iArrayMulHRResultInterval = new int[mIntHRResultNextIdx];
            for (iVar = 0; iVar < mIntHRResultNextIdx; iVar++) {
                iArrayMulHRResultInterval[iVar] = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_INTERVAL_IDX];
            }
            Arrays.sort(iArrayMulHRResultInterval);

            iHRAccu = 0;
            iHRAccuCnt = 0;
            for (iVar = 0; iVar < mIntHRResultNextIdx; iVar++) {
                doubleHRIntervalMSec = (double)iArrayMulHRResultInterval[iVar] * (double)SystemConfig.INT_BLOOD_VELOCITY_STFT_WINDOW_SHIFT_SIZE_64 * 1000.0 /8000.0;
                if((doubleHRIntervalMSec <= (SystemConfig.DOUBLE_HR_PERIOD_MSEC_MAX[mIntGroupIdx] * 1.1))
                        && (doubleHRIntervalMSec >= (SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx] * SystemConfig.DOUBLE_HR_PEAK_FEATURE_WINDOW_MSEC_RATIO_MODE_PEAK_ITRI[mIntGroupIdx]))){
                    iHRAccu = iHRAccu + iArrayMulHRResultInterval[iVar];
                    iHRAccuCnt++;
                }
            }
            if(iHRAccuCnt < 2){
                return false;
            }else {
                iHRAverage = iHRAccu / iHRAccuCnt;
            }

            //-----------------------------------
            mIntHRAccuAll = 0;
            mIntHRAccuCntAll = 0;
            mDoubleHRVarianceAccuAll = 0;
            mDoubleHRVarianceAccuUsed = 0;
            mIntHRAccuUsed = 0;
            mIntHRAccuCntUsed = 0;
            mIntHRAreaAccuAll = 0;
            mIntHRAreaVariAccuAll = 0;
            mIntPhantomPeakCntAccuAll = 0;
            mIntPhantomPeakCntAccuUsed = 0;
            for (iVar = 0; iVar < mIntHRResultNextIdx; iVar++) {
                iHRDiff = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_INTERVAL_IDX] - iHRAverage;
                if (iHRDiff < 0) {
                    iHRDiff = -iHRDiff;
                }
                mDoubleHRVarianceAccuAll = mDoubleHRVarianceAccuAll + iHRDiff * iHRDiff;
                mIntHRAccuCntAll++;

                mIntHRAreaAccuAll = mIntHRAreaAccuAll + mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_AREA_IDX];

                mIntPhantomPeakCntAccuAll = mIntPhantomPeakCntAccuAll + mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_PHANTOM_PEAK_CNT_IDX];

                doubleHRIntervalMSec = (double) mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_INTERVAL_IDX] * (double)SystemConfig.INT_BLOOD_VELOCITY_STFT_WINDOW_SHIFT_SIZE_64 * 1000.0 /8000.0;
                if((doubleHRIntervalMSec <= (SystemConfig.DOUBLE_HR_PERIOD_MSEC_MAX[mIntGroupIdx] * 1.1))
                        && (doubleHRIntervalMSec >= (SystemConfig.DOUBLE_HR_PERIOD_MSEC_MIN[mIntGroupIdx] * SystemConfig.DOUBLE_HR_PEAK_FEATURE_WINDOW_MSEC_RATIO_MODE_PEAK_ITRI[mIntGroupIdx]))){
                    mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_DISCARDED_IDX] = SystemConfig.INT_HR_RESULT_DISCARDED_STATE_NO;
                    setHRPeriodDiscarded(mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_START_IDX]
                            , mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_END_IDX], false);

                    mIntHRAccuUsed = mIntHRAccuUsed + mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_INTERVAL_IDX];
                    mDoubleHRVarianceAccuUsed = mDoubleHRVarianceAccuUsed + iHRDiff * iHRDiff;
                    mIntPhantomPeakCntAccuUsed = mIntPhantomPeakCntAccuUsed + mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_PHANTOM_PEAK_CNT_IDX];
                    mIntHRAccuCntUsed++;
                } else {
                    mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_DISCARDED_IDX] = SystemConfig.INT_HR_RESULT_DISCARDED_STATE_YES;
                    setHRPeriodDiscarded(mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_START_IDX]
                            , mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_END_IDX], true);
                }
            }

            mIntHRAverageSubSeg =  mIntHRAccuUsed / mIntHRAccuCntUsed;
            mDoubleHRPeriodMsec = ((double) mIntHRAccuUsed * SystemConfig.mDoubleSubSegTimesliceMsec) / (double) mIntHRAccuCntUsed;
            mDoubleHRAverageValue = (double) 60000 / mDoubleHRPeriodMsec;

            mDoubleHRVarianceUsed = Math.sqrt((mDoubleHRVarianceAccuUsed) / (double) mIntHRAccuCntUsed);
            mDoubleHRVarianceUsedRatio = mDoubleHRVarianceUsed / iHRAverage;
            mDoubleHRVarianceAll = Math.sqrt(mDoubleHRVarianceAccuAll / (double) mIntHRAccuCntAll);
            mDoubleHRVarianceAllRatio = mDoubleHRVarianceAll / (double) iHRAverage;

            mDoubleHRAreaAverageAll = (double) mIntHRAreaAccuAll / (double) mIntHRAccuCntAll;
            //mDoublePhantomPeakCntAverageAll = (double) mIntPhantomPeakCntAccuAll / (double) mIntHRAccuCntAll;

            mDoubleHRAreaVariAccuAll = 0;
            for (iVar = 0; iVar < mIntHRResultNextIdx; iVar++) {
                doubleDiff = (double) mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_AREA_IDX] - mDoubleHRAreaAverageAll;
                mDoubleHRAreaVariAccuAll = mDoubleHRAreaVariAccuAll + doubleDiff * doubleDiff;
            }
            mDoubleHRAreaVariAverageAll = Math.sqrt(mDoubleHRAreaVariAccuAll / (double) mIntHRAccuCntUsed);
            mDoubleHRAreaVariAverageAllRatio = mDoubleHRAreaVariAverageAll / mDoubleHRAreaAverageAll;
            mDoublePhantomPeakCntAverageUsed = (double) mIntPhantomPeakCntAccuUsed / (double) mIntHRAccuCntUsed;

            return true;
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("processResultHR.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            mDoubleHRAverageValue = -1;
            mDoubleVpkMeterAverage = -1;
            mDoubleVtiCmAverage = -1;
            mDoubleStrokeVolumeAverage = -1;
            mDoubleCOAverage = -1;
            mDoubleHRVarianceAll = Double.MAX_VALUE;
            mDoubleHRVarianceUsed = Double.MAX_VALUE;
            return false;
        }
    }


	public int iCountHRBloodSelected(){
		int iCount = 0;
		for (int iVar = 0; iVar < mIntHRResultNextIdx; iVar++) {
            if (mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_DISCARDED_IDX] == SystemConfig.INT_HR_RESULT_DISCARDED_STATE_NO) {
                iCount++;
            }
        }
		return iCount;
	}
	
    public boolean processResultHRByCalculate() {
        int iVar, iHRIntervalAccu, iHRIntervalAccuCnt;

        try {
            iHRIntervalAccu = 0;
            iHRIntervalAccuCnt = 0;
            for (iVar = 0; iVar < mIntHRResultNextIdx; iVar++) {
                if (mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_DISCARDED_IDX] == SystemConfig.INT_HR_RESULT_DISCARDED_STATE_NO) {
                    iHRIntervalAccu = iHRIntervalAccu + mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_INTERVAL_IDX];
                    iHRIntervalAccuCnt++;
                }
            }
            if (iHRIntervalAccuCnt >= 1) {
                mDoubleHRPeriodMsec = ((double) iHRIntervalAccu * SystemConfig.mDoubleSubSegTimesliceMsec) / (double) iHRIntervalAccuCnt;
                mDoubleHRAverageValue = (double) 60000 / mDoubleHRPeriodMsec;
                return true;
            } else {
                mDoubleHRAverageValue = -1;
                return false;
            }
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("processResultHRByCalculate.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            mDoubleHRAverageValue = -1;
            mDoubleVpkMeterAverage = -1;
            mDoubleVtiCmAverage = -1;
            mDoubleStrokeVolumeAverage = -1;
            mDoubleCOAverage = -1;
            mDoubleHRVarianceAll = Double.MAX_VALUE;
            mDoubleHRVarianceUsed = Double.MAX_VALUE;
            return false;
        }
    }

    public boolean processResultVTIandVpk() {
        int iVar, iVar2, iStart, iEnd, iFreqIdx, iAccuCnt;
        int iVTIAccuCur, iVTIAccuAll, iVTIAverage, iVTIVarianceAccu, iVTIVarianceAccuCnt, iVTIDiff;
        double doubleValueAccu, doubleCurFreqAccu, doubleValueCur;
        double doubleCurVTICm, doubleVpkAvg;

        try {
            //------------------------
            // Calculate Vpk
            //------------------------
            doubleValueAccu = 0.0;
            iAccuCnt = 0;
            for (iVar = 0; iVar < mIntHRResultNextIdx; iVar++) {
                if (mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_DISCARDED_IDX] == SystemConfig.INT_HR_RESULT_DISCARDED_STATE_NO) {
//                    doubleValueCur = MainActivity.mBVSignalProcessorPart1.mDoubleBVFreqValues[MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx[mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VPK_IDX]]];
                    doubleValueCur = MainActivity.mBVSignalProcessorPart1.mDoubleVPKTheoreticalTable[
                            MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx[mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VPK_IDX]]];
                    doubleValueAccu = doubleValueAccu + doubleValueCur;
                    iAccuCnt++;
                }
            }
            mDoubleVpkMeterAverageOri = doubleValueAccu / (double) iAccuCnt;
            mDoubleVpkMeterAverageAfterUserAngle = mDoubleVpkMeterAverageOri / UserManagerCommon.mDoubleCosineUserAngle;
            if (mDoubleVpkMeterAverageAfterUserAngle < 0)
                mDoubleVpkMeterAverageAfterUserAngle = 0;
            else if(mDoubleVpkMeterAverageAfterUserAngle > 255)
                mDoubleVpkMeterAverageAfterUserAngle = 255;
            mDoubleVpkMeterAverageAfterAngleAfterCali =  MainActivity.mBVSignalProcessorPart1.mDoubleVPKExperimentalTable[(int) (mDoubleVpkMeterAverageAfterUserAngle * 100.0)];
            mDoubleVpkMeterAverage = mDoubleVpkMeterAverageAfterAngleAfterCali;

            //------------------------
            // Calculate VTI
            //------------------------
            iAccuCnt = 0;
            doubleValueAccu = 0;
            iVTIAccuAll = 0;
            mIntVTIValues = new int[mIntHRResultNextIdx];
            for (iVar = 0; iVar < mIntHRResultNextIdx; iVar++) {
                if (mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_DISCARDED_IDX] == SystemConfig.INT_HR_RESULT_DISCARDED_STATE_NO) {
                    iStart = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_START_IDX];
                    iEnd = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_END_IDX];

                    doubleCurFreqAccu = 0.0;
                    iVTIAccuCur = 0;
                    for (iVar2 = iStart; iVar2 <= iEnd; iVar2++) {
                        iFreqIdx = mIntArrayVTIMaxIdx[iVar2];
                        if (iFreqIdx == -1) {
                            iFreqIdx = 0;
                        }
                        iVTIAccuCur = iVTIAccuCur + iFreqIdx;
                        doubleCurFreqAccu = doubleCurFreqAccu + MainActivity.mBVSignalProcessorPart1.fVPKCali(iFreqIdx);
                    }
                    doubleCurVTICm = doubleCurFreqAccu * 100.0; // 1 M = 100 cm
                    doubleCurVTICm = (doubleCurVTICm * SystemConfig.mDoubleSubSegTimesliceMsec) / 1000.0;

                    mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_VALUE_IDX] = iVTIAccuCur;

                    doubleValueAccu = doubleValueAccu + doubleCurVTICm;
                    iVTIAccuAll = iVTIAccuAll + iVTIAccuCur;
                    iAccuCnt++;
                }
            }

            mDoubleVtiCmAverageOri = doubleValueAccu / (double) iAccuCnt;
            mDoubleVtiCmAverageAfterUserAngle = mDoubleVtiCmAverageOri / UserManagerCommon.mDoubleCosineUserAngle;
            mDoubleVtiCmAverageAfterAngleAfterCali = mDoubleVtiCmAverageAfterUserAngle;
            mDoubleVtiCmAverage = mDoubleVtiCmAverageAfterAngleAfterCali;

            iVTIAverage = iVTIAccuAll / iAccuCnt;

            iVTIVarianceAccu = 0;
            iVTIVarianceAccuCnt = 0;
            for (iVar = 0; iVar < mIntHRResultNextIdx; iVar++) {
                //if (mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VPK_USED_IDX] == SystemConfig.INT_HR_RESULT_USED_STATE_MAY_USE) {
                iVTIDiff = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_VALUE_IDX] - iVTIAverage;
                iVTIVarianceAccu = iVTIVarianceAccu + (iVTIDiff * iVTIDiff);
                iVTIVarianceAccuCnt++;
                //}
            }
            mDoubleVTIVarianceAll = Math.sqrt((double) iVTIVarianceAccu / (double) iVTIVarianceAccuCnt);
            mDoubleVTIVarianceAllRatio = mDoubleVTIVarianceAll/(double) iVTIAverage;

            iVTIVarianceAccu = 0;
            iVTIVarianceAccuCnt = 0;
            for (iVar = 0; iVar < mIntHRResultNextIdx; iVar++) {
                if (mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_DISCARDED_IDX] == SystemConfig.INT_HR_RESULT_DISCARDED_STATE_NO) {
                    iVTIDiff = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_VALUE_IDX] - iVTIAverage;
                    iVTIVarianceAccu = iVTIVarianceAccu + (iVTIDiff * iVTIDiff);
                    iVTIVarianceAccuCnt++;
                }
            }
            mDoubleVTIVarianceUsed = Math.sqrt((double) iVTIVarianceAccu / (double) iVTIVarianceAccuCnt);
            mDoubleVTIVarianceUsedRatio = mDoubleVTIVarianceUsed / (double) iVTIAverage;

            return true;
        }catch(Exception ex1){
            ex1.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugStr("processResultVTIandVpk.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            mDoubleHRAverageValue = -1;
            mDoubleVpkMeterAverage = -1;
            mDoubleVtiCmAverage = -1;
            mDoubleStrokeVolumeAverage = -1;
            mDoubleCOAverage = -1;
            mDoubleHRVarianceAll = Double.MAX_VALUE;
            mDoubleHRVarianceUsed = Double.MAX_VALUE;
            return false;
        }
    }


    public void processOneVpkAndVTIStartEndByPeakestMode(int iHRResultNextIdx){
        //--- for Peakest Point as Vpk ---------------------
        int iVar2, iVar3, iStart, iEnd, iVpkMax, iVTIStartTemp, iVTIStartTempCnt;
        int iStartHR, iPeriod, iVpk, iVpkValue;
        boolean boolVTIStart, boolVTIEnd;

        mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_IS_PHANTOM_IDX] = 0;
        iStart = mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_START_IDX];
        iEnd = mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_END_IDX];
        iVpk = iStart +1;
        iVpkValue = MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx[iStart+1];
        for(iVar2 = iStart+2; iVar2 < iEnd ; iVar2++) {
            if (MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx[iVar2] > iVpkValue) {
                iVpk = iVar2;
                iVpkValue = MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx[iVar2];
            }
        }
        mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VPK_IDX] = iVpk;
        mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VPK_VALUE_IDX] = MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx[iVpk];

        boolVTIStart = false;
        iVTIStartTemp = iStart;
        iVTIStartTempCnt = 0;
        iStart = mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_START_IDX] + (mVTIStartFeature.mIntIntegralWindowSize/2);
        iEnd = mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VPK_IDX];
        for(iVar2 = iStart ; iVar2 < iEnd ; iVar2++) {
            if (checkPeakBottomStateForVTIStartS1(iVar2)) {
                iVTIStartTemp = iVar2;
                mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VTI_START_IDX] = iVTIStartTemp;
                boolVTIStart = true;
                iVTIStartTempCnt++;
                //if (iVTIStartTempCnt == 3) {
                //    break;
                //}
            }
        }

        boolVTIEnd = false;
        iStart = mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VPK_IDX];
        iEnd = mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_END_IDX];
        iStartHR = mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_START_IDX];
        iPeriod = iEnd - iStartHR;
        for(iVar2 = iStart ; iVar2 < iEnd ; iVar2++) {
            if (checkPeakBottomStateForVTIEndS1(iVar2)) {
                mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VTI_END_IDX] = iVar2;
                boolVTIEnd = true;
                //if(((double)(iVar2 - iStartHR) / (double)iPeriod) >= 0.5) {
                    break;
                //}
            }
        }

        if(!boolVTIStart) {
            mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VTI_START_IDX] = mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_START_IDX];
        }
        if(!boolVTIEnd) {
              mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VTI_END_IDX] = mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_END_IDX];
        }
        setPeakBottomStatesForVpkPeakS2(mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VPK_IDX]);
        setPeakBottomStatesForVTIStartS2(mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_START_IDX]);
        setPeakBottomStatesForVTIEndS2(mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VTI_END_IDX]);
    }

//* jaufa, 190806, + For One Group
public void processOneVpkAndVTIStartEndByOneMode(int iHRResultNextIdx){
    //--- for Strongest Wave as Vpk ---------------------
    int iVar2, iVar3, iStart, iEnd, iVpkMax, iVTIStartTemp, iVTIStartTempCnt;
    int iStartHR, iPeriod;
    boolean boolVTIStart, boolVTIEnd;
    int _iVpkMax=Integer.MIN_VALUE;

    mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_IS_PHANTOM_IDX] = 0;
    iStart = MainActivity.mBVSignalProcessorPart1.mIntArrayVTIStartPosition[iHRResultNextIdx];
    iEnd = MainActivity.mBVSignalProcessorPart1.mIntArrayVTIEndPosition[iHRResultNextIdx];
    mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VTI_START_IDX] = iStart;
    mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VTI_END_IDX] = iEnd;
    for(iVar2 = iStart; iVar2 < iEnd ; iVar2++) {
        if (_iVpkMax<MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx[iVar2]) {
            _iVpkMax = MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx[iVar2];
            mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VPK_IDX] = iVar2;
            mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VPK_VALUE_IDX] = MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx[iVar2];
        }
    }

    setPeakBottomStatesForVpkPeakS2(mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VPK_IDX]);
    setPeakBottomStatesForVTIStartS2(mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_START_IDX]);
    setPeakBottomStatesForVTIEndS2(mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VTI_END_IDX]);
}




    public void processOneVpkAndVTIStartEndByStrongestMode(int iHRResultNextIdx){
        //--- for Strongest Wave as Vpk ---------------------
        int iVar2, iVar3, iStart, iEnd, iVpkMax, iVTIStartTemp, iVTIStartTempCnt;
        int iStartHR, iPeriod;
        boolean boolVTIStart, boolVTIEnd;

        mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_IS_PHANTOM_IDX] = 0;
        iStart = mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_START_IDX];
        iEnd = mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_END_IDX];
        for(iVar2 = iStart; iVar2 < iEnd ; iVar2++) {
            if (checkPeakBottomStateForVpkPeakS1(iVar2)) {
                mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VPK_IDX] = iVar2;
                mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VPK_VALUE_IDX] = MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx[iVar2];
                break;
            }
        }

        boolVTIStart = false;
        iVTIStartTemp = iStart;
        iVTIStartTempCnt = 0;
        iStart = mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_START_IDX] + (mVTIStartFeature.mIntIntegralWindowSize/2);
        iEnd = mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VPK_IDX];
        for(iVar2 = iStart ; iVar2 < iEnd ; iVar2++) {
            if (checkPeakBottomStateForVTIStartS1(iVar2)) {
                iVTIStartTemp = iVar2;
                mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VTI_START_IDX] = iVTIStartTemp;
                boolVTIStart = true;
                iVTIStartTempCnt++;
                if (iVTIStartTempCnt == 3) {
                    break;
                }
            }
        }

        boolVTIEnd = false;
        iStart = mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VPK_IDX];
        iEnd = mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_END_IDX];
        iStartHR = mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_START_IDX];
        iPeriod = iEnd - iStartHR;
        for(iVar2 = iStart ; iVar2 < iEnd ; iVar2++) {
            if (checkPeakBottomStateForVTIEndS1(iVar2)) {
                mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VTI_END_IDX] = iVar2;
                boolVTIEnd = true;
                //if(((double)(iVar2 - iStartHR) / (double)iPeriod) >= 0.5) {
                    break;
                //}
            }
        }

        if(!boolVTIStart) {
            mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VTI_START_IDX] = mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_START_IDX];
        }
        if(!boolVTIEnd) {
            mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VTI_END_IDX] = mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_END_IDX];
        }
        setPeakBottomStatesForVpkPeakS2(mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VPK_IDX]);
        setPeakBottomStatesForVTIStartS2(mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_START_IDX]);
        setPeakBottomStatesForVTIEndS2(mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VTI_END_IDX]);
    }



    public void processOneVpkAndVTIStartEndByFullSelectMode(int iHRResultNextIdx){
        int iVar2, iVpkIdx, iVpkValue;

        mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VTI_START_IDX] = mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_START_IDX];
        mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VTI_END_IDX] = mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_END_IDX];
        setPeakBottomStatesForVTIStartS2(mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VTI_START_IDX]);
        setPeakBottomStatesForVTIEndS2(mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VTI_END_IDX]);
        iVpkIdx = mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_START_IDX];
        iVpkValue = MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx[iVpkIdx];
        for(iVar2 = mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_START_IDX]+1 ; iVar2 <=  mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_HR_END_IDX]; iVar2++){
            if(MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx[iVar2] > iVpkValue){
                iVpkValue = MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx[iVar2];
                iVpkIdx = iVar2;
            }
        }
        mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VPK_IDX] = iVpkIdx;
        setPeakBottomStatesForVpkPeakS2(mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VPK_IDX]);
        mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_VPK_VALUE_IDX] = iVpkValue;
        mIntArrayHRResult[iHRResultNextIdx][SystemConfig.INT_HR_RESULT_IS_PHANTOM_IDX] = 1;

    }


    public void processResultVpkAndVTIStartEnd(){
        int iVar;
        int iPhantomYNCntAccu;

        try{
            iPhantomYNCntAccu = 0;
            for (iVar = 0; iVar < mIntHRResultNextIdx; iVar++) {
                if(false && (mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_PHANTOM_PEAK_CNT_IDX] <= 1)){   // Not Human Body Signal
                    processOneVpkAndVTIStartEndByFullSelectMode(iVar);
                    iPhantomYNCntAccu ++;
                }else if((SystemConfig.mIntVTIModeIdx == SystemConfig.INT_VTI_MODE_0_STRONGEST)
                        || (SystemConfig.mIntVTIModeIdx == SystemConfig.INT_VTI_MODE_2_TWO_POINT)){
                    if(SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_0_SNSI_GM){
                        processOneVpkAndVTIStartEndByStrongestMode(iVar);
                    }else{
                        processOneVpkAndVTIStartEndByStrongestMode(iVar);
                    }
                }else {    //if(SystemConfig.mIntVTIModeIdx == SystemConfig.INT_VTI_MODE_1_FULL){
                    processOneVpkAndVTIStartEndByFullSelectMode(iVar);
                }
            }

            mDoublePhantomYNCntRatio = (double) iPhantomYNCntAccu / (double) (mIntHRResultNextIdx);

            for (iVar = 0; iVar < mIntHRResultNextIdx; iVar++) {
                 //mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_DISCARDED_IDX] = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_DISCARDED_IDX];
                if(mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_DISCARDED_IDX] == SystemConfig.INT_HR_RESULT_DISCARDED_STATE_NO) {
                    setVTIDiscarded(mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_START_IDX]
                            , mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_END_IDX], false);
                }else{
                    setVTIDiscarded(mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_START_IDX]
                            , mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_END_IDX], true);
                }
            }

            smoothVTIMax_2();

        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("processResultVpkAndVTIStartEnd.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }


    public void processResultStrokeVolume(){
        int iVar, iVar2, iStart, iEnd;
        double doubleRadius, doubleCSArea, doubleEndSV, doubleStartSV, doubleSlope;

        try {
            //--------------------------------------------------------
            // Calculate Stroke Volume
            //--------------------------------------------------------
            doubleRadius = UserManagerCommon.mDoubleUserPulmDiameterCm / 2.0;
            doubleCSArea = doubleRadius * doubleRadius * Math.PI;
            mDoubleStrokeVolumeAverageOri = mDoubleVtiCmAverageOri * doubleCSArea;
            mDoubleStrokeVolumeAverageAfterUserAngle = mDoubleVtiCmAverageAfterUserAngle * doubleCSArea;
            mDoubleStrokeVolumeAverageAfterAngleAfterCali = mDoubleStrokeVolumeAverageAfterUserAngle * mDoubleVpkMeterAverageAfterAngleAfterCali / mDoubleVpkMeterAverageAfterUserAngle;
            mDoubleStrokeVolumeAverage = mDoubleStrokeVolumeAverageAfterAngleAfterCali;

        }catch(Exception ex1) {
            ex1.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugStr("processResultStrokeVolume.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }


    public void processResultCardiacOutput(){
        int iVar, iVar2, iStart, iEnd;
        double doubleRadius, doubleCSArea, doubleEndSV, doubleStartSV, doubleSlope;

        try {
            //--------------------------------------------------------
            // Calculate Cardiac Output
            //--------------------------------------------------------
            mDoubleCOAverageOri = (mDoubleStrokeVolumeAverageOri * mDoubleHRAverageValue) /1000.0;
            mDoubleCOAverageAfterUserAngle = (mDoubleStrokeVolumeAverageAfterUserAngle * mDoubleHRAverageValue) /1000.0;
            mDoubleCOAverageAfterAngleAfterCali = mDoubleCOAverageAfterUserAngle * mDoubleVpkMeterAverageAfterAngleAfterCali / mDoubleVpkMeterAverageAfterUserAngle;
            mDoubleCOAverage = mDoubleCOAverageAfterAngleAfterCali;

        }catch(Exception ex1) {
            ex1.printStackTrace();
           // SystemConfig.mMyEventLogger.appendDebugStr("processResultCardiacOutput.Exception","");
           // SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }


    private void smoothVTIMax_2(){
        int iLoop, iVar , iVar2 , iVar3, iVar4, iStart, iEnd, iLen, iIdx, iSmoothEnd, iVpkValue;
        int iPreFreqMaxIdx, iDebugCnt=0;

        try {
             for (iVar = 0; iVar < mIntHRResultNextIdx; iVar++) {
                 //---------------------------------------------------------
                 // Smoothing VTIMax Envelope
                 //---------------------------------------------------------

                 //--- process from Vpk to VTI Start ---------------------
                 iStart = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VPK_IDX];
                 iEnd = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_START_IDX];
                 iLen = iStart - iEnd + 1;

                 iVpkValue = MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdxByMovingAverage[iStart];
                 if(iVpkValue < MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx[iStart]){
                     iVpkValue = MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx[iStart];
                 }
                 mIntArrayVTIMaxIdx[iStart] = iVpkValue;

                 iPreFreqMaxIdx = mIntArrayVTIMaxIdx[iStart];
                 for (iVar4 = 0; iVar4 <= iLen; iVar4++) {
                     iVar2 = iStart - iVar4;
                     if (mIntArrayVTIMaxIdx[iVar2] > mIntArrayVTIMaxIdx[iStart]) {
                         mIntArrayVTIMaxIdx[iVar2] = mIntArrayVTIMaxIdx[iStart];
                     } else {
                         mIntArrayVTIMaxIdx[iVar2] = MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdxByMovingAverage[iVar2];
                     }
                     if (mIntArrayVTIMaxIdx[iVar2] > iPreFreqMaxIdx) {
                         iSmoothEnd = iVar2 + SystemConfig.mIntVtiMaxIdxSmoothLeftSize;  // smooth  points
                         if (iSmoothEnd > iStart) {
                             iSmoothEnd = iStart;
                         }
                         for (iVar3 = iVar2 + 1; iVar3 < iSmoothEnd; iVar3++) {
                             if (mIntArrayVTIMaxIdx[iVar3] < (mIntArrayVTIMaxIdx[iVar2] *4 /5)) {
                                 //if (mIntArrayVTIMaxIdx[iVar3] < (mIntArrayVTIMaxIdx[iVar2])) {
                                 mIntArrayVTIMaxIdx[iVar3] = mIntArrayVTIMaxIdx[iVar2];
                             } else {
                                 break;
                             }
                         }
                     }
                     iPreFreqMaxIdx = mIntArrayVTIMaxIdx[iVar2];
                 }

                 //--- process from Vpk to VTI End---------------------

                 iStart = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VPK_IDX];
                 iEnd = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_END_IDX];

                 //mIntArrayVTIMaxIdx[iStart] = SystemConfig.mBVSignalProcessorPart1.mIntArrayMaxIdxByMovingAverage[iStart];
                 //iMaxIdxMin = SystemConfig.mBVSignalProcessorPart1.mIntArrayMaxIdx[iEnd];
                 iPreFreqMaxIdx = mIntArrayVTIMaxIdx[iStart];
                 for (iVar2 = iStart; iVar2 <= iEnd; iVar2++) {
                     if (mIntArrayVTIMaxIdx[iVar2] > mIntArrayVTIMaxIdx[iStart]) {
                         mIntArrayVTIMaxIdx[iVar2] = mIntArrayVTIMaxIdx[iStart];
                     } else {
                         mIntArrayVTIMaxIdx[iVar2] = MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdxByMovingAverage[iVar2];
                     }
                     if (mIntArrayVTIMaxIdx[iVar2] > iPreFreqMaxIdx) {
                         iSmoothEnd = iVar2 - SystemConfig.mIntVtiMaxIdxSmoothRightSize;  // smooth  points
                         if (iSmoothEnd <= iStart) {
                             iSmoothEnd = iStart - 1;
                         }
                         iVar3 = iVar2 - 1;
                         while (iVar3 >= iSmoothEnd) {
                             //if (mIntArrayVTIMaxIdx[iVar3] < mIntArrayVTIMaxIdx[iVar2]) {
                             if (mIntArrayVTIMaxIdx[iVar3] < (mIntArrayVTIMaxIdx[iVar2] *4/5)) {
                                 mIntArrayVTIMaxIdx[iVar3] = mIntArrayVTIMaxIdx[iVar2];
                                 iVar3--;
                             } else {
                                 break;
                             }
                         }
                     }
                     iPreFreqMaxIdx = mIntArrayVTIMaxIdx[iVar2];
                 }
            }
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("smoothVTIMax_2.Excpetion","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }


    public double getHRAverage(){return mDoubleHRAverageValue;}

    public double getPeakVelocityAverage(){
        return mDoubleVpkMeterAverage;
    }

    public double getVTIAverage(){
        return mDoubleVtiCmAverage;
    }

    public void setCandiPeakAndVpkState(int iSubSegIdx, int iStatePosIdx, int iStateVal ){
        // iStateVal = 0 or 1
        int iOperVal;

        if(iStateVal == 0){   // set =0
            iOperVal = 0xFF ^ (0x01 << iStatePosIdx);
            mIntCandiPeakAndVpkState[iSubSegIdx] = mIntCandiPeakAndVpkState[iSubSegIdx] & iOperVal;
        }else{     // set = 1
            iOperVal = 0x01 << iStatePosIdx;
            mIntCandiPeakAndVpkState[iSubSegIdx] = mIntCandiPeakAndVpkState[iSubSegIdx] | iOperVal;
        }
    }


    public int getHRPeriodVpkValue(int iSubSeg){
        int iVar, iStart, iEnd, iVpk;

        for(iVar = 0 ; iVar < mIntHRResultNextIdx ; iVar++){
            iStart = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_START_IDX];
            iEnd = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_END_IDX];
            iVpk = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VPK_VALUE_IDX];
            if((iSubSeg >= iStart) && (iSubSeg <= iEnd)){
                return iVpk;
            }
        }
        return 0;
    }

    public void setVTIDiscarded (int iStart, int iEnd, boolean boolSelected){
        int iVar;

        for(iVar = iStart ; iVar <= iEnd ; iVar++ ){
            setPeakBottomStatesForVTIDiscarded(iVar , boolSelected);
        }
    }

    public void setVTIAllDiscarded (boolean boolSelected){
        int iVar, iStart, iEnd;
        for(iVar = 0 ; iVar < mIntHRResultNextIdx ; iVar ++){
            iStart = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_START_IDX];
            iEnd = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_END_IDX];
            setVTIDiscarded(iStart, iEnd, boolSelected);
        }
    }

    public void setHRPeriodDiscarded (int iStart, int iEnd, boolean boolSelected){
        int iVar;

        for(iVar = iStart ; iVar <= iEnd ; iVar++ ){
            setPeakBottomStatesForHRPeriodDiscarded(iVar , boolSelected);
        }
    }

    public void setHRPeriodAllDiscarded (boolean boolSelected){
        int iVar, iStart, iEnd;
        for(iVar = 0 ; iVar < mIntHRResultNextIdx ; iVar ++){
            iStart = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_START_IDX];
            iEnd = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_END_IDX];
            setHRPeriodDiscarded(iStart, iEnd, boolSelected);
        }
    }

    public void setHRPeriod (int iStart, int iEnd){
        int iVar;

        for(iVar = iStart ; iVar <= iEnd ; iVar++ ){
            setPeakBottomStatesForHRPeriod(iVar);
        }
    }

    public void toggleHRPeriodDiscardedState(int iSubSegIdx){
        int iVar, iVar2;

        for(iVar = 0 ; iVar < mIntHRResultNextIdx ; iVar++){
            if((mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_START_IDX] <= iSubSegIdx)
                        && (iSubSegIdx <= mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_END_IDX])){
                if(mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_DISCARDED_IDX] == SystemConfig.INT_HR_RESULT_DISCARDED_STATE_NO){
                    mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_DISCARDED_IDX] = SystemConfig.INT_HR_RESULT_DISCARDED_STATE_YES;
                    setHRPeriodDiscarded(mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_START_IDX], mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_END_IDX], true);
                    mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_DISCARDED_IDX] = SystemConfig.INT_HR_RESULT_DISCARDED_STATE_YES;
                    setVTIDiscarded(mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_START_IDX], mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_END_IDX], true);
                    SystemConfig.mDopplerInfo.segList.get(iVar).isDiscarded = true;
                }else{
                    mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_DISCARDED_IDX] = SystemConfig.INT_HR_RESULT_DISCARDED_STATE_NO;
                    setHRPeriodDiscarded(mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_START_IDX], mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_END_IDX], false);
                    mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_DISCARDED_IDX] = SystemConfig.INT_HR_RESULT_DISCARDED_STATE_NO;
                    setVTIDiscarded(mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_START_IDX], mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_END_IDX], false);
                    SystemConfig.mDopplerInfo.segList.get(iVar).isDiscarded = false;
                }
                return;
            }
        }
    }

    public boolean checkPeakBottomStateForHRPeak(int iSubSegIdx){
        // iStateVal = 0 or 1
        int iVal;

        iVal = mIntArrayHRPeakBottomStates[iSubSegIdx] & 0x01;
        return iVal != 0;
    }

    public void setPeakBottomStatesForHRPeak(int iSubSegIdx){

        mIntArrayHRPeakBottomStates[iSubSegIdx] = mIntArrayHRPeakBottomStates[iSubSegIdx] | 0x01; //
    }

    public boolean checkPeakBottomStateForHRBottomS1(int iSubSegIdx){
        // iStateVal = 0 or 1
        int iVal;

        iVal = mIntArrayHRPeakBottomStates[iSubSegIdx] & 0x02;
        return iVal != 0;
    }

    public void setPeakBottomStatesForHRBottomS1(int iSubSegIdx){

        mIntArrayHRPeakBottomStates[iSubSegIdx] = mIntArrayHRPeakBottomStates[iSubSegIdx] | 0x02; //Bottom
    }

    public boolean checkPeakBottomStateForHRBottomS2(int iSubSegIdx){
        // iStateVal = 0 or 1
        int iVal;

        iVal = mIntArrayHRPeakBottomStates[iSubSegIdx] & 0x04;
        return iVal != 0;
    }

    public void setPeakBottomStatesForHRBottomS2(int iSubSegIdx){

        mIntArrayHRPeakBottomStates[iSubSegIdx] = mIntArrayHRPeakBottomStates[iSubSegIdx] | 0x04; //Bottom
    }

    public boolean checkPeakBottomStateForHRStart(int iSubSegIdx){
        // iStateVal = 0 or 1
        int iVal;

        iVal = mIntArrayHRPeakBottomStates[iSubSegIdx] & 0x08;
        return iVal != 0;
    }

    public void setPeakBottomStatesForHRStart(int iSubSegIdx){

        mIntArrayHRPeakBottomStates[iSubSegIdx] = mIntArrayHRPeakBottomStates[iSubSegIdx] | 0x08; //
    }

    public boolean checkPeakBottomStateForVpkPeakS1(int iSubSegIdx){
        // iStateVal = 0 or 1
        int iVal;

        iVal = mIntArrayHRPeakBottomStates[iSubSegIdx] & 0x10;
        return iVal != 0;
    }

    public void setPeakBottomStatesForVpkPeakS1(int iSubSegIdx){

        mIntArrayHRPeakBottomStates[iSubSegIdx] = mIntArrayHRPeakBottomStates[iSubSegIdx] | 0x0010; //
    }

    public void clearPeakBottomStatesForVpkPeakS1(int iSubSegIdx){

        mIntArrayHRPeakBottomStates[iSubSegIdx] = mIntArrayHRPeakBottomStates[iSubSegIdx] & 0xFFEF; //
    }

    public boolean checkPeakBottomStateForVpkPeakS2(int iSubSegIdx){
        // iStateVal = 0 or 1
        int iVal;

        iVal = mIntArrayHRPeakBottomStates[iSubSegIdx] & 0x20;
        return iVal != 0;
    }

    public void setPeakBottomStatesForVpkPeakS2(int iSubSegIdx){

        mIntArrayHRPeakBottomStates[iSubSegIdx] = mIntArrayHRPeakBottomStates[iSubSegIdx] | 0x0020; //
    }

    public void clearPeakBottomStatesForVpkPeakS2(int iSubSegIdx){

        mIntArrayHRPeakBottomStates[iSubSegIdx] = mIntArrayHRPeakBottomStates[iSubSegIdx] & 0xFFDF; //
    }

    public boolean checkPeakBottomStateForHRPeriod(int iSubSegIdx){
        // iStateVal = 0 or 1
        int iVal;

        iVal = mIntArrayHRPeakBottomStates[iSubSegIdx] & 0x40;
        return iVal != 0;
    }

    public void setPeakBottomStatesForHRPeriod(int iSubSegIdx){

        mIntArrayHRPeakBottomStates[iSubSegIdx] = mIntArrayHRPeakBottomStates[iSubSegIdx] | 0x40; //
    }

    public boolean checkPeakBottomStateForHRPeakHalf(int iSubSegIdx){
        // iStateVal = 0 or 1
        int iVal;

        iVal = mIntArrayHRPeakBottomStates[iSubSegIdx] & 0x80;
        return iVal != 0;
    }

    public boolean checkPeakBottomStateForPhantomPeak(int iSubSegIdx){
        // iStateVal = 0 or 1
        int iVal;

        iVal = mIntArrayHRPeakBottomStates[iSubSegIdx] & 0x100;
        return iVal != 0;
    }

    public void setPeakBottomStatesForPhantomPeak(int iSubSegIdx){

        mIntArrayHRPeakBottomStates[iSubSegIdx] = mIntArrayHRPeakBottomStates[iSubSegIdx] | 0x100; //
    }

    public boolean checkPeakBottomStateForVTIStartS1(int iSubSegIdx){
        // iStateVal = 0 or 1
        int iVal;

        iVal = mIntArrayHRPeakBottomStates[iSubSegIdx] & 0x200;
        return iVal != 0;
    }

    public void setPeakBottomStatesForVTIStartS1(int iSubSegIdx){

        mIntArrayHRPeakBottomStates[iSubSegIdx] = mIntArrayHRPeakBottomStates[iSubSegIdx] | 0x200; //
    }

    public boolean checkPeakBottomStateForVTIEndS1(int iSubSegIdx){
        // iStateVal = 0 or 1
        int iVal;

        iVal = mIntArrayHRPeakBottomStates[iSubSegIdx] & 0x400;
        return iVal != 0;
    }

    public void setPeakBottomStatesForVTIEndS1(int iSubSegIdx){

        mIntArrayHRPeakBottomStates[iSubSegIdx] = mIntArrayHRPeakBottomStates[iSubSegIdx] | 0x400; //
    }

    public boolean checkPeakBottomStateForVTIStartS2(int iSubSegIdx){
        // iStateVal = 0 or 1
        int iVal;

            iVal = mIntArrayHRPeakBottomStates[iSubSegIdx] & 0x800;
        return iVal != 0;
    }

    public void setPeakBottomStatesForVTIStartS2(int iSubSegIdx){

        mIntArrayHRPeakBottomStates[iSubSegIdx] = mIntArrayHRPeakBottomStates[iSubSegIdx] | 0x0800; //
    }

    public void clearPeakBottomStatesForVTIStartS2(int iSubSegIdx){

        mIntArrayHRPeakBottomStates[iSubSegIdx] = mIntArrayHRPeakBottomStates[iSubSegIdx] & 0xF7FF; //
    }

    public boolean checkPeakBottomStateForVTIEndS2(int iSubSegIdx){
        // iStateVal = 0 or 1
        int iVal;

        iVal = mIntArrayHRPeakBottomStates[iSubSegIdx] & 0x1000;
        return iVal != 0;
    }

    public void setPeakBottomStatesForVTIEndS2(int iSubSegIdx){

        mIntArrayHRPeakBottomStates[iSubSegIdx] = mIntArrayHRPeakBottomStates[iSubSegIdx] | 0x1000; //
    }

    public void clearPeakBottomStatesForVTIEndS2(int iSubSegIdx){

        mIntArrayHRPeakBottomStates[iSubSegIdx] = mIntArrayHRPeakBottomStates[iSubSegIdx] & 0xEFFF; //
    }

    public boolean checkPeakBottomStateForVTIDiscarded(int iSubSegIdx){
        // iStateVal = 0 or 1
        int iVal;

        iVal = mIntArrayHRPeakBottomStates[iSubSegIdx] & 0x2000;
        return iVal != 0;
    }

    public void setPeakBottomStatesForVTIDiscarded(int iSubSegIdx, boolean boolDiscarded){
        if(boolDiscarded) {
            mIntArrayHRPeakBottomStates[iSubSegIdx] = mIntArrayHRPeakBottomStates[iSubSegIdx] | 0x2000; //
        }else{
            mIntArrayHRPeakBottomStates[iSubSegIdx] = mIntArrayHRPeakBottomStates[iSubSegIdx] & 0xDFFF; //
        }
    }

    public boolean checkPeakBottomStateForHRPeriodDiscarded(int iSubSegIdx){
        // iStateVal = 0 or 1
        int iVal;

        iVal = mIntArrayHRPeakBottomStates[iSubSegIdx] & 0x4000;
        return iVal != 0;
    }

    public void setPeakBottomStatesForHRPeriodDiscarded(int iSubSegIdx, boolean boolSelected){
        if(boolSelected) {
            mIntArrayHRPeakBottomStates[iSubSegIdx] = mIntArrayHRPeakBottomStates[iSubSegIdx] | 0x4000; //
        }else{
            mIntArrayHRPeakBottomStates[iSubSegIdx] = mIntArrayHRPeakBottomStates[iSubSegIdx] & 0xBFFF; //
        }
    }

    public boolean checkPeakBottomStateForHREnd(int iSubSegIdx){
        // iStateVal = 0 or 1
        int iVal;

        iVal = mIntArrayHRPeakBottomStates[iSubSegIdx] & 0x8000;
        return iVal != 0;
    }

    public void setPeakBottomStatesForHREnd(int iSubSegIdx){

        mIntArrayHRPeakBottomStates[iSubSegIdx] = mIntArrayHRPeakBottomStates[iSubSegIdx] | 0x8000; //
    }

    public void setAllResultDiscarded(){
        int iVar;

        for (iVar = 0; iVar < mIntHRResultNextIdx; iVar++) {
            mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_DISCARDED_IDX] = SystemConfig.INT_HR_RESULT_DISCARDED_STATE_YES;
            //setHRPeriodDiscarded(SystemConfig.mBVSignalProcessorPart2Selected.mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_START_IDX]
            //        , mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_END_IDX], true);
            //setVTIDiscarded(mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_START_IDX]
            //        , mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_END_IDX], true);
        }
        mDoubleHRAverageValue = -1;
        mDoubleVpkMeterAverage = -1;
        mDoubleVtiCmAverage = -1;
        mDoubleStrokeVolumeAverage = -1;
        mDoubleCOAverage = -1;
        setHRPeriodAllDiscarded(true);
        setVTIAllDiscarded(true);
    }

//jaufa, N1, '181011, *
    public void changeVTIScopeBy2PointsSel(int iCurSubSeg){
        int iVar, iVar2;
        int iCurHRStart, iCurHREnd, iCurVtiStart, iCurVtiEnd, iCurVpk, iCurVpkValue;

        for(iVar = 0 ; iVar < mIntHRResultNextIdx ; iVar++){
            if((iCurSubSeg >= mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_START_IDX])
                    && (iCurSubSeg <= mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_END_IDX])){
                if(mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_START_END_PRE_SEL_IDX] == 0){
                    if(Math.abs(iCurSubSeg - mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_START_IDX])
                            >= Math.abs(iCurSubSeg - mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_END_IDX])){
                        mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_START_END_PRE_SEL_IDX]
                                = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_START_IDX];
                    }else{
                        mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_START_END_PRE_SEL_IDX]
                                = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_END_IDX];
                    }
                }
                if(iCurSubSeg ==  mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_START_END_PRE_SEL_IDX]){
                    mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_START_IDX] = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_START_END_PRE_SEL_IDX];
                    mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_END_IDX] = iCurSubSeg +1;
                }else if(iCurSubSeg > mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_START_END_PRE_SEL_IDX]){
                    mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_START_IDX] = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_START_END_PRE_SEL_IDX];
                    mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_END_IDX] = iCurSubSeg;
                }else{
                    mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_START_IDX] = iCurSubSeg;
                    mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_END_IDX] = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_START_END_PRE_SEL_IDX];
                }

                iCurHRStart = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_START_IDX];
                iCurHREnd = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_END_IDX];
                for(iVar2 = iCurHRStart ; iVar2 <= iCurHREnd ; iVar2++) {
                    mIntArrayVTIMaxIdx[iVar2] = 0;
                    if(checkPeakBottomStateForVTIStartS2(iVar2)){
                        clearPeakBottomStatesForVTIStartS2(iVar2);
                    }
                    if(checkPeakBottomStateForVTIEndS2(iVar2)){
                        clearPeakBottomStatesForVTIEndS2(iVar2);
                    }
                    if(checkPeakBottomStateForVpkPeakS2(iVar2)){
                        clearPeakBottomStatesForVpkPeakS2(iVar2);
                    }
                }
                iCurVtiStart = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_START_IDX];
                iCurVtiEnd = mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_END_IDX];
                for(iVar2 = iCurVtiStart ; iVar2 <= iCurVtiEnd ; iVar2++){
                    mIntArrayVTIMaxIdx[iVar2] = MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdxByMovingAverage[iVar2];
                }
                setPeakBottomStatesForVTIStartS2(iCurVtiStart);
                setPeakBottomStatesForVTIEndS2(iCurVtiEnd);

                iCurVpk = iCurVtiStart;
                iCurVpkValue = mIntArrayVTIMaxIdx[iCurVtiStart];
                for(iVar2 = iCurVtiStart+1 ; iVar2 <= iCurVtiEnd ; iVar2++){
                    if(iCurVpkValue < mIntArrayVTIMaxIdx[iVar2]){
                        iCurVpk = iVar2;
                        iCurVpkValue = mIntArrayVTIMaxIdx[iVar2];
                    }
                }
                mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VPK_IDX] = iCurVpk;
                mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VPK_VALUE_IDX] = iCurVpkValue;
                setPeakBottomStatesForVpkPeakS2(iCurVpk);

                mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_START_END_PRE_SEL_IDX] = iCurSubSeg;
                break;
            }
        }
    }

}
