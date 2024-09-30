package com.gis.CommonFragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
//import androidx.core.app.Fragment;
//import androidx.core.app.FragmentManager;

//import android.app.Fragment;
//import android.app.FragmentManager;
//import androidx.core.app.FragmentTransaction;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gis.BLEConnectionServices.BluetoothLeService;
import com.gis.CommonUtils.UUIDDatabase;
import com.gis.CommonUtils.Constants;
import com.gis.CommonUtils.Utils;
/*
import com.cypress.cysmart.CommonUtils.Logger;
*/
import com.gis.heartio.R;
import com.gis.heartio.UIOperationControlSubsystem.MainActivity;
import com.gis.heartio.UIOperationControlSubsystem.intervalFragment;
import com.gis.heartio.heartioApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.SystemConfig;
import com.gis.heartio.UIOperationControlSubsystem.onlineFragment;

/**
 * Created by smirno on 7/20/2015.
 */
public class ServiceDiscoveryFragment extends Fragment {
    // UUID key
    private static final String LIST_UUID = "UUID";
    // Stops scanning after 2 seconds.
    private static final long DELAY_PERIOD = 500; //500;
    private static final long SERVICE_DISCOVERY_TIMEOUT = 10000;
    static ArrayList<HashMap<String, BluetoothGattService>> mGattServiceData =
            new ArrayList<HashMap<String, BluetoothGattService>>();
    static ArrayList<HashMap<String, BluetoothGattService>> mGattServiceFindMeData =
            new ArrayList<HashMap<String, BluetoothGattService>>();
    static ArrayList<HashMap<String, BluetoothGattService>> mGattServiceProximityData =
            new ArrayList<HashMap<String, BluetoothGattService>>();
    static ArrayList<HashMap<String, BluetoothGattService>> mGattServiceSensorHubData =
            new ArrayList<HashMap<String, BluetoothGattService>>();
    private static ArrayList<HashMap<String, BluetoothGattService>> mGattdbServiceData =
            new ArrayList<HashMap<String, BluetoothGattService>>();
    private static ArrayList<HashMap<String, BluetoothGattService>> mGattServiceMasterData =
            new ArrayList<HashMap<String, BluetoothGattService>>();
    // Application
    private heartioApplication mApplication;
    private ProgressDialog mProgressDialog;
    private Timer mTimer;
    private TextView mNoserviceDiscovered;
    public  static boolean isInServiceFragment = false;

    //added by brandon
    private static BluetoothGattService mGattDataServiceSelected;
    private static BluetoothGattCharacteristic mGattDataCharacteristicSelected;
    private static BluetoothGattService mGattGainServiceSelected;
    private static BluetoothGattCharacteristic mGattGainCharacteristicSelected;
    private static BluetoothGattService mGattAngleEstimateServiceSelected;
    private static BluetoothGattCharacteristic mGattAngleEstimateCharacteristicSelected;
    private static List<BluetoothGattCharacteristic> mGattDataCharacteristics;
    private static List<BluetoothGattCharacteristic> mGattGainCharacteristics;
    private static List<BluetoothGattCharacteristic> mGattAngleEstimateCharacteristics;
    //private MainActivity mActivity;

