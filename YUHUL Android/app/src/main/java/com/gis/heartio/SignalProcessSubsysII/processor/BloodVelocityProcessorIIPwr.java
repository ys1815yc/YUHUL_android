package com.gis.heartio.SignalProcessSubsysII.processor;

import android.util.Log;

import com.gis.heartio.GIS_Log;
import com.gis.heartio.SignalProcessSubsysII.parameters.BloodVelocityConfig;
import com.gis.heartio.SignalProcessSubsysII.parameters.BloodVelocityResults;
import com.gis.heartio.SignalProcessSubsysII.transformer.EasyFilter;
import com.gis.heartio.SignalProcessSubsysII.transformer.FastDctLee;
import com.gis.heartio.SignalProcessSubsysII.utilities.Doppler;
import com.gis.heartio.SignalProcessSubsysII.utilities.Methodoligies;
import com.gis.heartio.SignalProcessSubsysII.utilities.Tag;
import com.gis.heartio.SignalProcessSubsysII.utilities.Type;
import com.gis.heartio.SignalProcessSubsystem.BVSignalProcessorPart1;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.SystemConfig;
import com.gis.heartio.UIOperationControlSubsystem.MainActivity;

import java.util.Arrays;

public class BloodVelocityProcessorIIPwr {
    private static final String TAG = "BloodVelocityProcessorIIPwr";
    private Methodoligies ma;
    private BloodVelocityConfig sys;
    private EasyFilter fil;
    // private Tag Tag;
    public BloodVelocityResults bv;
    // Sound Data
    double[] mDblArrayUltrasoundRAW;
    double[] mDblArrayUltrasoundFilter;
    double[] mDblArrayUltrasoundDCT;
    double mDblAudioAmplitude;

    // Spectrum Data
    public ShortTimeFT stFFT;
    double[][] mDblImgBVSpectrumFFT;
    double[][] mDblImgBVSpectrumFFTForHeartRate;
    double[][] mDblImgBVSpectrumCalculate;
    double[][] mDblImgBVSpectrumCalculateForHeartRate;
    int[][] mIntImgBitBVSpectrumCalculate;
    public int mIntBVSpectrumCalculateStartPts;
    public int mIntBVSpectrumCalculateEndPts;
    public double mDblBVSpectrumSampleSec, mDblBVSpectrumCompareSec;
    public int mIntBVSpectrumLatencyPts;
    public int mIntBVSpectrumCycles;

    // Strength Data
    double[] mDblArrSpectrumSignalStrength;
    double[] mDblArrSpectrumNoiseStrength;
    double[] mDblArrSpectrumSignalNoiseRatio;
    double mDblSpectrumSignalStrength;
    double mDblSpectrumWhiteNoiseStrength;
    double mDblSpectrumMeanNoiseStrength;
    double mDblSpectrumSignalWhiteNoiseRatio;
    double mDblSpectrumSignalMeanNoiseRatio;

    // Data for Calculate
    public int mIntSingleMode;
    public int mIntFakeMode;
    int[] mIntArrHeartBeatPeriod;
    public double[][] mDblArrsVelocityProfile;
    public double[] mDblArrVelocityProfile;
    public double[] mDblArrVelocityProfileGM;
    double[][] mDblArrBVSpectrumConditions;
    double[][][] mDblArrImgVF_HR;
    double[][] mDblArrsAngleProfile;
    double[] mDblArrAngleProfile;
    public double[][] mDblArrsVTIRange;
    public double[] mDblArrVTIRange;
    double[][] mDblArrsVTICalculate;
    double[] mDblArrVTICalculate;

    // Calibration Table
    double[] mDblArrVelocityCaliTable;

    // FFT Env Value to Velocity

    // Calculate Learning

    private void set_mDblArrayUltrasoundRAW(int[] isIn) {
        mDblArrayUltrasoundRAW = Type.toDbl(isIn, 0, sys.mIntUltrasoundSamplesMaxSizeForRun - 1);
        mDblArrayUltrasoundRAW = ma.getArrDCtoAC(mDblArrayUltrasoundRAW);

        double ampH = ma.getArrStrengthRangeMean(mDblArrayUltrasoundRAW, 0.9, 0.98);
        double ampL = ma.getArrStrengthRangeMean(mDblArrayUltrasoundRAW, 0.02, 0.1);
        mDblAudioAmplitude = ampH - ampL;
    }

    private void set_mDblArrayUltrasoundDCT(double[] dsIn) {
        int iLen = dsIn.length;
        if (Integer.bitCount(iLen) != 1) {
            int nT = Integer.highestOneBit(iLen) << 1;
            mDblArrayUltrasoundDCT = new double[nT];
            ma.setArrCopyOfRange(mDblArrayUltrasoundDCT, 0, dsIn, 0, iLen);

        } else {
            //mDblArrayUltrasoundDCT = dsIn;
            mDblArrayUltrasoundDCT = Arrays.copyOf(dsIn, iLen);
        }

        FastDctLee.transform(mDblArrayUltrasoundDCT);

        for (int i = 0; i < (mDblArrayUltrasoundDCT.length * 0.05); i++) {
            mDblArrayUltrasoundDCT[i] = 0;
        }

        for (int i = (int) (mDblArrayUltrasoundDCT.length * 0.8); i < (mDblArrayUltrasoundDCT.length); i++) {
            mDblArrayUltrasoundDCT[i] = 0;
        }

        FastDctLee.inverseTransform(mDblArrayUltrasoundDCT);
        for (int i = 0; i < mDblArrayUltrasoundDCT.length; i++)
            mDblArrayUltrasoundDCT[i] /= (mDblArrayUltrasoundDCT.length / 2.0);
        mDblArrayUltrasoundDCT = Arrays.copyOf(mDblArrayUltrasoundDCT, iLen);
    }

    private double[][] get_mDblImgBVSpectrumFFT(BloodVelocityConfig s, double[] dsIn) {
        stFFT = new ShortTimeFT(s, dsIn);
        return stFFT.getSpectrumAmpArrayAllElement();
    }

    public double[][] get_mDblImgBVSpectrumFFT() {
        double[][] r = mDblImgBVSpectrumFFT;
        int iX1 = 0, iY1 = 0;
        int iX2 = r.length - 1, iY2 = r[0].length - 1;
        r = ma.getArrCopyOfRange(r, iX1, iX2, iY1, iY2);
        return r;
    }


    private void prepare_SpectrumStrength(double[][] imgIn) {
        mDblArrSpectrumSignalStrength = new double[imgIn.length];
        mDblArrSpectrumNoiseStrength = new double[imgIn.length];
        mDblArrSpectrumSignalNoiseRatio = new double[imgIn.length];
        for (int i = 0; i < imgIn.length; i++) {

            mDblArrSpectrumSignalStrength[i] = ma.getArrStrengthRangeMean(imgIn[i], 0.9, 0.98);
            mDblArrSpectrumNoiseStrength[i] = ma.getArrMean(imgIn[i], 112, 128);
            if (mDblArrSpectrumNoiseStrength[i] > 0) {
                mDblArrSpectrumSignalNoiseRatio[i] = mDblArrSpectrumSignalStrength[i] / mDblArrSpectrumNoiseStrength[i];
            } else {
                mDblArrSpectrumSignalNoiseRatio[i] = 0;
            }
        }
    }

    private void prepare_BVSpectrumCalculate(double[][] imgIn, double[] dsSNR, int iStart, int iLength) {
        int iFinal = dsSNR.length;
        int iSection = iFinal - (iStart + iLength) + 1;
        double[] dSum = new double[iSection];
        int iS = iStart;
        int iE = iStart + iLength - 1;
        for (int i = 0; i < iSection; i++) {
            if (i == 0) {
                dSum[i] = ma.getArrSum(dsSNR, iS, iE);
            } else {
                dSum[i] = dSum[i - 1] - dsSNR[iS - 1] + dsSNR[iE];
            }
            iS++;
            iE++;
        }
        int maxPos = (int) ma.getArrMaxPos(dSum, 0, iSection - 1)[Tag.INT_POS_IDX];
        mIntBVSpectrumCalculateStartPts = maxPos;
        mIntBVSpectrumCalculateEndPts = maxPos + iLength - 1;
    }

