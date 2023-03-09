package com.gis.heartio.SignalProcessSubsystem;

import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.SystemConfig;
import com.gis.heartio.UIOperationControlSubsystem.MainActivity;

/**
 * Created by brandon on 2017/5/19.
 */

public class BVSignalFeaturePeakHR extends BVSignalFeaturePeak {

    public BVSignalFeaturePeakHR(BVSignalProcessorPart2 ProcessorPart2){
        mBVSignalProcessorPart2= ProcessorPart2;
        mDoubleIntegralValues = new double[SystemConfig.mIntSystemMaxSubSegSize];
        mEnumFeatureType = ENUM_INTEGRAL_FEATURE_TYPE.FEATURE_TYPE_HR_PEAK;  //brandon
        setIntegratorType(BVSignalIntegrator.ENUM_INTEGRAL_TYPE.INTEGRAL_TYPE_MAXIDX);
    }

    public void prepareStart() {
        int iVar , iVar2;
        boolean boolItriDevice;
        double doubleIntegralWindowMilliSec, doubleShiftSize;

        //mIntFeatureNextIdx = 0;
        mIntFindPeakLast = 0;
        //mIntCurSelIdxInWindow = 0;
        mIntCurFeatureWindowSize = 0;

        mDoubleIntegralPrePeakValue = 0;
        mIntFeaturePrePeakIdx = 0;

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

        mIntIntegralWindowSize = (int) (((double) SystemConfig.mIntUltrasoundSamplerate * mBVSignalProcessorPart2.mDoubleHRPeakIntegralWindowMsec)
                / doubleShiftSize / (double) 1000);
        mIntFeatureWindowSize = (int) (((double) SystemConfig.mIntUltrasoundSamplerate * mBVSignalProcessorPart2.mDoubleHRPeakFeatureWindowMsec)
                / (double) doubleShiftSize / (double) 1000);

        mIntFeatureReStartWindowBeginGap = (int)((double)mIntFeatureWindowSize * mDoubleFeatureReStartWindowBeginGapRatio);

        prepareStartIntegral();

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
    private int getPeakHigh(){
        int iVar, iPeakFindStart, iPeakFindEnd, iPeakIdx;

        //iPeakFindStart = mIntCurSelIdxInWindow - (mIntIntegralWindowSize * 6 / 10);
        //iPeakFindEnd = mIntCurSelIdxInWindow - (mIntIntegralWindowSize * 4 / 10);
        iPeakFindStart = mIntCurSelIdxInWindow - mIntIntegralWindowSize;
        iPeakFindEnd = mIntCurSelIdxInWindow;
        iPeakIdx = iPeakFindStart;

        try {
            for (iVar = iPeakFindStart; iVar <= iPeakFindEnd; iVar++) {
                if (MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx_Hr[iPeakIdx] < MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx_Hr[iVar]) {
                    iPeakIdx = iVar;
                }
            }
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("getPeakHigh.Exception","");
        }finally {
            return iPeakIdx;
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
