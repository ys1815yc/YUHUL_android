package com.gis.heartio.SupportSubsystem;

import com.gis.heartio.UIOperationControlSubsystem.MainActivity;

/**
 * Created by brandon on 2017/7/19.
 */

public class MyDataFilter2 {

    public int[] mIntFilterWindowData;
    public int  mIntFilterWindowNextIdx;
    public int  mIntFilterWindowDataCnt;

    //public static int INT_FILTER_PARAM_CNT = 6;

    public static double[] mDoubleFilterParam = {

            // 400-3200 8K
            0.03375827270909715,
            0.15264380272953587,
            0.290687673660621,
            0.290687673660621,
            0.15264380272953587,
            0.03375827270909715,

            // 400 - 3200 8K -80db
            /*0.01772997750111572,
            0.09621504027726112,
            0.22674466319401126,
            0.2964889394077751,
            0.22674466319401126,
            0.09621504027726112,
            0.01772997750111572,*/

            // 400 - 3400 8K -80db
            /*0.034474490312945694,
            0.16303682343790246,
            0.31723217254795016,
            0.31723217254795016,
            0.16303682343790246,
            0.034474490312945694,*/


            //300-3400 8K -80db
            /*0.032096719715791484,
            0.15179186632595545,
            0.29535207147868814,
            0.29535207147868814,
            0.15179186632595545,
            0.032096719715791484,*/

            //300-3400 8K -40
            /*0.12585577272683746,
            0.3567838866685229,
            0.3567838866685229,
            0.12585577272683746,*/

            // 500 - 3200 8K
            /*0.03488940012801084,
            0.15775838877729437,
            0.30042765060940024,
            0.30042765060940024,
            0.15775838877729437,
            0.03488940012801084,*/

            // 800-3200 8K
            /*-0.027170292594046606,
            -0.03336568589250602,
            0.14424977947759848,
            0.4157380515643767,
            0.4157380515643767,
            0.14424977947759848,
            -0.03336568589250602,
            -0.027170292594046606,*/

            //1000-3200 8K
            /*-0.016216272657839814,
            -0.002124878236940743,
            0.16656490136254165,
            0.40139705746543014,
            0.40139705746543014,
            0.16656490136254165,
            -0.002124878236940743,
            -0.016216272657839814,*/


            //700-3200 8K
            /*0.03682050979453883,
            0.17429198799598944,
            0.33804319883677003,
            0.33804319883677003,
            0.17429198799598944,
            0.03682050979453883,*/

            // 600-3000 8K
            /*0.039219953182652534,
            0.1673779022581945,
            0.31022448786881934,
            0.31022448786881934,
            0.1673779022581945,
            0.039219953182652534,*/

            // 200 - 3600 8K    X
            /*0.11563337719264401,
            0.33800657924843946,
            0.33800657924843946,
            0.11563337719264401,*/

            // 200 - 3800 8K
            /*0.12635905684433527,
            0.3759660875429849,
            0.3759660875429849,
            0.12635905684433527,*/

            //100 - 3200 8K
            /*0.03551609195537358,
            0.16059208303922154,
            0.30582400459256703,
            0.30582400459256703,
            0.16059208303922154,
            0.03551609195537358,*/


            //100-3800 8K low
            /*-0.0313237948777417,
            0.001010828448030125,
            0.2813237948777699,
            0.4979925365060681,
            0.2813237948777699,
            0.001010828448030125,
            -0.0313237948777417,*/


            // 750 - 3300 8K  X
            /*0.07335576070679468,
            0.27265960250708754,
            0.3992044285079551,
            0.27265960250708754,
            0.07335576070679468,*/

            // 750 - 3000  8K   X
            /*     -0.01885899740224828,
                 -0.04086113423742789,
                 0.05230879732907964,
                 0.2891821579898248,
                 0.42970867317973216,
                 0.2891821579898248,
                 0.05230879732907964,
                 -0.04086113423742789,
                 -0.01885899740224828,*/

            //800-3000 8K
            /*-0.011315084893688909,
            -0.014341578132018671,
            0.08034060848452812,
            0.2819709363469294,
            0.396813338068884,
            0.2819709363469294,
            0.08034060848452812,
            -0.014341578132018671,
            -0.011315084893688909,*/

            // 800-3800 8K
            /*0.1352645458240812,
            0.40246345629882097,
            0.40246345629882097,
            0.1352645458240812,*/


            //1000-2500 8K
            /*-0.014027428506767912,
            -0.04251334700092245,
            -0.029032765313177167,
            0.09658523187352905,
            0.29043651841969487,
            0.3876000511658356,
            0.29043651841969487,
            0.09658523187352905,
            -0.029032765313177167,
            -0.04251334700092245,
            -0.014027428506767912,*/
            //200-3000 8K
            /*0.03474980889574114,
            0.14830079194013154,
            0.2748662553985092,
            0.2748662553985092,
            0.14830079194013154,
            0.03474980889574114,*/

    };
    public static int INT_FILTER_PARAM_CNT = mDoubleFilterParam.length;
    public MyDataFilter2(){
        int iVar;

        mIntFilterWindowData = new int[INT_FILTER_PARAM_CNT];
        /*mDoubleFilterParam = new double[INT_FILTER_PARAM_CNT];
        mDoubleFilterParam[0] = 0.0386;
        mDoubleFilterParam[1] = -0.1434;
        mDoubleFilterParam[2] = 0.6106;
        mDoubleFilterParam[3] = 0.6106;
        mDoubleFilterParam[4] = -0.1434;
        mDoubleFilterParam[5] = 0.0386;*/

    }

