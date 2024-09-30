package com.gis.heartio.SignalProcessSubsystem.SupportSubsystem;

import static android.content.Context.CONNECTIVITY_SERVICE;

import static com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.SystemConfig.cloudToken;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by Cavin on 2018/1/2.
 */

public class Utilitys {
    public static int byteArrayToIntLittleEndian(byte[] b)
    {
        if (b.length==2) {
            return b[1] & 0xFF |
                    (b[0] & 0xFF) << 8;
        }else {
            return (b[0] >> 3) & 0x07;
        }
    }

    public static void swapLittleToBigEndian(byte[] bytesLittleEndian, byte[] bytesBigEndian, int iLength)
    {
        int iVar, iSampleLength, iDataLength;
        short shortValue;
        int iValue;
        byte[] byteConvertToInt;

        byteConvertToInt = new byte[2];

        iSampleLength = iLength/2;
        for(iVar = 0 ; iVar < iSampleLength ; iVar++){
            byteConvertToInt[1] = bytesLittleEndian[iVar *2];

            bytesBigEndian[iVar *2 +1] = bytesLittleEndian[iVar *2];
            bytesBigEndian[iVar *2 ] = bytesLittleEndian[iVar *2+1];
        }

        iDataLength = bytesLittleEndian.length;
        for(iVar = iLength ; iVar < iDataLength ; iVar++) {
            bytesBigEndian[iVar] = bytesLittleEndian[iVar];   // Seq
        }

    }

    public static void swapLittleToBigEndian_2(byte[] bytesLittleEndian, byte[] bytesBigEndian, int iLength)
    {
        int iVar, iSampleLength, iDataLength;

        iSampleLength = iLength/2;
        for(iVar = 0 ; iVar < iSampleLength ; iVar++){
            bytesBigEndian[iVar *2 +1] = bytesLittleEndian[iVar *2];
            bytesBigEndian[iVar *2 ] = bytesLittleEndian[iVar *2+1];
        }

        iDataLength = bytesLittleEndian.length;
        for(iVar = iLength ; iVar < iDataLength ; iVar++) {
            bytesBigEndian[iVar] = bytesLittleEndian[iVar];   // Seq
        }

    }

