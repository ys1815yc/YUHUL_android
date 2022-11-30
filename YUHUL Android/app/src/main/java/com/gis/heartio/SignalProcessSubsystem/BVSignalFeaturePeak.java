package com.gis.heartio.SignalProcessSubsystem;

import android.util.Log;

import com.gis.heartio.UIOperationControlSubsystem.MainActivity;

/**
 * Created by brandon on 2017/5/19.
 */

public class BVSignalFeaturePeak extends BVSignalIntegrator {

    protected int mIntFeatureWindowSize;
    protected int mIntCurSelIdxInWindow;
    protected int mIntFeatureNextIdx;
    public int mIntFindPeakLast;
    protected int mIntCurFeatureWindowSize;

    public int mIntFeaturePrePeakIdx = 0;
    public double mDoubleIntegralPrePeakValue;

    public double mDoubleFeatureComparePrePeakRatio;

    public double mDoubleFeatureReStartWindowBeginGapRatio;
    public int mIntFeatureReStartWindowBeginGap;

    public void setFeatureReStartWindowGapRatio(double doubleRatio){
        mDoubleFeatureReStartWindowBeginGapRatio = doubleRatio;
    }

    public void setFeatureComparePrePeakRatio(double doubleComparePrePeakRatio){

        mDoubleFeatureComparePrePeakRatio = doubleComparePrePeakRatio;
    }

    //public void prepareAfterLearned(){
    //    mIntFeatureNextIdx = mBVSignalProcessorPart2.mIntStartIdxAfterLearned;
    //    mIntCurSelIdxInWindow = mBVSignalProcessorPart2.mIntStartIdxAfterLearned;

    //}

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
                processPeakHigh();
                mIntFeatureNextIdx++;
            }
        }catch(Exception ex1){
           // SystemConfig.mMyEventLogger.appendDebugStr("procAllSegFeature.Exception","");
        }
    }


    protected boolean processPeakHigh() {
        int iVar, iPeakFindStart, iPeakFindEnd, iPeakIdx, iDebug;
        int iCurShortWindowLeftIdx;
        boolean boolFound = false, boolTwoPeakHalfs= false;

        try {

            if (mIntCurFeatureWindowSize < mIntFeatureWindowSize) {
                mIntCurFeatureWindowSize++;
            }

            if (mDoubleIntegralValues[mIntFeatureNextIdx] > mDoubleIntegralValues[mIntCurSelIdxInWindow]) {
                mIntCurSelIdxInWindow = mIntFeatureNextIdx;
            } else if (mDoubleIntegralValues[mIntFeatureNextIdx] == mDoubleIntegralValues[mIntCurSelIdxInWindow]) {
                //if (mBloodSignalIntegrator.mDoubleHRPeakIntegralValues[mIntHRPeakFeatureNextIdx] <= 0) {
                mIntCurSelIdxInWindow = mIntFeatureNextIdx;
                //}
            }

            if (mIntCurFeatureWindowSize == mIntFeatureWindowSize) {
                iCurShortWindowLeftIdx = mIntFeatureNextIdx - mIntCurFeatureWindowSize + 1;
                if (mIntCurSelIdxInWindow == iCurShortWindowLeftIdx) {
                    if (mDoubleIntegralValues[mIntCurSelIdxInWindow] >= (mDoubleIntegralPrePeakValue * mDoubleFeatureComparePrePeakRatio)) {
                        mIntFeaturePrePeakIdx = mIntCurSelIdxInWindow;
                        mDoubleIntegralPrePeakValue = mDoubleIntegralValues[mIntCurSelIdxInWindow];

                        iPeakIdx = getPeakHigh();
                        if (mEnumFeatureType == ENUM_INTEGRAL_FEATURE_TYPE.FEATURE_TYPE_HR_PEAK) {
                            mBVSignalProcessorPart2.setPeakBottomStatesForHRPeak(iPeakIdx);
                        //} else if (mEnumFeatureType == ENUM_INTEGRAL_FEATURE_TYPE.FEATURE_TYPE_VPK_S1_PEAK) {
                            mBVSignalProcessorPart2.setPeakBottomStatesForVpkPeakS1(iPeakIdx);
                        }

                        mIntFindPeakLast = iPeakIdx;
                        prepareWindowRestart();
                        boolFound = true;
                    }
                }
            }
            return boolFound;
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("processPeakHigh.Exception", "");
            return boolFound;
        }
    }

    private int getPeakHigh(){
        int iVar, iPeakFindStart, iPeakFindEnd, iPeakIdx;

        //iPeakFindStart = mIntCurSelIdxInWindow - (mIntIntegralWindowSize * 6 / 10);
        //iPeakFindEnd = mIntCurSelIdxInWindow - (mIntIntegralWindowSize * 4 / 10);
        iPeakFindStart = mIntCurSelIdxInWindow - mIntIntegralWindowSize;
        iPeakFindEnd = mIntCurSelIdxInWindow;
        iPeakIdx = iPeakFindStart;

        try {
            for (iVar = iPeakFindStart; iVar <= iPeakFindEnd; iVar++) {
                if (MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx[iPeakIdx] < MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx[iVar]) {
                    iPeakIdx = iVar;
                }
            }
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("getPeakHigh.Exception","");
        }finally {
            return iPeakIdx;
        }
    }

    private boolean  checkTwoPeakHalfs(int iPeakFindStart, int iPeakFindEnd) {
        int iVar, iCnt=0;
        for(iVar =  iPeakFindStart ; iVar <= iPeakFindEnd ; iVar++ ){
            if(mBVSignalProcessorPart2.checkPeakBottomStateForHRPeakHalf(iVar)){
                iCnt++;
            }
        }
        if(iCnt >=2){
            return true;
        }
        return false;
    }
}
