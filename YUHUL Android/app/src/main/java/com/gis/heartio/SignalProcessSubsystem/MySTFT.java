package com.gis.heartio.SignalProcessSubsystem;

import com.google.corp.productivity.specialprojects.android.samples.fft.STFT;

/**
 * Created by 780797 on 2016/6/29.
 */
public class MySTFT extends STFT {
    double[] mSpectrumAmpOutArrayElement;

    public MySTFT (int fftlen, int sampleRate, int minFeedSize, String wndName){
        super(fftlen, sampleRate, minFeedSize, wndName);
        mSpectrumAmpOutArrayElement = new double[fftlen/2+1];
    }

    public double[] getSpectrumAmpOutArrayElement(int iIdx){
        int iVar, iLength;
        iLength = mSpectrumAmpOutArrayElement.length;

        for(iVar = 0 ; iVar < iLength ; iVar++){
            mSpectrumAmpOutArrayElement[iVar] = spectrumAmpOutArray[iIdx][iVar];
        }
        return mSpectrumAmpOutArrayElement;
    }
}
