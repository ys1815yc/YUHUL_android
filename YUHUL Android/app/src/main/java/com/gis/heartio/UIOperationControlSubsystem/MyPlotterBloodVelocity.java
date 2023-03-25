package com.gis.heartio.UIOperationControlSubsystem;
/**
 * Created by 780797 on 2016/6/28.
 */
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
//import androidx.core.app.Fragment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.gis.CommonUtils.Constants;
import com.gis.heartio.GIS_Log;
import com.gis.heartio.GIS_SystemConfig;
import com.gis.heartio.SignalProcessSubsystem.RawDataProcessor;
import com.gis.heartio.SignalProcessSubsystem.ecgResult;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.SystemConfig;

import java.util.Arrays;
import java.util.List;

/**
 * Created by brandon on 2017/10/31.
 */

public class MyPlotterBloodVelocity extends MyPlotter {
        private final static String TAG = "MPBV";
        private Paint mPaintVelocity = null;
        private Paint mPaintSumAmp = null;
        private Paint mPaintMaxIdx = null;
        private Paint mPaintMaxIdx_HR = null;
        private Paint mPaintMaxIdx_VPK = null;
        private Paint mPaintMaxIdx_PERIOD = null;
        private Paint mPaintMaxIdxForVTI = null;
        private Paint mPaintVpkPeakS1 = null;
        private Paint mPaintVpkPeakS2 = null;
        private Paint mPaintVpkBottomS1 = null;
        private Paint mPaintVpkBottomS2 = null;
        private Paint mPaintPhantomPeak = null;
        //private Paint[] mPaintArrayCandiVpk;
        private Paint mPaintVTIMaxIdxNotUsable = null;
        private Paint mPaintVTIMaxIdxUsable = null;
        private Paint mPaintHRPeriodDiscarded = null;
        private Paint mPaintHRPeriodNotDiscarded = null;
        //private Paint mPaintCandiPeak = null;
        private Paint mPaintHRPeak = null;
        private Paint mPaintHRPeakHalf = null;
        private Paint mPaintHRBottomS1 = null;
        private Paint mPaintHRBottomS2 = null;
        private Paint mPaintVTILow = null;
        private Paint mPaintVTIStart = null;
        private Paint mPaintVTIEnd = null;

        private Paint mPaintVTIStartGIS = null;
        private Paint mPaintVTIEndGIS = null;

        private Paint mPaintAvgFreqMax = null;
        private Paint mPaintMulHRMinLow = null;
        private Paint mPaintHRStart = null;
        private int[][] mIntArrayDrawColorsGrayMode;
        private static final int DRAW_COLOR_SIZE = 256;

        private SurfaceHolder mSurfaceHolderBVOnLine;
        private SurfaceHolder mSurfaceHolderBVOffLine;
        private SurfaceHolder mSurfaceHolderTimeScaleDown,mSurfaceHolderTimeScaleUp;
        public SurfaceHolder mSurfaceHolderScale;
        private RawDataProcessor mRawDataProcessor;
        private SurfaceView mSurfaceViewBV, mSurfaceViewTimeScaleDown, mSurfaceViewTimeScaleUp;
        private SurfaceView mSurfaceViewScale;

        private Canvas mCanvasBVOffLine;

        private double[] mDoubleColorSignalAmps;

        private int mIntSViewDrawOnlineNextSubSegIdx, mIntSViewDrawOnlineNextViewIdx;
        //private int mIntSignalMappingLearnSubSegsOnLine;
        private boolean mBoolColorAndSignalMappedSetted;
        //private boolean mBoolColorAndSignalMappedOnLine;

        public float mFloatSurfaceViewWidth, mFloatSurfaceViewHeight, mFloatXGainDrawSizeToPointsRatio;
        public int mIntDrawSubSegStart, mIntDrawSubSegEnd, mIntSubSegRedrawStart, mIntSubSegRedrawEnd;
        public float mFloatBasicGainX, mFloatBasicGainY;

        private int mIntSvDrawMaxFreqCnt, mIntSvDrawTotalFreqCnt;
        private double mDoubleSvDrawScaleMax;

        private int mIntTimeScaleLineStrokeWidth;

        public float mFloatTimeScalePoint;

        private Bitmap mBitMapCanvasBV;

        private double mDoubleDrawStepRatio;

        private onlineFragment oFrag = null;
        private offlineFragment offFrag = null;
        private boolean needRedrawBVCanvas = false;
        private int redrawStartIdx, redrawEndIdx;

        private EcgSegView[] ecgSegViews = null;


        public MyPlotterBloodVelocity(SurfaceView inputSV,onlineFragment inputFragment, SurfaceView inputScaleSV) {

            mIntTimeScaleLineStrokeWidth = 10;

            mRawDataProcessor = MainActivity.mRawDataProcessor;
            //SystemConfig.mRawDataProcessor;
            //mSurfaceViewBV = SystemConfig.mFragment.mSurfaceViewUltrasound;
            this.mSurfaceViewBV = inputSV;
            this.mSurfaceViewScale = inputScaleSV;
            this.oFrag = inputFragment;
            mSurfaceViewTimeScaleDown = oFrag.mSurfaceViewTimeScaleDown;
            mSurfaceViewTimeScaleUp = oFrag.mSurfaceViewTimeScaleUp;
            initPaintsAndColor();
            GIS_Log.d("test", "online");
            drawFreqScale(this.mSurfaceViewScale);
            initSurfaceHolderOffline();
            clearCanvasByHolder(mSurfaceHolderBVOffLine);
        }

        public MyPlotterBloodVelocity(SurfaceView inputSV, offlineFragment inputFragment,SurfaceView inputScaleSV) {

            mIntTimeScaleLineStrokeWidth = 10;

            mRawDataProcessor = MainActivity.mRawDataProcessor;
            //SystemConfig.mRawDataProcessor;
            //mSurfaceViewBV = SystemConfig.mFragment.mSurfaceViewUltrasound;
            this.mSurfaceViewBV = inputSV;
            this.mSurfaceViewScale = inputScaleSV;
            this.offFrag = inputFragment;
            mSurfaceViewTimeScaleDown = offFrag.mSurfaceViewTimeScaleDown;
            mSurfaceViewTimeScaleUp = offFrag.mSurfaceViewTimeScaleUp;
            initPaintsAndColor();
            GIS_Log.d("test", "offline");
            drawFreqScale(this.mSurfaceViewScale);
            initSurfaceHolderOffline();
            clearCanvasByHolder(mSurfaceHolderBVOffLine);
        }

        public void setEcgSegViews(EcgSegView[] segViews){
            this.ecgSegViews = segViews;
        }

        private void initPaintsAndColor(){
            //try {
                mIntArrayDrawColorsGrayMode = new int[DRAW_COLOR_SIZE][3];
                mDoubleColorSignalAmps = new double[DRAW_COLOR_SIZE];

                mPaintVTIMaxIdxUsable = new Paint();
                mPaintVTIMaxIdxUsable.setColor(Color.BLUE);
                mPaintVTIMaxIdxUsable.setStrokeWidth((float) 10.0);

                mPaintVTIMaxIdxNotUsable = new Paint();
                mPaintVTIMaxIdxNotUsable.setColor(Color.RED);
                mPaintVTIMaxIdxNotUsable.setStrokeWidth((float) 10.0);

                mPaintHRPeriodNotDiscarded = new Paint();
                mPaintHRPeriodNotDiscarded.setColor(Color.GREEN);
                mPaintHRPeriodNotDiscarded.setStrokeWidth((float) 10.0);

                mPaintHRPeriodDiscarded = new Paint();
                //mPaintHRPeriodDiscarded.setColor(Color.RED);
                mPaintHRPeriodDiscarded.setColor(Color.LTGRAY);
                mPaintHRPeriodDiscarded.setStrokeWidth((float) 10.0);

                mPaintVelocity = new Paint();
                mPaintVelocity.setColor(Color.BLUE);
                mPaintVelocity.setStrokeWidth((float) 5.0);

                mPaintSumAmp = new Paint();
                mPaintSumAmp.setColor(Color.GRAY);
                mPaintSumAmp.setStrokeWidth((float) 5.0);

                mPaintMaxIdx = new Paint();
                mPaintMaxIdx.setColor(Color.RED);
                mPaintMaxIdx.setStrokeWidth((float) 5.0);
//jaufa + JAUFA.COLOR
                mPaintMaxIdx_HR = new Paint();
                mPaintMaxIdx_HR.setColor(Color.TRANSPARENT); //Color.YELLOW
                mPaintMaxIdx_HR.setStrokeWidth((float) 5.0);
                mPaintMaxIdx_PERIOD = new Paint();
                mPaintMaxIdx_PERIOD.setColor(Color.TRANSPARENT);
                mPaintMaxIdx_PERIOD.setStrokeWidth((float) 5.0);
                mPaintMaxIdx_VPK = new Paint();
                mPaintMaxIdx_VPK.setColor(Color.TRANSPARENT);
                mPaintMaxIdx_VPK.setStrokeWidth((float) 5.0);




                mPaintMaxIdxForVTI = new Paint();
                mPaintMaxIdxForVTI.setColor(Color.YELLOW);
                mPaintMaxIdxForVTI.setStrokeWidth((float) 10.0);

                mPaintVpkPeakS1 = new Paint();
                mPaintVpkPeakS1.setColor(Color.GREEN);
                mPaintVpkPeakS1.setStrokeWidth((float) 30.0);

                mPaintVpkPeakS2 = new Paint();
                mPaintVpkPeakS2.setColor(Color.TRANSPARENT);//Leslie setColor(Color.RED);
                mPaintVpkPeakS2.setStrokeWidth((float) 30.0);

                mPaintVpkBottomS1 = new Paint();
                mPaintVpkBottomS1.setColor(Color.GREEN);
                mPaintVpkBottomS1.setStrokeWidth((float) 30.0);

                mPaintVpkBottomS2 = new Paint();
                mPaintVpkBottomS2.setColor(Color.YELLOW);
                mPaintVpkBottomS2.setStrokeWidth((float) 30.0);

                mPaintPhantomPeak = new Paint();
                mPaintPhantomPeak.setColor(Color.CYAN);
                mPaintPhantomPeak.setStrokeWidth((float) 30.0);

                mPaintHRPeak = new Paint();
                mPaintHRPeak.setColor(Color.RED);
                mPaintHRPeak.setStrokeWidth((float) 30.0);
                mPaintHRPeakHalf = new Paint();
                mPaintHRPeakHalf.setColor(Color.RED);
                mPaintHRPeakHalf.setStrokeWidth((float) 30.0);
                mPaintHRBottomS1 = new Paint();
                mPaintHRBottomS1.setColor(Color.GREEN);
                mPaintHRBottomS1.setStrokeWidth((float) 30.0);
                mPaintHRBottomS2 = new Paint();
                mPaintHRBottomS2.setColor(Color.RED);
                mPaintHRBottomS2.setStrokeWidth((float) 30.0);

                mPaintVTILow = new Paint();
                mPaintVTILow.setColor(Color.GREEN);
                mPaintVTILow.setStrokeWidth((float) 20.0);
                mPaintVTIStart = new Paint();
                mPaintVTIStart.setColor(Color.RED);
                mPaintVTIStart.setStrokeWidth((float) 20.0);
                mPaintVTIEnd = new Paint();
                mPaintVTIEnd.setColor(Color.BLUE);
                mPaintVTIEnd.setStrokeWidth((float) 20.0);

                mPaintVTIStartGIS = new Paint();
                mPaintVTIEndGIS = new Paint();
                if(GIS_SystemConfig.isVisible){
                    mPaintVTIStartGIS.setColor(Color.RED);
                    mPaintVTIEndGIS.setColor(Color.RED);
                } else{
                    mPaintVTIStartGIS.setColor(Color.TRANSPARENT);
                    mPaintVTIEndGIS.setColor(Color.TRANSPARENT);
                }
                mPaintVTIStartGIS.setStrokeWidth((float) 5.0);
                mPaintVTIEndGIS.setStrokeWidth((float) 5.0);
                //mPaintVTIStartGIS.setPathEffect(new DashPathEffect(new float[] {15, 15}, 0));
                //mPaintVTIEndGIS.setPathEffect(new DashPathEffect(new float[] {15, 15}, 0));

                mPaintMulHRMinLow = new Paint();
                mPaintMulHRMinLow.setColor(Color.GRAY);
                mPaintMulHRMinLow.setStrokeWidth((float) 20.0);

                mPaintHRStart = new Paint();
                mPaintHRStart.setColor(Color.BLUE);
                mPaintHRStart.setStrokeWidth((float) 20.0);

            // Cavin added for reduce color preare
            prepareColorForGrayMode();

            //}catch(Exception ex1){
            //    Log.i("Application Error", "In BloodVelocityPlotter()");
            //}
        }

