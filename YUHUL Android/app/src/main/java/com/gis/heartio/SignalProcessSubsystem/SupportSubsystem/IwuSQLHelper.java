package com.gis.heartio.SignalProcessSubsystem.SupportSubsystem;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.content.ContentValues;
import android.util.Log;

import com.gis.heartio.GIS_Log;
import com.gis.heartio.UIOperationControlSubsystem.UserManagerCommon;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by brandon on 2017/8/14.
 */

public class IwuSQLHelper extends SQLiteOpenHelper {
    private static final String TAG = "IwuSQLHelper";
    public static final int mIntVersion = 5;  //資料庫版本
    public static final String mStrDatabaseName = "IwuDatabase1";
    public File mFileDatabBase;
    public String mStrDataBasePath;
    public SQLiteDatabase mDBWrite;
    public SQLiteDatabase mDBRead;

    public static final String STR_TABLE_ADMIN = "tableAdmin";
    public static final String KEY_ADMIN_PRIMARY = "_id";
    public static final String KEY_ADMIN_ID = "Identify";
    public static final String KEY_ADMIN_PW = "Password";

    public static final String STR_TABLE_USER = "tableUser";
    public static final String KEY_USER_PRIMARY = "_id";
    public static final String KEY_USER_CREATED_DATE = "CreateDate";
    public static final String KEY_USER_ID_NUMBER = "IdentityNumber";
    public static final String KEY_USER_FIRST_NAME = "FirstName";
    public static final String KEY_USER_LAST_NAME = "LastName";
    public static final String KEY_USER_HOSPITAL = "Hospital";
    public static final String KEY_USER_NAME = "Name";
    public static final String KEY_USER_GENDER = "Gender";
    public static final String KEY_USER_PHONE_NUMBER = "phoneNumber";
    public static final String KEY_USER_BIRTHDAY = "BirthDay";
    public static final String KEY_USER_HEIGHT = "Height";
    public static final String KEY_USER_JOB = "Job";
    public static final String KEY_USER_MARRY = "Marry";
    public static final String KEY_USER_NATION = "Nation";
    public static final String KEY_USER_OPERATION = "Operation";
    public static final String KEY_USER_DISEASE = "Disease";
    public static final String KEY_USER_PULM_DIAMETER = "PulmDia";
    public static final String KEY_USER_PWORD = "PassWord";
    public static final String KEY_USER_EMAIL = "Email";
    public static final String KEY_USER_HEIGHT_TO_CSA = "HgtToCsa";
    public static final String KEY_USER_ANGLE = "Angle";
    public static final String KEY_USER_BLE_MAC = "MacAddr";
    public static final String KEY_CURRENT_USER = "Current";
    public static final String KEY_USER_AGE = "Age";

    public static final String STR_TABLE_DATA = "tableData";
    public static final String KEY_DATA_TABLE_PRIMARY = "_id";
    public static final String KEY_DATA_TABLE_RESULT = "Result";
    public static final String KEY_DATA_TABLE_USER_ID = "UserID";
    public static final String KEY_DATA_TABLE_CREATED_DATE = "CreateDate";
    public static final String KEY_DATA_TABLE_HR = "HR";
    public static final String KEY_DATA_TABLE_VPK = "Vpk";
    public static final String KEY_DATA_TABLE_VTI = "VTI";
    public static final String KEY_DATA_TABLE_SV = "SV";
    public static final String KEY_DATA_TABLE_CO = "CO";
    public static final String KEY_DATA_TABLE_FILE_NAME = "FileName";
    public static final String  KEY_DATA_TABLE_DEVICE_MAC_ADDR = "MAC_Addr";

    public static final String STR_FAIL = "fail";
    public static final String STR_SUCESS = "sucess";

    public static int mIntIndexForTest;
    public static Cursor mCursorData;
    public static Cursor mCursorUser;
    public static String[] mStrCursorQueryFieldsForData;
    public static DataBaseDataInfo mDatabaseDataInfo = new DataBaseDataInfo();
    private Context mContext;

