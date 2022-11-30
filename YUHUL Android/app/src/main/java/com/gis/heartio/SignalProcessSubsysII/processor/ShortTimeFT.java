package com.gis.heartio.SignalProcessSubsysII.processor;

import java.util.Arrays;

//import com.gis.heartio.SignalProcessSubsysII.transformer.RealDoubleFFT;
import com.google.corp.productivity.specialprojects.android.fft.RealDoubleFFT;

import com.gis.heartio.SignalProcessSubsysII.parameters.BloodVelocityConfig;
import com.gis.heartio.SignalProcessSubsysII.utilities.Tag;
import com.gis.heartio.SignalProcessSubsysII.transformer.EasyFilter;

public class ShortTimeFT {
	RealDoubleFFT stFT;
	BloodVelocityConfig sys;
	EasyFilter fil = new EasyFilter();
	
	private double[] mDblFeedData;
	private int mIntSubSeqSize;
	private int mIntNextSubSeqShiftSize;
	private int mIntSubSeqFreqCnt;
	private int mIntSubSeqsTotalCnt;
	private int mIntOffset;
	private double[] mDoublesSTTOneTime;
	private double[][] mDoubleSpectrumValues;

	public ShortTimeFT(BloodVelocityConfig sysConfig, double[] intFeedData) {
		sys = sysConfig;
		mDblFeedData = Arrays.copyOf(intFeedData, intFeedData.length);
		mIntSubSeqSize = sys.mIntSTFTWindowSize;
		mIntNextSubSeqShiftSize = sys.mIntSTFTWindowShiftSize;
		mIntSubSeqFreqCnt = mIntSubSeqSize / 2 + 1;
		// mIntSubSeqFreqCnt = mIntSubSeqSize;
		sys.mIntSubSeqFreqCnt = mIntSubSeqFreqCnt;
		mIntSubSeqsTotalCnt = (int) ((intFeedData.length - mIntSubSeqSize) / mIntNextSubSeqShiftSize) + 1;
		sys.mIntSubSeqsTotalCnt = mIntSubSeqsTotalCnt;

		mIntOffset = 0;

		mDoublesSTTOneTime = new double[mIntSubSeqFreqCnt];
		mDoubleSpectrumValues = new double[mIntSubSeqsTotalCnt][mIntSubSeqFreqCnt];

		// --- Begin shortTimeFT ----------
		stFT = new RealDoubleFFT(mIntSubSeqSize);

		for (int iVar = 0; iVar < mIntSubSeqsTotalCnt; iVar++) {
			int iInitialIdx = iVar * mIntNextSubSeqShiftSize;

			double[] src, dest;
			src = mDblFeedData;
			dest = new double[mIntSubSeqSize];
			System.arraycopy(src, iInitialIdx, dest, 0, dest.length);

			mDoublesSTTOneTime = ft(dest);

			System.arraycopy(mDoublesSTTOneTime, 0, mDoubleSpectrumValues[iVar], 0, mDoubleSpectrumValues[iVar].length);
		}

	}

	public ShortTimeFT(double[] intFeedData, int intSubSeqSize, int intNextSubSeqShiftSize) {
		mDblFeedData = Arrays.copyOf(intFeedData, intFeedData.length);
		mIntSubSeqSize = intSubSeqSize;
		mIntNextSubSeqShiftSize = intNextSubSeqShiftSize;
		mIntSubSeqFreqCnt = intSubSeqSize / 2 + 1;
		mIntSubSeqsTotalCnt = (int) ((intFeedData.length - intSubSeqSize) / intNextSubSeqShiftSize) + 1;
		mIntOffset = 0;
		double dblEnergySum = 0;

		mDoublesSTTOneTime = new double[mIntSubSeqFreqCnt];
		mDoubleSpectrumValues = new double[mIntSubSeqsTotalCnt][mIntSubSeqFreqCnt];

		// --- Begin shortTimeFT ----------
		stFT = new RealDoubleFFT(mIntSubSeqSize);

		for (int iVar = 0; iVar < mIntSubSeqsTotalCnt; iVar++) {
			int iInitialIdx = iVar * mIntNextSubSeqShiftSize;

			double[] src, dest;
			src = mDblFeedData;
			dest = new double[mIntSubSeqSize];
			System.arraycopy(src, iInitialIdx, dest, 0, dest.length);

			mDoublesSTTOneTime = ft(dest);

			System.arraycopy(mDoublesSTTOneTime, 0, mDoubleSpectrumValues[iVar], 0, mDoubleSpectrumValues[iVar].length);
		}
		
		for (int x = 0; x < mDoubleSpectrumValues.length; x++) {
			for (int y = 0; x < mDoubleSpectrumValues[0].length; y++) {
				dblEnergySum += mDoubleSpectrumValues[x][y];
			}
			dblEnergySum = dblEnergySum / (mDoubleSpectrumValues.length * mDoubleSpectrumValues[0].length);
		}

		for (int x = 0; x < mDoubleSpectrumValues.length; x++) {
			for (int y = 0; x < mDoubleSpectrumValues[0].length; y++) {
				mDoubleSpectrumValues[x][y] /= dblEnergySum;
			}
		}

	}

