package com.gis.heartio.SignalProcessSubsysII.processor;
import com.gis.heartio.SignalProcessSubsysII.utilities.Type;

import java.util.Arrays;

import com.gis.heartio.SignalProcessSubsysII.transformer.DiscreteCosineTransform;

import com.gis.heartio.SignalProcessSubsysII.parameters.BloodVelocityConfig;

/*
 * Example:
 		ShortTimeDCT stDCT = new ShortTimeDCT(
			systemConfig, systemConfig.mIntArrayUltrasoundData);
 */
public class ShortTimeDCT {
	BloodVelocityConfig sys;
	DiscreteCosineTransform DCT = new DiscreteCosineTransform();
	
	double[][] mDblSpectrumValues;
	
	public ShortTimeDCT(BloodVelocityConfig sysConfig, int[] iFeedData) {
		int iWndSize, iShiftSize, iSpectrumSize, iFeedSize;
		double[] fFeedData;

		sys = sysConfig;
		iWndSize = sys.mIntSTFTWindowSize;
		iShiftSize = sys.mIntSTFTWindowShiftSize;
		iFeedSize = iFeedData.length;
		iSpectrumSize = ((iFeedSize-iWndSize)/iShiftSize) + 1;
		fFeedData = Type.toDbl(iFeedData);
		
		sys.mIntSubSeqFreqCnt = iWndSize;
		sys.mIntSubSeqsTotalCnt = iSpectrumSize;

		
		mDblSpectrumValues = new double[iSpectrumSize][iWndSize];

		double[] f1Spec;
		int iY;
		for (int x=0; x<iSpectrumSize; x++) {
			iY = x*iShiftSize;
			f1Spec = DCT.fDCT(Arrays.copyOfRange(fFeedData, iY, iY + iWndSize),1);
			System.arraycopy(f1Spec, 0, mDblSpectrumValues[x], 0, iWndSize); 
		}
	}
	
	public double[][] getSpectrumAmpArrayAllElement() {
		int iXLen = mDblSpectrumValues.length;
		int iYLen = mDblSpectrumValues[0].length;
		double[][] fArr = new double[iXLen][iYLen];// = mDoubleSpectrumValues.clone();

		for (int x = 0; x < iXLen; x++) {
			for (int y = 0; y < iYLen; y++) {
				fArr[x][y] = mDblSpectrumValues[x][y];
			}
		}
		return fArr;
	}
	

}

