package com.gis.heartio;

import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.SystemConfig;

import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.task.audio.classifier.Classifications;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GIS_VoiceAI {
    private static final String TAG = "GIS_VoiceAI";

    private static int indexLength = 8000; // 0.5s

    /* 將8K轉成16K 2023/02/06 by Doris */
    public static short[] resampleTo16k(short[] rawArray){
        int length = 16000;
        short[] temp = new short[length];
//        for(int i = 0 ; i < length/2 ; i++){ //複製同樣的點
//            temp[i * 2] = mShortUltrasoundDataBeforeFilter[i];
//            temp[i * 2 + 1] = mShortUltrasoundDataBeforeFilter[i];
//        }

        for(int i = 0 ; i < length/2 ; i++){
            temp[i * 2] = rawArray[i];
            if(i == 7999){
                temp[i * 2 + 1] = rawArray[i];
            }else{
                temp[i * 2 + 1] = (short) ((rawArray[i] + rawArray[i+1]) / 2);
            }
        }
        return temp;
    }

    public static void judgeVoice(TensorAudio tensorAudio, AudioClassifier audioClassifier, short[] rawData, int index) {

        if (tensorAudio != null && rawData != null && index >= indexLength) {
            short[] temp = Arrays.copyOfRange(rawData, index - indexLength, index);
            tensorAudio.load(resampleTo16k(temp));
            GIS_Log.d(TAG+index, String.valueOf(index));
            GIS_Log.d(TAG+indexLength, String.valueOf(indexLength));

            List<Classifications> output = audioClassifier.classify(tensorAudio);

            List<Category> filteredCategory =
                    output.get(0).getCategories().stream()
                            .filter(it -> it.getScore() > 0.3f)
                            .collect(Collectors.toList());

            String outputLabel = filteredCategory.get(0).getLabel();

            if (outputLabel.endsWith("PA")){
                SystemConfig.isPAvoice++;
                GIS_Log.d(TAG, String.valueOf(SystemConfig.isPAvoice));
            } else {
                SystemConfig.isPAvoice = 0;
            }
            GIS_Log.d(TAG, outputLabel);
        } else {
            GIS_Log.e(TAG, String.valueOf(tensorAudio));
        }
    }
}