	public double[] getSpectrumAmpArrayOneElement(int iIdx) {
		return Arrays.copyOf(mDoubleSpectrumValues[iIdx], mDoubleSpectrumValues[iIdx].length);
	}

	public double[][] getSpectrumAmpArrayAllElement() {
		int iXLen = mDoubleSpectrumValues.length;
		int iYLen = mDoubleSpectrumValues[0].length;
		double[][] dssArr = new double[iXLen][iYLen];// = mDoubleSpectrumValues.clone();

		for (int x = 0; x < iXLen; x++) {
			for (int y = 0; y < iYLen; y++) {
				dssArr[x][y] = mDoubleSpectrumValues[x][y];
			}
		}
		return dssArr;
	}

	public double[][] getSpectrumAmpArrayAllElementFilter(int iTagDirect) {
		int iXLen = mDoubleSpectrumValues.length;
		int iYLen = mDoubleSpectrumValues[0].length;
		double[][] dssArr = new double[iXLen][iYLen];// = mDoubleSpectrumValues.clone();
		/*vertical
		 */
		if (iTagDirect == Tag.INT_SPEC_FIL_VERTICAL) {
			dssArr = new double[iXLen][iYLen];// = mDoubleSpectrumValues.clone();
			double[] dsFirWnd = new double[] {0.2, 0.2, 0.2, 0.2, 0.2};
			for (int x = 0; x < iXLen; x++) {
				dssArr[x] =  fil.filterFIR(dsFirWnd, mDoubleSpectrumValues[x]);
			}
		}else if(iTagDirect == Tag.INT_SPEC_FIL_HORIZONTAL) {
		//*/
		//* horizontal
			double[][] dssArrT= new double[iYLen][iXLen];// = mDoubleSpectrumValues.clone();
			for (int x = 0; x < iXLen; x++) {
				for (int y=0; y<iYLen; y++) {
					dssArrT[y][x] =  mDoubleSpectrumValues[x][y];
				}
			}
			
			for (int x = 0; x < iYLen; x++) {
				dssArrT[x] =  fil.filterFIR(fil.mDblArrLPFS125FP8E16, dssArrT[x]);
			}
			dssArr = new double[iXLen][iYLen];// = mDoubleSpectrumValues.clone();
			for (int x = 0; x < iXLen; x++) {
				for (int y=0; y<iYLen; y++) {
					dssArr[x][y] =  dssArrT[y][x];
				}
			}
		}
		//*/
		return dssArr;
	}

	private double[] getReWnd(int iLen, double fCure) {
		double[] reWnd = new double[iLen];
		// fCure Range: 1 ~ 100
		fCure = 2 * (fCure - 50.0) / 50.0; // -0.02 ~ +2
		for (int x = 1; x <= iLen; x++) {
			double fV = (double) x / (double) (iLen);
			fV = Math.pow(fV, fCure);
			reWnd[x - 1] = fV;
		}
		return reWnd;

	}

	public double[][] getSpectrumAmpArrayAllElementReForm(double fCure) {
		int iXLen = mDoubleSpectrumValues.length;
		int iYLen = mDoubleSpectrumValues[0].length;
		double[] fWnd = getReWnd(iYLen, fCure);
		double[][] fArr = new double[iXLen][iYLen];// = mDoubleSpectrumValues.clone();

		for (int x = 0; x < iXLen; x++) {
			for (int y = 0; y < iYLen; y++) {
				fArr[x][y] = mDoubleSpectrumValues[x][y];
				fArr[x][y] = fArr[x][y] * fWnd[y];
			}
		}
		return fArr;
	}

	private double[] ft(double[] iFeedData) {
		double[] fIns = new double[iFeedData.length];
		for (int iVar = 0; iVar < iFeedData.length; iVar++) {
			fIns[iVar] = (double) iFeedData[iVar] / 32768.0;
		}
		stFT.ft(fIns);
		return fftToAmp(fIns);
	}

	private double[] ft(int[] iFeedData) {
		double[] fIns = new double[iFeedData.length];
		for (int iVar = 0; iVar < iFeedData.length; iVar++) {
			fIns[iVar] = (double) iFeedData[iVar] / 32768.0;
		}
		stFT.ft(fIns);
		return fftToAmp(fIns);
	}

	private double[] fftToAmp(double[] fIns) {
		// data.length should be even number
		double[] fOuts = new double[fIns.length];
		double scalar = 2.0 * 2.0 / (fIns.length * fIns.length); // *2 since there are positive and negative frequency
																	// part
		fOuts[0] = fIns[0] * fIns[0] * scalar / 4.0;
		int j = 1;
		for (int i = 1; i < fIns.length - 1; i += 2, j++) {
			fOuts[j] = (fIns[i] * fIns[i] + fIns[i + 1] * fIns[i + 1]) * scalar;
		}
		fOuts[j] = fIns[fIns.length - 1] * fIns[fIns.length - 1] * scalar / 4.0;
		return fOuts;
	}

}
