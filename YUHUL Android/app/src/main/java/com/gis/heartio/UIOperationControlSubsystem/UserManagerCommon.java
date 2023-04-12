package com.gis.heartio.UIOperationControlSubsystem;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.gis.heartio.GIS_Log;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.DataBaseUserInfo;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.IwuSQLHelper;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.userInfo;

/**
 * Created by brandon on 2017/8/25.
 package tw.org.itri.ultrasound;

 import android.app.Fragment;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.support.v4.app.FragmentActivity;

 /**
 * Created by 780797 on 2016/7/5.
 */
public class UserManagerCommon{
    private static final String TAG = "UserMangerCommon";
    private static Cursor mCursor;

    public static final String STR_USER_HEIGHT_TO_CSA_YES = "YES";
    public static final String STR_USER_HEIGHT_TO_CSA_NO = "NO";
    public static int INT_USER_HEIGHT_TO_CSA_YES = 0;
    public static int INT_USER_HEIGHT_TO_CSA_NO = 1;
    public static int INT_USER_HEIGHT_TO_CSA_DEFAULT = 0;
    public static double mDoubleUserPulmDiameterCm;
    public static double mDoubleUserAngleRadius, mDoubleCosineUserAngle;
    public static String mStrUserInfo;
    //public static DataBaseUserInfo mUserInfoCur = new DataBaseUserInfo();
    public static userInfo mUserInfoCur = new userInfo();
    public static boolean mBoolUserSelected;

    public static void storeEditUserInfo(){
        try {
            ContentValues values = new ContentValues();

            /*values.put(IwuSQLHelper.KEY_USER_ANGLE, UserManagerCommon.mUserInfoCur.mIntAngle);
            values.put(IwuSQLHelper.KEY_USER_PULM_DIAMETER, UserManagerCommon.mUserInfoCur.mIntPulmDiameter);
            values.put(IwuSQLHelper.KEY_USER_HEIGHT, UserManagerCommon.mUserInfoCur.mIntHeight);
            values.put(IwuSQLHelper.KEY_USER_PRIMARY, UserManagerCommon.mUserInfoCur.mIntUserID);
            values.put(IwuSQLHelper.KEY_USER_HEIGHT_TO_CSA, UserManagerCommon.mUserInfoCur.mStrHeightToCSA);
            values.put(IwuSQLHelper.KEY_USER_FIRST_NAME, UserManagerCommon.mUserInfoCur.mStrFirstName);
            values.put(IwuSQLHelper.KEY_USER_LAST_NAME, UserManagerCommon.mUserInfoCur.mStrLastName);
            values.put(IwuSQLHelper.KEY_USER_ID_NUMBER, UserManagerCommon.mUserInfoCur.mStrUserIDNumber);

            MainActivity.mIwuSQLHelper.mDBWrite.update(IwuSQLHelper.STR_TABLE_USER, values, "_id=" + UserManagerCommon.mUserInfoCur.mIntUserID, null);*/
            values.put(IwuSQLHelper.KEY_USER_ANGLE, UserManagerCommon.mUserInfoCur.angle);
            values.put(IwuSQLHelper.KEY_USER_PULM_DIAMETER, UserManagerCommon.mUserInfoCur.pulmDiameter);
            values.put(IwuSQLHelper.KEY_USER_HEIGHT, UserManagerCommon.mUserInfoCur.height);
            values.put(IwuSQLHelper.KEY_USER_PRIMARY, UserManagerCommon.mUserInfoCur.userCount);
            values.put(IwuSQLHelper.KEY_USER_HEIGHT_TO_CSA, UserManagerCommon.mUserInfoCur.heightToCSA);
            values.put(IwuSQLHelper.KEY_USER_FIRST_NAME, UserManagerCommon.mUserInfoCur.firstName);
            values.put(IwuSQLHelper.KEY_USER_LAST_NAME, UserManagerCommon.mUserInfoCur.lastName);
            values.put(IwuSQLHelper.KEY_USER_ID_NUMBER, UserManagerCommon.mUserInfoCur.userID);

            MainActivity.mIwuSQLHelper.mDBWrite.update(IwuSQLHelper.STR_TABLE_USER, values, "_id=" + UserManagerCommon.mUserInfoCur.userCount, null);
        }catch(Exception ex1){
           // SystemConfig.mMyEventLogger.appendDebugStr("storeEditUserInfo.Exception","");
        }

    }

