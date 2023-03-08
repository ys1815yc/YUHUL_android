package com.gis.heartio.UIOperationControlSubsystem;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
//import android.support.annotation.NonNull;
//import android.support.design.widget.BottomNavigationView;
//import androidx.core.app.Fragment;
//import androidx.core.app.FragmentManager;
//import androidx.core.app.FragmentTransaction;
//import android.support.v7.app.ActionBar;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.gis.BLEConnectionServices.BluetoothLeService;
import com.gis.CommonFragments.ProfileScanningFragment;
import com.gis.CommonFragments.ServiceDiscoveryFragment;
import com.gis.CommonUtils.Constants;
import com.gis.CommonUtils.Utils;
import com.gis.heartio.BLEStatusReceiver;
import com.gis.heartio.GIS_Algorithm;
import com.gis.heartio.R;
import com.gis.heartio.AudioSubsystem.MyAudioPlayerController;
import com.gis.heartio.SignalProcessSubsystem.BVSignalProcessController;
import com.gis.heartio.SignalProcessSubsystem.BVSignalProcessorPart1;
import com.gis.heartio.SignalProcessSubsystem.BVSignalProcessorPart2;
import com.gis.heartio.SignalProcessSubsystem.RawDataProcessor;
import com.gis.heartio.SupportSubsystem.IwuSQLHelper;
import com.gis.heartio.SupportSubsystem.MyThreadQMsg;
import com.gis.heartio.SupportSubsystem.SystemConfig;
import com.gis.heartio.SupportSubsystem.userInfo;
import com.gis.heartio.heartioApplication;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

