package com.gis.heartio.SignalProcessSubsystem.SupportSubsystem;

/**
 * Created by 780797 on 2016/8/29.
 */

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MyMsgQueue {

    public final static int MY_MSG_QUEUE_DEFAULT_SIZE = 1000;
    public static int MY_MSG_TIMEOUT_MILISEC_INFINITIVE = Integer.MAX_VALUE;
    public static int MY_MSG_TIMEOUT_MILISEC_1000 = 1000;
    public static int MY_MSG_TIMEOUT_MILISEC_2000 = 2000;
    public static int MY_MSG_TIMEOUT_MILISEC_3000 = 3000;

    //private int mIntDiscardMsgCnt;

    private ArrayBlockingQueue<MyThreadQMsg> mArrayBlockingQueue;

    public MyMsgQueue(int iQSize){
        if(iQSize <= 0) {
            mArrayBlockingQueue = new ArrayBlockingQueue<>(MY_MSG_QUEUE_DEFAULT_SIZE);
        }else{
            mArrayBlockingQueue = new ArrayBlockingQueue<>(iQSize);
        }
        //mIntDiscardMsgCnt=0;
    }

    public MyThreadQMsg getMsg(int iTimeoutMilisec){
        MyThreadQMsg myMsg = null;

        try {

            myMsg = mArrayBlockingQueue.poll(iTimeoutMilisec, TimeUnit.MILLISECONDS);
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("MyMsgQ.getMsg.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }

        return myMsg;
    }


    public boolean putMsg(MyThreadQMsg myMsg, int iTimeoutMilisec){
        boolean boolVar = false;

        try{
            boolVar = mArrayBlockingQueue.offer(myMsg, iTimeoutMilisec, TimeUnit.MILLISECONDS);
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("MyMsgQ.putMsg.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
        return boolVar;
    }

    public void clearMsg(){
        MyThreadQMsg myMsg;

        try {
            do {
                myMsg = mArrayBlockingQueue.poll(0, TimeUnit.MILLISECONDS);
            } while (myMsg != null);
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("MyMsgQ.clearMsg.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }

    public int getRemainCapacity(){
            return mArrayBlockingQueue.remainingCapacity();
    }
}