    private double get_PartialLevelII(int[][] iImg, int iStart, int iEnd, double dPartial) {
        double r = 0;
        double dTotal = iEnd - iStart + 1;
        double dCompare = dTotal * dPartial;
        for (int i = 0; i < iImg[0].length; i++) {
            int iCnt = 0;
            for (int j = iStart; j <= iEnd; j++) {
                if (iImg[j][i] == 1) {
                    iCnt++;
                }
            }
            if (iCnt >= (int) dCompare) {
                r = i;
                break;
            }
        }

        return r;
    }

    private double get_PartialLevel(double[] dProfile, int iStart, int iEnd, double dPartial) {
        double r = 0;
        double dTotal = iEnd - iStart + 1;
        double dCompare = dTotal * dPartial;
        for (int i = 0; i < 128; i++) {
            int iCnt = 0;
            for (int j = iStart; j <= iEnd; j++) {
                if (dProfile[j] <= i) {
                    iCnt++;
                }
            }
            if (iCnt >= (int) dCompare) {
                r = i;
                break;
            }
        }

        return r;
    }

    private int[] get_HeartRatePeriod(double[][] dImgIn, double sampleSec, double compareSec, int iCycle,
                                      double dTrigger) {
        int iTimeScale = sys.mIntUltrasoundSamplerate / sys.mIntSTFTWindowShiftSize;
        int iSampleLen = (int) (sampleSec * iTimeScale);
        int iCompareLen = (int) (compareSec * iTimeScale);
        int iTotalLen = iSampleLen + iCompareLen;
        double[][] dsCompareSum = new double[iCycle + 1][iCompareLen];
        Arrays.fill(dsCompareSum[iCycle], 1);
        int[] isHRPeriod = new int[iCycle + 1];

        int[][] iImg;

        iImg = get_ImgBitsII(dImgIn, dTrigger, 0, dImgIn[0].length - 1);

        int iShift = (iImg.length - iSampleLen - iCompareLen) / iCycle;

        for (int i = 0; i < iCycle; i++) {
//			int iLow = (int)get_PartialLevel(vf, i * iShift, i * iShift + iTotalLen -1, 0.2);
//			int iHigh = (int)get_PartialLevel(vf, i * iShift, i * iShift + iTotalLen -1, 0.8);

            int iLow = (int) get_PartialLevelII(iImg, i * iShift, i * iShift + iTotalLen - 1, 0.2);
            int iHigh = (int) get_PartialLevelII(iImg, i * iShift, i * iShift + iTotalLen - 1, 0.8);

            for (int j = 0; j < iCompareLen; j++) {
                dsCompareSum[i][j] = ma.calCompareXortoSum(iImg, iLow, iHigh, i * iShift, j, iSampleLen);
            }

            for (int j = 0; j < iCompareLen; j++) {
                dsCompareSum[iCycle][j] = dsCompareSum[iCycle][j] * dsCompareSum[i][j];
            }
        }

        for (int i = 0; i < iCycle + 1; i++) {
            double[][] ssPos = ma.getArrValleyPos(Tag.INT_PEAK, dsCompareSum[i], (int) (0.1 * iTimeScale), 0,
                    iCompareLen - 1);
            double[][] ssPosT = ma.getArrSort(Tag.INT_SORT_MAX, ssPos, Tag.INT_POS_VAL);

            int iCnt = 0;
            int iIni = 0;
            if (ssPosT[0].length > 1) {
                if (ssPosT[Tag.INT_POS_IDX][0] < 42) {
                    iIni = 1;
                }
                for (int j = 1 + iIni; j < ssPosT[0].length; j++) {
                    if (ssPosT[Tag.INT_POS_VAL][j] > ssPosT[Tag.INT_POS_VAL][0] * 0.85) {
                        iCnt++;
                    }
                }
                ssPos = ma.getArrCopyOfRange(ssPosT, 0, ssPosT.length - 1, iIni, iIni + iCnt);
            }

            ssPos = ma.getArrSort(Tag.INT_SORT_MIN, ssPos, Tag.INT_POS_IDX);
            if(0 == ssPos[0].length) {
                isHRPeriod[i] = 0;
            }else {
                isHRPeriod[i] = (int) ssPos[Tag.INT_POS_IDX][0];
            }
            if (ssPos[0].length >= 4) {
                if ((ssPosT[Tag.INT_POS_VAL][1] > ssPosT[Tag.INT_POS_VAL][0])
                        && (ssPosT[Tag.INT_POS_VAL][3] > ssPosT[Tag.INT_POS_VAL][2])) {
                    isHRPeriod[i] = (int) ssPos[Tag.INT_POS_IDX][1];
                }
            } else if (ssPos[0].length == 3) {
            //    int iP0 = (int) (ssPos[Tag.INT_POS_IDX][1] - ssPos[Tag.INT_POS_IDX][0]);
                int iP0 = (int) (ssPos[Tag.INT_POS_IDX][1]);
            //    int iP1 = (int) (ssPos[Tag.INT_POS_IDX][2] - ssPos[Tag.INT_POS_IDX][1]);
                int iP1 = (int) (ssPos[Tag.INT_POS_IDX][2]);
                if (iP1 > 1.8 * iP0 && iP1 < 2.2 * iP0) {
                    isHRPeriod[i] = (int) ssPos[Tag.INT_POS_IDX][1];
                } else {
                    if (ssPos[Tag.INT_POS_VAL][1] > ssPos[Tag.INT_POS_VAL][0]) {
                        isHRPeriod[i] = (int) ssPos[Tag.INT_POS_IDX][1];
                    } else {
                        isHRPeriod[i] = (int) ssPos[Tag.INT_POS_IDX][0];
                    }
                }
            } else if (ssPos[0].length == 2) {
                int iP0 = (int) Math.abs(ssPos[Tag.INT_POS_IDX][1] - 2 * ssPos[Tag.INT_POS_IDX][0]);
                if (ssPos[Tag.INT_POS_IDX][1] > 2.5 * ssPos[Tag.INT_POS_IDX][0]) {
                    isHRPeriod[i] = (int) ssPos[Tag.INT_POS_IDX][0];
                } else if (iP0 < (0.1 * ssPos[Tag.INT_POS_IDX][0])) {
                    isHRPeriod[i] = (int) ssPos[Tag.INT_POS_IDX][0];
                } else {
                    if (ssPos[Tag.INT_POS_IDX][0] > 80) {
                        isHRPeriod[i] = (int) ssPos[Tag.INT_POS_IDX][0];
                    } else {
                        isHRPeriod[i] = (int) ma.getArrSort(Tag.INT_SORT_MAX, ssPos,
                                Tag.INT_POS_VAL)[Tag.INT_POS_IDX][0];
                    }
                }
            } else if (ssPos[0].length == 1) {
                if (ssPos[Tag.INT_POS_IDX][0] < 60) {
                    isHRPeriod[i] = (int) (2 * ssPos[Tag.INT_POS_IDX][0]); // err??
                }
            }

        }
        return isHRPeriod;
    }

