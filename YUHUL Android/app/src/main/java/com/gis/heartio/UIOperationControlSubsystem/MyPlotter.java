package com.gis.heartio.UIOperationControlSubsystem;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by brandon on 2017/10/31.
 */

public class MyPlotter {

    private Context context;
    SurfaceView mSVUltrasound;

    public MyPlotter(){

    }

    public  MyPlotter(SurfaceView inputSV){
        this.mSVUltrasound = inputSV;
    }

    protected void clearUltrasoundSV() {
        SurfaceHolder surfaceHolder;
        Canvas canvas = null;

        //surfaceHolder = SystemConfig.mFragment.mSurfaceViewUltrasound.getHolder();
        surfaceHolder = mSVUltrasound.getHolder();
        canvas = surfaceHolder.lockCanvas();
        canvas.drawColor(Color.WHITE);
        surfaceHolder.unlockCanvasAndPost(canvas);
    }
}