    public  IwuSQLHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.mContext = context;
        this.mDBWrite = getWritableDatabase();
        this.mDBRead = getWritableDatabase();
    }

    public IwuSQLHelper(Context context, String name) {
        this(context, name, null,mIntVersion);
    }

    public IwuSQLHelper(Context context, String name, int version) {
        this(context, name, null, version);

    }

    public IwuSQLHelper(Context context) {
        this(context, mStrDatabaseName, null, mIntVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        int iReturn;

//        String DATABASE_CREATE_TABLE_USER =
//                "create table " + STR_TABLE_USER + "("
//                        + KEY_USER_PRIMARY + " INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL,"
//                        + KEY_USER_ID_NUMBER + " TEXT, "
//                        + KEY_USER_FIRST_NAME + " TEXT, "
//                        + KEY_USER_LAST_NAME + " TEXT, "
//                        + KEY_USER_HEIGHT_TO_CSA + " TEXT, "
//                        + KEY_USER_BLE_MAC + " TEXT, "
//                        + KEY_USER_HEIGHT + " INTEGER, "
//                        + KEY_USER_PULM_DIAMETER + " REAL, "
//                        + KEY_USER_ANGLE + " INTEGER, "
//                        + KEY_USER_GENDER + " INTEGER, "
//                        + KEY_USER_AGE + " INTEGER)";

        String DATABASE_CREATE_TABLE_USER =
                "create table " + STR_TABLE_USER + "("
                        + KEY_USER_PRIMARY + " INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL,"
                        + KEY_USER_ID_NUMBER + " TEXT, "
                        + KEY_USER_NAME + " TEXT, "
                        + KEY_USER_PHONE_NUMBER + " TEXT, "
                        + KEY_USER_BIRTHDAY + " TEXT, "
                        + KEY_USER_HOSPITAL + " TEXT, "
                        + KEY_USER_JOB + " TEXT, "
                        + KEY_USER_NATION + " TEXT, "
                        + KEY_USER_OPERATION + " TEXT, "
                        + KEY_USER_DISEASE + " TEXT, "
                        + KEY_USER_PULM_DIAMETER + " REAL, "
                        + KEY_USER_GENDER + " INTEGER, "
                        + KEY_USER_MARRY + " INTEGER)";

        db.execSQL(DATABASE_CREATE_TABLE_USER);

//        String DATABASE_CREATE_TABLE_DATA =
//                "create table " + STR_TABLE_DATA + "("
//                        + KEY_DATA_TABLE_PRIMARY + " INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL,"
//                        + KEY_DATA_TABLE_RESULT + " TEXT, "
//                        //+ KEY_DATA_TABLE_USER_ID + " INTEGER, "
//                        + KEY_DATA_TABLE_USER_ID + " TEXT, "
//                        + KEY_DATA_TABLE_CREATED_DATE + " TEXT, "
//                        + KEY_DATA_TABLE_HR + " INTEGER, "
//                        + KEY_DATA_TABLE_VPK + " REAL, "
//                        + KEY_DATA_TABLE_VTI + " REAL, "
//                        + KEY_DATA_TABLE_SV + " REAL, "
//                        + KEY_DATA_TABLE_CO + " REAL, "
//                        + KEY_DATA_TABLE_FILE_NAME + " TEXT)";
//        db.execSQL(DATABASE_CREATE_TABLE_DATA);

        String DATABASE_CREATE_TABLE_DATA =
                "create table " + STR_TABLE_DATA + "("
                        + KEY_DATA_TABLE_PRIMARY + " INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL,"
//                        + KEY_DATA_TABLE_RESULT + " TEXT, "
                        + KEY_DATA_TABLE_USER_ID + " INTEGER, "
//                        + KEY_USER_PHONE_NUMBER + " TEXT, "
                        + KEY_DATA_TABLE_CREATED_DATE + " TEXT, "
                        + KEY_DATA_TABLE_HR + " INTEGER, "
                        + KEY_DATA_TABLE_VPK + " REAL, "
                        + KEY_DATA_TABLE_VTI + " REAL, "
                        + KEY_DATA_TABLE_SV + " REAL, "
                        + KEY_DATA_TABLE_CO + " REAL, "
                        + KEY_DATA_TABLE_DEVICE_MAC_ADDR + " TEXT, "
                        + KEY_DATA_TABLE_FILE_NAME + " TEXT)";
        db.execSQL(DATABASE_CREATE_TABLE_DATA);

        String DATABASE_CREATE_TABLE_ADMIN =
                "create table " + STR_TABLE_ADMIN + "("
                    + KEY_ADMIN_PRIMARY + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                    + KEY_ADMIN_ID + " TEXT unique,"
                    + KEY_ADMIN_PW + " TEXT)";
        db.execSQL(DATABASE_CREATE_TABLE_ADMIN);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //oldVersion=舊的資料庫版本；newVersion=新的資料庫版本
        //db.execSQL("DROP TABLE IF EXISTS newMemorandum"); //刪除舊有的資料表
        //onCreate(db);
        switch (oldVersion) {
            case 1:
                //db.execSQL(SQL_MY_TABLE);
                //db.execSQL("ALTER TABLE "+STR_TABLE_USER+" ADD COLUMN "
                //        + KEY_CURRENT_USER + " TEXT");
                //break;
            case 2:
            //    db.execSQL("ALTER TABLE myTable ADD COLUMN myNewColumn TEXT");
                String DATABASE_CREATE_TABLE_ADMIN =
                        "create table " + STR_TABLE_ADMIN + "("
                                + KEY_ADMIN_PRIMARY + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                                + KEY_ADMIN_ID + " TEXT unique, "
                                + KEY_ADMIN_PW + " TEXT)";
                db.execSQL(DATABASE_CREATE_TABLE_ADMIN);
                break;
            case 3:
                String DATABASE_CREATE_TABLE_TEMP =
                        "create table tmpAdmin("
                            +KEY_ADMIN_PRIMARY + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                            +KEY_ADMIN_ID + " TEXT unique, "
                            +KEY_ADMIN_PW + " TEXT)";
                db.execSQL(DATABASE_CREATE_TABLE_TEMP);
                DATABASE_CREATE_TABLE_TEMP = "INSERT INTO tmpAdmin SELECT * FROM "+STR_TABLE_ADMIN;
                db.execSQL(DATABASE_CREATE_TABLE_TEMP);
                DATABASE_CREATE_TABLE_TEMP = "DROP TABLE "+STR_TABLE_ADMIN;
                db.execSQL(DATABASE_CREATE_TABLE_TEMP);
                DATABASE_CREATE_TABLE_TEMP = "ALTER TABLE tmpAdmin RENAME TO "+STR_TABLE_ADMIN;
                db.execSQL(DATABASE_CREATE_TABLE_TEMP);
                break;
            case 4:
                String DATABASE_ALTER_TEMP =
                        "ALTER TABLE "+STR_TABLE_USER+" ADD COLUMN "+KEY_USER_GENDER+" INTEGER DEFAULT 0";
                db.execSQL(DATABASE_ALTER_TEMP);
                DATABASE_ALTER_TEMP =
                        "ALTER TABLE "+STR_TABLE_USER+" ADD COLUMN "+KEY_USER_AGE+" INTEGER DEFAULT 0";
                db.execSQL(DATABASE_ALTER_TEMP);
                DATABASE_ALTER_TEMP =
                        "CREATE TABLE TempTable("
                                + KEY_USER_PRIMARY + " INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL,"
                                + KEY_USER_ID_NUMBER + " TEXT, "
                                + KEY_USER_NAME + " TEXT, "
                                + KEY_USER_PHONE_NUMBER + " TEXT, "
                                + KEY_USER_BIRTHDAY + " TEXT, "
                                + KEY_USER_HOSPITAL + " TEXT, "
                                + KEY_USER_JOB + " TEXT, "
                                + KEY_USER_NATION + " TEXT, "
                                + KEY_USER_OPERATION + " TEXT, "
                                + KEY_USER_DISEASE + " TEXT, "
                                + KEY_USER_PULM_DIAMETER + " REAL, "
                                + KEY_USER_GENDER + " INTEGER, "
                                + KEY_USER_MARRY + " INTEGER)";
                db.execSQL(DATABASE_ALTER_TEMP);
                DATABASE_ALTER_TEMP = "INSERT INTO TempTable SELECT * FROM "+STR_TABLE_USER;
                db.execSQL(DATABASE_ALTER_TEMP);
                DATABASE_ALTER_TEMP = "DROP TABLE "+STR_TABLE_USER;
                db.execSQL(DATABASE_ALTER_TEMP);
                DATABASE_ALTER_TEMP = "ALTER TABLE TempTable RENAME TO "+STR_TABLE_USER;
                db.execSQL(DATABASE_ALTER_TEMP);
                break;
        }
    }

    // 每次打開歷史紀錄後都會執行
    @Override
    public void onOpen(SQLiteDatabase db) {
        boolean boolExist1, boolExist2;
//        Log.d(TAG, "onOpen");

        super.onOpen(db);

        // TODO 每次成功打開數據庫後首先被執行

        mFileDatabBase = mContext.getDatabasePath(mStrDatabaseName);
        mStrDataBasePath = mFileDatabBase.getAbsolutePath();

        try {
            //boolExist1 = tableExists(db, STR_TABLE_USER);
            //boolExist2 = tableExists(db, STR_TABLE_DATA);

            //boolExist1 = isFieldExist(db, STR_TABLE_USER, KEY_USER_BLE_MAC);
            //boolExist2 = isFieldExist(db, "tableData", KEY_DATA_TABLE_CREATED_DATE);

            getAllFieldsFromTable(db, STR_TABLE_USER);
            getAllFieldsFromTable(db, STR_TABLE_DATA);
            boolExist2 = true;
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("IwuSQLHelper.onOpen.Exception()","");
           // SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            ex1.printStackTrace();
        }

    }

    @Override
    public synchronized void close() {
        super.close();
    }

    public void closeDatabase(){

        mDBWrite.close();
        mDBRead.close();
    }

    // For HIOT 20180707
   public void tryAddTestUser(){
        Cursor cursor;
        boolean boolTesterExist, isGISTesterExist;
        int iCnt;

        cursor = mDBWrite.rawQuery("select " + KEY_USER_PRIMARY + " FROM " + STR_TABLE_USER, null);

        iCnt = cursor.getCount();
        if(iCnt == 0){
            boolTesterExist = false;
        }else{
            boolTesterExist = true;
            cursor.moveToFirst();
        }
        cursor.close();
       isGISTesterExist = (iCnt>=19) ?true:false;

       if (!isGISTesterExist){
           addGISTesterToDatabase();
           cursor = mDBWrite.rawQuery("select " + KEY_USER_PRIMARY + " FROM " + STR_TABLE_USER, null);
           iCnt = cursor.getCount();
           cursor.moveToFirst();
           cursor.close();
       }

        /*if(!boolTesterExist){
            addUserTesterToDatabase();
            cursor = mDBWrite.rawQuery("select " + KEY_USER_PRIMARY + " FROM " + STR_TABLE_USER, null);
            iCnt = cursor.getCount();
            cursor.moveToFirst();
            cursor.close();
        }*/
    }



    /*public void addUserTesterToDatabase(){
        DataBaseUserInfo userInfo1, userInfo2;

        userInfo1 = new DataBaseUserInfo();
        userInfo1.mStrUserIDNumber = "0000000000";
        userInfo1.mStrFirstName = "Tester";
        userInfo1.mStrLastName = "Tester";
        userInfo1.mIntHeight = 170;
        userInfo1.mIntPulmDiameter = 250;
        userInfo1.mIntAngle = 0;
        userInfo1.mStrHeightToCSA = "YES";
        userInfo1.mStrBleMac = "00";

        addUserToDataBase(userInfo1);
    }*/

    // 實際沒用到
    public void addGISTesterToDatabase(){
        userInfo userInfo1;
        //String[] userIDs=   {"Masha", "Amanda","奕仁","Una","Annie","Doris","Kay friend","Chiver","Darren","Serena","Paggy","Cavin", "Hugo","JF", "Jimmy", "旻哲", "Kay", "Rosa"};
        String[] firstNames={"Masha","Amanda","奕仁","Una","Annie","Doris","Kay friend","Chiver","Darren","Serena","Paggy","Cavin", "Hugo","JF", "Jimmy","Emma", "旻哲", "Kay", "Rosa"};
        String[] lastNames= {"Masha", "Amanda","奕仁","Lin","Annie","Doris",  "friend",  "Wang", "Darren","Serena","Paggy","Su", "Liu"    ,"Li", "Jimmy","Chen"  , "Li",   "Lin",  "Wu"};
        int[] heights=       {162,       168,    165,   161,  163   ,165,     162,    174,     173,     170,    160,  172 ,  171,    166,   172,   177    ,168,    163,   161};
       // int[] pulmDia=       {22,         21,    20,    21,    21,   23,       21,     19,    22,    20,   17,    22,    19};


        for (int i=0;i<firstNames.length;i++){
            userInfo1 = new userInfo();
            userInfo1.userID = String.valueOf(i+1);
            userInfo1.firstName = firstNames[i];
            userInfo1.lastName = lastNames[i];
            userInfo1.height = heights[i];
           // userInfo1.mIntPulmDiameter = pulmDia[i];
            userInfo1.pulmDiameter = 20;
            userInfo1.angle = 0;
            userInfo1.heightToCSA = UserManagerCommon.STR_USER_HEIGHT_TO_CSA_NO;
            //userInfo1.mStrBleMac = UserManagerCommon.STR_USER_MAC_ADDR_ALL;

            addUserToDataBase(userInfo1);
        }


    }


    // 實際沒用到
    public void addUserToDataBase(userInfo userInfo) {
        long lnReturn;
        try {
            ContentValues cv = new ContentValues();
            cv.put(IwuSQLHelper.KEY_USER_ID_NUMBER, userInfo.userID);
            cv.put(IwuSQLHelper.KEY_USER_FIRST_NAME, userInfo.firstName);
            cv.put(IwuSQLHelper.KEY_USER_LAST_NAME, userInfo.lastName);
            cv.put(IwuSQLHelper.KEY_USER_HEIGHT, userInfo.height);
            cv.put(IwuSQLHelper.KEY_USER_PULM_DIAMETER, userInfo.pulmDiameter);
            cv.put(IwuSQLHelper.KEY_USER_HEIGHT_TO_CSA, userInfo.heightToCSA);
            cv.put(IwuSQLHelper.KEY_USER_ANGLE, userInfo.angle);
            cv.put(IwuSQLHelper.KEY_USER_BLE_MAC, userInfo.bleMac);
            cv.put(IwuSQLHelper.KEY_USER_GENDER, userInfo.gender);
            cv.put(IwuSQLHelper.KEY_USER_AGE, userInfo.age);

            lnReturn = mDBWrite.insert(IwuSQLHelper.STR_TABLE_USER, "", cv);
            userInfo.userCount = (int)lnReturn;

        }catch(Exception ex1){
            ex1.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugStr("addUserToDataBase.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }

    public boolean addDataInfo(dataInfo measureInfo){
        ContentValues cv1;
        long lnReturn;

        try {
            cv1 = new ContentValues();
            cv1.put(IwuSQLHelper.KEY_DATA_TABLE_USER_ID, measureInfo.userId);
            cv1.put(IwuSQLHelper.KEY_DATA_TABLE_CREATED_DATE, measureInfo.createdDate);
            cv1.put(IwuSQLHelper.KEY_DATA_TABLE_HR, measureInfo.HR);
            cv1.put(IwuSQLHelper.KEY_DATA_TABLE_RESULT, measureInfo.result);
            cv1.put(IwuSQLHelper.KEY_DATA_TABLE_VPK, measureInfo.Vpk);
            cv1.put(IwuSQLHelper.KEY_DATA_TABLE_VTI, measureInfo.VTI);
            cv1.put(IwuSQLHelper.KEY_DATA_TABLE_SV, measureInfo.SV);
            cv1.put(IwuSQLHelper.KEY_DATA_TABLE_CO, measureInfo.CO);

            // SystemConfig.mIwuSQLHelper.mDBWrite.beginTransaction();
            mDBWrite.beginTransaction();
            try {
                //lnReturn = SystemConfig.mIwuSQLHelper.mDBWrite.insert(STR_TABLE_DATA, "", cv1);
                //SystemConfig.mIwuSQLHelper.mDBWrite.setTransactionSuccessful();
                lnReturn = mDBWrite.insert(STR_TABLE_DATA, "", cv1);
                mDBWrite.setTransactionSuccessful();
            }catch(Exception ex2){
                ex2.printStackTrace();
            }finally{
                //SystemConfig.mIwuSQLHelper.mDBWrite.endTransaction();
                mDBWrite.endTransaction();
            }

            return true;
        }catch(Exception ex1){
            ex1.printStackTrace();
            return false;
        }
    }

    // 新增 data 到 SQLite 資料庫
    public long addDataInfoToDB(dataInfo measureInfo){
        ContentValues cv1;
        long lnReturn = -1;

        try {
            cv1 = new ContentValues();
            cv1.put(IwuSQLHelper.KEY_DATA_TABLE_USER_ID, measureInfo.userId);
            cv1.put(IwuSQLHelper.KEY_DATA_TABLE_DEVICE_MAC_ADDR, measureInfo.macAddress);
            cv1.put(IwuSQLHelper.KEY_DATA_TABLE_CREATED_DATE, measureInfo.createdDate);
            cv1.put(IwuSQLHelper.KEY_DATA_TABLE_HR, measureInfo.HR);
//            cv1.put(IwuSQLHelper.KEY_DATA_TABLE_RESULT, measureInfo.result);
            cv1.put(IwuSQLHelper.KEY_DATA_TABLE_VPK, measureInfo.Vpk);
            cv1.put(IwuSQLHelper.KEY_DATA_TABLE_VTI, measureInfo.VTI);
            cv1.put(IwuSQLHelper.KEY_DATA_TABLE_SV, measureInfo.SV);
            cv1.put(IwuSQLHelper.KEY_DATA_TABLE_CO, measureInfo.CO);
            cv1.put(IwuSQLHelper.KEY_DATA_TABLE_FILE_NAME, measureInfo.fileName);

            // SystemConfig.mIwuSQLHelper.mDBWrite.beginTransaction();
            mDBWrite.beginTransaction();
            try {
                //lnReturn = SystemConfig.mIwuSQLHelper.mDBWrite.insert(STR_TABLE_DATA, "", cv1);
                //SystemConfig.mIwuSQLHelper.mDBWrite.setTransactionSuccessful();
                lnReturn = mDBWrite.insert(STR_TABLE_DATA, "", cv1);
                mDBWrite.setTransactionSuccessful();
            }catch(Exception ex2){
                ex2.printStackTrace();
            }finally{
                mDBWrite.endTransaction();
            }
        }catch(Exception ex1){
            ex1.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugStr("addDataInfoToDB.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
        return lnReturn;
    }


    public boolean addDataInfoToDB(DataBaseDataInfo measureInfo){
        ContentValues cv1;
        long lnReturn;

        try {
            cv1 = new ContentValues();
            cv1.put(IwuSQLHelper.KEY_DATA_TABLE_USER_ID, measureInfo.mIntUserId);
            cv1.put(IwuSQLHelper.KEY_DATA_TABLE_CREATED_DATE, measureInfo.mStrCreatedDate);
            cv1.put(IwuSQLHelper.KEY_DATA_TABLE_HR, measureInfo.mIntHR);
            cv1.put(IwuSQLHelper.KEY_DATA_TABLE_RESULT, measureInfo.mStrResult);
            cv1.put(IwuSQLHelper.KEY_DATA_TABLE_VPK, measureInfo.mDoubleVpk);
            cv1.put(IwuSQLHelper.KEY_DATA_TABLE_VTI, measureInfo.mDoubleVTI);
            cv1.put(IwuSQLHelper.KEY_DATA_TABLE_SV, measureInfo.mDoubleSV);
            cv1.put(IwuSQLHelper.KEY_DATA_TABLE_CO, measureInfo.mDoubleCO);

           // SystemConfig.mIwuSQLHelper.mDBWrite.beginTransaction();
            mDBWrite.beginTransaction();
            try {
                //lnReturn = SystemConfig.mIwuSQLHelper.mDBWrite.insert(STR_TABLE_DATA, "", cv1);
                //SystemConfig.mIwuSQLHelper.mDBWrite.setTransactionSuccessful();
                lnReturn = mDBWrite.insert(STR_TABLE_DATA, "", cv1);
                mDBWrite.setTransactionSuccessful();
            }catch(Exception ex2){
                ex2.printStackTrace();
            }finally{
                //SystemConfig.mIwuSQLHelper.mDBWrite.endTransaction();
                mDBWrite.endTransaction();
            }

            return true;
        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("addDataInfoToDB.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            return false;
        }
    }

    public void tryAddDefaultAdmin(){
        Cursor cursor;
        boolean isExist;
        int iCnt;

        cursor = mDBWrite.rawQuery("select " + KEY_ADMIN_PRIMARY + " FROM " + STR_TABLE_ADMIN, null);

        iCnt = cursor.getCount();
        isExist = (iCnt==0)?false:true;
        if (!isExist){
            addDefaultAdminToDatabase();
            cursor = mDBWrite.rawQuery("select " + KEY_ADMIN_PRIMARY + " FROM " + STR_TABLE_ADMIN, null);
            iCnt = cursor.getCount();
            GIS_Log.d(TAG,"admin count = "+iCnt);
        }
        cursor.moveToFirst();
        cursor.close();

    }

    private void addDefaultAdminToDatabase(){
        adminInfo defaultAdmin;

        defaultAdmin = new adminInfo();
        defaultAdmin.Identify = "admin";
        defaultAdmin.password = "admin";

        addAdminToDataBase(defaultAdmin);
    }

    public adminInfo addAdminToDataBase(adminInfo inputAdmin){
        ContentValues cv;
        long ret;
        cv = new ContentValues();
        cv.put(KEY_ADMIN_ID, inputAdmin.Identify);
        cv.put(KEY_ADMIN_PW, inputAdmin.password);
        ret = mDBWrite.insert(STR_TABLE_ADMIN, "", cv);
        inputAdmin.adminID = ret;
        return inputAdmin;
    }


    /*public void addDataInfoToDBForTest() {
        DataBaseDataInfo dataInfo;
        Date date;
        SimpleDateFormat simpleDateFormat;

        dataInfo = new DataBaseDataInfo();
        dataInfo.mIntHR = mIntIndexForTest;
        dataInfo.mDoubleVpk = mIntIndexForTest+1;
        dataInfo.mDoubleVTI = mIntIndexForTest+20;
        dataInfo.mDoubleSV = mIntIndexForTest+30;
        dataInfo.mDoubleCO = mIntIndexForTest+40;
        dataInfo.mIntUserId = 1;
        dataInfo.mStrFileName = "FileName";
        simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.TAIWAN);
        date = new Date();
        dataInfo.mStrCreatedDate = simpleDateFormat.format(date);

        addDataInfoToDB(dataInfo);
        checkDataInfoWithinDB();
        mIntIndexForTest++;
    }*/


    public void checkDataInfoWithinDB() {
        int iIdx, iCntColumns, iCntRows;

        try {
            mStrCursorQueryFieldsForData = new String[]{IwuSQLHelper.KEY_DATA_TABLE_PRIMARY, IwuSQLHelper.KEY_DATA_TABLE_USER_ID,
                    IwuSQLHelper.KEY_DATA_TABLE_VTI, IwuSQLHelper.KEY_DATA_TABLE_VPK, IwuSQLHelper.KEY_DATA_TABLE_HR,
                    IwuSQLHelper.KEY_DATA_TABLE_RESULT, IwuSQLHelper.KEY_DATA_TABLE_CREATED_DATE, IwuSQLHelper.KEY_DATA_TABLE_CO,
                    IwuSQLHelper.KEY_DATA_TABLE_FILE_NAME, IwuSQLHelper.KEY_DATA_TABLE_SV};

            mCursorData = mDBWrite.query(IwuSQLHelper.STR_TABLE_DATA, mStrCursorQueryFieldsForData, null, null, null, null, null);
                    //SystemConfig.mIwuSQLHelper.mDBWrite.query(IwuSQLHelper.STR_TABLE_DATA, mStrCursorQueryFieldsForData, null, null, null, null, null);

            iCntColumns = mCursorData.getColumnCount();
            iCntRows = mCursorData.getCount();
            if (iCntRows > 0) {
                mCursorData.moveToLast();
                iIdx = mCursorData.getColumnIndex(IwuSQLHelper.KEY_DATA_TABLE_PRIMARY);
                mDatabaseDataInfo.mLnId = mCursorData.getInt(iIdx);
                iIdx = mCursorData.getColumnIndex(IwuSQLHelper.KEY_DATA_TABLE_FILE_NAME);
                mDatabaseDataInfo.mStrFileName = mCursorData.getString(iIdx);
                iIdx = mCursorData.getColumnIndex(IwuSQLHelper.KEY_DATA_TABLE_CO);
                mDatabaseDataInfo.mDoubleCO = (Double) mCursorData.getDouble(iIdx);
                iIdx = mCursorData.getColumnIndex(IwuSQLHelper.KEY_DATA_TABLE_CREATED_DATE);
                mDatabaseDataInfo.mStrCreatedDate = mCursorData.getString(iIdx);
                iIdx = mCursorData.getColumnIndex(IwuSQLHelper.KEY_DATA_TABLE_HR);
                mDatabaseDataInfo.mIntHR = mCursorData.getInt(iIdx);
                iIdx = mCursorData.getColumnIndex(IwuSQLHelper.KEY_DATA_TABLE_RESULT);
                mDatabaseDataInfo.mStrResult = mCursorData.getString(iIdx);
                iIdx = mCursorData.getColumnIndex(IwuSQLHelper.KEY_DATA_TABLE_SV);
                mDatabaseDataInfo.mDoubleSV = mCursorData.getDouble(iIdx);
                iIdx = mCursorData.getColumnIndex(IwuSQLHelper.KEY_DATA_TABLE_USER_ID);
                mDatabaseDataInfo.mIntUserId = mCursorData.getInt(iIdx);
                iIdx = mCursorData.getColumnIndex(IwuSQLHelper.KEY_DATA_TABLE_VPK);
                mDatabaseDataInfo.mDoubleVpk = mCursorData.getDouble(iIdx);
                iIdx = mCursorData.getColumnIndex(IwuSQLHelper.KEY_DATA_TABLE_VTI);
                mDatabaseDataInfo.mDoubleVTI = mCursorData.getDouble(iIdx);
            }
        }catch(Exception ex1){
            ex1.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugStr("IwuSQL.checkDataInfoWi.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }


    public static boolean tableExists(SQLiteDatabase db, String tableName)
    {
        if (tableName == null || db == null || !db.isOpen())
        {
            return false;
        }
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[] {"table", tableName});
        if (!cursor.moveToFirst())
        {
            cursor.close();
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    public static long getTableCount(SQLiteDatabase db, String tableName){
        long count = 0;
        count =  DatabaseUtils.queryNumEntries(db,tableName);
        GIS_Log.d(TAG,"user table count = "+count);
        return count;
    }


    // This method will return if your table exist a field or not
    public boolean isFieldExist(SQLiteDatabase db, String tableName, String fieldName)
    {
        boolean isExist = true;
        //SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("PRAGMA table_info("+tableName+")",null);
        int value = res.getColumnIndex(fieldName);
        res.close();

        if(value == -1)
        {
            isExist = false;
        }
        return isExist;
    }


    private String[] getAllFieldsFromTable(SQLiteDatabase db, String strTableName){
        String []  strColumnNames;
        int strColumnCount, index1, index2, index3;
        Cursor c = db.query(strTableName,null,null,null,null,null,null);
        strColumnNames = c.getColumnNames();
        strColumnCount = c.getColumnCount();
        index1 = c.getColumnIndex(KEY_DATA_TABLE_HR);
        index2 = c.getColumnIndex(KEY_DATA_TABLE_FILE_NAME);
        index3 = c.getColumnIndex(KEY_USER_BLE_MAC);
        c.close();
        /*
        Cursor c = db.rawQuery("SELECT * FROM " + strTableName + " WHERE 0", null);
        try {
            strColumnNames = c.getColumnNames();
            //strColumnNames = null;
        } finally {
            c.close();
        }*/
        Log.d(TAG, Arrays.toString(strColumnNames));
        Log.d(TAG, String.valueOf(index1));
        Log.d(TAG, String.valueOf(index2));
        Log.d(TAG, String.valueOf(index3));
        return strColumnNames;
    }

    public static userInfo getUserInfoFromPrimaryID(String strUserID, IwuSQLHelper inputHelper) {
        String[] columns, userIds;
        userInfo mUserInfo = null;

        Cursor mCursor;

        try {
            mUserInfo = new userInfo();
            mUserInfo.userCount = Integer.parseInt(strUserID);
            userIds = new String[1];
            userIds[0] = strUserID;
            columns = new String[]{KEY_USER_PRIMARY, KEY_USER_ID_NUMBER,
                    KEY_USER_NAME, KEY_USER_PHONE_NUMBER,
                    KEY_USER_BIRTHDAY, KEY_USER_HOSPITAL,
                    KEY_USER_JOB, KEY_USER_NATION,
                    KEY_USER_OPERATION, KEY_USER_DISEASE,
                    KEY_USER_PULM_DIAMETER, KEY_USER_GENDER, KEY_USER_MARRY};
            String selection = KEY_USER_PRIMARY + "=?";
            mCursor = inputHelper.mDBWrite.query(
                    STR_TABLE_USER,
                    columns,
                    selection,
                    userIds,
                    null,
                    null,
                    null
            );
            mCursor.moveToFirst();
            // Find selected user info.
            for (int i = 0; i<mCursor.getCount(); i++){
                if (mCursor.getInt(mCursor.getColumnIndexOrThrow(IwuSQLHelper.KEY_USER_PRIMARY))==mUserInfo.userCount){
                    break;
                }
                mCursor.moveToNext();
            }

            mUserInfo.userID = mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_USER_ID_NUMBER));
            mUserInfo.name = mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_USER_NAME));
            mUserInfo.phoneNumber = mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_USER_PHONE_NUMBER));
            mUserInfo.birthday = mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_USER_BIRTHDAY));
            mUserInfo.hospital = mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_USER_HOSPITAL));
            mUserInfo.job = mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_USER_JOB));
            mUserInfo.nationality = mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_USER_NATION));
            mUserInfo.operation = mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_USER_OPERATION));
            mUserInfo.disease = mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_USER_DISEASE));