// For Jaufa Algorithm


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    //private TextView mTextMessage;
    public static Boolean mApplicationInBackground = false;

    private static heartioApplication mApplication;
    public static boolean mIsIndicateEnabled;
    public static boolean mIsNotifyEnabled;

    public static boolean mIsReadEnabled=false, mIsWriteEnabled = false;

    public static int indexOfCurrentNavigationItem = 1;

    //characteristics
    private BluetoothGattCharacteristic mReadCharacteristic;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothGattCharacteristic mIndicateCharacteristic;

    /**
     * Used to manage connections of the Blue tooth LE Device
     */
    private static BluetoothLeService mBluetoothLeService;
    //private static DrawerLayout mParentView;

    public static onlineFragment oFrag = null;
    public static offlineFragment offFrag = null;

    public static String currentFragmentTag = "";

    public static final int UI_MSG_ID_AFTER_CREATE_VIEW =0;
    public static final int UI_MSG_ID_SHOW_POWER_LEVEL=1;
    public static final int UI_MSG_ID_SHOW_BLE_STATE=2;

    //public static final int UI_MSG_ID_SHOW_DATA_LOST_STATE=3;
    public static final int UI_MSG_ID_DEBUG_SHOW_USCOM_RX=4;
    public static final int UI_MSG_ID_DEBUG_SHOW_RAWDATA_RX1=5;
    public static final int UI_MSG_ID_DEBUG_SHOW_PROC_DATA_RX1=6;
    public static final int UI_MSG_ID_DEBUG_SHOW_PROC_DATA_RX2=7;
    public static final int UI_MSG_ID_DEBUG_SHOW_PROC_DATA_RX3=8;
    public static final int UI_MSG_ID_TIME_SCALE_TOUCH_DOWN = 9;
    public static final int UI_MSG_ID_TIME_SCALE_TOUCH_MOVE = 10;
    public static final int UI_MSG_ID_TIME_SCALE_TOUCH_UP = 11;
    public static final int UI_MSG_ID_SHOW_BV_SV_AFTER_ONLINE_START = 12;
    public static final int UI_MSG_ID_BLE_CONNECTED=13;
    public static final int UI_MSG_ID_BLE_DISCONNECTED=14;

    private static int intPrePowerLevel = -1;

    public static String fwVersion = "";
    public static String UDIStr = "";

    public static long startTimeMS = 0;
    public static long diffTimeMS = 0;

    public static double mDoublePER = 0.0;
    /**
     * Code to manage Service life cycle.
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
                    .getService();
            // Initializing the service
            if (!mBluetoothLeService.initialize()) {
                Log.d(TAG,"Service not initialized");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    String attachmentFileName = "attachment.cyacd";
    private boolean BLUETOOTH_STATUS_FLAG = true;
    private String Paired;
    private String Unpaired;
    // progress dialog variable
    private ProgressDialog mpdia;
    private android.app.AlertDialog mAlert;

    BLEStatusReceiver mBLEStatusReceiver;

    /**
     * Fragment managing the behaviors, interactions and presentation of the
     * navigation drawer.
     */
    //private NavigationDrawerFragment mNavigationDrawerFragment;
    /**
     * Broadcast receiver for getting the bonding information
     */
    private BroadcastReceiver mBondStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            //Received when the bond state is changed

            Log.i("Test in", "onReceive: ");
            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                final int previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);

                if (state == BluetoothDevice.BOND_BONDING) {
                    // Bonding...
                    //String dataLog2 = getResources().getString(R.string.dl_commaseparator)
                   //         + "[" + ProfileScanningFragment.mDeviceName + "|"
                   //         + ProfileScanningFragment.mDeviceAddress + "] " +
                    //        getResources().getString(R.string.dl_connection_pairing_request);
                    //Log.d(TAG,dataLog2);
                    Utils.bondingProgressDialog(MainActivity.this, mpdia, true);
                } else if (state == BluetoothDevice.BOND_BONDED) {
                    Log.e(TAG,"MainActivity--->Bonded");
                    Utils.stopDialogTimer();
                    // Bonded...
                    if (ProfileScanningFragment.mPairButton != null) {
                        ProfileScanningFragment.mPairButton.setText(Paired);
                        if (bondState == BluetoothDevice.BOND_BONDED && previousBondState == BluetoothDevice.BOND_BONDING) {
                            Toast.makeText(MainActivity.this, getResources().getString(R.string.toast_paired), Toast.LENGTH_SHORT).show();
                        }
                    }
                    //String dataLog = getResources().getString(R.string.dl_commaseparator)
                     //       + "[" + ProfileScanningFragment.mDeviceName + "|"
                     //       + ProfileScanningFragment.mDeviceAddress + "] " +
                     //       getResources().getString(R.string.dl_connection_paired);
                    //Log.d(TAG,dataLog);
                    Utils.bondingProgressDialog(MainActivity.this, mpdia, false);

                } else if (state == BluetoothDevice.BOND_NONE) {
                    // Not bonded...
                    Log.e(TAG,"MainActivity--->Not Bonded");
                    Utils.stopDialogTimer();
                    if (ProfileScanningFragment.mPairButton != null) {
                        ProfileScanningFragment.mPairButton.setText(Unpaired);
                        if (bondState == BluetoothDevice.BOND_NONE && previousBondState == BluetoothDevice.BOND_BONDED) {
                            Toast.makeText(MainActivity.this, getResources().getString(R.string.toast_unpaired), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this,
                                    getResources().getString(R.string.dl_connection_pairing_unsupported),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                    //String dataLog = getResources().getString(R.string.dl_commaseparator)
                    //        + "[" + ProfileScanningFragment.mDeviceName + "|"
                     //       + ProfileScanningFragment.mDeviceAddress + "] " +
                      //      getResources().getString(R.string.dl_connection_pairing_unsupported);
                    //Logger.datalog(dataLog);
                    Utils.bondingProgressDialog(MainActivity.this, mpdia, false);
                } else {
                    Log.e(TAG,"Error received in pair-->" + state);
                }
            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                Log.i(TAG,"BluetoothAdapter.ACTION_STATE_CHANGED.");
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) ==
                        BluetoothAdapter.STATE_OFF) {
                    Log.i(TAG,"BluetoothAdapter.STATE_OFF");
                    if (BLUETOOTH_STATUS_FLAG) {
                        connectionLostBluetoothalertbox(true);
                    }

                } else if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) ==
                        BluetoothAdapter.STATE_ON) {
                    Log.i(TAG,"BluetoothAdapter.STATE_ON");
                    if (BLUETOOTH_STATUS_FLAG) {
                        connectionLostBluetoothalertbox(false);
                    }

                }

            } else if (action.equals(BluetoothLeService.ACTION_PAIR_REQUEST)) {
                Log.e(TAG,"Pair request received");
                Log.e(TAG,"HomepageActivity--->Pair Request");
                Utils.stopDialogTimer();
            }
            Log.i("Test out", "onReceive: ");

        }
    };




    private NavigationBarView.OnItemSelectedListener mOnItemSelectedListener =
            new NavigationBarView.OnItemSelectedListener(){
        @Override
        public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
            Fragment selectedFragment = null;
            ActionBar ab = getSupportActionBar();
            if (intervalForegroundService.isRunning){
                return false;
            }
            String newTitle = "";
            String newFragmentTag = "";
            int newNavigationIndex = 0;
            SystemConfig.ENUM_UI_STATE newState = SystemConfig.ENUM_UI_STATE.ULTRASOUND_UI_STATE_NONE;

            switch (item.getItemId()) {
                case R.id.navigation_user:
                    //indexOfCurrentNavigationItem = 0;
                    newNavigationIndex = 0;
                    selectedFragment = userFragment.newInstance();
                    newFragmentTag = Constants.USER_MANAGER_TAG;
                    newTitle = getString(R.string.title_user);
                    break;
                case R.id.navigation_home:
                    //indexOfCurrentNavigationItem = 1;
                    newNavigationIndex = 1;
                    //mTextMessage.setText(R.string.title_home);
                    //selectedFragment = onlineFragment.newInstance();
                    Log.d(TAG,"BluetoothLeService.getConnectionState() = "+BluetoothLeService.getConnectionState());
                    if (BluetoothLeService.getConnectionState() == BluetoothLeService.STATE_CONNECTED){
                        //SystemConfig.mEnumUltrasoundUIState = SystemConfig.ENUM_UI_STATE.ULTRASOUND_UI_STATE_ONLINE;
                        newState = SystemConfig.ENUM_UI_STATE.ULTRASOUND_UI_STATE_ONLINE;
                       // if (oFrag == null){
                            selectedFragment = onlineFragment.newInstance();
                        //    oFrag = (onlineFragment)selectedFragment;
                        //}else{
                        //    selectedFragment = oFrag;
                        //}
                        newFragmentTag = Constants.ITRI_ULTRASOUND_FRAGMENT_TAG;
                    }else{
                        selectedFragment = ProfileScanningFragment.newInstance();
                        newFragmentTag = Constants.PROFILE_SCANNING_FRAGMENT_TAG;
                    }
                    newTitle = getString(R.string.title_home);
                    break;
               // case R.id.navigation_user:
               //     mTextMessage.setText(R.string.title_user);
                //    return true;
                case R.id.navigation_dashboard:
                    //indexOfCurrentNavigationItem = 4;
                    newNavigationIndex = 4;
                    //mTextMessage.setText(R.string.title_more);
                    //return true;
                    selectedFragment = MoreFragment.newInstance();
                    newFragmentTag = Constants.MORE_FRAGMENT_TAG;
                    newTitle = getString(R.string.title_more);
                    break;
                //case R.id.navigation_notifications:
                //    mTextMessage.setText(R.string.title_notifications);
                //    return true;
                case R.id.navigation_file:
                    //indexOfCurrentNavigationItem = 3;
                    newNavigationIndex = 3;
                   // mTextMessage.setText(R.string.title_file);
                    //SystemConfig.mEnumUltrasoundUIState = SystemConfig.ENUM_UI_STATE.ULTRASOUND_UI_STATE_OFFLINE;
                    //SystemConfig.mEnumUltrasoundSubUIState = SystemConfig.ENUM_SUB_UI_STATE.BLOOD_FLOW_VELOCITY;
                    newState = SystemConfig.ENUM_UI_STATE.ULTRASOUND_UI_STATE_OFFLINE;
                    //selectedFragment = offlineFragment.newInstance();
                    //selectedFragment = dataFragment.newInstance();
                    selectedFragment = data2Fragment.newInstance();

                    newTitle = getString(R.string.title_history);
                    newFragmentTag = Constants.DATA_MANAGER_TAG;
                    break;

                case R.id.navigation_interval:
                    //indexOfCurrentNavigationItem = 2;
                    newNavigationIndex = 2;
                    if (BluetoothLeService.getConnectionState() == BluetoothLeService.STATE_CONNECTED){
                        //SystemConfig.mEnumUltrasoundUIState = SystemConfig.ENUM_UI_STATE.ULTRASOUND_UI_STATE_ONLINE;
                        newState = SystemConfig.ENUM_UI_STATE.ULTRASOUND_UI_STATE_ONLINE;
                        // if (oFrag == null){
                        selectedFragment = intervalFragment.newInstance();
                        newFragmentTag = Constants.INTERVAL_FRAGMENT_TAG;
                    }else{
                        selectedFragment = ProfileScanningFragment.newInstance();
                        newFragmentTag = Constants.PROFILE_SCANNING_FRAGMENT_TAG;
                    }
                    newTitle = getString(R.string.title_interval);

                    break;
                    

            }

            if (SystemConfig.mEnumUltrasoundUIState == SystemConfig.ENUM_UI_STATE.ULTRASOUND_UI_STATE_ONLINE &&
                    currentFragmentTag.equals(Constants.ITRI_ULTRASOUND_FRAGMENT_TAG) ){
                //Log.d(TAG,"online change to what????");
                if(oFrag!=null && oFrag.mBtnSave!=null){
                    if (oFrag.mBtnSave.isEnabled()){
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(getString(R.string.msg_save_data_or_not_alert_before_leave));
                        builder.setPositiveButton(getString(R.string.alert_message_no), (dialog, which) -> {
                            oFrag.mBtnSave.setEnabled(false);
                            onNavigationItemSelected(item);
                        });

                        builder.setNegativeButton(getString(R.string.title_save), (dialog, which) -> {
                            oFrag.mBtnSave.performClick();
                            onNavigationItemSelected(item);
                        });

                        AlertDialog alert = builder.create();
                        alert.show();
                        return true;
                    }
                }
            }

            if (ab!=null){
                //ab.setTitle(getString(R.string.title_home));
                setTitle(newTitle);
            }
            if (newState != SystemConfig.ENUM_UI_STATE.ULTRASOUND_UI_STATE_NONE){
                SystemConfig.mEnumUltrasoundUIState = newState;
                if (newState == SystemConfig.ENUM_UI_STATE.ULTRASOUND_UI_STATE_OFFLINE){
                    SystemConfig.mEnumUltrasoundSubUIState = SystemConfig.ENUM_SUB_UI_STATE.BLOOD_FLOW_VELOCITY;
                }
            }
            indexOfCurrentNavigationItem = newNavigationIndex;
            currentFragmentTag = newFragmentTag;
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
           // transaction.replace(R.id.frame_layout, selectedFragment);
            transaction.replace(R.id.frame_layout, selectedFragment,currentFragmentTag);
            transaction.commit();
            return true;
            //return false;
        }
    };

    public static BVSignalProcessorPart1 mBVSignalProcessorPart1 = null;
    public static BVSignalProcessorPart2[] mBVSignalProcessorPart2Array;
    public static BVSignalProcessorPart2 mBVSignalProcessorPart2Selected;
    public static BVSignalProcessController mSignalProcessController = null;
    public static RawDataProcessor mRawDataProcessor = null;
    public static MyAudioPlayerController mAudioPlayerController = null;

    public static GIS_Algorithm.vtiBoundaryResult mVtiBoundaryResultByGIS = null;
    public static GIS_Algorithm.vtiAndVpkResult mVtiAndVpkResultByGIS = null;

    public static IwuSQLHelper mIwuSQLHelper;
    public static String currentAdminID = "";
    public static String currentAdminPW = "";

    public void setTitle(String title){
        if (getSupportActionBar()!=null){
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            TextView textView = new TextView(this);
            textView.setText(title);
            textView.setTextSize(20);
            textView.setTypeface(null, Typeface.BOLD);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT
                    , LinearLayout.LayoutParams.WRAP_CONTENT);

            textView.setLayoutParams(layoutParams);
            textView.setGravity(Gravity.CENTER);

            textView.setTextColor(getResources().getColor(R.color.black,getTheme()));
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(textView);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mApplication = (heartioApplication) getApplication();

        if (savedInstanceState == null){
            if (getIntent().getExtras()!=null){
                currentAdminID = getIntent().getExtras().getString("adminID","");
                currentAdminPW = getIntent().getExtras().getString("adminPW", "");
                Log.d(TAG,"currentAdminID = "+currentAdminID + "  from getExtras()");
            }
        }else{
            currentAdminID = (String)savedInstanceState.getSerializable("adminID");
            currentAdminPW = (String)savedInstanceState.getSerializable("adminPW");
            Log.d(TAG,"currentAdminID = "+currentAdminID +"  from savedInstanceState");
        }

        // Getting the characteristics from the application class
        mReadCharacteristic = mApplication.getBluetoothgattDatacharacteristic();
        mNotifyCharacteristic = mApplication.getBluetoothgattDatacharacteristic();

        //mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnItemSelectedListener(mOnItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_home);
        if (!SystemConfig.mTestMode){
            navigation.getMenu().removeItem(R.id.navigation_interval);
        }
        //BottomNavigationViewHelper.disableShiftMode(navigation);

        if(Build.VERSION.SDK_INT >= 23) {
            if (getSupportActionBar()!=null){
                getSupportActionBar().setElevation(0);
            }
        }


        if (Build.VERSION.SDK_INT >= 23) {
            // Marshmallow+ Permission APIs
            fuckMarshMallow();
            String pkg=getPackageName();
            PowerManager pm=getSystemService(PowerManager.class);
            if (pm!=null&&!pm.isIgnoringBatteryOptimizations(pkg)){
                Log.d(TAG,"Try to get permission : ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS  ");
                Intent pIntent= new Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                                .setData(Uri.parse("package:"+pkg));
                pIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(pIntent);
            }
        }

        SystemConfig.systemInit();

        //Manually displaying the first fragment - one time only
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (BluetoothLeService.getConnectionState() == BluetoothLeService.STATE_CONNECTED){
            //if (oFrag == null){
                //oFrag = onlineFragment.newInstance();
            //}
            SystemConfig.mEnumUltrasoundUIState = SystemConfig.ENUM_UI_STATE.ULTRASOUND_UI_STATE_ONLINE;
            transaction.replace(R.id.frame_layout, onlineFragment.newInstance(),Constants.ITRI_ULTRASOUND_FRAGMENT_TAG);

        }else{
            transaction.replace(R.id.frame_layout, ProfileScanningFragment.newInstance(),Constants.PROFILE_SCANNING_FRAGMENT_TAG);
        }
        transaction.commit();

        Paired = getResources().getString(R.string.bluetooth_pair_to_connect);
        Unpaired = getResources().getString(R.string.bluetooth_unpair_to_connect);
        // = (DrawerLayout) findViewById(R.id.drawer_layout);
        //mContainerView = (FrameLayout) findViewById(R.id.container);
        mpdia = new ProgressDialog(this);
        mpdia.setCancelable(false);
        mAlert = new android.app.AlertDialog.Builder(this).create();
        mAlert.setMessage(getResources().getString(
                R.string.alert_message_bluetooth_reconnect));
        mAlert.setCancelable(false);
        mAlert.setTitle(getResources().getString(R.string.app_name));
        mAlert.setButton(Dialog.BUTTON_POSITIVE, getResources().getString(
                R.string.alert_message_exit_ok), (dialogInterface, i) -> {
                    Intent intentActivity = getIntent();
                    finish();
                    overridePendingTransition(
                            R.anim.slide_left, R.anim.push_left);
                    startActivity(intentActivity);
                    overridePendingTransition(
                            R.anim.slide_right, R.anim.push_right);
                });
        mAlert.setCanceledOnTouchOutside(false);

        // Set the Clear cache on disconnect as false by devfault
        //if (!Utils.ifContainsSharedPreference(this, Constants.PREF_PAIR_CACHE_STATUS)) {
            Utils.setBooleanSharedPreference(this, Constants.PREF_PAIR_CACHE_STATUS, true);
        //}

        mIwuSQLHelper = new IwuSQLHelper(MainActivity.this);

        if (IwuSQLHelper.getTableCount(mIwuSQLHelper.mDBWrite,IwuSQLHelper.STR_TABLE_USER)>0){
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            int currentUserID_primary = sharedPref.getInt(IwuSQLHelper.KEY_CURRENT_USER,1);
            //Log.d(TAG,"String.valueOf(currentUserID_primary) = "+String.valueOf(currentUserID_primary));
            userInfo tmpUser = IwuSQLHelper.getUserInfoFromPrimaryID(String.valueOf(currentUserID_primary),mIwuSQLHelper);
            if (tmpUser!=null){
                UserManagerCommon.initUserUltrasoundParameter(tmpUser);
            }else{
                ArrayList<userInfo> userList = IwuSQLHelper.getAllUserInfo(mIwuSQLHelper);
                if (userList.size() > 0){
                    Log.d(TAG,"Total user : "+ userList.size());
                    UserManagerCommon.initUserUltrasoundParameter(userList.get(0));

                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt(IwuSQLHelper.KEY_CURRENT_USER, userList.get(0).userCount);
                    editor.commit();
                }else{
                    UserManagerCommon.initUserUltrasoundParameter(null);
                }
            }

        }else{
            UserManagerCommon.initUserUltrasoundParameter(null);
        }

        if (mBVSignalProcessorPart1 == null) {
            mBVSignalProcessorPart1 = new BVSignalProcessorPart1();
        }

        if (mBVSignalProcessorPart2Array == null) {
            mBVSignalProcessorPart2Array = new BVSignalProcessorPart2[SystemConfig.INT_HR_GROUP_CNT];
            for (int iVar = 0; iVar < SystemConfig.INT_HR_GROUP_CNT; iVar++) {
                MainActivity.mBVSignalProcessorPart2Array[iVar] = new BVSignalProcessorPart2(iVar);
            }
        }


        if (mRawDataProcessor == null) {
            mRawDataProcessor = new RawDataProcessor();
            mRawDataProcessor.initRawDataProcessor(this);
        }

        //----------- for BVSignalProcessorController -------
        if (mSignalProcessController == null) {
            mSignalProcessController = new BVSignalProcessController();
            mSignalProcessController.startThread(10);
        }

        //----------- for Audio Player Controller -------
        if (mAudioPlayerController == null) {
            mAudioPlayerController = new MyAudioPlayerController();
            mAudioPlayerController.startThread(10);
        }

        // Start service
        Intent gattServiceIntent = new Intent(getApplicationContext(),
                BluetoothLeService.class);
        if (SystemConfig.isHeartIO2){
            bindService(gattServiceIntent,mServiceConnection, BIND_AUTO_CREATE);
        }else{
            startService(gattServiceIntent);
        }





        mBLEStatusReceiver = new BLEStatusReceiver();
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        mIntentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        registerReceiver(mBLEStatusReceiver,mIntentFilter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("adminID",currentAdminID);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    public void connectionLostBluetoothalertbox(Boolean status) {
        //Disconnected
        if (status) {
            mAlert.show();
        } else {
            if (mAlert != null && mAlert.isShowing())
                mAlert.dismiss();
        }

    }

    @Override
    protected void onPause() {

        getIntent().setData(null);
        // Getting the current active fragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
        //.findFragmentByTag("ProfileScanningFragment");
       //         .findFragmentById(R.id.container);
        if (currentFragment instanceof ProfileScanningFragment) {
            Intent gattServiceIntent = new Intent(getApplicationContext(),
                    BluetoothLeService.class);
            stopService(gattServiceIntent);
        }
        mApplicationInBackground = true;
        BLUETOOTH_STATUS_FLAG = false;
        unregisterReceiver(mBondStateReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        //mIwuSQLHelper = new IwuSQLHelper(this);

        //LongStartSettingCommon.initLongStartSettingCommon();
        //mIwuSQLHelper.tryAddTestUser();

        Log.d(TAG,"onResume-->activity");
        Log.d(TAG,"registerReceiver");
        registerReceiver(mGattUpdateReceiver,
                Utils.makeGattUpdateIntentFilter());

        mIsNotifyEnabled = false;
        mIsIndicateEnabled = false;

        /*
        try {
            catchUpgradeFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
        mApplicationInBackground = false;
        BLUETOOTH_STATUS_FLAG = true;
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_PAIR_REQUEST);
        registerReceiver(mBondStateReceiver, intentFilter);
        super.onResume();

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            final boolean fromIntervalFragment = extras.getBoolean(Constants.INTERVAL_FRAGMENT_TAG);
            if (fromIntervalFragment){
                intervalFragment fr = intervalFragment.newInstance();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                // transaction.replace(R.id.frame_layout, selectedFragment);
                transaction.replace(R.id.frame_layout, fr,currentFragmentTag);
                transaction.commit();
            }
        }
    }

    @Override
    protected void onDestroy() {

        if (SystemConfig.isHeartIO2){
            /*try{
            unbindService(mServiceConnection);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }*/
        }else{

        }


        try {
            Log.d(TAG,"unregister GattUpdateReceiver");
            unregisterReceiver(mGattUpdateReceiver);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }

        try{
            Log.d(TAG,"unregister BLEStatusReceiver");
            unregisterReceiver(mBLEStatusReceiver);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }

        if (mSignalProcessController != null) {
            mSignalProcessController.stopThread();
        }
        if (mAudioPlayerController != null) {
            mAudioPlayerController.stopThread();
        }
        mIwuSQLHelper.closeDatabase();
        super.onDestroy();
    }



    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
        //.findFragmentByTag("ProfileScanningFragment");
        //         .findFragmentById(R.id.container);
        if (currentFragment instanceof onlineFragment
                || currentFragment instanceof intervalFragment
                || currentFragment instanceof ServiceDiscoveryFragment) {
            if (currentFragmentTag.equals(Constants.ITRI_ULTRASOUND_FRAGMENT_TAG)){
                Log.d(TAG,"onlineFragment backPress.");
                if (oFrag!=null && oFrag.mBtnSave!=null){
                    if (oFrag.mBtnSave.isEnabled()){
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(getString(R.string.msg_save_data_or_not_alert_before_leave));
                        builder.setPositiveButton(getString(R.string.alert_message_no), (dialog, which) -> {
                            oFrag.mBtnSave.setEnabled(false);
                            onBackPressed();
                        });

                        builder.setNegativeButton(getString(R.string.title_save), (dialog, which) -> {
                            oFrag.mBtnSave.performClick();
                            onBackPressed();
                        });

                        AlertDialog alert = builder.create();
                        alert.show();
                        return;
                    }
                }
            } else if (currentFragmentTag.equals(Constants.INTERVAL_FRAGMENT_TAG)){
                if (intervalForegroundService.isRunning){
                    return;
                }
            }
            //super.onBackPressed();
            if (BluetoothLeService.getConnectionState() == BluetoothLeService.STATE_CONNECTED){
                BluetoothLeService.disconnect();
            }
            currentFragmentTag = Constants.PROFILE_SCANNING_FRAGMENT_TAG;
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_layout, ProfileScanningFragment.newInstance(),Constants.PROFILE_SCANNING_FRAGMENT_TAG);
            transaction.commit();
        }else if (currentFragment instanceof MoreFragment||
                currentFragment instanceof dataFragment||
                currentFragment instanceof  userFragment ||
                currentFragment instanceof ProfileScanningFragment||
                currentFragment instanceof data2Fragment){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.alert_msg_exit_heartio));
            builder.setPositiveButton(getString(R.string.action_exit), (dialog, which) -> {
                //finish();
                android.os.Process.killProcess(android.os.Process.myPid());
            });

            builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());

            AlertDialog alert = builder.create();
            alert.show();

            final Button yesBtn = alert.getButton(AlertDialog.BUTTON_POSITIVE);
            yesBtn.setBackgroundResource(R.drawable.block_b);
            yesBtn.setTextColor(Color.WHITE);
            yesBtn.setTextSize(18f);
            yesBtn.setScaleX(0.60f);
            yesBtn.setScaleY(0.60f);

            final Button cancelBtn = alert.getButton(AlertDialog.BUTTON_NEGATIVE);
            cancelBtn.setBackgroundResource(R.drawable.block_g2);
            cancelBtn.setTextColor(Color.parseColor("#804A4A4A"));
            cancelBtn.setTextSize(18f);
            cancelBtn.setScaleX(0.60f);
            cancelBtn.setScaleY(0.60f);
            cancelBtn.setPadding(100,5,100,5);
        } else{
            Log.d(TAG," onBackPress.");
            super.onBackPressed();
        }


    }

    // For Location permission request
    // After  MarshMallow (6.0) it Needed for get bluetooth device list.
    private final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 9487;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
