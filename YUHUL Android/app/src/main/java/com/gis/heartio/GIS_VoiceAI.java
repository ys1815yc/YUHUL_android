package com.gis.heartio;

import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.SystemConfig;
import com.gis.heartio.UIOperationControlSubsystem.MainActivity;

import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.task.audio.classifier.Classifications;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GIS_VoiceAI {
    private static final String TAG = GIS_VoiceAI.class.getSimpleName();

    private static final int indexLength = 4000; // 0.5s

    /* 將8K轉成16K 2023/02/06 by Doris */
    public static short[] resampleTo16k(short[] rawArray){
        int length = rawArray.length * 2;
        short[] temp = new short[length];

        for(int i = 0 ; i < length/2 ; i++){
            temp[i * 2] = rawArray[i];
            if(i == (length/2 - 1)){
                temp[i * 2 + 1] = rawArray[i];
            }else{
                temp[i * 2 + 1] = (short) ((rawArray[i] + rawArray[i+1]) / 2);
            }
        }
        return temp;
    }

    /*判斷聲音種類，之後存10秒的類別 by Doris*/
    public static void judgeVoice(TensorAudio tensorAudio, AudioClassifier audioClassifier, short[] rawData, int index) {

        if (tensorAudio != null && rawData != null && index >= indexLength) {
            short[] temp = Arrays.copyOfRange(rawData, index - indexLength, index);
//            GIS_Log.d("temp", String.valueOf(temp.length));
//            GIS_Log.d("temp0", String.valueOf(temp[0]));
//            GIS_Log.d("temp-1", String.valueOf(temp[indexLength-1]));
//            GIS_Log.d("temp-2", String.valueOf(temp[indexLength-2]));
            tensorAudio.load(resampleTo16k(temp));

            List<Classifications> output = audioClassifier.classify(tensorAudio);

            List<Category> filteredCategory =
                    output.get(0).getCategories().stream()
                            .filter(it -> it.getScore() > 0.3f)
                            .collect(Collectors.toList());

            String outputLabel = filteredCategory.get(0).getLabel();

            if(SystemConfig.voiceIndex < 10){
                SystemConfig.voiceCategory[SystemConfig.voiceIndex++] = outputLabel;
                if(SystemConfig.voiceIndex == 10){
                    SystemConfig.voiceIndex = 0;
                }
            }

            if(outputLabel.equals("PA")){
                SystemConfig.continuePA++;
                SystemConfig.continueNotPA = 0;
            }else {
                SystemConfig.continuePA = 0;
                SystemConfig.continueNotPA++;
            }
            GIS_Log.d(TAG, outputLabel);

        } else {
            GIS_Log.d(TAG, String.valueOf(tensorAudio));
        }
    }

    /*判斷聲音類別是否為PA超過8個 2023/03/16 by Doris*/
    public static void isPA(){
        boolean PA = false;
        short times = 0;
        for (int i=0; i<SystemConfig.voiceCategory.length; i++){
            if(SystemConfig.voiceCategory[i].equals("PA") ){
                times++;
            }
        }
        if (times >= 8){
            PA = true;
        }
//        GIS_Log.d(TAG, "PA "+PA);

        MainActivity.oFrag.updateHRValue((SystemConfig.continuePA >= 4 && SystemConfig.continueNotPA <= 1 && PA));
    }
}
