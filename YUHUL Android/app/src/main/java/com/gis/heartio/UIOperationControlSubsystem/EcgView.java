package com.gis.heartio.UIOperationControlSubsystem;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.gis.heartio.SignalProcessSubsystem.ecgResult;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.SystemConfig;

import java.util.List;

public class EcgView extends View {
    private final static String TAG = EcgView.class.getSimpleName();
    private boolean isClear = false;
    public boolean isRunning = false;
    public int inputEndSubSegIdx = 0;
    public int inputStartSubSegIdx = 0;

    private final Paint mPaintECG, mPaintRPeak, mPaintVtiEnd;

    public EcgView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mPaintECG = new Paint();
        mPaintECG.setColor(Color.rgb(96,122,96));
        mPaintECG.setStrokeWidth((float) 3.0);

        mPaintRPeak = new Paint();
        mPaintRPeak.setColor(Color.RED);

        mPaintVtiEnd = new Paint();
        mPaintVtiEnd.setColor(Color.BLUE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isClear){
            isClear = false;
            if (SystemConfig.darkMode){
                canvas.drawColor(Color.BLACK);
            }else{
                canvas.drawColor(Color.WHITE);
            }
        } else {
            //if (isRunning){
                drawBloodVelocityByStartEndSubSegIdxOffLine(canvas,inputStartSubSegIdx,inputEndSubSegIdx);
            //}
        }
    }

    public void clearBVSegUIView(){
        isClear = true;
        this.invalidate();
    }

    public void drawBloodVelocityByStartEndSubSegIdxOffLine(Canvas canvas
                                            , int iStartSubSegIdx, int iEndSubSegIdx){

        int iXVar;
        float fViewWidth, fViewHeight;
        float mFloatBasicGainY;
        float mFloatXGainDrawSizeToPointsRatio;
        int iDrawSubSegSize;
        float fPosX;
        int lastRIndex = -1;

        short[][] shortSignalToShow = MainActivity.mBVSignalProcessorPart1.mShortECGValues;

        fViewHeight = canvas.getHeight();
        fViewWidth = canvas.getWidth();

        mFloatBasicGainY = fViewHeight / (float)4000.0;
        iDrawSubSegSize = iEndSubSegIdx - iStartSubSegIdx + 1;
        mFloatXGainDrawSizeToPointsRatio = fViewWidth / ((float) iDrawSubSegSize*4 + 1);
        if (SystemConfig.darkMode){
            canvas.drawColor(Color.BLACK);
        }else{
            canvas.drawColor(Color.WHITE);
        }
        iXVar = iStartSubSegIdx;
        while (iXVar <= iEndSubSegIdx) {
            fPosX = (float) (iXVar - iStartSubSegIdx + 1) * (mFloatXGainDrawSizeToPointsRatio * 4);
            for (int j=0;j< SystemConfig.mIntEcgSegCnt;j++){
                float y1 = fViewHeight -(shortSignalToShow[iXVar][j] * mFloatBasicGainY) -1;
                float y2;
                if (j+1>=SystemConfig.mIntEcgSegCnt){
                    y2 = fViewHeight - (shortSignalToShow[iXVar+1][(j+1)%4] * mFloatBasicGainY) -1;
                }else {
                    y2 = fViewHeight - (shortSignalToShow[iXVar][j+1] * mFloatBasicGainY) -1;
                }
                canvas.drawLine(fPosX, y1,
                        fPosX+mFloatXGainDrawSizeToPointsRatio, y2, mPaintECG);

                ecgResult tmpEcg = new ecgResult();
                int tmpIndex = (iXVar*4+j);
                tmpEcg.setRPeakIndex(tmpIndex);
                if (SystemConfig.vtiSegPlot){
                    if (containRPeak(MainActivity.mBVSignalProcessorPart1.mEcgList, tmpIndex)){
                        canvas.drawLine(fPosX, 0,
                                fPosX, fViewHeight, mPaintRPeak);
                        lastRIndex = tmpIndex;
                    }
                    if (lastRIndex!=-1 && tmpIndex == lastRIndex+SystemConfig.vtiLengthECG+15){
                        canvas.drawLine(fPosX, 0,
                                fPosX, fViewHeight, mPaintVtiEnd);
                    }
                }
                fPosX+=mFloatXGainDrawSizeToPointsRatio;
            }
            iXVar++;
        }
    }

    public boolean containRPeak(final List<ecgResult> list, final int index){
        boolean found = false;
        for (ecgResult tmpEcg:list){
            if (tmpEcg.getRPeakIndex()==index) {
                found = true;
                break;
            }
        }
        return found;
    }
}
