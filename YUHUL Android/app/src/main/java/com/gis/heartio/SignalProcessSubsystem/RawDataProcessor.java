package com.gis.heartio.SignalProcessSubsystem;

/**
 * Created by Cavin on 2018/1/2.
 */


import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.gis.BLEConnectionServices.BluetoothLeService;
import com.gis.heartio.GIS_Log;
import com.gis.heartio.SignalProcessSubsysII.utilities.Doppler;
import com.gis.heartio.SignalProcessSubsysII.utilities.Type;
import com.gis.heartio.SupportSubsystem.MyDataFilter2;
import com.gis.heartio.SupportSubsystem.SystemConfig;
import com.gis.heartio.SupportSubsystem.Utilitys;
import com.gis.heartio.UIOperationControlSubsystem.MainActivity;
import com.gis.heartio.UIOperationControlSubsystem.UserManagerCommon;
import com.gis.heartio.heartioApplication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
// Audio classifier
import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.task.audio.classifier.Classifications;

public class RawDataProcessor {

    private static final String TAG = "RawDataProcessor";
    public static final int RX_MAX_SEQENCE_NUM = 255;

    public enum ENUM_RAW_DATA_RX_STATE{
        RECEIVE_STATE_START,RECEIVE_STATE_CONTINUOUS,RECEIVE_STATE_END}
    public  ENUM_RAW_DATA_RX_STATE mEnumUltrasoundAttributeReceiveState;

    public enum ENUM_RAW_DATA_ONE_DATA_RX_STATE{
        RECEIVE_STATE_START,RECEIVE_STATE_CONTINUOUS,RECEIVE_STATE_END}
    public  ENUM_RAW_DATA_ONE_DATA_RX_STATE mEnumUltrasoundOneDataReceiveState;

    private static final int LOST_SEQ_MSG_MAX_CNT = 100;
    public int[] mIntLostSeqMsgs;
    public int mIntNextSeqLostMsgIdx;
    public int mIntPreSeqNum;

    public byte[] mByteArrayWavDataOffLine;
    private byte[] mByteArrayUltrasoundDataOnLineSave;
    private byte[] mByteArrayWaveDataAfterFilter;
    public short[] mShortUltrasoundData, mShortUltrasoundDataBeforeFilter;
    public short[] mShortUltrasoundDataInverse;
    public int[] mIntUltrasoundDataNotDCOffset;
    public short[] mShortEcgData;
    private byte[][] mByteRawPacketArrayOnLine;
    public int mIntDataNextIndex, mIntTryDataNextIndex;
    private int mIntDataCurSizeOffLine;
    public int mIntDataMaxValue, mIntDataMaxIdx;
    public int mIntCurPacketIdx;

    public int mIntDataDiffBaseline;
    public int mIntDataDiffMaxValue;
    public int mIntDataDiffMinValue;

    public byte[] mByteArrayWavHeader ;

    public String mStrBaseFolder;

    private int mIntNextSeqNum , mIntCurSeqNum;
    public String mStrCurSeqNums ;
    public String mStrLostSeqNums;
    public String mStrSpeed ;
    public boolean mBoolRxError ;

    //private byte[] mByteCurPacketLittleEndian;
    private byte[] mByteCurPacket;

    private int mShortDCOffsetLearnSize, mIntDCAccu;
    public short mShortDCOffset;
    public boolean mBoolDCOffsetLearned;

    public String  mStrFileTail;

    private int[] mIntPacketData;
    private byte[] mByteArray2Bytes;

    public int[] mIntUltrasoundDataGainLevel;
    public int  mIntGainFormatWatchCnt=0;
    private int mIntCurGainWtachCnt;
    private int mIntPreGainOnLine; // mIntPreGainCommandOnLine;
    public int mIntPacketIdxForGainTest;
    public int mIntGainObservePacketCntCur, mIntGainChangeIntervalPacketCnt;
    public int mIntGainObserveDiffValueMax, mIntGainObserveDiffValueMin;
    private byte[] mByteArrayGainCommand;
    public int mIntGainLevelCommandCur, mIntGainLevelCommandPre;

    public int mIntCurPacketDataDiffValueMax, mIntCurPacketDataDiffValueMin;

    private boolean mBoolDebugDataEnd;


    private MyDataFilter2 mMyDataFilter;

    //public int mIntDebugAllPacketDataDiffValueMax, mIntDebugAllPacketDataDiffValueMin;

    public int INT_POWER_SHOW_BETWEEN_CNT = 200;
    public int mIntPowerShowBetweenCntCur;
    public int mIntPowerLevelPre=-1;
    private boolean mBoolGainLevelFirstShowed;

    public String mStrCurFileName;
    private heartioApplication mApplication;
    private AppCompatActivity mActivity = null;

    public int mIntGainControlHighThresholdDiff,mIntGainControlLowThresholdDiff, mIntGainControlTargetValueDiff;
    public int mIntBasicGainForSound = 8;

    public int mIntLostCount = 0;
    private int mIntTotalPacketCount = 0;

    // For HeartIO2 write file
    private final int maxNPacket = 7000;//10000;
    public int openfile_status, nPacketStored;
    public FileOutputStream data_os;

    // Audio Classifier
    private AudioClassifier audioClassifier = null;
    private  TensorAudio tensorAudio = null;
    private int classificationIntervalPts = 1000; //原本4000

    private final String MODEL_FILE = "03_3.tflite";  //"soundclassifier_with_metadata.tflite";
     //"USPA_model_14.tflite";
    private final float MINIMUM_DISPLAY_THRESHOLD = 0.3f;

