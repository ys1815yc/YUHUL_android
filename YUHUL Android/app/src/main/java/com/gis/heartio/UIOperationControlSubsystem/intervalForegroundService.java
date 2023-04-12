package com.gis.heartio.UIOperationControlSubsystem;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.Settings;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.gis.BLEConnectionServices.BluetoothLeService;
import com.gis.CommonUtils.Constants;
import com.gis.CommonUtils.UUIDDatabase;
import com.gis.CommonUtils.Utils;
import com.gis.heartio.GIS_Log;
import com.gis.heartio.R;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.SystemConfig;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.dataInfo;
import com.gis.heartio.heartioApplication;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class intervalForegroundService extends Service {
    private static final String TAG = "intervalForegroundService";

    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";

    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";

    public static final int ACTION_MESSAGE_ID_ACK = 0;
    public static final int ACTION_MESSAGE_ID_STOP = 1;
    public static final int ACTION_MESSAGE_ID_START = 2;
    public static final int ACTION_MESSAGE_ID_RECORD_END = 3;
    public static final int ACTION_MESSAGE_ID_SAVE_RESULT = 4;
    public static final int ACTION_MESSAGE_ID_UPDATE_POWER_LEVEL = 5;
    public static final int ACTION_MESSAGE_ID_GET_COUNT = 6;
    public static final int ACTION_MESSAGE_ID_COUNT_RET = 7;
    public static final int ACTION_MESSAGE_ID_SERVICE_DISCOVERY = 8;
    public static final int ACTION_MESSAGE_ID_DISCONNECT = 9;

    private final String Notification_Channel_ID = "interval";
    private final int NOTIFY_ID = 1;

    private Notification notification;
    NotificationCompat.Builder builder;
    NotificationManager notificationManager;

    private static Messenger fragmentMessenger;
    private MessengerHandler messengerHandler;
    private static int count = 0;
    private static int scheduleCount = 0;
    public static volatile boolean isRunning;

    private static heartioApplication mApplication;

    private final int MINUTE_TO_MILLISECONDS = 60000;

    public static int intervalMins = 30;
    public static int durationMins = 120;

    intervalTimerTask mIntervalTT;
    Timer timer;

    private static int oldPowerLevel = -1;

    private BluetoothGattCharacteristic mNotifyCharacteristic;

    //---------------------------------------------------------------
    // for Service Discovery
    //---------------------------------------------------------------
    private static final long DELAY_PERIOD = 500;
    private static final String LIST_UUID = "UUID";

    private static final long SERVICE_DISCOVERY_TIMEOUT = 10000;   //--- 10 sec ---

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

    private static BluetoothGattService mGattDataServiceSelected;
    private static BluetoothGattCharacteristic mGattDataCharacteristicSelected;
    private static BluetoothGattService mGattGainServiceSelected;
    private static BluetoothGattCharacteristic mGattGainCharacteristicSelected;
    private static List<BluetoothGattCharacteristic> mGattDataCharacteristics;
    private static List<BluetoothGattCharacteristic> mGattGainCharacteristics;
    private Timer mTimerServiceDiscovery;

    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;

    public intervalForegroundService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        messengerHandler = new MessengerHandler();
        mApplication = (heartioApplication) getApplication();
        mNotifyCharacteristic = mApplication.getBluetoothgattDatacharacteristic();

        powerManager = (PowerManager)getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"intervalForegroundService:intervalWakeLockTag");
    }

    private class MessengerHandler extends Handler{
        @Override
        public void handleMessage(Message msg){
            GIS_Log.d(TAG,"msg="+msg.toString());
            if (msg.replyTo != null){
                fragmentMessenger = msg.replyTo;
                //notifyFragment(ACTION_MESSAGE_ID_ACK);
                sendMsgToFragment(ACTION_MESSAGE_ID_ACK);

            }
            if(msg.what == ACTION_MESSAGE_ID_START){
                intervalMins = msg.arg1;
                durationMins = msg.arg2;
                if(!isRunning){
                    startTimerTask();
                }
            }else if(msg.what == ACTION_MESSAGE_ID_RECORD_END){
                recEndAction();
            }else if(msg.what == ACTION_MESSAGE_ID_SAVE_RESULT){
                // jaufa, -, 181025
                //if(SystemConfig.mEnumBVResultReselectType == SystemConfig.ENUM_BV_RESULT_RESELECT_TYPE.VPK_RESELECT) {
                //    MainActivity.mSignalProcessController.reSelectBVResultByVpk();
                //}else if(SystemConfig.mEnumBVResultReselectType == SystemConfig.ENUM_BV_RESULT_RESELECT_TYPE.VTI_RESELECT) {
                //    MainActivity.mSignalProcessController.reSelectBVResultByVpk();
                //    MainActivity.mSignalProcessController.reSelectBVResultByVTI();
                //}
                sendMsgToFragment(ACTION_MESSAGE_ID_SAVE_RESULT);
                saveResult(MainActivity.mSignalProcessController.getResultDataAfterSignalProcess());
            }else if (msg.what == ACTION_MESSAGE_ID_GET_COUNT){
                sendMsgToFragment(ACTION_MESSAGE_ID_COUNT_RET);
            }else if (msg.what == ACTION_MESSAGE_ID_SERVICE_DISCOVERY){
                startServiceDiscovery();
            }else if (msg.what == ACTION_MESSAGE_ID_DISCONNECT){
                isRunning = false;
                releaseWakeLock();

                timer.cancel();
                stopForegroundService();
                sendMsgToFragment(ACTION_MESSAGE_ID_DISCONNECT);
            }


            super.handleMessage(msg);
        }
    }

    private void saveResult(dataInfo inputData){

        dataInfo mDataInfo = inputData;
        Date date;
        SimpleDateFormat simpleDateFormat,simpleDateFormat1;
        //String strDate;

        try {
            mDataInfo.userId = UserManagerCommon.mUserInfoCur.userID;
            mDataInfo.fileName = MainActivity.mRawDataProcessor.mStrCurFileName;
            simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.TAIWAN);
            simpleDateFormat1 = new SimpleDateFormat("HH:mm:ss", Locale.TAIWAN);
            date = new Date();
            mDataInfo.createdDate = simpleDateFormat.format(date);

            MainActivity.mIwuSQLHelper.addDataInfoToDB(mDataInfo);
            String dataStr;
            boolean isError = false;
            if (mDataInfo.HR <= 0){
                dataStr = simpleDateFormat1.format(date)+
                                    "\nData error! Please check HeartIO.";
                isError = true;
            }else{
                dataStr = simpleDateFormat1.format(date)+
                        "\nHR: "+mDataInfo.HR+
                        " ,Vpk: "+String.format("%.2f", mDataInfo.Vpk)+
                        " ,VTI: "+String.format("%.2f", mDataInfo.VTI)+
                        "\nSV: "+String.format("%.2f", mDataInfo.SV)+
                        " ,CO: "+ String.format("%.2f", mDataInfo.CO);
            }
            sendNotification(dataStr, isError);

        } catch (Exception ex1) {
            ex1.printStackTrace();

        }

    }

    private void sendNotification(String dataString, boolean isError){
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.bigText(dataString);

        if (getNotificationManager()!=null){
            if (isError){
                try {
                    MediaPlayer mediaPlayer=MediaPlayer.create(this,R.raw.blackout5);
                    mediaPlayer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            // Set big text style.
            builder.setStyle(bigTextStyle);
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);


            builder.setContentText(dataString);
            builder.setContentTitle("Interval Measurement");

            getNotificationManager().notify(NOTIFY_ID,builder.build());

        }

    }

    private void notifyFragment(final int messageID){
        /*isRunning = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(isRunning){
                    try {
                        Thread.sleep(1000);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    sendMsgToFragment(messageID);
                }
            }
        }).start();*/
    }

    private static void sendMsgToFragment(final int messageID){
        count++;
        Message message = Message.obtain();
        message.arg1 = count;
        if (messageID == ACTION_MESSAGE_ID_COUNT_RET||
                messageID==ACTION_MESSAGE_ID_SAVE_RESULT){
            message.arg2 = scheduleCount;
        }
        message.what = messageID;
        try {
            fragmentMessenger.send(message);
        }catch (RemoteException e){
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        GIS_Log.d(TAG,"onBind.");
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return new Messenger(messengerHandler).getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        GIS_Log.d(TAG,"onUnbind.");
        isRunning = false;
        releaseWakeLock();

        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        scheduleCount = 0;
        createNotificationChannel();
        if (intent!=null){
            String action = intent.getAction();
            if (action!=null){
                switch (action){
                    case ACTION_START_FOREGROUND_SERVICE:
                        startForegroundService();
                        //Toast.makeText(getApplicationContext(), "Foreground service is started.", Toast.LENGTH_LONG).show();
//                        if (getNotificationManager()!=null){
//                            getNotificationManager().notify(NOTIFY_ID,notification);
//                        }

                        //startTimerTask();
                        break;
                    case ACTION_STOP_FOREGROUND_SERVICE:
                        isRunning = false;
                        releaseWakeLock();

                        timer.cancel();
                        stopForegroundService();
                        //Toast.makeText(getApplicationContext(), "Foreground service is stopped.", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private NotificationManager getNotificationManager(){
        if (notificationManager == null)
            notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Interval Notifications";//getString(R.string.channel_name);
            String description = "Notification of interval measurement.";//getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel notificationChannel = new NotificationChannel(Notification_Channel_ID, name, importance);

            // Configure the notification channel.
            notificationChannel.setDescription(description);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setVibrationPattern(new long[]{1000,500,1000,500});
            notificationChannel.enableVibration(true);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            if (getNotificationManager()!=null){
                getNotificationManager().createNotificationChannel(notificationChannel);
            }
        }
    }

    private void startForegroundService(){
        GIS_Log.d(TAG,"Start interval foreground service.");
        registerReceiver(mServiceDiscoveryListner, Utils.makeGattUpdateIntentFilter());
        // Create notification default intent.
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Constants.INTERVAL_FRAGMENT_TAG, true);
        //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Create notification builder.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            builder = new NotificationCompat.Builder(this, Notification_Channel_ID);
        }else{
            builder = new NotificationCompat.Builder(this);
            // Make the notification max priority.
            // For android 7 and below
            builder.setPriority(Notification.PRIORITY_MAX);
            builder.setLights(Color.BLUE,1000,1000);
            builder.setVibrate(new long[]{1000,500,1000,500});
            builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        }


        // Make notification show big text.
        //NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        //bigTextStyle.setBigContentTitle("Interval measurment");
        // Set big text style.
        //builder.setStyle(bigTextStyle);

        // Make head-up notification
        //builder.setFullScreenIntent(pendingIntent, true);

        // Don't do this!!
        //builder.setContentIntent(pendingIntent);

        builder.setContentTitle("Interval Measurment");
        builder.setSmallIcon(R.drawable.multiple_w);

        // Add Stop button intent in notification
        Intent stopIntent = new Intent(this, intervalForegroundService.class);
        stopIntent.setAction(ACTION_STOP_FOREGROUND_SERVICE);
        PendingIntent pendingStopIntent = PendingIntent.getService(this, 0, stopIntent, 0);
        NotificationCompat.Action stopAction = new NotificationCompat.Action(
                                                        R.drawable.ic_stop_black_24dp,
                                                        getString(R.string.blood_pressure_stop_btn),
                                                        pendingStopIntent);
        builder.addAction(stopAction);

        // Build the notification.
        notification = builder.build();

        // Start foreground service.
        startForeground(NOTIFY_ID, notification);
    }

    private void stopForegroundService(){
        GIS_Log.d(TAG,"Stop interval foreground service.");
        GIS_Log.d(TAG,"SystemConfig.mEnumStartState = "+SystemConfig.mEnumStartState);
        try{
            unregisterReceiver(mServiceDiscoveryListner);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }

        if (SystemConfig.mEnumStartState==SystemConfig.ENUM_START_STATE.STATE_START){
            recEndAction();
        }
        stopForeground(true);
        //  Stop the foreground service
        stopSelf();

        if (getNotificationManager()!=null){
            getNotificationManager().cancel(0);
        }
        sendMsgToFragment(ACTION_MESSAGE_ID_STOP);
    }

    private void intervalMeasurementTrigger(){
        if (SystemConfig.mIntSoundEnabled == SystemConfig.INT_SOUND_ENABLED_YES) {
            //SystemConfig.mAudioPlayerController.putMsgForAudioOpen(SystemConfig.mIntUltrasoundSamplerate);
            MainActivity.mAudioPlayerController.openAudio(SystemConfig.mIntUltrasoundSamplerate);
        }

        startRecord();
    }

    private void startRecord() {
        if (BluetoothLeService.getConnectionState() != BluetoothLeService.STATE_CONNECTED) {
            return;
        }
        //switchDrawSurfaceViews(false);

        MainActivity.mRawDataProcessor.prepareStartOnLine();
        MainActivity.mBVSignalProcessorPart1.prepareStart();
        //mOnLineBloodVelocityUiManager.mBloodVelocityPlotter.prepareStartOnLine();
        // mBloodVelocityPlotter.prepareStartOnLine(mSurfaceViewScale);

        MainActivity.mSignalProcessController.putSignalProcessControllerMsgForPrepareCmd();

        SystemConfig.mEnumStartState = SystemConfig.ENUM_START_STATE.STATE_START;

        // for update Power level flag
            /*
            mBoolNotifyUsedByDataComm = true;
*/
        //SystemConfig.mUltrasoundComm.notifyStartCharacteristic();
        BluetoothLeService.setCharacteristicNotification( mApplication.getBluetoothgattDatacharacteristic(), //mDataNotifyCharacteristic,
                true);
/*
            SystemConfig.mUltrasoundComm.mBoolRxData = true;
*/
        BluetoothLeService.mBluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);

    }

    public void recEndAction(){

        recEndState();

        if (SystemConfig.mIntSoundEnabled == SystemConfig.INT_SOUND_ENABLED_YES) {
            //SystemConfig.mAudioPlayerController.closeAudio();
            MainActivity.mAudioPlayerController.putMsgForAudioClose();
        }
    }
    private void recEndState(){
        SystemConfig.mEnumStartState = SystemConfig.ENUM_START_STATE.STATE_STOP;

        stopBroadcastDataNotify(mNotifyCharacteristic);
        //SystemConfig.mUltrasoundComm.mBoolRxData = false;
        //mBoolNotifyUsedByDataComm = false;
    }

    /**
     * Stopping Broadcast receiver to broadcast notify characteristics
     *
     * @param gattCharacteristic
     */
    void stopBroadcastDataNotify(BluetoothGattCharacteristic gattCharacteristic) {
        if (gattCharacteristic != null) {
            if ((gattCharacteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                BluetoothLeService.setCharacteristicNotification(
                        gattCharacteristic, false);
            }
        }else{
            GIS_Log.d(TAG,"gattCharacteristic is null.");
        }
    }

    private void startTimerTask(){
        timer = new Timer();
        mIntervalTT = new intervalTimerTask();
        GIS_Log.d(TAG,"intervalMins = "+intervalMins+" , durationMins= "+durationMins);
        long intervalMS = intervalMins*MINUTE_TO_MILLISECONDS;
        final long startTime = System.currentTimeMillis();
        GIS_Log.d(TAG,"intervalMS= "+intervalMS+" ,startTime="+startTime);
        wakeLock.acquire(durationMins*MINUTE_TO_MILLISECONDS+10000);
        timer.schedule(mIntervalTT,1000,intervalMS);
         isRunning = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(isRunning){
                    try {
                        Thread.sleep(1000);
                        if (SystemConfig.mIntPowerLevel != oldPowerLevel){
                            oldPowerLevel = SystemConfig.mIntPowerLevel;
                            sendMsgToFragment(ACTION_MESSAGE_ID_UPDATE_POWER_LEVEL);
                            if (SystemConfig.mIntPowerLevel==2){
                                sendNotification(getString(R.string.alert_msg_low_power_will_stop), true);
                            }
                        }
                        long nowTime = System.currentTimeMillis();
                        long diffTime = nowTime-startTime;
                        if (diffTime >(durationMins*MINUTE_TO_MILLISECONDS+1000)||
                                SystemConfig.mIntPowerLevel==2 ){
                            isRunning = false;
                            releaseWakeLock();

                            timer.cancel();
                            stopForegroundService();
                        }
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    class intervalTimerTask extends TimerTask{

        @Override
        public void run() {
            scheduleCount++;
            GIS_Log.d(TAG,"Run interval measurement "+scheduleCount+" times.");
            intervalMeasurementTrigger();
        }
    }

    private void releaseWakeLock(){
        if (wakeLock != null){
            if (wakeLock.isHeld()){
                wakeLock.release();
            }
            wakeLock = null;
        }
    }


    //---------------------------------------------------------------
    // Service Discovery Function
    //---------------------------------------------------------------

    public void startServiceDiscovery(){
        //--- discover service ----------------
        Handler delayHandler = new Handler();
        delayHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (BluetoothLeService.getConnectionState() == BluetoothLeService.STATE_CONNECTED)
                    BluetoothLeService.discoverServices();
            }
        }, DELAY_PERIOD);

        //--- create service discovery timeout timer --------
        mTimerServiceDiscovery = new Timer();
        mTimerServiceDiscovery.schedule(new TimerTask() {
            @Override
            public void run() {
                //SystemConfig.mFragment.putLongStartMsg(SystemConfig.mFragment.INT_MSG_RECONNECT_SERVICE_DISCOVERY_FAIL);
            }
        }, SERVICE_DISCOVERY_TIMEOUT);
    }


    private final BroadcastReceiver mServiceDiscoveryListner=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean boolServiceFound;
            try {
                final String action = intent.getAction();
                if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
                        .equals(action)) {
                    if(mTimerServiceDiscovery  != null) mTimerServiceDiscovery.cancel();
                    boolServiceFound= checkGattServices(BluetoothLeService.getSupportedGattServices());
                    if(boolServiceFound){
                        //SystemConfig.mFragment.putReconnectMsg(SystemConfig.mFragment.INT_MSG_RECONNECT_SERVICE_DISCOVERY_OK);
                    }else{
                        //SystemConfig.mFragment.putReconnectMsg(SystemConfig.mFragment.INT_MSG_RECONNECT_SERVICE_DISCOVERY_FAIL);
                    }
                /*
                / Changes the MTU size to 512 in case LOLLIPOP and above devices
                */
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        BluetoothLeService.exchangeGattMtu(512);
                    }
                } else if (BluetoothLeService.ACTION_GATT_SERVICE_DISCOVERY_UNSUCCESSFUL
                        .equals(action)) {
                    if(mTimerServiceDiscovery  != null) mTimerServiceDiscovery.cancel();
                    //SystemConfig.mFragment.putReconnectMsg(SystemConfig.mFragment.INT_MSG_RECONNECT_SERVICE_DISCOVERY_FAIL);
                }
            }catch(Exception ex1){
                //    SystemConfig.mMyEventLogger.appendDebugStr("ServDiscFrag.onReceive.Exception","");
                //    SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            }
        }
    };

    private boolean checkGattServices(List<BluetoothGattService> gattServices) {

        //try {
        if (gattServices == null)  return false;

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
            return checkUltrasoundService();
        } else {
            return false;
        }
    }

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

        //try {
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
            if (uuid.equals(UUIDDatabase.UUID_ULTRASOUND_GAIN_CONTROL_CHARACTERISTIC)||
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

        for (int i = 0; i < mGattServiceData.size(); i++) {
            strUuid = mGattServiceData.get(i).get("UUID").getUuid().toString();
            GIS_Log.d(TAG,strUuid);
            if (mGattServiceData.get(i).get("UUID").getUuid().equals(UUIDDatabase.UUID_ULTRASOUND_DATA_SERVICE)||
                mGattServiceData.get(i).get("UUID").getUuid().equals(UUIDDatabase.UUID_HEARTIO_DATA_SERVICE)) {
                boolDataServiceFound = true;
                mGattDataServiceSelected = mGattServiceData.get(i).get("UUID");
                break;
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
            }
        }

        if (!boolDataCharacteristicFound) {
            return false;
        }

        mApplication.setBluetoothgattDatacharacteristic(mGattDataCharacteristicSelected);
        return true;
    }
}
