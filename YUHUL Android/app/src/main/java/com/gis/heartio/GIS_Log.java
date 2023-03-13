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
}
