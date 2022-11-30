package com.gis.heartio.AudioSubsystem;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.gis.heartio.SignalProcessSubsystem.BVSignalProcessorPart1;
import com.gis.heartio.SupportSubsystem.MyDataFilter2;
import com.gis.heartio.SupportSubsystem.SystemConfig;
import com.gis.heartio.UIOperationControlSubsystem.MainActivity;

/**
 * Created by 780797 on 2016/8/30.
 */
public class MyAudioPlayer {


    private static final int AUDIO_TOTAL_DATA_SEC = 1;//unit: sec

    //----- for Audio Basic Info. ---------------------
    public static final String INFO_TAG_AUDIO = "Audio Info";

    //----- for Audio State ---------------------
    private static final int STATE_PCM_DECODER_OPEN = 1;
    private static final int STATE_PCM_DECODER_PLAY = 2;
    private static final int STATE_PCM_DECODER_CLOSE = 3;
    private int mState;

    //----- for Audio Data & Codec & Play---------------------
    public short[] mShortArrayAudioSegment;
    private byte[] mBytes16PCM;
    //private int audio_track_play_buffer_size = 0;
    private AudioTrack audio_track_handle = null;

    //----- for DC offset ---------------------
    private static final int INT_AUDIO_DC_OFFSET_DEFAULT = 2048;
    private static final double DOULE_AUDIO_DC_OFFSET_CHECK_SEC = 1.0;
    private static final double DOULE_AUDIO_GAIN_CHECK_SEC = 1.0;
    private int mIntDCOffsetCheckCnt;
    private int mIntCurDCOffsetAccu, mIntCurDCOffsetAccuCnt;
    private short mShortDCOffset;
    private boolean mBoolDCOffset;

    //------ for Audio Gain -------------------
    private int mIntCurGainCheckCnt, mIntGainCheckCnt;
    private short mShortMaxVal;
    private static final short mShortAudioHighThreshHold = Short.MAX_VALUE * 5 /6;
    private static final short mShortAudioLowThreshHold = Short.MAX_VALUE * 3 /6;
    private static final short mShortAudioAduustTarget = Short.MAX_VALUE * 4 /6;
    //private int mIntAudioGainSetting = 1, mIntGain;
    private int mIntGain;

    //------ for Filter -------------------
    private MyDataFilter2 mMyDataFilter;

    //------ for Debug -------------------
    private int mIntDebugCurSeq = 0;
    private int mIntLostCnt;

    private int mIntStartNextIdxOnLine;

    public MyAudioPlayer() {

        mBytes16PCM = new byte[2];
        mShortArrayAudioSegment = new short[SystemConfig.mIntPacketDataByteSize/2];

        mMyDataFilter = new MyDataFilter2();

    }


