package com.gis.heartio.SignalProcessSubsystem.SupportSubsystem;

/**
 * Created by brandon on 2017/1/9.
 */

public class MyThreadQMsg {

    public static final int INT_MY_MSG_CMD_SPROCESS_PREPARE_PROCESS=1;
    public static final int INT_MY_MSG_CMD_SPROCESS_DATA_FINISH =2;
    public static final int INT_MY_MSG_EVT_SPROCESS_DATA_IN_START=3;
    public static final int INT_MY_MSG_EVT_SPROCESS_DATA_IN=4;
    public static final int INT_MY_MSG_EVT_SPROCESS_DATA_IN_END=5;
    public static final int INT_MY_MSG_EVT_SPROCESS_RESULT_OK =6;
    public static final int INT_MY_MSG_CMD_SPROCESS_STOP_BY_FIRST_POWER =7;

    //---------------------------------------------------
    // for Message between MainThread and AudioPlayer
    //----------------------------------------------------
    public static final int INT_MY_MSG_CMD_AUDIO_OPEN=10;
    public static final int INT_MY_MSG_CMD_AUDIO_CLOSE=11;
    public static final int INT_MY_MSG_CMD_AUDIO_SEGMENT_ON_LINE=12;
    public static final int INT_MY_MSG_CMD_AUDIO_ALL_SEGMENT=13;

    public static final int INT_MY_MSG_CMD_LONG_START_TO_START = 14;
    public static final int INT_MY_MSG_CMD_LONG_START_TO_STOP = 15;


    public int mIntMsgId;
    public int mIntParam1;
    public byte[] mByteArray;
}
