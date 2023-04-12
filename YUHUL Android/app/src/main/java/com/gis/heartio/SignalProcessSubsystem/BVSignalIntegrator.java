package com.gis.heartio.SignalProcessSubsystem;

import android.util.Log;

import com.gis.heartio.GIS_Log;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.SystemConfig;
import com.gis.heartio.UIOperationControlSubsystem.MainActivity;

/**
 * Created by brandon on 2017/5/10.
 */

public abstract class BVSignalIntegrator {
    private static final String TAG = "BVSignalIntegrator";

    public static enum ENUM_INTEGRAL_TYPE {INTEGRAL_TYPE_MAXIDX, INTEGRAL_TYPE_FREQ_AMP_MUL};
    ENUM_INTEGRAL_TYPE mEnumIntgralType = ENUM_INTEGRAL_TYPE.INTEGRAL_TYPE_MAXIDX;

    public enum ENUM_INTEGRAL_FEATURE_TYPE {FEATURE_TYPE_HR_PEAK_HALF, FEATURE_TYPE_HR_PEAK, FEATURE_TYPE_VPK_S1_PEAK, FEATURE_TYPE_PHANTOM_PEAK, FEATURE_TYPE_HR_BOTTOM, FEATURE_TYPE_VTI_START, FEATURE_TYPE_VTI_END};
    ENUM_INTEGRAL_FEATURE_TYPE mEnumFeatureType;

    public BVSignalProcessorPart2 mBVSignalProcessorPart2;
    //BVSignalIntegralFeature mBVSignalIntegralSelector;

    public double[] mDoubleIntegralValues;
    public double[] mDoubleIntegralWindowDatas;

    int mIntIntegralWindowSize;
    int mIntIntegralNextIdx = 0;
    int mIntCurIntegralWindowSize = 0;
    int mIntIntegralWindowNextIdx = 0;
    double mDoubleIntegralWindowAccu = 0;


    public void setIntegratorType(ENUM_INTEGRAL_TYPE enumIntrgratorType){
        mEnumIntgralType = enumIntrgratorType;
    }


    public void setParamIntegralWindowMilliSec(int iMilliSec){

        if(SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_44K){
            mIntIntegralWindowSize = (int) (((double) SystemConfig.mIntUltrasoundSamplerate * iMilliSec)
                    / (double) SystemConfig.INT_BLOOD_VELOCITY_STFT_WINDOW_SHIFT_SIZE_256 / (double) 1000);
        }else if(SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.USCOM_44K){
            mIntIntegralWindowSize = (int) (((double) SystemConfig.mIntUltrasoundSamplerate * iMilliSec)
                    / (double) SystemConfig.INT_BLOOD_VELOCITY_STFT_WINDOW_SHIFT_SIZE_256 / (double) 1000);
        }else{
            mIntIntegralWindowSize = (int) (((double) SystemConfig.mIntUltrasoundSamplerate * iMilliSec)
                    / (double) SystemConfig.INT_BLOOD_VELOCITY_STFT_WINDOW_SHIFT_SIZE_64 / (double) 1000);
        }
    }


    public void prepareStartIntegral() {
        int iVar;

        try {
            for (iVar = 0; iVar < MainActivity.mBVSignalProcessorPart1.mIntTotalSubSegMaxSize; iVar++) {
                mDoubleIntegralValues[iVar] = 0;
            }

            mDoubleIntegralWindowDatas = new double[mIntIntegralWindowSize];

            mIntCurIntegralWindowSize = 0;
            mIntIntegralWindowNextIdx = 0;
            mDoubleIntegralWindowAccu = 0;
            //mIntIntegralNextIdx = 0;
            mIntIntegralNextIdx = SystemConfig.mIntEndIdxNoiseLearn;

        }catch(Exception ex1){
            GIS_Log.e(TAG, "prepareStartIntegral: " + ex1);
        }
    }

    public void prepareAfterLearned() {
        mIntIntegralNextIdx = SystemConfig.mIntEndIdxNoiseLearn;
    }


    public double calculateFreqAmpMul(int iSubSegIdx){
        int iVar;
        double doubleAmpSum = 0;

        if(MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx[mIntIntegralNextIdx] > 0){
            for(iVar = 1 ; iVar <= MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx[mIntIntegralNextIdx] ; iVar++) {
                //doubleAmpSum = doubleAmpSum + mBVSignalProcessorPart2.mDoubleBVSpectrumValues[mIntHRBottomIntegralNextIdx][iVar];
                doubleAmpSum = doubleAmpSum + MainActivity.mBVSignalProcessorPart1.mDoubleBVSpectrumValues[mIntIntegralNextIdx][iVar]
                        * MainActivity.mBVSignalProcessorPart1.mDoubleBVFreqValues[iVar];
            }
        }
        return doubleAmpSum;
    }

    public void processAllSegmentIntegral() {
        double doubleOldWindowValue;
        //-------------------------------------------------------------------------
        //  Peak Intgration
        //-------------------------------------------------------------------------
        try {

            while (mIntIntegralNextIdx < MainActivity.mBVSignalProcessorPart1.mIntMaxIdxNextIdx) {
                if (mIntCurIntegralWindowSize < mIntIntegralWindowSize) {
                    if (mEnumIntgralType == ENUM_INTEGRAL_TYPE.INTEGRAL_TYPE_MAXIDX) {
                        mDoubleIntegralWindowDatas[mIntIntegralWindowNextIdx] = (double) MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx[mIntIntegralNextIdx];
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
                        mDoubleIntegralWindowDatas[mIntIntegralWindowNextIdx] = (double) MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx[mIntIntegralNextIdx];
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
           // SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
     }
}
