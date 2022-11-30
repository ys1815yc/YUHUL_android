/*
 * Copyright Cypress Semiconductor Corporation, 2014-2015 All rights reserved.
 *
 * This software, associated documentation and materials ("Software") is
 * owned by Cypress Semiconductor Corporation ("Cypress") and is
 * protected by and subject to worldwide patent protection (UnitedStates and foreign), United States copyright laws and international
 * treaty provisions. Therefore, unless otherwise specified in a separate license agreement between you and Cypress, this Software
 * must be treated like any other copyrighted material. Reproduction,
 * modification, translation, compilation, or representation of this
 * Software in any other form (e.g., paper, magnetic, optical, silicon)
 * is prohibited without Cypress's express written permission.
 *
 * Disclaimer: THIS SOFTWARE IS PROVIDED AS-IS, WITH NO WARRANTY OF ANY
 * KIND, EXPRESS OR IMPLIED, INCLUDING, BUT NOT LIMITED TO,
 * NONINFRINGEMENT, IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE. Cypress reserves the right to make changes
 * to the Software without notice. Cypress does not assume any liability
 * arising out of the application or use of Software or any product or
 * circuit described in the Software. Cypress does not authorize its
 * products for use as critical components in any products where a
 * malfunction or failure may reasonably be expected to result in
 * significant injury or death ("High Risk Product"). By including
 * Cypress's product in a High Risk Product, the manufacturer of such
 * system or application assumes all risk of such use and in doing so
 * indemnifies Cypress against all liability.
 *
 * Use of this Software may be limited by and subject to the applicable
 * Cypress software license agreement.
 *
 *
 */

package com.gis.CommonFragments;

//import android.app.ActionBar;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

//import androidx.core.app.Fragment;
//import androidx.core.app.FragmentManager;
//import android.support.v4.widget.SwipeRefreshLayout;
//import androidx.core.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

//import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
//import android.widget.Filter;
//import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gis.BLEConnectionServices.BluetoothLeService;
import com.gis.CommonUtils.UUIDDatabase;
import com.gis.heartio.R;
import com.gis.heartio.UIOperationControlSubsystem.MainActivity;
import com.gis.CommonUtils.Constants;
import com.gis.CommonUtils.Utils;
/*
import com.cypress.cysmart.CommonUtils.Logger;
*/

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
//import android.support.v7.app.AppCompatActivity;

import com.gis.heartio.SupportSubsystem.SystemConfig;

public class ProfileScanningFragment extends Fragment {
    private static final String TAG = "ProfileScanningFragment";
    // Stops scanning after 6 seconds.  HeartIO2 at least 5 seconds
    private static final long SCAN_PERIOD_TIMEOUT = 3000;
    private Timer mScanTimer;
    private boolean mScanning;

    // Connection time out after 10 seconds.
    private static final long CONNECTION_TIMEOUT = 10000;
    private Timer mConnectTimer;
    private boolean mConnectTimerON = false;

    // Activity request constant
    private static final int REQUEST_ENABLE_BT = 1;

    // device details
    public static String mDeviceName = "name";
    public static String mDeviceAddress = "address";

    //Pair status button and variables
    public static Button mPairButton;

    //Bluetooth adapter
    private static BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mLEScanner;
    //private List<ScanFilter> filters;
    private ScanSettings settings;

    // Devices list variables
    private static ArrayList<BluetoothDevice> mLeDevices;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    //private SwipeRefreshLayout mSwipeLayout;
    private Map<String, Integer> mDevRssiValues;

    //GUI elements
    private ListView mProfileListView;
    //private TextView mRefreshText;
    private ProgressDialog mProgressdialog;

    //  Flags
    private boolean mSearchEnabled = false;
    public static boolean isInFragment = false;

    //Delay Time out
    private static final long DELAY_PERIOD = 500;

    private Button mBtnScan;

