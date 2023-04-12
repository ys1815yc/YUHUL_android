package com.gis.heartio.SignalProcessSubsystem;

import android.util.Log;

import com.gis.heartio.GIS_Log;
import com.gis.heartio.UIOperationControlSubsystem.MainActivity;

/**
 * Created by brandon on 2017/5/19.
 */

public class BVSignalFeatureBottom extends BVSignalIntegrator {
    private static final String TAG = "BVSignalFeatureBottom";
    protected int mIntFeatureWindowSize;
    protected int mIntCurSelIdxInWindow;
    protected int mIntFeatureNextIdx;
    public int mIntFindFeatureLast;
    protected int mIntCurFeatureWindowSize;

    //public int mIntFeaturePreIdx;
    //public double mDoubleIntegralPrePeakValue;

    public double mDoubleFeatureComparePrePeakRatio;

    public double mDoubleFeatureReStartWindowBeginGapRatio;
    public int mIntFeatureReStartWindowBeginGap;

    protected double mDoubleCurPeakHighIntegralVal;


    public void setFeatureReStartWindowGapRatio(double doubleRatio){
        mDoubleFeatureReStartWindowBeginGapRatio = doubleRatio;
    }

    public void setFeatureComparePrePeakRatio(double doubleComparePrePeakRatio){

        mDoubleFeatureComparePrePeakRatio = doubleComparePrePeakRatio;
    }

    public void prepareWindowRestart() {

        mIntFeatureNextIdx = mIntFeatureNextIdx - mIntFeatureWindowSize + mIntFeatureReStartWindowBeginGap;
        mIntCurFeatureWindowSize=0;
        mIntCurSelIdxInWindow = mIntFeatureNextIdx+1;

    }


    public void processAllSegmentFeature() {
        //-------------------------------------------------------------------------
        //  Find Blood Flow Peak Point
        //-------------------------------------------------------------------------
        try {

            while (mIntFeatureNextIdx < mIntIntegralNextIdx) {
                processBottom();
                mIntFeatureNextIdx++;
            }
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("FeatureBottom.processSegmentFeature.Exception","");
        }
    }


    protected boolean processBottom(){
        int iVar, iBottomFindStart, iBottomFindEnd, iBottomIdx;
        int iCurShortWindowLeftIdx;
        boolean boolFound;

        boolFound = false;

        try {

            if (mIntCurFeatureWindowSize < mIntFeatureWindowSize) {
                mIntCurFeatureWindowSize++;
            }
            if (mDoubleIntegralValues[mIntCurSelIdxInWindow] >= mDoubleIntegralValues[mIntFeatureNextIdx]) {
                mIntCurSelIdxInWindow = mIntFeatureNextIdx;
            } else if (mDoubleIntegralValues[mIntFeatureNextIdx] == mDoubleIntegralValues[mIntCurSelIdxInWindow]) {
                //if (mBloodSignalIntegrator.mDoubleHRBottomIntegralValues[mIntHRBottomFeatureNextIdx] <= 0) {
                mIntCurSelIdxInWindow = mIntFeatureNextIdx;
                //}
            }

            if (mIntCurFeatureWindowSize == mIntFeatureWindowSize) {
                iCurShortWindowLeftIdx = mIntFeatureNextIdx - mIntCurFeatureWindowSize + 1;
                if (mIntCurSelIdxInWindow == iCurShortWindowLeftIdx) {
                    iBottomFindStart = mIntCurSelIdxInWindow - (mIntIntegralWindowSize *6 / 10);
                    iBottomFindEnd = mIntCurSelIdxInWindow - (mIntIntegralWindowSize *4 / 10);

                    iBottomIdx = iBottomFindStart;
                    for (iVar = iBottomFindStart; iVar <= iBottomFindEnd; iVar++) {
                        if (MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx[iBottomIdx] > MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx[iVar]) {
                            iBottomIdx = iVar;
                        }
                    }

                    if(mEnumFeatureType == ENUM_INTEGRAL_FEATURE_TYPE.FEATURE_TYPE_HR_BOTTOM) {
                        mBVSignalProcessorPart2.setPeakBottomStatesForHRBottomS1(iBottomIdx);
                    }else if(mEnumFeatureType == ENUM_INTEGRAL_FEATURE_TYPE.FEATURE_TYPE_VTI_START) {
                        mBVSignalProcessorPart2.setPeakBottomStatesForVTIStartS1(iBottomIdx);
                    }else if(mEnumFeatureType == ENUM_INTEGRAL_FEATURE_TYPE.FEATURE_TYPE_VTI_END) {
                        mBVSignalProcessorPart2.setPeakBottomStatesForVTIEndS1(iBottomIdx);
                    }

                    mIntFindFeatureLast = iBottomIdx;
                    prepareWindowRestart();
                    boolFound = true;
                }
            }
            return boolFound;
        }catch(Exception ex1){
            GIS_Log.e(TAG, "processBottom: " + ex1);
            return boolFound;
        }
    }

}
