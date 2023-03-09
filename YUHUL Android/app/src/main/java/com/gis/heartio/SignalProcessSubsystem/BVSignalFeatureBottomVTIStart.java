package com.gis.heartio.SignalProcessSubsystem;

import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.SystemConfig;

/**
 * Created by brandon on 2017/5/19.
 */

public class BVSignalFeatureBottomVTIStart extends BVSignalFeatureBottom {

    public BVSignalFeatureBottomVTIStart(BVSignalProcessorPart2 ProcessorPart2){
        mBVSignalProcessorPart2= ProcessorPart2;
        mDoubleIntegralValues = new double[SystemConfig.mIntSystemMaxSubSegSize];
        mEnumFeatureType = ENUM_INTEGRAL_FEATURE_TYPE.FEATURE_TYPE_VTI_START;  //brandon
        setIntegratorType(BVSignalIntegrator.ENUM_INTEGRAL_TYPE.INTEGRAL_TYPE_MAXIDX);
    }

    public void prepareStart() {
        int iVar , iVar2;
        boolean boolItriDevice;
        double doubleIntegralWindowMilliSec, doubleShiftSize;

        //mIntFeatureNextIdx = 0;
        mIntFindFeatureLast = 0;
        //mIntCurSelIdxInWindow = 0;
        mIntCurFeatureWindowSize = 0;

        //mDoubleIntegralPrePeakValue = 0;
        //mIntFeaturePrePeakIdx = 0;

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

        mIntIntegralWindowSize = (int) (((double) SystemConfig.mIntUltrasoundSamplerate * mBVSignalProcessorPart2.mDoubleVTIStartIntegralWindowMsec)
                / doubleShiftSize / (double) 1000);
        mIntFeatureWindowSize = (int) (((double) SystemConfig.mIntUltrasoundSamplerate * mBVSignalProcessorPart2.mDoubleVTIStartFeatureWindowMsec)
                / (double) doubleShiftSize / (double) 1000);

        mIntFeatureReStartWindowBeginGap = (int)((double)mIntFeatureWindowSize * mDoubleFeatureReStartWindowBeginGapRatio);

        prepareStartIntegral();
    }

}