    private int[] get_HeartRatePeriodFake(double[][] dImgIn, double sampleSec, double compareSec, int iCycle,
                                          double dTrigger) {
        int iTimeScale = sys.mIntUltrasoundSamplerate / sys.mIntSTFTWindowShiftSize;
        int iSampleLen = (int) (sampleSec * iTimeScale);
        int iCompareLen = (int) (compareSec * iTimeScale);
        int iTotalLen = iSampleLen + iCompareLen;
        double[][] dsCompareSum = new double[iCycle + 1][iCompareLen];
        Arrays.fill(dsCompareSum[iCycle], 1);
        int[] isHRPeriod = new int[iCycle + 1];

        int[][] iImg = new int[dImgIn.length][dImgIn[0].length];
        double[] vf = new double[dImgIn.length];
        double[] ang = new double[dImgIn.length];

        set_VelocityProfile(iImg, vf, ang, dImgIn, dTrigger);

        int iShift = (iImg.length - iSampleLen - iCompareLen) / iCycle;

        for (int i = 0; i < iCycle; i++) {
            int iLow = (int) get_PartialLevel(vf, i * iShift, i * iShift + iTotalLen - 1, 0.2);
            int iHigh = (int) get_PartialLevel(vf, i * iShift, i * iShift + iTotalLen - 1, 0.8);

            for (int j = 0; j < iCompareLen; j++) {
                dsCompareSum[i][j] = ma.calCompareXortoSum(iImg, iLow, iHigh, i * iShift, j, iSampleLen);
            }

            for (int j = 0; j < iCompareLen; j++) {
                dsCompareSum[iCycle][j] = dsCompareSum[iCycle][j] * dsCompareSum[i][j];
            }
        }

        for (int i = 0; i < iCycle + 1; i++) {
            int iCnt = 0;
            int iIni = 0;
            double[][] ssPos = ma.getArrValleyPos(Tag.INT_PEAK, dsCompareSum[i], (int) (0.1 * iTimeScale), 0,
                    iCompareLen - 1);

//200115 judge if length smaller 42 (178 bpm) then ignore
            if(ssPos[0].length > 1) {
                for (int j = 0; j < ssPos[0].length; j++) {
                    if (ssPos[Tag.INT_POS_IDX][j] < 42) {
                        iIni++;
                    }
                }
                ssPos = ma.getArrCopyOfRange(ssPos, 0, ssPos.length - 1, iIni, ssPos[0].length - 1);
            }
            
            double[][] ssPosT = ma.getArrSort(Tag.INT_SORT_MAX, ssPos, Tag.INT_POS_VAL);

            if (ssPosT[0].length > 1) {
//                if (ssPosT[Tag.INT_POS_IDX][0] < 42) {
//                    iIni = 1;
//                }
//                for (int j = 1 + iIni; j < ssPosT[0].length; j++) {
              for (int j = 1; j < ssPosT[0].length; j++) {
                    if (ssPosT[Tag.INT_POS_VAL][j] > ssPosT[Tag.INT_POS_VAL][0] * 0.85) {
                        iCnt++;
                    }
                }
                ssPos = ma.getArrCopyOfRange(ssPosT, 0, ssPosT.length - 1, 0, iCnt);
            }

            ssPos = ma.getArrSort(Tag.INT_SORT_MIN, ssPos, Tag.INT_POS_IDX);
            isHRPeriod[i] = (int) ssPos[Tag.INT_POS_IDX][0];
            if (ssPos[0].length >= 4) {
                if ((ssPos[Tag.INT_POS_VAL][1] > ssPos[Tag.INT_POS_VAL][0])
                        && (ssPos[Tag.INT_POS_VAL][3] > ssPos[Tag.INT_POS_VAL][2])) {
                    isHRPeriod[i] = (int) ssPos[Tag.INT_POS_IDX][1];
                }
            } else if (ssPos[0].length == 3) {
                int iP0 = (int) (ssPos[Tag.INT_POS_IDX][1] - ssPos[Tag.INT_POS_IDX][0]);
                int iP1 = (int) (ssPos[Tag.INT_POS_IDX][2] - ssPos[Tag.INT_POS_IDX][1]);
                if (iP1 > 1.8 * iP0 && iP1 < 2.2 * iP0) {
                    isHRPeriod[i] = (int) ssPos[Tag.INT_POS_IDX][1];
                } 
            } else if (ssPos[0].length == 2) {
                int iP0 = (int) ssPos[Tag.INT_POS_IDX][0];
                int iP1 = (int) ssPos[Tag.INT_POS_IDX][1];
                if (!(iP1 > 1.8 * iP0 && iP1 < 2.2 * iP0)) {
                    isHRPeriod[i] = (int) ssPosT[Tag.INT_POS_IDX][0];
                } 
/*
            	int iP0 = (int) Math.abs(ssPos[Tag.INT_POS_IDX][1] - 2 * ssPos[Tag.INT_POS_IDX][0]);
                if (ssPos[Tag.INT_POS_IDX][1] > 2.5 * ssPos[Tag.INT_POS_IDX][0]) {
                    isHRPeriod[i] = (int) ssPos[Tag.INT_POS_IDX][0];
                } else if (iP0 < (0.1 * ssPos[Tag.INT_POS_IDX][0])) {
                    isHRPeriod[i] = (int) ssPos[Tag.INT_POS_IDX][0];
                } else {
                    if (ssPos[Tag.INT_POS_IDX][0] > 80) {
                        isHRPeriod[i] = (int) ssPos[Tag.INT_POS_IDX][0];
                    } else {
                        isHRPeriod[i] = (int) ma.getArrSort(Tag.INT_SORT_MAX, ssPos,
                                Tag.INT_POS_VAL)[Tag.INT_POS_IDX][0];
                    }
                }
//*/
            } else if (ssPos[0].length == 1) {
                if (ssPos[Tag.INT_POS_IDX][0] < 42) {
                    isHRPeriod[i] = 0;
                    //isHRPeriod[i] = (int) (2 * ssPos[Tag.INT_POS_IDX][0]); // err??
                }
            }

        }
        return isHRPeriod;
    }

    private int[][] get_ImgBits(double[] vf, int x, int y) {
        // TODO Auto-generated method stub
        int[][] r = new int[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                if (j <= (int) vf[i]) {
                    r[i][j] = 1;
                } else {
                    r[i][j] = 0;
                }
            }
        }

