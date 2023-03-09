package com.gis.heartio.SignalProcessSubsystem.SupportSubsystem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.SQLException;

public class LoginDatabaseAdapter {
    private static final String TAG = "LoginDatabaseAdapter";
    private final Context context;
    public static final int retOk = 1;
    public static final int retFail = -1;
    private static IwuSQLHelper helper;
    public static SQLiteDatabase db;
    public static String getPassword = "";

    public LoginDatabaseAdapter(Context context)
    {
        this.context = context;
        helper = new IwuSQLHelper(this.context);
    }

    public LoginDatabaseAdapter open() throws SQLException
    {
        db = helper.getWritableDatabase();
        return this;
    }

    public void close()
    {
        db.close();
    }

    public SQLiteDatabase getDatabaseInstance()
    {
        return db;
    }

    public int insertEntry(String userID, String password, String firstName, String lastName){
        adminInfo tmpAdmin = new adminInfo();
        tmpAdmin.Identify = userID;
        tmpAdmin.password = password;
        if (firstName!=null && !firstName.equals("")){
            tmpAdmin.firstName = firstName;
        }
        if (lastName!=null && !lastName.equals("")){
            tmpAdmin.lastName = lastName;
        }
        adminInfo retAdmin = helper.addAdminToDataBase(tmpAdmin);
        return (retAdmin.adminID!=-1)?retOk:retFail;
    }

    public String getSingleEntry(String username){
        db = helper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(IwuSQLHelper.STR_TABLE_ADMIN, null,
                    "Identify=?", new String[]{username},
                    null, null, null);
            if (cursor.getCount()<1){
                if (cursor!=null)
                    cursor.close();
                return "NOT EXIST";
            }
            cursor.moveToFirst();
            getPassword = cursor.getString(cursor.getColumnIndex(IwuSQLHelper.KEY_ADMIN_PW));
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (cursor!=null)
                cursor.close();
        }

        return getPassword;
    }

    public int deleteEntry(String userID){
        String where = IwuSQLHelper.KEY_ADMIN_ID+"=?";
        int numberOfEntriesDeleted = db.delete(IwuSQLHelper.STR_TABLE_ADMIN,where,new String[]{userID});
        Log.d(TAG,"Delete user:"+userID+" , primary ID:"+numberOfEntriesDeleted);
        return numberOfEntriesDeleted;
    }

    public void updateEntry(String username, String password){
        ContentValues updateValues = new ContentValues();

        updateValues.put(IwuSQLHelper.KEY_ADMIN_ID,username);
        updateValues.put(IwuSQLHelper.KEY_ADMIN_PW,password);

        String where = IwuSQLHelper.KEY_ADMIN_ID+" = ?";
        db.update(IwuSQLHelper.STR_TABLE_ADMIN,updateValues,where,new String[]{username});
    }
}
