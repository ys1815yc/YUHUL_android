package com.gis.heartio.SignalProcessSubsysII;

import com.gis.heartio.SignalProcessSubsysII.parameters.BloodVelocityConfig;
import com.gis.heartio.SignalProcessSubsysII.processor.BloodVelocityProcessorIIPwr;
import com.gis.heartio.SignalProcessSubsysII.utilities.Methodoligies;
import com.gis.heartio.SignalProcessSubsysII.utilities.Tag;
import com.gis.heartio.SignalProcessSubsysII.utilities.Type;
import com.gis.heartio.SupportSubsystem.SystemConfig;
import com.gis.heartio.SupportSubsystem.dataInfo;
import com.gis.heartio.UIOperationControlSubsystem.UserManagerCommon;


public class BVProcessSubsysII {
    BloodVelocityConfig BVConfig;
    BloodVelocityProcessorIIPwr BVPwr;
    Methodoligies MA;

    public BVProcessSubsysII(int[] mIntArrayVTIMaxIdx, double[][] mDoubleBVSpectrumValues, short[] mShortUltrasoundDatas) {
        BVConfig = new BloodVelocityConfig();
        MA = new Methodoligies();

        BVConfig.mIntUltrasoundSamplesMaxSizeForRun = SystemConfig.mIntUltrasoundSamplesMaxSizeForRun;
        BVConfig.mIntUltrasoundSamplerate = SystemConfig.mIntUltrasoundSamplerate;
        BVConfig.mDblUltrasoundSamplesTotalSec = BVConfig.mIntUltrasoundSamplesMaxSizeForRun / BVConfig.mIntUltrasoundSamplerate;
        BVConfig.mIntSTFTWindowSize = SystemConfig.mIntSTFTWindowSize;
        BVConfig.mIntSTFTWindowShiftSize = SystemConfig.mIntSTFTWindowShiftSize;
        BVConfig.mIntSubSeqsTotalCnt =  (BVConfig.mIntUltrasoundSamplesMaxSizeForRun / BVConfig.mIntSTFTWindowShiftSize)
                                    - (BVConfig.mIntSTFTWindowSize / BVConfig.mIntSTFTWindowShiftSize);
        BVConfig.mIntSubSeqFreqCnt = (int)(BVConfig.mIntSTFTWindowSize / 2.0) | 0x01;
        BVConfig.mDblUserPulmonaryDiameter = 10.0 * UserManagerCommon.mDoubleUserPulmDiameterCm;

        BVConfig.mDoubleBVSpectrumFilter = MA.getArrCopyOfRange(mDoubleBVSpectrumValues, 0, BVConfig.mIntSubSeqsTotalCnt - 1,0, BVConfig.mIntSubSeqFreqCnt - 1);
        BVConfig.mIntArrayUltrasoundData = Type.toInt(mShortUltrasoundDatas, 0, BVConfig.mIntUltrasoundSamplesMaxSizeForRun - 1);

        if (BVConfig.mIntProcedureMode == Tag.INT_PROCEDURE_MODE_PWR) {
            BVPwr = new BloodVelocityProcessorIIPwr(BVConfig);

            BVPwr.VelocityProcedure(BVConfig,
                    Type.toInt(mShortUltrasoundDatas,0,(SystemConfig.mIntUltrasoundSamplesMaxSizeForRun -1)),
                    Tag.INT_PROCEDURE_MODE_HUMAN,
                    //SystemConfig.mTestMode? 1 : 0 ,
                    SystemConfig.mIntSingleVpkEnabled);

            System.arraycopy(BVConfig.mIntArrayVTIMaxIdx, 0, mIntArrayVTIMaxIdx, 0, BVConfig.mIntArrayVTIMaxIdx.length);

            int iStartPos = BVPwr.mIntBVSpectrumCalculateStartPts;
            int iLatency = BVPwr.mIntBVSpectrumLatencyPts;

        }


    }
    public dataInfo getJResultDataAfterSignalProcess() {
        dataInfo mDataInfo = new dataInfo();

        if (BVConfig.mIntProcedureMode == Tag.INT_PROCEDURE_MODE_PWR) {
            mDataInfo.HR  = (int) BVPwr.bv.mDblHR;
            mDataInfo.Vpk = BVPwr.bv.mDblVPK;
            mDataInfo.VTI = BVPwr.bv.mDblVTI;

            mDataInfo.SV  = BVPwr.bv.mDblSV;
            mDataInfo.CO  = BVPwr.bv.mDblCO;
            // For test value
            mDataInfo.tmpValue = BVPwr.bv.mDblTmpValue;

//            double dDia = 10.0 * UserManagerCommon.mDoubleUserPulmDiameterCm;
//            mDataInfo.SV  = BVPwr.bv.mDblVTI * Math.PI * Math.pow(dDia / 2.0 / 10.0, 2);
//            mDataInfo.CO  = mDataInfo.SV * mDataInfo.HR / 1000.0;


/*      for FDA,
//            mDataInfo.Vpk = 1.29;
//            mDataInfo.Vpk = 1.18;
//            mDataInfo.Vpk = 1.04;
//           mDataInfo.Vpk = 0.88;
//            mDataInfo.Vpk = 0.73;
            mDataInfo.Vpk = 0.61;

//            mDataInfo.VTI = 0.7;
//            mDataInfo.VTI = 0.59;
//            mDataInfo.VTI = 0.50;
//            mDataInfo.VTI = 0.41;
//            mDataInfo.VTI = 0.33;
            mDataInfo.VTI = 0.26;

//            mDataInfo.SV = 8.9;
//            mDataInfo.SV = 7.44;
//            mDataInfo.SV = 6.10;
//            mDataInfo.SV = 5.00;
//            mDataInfo.SV = 3.96;
            mDataInfo.SV = 3.03;

//            mDataInfo.CO = 0.76;
//            mDataInfo.CO = 0.66;
//            mDataInfo.CO = 0.55;
//            mDataInfo.CO = 0.45;
//            mDataInfo.CO = 0.36;
            mDataInfo.CO = 0.28;

            mDataInfo.HR = (int)(mDataInfo.CO / (mDataInfo.SV/1000.0));
//*      for FDA,  */

        }



        if (false &&
                ((mDataInfo.HR > 180 || mDataInfo.HR <30)
            ||(mDataInfo.Vpk > 1.7 || mDataInfo.Vpk <0.25))
           )
        {
            mDataInfo.HR  = 0;
            mDataInfo.Vpk = 0;
            mDataInfo.VTI = 0;
            mDataInfo.SV  = 0;
            mDataInfo.CO  = 0;
        }
        return mDataInfo;
    }