    public void dataToSoundBySegmentEndIdxOnLine(int dataNextIdx) {
        int iSampleSize, iSegmentStartIdx, iDataLength1, iDataLength2;
        int iSegmentEndIdx, result_write_num;

        if (mState != STATE_PCM_DECODER_PLAY) {
            Log.d("AudioPlayer","mState != STATE_PCM_DECODER_PLAY");
            return;
        }else if(MainActivity.mRawDataProcessor.mIntDataNextIndex == mIntStartNextIdxOnLine){
            Log.d("AudioPlayer","MainActivity.mRawDataProcessor.mIntDataNextIndex == mIntStartNextIdxOnLine");
            return;
        }

        //SystemConfig.mMyEventLogger.appendDebugStr("AudioNextIdx =", String.valueOf(mIntStartNextIdxOnLine));
        //SystemConfig.mMyEventLogger.appendDebugStr("DataNextIdx =", String.valueOf(SystemConfig.mRawDataProcessor.mIntDataNextIndex));

        try {
            iSegmentEndIdx = MainActivity.mRawDataProcessor.mIntDataNextIndex -1;
            if(iSegmentEndIdx == -1){
                iSegmentEndIdx = SystemConfig.mIntUltrasoundSamplesMaxSizeForRun-1;
            }
            if (SystemConfig.isHeartIO2){
                iSampleSize = SystemConfig.mIntPacketDataByteSizeHeartIO2;
            }else{
                iSampleSize = SystemConfig.mIntPacketDataByteSize / 2;
            }


            if(mIntStartNextIdxOnLine == -1){
                mIntStartNextIdxOnLine = iSegmentEndIdx - iSampleSize +1;
            }

            //------- put data to audio -------------------------------
//            Log.d("AudioPlayer","mIntStartNextIdxOnLine="+mIntStartNextIdxOnLine);
//            Log.d("AudioPlayer","iSegmentEndIdx="+iSegmentEndIdx);
            if(mIntStartNextIdxOnLine > iSegmentEndIdx){
                //SystemConfig.mMyEventLogger.appendDebugStr("AudioStep1", "");
                iDataLength1 = SystemConfig.mIntUltrasoundSamplesMaxSizeForRun - mIntStartNextIdxOnLine ;
                //Log.d("AudioPlayer","iDataLength1="+iDataLength1);
                while (iDataLength1 > 0) {
                    if (BVSignalProcessorPart1.isInverseFreq){
                        result_write_num = audio_track_handle.write(MainActivity.mRawDataProcessor.mShortUltrasoundDataInverse, mIntStartNextIdxOnLine, iDataLength1);
                    }else{
                        result_write_num = audio_track_handle.write(MainActivity.mRawDataProcessor.mShortUltrasoundData, mIntStartNextIdxOnLine, iDataLength1);
                    }
                    iDataLength1 = iDataLength1 - result_write_num;
                    mIntStartNextIdxOnLine = mIntStartNextIdxOnLine + result_write_num;
                    if(mIntStartNextIdxOnLine >= SystemConfig.mIntUltrasoundSamplesMaxSizeForRun){
                        mIntStartNextIdxOnLine = 0;
                    }
                    //SystemConfig.mMyEventLogger.appendDebugStr("iDataLen1=",String.valueOf(iDataLength1));
                }
            }

            //SystemConfig.mMyEventLogger.appendDebugStr("AudioStep2", "");
            iDataLength2 = iSegmentEndIdx - mIntStartNextIdxOnLine +1;
            //Log.d("AudioPlayer","iDataLength2="+iDataLength2);
            //SystemConfig.mMyEventLogger.appendDebugStr("iDataLength2=", String.valueOf(iDataLength2));
            while (iDataLength2 > 0) {
                if (BVSignalProcessorPart1.isInverseFreq){
                    result_write_num = audio_track_handle.write(MainActivity.mRawDataProcessor.mShortUltrasoundDataInverse, mIntStartNextIdxOnLine, iDataLength2);
                }else{
                    result_write_num = audio_track_handle.write(MainActivity.mRawDataProcessor.mShortUltrasoundData, mIntStartNextIdxOnLine, iDataLength2);
                }
                //SystemConfig.mMyEventLogger.appendDebugStr("result_write_num=", String.valueOf(result_write_num));
                iDataLength2 = iDataLength2 - result_write_num;
                mIntStartNextIdxOnLine = mIntStartNextIdxOnLine + result_write_num;
                if (mIntStartNextIdxOnLine >= SystemConfig.mIntUltrasoundSamplesMaxSizeForRun) {
                    mIntStartNextIdxOnLine = mIntStartNextIdxOnLine - SystemConfig.mIntUltrasoundSamplesMaxSizeForRun;
                }
            }
        }catch(Exception ex1){
            ex1.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugDateEvent();
            //SystemConfig.mMyEventLogger.appendDebugStr("dataToSoundSegmentEndIdx.Exception", "");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(), "");
        }
    }

/*
    public void dataToSoundBySegmentEndIdxOnLine_20171119(int iSegmentEndIdx) {
        int iSampleSize, iSegmentStartIdx, iDataLength1, iDataLength2;
        int result_write_num;

        if (mState != STATE_PCM_DECODER_PLAY) {
            return;
        }

        try {
            iSampleSize = SystemConfig.mIntPacketDataByteSize / 2;
            if(iSegmentEndIdx >=  iSampleSize -1) {
                iSegmentStartIdx = iSegmentEndIdx - iSampleSize + 1;
            }else{
                iSegmentStartIdx = SystemConfig.mIntRawPacketCntsOnLineForRun - iSampleSize +  iSegmentEndIdx +1;
            }

            //------- put data to audio -------------------------------
            //SystemConfig.mMyEventLogger.appendDebugStr("iSegEndIdx=",String.valueOf(iSegmentStartIdx) + "/" +String.valueOf(iSegmentEndIdx));
            if(iSegmentEndIdx >= iSegmentStartIdx){
                //SystemConfig.mMyEventLogger.appendDebugStr("AudioType-1","");
                result_write_num = audio_track_handle.write(SystemConfig.mRawDataProcessor.mShortUltrasoundDatas, iSegmentStartIdx, iSampleSize);
                if(result_write_num < iSampleSize){
                    mIntLostCnt++;
                    //SystemConfig.mMyEventLogger.appendDebugStr("AudioWriteFail-1",String.valueOf(result_write_num));
                }
            }else{
                //SystemConfig.mMyEventLogger.appendDebugStr("AudioType-2","");
                iDataLength1 = SystemConfig.mIntRawPacketCntsOnLineForRun - iSegmentStartIdx;
                //SystemConfig.mMyEventLogger.appendDebugStr("iDataLen1=",String.valueOf(iDataLength1));
                result_write_num = audio_track_handle.write(SystemConfig.mRawDataProcessor.mShortUltrasoundDatas, iSegmentStartIdx, iDataLength1);
                if(result_write_num < iDataLength1){
                    mIntLostCnt++;
                    //SystemConfig.mMyEventLogger.appendDebugStr("AudioWriteFail-2","");
                }
                iDataLength2 = iSegmentEndIdx +1;
                //SystemConfig.mMyEventLogger.appendDebugStr("iDataLen2=",String.valueOf(iDataLength2));
                result_write_num = audio_track_handle.write(SystemConfig.mRawDataProcessor.mShortUltrasoundDatas, 0, iDataLength2);
                if(result_write_num < iDataLength2){
                    mIntLostCnt++;
                    //SystemConfig.mMyEventLogger.appendDebugStr("AudioWriteFail-3","");
                }
            }
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("dataToSoundSegmentEndIdx.Exception", "");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(), "");
        }
    }

*/