    private final BroadcastReceiver mServiceDiscoveryListner=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
                    .equals(action)) {
                //Logger.e("Service discovered");
                if (mTimer != null)
                    mTimer.cancel();
                prepareGattServices(BluetoothLeService.getSupportedGattServices());

                /*
                                    / Changes the MTU size to 512 in case LOLLIPOP and above devices
                                */
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (SystemConfig.isHeartIO2){
//                        BluetoothLeService.exchangeGattMtu(127);
                        BluetoothLeService.exchangeGattMtu(247);
                    }else{
                        BluetoothLeService.exchangeGattMtu(512);
//                        BluetoothLeService.exchangeGattMtu(498);
                    }
                }
            } else if (BluetoothLeService.ACTION_GATT_SERVICE_DISCOVERY_UNSUCCESSFUL
                    .equals(action)) {
                mProgressDialog.dismiss();
                if (mTimer != null)
                    mTimer.cancel();
                showNoServiceDiscoverAlert();
            }

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;

        rootView = inflater.inflate(R.layout.servicediscovery_temp_fragment, container, false);
        mNoserviceDiscovered = rootView.findViewById(R.id.no_service_text);
        mProgressDialog = new ProgressDialog(getActivity());
        mTimer = showServiceDiscoveryAlert(false);
        mApplication = (heartioApplication) getActivity().getApplication();
        Handler delayHandler = new Handler();
        delayHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Logger.e("Discover service called");
                if (BluetoothLeService.getConnectionState() == BluetoothLeService.STATE_CONNECTED)
                    BluetoothLeService.discoverServices();
            }
        }, DELAY_PERIOD);
        setHasOptionsMenu(true);

        //added by brandon
        //mActivity = (MainActivity) getActivity();

        return rootView;
    }

    private Timer showServiceDiscoveryAlert(boolean isReconnect) {
        Timer timer;

        mProgressDialog.setTitle(getString(R.string.progress_tile_service_discovering));
        if (!isReconnect) {
            mProgressDialog.setMessage(getString(R.string.progress_message_service_discovering));
        } else {
            mProgressDialog.setMessage(getString(R.string.progress_message_reconnect));
        }
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.show();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                    mNoserviceDiscovered.post(new Runnable() {
                        @Override
                        public void run() {
                            mNoserviceDiscovered.setVisibility(View.VISIBLE);
                        }
                    });
                }

            }
        }, SERVICE_DISCOVERY_TIMEOUT);
        return timer;
    }

    /**
     * Getting the GATT Services
     *
     * @param gattServices
     */
    private void prepareGattServices(List<BluetoothGattService> gattServices) {
       // try {
            // Optimization code for Sensor HUb
            //if (isSensorHubPresent(gattServices)) {
            //    prepareSensorHubData(gattServices);
            //} else {
            prepareData(gattServices);
            //}
      //  }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("ServDiscFrag.prepareGattService.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
      //  }

    }
    /**
     * Check whether SensorHub related services are present in the discovered
     * services
     *
     * @param gattServices
     * @return {@link Boolean}
     */
    boolean isSensorHubPresent(List<BluetoothGattService> gattServices) {
        boolean present = false;
        for (BluetoothGattService gattService : gattServices) {
            UUID uuid = gattService.getUuid();
            if (uuid.equals(UUIDDatabase.UUID_BAROMETER_SERVICE)) {
                present = true;
            }
        }
        return present;
    }
    private void prepareSensorHubData(List<BluetoothGattService> gattServices) {

        boolean mGattSet = false;
        boolean mSensorHubSet = false;

        if (gattServices == null)
            return;
        // Clear all array list before entering values.
        mGattServiceData.clear();
        mGattServiceMasterData.clear();
        mGattServiceSensorHubData.clear();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, BluetoothGattService> mCurrentServiceData = new HashMap<String, BluetoothGattService>();
            UUID uuid = gattService.getUuid();
            // Optimization code for SensorHub Profile
            if (uuid.equals(UUIDDatabase.UUID_LINK_LOSS_SERVICE)
                    || uuid.equals(UUIDDatabase.UUID_TRANSMISSION_POWER_SERVICE)
                    || uuid.equals(UUIDDatabase.UUID_IMMEDIATE_ALERT_SERVICE)
                    || uuid.equals(UUIDDatabase.UUID_BAROMETER_SERVICE)
                    || uuid.equals(UUIDDatabase.UUID_ACCELEROMETER_SERVICE)
                    || uuid.equals(UUIDDatabase.UUID_ANALOG_TEMPERATURE_SERVICE)
                    || uuid.equals(UUIDDatabase.UUID_BATTERY_SERVICE)
                    || uuid.equals(UUIDDatabase.UUID_DEVICE_INFORMATION_SERVICE)) {
                mCurrentServiceData.put(LIST_UUID, gattService);
                mGattServiceMasterData.add(mCurrentServiceData);
                if (!mGattServiceSensorHubData.contains(mCurrentServiceData)) {
                    mGattServiceSensorHubData.add(mCurrentServiceData);
                }
                if (!mSensorHubSet
                        && uuid.equals(UUIDDatabase.UUID_BAROMETER_SERVICE)) {
                    mSensorHubSet = true;
                    mGattServiceData.add(mCurrentServiceData);
                }

            }
            // Optimization code for GATTDB
            else if (uuid
                    .equals(UUIDDatabase.UUID_GENERIC_ACCESS_SERVICE)
                    || uuid.equals(UUIDDatabase.UUID_GENERIC_ATTRIBUTE_SERVICE)) {
                mCurrentServiceData.put(LIST_UUID, gattService);
                mGattdbServiceData.add(mCurrentServiceData);
                if (!mGattSet) {
                    mGattSet = true;
                    mGattServiceData.add(mCurrentServiceData);
                }

            } else {
                mCurrentServiceData.put(LIST_UUID, gattService);
                mGattServiceMasterData.add(mCurrentServiceData);
                mGattServiceData.add(mCurrentServiceData);
            }
        }
        mApplication.setGattServiceMasterData(mGattServiceMasterData);
        if(mGattdbServiceData.size()>0){
            updateWithProfileControlFragment();
        }else{
            //Logger.e("No service found");
            mProgressDialog.dismiss();
            showNoServiceDiscoverAlert();
        }
    }

    private void updateWithProfileControlFragment() {
       // try {
            mProgressDialog.dismiss();
            SystemConfig.mEnumUltrasoundSubUIState = SystemConfig.ENUM_SUB_UI_STATE.PROFILE_CONTROL;

            FragmentManager fragmentManager = getFragmentManager();
            ProfileControlFragment profileControlFragment = new ProfileControlFragment().
                    create(BluetoothLeService.getmBluetoothDeviceName(),
                            BluetoothLeService.getmBluetoothDeviceAddress());
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.container, profileControlFragment,
                            Constants.PROFILE_CONTROL_FRAGMENT_TAG)
                    .commit();
       // }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("ServDiscFrag.updateWithProfControlFrag.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
      //  }

    }

    private void updateWithUltrasoundFragment() {
        mProgressDialog.dismiss();

        SystemConfig.mEnumUltrasoundSubUIState = SystemConfig.ENUM_SUB_UI_STATE.BLOOD_FLOW_VELOCITY;

        //FragmentManager fragmentManager = getFragmentManager();
        // ItriWearUltrasoundFragment itriWearUltrasoundFragment = new ItriWearUltrasoundFragment();

        Fragment oFragment = null;
        //String fragmentTag = "";
        if (MainActivity.indexOfCurrentNavigationItem == 1) {
            SystemConfig.mEnumUltrasoundUIState = SystemConfig.ENUM_UI_STATE.ULTRASOUND_UI_STATE_ONLINE;
            oFragment = onlineFragment.newInstance();
            MainActivity.currentFragmentTag = Constants.ITRI_ULTRASOUND_FRAGMENT_TAG;
        }else if (MainActivity.indexOfCurrentNavigationItem == 2){
            SystemConfig.mEnumUltrasoundUIState = SystemConfig.ENUM_UI_STATE.ULTRASOUND_UI_STATE_ONLINE;
            oFragment = intervalFragment.newInstance();
            MainActivity.currentFragmentTag = Constants.INTERVAL_FRAGMENT_TAG;
        }
        //fragmentManager.beginTransaction().replace(R.id.container, itriWearUltrasoundFragment, Constants.ITRI_ULTRASOUND_FRAGMENT_TAG).commit();
        //fragmentManager.beginTransaction().replace(R.id.container, oFragment , Constants.ITRI_ULTRASOUND_FRAGMENT_TAG).commit();
        if (getActivity()!=null){
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_layout, oFragment,MainActivity.currentFragmentTag);
            transaction.commit();
        }
    }
    /**
     * Prepare GATTServices data.
     *
     * @param gattServices
     */
    private void prepareData(List<BluetoothGattService> gattServices) {
        boolean mFindmeSet = false;
        boolean mProximitySet = false;
        boolean mGattSet = false;

        //try {
            if (gattServices == null)
                return;
            // Clear all array list before entering values.
            mGattServiceData.clear();
            mGattServiceFindMeData.clear();
            mGattServiceMasterData.clear();

            // Loops through available GATT Services.
            for (BluetoothGattService gattService : gattServices) {
                HashMap<String, BluetoothGattService> currentServiceData = new HashMap<String, BluetoothGattService>();
                UUID uuid = gattService.getUuid();

                if (uuid.equals(UUIDDatabase.UUID_GENERIC_ACCESS_SERVICE)
                        || uuid.equals(UUIDDatabase.UUID_GENERIC_ATTRIBUTE_SERVICE)) {
                    currentServiceData.put(LIST_UUID, gattService);
                    mGattServiceData.add(currentServiceData);
                } else {
                    currentServiceData.put(LIST_UUID, gattService);
                    mGattServiceData.add(currentServiceData);
                    mGattServiceMasterData.add(currentServiceData);
                }
            }
            mApplication.setGattServiceMasterData(mGattServiceMasterData);
            if (mGattServiceData.size() > 0) {
                //modified by brandon
                //updateWithNewFragment();
                if (!checkUltrasoundService()) {
                    mProgressDialog.dismiss();
                    showNoServiceDiscoverAlert();
                } else {
                    mProgressDialog.dismiss();
                    //ProfileScanningFragment profileScanFragment = new ProfileScanningFragment();
                    //getFragmentManager().beginTransaction().replace(R.id.)
                    //showGoBackToProfileScanAlert();
                    updateWithUltrasoundFragment();
                }
            } else {
                mProgressDialog.dismiss();
                showNoServiceDiscoverAlert();
            }
      //  }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("ServDiscFrag.prepareData.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
      //  }
    }

    private void showNoServiceDiscoverAlert() {
       // try {
            if (mNoserviceDiscovered != null) {
                mNoserviceDiscovered.setText("No Wearable Ultrasound Service is Found !");
                mNoserviceDiscovered.setVisibility(View.VISIBLE);
            }
       // }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("ServDiscFrag.showNoServDiscAlert.Exception","");
           // SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
       // }
    }

    @Override
    public void onPause() {
        super.onPause();
        isInServiceFragment = false;
        try {
            getActivity().unregisterReceiver(mServiceDiscoveryListner);
       }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("ServDiscFrag.onPause.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            ex1.printStackTrace();
       }
    }

    @Override
    public void onResume() {
      //  try {
            super.onResume();
            //Logger.e("Service discovery onResume");
            isInServiceFragment = true;
            getActivity().registerReceiver(mServiceDiscoveryListner,
                    Utils.makeGattUpdateIntentFilter());
            // Initialize ActionBar as per requirement
            //Utils.setUpActionBar(getActivity(),
            //        getResources().getString(R.string.profile_control_fragment));
       // }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("ServDiscFrag.onResume.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
      //  }

    }

    private final Runnable runnableSDFrag = new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            msg.what=1;
            mHandler.sendMessage(msg);
        }
    };

    private final Handler mHandler = new Handler(){
        Activity activity;
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                activity=getActivity();
                getActivity().onBackPressed();
            }
        }
    };


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
       // try {
            menu.clear();
            //inflater.inflate(R.menu.global, menu);
      //  }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("ServDiscFrag.onCreateOptionMenu.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
       // }
    }


    //added by brandon
    private boolean checkUltrasoundService(){
        if (!checkUltrasoundDataService()) {
            return false;
        }

        if (SystemConfig.mIntGainControlEnabled == SystemConfig.INT_GAIN_CONTROL_ENABLED_YES) {
            if (!checkGainService()) {
                return false;
            }
        }

        return true;
    }


    private boolean checkGainService(){
        boolean boolServiceFound = false;
        boolean boolCharacteristicFound = false;
        BluetoothGattService oGattService;
        UUID uuid;
        for (int i = 0; i < mGattServiceData.size(); i++) {
            if (mGattServiceData.get(i).get("UUID").getUuid().equals(UUIDDatabase.UUID_ULTRASOUND_GAIN_CONTROL_SERVICE)||
                    mGattServiceData.get(i).get("UUID").getUuid().equals(UUIDDatabase.UUID_HEARTIO_DATA_SERVICE)) {
                boolServiceFound = true;
                mGattGainServiceSelected = mGattServiceData.get(i).get("UUID");
                break;
            }
        }

        if (!boolServiceFound) {
            return false;
        }

        mGattGainCharacteristics = mGattGainServiceSelected.getCharacteristics();
        mApplication.setGattGainCharacteristics(mGattGainCharacteristics);
        for (BluetoothGattCharacteristic gattCharacteristic : mGattGainCharacteristics) {
            uuid = gattCharacteristic.getUuid();
            if (uuid.equals(UUIDDatabase.UUID_ULTRASOUND_GAIN_CONTROL_CHARACTERISTIC) ||
                uuid.equals(UUIDDatabase.UUID_HEARTIO_GAIN_CHARACTERISTIC)) {
                boolCharacteristicFound = true;
                mGattGainCharacteristicSelected = gattCharacteristic;
                break;
            }
        }

        if (!boolCharacteristicFound) {
            return false;
        }

        mApplication.setBluetoothgattGaincharacteristic(mGattGainCharacteristicSelected);
        return true;
    }


    private boolean checkUltrasoundDataService(){
        boolean boolDataServiceFound = false;
        boolean boolDataCharacteristicFound = false;
        BluetoothGattService oGattService;
        UUID uuid;
        String strUuid;
        String strDeviiceName, strDeviceAddress;
        strDeviiceName = BluetoothLeService.getmBluetoothDeviceName();
        strDeviceAddress = BluetoothLeService.getmBluetoothDeviceAddress();

        for (int i = 0; i < mGattServiceData.size(); i++) {
            strUuid = mGattServiceData.get(i).get("UUID").getUuid().toString();
            if (mGattServiceData.get(i).get("UUID").getUuid().equals(UUIDDatabase.UUID_ULTRASOUND_DATA_SERVICE)||
                mGattServiceData.get(i).get("UUID").getUuid().equals(UUIDDatabase.UUID_HEARTIO_DATA_SERVICE)) {
                boolDataServiceFound = true;
                mGattDataServiceSelected = mGattServiceData.get(i).get("UUID");
                //break;
            } else if (mGattServiceData.get(i).get("UUID").getUuid().equals(UUIDDatabase.UUID_IMMEDIATE_ALERT_SERVICE) ||
                    mGattServiceData.get(i).get("UUID").getUuid().equals(UUIDDatabase.UUID_DEVICE_INFORMATION_SERVICE)){
                List<BluetoothGattCharacteristic> mCharacteristics = mGattServiceData.get(i).get("UUID").getCharacteristics();
                mApplication.setGattDeviceInfoCharacteristics(mCharacteristics);
                for (BluetoothGattCharacteristic gattCharacteristic : mCharacteristics){
                    uuid = gattCharacteristic.getUuid();
                    if (uuid.equals(UUIDDatabase.UUID_FIRMWARE_REVISION_STRING)){
                        mApplication.setBluetoothgattFirmwareVersionharacteristic(gattCharacteristic);
                    } else if (uuid.equals(UUIDDatabase.UUID_SOFTWARE_REVISION_STRING)){
                        mApplication.setBluetoothgattMacAddrcharacteristic(gattCharacteristic);
                    }
                }
            }
        }

        if (!boolDataServiceFound) {
            return false;
        }

        mGattDataCharacteristics = mGattDataServiceSelected.getCharacteristics();
        mApplication.setGattDataCharacteristics(mGattDataCharacteristics);
        for (BluetoothGattCharacteristic gattCharacteristic : mGattDataCharacteristics) {
            uuid = gattCharacteristic.getUuid();
            if (uuid.equals(UUIDDatabase.UUID_ULTRASOUND_DATA_CHARACTERISTIC)||
                uuid.equals(UUIDDatabase.UUID_HEARTIO_DATA_CHARACTERISTIC)) {
                boolDataCharacteristicFound = true;
                mGattDataCharacteristicSelected = gattCharacteristic;
            } else if (uuid.equals((UUIDDatabase.UUID_HEARTIO_UDI_PARA_CHARACTERISTIC))){
                mApplication.setBluetoothgattUdiParacharacteristic(gattCharacteristic);
            }


            // For HeartIO2
            if(SystemConfig.isHeartIO2 ){
                if (uuid.equals(UUIDDatabase.UUID_HEARTIO2_DATA_CHARACTERISTIC)){
                    mGattDataCharacteristicSelected = gattCharacteristic;
                } else if (uuid.equals(UUIDDatabase.UUID_HEARTIO2_RUNCMD_CHARACTERISTIC)){
                    mApplication.setBluetoothgattRuncharacteristic(gattCharacteristic);
                }

            }
        }

        if (!boolDataCharacteristicFound) {
            return false;
        }

        mApplication.setBluetoothgattDatacharacteristic(mGattDataCharacteristicSelected);
        return true;
    }


    private void showGoBackToProfileScanAlert() {
        mNoserviceDiscovered.setText("please go back to scan device !");
            mNoserviceDiscovered.setVisibility(View.VISIBLE);
    }
}
