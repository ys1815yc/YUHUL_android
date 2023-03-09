package com.gis.heartio.SignalProcessSubsystem;

import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.SystemConfig;
import com.gis.heartio.UIOperationControlSubsystem.MainActivity;

/**
 * Created by brandon on 2017/5/19.
 */

public class BVSignalFeatureBottomHR extends BVSignalIntegrator {

    private int mIntFeatureWindowSize;
    private int mIntCurSelIdxInWindow;
    private int mIntFeatureNextIdx;
    public int mIntFindPeakLast;
    public int mIntFindBottomLast;
    private int mIntCurFeatureWindowSize;

    public double mDoubleFeatureComparePrePeakRatio;

    public double mDoubleFeatureReStartWindowBeginGapRatioAfterHigh;
    public double mDoubleFeatureReStartWindowBeginGapRatioAfterLow;
    public int mIntFeatureReStartWindowBeginGapAfterHigh, mIntFeatureReStartWindowBeginGapAfterLow;

    public enum ENUM_FEATURE_STATE {STATE_START, STATE_FIND_HIGH, STATE_FIND_LOW}
    private ENUM_FEATURE_STATE mEnumFeatureState;

    //public enum ENUM_FEATURE_FOUND_STATE {FOUND_NONE, FOUND_HIGH, FOUND_LOW}
    //private ENUM_FEATURE_FOUND_STATE mEnumFeatureFoundState;

    public BVSignalFeatureBottomHR(BVSignalProcessorPart2 ProcessorPart2){
        mBVSignalProcessorPart2= ProcessorPart2;
        mDoubleIntegralValues = new double[SystemConfig.mIntSystemMaxSubSegSize];

        mEnumFeatureType = ENUM_INTEGRAL_FEATURE_TYPE.FEATURE_TYPE_HR_BOTTOM;  //brandon
        setIntegratorType(BVSignalIntegrator.ENUM_INTEGRAL_TYPE.INTEGRAL_TYPE_MAXIDX);
    }


    public void setFeatureReStartWindowGapRatioAfterHigh(double doubleRatio){
        mDoubleFeatureReStartWindowBeginGapRatioAfterHigh = doubleRatio;
    }

    public void setFeatureReStartWindowGapRatioAfterLow(double doubleRatio){
        mDoubleFeatureReStartWindowBeginGapRatioAfterLow = doubleRatio;
    }

    public void prepareStart() {
        int iVar , iVar2;
        boolean boolItriDevice;
        double doubleIntegralWindowMilliSec, doubleShiftSize;

        mIntFindPeakLast = 0;
        mIntFindBottomLast = 0;
        mIntCurFeatureWindowSize = 0;

        mIntFeatureNextIdx = SystemConfig.mIntEndIdxNoiseLearn;
        mIntCurSelIdxInWindow = SystemConfig.mIntEndIdxNoiseLearn;

        if((SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_BE_ONLINE) ||
                (SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_LE_ONLINE) ||
                (SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_OFFLINE)) {
            doubleShiftSize = (double) SystemConfig.INT_BLOOD_VELOCITY_STFT_WINDOW_SHIFT_SIZE_64;
            boolItriDevice = true;
        }else if(SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_44K){
            doubleShiftSize = (double) SystemConfig.INT_BLOOD_VELOCITY_STFT_WINDOW_SHIFT_SIZE_256;
            boolItriDevice = true;
        }else if(SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.USCOM_44K){
            doubleShiftSize = (double) SystemConfig.INT_BLOOD_VELOCITY_STFT_WINDOW_SHIFT_SIZE_256;
            boolItriDevice = false;
        }else{
            doubleShiftSize = (double) SystemConfig.INT_BLOOD_VELOCITY_STFT_WINDOW_SHIFT_SIZE_64;
            boolItriDevice = false;
        }

        mIntIntegralWindowSize = (int) (((double) SystemConfig.mIntUltrasoundSamplerate * mBVSignalProcessorPart2.mDoubleHRBottomIntegralWindowMsec)
                / doubleShiftSize / (double) 1000);
        mIntFeatureWindowSize = (int) (((double) SystemConfig.mIntUltrasoundSamplerate * mBVSignalProcessorPart2.mDoubleHRBottomFeatureWindowMsec)
                / doubleShiftSize / (double) 1000);

        mIntFeatureReStartWindowBeginGapAfterHigh = (int)((double)mIntFeatureWindowSize * mDoubleFeatureReStartWindowBeginGapRatioAfterHigh);
        mIntFeatureReStartWindowBeginGapAfterLow = (int)((double)mIntFeatureWindowSize * mDoubleFeatureReStartWindowBeginGapRatioAfterLow);

        mEnumFeatureState = ENUM_FEATURE_STATE.STATE_START;
        //mEnumFeatureFoundState = ENUM_FEATURE_FOUND_STATE.FOUND_NONE;

        //mDoublePhantomHighMax = Double.MIN_VALUE;

        prepareStartIntegral();
    }