    public static void storeAddUserInfo(){

        try {
            ContentValues values = new ContentValues();

            /*values.put(IwuSQLHelper.KEY_USER_ANGLE, UserManagerCommon.mUserInfoCur.mIntAngle);
            values.put(IwuSQLHelper.KEY_USER_PULM_DIAMETER, UserManagerCommon.mUserInfoCur.mIntPulmDiameter);
            values.put(IwuSQLHelper.KEY_USER_HEIGHT, UserManagerCommon.mUserInfoCur.mIntHeight);
            values.put(IwuSQLHelper.KEY_USER_PRIMARY, UserManagerCommon.mUserInfoCur.mIntUserID);
            values.put(IwuSQLHelper.KEY_USER_HEIGHT_TO_CSA, UserManagerCommon.mUserInfoCur.mStrHeightToCSA);
            values.put(IwuSQLHelper.KEY_USER_FIRST_NAME, UserManagerCommon.mUserInfoCur.mStrFirstName);
            values.put(IwuSQLHelper.KEY_USER_LAST_NAME, UserManagerCommon.mUserInfoCur.mStrLastName);
            values.put(IwuSQLHelper.KEY_USER_ID_NUMBER, UserManagerCommon.mUserInfoCur.mStrUserIDNumber);*/
            values.put(IwuSQLHelper.KEY_USER_ANGLE, UserManagerCommon.mUserInfoCur.angle);
            values.put(IwuSQLHelper.KEY_USER_PULM_DIAMETER, UserManagerCommon.mUserInfoCur.pulmDiameter);
            values.put(IwuSQLHelper.KEY_USER_HEIGHT, UserManagerCommon.mUserInfoCur.height);
            values.put(IwuSQLHelper.KEY_USER_PRIMARY, UserManagerCommon.mUserInfoCur.userCount);
            values.put(IwuSQLHelper.KEY_USER_HEIGHT_TO_CSA, UserManagerCommon.mUserInfoCur.heightToCSA);
            values.put(IwuSQLHelper.KEY_USER_FIRST_NAME, UserManagerCommon.mUserInfoCur.firstName);
            values.put(IwuSQLHelper.KEY_USER_LAST_NAME, UserManagerCommon.mUserInfoCur.lastName);
            values.put(IwuSQLHelper.KEY_USER_ID_NUMBER, UserManagerCommon.mUserInfoCur.userID);

            MainActivity.mIwuSQLHelper.mDBWrite.insert(IwuSQLHelper.STR_TABLE_USER, null, values);
        }catch(Exception ex1){
            ex1.printStackTrace();
           // SystemConfig.mMyEventLogger.appendDebugStr("storeEditUserInfo.Exception","");
        }
    }

    public static double getPulmDiameterFromHeight(int iHeight){
        double doublePulmDiameter;

        doublePulmDiameter = (double)iHeight * 0.0106 + 0.265;
        return doublePulmDiameter;
    }

    public static DataBaseUserInfo getUserUltrasoundParameterFromID(int iUserID) {
        String[] columns, userIds;
        int iPulmDiaValue, iIdx;
        String string;
        DataBaseUserInfo userInfo;

        userInfo = new DataBaseUserInfo();
        userInfo.mIntUserID = iUserID;

        try {
            userIds = new String[1];
            userIds[0] = String.valueOf(iUserID);
            columns = new String[]{IwuSQLHelper.KEY_USER_PRIMARY, IwuSQLHelper.KEY_USER_ID_NUMBER, IwuSQLHelper.KEY_USER_FIRST_NAME, IwuSQLHelper.KEY_USER_LAST_NAME
                    , IwuSQLHelper.KEY_USER_HEIGHT_TO_CSA, IwuSQLHelper.KEY_USER_HEIGHT, IwuSQLHelper.KEY_USER_PULM_DIAMETER
                    , IwuSQLHelper.KEY_USER_ANGLE};
            mCursor = MainActivity.mIwuSQLHelper.mDBWrite.query(IwuSQLHelper.STR_TABLE_USER, columns, IwuSQLHelper.KEY_USER_PRIMARY, userIds, null, null, null);

            iIdx = mCursor.getColumnIndex(IwuSQLHelper.KEY_USER_ID_NUMBER);
            userInfo.mStrUserIDNumber = mCursor.getString(iIdx);
            iIdx = mCursor.getColumnIndex(IwuSQLHelper.KEY_USER_LAST_NAME);
            userInfo.mStrLastName = mCursor.getString(iIdx);
            iIdx = mCursor.getColumnIndex(IwuSQLHelper.KEY_USER_FIRST_NAME);
            userInfo.mStrFirstName = mCursor.getString(iIdx);
            iIdx = mCursor.getColumnIndex(IwuSQLHelper.KEY_USER_HEIGHT_TO_CSA);
            userInfo.mStrHeightToCSA = mCursor.getString(iIdx);
            iIdx = mCursor.getColumnIndex(IwuSQLHelper.KEY_USER_HEIGHT);
            userInfo.mIntHeight = mCursor.getInt(iIdx);
            iIdx = mCursor.getColumnIndex(IwuSQLHelper.KEY_USER_PULM_DIAMETER);
            userInfo.mIntPulmDiameter = mCursor.getInt(iIdx);
            iIdx = mCursor.getColumnIndex(IwuSQLHelper.KEY_USER_ANGLE);
            userInfo.mIntAngle = mCursor.getInt(iIdx);
            iIdx = mCursor.getColumnIndex(IwuSQLHelper.KEY_USER_BLE_MAC);
            userInfo.mStrBleMac = mCursor.getString(iIdx);

            getUserUltrasoundParameterFromObject();

            mCursor.close();
        } catch (Exception ex1) {
            ex1.printStackTrace();
        }finally{
            return userInfo;
        }
    }