    public void prepareStart(){
        mIntFilterWindowNextIdx = 0;
        mIntFilterWindowDataCnt = 0;
    }
    private int n=0;
    private double[] x = new double[INT_FILTER_PARAM_CNT];

    /*public int getDataAfterFilter(){
        int iVar, iDataIdx, iRest, iTarget;
        double doubleDataAfterFilter;

        doubleDataAfterFilter = 0;
        iDataIdx = mIntFilterWindowNextIdx +1;
        if(iDataIdx == INT_FILTER_PARAM_CNT){
            iDataIdx = 0;
        }
        for(iVar = 0 ; iVar < INT_FILTER_PARAM_CNT-1 ; iVar++){
            doubleDataAfterFilter = doubleDataAfterFilter + mDoubleFilterParam[iVar] * (double)mIntFilterWindowData[iDataIdx];
            iDataIdx++;
            if(iDataIdx == INT_FILTER_PARAM_CNT) {
                iDataIdx = 0;
            }
        }
        doubleDataAfterFilter = doubleDataAfterFilter + mDoubleFilterParam[INT_FILTER_PARAM_CNT-1] ;
        iTarget = ((int)(doubleDataAfterFilter *10)) / 10;
        iRest = ((int)(doubleDataAfterFilter *10)) % 10;
        if(iRest >= 5){iTarget++;}
        return iTarget;*/
    public double getDataAfterFilter(double x_in){
        double y = 0.0;

        // Store the current input, overwriting the oldest input
        x[n] = x_in;

        // Multiply the filter coefficients by the previous inputs and sum
        for (int i=0; i<INT_FILTER_PARAM_CNT; i++)
        {
            y += mDoubleFilterParam[i] * x[((INT_FILTER_PARAM_CNT - i) + n) % INT_FILTER_PARAM_CNT];
        }

        // Increment the input buffer index to the next location
        n = (n + 1) % INT_FILTER_PARAM_CNT;

        return y;
    }

    public void filterProcessForData(int iDataNextIndex){
        mIntFilterWindowData[mIntFilterWindowNextIdx] = MainActivity.mRawDataProcessor.mShortUltrasoundDataBeforeFilter[iDataNextIndex];
        if (mIntFilterWindowDataCnt < INT_FILTER_PARAM_CNT) {
            mIntFilterWindowDataCnt++;
        }
        if (mIntFilterWindowDataCnt == INT_FILTER_PARAM_CNT) {
            MainActivity.mRawDataProcessor.mShortUltrasoundData[iDataNextIndex] = (short)getDataAfterFilter(MainActivity.mRawDataProcessor.mShortUltrasoundDataBeforeFilter[iDataNextIndex]); /*getDataAfterFilter();*/
        }
        mIntFilterWindowNextIdx ++;
        if(mIntFilterWindowNextIdx == INT_FILTER_PARAM_CNT){
            mIntFilterWindowNextIdx = 0;
        }
    }

    public void filterProcessForAudio(int iDataNextIndex){
        mIntFilterWindowData[mIntFilterWindowNextIdx] = MainActivity.mAudioPlayerController.mAudioPlayer.mShortArrayAudioSegment[iDataNextIndex];
        if (mIntFilterWindowDataCnt < INT_FILTER_PARAM_CNT) {
            mIntFilterWindowDataCnt++;
        }
        if (mIntFilterWindowDataCnt == INT_FILTER_PARAM_CNT) {
            MainActivity.mAudioPlayerController.mAudioPlayer.mShortArrayAudioSegment[iDataNextIndex] = (short) getDataAfterFilter(MainActivity.mAudioPlayerController.mAudioPlayer.mShortArrayAudioSegment[iDataNextIndex]);/*getDataAfterFilter();*/
        }
        mIntFilterWindowNextIdx ++;
        if(mIntFilterWindowNextIdx == INT_FILTER_PARAM_CNT){
            mIntFilterWindowNextIdx = 0;
        }
    }
}