    public void prepareWindowRestart(boolean boolAfterHigh) {

        if(boolAfterHigh) {
            mIntFeatureNextIdx = mIntFeatureNextIdx - mIntFeatureWindowSize + mIntFeatureReStartWindowBeginGapAfterHigh +1;
        }else{
            mIntFeatureNextIdx = mIntFeatureNextIdx - mIntFeatureWindowSize + mIntFeatureReStartWindowBeginGapAfterLow +1;
        }
        mIntCurFeatureWindowSize=0;
        mIntCurSelIdxInWindow = mIntFeatureNextIdx+1;

    }


    public void processAllSegmentFeature() {
        int iVar, iPeakFindStart, iPeakFindEnd, iPeakIdx;
        int iCurShortWindowLeftIdx;

        try {
            //-------------------------------------------------------------------------
            //  Find Blood Flow Peak Point
            //-------------------------------------------------------------------------
            while (mIntFeatureNextIdx < mIntIntegralNextIdx) {
                if(mEnumFeatureState == ENUM_FEATURE_STATE.STATE_START) {
                    if (findHigh()) {
                        mEnumFeatureState = ENUM_FEATURE_STATE.STATE_FIND_LOW;
                    }
                }else if(mEnumFeatureState == ENUM_FEATURE_STATE.STATE_FIND_HIGH) {
                    if (findHigh()) {
                        mEnumFeatureState = ENUM_FEATURE_STATE.STATE_FIND_LOW;
                    }
                }else {
                    if (findBottom()) {
                        mEnumFeatureState = ENUM_FEATURE_STATE.STATE_FIND_HIGH;
                    }
                }
                mIntFeatureNextIdx++;
            }
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("processSegmentFeaturePeak.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }

    private boolean findHigh(){
        int iCurShortWindowLeftIdx;
        double doubleCompare;
        boolean boolFound;

        boolFound = false;

        try{

            if (mIntCurFeatureWindowSize < mIntFeatureWindowSize) {
                mIntCurFeatureWindowSize++;
            }

            if (mDoubleIntegralValues[mIntFeatureNextIdx] > mDoubleIntegralValues[mIntCurSelIdxInWindow]) {
                mIntCurSelIdxInWindow = mIntFeatureNextIdx;
            }

            if (mIntCurFeatureWindowSize == mIntFeatureWindowSize) {
                iCurShortWindowLeftIdx = mIntFeatureNextIdx - mIntCurFeatureWindowSize + 1;
                if (mIntCurSelIdxInWindow == iCurShortWindowLeftIdx) {
                    if (mIntFindPeakLast == 0) {
                        //mBVSignalProcessorPart2.setPeakBottomStatesForPhantomPeak(mIntCurSelIdxInWindow);
                        mIntFindPeakLast = mIntCurSelIdxInWindow;
                    } else {
                        doubleCompare = (mDoubleIntegralValues[mIntFindBottomLast] + mDoubleIntegralValues[mIntFindPeakLast]) *0.5;
                        if (mDoubleIntegralValues[mIntCurSelIdxInWindow] >= doubleCompare) {
                            mIntFindPeakLast = mIntCurSelIdxInWindow;
                        }
                    }
                    prepareWindowRestart(true);
                    boolFound = true;
                }
            }
            return boolFound;
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("FeaturePhantom.findHigh.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            return boolFound;
        }
    }

    private boolean findBottom(){
        int iVar, iBottomFindStart, iBottomFindEnd, iBottomIdx;
        int iCurShortWindowLeftIdx;
        double doubleCompare;
        boolean boolFound;

        boolFound = false;

        try {

            if (mIntCurFeatureWindowSize < mIntFeatureWindowSize) {
                mIntCurFeatureWindowSize++;
            }
            if (mDoubleIntegralValues[mIntCurSelIdxInWindow] > mDoubleIntegralValues[mIntFeatureNextIdx]) {
                mIntCurSelIdxInWindow = mIntFeatureNextIdx;
            }

            if (mIntCurFeatureWindowSize == mIntFeatureWindowSize) {
                iCurShortWindowLeftIdx = mIntFeatureNextIdx - mIntCurFeatureWindowSize + 1;
                if (mIntCurSelIdxInWindow == iCurShortWindowLeftIdx) {
                    if (mIntFindBottomLast == 0) {
                        mIntFindBottomLast = mIntCurSelIdxInWindow;
                    }else {
                        //doubleCompare = (mDoubleIntegralValues[mIntFindBottomLast] + mDoubleIntegralValues[mIntFindPeakLast]) *0.5;
                        //if (mDoubleIntegralValues[mIntCurSelIdxInWindow] < doubleCompare) {
                            iBottomFindStart = mIntCurSelIdxInWindow - (mIntIntegralWindowSize * 6 / 10);
                            iBottomFindEnd = mIntCurSelIdxInWindow - (mIntIntegralWindowSize * 4 / 10);

                            iBottomIdx = iBottomFindStart;
                            for (iVar = iBottomFindStart; iVar <= iBottomFindEnd; iVar++) {
                                if (MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx[iBottomIdx] > MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx[iVar]) {
                                    iBottomIdx = iVar;
                                }
                            }
                            mBVSignalProcessorPart2.setPeakBottomStatesForHRBottomS1(iBottomIdx);
                            mIntFindBottomLast = mIntCurSelIdxInWindow;
                        //}
                    }
                    prepareWindowRestart(false);
                    boolFound = true;
                }
            }
            return boolFound;
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("FeaturePhantom.findBottom.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            return boolFound;
        }
    }
    //*jaufa, 180608, + Override
    @Override
    public void processAllSegmentIntegral() {
        double doubleOldWindowValue;
        //-------------------------------------------------------------------------
        //  Peak Intgration
        //-------------------------------------------------------------------------
        try {

            while (mIntIntegralNextIdx < MainActivity.mBVSignalProcessorPart1.mIntMaxIdxNextIdx) {
                if (mIntCurIntegralWindowSize < mIntIntegralWindowSize) {
                    if (mEnumIntgralType == ENUM_INTEGRAL_TYPE.INTEGRAL_TYPE_MAXIDX) {
                        mDoubleIntegralWindowDatas[mIntIntegralWindowNextIdx] = (double) MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx_Hr[mIntIntegralNextIdx];
                    } else {
                        mDoubleIntegralWindowDatas[mIntIntegralWindowNextIdx] = calculateFreqAmpMul(mIntIntegralNextIdx);
                    }

                    mDoubleIntegralWindowAccu = mDoubleIntegralWindowAccu + mDoubleIntegralWindowDatas[mIntIntegralWindowNextIdx];
                    mDoubleIntegralValues[mIntIntegralNextIdx] = mDoubleIntegralWindowAccu;

                    mIntCurIntegralWindowSize++;

                    mIntIntegralWindowNextIdx++;
                    if (mIntIntegralWindowNextIdx >= mIntIntegralWindowSize) {
                        mIntIntegralWindowNextIdx = 0;
                    }
                } else {
                    doubleOldWindowValue = mDoubleIntegralWindowDatas[mIntIntegralWindowNextIdx];
                    if (mEnumIntgralType == ENUM_INTEGRAL_TYPE.INTEGRAL_TYPE_MAXIDX) {
                        mDoubleIntegralWindowDatas[mIntIntegralWindowNextIdx] = (double) MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx_Hr[mIntIntegralNextIdx];
                    } else {
                        mDoubleIntegralWindowDatas[mIntIntegralWindowNextIdx] = calculateFreqAmpMul(mIntIntegralNextIdx);
                    }
                    mDoubleIntegralWindowAccu = mDoubleIntegralWindowAccu + mDoubleIntegralWindowDatas[mIntIntegralWindowNextIdx]
                            - doubleOldWindowValue;
                    mDoubleIntegralValues[mIntIntegralNextIdx] = mDoubleIntegralWindowAccu;
                    mIntIntegralWindowNextIdx++;
                    if (mIntIntegralWindowNextIdx >= mIntIntegralWindowSize) {
                        mIntIntegralWindowNextIdx = 0;
                    }
                }
                mIntIntegralNextIdx++;
            }
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("processAllSegmentIntrgral.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }
    public double calculateFreqAmpMul(int iSubSegIdx){
        int iVar;
        double doubleAmpSum = 0;

        if(MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx_Hr[mIntIntegralNextIdx] > 0){
            for(iVar = 1 ; iVar <= MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx_Hr[mIntIntegralNextIdx] ; iVar++) {
                //doubleAmpSum = doubleAmpSum + mBVSignalProcessorPart2.mDoubleBVSpectrumValues[mIntHRBottomIntegralNextIdx][iVar];
                doubleAmpSum = doubleAmpSum + MainActivity.mBVSignalProcessorPart1.mDoubleBVSpectrumValues[mIntIntegralNextIdx][iVar]
                        * MainActivity.mBVSignalProcessorPart1.mDoubleBVFreqValues[iVar];
            }
        }
        return doubleAmpSum;
    }

//*jaufa, 180608, + Override  */
}