        return r;
    }

    private int[][] get_ImgBitsII(double[][] img, double t, int s, int e) {
        // TODO Auto-generated method stub
        int x = img.length;
        int y = e - s + 1;
        int[][] r = new int[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                if (img[i][j + s] < t) {
                    r[i][j] = 1;
                } else {
                    r[i][j] = 0;
                }
            }
            Arrays.sort(r[i]);
        }
        return r;
    }

    public BloodVelocityProcessorIIPwr(BloodVelocityConfig s) {
        ma = new Methodoligies();
        bv = new BloodVelocityResults();
        fil = new EasyFilter();
        sys = s;
        mDblArrVelocityCaliTable = ma.getVelocityCalculateTable();
    }

    public void VelocityProcedure(BloodVelocityConfig s, int[] rawData, int iFakeMode, int iSingleMode) {
        mIntSingleMode = iSingleMode;
        mIntFakeMode = iFakeMode;
//        bv.setBVInit();

        set_mDblArrayUltrasoundRAW(rawData);

        double[] wnd = new double[5];
        Arrays.fill(wnd, 1 / (double)wnd.length);
//        mDblArrayUltrasoundFilter = fil.filterFIR(wnd, mDblArrayUltrasoundRAW);
//        set_mDblArrayUltrasoundDCT(this.mDblArrayUltrasoundRAW);

        mDblImgBVSpectrumFFT = get_mDblImgBVSpectrumFFT(sys, this.mDblArrayUltrasoundRAW);

        // Cavin Test Start
        //bv.mDblTmpValue = 125 / SystemConfig.mDopplerInfo.HRLength * 60;
        bv.mDblTmpValue = Doppler.getMax(SystemConfig.mDopplerVFOutput[0]);
        GIS_Log.d(TAG,"Wu max freq="+bv.mDblTmpValue);
        mDblImgBVSpectrumFFTForHeartRate = mDblImgBVSpectrumFFT;

        //        mDblImgBVSpectrumFFTForHeartRate = get_mDblImgBVSpectrumFFT(sys, this.mDblArrayUltrasoundFilter);

        prepare_SpectrumStrength(mDblImgBVSpectrumFFT);
        prepare_BVSpectrumCalculate(mDblImgBVSpectrumFFT, mDblArrSpectrumSignalNoiseRatio,
                BloodVelocityConfig.INTEGER_NOISE_LEARN_START_IDX, BloodVelocityConfig.INTEGER_CALCULATE_LENGTH_PTS);

        mDblImgBVSpectrumCalculate = ma.getArrCopyOfRange(mDblImgBVSpectrumFFT, mIntBVSpectrumCalculateStartPts,
                mIntBVSpectrumCalculateEndPts, 0, mDblImgBVSpectrumFFT[0].length - 1);

        mDblImgBVSpectrumCalculateForHeartRate = ma.getArrCopyOfRange(mDblImgBVSpectrumFFTForHeartRate, mIntBVSpectrumCalculateStartPts,
                mIntBVSpectrumCalculateEndPts, 0, mDblImgBVSpectrumFFT[0].length - 1);

        prepare_SpectrumStrength(mDblImgBVSpectrumCalculate);


        mIntBVSpectrumCycles = 5;
        mDblBVSpectrumSampleSec = 2.0;
        mDblBVSpectrumCompareSec = 2.0;
        //mIntBVSpectrumLatencyPts = (int) ((mIntBVSpectrumCalculateEndPts - mIntBVSpectrumCalculateStartPts + 1)
        //        - ((mDblBVSpectrumCompareSec + mDblBVSpectrumSampleSec) * 125)) / mIntBVSpectrumCycles;

        sys.mIntArrayVTIMaxIdx = new int[this.mDblImgBVSpectrumFFT.length];

        if (iSingleMode == 1) {
            set_SpectrumStrength(mDblImgBVSpectrumCalculate);
            for_VelocityProfile(mDblImgBVSpectrumCalculate, iFakeMode, iSingleMode);

            bv.mDblVPK = ma.getArrStrengthRangeMean(ma.getArrVelocity(mDblArrVelocityProfile, mDblArrVelocityCaliTable),
                    0.3, 0.7);

            bv.mDblHR = 0;
            bv.mDblVTI = 0;

            bv.mDblAudioAmplitude = this.mDblAudioAmplitude;

            bv.mDblSignalStrength = this.mDblSpectrumSignalStrength;

            bv.mDblWhiteNoiseStrength = this.mDblSpectrumWhiteNoiseStrength;
            bv.mDblMeanNoiseStrength = this.mDblSpectrumMeanNoiseStrength;

            bv.mDblSignalWhiteNoiseRatio = this.mDblSpectrumSignalWhiteNoiseRatio;
            bv.mDblSignalMeanNoiseRatio = this.mDblSpectrumSignalMeanNoiseRatio;

            double[] vf_t = mDblArrVelocityProfile;
            //sys.mIntArrayVTIMaxIdx = new int[this.mDblImgBVSpectrumFFT.length];
            for (int i = mIntBVSpectrumCalculateStartPts; i <= mIntBVSpectrumCalculateEndPts; i++) {
                sys.mIntArrayVTIMaxIdx[i] = (int) vf_t[i - mIntBVSpectrumCalculateStartPts];
            }

        } else {

            set_SpectrumStrength(mDblImgBVSpectrumCalculate);
            double dTrigger;
            if (iFakeMode == 1) {
                mIntBVSpectrumCycles = 5;
                dTrigger = 0.1 * (this.mDblSpectrumSignalStrength - this.mDblSpectrumWhiteNoiseStrength)
                        + this.mDblSpectrumWhiteNoiseStrength;
                mIntArrHeartBeatPeriod = get_HeartRatePeriodFake(mDblImgBVSpectrumCalculate, mDblBVSpectrumSampleSec,
                        mDblBVSpectrumCompareSec, mIntBVSpectrumCycles, dTrigger);
                double[] sim = ma.getSimilarValue(mIntArrHeartBeatPeriod, 0.2);
                bv.mDblPeriod = sim[0];
                if (bv.mDblPeriod == 0) {
                    bv.mDblHeartRateStability = 0;
                    bv.mDblHeartRateStability = 0;
                    bv.mDblHR = 0;
                }else{
                    bv.mDblHeartRateStability = (sim[1] / sim[0]) / mIntArrHeartBeatPeriod.length;
                    int iTimeScale = s.mIntUltrasoundSamplerate / s.mIntSTFTWindowShiftSize;
                    bv.mDblHR = (iTimeScale / bv.mDblPeriod) * 60.0;
                }
            } else {

                mIntBVSpectrumCycles = 6;
                // Cavin modified for test 20210617
//                dTrigger = ma.get_SpectrumStrengthRange(this.mDblImgBVSpectrumCalculateForHeartRate, 2, 120, 0.2, 0.8);
                dTrigger = ma.get_SpectrumStrengthRange(this.mDblImgBVSpectrumCalculateForHeartRate, 0, 128, 0.2, 0.6);
                //dTrigger = ma.get_SpectrumStrengthRange(this.mDblImgBVSpectrumCalculateForHeartRate, 0, 128, 0.1, 0.8);

                // Calculate HeartRate
                mIntArrHeartBeatPeriod = get_HeartRatePeriod(mDblImgBVSpectrumCalculateForHeartRate, mDblBVSpectrumSampleSec,
                        mDblBVSpectrumCompareSec, mIntBVSpectrumCycles, dTrigger);

                bv.mDblPeriod = mIntArrHeartBeatPeriod[mIntBVSpectrumCycles];
                if (bv.mDblPeriod == 0) {
                    bv.mDblHeartRateStability = 0;
                    bv.mDblHR = 0;
                }else{
                    int iCnt = 1;
                    for (int i = 0; i < mIntBVSpectrumCycles; i++) {
                        int iL = Math
                                .abs(mIntArrHeartBeatPeriod[i] - mIntArrHeartBeatPeriod[mIntArrHeartBeatPeriod.length - 1]);
                        if (iL <= (int) (mIntArrHeartBeatPeriod[mIntArrHeartBeatPeriod.length - 1] * 0.15)) {
                            iCnt++;
                        }
                    }
                    bv.mDblHeartRateStability = (double) iCnt / (double) mIntArrHeartBeatPeriod.length;
                    int iTimeScale = s.mIntUltrasoundSamplerate / s.mIntSTFTWindowShiftSize;
                    GIS_Log.d(TAG,"iTimeScale = "+ iTimeScale + "  bv.mDblPeriod = "+bv.mDblPeriod);
                    bv.mDblHR = (iTimeScale / bv.mDblPeriod) * 60.0;
                }
            }


            if (1 == iFakeMode && 0 != bv.mDblPeriod) {
                for_VelocityProfile(mDblImgBVSpectrumCalculate, iFakeMode, iSingleMode);
                mDblArrVelocityProfile = fil.filterFIR(fil.mDblArrLPFS125FP8E16, mDblArrVelocityProfile);

                this.mDblArrVTIRange = get_VTIStartEnd((int) bv.mDblPeriod, mDblArrVelocityProfile);

                mDblArrVTICalculate = new double[Tag.INT_RESULT_VTI_TAG];
                Calculate(mDblArrVTICalculate, mDblImgBVSpectrumCalculate, mDblArrVelocityProfile, mDblArrVTIRange);

                bv.mDblVPK = mDblArrVTICalculate[Tag.INT_RESULT_VTI_VTI_Peak];
                bv.mDblVTI = mDblArrVTICalculate[Tag.INT_RESULT_VTI_VTI_F];

                int st = (int) mDblArrVTIRange[Tag.INT_CAL_VTI_START];
                int ed = (int) mDblArrVTIRange[Tag.INT_CAL_VTI_END];
                mDblArrVTIRange[Tag.INT_CAL_VTI_START] = mDblArrVTIRange[Tag.INT_CAL_VTI_START]
                        + this.mIntBVSpectrumCalculateStartPts;
                mDblArrVTIRange[Tag.INT_CAL_VTI_END] = mDblArrVTIRange[Tag.INT_CAL_VTI_END]
                        + this.mIntBVSpectrumCalculateStartPts;


                double[] vf_t = mDblArrVelocityProfile;
                //sys.mIntArrayVTIMaxIdx = new int[this.mDblImgBVSpectrumFFT.length];
                for (int i = mIntBVSpectrumCalculateStartPts; i <= mIntBVSpectrumCalculateEndPts; i++) {
                    sys.mIntArrayVTIMaxIdx[i] = (int) vf_t[i - mIntBVSpectrumCalculateStartPts];
                }

                bv.mDblDia = sys.mDblUserPulmonaryDiameter;
                bv.mDblSV = bv.mDblVTI * Math.PI * Math.pow(bv.mDblDia / 2.0 / 10.0, 2);
                bv.mDblCO = bv.mDblHR * bv.mDblSV / 1000.0;

                bv.mDblAudioAmplitude = this.mDblAudioAmplitude;

                bv.mDblSignalStrength = this.mDblSpectrumSignalStrength;

                bv.mDblWhiteNoiseStrength = this.mDblSpectrumWhiteNoiseStrength;
                bv.mDblMeanNoiseStrength = this.mDblSpectrumMeanNoiseStrength;

                bv.mDblSignalWhiteNoiseRatio = bv.mDblSignalStrength / bv.mDblWhiteNoiseStrength;
                bv.mDblSignalMeanNoiseRatio = bv.mDblSignalStrength / bv.mDblMeanNoiseStrength;

            } else if(0 == iFakeMode && 0 != bv.mDblPeriod){
                for_VelocityProfile(mDblImgBVSpectrumCalculate, iFakeMode, iSingleMode);

                // check velocity profile
                int maxVelocity = (int)ma.getArrMax(mDblArrVelocityProfile,0,mDblArrVelocityProfile.length-1);
                int maxCount = 0;
                int count32 = 0;
                int count64 = 0;
                int count96 = 0;
                int maxContiCount = 0;
                int contiCount = 0;
                for (int i = 0; i<mDblArrVelocityProfile.length; i++){
                    if (mDblArrVelocityProfile[i] == maxVelocity){
                        maxCount++;
                        if (i>1 && mDblArrVelocityProfile[i-1]==mDblArrVelocityProfile[i]){
                            contiCount++;
                            maxContiCount = contiCount;
                        } else{
                            contiCount = 0;
                        }
                    }
                    if (mDblArrVelocityProfile[i] == 32){
                        count32++;
                    }else if (mDblArrVelocityProfile[i] == 64){
                        count64++;
                    }else if (mDblArrVelocityProfile[i] == 96){
                        count96++;
                    }
                }
                if (maxContiCount >= 2){
                    GIS_Log.d(TAG,"maxVelocity="+maxVelocity);
                    GIS_Log.d(TAG,"maxCount="+maxCount);
                    GIS_Log.d(TAG,"maxContiCount="+maxContiCount);
                    GIS_Log.d(TAG,"count32="+count32);
                    GIS_Log.d(TAG,"count64="+count64);
                    GIS_Log.d(TAG,"count96="+count96);
                }

                // Cavin test start 210819
                double[] tmpVelocityProfile = new double[BloodVelocityConfig.INTEGER_CALCULATE_LENGTH_PTS];
                System.arraycopy(SystemConfig.mDopplerVFOutput[0],mIntBVSpectrumCalculateStartPts
                        ,tmpVelocityProfile,0,BloodVelocityConfig.INTEGER_CALCULATE_LENGTH_PTS);
                GIS_Log.d(TAG, "SystemConfig.mDopplerVFOutput[0].length = "+SystemConfig.mDopplerVFOutput[0].length
                        +", mDblArrVelocityProfile.length = "+ mDblArrVelocityProfile.length);
                this.mDblArrsVTIRange = get_VTIsStartEnd((int)bv.mDblPeriod,
                        tmpVelocityProfile,tmpVelocityProfile);
//                this.mDblArrsVTIRange = get_VTIsStartEnd((int) bv.mDblPeriod,
//                        mDblArrVelocityProfile, mDblArrVelocityProfileGM);
                // Cavin test end 210819

                bv.mDblVTIStability = mDblArrsVTIRange[0].length
                        / ((double) mDblImgBVSpectrumCalculate.length / bv.mDblPeriod);

                double[][] sort;

                double[] vf_t = new double[mDblArrVelocityProfile.length];

                if (mDblArrsVTIRange[0].length > 0) {
                    for (int i = 0; i < mDblArrsVTIRange[0].length; i++) {
                        int st = (int) mDblArrsVTIRange[Tag.INT_CAL_VTI_START][i];
                        int ed = (int) mDblArrsVTIRange[Tag.INT_CAL_VTI_END][i];
                        for (int j = st; j <= ed; j++) {
                            vf_t[j] = mDblArrVelocityProfile[j];
                        }
                    }

                    //* reshape
                    vf_t = fil.filterFIR(fil.mDblArrLPFS125FP1E2, vf_t);
                    int[] iVf_t = Type.toInt(vf_t);
                    vf_t = new double[mDblArrVelocityProfile.length];

                    for (int i = 0; i < mDblArrsVTIRange[0].length; i++) {
                        int st = (int) mDblArrsVTIRange[Tag.INT_CAL_VTI_START][i];
                        int ed = (int) mDblArrsVTIRange[Tag.INT_CAL_VTI_END][i];
                        int[] iSort = ma.getArrCopyOfRange(iVf_t, st, ed);
                        iSort = ma.getArrSort(Tag.INT_SORT_MAX, iSort);
                        double gain = mDblArrsVTIRange[Tag.INT_CAL_VTI_VPK_RAW][i] / ma.getArrMax(iVf_t, st, ed);

                        for (int j = st; j <= ed; j++) {
                            int k = j - st;
                            if ((k > 0) && (k <= iSort.length / 2)) {
                                vf_t[j] = (int) (iSort[(iSort.length - 2 * k)] * gain);
                            } else if ((k < iSort.length) && (k > iSort.length / 2)) {
                                vf_t[j] = (int) (iSort[(k - iSort.length / 2) * 2] * gain);
                            }
                        }
                    }
//* reshape */
//calculate VTI
                    double[] vf_cal = ma.getArrVelocity(vf_t, this.mDblArrVelocityCaliTable);

                    for (int i = 0; i < mDblArrsVTIRange[0].length; i++) {
                        int st = (int) mDblArrsVTIRange[Tag.INT_CAL_VTI_START][i];
                        int ed = (int) mDblArrsVTIRange[Tag.INT_CAL_VTI_END][i];
                        mDblArrsVTIRange[Tag.INT_CAL_VTI_VTI][i] = ma.getArrSum(vf_cal, st, ed);
                    }

//position offset
                    for (int i = 0; i < mDblArrsVTIRange[0].length; i++) {
                        mDblArrsVTIRange[Tag.INT_CAL_VTI_START][i] = mDblArrsVTIRange[Tag.INT_CAL_VTI_START][i]
                                + this.mIntBVSpectrumCalculateStartPts;
                        mDblArrsVTIRange[Tag.INT_CAL_VTI_END][i] = mDblArrsVTIRange[Tag.INT_CAL_VTI_END][i]
                                + this.mIntBVSpectrumCalculateStartPts;
                    }

                    if (mDblArrsVTIRange[0].length > 2) {
                        sort = ma.getArrSort(Tag.INT_SORT_MAX, mDblArrsVTIRange, Tag.INT_CAL_VTI_VPK);
                        bv.mDblVPK = ma.getArrMean(sort[Tag.INT_CAL_VTI_VPK], 1, 2);   // original
//                        bv.mDblVPK = ma.getArrMean(sort[Tag.INT_CAL_VTI_VPK], 0, 2);
//                        bv.mDblVPK = (sort[Tag.INT_CAL_VTI_VPK][0]);
                        sort = ma.getArrSort(Tag.INT_SORT_MAX, mDblArrsVTIRange, Tag.INT_CAL_VTI_VTI);
                        bv.mDblVTI = ma.getArrMean(sort[Tag.INT_CAL_VTI_VTI], 1, 2);   // original
//                        bv.mDblVTI = ma.getArrMean(sort[Tag.INT_CAL_VTI_VTI], 0, 2);
//                        bv.mDblVTI = (sort[Tag.INT_CAL_VTI_VTI][0]);
                    } else if (mDblArrsVTIRange[0].length > 1) {
                        bv.mDblVPK = ma.getArrMean(mDblArrsVTIRange[Tag.INT_CAL_VTI_VPK], 0, 1);
                        bv.mDblVTI = ma.getArrMean(mDblArrsVTIRange[Tag.INT_CAL_VTI_VTI], 0, 1);
                    } else if (mDblArrsVTIRange[0].length == 1) {
                        bv.mDblVPK = (mDblArrsVTIRange[Tag.INT_CAL_VTI_VPK][0]);
                        bv.mDblVTI = (mDblArrsVTIRange[Tag.INT_CAL_VTI_VTI][0]);
                    } else {
                        bv.mDblVPK = 0;
                        bv.mDblVTI = 0;
                    }

                    //sys.mIntArrayVTIMaxIdx = new int[this.mDblImgBVSpectrumFFT.length];

                    for (int i = mIntBVSpectrumCalculateStartPts; i <= mIntBVSpectrumCalculateEndPts; i++) {
                        sys.mIntArrayVTIMaxIdx[i] = (int) vf_t[i - mIntBVSpectrumCalculateStartPts];
                    }
                }

                bv.mDblDia = sys.mDblUserPulmonaryDiameter;
                bv.mDblSV = bv.mDblVTI * Math.PI * Math.pow(bv.mDblDia / 2.0 / 10.0, 2);
                bv.mDblCO = bv.mDblHR * bv.mDblSV / 1000.0;

                bv.mDblAudioAmplitude = this.mDblAudioAmplitude;

                bv.mDblSignalStrength = this.mDblSpectrumSignalStrength;

                bv.mDblWhiteNoiseStrength = this.mDblSpectrumWhiteNoiseStrength;
                bv.mDblMeanNoiseStrength = this.mDblSpectrumMeanNoiseStrength;

                bv.mDblSignalWhiteNoiseRatio = bv.mDblSignalStrength / bv.mDblWhiteNoiseStrength;
                bv.mDblSignalMeanNoiseRatio = bv.mDblSignalStrength / bv.mDblMeanNoiseStrength;

                // check max count
                if (maxCount >= 40 &&(count32 >= 10 ||
                    count64 >= 10 || count96 >= 10)){
                    bv.mDblVPK = 0;
                    bv.mDblSV = 0;
                    bv.mDblCO = 0;
                    bv.mDblHR = 0;
                    bv.mDblVTI = 0;
                    GIS_Log.d(TAG,"error");
                    MainActivity.mBVSignalProcessorPart1.mIntHRErrCode |= BVSignalProcessorPart1.BINARY_ERR_CODE_ELECTRICAL_INTEFFERENCE;
                }

            }

        }


    }

    private void Calculate(double[] vti_o, double[][] v_HR, double[] vf_s, double[] seg) {
        // TODO Auto-generated method stub
        if (seg[Tag.INT_CAL_VTI_MPID] != 1) {

        } else {
            _VPK2(vti_o, v_HR, vf_s, seg[Tag.INT_CAL_VTI_START], seg[Tag.INT_CAL_VTI_END]);
        }
    }

    private void Calculate(double[][] vti_o, double[][][] v_HR, double[][] vf_s, double[][] seg) {
        int iCycle = seg.length;
        for (int k = 0; k < iCycle; k++) {
            if (seg[k][Tag.INT_CAL_VTI_MPID] != 1) {

            } else {
                _VPK2(vti_o[k], v_HR[k], vf_s[k], seg[k][Tag.INT_CAL_VTI_START], seg[k][Tag.INT_CAL_VTI_END]);

            }
        }

    }


    private void _VPK2(double[] dsO, double[][] sp_in, double[] vf_in, double add_s, double add_e) {
        // TODO Auto-generated method stub
        double VF_s = vf_in[(int) add_s];
        double VF_e = vf_in[(int) add_e];
        double[][] sp = ma.getArrCopyOfRange(sp_in, (int) add_s, (int) add_e, 0, sp_in[0].length - 1);
        double[] vf_s = ma.getArrCopyOfRange(vf_in, (int) add_s, (int) add_e);
        vf_s = ma.getArrVelocity(vf_s, mDblArrVelocityCaliTable);

        int iRange = (int) ((add_e - add_s) / 5);
        double[] vf_p = ma.getArrCopyOfRange(vf_in, (int) add_s + iRange, (int) add_e - iRange);

        for (int i = 0; i < (int) (add_e - add_s + 1); i++) {
            for (int j = (int) (vf_s[i]); j < sp[0].length; j++) {
                sp[i][j] = 0;
            }
        }

        dsO[Tag.INT_RESULT_VTI_VF_s] = VF_s;
        dsO[Tag.INT_RESULT_VTI_VF_e] = VF_e;
        dsO[Tag.INT_RESULT_VTI_VTI_F] = ma.getArrSum(vf_s) * (1.0 / 125.0) * 100;
        dsO[Tag.INT_RESULT_VTI_VTI_Sum] = ma.getArrSum(sp);
        dsO[Tag.INT_RESULT_VTI_VTI_Peak] = ma.getArrMax(vf_s);
    }

    private int pseg(int[] tp_out_fg_o, double[] tp_in) {
        int vth;
        double tp_max = ma.getArrMax(tp_in);
        double BWR = 0;
        int pth = 0;
        int iLen = tp_in.length;
        int[] tp_in_fg = new int[iLen];
        int[] tp_pn_fg = new int[iLen];
        while ((pth < 100) && (BWR < 0.15)) {
            pth = pth + 1;
            for (int i = 0; i < tp_in.length; i++) {
                if (tp_in[i] > (pth * tp_max / 100)) {
                    tp_in_fg[i] = 0;
                    tp_pn_fg[i] = 1;
                } else {
                    tp_in_fg[i] = 1;
                    tp_pn_fg[i] = 0;
                }
            }
            BWR = ma.getArrMean(tp_in_fg, 0, iLen - 1);
        }
        vth = pth;
        int int_fg = 0;
        for (int i = 0; i < tp_in.length; i++) {
            if (tp_in_fg[i] == 1) {
                int_fg = int_fg + 1;
            } else {
                int_fg = 0;
            }
            tp_out_fg_o[i] = int_fg;
        }
        return vth;
    }

    // Hn: Heart beat cycle length. vf: velocity profile
    private double[][] get_VTIsStartEnd(int Hn, double[] vf_Flow, double[] vf_GM) {
        // TODO Auto-generated method stub

        double[][] r;
        double[] vf_After; // After Summation by Strength
        double[] vf_Now; // Between Summation by Flow
        double[] vf_Before; // Before Summation by Flow
        double[] vf_Search = new double[vf_Flow.length]; // Before Summation by Flow

        // |0x01 to make sure iLpXX is odd
        int iLp03 = (int) (Hn * 0.03) | 0x01;
        int iLp05 = (int) (Hn * 0.05) | 0x01;
        int iLp10 = (int) (Hn * 0.1) | 0x01;
        int iLp15 = (int) (Hn * 0.15) | 0x01;
        int iLp20 = (int) (Hn * 0.20) | 0x01;
        int iLp25 = (int) (Hn * 0.25) | 0x01;
        int iLp40 = (int) (Hn * 0.4) | 0x01;
        int iLp30 = (int) (Hn * 0.3) | 0x01;
        int iLp35 = (int) (Hn * 0.35) | 0x01;
        int iLp45 = (int) (Hn * 0.45) | 0x01;
        int iLp55 = (int) (Hn * 0.55) | 0x01;
        int iLp75 = (int) (Hn * 0.75) | 0x01;
        int iLp80 = (int) (Hn * 0.80) | 0x01;

        double[] wnd = new double[iLp35];
        Arrays.fill(wnd, 1.0);
        vf_After = fil.filterFIR(Tag.INT_AFTER, wnd, vf_GM);

        wnd = new double[iLp05];
        Arrays.fill(wnd, 1.0);
        vf_Now = fil.filterFIR(Tag.INT_AFTER, wnd, vf_Flow);

        wnd = new double[iLp15];
        Arrays.fill(wnd, 1.0);
        vf_Before = fil.filterFIR(Tag.INT_BEFORE, wnd, vf_Flow);

        // Cavin test 20210826
//        for (int i = 0; i < vf_Flow.length; i++) {
//            vf_Search[i] = vf_After[i] / ma.getArrMin(vf_Before, i - iLp20, i);
//        }
        for (int i = iLp20; i < vf_Flow.length - iLp35;i++){
            vf_Search[i] = vf_After[i] / ma.getArrMin(vf_Before, i - iLp20, i);
        }
        // Cavin test 20210826 end

        int iStart = iLp55;
        int iEnd = 0;
        int iCnt = 0;

        int[] posStart = new int[2 * vf_Flow.length / Hn];
        while (iEnd < vf_Flow.length - iLp55) {
            iEnd = (int) (iStart + Hn * 1.5);
            if (iEnd >= vf_Flow.length - iLp55)
                break;
            double[][] t_pk = ma.getArrValleyPos(Tag.INT_PEAK, vf_Search, iLp10, iStart, iEnd);
            t_pk = ma.getArrSort(Tag.INT_SORT_MAX, t_pk, Tag.INT_POS_VAL);
            if (t_pk[Tag.INT_POS_IDX].length > 0) {
                // Cavin test 20210826
//                posStart[iCnt] = (int) t_pk[Tag.INT_POS_IDX][0];
                posStart[iCnt] = (int) t_pk[Tag.INT_POS_IDX][0]+2;
                // Cavin test 20210826 end
                iStart = posStart[iCnt] + iLp75;
                iCnt++;
            } else {
                iStart = iStart + iLp75;
            }
        }

//* Actual Start & End
        posStart = ma.getArrCopyOfRange(posStart, 0, iCnt - 1);
        int[] posEnd = new int[posStart.length];
        for (int i = 0; i < posStart.length; i++) {
            double[][] posPeak = ma.getArrValleyPos(Tag.INT_PEAK, vf_Now, iLp05, posStart[i], posStart[i] + iLp55);
            // Cavin closed 20210729
//            if (posPeak[0].length > 0) {
//                int iPos = (int) ma.getArrSort(Tag.INT_SORT_MAX, posPeak, Tag.INT_POS_VAL)[Tag.INT_POS_IDX][0];

//                double[][] ssPos = ma.getArrValleyPos(Tag.INT_VALLEY, Tag.INT_REWARD, vf_Now, vf_Now[iPos], iLp05, iPos,
//                        iPos - iLp55);
//                double[][] srPos = ma.getArrValleyPos(Tag.INT_VALLEY, Tag.INT_REWARD, vf_Now, vf_Now[iPos], iLp05, iPos,
//                        iPos - iLp55);
//                if (srPos[0].length > 0){
//                    srPos = ma.getArrSort(Tag.INT_SORT_MIN, srPos, Tag.INT_POS_VAL);
//                    posStart[i] = (int) srPos[Tag.INT_POS_IDX][0];
//                    posEnd[i] = posStart[i] + iLp35;
//                }else{
//                    double[][] ssPos;
//                    ssPos = ma.getArrValleyPos(Tag.INT_VALLEY, Tag.INT_FORWARD, vf_Now, vf_Now[iPos], iLp05, iPos,
//                            iPos + iLp55);
//
//                    if (ssPos[0].length > 0) {
//                        ssPos = ma.getArrSort(Tag.INT_SORT_MIN, ssPos, Tag.INT_POS_VAL);
//                        posEnd[i] = (int) ssPos[Tag.INT_POS_IDX][0];
//                        posStart[i] = posEnd[i] - iLp35;
//                    } else {
//                        posEnd[i] = posStart[i] + iLp35;
//                    }
//                }

//            } else {
                posEnd[i] = posStart[i] + iLp35;
//            }
        }

        r = new double[Tag.INT_CAL_VTI_TAG][posStart.length];
        for (int i = 0; i < posStart.length; i++) {
            r[Tag.INT_CAL_VTI_START][i] = posStart[i];
            r[Tag.INT_CAL_VTI_END][i] = posEnd[i];
            r[Tag.INT_CAL_VTI_VPK_RAW][i] = get_Cal_VTI_Vpk(vf_Flow, this.mDblArrSpectrumNoiseStrength, posStart[i],
                    posEnd[i], Hn);
            r[Tag.INT_CAL_VTI_VPK_RAW][i] = Math.min(128, r[Tag.INT_CAL_VTI_VPK_RAW][i]);
            r[Tag.INT_CAL_VTI_VPK][i] = mDblArrVelocityCaliTable[(int) r[Tag.INT_CAL_VTI_VPK_RAW][i]];
        }
        return r;

    }


    private double get_Cal_VTI_Vpk(double[] vf, double[] noiseStrength, int vtiStart, int vtiEnd, int Hn) {
        double r;
        double[] calVF = ma.getArrCopyOfRange(vf, vtiStart, vtiStart + Hn - 1);
        double[] calNoise = ma.getArrCopyOfRange(noiseStrength, vtiStart, vtiStart + Hn - 1);
        int calLen = vtiEnd - vtiStart + 1;

        double dTrigger = ma.getArrStrengthOrder(calNoise, 0.4, 0, calNoise.length - 1);
        for (int i = 0; i < calNoise.length; i++) {
            if (calNoise[i] > dTrigger) {
                calVF[i] = 0;
            }
        }
        r = ma.getArrMax(calVF, 0, calLen);
        return r;

    }

    private double[] get_VTIStartEnd(int Hn, double[] vf_s) {
        // TODO Auto-generated method stub
        double[] r = new double[Tag.INT_CAL_VTI_TAG];
        int[] TPs_out_fg = new int[vf_s.length];
        int vfth = pseg(TPs_out_fg, vf_s);

        double[] dbl_o = FFLP3(TPs_out_fg, Hn, vf_s, vfth);
        r[Tag.INT_CAL_VTI_START] = dbl_o[Tag.INT_CAL_VTI_START];
        r[Tag.INT_CAL_VTI_END] = dbl_o[Tag.INT_CAL_VTI_END];
        r[Tag.INT_CAL_VTI_BWR] = dbl_o[Tag.INT_CAL_VTI_BWR];
        r[Tag.INT_CAL_VTI_MPID] = dbl_o[Tag.INT_CAL_VTI_MPID];

        return r;

    }

    // [segs,sege,bwr,pkx,pky,Mip_id]=FFLP3(TPs_out_fg,Hn,vf_s,vfth);
    private double[] FFLP3(int[] peak_in, int INT_in, double[] vf_in, int th_in) {
        int seg_s, seg_e;
        double BWR;
        int[][] pk = ma.getArrValleyPos(Tag.INT_PEAK, peak_in, (int) (INT_in * 0.05));
        int[][] pk2 = new int[pk.length][pk[0].length];
        int MPid = 1;

        if (pk[0].length < 2) {
            pk2 = pk;
        } else {
            double p_th = ma.getArrMean(pk[Tag.INT_POS_VAL], 0, pk[Tag.INT_POS_VAL].length - 1)
                    - ma.getArrMin(pk[Tag.INT_POS_VAL], 0, pk[Tag.INT_POS_VAL].length - 1);
            p_th = p_th / 2.0;
            int xi = 0;
            for (int ii = 0; ii < pk[0].length; ii++) {
                if (pk[Tag.INT_POS_VAL][ii] > p_th) {
                    pk2[Tag.INT_POS_IDX][xi] = pk[Tag.INT_POS_IDX][ii];
                    pk2[Tag.INT_POS_VAL][xi] = pk[Tag.INT_POS_VAL][ii];
                    xi++;
                }
            }

            pk2 = ma.getArrCopyOfRange(pk2, 0, pk2.length - 1, 0, xi - 1);

        }

        int dvN = (((pk2[0].length - 1) + 1) * (pk2[0].length - 1)) / 2;
        double[] dv = new double[dvN];
        int[] dvs = new int[dvN];
        int[] dve = new int[dvN];

        if (pk2[0].length < 2) {
            seg_s = 0;
            seg_e = 1;
            BWR = 1;
        } else {
            int dvn = 0;

            for (int ii = 0; ii < pk2[0].length - 1; ii++) {
                for (int jj = ii + 1; jj < pk2[0].length; jj++) {
                    // dv(dvn)=abs((PKX2(jj)-PKY2(jj)/2)-(PKX2(ii)-PKY2(ii)/2)-INT_in);

                    dv[dvn] = Math.abs(((double) (pk2[Tag.INT_POS_IDX][jj] - pk2[Tag.INT_POS_VAL][jj] / 2.0))
                            - ((double) (pk2[Tag.INT_POS_IDX][ii] - pk2[Tag.INT_POS_VAL][ii] / 2.0)) - (double) INT_in);
                    dvs[dvn] = ii;
                    dve[dvn] = jj;
                    dvn++;
                }
            }
            double[] min_v = ma.getArrMinPos(dv, 0, dv.length - 1);
            int si = dvs[(int) min_v[Tag.INT_POS_IDX]];
            int se = dve[(int) min_v[Tag.INT_POS_IDX]];
            int lgi = 0;
            int Spky = 0;
            int[] plg = new int[se - si + 1];
            for (int ti = si; ti < se; ti++) {
                plg[lgi] = pk2[Tag.INT_POS_IDX][ti + 1] - pk2[Tag.INT_POS_VAL][ti + 1] - pk2[Tag.INT_POS_IDX][ti];
                lgi = lgi + 1;
            }

            double th_max = th_in * ma.getArrMax(vf_in, 0, vf_in.length - 1) / 100;
            for (int ti = pk2[Tag.INT_POS_IDX][si] + (INT_in / 3); ti < pk2[Tag.INT_POS_IDX][se] - (INT_in / 2); ti++) {
                if (vf_in[ti] < th_max) {
                    Spky = Spky + 1;
                }

            }
            if (Spky > 5) {
                MPid = 0;
            }
            BWR = ma.getArrSum(plg, 0, plg.length - 1)
                    / (double) (pk2[Tag.INT_POS_IDX][se] - pk2[Tag.INT_POS_IDX][si]);
            seg_s = pk2[Tag.INT_POS_IDX][si] - (int) (pk2[Tag.INT_POS_VAL][si] / 2);
            seg_e = pk2[Tag.INT_POS_IDX][se] - (int) (pk2[Tag.INT_POS_VAL][se] / 2);

        }
        double[] db_o = new double[4];
        db_o[Tag.INT_CAL_VTI_START] = seg_s;
        db_o[Tag.INT_CAL_VTI_END] = seg_e;
        db_o[Tag.INT_CAL_VTI_BWR] = BWR;
        db_o[Tag.INT_CAL_VTI_MPID] = MPid;

        return db_o;
    }

    private void set_VelocityProfile(int[][] iImg_o, double[] vf_o, double[] ang_o, double[][] dImgIn, double bw_th) {

        int m = dImgIn.length;
        int n = dImgIn[0].length;
        // double bw_th = 80 * ma.getImgMean(dImgIn, 0, m - 1, 98, n-1);
        int[][] p_in;
        double[][] p_in2 = ma.getArrCopyOfRange(dImgIn, 0, m - 1, 0, n - 1);

        p_in = ma.calSpectrumBitImgMap(dImgIn, 0, m - 1, bw_th);
        for (int i = 0; i < p_in.length; i++) {
            for (int j = 0; j < p_in[0].length; j++) {
                iImg_o[i][j] = p_in[i][j];
            }
        }

        double[] i_x_im_ipsd = new double[n];
        double[] i_power = new double[n];
        double[] t_power = new double[m];
        double[] t_power2 = new double[m];
        for (int npx = 0; npx < m; npx++) {
            int[] sel_im_line = p_in[npx];
            double[] sel_im_line2 = p_in2[npx];
            for (int i = 0; i < n; i++) {
                i_x_im_ipsd[i] = ma.getArrSum(sel_im_line, 0, i);
                i_power[i] = ma.getArrSum(sel_im_line2, 0, i);
            }
            t_power[npx] = ma.getArrSum(sel_im_line, 0, n - 1);
            t_power2[npx] = ma.getArrSum(sel_im_line2, 0, n - 1);

            int npy = 0;
            // by Jaufa
//            while ((npy < n - 8)
//                    && ((((sel_im_line[npy + 1] == 1) || (sel_im_line[npy + 2] == 1) || (sel_im_line[npy + 3] == 1))
//                    && (ma.getArrSum(sel_im_line, npy, npy + 12) >= 3))
//                    || (i_x_im_ipsd[npy] / t_power[npx] < 0.8))) {
//                npy++;
//            }
            // Cavin test 20210826
            while ((npy < n - 8)
                    && ((((sel_im_line[npy + 1] == 1) || (sel_im_line[npy + 2] == 1) || (sel_im_line[npy + 3] == 1))
                    && (ma.getArrSum(sel_im_line, npy, npy + 12) >= 3))
                    || (i_x_im_ipsd[npy] / t_power[npx] < 0.9))) {
                npy++;
            }
            // Cavin test end 20210826

            vf_o[npx] = npy;

            int npv = 0;
            // Cavin test 20210826
//            while (npv < n && (i_power[npv] / t_power2[npx] < 0.9)) {
            while (npv < n && (i_power[npv] / t_power2[npx] < 0.95)) {
                // Cavin test 20210826
                npv = npv + 1;
            }

            int npa = 1;
            while (npa < n && (i_power[npa] / t_power[npx] < 0.5)) {
                npa = npa + 1;
            }

            if (vf_o[npx] < 20) {
                ang_o[npx] = 0;
            } else {
                ang_o[npx] = (float) (npv - npa) / (float) npa;
            }
        }

        double[] filR = fil.filterShoot(vf_o, 10, 16);
        ma.forArrCopy(vf_o, filR);
    }

    private void set_SpectrumStrength(double[][] dImgIn) {
        double max = ma.get_SpectrumStrengthRange(dImgIn, 0, 128, 0.90, 0.98);
        double min = ma.get_SpectrumStrengthRange(dImgIn, 96, 128, 0.02, 0.2);
        double meanNoise = ma.get_SpectrumStrengthRange(dImgIn, 96, 128, 0.1, 0.9);

        this.mDblSpectrumSignalStrength = max;
        this.mDblSpectrumWhiteNoiseStrength = min;
        this.mDblSpectrumMeanNoiseStrength = meanNoise;
        this.mDblSpectrumSignalWhiteNoiseRatio = max / min;
        this.mDblSpectrumSignalMeanNoiseRatio = max / meanNoise;

    }

    private void for_VelocityProfile(double[][] dImgIn, int iFakeMode, int iSingleMode) {
        // TODO Auto-generated method stub
        mDblArrAngleProfile = new double[dImgIn.length];
        mDblArrVelocityProfile = new double[dImgIn.length];
        mDblArrVelocityProfileGM = new double[dImgIn.length];
        mIntImgBitBVSpectrumCalculate = new int[dImgIn.length][dImgIn[0].length];

        double dTrigger;
        if (iSingleMode == 1) {
            double test = 0;
            for (int i = 0; i < mDblArrVelocityProfile.length; i++) {
                double[] gmVector = dImgIn[i];
                mDblArrVelocityProfile[i] = ma.getArrStrengthGM(gmVector, 2, 128)[Tag.mIntX];
                if (mDblArrVelocityProfile[i] > 10 && mDblArrVelocityProfile[i] < 104) {
                    mDblArrVelocityProfile[i] = ma.getArrStrengthGM(gmVector, (int) mDblArrVelocityProfile[i] - 2,
//                            (int) (mDblArrVelocityProfile[i] + 24))[Tag.mIntX];
                    		128)[Tag.mIntX];
                }
            }


        } else if (iFakeMode == 1) {
            dTrigger = mDblSpectrumWhiteNoiseStrength
                    + (0.25 * (mDblSpectrumSignalStrength - mDblSpectrumWhiteNoiseStrength));
            mIntImgBitBVSpectrumCalculate = new int[dImgIn.length][dImgIn[0].length];
            set_VelocityProfile(mIntImgBitBVSpectrumCalculate, mDblArrVelocityProfile, mDblArrAngleProfile, dImgIn,
                    dTrigger);
        } else {
            dTrigger = 60 * mDblSpectrumWhiteNoiseStrength;
            set_VelocityProfile(mIntImgBitBVSpectrumCalculate, mDblArrVelocityProfile, mDblArrAngleProfile, dImgIn,
                    dTrigger);

            for (int i = 0; i < mDblArrVelocityProfileGM.length; i++) {
                double[] gmVector = dImgIn[i];
                double fVpk = this.mDblArrSpectrumSignalNoiseRatio[i];
                if (fVpk > 32) {
                    mDblArrVelocityProfileGM[i] = ma.getArrStrengthGM(gmVector, 2, 128)[Tag.mIntX];
                    if (mDblArrVelocityProfileGM[i] > 10 && mDblArrVelocityProfileGM[i] < 104) {
                        mDblArrVelocityProfileGM[i] = ma.getArrStrengthGM(gmVector, (int) mDblArrVelocityProfileGM[i] - 2,
//                                (int) (mDblArrVelocityProfileGM[i] + 24))[Tag.mIntX];
                              128)[Tag.mIntX];
                    }
                }
            }
        }

    }

}
