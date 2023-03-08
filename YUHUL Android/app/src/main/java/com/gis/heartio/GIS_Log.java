package com.gis.heartio;

import android.annotation.SuppressLint;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

public class GIS_Log {

    @SuppressLint("SimpleDateFormat")
    @SuppressWarnings("SameParameterValue")
    public static void Leslie_Log(String pre,double[] in){
        try{
            String exportDirPath = "/storage/emulated/0/Android/data/com.gis.heartio/files/Documents/GISTestLog";
            SimpleDateFormat df;
            df = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String strDate = df.format(new Date());
            String filename = exportDirPath + "/Leslie_Test_" + pre + "_" + strDate +".txt";

            FileWriter fw = new FileWriter(filename, false);
            BufferedWriter bw = new BufferedWriter(fw);
            for (double temp : in){
                bw.write(String.valueOf(temp) + ",");
                bw.newLine();
            }
            bw.close();
        }catch(IOException e){
            e.printStackTrace();
        }

    }

    @SuppressLint("SimpleDateFormat")
    @SuppressWarnings("SameParameterValue")
    public static void Leslie_Log(String pre,int[] in){
        try{
            String exportDirPath = "/storage/emulated/0/Android/data/com.gis.heartio/files/Documents/GISTestLog";
            SimpleDateFormat df;
            df = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String strDate = df.format(new Date());
            String filename = exportDirPath + "/Leslie_Test_" + pre + "_" + strDate +".txt";

            FileWriter fw = new FileWriter(filename, false);
            BufferedWriter bw = new BufferedWriter(fw);
            for (int temp : in){
                bw.write(String.valueOf(temp) + ",");
                bw.newLine();
            }
            bw.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @SuppressLint("SimpleDateFormat")
    @SuppressWarnings("SameParameterValue")
    public static void Leslie_Log(String pre,byte[] in){
        try{
            String exportDirPath = "/storage/emulated/0/Android/data/com.gis.heartio/files/Documents/GISTestLog";
            SimpleDateFormat df;
            df = new SimpleDateFormat("yyyyMMdd_HHmm");
            String strDate = df.format(new Date());
            String filename = exportDirPath + "/Leslie_Test_" + pre + "_" + strDate +".txt";

            FileWriter fw = new FileWriter(filename, true);
            BufferedWriter bw = new BufferedWriter(fw);
            for (byte temp : in){
                bw.write(String.valueOf(temp) + ",");
                bw.newLine();
            }
            bw.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @SuppressLint("SimpleDateFormat")
    @SuppressWarnings("SameParameterValue")
    public static void Leslie_Log(String pre,short[] in){
        try{
            String exportDirPath = "/storage/emulated/0/Android/data/com.gis.heartio/files/Documents/Doris";
            SimpleDateFormat df;
            df = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String strDate = df.format(new Date());
            String filename = exportDirPath + "/Leslie_Test_" + pre + "_" + strDate +".txt";

            FileWriter fw = new FileWriter(filename, false);
            BufferedWriter bw = new BufferedWriter(fw);
            for (short temp : in){
                bw.write(String.valueOf(temp) + ",");
                bw.newLine();
            }
//            for (int i=0; i<in.length; i++){
//                bw.write(i + ": " + String.valueOf(in[i]) + ",");
//                bw.newLine();
//            }
            bw.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @SuppressLint("SimpleDateFormat")
    @SuppressWarnings("SameParameterValue")
    public static void Leslie_Log(String pre,double[][] in){
        try{
            String exportDirPath = "/storage/emulated/0/Android/data/com.gis.heartio/files/Documents/GISTestLog";
            SimpleDateFormat df;
            df = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String strDate = df.format(new Date());
            int count = 0;
            for (double[] temp : in){
                String filename = exportDirPath + "/Leslie_Test_" + pre + "(" + count++ + ")_" + strDate +".txt";
                FileWriter fw = new FileWriter(filename, false);
                BufferedWriter bw = new BufferedWriter(fw);
                for (double v : temp) {
                    bw.write(v + ",");
                }
                bw.newLine();
                bw.close();
            }
            File file = new File(exportDirPath + "/Leslie_Test_" + pre + "_" + strDate +".txt");
            for(int i = 0 ; i < count ; i++){
                String filenameTemp = exportDirPath + "/Leslie_Test_" + pre + "(" + i + ")_" + strDate +".txt";
                FileOutputStream fileOutputStream = new FileOutputStream(file,true);
                FileInputStream fileInputStream = new FileInputStream(filenameTemp);
                FileChannel fileInputStreamChannel = fileInputStream.getChannel();
                FileChannel fileoutputStreamChannel = fileOutputStream.getChannel();
                fileInputStreamChannel.transferTo(0, fileInputStreamChannel.size(),fileoutputStreamChannel);
                fileoutputStreamChannel.close();
                fileInputStreamChannel.close();
                fileOutputStream.flush();
                fileOutputStream.close();
                fileInputStream.close();
                File fileTemp = new File(filenameTemp);
                boolean delete = fileTemp.delete();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void Leslie_Log(String pre, byte[] in,String pre2, short[]in2){
        try{
            String strFile = "/storage/emulated/0/Android/data/com.gis.heartio/files/Documents/GISTestLog/";
            String file1 = strFile + pre + ".txt";

            File file= new File(file1);
            Objects.requireNonNull(file.getParentFile()).mkdirs();
/*
            String shortToString = new String(in, StandardCharsets.UTF_8);
            OutputStreamWriter oStreamWriter = new OutputStreamWriter(new FileOutputStream(file), "utf-8");
            oStreamWriter.append(shortToString);
            oStreamWriter.close();*/
            //-------------------------------
            String file2 = strFile + pre2 + ".txt";
            file = new File(file2);

            String shortToString2 = Arrays.toString(in2);
            OutputStreamWriter oStreamWriter2 = new OutputStreamWriter(new FileOutputStream(file), "utf-8");
            oStreamWriter2.append(shortToString2);
            oStreamWriter2.close();
        }catch (Exception ex){
            ex.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugStr("storeByteDataToWaveFile.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex.toString(),"");
        }
    }

    public static void e(String tag, String msg){
        if(GIS_SystemConfig.logEnable){
            Log.e(tag,msg);
        }
    }

    public static void d(String tag, String msg){
        if(GIS_SystemConfig.logEnable){
            Log.d(tag,msg);
        }
    }

    public static void w(String tag, String msg){
        if(GIS_SystemConfig.logEnable){
            Log.w(tag,msg);
        }
    }


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
            Log.d("length: ", String.valueOf(temp.length));

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
            String exportDirPath = "/storage/emulated/0/Android/data/com.gis.heartio/files/Documents/Doris";
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
            Log.d("length: ", String.valueOf(temp.length));
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
}
