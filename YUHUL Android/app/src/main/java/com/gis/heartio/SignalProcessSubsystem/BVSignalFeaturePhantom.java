package com.gis.heartio.SignalProcessSubsystem;

import com.gis.heartio.SupportSubsystem.SystemConfig;
import com.gis.heartio.UIOperationControlSubsystem.MainActivity;

import java.util.Arrays;

/**
 * Created by brandon on 2017/5/19.
 */

public class BVSignalFeaturePhantom extends BVSignalIntegrator {

    public enum ENUM_PHANTOM_DRAW_TYPE {STAGE0_PEAK, STAGE0_BOTTOM, STAGE1_PEAK, STAGE1_BOTTOM,PEAK_BASE, BOTTOM_BASE, BASE_LINE}
    private ENUM_PHANTOM_DRAW_TYPE mEnumPhantomDrawType = ENUM_PHANTOM_DRAW_TYPE.STAGE1_PEAK;

    private double mDoublePhantomDiffSumBaseRatio;

    public int INT_PHANTOM_STAGE_0 = 0;
    public int INT_PHANTOM_STAGE_1 = 1;
    private int mIntStage;
    private int mIntDiffSumValuePeak, mIntDiffSumValueBottom, mIntDiffSumBase, mIntCompareBase;
    private int INT_PHANTOM_SORT_CNT_MAX = 200;
    private int[] mIntArraySortBottomsMaxSize, mIntArraySortPeaksMaxSize;
    private int[] mIntArraySortBottoms, mIntArraySortPeaks;
    private int mIntSortBottomCnt, mIntSortPeakCnt;
    private int mIntDiffAtPeak, mIntPreDiffAtBottom;

    private int mIntFeatureWindowSize;
    private int mIntCurSelIdxInWindow;
    private int mIntFeatureNextIdxStage1;
    private int mIntFeatureNextIdxStage0;
    public int mIntFindPeakLast;
    public int mIntFindBottomLast;
    private int mIntCurFeatureWindowSize;

    //public int mIntFeaturePrePeakIdx;
    //public double mDoubleIntegralPrePeakValue;

    public double mDoubleFeatureComparePrePeakRatio;

    public double mDoubleFeatureReStartWindowBeginGapRatioAfterHigh;
    public double mDoubleFeatureReStartWindowBeginGapRatioAfterLow;
    public int mIntFeatureReStartWindowBeginGapAfterHigh, mIntFeatureReStartWindowBeginGapAfterLow;

    public enum ENUM_FEATURE_PHANTOM_STATE {STATE_START, STATE_FIND_HIGH, STATE_FIND_LOW}
    private ENUM_FEATURE_PHANTOM_STATE mEnumFeatureState;

    //public enum ENUM_FEATURE_FOUND_STATE {FOUND_NONE, FOUND_HIGH, FOUND_LOW}
    //private ENUM_FEATURE_FOUND_STATE mEnumFeatureFoundState;

    //private double mDoublePhantomHighMax;

    public BVSignalFeaturePhantom(BVSignalProcessorPart2 ProcessorPart2){
        mBVSignalProcessorPart2= ProcessorPart2;
        mDoubleIntegralValues = new double[SystemConfig.mIntSystemMaxSubSegSize];

        mEnumFeatureType = ENUM_INTEGRAL_FEATURE_TYPE.FEATURE_TYPE_PHANTOM_PEAK;  //brandon
        setIntegratorType(BVSignalIntegrator.ENUM_INTEGRAL_TYPE.INTEGRAL_TYPE_MAXIDX);

        mIntArraySortPeaksMaxSize = new int[INT_PHANTOM_SORT_CNT_MAX];
        mIntArraySortBottomsMaxSize = new int[INT_PHANTOM_SORT_CNT_MAX];

    }


    public void setFeatureReStartWindowGapRatioAfterHigh(double doubleRatio){
        mDoubleFeatureReStartWindowBeginGapRatioAfterHigh = doubleRatio;
    }