    public static void initUserUltrasoundParameter(userInfo userInfo){
        if (userInfo == null){
            userInfo = new userInfo();
            userInfo.angle = 0;
            userInfo.height = 172;
            userInfo.pulmDiameter = 25;
            userInfo.firstName = "Tester";
            userInfo.lastName = "GIS";
            userInfo.userCount = 1;
            userInfo.userID = "001";
            //userInfo.heightToCSA = STR_USER_HEIGHT_TO_CSA_YES;
            userInfo.heightToCSA = STR_USER_HEIGHT_TO_CSA_NO;
        }

        GIS_Log.d(TAG,"userInfo.firstName="+userInfo.firstName);
        GIS_Log.d(TAG,"userInfo.heightToCSA="+userInfo.heightToCSA);
        UserManagerCommon.mUserInfoCur = userInfo;
        UserManagerCommon.mDoubleUserAngleRadius = ((double) UserManagerCommon.mUserInfoCur.angle / (double) 180) * Math.PI;
        UserManagerCommon.mDoubleCosineUserAngle = Math.cos(UserManagerCommon.mDoubleUserAngleRadius);

        // TODO : User management
//        if (UserManagerCommon.mUserInfoCur.heightToCSA.equals(UserManagerCommon.STR_USER_HEIGHT_TO_CSA_NO)) {
            UserManagerCommon.mDoubleUserPulmDiameterCm = (double) UserManagerCommon.mUserInfoCur.pulmDiameter / (double) 10;
//        } else {
//            UserManagerCommon.mDoubleUserPulmDiameterCm = getPulmDiameterFromHeight(UserManagerCommon.mUserInfoCur.height);
//        }
        GIS_Log.d(TAG,"UserManagerCommon.mDoubleUserPulmDiameterCm="+UserManagerCommon.mDoubleUserPulmDiameterCm);
    }

    public static void getUserUltrasoundParameterFromObject() {
        UserManagerCommon.mDoubleUserAngleRadius = ((double) UserManagerCommon.mUserInfoCur.angle / (double) 180) * Math.PI;
        UserManagerCommon.mDoubleCosineUserAngle = Math.cos(UserManagerCommon.mDoubleUserAngleRadius);

        // TODO : User management
        /*if (UserManagerCommon.mUserInfoCur.mStrHeightToCSA.equals(UserManagerCommon.STR_USER_HEIGHT_TO_CSA_NO)) {
            UserManagerCommon.mDoubleUserPulmDiameterCm = (double) UserManagerCommon.mUserInfoCur.mIntPulmDiameter / (double) 10;
        } else {
            UserManagerCommon.mDoubleUserPulmDiameterCm = getPulmDiameterFromHeight(UserManagerCommon.mUserInfoCur.height);
        }*/
        //if (UserManagerCommon.mUserInfoCur.heightToCSA.equals(UserManagerCommon.STR_USER_HEIGHT_TO_CSA_NO)) {
            UserManagerCommon.mDoubleUserPulmDiameterCm = (double) UserManagerCommon.mUserInfoCur.pulmDiameter / (double) 10;
        //} else {
         //   UserManagerCommon.mDoubleUserPulmDiameterCm = getPulmDiameterFromHeight(UserManagerCommon.mUserInfoCur.height);
        //}
    }
}

