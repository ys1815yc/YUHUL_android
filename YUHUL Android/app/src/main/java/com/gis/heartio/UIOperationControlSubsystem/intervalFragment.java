package com.gis.heartio.UIOperationControlSubsystem;


import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
//import androidx.core.app.Fragment;
//import androidx.core.app.FragmentTransaction;
//import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.gis.BLEConnectionServices.BluetoothLeService;
import com.gis.CommonFragments.ProfileScanningFragment;
import com.gis.CommonUtils.Constants;
import com.gis.CommonUtils.Utils;
import com.gis.heartio.R;
import com.gis.heartio.SupportSubsystem.IwuSQLHelper;
import com.gis.heartio.SupportSubsystem.SystemConfig;
import com.gis.heartio.SupportSubsystem.dataInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class intervalFragment extends Fragment {
    private final String TAG = "intervalFragment";
    private AppCompatActivity mActivity=null;
    private int mCount = 0;
    private int runCount = 0;

    private List<dataInfo> dataList = new ArrayList<>();
    private IwuSQLHelper mHelper;

    private ListView mIntervalDataListView;
    private List<HashMap<String,Object>> hashMapsList;

    private ToggleButton mTBtnStartInterval;
    private EditText mIntervalEditText, mDurationEditText;
    private TextView mUserInfoTextView;
    private LinearLayout mDataTitleLayout;


    public intervalFragment() {
        // Required empty public constructor
        SystemConfig.initItriDevice8KOnlineBE();
    }

    /*private void updateCount(){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

            }
        });
    }*/
    private void updateToggleButton(){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mTBtnStartInterval.setChecked(false);
            }
        });
    }



    private Handler mMessengerHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            mCount = msg.arg1;
            //updateCount();
            if (msg.what == intervalForegroundService.ACTION_MESSAGE_ID_STOP){
                updateToggleButton();
                stopAll();
            } else if(msg.what == intervalForegroundService.ACTION_MESSAGE_ID_ACK){
                Message message = Message.obtain();
                //message.replyTo = mFragmentMessenger;
                message.what = intervalForegroundService.ACTION_MESSAGE_ID_START;
                message.arg1 = Integer.parseInt(mIntervalEditText.getText().toString());
                message.arg2 = Integer.parseInt(mDurationEditText.getText().toString());

                try{
                    MainActivity.mServiceMessenger.send(message);
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }else if (msg.what == intervalForegroundService.ACTION_MESSAGE_ID_SAVE_RESULT){
                //showResult();

                if (msg.arg2>runCount){
                    runCount = msg.arg2;
                    queryIntervalDataToList(UserManagerCommon.mUserInfoCur.userID,runCount);
                    /*for (int i=0;i<dataList.size();i++){
                        Log.d(TAG," Vpk = " + String.format("%.2f",dataList.get(i).Vpk)
                                +"  ,VTI= "+String.format("%.2f",dataList.get(i).VTI)
                                + " \nSV="+String.format("%.2f",dataList.get(i).SV)
                                +" ,HR= "+dataList.get(i).HR);
                    }*/
                    showListDataToListView();
                }
            }else if (msg.what == intervalForegroundService.ACTION_MESSAGE_ID_UPDATE_POWER_LEVEL){
                MainActivity.updatePowerLevel(SystemConfig.mIntPowerLevel,mUserInfoTextView, getContext());
            }else if (msg.what == intervalForegroundService.ACTION_MESSAGE_ID_COUNT_RET){
                if (msg.arg2>runCount){
                    runCount = msg.arg2;
                    queryIntervalDataToList(UserManagerCommon.mUserInfoCur.userID,runCount);
                    /*for (int i=0;i<dataList.size();i++){
                        Log.d(TAG," Vpk = " + String.format("%.2f",dataList.get(i).Vpk)
                                +"  ,VTI= "+String.format("%.2f",dataList.get(i).VTI)
                                + " \nSV="+String.format("%.2f",dataList.get(i).SV)
                                +" ,HR= "+dataList.get(i).HR);
                    }*/
                    showListDataToListView();
                }
            }else if (msg.what == intervalForegroundService.ACTION_MESSAGE_ID_DISCONNECT){
                mTBtnStartInterval.setEnabled(false);
            }
            super.handleMessage(msg);
        }
    };

    /*private void showResult(){
        int iHR;
        double doubleVpkAfterAngleAfterCali;
        double doubleVtiAfterAngleAfterCali;
        double doubleSVAfterAngleAfterCali;
        double doubleCOAfterAngleAfterCali;
        //------------------------------------------------------------------
        // for Heart Rate
        //------------------------------------------------------------------
        iHR = (int) MainActivity.mBVSignalProcessorPart2Selected.getHRAverage();
        if (iHR <= 0) {
            Log.d(TAG,"HR fail.");
        } else {
            Log.d(TAG,"HR = "+iHR);
        }

        //----------------------------------------------------------------------------------
        // for Vpk
        //-----------------------------------------------------------------------------------
        if (MainActivity.mBVSignalProcessorPart2Selected.getPeakVelocityAverage() <= 0) {
            Log.d(TAG,"Vpk fail.");
        } else {
            doubleVpkAfterAngleAfterCali = MainActivity.mBVSignalProcessorPart2Selected.mDoubleVpkMeterAverageAfterAngleAfterCali;
            Log.d(TAG,"Vpk = "+doubleVpkAfterAngleAfterCali*2.0);
        }

        //---------------------------------------------------------------
        // for VTI
        //---------------------------------------------------------------
        if(MainActivity.mBVSignalProcessorPart2Selected.getVTIAverage() <= 0) {
            Log.d(TAG,"VTI  fail");
        }else {
            doubleVtiAfterAngleAfterCali = MainActivity.mBVSignalProcessorPart2Selected.mDoubleVtiCmAverageAfterAngleAfterCali;
            Log.d(TAG,"VTI = "+doubleVtiAfterAngleAfterCali*2.0);
        }

        //-------------------------------------------------
        // for Stroke Volume
        //--------------------------------------------------
        if(MainActivity.mBVSignalProcessorPart2Selected.mDoubleStrokeVolumeAverage <= 0) {
            Log.d(TAG,"SV  fail");
        }else {
            //doubleSVAngle = MainActivity.mBVSignalProcessorPart2Selected.mDoubleStrokeVolumeAverageAfterUserAngle;
            doubleSVAfterAngleAfterCali = MainActivity.mBVSignalProcessorPart2Selected.mDoubleStrokeVolumeAverageAfterAngleAfterCali;
            //doubleSVOri = MainActivity.mBVSignalProcessorPart2Selected.mDoubleStrokeVolumeAverageOri;

            Log.d(TAG,"SV = "+doubleSVAfterAngleAfterCali*2.0);
        }

        //-------------------------------------------------
        // for Cardiac Output
        //--------------------------------------------------
        if(MainActivity.mBVSignalProcessorPart2Selected.getHRAverage() <= 0) {
            Log.d(TAG,"CO  fail");
        }else {
            //doubleCOAngle = MainActivity.mBVSignalProcessorPart2Selected.mDoubleCOAverageAfterUserAngle;
            doubleCOAfterAngleAfterCali = MainActivity.mBVSignalProcessorPart2Selected.mDoubleCOAverageAfterAngleAfterCali;
            //doubleCOOri = MainActivity.mBVSignalProcessorPart2Selected.mDoubleCOAverageOri;

            //strCardiacOutput = String.format("%.2f", doubleCOAngle);
            Log.d(TAG,"CO = "+doubleCOAfterAngleAfterCali*2.0);
        }
    }*/

    private void stopAll(){
        if (MainActivity.isMessengerServiceConnected){
            mActivity.unbindService(MainActivity.messengerServiceConnection);
            MainActivity.isMessengerServiceConnected = false;
        }
    }


    public static intervalFragment newInstance(){
        return new intervalFragment();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mActivity = (AppCompatActivity) getActivity();

        if (mActivity!=null){
            mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mActivity.getSupportActionBar().setDisplayShowHomeEnabled(true);
            setHasOptionsMenu(true);
        }

        mHelper = new IwuSQLHelper(mActivity);
        hashMapsList = new ArrayList<>();

        // Initial VTI mode to strongest mode.
        SystemConfig.mIntVTIModeIdx = SystemConfig.INT_VTI_MODE_0_STRONGEST;

        View rootView = inflater.inflate(R.layout.fragment_item_interval, container, false);
        mIntervalEditText = rootView.findViewById(R.id.intervalEditText);
        mDurationEditText = rootView.findViewById(R.id.durationEditText);

        mUserInfoTextView = rootView.findViewById(R.id.userInfoIntervalTextView);
        String userInfoStr = UserManagerCommon.mUserInfoCur.userID+ "   " +
                UserManagerCommon.mUserInfoCur.firstName + "  "+
                UserManagerCommon.mUserInfoCur.lastName+ "   PA Dia.  "+
                UserManagerCommon.mDoubleUserPulmDiameterCm;

        mUserInfoTextView.setText(userInfoStr);
        mDataTitleLayout = rootView.findViewById(R.id.dataTitleLinearLayout);

        /*if (intervalForegroundService.isRunning){
            mIntervalEditText.setText(String.valueOf(intervalForegroundService.intervalMins));
            mDurationEditText.setText(String.valueOf(intervalForegroundService.durationMins));
        }*/

        mTBtnStartInterval = rootView.findViewById(R.id.intervalToggleButton);
        initUIComponentStatus();
        mTBtnStartInterval.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    if (checkSetting()){
                        if (mActivity!=null){
                            runCount = 0;
                            Intent intent = new Intent(mActivity, intervalForegroundService.class);
                            intent.setAction(intervalForegroundService.ACTION_START_FOREGROUND_SERVICE);
                            mActivity.startService(intent);
                            if (!MainActivity.isMessengerServiceConnected){
                                mActivity.bindService(intent, MainActivity.messengerServiceConnection, Service.BIND_AUTO_CREATE);
                            }
                            Utils.setIntSharedPreference(mActivity,
                                    Utils.PERF_KEY_INTERVAL,Integer.parseInt(mIntervalEditText.getText().toString()));
                            Utils.setIntSharedPreference(mActivity,
                                    Utils.PERF_KEY_DURATION, Integer.parseInt(mDurationEditText.getText().toString()));

                        }
                    }else{
                        mTBtnStartInterval.setChecked(false);
                    }
                }else{
                    if (intervalForegroundService.isRunning){
                        Intent intent = new Intent(mActivity, intervalForegroundService.class);
                        intent.setAction(intervalForegroundService.ACTION_STOP_FOREGROUND_SERVICE);
                        mActivity.startService(intent);
                        stopAll();
                    }
                }
            }
        });

        mIntervalDataListView = rootView.findViewById(R.id.intervelDataListView);

        MainActivity.mFragmentMessenger = new Messenger(mMessengerHandler);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG,"onCreateOptionMenu");
        inflater.inflate(R.menu.fake_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (intervalForegroundService.isRunning){
            Intent intent = new Intent(mActivity, intervalForegroundService.class);
            mActivity.bindService(intent, MainActivity.messengerServiceConnection, Service.BIND_AUTO_CREATE);

            Message message = Message.obtain();
            //message.replyTo = mFragmentMessenger;
            message.what = intervalForegroundService.ACTION_MESSAGE_ID_GET_COUNT;

            try{
                MainActivity.mServiceMessenger.send(message);
            }catch (RemoteException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        //if (intervalForegroundService.isRunning){

        initUIComponentStatus();
        //}
        FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction();
        if (BluetoothLeService.getConnectionState() != BluetoothLeService.STATE_CONNECTED){
            transaction.replace(R.id.frame_layout, ProfileScanningFragment.newInstance(),Constants.PROFILE_SCANNING_FRAGMENT_TAG);
        }
        transaction.commit();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy()");
        if (intervalForegroundService.isRunning){
            mActivity.unbindService(MainActivity.messengerServiceConnection);
        }
        mHelper.closeDatabase();
        super.onDestroy();
    }

    private void showListDataToListView(){
        //mDataTitleLayout.setVisibility(View.VISIBLE);
        SimpleAdapter simpleAdapterInterval = new SimpleAdapter(mActivity, hashMapsList, R.layout.listitem_interval_data,
                new String[]{"Time","HR","Vpk","VTI","SV","CO"},
                new int[]{R.id.TimeValueTextView,R.id.HRValueTextView,R.id.VpkValueTextView,
                        R.id.VtiValueTextView,R.id.SVValueTextView,R.id.COValueTextView});
        mIntervalDataListView.setAdapter(simpleAdapterInterval);
        simpleAdapterInterval.notifyDataSetChanged();
    }

    private void initUIComponentStatus(){
        int tmpInterval = Utils.getIntSharedPreference(mActivity,Utils.PERF_KEY_INTERVAL);
        if (tmpInterval!=0){
            mIntervalEditText.setText(String.valueOf(intervalForegroundService.intervalMins));
        }
        int tmpDuration = Utils.getIntSharedPreference(mActivity, Utils.PERF_KEY_DURATION);
        if (tmpDuration!=0){
            mDurationEditText.setText(String.valueOf(intervalForegroundService.durationMins));
        }
        if (intervalForegroundService.isRunning){
            mTBtnStartInterval.setChecked(true);
        }
        if (SystemConfig.mIntPowerLevel!=-1 && SystemConfig.mIntPowerLevel!=0){
            MainActivity.updatePowerLevel(SystemConfig.mIntPowerLevel, mUserInfoTextView,mActivity);
        }
    }

    private boolean checkSetting(){
        if (BluetoothLeService.getConnectionState() != BluetoothLeService.STATE_CONNECTED){
            return false;
        }
        int duration = Integer.parseInt(mDurationEditText.getText().toString());
        int interval = Integer.parseInt(mIntervalEditText.getText().toString());
        if (interval < 30 && !SystemConfig.mTestMode){
            Toast.makeText(getContext(), getString(R.string.msg_interval_less_then_30), Toast.LENGTH_LONG).show();
            return false;
        }
        if (interval > duration){
            Toast.makeText(getContext(), getString(R.string.msg_duration_interval_error), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void queryIntervalDataToList(String userID,int queryCount){
        dataList.clear();
        hashMapsList.clear();
        int qCount = queryCount;
        if (mHelper.mDBWrite.isOpen()){
            Cursor cursor = mHelper.mDBWrite.rawQuery("select *  from "+ IwuSQLHelper.STR_TABLE_DATA+" where userID = ?",new String[] {userID});
            if (cursor.moveToLast()){
                while(qCount>0){
                    dataInfo tmpData = new dataInfo();
                    tmpData.Id = cursor.getLong(cursor.getColumnIndexOrThrow(IwuSQLHelper.KEY_DATA_TABLE_PRIMARY));
                    tmpData.createdDate = cursor.getString(cursor.getColumnIndexOrThrow(IwuSQLHelper.KEY_DATA_TABLE_CREATED_DATE));
                    tmpData.userId = cursor.getString(cursor.getColumnIndexOrThrow(IwuSQLHelper.KEY_DATA_TABLE_USER_ID));
                    tmpData.fileName = cursor.getString(cursor.getColumnIndexOrThrow(IwuSQLHelper.KEY_DATA_TABLE_FILE_NAME));
                    tmpData.SV = cursor.getDouble(cursor.getColumnIndexOrThrow(IwuSQLHelper.KEY_DATA_TABLE_SV));
                    tmpData.CO = cursor.getDouble(cursor.getColumnIndexOrThrow(IwuSQLHelper.KEY_DATA_TABLE_CO));
                    tmpData.VTI = cursor.getDouble(cursor.getColumnIndexOrThrow(IwuSQLHelper.KEY_DATA_TABLE_VTI));
                    tmpData.Vpk = cursor.getDouble(cursor.getColumnIndexOrThrow(IwuSQLHelper.KEY_DATA_TABLE_VPK));
                    tmpData.HR = cursor.getInt(cursor.getColumnIndexOrThrow(IwuSQLHelper.KEY_DATA_TABLE_HR));

                    dataList.add(tmpData);
                    HashMap<String,Object> tmpHashMap = new HashMap<>();

                    SimpleDateFormat simpleDateFormat,simpleDateFormat1;
                    simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
                    simpleDateFormat1 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                    Date date = null;
                    try {
                        date = simpleDateFormat.parse(tmpData.createdDate);
                        double tmpPerRate = 0.0;//MainActivity.mDoublePER * 100;
                        if (tmpData.fileName.contains("err")){
                            String errStr = tmpData.fileName.substring(tmpData.fileName.indexOf("err")+3,tmpData.fileName.indexOf("_."));
                            //Log.d(TAG,"errStr = " + errStr);
                            double errPacketNum = Double.parseDouble(errStr);
                            tmpPerRate =  errPacketNum / (461 + errPacketNum) * 100;
                        }

                        String PERStr = String.format(Locale.getDefault(),"  PER: %.2f %%", tmpPerRate);

                        if (tmpData.HR == 0 || tmpData.Vpk ==0){
                            tmpHashMap.put("Time", simpleDateFormat1.format(date) + "  Error");
                        }else {
                            tmpHashMap.put("Time", simpleDateFormat1.format(date) + PERStr);
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    tmpHashMap.put("HR", tmpData.HR);
                    tmpHashMap.put("Vpk", String.format("%.2f",tmpData.Vpk));
                    tmpHashMap.put("VTI", String.format("%.2f",tmpData.VTI));
                    tmpHashMap.put("SV", String.format("%.2f",tmpData.SV));
                    tmpHashMap.put("CO", String.format("%.2f",tmpData.CO));

                    hashMapsList.add(tmpHashMap);

                    qCount--;
                    cursor.moveToPrevious();
                }
            }
            cursor.close();
        }

    }

}