    public void dataToSoundBySegment(byte[] byteArray, int iLength) {
        int iSampleSize, iVar, iValue, iCompare, iFilterLength;
        int pcm_data_size, pcm_data_buffer_offset;
        int write_num,result_write_num;
        int iDebug1;
        boolean boolDebugShow;
        // Modified to 4 for volume gain
        int mIntAudioGainSetting = 4;

        if (mState != STATE_PCM_DECODER_PLAY) {
            return;
        }

        try {
            iSampleSize = iLength / 2;
            if (SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_BE_ONLINE) {
                for (iVar = 0; iVar < iSampleSize; iVar++) {
                    mBytes16PCM[0] = byteArray[iVar * 2];
                    mBytes16PCM[1] = byteArray[iVar * 2 + 1];
                    mShortArrayAudioSegment[iVar] = ByteBuffer.wrap(mBytes16PCM).order(ByteOrder.LITTLE_ENDIAN).getShort();
                }
            }else if (SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_LE_ONLINE) {
                for (iVar = 0; iVar < iSampleSize; iVar++) {
                    mBytes16PCM[0] = byteArray[iVar * 2+1];
                    mBytes16PCM[1] = byteArray[iVar * 2];
                    mShortArrayAudioSegment[iVar] = ByteBuffer.wrap(mBytes16PCM).order(ByteOrder.LITTLE_ENDIAN).getShort();
                }
            }else{
                for (iVar = 0; iVar < iSampleSize; iVar++) {
                    mBytes16PCM[0] = byteArray[iVar * 2];
                    mBytes16PCM[1] = byteArray[iVar * 2 + 1];
                    mShortArrayAudioSegment[iVar] = ByteBuffer.wrap(mBytes16PCM).order(ByteOrder.LITTLE_ENDIAN).getShort();
                }
            }

            //--- estimate DC Offfset -------------------------------
            if(mIntCurDCOffsetAccuCnt < mIntDCOffsetCheckCnt) {
                for (iVar = 0; iVar < iSampleSize; iVar++) {
                    mIntCurDCOffsetAccu = mIntCurDCOffsetAccu + mShortArrayAudioSegment[iVar];
                }
                mIntCurDCOffsetAccuCnt = mIntCurDCOffsetAccuCnt + iSampleSize;
                return;
            }
            if (!mBoolDCOffset){
                mShortDCOffset = (short) (mIntCurDCOffsetAccu / mIntCurDCOffsetAccuCnt);
                mBoolDCOffset = true;
            }

            //--- estimate Audio Gain -------------------------------
            if(mIntAudioGainSetting == 0) {
                for (iVar = 0; iVar < iSampleSize; iVar++) {
                    if (mShortMaxVal < (mShortArrayAudioSegment[iVar] - mShortDCOffset)) {
                        mShortMaxVal = (short) (mShortArrayAudioSegment[iVar] - mShortDCOffset);
                    }
                }
                mIntCurGainCheckCnt = mIntCurGainCheckCnt + iSampleSize;

                if (mIntCurGainCheckCnt >= mIntGainCheckCnt) {
                    iValue = (int) mShortMaxVal * mIntGain;
                    if (iValue > (int) mShortAudioHighThreshHold) {
                        mIntGain = mShortAudioAduustTarget / mShortMaxVal;
                    } else if (iValue < (int) mShortAudioLowThreshHold) {
                        mIntGain = mShortAudioAduustTarget / mShortMaxVal;
                    }
                    mIntCurGainCheckCnt = 0;
                    mShortMaxVal = Short.MIN_VALUE;
                }
            }else{
                mIntGain = mIntAudioGainSetting;
            }

            if ((SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_BE_ONLINE)
                    || (SystemConfig.mEnumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_LE_ONLINE)){
                for (iVar = 0; iVar < iSampleSize; iVar++) {
                    iCompare = (mShortArrayAudioSegment[iVar] - mShortDCOffset) * mIntGain;
                    if (iCompare <= (int)Short.MIN_VALUE) {
                        iCompare = Short.MIN_VALUE;
                    } else if (iCompare >= (short) Short.MAX_VALUE) {
                        iCompare = Short.MAX_VALUE;
                    }
                    mShortArrayAudioSegment[iVar] = (short) iCompare;
                }
            }

            boolDebugShow = false;
            if(boolDebugShow) {
                if (mIntDebugCurSeq < 1000) {
                    if (mIntDebugCurSeq == 0) {
                        //SystemConfig.mMyEventLogger.appendDebugDateEvent();
                    }
                    iDebug1 = mIntDebugCurSeq % 100;
                    if (iDebug1 == 0) {
                        //SystemConfig.mMyEventLogger.appendDebugStr("dataToSoundBySegment=",String.valueOf(iDebug1));
                    }
                    mIntDebugCurSeq++;
                }
            }

            //------- put data to audio -------------------------------
            pcm_data_size = iSampleSize;
            pcm_data_buffer_offset = 0;
            while (pcm_data_size > 0) {
                write_num = pcm_data_size;
                result_write_num = audio_track_handle.write(mShortArrayAudioSegment, pcm_data_buffer_offset, write_num);
                pcm_data_buffer_offset = pcm_data_buffer_offset + result_write_num;
                pcm_data_size = pcm_data_size - result_write_num;
                if(pcm_data_size > 0){
                    mIntLostCnt++;
                    //SystemConfig.mMyEventLogger.appendDebugStr("AudioWriteFail","");
                }
            }
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("dataToSoundSegment.Exception", "");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(), "");
            ex1.printStackTrace();
        }
    }

    public void dataToSoundByAllSegment(byte[] byteArray, int iLength) {
        int iSegmentQty, iVar;
        byte[] byteArrayOneSegment;

        if (mState != STATE_PCM_DECODER_PLAY) {
            return;
        }

        iSegmentQty = iLength / SystemConfig.mIntPacketDataByteSize;
        byteArrayOneSegment = new byte[SystemConfig.mIntPacketDataByteSize];

        for (iVar = 0 ; iVar < iSegmentQty ; iVar++) {
            System.arraycopy(byteArray, iVar*SystemConfig.mIntPacketDataByteSize, byteArrayOneSegment, 0, SystemConfig.mIntPacketDataByteSize);
            dataToSoundBySegment(byteArrayOneSegment, SystemConfig.mIntPacketDataByteSize);
        }
    }

    public void StartPlayAudio(int iSamplingRate)
    {
        Log.d("AudioPlayer","Start Play Audio!!!!!!!");
        int audio_track_play_buffer_size;
        try {
            audio_track_play_buffer_size = iSamplingRate * AUDIO_TOTAL_DATA_SEC * SystemConfig.INT_ULTRASOUND_DATA_1DATA_BYTES_16PCM;
            if (Build.VERSION.SDK_INT <23){
                 audio_track_handle = new AudioTrack(AudioManager.STREAM_MUSIC,
                                                iSamplingRate,
                                                AudioFormat.CHANNEL_OUT_MONO,
                                                AudioFormat.ENCODING_PCM_16BIT,
                                                audio_track_play_buffer_size,
                                                AudioTrack.MODE_STREAM);
            }else {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .build();
                AudioFormat format = new AudioFormat.Builder().
                        setEncoding(AudioFormat.ENCODING_PCM_16BIT).
                        setSampleRate(iSamplingRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).
                                build();

                audio_track_handle = new AudioTrack.Builder().
                        setAudioAttributes(audioAttributes).
                        setAudioFormat(format).
                        setBufferSizeInBytes(audio_track_play_buffer_size).
                        build();
            }

            mState = STATE_PCM_DECODER_OPEN;

            mMyDataFilter.prepareStart();
            mIntDCOffsetCheckCnt = (int) ((double) SystemConfig.mIntUltrasoundSamplerate * DOULE_AUDIO_DC_OFFSET_CHECK_SEC);
            mShortDCOffset = (short) INT_AUDIO_DC_OFFSET_DEFAULT;
            mIntCurDCOffsetAccu = 0;
            mIntCurDCOffsetAccuCnt = 0;
            mBoolDCOffset = false;
            mIntGainCheckCnt = (int) ((double) SystemConfig.mIntUltrasoundSamplerate * DOULE_AUDIO_GAIN_CHECK_SEC);
            mIntGain = 1;
            mShortMaxVal = Short.MIN_VALUE;
            mIntCurGainCheckCnt = 0;
            mIntDebugCurSeq = 0;
            mIntLostCnt = 0;
            mIntStartNextIdxOnLine = -1;
            audio_track_handle.play();
            mState = STATE_PCM_DECODER_PLAY;

        }catch(Exception ex1){
            ex1.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugStr("MyAudioPlayer.StartPlayAudio.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }

    public void StopPlayAudio()
    {
        if((mState == STATE_PCM_DECODER_OPEN) || (mState == STATE_PCM_DECODER_PLAY))
        {
            mState = STATE_PCM_DECODER_CLOSE;
            if (audio_track_handle.getPlayState()!=AudioTrack.PLAYSTATE_STOPPED){
                try {
                    audio_track_handle.stop();
                }catch (IllegalStateException e){
                    e.printStackTrace();
                }
            }
            audio_track_handle.release();
            audio_track_handle = null;
        }
    }

}