    public void setFeatureReStartWindowGapRatioAfterLow(double doubleRatio){
        mDoubleFeatureReStartWindowBeginGapRatioAfterLow = doubleRatio;
    }

    public void setFeatureComparePrePeakRatio(double doubleComparePrePeakRatio){

        mDoubleFeatureComparePrePeakRatio = doubleComparePrePeakRatio;
    }

    public void prepareStart() {
        double doubleShiftSize;

        mIntSortPeakCnt = 0;
        mIntSortBottomCnt = 0;

        mIntFindPeakLast = 0;
        mIntFindBottomLast = 0;
        mIntCurFeatureWindowSize = 0;

        mIntFeatureNextIdxStage0 = SystemConfig.mIntEndIdxNoiseLearn;
        mIntCurSelIdxInWindow = SystemConfig.mIntEndIdxNoiseLearn;

        if((SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_BE_ONLINE) ||
                (SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_LE_ONLINE) ||
                (SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_OFFLINE)) {
            doubleShiftSize = (double) SystemConfig.INT_BLOOD_VELOCITY_STFT_WINDOW_SHIFT_SIZE_64;
        }else if(SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_44K){
            doubleShiftSize = (double) SystemConfig.INT_BLOOD_VELOCITY_STFT_WINDOW_SHIFT_SIZE_256;
        }else if(SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.USCOM_44K){
            doubleShiftSize = (double) SystemConfig.INT_BLOOD_VELOCITY_STFT_WINDOW_SHIFT_SIZE_256;
        }else{
            doubleShiftSize = (double) SystemConfig.INT_BLOOD_VELOCITY_STFT_WINDOW_SHIFT_SIZE_64;
        }

        mIntIntegralWindowSize = (int) (((double) SystemConfig.mIntUltrasoundSamplerate * mBVSignalProcessorPart2.mDoublePhantomPeakIntegralWindowMsec)
                / doubleShiftSize / (double) 1000);
        mIntFeatureWindowSize = (int) (((double) SystemConfig.mIntUltrasoundSamplerate * mBVSignalProcessorPart2.mDoublePhantomPeakFeatureWindowMsec)
                / doubleShiftSize / (double) 1000);

        mIntFeatureReStartWindowBeginGapAfterHigh = (int)((double)mIntFeatureWindowSize * mDoubleFeatureReStartWindowBeginGapRatioAfterHigh);
        mIntFeatureReStartWindowBeginGapAfterLow = (int)((double)mIntFeatureWindowSize * mDoubleFeatureReStartWindowBeginGapRatioAfterLow);

        mEnumFeatureState = ENUM_FEATURE_PHANTOM_STATE.STATE_START;

        //mDoublePhantomHighMax = Double.MIN_VALUE;

        prepareStartIntegral();
    }


    public void prepareStartStage1() {

        mIntFindPeakLast = 0;
        mIntFindBottomLast = 0;
        mIntCurFeatureWindowSize = 0;
        mIntFeatureNextIdxStage1 = SystemConfig.mIntEndIdxNoiseLearn;
        mIntCurSelIdxInWindow = SystemConfig.mIntEndIdxNoiseLearn;

        mEnumFeatureState = ENUM_FEATURE_PHANTOM_STATE.STATE_START;
    }


    public void prepareWindowRestart(boolean boolAfterHigh) {

        if(mIntStage == INT_PHANTOM_STAGE_0){
            prepareWindowRestartStage0(boolAfterHigh);
        }else{
            prepareWindowRestartStage1(boolAfterHigh);
        }
    }


    public void prepareWindowRestartStage0(boolean boolAfterHigh) {

        if(boolAfterHigh) {
            mIntFeatureNextIdxStage0 = mIntFeatureNextIdxStage0 - mIntFeatureWindowSize + mIntFeatureReStartWindowBeginGapAfterHigh +1;
        }else{
            mIntFeatureNextIdxStage0 = mIntFeatureNextIdxStage0 - mIntFeatureWindowSize + mIntFeatureReStartWindowBeginGapAfterLow +1;
        }
        mIntCurFeatureWindowSize=0;
        mIntCurSelIdxInWindow = mIntFeatureNextIdxStage0+1;

    }


