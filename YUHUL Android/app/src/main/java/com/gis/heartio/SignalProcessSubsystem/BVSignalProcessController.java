package com.gis.heartio.SignalProcessSubsystem;

import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import com.gis.heartio.GIS_Log;
import com.gis.heartio.SignalProcessSubsysII.BVProcessSubsysII;
import com.gis.heartio.SignalProcessSubsysII.utilities.Doppler;
import com.gis.heartio.SignalProcessSubsysII.utilities.Type;
import com.gis.heartio.SignalProcessSubsysII.utilities.segObject;
import com.gis.heartio.SignalProcessSubsysII.utilities.wuDopplerInfo;
import com.gis.heartio.SupportSubsystem.IwuSQLHelper;
import com.gis.heartio.SupportSubsystem.MyMsgQueue;
import com.gis.heartio.SupportSubsystem.MyThreadQMsg;
import com.gis.heartio.SupportSubsystem.SystemConfig;
import com.gis.heartio.SupportSubsystem.dataInfo;
import com.gis.heartio.UIOperationControlSubsystem.MainActivity;
import com.gis.heartio.UIOperationControlSubsystem.UserManagerCommon;
import com.gis.heartio.UIOperationControlSubsystem.intervalForegroundService;
import com.gis.heartio.UIOperationControlSubsystem.onlineFragment;

/**
 * Created by 780797 on 2016/8/29.
 */
public class BVSignalProcessController {

    private final int INT_MSGQ_MSG_CNT_MAX = 4000;
    public int mIntPutMsgLostCnt;
    public int mIntMaxMsgInQ;

    private static int INT_SELECT_PRIORITY_HIGH = 0;
    private static int INT_SELECT_PRIORITY_LOW = 1;
    private static int INT_SELECT_PRIORITY_VERY_LOW = 2;

    private Runnable mRunnable;
    private Thread mThread;
    private MyMsgQueue mMyMsgQueue;
    private boolean mBoolEndCmd=false;
    private boolean mBoolStartedByGetFirstPower = false;

    private int mIntDebug;

    private double[] mDoubleArrayHRAverageValue, mIntArrayHRAverageSubSegs;
    private double[] mDoubleArrayHRVarianceAll, mDoubleArrayHRVarianceAllRatio;
    private double[] mDoubleArrayHRVarianceUsed,  mDoubleArrayHRVarianceUsedRatio ;
    private double[] mDoubleArrayPhantomYNCntRatio, mDoubleArrayPhantomCntAverageUsed ;
    private double[] mDoubleArrayVTIVarianceAll, mDoubleArrayVTIVarianceAllRatio;
    private double[] mDoubleArrayVTIVarianceUsed, mDoubleArrayVTIVarianceUsedRatio;
    private double[] mDoubleArrayHRAreaVariAllRatio;
    private int[] mIntArrayUsedCnt;
    private boolean[] mBoolArraySelected;
    private int[] mIntPriorityArray;

    //*********************************************
    // for Calibration Table
    //*********************************************
    public double[] mDoublePhantomAdjustTable;
    public int INT_PHANTOM_ADJUST_TABLE_DATA_CNT = 251;

    public BVSignalProcessController() {
        int iVar;

        mDoubleArrayHRAverageValue = new double[SystemConfig.INT_HR_GROUP_CNT];
        mIntArrayHRAverageSubSegs = new double[SystemConfig.INT_HR_GROUP_CNT];
        mDoubleArrayHRVarianceAll = new double[SystemConfig.INT_HR_GROUP_CNT];
        mDoubleArrayHRVarianceAllRatio = new double[SystemConfig.INT_HR_GROUP_CNT];
        mDoubleArrayHRVarianceUsed = new double[SystemConfig.INT_HR_GROUP_CNT];
        mDoubleArrayHRVarianceUsedRatio = new double[SystemConfig.INT_HR_GROUP_CNT];
        mDoubleArrayPhantomYNCntRatio = new double[SystemConfig.INT_HR_GROUP_CNT];
        mDoubleArrayPhantomCntAverageUsed = new double[SystemConfig.INT_HR_GROUP_CNT];
        mDoubleArrayVTIVarianceAll = new double[SystemConfig.INT_HR_GROUP_CNT];
        mDoubleArrayVTIVarianceAllRatio = new double[SystemConfig.INT_HR_GROUP_CNT];
        mDoubleArrayVTIVarianceUsed = new double[SystemConfig.INT_HR_GROUP_CNT];
        mDoubleArrayVTIVarianceUsedRatio = new double[SystemConfig.INT_HR_GROUP_CNT];
        mDoubleArrayHRAreaVariAllRatio = new double[SystemConfig.INT_HR_GROUP_CNT];
        mIntArrayUsedCnt = new int[SystemConfig.INT_HR_GROUP_CNT];
        mBoolArraySelected = new boolean[SystemConfig.INT_HR_GROUP_CNT];

        mDoublePhantomAdjustTable = new double[INT_PHANTOM_ADJUST_TABLE_DATA_CNT];
        initCaliTable();

        mIntPriorityArray = new int[SystemConfig.INT_HR_GROUP_CNT];
        for(iVar = 0 ; iVar < SystemConfig.INT_HR_GROUP_CNT ; iVar++){
            mIntPriorityArray[iVar] = 0 ;  // 0 : High Priority  1 : Low Priority
        }

        //mHandlerUI = SystemConfig.mFragment.mHandlerReceiveDataBySegmentOnLine;
        mMyMsgQueue = new MyMsgQueue(INT_MSGQ_MSG_CNT_MAX);
        mIntPutMsgLostCnt=0;
        mIntMaxMsgInQ = 0;

//        mRunnable = () -> SignalProcessThread();
    }


