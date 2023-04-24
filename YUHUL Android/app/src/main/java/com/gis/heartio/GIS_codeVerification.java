package com.gis.heartio;

import static com.gis.heartio.GIS_VoiceAI.resampleTo16k;

import android.util.Log;

import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.SystemConfig;

import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.task.audio.classifier.Classifications;

import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class GIS_codeVerification {
    private static final String TAG = "GIS_codeVerification";

    /* 將原始raw data以16K儲存下來 2023/02/04 by Doris */
    public static void storeByteToRawData16K(short[] rawArray){
        try{
            String exportDirPath = "/storage/emulated/0/Android/data/com.gis.heartio/files/Documents/Doris";
            SimpleDateFormat df;
            df = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String strDate = df.format(new Date());
            String filename = exportDirPath + "/raw16K_" + strDate + ".raw";

            File file= new File(filename);
            if (file.exists()){
                file.delete();
            }

            FileOutputStream fileOutputStream = new FileOutputStream(file, true);

            int length = 448000;
            byte[] temp = new byte[length];
            /* tempRaw長度=111999 用來儲存原始raw array兩點相加除以2的值 2023/02/09 by Doris */
            short[] tempRaw = new short[length/4-1];
            GIS_Log.d(TAG,"length: " + String.valueOf(temp.length));

            //分兩個byte來存 用複製點的方式
//            for(int i = 0 ; i < length/4 ; i++){
//                temp[i * 4] = (byte)(rawArray[i] & 0xFF);
//                temp[i * 4 + 2] = (byte)(rawArray[i] & 0xFF);
//                temp[i * 4 + 1] = (byte)((rawArray[i] >> 8) & 0xFF);
//                temp[i * 4 + 3] = (byte)((rawArray[i] >> 8) & 0xFF);
//            }

            //分兩個byte來存 插入相鄰兩點相加除以2
            for(int i = 0 ; i < length/4 ; i++){
                temp[i * 4] = (byte)(rawArray[i] & 0xFF);
                temp[i * 4 + 1] = (byte)((rawArray[i] >> 8) & 0xFF);

                if(i == 111999){
                    temp[i * 4 + 2] = (byte)(rawArray[i] & 0xFF);
                    temp[i * 4 + 3] = (byte)((rawArray[i] >> 8) & 0xFF);
                    break;
                }

                tempRaw[i] = (short) ((rawArray[i] + rawArray[i+1]) / 2);
                temp[i * 4 + 2] = (byte)(tempRaw[i] & 0xFF);
                temp[i * 4 + 3] = (byte)((tempRaw[i] >> 8) & 0xFF);
            }
            fileOutputStream.write(temp);
            fileOutputStream.close();

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    /* 將原始raw data以8K儲存下來 2023/02/04 by Doris */
    public static void storeByteToRawData8K(short[] rawArray){
        try{
            String exportDirPath = "/storage/emulated/0/Android/data/com.gis.heartio/files/Documents/001";
            SimpleDateFormat df;
            df = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String strDate = df.format(new Date());
            String filename = exportDirPath + "/raw8K_" + strDate + ".raw";

            File file= new File(filename);
            if (file.exists()){
                file.delete();
            }

            FileOutputStream fileOutputStream = new FileOutputStream(file, true);

            int length = 224000;
            byte[] temp = new byte[length];
            GIS_Log.d(TAG,"length: " + String.valueOf(temp.length));
            for(int i = 0 ; i < length/2 ; i++){ //分兩個byte來存
                temp[i * 2] = (byte)(rawArray[i] & 0xFF);
                temp[i * 2 + 1] = (byte)((rawArray[i] >> 8) & 0xFF);
            }
            fileOutputStream.write(temp);
            fileOutputStream.close();

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    public static void resampleCutTest(TensorAudio tensorAudio, AudioClassifier audioClassifier, short[] rawArray){

        String exportDirPath = "/storage/emulated/0/Android/data/com.gis.heartio/files/Documents/Doris";
        SimpleDateFormat df;
        df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String strDate = df.format(new Date());
        String filename = exportDirPath + "/Log_" + strDate + ".txt";

        try {
            FileWriter fw = new FileWriter(filename);
            BufferedWriter buff = new BufferedWriter(fw);

            if (tensorAudio != null) {
                /* 一次resample一段8000 */
                for (int i = 0; i < rawArray.length; i += 8000) {
                    short[] temp = Arrays.copyOfRange(rawArray, i, i + 8000);
                    tensorAudio.load(resampleTo16k(temp));
                    List<Classifications> output = audioClassifier.classify(tensorAudio);
                    List<Category> filteredCategory =
                            output.get(0).getCategories().stream()
                                    .filter(it -> it.getScore() > 0.3f)
                                    .collect(Collectors.toList());
//                String outputLabel = filteredCategory.get(0).getLabel();

                    buff.write(i+" : ");
                    buff.write(filteredCategory.toString());
                    buff.newLine();
                    GIS_Log.d(TAG, filteredCategory.toString());
                }
                buff.flush();
                buff.close();
            }
        }catch (IOException e){
            GIS_Log.e(TAG, "resampleCutTest: " + e);
        }
    }
}