    public void prepareWindowRestartStage1(boolean boolAfterHigh) {

        if(boolAfterHigh) {
            mIntFeatureNextIdxStage1 = mIntFeatureNextIdxStage1 - mIntFeatureWindowSize + mIntFeatureReStartWindowBeginGapAfterHigh +1;
        }else{
            mIntFeatureNextIdxStage1 = mIntFeatureNextIdxStage1 - mIntFeatureWindowSize + mIntFeatureReStartWindowBeginGapAfterLow +1;
        }
        mIntCurFeatureWindowSize=0;
        mIntCurSelIdxInWindow = mIntFeatureNextIdxStage1+1;

    }


     public void processAllSegmentFeatureStage0() {

         mIntStage = INT_PHANTOM_STAGE_0;

        try {
            //-------------------------------------------------------------------------
            //  Find Blood Flow Peak Point
            //-------------------------------------------------------------------------
            while (mIntFeatureNextIdxStage0 < mIntIntegralNextIdx) {
                if(mEnumFeatureState == ENUM_FEATURE_PHANTOM_STATE.STATE_START) {
                    if (findPeakStage0(mIntFeatureNextIdxStage0)) {
                        mEnumFeatureState = ENUM_FEATURE_PHANTOM_STATE.STATE_FIND_LOW;
                    }
                }else if(mEnumFeatureState == ENUM_FEATURE_PHANTOM_STATE.STATE_FIND_HIGH) {
                    if (findPeakStage0(mIntFeatureNextIdxStage0)) {
                        mEnumFeatureState = ENUM_FEATURE_PHANTOM_STATE.STATE_FIND_LOW;
                    }
                }else {
                    if (findBottomStage0(mIntFeatureNextIdxStage0)) {
                        mEnumFeatureState = ENUM_FEATURE_PHANTOM_STATE.STATE_FIND_HIGH;
                    }
                }
                mIntFeatureNextIdxStage0++;
            }

        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("processSegmentFeaturePeak.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }


    private boolean findPeakStage0(int iTheIdx){
        int iCurShortWindowLeftIdx;
        boolean boolFound;

        boolFound = false;

        try{

            if (mIntCurFeatureWindowSize < mIntFeatureWindowSize) {
                mIntCurFeatureWindowSize++;
            }

            if (mDoubleIntegralValues[iTheIdx] >= mDoubleIntegralValues[mIntCurSelIdxInWindow]) {
                mIntCurSelIdxInWindow = iTheIdx;
            }

            if (mIntCurFeatureWindowSize == mIntFeatureWindowSize) {
                iCurShortWindowLeftIdx = iTheIdx - mIntCurFeatureWindowSize + 1;
                if (mIntCurSelIdxInWindow == iCurShortWindowLeftIdx) {
                    if (mIntFindPeakLast == 0) {
                        if(mEnumPhantomDrawType == ENUM_PHANTOM_DRAW_TYPE.STAGE0_PEAK) {
                            mBVSignalProcessorPart2.setPeakBottomStatesForPhantomPeak(mIntCurSelIdxInWindow);
                        }
                        mIntFindPeakLast = mIntCurSelIdxInWindow;
                        prepareWindowRestart(true);
                        boolFound = true;
                    } else {
                        if (mIntSortPeakCnt < INT_PHANTOM_SORT_CNT_MAX) {
                            mIntArraySortPeaksMaxSize[mIntSortPeakCnt] =(int) mDoubleIntegralValues[mIntCurSelIdxInWindow];
                            mIntSortPeakCnt++;
                            if(mEnumPhantomDrawType == ENUM_PHANTOM_DRAW_TYPE.STAGE0_PEAK){
                                mBVSignalProcessorPart2.setPeakBottomStatesForPhantomPeak(mIntCurSelIdxInWindow);
                            }
                        }
                        mIntFindPeakLast = mIntCurSelIdxInWindow;
                        prepareWindowRestart(true);
                        boolFound = true;
                    }
                }
            }
            return boolFound;
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("FeaturePhantom.findPhantomHigh.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            return boolFound;
        }
    }



    private boolean findBottomStage0(int iTheIdx){
        int iCurShortWindowLeftIdx;
        boolean boolFound;

        boolFound = false;

        try {

            if (mIntCurFeatureWindowSize < mIntFeatureWindowSize) {
                mIntCurFeatureWindowSize++;
            }
            if (mDoubleIntegralValues[mIntCurSelIdxInWindow] >= mDoubleIntegralValues[iTheIdx]) {
                mIntCurSelIdxInWindow = iTheIdx;
            }

            if (mIntCurFeatureWindowSize == mIntFeatureWindowSize) {
                iCurShortWindowLeftIdx = iTheIdx - mIntCurFeatureWindowSize + 1;
                if (mIntCurSelIdxInWindow == iCurShortWindowLeftIdx) {
                    if (mIntFindBottomLast == 0) {
                        //if(mIntSortBottomCnt < INT_PHANTOM_SORT_CNT_MAX) {
                        //    mIntArraySortBottomsMaxSize[mIntSortBottomCnt] = (int) mDoubleIntegralValues[mIntCurSelIdxInWindow];
                        //    mIntSortBottomCnt++;
                        //    if(mEnumPhantomDrawType == ENUM_PHANTOM_DRAW_TYPE.STAGE0_BOTTOM){
                        //        mBVSignalProcessorPart2.setPeakBottomStatesForPhantomPeak(mIntCurSelIdxInWindow);
                        //    }
                        //}
                    }else {
                        if (mIntSortBottomCnt < INT_PHANTOM_SORT_CNT_MAX) {
                            mIntArraySortBottomsMaxSize[mIntSortBottomCnt] = (int) mDoubleIntegralValues[mIntCurSelIdxInWindow];
                            mIntSortBottomCnt++;
                            if(mEnumPhantomDrawType == ENUM_PHANTOM_DRAW_TYPE.STAGE0_BOTTOM){
                                mBVSignalProcessorPart2.setPeakBottomStatesForPhantomPeak(mIntCurSelIdxInWindow);
                            }
                        }
                    }
                    mIntFindBottomLast = mIntCurSelIdxInWindow;
                    prepareWindowRestart(false);
                    boolFound = true;
                }
            }
            return boolFound;
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("FeaturePhantom.findBottomStage1.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            return boolFound;
        }
    }


    public void checkDiffSumPeakBottomFromStage0(){
        int iVar, iComparePeakIdx, iCompareBottomIdx;

            //--- 處理Peak點，取出所有Peak點   -------------------
            if (mIntSortPeakCnt == INT_PHANTOM_SORT_CNT_MAX) {
                mIntArraySortPeaks = mIntArraySortPeaksMaxSize;
            } else {
                mIntArraySortPeaks = new int[mIntSortPeakCnt];
                for (iVar = 0; iVar < mIntSortPeakCnt; iVar++) {
                    mIntArraySortPeaks[iVar] = mIntArraySortPeaksMaxSize[iVar];
                }
            }
            //--- 對所有高點排序，取75%處高點為計算DiffSumPeak之基礎Peak   --------
            Arrays.sort(mIntArraySortPeaks);
            iComparePeakIdx = (int) ((double) mIntSortPeakCnt * 0.75);
            mIntDiffSumValuePeak = mIntArraySortPeaks[iComparePeakIdx];

            //--- 處理Bottom點，取出所有Bottom點   -------------------
            if (mIntSortBottomCnt == INT_PHANTOM_SORT_CNT_MAX) {
                mIntArraySortBottoms = mIntArraySortBottomsMaxSize;
            } else {
                mIntArraySortBottoms = new int[mIntSortBottomCnt];
                for (iVar = 0; iVar < mIntSortBottomCnt; iVar++) {
                    mIntArraySortBottoms[iVar] = mIntArraySortBottomsMaxSize[iVar];
                }
            }
            //--- 對所有高點排序，取%5處低點為計算DiffSumPeak之基礎Bottom   --------
            Arrays.sort(mIntArraySortBottoms);
            iCompareBottomIdx = (int) ((double) mIntSortBottomCnt * 0.05);
            mIntDiffSumValueBottom = mIntArraySortBottoms[iCompareBottomIdx];

            //--- 決定DiffSumBase，Phantom Peak 決定之方法為跟前一點Phantom Peak相比，--------
            //--- 必須(降下去之值+升上來之值)> mIntDiffSumBase，才是Phantom Peak   --------
            if(SystemConfig.mIntHRSpecial == SystemConfig.INT_HR_SPECIAL_NO){
                mDoublePhantomDiffSumBaseRatio = SystemConfig.DOUBLE_PHANTOM_DIFF_SUM_BASE_RATIO_NORMAL;
            }else{
                mDoublePhantomDiffSumBaseRatio = SystemConfig.DOUBLE_PHANTOM_DIFF_SUM_BASE_RATIO_HR_SPECIAL;
            }
            mIntDiffSumBase = (int) ((mIntDiffSumValuePeak - mIntDiffSumValueBottom) * mDoublePhantomDiffSumBaseRatio);
            //--- DiffSumPeak 和 DiffSumBottom 之中間值，DiffSumPeak必須在中間值以上，DiffSumBottom 必須在中間值以下  -----
            mIntCompareBase = (int) (mIntDiffSumValueBottom + (mIntDiffSumValuePeak - mIntDiffSumValueBottom) * 0.5);

        //SystemConfig.mMyEventLogger.appendDebugIntEvent("mIntDiffSumBase=", mIntDiffSumBase,mIntDiffSumValuePeak, mIntDiffSumValueBottom,0,0 );
    }


    public void processAllSegmentFeatureStage1() {
        int iProcIdxMax, iVar;
        int iComparePeakIdx, iCompareBottomIdx;

        mIntStage = INT_PHANTOM_STAGE_1;

        try {
            //-------------------------------------------------------------------------
            //  Find Blood Flow Peak Point
            //-------------------------------------------------------------------------
            while (mIntFeatureNextIdxStage1 < mIntIntegralNextIdx) {
                if(mEnumFeatureState == ENUM_FEATURE_PHANTOM_STATE.STATE_START) {
                    if (findPeakStage1(mIntFeatureNextIdxStage1)) {
                        mEnumFeatureState = ENUM_FEATURE_PHANTOM_STATE.STATE_FIND_LOW;
                    }
                }else if(mEnumFeatureState == ENUM_FEATURE_PHANTOM_STATE.STATE_FIND_HIGH) {
                    if (findPeakStage1(mIntFeatureNextIdxStage1)) {
                        mEnumFeatureState = ENUM_FEATURE_PHANTOM_STATE.STATE_FIND_LOW;
                    }
                }else {
                    if (findBottomStage1(mIntFeatureNextIdxStage1)) {
                        mEnumFeatureState = ENUM_FEATURE_PHANTOM_STATE.STATE_FIND_HIGH;
                    }
                }
                mIntFeatureNextIdxStage1++;
            }

        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("processSegmentFeaturePeak.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }

    private boolean findPeakStage1(int iTheIdx){
        int iCurShortWindowLeftIdx, iDiffSumCur;
        double doubleCompare;
        boolean boolFound;

        boolFound = false;

        try{

            if (mIntCurFeatureWindowSize < mIntFeatureWindowSize) {
                mIntCurFeatureWindowSize++;
            }

            if (mDoubleIntegralValues[iTheIdx] > mDoubleIntegralValues[mIntCurSelIdxInWindow]) {
                mIntCurSelIdxInWindow = iTheIdx;
            }

            if (mIntCurFeatureWindowSize == mIntFeatureWindowSize) {
                iCurShortWindowLeftIdx = iTheIdx - mIntCurFeatureWindowSize + 1;
                if (mIntCurSelIdxInWindow == iCurShortWindowLeftIdx) {
                    if (mIntFindPeakLast == 0) {
                        if (mDoubleIntegralValues[mIntCurSelIdxInWindow] > (mIntCompareBase+mIntDiffSumBase/4)) {
                            mIntFindPeakLast = mIntCurSelIdxInWindow;
                            if (mEnumPhantomDrawType == ENUM_PHANTOM_DRAW_TYPE.STAGE1_PEAK) {
                                mBVSignalProcessorPart2.setPeakBottomStatesForPhantomPeak(mIntCurSelIdxInWindow);
                            }
                        }
                        prepareWindowRestart(true);
                        boolFound = true;
                    } else  {
                        if (mIntFindBottomLast > 0) {
                            iDiffSumCur = (int) mDoubleIntegralValues[mIntCurSelIdxInWindow]
                                    - (int) mDoubleIntegralValues[mIntFindBottomLast] + mIntPreDiffAtBottom;
                            if ((iDiffSumCur >= mIntDiffSumBase) && (mDoubleIntegralValues[mIntCurSelIdxInWindow] >= mIntCompareBase)) {
                                mIntFindPeakLast = mIntCurSelIdxInWindow;
                                if (mEnumPhantomDrawType == ENUM_PHANTOM_DRAW_TYPE.STAGE1_PEAK) {
                                    mBVSignalProcessorPart2.setPeakBottomStatesForPhantomPeak(mIntCurSelIdxInWindow);
                                }
                            }
                        }
                        prepareWindowRestart(true);
                        boolFound = true;
                    }
                }
            }
            return boolFound;
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("FeaturePhantom.findPhantomHigh.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            return boolFound;
        }
    }

    private boolean findBottomStage1(int iTheIdx){
        int iCurShortWindowLeftIdx;
        double doubleCompare;
        boolean boolFound;

        boolFound = false;

        try {

            if (mIntCurFeatureWindowSize < mIntFeatureWindowSize) {
                mIntCurFeatureWindowSize++;
            }
            if (mDoubleIntegralValues[mIntCurSelIdxInWindow] > mDoubleIntegralValues[iTheIdx]) {
                mIntCurSelIdxInWindow = iTheIdx;
            }

            if (mIntCurFeatureWindowSize == mIntFeatureWindowSize) {
                iCurShortWindowLeftIdx = iTheIdx - mIntCurFeatureWindowSize + 1;
                if (mIntCurSelIdxInWindow == iCurShortWindowLeftIdx) {
                    if (mIntFindBottomLast == 0) {
                        if (mIntFindPeakLast > 0) {
                            if (mDoubleIntegralValues[mIntCurSelIdxInWindow] <(mIntCompareBase-mIntDiffSumBase/4)) {
                                mIntPreDiffAtBottom = (int) mDoubleIntegralValues[mIntFindPeakLast]
                                        - (int) mDoubleIntegralValues[mIntCurSelIdxInWindow];
                                mIntFindBottomLast = mIntCurSelIdxInWindow;
                            }
                        }
                    }else {
                        mIntPreDiffAtBottom = (int) mDoubleIntegralValues[mIntFindPeakLast]
                                - (int) mDoubleIntegralValues[mIntCurSelIdxInWindow];
                        mIntFindBottomLast = mIntCurSelIdxInWindow;
                        if (mEnumPhantomDrawType == ENUM_PHANTOM_DRAW_TYPE.STAGE1_BOTTOM) {
                            mBVSignalProcessorPart2.setPeakBottomStatesForPhantomPeak(mIntCurSelIdxInWindow);
                        }
                    }
                    prepareWindowRestart(false);
                    boolFound = true;
                }
            }
            return boolFound;
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("FeaturePhantom.findBottomStage1.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            return boolFound;
        }
    }
}