    public static ProfileScanningFragment newInstance() {
        ProfileScanningFragment fragment = new ProfileScanningFragment();
        return fragment;
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            /* Connect to device found */
            //Log.i("callbackType", String.valueOf(callbackType));
            final BluetoothDevice btDevice = result.getDevice();
            //connectToDevice(btDevice);
            MainActivity mActivity = (MainActivity) getActivity();
            if (mActivity != null) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!mSearchEnabled) {
                            if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                if (Build.VERSION.SDK_INT >= 31 ) {
                                    return;
                                }
                            }
                            if (btDevice.getName() != null &&
                                    (btDevice.getName().contains("HeartIO") || btDevice.getName().contains("ITRI") ||
                                            btDevice.getName().contains("AD8232") || btDevice.getName().toLowerCase(Locale.ROOT).contains("yuhul"))) {
                                //Log.d("scanResult","device name= " + btDevice.getName());
                                mLeDeviceListAdapter.addDevice(btDevice, result.getRssi());
                            }

                            try {
                                mLeDeviceListAdapter.notifyDataSetChanged();
                                //updateProfileListView();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            //Process a batch scan results
            for (ScanResult sr : results) {
                Log.i("Scan Item: ", sr.toString());
            }
        }

    };


    /**
     * Call back for BLE Scan
     * This call back is called when a BLE device is found near by.
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi,
                             byte[] scanRecord) {
            MainActivity mActivity = (MainActivity) getActivity();
            if (mActivity != null) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!mSearchEnabled) {
                            mLeDeviceListAdapter.addDevice(device, rssi);
                            try {
                                mLeDeviceListAdapter.notifyDataSetChanged();
                                //updateProfileListView();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }

        }
    };

    /**
     * BroadcastReceiver for receiving the GATT communication status
     */
    private final BroadcastReceiver mGattConnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            // Status received when connected to GATT Server
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                //SystemConfig.mMyEventLogger.appendDebugStr("Connected received","");
                mProgressdialog.setMessage(getString(R.string.alert_message_bluetooth_connect));
                if (mScanning) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        if (Build.VERSION.SDK_INT >= 31 ) {
                            return;
                        }
                    }
                    if (Build.VERSION.SDK_INT < 21) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        if (mLEScanner != null) {
                            mLEScanner.stopScan(mScanCallback);
                        }
                    }

                    mScanning = false;
                }
                mProgressdialog.dismiss();
                mLeDevices.clear();
                mDevRssiValues.clear();
                if (mConnectTimer != null)
                    mConnectTimer.cancel();
                mConnectTimerON = false;
                updateWithNewFragment();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                /**
                            * Disconnect event.When the connect timer is ON,Reconnect the device
                            * else show disconnect message
                            */
                if (mConnectTimerON) {
                    BluetoothLeService.reconnect();
                } else {
                    Toast.makeText(getActivity(),
                            R.string.profile_cannot_connect_message,
                            Toast.LENGTH_SHORT).show();
                }
            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                final int bleState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR);
                switch (bleState){
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG,"Trun on bluetooth!!!!!!!!!");
                        mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                        break;
                }
            }

        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mrootView;

        mrootView = inflater.inflate(R.layout.fragment_profile_scan, container,
                false);

        if (((AppCompatActivity)getActivity())!=null){
            ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        System.gc();

        //try {
            mDevRssiValues = new HashMap<String, Integer>();

            mProfileListView = (ListView) mrootView
                    .findViewById(R.id.listView_profiles);
            mLeDeviceListAdapter = new LeDeviceListAdapter();
            mProfileListView.setAdapter(mLeDeviceListAdapter);
            //mProfileListView.setTextFilterEnabled(false);
            setHasOptionsMenu(true);

            mProgressdialog = new ProgressDialog(getActivity());
            mProgressdialog.setCancelable(false);

            mBtnScan = (Button) mrootView.findViewById(R.id.btnScan);
            mBtnScan.setOnClickListener(btnScanOnClick);

            checkBleSupportAndInitialize();
            prepareNullList();
            //prepareList();

            /**
             * Swipe listener,initiate a new scan on refresh. Stop the swipe refresh
             * after 5 seconds
             */
            /**
             * Creating the dataLogger file and
             * updating the datalogger history
             */
            //Logger.createDataLoggerFile(getActivity());
            mProfileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    if (mLeDeviceListAdapter.getCount() > 0) {
                        final BluetoothDevice device = mLeDeviceListAdapter
                                .getDevice(position);
                        if (device != null) {
                            scanLeDevice(false);
                            connectDevice(device, true,false);
                            /*
                            if (getContext()!=null){
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle(getString(R.string.alert_msg_autoconnect_to_device));
                                builder.setPositiveButton(getString(R.string.alert_message_yes), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        connectDevice(device, true,true);
                                    }
                                });

                                builder.setNegativeButton(getString(R.string.alert_message_no), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        connectDevice(device, true,false);
                                        dialog.cancel();
                                    }
                                });

                                AlertDialog alert = builder.create();
                                alert.show();

                                final Button yesBtn = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                                yesBtn.setBackgroundResource(R.drawable.block_b);
                                yesBtn.setTextColor(Color.WHITE);
                                yesBtn.setTextSize(18f);
                                yesBtn.setScaleX(0.60f);
                                yesBtn.setScaleY(0.60f);

                                final Button noBtn = alert.getButton(AlertDialog.BUTTON_NEGATIVE);
                                noBtn.setBackgroundResource(R.drawable.block_g2);
                                noBtn.setTextColor(Color.parseColor("#804A4A4A"));
                                noBtn.setTextSize(18f);
                                noBtn.setScaleX(0.60f);
                                noBtn.setScaleY(0.60f);
                                noBtn.setPadding(100,5,100,5);
                            }
                            */


                        }
                    }
                }
            });

            return mrootView;
        //}catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("ProfileScan.onCreateView.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        //    return mrootView;
       // }
    }

    private final View.OnClickListener btnScanOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
           // try {
                if (!mScanning) {
                    //mBtnScan.setVisibility(View.GONE);
                    // Prepare list view and initiate scanning
                    if (mLeDeviceListAdapter != null) {
                        mLeDeviceListAdapter.clear();
                        try {
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    scanLeDevice(true);
                    mScanning = true;
                    mSearchEnabled = false;
                    //mRefreshText.setText(getResources().getString(
                    //        R.string.profile_control_device_scanning));
                }
           // }catch(Exception ex1){
               // SystemConfig.mMyEventLogger.appendDebugStr("ProfileScan.btnScanClick.Exception","");
               // SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
           // }
        }
    };

    private void checkBleSupportAndInitialize() {
        // Use this check to determine whether BLE is supported on the device.
        if (!getActivity().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(getActivity(), R.string.device_ble_not_supported,
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        // Initializes a Blue tooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager) getActivity()
                .getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Blue tooth
            Toast.makeText(getActivity(),
                    R.string.device_bluetooth_not_supported, Toast.LENGTH_SHORT)
                   .show();
                getActivity().finish();
        }
        if (!mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.enable();
        }else{
            mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        }

    }

    /**
     * Method to connect to the device selected. The time allotted for having a
     * connection is 8 seconds. After 8 seconds it will disconnect if not
     * connected and initiate scan once more
     *
     * @param device
     */

    private void connectDevice(BluetoothDevice device,boolean isFirstConnect,final boolean bAutoConnect) {

        mDeviceAddress = device.getAddress();
        mDeviceName = device.getName();
        // Get the connection status of the device
        if (BluetoothLeService.getConnectionState() == BluetoothLeService.STATE_DISCONNECTED) {
            // Logger.v("BLE DISCONNECTED STATE");
            // Disconnected,so connect
            BluetoothLeService.connect(mDeviceAddress, mDeviceName, getActivity(), bAutoConnect);
            showConnectAlertMessage(mDeviceName, mDeviceAddress);
        } else {
            // Logger.v("BLE OTHER STATE-->" + BluetoothLeService.getConnectionState());
            // Connecting to some devices,so disconnect and then connect
            BluetoothLeService.disconnect();
            Handler delayHandler = new Handler();
            delayHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    BluetoothLeService.connect(mDeviceAddress, mDeviceName, getActivity(), bAutoConnect);
                    //    SystemConfig.mDeviceAddressConnected = mDeviceAddress;
                   //     SystemConfig.mDeviceNameConnected = mDeviceName;
                    showConnectAlertMessage(mDeviceName, mDeviceAddress);
                }
             }, DELAY_PERIOD);

        }
        SystemConfig.isHeartIO2 = mDeviceName.contains("AD8232") || mDeviceName.contains("HeartIO2");

        if (isFirstConnect) {
            startConnectTimer();
            mConnectTimerON = true;
        }

    }

    private void showScanAlertMessage(){
       // mProgressdialog.setTitle(getResources().getString(
        //        R.string.profile_control_device_scanning));
        mProgressdialog.setMessage(getString(R.string.profile_control_device_scanning));


        if (!getActivity().isDestroyed() && mProgressdialog != null) {
            mProgressdialog.show();
        }
    }

    private void showConnectAlertMessage(String devicename,String deviceaddress) {
        mProgressdialog.setTitle(getResources().getString(
                R.string.alert_message_connect_title));
        mProgressdialog.setMessage(getResources().getString(
                R.string.alert_message_connect)
                + "\n"
                + devicename
                + "\n"
                + deviceaddress
                + "\n"
                + getResources().getString(R.string.alert_message_wait));

        if (!getActivity().isDestroyed() && mProgressdialog != null) {
            mProgressdialog.show();
        }
    }

    /**
     * Method to scan BLE Devices. The status of the scan will be detected in
     * the BluetoothAdapter.LeScanCallback
     *
     * @param enable
     */
    private void scanLeDevice(final boolean enable) {
       // try {
            if (enable) {
                if (!mScanning) {
                    showScanAlertMessage();
                    startScanTimer();
                    mScanning = true;
                    //mRefreshText.setText(getResources().getString(
                    //        R.string.profile_control_device_scanning));
                    if (Build.VERSION.SDK_INT < 21){
                        mBluetoothAdapter.startLeScan(mLeScanCallback);
                    }else{
                        if (mLEScanner==null){
                            mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                        }
                        List<ScanFilter> filters = new ArrayList<>();
                        //scan specified devices only with ScanFilter
                        ScanFilter scanFilter =
                               new ScanFilter.Builder()
                                     //  .setServiceUuid(new ParcelUuid(UUIDDatabase.UUID_ULTRASOUND_DATA_SERVICE))
                                       .setServiceUuid(UUIDDatabase.PARCEL_UUID_ULTRASOUND_DATA_SERVICE)
                                       .build();
                        ScanFilter scanFilter1 =
                                new ScanFilter.Builder()
                                    .setServiceUuid(UUIDDatabase.PARCEL_UUID_HEARTIO_DATA_SERVICE)
                                    .build();

                        //filters.add(scanFilter);
                        //filters.add(scanFilter1);
                        settings = new ScanSettings.Builder()
                                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                                .build();
                        if (mLEScanner!=null){
                            //mLEScanner.startScan(Collections.singletonList(scanFilter),settings,mScanCallback);
                            mLEScanner.startScan(filters,settings,mScanCallback);
                        }

                    }

                    //mSwipeLayout.setRefreshing(true);
                }
            } else {
                mScanning = false;
                //mSwipeLayout.setRefreshing(false);
                if (Build.VERSION.SDK_INT <21){
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }else{
                    if (mLEScanner!=null){
                        mLEScanner.stopScan(mScanCallback);
                        mLEScanner.flushPendingScanResults(mScanCallback);
                    }
                }
                if (getActivity()!=null){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mProgressdialog != null && mProgressdialog.isShowing()) {
                                mProgressdialog.dismiss();
                            }
                        }
                    });
                }
            }
       // }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("ProfileScan.scanLeDevice.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
      //  }

    }

    /**
     * Preparing the BLE Devicelist
     */

    public void prepareNullList() {     // By Brandon
       // try {
            // Initializes ActionBar as required
            //setUpActionBar();
            // Prepare list view and initiate scanning
            mLeDevices.clear();
            mDevRssiValues.clear();
            mLeDeviceListAdapter = new LeDeviceListAdapter();
            mProfileListView.setAdapter(mLeDeviceListAdapter);
            //scanLeDevice(true);
            //mSearchEnabled = false;
            mSearchEnabled = true;
        //}catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("ProfileScan.prepareNullList.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
       // }
    }


    public void prepareList() {
        // Initializes ActionBar as required
        //setUpActionBar();
        // Prepare list view and initiate scanning
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        mProfileListView.setAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);
        mSearchEnabled = false;
    }

    @Override
    public void onResume() {
       // try {
            super.onResume();
            //Logger.e("Scanning onResume");
            isInFragment = true;
            if (checkBluetoothStatus()) {
                prepareNullList();    // By Brandon
                //prepareList();    // By Brandon
            }
            //Logger.e("Registering receiver in Profile scannng");
            getActivity().registerReceiver(mGattConnectReceiver,
                    Utils.makeGattUpdateIntentFilter());
       // }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("ProfileScan.onResume.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
       // }

    }

    @Override
    public void onPause() {
        try {
            //Logger.e("Scanning onPause");
            isInFragment = false;
            if (mProgressdialog != null && mProgressdialog.isShowing()) {
                mProgressdialog.dismiss();
            }
            //Logger.e("UN Registering receiver in Profile scannng");
            getActivity().unregisterReceiver(mGattConnectReceiver);
            super.onPause();
       }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("ProfileScan.onPause.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            ex1.printStackTrace();
       }
    }

    @Override
    public void onDestroy() {
       // String strMsg ;

     //   try {
            super.onDestroy();  // add by brandon
            scanLeDevice(false);
            isInFragment = false;
            if (mLeDeviceListAdapter != null)
                mLeDeviceListAdapter.clear();
            if (mLeDeviceListAdapter != null) {
                try {
                    mLeDeviceListAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
           // try {   // added by brandon
                //mSwipeLayout.setRefreshing(false);
           // } catch (Exception ex1) {
           //     strMsg = ex1.toString();
           // }
         //   super.onDestroy();
    //    }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("ProfileScan.onDestroy.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
    //    }

    }

    private void updateWithNewFragment() {
        if (mLeDeviceListAdapter != null) {
            mLeDeviceListAdapter.clear();
            try {
                mLeDeviceListAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //  getActivity().unregisterReceiver(mGattConnectReceiver);
        SystemConfig.mEnumUltrasoundSubUIState = SystemConfig.ENUM_SUB_UI_STATE.SERVICE_DISCOVERY;
        FragmentManager fragmentManager = getFragmentManager();
        ServiceDiscoveryFragment serviceDiscoveryFragment = new ServiceDiscoveryFragment();
        fragmentManager.beginTransaction().remove(getFragmentManager().
                findFragmentByTag(Constants.PROFILE_SCANNING_FRAGMENT_TAG)).commit();
        fragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, serviceDiscoveryFragment,
                        //.replace(R.id.container, serviceDiscoveryFragment,
                        Constants.SERVICE_DISCOVERY_FRAGMENT_TAG)
                .commit();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
       // try {
            menu.clear();
            //inflater.inflate(R.menu.global, menu);

            super.onCreateOptionsMenu(menu, inflater);
       // }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("ProfileScan.onCreateOptionMenu.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
       // }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    /**
     * Setting up the ActionBar
     */
    /*void setUpActionBar() {
       // try {
            ActionBar actionBar = getActivity().getActionBar();
            if (actionBar != null) {
                actionBar.setIcon(new ColorDrawable(getResources().getColor(
                        android.R.color.transparent)));
            }
            if (actionBar != null) {
                actionBar.setTitle(R.string.profile_scan_fragment);
            }
        //}catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("ProfileScan.setUpActBar.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
       // }
    }*/

    //For Pairing
    private void pairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("createBond", (Class[]) null);
            m.invoke(device, (Object[]) null);

        }catch(Exception ex1){
           // SystemConfig.mMyEventLogger.appendDebugStr("ProfileScan.pairDevice.Exception","");
           // SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            if (mProgressdialog != null && mProgressdialog.isShowing()) {
                mProgressdialog.dismiss();
            }
        }

    }

    //For UnPairing
    private void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);

        }catch(Exception ex1){
           // SystemConfig.mMyEventLogger.appendDebugStr("ProfileScan.unpairDevice.Exception","");
           // SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            if (mProgressdialog != null && mProgressdialog.isShowing()) {
                mProgressdialog.dismiss();
            }
        }

    }

    public boolean checkBluetoothStatus() {
        /**
         * Ensures Blue tooth is enabled on the device. If Blue tooth is not
         * currently enabled, fire an intent to display a dialog asking the user
         * to grant permission to enable it.
         */
       // try {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                return false;
            }
            return true;
       // }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("ProfileScan.checkBleStatus.Exception","");
           // SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        //    return false;
       // }
    }


    /**
     * Holder class for the list view view widgets
     */
    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceRssi;
        Button pairStatus;
    }

    /**
     * Connect Timer
     */
    private void startConnectTimer(){
       // try {
            mConnectTimer = new Timer();
            mConnectTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    //mProgressdialog.dismiss();
                    //SystemConfig.mMyEventLogger.appendDebugStr("CONNECTION TIME OUT","");
                    mConnectTimerON = false;
                    BluetoothLeService.disconnect();
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressdialog.dismiss();
                                Toast.makeText(getActivity(),
                                        R.string.profile_cannot_connect_message,
                                        Toast.LENGTH_SHORT).show();
                                if (mLeDeviceListAdapter != null)
                                    mLeDeviceListAdapter.clear();
                                if (mLeDeviceListAdapter != null) {
                                    try {
                                        mLeDeviceListAdapter.notifyDataSetChanged();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                scanLeDevice(true);
                                mScanning = true;
                            }
                        });
                    }

                }
            }, CONNECTION_TIMEOUT);
            //SystemConfig.mMyEventLogger.appendDebugStr("Connect Timer Start","");
       // }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("ProfileScan.startConnectTimer.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
      //  }
    }
    /**
     * Swipe refresh timer
     */
    public void startScanTimer(){
        //try {
            mScanTimer = new Timer();
            mScanTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mScanning = false;
                    if (Build.VERSION.SDK_INT <21){
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    }else{
                        if (mLEScanner!=null){
                            mLEScanner.stopScan(mScanCallback);
                        }
                    }
                    if (getActivity()!=null){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mProgressdialog != null && mProgressdialog.isShowing()) {
                                    mProgressdialog.dismiss();
                                }
                            }
                        });
                    }

                    //mRefreshText.post(new Runnable() {
                    //    @Override
                    //    public void run() {
                    //        mRefreshText.setText(getResources().getString(
                    //                R.string.profile_control_no_device_message));
                    //    }
                    //});
                    //mSwipeLayout.setRefreshing(false);

                    // Set scan button visible
                    /*
                    mBtnScan.post(new Runnable() {
                        @Override
                        public void run() {
                            mBtnScan.setVisibility(View.VISIBLE);
                        }
                    });
                    */
                    scanLeDevice(false);
                }
            }, SCAN_PERIOD_TIMEOUT);
      //  }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("ProfileScan.startScanTimer.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
      //  }
    }

    /**
     * List Adapter for holding devices found through scanning.
     */
    private class LeDeviceListAdapter extends BaseAdapter {

        private LayoutInflater mInflator;
        private int rssiValue;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = getActivity().getLayoutInflater();
        }

        private void addDevice(BluetoothDevice device, int rssi) {
           // try {
                this.rssiValue = rssi;
                // New device found
                if (!mLeDevices.contains(device)) {
                    mDevRssiValues.put(device.getAddress(), rssi);
                    mLeDevices.add(device);
                } else {
                    mDevRssiValues.put(device.getAddress(), rssi);
                }
           // }catch(Exception ex1){
                //SystemConfig.mMyEventLogger.appendDebugStr("ProfileScan.LeAdaptor.addDevice.Exception","");
                //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
           // }
        }

        public int getRssiValue() {
            return rssiValue;
        }

        /**
         * Getter method to get the blue tooth device
         *
         * @param position
         * @return BluetoothDevice
         */
        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        /**
         * Clearing all values in the device array list
         */
        public void clear() {
          //  try {
                mLeDevices.clear();
                mDevRssiValues.clear();
          //  }catch(Exception ex1){
                //SystemConfig.mMyEventLogger.appendDebugStr("ProfileScan.Adapter.clear.Exception","");
                //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
          //  }
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }


        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }


        @Override
        public View getView(final int position, View view, ViewGroup viewGroup) {
            final ViewHolder viewHolder;

            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, viewGroup,
                        false);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view
                        .findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view
                        .findViewById(R.id.device_name);
                viewHolder.deviceRssi = (TextView) view
                        .findViewById(R.id.device_rssi);
                viewHolder.pairStatus = (Button) view.findViewById(R.id.btn_pair);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            /**
             * Setting the name and the RSSI of the BluetoothDevice. provided it
             * is a valid one
             */
            final BluetoothDevice device = mLeDevices.get(position);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0) {
                try {
                    viewHolder.deviceName.setText(deviceName);
                    viewHolder.deviceAddress.setText(device.getAddress());
                    byte rssival = (byte) mDevRssiValues.get(device.getAddress())
                            .intValue();
                    if (rssival != 0) {
                        viewHolder.deviceRssi.setText(String.valueOf(rssival));
                    }

                    //String pairStatus = (device.getBondState() == BluetoothDevice.BOND_BONDED) ? getActivity().getResources().getString(R.string.bluetooth_pair_to_connect) : getActivity().getResources().getString(R.string.bluetooth_unpair_to_connect);
                    //viewHolder.pairStatus.setText(pairStatus);

                    //  set pair button to connect
                    viewHolder.pairStatus.setText(getActivity().getResources().getString(R.string.bluetooth_pair_to_connect) );
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                viewHolder.deviceName.setText(R.string.device_unknown);
                viewHolder.deviceName.setSelected(true);
                viewHolder.deviceAddress.setText(device.getAddress());
            }
            viewHolder.pairStatus.setVisibility(View.INVISIBLE);
            /*
            viewHolder.pairStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //myOnClickForPairStatus(device, view);
                    myConnectProcess(device);
                }
            });
            */
            return view;
        }

    }

    private void myConnectProcess(BluetoothDevice device){
        //try {
            scanLeDevice(false);
            connectDevice(device, true, false);
        //}catch(Exception ex1) {
            //SystemConfig.mMyEventLogger.appendDebugStr("ProfileScan.myConnectProcess.Exception", "");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(), "");
            //ex1.printStackTrace();
        //}
    }

    private void myOnClickForPairStatus(BluetoothDevice device, View view){
        mPairButton = (Button) view;
        mDeviceAddress = device.getAddress();
        mDeviceName = device.getName();
        String status = mPairButton.getText().toString();
        if (status.equalsIgnoreCase(getResources().getString(R.string.bluetooth_pair_to_connect))) {
            unpairDevice(device);
        } else {
            pairDevice(device);
        }
    }

    public void updateProfileListView(){

        mProfileListView.setAdapter(mLeDeviceListAdapter);
        //mProfileListView.setTextFilterEnabled(false);
    }
}
