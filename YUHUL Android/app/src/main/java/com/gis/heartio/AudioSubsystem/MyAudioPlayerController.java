package com.gis.heartio.AudioSubsystem;

import com.gis.heartio.SupportSubsystem.MyMsgQueue;
import com.gis.heartio.SupportSubsystem.MyThreadQMsg;

/**
 * Created by brandon on 2017/1/9.
 */

public class MyAudioPlayerController {

    public static int INT_AUDIO_MSGQ_SIZE = 3000;
    //private Handler mHandlerUI;
    private Runnable mRunnable;
    private Thread mThread;
    private MyMsgQueue mMyMsgQueue;
    public MyAudioPlayer mAudioPlayer=null;

    public int mIntPutMsgLostCnt;
    public int mIntMaxMsgCntInQ, mIntMaxMsgCntStartInQ;
    public boolean isPlaying = false;

    public MyAudioPlayerController(){
        try {
            mMyMsgQueue = new MyMsgQueue(INT_AUDIO_MSGQ_SIZE);
            mRunnable = new Runnable() {
                public void run() {
                    AudioPlayerThread();
                }
            };
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("MyAudioController.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            ex1.printStackTrace();
        }
    }

    public void startThread(int iPriority){
        try {
            mThread = new Thread(mRunnable);
            mThread.setPriority(iPriority);
            mThread.start();
        }catch(Exception ex1){
            ex1.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugStr("AudioController.startThread.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }

    public void stopThread(){
        try {
            if (mThread != null) {
                mThread.interrupt();
                mThread = null;
            }
        }catch(Exception ex1){
            ex1.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugStr("AudioController.stopThread.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }

    private void  AudioPlayerThread(){
        MyThreadQMsg myMsg;

        //SystemConfig.mLnThreadIdAudioPlayerController =  Thread.currentThread().getId();
        //SystemConfig.mIntPriorityAudioPlayerController =  Thread.currentThread().getPriority();

        while (true) {
            try {
                myMsg = mMyMsgQueue.getMsg(MyMsgQueue.MY_MSG_TIMEOUT_MILISEC_INFINITIVE);
                //myMsg = mMyMsgQueue.getMsg(100);
                if (myMsg != null) {
                    switch (myMsg.mIntMsgId) {
                        case MyThreadQMsg.INT_MY_MSG_CMD_AUDIO_OPEN:
                            openAudio(myMsg.mIntParam1);
                            //SystemConfig.mMyEventLogger.appendDebugStr("openAudio","");
                            break;

                        case MyThreadQMsg.INT_MY_MSG_CMD_AUDIO_CLOSE:
                            closeAudio();
                            //SystemConfig.mMyEventLogger.appendDebugStr("closeAudio","");
                            break;

                        case MyThreadQMsg.INT_MY_MSG_CMD_AUDIO_SEGMENT_ON_LINE:
                            clearAllButCloseAudioMsg();
                            mAudioPlayer.dataToSoundBySegmentEndIdxOnLine(myMsg.mIntParam1 );
                            break;

                        case MyThreadQMsg.INT_MY_MSG_CMD_AUDIO_ALL_SEGMENT:
                            mAudioPlayer.dataToSoundByAllSegment(myMsg.mByteArray, myMsg.mIntParam1);
                            //SystemConfig.mMyEventLogger.appendDebugStr("dataToSoundByAllSegment","");
                            break;
                    }
                }
            }catch(Exception ex1){
                //SystemConfig.mMyEventLogger.appendDebugStr("AudioPlayerThread.Exception","");
                //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
                ex1.printStackTrace();
            }
        }
    }


    private int getMsgCntInQ(){
        int iRemain;
        try {
            iRemain = mMyMsgQueue.getRemainCapacity();
            return (INT_AUDIO_MSGQ_SIZE - iRemain);
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("getMsgCntInQ.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            return 0;
        }
    }

    private boolean putMsg(MyThreadQMsg myMsg){
        try {
            if (mMyMsgQueue.putMsg(myMsg, 1000) == false) {
                mIntPutMsgLostCnt++;
                return false;
            }
            return true;
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("putMsg.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            return false;
        }
    }

    public boolean putMsgForAudioOpen(int iSamplerate){
       //try {
            MyThreadQMsg myMsgForAudio = new MyThreadQMsg();
            myMsgForAudio.mIntMsgId = MyThreadQMsg.INT_MY_MSG_CMD_AUDIO_OPEN;
            myMsgForAudio.mByteArray = null;
            myMsgForAudio.mIntParam1 = iSamplerate;
            putMsg(myMsgForAudio);
            return true;
        //}catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("putMsgForAudioOpen.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        //    return false;
        //}
    }

    public boolean putMsgForAudioClose(){

        try {
            MyThreadQMsg myMsgForAudio = new MyThreadQMsg();
            myMsgForAudio.mIntMsgId = MyThreadQMsg.INT_MY_MSG_CMD_AUDIO_CLOSE;
            myMsgForAudio.mByteArray = null;
            putMsg(myMsgForAudio);
            return true;
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("putMsgForAudioClose.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            return false;
        }
    }


    public boolean putMsgForAudioSegmentOnLine(int iSegmentEndIdx){
        int iMsgCnt;

        try {
            MyThreadQMsg myMsgForAudio = new MyThreadQMsg();
            myMsgForAudio.mIntMsgId = MyThreadQMsg.INT_MY_MSG_CMD_AUDIO_SEGMENT_ON_LINE;
            myMsgForAudio.mIntParam1 = iSegmentEndIdx;
            putMsg(myMsgForAudio);

            iMsgCnt = getMsgCntInQ();
            if (mIntMaxMsgCntInQ < iMsgCnt) {
                mIntMaxMsgCntInQ = iMsgCnt;
            }
            if (mIntMaxMsgCntStartInQ == 0) {
                if (mIntMaxMsgCntInQ > 0) {
                    mIntMaxMsgCntStartInQ = mIntMaxMsgCntInQ;
                }
            }

            return true;
        }catch(Exception ex1){
            ex1.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugStr("putMsgForAudioSegmentOnLine.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            return false;
        }
    }



    public boolean putMsgForAudioAllSegment(byte[] byteArray, int iLength){

        try {
            MyThreadQMsg myMsgForAudio = new MyThreadQMsg();
            myMsgForAudio.mIntMsgId = MyThreadQMsg.INT_MY_MSG_CMD_AUDIO_ALL_SEGMENT;
            myMsgForAudio.mByteArray = byteArray;
            myMsgForAudio.mIntParam1 = iLength;
            putMsg(myMsgForAudio);
            return true;
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("putMsgForAllSegment.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            return false;
        }
    }



    public void openAudio(int iSamplingRate){
        try {
            if (mAudioPlayer != null) {
                mAudioPlayer.StopPlayAudio();
                mAudioPlayer = null;
            }
            mAudioPlayer = new MyAudioPlayer();
            //?mAudioPlayer.StartPlayAudio(8500);
            mAudioPlayer.StartPlayAudio(iSamplingRate);
            isPlaying = true;

        }catch(Exception ex1){
            ex1.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugStr("Controller.openAudio.Excpetion","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }

    public void closeAudio(){
        try{

            //SystemConfig.mMyEventLogger.appendDebugStr("Audio.LostCnt=", String.valueOf(mAudioPlayer.mIntLostCnt));

            if(mAudioPlayer != null){
                mAudioPlayer.StopPlayAudio();
                mAudioPlayer=null;
                isPlaying = false;
            }

            //SystemConfig.mMyEventLogger.appendDebugStr("Audio.MaxMsgCntStartInQ=", String.valueOf(mIntMaxMsgCntStartInQ));
            //SystemConfig.mMyEventLogger.appendDebugStr("Audio.MaxMsgCntInQ=", String.valueOf(mIntMaxMsgCntInQ));
        }catch(Exception ex1){
            ex1.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugStr("Controller.closeAudio.Excpetion","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            //SystemConfig.mMyEventLogger.appendDebugStr("Audio.MaxMsgCntInQ=", String.valueOf(mIntMaxMsgCntInQ));
        }
    }
/*
    public void dataToSoundBySegmentEndIdxOnLine(){
        mAudioPlayer.dataToSoundBySegmentEndIdxOnLine();
    }
*/

    public void dataToSoundBySegment(byte[] byteArray, int iLength){
        mAudioPlayer.dataToSoundBySegment(byteArray, iLength);
    }

    public void dataToSoundByAllSegment(byte[] byteArray, int iLength){
        mAudioPlayer.dataToSoundByAllSegment(byteArray, iLength);
    }


    public void clearAllMsg(){
        try {
            mMyMsgQueue.clearMsg();
            mIntPutMsgLostCnt = 0;
            mIntMaxMsgCntInQ = 0;
            mIntMaxMsgCntStartInQ = 0;
            //SystemConfig.mMyEventLogger.appendDebugStr("audioCtrl.clearMsg", "");
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("AudioCtrl.clearMsg.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }


    public void clearAllButCloseAudioMsg(){
        MyThreadQMsg myMsg, myCloseAudioMsg = null;

        try {

            while(true) {
                myMsg = mMyMsgQueue.getMsg(0);
                if(myMsg == null){
                    break;
                }else if(myMsg.mIntMsgId == MyThreadQMsg.INT_MY_MSG_CMD_AUDIO_CLOSE){
                    myCloseAudioMsg = myMsg;
                    break;
                }
            }

            if(myCloseAudioMsg != null){
                mMyMsgQueue.putMsg(myCloseAudioMsg, MyMsgQueue.MY_MSG_TIMEOUT_MILISEC_INFINITIVE);
            }
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("clearAllButCloseAudioMsg.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }
}
