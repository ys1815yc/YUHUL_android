package com.gis.heartio.UIOperationControlSubsystem;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
//import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.SystemConfig;

public class EcgSegView extends View {
    private final static String TAG = EcgSegView.class.getSimpleName();
    public int inputTryEndSubSegIdx = 0;
    private boolean isClear = false;
    public int mViewDrawOnlineNextSubSegIdx;
    public boolean isRunning = false;

    private final Paint mPaintECG;

    public EcgSegView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaintECG = new Paint();
        mPaintECG.setColor(Color.rgb(96,122,96));
        mPaintECG.setStrokeWidth((float) 3.0);
    }

    @Override
    public void onDraw(Canvas canvas){
        if (isClear){
            isClear = false;
            if (SystemConfig.darkMode){
                canvas.drawColor(Color.BLACK);
            }else{
                canvas.drawColor(Color.WHITE);
            }
            return;
        }

        if (isRunning){
            drawByEndSubSegIdxOnline(canvas, inputTryEndSubSegIdx);
        }
    }

    public void clearBVSegUIView(){
        isClear = true;
        this.invalidate();
    }

    public void drawByEndSubSegIdxOnline(Canvas canvas, int iTryEndSubSegIdxParam){
        if (iTryEndSubSegIdxParam < mViewDrawOnlineNextSubSegIdx){
            return;
        }

        int iTryStartSubSegIdx, iTryEndSubSegIdx;
        int iTryXVar, iXVar;
        float fPosX;
        int iDrawSubSegSize;
        float fSurfaceViewWidth, fSurfaceViewHeight;
        float mFloatBasicGainX, mFloatBasicGainY;
        short[][] shortSignalToShow = MainActivity.mBVSignalProcessorPart1.mShortECGValues;

        iTryEndSubSegIdx = mViewDrawOnlineNextSubSegIdx;
        iTryStartSubSegIdx = mViewDrawOnlineNextSubSegIdx - SystemConfig.INT_SVIEW_ONLINE_DRAW_SUBSEG_SIZE + 1;
        fSurfaceViewWidth = canvas.getWidth();
        fSurfaceViewHeight = canvas.getHeight();

        iDrawSubSegSize = SystemConfig.INT_SVIEW_ONLINE_DRAW_SUBSEG_SIZE;

        mFloatBasicGainY = fSurfaceViewHeight / (float)4000.0;
        mFloatBasicGainX = fSurfaceViewWidth / (float)(( iDrawSubSegSize*4)+1);
        if (SystemConfig.darkMode){
            canvas.drawColor(Color.BLACK);
        }else{
            canvas.drawColor(Color.WHITE);
        }
        iTryXVar = iTryStartSubSegIdx;
//        String logX = "";
        while (iTryXVar <= iTryEndSubSegIdx) {
            fPosX = (float) (iTryXVar - iTryStartSubSegIdx + 1) * (mFloatBasicGainX * 4);
            iXVar = iTryXVar % MainActivity.mBVSignalProcessorPart1.mIntTotalSubSegMaxSize;

            for (int j=0;j<SystemConfig.mIntEcgSegCnt;j++){
                float y1 = fSurfaceViewHeight -(shortSignalToShow[iXVar][j] * mFloatBasicGainY) -1;
                float y2;
                if (j+1>=SystemConfig.mIntEcgSegCnt){
                    if((iXVar+1)<MainActivity.mBVSignalProcessorPart1.mIntTotalSubSegMaxSize){
                        y2 = fSurfaceViewHeight - (shortSignalToShow[iXVar + 1][(j + 1) % 4] * mFloatBasicGainY) - 1;
                    }else{
                        y2 = fSurfaceViewHeight - (shortSignalToShow[0][(j + 1) % 4] * mFloatBasicGainY) - 1;
                    }
                }else {
                    y2 = fSurfaceViewHeight - (shortSignalToShow[iXVar][j+1] * mFloatBasicGainY) -1;
                }
//                canvas.drawPoint(fPosX,y1,mPaintECG);

                canvas.drawLine(fPosX, y1,
                        fPosX+mFloatBasicGainX, y2, mPaintECG);
                fPosX+=mFloatBasicGainX;
//                logX = logX +""+ fPosX+" ,";
            }

            iTryXVar++;
        }
    }
}