        public void prepareStartForSvDraw(){
            double doubleMaxScaleBase10, doubleScaleGapBase10, doubleMaxScale10, doubleSvDrawMaxFreqCnt;

            try {
                doubleMaxScaleBase10 = (4000.0 * 1540.0) / (2.0 * SystemConfig.mDoubleSensorWaveFreq) * 10; //doubleScaleAdjust * 10.0;
                doubleScaleGapBase10 = doubleMaxScaleBase10 / (double)(MainActivity.mBVSignalProcessorPart1.mIntTotalFreqSeqsCnt-1);

                //doubleMaxScale10 = (SystemConfig.mIntBVSvMaxScaleIdx + 1) * 0.1 * 10;
                doubleMaxScale10 = SystemConfig.mDoubleBVSvMaxScaleForReal * 10.0;
                doubleSvDrawMaxFreqCnt = (int)(doubleMaxScale10 / doubleScaleGapBase10) +1;
                mIntSvDrawMaxFreqCnt = (int)Math.ceil(doubleSvDrawMaxFreqCnt);
                /*mIntSvDrawMaxFreqCnt = (int)(doubleSvDrawMaxFreqCnt / 1);
                if (((doubleSvDrawMaxFreqCnt * 10) % 10) != 0) {
                    mIntSvDrawMaxFreqCnt++;
                }*/
                mDoubleSvDrawScaleMax = (mIntSvDrawMaxFreqCnt-1) * doubleScaleGapBase10 / (double) 10;

                mIntSvDrawTotalFreqCnt = Math.min(mIntSvDrawMaxFreqCnt, MainActivity.mBVSignalProcessorPart1.mIntTotalFreqSeqsCnt);
            }catch(Exception ex1){
                ex1.printStackTrace();
                //SystemConfig.mMyEventLogger.appendDebugStr("prepareStartForSvDraw.Exception","");
                //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            }
        }

        private void initSurfaceHolderOffline(){
            mSurfaceHolderBVOffLine = mSurfaceViewBV.getHolder();
            mSurfaceHolderBVOffLine.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
//                    Log.d(TAG,"mSurfaceHolderBVOffLine surfaceCreated!!!!!!!!");
                    clearCanvasByHolder(mSurfaceHolderBVOffLine);
                    if (needRedrawBVCanvas){
                        drawBloodVelocityByStartEndSubSegIdxOffLine(redrawStartIdx, redrawEndIdx, false);
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                    //Log.d(TAG,"mSurfaceHolderBVOffLine surfaceChanged!!!!!!");

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

                }
            });

        }

        public void prepareStartOffLine() {

            mBoolColorAndSignalMappedSetted = false;

            prepareStartForSvDraw();

            drawFreqScaleByHolder(mSurfaceHolderScale);

        }

        public void prepareStartOnLine() {
            mIntSViewDrawOnlineNextViewIdx = 0;

            //mIntSignalMappingLearnSubSegsOnLine = SystemConfig.mIntUltrasoundSamplerate / SystemConfig.mIntSTFTWindowShiftSize;
            //mIntSignalMappingLearnSubSegsOnLine = SystemConfig.mIntStartIdxNoiseLearn;
            //mIntSViewDrawOnlineNextSubSegIdx = mIntSignalMappingLearnSubSegsOnLine + 2 * SystemConfig.INT_SVIEW_ONLINE_DRAW_SUBSEG_SIZE;
            mIntSViewDrawOnlineNextSubSegIdx = SystemConfig.mIntEndIdxNoiseLearn;
            mBoolColorAndSignalMappedSetted = false;

            prepareStartForSvDraw();

            drawFreqScaleByHolder(mSurfaceHolderScale);
            SystemConfig.mIntSVDrawSizeBloodVelocity = SystemConfig.INT_SV_DRAW_SIZE_DEFAULT_BLOOD_VELOCITY;
        }

        public void DrawLinesBySegmentOnLine() {
            int iCurSubSegSize;

            iCurSubSegSize = MainActivity.mBVSignalProcessorPart1.getCurSubSegSize();
//            Log.d("MPBV","iCurSubSegSize = "+iCurSubSegSize);
            drawByEndSubSegIdxOnLine(iCurSubSegSize - 1);
        }

        public void drawByEndSubSegIdxOnLine(int iTryEndSubSegIdxParam) {
            if (oFrag == null){
                return;
            }
            int iTryStartSubSegIdx, iTryEndSubSegIdx;
            float fPosX, fPosY1, fPosY2;
            float fSurfaceViewWidth, fSurfaceViewHeight;
            int iTryXVar, iXVar, iYVar;
            double doubleAmpVal;
            int iDrawSubSegSize;
            double[][] doubleSignalToShow;
            boolean boolDrawPoint;

            if (!mBoolColorAndSignalMappedSetted) {
                if(SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_0_SNSI_GM){
//                    Log.e("MPBV", "mIntEndIdxNoiseLearn = "+SystemConfig.mIntEndIdxNoiseLearn);
                    if (iTryEndSubSegIdxParam >= SystemConfig.mIntEndIdxNoiseLearn) {
                        if (SystemConfig.mDoubleNoiseRangeWu != 0) {
                            prepareColorAndSignalMappingWuMode();
                            //SystemConfig.mMyEventLogger.appendDebugStr("SignalMappingWuMode().exec", "");
                            mBoolColorAndSignalMappedSetted = true;
                        }
                    }
                }else if(SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_1_WU_NEW) {
                    if (iTryEndSubSegIdxParam >= SystemConfig.mIntEndIdxNoiseLearn) {
                        prepareColorAndSignalMappingWuNewMode();
                        mBoolColorAndSignalMappedSetted = true;
                    }
                }else{   // SNR Mode
                    if (iTryEndSubSegIdxParam >= SystemConfig.mIntEndIdxNoiseLearn) {
                        prepareColorAndSignalMappingHuangMode();
                        mBoolColorAndSignalMappedSetted = true;
                    }
                    //Log.d(TAG,"(!mBoolColorAndSignalMappedAutoMode) && (SystemConfig.mEnumTryState == SystemConfig.ENUM_TRY_STATE.STATE_TRY)");
                }
                return;
            }


            if (iTryEndSubSegIdxParam < mIntSViewDrawOnlineNextSubSegIdx) {
                return;
            }

            if (SystemConfig.isHeartIO2){
                ecgSegViews[mIntSViewDrawOnlineNextViewIdx].isRunning = true;
                ecgSegViews[mIntSViewDrawOnlineNextViewIdx].mViewDrawOnlineNextSubSegIdx = mIntSViewDrawOnlineNextSubSegIdx;
                ecgSegViews[mIntSViewDrawOnlineNextViewIdx].inputTryEndSubSegIdx = iTryEndSubSegIdxParam;
                ecgSegViews[mIntSViewDrawOnlineNextViewIdx].invalidate();
            }

            doubleSignalToShow = MainActivity.mBVSignalProcessorPart1.mDoubleBVSpectrumValues;
            //Log.d(TAG,"doubleSignalToShow="+doubleSignalToShow);
            //SystemConfig.mMyEventLogger.appendDebugIntEvent("ViewIdx=", mIntSViewDrawOnlineNextViewIdx,0,0,0,0);
            iTryEndSubSegIdx = mIntSViewDrawOnlineNextSubSegIdx;
            iTryStartSubSegIdx = mIntSViewDrawOnlineNextSubSegIdx - SystemConfig.INT_SVIEW_ONLINE_DRAW_SUBSEG_SIZE + 1;

            fSurfaceViewWidth = onlineFragment.mSurfaceViewsOnline[mIntSViewDrawOnlineNextViewIdx].getWidth();
            fSurfaceViewHeight = onlineFragment.mSurfaceViewsOnline[mIntSViewDrawOnlineNextViewIdx].getHeight();

            iDrawSubSegSize = SystemConfig.INT_SVIEW_ONLINE_DRAW_SUBSEG_SIZE;

            mFloatBasicGainY = fSurfaceViewHeight / (float)mIntSvDrawMaxFreqCnt;
            mFloatBasicGainX = fSurfaceViewWidth / ((float) iDrawSubSegSize + 1);

            mSurfaceHolderBVOnLine = onlineFragment.mSurfaceViewsOnline[mIntSViewDrawOnlineNextViewIdx].getHolder();
            Canvas canvas = null;
            try {
                canvas = mSurfaceHolderBVOnLine.lockCanvas();
                if (SystemConfig.darkMode){
                    canvas.drawColor(Color.BLACK);
                }else{
                    canvas.drawColor(Color.WHITE);
                }
                //SystemConfig.mMyEventLogger.appendDebugStr("Canvas Lock");
                //?synchronized (mSurfaceHolderBVOnLine) {
                    iTryXVar = iTryStartSubSegIdx;
                    while (iTryXVar <= iTryEndSubSegIdx) {
                        fPosX = (float) (iTryXVar - iTryStartSubSegIdx + 1) * mFloatBasicGainX;
                        iXVar = iTryXVar % MainActivity.mBVSignalProcessorPart1.mIntTotalSubSegMaxSize;
                        for (iYVar = 0; iYVar <= mIntSvDrawTotalFreqCnt; iYVar++) {
//                            if(mIntSvDrawTotalFreqCnt <= 64){
                                boolDrawPoint = true;
//                            }else if((iYVar % 2 ) == 0) {
//                                boolDrawPoint = true;
//                            }else{
//                                boolDrawPoint = false;
//                            }
//                            if(boolDrawPoint){
                                fPosY2 = (float) iYVar * mFloatBasicGainY;
                                if (iYVar == 0) {
                                    fPosY1 = fPosY2;
                                } else {
                                    fPosY1 = (float) (iYVar - 1) * mFloatBasicGainY;
                                }

                                //doubleAmpVal = doubleSignalToShow[iXVar][iYVar];
                                if(SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_0_SNSI_GM) {
                                    //doubleAmpVal = Math.log10((doubleSignalToShow[iXVar][iYVar]/MainActivity.mBVSignalProcessorPart1.mDoubleAmpPsdMaxNormPeriod[iXVar])*1000+1) / Math.log10(1001);
                                    doubleAmpVal = doubleSignalToShow[iXVar][iYVar];
                                }else{
                                   // doubleAmpVal = doubleSignalToShow[iXVar][iYVar]/MainActivity.mBVSignalProcessorPart1.mDoubleAmpPsdMaxNormPeriod[iXVar];
                                    doubleAmpVal = doubleSignalToShow[iXVar][iYVar]/MainActivity.mBVSignalProcessorPart1.mDoubleAmpPsdMaxPeriodFirst;
                                }

                                if (doubleAmpVal > 0) {
                                    mPaintVelocity.setColor(getPaintColor(doubleAmpVal));
//                                    Log.d("color", String.valueOf(getPaintColor(doubleAmpVal)));
                                    if (SystemConfig.mBloodVelocityPlotterDrawType == SystemConfig.BLOOD_VELOCITY_PLOTTER_DRAW_TYPE_ENUM.TYPE_LINE) {
                                        if (iYVar == 0) {
                                            canvas.drawPoint((int) fPosX, (int) fPosY2, mPaintVelocity);
                                        } else {
                                            canvas.drawLine(fPosX, fPosY1, fPosX, fPosY2, mPaintVelocity);
                                        }
                                    } else {
                                        canvas.drawPoint((int) fPosX, (int) fPosY2, mPaintVelocity);
                                    }
                                }
//                            }
                        }
                        iTryXVar++;
                    }
                //}
            } catch (Exception ex1) {
                //SystemConfig.mMyEventLogger.appendDebugStr("drawByEndSegIdxOnLine.Exception","");
                //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
                ex1.printStackTrace();
            } finally {
                if (canvas != null) {
                    mSurfaceHolderBVOnLine.unlockCanvasAndPost(canvas);
                    canvas = null;
                }
                mIntSViewDrawOnlineNextViewIdx++;
                if (mIntSViewDrawOnlineNextViewIdx >= SystemConfig.INT_SURFACE_VIEWS_ON_LINE_USE_SIZE) {
                    mIntSViewDrawOnlineNextViewIdx = 0;
                }
                mIntSViewDrawOnlineNextSubSegIdx = mIntSViewDrawOnlineNextSubSegIdx + SystemConfig.INT_SVIEW_ONLINE_DRAW_SUBSEG_SIZE;
                clearSviewsByIdxOnLine(mIntSViewDrawOnlineNextViewIdx);
                if (SystemConfig.isHeartIO2){
                    ecgSegViews[mIntSViewDrawOnlineNextViewIdx].clearBVSegUIView();
                }
            }
        }

        private void clearSviewsByIdxOnLine(int iClearViewIdx) {
            if (oFrag == null){
                return;
            }
            Log.i(TAG, "clearSviewsByIdxOnLine");
            SurfaceHolder surfaceHolder;

            surfaceHolder = onlineFragment.mSurfaceViewsOnline[iClearViewIdx].getHolder();
            Canvas canvas = surfaceHolder.lockCanvas();
            if (SystemConfig.darkMode){
                canvas.drawColor(Color.BLACK);
            }else{
                canvas.drawColor(Color.WHITE); //雨刷
            }
            surfaceHolder.unlockCanvasAndPost(canvas);
            canvas = null;
        }



        public void drawByStartEndSubSegIdxOffLine(final int iStartSubSegIdx, final int iEndSubSegIdx) {
            if (SystemConfig.mIntPlotterModeIdx == SystemConfig.INT_PLOTTER_MODE_IDX_0_BLOOD_VELOCITY) {
                if(mSurfaceHolderBVOffLine == null) {
                    //Log.d(TAG,"[415] mSurfaceHolderBVOffLine == null");
                    initSurfaceHolderOffline();
                    //drawBloodVelocityByStartEndSubSegIdxOffLine(iStartSubSegIdx, iEndSubSegIdx, false);
                }else{
                    //Log.d(TAG,"mSurfaceHolderBVOffLine != null");
                    drawBloodVelocityByStartEndSubSegIdxOffLine(iStartSubSegIdx, iEndSubSegIdx, false);
                    if (SystemConfig.isHeartIO2||(offFrag!=null && offFrag.hasECG)){
                        if (MainActivity.currentFragmentTag.equals(Constants.ITRI_ULTRASOUND_FRAGMENT_TAG)){  //online
                            oFrag.ecgViewOffline.inputStartSubSegIdx = iStartSubSegIdx;
                            oFrag.ecgViewOffline.inputEndSubSegIdx = iEndSubSegIdx;
                            oFrag.ecgViewOffline.invalidate();
                        }else{
                            offFrag.mECGOffline.inputStartSubSegIdx = iStartSubSegIdx;
                            offFrag.mECGOffline.inputEndSubSegIdx = iEndSubSegIdx;
                            offFrag.mECGOffline.invalidate();
                        }
                    }
                }

            } else {
                drawFeatureByStartEndSubSegIdxOffLine(iStartSubSegIdx, iEndSubSegIdx, SystemConfig.mIntPlotterModeIdx);
            }
        }

        private void clearCanvasByHolder(SurfaceHolder inputHolder){
            Canvas mCanvas = inputHolder.lockCanvas();
            if (mCanvas!=null) {
                if (SystemConfig.darkMode){
                    mCanvas.drawColor(Color.BLACK);
                }else{
                    mCanvas.drawColor(Color.WHITE);
                }
                inputHolder.unlockCanvasAndPost(mCanvas);
            }
        }


        public void drawBloodVelocityByStartEndSubSegIdxOffLine(int iStartSubSegIdx, int iEndSubSegIdx, boolean boolByTimeScaleMove) {
            float fPosX, fPosY1, fPosY2;
            //float fSurfaceViewWidth, fSurfaceViewHeight;
            float fPreXVTIMaxIdx, fPreYVTIMaxIdx, fPreXHRPeriod, fPreYHRPeriod;
            int iXVar, iYVar, iHRPeriodCurVpk;
            double doubleAmpVal, doubleValue1;
            int iDrawSubSegSize;
            int iMaxDrawFreqCnt;
            Paint paintVTICurUsed, paintHRPeriodCurUsed;
            boolean boolHRPeriodFound = false;
            boolean boolVTIStart, boolVTIEnd, boolHRPeriodStart, boolHRPeriodEnd;
            boolean boolDrawVTIStartEnd, boolDrawVpk;
            boolean boolDrawMaxIdxAsLine;
            float floatPreMaxIdxLineX, floatPreMaxIdxLineY;
            float floatPreMaxIdxLineX_HR, floatPreMaxIdxLineY_HR,
                    floatPreMaxIdxLineX_PERIOD, floatPreMaxIdxLineY_PERIOD,
                    floatPreMaxIdxLineX_VPK, floatPreMaxIdxLineY_VPK;
            float floatPreMaxIdxForVTILineX, floatPreMaxIdxForVTILineY;
            double[][] doubleSignalToShow;
            double doubleDebug;

            int lastRIndex = -1;

            if (!boolByTimeScaleMove){
                if (!mBoolColorAndSignalMappedSetted) {
                    if(SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_0_SNSI_GM){
                        prepareColorAndSignalMappingWuMode();
                        //SystemConfig.mMyEventLogger.appendDebugStr("SignalMappingWuMode().exec", "");
                    }else {
                        prepareColorAndSignalMappingHuangMode();
                    }
                    mBoolColorAndSignalMappedSetted = true;
                }
            }

            doubleSignalToShow = MainActivity.mBVSignalProcessorPart1.mDoubleBVSpectrumValues;


            mFloatSurfaceViewWidth = mSurfaceViewBV.getWidth();
            mFloatSurfaceViewHeight = mSurfaceViewBV.getHeight();
            mIntDrawSubSegStart = iStartSubSegIdx;
            mIntDrawSubSegEnd = iEndSubSegIdx;

            iDrawSubSegSize = iEndSubSegIdx - iStartSubSegIdx + 1;
            mFloatXGainDrawSizeToPointsRatio = mFloatSurfaceViewWidth / ((float) iDrawSubSegSize + 1);

            //mFloatBasicGainY = mFloatSurfaceViewHeight / ((float) SystemConfig.mIntFreqIdxsMaxSize - 1);

            mFloatBasicGainY = mFloatSurfaceViewHeight / (float) mIntSvDrawMaxFreqCnt;
            /*if(mSurfaceHolderBVOffLine == null) {
                Log.d(TAG,"[486] mSurfaceHolderBVOffLine == null");
                mSurfaceHolderBVOffLine = mSurfaceViewBV.getHolder();
            }*/

            try {

                mCanvasBVOffLine = mSurfaceHolderBVOffLine.lockCanvas();
                //synchronized (mSurfaceHolderBVOffLine) {
                if (mCanvasBVOffLine!=null){
                    if (SystemConfig.darkMode){
                        mCanvasBVOffLine.drawColor(Color.BLACK);
                    }else{
                        mCanvasBVOffLine.drawColor(Color.WHITE);
                    }

                    iXVar = iStartSubSegIdx;
                    while (iXVar <= iEndSubSegIdx) {
                        fPosX = (float) (iXVar - iStartSubSegIdx + 1) * mFloatXGainDrawSizeToPointsRatio;
                        for (iYVar = 0; iYVar <= mIntSvDrawTotalFreqCnt ; iYVar++) {
                            fPosY2 = (float) iYVar * mFloatBasicGainY;
                            if (iYVar == 0) {
                                fPosY1 = fPosY2;
                            } else {
                                fPosY1 = (float) (iYVar - 1) * mFloatBasicGainY;
                            }
                            if(SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_0_SNSI_GM) {
                                // doubleAmpVal = Math.log10((doubleSignalToShow[iXVar][iYVar]/MainActivity.mBVSignalProcessorPart1.mDoubleAmpPsdMaxNormPeriod[iXVar]) *1000+1)
                                //                         /Math.log10(1001);
                                doubleAmpVal = doubleSignalToShow[iXVar][iYVar];
                            }else{
                                //doubleAmpVal = doubleSignalToShow[iXVar][iYVar]/MainActivity.mBVSignalProcessorPart1.mDoubleAmpPsdMaxNormPeriod[iXVar];
                                doubleAmpVal = doubleSignalToShow[iXVar][iYVar]/MainActivity.mBVSignalProcessorPart1.mDoubleAmpPsdMaxPeriodFirst;
                            }

                            if (doubleAmpVal > 0) {
                                mPaintVelocity.setColor(getPaintColor(doubleAmpVal));
                                if (SystemConfig.mBloodVelocityPlotterDrawType == SystemConfig.BLOOD_VELOCITY_PLOTTER_DRAW_TYPE_ENUM.TYPE_LINE) {
                                    if (iYVar == 0) {
                                        mCanvasBVOffLine.drawPoint((int) fPosX, (int) fPosY2, mPaintVelocity);
                                    } else {
                                        mCanvasBVOffLine.drawLine(fPosX, fPosY1, fPosX, fPosY2, mPaintVelocity);
                                    }
                                } else {
                                    mCanvasBVOffLine.drawPoint((int) fPosX, (int) fPosY2, mPaintVelocity);
                                }
                            }
                        }
                        iXVar++;
                    }

                    //*****************************************************************
                    // Draw Debug
                    //*****************************************************************

                    if(!SystemConfig.mBoolDebugPlotterDrawLine){
                        return;
                    }

                    iXVar = iStartSubSegIdx;
                    fPreXVTIMaxIdx = 0;
                    fPreYVTIMaxIdx = 0;
                    fPreXHRPeriod = 0;
                    fPreYHRPeriod = 0;
                    iHRPeriodCurVpk = 0;
                    paintVTICurUsed = mPaintVTIMaxIdxUsable;
                    paintHRPeriodCurUsed = mPaintHRPeriodNotDiscarded;
                    floatPreMaxIdxLineX = 0;
                    floatPreMaxIdxLineY = 0;
                    floatPreMaxIdxLineX_HR = 0;
                    floatPreMaxIdxLineY_HR = 0;
                    floatPreMaxIdxLineX_PERIOD=0;
                    floatPreMaxIdxLineY_PERIOD=0;
                    floatPreMaxIdxLineX_VPK=0;
                    floatPreMaxIdxLineY_VPK=0;

                    floatPreMaxIdxForVTILineX = 0;
                    floatPreMaxIdxForVTILineY = 0;

                    boolean drawFlagGIS = false;
                    while (iXVar <= iEndSubSegIdx) {
                        fPosX = (float) (iXVar - iStartSubSegIdx + 1) * mFloatXGainDrawSizeToPointsRatio;

                        if(GIS_SystemConfig.isVisible){
                            //Draw Vti Boundary ( Start and End ) Line
                            for (double start:MainActivity.mVtiBoundaryResultByGIS.startLocationsOffline) {
                                if(iXVar == (int)start){
                                    float tempX = (float) (iXVar - iStartSubSegIdx + 1) * mFloatXGainDrawSizeToPointsRatio;
                                    float tempY = (float) SystemConfig.mDopplerVFOutput[0][iXVar] * mFloatBasicGainY;
                                    drawFlagGIS = true;
                                    mCanvasBVOffLine.drawLine((int) tempX, 0, (int) tempX, tempY, mPaintVTIStartGIS);
                                    break;
                                }
                            }

                            for (double end:MainActivity.mVtiBoundaryResultByGIS.endLocationsOffline) {
                                if(iXVar == (int)end){
                                    float tempX = (float) (iXVar - iStartSubSegIdx + 1) * mFloatXGainDrawSizeToPointsRatio;
                                    float tempY = (float) SystemConfig.mDopplerVFOutput[0][iXVar] * mFloatBasicGainY;

                                    drawFlagGIS = false;
                                    mCanvasBVOffLine.drawLine((int) tempX, 0, (int) tempX, tempY, mPaintVTIEndGIS);
                                    break;
                                }
                            }
                        }

                        //--------------------------------------------------
                        //-- draw VTI Start End State
                        //--------------------------------------------------
                        boolDrawVTIStartEnd= false;
                        if(boolDrawVTIStartEnd ) {
                            if (MainActivity.mBVSignalProcessorPart2Selected.checkPeakBottomStateForVTIStartS2(iXVar)) {
                                mCanvasBVOffLine.drawPoint((int) fPosX, 30, mPaintVTIStart);
                            } else if (MainActivity.mBVSignalProcessorPart2Selected.checkPeakBottomStateForVTIEndS2(iXVar)) {
                                mCanvasBVOffLine.drawPoint((int) fPosX, 30, mPaintVTIEnd);
                            }
                            // draw Vpk
                            if (MainActivity.mBVSignalProcessorPart2Selected.checkPeakBottomStateForVpkPeakS2(iXVar)) {
                                fPosY2 = MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdxByMovingAverage[iXVar] * mFloatBasicGainY;
                                mCanvasBVOffLine.drawPoint((int) fPosX, fPosY2, mPaintVpkPeakS2);
                            }
                        }

                        //--------------------------------------------------
                        //-- draw MaxIdx
                        //--------------------------------------------------
                        boolDrawMaxIdxAsLine = true;
                        boolean boolDrawMaxIdx = true;
                        if((SystemConfig.mIntDrawVTIEnabled == SystemConfig.INT_DRAW_VTI_ENABLED_YES)
                                        && (boolDrawMaxIdx)){
                            //jaufa-
//                            fPosY2 = MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdxByMovingAverage[iXVar] * mFloatBasicGainY;
//                            fPosY2 = MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx[iXVar] * mFloatBasicGainY;
                            fPosY2 = (float) SystemConfig.mDopplerVFOutput[0][iXVar] * mFloatBasicGainY;
                            if (!boolDrawMaxIdxAsLine){
                                mCanvasBVOffLine.drawPoint((int) fPosX, (int) fPosY2, mPaintMaxIdx);
                            }else{
                                if(drawFlagGIS) {
                                    if (floatPreMaxIdxLineX == 0) {
                                        mCanvasBVOffLine.drawPoint((int) fPosX, (int) fPosY2, mPaintMaxIdx);
                                    } else {
                                        mCanvasBVOffLine.drawLine(floatPreMaxIdxLineX, floatPreMaxIdxLineY, fPosX, fPosY2, mPaintMaxIdx);
                                    }
                                }
                                floatPreMaxIdxLineX =  fPosX;
                                floatPreMaxIdxLineY =  fPosY2;
                            }

                            //jaufa- fPosY2 = SystemConfig.mBVSignalProcessorPart1.mIntArrayMaxIdxByMovingAverage[iXVar] * mFloatBasicGainY;
                            fPosY2 = MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx_Hr[iXVar] * mFloatBasicGainY;
                            if (!boolDrawMaxIdxAsLine){
                                mCanvasBVOffLine.drawPoint((int) fPosX, (int) fPosY2, mPaintMaxIdx_HR);
                            }else{
                                if(floatPreMaxIdxLineX_HR == 0){
                                    mCanvasBVOffLine.drawPoint((int) fPosX, (int) fPosY2, mPaintMaxIdx_HR);
                                }else{
                                    mCanvasBVOffLine.drawLine(floatPreMaxIdxLineX_HR, floatPreMaxIdxLineY_HR, fPosX, fPosY2, mPaintMaxIdx_HR);
                                }
                                floatPreMaxIdxLineX_HR =  fPosX;
                                floatPreMaxIdxLineY_HR =  fPosY2;
                            }

                            //jaufa- fPosY2 = SystemConfig.mBVSignalProcessorPart1.mIntArrayMaxIdxByMovingAverage[iXVar] * mFloatBasicGainY;
                            fPosY2 = MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdx_Period[iXVar] * mFloatBasicGainY;
                            if (!boolDrawMaxIdxAsLine){
                                mCanvasBVOffLine.drawPoint((int) fPosX, (int) fPosY2, mPaintMaxIdx_PERIOD);
                            }else{
                                if(floatPreMaxIdxLineX_PERIOD == 0){
                                    mCanvasBVOffLine.drawPoint((int) fPosX, (int) fPosY2, mPaintMaxIdx_PERIOD);
                                }else{
                                    mCanvasBVOffLine.drawLine(floatPreMaxIdxLineX_PERIOD, floatPreMaxIdxLineY_PERIOD, fPosX, fPosY2, mPaintMaxIdx_PERIOD);
                                }
                                floatPreMaxIdxLineX_PERIOD =  fPosX;
                                floatPreMaxIdxLineY_PERIOD =  fPosY2;
                            }


                            if (MainActivity.mBVSignalProcessorPart2Selected.checkPeakBottomStateForVpkPeakS2(iXVar)) {
                                fPosY2 = MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdxByMovingAverage[iXVar] * mFloatBasicGainY;
                                mCanvasBVOffLine.drawPoint((int) fPosX, fPosY2, mPaintVpkPeakS2);
                            }
                        }

                        //--------------------------------------------------
                        //-- draw VpkIdx
                        //--------------------------------------------------
                        boolDrawVpk = false;
                        if(boolDrawVpk){
                            if (MainActivity.mBVSignalProcessorPart2Selected.checkPeakBottomStateForVpkPeakS2(iXVar)) {
                                fPosY2 = MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdxByMovingAverage[iXVar] * mFloatBasicGainY;
                                mCanvasBVOffLine.drawPoint((int) fPosX, fPosY2, mPaintVpkPeakS2);
                            }
                        }

                        //--------------------------------------------------
                        //-- draw MaxIdxMovingAverage
                        //--------------------------------------------------
                        boolean boolDrawMaxIdxMovingAverage = false;
                        boolean boolDrawVTIMovingAverageAsLine = true;
                        if (SystemConfig.mIntSingleVpkEnabled == SystemConfig.INT_SINGLE_VPK_ENABLED_YES) {
                            boolDrawMaxIdxMovingAverage = true;
                        }
                        if(boolDrawMaxIdxMovingAverage) {
                            if (false && (SystemConfig.mIntSingleVpkEnabled == SystemConfig.INT_SINGLE_VPK_ENABLED_YES)) {
                                fPosY2 = MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdxByMovingAverage[iXVar] * mFloatBasicGainY;
                                if (floatPreMaxIdxForVTILineX == 0) {
                                    mCanvasBVOffLine.drawPoint((int) fPosX, (int) fPosY2, mPaintMaxIdxForVTI);
                                } else {
                                    mCanvasBVOffLine.drawLine(floatPreMaxIdxForVTILineX, floatPreMaxIdxForVTILineY, fPosX, fPosY2, mPaintMaxIdxForVTI);
                                }
                                floatPreMaxIdxForVTILineX = fPosX;
                                floatPreMaxIdxForVTILineY = fPosY2;
                            } else {
                                fPosY2 = MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdxByMovingAverage[iXVar] * mFloatBasicGainY;
                                if (!boolDrawVTIMovingAverageAsLine) {
                                    mCanvasBVOffLine.drawPoint((int) fPosX, (int) fPosY2, mPaintMaxIdxForVTI);
                                } else {
                                    if (floatPreMaxIdxForVTILineX == 0) {
                                        mCanvasBVOffLine.drawPoint((int) fPosX, (int) fPosY2, mPaintMaxIdxForVTI);
                                    } else {
                                        mCanvasBVOffLine.drawLine(floatPreMaxIdxForVTILineX, floatPreMaxIdxForVTILineY, fPosX, fPosY2, mPaintMaxIdxForVTI);
                                    }
                                    floatPreMaxIdxForVTILineX = fPosX;
                                    floatPreMaxIdxForVTILineY = fPosY2;
                                }
                            }
                        }

                        //--------------------------------------------------
                        //-- draw VTI
                        //--------------------------------------------------
                        if(SystemConfig.mIntDrawVTIEnabled == SystemConfig.INT_DRAW_VTI_ENABLED_YES) {
                            if (SystemConfig.mIntVTIModeIdx==SystemConfig.INT_VTI_MODE_2_TWO_POINT){
                                // Cavin close for testing 20210726
                                if(iXVar>=offlineFragment.twoPointStart && iXVar<=offlineFragment.twoPointEnd){
                                    boolVTIStart = (iXVar == offlineFragment.twoPointStart);
                                    boolVTIEnd = (iXVar == offlineFragment.twoPointEnd);
//                                    fPosY2 = (float)SystemConfig.mDopplerVFOutput[0][iXVar] * mFloatBasicGainY;
                                    fPosY2 = MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdxByMovingAverage[iXVar] * mFloatBasicGainY;
//                                Log.d(TAG,"fPosY2="+fPosY2);
//                                mCanvasBVOffLine.drawPoint(fPosX,fPosY2,paintVTICurUsed);
                                    if(boolVTIStart) {
                                        //mCanvasBVOffLine.drawPoint((int) fPosX, (int) fPosY2, mPaintVTIMax);
                                        mCanvasBVOffLine.drawLine(fPreXVTIMaxIdx, 0, fPosX, fPosY2, paintVTICurUsed);
                                        fPreYVTIMaxIdx = fPosY2;
                                        fPreXVTIMaxIdx = fPosX;
                                    } else {
                                        mCanvasBVOffLine.drawLine(fPreXVTIMaxIdx, fPreYVTIMaxIdx, fPosX, fPosY2, paintVTICurUsed);
                                        fPreYVTIMaxIdx = fPosY2;
                                        fPreXVTIMaxIdx = fPosX;
                                        if(boolVTIEnd) {
                                            //mCanvasBVOffLine.drawPoint((int) fPosX, (int) fPosY2, mPaintVTIMax);
                                            mCanvasBVOffLine.drawLine(fPosX, fPosY2, fPosX, 0, paintVTICurUsed);
                                        }
                                    }
                                } else {
                                    fPreYVTIMaxIdx = 0;
                                    fPreXVTIMaxIdx = fPosX;
                                }
                                // Cavin close for testing 20210726
                            }else if (SystemConfig.mIntVTIModeIdx==SystemConfig.INT_VTI_MODE_0_STRONGEST){
                                   /*// Cavin close for testing 20211209
                                if (MainActivity.mBVSignalProcessorPart2Selected.mIntArrayVTIMaxIdx[iXVar] > 0) {
                                    boolVTIStart = MainActivity.mBVSignalProcessorPart2Selected.checkPeakBottomStateForVTIStartS2(iXVar);
                                    boolVTIEnd = MainActivity.mBVSignalProcessorPart2Selected.checkPeakBottomStateForVTIEndS2(iXVar);
                                    fPosY2 = MainActivity.mBVSignalProcessorPart2Selected.mIntArrayVTIMaxIdx[iXVar] * mFloatBasicGainY;

                                    if(boolVTIStart) {
                                        //mCanvasBVOffLine.drawPoint((int) fPosX, (int) fPosY2, mPaintVTIMax);
                                        mCanvasBVOffLine.drawLine(fPreXVTIMaxIdx, 0, fPosX, fPosY2, paintVTICurUsed);
                                        fPreYVTIMaxIdx = fPosY2;
                                        fPreXVTIMaxIdx = fPosX;
                                    } else {
                                        mCanvasBVOffLine.drawLine(fPreXVTIMaxIdx, fPreYVTIMaxIdx, fPosX, fPosY2, paintVTICurUsed);
                                        fPreYVTIMaxIdx = fPosY2;
                                        fPreXVTIMaxIdx = fPosX;
                                        if(boolVTIEnd) {
                                            //mCanvasBVOffLine.drawPoint((int) fPosX, (int) fPosY2, mPaintVTIMax);
                                            mCanvasBVOffLine.drawLine(fPosX, fPosY2, fPosX, 0, paintVTICurUsed);
                                        }
                                    }
                                } else {
                                    fPreYVTIMaxIdx = 0;
                                    fPreXVTIMaxIdx = fPosX;
                                } */
                            }
                        }

                        //--------------------------------------------------
                        //-- draw HR Start
                        //--------------------------------------------------
                        boolean boolDrawHRStart = false;
                        if(boolDrawHRStart) {
                            if (MainActivity.mBVSignalProcessorPart2Selected.checkPeakBottomStateForHRStart(iXVar)) {
                                mCanvasBVOffLine.drawPoint((int) fPosX, 220, mPaintHRStart);
                            }
                        }

                        //--------------------------------------------------
                        //-- draw R peak
                        //--------------------------------------------------
                        if (SystemConfig.vtiSegPlot && (SystemConfig.isHeartIO2 ||
                                (MainActivity.currentFragmentTag.equals(Constants.DATA_MANAGER_TAG)&&
                                MainActivity.offFrag!=null && MainActivity.offFrag.hasECG))
                        ){
                            // ECG data copy
                            if (containRPeakInUltraSound(MainActivity.mBVSignalProcessorPart1.mEcgList, iXVar)){
                                Paint mPaintRPeak = new Paint();
                                mPaintRPeak.setColor(Color.RED);
                                mPaintRPeak.setStrokeWidth(5);
                                mCanvasBVOffLine.drawLine(fPosX,0,fPosX,220,mPaintRPeak);
                                lastRIndex = iXVar;
                            }
                            if (lastRIndex!=-1 && iXVar == lastRIndex+SystemConfig.vtiLengthUS){
                                Paint mPaintVtiEnd = new Paint();
                                mPaintVtiEnd.setColor(Color.BLUE);
                                mPaintVtiEnd.setStrokeWidth(5);
                                mCanvasBVOffLine.drawLine(fPosX,0,fPosX,220,mPaintVtiEnd);
                            }
                        }

                        //--------------------------------------------------
                        //-- draw HR Period
                        //--------------------------------------------------
                        if(SystemConfig.mIntDrawVTIEnabled == SystemConfig.INT_DRAW_VTI_ENABLED_YES) {
                            if (MainActivity.mBVSignalProcessorPart2Selected.checkPeakBottomStateForHRPeriod(iXVar)) {
                                boolHRPeriodStart = MainActivity.mBVSignalProcessorPart2Selected.checkPeakBottomStateForHRStart(iXVar);
                                boolHRPeriodEnd = MainActivity.mBVSignalProcessorPart2Selected.checkPeakBottomStateForHREnd(iXVar);
                                if ((!boolHRPeriodFound) || boolHRPeriodStart) {
                                    if (MainActivity.mBVSignalProcessorPart2Selected.checkPeakBottomStateForHRPeriodDiscarded(iXVar)) {
                                        paintHRPeriodCurUsed = mPaintHRPeriodDiscarded;
                                    } else {
                                        paintHRPeriodCurUsed = mPaintHRPeriodNotDiscarded;
                                    }

                                    iHRPeriodCurVpk = MainActivity.mBVSignalProcessorPart2Selected.getHRPeriodVpkValue(iXVar);
                                    boolHRPeriodFound = true;
                                    boolHRPeriodStart = true;
                                }
                                fPosY2 = iHRPeriodCurVpk * mFloatBasicGainY;
                                if (boolHRPeriodStart || boolHRPeriodEnd) {
                                    //mCanvasBVOffLine.drawPoint((int) fPosX, (int) fPosY2, mPaintVTIMax);
                                    //0708 byJaufa
                                    mCanvasBVOffLine.drawLine(fPosX, 0, fPosX, fPosY2, paintHRPeriodCurUsed);
                                } else {
                                    //0708 byJaufa
                                    mCanvasBVOffLine.drawLine(fPreXHRPeriod, fPreYHRPeriod, fPosX, fPosY2, paintHRPeriodCurUsed);
                                }
                                fPreYHRPeriod = fPosY2;
                                fPreXHRPeriod = fPosX;
                            } else {
                                fPreYHRPeriod = 0;
                                fPreXHRPeriod = fPosX;
                                boolHRPeriodFound = false;
                            }
                        }
                        //--------------------------------------------------
                        //-- Others
                        //--------------------------------------------------

                        iXVar++;
                    }


                    if(SystemConfig.mIntTimeScaleEnabled == SystemConfig.INT_TIME_SCALE_ENABLED_YES) {
                        setTimeScaleLineWithCanvas(mCanvasBVOffLine);
                        mIntSubSegRedrawStart = mIntDrawSubSegStart + (int) (mFloatTimeScalePoint / mFloatXGainDrawSizeToPointsRatio) - 1;
                        mIntSubSegRedrawEnd = mIntDrawSubSegStart + (int) (mFloatTimeScalePoint / mFloatXGainDrawSizeToPointsRatio) + 1;
                        if (mIntSubSegRedrawStart < mIntDrawSubSegStart) {
                            mIntSubSegRedrawStart = mIntDrawSubSegStart;
                        }
                        if (mIntSubSegRedrawEnd > mIntDrawSubSegEnd) {
                            mIntSubSegRedrawEnd = mIntDrawSubSegStart;
                        }

                        mBitMapCanvasBV = Bitmap.createBitmap( mCanvasBVOffLine.getWidth(), mCanvasBVOffLine.getHeight(), Bitmap.Config.ARGB_8888);
                        mCanvasBVOffLine.setBitmap(mBitMapCanvasBV);
                    }
                /*}

                    if (mCanvasBVOffLine != null) {*/
                    mSurfaceHolderBVOffLine.unlockCanvasAndPost(mCanvasBVOffLine);
                    mCanvasBVOffLine = null;
                    needRedrawBVCanvas = false;
                }else{    // mCanvasBVOffLine ==  null;
                    needRedrawBVCanvas = true;
                    redrawStartIdx = iStartSubSegIdx;
                    redrawEndIdx = iEndSubSegIdx;
                }
         //       }   // End of synchronized
            } catch (Exception e) {
                e.printStackTrace();
                //SystemConfig.mMyEventLogger.appendDebugStr("drawBVBySegOffLine.Exception","");
                //SystemConfig.mMyEventLogger.appendDebugStr(e.toString(),"");
            }
        }

    public boolean containRPeakInUltraSound(final List<ecgResult> list, final int index){
        boolean found = false;
        for (ecgResult tmpEcg:list){
            int tmpIndex = tmpEcg.getRPeakIndex() >> 2;
            if (tmpIndex == index) {
                found = true;
                break;
            }
        }
        return found;
    }


        public void drawBVSignalByChangeDiscarededState() {
            drawBloodVelocityByStartEndSubSegIdxOffLine(mIntDrawSubSegStart, mIntDrawSubSegEnd, false);
        }


        public void drawFeatureByStartEndSubSegIdxOffLine(int iStartSubSegIdx, int iEndSubSegIdx, int iPlotterMode) {

            if (!(MainActivity.currentFragmentTag.equals(Constants.ITRI_ULTRASOUND_FRAGMENT_TAG)
                    || MainActivity.currentFragmentTag.equals(Constants.FILEOPEN_TAG))){
                return;
            }
            float fPosX1, fPosX2, fPosY1, fPosY2;
            float fSurfaceViewWidth, fSurfaceViewHeight;
            int iXVar;
            int iDrawSubSegSize;
            double doubleAmpSumMax;
            int iTotalSubSegSize;
            double[] doubleFeatureData;

            if(iPlotterMode == SystemConfig.INT_PLOTTER_MODE_IDX_0_BLOOD_VELOCITY) {
                return;
            }else if(iPlotterMode == SystemConfig.INT_PLOTTER_MODE_IDX_1_HR_PEAK){
                doubleFeatureData = MainActivity.mBVSignalProcessorPart2Selected.mHRPeakFeature.mDoubleIntegralValues;
            }else if(iPlotterMode == SystemConfig.INT_PLOTTER_MODE_IDX_2_HR_BOTTOM){
                doubleFeatureData = MainActivity.mBVSignalProcessorPart2Selected.mHRBottomFeature.mDoubleIntegralValues;
            }else if(iPlotterMode == SystemConfig.INT_PLOTTER_MODE_IDX_3_PHANTOM_PEAK){
                doubleFeatureData = MainActivity.mBVSignalProcessorPart2Selected.mPhantomPeakFeature.mDoubleIntegralValues;
            }else if(iPlotterMode == SystemConfig.INT_PLOTTER_MODE_IDX_4_VTI_START){
                doubleFeatureData = MainActivity.mBVSignalProcessorPart2Selected.mVTIStartFeature.mDoubleIntegralValues;
            }else { //if(iPlotterMode == SystemConfig.INT_PLOTTER_MODE_IDX_5_VTI_END){
                doubleFeatureData = MainActivity.mBVSignalProcessorPart2Selected.mVTIEndFeature.mDoubleIntegralValues;
            }

            fSurfaceViewWidth = mSurfaceViewBV.getWidth();
            fSurfaceViewHeight = mSurfaceViewBV.getHeight();

            iDrawSubSegSize = iEndSubSegIdx - iStartSubSegIdx + 1;
            iTotalSubSegSize = mRawDataProcessor.getUltrasoundCurrSamplesSize() / SystemConfig.mIntSTFTWindowShiftSize;
            doubleAmpSumMax = 0;
            for (iXVar = 0; iXVar < iTotalSubSegSize - 1; iXVar++) {
                if (doubleAmpSumMax < doubleFeatureData[iXVar]) {
                    doubleAmpSumMax = doubleFeatureData[iXVar];
                }
            }

            mFloatBasicGainY = fSurfaceViewHeight / ((float) doubleAmpSumMax);
            mFloatBasicGainX = fSurfaceViewWidth / (float) (iDrawSubSegSize + 1);

            /*if(mSurfaceHolderBVOffLine == null) {
                Log.d(TAG,"[798] mSurfaceHolderBVOffLine == null");
                mSurfaceHolderBVOffLine = mSurfaceViewBV.getHolder();
            }*/
            try {
                mCanvasBVOffLine = mSurfaceHolderBVOffLine.lockCanvas();
                if (SystemConfig.darkMode){
                    mCanvasBVOffLine.drawColor(Color.BLACK);
                }else{
                    mCanvasBVOffLine.drawColor(Color.WHITE);
                }

//                synchronized (mSurfaceHolderBVOffLine) {
                    iXVar = iStartSubSegIdx;
                    while (iXVar <= iEndSubSegIdx) {
                        fPosX2 = (float) (iXVar - iStartSubSegIdx + 1) * mFloatBasicGainX;
                        fPosY2 = (float) doubleFeatureData[iXVar] * mFloatBasicGainY;
                        fPosX1 = fPosX2;
                        fPosY1 = 0;
                        if (iXVar == 0) {
                            //mCanvasBVOffLine.drawPoint((int) fPosX2, (int) fPosY2, mPaint);
                            mCanvasBVOffLine.drawLine(fPosX1, fPosY1, fPosX2, fPosY2, mPaintSumAmp);
                        } else {
                            //mCanvasBVOffLine.drawLine(fPosX1, fPosY1, fPosX2, fPosY2, mPaintSumAmp);
                            mCanvasBVOffLine.drawLine(fPosX1, fPosY1, fPosX2, fPosY2, mPaintSumAmp);
                        }
                        iXVar++;
                    }

                    //*****************************************************************
                    // Draw Debug
                    //*****************************************************************

                    iXVar = iStartSubSegIdx;
                    while (iXVar <= iEndSubSegIdx) {
                        fPosX2 = (float) (iXVar - iStartSubSegIdx + 1) * mFloatBasicGainX;
                        fPosY2 = (float) doubleFeatureData[iXVar] * mFloatBasicGainY;

                        //--------------------------------------------------
                        //-- draw FreqAmpSumPeakState
                        //--------------------------------------------------
                        if((iPlotterMode == SystemConfig.INT_PLOTTER_MODE_IDX_1_HR_PEAK)
                                ||(iPlotterMode == SystemConfig.INT_PLOTTER_MODE_IDX_2_HR_BOTTOM)){
                            if (MainActivity.mBVSignalProcessorPart2Selected.checkPeakBottomStateForHRPeak(iXVar)) {
                                mCanvasBVOffLine.drawPoint((int) fPosX2, (int) (fPosY2), mPaintHRPeak);
                            }
                        }

                        if(iPlotterMode == SystemConfig.INT_PLOTTER_MODE_IDX_3_PHANTOM_PEAK){
                            if (MainActivity.mBVSignalProcessorPart2Selected.checkPeakBottomStateForPhantomPeak(iXVar)) {
                                mCanvasBVOffLine.drawPoint((int) fPosX2, (int) (fPosY2), mPaintPhantomPeak);
                            }
                        }

                        if ((iPlotterMode == SystemConfig.INT_PLOTTER_MODE_IDX_2_HR_BOTTOM)
                                || (iPlotterMode == SystemConfig.INT_PLOTTER_MODE_IDX_1_HR_PEAK)){
                            if (MainActivity.mBVSignalProcessorPart2Selected.checkPeakBottomStateForHRBottomS2(iXVar)) {
                                mCanvasBVOffLine.drawPoint((int) fPosX2, 20, mPaintHRBottomS2);
                            } else if (MainActivity.mBVSignalProcessorPart2Selected.checkPeakBottomStateForHRBottomS1(iXVar)) {
                                fPosY2 = MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdxByMovingAverage[iXVar] * mFloatBasicGainY;
                                mCanvasBVOffLine.drawPoint((int) fPosX2, 20, mPaintHRBottomS1);
                            }
                        }

                        if(iPlotterMode == SystemConfig.INT_PLOTTER_MODE_IDX_4_VTI_START){
                            if (MainActivity.mBVSignalProcessorPart2Selected.checkPeakBottomStateForVpkPeakS2(iXVar)) {
                                mCanvasBVOffLine.drawPoint((int) fPosX2, (int) (fPosY2), mPaintVpkPeakS2);
                            }
                            if (MainActivity.mBVSignalProcessorPart2Selected.checkPeakBottomStateForVTIStartS2(iXVar)) {
                                mCanvasBVOffLine.drawPoint((int) fPosX2, 30, mPaintVTIStart);
                            } else if (MainActivity.mBVSignalProcessorPart2Selected.checkPeakBottomStateForVTIEndS2(iXVar)) {
                                mCanvasBVOffLine.drawPoint((int) fPosX2, 30, mPaintVTIEnd);
                            } else if (MainActivity.mBVSignalProcessorPart2Selected.checkPeakBottomStateForVTIStartS1(iXVar)) {
                                mCanvasBVOffLine.drawPoint((int) fPosX2, 30, mPaintVTILow);
                            }
                        }

                        if(iPlotterMode == SystemConfig.INT_PLOTTER_MODE_IDX_5_VTI_END){
                            if (MainActivity.mBVSignalProcessorPart2Selected.checkPeakBottomStateForVpkPeakS2(iXVar)) {
                                mCanvasBVOffLine.drawPoint((int) fPosX2, (int) (fPosY2), mPaintVpkPeakS2);
                            }
                            if (MainActivity.mBVSignalProcessorPart2Selected.checkPeakBottomStateForVTIEndS2(iXVar)) {
                                mCanvasBVOffLine.drawPoint((int) fPosX2, 30, mPaintVTIEnd);
                            } else if (MainActivity.mBVSignalProcessorPart2Selected.checkPeakBottomStateForVTIEndS1(iXVar)) {
                                mCanvasBVOffLine.drawPoint((int) fPosX2, 30, mPaintVTILow);
                            }
                        }

                        //--------------------------------------------------
                        //-- draw HR MinLow & MinHigh
                        //--------------------------------------------------
                        if (MainActivity.mBVSignalProcessorPart2Selected.checkPeakBottomStateForHRStart(iXVar)) {
                            mCanvasBVOffLine.drawPoint((int) fPosX2, 220, mPaintHRStart);
                        }

                        if (MainActivity.mBVSignalProcessorPart2Selected.checkPeakBottomStateForHRStart(iXVar)) {
                            mCanvasBVOffLine.drawPoint((int) fPosX2, 220, mPaintHRStart);
                        }

                        //--------------------------------------------------
                        //-- draw VTIMaxIdx text
                        //--------------------------------------------------
                        if (MainActivity.mBVSignalProcessorPart2Selected.mIntArrayVTIMaxIdx[iXVar] >= 0) {
                            fPosY2 = MainActivity.mBVSignalProcessorPart2Selected.mIntArrayVTIMaxIdx[iXVar] * mFloatBasicGainY / 2;
                            mCanvasBVOffLine.drawPoint((int) fPosX2, fPosY2, mPaintVTIMaxIdxUsable);
                        }

                        //if(SystemConfig.mBVSignalProcessorPart2Selected.mIntArrayVelocityPeakState[ iXVar] == 1){
                        //    mCanvasBVOffLine.drawPoint((int) fPosX2, (int) (fPosY2+20), mPaintCandiPeak);
                        //}

                        iXVar++;
                    }
//                }
                /*if (mCanvasBVOffLine!= null) {
                    mSurfaceHolderBVOffLine.unlockCanvasAndPost(mCanvasBVOffLine);
                    mCanvasBVOffLine= null;
                }*/

            } catch (Exception e) {
                e.printStackTrace();
                //SystemConfig.mMyEventLogger.appendDebugStr("drawFeatureByStartEndSubOffLine.Exception","");
                //SystemConfig.mMyEventLogger.appendDebugStr(e.toString(),"");
            }finally {
                if (mCanvasBVOffLine != null) {
                    mSurfaceHolderBVOffLine.unlockCanvasAndPost(mCanvasBVOffLine);
                    mCanvasBVOffLine = null;
                }
            }
        }

        public void DrawBySubSegOffLine(int iStartSubSegIdx, int iSubSegLength) {
            int iLastSubSegIdx;

            iLastSubSegIdx = iStartSubSegIdx + iSubSegLength - 1;

            drawByStartEndSubSegIdxOffLine(iStartSubSegIdx, iLastSubSegIdx);
        }

        private void prepareColorAndSignalMappingHuangMode() {

            //prepareColorForGrayMode();
            prepareColorToSignalHuangMode();
        }


    private void prepareColorAndSignalMappingWuNewMode() {

        //prepareColorForGrayMode();
        if((SystemConfig.mEnumPlotterGrayModeForWuNew == SystemConfig.ENUM_PLOTTER_GRAY_MODE_FOR_WU_NEW.WU_NEW_DIFF_MAX_NOISE)
                || (SystemConfig.mEnumPlotterGrayModeForWuNew == SystemConfig.ENUM_PLOTTER_GRAY_MODE_FOR_WU_NEW.WU_NEW_DIFF_MAX_MIN)) {
            prepareColorMapToSignalByDiffWuNewMode();
        }else{   //if(SystemConfig.mEnumPlotterGrayModeForWuNew == SystemConfig.ENUM_PLOTTER_GRAY_MODE_FOR_WU_NEW.WU_NEW_RATIO) {
            prepareColorMapToSignalByRatioWuNewMode();
        }
    }


    private void prepareColorAndSignalMappingWuMode() {

        //prepareColorForGrayMode();
//        prepareColorMapToSignalWuMode();
        prepareColorMapToSignalWuMode210713();
    }
    private void prepareColorAndSignalMappingWuModeOnline() {

        //prepareColorForGrayMode();
        prepareColorMapToSignalWuModeOnline();
    }


    private void prepareColorForGrayMode() {
        int iVar;

        //mDoubleColorAmpGap = SystemConfig.mBVSignalProcessorPart1.mDoubleAmpMax / (DRAW_COLOR_SIZE);

        for (iVar = 0; iVar < DRAW_COLOR_SIZE; iVar++) {
            if (SystemConfig.darkMode){
                mIntArrayDrawColorsGrayMode[iVar][0] = iVar;
                mIntArrayDrawColorsGrayMode[iVar][1] = iVar;
                mIntArrayDrawColorsGrayMode[iVar][2] = iVar;
            }else{
                mIntArrayDrawColorsGrayMode[iVar][0] = 255 - iVar;
                mIntArrayDrawColorsGrayMode[iVar][1] = 255 - iVar;
                mIntArrayDrawColorsGrayMode[iVar][2] = 255 - iVar;
            }
        }
    }


    private void prepareColorToSignalHuangMode() {

        if(SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_2_SNR_AMP) {
            SystemConfig.mDoubleSViewGrayLevelStepRatio = SystemConfig.DOUBLE_SV_GRAYLEVEL_DRAW_STEP_RATIO_HUANG_SNR_AMP;
        }else if(SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_3_SNR_POWER) {
            SystemConfig.mDoubleSViewGrayLevelStepRatio = SystemConfig.DOUBLE_SV_GRAYLEVEL_DRAW_STEP_RATIO_HUANG_SNR_AMP
                                                * SystemConfig.DOUBLE_SV_GRAYLEVEL_DRAW_STEP_RATIO_HUANG_SNR_AMP;
        }else{
            SystemConfig.mDoubleSViewGrayLevelStepRatio = SystemConfig.DOUBLE_SV_GRAYLEVEL_DRAW_STEP_RATIO_GM;
        }
    }

    private void prepareColorMapToSignalWuMode210713() {
        double doubleStepGap;
        int iVar;
        final int grayBoundary = 45;
        doubleStepGap = SystemConfig.mDoubleNoiseRangeWu * 0.0223;  // *0.9 to reduce contrast by Cavin
        //doubleStepGap = (SystemConfig.mDoubleSignalStrengthMaxWu - SystemConfig.mDoubleNoiseStrengthWu) / 255.0;
        mDoubleColorSignalAmps[grayBoundary] = SystemConfig.mDoubleNoiseStrengthWu;

        for(iVar = 1 ; iVar< DRAW_COLOR_SIZE ; iVar++){
            if (iVar<grayBoundary){
                mDoubleColorSignalAmps[iVar] = mDoubleColorSignalAmps[grayBoundary] + doubleStepGap * (grayBoundary - iVar);
            } else {
                mDoubleColorSignalAmps[iVar] = mDoubleColorSignalAmps[grayBoundary] + doubleStepGap * (iVar);
            }
//            Log.d("prepareColorMapToSignalWuMode210713", String.valueOf(mDoubleColorSignalAmps[iVar]));
        }
    }


    private void prepareColorMapToSignalWuMode() {
        double doubleStepGap;
        int iVar;

        doubleStepGap = SystemConfig.mDoubleNoiseRangeWu * 0.1;  // *0.9 to reduce contrast by Cavin
        //doubleStepGap = (SystemConfig.mDoubleSignalStrengthMaxWu - SystemConfig.mDoubleNoiseStrengthWu) / 255.0;
        mDoubleColorSignalAmps[0] = SystemConfig.mDoubleNoiseStrengthWu*0.01;
        for(iVar = 1 ; iVar< DRAW_COLOR_SIZE ; iVar++){
            mDoubleColorSignalAmps[iVar] = mDoubleColorSignalAmps[0] + doubleStepGap * (iVar);
        }
    }

    private void prepareColorMapToSignalWuModeOnline() {
        double doubleStepGap;
        int iVar;

        doubleStepGap = SystemConfig.mDoubleNoiseRangeWu;  // *0.9 to reduce contrast by Cavin
        //doubleStepGap = (SystemConfig.mDoubleSignalStrengthMaxWu - SystemConfig.mDoubleNoiseStrengthWu) / 255.0;
        mDoubleColorSignalAmps[0] = SystemConfig.mDoubleNoiseStrengthWu;
        for(iVar = 1 ; iVar< DRAW_COLOR_SIZE ; iVar++){
            mDoubleColorSignalAmps[iVar] = mDoubleColorSignalAmps[0] + doubleStepGap * (iVar);
        }
    }


    private void prepareColorMapToSignalByRatioWuNewMode() {
        double doubleStepGap, doubleGrayTopSignal;
        double doubleLogStepGap;
        double doubleDebug;
        int iVar, iVar2;

        doubleDebug = SystemConfig.mDoubleSignalMaxForLearnAfterNormWuNew;
        doubleGrayTopSignal = 1.0/SystemConfig.mDoubleDivideParamForPlotterGrayMaxWuNew;
        doubleLogStepGap = Math.log10(SystemConfig.mDoubleSignalMinForLearnAfterNormWuNew/ doubleGrayTopSignal) / 255;
        doubleStepGap = Math.pow(10.0, doubleLogStepGap);
        mDoubleColorSignalAmps[255] = doubleGrayTopSignal;
        for(iVar = 1 ; iVar< DRAW_COLOR_SIZE ; iVar++){
            if(SystemConfig.darkMode){
                iVar2 = iVar;
            }else{
                iVar2 = 255-iVar;
            }
            mDoubleColorSignalAmps[iVar2] = mDoubleColorSignalAmps[iVar2+1] * doubleStepGap;
        }
        SystemConfig.mDoubleSViewGrayLevelStepRatio = doubleStepGap;
    }


    private void prepareColorMapToSignalByDiffWuNewMode() {
        double doubleStepGap, doubleGrayTopSignal;
        double doubleDiffStepGap;
        double doubleDebug;
        int iVar, iVar2;

        doubleGrayTopSignal = 1.0/SystemConfig.mDoubleDivideParamForPlotterGrayMaxWuNew;
        doubleDiffStepGap = (doubleGrayTopSignal - SystemConfig.mDoubleSignalMinForLearnAfterNormWuNew) / 255;
        mDoubleColorSignalAmps[255] = doubleGrayTopSignal;
        for(iVar = 1 ; iVar< DRAW_COLOR_SIZE ; iVar++){
            if (SystemConfig.darkMode){
                iVar2 = iVar;
            }else{
                iVar2 = 255-iVar;
            }
            mDoubleColorSignalAmps[iVar2] = mDoubleColorSignalAmps[iVar2+1] - doubleDiffStepGap;
        }
    }


    private void prepareColorMapToSignalByRatioWuMode_20180() {
        double doubleStepGap;
        int iVar;

        //doubleStepGap = SystemConfig.mDoubleNoiseRangeWu;
        doubleStepGap = (SystemConfig.mDoubleSignalStrengthMaxWu - SystemConfig.mDoubleNoiseStrengthWu) / 255.0;
        mDoubleColorSignalAmps[0] = SystemConfig.mDoubleNoiseStrengthWu;
        for(iVar = 1 ; iVar< DRAW_COLOR_SIZE ; iVar++){
            mDoubleColorSignalAmps[iVar] = mDoubleColorSignalAmps[0] + doubleStepGap * (iVar);
        }
    }


    private int getPaintColor(double doubleSignalStrength) {
        if(SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_0_SNSI_GM) {
            return getPaintColorWuMode(doubleSignalStrength);
        }else if(SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_1_WU_NEW) {
            return getPaintColorWuMode(doubleSignalStrength);
        }else{
            return getPaintColorHuangMode(doubleSignalStrength);
        }
    }


    private int getPaintColorHuangMode(double doubleSignalStrength) {
        int iTheIdx, iColor;
        //int iRed, iGreen, iBlue;

        iTheIdx = (DRAW_COLOR_SIZE-1) - (int) (Math.log10(doubleSignalStrength) / Math.log10(SystemConfig.mDoubleSViewGrayLevelStepRatio));
        if (iTheIdx >= DRAW_COLOR_SIZE) {
            iTheIdx = DRAW_COLOR_SIZE-1;
        }else if (iTheIdx < 0){
            iTheIdx = 0;
        }

        iColor = Color.rgb(mIntArrayDrawColorsGrayMode[iTheIdx][0], mIntArrayDrawColorsGrayMode[iTheIdx][1], mIntArrayDrawColorsGrayMode[iTheIdx][2]);
        return iColor;
    }


    private int getPaintColorWuNewMode(double doubleSignalStrength) {
        int iTheIdx, iColor;
        //int iRed, iGreen, iBlue;

        iTheIdx = (DRAW_COLOR_SIZE-1) - (int) (Math.log10(doubleSignalStrength) / Math.log10(SystemConfig.mDoubleSViewGrayLevelStepRatio));
        if (iTheIdx >= DRAW_COLOR_SIZE) {
            iTheIdx = DRAW_COLOR_SIZE-1;
        }else if (iTheIdx < 0){
            iTheIdx = 0;
        }

        iColor = Color.rgb(mIntArrayDrawColorsGrayMode[iTheIdx][0], mIntArrayDrawColorsGrayMode[iTheIdx][1], mIntArrayDrawColorsGrayMode[iTheIdx][2]);
        return iColor;
    }



    private int getPaintColorWuMode(double doubleSignalStrength) {
        int iTheIdx, iColor;
        //int iRed, iGreen, iBlue;

        iTheIdx = Arrays.binarySearch(mDoubleColorSignalAmps, doubleSignalStrength);
//        Log.d("getPaintColorWuMode", String.valueOf(iTheIdx));
        if (iTheIdx < 0) {
            iTheIdx = -iTheIdx-1;
        }

        if (iTheIdx >= DRAW_COLOR_SIZE) {
            iTheIdx = DRAW_COLOR_SIZE - 1;
        }

        iColor = Color.rgb(mIntArrayDrawColorsGrayMode[iTheIdx][0], mIntArrayDrawColorsGrayMode[iTheIdx][1], mIntArrayDrawColorsGrayMode[iTheIdx][2]);

        return iColor;
    }

    public void drawFreqScaleByHolder(SurfaceHolder inputSFHolder){
        int iScaleSize = 8;
        double mSVDrawScaleMax = mDoubleSvDrawScaleMax;
        double doubleScaleGap = mSVDrawScaleMax / (double)iScaleSize;

        double doubleScale;

        float iDrawTextSize;// = 50;
        int [] iDrawPosition = new int[iScaleSize];
        //double doubleMaxScaleBase10, doubleMaxSvForMaxScale, doubleSvGap, doubleScaleGapBase10;

        //int  iScaleSizeMax, iScaleGap10, iMaxScale10, iScale, iDrawTextSize;
        //double doubleScaleGap, doubleScale, doubleScaleAdjust = 1.23;

        Paint paintVar = new Paint();
        if (SystemConfig.darkMode){
            paintVar.setColor(Color.WHITE);
        }else{
            paintVar.setColor(Color.BLACK);
        }
        //paintVar.setTextSize(iDrawTextSize);

        Canvas canvas = inputSFHolder.lockCanvas();
        if (canvas != null) {
            double fSurfaceViewHeight = canvas.getHeight();
            double fSurfaceViewWidth = canvas.getWidth();

            iDrawTextSize = (float) (fSurfaceViewWidth/2.5);
//            Log.d(TAG,"fSurfaceViewWidth = "+fSurfaceViewWidth);
//            Log.d(TAG,"iDrawTextSize = "+iDrawTextSize);
            paintVar.setTextSize(iDrawTextSize);
            double doubleSvGap =  (double) fSurfaceViewHeight / (double)iScaleSize;
            for(int iVar = 0; iVar < iScaleSize; iVar++) {
                iDrawPosition[iVar] = (int) ((iVar + 1) * doubleSvGap + (iDrawTextSize / 2));
            }
            if (SystemConfig.darkMode){
                canvas.drawColor(Color.BLACK);
            }else{
                canvas.drawColor(Color.WHITE);
            }
//            synchronized (inputSFHolder) {
            canvas.drawText("m/s", 0, iDrawTextSize, paintVar);

            // *2.0 => divided by cos60, change to *2 / (cosTx + cosRx)

            double rxAngle = SystemConfig.rxAngle;
            double txAngle = rxAngle - 5.0;
            double cos_rxAngle = Math.cos(((rxAngle / 180.0) * Math.PI));
            double cos_txAngle = Math.cos(((txAngle / 180.0) * Math.PI));

            for (int iVar = 0; iVar < iScaleSize; iVar++) {
                doubleScale = (iVar + 1) * doubleScaleGap;
                if(SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_0_SNSI_GM) {
                    canvas.drawText("-" + String.format("%.2f", doubleScale * 2.0/(cos_rxAngle+cos_txAngle)), 0, iDrawPosition[iVar], paintVar);
                }else{
                    canvas.drawText("-" + String.format("%.2f", doubleScale), 0, iDrawPosition[iVar], paintVar);
                }
            }
//            }

            inputSFHolder.unlockCanvasAndPost(canvas);
            //canvas = null;
        }
    }

    private void drawFreqScale(SurfaceView inputSV){
        Log.d(TAG,"drawFreqScale");
        //surfaceHolder = oFrag.mSurfaceViewScale.getHolder(); //SystemConfig.mFragment.mSurfaceViewScale.getHolder();

       // try {
            mSurfaceHolderScale = inputSV.getHolder();
            mSurfaceHolderScale.addCallback(new SurfaceHolder.Callback() {

                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    Log.d(TAG,"surfaceHolder surfaceCreated!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    drawFreqScaleByHolder(surfaceHolder);
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                    Log.d(TAG,"surfaceHolder Changed!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

                }
            });

       // } catch (Exception e) {
       //     e.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugStr("BVPlotter.drawScale.Exception", "");
            // SystemConfig.mMyEventLogger.appendDebugStr(e.toString(), "");
       // }
    }




        public int getSubSegIdxFromSurfaceView (float fPosX){
            int iSubSegIdx;
            iSubSegIdx =  mIntDrawSubSegStart + (int)((double) fPosX / (double) mFloatXGainDrawSizeToPointsRatio);
            return iSubSegIdx;
        }

    protected void setTimeScalePoint(float fPos, boolean boolTimeScaleDown, boolean boolTimeScaleUp){
            if (oFrag == null&&offFrag==null){
                return;
            }
        Paint paintPoint;
        float fWidth;
        Rect rectArea;
        int iRectHeight, iRectWidth, iStartX, iEndX;
        Canvas canvas;

        if (oFrag!=null){
            onlineFragment.mFloatTimeScalePosCur = fPos;
        }else {
            offlineFragment.mFloatTimeScalePosCur = fPos;
        }

        fWidth = mSurfaceViewTimeScaleDown.getWidth();//oFrag.mSurfaceViewTimeScaleDown.getWidth(); //SystemConfig.mFragment.mSurfaceViewTimeScale.getWidth();
        iRectHeight = mSurfaceViewTimeScaleDown.getHeight();//(int) oFrag.mSurfaceViewTimeScaleDown.getHeight(); //SystemConfig.mFragment.mSurfaceViewTimeScale.getHeight();

        paintPoint = new Paint();
        if(SystemConfig.darkMode){
            paintPoint.setColor(Color.WHITE);
        }else{
            paintPoint.setColor(Color.BLACK);
        }

        if(fPos < (iRectHeight /2)){
            iStartX = 0;
        }else{
            iStartX = (int)fPos - (iRectHeight /2);
        }
        iEndX = (int)fPos + (iRectHeight /2);

        try {
            if(boolTimeScaleDown) {
                if (mSurfaceHolderTimeScaleDown == null) {
                    if (oFrag!=null){
                        mSurfaceHolderTimeScaleDown = oFrag.mSurfaceViewTimeScaleDown.getHolder(); //SystemConfig.mFragment.mSurfaceViewTimeScale.getHolder();
                    }else if (offFrag!=null){
                        mSurfaceHolderTimeScaleDown = offFrag.mSurfaceViewTimeScaleDown.getHolder();
                    }else {
                        return;
                    }
                }
                canvas = mSurfaceHolderTimeScaleDown.lockCanvas();
                if (SystemConfig.darkMode){
                    canvas.drawColor(Color.BLACK);
                }else{
                    canvas.drawColor(Color.WHITE);
                }
                rectArea = new Rect(iStartX, 0, iEndX, iRectHeight);
                canvas.drawRect(rectArea, paintPoint);
                mSurfaceHolderTimeScaleDown.unlockCanvasAndPost(canvas);
                canvas = null;
            }

            if(boolTimeScaleUp) {
                if (mSurfaceHolderTimeScaleUp == null) {
                    if (oFrag!=null){
                        mSurfaceHolderTimeScaleUp = oFrag.mSurfaceViewTimeScaleUp.getHolder();
                    }else if (offFrag!=null){
                        mSurfaceHolderTimeScaleUp = offFrag.mSurfaceViewTimeScaleUp.getHolder();
                    }else {
                        return;
                    }
                }
                canvas = mSurfaceHolderTimeScaleUp.lockCanvas();
                if (SystemConfig.darkMode){
                    canvas.drawColor(Color.BLACK);
                }else{
                    canvas.drawColor(Color.WHITE);
                }

                rectArea = new Rect(iStartX, 0, iEndX, iRectHeight);
                canvas.drawRect(rectArea, paintPoint);
                mSurfaceHolderTimeScaleUp.unlockCanvasAndPost(canvas);
                canvas = null;
            }

            mFloatTimeScalePoint = fPos;
        }catch(Exception ex1){
            ex1.printStackTrace();
           // SystemConfig.mMyEventLogger.appendDebugStr("setTimeScalePoint.Exception","");
           // SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }


    protected void setTimeScaleLineWithCanvas(Canvas canvas){
        Paint paintLine;
        SurfaceHolder surfaceHolder;
        float fWidth, fHeight, fPosStartX, fPosStartY, fPosEndX, fPosEndY ;
        Rect rectArea;
        int iRectLength;

        try {
            fWidth = mSurfaceViewBV.getWidth(); //SystemConfig.mFragment.mSurfaceViewUltrasound.getWidth();
            fHeight = mSurfaceViewBV.getHeight(); //SystemConfig.mFragment.mSurfaceViewUltrasound.getHeight();

            paintLine = new Paint();
            if (SystemConfig.darkMode){
                paintLine.setColor(Color.WHITE);
            }else {
                paintLine.setColor(Color.BLACK);
            }
            paintLine.setStrokeWidth(mIntTimeScaleLineStrokeWidth);

            if((int)mFloatTimeScalePoint < (mIntTimeScaleLineStrokeWidth/2)){
                fPosStartX = 0;
                //fPosStartX = (float) (fWidth / 2.0);
            }else{
                fPosStartX = mFloatTimeScalePoint - (mIntTimeScaleLineStrokeWidth/2);
            }
            fPosStartY = 0;
            fPosEndX = fPosStartX;
            fPosEndY = fHeight;

            canvas.drawLine(fPosStartX, fPosStartY, fPosEndX, fPosEndY, paintLine);
        }catch(Exception ex1){
           // SystemConfig.mMyEventLogger.appendDebugStr("setTimeScaleLineWithCanvas.Exception","");
           // SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            ex1.printStackTrace();
        }
    }



    protected void setTimeScaleLine(){
        Paint paintLine;
        float fWidth, fHeight, fPosStartX, fPosStartY, fPosEndX, fPosEndY ;
        Rect rectArea;
        int iRectLength, iStrokeWidth;

        try{
            iStrokeWidth = 10;

            fWidth = mSurfaceViewBV.getWidth();//SystemConfig.mFragment.mSurfaceViewUltrasound.getWidth();
            fHeight = mSurfaceViewBV.getHeight(); //SystemConfig.mFragment.mSurfaceViewUltrasound.getHeight();

            paintLine = new Paint();
            if (SystemConfig.darkMode){
                paintLine.setColor(Color.WHITE);
            }else{
                paintLine.setColor(Color.BLACK);
            }
            paintLine.setStrokeWidth(iStrokeWidth);

            if((int)mFloatTimeScalePoint < (iStrokeWidth/2)){
                fPosStartX = 0;
                //fPosStartX = (float) (fWidth / 2.0);
            }else{
                fPosStartX = mFloatTimeScalePoint - (iStrokeWidth/2);
            }
            fPosStartY = 0;
            fPosEndX = fPosStartX;
            fPosEndY = fHeight;

            /*if (mSurfaceHolderBVOffLine == null) {
                Log.d(TAG,"[1231] mSurfaceHolderBVOffLine == null");
                mSurfaceHolderBVOffLine = mSurfaceViewBV.getHolder(); //SystemConfig.mFragment.mSurfaceViewUltrasound.getHolder();
            }*/

            mCanvasBVOffLine= mSurfaceHolderBVOffLine.lockCanvas();
            if (SystemConfig.darkMode){
                mCanvasBVOffLine.drawColor(Color.BLACK);
            }else{
                mCanvasBVOffLine.drawColor(Color.WHITE);
            }
            mCanvasBVOffLine.drawLine(fPosStartX, fPosStartY, fPosEndX, fPosEndY, paintLine);
            mSurfaceHolderBVOffLine.unlockCanvasAndPost(mCanvasBVOffLine);
            mCanvasBVOffLine= null;

        }catch(Exception ex1){
            ex1.printStackTrace();
           // SystemConfig.mMyEventLogger.appendDebugStr("setTimeScaleLine.Exception","");
           // SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }


    public void drawBVSvByTimeScaleTouchDown(float fPos){
        Paint paintClear;

        if (MainActivity.mRawDataProcessor.mIntDataNextIndex == 0) {
            return;
        }

        paintClear = new Paint();
        paintClear.setStrokeWidth(mPaintVelocity.getStrokeWidth());
        if (SystemConfig.darkMode){
            paintClear.setColor(Color.BLACK);
        }else{
            paintClear.setColor(Color.WHITE);
        }
        setTimeScalePoint(fPos, true, true);
    }



    public void drawBVSvByTimeScaleTouchMove(float fPos){
        try {

            drawBVSvByTimeScaleTouchDown(fPos);

        }catch(Exception ex1){
            ex1.printStackTrace();
           // SystemConfig.mMyEventLogger.appendDebugStr("Plotter.drawBVSvByTScaleTouchMove.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }


    public void drawBVSvByTimeScaleTouchUp(float fPos){
        try {
            Log.d(TAG,"drawBVSvByTimeScaleTouchUp("+fPos+")");
            drawBVSvByTimeScaleTouchDown(fPos);
            drawBloodVelocityByStartEndSubSegIdxOffLine(mIntDrawSubSegStart, mIntDrawSubSegEnd, true);

        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("Plotter.drawBVSvByTScaleTouchUp.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            ex1.printStackTrace();
        }
    }




    public int getTimeScaleSubSegmentIdx(float fPos){
        int iDrawSubSegSize, iSubSegIdxCur;

        mFloatSurfaceViewWidth = mSurfaceViewBV.getWidth();
        mFloatSurfaceViewHeight = mSurfaceViewBV.getHeight();
        iDrawSubSegSize = mIntDrawSubSegEnd - mIntDrawSubSegStart + 1;
        mFloatXGainDrawSizeToPointsRatio = mFloatSurfaceViewWidth / ((float) iDrawSubSegSize + 1);

        iSubSegIdxCur = mIntDrawSubSegStart + (int) (fPos / mFloatXGainDrawSizeToPointsRatio) -1;
        if(iSubSegIdxCur < 0){
            iSubSegIdxCur = 0;
        }
        return iSubSegIdxCur;
    }
}