    public RawDataProcessor(){
        try {
            /*
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                mStrBaseFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
            } else {
                mStrBaseFolder = SystemConfig.mFragment.getActivity().getFilesDir().getAbsolutePath();
            }
            */

            mIntLostSeqMsgs = new int[LOST_SEQ_MSG_MAX_CNT];

            mByteArray2Bytes = new byte[2];

            mIntPacketData = new int[SystemConfig.mIntPacketDataByteSize / SystemConfig.INT_ULTRASOUND_DATA_1DATA_BYTES_16PCM];
            mByteArrayGainCommand = new byte[1];

            mMyDataFilter = new MyDataFilter2();
        }catch(Exception ex1){
            ex1.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugStr("RawDataProc.constrct.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }

    public void updateStrBaseFolder(){
        mStrBaseFolder = Utilitys.getUserBaseFilePath(mActivity,UserManagerCommon.mUserInfoCur.userID );
    }

    public void initRawDataProcessor(Activity activity) {
        int iVar, iVar2;
        //Calendar calendarVar;
        mActivity = (AppCompatActivity) activity;
        mApplication = (heartioApplication) mActivity.getApplication() ;
        try {

            mStrFileTail = "";
            //----------------------------
            // for online init
            //-----------------------------
            iVar = (SystemConfig.INT_ULTRASOUND_1PACKET_PAYLOAD_BYTES_ITRI - SystemConfig.INT_ULTRASOUND_DATA_GAIN_BYTES_ITRI - SystemConfig.INT_ULTRASOUND_DATA_SEQ_BYTES_ITRI);

            mByteRawPacketArrayOnLine = new byte[SystemConfig.mIntRawPacketCntsOnLine][SystemConfig.INT_ULTRASOUND_1PACKET_PAYLOAD_BYTES_ITRI];
            //mByteCurPacket = new byte[SystemConfig.mIntPacketDataSizeMax+SystemConfig.ULTRASOUND_DATA_SEQ_BYTES_ITRI+SystemConfig.ULTRASOUND_DATA_GAIN_BYTES_ITRI];
            mByteCurPacket = new byte[SystemConfig.INT_ULTRASOUND_1PACKET_PAYLOAD_BYTES_ITRI];
            //mByteCurPacketLittleEndian = new byte[SystemConfig.mIntPacketDataSize+SystemConfig.ULTRASOUND_DATA_SEQ_BYTES_ITRI+SystemConfig.ULTRASOUND_DATA_GAIN_BYTES_ITRI];


            //--------------------------------------------------
            //--------------------------------------------------

            mIntDataNextIndex = 0;
            mIntTryDataNextIndex = 0;

            mIntCurPacketIdx = 0;
            mIntNextSeqNum = -1;

            mIntDataDiffBaseline = 0;
            mIntDataDiffMaxValue = Integer.MIN_VALUE;
            mIntDataDiffMinValue = Integer.MAX_VALUE;

            mStrCurSeqNums = "";
            mStrLostSeqNums = "";
            //mStrChkSumErrorSeqNums = "";
            mBoolRxError = false;

            //calendarVar = Calendar.getInstance();
            //mStrFileDateTimePrefix = "UDB" + String.valueOf(calendarVar.get(Calendar.YEAR));
            //mStrFileDateTimePrefix =  mStrFileDateTimePrefix + String.valueOf(calendarVar.get(Calendar.MONTH)+1);
            //mStrFileDateTimePrefix =   mStrFileDateTimePrefix  + String.valueOf(calendarVar.get(Calendar.DAY_OF_MONTH));
            //mStrFileDateTimePrefix =   mStrFileDateTimePrefix  + String.valueOf(calendarVar.get(Calendar.HOUR_OF_DAY)) + "-";

            mStrBaseFolder = Utilitys.getUserBaseFilePath(mActivity,UserManagerCommon.mUserInfoCur.userID );

            mByteArrayWavHeader = new byte[44];

            mShortUltrasoundData = new short[SystemConfig.INT_ULTRASOUND_DATA_MAX_SIS];
            mIntUltrasoundDataNotDCOffset = new int[SystemConfig.INT_ULTRASOUND_DATA_MAX_SIS];
            mIntUltrasoundDataGainLevel = new int[SystemConfig.INT_ULTRASOUND_DATA_MAX_SIS];
//            mShortUltrasoundDataBeforeFilter = new short[SystemConfig.INT_ULTRASOUND_DATA_MAX_SIS];
            /* 將 mShortUltrasoundDataBeforeFilter 長度重設為112000 (8000K * 14s) 2023/02/07 by Doris*/
            mShortUltrasoundDataBeforeFilter = new short[SystemConfig.INT_ULTRASOUND_START_ONLINE_SEC * SystemConfig.INT_ULTRASOUND_SAMPLING_RATE_8K];
            mByteArrayUltrasoundDataOnLineSave = new byte[SystemConfig.INT_ULTRASOUND_DATA_MAX_SIS_ITRI_8K];
            mByteArrayWaveDataAfterFilter = new byte[SystemConfig.INT_ULTRASOUND_DATA_MAX_SIS * SystemConfig.INT_ULTRASOUND_DATA_1DATA_BYTES_16PCM];

            // Cavin add for inverse sensor
            mShortUltrasoundDataInverse = new short[SystemConfig.INT_ULTRASOUND_DATA_MAX_SIS];
            // ECG data array initial
            mShortEcgData = new short[SystemConfig.INT_ECG_DATA_MAX_SIZE];

        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("initRawDataProc.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            ex1.printStackTrace();
        }
    }

    public void prepareStartCommon(){
        int iVar1, iVar2;

        //try {

            mIntPowerShowBetweenCntCur = 0;

            mBoolRxError = false;

            mIntDataNextIndex = 0;
            mIntTryDataNextIndex = 0;
            mIntDataMaxValue = 0;
            mIntDataMaxIdx = 0;

            mEnumUltrasoundOneDataReceiveState = RawDataProcessor.ENUM_RAW_DATA_ONE_DATA_RX_STATE.RECEIVE_STATE_START;
            mEnumUltrasoundAttributeReceiveState = RawDataProcessor.ENUM_RAW_DATA_RX_STATE.RECEIVE_STATE_START;
            mIntNextSeqLostMsgIdx = 0;

            mIntCurPacketIdx = 0;
            mIntNextSeqNum = -1;

            mIntDataDiffMaxValue = Integer.MIN_VALUE;
            mIntDataDiffMinValue = Integer.MAX_VALUE;

            mShortDCOffset = 0;
            mIntDCAccu = 0;

            mMyDataFilter.prepareStart();
            mShortDCOffsetLearnSize = (int) (SystemConfig.mIntUltrasoundSamplerate * SystemConfig.DOUBLE_NOISE_SIGNAL_LEARN_START_SEC) - 1;

            //mIntDebugAllPacketDataDiffValueMax = Integer.MIN_VALUE;
            //mIntDebugAllPacketDataDiffValueMin = Integer.MAX_VALUE;

            mBoolDCOffsetLearned = false;

            SystemConfig.mIntSVDrawSizeRawData = SystemConfig.INT_SV_DRAW_SIZE_DEFAULT_RAW_DATA;

            mBoolDebugDataEnd = false;
            if (SystemConfig.isHeartIO2 && SystemConfig.saveFile){
                openfile_status = 0;
            }
       // }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("RawDataProc.prepareStartCommon.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
       // }
    }

    public void prepareStartOnLine(){
        //try {
            prepareStartCommon();
            mIntGainFormatWatchCnt = 0;
            mIntPacketIdxForGainTest = 0;
            mIntPreGainOnLine = -1;
            //mIntPreGainCommandOnLine = -1;
            //SystemConfig.mIntGainCommand = -1;
            mIntGainObserveDiffValueMax = Integer.MIN_VALUE;
            mIntGainObserveDiffValueMin = Integer.MAX_VALUE;

            mIntGainChangeIntervalPacketCnt = ((SystemConfig.mIntUltrasoundSamplerate * SystemConfig.INT_GAIN_CHANGE_INTERVAL_MSEC * 2) / SystemConfig.mIntPacketDataByteSize / 1000) + 1;
            mIntGainObservePacketCntCur = 0;

            mBoolGainLevelFirstShowed = false;
            //mEnumGainFormat = ENUM_GAIN_FORMAT.FORMAT_ADJUST_ENABLE;

            SystemConfig.mIntUltrasoundSamplesMaxSizeForRun = SystemConfig.mIntUltrasoundSamplesMaxSize;
            SystemConfig.mIntRawPacketCntsOnLineForRun = SystemConfig.getPacketCntFormDataSize(SystemConfig.mIntUltrasoundSamplesMaxSizeForRun);

            SystemConfig.mIntSVDrawSizeRawData = SystemConfig.INT_SV_DRAW_SIZE_DEFAULT_RAW_DATA;

            mIntGainLevelCommandCur = -1;   // Not Checked
            mIntGainLevelCommandPre = -1;   // Not Checked

            mIntLostCount = 0;
            mIntTotalPacketCount = 0;
            try {
                startAudioClassification();
            }catch (IOException ex1){
                ex1.printStackTrace();
            }

       // }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("RawDataProc.prepareStartOnLine.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
      //  }
     }

    public void prepareStartOffLine(){
        int iVar;

        prepareStartCommon();
    }


    public boolean receiveAttributeDataBySegmentOnLine(byte[] byteArray) {
        int iLength;
        boolean boolReturn;

        try {

            if (byteArray.length != SystemConfig.INT_ULTRASOUND_1PACKET_PAYLOAD_BYTES_ITRI && !SystemConfig.isHeartIO2) {
                Log.d(TAG,"byteArray.length != SystemConfig.ULTRASOUND_1PACKET_PAYLOAD_BYTES_ITRI");
                return false;
            } else if(byteArray.length != SystemConfig.INT_HEARTIO2_1PACKET_PAYLOAD_BYTES && SystemConfig.isHeartIO2){
                return false;
            }

            if (SystemConfig.isHeartIO2){
//                Log.d(TAG,"isHeartIO2");
                boolReturn = processUltrasoundAndEcgBySegOnline(byteArray);
            }else{
                boolReturn = processUltrasoundDataBySegmentOnLine(byteArray);
            }

            if (!boolReturn) {
                //Log.d(TAG,"!boolReturn");
                return false;
            }

            if (mEnumUltrasoundAttributeReceiveState == ENUM_RAW_DATA_RX_STATE.RECEIVE_STATE_START) {
//                Log.d(TAG,"mEnumUltrasoundAttributeReceiveState = ENUM_RAW_DATA_RX_STATE.RECEIVE_STATE_START");
                mIntCurPacketIdx = 0;
                /*SystemConfig.mLongDataStartMillis = SystemClock.elapsedRealtime();*/
                mEnumUltrasoundAttributeReceiveState = ENUM_RAW_DATA_RX_STATE.RECEIVE_STATE_CONTINUOUS;
            } else if(SystemConfig.mEnumTryState != SystemConfig.ENUM_TRY_STATE.STATE_TRY){
                mIntCurPacketIdx++;
                if (mIntCurPacketIdx == (SystemConfig.mIntRawPacketCntsOnLineForRun - 1)) {
//                    Log.d(TAG,"mEnumUltrasoundAttributeReceiveState = ENUM_RAW_DATA_RX_STATE.RECEIVE_STATE_END");
//                    Log.d(TAG,"mIntCurPacketIdx="+mIntCurPacketIdx);
                    mEnumUltrasoundAttributeReceiveState = ENUM_RAW_DATA_RX_STATE.RECEIVE_STATE_END;
                    /*SystemConfig.mLongDataEndMills = SystemClock.elapsedRealtime();*/
                }
            }

            return true;
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("RawDataProcessor.receiveAttr.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            ex1.printStackTrace();
            return false;
        }
    }

    public void changeReceiveDataMaxSizeByStop(){
        int iVar;

        if (SystemConfig.mIntRawPacketCntsOnLineForRun > mIntCurPacketIdx+2) {
            SystemConfig.mIntRawPacketCntsOnLineForRun = mIntCurPacketIdx + 2;
            iVar = (SystemConfig.INT_ULTRASOUND_1PACKET_PAYLOAD_BYTES_ITRI - SystemConfig.INT_ULTRASOUND_DATA_GAIN_BYTES_ITRI - SystemConfig.INT_ULTRASOUND_DATA_SEQ_BYTES_ITRI);
            SystemConfig.mIntUltrasoundSamplesMaxSizeForRun = (SystemConfig.mIntRawPacketCntsOnLineForRun * iVar) / SystemConfig.mInt1DataBytes;
        }
    }

    public boolean processUltrasoundAndEcgBySegOnline(byte[] byteArray){
        boolean ret = false;
        int	maxL;
        int[] newValue;
        int[] newECG_1;
        int[] newECG_2;

        int	sN_1 = 0, sN_2;
        int	ADS_status = 0;
        int	ECG_1, ECG_2 = 0;
        int[] ADCInt = new int[16];
        int BatteryStatus;

        //.	newValue = new int[byteArray.length];
        newValue = new int[21];
        newECG_1 = new int[1];
        newECG_2 = new int[1];

        if (byteArray != null) {
			/*
            // receive battery value
            if (byteArray.length == 1) {
                //printToConsole("[ReceiveByte] battery value is : " + String.format("%d",byteArray[0]) + "%" );
                batteryValue.setText(String.format("%d",byteArray[0]) + "%");
                return;
            }
            else {
                dataProcessHandle.parseIncomingData(byteArray);
            }
			*/
            maxL = byteArray.length;

//            if (openfile_status == 0 && SystemConfig.saveFile) {			//.	open file
//                //.	改由 Toggle button 的程式處理
//                //.	dataProcessHandle.createDataFile(ctx);
//                //.	openfile_status = 1;
//                nPacketStored = 0;
//            }

//            if (openfile_status == 1 && SystemConfig.saveFile) {
//            if (openfile_status >= 1) {

            //.	clear
            //.	for (int i=0; i<CHAR6_LENGTH; i++) newValue[i] = 0;

            //.	byte to integer
            for (int i=0; i<maxL; i++) {
                int	k = i % 40;

                int	itmp = Utilitys.byteToInt( byteArray[i]);
                //.	parsing

                ECG_1 = 0;
                sN_2 = 0;
                switch (k)	{
                    case 0:	ADS_status = 0x000000ff & itmp;							break;
                    case 1:	ADS_status = (ADS_status << 8) | (0x000000ff & itmp);	break;

                    case 2:	  							                    break;  // Do nothing
                    case 3:	BatteryStatus = (0x000000ff & itmp);                   	break;

                    case 4:	sN_1 = (0x000000ff & itmp);		                        break;
                    case 5:	sN_1 = (sN_1 << 8) | (0x000000ff & itmp);               break;

                    case 6:	ECG_2 = 0x0000000f & itmp;			                    break;
                    case 7:	ECG_2 = (ECG_2 << 8) | (0x000000ff & itmp);		        break;

                    case 8:	ADCInt[0] = 0x0000000f & itmp;								break;
                    case 9:	ADCInt[0] = (ADCInt[0] << 8) | (0x000000ff & itmp);				break;

                    case 10:ADCInt[1] = 0x0000000f & itmp;								break;
                    case 11:ADCInt[1] = (ADCInt[1] << 8) | (0x000000ff & itmp);				break;

                    case 12:ADCInt[2] = 0x0000000f & itmp;                              break;
                    case 13:ADCInt[2] = (ADCInt[2] << 8) | (0x000000ff & itmp);				break;

                    case 14:ADCInt[3] = 0x0000000f & itmp;                              break;
                    case 15:ADCInt[3] = (ADCInt[3] << 8) | (0x000000ff & itmp);				break;

                    case 16:ADCInt[4] = 0x0000000f & itmp;                              break;
                    case 17:ADCInt[4] = (ADCInt[4] << 8) | (0x000000ff & itmp);				break;

                    case 18:ADCInt[5] = 0x0000000f & itmp;                              break;
                    case 19:ADCInt[5] = (ADCInt[5] << 8) | (0x000000ff & itmp);				break;

                    case 20:ADCInt[6] = 0x0000000f & itmp;								break;
                    case 21:ADCInt[6] = (ADCInt[6] << 8) | (0x000000ff & itmp);				break;

                    case 22:ADCInt[7] = 0x0000000f & itmp;								break;
                    case 23:ADCInt[7] = (ADCInt[7] << 8) | (0x000000ff & itmp);				break;

                    case 24:ADCInt[8] = 0x0000000f & itmp;								break;
                    case 25:ADCInt[8] = (ADCInt[8] << 8) | (0x000000ff & itmp);				break;

                    case 26:ADCInt[9] = 0x0000000f & itmp;								break;
                    case 27:ADCInt[9] = (ADCInt[9] << 8) | (0x000000ff & itmp);				break;

                    case 28:ADCInt[10] = 0x0000000f & itmp;								break;
                    case 29:ADCInt[10] = (ADCInt[10] << 8) | (0x000000ff & itmp);			break;

                    case 30:ADCInt[11] = 0x0000000f & itmp;								break;
                    case 31:ADCInt[11] = (ADCInt[11] << 8) | (0x000000ff & itmp);			break;

                    case 32:ADCInt[12] = 0x0000000f & itmp;                             break;
                    case 33:ADCInt[12] = (ADCInt[12] << 8) | (0x000000ff & itmp);			break;

                    case 34:ADCInt[13] = 0x0000000f & itmp;                             break;
                    case 35:ADCInt[13] = (ADCInt[13] << 8) | (0x000000ff & itmp);			break;

                    case 36:ADCInt[14] = 0x0000000f & itmp;                             break;
                    case 37:ADCInt[14] = (ADCInt[14] << 8) | (0x000000ff & itmp);			break;

                    case 38:ADCInt[15] = 0x0000000f & itmp;                             break;
                    case 39:ADCInt[15] = (ADCInt[15] << 8) | (0x000000ff & itmp);			break;
                }


                if (k == 39) {
                    mIntCurSeqNum = sN_1;
                    newValue[0] = sN_1;
                    newValue[1] = sN_2;

                    newValue[2] = ADS_status;

                    newValue[3] = ECG_1;
                    newValue[4] = ECG_2;
                    //Log.d(TAG,"ECG = "+ECG_2);
                    // add to data array
                    for (int j=5;j<newValue.length;j++){
                        newValue[j] = ADCInt[j-5];
                        addUltrasoundNewSampleBySegmentOnLine(newValue[j]);
                    }
                    addECGNewSampleBySegmentOnLine(ECG_2);

                    //.	write to file
                    if (openfile_status == 1 && SystemConfig.saveFile ) writeDataVal(newValue);

                    if (openfile_status == 1 && SystemConfig.saveFile) {
                        nPacketStored = nPacketStored + 1;

                        if (nPacketStored >= maxNPacket) {
                            closeDataFile();
                            openfile_status = 2;

//                            mActivity.runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast.makeText(ctx, "data file closed", Toast.LENGTH_SHORT).show();
//                                }
//                            });
                        }
                    }

                    if (mIntNextSeqNum != mIntCurSeqNum && mIntNextSeqNum!= -1) {
                        mBoolRxError = true;
                        int oneLostCount = Math.abs(mIntCurSeqNum - mIntNextSeqNum);
                        mIntLostCount += oneLostCount;
                        Log.d(TAG,"Lost 40 * "+oneLostCount+"   bytes");
                    }
                    mIntPreSeqNum = mIntCurSeqNum;
                    mIntNextSeqNum = mIntCurSeqNum + 1;
                    if (mIntNextSeqNum == 0xFFFF+1){
                        mIntNextSeqNum = 0;
                    }
                }


            }
//            }  // openfile_status check end.



            ret = true;
        }
        return ret;
    }

    public boolean processUltrasoundDataBySegmentOnLine(byte[] byteArray){
        byte[] byteData, byteSeq, byteGainPower;
        int iVar , iVar4, iVar5, iNoOfSamplesInOnePacket, iLostSeqNum, iLength, iData;
        boolean boolGainLevelMsgShow;

        try {

            iNoOfSamplesInOnePacket = SystemConfig.mIntPacketDataByteSize / SystemConfig.mInt1DataBytes;

            byteData = new byte[SystemConfig.mInt1DataBytes];
            byteSeq = new byte[SystemConfig.INT_ULTRASOUND_DATA_SEQ_BYTES_ITRI];
            byteGainPower = new byte[SystemConfig.INT_ULTRASOUND_DATA_GAIN_BYTES_ITRI];
            //Log.d(TAG,"byteArray.length="+byteArray.length);
            /*for (iVar = 0; iVar < SystemConfig.INT_ULTRASOUND_1PACKET_PAYLOAD_BYTES_ITRI; iVar++) {
                mByteCurPacket[iVar] = byteArray[iVar];
            }*/
            // memory copy change to Arrays.copyOf .
            mByteCurPacket = Arrays.copyOf(byteArray,SystemConfig.INT_ULTRASOUND_1PACKET_PAYLOAD_BYTES_ITRI);

            for (iVar5 = 0; iVar5 < SystemConfig.INT_ULTRASOUND_DATA_SEQ_BYTES_ITRI; iVar5++) {
                byteSeq[iVar5] = mByteCurPacket[SystemConfig.INT_ULTRASOUND_1PACKET_PAYLOAD_BYTES_ITRI - SystemConfig.INT_ULTRASOUND_DATA_GAIN_BYTES_ITRI - SystemConfig.INT_ULTRASOUND_DATA_SEQ_BYTES_ITRI + iVar5];
            }


            for (iVar5 = 0; iVar5 < SystemConfig.INT_ULTRASOUND_DATA_GAIN_BYTES_ITRI; iVar5++) {
                byteGainPower[iVar5] = mByteCurPacket[SystemConfig.INT_ULTRASOUND_1PACKET_PAYLOAD_BYTES_ITRI - SystemConfig.INT_ULTRASOUND_DATA_GAIN_BYTES_ITRI + iVar5];
            }

            if(SystemConfig.mIntPowerLevel == -1) {
                SystemConfig.mIntPowerLevel = Utilitys.getPowerFrombyteArray(byteGainPower);
                if (MainActivity.oFrag!=null){
                    MainActivity.oFrag.putUiMsg(MainActivity.UI_MSG_ID_SHOW_POWER_LEVEL);
                }
                mIntPowerLevelPre = SystemConfig.mIntPowerLevel;
                /*if ((SystemConfig.mBoolDebugPowerLevelChangeHistory) || (SystemConfig.mIntSystemSettingFunction == SystemConfig.INT_SYSTEM_SETTING_FUNCTION_2_POWER_CONSUME_TEST)){
                    Date date = new Date();
                    SystemConfig.mMyEventLogger.appendPowerLevelEvent(date, " PowerStart = ", SystemConfig.mIntPowerLevel);
                }*/
                    //Log.d(TAG,"Power Level : "+SystemConfig.mIntPowerLevel);
            } else if((mIntDataNextIndex % INT_POWER_SHOW_BETWEEN_CNT) == 0){
                SystemConfig.mIntPowerLevel = Utilitys.getPowerFrombyteArray(byteGainPower);
//                Log.d(TAG,"mIntPowerLevelPre="+mIntPowerLevelPre+" ,SystemConfig.mIntPowerLevel="+SystemConfig.mIntPowerLevel);
                if(mIntPowerLevelPre != SystemConfig.mIntPowerLevel
                        || SystemConfig.mIntPowerLevel <= 2) {
                    if (MainActivity.oFrag != null){
                        MainActivity.oFrag.putUiMsg(MainActivity.UI_MSG_ID_SHOW_POWER_LEVEL);
                    }
                    mIntPowerLevelPre = SystemConfig.mIntPowerLevel;
                    //if ((SystemConfig.mBoolDebugPowerLevelChangeHistory) || (SystemConfig.mIntSystemSettingFunction == SystemConfig.INT_SYSTEM_SETTING_FUNCTION_2_POWER_CONSUME_TEST)) {
                        //Date date = new Date();
                        //SystemConfig.mMyEventLogger.appendPowerLevelEvent(date, " PowerChange = ", SystemConfig.mIntPowerLevel);
                    //}
                }
            }

            if(mEnumUltrasoundAttributeReceiveState == ENUM_RAW_DATA_RX_STATE.RECEIVE_STATE_END){
                return false;
            }

            if ((SystemConfig.mEnumTryState == SystemConfig.ENUM_TRY_STATE.STATE_TRY_STOP)
                    && (SystemConfig.mEnumStartState == SystemConfig.ENUM_START_STATE.STATE_STOP)){
                return false;
            }

            mIntCurSeqNum = Utilitys.byteArrayToIntBigEndian(byteSeq);
            /*if(SystemConfig.mBoolDebugLogAllSeqNo) {
                if(SystemConfig.mEnumStartState == SystemConfig.ENUM_START_STATE.STATE_START) {
                    SystemConfig.mMyEventLogger.appendDebugStr(String.valueOf(mIntCurSeqNum), "");
                }
            }*/

            iLength = (byteArray.length / 2)-1;
            SystemConfig.mIntGainLevel = Utilitys.getGainFrombyteArray(byteGainPower);
            if (mIntGainLevelCommandPre == -1) {
                mIntGainLevelCommandPre = SystemConfig.mIntGainLevel;
            }
            if (mIntGainLevelCommandCur == -1) {
                mIntGainLevelCommandCur = SystemConfig.mIntGainLevel;
            }
            boolGainLevelMsgShow = true;
            if(boolGainLevelMsgShow) {
                //----- show GainLevel -----------------------
                if (!mBoolGainLevelFirstShowed) {
                    //SystemConfig.mMyEventLogger.appendDebugStr("Gain Level Start=", String.valueOf(SystemConfig.mIntGainLevel));
                    mIntPreGainOnLine = SystemConfig.mIntGainLevel;
                    mBoolGainLevelFirstShowed = true;
                } else if ((SystemConfig.mIntGainLevel != mIntPreGainOnLine)
                        && (SystemConfig.mIntSystemSettingFunction == SystemConfig.INT_SYSTEM_SETTING_FUNCTION_0_NORMAL)) {
                    Date dateGain = new Date();
                    //SystemConfig.mMyEventLogger.appendGainLevelEvent(dateGain, MyEventLogger.STR_EVENT_GAIN_LEVEL, SystemConfig.mIntGainLevel);
                    mIntPreGainOnLine = SystemConfig.mIntGainLevel;
                }
                mIntPacketIdxForGainTest++;
            }

            if(SystemConfig.mIntGainControlEnabled == SystemConfig.INT_GAIN_CONTROL_ENABLED_YES) {
                //----- calculate max. value in packet -----
                if (mBoolDCOffsetLearned) {
                    mIntCurPacketDataDiffValueMax = Integer.MIN_VALUE;
                    mIntCurPacketDataDiffValueMin = Integer.MAX_VALUE;
                    for (iVar = 0; iVar < iLength; iVar++) {
                        mByteArray2Bytes[0] = byteArray[iVar * 2];
                        mByteArray2Bytes[1] = byteArray[iVar * 2 + 1];
                        mIntPacketData[iVar] = Utilitys.byteArrayToIntBigEndian(mByteArray2Bytes);
                        if (mIntCurPacketDataDiffValueMax < (mIntPacketData[iVar] - mShortDCOffset)) {
                            mIntCurPacketDataDiffValueMax = mIntPacketData[iVar] - mShortDCOffset;
                        }
                        if ((mIntPacketData[iVar] - mShortDCOffset) >= 0) {
                            if (mIntCurPacketDataDiffValueMin > (mIntPacketData[iVar] - mShortDCOffset)) {
                                mIntCurPacketDataDiffValueMin = mIntPacketData[iVar] - mShortDCOffset;
                            }
                        }
                    }
                    //-----  try adjust Gain ---------
                    if (SystemConfig.mIntGainLevelCommandSetting == SystemConfig.INT_GAIN_CONTROL_LEVEL_CNT) {
                        if (SystemConfig.mEnumTryState == SystemConfig.ENUM_TRY_STATE.STATE_TRY) {
                            tryAdjustGainCommandAuto();
                        }
                    } else {
                        tryAdjustGainCommandFix();
                    }
                }
            }

            if (mEnumUltrasoundOneDataReceiveState == ENUM_RAW_DATA_ONE_DATA_RX_STATE.RECEIVE_STATE_START) {
                /*
                if (SystemConfig.mIntSystemSettingFunction == SystemConfig.INT_SYSTEM_SETTING_FUNCTION_2_POWER_CONSUME_TEST) {
                    if (SystemConfig.mEnumPowerConsumeTestState == SystemConfig.ENUM_POWER_CONSUME_TEST_STATE.TEST_STATE_STOP) {
                        PowerConsumeTest.prepareTest();
                    }
                    PowerConsumeTest.doPowerTest(SystemConfig.mIntPowerLevel);
                    ItriWearUltrasoundFragment.putUiMsg(ItriWearUltrasoundFragment.UI_MSG_ID_SHOW_POWER_LEVEL);
                    return false;
                }
*/
                mStrLostSeqNums = "";
            } else {
                if (mIntPreSeqNum == mIntCurSeqNum) {
                    return false;
                }

                if (mIntNextSeqNum != mIntCurSeqNum) {
                    mBoolRxError = true;
                    mIntLostCount += Math.abs(mIntCurSeqNum - mIntNextSeqNum);

                    iLostSeqNum = mIntNextSeqNum;
                    while (iLostSeqNum != mIntCurSeqNum) {
                        //mStrLostSeqNums = mStrLostSeqNums + " " + String.valueOf(iLostSeqNum);
                        if (mIntNextSeqLostMsgIdx < LOST_SEQ_MSG_MAX_CNT) {
                            mIntLostSeqMsgs[mIntNextSeqLostMsgIdx] = iLostSeqNum;
                            mIntNextSeqLostMsgIdx++;
                        }
                        iLostSeqNum++;
                        if (iLostSeqNum >= RX_MAX_SEQENCE_NUM + 1) {
                            iLostSeqNum = 0;
                        }
                    }
                }
            }
            mIntPreSeqNum = mIntCurSeqNum;
            mIntNextSeqNum = mIntCurSeqNum + 1;
            mIntTotalPacketCount = mIntTotalPacketCount + 1;
            if (mIntNextSeqNum == RX_MAX_SEQENCE_NUM + 1) {
                mIntNextSeqNum = 0;
            }


            //Get Ultrasound sensor data
            for (iVar4 = 0; iVar4 < iNoOfSamplesInOnePacket; iVar4++) {
                for (iVar5 = 0; iVar5 < SystemConfig.mInt1DataBytes; iVar5++) {
                    byteData[iVar5] = mByteCurPacket[iVar4 * SystemConfig.mInt1DataBytes + iVar5];
                }

                /*if (iVar4 == 0) {
                    addUltrasoundNewSampleBySegmentOnLine(byteData, true, false);
                } else if (iVar4 != (iNoOfSamplesInOnePacket - 1)) {
                    addUltrasoundNewSampleBySegmentOnLine(byteData, false, false);
                } else {
                    addUltrasoundNewSampleBySegmentOnLine(byteData, false, true);
                }*/
                addUltrasoundNewSampleBySegmentOnLine(byteData);
                mIntDataDiffBaseline = (mIntDataDiffMaxValue + mIntDataDiffMinValue) / 2;
            }

            //SystemConfig.mAudioPlayerController.putMsgForAudioSegmentOnLine(SystemConfig.mRawDataProcessor.mIntDataNextIndex-1);

            return true;
        }catch(Exception ex1){
            ex1.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugStr("proceUltraDataBySegmentOnLine: Exp","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            return false;
        }
    }

    public void addECGNewSampleBySegmentOnLine(int inputValue){
        mShortEcgData[mIntDataNextIndex/16] = (short)inputValue;
    }

    public void addUltrasoundNewSampleBySegmentOnLine(int inputValue){
        if(SystemConfig.mEnumTryState != SystemConfig.ENUM_TRY_STATE.STATE_TRY) {
            if (mIntTryDataNextIndex == SystemConfig.mIntUltrasoundSamplesMaxSizeForRun) {
                return;
            }
        }
        addUltrasoundNewSampleInt(inputValue);

//        int iByteIndex = mIntDataNextIndex * SystemConfig.mInt1DataBytes;
//
//        if(mIntDataNextIndex < (mByteArrayUltrasoundDataOnLineSave.length / 2)) {
//            for (int iVar2 = 0; iVar2 < SystemConfig.mInt1DataBytes; iVar2++) {
//                mByteArrayUltrasoundDataOnLineSave[iByteIndex + iVar2] = byteArray[iVar2];
//            }
//        }
    }

    public void addUltrasoundNewSampleInt(int inputValue){
        int iByteIndex, iGainLevel, iValue, iVar2;
        // Cavin Test DC OFFSET 20211222
        mIntUltrasoundDataNotDCOffset[mIntDataNextIndex] = inputValue;
        if (Doppler.cavinDCOffset){
            if (mIntDataNextIndex!=0){
                iValue = (int)(inputValue - mIntUltrasoundDataNotDCOffset[mIntDataNextIndex-1]+0.999*(double)mShortUltrasoundData[mIntDataNextIndex-1]);
            }else{
                iValue = inputValue;
            }
        }else{
            //Leslie modified - cancel gain for rawData
            iValue = (inputValue - mShortDCOffset);// * mIntBasicGainForSound;
            //            iValue = shortValueNotDCOffset;
        }
        // Cavin Test DC OFFSET 20211222 end
        mShortUltrasoundData[mIntDataNextIndex] = (short) iValue;
        mIntUltrasoundDataGainLevel[mIntDataNextIndex] = SystemConfig.mIntGainLevel;
        if(SystemConfig.mIntFilterDataEnabled == SystemConfig.INT_FILTER_ENABLED_YES) {
            mShortUltrasoundDataBeforeFilter[mIntDataNextIndex] = (short) iValue;
            mShortUltrasoundData[mIntDataNextIndex] = (short) MainActivity.mBVSignalProcessorPart1.butterworthUSHigh.filter(mShortUltrasoundData[mIntDataNextIndex]);
//            mMyDataFilter.filterProcessForData(mIntDataNextIndex);
        }

//        iByteIndex = mIntDataNextIndex * SystemConfig.mInt1DataBytes;

//        if(mIntDataNextIndex < (mByteArrayUltrasoundDataOnLineSave.length / 2)) {
//            for (iVar2 = 0; iVar2 < SystemConfig.mInt1DataBytes; iVar2++) {
//                mByteArrayUltrasoundDataOnLineSave[iByteIndex + iVar2] = byteArray[iVar2];
//            }
//        }

        if(SystemConfig.mEnumStartState == SystemConfig.ENUM_START_STATE.STATE_START) {
            if (mIntDataNextIndex < SystemConfig.mIntUltrasoundSamplesMaxSizeForRun) {
                if (mIntDataMaxValue < inputValue) {
                    mIntDataMaxValue = inputValue;
                    mIntDataMaxIdx = mIntDataNextIndex;
                }
                if (mIntDataNextIndex == SystemConfig.mIntUltrasoundSamplesMaxSizeForRun-1) {
                    if(!mBoolDebugDataEnd) {
                        //SystemConfig.mMyEventLogger.appendDebugStr("mIntDataMax/Idx = ", String.valueOf(mIntDataMaxValue) + "/" + String.valueOf(mIntDataNextIndex));
                        mBoolDebugDataEnd = true;
                    }
                }
            }
        }

        if(SystemConfig.mEnumTryState == SystemConfig.ENUM_TRY_STATE.STATE_TRY) {
            if (mEnumUltrasoundOneDataReceiveState == ENUM_RAW_DATA_ONE_DATA_RX_STATE.RECEIVE_STATE_START) {
                mEnumUltrasoundOneDataReceiveState = ENUM_RAW_DATA_ONE_DATA_RX_STATE.RECEIVE_STATE_CONTINUOUS;
            }
        }else if(SystemConfig.mEnumStartState == SystemConfig.ENUM_START_STATE.STATE_START) {
            if (mEnumUltrasoundOneDataReceiveState == ENUM_RAW_DATA_ONE_DATA_RX_STATE.RECEIVE_STATE_START) {
                mEnumUltrasoundOneDataReceiveState = ENUM_RAW_DATA_ONE_DATA_RX_STATE.RECEIVE_STATE_CONTINUOUS;
            } else if (mEnumUltrasoundOneDataReceiveState == ENUM_RAW_DATA_ONE_DATA_RX_STATE.RECEIVE_STATE_CONTINUOUS) {
                if (mIntDataNextIndex == SystemConfig.mIntUltrasoundSamplesMaxSizeForRun - 1) {
                    if (!SystemConfig.isHeartIO2) {
                        storeByteDataToWavFile();
                    }
                    mEnumUltrasoundOneDataReceiveState = ENUM_RAW_DATA_ONE_DATA_RX_STATE.RECEIVE_STATE_END;
                    //SystemConfig.mMyEventLogger.appendDebugStr("storeByteDataToWavFile", "");
                }
            }
        }

        if(SystemConfig.mEnumStartState == SystemConfig.ENUM_START_STATE.STATE_START) {
            if (mIntDataNextIndex < SystemConfig.mIntUltrasoundSamplesMaxSizeForRun-1) {
                mIntDataNextIndex++;
            }
        }else{
            mIntTryDataNextIndex++;
            mIntDataNextIndex++;

            if (mIntDataNextIndex == SystemConfig.mIntUltrasoundSamplesMaxSizeForRun) {
                mIntDataNextIndex=0;
            }else if(mIntDataNextIndex % classificationIntervalPts == 0){
                 classificationUSPA();
            }
        }
    }
    //public void addUltrasoundNewSampleBySegmentOnLine(byte[] byteArray, boolean boolSegmentStart, boolean boolSegmentEnd) {
    public void addUltrasoundNewSampleBySegmentOnLine(byte[] byteArray) {
        int iByteIndex, iGainLevel, iValue, iVar2;
        short shortValueNotDCOffset;

        if(SystemConfig.mEnumTryState != SystemConfig.ENUM_TRY_STATE.STATE_TRY) {
            if (mIntTryDataNextIndex == SystemConfig.mIntUltrasoundSamplesMaxSizeForRun) {
                return;
            }
        }

        try {
            if (!mBoolDCOffsetLearned){
                checkDCOffsetAndGainThresholdAndCorrectDataForItriDevice(mIntDataNextIndex, byteArray, false);
            }
            //iVar1 = byteArrayToIntBigEndian(byteArray);
            shortValueNotDCOffset = (short)Utilitys.byteArrayToIntBigEndian(byteArray);
            addUltrasoundNewSampleInt(shortValueNotDCOffset);
//            iValue = (shortValueNotDCOffset - mShortDCOffset) * mIntBasicGainForSound;
//            iValue = shortValueNotDCOffset;
            mIntUltrasoundDataNotDCOffset[mIntDataNextIndex] = shortValueNotDCOffset;
            /*
            mShortUltrasoundData[mIntDataNextIndex] = (short) iValue;
            mIntUltrasoundDataGainLevel[mIntDataNextIndex] = SystemConfig.mIntGainLevel;
            if(SystemConfig.mIntFilterDataEnabled == SystemConfig.INT_FILTER_ENABLED_YES) {
                mShortUltrasoundDataBeforeFilter[mIntDataNextIndex] = (short) iValue;
                mMyDataFilter.filterProcessForData(mIntDataNextIndex);
            }
*/
            iByteIndex = mIntDataNextIndex * SystemConfig.mInt1DataBytes;

            if(mIntDataNextIndex < (mByteArrayUltrasoundDataOnLineSave.length / 2)) {
                for (iVar2 = 0; iVar2 < SystemConfig.mInt1DataBytes; iVar2++) {
                    mByteArrayUltrasoundDataOnLineSave[iByteIndex + iVar2] = byteArray[iVar2];
                }
            }

//            if(SystemConfig.mEnumStartState == SystemConfig.ENUM_START_STATE.STATE_START) {
//                if (mIntDataNextIndex < SystemConfig.mIntUltrasoundSamplesMaxSizeForRun) {
//                    if (mIntDataMaxValue < shortValueNotDCOffset) {
//                        mIntDataMaxValue = shortValueNotDCOffset;
//                        mIntDataMaxIdx = mIntDataNextIndex;
//                    }
//                    if (mIntDataNextIndex == SystemConfig.mIntUltrasoundSamplesMaxSizeForRun-1) {
//                        if(!mBoolDebugDataEnd) {
//                            //SystemConfig.mMyEventLogger.appendDebugStr("mIntDataMax/Idx = ", String.valueOf(mIntDataMaxValue) + "/" + String.valueOf(mIntDataNextIndex));
//                            mBoolDebugDataEnd = true;
//                        }
//                    }
//                }
//            }
//
//            if(SystemConfig.mEnumTryState == SystemConfig.ENUM_TRY_STATE.STATE_TRY) {
//                if (mEnumUltrasoundOneDataReceiveState == ENUM_RAW_DATA_ONE_DATA_RX_STATE.RECEIVE_STATE_START) {
//                    mEnumUltrasoundOneDataReceiveState = ENUM_RAW_DATA_ONE_DATA_RX_STATE.RECEIVE_STATE_CONTINUOUS;
//                }
//            }else if(SystemConfig.mEnumStartState == SystemConfig.ENUM_START_STATE.STATE_START) {
//                if (mEnumUltrasoundOneDataReceiveState == ENUM_RAW_DATA_ONE_DATA_RX_STATE.RECEIVE_STATE_START) {
//                    mEnumUltrasoundOneDataReceiveState = ENUM_RAW_DATA_ONE_DATA_RX_STATE.RECEIVE_STATE_CONTINUOUS;
//                } else if (mEnumUltrasoundOneDataReceiveState == ENUM_RAW_DATA_ONE_DATA_RX_STATE.RECEIVE_STATE_CONTINUOUS) {
//                    if (mIntDataNextIndex == SystemConfig.mIntUltrasoundSamplesMaxSizeForRun - 1) {
//                        storeByteDataToWavFile();
//                        mEnumUltrasoundOneDataReceiveState = ENUM_RAW_DATA_ONE_DATA_RX_STATE.RECEIVE_STATE_END;
//                        //SystemConfig.mMyEventLogger.appendDebugStr("storeByteDataToWavFile", "");
//                    }
//                }
//            }
//
//            if(SystemConfig.mEnumStartState == SystemConfig.ENUM_START_STATE.STATE_START) {
//                if (mIntDataNextIndex < SystemConfig.mIntUltrasoundSamplesMaxSizeForRun-1) {
//                    mIntDataNextIndex++;
//                }
//            }else{
//                mIntTryDataNextIndex++;
//                mIntDataNextIndex++;
//                if (mIntDataNextIndex == SystemConfig.mIntUltrasoundSamplesMaxSizeForRun) {
//                    mIntDataNextIndex=0;
//                }
//            }

        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("addUltrasoundNewSample.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            ex1.printStackTrace();
        }
    }

    public void storeSTFTDataToFile()
    {
        String strData, strFile, strErr;
        int iVar1, iVar2, iTotalCnt;
        File file;
        boolean boolVar;
        FileWriter fw;
        BufferedWriter bw;
        StringBuilder stringBuilder;
        String string;

        strFile=getSTFTFileName();
        file= new File(strFile);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        try {
            fw = new FileWriter(strFile, true);
        }catch(Exception ex1){
            return;
        }

        bw = new BufferedWriter(fw);
        PrintWriter out = new PrintWriter(bw);

        try {
            stringBuilder = new StringBuilder();
            iTotalCnt = MainActivity.mBVSignalProcessorPart1.mIntSTFFTNextSubSegIdx;
            for (iVar1 = 0; iVar1 < iTotalCnt; iVar1++) {
                stringBuilder.setLength(0);
                for (iVar2 = 0; iVar2 < MainActivity.mBVSignalProcessorPart1.mIntTotalFreqSeqsCnt; iVar2++){
                    stringBuilder.append(MainActivity.mBVSignalProcessorPart1.mDoubleBVSpectrumValues[iVar1][iVar2] + "");
                    if (iVar2 < MainActivity.mBVSignalProcessorPart1.mIntTotalFreqSeqsCnt - 1) {
                        stringBuilder.append(",");
                    }
                }
                //stringBuilder.append("\n");
                string = stringBuilder.toString();
                out.println(string);
            }
        }catch (Exception ex){
            //SystemConfig.mMyEventLogger.appendDebugStr("RawDataProc.storeSTFTData.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex.toString(),"");
        }
        finally {
            try {
                if (out != null)
                    out.close();
            } catch (Exception e) {
                //exception handling left as an exercise for the reader
            }
            try {
                if (bw != null)
                    bw.close();
            } catch (IOException e) {
                //exception handling left as an exercise for the reader
            }
            try {
                if (fw != null)
                    fw.close();
            } catch (IOException e) {
                //exception handling left as an exercise for the reader
            }
        }
    }

    public int createDataFile(Context ctx_handle){

        mStrCurFileName=getNewTxtFileName();

        String strFile=mStrBaseFolder + File.separator + mStrCurFileName;

        //Calendar calendar = Calendar.getInstance();
        //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        //String fileName = dateFormat.format(calendar.getTime());
        //File data_file = new File(ctx_handle.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName + ".txt");
        File data_file = new File(strFile);
        Log.i(TAG, data_file.getAbsolutePath());

//        if (!data_file.mkdirs()) {
//            Log.e(ITRI_TAG, "Directory not created");
//        }

        File dirPath = new File(ctx_handle.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath());
        Log.i(TAG, dirPath.getAbsolutePath());
        if (!dirPath.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }

        dirPath = new File(mStrBaseFolder);
        if (!dirPath.exists()){
            dirPath.mkdirs();
        }

        if (data_file.exists()) {
            data_file.delete();
        }

        try {
            data_os = new FileOutputStream(data_file);

            Log.i(TAG, "FileOutputStream create successful!!");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int closeDataFile(){
        if (null != data_os) {
            try {
                Log.i(TAG, "close file output stream !");
                data_os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public int writeDataVal(int[] newValue){
        String spaceStr = " ";
        char retChar = '\n';

        if (null != data_os) {
            try {
                for(int count = 0; count < newValue.length; count++) {
                    //.	data_os.write(Integer.toString(newValue[count]).getBytes());

                    data_os.write(Integer.toHexString(newValue[count]).getBytes());
                    data_os.write(spaceStr.getBytes());
                    //.	data_os.write(aaa);
                }
                data_os.write(retChar);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }


    public void storeByteDataToWavFile()
    {
        try{
            mStrCurFileName=getNewWavFileName();

            String strFile=mStrBaseFolder + File.separator + mStrCurFileName;
            //SystemConfig.mMyEventLogger.appendDebugStr("File =" +  strFile);
            Log.d(TAG,"strFile="+strFile);

            File file= new File(strFile);
            Objects.requireNonNull(file.getParentFile()).mkdirs();

            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            setWavFileHeaderByteArray();
            fileOutputStream.write(mByteArrayWavHeader, 0, 44);
            //fileOutputStream.write(mByteArrayUltrasoundDataOnLineSave, 0,  SystemConfig.mIntUltrasoundSamplesMaxSizeForRun * SystemConfig.mInt1DataBytes);

            /* length = 224000 (8K*14s) */
            int length = SystemConfig.mIntUltrasoundSamplesMaxSizeForRun * SystemConfig.mInt1DataBytes;
            byte[] temp = new byte[length];
//            Log.d("length: ", String.valueOf(length));
            for(int i = 0 ; i < length/2 ; i++){ //分兩個byte來存
                temp[i * 2] = (byte)(mShortUltrasoundDataBeforeFilter[i] & 0xFF);
                temp[i * 2 + 1] = (byte)((mShortUltrasoundDataBeforeFilter[i] >> 8) & 0xFF);
            }
            fileOutputStream.write(temp);

            //SystemConfig.mMyEventLogger.appendDebugIntEvent("StoreData Sample=", SystemConfig.mIntUltrasoundSamplesMaxSizeForRun,0,0,0,0);
            fileOutputStream.close();

        }catch (Exception ex){
            ex.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugStr("storeByteDataToWaveFile.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex.toString(),"");
        }
    }

    /* 將原始raw data儲存下來 2023/02/04 by Doris */
    public void storeByteToRawData16K(){
        try{
            String exportDirPath = "/storage/emulated/0/Android/data/com.gis.heartio/files/Documents/Doris";
            SimpleDateFormat df;
            df = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String strDate = df.format(new Date());
            String filename = exportDirPath + "/raw_" + strDate + ".raw";

            File file= new File(filename);
//            Objects.requireNonNull(file.getParentFile()).mkdirs();

            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
//            setWavFileHeaderByteArray();
//            fileOutputStream.write(mByteArrayWavHeader, 0, 44);

            /* length = 448000 */
            int length = 448000;
            byte[] temp = new byte[length];
            Log.d("length: ", String.valueOf(temp.length));
            for(int i = 0 ; i < length/4 ; i++){ //分兩個byte來存
                temp[i * 4] = (byte)(mShortUltrasoundDataBeforeFilter[i] & 0xFF);
                temp[i * 4 + 2] = (byte)(mShortUltrasoundDataBeforeFilter[i] & 0xFF);
                temp[i * 4 + 1] = (byte)((mShortUltrasoundDataBeforeFilter[i] >> 8) & 0xFF);
                temp[i * 4 + 3] = (byte)((mShortUltrasoundDataBeforeFilter[i] >> 8) & 0xFF);
            }
            fileOutputStream.write(temp);
            fileOutputStream.close();

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }



     public void setTimeScaleMiniSecsFromDateStr(String strDate){
        Calendar calendarVar;
        String strYear, strMonth, strDay, strHour, strMin, strSec, strMiniSec;
        int iNum;

        try {
            strYear = strDate.substring(0, 4);
            strMonth = strDate.substring(4, 6);
            strDay = strDate.substring(6, 8);
            strHour = strDate.substring(9, 11);
            strMin = strDate.substring(11, 13);
            strSec = strDate.substring(13, 15);
            strMiniSec = strDate.substring(15);
            calendarVar = Calendar.getInstance();
            iNum = Integer.parseInt(strYear);
            calendarVar.set(Calendar.YEAR, iNum);
            iNum = Integer.parseInt(strMonth)-1;
            calendarVar.set(Calendar.MONTH, iNum);
            iNum = Integer.parseInt(strDay);
            calendarVar.set(Calendar.DAY_OF_MONTH, iNum);
            iNum = Integer.parseInt(strHour);
            calendarVar.set(Calendar.HOUR_OF_DAY, iNum);
            iNum = Integer.parseInt(strMin);
            calendarVar.set(Calendar.MINUTE, iNum);
            iNum = Integer.parseInt(strSec);
            calendarVar.set(Calendar.SECOND, iNum);
            iNum = Integer.parseInt(strMiniSec);
            calendarVar.set(Calendar.MILLISECOND, iNum);

            /*SystemConfig.mLongOpenWavFileDateMiniSecs = calendarVar.getTimeInMillis();*/
        }catch(Exception ex1){
            calendarVar = Calendar.getInstance();
            calendarVar.set(Calendar.YEAR, 1911);
            /*SystemConfig.mLongOpenWavFileDateMiniSecs = calendarVar.getTimeInMillis();*/
            //SystemConfig.mMyEventLogger.appendDebugStr("setTimeScaleMiniSecsFrom.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }


    private String getNewWavFileName(){
        String strFileName;
        String strFileTail;
        String strFileTail2;
        String strChar;
        String strFileFormat;
        String strDate;
        SimpleDateFormat df;
        int iVar;

        try {
            Calendar calendarVar = Calendar.getInstance();
            //df = new SimpleDateFormat("yyyyMMdd_HHmmssSSS");
            df = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
            strDate = df.format((calendarVar.getTime()));

            strFileTail = mStrFileTail;
            strFileTail2 = "";
            if (strFileTail.length() > 0) {
                strFileTail2 = "_";
                for (iVar = 0; iVar < strFileTail.length(); iVar++) {
                    strChar = strFileTail.substring(iVar, iVar + 1);
                    if (!strChar.equals(" ")) {
                        strFileTail2 = strFileTail2 + strChar;
                    }
                }
            }

            strFileFormat = "C";
            strFileName = "UD" + strFileFormat + strDate + "_";
            Log.d(TAG,"mBoolRxError="+mBoolRxError);
            if (mBoolRxError) {
                strFileName = strFileName + "err"+mIntLostCount + "_";
            }
            MainActivity.mDoublePER = (double) mIntLostCount / (double) (mIntTotalPacketCount + mIntLostCount);
            double perRate = MainActivity.mDoublePER * 100;
            Log.d(TAG,"Total packet = "+ mIntTotalPacketCount + ", PER = "+ perRate + "%");

            strFileName = strFileName
                 //   + UserManagerCommon.mUserInfoCur.mStrUserIDNumber    // Skip user info now.
                    + strFileTail2 + ".wav";

            return strFileName;
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("RawDataProc.getNewWaveFileName.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            ex1.printStackTrace();
            return "FileNameError";
        }
    }

    private String getNewTxtFileName(){
        String strFileName;
        String strFileTail;
        String strFileTail2;
        String strChar;
        String strFileFormat;
        String strDate;
        SimpleDateFormat df;
        int iVar;

        try {
            Calendar calendarVar = Calendar.getInstance();
            df = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
            strDate = df.format((calendarVar.getTime()));

            strFileTail = mStrFileTail;
            strFileTail2 = "";
            if (strFileTail.length() > 0) {
                strFileTail2 = "_";
                for (iVar = 0; iVar < strFileTail.length(); iVar++) {
                    strChar = strFileTail.substring(iVar, iVar + 1);
                    if (!strChar.equals(" ")) {
                        strFileTail2 = strFileTail2 + strChar;
                    }
                }
            }

            strFileFormat = "C";
            strFileName = "UD" + strFileFormat + strDate + "_";
            Log.d(TAG,"mBoolRxError="+mBoolRxError);
            if (mBoolRxError) {
                strFileName = strFileName + "err"+mIntLostCount + "_";
            }
            MainActivity.mDoublePER = (double) mIntLostCount / (double) (mIntTotalPacketCount + mIntLostCount);
            double perRate = MainActivity.mDoublePER * 100;
            Log.d(TAG,"Total packet = "+ mIntTotalPacketCount + ", PER = "+ perRate + "%");

            strFileName = strFileName
                    //   + UserManagerCommon.mUserInfoCur.mStrUserIDNumber    // Skip user info now.
                    + strFileTail2 + ".txt";

            return strFileName;
        }catch(Exception ex1){
            ex1.printStackTrace();
            return "FileNameError";
        }
    }


    private void setWavFileHeaderByteArray(){
        int iVar;
        byte[] buffer4Byte;

        try{
            buffer4Byte=new byte[4];

            mByteArrayWavHeader[0]= 0x52;
            mByteArrayWavHeader[1]= 0x49;
            mByteArrayWavHeader[2]= 0x46;
            mByteArrayWavHeader[3]= 0x46;

            iVar = 44 -8 + mIntDataNextIndex * SystemConfig.mInt1DataBytes;
            Utilitys.intToByteArrayBigEndian(iVar , buffer4Byte);
            mByteArrayWavHeader[4] = buffer4Byte[0];
            mByteArrayWavHeader[5] = buffer4Byte[1];
            mByteArrayWavHeader[6] = buffer4Byte[2];
            mByteArrayWavHeader[7] = buffer4Byte[3];

            mByteArrayWavHeader[8]= 0x57;
            mByteArrayWavHeader[9]= 0x41;
            mByteArrayWavHeader[10]= 0x56;
            mByteArrayWavHeader[11]= 0x45;

            mByteArrayWavHeader[12]= 0x66;
            mByteArrayWavHeader[13]= 0x6d;
            mByteArrayWavHeader[14]= 0x74;
            mByteArrayWavHeader[15]= 0x20;

            mByteArrayWavHeader[16]= 0x10;
            mByteArrayWavHeader[17]= 0x0;
            mByteArrayWavHeader[18]= 0x0;
            mByteArrayWavHeader[19]= 0x0;

            mByteArrayWavHeader[20]= 0x01;
            mByteArrayWavHeader[21]= 0x0;

            mByteArrayWavHeader[22]= 0x01;
            mByteArrayWavHeader[23]= 0x00;

            iVar = SystemConfig.mIntUltrasoundSamplerate;
            Utilitys.intToByteArrayBigEndian(iVar , buffer4Byte);
            mByteArrayWavHeader[24] = buffer4Byte[0];
            mByteArrayWavHeader[25] = buffer4Byte[1];
            mByteArrayWavHeader[26] = buffer4Byte[2];
            mByteArrayWavHeader[27] = buffer4Byte[3];

            iVar = SystemConfig.mIntUltrasoundSamplerate * SystemConfig.mInt1DataBytes;
            Utilitys.intToByteArrayBigEndian(iVar , buffer4Byte);
            mByteArrayWavHeader[28] = buffer4Byte[0];
            mByteArrayWavHeader[29] = buffer4Byte[1];
            mByteArrayWavHeader[30] = buffer4Byte[2];
            mByteArrayWavHeader[31] = buffer4Byte[3];

            iVar = SystemConfig.mInt1DataBytes;
            Utilitys.intToByteArrayBigEndian(iVar , buffer4Byte);
            mByteArrayWavHeader[32] = buffer4Byte[0];
            mByteArrayWavHeader[33] = buffer4Byte[1];

            iVar = SystemConfig.mInt1DataBytes * 8;
            Utilitys.intToByteArrayBigEndian(iVar , buffer4Byte);
            mByteArrayWavHeader[34] = buffer4Byte[0];
            mByteArrayWavHeader[35] = buffer4Byte[1];

            mByteArrayWavHeader[36]= 0x64;
            mByteArrayWavHeader[37]= 0x61;
            mByteArrayWavHeader[38]= 0x74;
            mByteArrayWavHeader[39]= 0x61;

            //iVar = SystemConfig.ULTRASOUND_DATA_DEFAULT_SIZE * SystemConfig.mIntUltrasound1DataBytes;
            //iVar = SystemConfig.ULTRASOUND_DATA_MAX_SIZS_ITRI_8K;
            iVar =  SystemConfig.mIntUltrasoundSamplesMaxSizeForRun *2;
            Utilitys.intToByteArrayBigEndian(iVar , buffer4Byte);
            mByteArrayWavHeader[40] = buffer4Byte[0];
            mByteArrayWavHeader[41] = buffer4Byte[1];
            mByteArrayWavHeader[42] = buffer4Byte[2];
            mByteArrayWavHeader[43] = buffer4Byte[3];
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("setWaveFileHeaderByte.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }


    public int getUltrasoundCurrSamplesSize(){
        if(SystemConfig.mEnumTryState != SystemConfig.ENUM_TRY_STATE.STATE_TRY) {
            return mIntDataNextIndex;
        }else{
            return mIntTryDataNextIndex;
        }
    }

    public void setUltrasoundCurrSamplesSize(int iVar){
        mIntDataNextIndex = iVar;
    }

    public int getUltrasoundCurrSamplesSizeOffLine(){
        return mIntDataCurSizeOffLine;
    }

    public boolean isFileWaveType(String strFileName) {
        String strFileTail;
        int iLength;

        iLength = strFileName.length();
        strFileTail = strFileName.substring(iLength-3, iLength);
        return strFileTail.equals("wav");
    }

    public boolean isFileTxtType(String strFileName) {
        String strFileTail;
        int iLength;

        iLength = strFileName.length();
        strFileTail = strFileName.substring(iLength-3, iLength);
        return strFileTail.toLowerCase().equals("txt");
    }

    public SystemConfig.ENUM_DEVICE_TYPE checkDeviceType(String strFileName) {
        String strFileFirst;
        int iLength;

        try {
            //iLength = strFileName.length();
            strFileFirst = strFileName.substring(0, 7);
            if (strFileFirst.equals("USCOM8K")) {
                return SystemConfig.ENUM_DEVICE_TYPE.USCOM_8K;
            }

            strFileFirst = strFileName.substring(0, 6);
            if (strFileFirst.equals("ITRI8K")) {
                return SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_OFFLINE;
            }

            strFileFirst = strFileName.substring(0, 3);
            if (strFileFirst.equals("UD2")) {
                return SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_LE_ONLINE;
            } else if (strFileFirst.equals("UDB") || strFileFirst.equals("UDC")) {
                return SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_BE_ONLINE;
            } else if (strFileFirst.equals("USC")) {
                return SystemConfig.ENUM_DEVICE_TYPE.USCOM_44K;
            } else {
                return SystemConfig.ENUM_DEVICE_TYPE.ITRI_44K;
            }
        } catch (Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("RawDataProc.checkDeviceType.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            return SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_BE_ONLINE;
        }
    }

    public boolean setDataFromTxtFile(String strFile){
        boolean ret = false;
        File file= new File(strFile);
        try {
            //mByteArrayWavDataOffLine = new byte[ SystemConfig.mIntUltrasoundSamplesMaxSizeForRun * iBlockAlign / iNumChannels];
            SystemConfig.mIntUltrasoundSamplesMaxSizeForRun = SystemConfig.mIntUltrasoundSamplesMaxSize;
            mByteArrayWavDataOffLine = new byte[224000];    // 112000*2/1

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            int iVar = 0;
            while ((line = br.readLine())!=null){

                String[] split = line.split("\\s+");
//                for (int i = 0; i < split.length; i++){
//                    Log.d(TAG,"split["+i+"]= "+ split[i]);
//                }
                mShortEcgData[iVar / 16] = Short.parseShort(split[4],16);
                for (int i = 5; i< split.length;i++){
                    mShortUltrasoundData[iVar] = (short)Short.parseShort(split[i],16);

                    if ( (iVar*2+1)< mByteArrayWavDataOffLine.length){
                        mByteArrayWavDataOffLine[iVar*2] = (byte)(mShortUltrasoundData[iVar] & 0xff);
                        mByteArrayWavDataOffLine[iVar*2+1] = (byte)(mShortUltrasoundData[iVar] >> 8 & 0xff);
                    }

                    if(SystemConfig.mIntFilterDataEnabled == SystemConfig.INT_FILTER_ENABLED_YES) {
                        mShortUltrasoundDataBeforeFilter[iVar] = mShortUltrasoundData[iVar];
                        //cavin test
//                        mShortUltrasoundData[iVar] = (short) MainActivity.mBVSignalProcessorPart1.chebyshevUSHigh.filter(mShortUltrasoundData[iVar]);
//                    mShortUltrasoundData[iVar] = (short) MainActivity.mBVSignalProcessorPart1.butterworthUSBS.filter(mShortUltrasoundData[iVar]);
                        mShortUltrasoundData[iVar] = (short) MainActivity.mBVSignalProcessorPart1.butterworthUSHigh.filter(mShortUltrasoundData[iVar]);
//                        mMyDataFilter.filterProcessForData(iVar);
//                        putByteDataAfterFilter(iVar);
                    }

                    iVar++;
                    if (iVar >= SystemConfig.mIntUltrasoundSamplesMaxSizeForRun){
                        break;
                    }
                }
//                    if (mIntDataMaxValue < shortValueNotDCOffset) {
//                        mIntDataMaxValue = shortValueNotDCOffset;
//                        mIntDataMaxIdx = iVar;
//                    }

//                }
                mIntDataNextIndex= SystemConfig.mIntUltrasoundSamplesMaxSizeForRun;
                mIntDataCurSizeOffLine = mIntDataNextIndex;

            }
            ret = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }


    public boolean setDataFromWavFile(String strFile){
        int sizeFileBytes;
        byte[] byteData1, byteData2;
        int iVar, iVar2 , iData;

        byte[] buffer2Byte, buffer4Byte;
        byte[] bufferHeader;
        int  iBitsPerSample ,  iSampleRate , iFileSize,  iDataMaxSiz;
        int iChunkSize;
        int iSubChunkSize1, iAudioFormat, iNumChannels , iByteRate, iBlockAlign ;
        int iSubChunkSize2;
        String strChunkDesc, strWave , strFmt , strData;
        int iLength1, iLength2;
        int iUltrasoundSamplesMaxSize;
        short shortValueNotDCOffset;
        int iDCAccu, iDCVal;

        try {

            File file= new File(strFile);
            sizeFileBytes = (int) file.length();

            buffer2Byte=new byte[2];
            buffer4Byte=new byte[4];

            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(mByteArrayWavHeader, 0, 44);

            buffer4Byte[0]=mByteArrayWavHeader[0];
            buffer4Byte[1]=mByteArrayWavHeader[1];
            buffer4Byte[2]=mByteArrayWavHeader[2];
            buffer4Byte[3]=mByteArrayWavHeader[3];
            strChunkDesc = new String(buffer4Byte);
            if (!strChunkDesc.equals("RIFF")){
                return false;
            }

            buffer4Byte[0]=mByteArrayWavHeader[4];
            buffer4Byte[1]=mByteArrayWavHeader[5];
            buffer4Byte[2]=mByteArrayWavHeader[6];
            buffer4Byte[3]=mByteArrayWavHeader[7];
            //iChunkSize=RawDataProcessor.byteArrayToIntForWAVParamBigEndian(buffer4Byte);
            iChunkSize=Utilitys.byteArrayToIntForWAVDataBigEndian(buffer4Byte);

            buffer4Byte[0]=mByteArrayWavHeader[8];
            buffer4Byte[1]=mByteArrayWavHeader[9];
            buffer4Byte[2]=mByteArrayWavHeader[10];
            buffer4Byte[3]=mByteArrayWavHeader[11];
            strWave =new String(buffer4Byte);
            if (!strWave.equals("WAVE")){
                return false;
            }

            buffer4Byte[0]=mByteArrayWavHeader[12];
            buffer4Byte[1]=mByteArrayWavHeader[13];
            buffer4Byte[2]=mByteArrayWavHeader[14];
            buffer4Byte[3]=mByteArrayWavHeader[15];
            strFmt =new String(buffer4Byte);
            if (!strFmt.equals("fmt ")){
                return false;
            }

            buffer4Byte[0]=mByteArrayWavHeader[16];
            buffer4Byte[1]=mByteArrayWavHeader[17];
            buffer4Byte[2]=mByteArrayWavHeader[18];
            buffer4Byte[3]=mByteArrayWavHeader[19];
            //iSubChunkSize1=RawDataProcessor.byteArrayToIntForWAVParamBigEndian(buffer4Byte);
            iSubChunkSize1=Utilitys.byteArrayToIntForWAVDataBigEndian(buffer4Byte);
            if ( iSubChunkSize1 != 16){
                return false;
            }

            buffer2Byte[0]=mByteArrayWavHeader[20];
            buffer2Byte[1]=mByteArrayWavHeader[21];
            //iAudioFormat=RawDataProcessor.byteArrayToIntForWAVParamBigEndian(buffer2Byte);
            iAudioFormat=Utilitys.byteArrayToIntForWAVDataBigEndian(buffer2Byte);
            if ( iAudioFormat != 1){
                return false;
            }

            buffer2Byte[0]=mByteArrayWavHeader[22];
            buffer2Byte[1]=mByteArrayWavHeader[23];
            //iNumChannels=RawDataProcessor.byteArrayToIntForWAVParamBigEndian(buffer2Byte);
            iNumChannels=Utilitys.byteArrayToIntForWAVDataBigEndian(buffer2Byte);
//            Log.d(TAG, "iNumChannels="+iNumChannels);
            if ( iNumChannels > 2){
                return false;
            }

            buffer4Byte[0]=mByteArrayWavHeader[24];
            buffer4Byte[1]=mByteArrayWavHeader[25];
            buffer4Byte[2]=mByteArrayWavHeader[26];
            buffer4Byte[3]=mByteArrayWavHeader[27];
            //iSampleRate=RawDataProcessor.byteArrayToIntForWAVParamBigEndian(buffer4Byte);
            iSampleRate=Utilitys.byteArrayToIntForWAVDataBigEndian(buffer4Byte);
            SystemConfig.mIntUltrasoundSamplerate =  iSampleRate;

            buffer4Byte[0]=mByteArrayWavHeader[28];
            buffer4Byte[1]=mByteArrayWavHeader[29];
            buffer4Byte[2]=mByteArrayWavHeader[30];
            buffer4Byte[3]=mByteArrayWavHeader[31];
            //iByteRate=RawDataProcessor.byteArrayToIntForWAVParamBigEndian(buffer4Byte);
            iByteRate=Utilitys.byteArrayToIntForWAVDataBigEndian(buffer4Byte);

            buffer2Byte[0]=mByteArrayWavHeader[32];
            buffer2Byte[1]=mByteArrayWavHeader[33];
            //iBlockAlign=RawDataProcessor.byteArrayToIntForWAVParamBigEndian(buffer2Byte);
            iBlockAlign=Utilitys.byteArrayToIntForWAVDataBigEndian(buffer2Byte);
//            Log.d("offline", "iBlockAlign="+iBlockAlign);

            buffer2Byte[0]=mByteArrayWavHeader[34];
            buffer2Byte[1]=mByteArrayWavHeader[35];
            //iBitsPerSample=RawDataProcessor.byteArrayToIntForWAVParamBigEndian(buffer2Byte);
            iBitsPerSample=Utilitys.byteArrayToIntForWAVDataBigEndian(buffer2Byte);
            if (iBitsPerSample == 8){
                byteData1= new byte[1];
                byteData2= new byte[1];
                SystemConfig.mInt1DataBytes =1;
            }else if (iBitsPerSample == 16){
                byteData1= new byte[2];
                byteData2= new byte[2];
                SystemConfig.mInt1DataBytes =2;
            }else{
                return false;
            }

            buffer4Byte[0]=mByteArrayWavHeader[36];
            buffer4Byte[1]=mByteArrayWavHeader[37];
            buffer4Byte[2]=mByteArrayWavHeader[38];
            buffer4Byte[3]=mByteArrayWavHeader[39];
            strData =new String(buffer4Byte);
            if (!strData.equals("data")){
                return false;
            }

            buffer4Byte[0]=mByteArrayWavHeader[40];
            buffer4Byte[1]=mByteArrayWavHeader[41];
            buffer4Byte[2]=mByteArrayWavHeader[42];
            buffer4Byte[3]=mByteArrayWavHeader[43];
            //iSubChunkSize2=RawDataProcessor.byteArrayToIntForWAVParamBigEndian(buffer4Byte);
            iSubChunkSize2=Utilitys.byteArrayToIntForWAVDataBigEndian(buffer4Byte);
            //Log.d("offline","iSubChunkSize2 = "+iSubChunkSize2);

            SystemConfig.mIntUltrasoundSamplesMaxSizeForRun = SystemConfig.mIntUltrasoundSamplesMaxSize;
            iUltrasoundSamplesMaxSize =  ((iSubChunkSize2*8) / iBitsPerSample)/iNumChannels;
            if (iUltrasoundSamplesMaxSize < SystemConfig.mIntUltrasoundSamplesMaxSizeForRun){
                SystemConfig.mIntUltrasoundSamplesMaxSizeForRun =  iUltrasoundSamplesMaxSize;
            }

            mByteArrayWavDataOffLine = new byte[ SystemConfig.mIntUltrasoundSamplesMaxSizeForRun * iBlockAlign / iNumChannels];
            iLength1 = mByteArrayWavDataOffLine.length;
            iLength2 = sizeFileBytes - iLength1;
            fileInputStream.read(mByteArrayWavDataOffLine, 0, mByteArrayWavDataOffLine.length);

            //mShortDCOffset =0;
            //mIntDCAccu = 0;
            for (iVar =0 ; iVar < SystemConfig.mIntUltrasoundSamplesMaxSizeForRun; iVar++) {
                for (iVar2 = 0; iVar2 < SystemConfig.mInt1DataBytes; iVar2++) {
                    byteData1[iVar2] = mByteArrayWavDataOffLine[iVar * iBlockAlign + iVar2];
                    if (iNumChannels == 2) {
                        byteData2[iVar2] = mByteArrayWavDataOffLine[iVar * iBlockAlign + iVar2 + SystemConfig.mInt1DataBytes];
                    }
                }

                //--------------------
                // DC Offset
                //--------------------

                //if (SystemConfig.mBoolDCOffsetForceAllData) {
                //    mShortDCOffset = 0;
                //} else{
                    if (SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_LE_ONLINE){
                        checkDCOffsetAndGainThresholdAndCorrectDataForItriDevice(iVar, byteData1, true);
                    } else if (SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_BE_ONLINE){
                        checkDCOffsetAndGainThresholdAndCorrectDataForItriDevice(iVar, byteData1, false);
                    } else if ((SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_OFFLINE)
                            || (SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.USCOM_8K)) {
                        mBoolDCOffsetLearned = true;
                    }
                //}

                //-------------------------------------------------
                // for  mIntUltrasoundData[iVar]
                //--------------------------------------------------

                if(SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_LE_ONLINE) {
                    shortValueNotDCOffset = (short) Utilitys.byteArrayToIntForWAVDataLittleEndian(byteData1);
                }else if(SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_BE_ONLINE) {
                    shortValueNotDCOffset = (short) Utilitys.byteArrayToIntForWAVDataBigEndian(byteData1);
                 }else if(SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_OFFLINE) {
                    shortValueNotDCOffset = (short) Utilitys.byteArrayToIntForWAVDataBigEndian(byteData1);
                }else if(SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_44K) {
                    shortValueNotDCOffset = (short) Utilitys.byteArrayToIntForWAVDataBigEndian(byteData1);
                } else{
                    shortValueNotDCOffset = (short) Utilitys.byteArrayToIntForWAVDataBigEndian(byteData1);
                }
                mShortUltrasoundData[iVar] = (short)(shortValueNotDCOffset - mShortDCOffset);

                if (mIntDataMaxValue < shortValueNotDCOffset) {
                    mIntDataMaxValue = shortValueNotDCOffset;
                    mIntDataMaxIdx = iVar;
                }
                if (iVar == SystemConfig.mIntUltrasoundSamplesMaxSizeForRun-1) {
//                    SystemConfig.mMyEventLogger.appendDebugStr("mIntDataMax/Idx = ", String.valueOf(mIntDataMaxValue) + "/" + String.valueOf(mIntDataMaxIdx));
//                    SystemConfig.mMyEventLogger.appendDebugStr("mShortDCOffset = ", String.valueOf(mShortDCOffset));
                }

                if(SystemConfig.mIntFilterDataEnabled == SystemConfig.INT_FILTER_ENABLED_YES) {
                    mShortUltrasoundDataBeforeFilter[iVar] = mShortUltrasoundData[iVar] ;

                    //cavin test
//                    mShortUltrasoundData[iVar] = (short) MainActivity.mBVSignalProcessorPart1.chebyshevUSHigh.filter(mShortUltrasoundData[iVar]);
//                    mShortUltrasoundData[iVar] = (short) MainActivity.mBVSignalProcessorPart1.butterworthUSBS.filter(mShortUltrasoundData[iVar]);
                    mShortUltrasoundData[iVar] = (short) MainActivity.mBVSignalProcessorPart1.butterworthUSHigh.filter(mShortUltrasoundData[iVar]);
//                    if (BVSignalProcessorPart1.isInverseFreq){
                        mShortUltrasoundData[iVar] = (short) MainActivity.mBVSignalProcessorPart1.butterworthUSLow.filter(mShortUltrasoundData[iVar]);
//                    }
//                    mMyDataFilter.filterProcessForData(iVar);
//                    putByteDataAfterFilter(iVar);
                }
            }
            if (BVSignalProcessorPart1.isInverseFreq){
                /*
                short[] tmpShortUSData = Doppler.mDblArrayUltrasoundDCTInverse(mShortUltrasoundData);
                for (int i=0;i<mByteArrayWavDataOffLine.length/2;i++){
                    mByteArrayWavDataOffLine[i*2] = (byte)(tmpShortUSData[i] & 0xff);
                    mByteArrayWavDataOffLine[i*2+1] = (byte)(tmpShortUSData[i] >> 8 & 0xff);
                }

                 */
            }
            mIntDataNextIndex= SystemConfig.mIntUltrasoundSamplesMaxSizeForRun;
            mIntDataCurSizeOffLine = mIntDataNextIndex;

            // SystemConfig.mAudioPlayer.dataToSoundBySegment(mByteArrayWavDataOffLine, mByteArrayWavDataOffLine.length);
            //mByteArrayWavDataOffLine = null;

            setTimeScaleMiniSecsFromFileName();

            return true;
        }catch (Exception ex){
            //SystemConfig.mMyEventLogger.appendDebugStr("setDataFromWavFile.Exception", "");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex.toString(),"");
            mByteArrayWavDataOffLine = null;
            return false;
        }
    }


    public void checkDCOffsetAndGainThresholdAndCorrectDataForItriDevice(int iSampleIdx, byte[] byteData1, boolean boolLE){
        int iVar, iDiff;

        try {
            if (iSampleIdx > mShortDCOffsetLearnSize || !SystemConfig.mBoolDCOffsetEnable) {
                return;
            } else if (iSampleIdx < mShortDCOffsetLearnSize) {
                if (boolLE) {
                    mIntDCAccu = mIntDCAccu + Utilitys.byteArrayToIntForWAVDataLittleEndian(byteData1);
                } else {
                    mIntDCAccu = mIntDCAccu + Utilitys.byteArrayToIntForWAVDataBigEndian(byteData1);
                }
            } else {
                // Cavin test DC offset
                if (Doppler.cavinDCOffset){
                    // Do nothing
                }else{
                    mShortDCOffset = (short)(mIntDCAccu / mShortDCOffsetLearnSize);
                    for (iVar = 0; iVar < mShortDCOffsetLearnSize - 1; iVar++) {
                        mShortUltrasoundData[iVar] = (short)(mShortUltrasoundData[iVar] - mShortDCOffset);
                    }
                }

                // Cavin test DC offset end
                mBoolDCOffsetLearned = true;

                mIntGainLevelCommandCur = SystemConfig.mIntGainLevel;

                iDiff = SystemConfig.INT_GAIN_CONTROL_TOP_VALUE - mShortDCOffset;
                mIntGainControlHighThresholdDiff = (int) (iDiff * SystemConfig.DOUBLE_GAIN_CONTROL_HIGH_THRESHOLD_RATIO);
                mIntGainControlLowThresholdDiff = (int) (iDiff * SystemConfig.DOUBLE_GAIN_CONTROL_LOW_THRESHOLD_RATIO);
                mIntGainControlTargetValueDiff = (int) (iDiff * SystemConfig.DOUBLE_GAIN_CONTROL_TARGET_VALUE_RATIO);
                SystemConfig.mIntGainControlHighThreshold = mIntGainControlHighThresholdDiff + mShortDCOffset;
                SystemConfig.mIntGainControlLowThreshold = mIntGainControlLowThresholdDiff + mShortDCOffset;
                SystemConfig.mIntGainControlTargetValue = mIntGainControlTargetValueDiff + mShortDCOffset;
            }
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("RawDataProc.checkDCOffsetAndGainAndCor.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }

    public String getSTFTFileName(){
        String strFile="";
        int iEndIdx;

        //iEndIdx = SystemConfig.mFragment.mStrSelectedFile.toString().length()-4;
        //strFile = SystemConfig.mFragment.mStrSelectedFile.substring(0, iEndIdx) ;
        strFile += "_STFT.txt";
        return strFile;
    }

    public String getLogBefFileName(){
        String strFile="";
        int iEndIdx;

        /*iEndIdx = SystemConfig.mFragment.mStrSelectedFile.toString().length()-4;
        strFile = SystemConfig.mFragment.mStrSelectedFile.substring(0, iEndIdx) + "_LOGBef" + ".txt";*/
        strFile += "_LOGBef.txt";
        return strFile;
    }


    public String getLogAftFileName(){
        String strFile = "";
        int iEndIdx;

        //iEndIdx = SystemConfig.mFragment.mStrSelectedFile.toString().length()-4;
        //strFile = SystemConfig.mFragment.mStrSelectedFile.substring(0, iEndIdx) + "_LOGAft" + ".txt";
        strFile += "_LOGAft.txt";
        return strFile;
    }

        public String getPSDFileName(){
            String strFile="";
            int iEndIdx;

            /*iEndIdx = SystemConfig.mFragment.mStrSelectedFile.toString().length()-4;
            strFile = SystemConfig.mFragment.mStrSelectedFile.substring(0, iEndIdx);*/
            strFile += "_PSD.txt";
            return strFile;
        }


        public String getAmpIntegralFileName(){
            String strFile="";
            int iEndIdx;

            /*iEndIdx = .mStrSelectedFile.toString().length()-4;
            strFile = SystemConfig.mFragment.mStrSelectedFile.substring(0, iEndIdx) ;*/
            strFile+="_AMPI.txt";
            return strFile;
        }
    /*
          public String getAmpIntegralLogFileName(){
              String strFile;
              int iEndIdx;

              iEndIdx = SystemConfig.mFragment.mStrSelectedFile.toString().length()-4;
              strFile = SystemConfig.mFragment.mStrSelectedFile.substring(0, iEndIdx) + "_AMPIL" + ".txt";
              return strFile;
          }


          public String getMaxIdxFileName(){
              String strFile;
              int iEndIdx;

              iEndIdx = SystemConfig.mFragment.mStrSelectedFile.toString().length()-4;
              strFile = SystemConfig.mFragment.mStrSelectedFile.substring(0, iEndIdx) + "_SigFreqIdx_" + ".txt";
              return strFile;
          }


          public String getVTIFileName(){
              String strFile;
              int iEndIdx;

              iEndIdx = SystemConfig.mFragment.mStrSelectedFile.toString().length()-4;
              strFile = SystemConfig.mFragment.mStrSelectedFile.substring(0, iEndIdx) + "_VTI_" + ".txt";
              return strFile;
          }

  */
          public String getMaxIdxForVTIFileName(){
              String strFile="";
              int iEndIdx;

              //iEndIdx = SystemConfig.mFragment.mStrSelectedFile.toString().length()-4;
              //strFile = SystemConfig.mFragment.mStrSelectedFile.substring(0, iEndIdx) + "_MaxFreqForVTI_" + ".txt";
              return strFile;
          }



    public String getFileNameAfterFilter(){
        String strFile="";
        int iEndIdx;

        /*iEndIdx = SystemConfig.mFragment.mStrSelectedFile.toString().length()-4;
        strFile = SystemConfig.mFragment.mStrSelectedFile.substring(0, iEndIdx) + "_filter"+ ".wav";*/
        strFile += "_filter.wav";
        return strFile;
    }

    public void tryAdjustGainCommandAuto() {

        try {

            if (!mBoolDCOffsetLearned) {
                return;
            }

            if (mIntGainLevelCommandCur != SystemConfig.mIntGainLevel) {
                return;
            }

            if (mIntGainObserveDiffValueMax < mIntCurPacketDataDiffValueMax) {
                mIntGainObserveDiffValueMax = mIntCurPacketDataDiffValueMax;
            }

            if (mIntGainObserveDiffValueMin > mIntCurPacketDataDiffValueMin) {
                mIntGainObserveDiffValueMin = mIntCurPacketDataDiffValueMin;
            }

            if (mIntGainObservePacketCntCur < mIntGainChangeIntervalPacketCnt) {
                mIntGainObservePacketCntCur++;
            }
            if (mIntGainObservePacketCntCur == mIntGainChangeIntervalPacketCnt) {
                mIntGainLevelCommandPre = SystemConfig.mIntGainLevel;
               // if(SystemConfig.mBoolDebugGainControl) {
                   // SystemConfig.mMyEventLogger.appendDebugStr("GainCommandPre=", String.valueOf(SystemConfig.mIntGainLevel));
               // }
                if (SystemConfig.mIntGainLevelCommandSetting == SystemConfig.INT_GAIN_CONTROL_LEVEL_CNT) {   // auto adjust
                    if ((mIntGainObserveDiffValueMax > mIntGainControlHighThresholdDiff)
                            || (mIntGainObserveDiffValueMax < mIntGainControlLowThresholdDiff)) {
                        adjustGainCommandAuto();
                    } else {
                        mIntGainLevelCommandCur = SystemConfig.mIntGainLevel;
                    }
                    mIntGainObservePacketCntCur = 0;
                    mIntGainObserveDiffValueMax = Integer.MIN_VALUE;
                    mIntGainObserveDiffValueMin = Integer.MAX_VALUE;
                }
            }
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("tryAdjustGainAuto.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }


    public void tryAdjustGainCommandFix() {

        try{
            if (!mBoolDCOffsetLearned) {
                return;
            }

            if (mIntGainObserveDiffValueMax < mIntCurPacketDataDiffValueMax) {
                mIntGainObserveDiffValueMax = mIntCurPacketDataDiffValueMax;
            }

            if (mIntGainObserveDiffValueMin > mIntCurPacketDataDiffValueMin) {
                mIntGainObserveDiffValueMin = mIntCurPacketDataDiffValueMin;
            }

            if (mIntGainObservePacketCntCur < mIntGainChangeIntervalPacketCnt) {
                mIntGainObservePacketCntCur++;
            }
            if (mIntGainObservePacketCntCur == mIntGainChangeIntervalPacketCnt) {
                if (SystemConfig.mIntGainLevel != SystemConfig.mIntGainLevelCommandSetting) {
                    adjustGainCommandFixed(SystemConfig.mIntGainLevelCommandSetting);
                }
                mIntGainObservePacketCntCur = 0;
                mIntGainObserveDiffValueMax = Integer.MIN_VALUE;
                mIntGainObserveDiffValueMin = Integer.MAX_VALUE;
            }
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("tryAdjustGainFix.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }


    public void adjustGainCommandFixed(int iGainLevelCmd) {

        BluetoothGattCharacteristic bluetoothGattCharacteristic;
        if (iGainLevelCmd < 0) {
            return;
        }

        try {
            Log.d(TAG,"iGainLevelCmd = "+ iGainLevelCmd);
            mByteArrayGainCommand[0] = (byte) iGainLevelCmd;
            bluetoothGattCharacteristic = mApplication.getBluetoothgattGaincharacteristic();
            bluetoothGattCharacteristic.setValue(mByteArrayGainCommand);

            //SystemConfig.mUltrasoundComm.writeCharacteristic(bluetoothGattCharacteristic);
            MainActivity.prepareBroadcastWrite(bluetoothGattCharacteristic);
            MainActivity.mIsWriteEnabled=true;
           // if(SystemConfig.mBoolDebugGainControl) {
                //SystemConfig.mMyEventLogger.appendDebugStr("GainCommansFixed : ", String.valueOf(iGainLevelCmd));
            //}
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("GainCommandFixed.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }

    public void adjustGainCommandAuto() {
        int iGainValueCur, iGainValueTarget, iNewCmd;
        BluetoothGattCharacteristic bluetoothGattCharacteristic;

        try{
            iGainValueCur = SystemConfig.mIntGainLevelMapVer1[SystemConfig.mIntGainLevel];

            iGainValueTarget =  iGainValueCur * mIntGainControlTargetValueDiff / mIntGainObserveDiffValueMax;
            iNewCmd = getNewGainLevelFromGainValue(iGainValueTarget);
            if (mIntGainObserveDiffValueMax >= mIntGainControlHighThresholdDiff){
                if((iNewCmd == SystemConfig.mIntGainLevel) && (SystemConfig.mIntGainLevel < SystemConfig.INT_GAIN_CONTROL_LEVEL_CNT-1)){
                    iNewCmd = SystemConfig.mIntGainLevel +1;
                }
            }
            mByteArrayGainCommand[0] = (byte)iNewCmd;
            Log.d(TAG,"iNewCmd = "+iNewCmd);

            bluetoothGattCharacteristic = mApplication.getBluetoothgattGaincharacteristic();
            bluetoothGattCharacteristic.setValue(mByteArrayGainCommand);

            //SystemConfig.mUltrasoundComm.writeCharacteristic(bluetoothGattCharacteristic);
            MainActivity.prepareBroadcastWrite(bluetoothGattCharacteristic);
            MainActivity.mIsWriteEnabled=true;

            mIntGainLevelCommandCur = (int) mByteArrayGainCommand[0];

           // if(SystemConfig.mBoolDebugGainControl) {
               // SystemConfig.mMyEventLogger.appendDebugDateEvent();
               // SystemConfig.mMyEventLogger.appendDebugIntEvent("Gain Command : ",  mShortDCOffset, mIntGainObserveDiffValueMax, SystemConfig.mIntGainLevel, SystemConfig.mIntGainControlTargetValueDiff, mIntGainLevelCommandCur);
           // }
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("GainCommandAuto.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }


    public void loopByMiniSec(int iMiniSecLoop) {
        Calendar calendar1, calendar2;
        Date date1, date2;
        long lngStarMiniSec, lngDiffMiniSec;
        boolean boolExit;

        try {
            calendar1 = Calendar.getInstance();
            date1 = calendar1.getTime();
            lngStarMiniSec = date1.getTime();
            boolExit = false;
            while (!boolExit) {
                calendar2 = Calendar.getInstance();
                date2 = calendar2.getTime();
                lngDiffMiniSec = date2.getTime() - lngStarMiniSec;
                if (lngDiffMiniSec > iMiniSecLoop) {
                    boolExit = true;
                }
            }
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("RawDataProc.loopByMiniSec","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }




    public int readGainCommandAttribute() {
        BluetoothGattCharacteristic bluetoothGattCharacteristic;

        bluetoothGattCharacteristic = mApplication.getBluetoothgattGaincharacteristic();
        // prepareBroadcastRead
        if ((bluetoothGattCharacteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            BluetoothLeService.readCharacteristic(bluetoothGattCharacteristic);
        }
        mByteArrayGainCommand = bluetoothGattCharacteristic.getValue();
        //SystemConfig.mMyEventLogger.appendDebugIntEvent("Gain Attribute Read : ",(int)mByteArrayGainCommand[0], 0,0,0,0);
        return (int)mByteArrayGainCommand[0];
    }

    public void putByteDataAfterFilter(int iDataIdx){
        int iData;
        iData = mShortUltrasoundData[iDataIdx];
        Utilitys.intToByteArrayBigEndian(iData, mByteArray2Bytes);
        mByteArrayWaveDataAfterFilter[iDataIdx * 2] = mByteArray2Bytes[0];
        mByteArrayWaveDataAfterFilter[iDataIdx * 2 +1] = mByteArray2Bytes[1];
        iData = Utilitys.byteArrayToIntBigEndian(mByteArray2Bytes);
    }


    public int getNewGainLevelFromGainValue(int iGainValue){
        int iVar, iVar2, iGainLevelHigh ;

        try {
            for (iVar = 0; iVar < SystemConfig.INT_GAIN_CONTROL_LEVEL_CNT - 1; iVar++) {
                iVar2 = SystemConfig.INT_GAIN_CONTROL_LEVEL_CNT - 1 - iVar;
                if (iGainValue <= SystemConfig.mIntGainLevelMapVer1[iVar2]) {
                    return iVar2;
                }
            }
            return 0;
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("RawDataProc.getNewGainLevelFromGainValue.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            return 4;
        }
    }

    private void setTimeScaleMiniSecsFromFileName(){
        int iLength;
        String strDateTime;

        try {
            iLength = mStrCurFileName.length();
            strDateTime = mStrCurFileName.substring(3, 21);
            setTimeScaleMiniSecsFromDateStr(strDateTime);
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("setTimeScaleMiniFromFile.Exception","");
            setTimeScaleMiniSecsFromDateStr("20000101_000000000");
        }
    }

    private void startAudioClassification() throws IOException {
        // If the audio classifier is initialized and running, do nothing.
        if (audioClassifier != null) return;

        // Initialize the audio classifier
        AudioClassifier classifier = AudioClassifier.createFromFile(mActivity, MODEL_FILE);
        TensorAudio audioTensor = classifier.createInputTensorAudio();


        audioClassifier = classifier;
        tensorAudio = audioTensor;
    }

    /* 將8K轉成16K 2023/02/06 by Doris */
    public short[] resampleTo16k(){
        int length = 224000;
        short[] temp = new short[length];
//        Log.d("length: ", String.valueOf(temp.length));
        for(int i = 0 ; i < length/2 ; i++){
            temp[i * 2] = mShortUltrasoundDataBeforeFilter[i];
            temp[i * 2 + 1] = mShortUltrasoundDataBeforeFilter[i];
        }
        return temp;
    }

    public void classificationUSPA(){
              if (tensorAudio!=null &&
                      mShortUltrasoundDataBeforeFilter!=null &&
                      mIntDataNextIndex>classificationIntervalPts){
//                  tensorAudio.load(mShortUltrasoundDataBeforeFilter
//                          ,mIntDataNextIndex-classificationIntervalPts,classificationIntervalPts);
//                  tensorAudio.load(resampleTo16k());
                  tensorAudio.load(resampleTo16k()
                          ,mIntDataNextIndex-classificationIntervalPts,classificationIntervalPts);

                  Log.d("mIntDataNextIndex: ", String.valueOf(mIntDataNextIndex));
                  Log.d("mShortUltrasoundDataBeforeFilter: ", String.valueOf(mShortUltrasoundDataBeforeFilter.length));
                  List<Classifications> output = audioClassifier.classify(tensorAudio);

                  List<Category> filteredCategory =
                          output.get(0).getCategories().stream()
                                  .filter(it->it.getScore()>MINIMUM_DISPLAY_THRESHOLD)
                                  .collect(Collectors.toList());

                  String outputString = filteredCategory.toString();
                  String outputSplit[] = outputString.split(",");
                  if(outputSplit[0].contains("PA")){
                      SystemConfig.isPAvoice ++;
                      Log.d(TAG, String.valueOf(SystemConfig.isPAvoice));
//                      if (SystemConfig.isPAvoice >= 15){
//                          storeByteToRawData16K();
//                      }
                  }else{
                      SystemConfig.isPAvoice = 0;
                      Log.d(TAG, "clear PA count~");
                  }

//                  for (int i=0; i<mShortUltrasoundDataBeforeFilter.length; i++){
//                      Log.d("mShort", String.valueOf(mShortUltrasoundDataBeforeFilter[i]));
//                      Log.d("mShort", i + " value : " + mShortUltrasoundDataBeforeFilter[i]);
//                  }

                  Log.d(TAG, outputString);
//                  Log.d("mShortUltrasoundDataBeforeFilter", String.valueOf(mShortUltrasoundDataBeforeFilter.length));
//                  Log.d("tensorAudio", String.valueOf(tensorAudio.getFormat()));
//                  Log.d("audioClassifier", String.valueOf(audioClassifier));
//                  Log.d("audioClassifier", String.valueOf(audioClassifier.getRequiredTensorAudioFormat()));
//                  Log.d("output", String.valueOf(output));
//                  Log.d("sampleRate", String.valueOf(SystemConfig.mIntUltrasoundSamplerate));
//                  Log.d(TAG,outputSplit[0]);

              }
    }


}