//                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                boolean allGranted = true;
                // Fill with results
                for (int i = 0; i < permissions.length; i++) {
                    perms.put(permissions[i], grantResults[i]);
                    if (grantResults[i]!=PackageManager.PERMISSION_GRANTED){
                        if (permissions[i]==Manifest.permission.ACCESS_FINE_LOCATION&&Build.VERSION.SDK_INT > 30){
                            continue;
                        }
                        allGranted = false;
                    }
                }

                // Check for ACCESS_FINE_LOCATION
//                if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (allGranted) {
                    // All Permissions Granted
                    // Permission Denied
                    Toast.makeText(MainActivity.this, getString(R.string.all_permission_granted), Toast.LENGTH_SHORT)
                            .show();


                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, getString(R.string.permissions_denied_exiting_app), Toast.LENGTH_SHORT)
                            .show();

                    finish();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    //@TargetApi(Build.VERSION_CODES.M)
    private void fuckMarshMallow() {
        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();
        if(Build.VERSION.SDK_INT <= 30) {
            if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
                permissionsNeeded.add(getString(R.string.show_location));
        }
        if (!addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE))
            permissionsNeeded.add(getString(R.string.read_external_storage));
        if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionsNeeded.add(getString(R.string.write_external_storage));
        if (Build.VERSION.SDK_INT >=31){
            if (!addPermission(permissionsList, Manifest.permission.BLUETOOTH_SCAN))
                permissionsNeeded.add(getString(R.string.ble_scan));
            if (!addPermission(permissionsList, Manifest.permission.BLUETOOTH_CONNECT))
                permissionsNeeded.add(getString(R.string.ble_connect));
        }

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {

                // Need Rationale
                String message = getString(R.string.app_need_access_to) + permissionsNeeded.get(0);

                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);

                showMessageOKCancel(message,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                            }
                        });
                return;
            }
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        }

        //Toast.makeText(MainActivity.this, "No new Permission Required- Launching App .You are Awesome!!", Toast.LENGTH_SHORT)
        //        .show();
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.app.AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton(getString(R.string.alert_message_exit_ok), okListener)
                .setNegativeButton(getString(R.string.cancel), null)
                .create()
                .show();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean addPermission(List<String> permissionsList, String permission) {

        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }


    private BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Bundle extras = intent.getExtras();
            if (extras!=null&&action!=null){
                if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    // Data Received
                    if (extras.containsKey(Constants.EXTRA_BYTE_VALUE)) {
                        BluetoothGattCharacteristic requiredCharacteristic = mApplication.getBluetoothgattDatacharacteristic();
                        //mApplication.getBluetoothgattcharacteristic();
                        String uuidRequired = requiredCharacteristic.getUuid().toString();
                        String receivedUUID = "";
                        String requiredServiceUUID = requiredCharacteristic.
                                getService().getUuid().toString();
                        String receivedServiceUUID = "";
                        int requiredInstanceID = requiredCharacteristic.getInstanceId();
                        int receivedInstanceID = -1;
                        int receivedServiceInstanceID = -1;
                        int requiredServiceInstanceID =
                                requiredCharacteristic.getService().
                                        getInstanceId();
                        if (extras.containsKey(Constants.EXTRA_BYTE_UUID_VALUE)) {
                            receivedUUID = intent.getStringExtra(
                                    Constants.EXTRA_BYTE_UUID_VALUE);
                        }
                        if (extras.containsKey(Constants.EXTRA_BYTE_INSTANCE_VALUE)) {
                            receivedInstanceID = intent.getIntExtra(Constants.
                                            EXTRA_BYTE_INSTANCE_VALUE,
                                    -1);
                        }
                        if (extras.containsKey(Constants.
                                EXTRA_BYTE_SERVICE_UUID_VALUE)) {
                            receivedServiceUUID = intent.getStringExtra(
                                    Constants.EXTRA_BYTE_SERVICE_UUID_VALUE);
                        }
                        if (extras.containsKey(Constants.
                                EXTRA_BYTE_SERVICE_INSTANCE_VALUE)) {
                            receivedServiceInstanceID =
                                    intent.getIntExtra(Constants.EXTRA_BYTE_SERVICE_INSTANCE_VALUE,
                                            -1);
                        }
                        if (extras.containsKey(Constants.EXTRA_FRS_VALUE)) {
                            //Log.d("MainActivity","FW version: "+ intent.getStringExtra(Constants.EXTRA_FRS_VALUE).trim());
                            MainActivity.fwVersion = intent.getStringExtra(Constants.EXTRA_FRS_VALUE).trim();
                        }
                        if (extras.containsKey(Constants.EXTRA_SRS_VALUE)){
                            Log.d(TAG, "Latency: "+ diffTimeMS);
                            Log.d("MainActivity","MAC address:"+ intent.getStringExtra(Constants.EXTRA_SRS_VALUE).trim());
                        }
                        if (extras.containsKey(Constants.EXTRA_UDI_VALUE)){
                            String tmpString = intent.getStringExtra(Constants.EXTRA_UDI_VALUE).trim();
                            Log.d(TAG, "tmp string = "+ tmpString);
                            if (tmpString.length() != 0 && tmpString.length() == 56){
                                String udiStr = tmpString.substring(0,40);
                                String paraStr = tmpString.substring(40,56);
                                Log.d(TAG, "UDI ="+udiStr);
                                MainActivity.UDIStr = udiStr;
                                Log.d(TAG,"param ="+paraStr);
                            }

                        }
                        if (uuidRequired.equalsIgnoreCase(receivedUUID)
                                && requiredServiceUUID.equalsIgnoreCase(receivedServiceUUID)
                                && requiredInstanceID == receivedInstanceID
                                && requiredServiceInstanceID == receivedServiceInstanceID) {

                            byte[] array = intent.getByteArrayExtra(Constants.EXTRA_BYTE_VALUE);
                            MyThreadQMsg myMsgForSignalProcess = new MyThreadQMsg();
                            myMsgForSignalProcess.mIntMsgId = MyThreadQMsg.INT_MY_MSG_EVT_SPROCESS_DATA_IN;
                            myMsgForSignalProcess.mByteArray = Arrays.copyOf(array,array.length);
//                            Log.d(TAG,"byteArray.length="+myMsgForSignalProcess.mByteArray.length);
                            MainActivity.mSignalProcessController.putMsg(myMsgForSignalProcess);

                            //Log.d(TAG,Utils.ByteArraytoHex(array));
                            //displayHexValue(array);
                            //displayASCIIValue(mHexValue.getText().
                            //       toString());
                            //displayTimeandDate();
                        }
                    }
                    if (extras.containsKey(Constants.EXTRA_DESCRIPTOR_BYTE_VALUE)) {
                        if (extras.containsKey(Constants.
                                EXTRA_DESCRIPTOR_BYTE_VALUE_CHARACTERISTIC_UUID)) {
                            BluetoothGattCharacteristic requiredCharacteristic =
                                    mApplication.getBluetoothgattDatacharacteristic();
                            // mApplication.getBluetoothgattcharacteristic();
                            /**
                             * Checking the received characteristic UUID and Expected UUID are same
                             */
                            String uuidRequired = requiredCharacteristic.getUuid().toString();
                            String receivedUUID = intent.getStringExtra(
                                    Constants.EXTRA_DESCRIPTOR_BYTE_VALUE_CHARACTERISTIC_UUID);
                            if (uuidRequired.equalsIgnoreCase(receivedUUID)) {
                                if (extras.containsKey(Constants.
                                        EXTRA_BYTE_DESCRIPTOR_INSTANCE_VALUE)) {
                                    /**
                                     * Checking the received characteristic instance ID and Expected
                                     * characteristic instance ID
                                     */
                                    int requiredInstanceID = requiredCharacteristic.getInstanceId();
                                    int receivedInstanceID = intent.getIntExtra(Constants.
                                                    EXTRA_BYTE_DESCRIPTOR_INSTANCE_VALUE,
                                            -1);
                                    if (requiredInstanceID == receivedInstanceID) {
                                        byte[] array = intent.getByteArrayExtra(
                                                Constants.EXTRA_DESCRIPTOR_BYTE_VALUE);
                                        //updateButtonStatus(array);
                                    }
                                }

                            }

                        }
                    }
                }
                if (action.equals(BluetoothLeService.ACTION_GATT_CHARACTERISTIC_ERROR)) {
                    if (extras.containsKey(Constants.EXTRA_CHARACTERISTIC_ERROR_MESSAGE)) {
                        String errorMessage = extras.
                                getString(Constants.EXTRA_CHARACTERISTIC_ERROR_MESSAGE);
                        //displayAlertWithMessage(errorMessage);
                        //mAsciivalue.setText("");
                        //mHexValue.setText("");
                    }

                }
                if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                    final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);

                    if (state == BluetoothDevice.BOND_BONDING) {
                        // Bonding...
                        // Logger.i("Bonding is in process....");
                        //Utils.bondingProgressDialog(getActivity(), mProgressDialog, true);
                    } else if (state == BluetoothDevice.BOND_BONDED) {
                        // Bonded...
                    /*
                    String dataLog = getResources().getString(R.string.dl_commaseparator)
                            + "[" + BluetoothLeService.getmBluetoothDeviceName() + "|"
                            + BluetoothLeService.getmBluetoothDeviceAddress() + "]" +
                            getResources().getString(R.string.dl_commaseparator) +
                            getResources().getString(R.string.dl_connection_paired);
                    Logger.datalog(dataLog);
                    */
                        //Utils.bondingProgressDialog(getActivity(), mProgressDialog, false);
                        if (mIsIndicateEnabled) {
                            prepareBroadcastDataIndicate(mIndicateCharacteristic);
                        }
                        if (mIsNotifyEnabled) {
                            prepareBroadcastDataNotify(mNotifyCharacteristic);
                        }
                        if (mIsReadEnabled) {
                            prepareBroadcastDataRead(mReadCharacteristic);
                        }
                        if (mIsWriteEnabled) {
                            prepareBroadcastWrite(mApplication.getBluetoothgattGaincharacteristic());
                        }

                    } else if (state == BluetoothDevice.BOND_NONE) {
                    /*
                    String dataLog = getResources().getString(R.string.dl_commaseparator)
                            + "[" + BluetoothLeService.getmBluetoothDeviceName() + "|"
                            + BluetoothLeService.getmBluetoothDeviceAddress() + "]" +
                            getResources().getString(R.string.dl_commaseparator) +
                            getResources().getString(R.string.dl_connection_unpaired);
                    Logger.datalog(dataLog);
                    */
                    }
                }
            }

        }

    };

    /**
     * Preparing Broadcast receiver to broadcast read characteristics
     *
     * @param gattCharacteristic
     */
    void prepareBroadcastDataRead(BluetoothGattCharacteristic gattCharacteristic) {
        if ((gattCharacteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            BluetoothLeService.readCharacteristic(gattCharacteristic);
        }
    }

    public static void prepareBroadcastWrite(
            BluetoothGattCharacteristic gattCharacteristic) {
        if ((gattCharacteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
            //BluetoothLeService.writeCharacteristic(gattCharacteristic);
            BluetoothLeService.writeCharacteristicMyNoResponse(gattCharacteristic);
        }
    }

    /**
     * Preparing Broadcast receiver to broadcast notify characteristics
     *
     * @param gattCharacteristic
     */
    void prepareBroadcastDataNotify(BluetoothGattCharacteristic gattCharacteristic){
        if (( gattCharacteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            BluetoothLeService.setCharacteristicNotification( gattCharacteristic,
                    true);
        }
    }



    /**
     * Preparing Broadcast receiver to broadcast indicate characteristics
     *
     * @param gattCharacteristic
     */
    void prepareBroadcastDataIndicate(BluetoothGattCharacteristic gattCharacteristic) {
        if ((gattCharacteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
            BluetoothLeService.setCharacteristicIndication(
                    gattCharacteristic, true);
        }
    }

    public static Messenger mServiceMessenger;
    public static Messenger mFragmentMessenger;
    public static boolean isMessengerServiceConnected = false;

    public static ServiceConnection messengerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            isMessengerServiceConnected = true;

            mServiceMessenger = new Messenger(iBinder);

            Message message = Message.obtain();
            message.replyTo = mFragmentMessenger;
            //message.arg1 = Integer.parseInt(mIntervalEditText.getText().toString());
            //message.arg2 = Integer.parseInt(mDurationEditText.getText().toString());

            try{
                mServiceMessenger.send(message);
            }catch (RemoteException e){
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    public static void updatePowerLevel(int inputLevel, TextView inputTextView, Context ctx){
        Log.d(TAG,"updatePowerLevel inputLevel = "+inputLevel);
        if (intPrePowerLevel!= inputLevel){
            if (inputLevel == 2 && intPrePowerLevel <= 3){
                showMessageDialog(ctx.getString(R.string.alert_msg_low_power_charge), ctx);
            }
            intPrePowerLevel = inputLevel;
        }
        switch (inputLevel){
            case 0:  // <3.5v
                //inputTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_battery_alert_black_24dp,0,0,0);
                //break;
            case 1: // <3.6v
                //inputTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_battery_20_black_24dp,0,0,0);
                //break;
            case 2:  // <3.7v && >3.6v
                //inputTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_battery_30_black_24dp,0,0,0);
                inputTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_battery_alert_black_24dp,0,0,0);
                break;
            case 3:  // <3.8 && >3.7
                //inputTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_battery_50_black_24dp,0,0,0);
                inputTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_battery_20_black_24dp,0,0,0);
                break;
            case 4:  //  <3.9v && >3.8v
                //inputTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_battery_60_black_24dp,0,0,0);
                inputTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_battery_50_black_24dp,0,0,0);
                break;
            case 5:   //  <4.0v  && > 3.9v
                inputTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_battery_80_black_24dp,0,0,0);
                break;
            case 6:    //  < 4.1v  &&　> 4.0v
                           /* inputTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_battery_90_black_24dp,0,0,0);
                            break;*/
            case  7:   //   > 4.1v
                inputTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_battery_full_black_24dp,0,0,0);
                break;

            default:
                inputTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_battery_alert_black_24dp,0,0,0);
                break;
        }
    }

    private static void showMessageDialog(final String title, final Context ctx){
        AlertDialog alertIncorrectDialog = new AlertDialog.Builder(ctx).create();
        alertIncorrectDialog.setTitle(title);
        alertIncorrectDialog.setButton(Dialog.BUTTON_POSITIVE,
                ctx.getString(R.string.alert_message_exit_ok),
                new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Do nothing.
                    }
                });
        alertIncorrectDialog.show();

        final Button okBtn = alertIncorrectDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        okBtn.setBackgroundResource(R.drawable.block_b);
        okBtn.setTextColor(Color.WHITE);
        okBtn.setTextSize(18f);
        okBtn.setScaleX(0.60f);
        okBtn.setScaleY(0.60f);

    }


}