    public static int byteArrayToIntForWAVDataBigEndian(byte[] b) {
        int iData;

        try {
            if (b.length == 4) {
                iData = (b[0] & 0xFF) | ((b[1] & 0xFF) << 8) | ((b[2] & 0xFF) << 16) | ((b[3] & 0xFF) << 24);
            } else if (b.length == 2) {
                iData = (b[0] & 0xFF) | ((b[1] & 0xFF) << 8);
            } else {
                iData = (b[0] & 0xFF);
            }
            //return (iData>>6);
            return iData;
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("RawDataProc.byteArrayToIntBE.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            return 0;
        }
    }

    public static int byteArrayToIntForWAVDataBigEndian2(byte[] b) {
        short[] shorts = new short[b.length / 2];
        // to turn bytes to shorts as either big endian or little endian.
        ByteBuffer.wrap(b).order(ByteOrder.BIG_ENDIAN).asShortBuffer().get(shorts);
        return (int)shorts[0];
    }

    public static int byteArrayToIntForWAVDataLittleEndian(byte[] b) {
        int iData;

        try {
            if (b.length == 4) {
                iData = (b[3] & 0xFF) | ((b[2] & 0xFF) << 8) | ((b[1] & 0xFF) << 16) | ((b[0] & 0xFF) << 24);
            } else if (b.length == 2) {
                iData = (b[1] & 0xFF) | ((b[0] & 0xFF) << 8);
            } else {
                iData = (b[0] & 0xFF);
            }
            //return (iData>>6);
            return iData;
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("RawDataProc.byteArrayToIntLE.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            return 0;
        }
    }

    public static void intToByteArrayBigEndian(int iVar,  byte[] byteArrayVar) {

        try {
            byteArrayVar[0] = (byte) (iVar & 0x000000FF);
            byteArrayVar[1] = (byte) ((iVar & 0x0000FF00) >> 8);
            if (byteArrayVar.length == 4) {
                byteArrayVar[2] = (byte) ((iVar & 0x00FF0000) >> 16);
                byteArrayVar[3] = (byte) ((iVar & 0xFF000000) >> 24);
            }
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("RawDataProc.intToByteArrayBE.Exception","");
           // SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            ex1.printStackTrace();
        }
    }

    public static int byteArrayToIntForWAVParamLittleEndian2(byte[] b)
    {
        if (b.length==4) {
            return b[3] & 0xFF |
                    (b[2] & 0xFF) << 8 |
                    (b[1] & 0xFF) << 16 |
                    (b[0] & 0xFF) << 24;
        }else if (b.length==2) {
            return b[1] & 0xFF |
                    (b[0] & 0xFF) << 8;
        }else {
            return b[0] & 0xFF ;
        }
    }


    public static int byteArrayToIntBigEndian(byte[] b)
    {
        if (b.length==2) {
            return b[0] & 0xFF |
                    (b[1] & 0xFF) << 8;
        }else {
            return b[0] & 0xFF;
        }
    }

    public static void byteArrayToByteArrayWithGain(byte[] b, byte[] bg)
    {
        int iValue;

        iValue = (b[0] & 0xFF) | ((b[1] & 0xFF) << 8);
        iValue = iValue + (SystemConfig.mIntGainLevel << 12);

        bg[0] = (byte) (iValue & 0x000000FF);
        bg[1] = (byte) ((iValue & 0x0000FF00) >> 8);
    }

    public static int getGainFrombyteArray(byte[] b)
    {
        return b[0] & 0x07;
    }

    public static int getPowerFrombyteArray(byte[] b)
    {
        return (b[0] >> 3) & 0x07;
    }

    public static String getUserBaseFilePath(Activity mActivity, String inputUserID){
        String mStrBaseFolder;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //    mStrBaseFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
            mStrBaseFolder = mActivity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
        } else {
            mStrBaseFolder = mActivity.getFilesDir().getAbsolutePath();
        }

        mStrBaseFolder += File.separator + inputUserID;

        return mStrBaseFolder;
    }

    public static String getAppDataFilePath(Activity mActivity){
        String mStrBaseFolder;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            mStrBaseFolder = mActivity.getExternalFilesDir(null).getAbsolutePath();
        } else {
            mStrBaseFolder = mActivity.getFilesDir().getAbsolutePath();
        }

        return mStrBaseFolder;
    }

    public static String getDownloadBaseFilePath(Activity mActivity){
        String mStrBaseFolder;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            mStrBaseFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
//            mStrBaseFolder = mActivity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
        } else {
            mStrBaseFolder = mActivity.getFilesDir().getAbsolutePath();
        }
        return mStrBaseFolder;
    }

    public static int byteToInt ( byte num ) {
        return ((int)num & 0x000000FF);
    }

//    public static int swapLittleNBig(int inputNum){
//        int item = (inputNum & 0x000000FF) << 8 ;
//        return (item | ((inputNum >> 8 ) & 0x000000FF));
////        return inputNum;  // Do nothing!
//    }

    /* 判斷裝置是否連網&取得token值 2024/07/22 by Doris */
    public static boolean isNetOnline(Activity mActivity) {
        ConnectivityManager connMgr = (ConnectivityManager) mActivity.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        // get API token

        return (networkInfo != null && networkInfo.isConnected());
    }
    public static void getToken(Activity mActivity){
        String TAG = "isNetOnline";
        if (cloudToken==null){
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, "{\r\n    \"username\": \"mobile\",\r\n    \"password\": \"mobile@123\"\r\n}");

            Request request = new Request.Builder()
                    .url("http://139.196.4.88:8081/beplus/sys/mLogin")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            // 建立Call
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response){
                    //連線成功，取得回傳response
                    try {
//                        assert response.body() != null;
                        String result = response.body().string();
//                        Log.i(TAG, "result = "+result);

                        JSONObject jObject = new JSONObject(result);
                        String status = jObject.getString("success");
                        if (status.equals("true")){
                            JSONObject tokenResult = jObject.getJSONObject("result");
                            cloudToken = tokenResult.getString("token");
                        }else {
//                            Toast.makeText(mActivity, "伺服器連線異常", Toast.LENGTH_SHORT).show();
                        }
//                        Log.d(TAG, "success = "+status);
//                        Log.d(TAG, "token = "+cloudToken);
                    } catch (IOException | JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    //當連線失敗
                    throw new RuntimeException(e);
                }
            });
        }else {
            Log.d("isNetOnline: ", cloudToken);
        }
    }
}