 //   private void calculateSpecturm() {
 //       BVProcessor.setSpectrumSignalNoiseStregth(systemConfig, systemConfig.mDoubleBVSpectrumFilter);
 //       //BVProcessor.setSpectrumSignalNoiseStregth(systemConfig, systemConfig.mDoubleBVSpectrumFilter);
 //       BVProcessor.setSpectrumCalculateLearned(systemConfig, systemConfig.DOUBLE_CALCULATE_DEADBAND_SEC, systemConfig.DOUBLE_CALCULATE_LENGTH_SEC);
 //       BVProcessor.setAudioEnvelope(systemConfig, systemConfig.mIntArrayUltrasoundDatas, systemConfig.mIntSTFTWindowShiftSize);
 //       BVProcessor.setSpectrumEnvelope(systemConfig, systemConfig.mDoubleBVSpectrumFilter);
 //       BVProcessor.setVelocityProcedure(systemConfig);
 //   }

 //   private void prepareRawData() {
 //       systemConfig.mDblUserPulmonaryDiameter = Double.valueOf(window.textDia.getText());
 //       systemConfig.mIntArrayUltrasoundDatas = rawDataProcessor.getIntArrayWavDataChannel1();
//
 //       stFFT = new ShortTimeFT(systemConfig, systemConfig.mIntArrayUltrasoundDatas);
 //       systemConfig.mDoubleBVSpectrumImg = stFFT.getSpectrumAmpArrayAllElementReForm((double)window.sliderCurve.getValue());
//
 //       //Calculate Blood Velocity Method
  //      systemConfig.mDoubleBVSpectrumOrg = stFFT.getSpectrumAmpArrayAllElement();

  //      //systemConfig.mDoubleBVSpectrumFilter = stFFT.getSpectrumAmpArrayAllElementFilter(tag.INT_SPEC_FIL_VERTICAL);
  //      systemConfig.mDoubleBVSpectrumFilter = systemConfig.mDoubleBVSpectrumOrg;

  //  }



}