    public void startThread(int iPriority){
        try {
//            mThread = new Thread(mRunnable);
            mThread = new Thread(() -> SignalProcessThread());
            mThread.setPriority(iPriority);
            mThread.start();
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("BVCtrl.starThread.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            ex1.printStackTrace();
        }
    }

    public void stopThread(){
        if(mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
    }

    private void SignalProcessThread(){
        MyThreadQMsg myMsg;
        Message uiMessage;
        int iVar;
        long longStart, longStop;
        RawDataProcessor.ENUM_RAW_DATA_RX_STATE enumRxState;
        boolean boolReturn;
        int iUiMsgWhat, iMsgCntInQ;

        //SystemConfig.mLnThreadIdBVSignalProcController = Thread.currentThread().getId();      // Cavin Closed
        //SystemConfig.mIntPriorityBVSignalProcController = Thread.currentThread().getPriority();     // Cavin Closed

        try {
            /*longStart = SystemClock.elapsedRealtime();
            for (iVar = 0; iVar < 9999; iVar++) {
                SystemClock.elapsedRealtime();
            }
            longStop = SystemClock.elapsedRealtime();*/
            //SystemConfig.mLongAvgElapseMillis = (longStop - longStart) / 10000;   // Cavin Closed

            while (!mBoolEndCmd) {
                myMsg = mMyMsgQueue.getMsg(MyMsgQueue.MY_MSG_TIMEOUT_MILISEC_INFINITIVE);
                //Log.d("BVSPC","myMsg = "+myMsg.toString());
                if (myMsg != null) {
                    switch (myMsg.mIntMsgId) {
                        case MyThreadQMsg.INT_MY_MSG_EVT_SPROCESS_DATA_IN:
                            boolReturn = receiveAttributeDataBySegmentOnLine(myMsg.mByteArray);
                            if(boolReturn){
                                if(MainActivity.mRawDataProcessor.mEnumUltrasoundOneDataReceiveState == RawDataProcessor.ENUM_RAW_DATA_ONE_DATA_RX_STATE.RECEIVE_STATE_END){
                                    MainActivity.mSignalProcessController.putMsgForProcessDataFinish();
                                    GIS_Log.e("Leslie","RECEIVE_STATE_END");
                                }

                                if (intervalForegroundService.isRunning &&
                                        MainActivity.mRawDataProcessor.mEnumUltrasoundAttributeReceiveState
                                                == RawDataProcessor.ENUM_RAW_DATA_RX_STATE.RECEIVE_STATE_END ){
                                    //Log.d("BVSPC","intervalForegroundService.isRunning="+intervalForegroundService.isRunning);
                                    Message message = Message.obtain();
                                    message.what = intervalForegroundService.ACTION_MESSAGE_ID_RECORD_END;
                                    try{
                                        MainActivity.mServiceMessenger.send(message);
                                    }catch (RemoteException e){
                                        e.printStackTrace();
                                    }
                                }

                                if (SystemConfig.mEnumUltrasoundUIState == SystemConfig.ENUM_UI_STATE.ULTRASOUND_UI_STATE_ONLINE) {
                                    //Log.d("BVSPC","SystemConfig.mEnumUltrasoundUIState == SystemConfig.ENUM_UI_STATE.ULTRASOUND_UI_STATE_ONLINE");
                                    enumRxState = MainActivity.mRawDataProcessor.mEnumUltrasoundAttributeReceiveState;

                                    uiMessage = new Message();
                                    if (enumRxState == RawDataProcessor.ENUM_RAW_DATA_RX_STATE.RECEIVE_STATE_CONTINUOUS) {
                                        uiMessage.what = MyThreadQMsg.INT_MY_MSG_EVT_SPROCESS_DATA_IN;
//                                        GIS_Log.Leslie_LogCat("Leslie","STATE_CONTINUOUS");
                                    } else if (enumRxState == RawDataProcessor.ENUM_RAW_DATA_RX_STATE.RECEIVE_STATE_START) {
                                        uiMessage.what = MyThreadQMsg.INT_MY_MSG_EVT_SPROCESS_DATA_IN_START;
                                    }
                                    if(SystemConfig.mEnumStartState == SystemConfig.ENUM_START_STATE.STATE_START) {
                                        if (enumRxState == RawDataProcessor.ENUM_RAW_DATA_RX_STATE.RECEIVE_STATE_END) {
                                            uiMessage.what = MyThreadQMsg.INT_MY_MSG_EVT_SPROCESS_DATA_IN_END;
                                        }
                                    }
                                    iUiMsgWhat = uiMessage.what;
                                    if (onlineFragment.mHandlerReceiveDataBySegmentOnLine!=null){
                                        onlineFragment.mHandlerReceiveDataBySegmentOnLine.sendMessage(uiMessage);
                                    }

                                }

                            }
                            break;

                        case MyThreadQMsg.INT_MY_MSG_CMD_SPROCESS_DATA_FINISH:
                            if(!mBoolStartedByGetFirstPower) {
                                processAllSegmentPart1Step2OffLine();

//                                selectProcessorPart2();
//*jaufa, 180815, +
                                //* jaufa, 180805, + For One Group
                                if (SystemConfig.INT_HR_GROUP_CNT==1){


                                    MainActivity.mBVSignalProcessorPart2Array[0].processAllSegmentOne();

                                    //processAllSegmentHRByPeakMode();
                                    //processResultBloodSignal();
                                    GIS_Log.e("Leslie","in");
                                    prepareWuDoppler();
                                    MainActivity.mBVSignalProcessorPart2Array[0].processAllSegmentHRnVTIOne();
                                    MainActivity.mBVSignalProcessorPart2Array[0].processResultBloodSignalByNoSelOne();

                                    //SystemConfig.mBVSignalProcessorPart1.mIntArrayMaxIdxByMovingAverage                 // No Action
                                    //        = SystemConfig.mBVSignalProcessorPart1.mIntArrayMaxIdx;

                                    MainActivity.mBVSignalProcessorPart2Selected = MainActivity.mBVSignalProcessorPart2Array[0];
                                    SystemConfig.mBoolProcessorPart2Selected = true;

                                    MainActivity.mBVSignalProcessorPart2Selected.setHRPeriodAllDiscarded(true);
                                    MainActivity.mBVSignalProcessorPart2Selected.setVTIAllDiscarded(true);

                                    //SystemConfig.mBVSignalProcessorPart2Selected.setAllResultDiscarded();
                                    //SystemConfig.mBVSignalProcessorPart2Array[0].processResultBloodSignalOne();
                                    //selectProcessorPart2One();
                                    //selectProcessorPart2();



                                    /*
                                    MainActivity.mBVSignalProcessorPart2Array[0].processAllSegmentOne();
                                    //processAllSegmentHRByPeakMode();
                                    //processResultBloodSignal();
                                    MainActivity.mBVSignalProcessorPart2Array[0].processAllSegmentHRnVTIOne();
                                    //SystemConfig.mBVSignalProcessorPart2Array[0].processResultBloodSignalByNoSelOne();
                                    MainActivity.mBVSignalProcessorPart2Selected = MainActivity.mBVSignalProcessorPart2Array[0];
                                    SystemConfig.mBoolProcessorPart2Selected = true;
                                    MainActivity.mBVSignalProcessorPart2Selected.setHRPeriodAllDiscarded(true);
                                    MainActivity.mBVSignalProcessorPart2Selected.setVTIAllDiscarded(true);
*/
                                }else {
                                    processAllSegmentPart2OffLine();
                                    selectProcessorPart2();
                                }
//*jaufa, 180815, +  */
                                uiMessage = new Message();
                                uiMessage.what = MyThreadQMsg.INT_MY_MSG_EVT_SPROCESS_RESULT_OK;
                                if (onlineFragment.mHandlerReceiveDataBySegmentOnLine!=null){
                                    onlineFragment.mHandlerReceiveDataBySegmentOnLine.sendMessage(uiMessage);
                                }

                                if (intervalForegroundService.isRunning //&&
                                        //uiMessage.what == MyThreadQMsg.INT_MY_MSG_EVT_SPROCESS_RESULT_OK ){
                                        ){
                                    //Log.d("BVSPC","intervalForegroundService.isRunning="+intervalForegroundService.isRunning);
                                    Message message = Message.obtain();
                                    message.what = intervalForegroundService.ACTION_MESSAGE_ID_SAVE_RESULT;
                                    try{
                                        MainActivity.mServiceMessenger.send(message);
                                    }catch (RemoteException e){
                                        e.printStackTrace();
                                    }
                                }

                                //SystemConfig.mMyEventLogger.appendDebugStr("BVController.MaxMsgInQ=", String.valueOf(mIntMaxMsgInQ));
                                //SystemConfig.mMyEventLogger.appendDebugStr("BVController.MaxMsgLost=", String.valueOf(mIntPutMsgLostCnt));
                            }
                            break;

                        case MyThreadQMsg.INT_MY_MSG_CMD_SPROCESS_PREPARE_PROCESS:
                            GIS_Log.e("Leslie","PREPARE_PROCESS");
                            mBoolStartedByGetFirstPower = false;
                            mMyMsgQueue.clearMsg();
                            mIntMaxMsgInQ = 0;
                            break;

                        case MyThreadQMsg.INT_MY_MSG_CMD_SPROCESS_STOP_BY_FIRST_POWER:
                            GIS_Log.e("Leslie","FIRST_POWER");
                            mBoolStartedByGetFirstPower = true;
                            break;
                    }
                }
            }
        }catch(Exception ex1){
            Log.i("DataProController Exp", "SignalProcessThread: ");
            ex1.printStackTrace();
        }
    }

    public boolean receiveAttributeDataBySegmentOnLine(byte[] byteArray) {
        try {
            if (!MainActivity.mRawDataProcessor.receiveAttributeDataBySegmentOnLine(byteArray)) {
                return false;
            }

            if (!BVSignalProcessorPart1.isInverseFreq){
                MainActivity.mAudioPlayerController.putMsgForAudioSegmentOnLine(MainActivity.mRawDataProcessor.mIntDataNextIndex-1);
            }

            MainActivity.mBVSignalProcessorPart1.processSegment();
            //SystemConfig.mMyEventLogger.appendDebugStr("recAttrDataBySegOnline.part1.processSegment","");

            return true;
        }catch(Exception ex1){
            ex1.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugIntEvent("BVSigProcControl.reciveAttribute.Exception",0,0,0,0,0);
            return false;
        }
    }

    public boolean putMsg(MyThreadQMsg myMsg){
        if (!mMyMsgQueue.putMsg(myMsg, 0)){
            mIntPutMsgLostCnt++;
            return false;
        }
        getMaxMsgCntInQ();
        return true;
    }

    public void putMsgForProcessDataFinish(){
        MyThreadQMsg msgForProcess = new MyThreadQMsg();
        msgForProcess.mIntMsgId = MyThreadQMsg.INT_MY_MSG_CMD_SPROCESS_DATA_FINISH;
        msgForProcess.mByteArray = null;
        putMsg(msgForProcess);
    }

    public void putSignalProcessControllerMsgForPrepareCmd(){
        MyThreadQMsg myMsg = new MyThreadQMsg();
        myMsg.mIntMsgId = MyThreadQMsg.INT_MY_MSG_CMD_SPROCESS_PREPARE_PROCESS;
        putMsg(myMsg);
    }

     public void processAllSegmentPart1Step1OffLine() {
        int iSegment, iTotalSegmentSize, iSamplesSize;

        MainActivity.mBVSignalProcessorPart1.prepareStart();

        iSamplesSize = MainActivity.mRawDataProcessor.getUltrasoundCurrSamplesSizeOffLine();
        iTotalSegmentSize = (iSamplesSize * SystemConfig.mInt1DataBytes) / SystemConfig.mIntPacketDataByteSize;

        for (iSegment = 0; iSegment < iTotalSegmentSize; iSegment++) {
            MainActivity.mRawDataProcessor.setUltrasoundCurrSamplesSize((iSegment + 1) * SystemConfig.mIntPacketDataByteSize / SystemConfig.mInt1DataBytes);
            if (iSegment == 0) {
                MainActivity.mRawDataProcessor.mEnumUltrasoundOneDataReceiveState = RawDataProcessor.ENUM_RAW_DATA_ONE_DATA_RX_STATE.RECEIVE_STATE_START;
            } else if (iSegment == iTotalSegmentSize - 1) {
                MainActivity.mRawDataProcessor.mEnumUltrasoundOneDataReceiveState = RawDataProcessor.ENUM_RAW_DATA_ONE_DATA_RX_STATE.RECEIVE_STATE_END;
                GIS_Log.e("Leslie","processAllSegmentPart1Step1OffLine");
            }
            MainActivity.mBVSignalProcessorPart1.processSegment();
        }
    }


    public void processAllSegmentPart1Step2OffLine() {

        if (SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_0_SNSI_GM) {
            MainActivity.mBVSignalProcessorPart1.processAllSegmentMaxIdxBySnSiGm();
        }else if (SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_1_WU_NEW) {
            MainActivity.mBVSignalProcessorPart1.processAllSegmentMaxIdxByWuNew();
        }else {
            MainActivity.mBVSignalProcessorPart1.processAllSegmentMaxIdxBySNR();
        }

        if (SystemConfig.mBoolMaxIdxMovingAverageForVTI) {
            MainActivity.mBVSignalProcessorPart1.processMaxIdxByMovingAverage(3, SystemConfig.mIntEndIdxNoiseLearn+1 , MainActivity.mBVSignalProcessorPart1.mIntMaxIdxNextIdx - 1);
        } else {
            MainActivity.mBVSignalProcessorPart1.copyToMaxIdxForVTI();
        }
    }


    public void processAllSegmentPart2OffLine() {
        int iVar;

        try {
            if (SystemConfig.mIntPart2TestIdx == -1) {
                for (iVar = 0; iVar < SystemConfig.INT_HR_GROUP_CNT; iVar++) {
                    MainActivity.mBVSignalProcessorPart2Array[iVar].processAllSegment();
                }
            } else {
                MainActivity.mBVSignalProcessorPart2Array[SystemConfig.mIntPart2TestIdx].processAllSegment();
            }
        }catch(Exception ex1){
            ex1.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugStr("procAllSegPart2OffL.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }

    //*jaufa, 180805 For One by Jaufa
    public void selectProcessorPart2One() {
        int iVar, iSelectedCnt,iSelectIdx, iMaxCnt;
        double doubleHRVariationMin;
        int iLastPhantomIdx, iFirstHumanIdx;
        double doubleHRAreaCompareRatio;

        try {
            iSelectIdx = -1;
            MainActivity.mBVSignalProcessorPart2Selected = null;

            if (SystemConfig.mIntPart2TestIdx != -1) {
                iVar = SystemConfig.mIntPart2TestIdx;
                mDoubleArrayHRAverageValue[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleHRAverageValue;
                mIntArrayHRAverageSubSegs[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mIntHRAverageSubSeg;
                mDoubleArrayHRVarianceAll[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleHRVarianceAll;
                mDoubleArrayHRVarianceAllRatio[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleHRVarianceAllRatio;
                mDoubleArrayHRVarianceUsed[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleHRVarianceUsed;
                mDoubleArrayHRVarianceUsedRatio[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleHRVarianceUsedRatio;
                mDoubleArrayPhantomYNCntRatio[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoublePhantomYNCntRatio;
                mDoubleArrayPhantomCntAverageUsed[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoublePhantomPeakCntAverageUsed;
                mDoubleArrayVTIVarianceAll[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleVTIVarianceAll;
                mDoubleArrayVTIVarianceAllRatio[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleVTIVarianceAllRatio;
                mDoubleArrayVTIVarianceUsed[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleVTIVarianceUsed;
                mDoubleArrayVTIVarianceUsedRatio[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleVTIVarianceUsedRatio;
                mDoubleArrayHRAreaVariAllRatio[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleHRAreaVariAverageAllRatio;
                mIntArrayUsedCnt[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mIntHRAccuCntUsed;
                MainActivity.mBVSignalProcessorPart2Selected = MainActivity.mBVSignalProcessorPart2Array[SystemConfig.mIntPart2TestIdx];
                SystemConfig.mBoolProcessorPart2Selected = true;
            } else {
                for (iVar = 0; iVar < SystemConfig.INT_HR_GROUP_CNT; iVar++) {
                    mDoubleArrayHRAverageValue[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleHRAverageValue;
                    mIntArrayHRAverageSubSegs[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mIntHRAverageSubSeg;
                    mDoubleArrayHRVarianceAll[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleHRVarianceAll;
                    mDoubleArrayHRVarianceAllRatio[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleHRVarianceAllRatio;
                    mDoubleArrayHRVarianceUsed[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleHRVarianceUsed;
                    mDoubleArrayHRVarianceUsedRatio[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleHRVarianceUsedRatio;
                    mDoubleArrayPhantomYNCntRatio[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoublePhantomYNCntRatio;
                    mDoubleArrayVTIVarianceAll[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleVTIVarianceAll;
                    mDoubleArrayVTIVarianceAllRatio[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleVTIVarianceAllRatio;
                    mDoubleArrayVTIVarianceUsed[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleVTIVarianceUsed;
                    mDoubleArrayVTIVarianceUsedRatio[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleVTIVarianceUsedRatio;
                    mIntArrayUsedCnt[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mIntHRAccuCntUsed;
                    mDoubleArrayHRAreaVariAllRatio[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleHRAreaVariAverageAllRatio;
                }
            }

            //--- 心跳數>0 ， 在BVSignalProcessorPart2中計算HR，符合通過標準
            iSelectedCnt = 0;
            for (iVar = 0; iVar < SystemConfig.INT_HR_GROUP_CNT; iVar++) {
                if(MainActivity.mBVSignalProcessorPart2Array[iVar].mBoolHRResultInPart2){
                    mBoolArraySelected[iVar] = true;
                    iSelectedCnt++;
                }else {
                    mBoolArraySelected[iVar] = false;
                }
            }

            // ---- find last phantom and first human signal -----------------------------------
            iLastPhantomIdx = -1;
            iFirstHumanIdx = -1;
            if (iSelectedCnt >= 1) {
                for (iVar = 0; iVar < SystemConfig.INT_HR_GROUP_CNT; iVar++) {
                    if (mBoolArraySelected[iVar]) {
                        if ((mDoubleArrayPhantomYNCntRatio[iVar] >= 0.7) && (iFirstHumanIdx == -1)) {
                            iLastPhantomIdx = iVar;
                        }
                    }
                }
                if(iLastPhantomIdx < (SystemConfig.INT_HR_GROUP_CNT-1)){
                    for (iVar = iLastPhantomIdx+1 ; iVar < SystemConfig.INT_HR_GROUP_CNT; iVar++) {
                        if (mBoolArraySelected[iVar]) {
                            iFirstHumanIdx = iVar;
                            break;
                        }
                    }
                }
            }

            if(iLastPhantomIdx != -1) {         // Phantom Found
                if(iFirstHumanIdx == -1){       // Human no found
                    MainActivity.mBVSignalProcessorPart2Selected = MainActivity.mBVSignalProcessorPart2Array[iLastPhantomIdx];
                    SystemConfig.mBoolProcessorPart2Selected = true;

                }else{
                    doubleHRAreaCompareRatio = mDoubleArrayHRAreaVariAllRatio[iLastPhantomIdx];
                    //SystemConfig.mMyEventLogger.appendDebugStr("select.HRAreaCompare=", String.format("%.2f", doubleHRAreaCompareRatio));
                    //if ((doubleHRAreaCompareRatio <= 0.12) && (mDoubleArrayHRVarianceAll[iLastPhantomIdx] < 0.15)) {
                    if (doubleHRAreaCompareRatio <= 0.12){
                        MainActivity.mBVSignalProcessorPart2Selected = MainActivity.mBVSignalProcessorPart2Array[iLastPhantomIdx];
                    }else{
                        iSelectIdx = selectFromHumanGroup(iFirstHumanIdx);
                        MainActivity.mBVSignalProcessorPart2Selected = MainActivity.mBVSignalProcessorPart2Array[iSelectIdx];
                        SystemConfig.mBoolProcessorPart2Selected = true;
                    }
                }
            }else if(iFirstHumanIdx != -1){     //--- only human signal
                iSelectIdx = selectFromHumanGroup(iFirstHumanIdx);
                MainActivity.mBVSignalProcessorPart2Selected = MainActivity.mBVSignalProcessorPart2Array[iSelectIdx];
                SystemConfig.mBoolProcessorPart2Selected = true;
            }

            if (MainActivity.mBVSignalProcessorPart2Selected == null) {
                doubleHRVariationMin = Double.MAX_VALUE;
                for (iVar = 0; iVar < SystemConfig.INT_HR_GROUP_CNT; iVar++) {
                    if (mBoolArraySelected[iVar]) {
                        if (doubleHRVariationMin > mDoubleArrayHRVarianceAllRatio[iVar]) {
                            MainActivity.mBVSignalProcessorPart2Selected = MainActivity.mBVSignalProcessorPart2Array[iVar];
                            doubleHRVariationMin = mDoubleArrayHRVarianceAllRatio[iVar];
                        }
                    }
                }
                SystemConfig.mBoolProcessorPart2Selected = false;
            }
            if( MainActivity.mBVSignalProcessorPart2Selected == null){
                MainActivity.mBVSignalProcessorPart2Selected = MainActivity.mBVSignalProcessorPart2Array[1];
                SystemConfig.mBoolProcessorPart2Selected = false;
                MainActivity.mBVSignalProcessorPart2Selected.setAllResultDiscarded();
            }

            if(SystemConfig.mIntVTIModeIdx == SystemConfig.INT_VTI_MODE_2_TWO_POINT){
                MainActivity.mBVSignalProcessorPart2Selected.setAllResultDiscarded();
                SystemConfig.mBoolProcessorPart2Selected = false;
            }

        }catch(Exception ex1){
            ex1.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugIntEvent("selectProcessorPart2One.Exception",0,0,0,0,0);
        }
    }


    public void selectProcessorPart2() {
        int iVar, iSelectedCnt,iSelectIdx, iMaxCnt;
        double doubleHRVariationMin;
        int iLastPhantomIdx, iFirstHumanIdx;
        double doubleHRAreaCompareRatio;

        try {
            iSelectIdx = -1;
            MainActivity.mBVSignalProcessorPart2Selected = null;

            if (SystemConfig.mIntPart2TestIdx != -1) {
                iVar = SystemConfig.mIntPart2TestIdx;
                mDoubleArrayHRAverageValue[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleHRAverageValue;
                mIntArrayHRAverageSubSegs[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mIntHRAverageSubSeg;
                mDoubleArrayHRVarianceAll[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleHRVarianceAll;
                mDoubleArrayHRVarianceAllRatio[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleHRVarianceAllRatio;
                mDoubleArrayHRVarianceUsed[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleHRVarianceUsed;
                mDoubleArrayHRVarianceUsedRatio[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleHRVarianceUsedRatio;
                mDoubleArrayPhantomYNCntRatio[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoublePhantomYNCntRatio;
                mDoubleArrayPhantomCntAverageUsed[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoublePhantomPeakCntAverageUsed;
                mDoubleArrayVTIVarianceAll[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleVTIVarianceAll;
                mDoubleArrayVTIVarianceAllRatio[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleVTIVarianceAllRatio;
                mDoubleArrayVTIVarianceUsed[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleVTIVarianceUsed;
                mDoubleArrayVTIVarianceUsedRatio[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleVTIVarianceUsedRatio;
                mDoubleArrayHRAreaVariAllRatio[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleHRAreaVariAverageAllRatio;
                mIntArrayUsedCnt[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mIntHRAccuCntUsed;
                MainActivity.mBVSignalProcessorPart2Selected = MainActivity.mBVSignalProcessorPart2Array[SystemConfig.mIntPart2TestIdx];
                SystemConfig.mBoolProcessorPart2Selected = true;
                Log.d("BVSignalPC","[323] SystemConfig.mBoolProcessorPart2Selected = true");
                return;
            } else {
                for (iVar = 0; iVar < SystemConfig.INT_HR_GROUP_CNT; iVar++) {
                    mDoubleArrayHRAverageValue[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleHRAverageValue;
                    mIntArrayHRAverageSubSegs[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mIntHRAverageSubSeg;
                    mDoubleArrayHRVarianceAll[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleHRVarianceAll;
                    mDoubleArrayHRVarianceAllRatio[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleHRVarianceAllRatio;
                    mDoubleArrayHRVarianceUsed[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleHRVarianceUsed;
                    mDoubleArrayHRVarianceUsedRatio[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleHRVarianceUsedRatio;
                    mDoubleArrayPhantomYNCntRatio[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoublePhantomYNCntRatio;
                    mDoubleArrayVTIVarianceAll[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleVTIVarianceAll;
                    mDoubleArrayVTIVarianceAllRatio[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleVTIVarianceAllRatio;
                    mDoubleArrayVTIVarianceUsed[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleVTIVarianceUsed;
                    mDoubleArrayVTIVarianceUsedRatio[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleVTIVarianceUsedRatio;
                    mIntArrayUsedCnt[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mIntHRAccuCntUsed;
                    mDoubleArrayHRAreaVariAllRatio[iVar] = MainActivity.mBVSignalProcessorPart2Array[iVar].mDoubleHRAreaVariAverageAllRatio;
                }
            }

            //--- 心跳數>0 ， 在BVSignalProcessorPart2中計算HR，符合通過標準
            iSelectedCnt = 0;
            for (iVar = 0; iVar < SystemConfig.INT_HR_GROUP_CNT; iVar++) {
                if(MainActivity.mBVSignalProcessorPart2Array[iVar].mBoolHRResultInPart2){
                    mBoolArraySelected[iVar] = true;
                    iSelectedCnt++;
                }else {
                    mBoolArraySelected[iVar] = false;
                }
            }

            // ---- find last phantom and first human signal -----------------------------------
            iLastPhantomIdx = -1;
            iFirstHumanIdx = -1;
            if (iSelectedCnt >= 1) {
                for (iVar = 0; iVar < SystemConfig.INT_HR_GROUP_CNT; iVar++) {
                    if (mBoolArraySelected[iVar]) {
                        if ((mDoubleArrayPhantomYNCntRatio[iVar] >= 0.7) && (iFirstHumanIdx == -1)) {
                            iLastPhantomIdx = iVar;
                        }
                    }
                }
                if(iLastPhantomIdx < (SystemConfig.INT_HR_GROUP_CNT-1)){
                    for (iVar = iLastPhantomIdx+1 ; iVar < SystemConfig.INT_HR_GROUP_CNT; iVar++) {
                        if (mBoolArraySelected[iVar]) {
                            iFirstHumanIdx = iVar;
                            break;
                        }
                    }
                }
            }

            if(iLastPhantomIdx != -1) {         // Phantom Found
                if(iFirstHumanIdx == -1){       // Human no found
                    MainActivity.mBVSignalProcessorPart2Selected = MainActivity.mBVSignalProcessorPart2Array[iLastPhantomIdx];
                    SystemConfig.mBoolProcessorPart2Selected = true;
                    //Log.d("BVSignalPC","[378] SystemConfig.mBoolProcessorPart2Selected = true");

                }else{
                    doubleHRAreaCompareRatio = mDoubleArrayHRAreaVariAllRatio[iLastPhantomIdx];
                    //SystemConfig.mMyEventLogger.appendDebugStr("select.HRAreaCompare=", String.format("%.2f", doubleHRAreaCompareRatio));
                    //if ((doubleHRAreaCompareRatio <= 0.12) && (mDoubleArrayHRVarianceAll[iLastPhantomIdx] < 0.15)) {
                    if (doubleHRAreaCompareRatio <= 0.12){
                        MainActivity.mBVSignalProcessorPart2Selected = MainActivity.mBVSignalProcessorPart2Array[iLastPhantomIdx];
                    }else{
                        iSelectIdx = selectFromHumanGroup(iFirstHumanIdx);
                        MainActivity.mBVSignalProcessorPart2Selected = MainActivity.mBVSignalProcessorPart2Array[iSelectIdx];
                        SystemConfig.mBoolProcessorPart2Selected = true;
                        //Log.d("BVSignalPC","[390] SystemConfig.mBoolProcessorPart2Selected = true");
                    }
                }
            }else if(iFirstHumanIdx != -1){     //--- only human signal
                iSelectIdx = selectFromHumanGroup(iFirstHumanIdx);
                MainActivity.mBVSignalProcessorPart2Selected = MainActivity.mBVSignalProcessorPart2Array[iSelectIdx];
                SystemConfig.mBoolProcessorPart2Selected = true;
                //Log.d("BVSignalPC","[397] SystemConfig.mBoolProcessorPart2Selected = true");
            }

            if (MainActivity.mBVSignalProcessorPart2Selected == null) {
                doubleHRVariationMin = Double.MAX_VALUE;
                for (iVar = 0; iVar < SystemConfig.INT_HR_GROUP_CNT; iVar++) {
                    if (mBoolArraySelected[iVar]) {
                        if (doubleHRVariationMin > mDoubleArrayHRVarianceAllRatio[iVar]) {
                            MainActivity.mBVSignalProcessorPart2Selected = MainActivity.mBVSignalProcessorPart2Array[iVar];
                            doubleHRVariationMin = mDoubleArrayHRVarianceAllRatio[iVar];
                        }
                    }
                }

                //Log.d("BVSignalPC","[411] SystemConfig.mBoolProcessorPart2Selected = false");
            }
            if( MainActivity.mBVSignalProcessorPart2Selected == null){
                MainActivity.mBVSignalProcessorPart2Selected = MainActivity.mBVSignalProcessorPart2Array[1];
                MainActivity.mBVSignalProcessorPart2Selected.setAllResultDiscarded();
                SystemConfig.mBoolProcessorPart2Selected = false;
                //Log.d("BVSignalPC","[416] SystemConfig.mBoolProcessorPart2Selected = false");
            }

            if(SystemConfig.mIntVTIModeIdx == SystemConfig.INT_VTI_MODE_2_TWO_POINT){
                MainActivity.mBVSignalProcessorPart2Selected.setAllResultDiscarded();
                SystemConfig.mBoolProcessorPart2Selected = false;
                //Log.d("BVSignalPC","[423] SystemConfig.mBoolProcessorPart2Selected = false");
            }

        }catch(Exception ex1){
            ex1.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugIntEvent("selectProcessorPart2.Exception",0,0,0,0,0);
        }
    }

    private void getMaxMsgCntInQ(){
        int iMsgCntInQ;

        iMsgCntInQ = INT_MSGQ_MSG_CNT_MAX - mMyMsgQueue.getRemainCapacity();
        if(mIntMaxMsgInQ < iMsgCntInQ){
            mIntMaxMsgInQ = iMsgCntInQ;
        }
    }




    public double adjustValueByCali(double doubleVpk) {
        if (SystemConfig.mIntCalibrationAdjustType == SystemConfig.INT_CALIBRATION_TYPE_0_NO_CALI) {
            return doubleVpk;
        } else if (SystemConfig.mIntCalibrationAdjustType == SystemConfig.INT_CALIBRATION_TYPE_1_TABLE) {
            return adjustValueFromTable(doubleVpk);
        } else {
            return (doubleVpk * 2.0);
        }
    }


    public double adjustValueFromTable(double doubleVpk) {
        int iVpk1000;
        int iVpk100, iValue;
        double doubleValue;

        iVpk1000 = (int) (doubleVpk * 1000);
        iVpk100 = iVpk1000 / 10;
        iValue = iVpk1000 % 10;
        if (iValue >= 5) {
            iVpk100++;
        }
        if (iVpk100 >= INT_PHANTOM_ADJUST_TABLE_DATA_CNT) {
            iVpk100 = INT_PHANTOM_ADJUST_TABLE_DATA_CNT - 1;
        }

        doubleValue = mDoublePhantomAdjustTable[iVpk100];
        return doubleValue;
    }


    public void initCaliTable() {
        int iVar;
        double doubleGap;

        try {
            for (iVar = 0; iVar < INT_PHANTOM_ADJUST_TABLE_DATA_CNT; iVar++) {
                mDoublePhantomAdjustTable[iVar] = 0;
            }

            doubleGap = 0.249678551057612 / 0.49;
            for (iVar = 0; iVar < 49; iVar++) {
                mDoublePhantomAdjustTable[iVar] = doubleGap * iVar;
            }
            mDoublePhantomAdjustTable[49] = 0.249678551057612;
            mDoublePhantomAdjustTable[50] = 0.250781186312345;
            mDoublePhantomAdjustTable[51] = 0.252298686539741;
            mDoublePhantomAdjustTable[52] = 0.254231051739798;
            mDoublePhantomAdjustTable[53] = 0.256578281912518;
            mDoublePhantomAdjustTable[54] = 0.2593403770579;
            mDoublePhantomAdjustTable[55] = 0.262517337175944;
            mDoublePhantomAdjustTable[56] = 0.266109162266651;
            mDoublePhantomAdjustTable[57] = 0.270115852330019;
            mDoublePhantomAdjustTable[58] = 0.27453740736605;
            mDoublePhantomAdjustTable[59] = 0.279373827374742;
            mDoublePhantomAdjustTable[60] = 0.284625112356097;
            mDoublePhantomAdjustTable[61] = 0.290291262310114;
            mDoublePhantomAdjustTable[62] = 0.296372277236793;
            mDoublePhantomAdjustTable[63] = 0.302868157136134;
            mDoublePhantomAdjustTable[64] = 0.309778902008137;
            mDoublePhantomAdjustTable[65] = 0.317104511852803;
            mDoublePhantomAdjustTable[66] = 0.32484498667013;
            mDoublePhantomAdjustTable[67] = 0.33300032646012;
            mDoublePhantomAdjustTable[68] = 0.341570531222771;
            mDoublePhantomAdjustTable[69] = 0.350555600958085;
            mDoublePhantomAdjustTable[70] = 0.359955535666061;
            mDoublePhantomAdjustTable[71] = 0.369770335346699;
            mDoublePhantomAdjustTable[72] = 0.38;
            mDoublePhantomAdjustTable[73] = 0.390644529625962;
            mDoublePhantomAdjustTable[74] = 0.401703924224587;
            mDoublePhantomAdjustTable[75] = 0.413178183795873;
            mDoublePhantomAdjustTable[76] = 0.425067308339822;
            mDoublePhantomAdjustTable[77] = 0.437371297856433;
            mDoublePhantomAdjustTable[78] = 0.450090152345706;
            mDoublePhantomAdjustTable[79] = 0.463223871807641;
            mDoublePhantomAdjustTable[80] = 0.476772456242238;
            mDoublePhantomAdjustTable[81] = 0.490735905649498;
            mDoublePhantomAdjustTable[82] = 0.505114220029419;
            mDoublePhantomAdjustTable[83] = 0.519907399382003;
            mDoublePhantomAdjustTable[84] = 0.535115443707249;
            mDoublePhantomAdjustTable[85] = 0.553384052214441;
            mDoublePhantomAdjustTable[86] = 0.565226342745131;
            mDoublePhantomAdjustTable[87] = 0.577068633275821;
            mDoublePhantomAdjustTable[88] = 0.588910923806511;
            mDoublePhantomAdjustTable[89] = 0.6007532143372;
            mDoublePhantomAdjustTable[90] = 0.61259550486789;
            mDoublePhantomAdjustTable[91] = 0.62443779539858;
            mDoublePhantomAdjustTable[92] = 0.636280085929269;
            mDoublePhantomAdjustTable[93] = 0.648122376459959;
            mDoublePhantomAdjustTable[94] = 0.659964666990649;
            mDoublePhantomAdjustTable[95] = 0.671806957521339;
            mDoublePhantomAdjustTable[96] = 0.683649248052028;
            mDoublePhantomAdjustTable[97] = 0.695491538582718;
            mDoublePhantomAdjustTable[98] = 0.707333829113408;
            mDoublePhantomAdjustTable[99] = 0.719176119644098;
            mDoublePhantomAdjustTable[100] = 0.731018410174787;
            mDoublePhantomAdjustTable[101] = 0.742860700705477;
            mDoublePhantomAdjustTable[102] = 0.754702991236167;
            mDoublePhantomAdjustTable[103] = 0.766545281766857;
            mDoublePhantomAdjustTable[104] = 0.778387572297546;
            mDoublePhantomAdjustTable[105] = 0.790229862828236;
            mDoublePhantomAdjustTable[106] = 0.802072153358926;
            mDoublePhantomAdjustTable[107] = 0.813914443889616;
            mDoublePhantomAdjustTable[108] = 0.825756734420305;
            mDoublePhantomAdjustTable[109] = 0.837599024950995;
            mDoublePhantomAdjustTable[110] = 0.849441315481685;
            mDoublePhantomAdjustTable[111] = 0.861283606012374;
            mDoublePhantomAdjustTable[112] = 0.873125896543064;
            mDoublePhantomAdjustTable[113] = 0.884968187073754;
            mDoublePhantomAdjustTable[114] = 0.896810477604444;
            mDoublePhantomAdjustTable[115] = 0.908652768135133;
            mDoublePhantomAdjustTable[116] = 0.920495058665823;
            mDoublePhantomAdjustTable[117] = 0.932337349196513;
            mDoublePhantomAdjustTable[118] = 0.944179639727203;
            mDoublePhantomAdjustTable[119] = 0.956021930257892;
            mDoublePhantomAdjustTable[120] = 0.967864220788582;
            mDoublePhantomAdjustTable[121] = 0.979706511319272;
            mDoublePhantomAdjustTable[122] = 0.991548801849962;
            mDoublePhantomAdjustTable[123] = 1.00339109238065;
            mDoublePhantomAdjustTable[124] = 1.01523338291134;
            mDoublePhantomAdjustTable[125] = 1.02707567344203;
            mDoublePhantomAdjustTable[126] = 1.03891796397272;
            mDoublePhantomAdjustTable[127] = 1.05076025450341;
            mDoublePhantomAdjustTable[128] = 1.0626025450341;
            mDoublePhantomAdjustTable[129] = 1.07444483556479;
            mDoublePhantomAdjustTable[130] = 1.08628712609548;
            mDoublePhantomAdjustTable[131] = 1.09812941662617;
            mDoublePhantomAdjustTable[132] = 1.10997170715686;
            mDoublePhantomAdjustTable[133] = 1.12181399768755;
            mDoublePhantomAdjustTable[134] = 1.13365628821824;
            mDoublePhantomAdjustTable[135] = 1.14549857874893;
            mDoublePhantomAdjustTable[136] = 1.15734086927962;
            mDoublePhantomAdjustTable[137] = 1.16918315981031;
            mDoublePhantomAdjustTable[138] = 1.181025450341;
            mDoublePhantomAdjustTable[139] = 1.19286774087169;
            mDoublePhantomAdjustTable[140] = 1.20471003140238;
            mDoublePhantomAdjustTable[141] = 1.21655232193307;
            mDoublePhantomAdjustTable[142] = 1.22839461246376;
            mDoublePhantomAdjustTable[143] = 1.24023690299445;
            mDoublePhantomAdjustTable[144] = 1.25207919352514;
            mDoublePhantomAdjustTable[145] = 1.26392148405583;
            mDoublePhantomAdjustTable[146] = 1.27576377458652;
            mDoublePhantomAdjustTable[147] = 1.28760606511721;
            mDoublePhantomAdjustTable[148] = 1.29944835564789;
            mDoublePhantomAdjustTable[149] = 1.31129064617858;
            mDoublePhantomAdjustTable[150] = 1.32313293670927;
            mDoublePhantomAdjustTable[151] = 1.33497522723996;
            mDoublePhantomAdjustTable[152] = 1.34681751777065;
            mDoublePhantomAdjustTable[153] = 1.35865980830134;
            mDoublePhantomAdjustTable[154] = 1.37050209883203;
            mDoublePhantomAdjustTable[155] = 1.38234438936272;
            mDoublePhantomAdjustTable[156] = 1.39418667989341;
            mDoublePhantomAdjustTable[157] = 1.4060289704241;
            mDoublePhantomAdjustTable[158] = 1.41787126095479;
            mDoublePhantomAdjustTable[159] = 1.42971355148548;
            mDoublePhantomAdjustTable[160] = 1.44155584201617;
            mDoublePhantomAdjustTable[161] = 1.45339813254686;
            mDoublePhantomAdjustTable[162] = 1.46524042307755;
            mDoublePhantomAdjustTable[163] = 1.47708271360824;
            mDoublePhantomAdjustTable[164] = 1.48892500413893;
            mDoublePhantomAdjustTable[165] = 1.50076729466962;
            mDoublePhantomAdjustTable[166] = 1.51260958520031;
            mDoublePhantomAdjustTable[167] = 1.524451875731;
            mDoublePhantomAdjustTable[168] = 1.53629416626169;
            mDoublePhantomAdjustTable[169] = 1.54813645679238;
            mDoublePhantomAdjustTable[170] = 1.55997874732307;
            mDoublePhantomAdjustTable[171] = 1.57182103785376;
            mDoublePhantomAdjustTable[172] = 1.58366332838445;
            mDoublePhantomAdjustTable[173] = 1.59550561891514;
            mDoublePhantomAdjustTable[174] = 1.60734790944583;
            mDoublePhantomAdjustTable[175] = 1.61919019997652;
            mDoublePhantomAdjustTable[176] = 1.63103249050721;
            mDoublePhantomAdjustTable[177] = 1.6428747810379;
            mDoublePhantomAdjustTable[178] = 1.65471707156859;
            mDoublePhantomAdjustTable[179] = 1.66655936209928;
            mDoublePhantomAdjustTable[180] = 1.67840165262997;
            mDoublePhantomAdjustTable[181] = 1.69024394316066;
            mDoublePhantomAdjustTable[182] = 1.70208623369135;
            mDoublePhantomAdjustTable[183] = 1.71392852422204;
            mDoublePhantomAdjustTable[184] = 1.72577081475272;
            mDoublePhantomAdjustTable[185] = 1.73761310528341;
            mDoublePhantomAdjustTable[186] = 1.7494553958141;
            mDoublePhantomAdjustTable[187] = 1.76129768634479;
            mDoublePhantomAdjustTable[188] = 1.77313997687548;
            mDoublePhantomAdjustTable[189] = 1.78498226740617;
            mDoublePhantomAdjustTable[190] = 1.79682455793686;
            mDoublePhantomAdjustTable[191] = 1.80866684846755;
            mDoublePhantomAdjustTable[192] = 1.82050913899824;
            mDoublePhantomAdjustTable[193] = 1.83235142952893;
            mDoublePhantomAdjustTable[194] = 1.84419372005962;
            mDoublePhantomAdjustTable[195] = 1.85603601059031;
            mDoublePhantomAdjustTable[196] = 1.867878301121;
            mDoublePhantomAdjustTable[197] = 1.87972059165169;
            mDoublePhantomAdjustTable[198] = 1.89156288218238;
            mDoublePhantomAdjustTable[199] = 1.90340517271307;
            mDoublePhantomAdjustTable[200] = 1.91524746324376;
            mDoublePhantomAdjustTable[201] = 1.92708975377445;
            mDoublePhantomAdjustTable[202] = 1.93893204430514;
            mDoublePhantomAdjustTable[203] = 1.95077433483583;
            mDoublePhantomAdjustTable[204] = 1.96261662536652;
            mDoublePhantomAdjustTable[205] = 1.97445891589721;
            mDoublePhantomAdjustTable[206] = 1.9863012064279;
            mDoublePhantomAdjustTable[207] = 1.99814349695859;
            mDoublePhantomAdjustTable[208] = 2.00998578748928;
            mDoublePhantomAdjustTable[209] = 2.02182807801997;
            mDoublePhantomAdjustTable[210] = 2.03367036855066;
            mDoublePhantomAdjustTable[211] = 2.04551265908135;
            mDoublePhantomAdjustTable[212] = 2.05735494961204;
            mDoublePhantomAdjustTable[213] = 2.06919724014273;
            mDoublePhantomAdjustTable[214] = 2.08103953067342;
            mDoublePhantomAdjustTable[215] = 2.09288182120411;
            mDoublePhantomAdjustTable[216] = 2.1047241117348;
            mDoublePhantomAdjustTable[217] = 2.11656640226549;
            mDoublePhantomAdjustTable[218] = 2.12840869279618;
            mDoublePhantomAdjustTable[219] = 2.14025098332687;
            mDoublePhantomAdjustTable[220] = 2.15209327385756;
            mDoublePhantomAdjustTable[221] = 2.16393556438825;
            mDoublePhantomAdjustTable[222] = 2.17577785491893;
            mDoublePhantomAdjustTable[223] = 2.18762014544962;
            mDoublePhantomAdjustTable[224] = 2.19946243598031;
            mDoublePhantomAdjustTable[225] = 2.211304726511;
            mDoublePhantomAdjustTable[226] = 2.22314701704169;
            mDoublePhantomAdjustTable[227] = 2.23498930757238;
            mDoublePhantomAdjustTable[228] = 2.24683159810307;
            mDoublePhantomAdjustTable[229] = 2.25867388863376;
            mDoublePhantomAdjustTable[230] = 2.27051617916445;
            mDoublePhantomAdjustTable[231] = 2.28235846969514;
            mDoublePhantomAdjustTable[232] = 2.29420076022583;
            mDoublePhantomAdjustTable[233] = 2.30604305075652;
            mDoublePhantomAdjustTable[234] = 2.31788534128721;
            mDoublePhantomAdjustTable[235] = 2.3297276318179;
            mDoublePhantomAdjustTable[236] = 2.34156992234859;
            mDoublePhantomAdjustTable[237] = 2.35341221287928;
            mDoublePhantomAdjustTable[238] = 2.36525450340997;
            mDoublePhantomAdjustTable[239] = 2.37709679394066;
            mDoublePhantomAdjustTable[240] = 2.38893908447135;
            mDoublePhantomAdjustTable[241] = 2.40078137500204;
            mDoublePhantomAdjustTable[242] = 2.41262366553273;
            mDoublePhantomAdjustTable[243] = 2.42446595606342;
            mDoublePhantomAdjustTable[244] = 2.43630824659411;
            mDoublePhantomAdjustTable[245] = 2.4481505371248;
            mDoublePhantomAdjustTable[246] = 2.45999282765549;
            mDoublePhantomAdjustTable[247] = 2.47183511818618;
            mDoublePhantomAdjustTable[248] = 2.48367740871687;
            mDoublePhantomAdjustTable[249] = 2.49551969924756;
            mDoublePhantomAdjustTable[250] = 2.50736198977825;
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("initCaliTable.Exception","");
        }
    }




    public void reSelectBVResultByVpk(){
        int[] iArrayForHRSelect;
        int iVar, iMinForSelect;

        try {
            if ((MainActivity.mBVSignalProcessorPart2Selected == null) || (!SystemConfig.mBoolProcessorPart2Selected)) {
                return;
            }

            //---- The select HR count should  <= SystemConfig.INT_HR_USED_MAX_CNT -------------
            if (MainActivity.mBVSignalProcessorPart2Selected.mIntHRAccuCntUsed > SystemConfig.INT_HR_USED_MAX_CNT_BY_RESELECTED_VPK_DEFAULT) {
                iArrayForHRSelect = new int[MainActivity.mBVSignalProcessorPart2Selected.mIntHRAccuCntUsed];
                int iVar2=0;
                for (iVar = 0; iVar < MainActivity.mBVSignalProcessorPart2Selected.mIntHRResultNextIdx; iVar++) {
                    if (MainActivity.mBVSignalProcessorPart2Selected.mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_DISCARDED_IDX] == SystemConfig.INT_HR_RESULT_DISCARDED_STATE_NO) {
                        iArrayForHRSelect[iVar2] = MainActivity.mBVSignalProcessorPart2Selected.mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VPK_VALUE_IDX];
                        iVar2++;
                    }
                }
                Arrays.sort(iArrayForHRSelect);
                iMinForSelect = iArrayForHRSelect[MainActivity.mBVSignalProcessorPart2Selected.mIntHRAccuCntUsed - SystemConfig.INT_HR_USED_MAX_CNT_BY_RESELECTED_VPK_DEFAULT];
                MainActivity.mBVSignalProcessorPart2Selected.mIntHRAccuCntUsedByReselectVpk = 0;
                for (iVar = 0; iVar < MainActivity.mBVSignalProcessorPart2Selected.mIntHRResultNextIdx; iVar++) {
                    if (MainActivity.mBVSignalProcessorPart2Selected.mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_DISCARDED_IDX] == SystemConfig.INT_HR_RESULT_DISCARDED_STATE_NO) {
                        if (MainActivity.mBVSignalProcessorPart2Selected.mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VPK_VALUE_IDX] < iMinForSelect) {
                            MainActivity.mBVSignalProcessorPart2Selected.mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_DISCARDED_IDX] = SystemConfig.INT_HR_RESULT_DISCARDED_STATE_YES;
                            MainActivity.mBVSignalProcessorPart2Selected.setHRPeriodDiscarded(MainActivity.mBVSignalProcessorPart2Selected.mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_START_IDX], MainActivity.mBVSignalProcessorPart2Selected.mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_END_IDX], true);
                            MainActivity.mBVSignalProcessorPart2Selected.mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_DISCARDED_IDX] = SystemConfig.INT_HR_RESULT_DISCARDED_STATE_YES;
                            MainActivity.mBVSignalProcessorPart2Selected.setVTIDiscarded(MainActivity.mBVSignalProcessorPart2Selected.mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_START_IDX], MainActivity.mBVSignalProcessorPart2Selected.mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_END_IDX], true);
                        }else{
                            MainActivity.mBVSignalProcessorPart2Selected.mIntHRAccuCntUsedByReselectVpk++;
                        }
                    }
                }
                MainActivity.mBVSignalProcessorPart2Selected.processResultBloodSignalByRecalculate();
            }
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("reSelectBVResult.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            ex1.printStackTrace();
        }
    }

    public dataInfo getResultDataAfterSignalProcessByWu(){
        dataInfo mDataInfo = new dataInfo();
        int selectCount = 0;
        double HRInterval = 0;
        double VpkAccu = 0;
        double VtiAccu = 0;
        for (int i = 0; i<SystemConfig.mDopplerInfo.segList.size();i++){
            if (!SystemConfig.mDopplerInfo.segList.get(i).isDiscarded){
                HRInterval += (SystemConfig.mDopplerInfo.segList.get(i).EndPt - SystemConfig.mDopplerInfo.segList.get(i).StartPt);
                VpkAccu += SystemConfig.mDopplerInfo.segList.get(i).segVpk;
                VtiAccu += SystemConfig.mDopplerInfo.segList.get(i).segVTI;
                selectCount++;
            }
        }
        if (selectCount>0){
            double averageHRLength = HRInterval / selectCount;
            //mDataInfo.HR = (int) ((125 / averageHRLength) * 60);
            mDataInfo.HR = (int)SystemConfig.mDopplerInfo.HR;
            mDataInfo.Vpk = VpkAccu / selectCount;
            //mDataInfo.VTI = (VtiAccu / selectCount) * 100;
            mDataInfo.VTI = VtiAccu / selectCount;
            double doubleRadius = UserManagerCommon.mDoubleUserPulmDiameterCm / 2.0;
            double doubleCSArea = doubleRadius * doubleRadius * Math.PI;
            mDataInfo.SV = mDataInfo.VTI * doubleCSArea;
            mDataInfo.CO = mDataInfo.SV * mDataInfo.HR / 1000.0;
        } else {
            return getResultDataAfterSignalProcessWu201022();   // Dr. Wu version
//            return getResultDataAfterSignalProcess();         // JF version
        }


        return mDataInfo;
    }

    public int getHRFromECG(){
        final List<ecgResult> list = MainActivity.mBVSignalProcessorPart1.mEcgList;
        List<Integer> hrSegmentLenList = new ArrayList<>();
        double intHR =-1;
        int preIndex = -1;
        int currentIndex;
        if (SystemConfig.isHeartIO2||(MainActivity.offFrag!=null && MainActivity.offFrag.hasECG)){
            for (ecgResult tmpEcg:list){
                if (preIndex == -1){
                    preIndex = tmpEcg.getRPeakIndex();
                    continue;
                }
                currentIndex = tmpEcg.getRPeakIndex();
                if (currentIndex!=preIndex){
                    hrSegmentLenList.add(currentIndex - preIndex);
                    preIndex = currentIndex;
                }
            }
            Collections.sort(hrSegmentLenList);
            if (hrSegmentLenList.size()>3){
                hrSegmentLenList.remove(0);
                hrSegmentLenList.remove(hrSegmentLenList.size()-1);
            }
            if (!hrSegmentLenList.isEmpty()){
                int sum = 0;
                for (Integer hrLen : hrSegmentLenList){
                    sum += hrLen;
                }
                intHR = (double)sum / (double)hrSegmentLenList.size();
                intHR = 60 * 1000 / (intHR*2);
            }
        }
        return (int)intHR;
    }

    public static dataInfo getResultFromTwoPointWu(int startPoint, int endPoint){
        dataInfo result = new dataInfo();

        double doubleRadius = UserManagerCommon.mDoubleUserPulmDiameterCm / 2.0;
        double doubleCSArea = doubleRadius * doubleRadius * Math.PI;

        result.HR = (int)SystemConfig.mDopplerInfo.HR;

        Log.d("BVSPC","SystemConfig.rxAngle ="+SystemConfig.rxAngle);

        if (SystemConfig.mIntSingleVpkEnabled == SystemConfig.INT_SINGLE_VPK_ENABLED_YES){
            result.VTI = getVpkOrgFromTwoPointWu(startPoint,endPoint);
            result.Vpk = Doppler.frequency_to_velocity_By_Angle(result.VTI, Doppler.PHANTON_C, SystemConfig.rxAngle);
        }else{
            result.VTI = getVTIFromTwoPointWu(startPoint, endPoint);
            //result.Vpk = getVpkFromTwoPointWu(startPoint, endPoint);
            //Cavin test start
            double doubleVpkOrg = getVpkOrgFromTwoPointWu(startPoint,endPoint);
            result.Vpk = Doppler.frequency_to_velocity_By_Angle(result.VTI, Doppler.HUMAN_C, SystemConfig.rxAngle);
            //Cavin test end
        }

        result.SV = result.VTI * doubleCSArea;
        result.CO = result.SV * result.HR / 1000.0;
        Log.d("BVPC","two Point result HR = "+result.HR);
        Log.d("BVPC","two Point result VTI = "+result.VTI);
        Log.d("BVPC","two Point result Vpk = "+result.Vpk);
        Log.d("BVPC","two Point result SV = "+result.SV);
        Log.d("BVPC","two Point result CO = "+result.CO);

        return result;
    }

    public static double getVpkFromTwoPointWu(int startPoint, int endPoint){
        double result = 0.0;

        for (int i=startPoint;i<=endPoint;i++){
            if (SystemConfig.mDopplerVFOutput[2][i] > result)
            result = SystemConfig.mDopplerVFOutput[2][i];
        }
        return result;
    }

    public static double getVTIFromTwoPointWu(int startPoint, int endPoint){
        double result = 0.0;

        for (int i=startPoint;i<=endPoint;i++){
            result += SystemConfig.mDopplerVFOutput[2][i];
        }
        return result;
    }

    public static double getVpkOrgFromTwoPointWu(int startPoint, int endPoint){
        double result = 0.0;
        double[] vf_r =new double[endPoint-startPoint+1];
        for(int i=0;i<endPoint-startPoint+1;i++)
        {
//            vf_r[i]=SystemConfig.mDopplerVFOutput[0][startPoint+i];
            vf_r[i] = MainActivity.mBVSignalProcessorPart1.mIntArrayMaxIdxByMovingAverage[startPoint+i];
//			   		System.out.println(vfs[i] );
        }

        result= (int)Doppler.getMax(vf_r);
        return result;
    }

    public double getVTIFromECG(){
        final List<ecgResult> list = MainActivity.mBVSignalProcessorPart1.mEcgList;
        double result = 0.0;
        if (SystemConfig.isHeartIO2||(MainActivity.offFrag!=null && MainActivity.offFrag.hasECG)){
            for (int j=0;j<list.size();j++){
                double tmpSum = 0;
                int rPeakIdx = list.get(j).RPeakIndex / 4;
                if (rPeakIdx<1750){
//                    Log.d("BVSPC","rPeakIdx="+rpeakIdx);
                    double maxVpk = 0.0;
                    for (int i = rPeakIdx+15;i<rPeakIdx+SystemConfig.vtiLengthUS;i++){
                        if (i<SystemConfig.mDopplerVFOutput[2].length){
//                            Log.d("BVSPC","SystemConfig.mDopplerVFOutput[2]["+i+"]="+SystemConfig.mDopplerVFOutput[2][i]);
                            if (SystemConfig.mDopplerVFOutput[2][i]<2){
                                tmpSum += SystemConfig.mDopplerVFOutput[2][i];
                                if (maxVpk < SystemConfig.mDopplerVFOutput[2][i]){
                                    maxVpk = SystemConfig.mDopplerVFOutput[2][i];
                                }
                            }else{
                                Log.d("BVSPC","SystemConfig.mDopplerVFOutput[2]["+i+"]="+SystemConfig.mDopplerVFOutput[2][i]);
                            }
                        }
                    }
                    list.get(j).setDoubleVTI(tmpSum);
                    list.get(j).setDoubleVpk(maxVpk);

//                    Log.d("BVSPC","tmpVTI="+tmpSum);
                }
            }
            ecgResult tmpEcg = list.stream()
                    .max(Comparator.comparing(ecgResult::getDoubleVTI))
                    .orElseThrow(NoSuchElementException::new);
            Log.d("BVSPC","vti index = "+list.indexOf(tmpEcg));
            result = tmpEcg.getDoubleVTI();

        }
        return result;
    }


    //190620
    public dataInfo getResultDataAfterSignalProcess() {
        dataInfo mDataInfo;
        BVProcessSubsysII BVProcessSubsysII = new BVProcessSubsysII(
                MainActivity.mBVSignalProcessorPart2Selected.mIntArrayVTIMaxIdx
                ,MainActivity.mBVSignalProcessorPart1.mDoubleBVSpectrumValues
                ,MainActivity.mRawDataProcessor.mShortUltrasoundData
        );
        mDataInfo = BVProcessSubsysII.getJResultDataAfterSignalProcess();

        mDataInfo.ErrCode = MainActivity.mBVSignalProcessorPart1.mIntHRErrCode;
//        Log.d("BVSPC", "error code = "+mDataInfo.ErrCode);

        return mDataInfo;
    }

    public dataInfo getResultDataAfterSignalProcessWu201022() {
        dataInfo mDataInfo;
        BVProcessSubsysII BVProcessSubsysII = new BVProcessSubsysII(
                MainActivity.mBVSignalProcessorPart2Selected.mIntArrayVTIMaxIdx
                ,MainActivity.mBVSignalProcessorPart1.mDoubleBVSpectrumValues
                ,MainActivity.mRawDataProcessor.mShortUltrasoundData
        );
        mDataInfo = BVProcessSubsysII.getJResultDataAfterSignalProcess();

        // For wu algorithm test
        mDataInfo.HR = (int)SystemConfig.mDopplerInfo.HR;
        mDataInfo.Vpk = SystemConfig.mDopplerInfo.Vpk;
        mDataInfo.VTI = SystemConfig.mDopplerInfo.VTI;

        double doubleRadius = UserManagerCommon.mDoubleUserPulmDiameterCm / 2.0;
        double doubleCSArea = doubleRadius * doubleRadius * Math.PI;
        mDataInfo.SV = mDataInfo.VTI * doubleCSArea;
        mDataInfo.CO = mDataInfo.SV * mDataInfo.HR / 1000.0;

        if (SystemConfig.isHeartIO2||(MainActivity.offFrag!=null && MainActivity.offFrag.hasECG)){
            int ecgHR = getHRFromECG();
            double ecgVTI = getVTIFromECG();
            Log.d("BVSPC","ECG HR = "+  ecgHR+" , US HR = "+ mDataInfo.HR);
            Log.d("BVSPC","ECG VTI = "+ecgVTI);
            double ecgSV = ecgVTI * doubleCSArea;
            double ecgCO = ecgSV * ecgHR / 1000.0;
            Log.d("BVSPC", "ECG SV = "+ecgSV+", ECG CO = "+ecgCO);

            ecgResult tmpEcg = MainActivity.mBVSignalProcessorPart1.mEcgList.stream()
                    .max(Comparator.comparing(ecgResult::getDoubleVTI))
                    .orElseThrow(NoSuchElementException::new);

            Log.d("BVSPC", "ECG Vpk = "+tmpEcg.getDoubleVpk());
        }

        mDataInfo.ErrCode = MainActivity.mBVSignalProcessorPart1.mIntHRErrCode;
        Log.d("BVSPC", "error code = "+mDataInfo.ErrCode);

        return mDataInfo;
    }

    public dataInfo getResultDataAfterSignalProcess190620(){
        dataInfo mDataInfo = new dataInfo();


        if (SystemConfig.mIntSingleVpkEnabled == SystemConfig.INT_SINGLE_VPK_ENABLED_YES){
            MainActivity.mBVSignalProcessorPart1.processMaxIdxByMovingAverageForSingleVpk();
            //double doubleVpkMeterAverageOri, doubleVpkMeterAverageAfterUserAngle, doubleVpkMeterAverageAfterAngleAfterCali;

            MainActivity.mBVSignalProcessorPart2Selected.mDoubleVpkMeterAverageOri = (((SystemConfig.mDoubleSingleVpkAvg * SystemConfig.DOUBLE_ULTRASOUND_SPEED_FOR_BODY_METER_PERSEC) / 2) / SystemConfig.mDoubleSensorWaveFreq);
            MainActivity.mBVSignalProcessorPart2Selected.mDoubleVpkMeterAverageAfterUserAngle = MainActivity.mBVSignalProcessorPart2Selected.mDoubleVpkMeterAverageOri / UserManagerCommon.mDoubleCosineUserAngle;
            MainActivity.mBVSignalProcessorPart2Selected.mDoubleVpkMeterAverageAfterAngleAfterCali = MainActivity.mSignalProcessController.adjustValueByCali(MainActivity.mBVSignalProcessorPart2Selected.mDoubleVpkMeterAverageAfterUserAngle);

//* jaufa, +, 181102
            double tIntVpkMeterRaw = SystemConfig.mDoubleSingleVpkAvg /MainActivity.mBVSignalProcessorPart1.mDoubleFreqGap;
            if (tIntVpkMeterRaw < 0)
                tIntVpkMeterRaw = 0;
            else if(tIntVpkMeterRaw > 255)
                tIntVpkMeterRaw = 255;
            MainActivity.mBVSignalProcessorPart2Selected.mDoubleVpkMeterAverageOri = MainActivity.mBVSignalProcessorPart1.mDoubleVPKTheoreticalTable[(int) tIntVpkMeterRaw];
            MainActivity.mBVSignalProcessorPart2Selected.mDoubleVpkMeterAverageAfterAngleAfterCali = MainActivity.mBVSignalProcessorPart1.mDoubleVPKExperimentalTable[(int) (MainActivity.mBVSignalProcessorPart2Selected.mDoubleVpkMeterAverageOri * 100.0)];
//* jaufa, +, 181102 */



            if(SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_0_SNSI_GM){
                mDataInfo.Vpk = MainActivity.mBVSignalProcessorPart2Selected.mDoubleVpkMeterAverageAfterAngleAfterCali;
            }else{
                mDataInfo.Vpk = MainActivity.mBVSignalProcessorPart2Selected.mDoubleVpkMeterAverageAfterAngleAfterCali;
            }
            mDataInfo.HR = 0;
            mDataInfo.VTI = MainActivity.mBVSignalProcessorPart2Selected.mDoubleVpkMeterAverageOri;
            mDataInfo.SV = 0;
            mDataInfo.CO = 0;
        }else {
            // jaufa, +, 190102, Adding ErrCode conditions
            if (SystemConfig.mDoubleSNRLearned < 200) {
                MainActivity.mBVSignalProcessorPart1.mIntHRErrCode |= BVSignalProcessorPart1.BINARY_ERR_CODE_SNR_SMALL;
            }
            if (MainActivity.mBVSignalProcessorPart1.mDoubleHrBloodSuccessRatio <= 0.4) {
                //MainActivity.mBVSignalProcessorPart1.mIntHRErrCode |= MainActivity.mBVSignalProcessorPart1.BINARY_ERR_CODE_LENGTH_INVALID;
            }
            if (MainActivity.mBVSignalProcessorPart1.mDoubleHrBloodVpk <= 0.4 && MainActivity.mBVSignalProcessorPart1.mDoubleHrBloodVpk > 0) {
                MainActivity.mBVSignalProcessorPart1.mIntHRErrCode |= BVSignalProcessorPart1.BINARY_ERR_CODE_Vpk_SMALL;
            }

            mDataInfo.result = IwuSQLHelper.STR_SUCESS;

            mDataInfo.ErrCode = MainActivity.mBVSignalProcessorPart1.mIntHRErrCode;

            // reject Err parts
            //MainActivity.mBVSignalProcessorPart1.mIntHRErrCode &= (~MainActivity.mBVSignalProcessorPart1.BINARY_ERR_CODE_HR_UNSTABLE);
            //MainActivity.mBVSignalProcessorPart1.mIntHRErrCode &= (~MainActivity.mBVSignalProcessorPart1.BINARY_ERR_CODE_HR_INVALID);
            //MainActivity.mBVSignalProcessorPart1.mIntHRErrCode &= (~MainActivity.mBVSignalProcessorPart1.BINARY_ERR_CODE_VTI_UNSTABLE);
            //MainActivity.mBVSignalProcessorPart1.mIntHRErrCode &= (~MainActivity.mBVSignalProcessorPart1.BINARY_ERR_CODE_VTI_INVALID);
            //MainActivity.mBVSignalProcessorPart1.mIntHRErrCode &= (~MainActivity.mBVSignalProcessorPart1.BINARY_ERR_CODE_Vpk_SMALL);
            //MainActivity.mBVSignalProcessorPart1.mIntHRErrCode &= (~MainActivity.mBVSignalProcessorPart1.BINARY_ERR_CODE_Vpk_INVALID);
            //MainActivity.mBVSignalProcessorPart1.mIntHRErrCode &= (~MainActivity.mBVSignalProcessorPart1.BINARY_ERR_CODE_SNR_SMALL);
            //MainActivity.mBVSignalProcessorPart1.mIntHRErrCode &= (~MainActivity.mBVSignalProcessorPart1.BINARY_ERR_CODE_LENGTH_INVALID);

            // jaufa, *, 181025
            if (MainActivity.mBVSignalProcessorPart2Selected.iCountHRBloodSelected() > 0) {
                MainActivity.mBVSignalProcessorPart2Selected.processResultBloodSignalByRecalculate();
                mDataInfo.HR = (int) MainActivity.mBVSignalProcessorPart2Selected.mDoubleArrayHRBloodResult
                        [SystemConfig.DOUBLE_HR_BLOOD_RESULT_SEL_STATE][SystemConfig.DOUBLE_HR_BLOOD_RESULT_HR_IDX];
                mDataInfo.Vpk = MainActivity.mBVSignalProcessorPart2Selected.mDoubleArrayHRBloodResult
                        [SystemConfig.DOUBLE_HR_BLOOD_RESULT_SEL_STATE][SystemConfig.DOUBLE_HR_BLOOD_RESULT_VPK_IDX];
                mDataInfo.VTI = MainActivity.mBVSignalProcessorPart2Selected.mDoubleArrayHRBloodResult
                        [SystemConfig.DOUBLE_HR_BLOOD_RESULT_SEL_STATE][SystemConfig.DOUBLE_HR_BLOOD_RESULT_VTI_IDX];
                mDataInfo.SV = MainActivity.mBVSignalProcessorPart2Selected.mDoubleArrayHRBloodResult
                        [SystemConfig.DOUBLE_HR_BLOOD_RESULT_SEL_STATE][SystemConfig.DOUBLE_HR_BLOOD_RESULT_SV_IDX];
                mDataInfo.CO = MainActivity.mBVSignalProcessorPart2Selected.mDoubleArrayHRBloodResult
                        [SystemConfig.DOUBLE_HR_BLOOD_RESULT_SEL_STATE][SystemConfig.DOUBLE_HR_BLOOD_RESULT_CO_IDX];
            } else {
                if (MainActivity.mBVSignalProcessorPart1.mIntHRErrCode > 0) {
                    mDataInfo.result = IwuSQLHelper.STR_FAIL;
                    mDataInfo.HR = 0;
                    mDataInfo.Vpk = 0;
                    mDataInfo.VTI = 0;
                    mDataInfo.SV = 0;
                    mDataInfo.CO = 0;
                } else {
                    mDataInfo.HR = (int) MainActivity.mBVSignalProcessorPart2Selected.mDoubleArrayHRBloodResult
                            [SystemConfig.DOUBLE_HR_BLOOD_RESULT_ORI_STATE][SystemConfig.DOUBLE_HR_BLOOD_RESULT_HR_IDX];
                    mDataInfo.Vpk = MainActivity.mBVSignalProcessorPart2Selected.mDoubleArrayHRBloodResult
                            [SystemConfig.DOUBLE_HR_BLOOD_RESULT_ORI_STATE][SystemConfig.DOUBLE_HR_BLOOD_RESULT_VPK_IDX];
                    mDataInfo.VTI = MainActivity.mBVSignalProcessorPart2Selected.mDoubleArrayHRBloodResult
                            [SystemConfig.DOUBLE_HR_BLOOD_RESULT_ORI_STATE][SystemConfig.DOUBLE_HR_BLOOD_RESULT_VTI_IDX];
                    mDataInfo.SV = MainActivity.mBVSignalProcessorPart2Selected.mDoubleArrayHRBloodResult
                            [SystemConfig.DOUBLE_HR_BLOOD_RESULT_ORI_STATE][SystemConfig.DOUBLE_HR_BLOOD_RESULT_SV_IDX];
                    mDataInfo.CO = MainActivity.mBVSignalProcessorPart2Selected.mDoubleArrayHRBloodResult
                            [SystemConfig.DOUBLE_HR_BLOOD_RESULT_ORI_STATE][SystemConfig.DOUBLE_HR_BLOOD_RESULT_CO_IDX];
                }
                // jaufa, *, 181025 */
            }
        }

        return mDataInfo;
    }


    public void reSelectBVResultByVTI(){
        int[] iArrayForHRSelect;
        int iVar, iMinForSelect;

        try {
            if ((MainActivity.mBVSignalProcessorPart2Selected == null) || (!SystemConfig.mBoolProcessorPart2Selected)) {
                return;
            }

            //---- The select HR count should  <= SystemConfig.INT_HR_USED_MAX_CNT -------------
            if (MainActivity.mBVSignalProcessorPart2Selected.mIntHRAccuCntUsedByReselectVpk > SystemConfig.INT_HR_USED_MAX_CNT_BY_RESELECTED_VTI_DEFAULT) {
                iArrayForHRSelect = new int[MainActivity.mBVSignalProcessorPart2Selected.mIntHRAccuCntUsedByReselectVpk];
                int iVar2=0;
                for (iVar = 0; iVar < MainActivity.mBVSignalProcessorPart2Selected.mIntHRResultNextIdx; iVar++) {
                    if (MainActivity.mBVSignalProcessorPart2Selected.mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_DISCARDED_IDX] == SystemConfig.INT_HR_RESULT_DISCARDED_STATE_NO) {
                        iArrayForHRSelect[iVar2] = MainActivity.mBVSignalProcessorPart2Selected.mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VPK_VALUE_IDX];
                        iVar2++;
                    }
                }
                Arrays.sort(iArrayForHRSelect);
                iMinForSelect = iArrayForHRSelect[MainActivity.mBVSignalProcessorPart2Selected.mIntHRAccuCntUsedByReselectVpk - SystemConfig.INT_HR_USED_MAX_CNT_BY_RESELECTED_VTI_DEFAULT];
                for (iVar = 0; iVar < MainActivity.mBVSignalProcessorPart2Selected.mIntHRResultNextIdx; iVar++) {
                    if (MainActivity.mBVSignalProcessorPart2Selected.mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_DISCARDED_IDX] == SystemConfig.INT_HR_RESULT_DISCARDED_STATE_NO) {
                        if (MainActivity.mBVSignalProcessorPart2Selected.mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VPK_VALUE_IDX] < iMinForSelect) {
                            MainActivity.mBVSignalProcessorPart2Selected.mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_DISCARDED_IDX] = SystemConfig.INT_HR_RESULT_DISCARDED_STATE_YES;
                            MainActivity.mBVSignalProcessorPart2Selected.setHRPeriodDiscarded(MainActivity.mBVSignalProcessorPart2Selected.mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_START_IDX], MainActivity.mBVSignalProcessorPart2Selected.mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_END_IDX], true);
                            MainActivity.mBVSignalProcessorPart2Selected.mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_HR_DISCARDED_IDX] = SystemConfig.INT_HR_RESULT_DISCARDED_STATE_YES;
                            MainActivity.mBVSignalProcessorPart2Selected.setVTIDiscarded(MainActivity.mBVSignalProcessorPart2Selected.mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_START_IDX], MainActivity.mBVSignalProcessorPart2Selected.mIntArrayHRResult[iVar][SystemConfig.INT_HR_RESULT_VTI_END_IDX], true);
                        }
                    }
                }
                MainActivity.mBVSignalProcessorPart2Selected.processResultBloodSignalByRecalculate();
            }
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("reSelectBVResult.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            ex1.printStackTrace();
        }
    }

    // Cavin Find VTI start and end position
    private void findHRVTIStartEndPosition( int[] arIntMaxIdxHR, int[] arIntMaxIdxVTI){
        int intHRPeriodCounts = SystemConfig.mDopplerInfo.segList.size();
        for (int iVar=0; iVar< intHRPeriodCounts; iVar++) {
            int iHRStart = SystemConfig.mDopplerInfo.segList.get(iVar).StartPt;
            int iHREnd = SystemConfig.mDopplerInfo.segList.get(iVar).EndPt;
            int iCompareVTIThis;
            int iCompareVTIMax = Integer.MIN_VALUE;
            int iCompareVTIStart;
            int iCompareVTIEnd;
            boolean bCompareVTIOK = false;
            int iCompareCondition;
            int iValueNow, iValuePre, iValueDiff;

            int iValleyLasting=0;

            //Step 1: Find Valley  from HR array
            iCompareCondition = 0x00FF;
            iValuePre = arIntMaxIdxHR[iHRStart];
            iCompareVTIStart = iHRStart;
            iCompareVTIEnd = iHREnd;
            for (int iVar1 = iHRStart; iVar1 <= iHREnd; iVar1++){
                iCompareCondition &= 0x00FF;
                //Step 1.1: Find Next Vally
                iValueNow = arIntMaxIdxHR[iVar1];
                iValueDiff = Math.abs(iValueNow-iValuePre);
                if (iValueDiff >= 0){    // Amplitude determine: move to "0b0111"
                    iCompareCondition = Integer.rotateLeft(iCompareCondition, 1);
                    if(arIntMaxIdxHR[iVar1] >= arIntMaxIdxHR[iVar1 - 1]) {
                        iCompareCondition += 1;
                    }
                    if (((iCompareCondition & 0b1111) == 0b0110)) {    //雜訊
                        if (arIntMaxIdxHR[iVar1 - 3] < arIntMaxIdxHR[iVar1]) {
                            iCompareCondition = 0b0111;
                        } else {
                            iValleyLasting = 0;
                        }
                    }
                    if (((iCompareCondition & 0b1111) == 0b0111)) {
                        iCompareVTIEnd = iVar1-3-iValleyLasting;
                        if (((arIntMaxIdxHR[iVar1]-arIntMaxIdxHR[iCompareVTIEnd]) > 6)
                                || ((arIntMaxIdxHR[iVar1] <= 3 && arIntMaxIdxHR[iCompareVTIEnd] <= 3))) {
                            bCompareVTIOK = true;
                            iValleyLasting = 0;
                        }else{
                            iValleyLasting ++;
                            iCompareCondition = 0b0011;
                        }
                    }

                    iValuePre=iValueNow;

                    if (((iCompareCondition & 0b111) == 0b010)) { //雜訊
                        if ((arIntMaxIdxHR[iVar1 - 2] < arIntMaxIdxHR[iVar1]) ||
                                (arIntMaxIdxHR[iVar1 - 2] <= 3 &&  arIntMaxIdxHR[iVar1] <= 3)){
                            iCompareCondition = 0b011;
                        } else {
                            iValleyLasting = 0;
                        }
                    }

                }
                if (iVar1 == iHREnd){
                    iCompareVTIEnd = iHREnd;
                    bCompareVTIOK = true;
                }
                if (bCompareVTIOK) {
                    bCompareVTIOK = false;
                    iCompareVTIThis = 0;
                    for (int _iVar2 = iCompareVTIStart; _iVar2 <= iCompareVTIEnd; _iVar2++) {
                        iCompareVTIThis += arIntMaxIdxVTI[_iVar2];
                    }
                    if (iCompareVTIMax <= iCompareVTIThis) {
                        iCompareVTIMax = iCompareVTIThis;
                        SystemConfig.mDopplerInfo.segList.get(iVar).segVTIStartPt = iCompareVTIStart;
                        SystemConfig.mDopplerInfo.segList.get(iVar).segVTIEndPt = iCompareVTIEnd;
                    }
                    iCompareVTIStart = iCompareVTIEnd + 1;
                }
            }
        }
    }

    public void prepareWuDoppler(){
//        short[] o_im1 = Doppler.mDblArrayUltrasoundDCT(MainActivity.mRawDataProcessor.mShortUltrasoundData);
        double[][] o_im2 = Doppler.Spectrogram(
                Type.toDbl(MainActivity.mRawDataProcessor.mShortUltrasoundData,
                        0, (SystemConfig.mIntUltrasoundSamplesMaxSizeForRun -1)),
                        256, 192);

//        double[][] o_im2 = Doppler.Spectrogram(
//                Type.toDbl(o_im1,
//                        0, (SystemConfig.mIntUltrasoundSamplesMaxSizeForRun -1)),
//                256, 192);



        int width=o_im2[0].length;
        for (int i=0;i<3;i++) {
            for (int j = 0; j < width; j++) {
                o_im2[i][j] = 0;
            }
        }

        //  Test for Dr. Wu STFT Check Start output CSV
/*
        File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        SimpleDateFormat df;

        df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String strDate = df.format(new Date());

        //String filename = UserManagerCommon.mUserInfoCur.userID+"_"+ strDate +".csv";
        String filename = MainActivity.offFrag.currentFilename.replace(".wav",".csv");

        try {
            int depth = mFFTinput.length;
            int width = mFFTinput[0].length;
            File csv = new File(exportDir,filename);
            BufferedWriter bw = new BufferedWriter(new FileWriter(csv, false));
            for (int tl = 0; tl < depth; tl++)
            {
                for (int kl = 0; kl < width-1; kl++)
                {
                    bw.write(Double.toString(mFFTinput[tl][kl]));
                    bw.write(",");
                }
                bw.write(Double.toString(mFFTinput[tl][width-1]));
                bw.newLine();
            }
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/

        // test SNSI 20210409
//        SNSIResult snsiResult = Doppler.SNSI_VF3(o_im2);
//        double[] vf3 = Doppler.MOV_AVG(snsiResult.vf,7);
//        double[][] fpkResult = Doppler.fpk_snsi(vf3,50,1);
//        Log.d("BVSPC","fpk mean = "+Doppler.mean(fpkResult[1]));
        // test SNSI end  20210409

        //  Test for Dr. Wu STFT Check End output CSV
        double [][] o_im5 = Doppler.normal2(o_im2);
//        double [][] o_im4 = Doppler.normal(o_im5);
//        SystemConfig.mDopplerVFOutput = Doppler.VF_test(o_im4);
        SystemConfig.mDopplerVFOutput = Doppler.VF_SNSI(o_im5);
        SystemConfig.mDopplerInfo = new wuDopplerInfo();
        Log.d("BVPC","Wu HR interval = "+ SystemConfig.mDopplerVFOutput[7][12]);
        Log.d("BVPC","Wu SNR fail number = "+ SystemConfig.mDopplerVFOutput[4][0]);
//        Log.d("BVPC","SNR fail flag = "+ SystemConfig.mDopplerVFOutput[4][1]);
//        Log.d("BVPC","SEG fail flag = "+ SystemConfig.mDopplerVFOutput[4][2]);
//        Log.d("BVPC","HR fail flag = "+ SystemConfig.mDopplerVFOutput[4][3]);
        SystemConfig.mDopplerInfo.isSNRFail = (SystemConfig.mDopplerVFOutput[4][1] == 1);
        SystemConfig.mDopplerInfo.isSegFail = (SystemConfig.mDopplerVFOutput[4][2] == 1);
        SystemConfig.mDopplerInfo.isHRFail = (SystemConfig.mDopplerVFOutput[4][3] == 1);
//        Log.d("BVPC","SNR fail flag = "+ SystemConfig.mDopplerInfo.isSNRFail);
//        Log.d("BVPC","SEG fail flag = "+ SystemConfig.mDopplerInfo.isSegFail);
//        Log.d("BVPC","HR fail flag = "+ SystemConfig.mDopplerInfo.isHRFail);
        SystemConfig.mDopplerInfo.HR = SystemConfig.mDopplerVFOutput[4][4];
        SystemConfig.mDopplerInfo.Vpk = SystemConfig.mDopplerVFOutput[4][5];
        SystemConfig.mDopplerInfo.VTI = SystemConfig.mDopplerVFOutput[4][6];
        Log.d("BVPC","Wu HR = "+ SystemConfig.mDopplerInfo.HR);
        Log.d("BVPC","Wu Vpk = "+ SystemConfig.mDopplerInfo.Vpk);
        Log.d("BVPC","Wu VTI = "+ SystemConfig.mDopplerInfo.VTI);
        SystemConfig.mDopplerInfo.HRLength = SystemConfig.mDopplerVFOutput[7][12];
        for (int j = 7; j< SystemConfig.mDopplerVFOutput.length; j++){
            if (SystemConfig.mDopplerVFOutput[j][16]!=0){
                segObject tmpSeg;
                //Log.d("BVPP","SystemConfig.mDopplerVFOutput["+j+"]["+i+"] = "+SystemConfig.mDopplerVFOutput[j][i]);
                // last end point can't overlap this start point
                if (j>=8 && (SystemConfig.mDopplerVFOutput[j][15] == SystemConfig.mDopplerVFOutput[j-1][16])){
                    tmpSeg = new segObject((int)(SystemConfig.mDopplerVFOutput[j][15]+1),
                            (int)SystemConfig.mDopplerVFOutput[j][16],
                            SystemConfig.mDopplerVFOutput[j][17],
                            SystemConfig.mDopplerVFOutput[j][18]);
                }else{
                    tmpSeg = new segObject((int)SystemConfig.mDopplerVFOutput[j][15],
                            (int)SystemConfig.mDopplerVFOutput[j][16],
                            SystemConfig.mDopplerVFOutput[j][17],
                            SystemConfig.mDopplerVFOutput[j][18]);
                }

//                Log.d("BVSPC","tmpSeg.StartPt  = "+ tmpSeg.StartPt);
                // find vpk index
//                for (int i = tmpSeg.StartPt; i<tmpSeg.EndPt;i++){
//                    Log.d("BVSPC","SystemConfig.mDopplerVFOutput[1]["+i+"]  = "+ SystemConfig.mDopplerVFOutput[1][i]);
//                    if (SystemConfig.mDopplerVFOutput[2][i] == tmpSeg.segVpk){
//                        Log.d("BVSPC","found vpk index = "+ i);
//                        tmpSeg.segVpkIdx = i;
//                    }
//
//                }
                SystemConfig.mDopplerInfo.segList.add(tmpSeg);
            }
        }

        double maxFeq = Doppler.getMax(SystemConfig.mDopplerVFOutput[0]);
        int maxCount = 0;
        int count33 = 0;
        int count65 = 0;
        int count97 = 0;
        for (int i = 0; i< SystemConfig.mDopplerVFOutput[0].length; i++){
            if (maxFeq == SystemConfig.mDopplerVFOutput[0][i]){
                maxCount++;
            }
            if (SystemConfig.mDopplerVFOutput[0][i] == 33){
                count33++;
            }else if (SystemConfig.mDopplerVFOutput[0][i] == 65){
                count65++;
            }else if (SystemConfig.mDopplerVFOutput[0][i] == 97){
                count97++;
            }
        }
        if (maxCount >= 2){
            Log.d("BVSPC","maxFeq = "+maxFeq);
            Log.d("BVSPC","maxCount = "+maxCount);
            Log.d("BVSPC","count33 = "+ count33);
            Log.d("BVSPC","count66 = "+ count65);
            Log.d("BVSPC","count97 = "+ count97);
        }


//        findHRVTIStartEndPosition(
//                Type.toInt(Doppler.MOV_AVG(SystemConfig.mDopplerVFOutput[0], 6))
//                , Type.toInt(Doppler.MOV_AVG(SystemConfig.mDopplerVFOutput[0], 6)));
//        for (int i = 0; i< SystemConfig.mDopplerInfo.segList.size();i++){
//            Log.d("BVSPC","SystemConfig.mDopplerInfo.segList.get("+i+").segVTIStartPt = "+SystemConfig.mDopplerInfo.segList.get(i).segVTIStartPt);
//            Log.d("BVSPC","SystemConfig.mDopplerInfo.segList.get("+i+").segVTIEndPt = "+SystemConfig.mDopplerInfo.segList.get(i).segVTIEndPt);
//        }
    }


    public void processSignalAfterDataRxOffLine(){
        try {
            processAllSegmentPart1Step1OffLine();
            processAllSegmentPart1Step2OffLine();

            //* jaufa, 180805, + For One Group
            if (SystemConfig.INT_HR_GROUP_CNT==1){

                MainActivity.mBVSignalProcessorPart2Array[0].processAllSegmentOne();

                //processAllSegmentHRByPeakMode();
                //processResultBloodSignal();
                prepareWuDoppler();
                MainActivity.mBVSignalProcessorPart2Array[0].processAllSegmentHRnVTIOne();
                MainActivity.mBVSignalProcessorPart2Array[0].processResultBloodSignalByNoSelOne();

                //SystemConfig.mBVSignalProcessorPart1.mIntArrayMaxIdxByMovingAverage                 // No Action
                //        = SystemConfig.mBVSignalProcessorPart1.mIntArrayMaxIdx;

                MainActivity.mBVSignalProcessorPart2Selected = MainActivity.mBVSignalProcessorPart2Array[0];
                SystemConfig.mBoolProcessorPart2Selected = true;

                MainActivity.mBVSignalProcessorPart2Selected.setHRPeriodAllDiscarded(true);
                MainActivity.mBVSignalProcessorPart2Selected.setVTIAllDiscarded(true);

                //SystemConfig.mBVSignalProcessorPart2Selected.setAllResultDiscarded();
                //SystemConfig.mBVSignalProcessorPart2Array[0].processResultBloodSignalOne();
                //selectProcessorPart2One();
                //selectProcessorPart2();

            }else {
                processAllSegmentPart2OffLine();
                selectProcessorPart2();
                if (SystemConfig.mEnumBVResultReselectType == SystemConfig.ENUM_BV_RESULT_RESELECT_TYPE.VPK_RESELECT) {
                    reSelectBVResultByVpk();
                } else if (SystemConfig.mEnumBVResultReselectType == SystemConfig.ENUM_BV_RESULT_RESELECT_TYPE.VTI_RESELECT) {
                    reSelectBVResultByVpk();
                    reSelectBVResultByVTI();
                }
            }
        } catch (Exception ex1) {
            ex1.printStackTrace();
            // SystemConfig.mMyEventLogger.appendDebugStr("processBVSignalAfterDataRx.Exception", "");
            // SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(), "");
        }
    }
    public int selectFromHumanGroup(int iStartGroup){
        int iVar, iUsedCntMax, iSelectIdx;

        iUsedCntMax = 0;
        iSelectIdx = -1;
        for (iVar = iStartGroup; iVar < SystemConfig.INT_HR_GROUP_CNT; iVar++) {
            //if ((mBoolArraySelected[iVar]) && (SystemConfig.mBVSignalProcessorPart2Array[iVar].mDoublePhantomPeakCntAverageUsed < 5)) {
            if (mBoolArraySelected[iVar]) {
                if (iUsedCntMax <= MainActivity.mBVSignalProcessorPart2Array[iVar].mIntHRAccuCntUsed) {
                    iUsedCntMax = MainActivity.mBVSignalProcessorPart2Array[iVar].mIntHRAccuCntUsed;
                    MainActivity.mBVSignalProcessorPart2Selected = MainActivity.mBVSignalProcessorPart2Array[iVar];
                    iSelectIdx = iVar;
                }
            }
        }
        return iSelectIdx;
    }

}