//            mUserInfo.lastName = mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_USER_LAST_NAME));
//            mUserInfo.firstName = mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_USER_FIRST_NAME));
//            mUserInfo.heightToCSA = mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_USER_HEIGHT_TO_CSA));
//            mUserInfo.height = mCursor.getInt(mCursor.getColumnIndexOrThrow(KEY_USER_HEIGHT));
//            mUserInfo.pulmDiameter = mCursor.getInt(mCursor.getColumnIndexOrThrow(KEY_USER_PULM_DIAMETER));
            mUserInfo.pulmDiameter = mCursor.getDouble(mCursor.getColumnIndexOrThrow(KEY_USER_PULM_DIAMETER));
//            mUserInfo.angle = mCursor.getInt(mCursor.getColumnIndexOrThrow(KEY_USER_ANGLE));
            mUserInfo.gender = mCursor.getInt(mCursor.getColumnIndexOrThrow(KEY_USER_GENDER));
            mUserInfo.marry = mCursor.getInt(mCursor.getColumnIndexOrThrow(KEY_USER_MARRY));
//            mUserInfo.age = mCursor.getInt(mCursor.getColumnIndexOrThrow(KEY_USER_AGE));
//            int idx =mCursor.getColumnIndexOrThrow(KEY_USER_BLE_MAC);
//            if (idx!=-1){
//                mUserInfo.bleMac = mCursor.getString(idx);
//            }

            //getUserUltrasoundParameterFromObject();

            mCursor.close();
        } catch (Exception ex1) {
            ex1.printStackTrace();
            mUserInfo = null;
        }
        return mUserInfo;

    }

    public static ArrayList<userInfo> getAllUserInfo(IwuSQLHelper inputHelper) {
        String[] columns, userIds;
        userInfo mUserInfo = null;
        ArrayList<userInfo> resultList = new ArrayList<>();
        Cursor mCursor;

        try {
            mUserInfo = new userInfo();
            //userIds = new String[1];
            columns = new String[]{KEY_USER_PRIMARY, KEY_USER_ID_NUMBER,
                    KEY_USER_NAME, KEY_USER_PHONE_NUMBER,
                    KEY_USER_BIRTHDAY, KEY_USER_HOSPITAL,
                    KEY_USER_JOB, KEY_USER_NATION,
                    KEY_USER_OPERATION, KEY_USER_DISEASE,
                    KEY_USER_PULM_DIAMETER, KEY_USER_GENDER, KEY_USER_MARRY};
            String selection = KEY_USER_PRIMARY + "=?";
            mCursor = inputHelper.mDBWrite.query(
                    STR_TABLE_USER,
                    columns,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            mCursor.moveToFirst();
            // Find selected user info.
            for (int i = 0; i<mCursor.getCount(); i++){
                mUserInfo.userCount = mCursor.getInt(mCursor.getColumnIndexOrThrow(IwuSQLHelper.KEY_USER_PRIMARY));
                mUserInfo.userID = mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_USER_ID_NUMBER));
                mUserInfo.name = mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_USER_NAME));
                mUserInfo.phoneNumber = mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_USER_PHONE_NUMBER));
                mUserInfo.birthday = mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_USER_BIRTHDAY));
                mUserInfo.hospital = mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_USER_HOSPITAL));
                mUserInfo.job = mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_USER_JOB));
                mUserInfo.nationality = mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_USER_NATION));
                mUserInfo.operation = mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_USER_OPERATION));
                mUserInfo.disease = mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_USER_DISEASE));
                //mUserInfo.pulmDiameter = mCursor.getInt(mCursor.getColumnIndexOrThrow(KEY_USER_PULM_DIAMETER));
                mUserInfo.pulmDiameter = mCursor.getDouble(mCursor.getColumnIndexOrThrow(KEY_USER_PULM_DIAMETER));
                mUserInfo.gender = mCursor.getInt(mCursor.getColumnIndexOrThrow(KEY_USER_GENDER));
                mUserInfo.marry = mCursor.getInt(mCursor.getColumnIndexOrThrow(KEY_USER_MARRY));
//                int idx =mCursor.getColumnIndexOrThrow(KEY_USER_BLE_MAC);
//                if (idx!=-1){
//                    mUserInfo.bleMac = mCursor.getString(idx);
//                }
                resultList.add(mUserInfo);
                mCursor.moveToNext();
            }

            mCursor.close();
        } catch (Exception ex1) {
            ex1.printStackTrace();
            mUserInfo = null;
        }
        return resultList;

    }

}
