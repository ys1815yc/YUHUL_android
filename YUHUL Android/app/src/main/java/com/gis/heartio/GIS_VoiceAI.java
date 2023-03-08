package com.gis.heartio;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.gis.heartio.SupportSubsystem.SystemConfig;

import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.task.audio.classifier.Classifications;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GIS_VoiceAI {
    private static final String TAG = "GIS_VoiceAI";
    private static final String MODEL_FILE = "03_3.tflite";

    private static AudioClassifier audioClassifier = null;
    private static TensorAudio tensorAudio = null;
    private static int indexLength = 8000; // 0.5s

    private static AppCompatActivity mActivity = null;
    private static boolean initFlag = false;

    public static void initVoiceAI() throws IOException {
        if (audioClassifier != null) {
            return;
        }

        // Initialize the audio classifier
        AudioClassifier classifier = AudioClassifier.createFromFile(mActivity, MODEL_FILE);
        TensorAudio audioTensor = classifier.createInputTensorAudio();

        audioClassifier = classifier;
        tensorAudio = audioTensor;
        Log.e(TAG, "GIS_VoiceAI()");
    }


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

    public static void judgeVoice(short[] rawData, int index) {
        if (!initFlag){
            try {
                initVoiceAI();
            } catch (IOException ex1) {
                ex1.printStackTrace();
            }
            initFlag = true;
            Log.e(TAG, "judgeVoice");
        }

        if (tensorAudio != null && rawData != null && index >= indexLength) {
            short[] temp = Arrays.copyOfRange(rawData, index - indexLength, index);
            tensorAudio.load(resampleTo16k(temp));
//                  tensorAudio.load(mShortUltrasoundDataBeforeFilter
//                          ,mIntDataNextIndex-classificationIntervalPts,classificationIntervalPts);

            /* 驗證 mShortUltrasoundDataBeforeFilter的value 2023/03/02 by Doris*/
//                  for(int i=0; i< mShortUltrasoundDataBeforeFilter.length; i+=8000){
//                      if(!lastData){
//                          lastArray = mShortUltrasoundDataBeforeFilter.clone();
//                          lastData = true;
//                          continue;
//                      }
//                      Log.d("mShortArray "+i, String.valueOf(mShortUltrasoundDataBeforeFilter[i]));
//                      if (Math.abs(mShortUltrasoundDataBeforeFilter[i] - lastArray[i]) == 0){
////                          Log.d("lastArray ", String.valueOf(i));
////                          Log.d("nowArray "+i, String.valueOf(mShortArray[i]));
////                          Log.d("nowArray "+i, String.valueOf(mShortUltrasoundDataBeforeFilter[i]));
//                      }
//                      lastArray = mShortUltrasoundDataBeforeFilter.clone();
//                      Log.d("lastArray "+i, String.valueOf(lastArray[i]));
////                      Log.d("lastData"+i, String.valueOf(lastData == false));
//                  }
            List<Classifications> output = audioClassifier.classify(tensorAudio);

            List<Category> filteredCategory =
                    output.get(0).getCategories().stream()
                            .filter(it -> it.getScore() > 0.3f)
                            .collect(Collectors.toList());

            String outputString = filteredCategory.toString();
            String outputSplit[] = outputString.split(",");
            if (outputSplit[0].contains("PA")) {
                SystemConfig.isPAvoice++;
                Log.d(TAG, String.valueOf(SystemConfig.isPAvoice));
            } else {
                SystemConfig.isPAvoice = 0;
//                      Log.d(TAG, "clear PA count~");
            }
            Log.d(TAG, outputString);
        } else {
//            Log.e(TAG, "else");
            Log.e(TAG, String.valueOf(tensorAudio));
        }
    }
}
