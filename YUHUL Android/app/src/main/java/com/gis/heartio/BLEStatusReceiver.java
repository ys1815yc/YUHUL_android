package com.gis.heartio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.gis.BLEConnectionServices.BluetoothLeService;
import com.gis.CommonUtils.Constants;
import com.gis.heartio.UIOperationControlSubsystem.MainActivity;
import com.gis.heartio.UIOperationControlSubsystem.intervalForegroundService;

public class BLEStatusReceiver extends BroadcastReceiver {
    private static final String TAG = "BLEStatusReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        GIS_Log.d(TAG,"action = "+action);
        if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
            Log.d(TAG,"action = "+BluetoothLeService.ACTION_GATT_DISCONNECTED);
            //Logger.e("onReceive--" + HomePageActivity.mApplicationInBackground);
            if (!heartioApplication.mApplicationInBackground
           //         || !OTAFilesListingActivity.mApplicationInBackground
            //        || !DataLoggerHistoryList.mApplicationInBackground
                    ) {
                //Log.d("BLEStatusReceiver","ACTION_GATT_DISCONNECTED");

                if (MainActivity.currentFragmentTag.equals(Constants.ITRI_ULTRASOUND_FRAGMENT_TAG)){
                    Toast.makeText(context,
                            context.getResources().getString(R.string.alert_message_bluetooth_disconnect),
                            Toast.LENGTH_SHORT).show();
                    MainActivity.oFrag.putUiMsg(MainActivity.UI_MSG_ID_BLE_DISCONNECTED);
                }else if (MainActivity.currentFragmentTag.equals(Constants.INTERVAL_FRAGMENT_TAG)){
                    Toast.makeText(context,
                            context.getResources().getString(R.string.alert_message_bluetooth_disconnect),
                            Toast.LENGTH_SHORT).show();

                    Message message = Message.obtain();
                    message.what = intervalForegroundService.ACTION_MESSAGE_ID_DISCONNECT;
                    try{
                        if (MainActivity.mServiceMessenger!=null){
                            MainActivity.mServiceMessenger.send(message);
                        }
                    }catch (RemoteException e){
                        e.printStackTrace();
                    }
                }
                /*if (OTAFirmwareUpgradeFragment.mFileupgradeStarted) {
                    //Resetting all preferences on Stop Button
                    Utils.setStringSharedPreference(context, Constants.PREF_OTA_FILE_ONE_NAME, "Default");
                    Utils.setStringSharedPreference(context, Constants.PREF_OTA_FILE_TWO_PATH, "Default");
                    Utils.setStringSharedPreference(context, Constants.PREF_OTA_FILE_TWO_NAME, "Default");
                    Utils.setStringSharedPreference(context, Constants.PREF_BOOTLOADER_STATE, "Default");
                    Utils.setIntSharedPreference(context, Constants.PREF_PROGRAM_ROW_NO, 0);
                }*/
                //if (!ProfileScanningFragment.isInFragment &&
                 //       !ServiceDiscoveryFragment.isInServiceFragment&&!heartioApplication.mApplicationInBackground) {
                    //Logger.e("Not in PSF and SCF");
               //     if (BluetoothLeService.getConnectionState() == BluetoothLeService.STATE_DISCONNECTED) {
                //        Toast.makeText(context,
                //                context.getResources().getString(R.string.alert_message_bluetooth_disconnect),
                //                Toast.LENGTH_SHORT).show();
                        /*Intent homePage = new Intent(context, MainActivity.class);
                        homePage.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(homePage);*/
              //      }
           //     }
            }
        }else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)){
            Log.d(TAG,"action = "+BluetoothLeService.ACTION_GATT_CONNECTED);
            Log.d(TAG,"MainActivity.currrentFragmentTag = "+MainActivity.currentFragmentTag);
            if (MainActivity.currentFragmentTag.equals(Constants.ITRI_ULTRASOUND_FRAGMENT_TAG)){
                MainActivity.oFrag.putUiMsg(MainActivity.UI_MSG_ID_BLE_CONNECTED);
            }else if (MainActivity.currentFragmentTag.equals(Constants.INTERVAL_FRAGMENT_TAG)){
                Message message = Message.obtain();
                message.what = intervalForegroundService.ACTION_MESSAGE_ID_SERVICE_DISCOVERY;
                try{
                    MainActivity.mServiceMessenger.send(message);
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
