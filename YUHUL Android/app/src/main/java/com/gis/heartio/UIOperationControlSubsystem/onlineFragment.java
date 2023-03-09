package com.gis.heartio.UIOperationControlSubsystem;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.gis.BLEConnectionServices.BluetoothLeService;
import com.gis.CommonFragments.ServiceDiscoveryFragment;
import com.gis.CommonUtils.Constants;
import com.gis.heartio.GIS_DataController;
import com.gis.heartio.GIS_Log;
import com.gis.heartio.R;
import com.gis.heartio.SignalProcessSubsystem.BVSignalProcessorPart1;
import com.gis.heartio.SignalProcessSubsystem.RawDataProcessor;
import com.gis.heartio.SupportSubsystem.MyThreadQMsg;
import com.gis.heartio.SupportSubsystem.SystemConfig;
import com.gis.heartio.SupportSubsystem.dataInfo;
import com.gis.heartio.heartioApplication;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * Created by Cavin on 2017/12/15.
 */

public class onlineFragment extends Fragment {
    private final String TAG = "online";
    private final String mStrFailed = "Failed ";
    public Button mBtnNotify;
    private Button mBtnCalculate;
    public Button mBtnSave;
    public ToggleButton mTBtnTryNotify;
    public ToggleButton mTBtnRec;
    private TextView mHRValueTextView, mVTIValueTextView,
            mVpkValueTextView, mSVValueTextView, mCOValueTextView;

    private TextView mSNRTitleTextView, mSNRValueTextView;
    private TextView mAmpTitleTextView, mAmpValueTextView;
    private TextView mVelocityTextView;
    private TextView mOnlineAngleTestTV;

    private RadioGroup mVTIModeGroup;
    private RadioButton mStrongPeakModeRB;
    private RadioButton mTwoPointModeRB;
    private TextView mUserInfoTextView;
    private SwitchCompat mSingleVpkSwitch;
    private SwitchCompat mInverseSwitch, mFourthLevelSwitchOnline;

    private LinearLayout resultVti, resultVpk;

    private ProgressBar mProgressBar;
    private CountDownTimer mCountdownTimer;
    private TextView mRecordSecondsCount;

    private static heartioApplication mApplication;

    public SurfaceView mSurfaceViewSpace;
    public SurfaceView mSurfaceViewScale;
    public SurfaceView mSurfaceViewUltrasound;
    public static SurfaceView[] mSurfaceViewsOnline;
    public SurfaceView mSurfaceViewTimeScaleDownSpace, mSurfaceViewTimeScaleDown;
    public SurfaceView mSurfaceViewTimeScaleUpSpace, mSurfaceViewTimeScaleUp;
    private boolean mBoolSvUltrasoundTouchAtUpper;

    private LinearLayout ecgLinearLayout = null;
    public EcgSegView[] ecgSegViews = null;
    public EcgView ecgViewOffline = null;

    // Indicate/Notify/Read Flag


    public static Handler mHandlerReceiveDataBySegmentOnLine;

    private boolean needReDrawScale = false;

    //characteristics
    private BluetoothGattCharacteristic mReadCharacteristic;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothGattCharacteristic mIndicateCharacteristic;

    public static MyPlotterBloodVelocity mBloodVelocityPlotter;

    public Handler mHandlerUiMsg;

    public float mFloatSvOnTouchStartX;
    public float mFloatSvOnTouchStartY;
    public float mFloatSvOnTouchEndX;
    public float mFloatSvOnTouch2PointX;
    private float mFloatShiftX, mFloatShiftY;

    public static float mFloatTimeScalePosCur;
    private AppCompatActivity mActivity = null;
    private ImageView bleStateImageView;

    private static int intPrePowerLevel = -1;
    private static int lowPowerCount = 0;
    private dataInfo currentResult;

    private ImageView greenLightImg;
    private Handler mHandler;
    private Runnable mRunnable;
    public onlineFragment() {
        //SystemConfig.mEnumUltrasoundUIState = SystemConfig.ENUM_UI_STATE.ULTRASOUND_UI_STATE_ONLINE;
        //if(SystemConfig.mEnumUltrasoundUIState == SystemConfig.ENUM_UI_STATE.ULTRASOUND_UI_STATE_ONLINE){
        SystemConfig.initItriDevice8KOnlineBE();
        //}
        MainActivity.oFrag = this;
        currentResult = new dataInfo();
    }

    public static onlineFragment newInstance() {
        return new onlineFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (AppCompatActivity) getActivity();
        if (getActivity() != null) {
            mApplication = (heartioApplication) getActivity().getApplication();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_item_online, container, false);

        resultVti = rootView.findViewById(R.id.resultVTILinearLayout);
        resultVpk = rootView.findViewById(R.id.resultVpkLinearLayout);

        ecgLinearLayout = rootView.findViewById(R.id.ecgLinearLayout);
        if (SystemConfig.isHeartIO2) {
            ecgLinearLayout.setVisibility(View.VISIBLE);
            initEcgSegView(rootView);
        } else {
            ecgLinearLayout.setVisibility(View.GONE);
        }


        if (mActivity != null) {
            Objects.requireNonNull(mActivity.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            mActivity.getSupportActionBar().setDisplayShowHomeEnabled(true);
            setHasOptionsMenu(true);
        }

        mProgressBar = rootView.findViewById(R.id.recordProgressBar);
        mProgressBar.setVisibility(View.GONE);
        mRecordSecondsCount = rootView.findViewById(R.id.recordSec);
        mRecordSecondsCount.setVisibility(View.GONE);

        // Init User Info
        mUserInfoTextView = rootView.findViewById(R.id.userInfoOnlineTextView);
        String userInfoStr = UserManagerCommon.mUserInfoCur.userID + "   " +
                UserManagerCommon.mUserInfoCur.firstName + "  " +
                UserManagerCommon.mUserInfoCur.lastName + "   PA Dia.  " +
                UserManagerCommon.mDoubleUserPulmDiameterCm;

        mUserInfoTextView.setText(userInfoStr);

        mBtnCalculate = rootView.findViewById(R.id.calculateOnline);
        mBtnCalculate.setOnClickListener(btnCalculateOnClick);
        mBtnCalculate.setEnabled(false);

        mBtnSave = rootView.findViewById(R.id.saveBtn);
        mBtnSave.setOnClickListener(btnSaveOnClick);
        mBtnSave.setEnabled(false);

        mBtnNotify = rootView.findViewById(R.id.notifybtn);
        mBtnNotify.setOnClickListener(btnNotifyOnClick);

        mTBtnTryNotify = rootView.findViewById(R.id.trynotifytogglebtn);
        mTBtnTryNotify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton compoundButton, boolean b) {
                Log.d(TAG, "onCheckedChanged b=" + b);
                final boolean tb = b;
                if (b) {
                    if (mBtnSave.isEnabled()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                        builder.setTitle(getString(R.string.msg_save_data));
                        builder.setPositiveButton(getString(R.string.alert_message_no), (dialog, which) -> {
                            mBtnSave.setEnabled(false);
                            onCheckedChanged(compoundButton, tb);
                        });

                        builder.setNegativeButton(getString(R.string.title_save), (dialog, which) -> {
                            mBtnSave.performClick();
                            onCheckedChanged(compoundButton, tb);
                        });

                        AlertDialog alert = builder.create();
                        alert.show();
                        return;
                    }
                    if (SystemConfig.mIntSoundEnabled == SystemConfig.INT_SOUND_ENABLED_YES) {
                        MainActivity.mAudioPlayerController.clearAllMsg();
                        //SystemConfig.mAudioPlayerController.putMsgForAudioOpen(SystemConfig.mIntUltrasoundSamplerate);
                        MainActivity.mAudioPlayerController.putMsgForAudioOpen(SystemConfig.mIntUltrasoundSamplerate);
                    }
                    MainActivity.mIsNotifyEnabled = true;
                    enableTryAction();

                    greenLightImg.setVisibility(View.INVISIBLE);
                    mTBtnRec.setTextColor(Color.BLACK);
                    mTBtnRec.setVisibility(View.VISIBLE);
                    mBtnCalculate.setEnabled(false);
                    mBtnSave.setEnabled(false);

                    mHandler = new Handler();
                    mRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if(MainActivity.mIsNotifyEnabled){
                                mTBtnTryNotify.setChecked(false);
                                mHandler.postDelayed(this, 3000L);
//                                Log.d("setChecked", "setChecked(false)");
                            } else {
                                mTBtnTryNotify.setChecked(true);
//                                Log.d("setChecked", "setChecked(true)");
                            }
                        }
                    };
                    mHandler.postDelayed(mRunnable,75 * 1000L);

                    if (!SystemConfig.mTestMode) {
                        // 5 mins 1 min per tick
                        mCountdownTimer = new CountDownTimer(300 * 1000L, 60 * 1000L) {
                            @Override
                            public void onTick(long l) {
                                int iProgress = (5 - (int) (l / 60000));
                                //mProgressBar.setProgress(14-(int)(l/1000));
                                Log.d(TAG, "progress:" + iProgress);
                                //mRecordSecondsCount.setText(""+l/1000);
//                            if (iProgress > 1){
//                                reTry();
//                            }
                            }

                            @Override
                            public void onFinish() {
                                Log.d(TAG, "Counter timer onFinish");
                                mTBtnTryNotify.setChecked(false);
                                if (mActivity != null) {
                                    showMessageDialog(getString(R.string.msg_click_try_to_countinu_testing), mActivity);
                                }
                                //reTry();
                            }
                        };
                        mCountdownTimer.cancel();
                        mCountdownTimer.start();
                    }
                } else {
                    MainActivity.mIsNotifyEnabled = false;
                    if(mHandler != null){
                        mHandler.removeCallbacks(mRunnable);
                    }
                    tryStopAction();
                    /* 將接收raw data的陣列清空 2023/02/23 by Doris */
                    Arrays.fill(MainActivity.mRawDataProcessor.mShortUltrasoundDataBeforeFilter, (short) 0);
//                    mTBtnRec.setVisibility(View.GONE);      // Viento want it visible after first record.
                    if (!SystemConfig.mTestMode) {
                        mCountdownTimer.cancel();
                    }
                }
            }
        });

        mTBtnRec = rootView.findViewById(R.id.recToggleButton);
        mTBtnRec.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton compoundButton, final boolean b) {
                if (b) {
                    SurfaceHolder surfaceHolder;
                    Canvas canvas;
                    String strDebug;
                    int iVar, iGainCmd = 5;

                    //    mTextViewDataLostState.setVisibility(View.GONE);
                    if (!mTBtnTryNotify.isChecked()) {
                        Log.d(TAG, "Did not try!!!!");
                        if (mBtnSave.isEnabled()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                            builder.setTitle(getString(R.string.msg_save_data));
                            builder.setPositiveButton(getString(R.string.alert_message_no), (dialog, which) -> {
                                mBtnSave.setEnabled(false);
                                onCheckedChanged(compoundButton, b);
                            });

                            builder.setNegativeButton(getString(R.string.title_save), (dialog, which) -> {
                                mBtnSave.performClick();
                                onCheckedChanged(compoundButton, b);
                            });

                            AlertDialog alert = builder.create();
                            alert.show();
                            return;
                        } else {
                            mTBtnTryNotify.setChecked(true);
                        }

                    }

                    if ((SystemConfig.mIntGainControlEnabled == SystemConfig.INT_GAIN_CONTROL_ENABLED_YES)
                            && (SystemConfig.mIntGainLevelCommandSetting == SystemConfig.INT_GAIN_CONTROL_LEVEL_CNT)) {
                        //iGainCmd = (SystemConfig.mRawDataProcessor.mIntGainLevelCommandCur + SystemConfig.mRawDataProcessor.mIntGainLevelCommandPre + 1) / 2;
                        // if (SystemConfig.mRawDataProcessor.mIntGainLevelCommandCur >= SystemConfig.mRawDataProcessor.mIntGainLevelCommandPre) {
                        if (MainActivity.mRawDataProcessor.mIntGainLevelCommandCur >= MainActivity.mRawDataProcessor.mIntGainLevelCommandPre) {
                            iGainCmd = MainActivity.mRawDataProcessor.mIntGainLevelCommandCur; //SystemConfig.mRawDataProcessor.mIntGainLevelCommandCur;
                        } else {
                            iGainCmd = MainActivity.mRawDataProcessor.mIntGainLevelCommandPre; //SystemConfig.mRawDataProcessor.mIntGainLevelCommandPre;
                        }
                        //SystemConfig.mRawDataProcessor.adjustGainCommandFixed(iGainCmd);
                        MainActivity.mRawDataProcessor.adjustGainCommandFixed(iGainCmd);
                        // SystemConfig.mMyEventLogger.appendDebugIntEvent("Gain Setting = ", SystemConfig.mRawDataProcessor.mIntGainLevelCommandPre, SystemConfig.mRawDataProcessor.mIntGainLevelCommandCur, iGainCmd, 0, 0);
                    }
                    mTBtnRec.setTextColor(Color.BLACK);
                    greenLightImg.setVisibility(View.INVISIBLE);
                    tryEndAction(false);
                    if(mHandler != null){
                        mHandler.removeCallbacks(mRunnable);
                    }
                    // For power level
                /*
                SystemConfig.mUltrasoundComm.mBoolRxData = false;
                */
                    if ((SystemConfig.mIntGainControlEnabled == SystemConfig.INT_GAIN_CONTROL_ENABLED_YES)
                            && (SystemConfig.mIntGainLevelCommandSetting == SystemConfig.INT_GAIN_CONTROL_LEVEL_CNT)) {
                        //SystemConfig.mRawDataProcessor.adjustGainCommandFixed(iGainCmd);
                        MainActivity.mRawDataProcessor.adjustGainCommandFixed(iGainCmd);
                    }

                    for (iVar = 0; iVar < SystemConfig.INT_SURFACE_VIEWS_ON_LINE_USE_SIZE; iVar++) {
                        surfaceHolder = mSurfaceViewsOnline[iVar].getHolder();
                        canvas = surfaceHolder.lockCanvas();
                        canvas.drawColor(Color.WHITE);
                        surfaceHolder.unlockCanvasAndPost(canvas);
                        if (SystemConfig.isHeartIO2) {
                            ecgSegViews[iVar].clearBVSegUIView();
                        }
                    }

                    if (SystemConfig.mIntSoundEnabled == SystemConfig.INT_SOUND_ENABLED_YES) {
                        //SystemConfig.mAudioPlayerController.putMsgForAudioOpen(SystemConfig.mIntUltrasoundSamplerate);
                        // SystemConfig.mAudioPlayerController.openAudio(SystemConfig.mIntUltrasoundSamplerate);
                        MainActivity.mAudioPlayerController.openAudio(SystemConfig.mIntUltrasoundSamplerate);
                    }

                    if ((SystemConfig.mIntGainControlEnabled == SystemConfig.INT_GAIN_CONTROL_ENABLED_YES)
                            && (SystemConfig.mIntGainLevelCommandSetting == SystemConfig.INT_GAIN_CONTROL_LEVEL_CNT)) {
                        //SystemConfig.mRawDataProcessor.adjustGainCommandFixed(iGainCmd);
                        MainActivity.mRawDataProcessor.adjustGainCommandFixed(iGainCmd);
                    }

                    // SystemConfig.mMyEventLogger.appendDebugStr("****************************", "");
                    //strDebug = "**** New Measure: " + SystemConfig.mMyEventLogger.getDateStr() + " ***";
                    // SystemConfig.mMyEventLogger.appendDebugStr(strDebug, "");
                    startRecord();
                    requireActivity().runOnUiThread(() -> {
                        mProgressBar.setVisibility(View.VISIBLE);
                        mProgressBar.setMax(14);
                        mProgressBar.setProgress(0);
                        mRecordSecondsCount.setVisibility(View.VISIBLE);
                        if (!SystemConfig.mTestMode && mCountdownTimer != null) {
                            mCountdownTimer.cancel();
                        }
                        mCountdownTimer = new CountDownTimer(14000, 1000) {
                            @Override
                            public void onTick(long l) {
                                mProgressBar.setProgress(14 - (int) (l / 1000));
                                //Log.d(TAG,"progress:"+(14-(int)(l/1000)));
                                mRecordSecondsCount.setText("" + l / 1000);
                            }

                            @Override
                            public void onFinish() {
                                mProgressBar.setProgress(0);
                                mProgressBar.setVisibility(View.GONE);
                                mRecordSecondsCount.setVisibility(View.GONE);
                            }
                        };
                        mCountdownTimer.start();
                    });

                    mTBtnTryNotify.setEnabled(false);
                } else {
                    mCountdownTimer.cancel();
                    mProgressBar.setVisibility(View.GONE);
                    mRecordSecondsCount.setVisibility(View.GONE);
                    stopRecord();
                    mTBtnTryNotify.setEnabled(true);
                    mTBtnTryNotify.setChecked(false);
                }
            }
        });
        mTBtnRec.setVisibility(View.GONE);

        mSurfaceViewTimeScaleDown = (SurfaceView) rootView.findViewById(R.id.svTimeScaleDown);
        mSurfaceViewTimeScaleDown.setZOrderOnTop(true);
        mSurfaceViewTimeScaleDownSpace = (SurfaceView) rootView.findViewById(R.id.svTimeScaleDownSpace);
        mSurfaceViewTimeScaleDownSpace.setZOrderOnTop(true);
        mSurfaceViewTimeScaleUp = (SurfaceView) rootView.findViewById(R.id.svTimeScaleUp);
        mSurfaceViewTimeScaleUp.setZOrderOnTop(true);
        mSurfaceViewTimeScaleUpSpace = (SurfaceView) rootView.findViewById(R.id.svTimeScaleUpSpace);
        mSurfaceViewTimeScaleUpSpace.setZOrderOnTop(true);
        //mSurfaceViewUltrasound.setFormat(PixelFormat.TRANSLUCENT);

        mSurfaceViewSpace = rootView.findViewById(R.id.svSpace);
        mSurfaceViewSpace.setZOrderOnTop(true);
        mSurfaceViewScale = rootView.findViewById(R.id.svScale);
        mSurfaceViewScale.setZOrderOnTop(true);
        mSurfaceViewUltrasound = rootView.findViewById(R.id.svUltrasound);
        mSurfaceViewUltrasound.setZOrderOnTop(true);

        mSurfaceViewUltrasound.setOnTouchListener(mSvUltrasoundOnTouchListener);

        mSurfaceViewsOnline = new SurfaceView[SystemConfig.INT_SURFACE_VIEWS_ON_LINE_MAX_SIZE];
        mSurfaceViewsOnline[0] = rootView.findViewById(R.id.svUltrasoundOnline0);
        mSurfaceViewsOnline[1] = rootView.findViewById(R.id.svUltrasoundOnline1);
        mSurfaceViewsOnline[2] = rootView.findViewById(R.id.svUltrasoundOnline2);
        mSurfaceViewsOnline[3] = rootView.findViewById(R.id.svUltrasoundOnline3);
        mSurfaceViewsOnline[4] = rootView.findViewById(R.id.svUltrasoundOnline4);
        mSurfaceViewsOnline[5] = rootView.findViewById(R.id.svUltrasoundOnline5);
        mSurfaceViewsOnline[6] = rootView.findViewById(R.id.svUltrasoundOnline6);
        mSurfaceViewsOnline[7] = rootView.findViewById(R.id.svUltrasoundOnline7);
        mSurfaceViewsOnline[8] = rootView.findViewById(R.id.svUltrasoundOnline8);
        mSurfaceViewsOnline[9] = rootView.findViewById(R.id.svUltrasoundOnline9);
        mSurfaceViewsOnline[10] = rootView.findViewById(R.id.svUltrasoundOnline10);
        mSurfaceViewsOnline[11] = rootView.findViewById(R.id.svUltrasoundOnline11);
        mSurfaceViewsOnline[12] = rootView.findViewById(R.id.svUltrasoundOnline12);
        mSurfaceViewsOnline[13] = rootView.findViewById(R.id.svUltrasoundOnline13);
        mSurfaceViewsOnline[14] = rootView.findViewById(R.id.svUltrasoundOnline14);
        mSurfaceViewsOnline[15] = rootView.findViewById(R.id.svUltrasoundOnline15);
        mSurfaceViewsOnline[16] = rootView.findViewById(R.id.svUltrasoundOnline16);
        mSurfaceViewsOnline[17] = rootView.findViewById(R.id.svUltrasoundOnline17);
        mSurfaceViewsOnline[18] = rootView.findViewById(R.id.svUltrasoundOnline18);
        mSurfaceViewsOnline[19] = rootView.findViewById(R.id.svUltrasoundOnline19);
        mSurfaceViewsOnline[20] = rootView.findViewById(R.id.svUltrasoundOnline20);
        mSurfaceViewsOnline[21] = rootView.findViewById(R.id.svUltrasoundOnline21);
        mSurfaceViewsOnline[22] = rootView.findViewById(R.id.svUltrasoundOnline22);
        mSurfaceViewsOnline[23] = rootView.findViewById(R.id.svUltrasoundOnline23);
        mSurfaceViewsOnline[24] = rootView.findViewById(R.id.svUltrasoundOnline24);
        mSurfaceViewsOnline[25] = rootView.findViewById(R.id.svUltrasoundOnline25);
        mSurfaceViewsOnline[26] = rootView.findViewById(R.id.svUltrasoundOnline26);
        mSurfaceViewsOnline[27] = rootView.findViewById(R.id.svUltrasoundOnline27);
        mSurfaceViewsOnline[28] = rootView.findViewById(R.id.svUltrasoundOnline28);
        mSurfaceViewsOnline[29] = rootView.findViewById(R.id.svUltrasoundOnline29);
        mSurfaceViewsOnline[30] = rootView.findViewById(R.id.svUltrasoundOnline30);
        mSurfaceViewsOnline[31] = rootView.findViewById(R.id.svUltrasoundOnline31);
        for (int iVar = 0; iVar < SystemConfig.INT_SURFACE_VIEWS_ON_LINE_MAX_SIZE; iVar++) {
            mSurfaceViewsOnline[iVar].setZOrderOnTop(true);
        }


        mStrongPeakModeRB = rootView.findViewById(R.id.strongPeakModeOnline);
        mTwoPointModeRB = rootView.findViewById(R.id.twoPointModeOnline);
        mVTIModeGroup = rootView.findViewById(R.id.VTIModeGroupOnline);

        mVTIModeGroup.check(R.id.strongPeakModeOnline);
        mVTIModeGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            if (i == R.id.strongPeakModeOnline) {
                if (SystemConfig.mIntVTIModeIdx != SystemConfig.INT_VTI_MODE_0_STRONGEST) {
                    SystemConfig.mIntVTIModeIdx = SystemConfig.INT_VTI_MODE_0_STRONGEST;
                }
                mSurfaceViewTimeScaleUp.setVisibility(View.INVISIBLE);
                mSurfaceViewTimeScaleUpSpace.setVisibility(View.INVISIBLE);
            } else if (i == R.id.twoPointModeOnline) {
                if (SystemConfig.mIntVTIModeIdx != SystemConfig.INT_VTI_MODE_2_TWO_POINT) {
                    SystemConfig.mIntVTIModeIdx = SystemConfig.INT_VTI_MODE_2_TWO_POINT;
                }
                mSurfaceViewTimeScaleUp.setVisibility(View.VISIBLE);
                mSurfaceViewTimeScaleUpSpace.setVisibility(View.VISIBLE);
                mSurfaceViewTimeScaleDown.setVisibility(View.INVISIBLE);
                mSurfaceViewTimeScaleDownSpace.setVisibility(View.INVISIBLE);
            } else {
                SystemConfig.mIntVTIModeIdx = SystemConfig.INT_VTI_MODE_0_STRONGEST;
            }
        });

        mHRValueTextView = rootView.findViewById(R.id.hrValueTextView);
        mVTIValueTextView = rootView.findViewById(R.id.VTIValueTextView);
        mVpkValueTextView = rootView.findViewById(R.id.VpkValueTextView);
        mSVValueTextView = rootView.findViewById(R.id.SVValueTextView);
        mCOValueTextView = rootView.findViewById(R.id.COValueTextView);

        mBloodVelocityPlotter = new MyPlotterBloodVelocity(mSurfaceViewUltrasound, this, mSurfaceViewScale);

        mBloodVelocityPlotter.setEcgSegViews(ecgSegViews);

        if (SystemConfig.mIntTimeScaleEnabled != SystemConfig.INT_TIME_SCALE_ENABLED_YES) {
            invisibleAllTimeScaleSurface();
        }

        // Getting the characteristics from the application class
        //mReadCharacteristic = mApplication.getBluetoothgattDatacharacteristic();
        mNotifyCharacteristic = mApplication.getBluetoothgattDatacharacteristic();

        SystemConfig.mEnumTryState = SystemConfig.ENUM_TRY_STATE.STATE_TRY_STOP;
        SystemConfig.mEnumStartState = SystemConfig.ENUM_START_STATE.STATE_STOP;

        mHandlerReceiveDataBySegmentOnLine = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                //handleReceiveDataBySegmentOnLine(msg);
                receiveAttributeDataBySegmentOnLine(msg.what);
                super.handleMessage(msg);
            }
        };


        mHandlerUiMsg = new myUIHandler();
        /*new Handler() {
            @Override
            public void handleMessage(Message msg) {
                //super.handleMessage(msg);
                handleUiMsg(msg);
            }
        };*/
        bleStateImageView = rootView.findViewById(R.id.bleStateImageView);
        if (BluetoothLeService.getConnectionState() == BluetoothLeService.STATE_CONNECTED) {
            bleStateImageView.setImageResource(R.drawable.ic_bluetooth_connected_black_24dp);
        } else {
            bleStateImageView.setImageResource(R.drawable.ic_bluetooth_black_24dp);
        }

        mSingleVpkSwitch = rootView.findViewById(R.id.singleVpkSwitchOnline);
        mSingleVpkSwitch.setChecked(SystemConfig.mIntSingleVpkEnabled == SystemConfig.INT_SINGLE_VPK_ENABLED_YES);
        mSingleVpkSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                SystemConfig.mIntSingleVpkEnabled = SystemConfig.INT_SINGLE_VPK_ENABLED_YES;
                //mVpkValueTextView.setTextSize(12);
                // For phantom test 20210423
                SystemConfig.mIntGainLevelCommandSetting = 4;
                // For phantom test 20210423
//                    SystemConfig.mIntGainLevelCommandSetting = 2;
            } else {
                SystemConfig.mIntSingleVpkEnabled = SystemConfig.INT_SINGLE_VPK_ENABLED_NO;
                //mVpkValueTextView.setTextSize(18);
                // For phantom test 20210423
                SystemConfig.mIntGainLevelCommandSetting = 4;
                // For phantom test 20210423
//                    SystemConfig.mIntGainLevelCommandSetting =  SystemConfig.INT_GAIN_CONTROL_LEVEL_CNT;
            }
        });

        mInverseSwitch = rootView.findViewById(R.id.inverseSwitch);
        mInverseSwitch.setChecked(BVSignalProcessorPart1.isInverseFreq);
        mInverseSwitch.setOnCheckedChangeListener(((compoundButton, b) -> {
            BVSignalProcessorPart1.isInverseFreq = b;
        }));

        mFourthLevelSwitchOnline = rootView.findViewById(R.id.fourthLevelOnline);
        mFourthLevelSwitchOnline.setChecked(BVSignalProcessorPart1.isFourthLevel);
        mFourthLevelSwitchOnline.setOnCheckedChangeListener(((compoundButton, b) -> {
            BVSignalProcessorPart1.isFourthLevel = b;
            MainActivity.mBVSignalProcessorPart1.initButterworthFilterForUS();
        }));

        mSNRTitleTextView = rootView.findViewById(R.id.SNRtitleTextView);
        mSNRValueTextView = rootView.findViewById(R.id.SNRValueTextView);
        mAmpTitleTextView = rootView.findViewById(R.id.ampTitleTextView);
        mAmpValueTextView = rootView.findViewById(R.id.ampValueTextView);
        mVelocityTextView = rootView.findViewById(R.id.velocityTextView);
        mOnlineAngleTestTV = rootView.findViewById(R.id.onlineAngleTest);
        if (SystemConfig.isYuhul){
            resultVpk.setVisibility(View.GONE);
            resultVti.setVisibility(View.GONE);
        }
        if (SystemConfig.mTestMode) {
            // For phantom test 20210423
            SystemConfig.mIntGainLevelCommandSetting = 4;
            // For phantom test 20210423
            mSingleVpkSwitch.setVisibility(View.VISIBLE);
            mInverseSwitch.setVisibility(View.VISIBLE);
            mFourthLevelSwitchOnline.setVisibility(View.VISIBLE);
            mSNRTitleTextView.setVisibility(View.VISIBLE);
            mSNRValueTextView.setVisibility(View.VISIBLE);
            mAmpTitleTextView.setVisibility(View.VISIBLE);
            mAmpValueTextView.setVisibility(View.VISIBLE);
            mVelocityTextView.setVisibility(View.VISIBLE);
            mOnlineAngleTestTV.setVisibility(View.VISIBLE);
            resultVpk.setVisibility(View.VISIBLE);
            resultVti.setVisibility(View.VISIBLE);
        } else {
            SystemConfig.mIntGainLevelCommandSetting = SystemConfig.INT_GAIN_CONTROL_LEVEL_CNT;
            mSingleVpkSwitch.setVisibility(View.GONE);
            mInverseSwitch.setVisibility(View.GONE);
            mFourthLevelSwitchOnline.setVisibility(View.GONE);
            mSNRTitleTextView.setVisibility(View.GONE);
            mSNRValueTextView.setVisibility(View.GONE);
            mAmpTitleTextView.setVisibility(View.GONE);
            mAmpValueTextView.setVisibility(View.GONE);
            mVelocityTextView.setVisibility(View.GONE);
            mOnlineAngleTestTV.setVisibility(View.GONE);
        }

        greenLightImg = rootView.findViewById(R.id.greenlight);

        return rootView;
    }

    private void initEcgSegView(View rootView) {
        ecgSegViews = new EcgSegView[SystemConfig.INT_SURFACE_VIEWS_ON_LINE_USE_SIZE];
        ecgSegViews[0] = rootView.findViewById(R.id.ecgOnlineView0);
        ecgSegViews[1] = rootView.findViewById(R.id.ecgOnlineView1);
        ecgSegViews[2] = rootView.findViewById(R.id.ecgOnlineView2);
        ecgSegViews[3] = rootView.findViewById(R.id.ecgOnlineView3);
        ecgSegViews[4] = rootView.findViewById(R.id.ecgOnlineView4);
        ecgSegViews[5] = rootView.findViewById(R.id.ecgOnlineView5);
        ecgSegViews[6] = rootView.findViewById(R.id.ecgOnlineView6);
        ecgSegViews[7] = rootView.findViewById(R.id.ecgOnlineView7);
        ecgSegViews[8] = rootView.findViewById(R.id.ecgOnlineView8);
        ecgSegViews[9] = rootView.findViewById(R.id.ecgOnlineView9);
        ecgSegViews[10] = rootView.findViewById(R.id.ecgOnlineView10);
        ecgSegViews[11] = rootView.findViewById(R.id.ecgOnlineView11);
        ecgSegViews[12] = rootView.findViewById(R.id.ecgOnlineView12);
        ecgSegViews[13] = rootView.findViewById(R.id.ecgOnlineView13);
        ecgSegViews[14] = rootView.findViewById(R.id.ecgOnlineView14);
        ecgSegViews[15] = rootView.findViewById(R.id.ecgOnlineView15);
        ecgSegViews[16] = rootView.findViewById(R.id.ecgOnlineView16);
        ecgSegViews[17] = rootView.findViewById(R.id.ecgOnlineView17);
        ecgSegViews[18] = rootView.findViewById(R.id.ecgOnlineView18);
        ecgSegViews[19] = rootView.findViewById(R.id.ecgOnlineView19);
        ecgSegViews[20] = rootView.findViewById(R.id.ecgOnlineView20);
        ecgSegViews[21] = rootView.findViewById(R.id.ecgOnlineView21);
        ecgSegViews[22] = rootView.findViewById(R.id.ecgOnlineView22);
        ecgSegViews[23] = rootView.findViewById(R.id.ecgOnlineView23);
        ecgSegViews[24] = rootView.findViewById(R.id.ecgOnlineView24);
        ecgSegViews[25] = rootView.findViewById(R.id.ecgOnlineView25);
        ecgSegViews[26] = rootView.findViewById(R.id.ecgOnlineView26);

        ecgViewOffline = rootView.findViewById(R.id.ecgOffline);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG, "onCreateOptionMenu");
        inflater.inflate(R.menu.fake_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    // sometimes not try again after stop. why??
    private void reTry() {
        mTBtnTryNotify.setChecked(false);
        mTBtnTryNotify.setChecked(true);

    }

    private View.OnClickListener btnCalculateOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                if (SystemConfig.mEnumUltrasoundUIState == SystemConfig.ENUM_UI_STATE.ULTRASOUND_UI_STATE_ONLINE) {
                    if ((SystemConfig.mEnumTryState == SystemConfig.ENUM_TRY_STATE.STATE_TRY)
                            || (SystemConfig.mEnumStartState == SystemConfig.ENUM_START_STATE.STATE_START)) {
                        return;
                    }
                } else {

                }

                if (MainActivity.mBVSignalProcessorPart2Selected == null) {
                    return;
                }

                SystemConfig.mBoolProcessorPart2Selected = true;

                // jaufa, -, 181025
                //MainActivity.mBVSignalProcessorPart2Selected.processResultBloodSignalByRecalculate();
                // Cavin 200903
                //showResultBloodVelocityCommon(MainActivity.mSignalProcessController.getResultDataAfterSignalProcess());
                currentResult = MainActivity.mSignalProcessController.getResultDataAfterSignalProcessByWu();
                showResultBloodVelocityCommon(currentResult);

                //?enableBloodVelocityStartFromCalculateAction();
            } catch (Exception ex1) {
                ex1.printStackTrace();
                //SystemConfig.mMyEventLogger.appendDebugStr("Fragment.btnCalculateOnClick.Exception", "");
                //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(), "");
            }
        }
    };

    private View.OnClickListener btnSaveOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            saveToDatabase();
            mBtnSave.setEnabled(false);
            mBtnCalculate.setEnabled(false);
        }
    };


    private class myUIHandler extends Handler {
        //private final WeakReference<MainActivity> mActivity;

        public myUIHandler() {
            //mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            //MainActivity activity = mActivity.get();
            //if (activity!=null){
            handleUiMsg(msg);
            //}
        }

        private void handleUiMsg(Message msg) {
            int iMsgId, iCmd;

            try {

                iMsgId = msg.what;

                /*   if (iMsgId == MainActivity.UI_MSG_ID_AFTER_CREATE_VIEW) {
             if (SystemConfig.mEnumUltrasoundSubUIState == SystemConfig.ENUM_SUB_UI_STATE.BLOOD_FLOW_VELOCITY) {
                    if (SystemConfig.mEnumUltrasoundUIState == SystemConfig.ENUM_UI_STATE.ULTRASOUND_UI_STATE_ONLINE) {
                        mOnLineBloodVelocityUiManager.mBloodVelocityPlotter.drawScale();
                        mOnLineBloodVelocityUiManager.mBloodVelocityPlotter.clearUltrasoundSV();
                        if(SystemConfig.mIntTimeScaleEnabled == SystemConfig.INT_TIME_SCALE_ENABLED_YES){
                            mOnLineBloodVelocityUiManager.mBloodVelocityPlotter.setTimeScalePoint(0);
                            mOnLineBloodVelocityUiManager.mBloodVelocityPlotter.setTimeScaleLine();
                        }
                    } else {
                        //mOffLineBloodVelocityUiManager.mBloodVelocityPlotter.drawScale();
                        mOffLineBloodVelocityUiManager.mBloodVelocityPlotter.clearUltrasoundSV();
                        if(SystemConfig.mIntTimeScaleEnabled == SystemConfig.INT_TIME_SCALE_ENABLED_YES){
                            mOffLineBloodVelocityUiManager.mBloodVelocityPlotter.setTimeScalePoint(0);
                            mOffLineBloodVelocityUiManager.mBloodVelocityPlotter.setTimeScaleLine();
                        }
                    }
                } else if (SystemConfig.mEnumUltrasoundSubUIState == SystemConfig.ENUM_SUB_UI_STATE.RAW_DATA) {
                    if (SystemConfig.mEnumUltrasoundUIState == SystemConfig.ENUM_UI_STATE.ULTRASOUND_UI_STATE_ONLINE) {
                        mOnLineRawDataUiManager.mRawDataPlotter.clearUltrasoundSV();
                    } else {
                        mOffLineRawDataUiManager.mRawDataPlotter.clearUltrasoundSV();
                    }
                }
                } else */
                if (iMsgId == MainActivity.UI_MSG_ID_SHOW_POWER_LEVEL) {
                    //if((SystemConfig.mIntPowerControlEnabled == SystemConfig.INT_POWER_CONTROL_ENABLED_YES)
                    //        || (SystemConfig.mIntSystemSettingFunction == SystemConfig.INT_SYSTEM_SETTING_FUNCTION_3_POWER_CONSUME_TEST)){

                    MainActivity.updatePowerLevel(SystemConfig.mIntPowerLevel, mUserInfoTextView, mActivity);
                    if (intPrePowerLevel != SystemConfig.mIntPowerLevel) {
                        if (SystemConfig.mIntPowerLevel == 2 && intPrePowerLevel <= 3) {
                            //mActivity.onBackPressed();
                            stopAndDisableMeasurement();
                            lowPowerCount = 0;
                        }
                        intPrePowerLevel = SystemConfig.mIntPowerLevel;
                    }
                    if (SystemConfig.mIntPowerLevel != -1 && SystemConfig.mIntPowerLevel <= 2) {
                        lowPowerCount++;
                        if (lowPowerCount > 3) {
                            Log.d(TAG, "Low power count > 3 !!!!!!");
                            showMessageDialog(getString(R.string.alert_msg_low_power_charge), mActivity);
                            stopAndDisableMeasurement();
                            lowPowerCount = 0;
                        }
                    } else {
                        lowPowerCount = 0;
                    }
                    //}
//                } else if (iMsgId == MainActivity.UI_MSG_ID_SHOW_BLE_STATE) {
                /*if (SystemConfig.mEnumBleState == SystemConfig.ENUM_BLE_STATE.CONNECTION_CREATED) {
                    mTextViewBleState.setBackgroundColor(Color.GREEN);
                } else {
                    mTextViewBleState.setBackgroundColor(Color.RED);
                }*/
                    //}else if (iMsgId == UI_MSG_ID_SHOW_DATA_LOST_STATE) {
                    //        mTextViewDataLostState.setVisibility(View.VISIBLE);
//                } else if (iMsgId == MainActivity.UI_MSG_ID_DEBUG_SHOW_USCOM_RX) {
//                    //SystemConfig.mMyEventLogger.appendDebugIntEvent("Uscom.Rx=", UltrasoundComm.mIntDebugInt, 0, 0, 0, 0);
//                } else if (iMsgId == MainActivity.UI_MSG_ID_DEBUG_SHOW_RAWDATA_RX1) {
//                    // SystemConfig.mMyEventLogger.appendDebugIntEvent("RawData.Rx1=", UltrasoundComm.mIntDebugInt, 0, 0, 0, 0);
//                } else if (iMsgId == MainActivity.UI_MSG_ID_DEBUG_SHOW_PROC_DATA_RX1) {
//                    // SystemConfig.mMyEventLogger.appendDebugIntEvent("ProcData.Rx1=", UltrasoundComm.mIntDebugInt, 0, 0, 0, 0);
//                } else if (iMsgId == MainActivity.UI_MSG_ID_DEBUG_SHOW_PROC_DATA_RX2) {
//                    //SystemConfig.mMyEventLogger.appendDebugIntEvent("ProcData.Rx2=", UltrasoundComm.mIntDebugInt, 0, 0, 0, 0);
//                } else if (iMsgId == MainActivity.UI_MSG_ID_DEBUG_SHOW_PROC_DATA_RX3) {
//                    // SystemConfig.mMyEventLogger.appendDebugIntEvent("ProcData.Rx3=", UltrasoundComm.mIntDebugInt, 0, 0, 0, 0);
                } else if ((iMsgId == MainActivity.UI_MSG_ID_TIME_SCALE_TOUCH_DOWN)
                        || (iMsgId == MainActivity.UI_MSG_ID_TIME_SCALE_TOUCH_MOVE)
                        || (iMsgId == MainActivity.UI_MSG_ID_TIME_SCALE_TOUCH_UP)) {
                    processTimeScaleTouchEvent(iMsgId);
                    mBloodVelocityPlotter.DrawBySubSegOffLine(SystemConfig.mIntSVDrawStartBloodVelocity, SystemConfig.mIntSVDrawSizeBloodVelocity);
                } else if (iMsgId == MainActivity.UI_MSG_ID_SHOW_BV_SV_AFTER_ONLINE_START) {
                    mBloodVelocityPlotter.DrawBySubSegOffLine(SystemConfig.mIntSVDrawStartBloodVelocity, SystemConfig.mIntSVDrawSizeBloodVelocity);
                } else if (iMsgId == MainActivity.UI_MSG_ID_BLE_CONNECTED) {
                    Log.d(TAG, "iMsgId == MainActivity.UI_MSG_ID_BLE_CONNECTED");
                    bleStateImageView.setImageResource(R.drawable.ic_bluetooth_connected_black_24dp);
                    mTBtnTryNotify.setEnabled(true);
                    updateWithServiceDiscoveryFragment();
                } else if (iMsgId == MainActivity.UI_MSG_ID_BLE_DISCONNECTED) {
                    Log.d(TAG, "iMsgId == MainActivity.UI_MSG_ID_BLE_DISCONNECTED");
                    bleStateImageView.setImageResource(R.drawable.ic_bluetooth_black_24dp);
                    mTBtnTryNotify.setEnabled(false);
                }
            } catch (Exception ex1) {
                ex1.printStackTrace();
                //SystemConfig.mMyEventLogger.appendDebugStr("Fragment.handleUiMsg.Exception","");
                //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
            }
        }
    }

    private void stopAndDisableMeasurement() {
        if (SystemConfig.mEnumTryState == SystemConfig.ENUM_TRY_STATE.STATE_TRY) {
            mTBtnTryNotify.setChecked(false);

            mTBtnTryNotify.setEnabled(false);

        } else if (SystemConfig.mEnumStartState == SystemConfig.ENUM_START_STATE.STATE_START) {
            mTBtnRec.setChecked(false);

            mTBtnTryNotify.setEnabled(false);  // Run out of battery!! don't notify anymore before recharge.
        }
    }


    private void processTimeScaleTouchEvent(int iEventAction) {
        int iSubSegIdxCur;
        try {
            if (iEventAction == MainActivity.UI_MSG_ID_TIME_SCALE_TOUCH_DOWN) {
                mBloodVelocityPlotter.drawBVSvByTimeScaleTouchDown(mFloatSvOnTouchStartX);
                iSubSegIdxCur = mBloodVelocityPlotter.getTimeScaleSubSegmentIdx(mFloatTimeScalePosCur);
                /*setTimeScaleInfoCur(iSubSegIdxCur);*/
            } else if (iEventAction == MainActivity.UI_MSG_ID_TIME_SCALE_TOUCH_MOVE) {
                mBloodVelocityPlotter.drawBVSvByTimeScaleTouchMove(mFloatSvOnTouchStartX);
                iSubSegIdxCur = mBloodVelocityPlotter.getTimeScaleSubSegmentIdx(mFloatSvOnTouchStartX);
                /*setTimeScaleInfoCur(iSubSegIdxCur);*/
            } else if (iEventAction == MainActivity.UI_MSG_ID_TIME_SCALE_TOUCH_UP) {
                mBloodVelocityPlotter.drawBVSvByTimeScaleTouchUp(mFloatSvOnTouchStartX);
                iSubSegIdxCur = mBloodVelocityPlotter.getTimeScaleSubSegmentIdx(mFloatSvOnTouchStartX);
                /*setTimeScaleInfoCur(iSubSegIdxCur);*/
            }
        } catch (Exception ex1) {
            ex1.printStackTrace();
        }
    }

/*    private void handleReceiveDataBySegmentOnLine(Message msg){
        receiveAttributeDataBySegmentOnLine(msg.what);
    }*/

    public void receiveAttributeDataBySegmentOnLine(int iRxState) {
        try {
            if (SystemConfig.mEnumTryState == SystemConfig.ENUM_TRY_STATE.STATE_TRY) {
                updateSViewBySegmentOnLine(iRxState);
                if (iRxState == MyThreadQMsg.INT_MY_MSG_EVT_SPROCESS_DATA_IN_END) {
                    if ((SystemConfig.mEnumUltrasoundSubUIState == SystemConfig.ENUM_SUB_UI_STATE.BLOOD_FLOW_VELOCITY)
                            || (SystemConfig.mEnumUltrasoundSubUIState == SystemConfig.ENUM_SUB_UI_STATE.AUX_INFO)) {
                        if (SystemConfig.mEnumTryState == SystemConfig.ENUM_TRY_STATE.STATE_TRY) {
                            tryEndAction(true);
                        } else {
                            //enableStartEndAction();
                            recEndAction();
                        }
                    }
                }
                return;
            }

            //if(SystemConfig.mEnumStartState == SystemConfig.ENUM_START_STATE.STATE_START) {
            if (iRxState == MyThreadQMsg.INT_MY_MSG_EVT_SPROCESS_DATA_IN) {
                if (MainActivity.indexOfCurrentNavigationItem == 1) {
                    updateSViewBySegmentOnLine(iRxState);
                }
            } else if (iRxState == MyThreadQMsg.INT_MY_MSG_EVT_SPROCESS_DATA_IN_START) {
                //SystemConfig.setVelocityDrawSizeToItri8KOnLine();
                // if ((SystemConfig.mEnumUltrasoundSubUIState == SystemConfig.ENUM_SUB_UI_STATE.BLOOD_FLOW_VELOCITY)
                //         || (SystemConfig.mEnumUltrasoundSubUIState == SystemConfig.ENUM_SUB_UI_STATE.AUX_INFO)) {
                // mOnLineBloodVelocityUiManager.visibleSplitSViews();
                //    visibleSplitSViews();
                //}
                updateSViewBySegmentOnLine(iRxState);
            } else if (iRxState == MyThreadQMsg.INT_MY_MSG_EVT_SPROCESS_DATA_IN_END) {
                //SystemConfig.mMyEventLogger.appendDebugStr("DATA_IN_END","");
                recEndAction();
                if ((SystemConfig.mEnumUltrasoundSubUIState == SystemConfig.ENUM_SUB_UI_STATE.BLOOD_FLOW_VELOCITY)
                        || (SystemConfig.mEnumUltrasoundSubUIState == SystemConfig.ENUM_SUB_UI_STATE.AUX_INFO)) {
                    //enableStartEndAction();
                    //recEndAction();
                    setBloodVelocityStartDataPosWhenEndOnLine();
                } else if (SystemConfig.mEnumUltrasoundSubUIState == SystemConfig.ENUM_SUB_UI_STATE.RAW_DATA) {
                    //SystemConfig.mMyEventLogger.appendDebugStr("DATA_IN_END=", "RAW_DATA");
                    //SystemConfig.mMyEventLogger.appendDebugStr("mIntPacketCntForRun=", String.valueOf(SystemConfig.mIntRawPacketCntsOnLineForRun));
                    //enableStartEndAction();
                    //recEndAction();
                    /*
                    mOnLineRawDataUiManager.setRawDataEndDataPosOnLine();
                    mBtnAngleEstimateStart.setVisibility(View.VISIBLE);
                    mBtnStop.setVisibility(View.GONE);
                    */
                }
                updateSViewBySegmentOnLine(iRxState);
            } else if (iRxState == MyThreadQMsg.INT_MY_MSG_EVT_SPROCESS_RESULT_OK) {
                //SystemConfig.mMyEventLogger.appendDebugStr("RESULT_OK","");
                switchDrawSurfaceViews(true);

                if ((SystemConfig.mEnumUltrasoundSubUIState == SystemConfig.ENUM_SUB_UI_STATE.BLOOD_FLOW_VELOCITY)
                        || (SystemConfig.mEnumUltrasoundSubUIState == SystemConfig.ENUM_SUB_UI_STATE.AUX_INFO)) {
                    setBloodVelocityStartDataPosWhenEndOnLine();
                    // jaufa, -, 181025
                    //mOnLineBloodVelocityUiManager.mBloodVelocityPlotter.DrawBySubSegOffLine(SystemConfig.mIntSVDrawStartBloodVelocity, SystemConfig.mIntSVDrawSizeBloodVelocity);
                    //if(SystemConfig.mEnumBVResultReselectType == SystemConfig.ENUM_BV_RESULT_RESELECT_TYPE.VPK_RESELECT) {
                    //    MainActivity.mSignalProcessController.reSelectBVResultByVpk();
                    //}else if(SystemConfig.mEnumBVResultReselectType == SystemConfig.ENUM_BV_RESULT_RESELECT_TYPE.VTI_RESELECT) {
                    //    MainActivity.mSignalProcessController.reSelectBVResultByVpk();
                    //    MainActivity.mSignalProcessController.reSelectBVResultByVTI();
                    //}
//                    currentResult = MainActivity.mSignalProcessController.getResultDataAfterSignalProcess();

                    //Leslie
                    //currentResult = MainActivity.mSignalProcessController.getResultDataAfterSignalProcessWu201022();
                    currentResult = new GIS_DataController().getMeasureResult();
                    //Leslie End_20230308

                    showResultBloodVelocityCommon(currentResult);
//                    if ((currentResult.ErrCode&BVSignalProcessorPart1.BINARY_ERR_CODE_ELECTRICAL_INTEFFERENCE)==BVSignalProcessorPart1.BINARY_ERR_CODE_ELECTRICAL_INTEFFERENCE){
//                        showMessageDialog(getString(R.string.alert_title_error),mActivity); // Electrical Interference
//                    }
                    mTBtnRec.setChecked(false);
//                    mTBtnRec.setVisibility(View.GONE);        // Viento want it visible after once record.
                    mBtnCalculate.setEnabled(true);
                    mBtnSave.setEnabled(true);

                    //SystemConfig.mMyEventLogger.appendDebugStr("Gain Level End = ", String.valueOf(SystemConfig.mIntGainLevel));
                }
//                else {
                //mBtnAngleEstimateStart.setVisibility(View.VISIBLE);
                //mBtnStop.setVisibility(View.GONE);
//                }

                /*if(SystemConfig.mRawDataProcessor.mBoolRxError){
                    mTextViewDataLostState.setVisibility(View.VISIBLE);
                }else{
                    mTextViewDataLostState.setVisibility(View.GONE);
                }*/
                if (SystemConfig.mEnumUltrasoundSubUIState == SystemConfig.ENUM_SUB_UI_STATE.BLOOD_FLOW_VELOCITY) {

                    /*if(SystemConfig.mIntTimeScaleEnabled == SystemConfig.INT_TIME_SCALE_ENABLED_YES) {
                        mOnLineBloodVelocityUiManager.mBloodVelocityPlotter.setTimeScalePoint(mFloatTimeScalePosCur);
                        iSubSegIdx = mOnLineBloodVelocityUiManager.mBloodVelocityPlotter.getTimeScaleSubSegmentIdx();
                        setTimeScaleInfoCur(iSubSegIdx);
                    }
                    */
                    //putUiMsg(MainActivity.UI_MSG_ID_SHOW_BV_SV_AFTER_ONLINE_START);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> mBloodVelocityPlotter.DrawBySubSegOffLine(SystemConfig.mIntSVDrawStartBloodVelocity, SystemConfig.mIntSVDrawSizeBloodVelocity));
                    }
                }
            }
            //}
        } catch (Exception ex1) {
            ex1.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugStr("Fragment.receiveAttrDataBtSegOnLine.Exception", "");
            // SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(), "");
        }
    }


    public void updateSViewBySegmentOnLine(int iRxState) {

        try {
            //Log.d("onlineFragment","updateSViewBySegmentOnLine iRxState="+iRxState);
            if ((SystemConfig.mEnumUltrasoundSubUIState == SystemConfig.ENUM_SUB_UI_STATE.BLOOD_FLOW_VELOCITY)
                    || (SystemConfig.mEnumUltrasoundSubUIState == SystemConfig.ENUM_SUB_UI_STATE.AUX_INFO)) {
                // mOnLineBloodVelocityUiManager.updateSViewBySegmentOnLine();
                if (MainActivity.currentFragmentTag.equals(Constants.ITRI_ULTRASOUND_FRAGMENT_TAG)) {
                    mBloodVelocityPlotter.DrawLinesBySegmentOnLine();
                }


                //mOnLineBloodVelocityUiManager.updatePowerStatusOnLine();
            }
//            else if (SystemConfig.mEnumUltrasoundSubUIState == SystemConfig.ENUM_SUB_UI_STATE.RAW_DATA) {
                /*
                mOnLineRawDataUiManager.updateSViewBySegmentOnLine();
                mOnLineRawDataUiManager.updateRawDataViewBySegmentOnLine();
                */
//            }
        } catch (Exception ex1) {
            ex1.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugStr("Fragment.updateSViewBySegOnLine.Exception", "");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(), "");
        }
    }

    public void updateSNRAmpValue(final int inputSNR, final double inputVelocity, final int inputAmp) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                mSNRValueTextView.setText("" + inputSNR);
                mAmpValueTextView.setText("" + inputAmp + " , gain=" + SystemConfig.mIntGainLevel);
                String strV = String.format("%.02f", inputVelocity);
                mVelocityTextView.setText("Velocity: " + strV);
            });
        }
    }

    public void updateHRValue(final int HR, final boolean isStable) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (isStable && SystemConfig.isPAvoice>=3) {
//                    mHRValueTextView.setTextColor(Color.BLACK);
                    if (!mTBtnRec.isChecked()) {
                        mTBtnRec.setTextColor(Color.argb(255, 0, 117, 0));
                        greenLightImg.setVisibility(View.VISIBLE);
                    }
                } else {
//                    mHRValueTextView.setTextColor(Color.GRAY);
                    mTBtnRec.setTextColor(Color.BLACK);
                    greenLightImg.setVisibility(View.INVISIBLE);
                }
//                mHRValueTextView.setText(String.format(Locale.US, "%d", HR));
            });
        }
    }

    public void setBloodVelocityStartDataPosWhenEndOnLine() {
        int iStartPos, iCurPos;

        iCurPos = MainActivity.mBVSignalProcessorPart1.getCurSubSegSize();
        if (iCurPos >= SystemConfig.mIntSVDrawSizeBloodVelocity) {
            iStartPos = iCurPos - SystemConfig.mIntSVDrawSizeBloodVelocity;
        } else {
            iStartPos = 0;
        }
        SystemConfig.mIntSVDrawStartBloodVelocity = iStartPos;
    }

    private void initResultValue() {
        mHRValueTextView.setText("--");
        mCOValueTextView.setText("--");
        mSVValueTextView.setText("--");
        mVTIValueTextView.setText("--");
        mVpkValueTextView.setText("--");
    }

    protected void showResultBloodVelocityCommon(dataInfo inputData) {
        int iDrawStartIdx, iDrawSize, iHR;
        double doubleCardiacOutput, doubleHR, doubleRadius;
        String strHR, strPeakVelocity, strVTI, strStrokeVolume, strCardiacOutput, strCSArea, strRemark;
        String strVpk2, strVTI2, strSV2, strCO2;
        BigDecimal bigDecimal;
        double doubleVpkAfterAngleAfterCali;
        double doubleVtiAfterAngleAfterCali;
        double doubleSVAfterAngleAfterCali;
        double doubleCOAfterAngleAfterCali;

        try {
            iDrawStartIdx = SystemConfig.mIntSVDrawStartBloodVelocity;
            iDrawSize = SystemConfig.mIntSVDrawSizeBloodVelocity;
            //userInfoSettingUiManager = new UserInfoSettingUiManager(SystemConfig.mFragment);

            //------------------------------------------------------------------
            // show common data
            //------------------------------------------------------------------
            /*strRemark = "[Vpk Remark 1] : " + " Doppler Angle = " + String.valueOf(UserManagerCommon.mUserInfoCur.mIntAngle) + " " + "Deg.";*/
//            strRemark = "[Vpk Remark 1] : " + " Doppler Angle = " + String.valueOf(UserManagerCommon.mUserInfoCur.angle) + " " + "Deg.";
            /*SystemConfig.mFragment.mTextViewVpkRemark1.setText(strRemark);*/
//            bigDecimal = new BigDecimal(UserManagerCommon.mDoubleCosineUserAngle);
//            bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);// 小數後面1位, 四捨五入
            /*strRemark = "[Vpk Remark 2] : " + "Cosine(" + String.valueOf(UserManagerCommon.mUserInfoCur.mIntAngle) + ") = " + bigDecimal.doubleValue();*/
//            strRemark = "[Vpk Remark 2] : " + "Cosine(" + String.valueOf(UserManagerCommon.mUserInfoCur.angle) + ") = " + bigDecimal.doubleValue();

            //------------------------------------------------------------------
            // check if single vpk
            //------------------------------------------------------------------
            if (SystemConfig.mIntSingleVpkEnabled == SystemConfig.INT_SINGLE_VPK_ENABLED_YES) {
                //MainActivity.mBVSignalProcessorPart1.processMaxIdxByMovingAverageForSingleVpk();
                if (MainActivity.mBVSignalProcessorPart2Selected != null) {
                    if (SystemConfig.mEnumUltrasoundUIState == SystemConfig.ENUM_UI_STATE.ULTRASOUND_UI_STATE_OFFLINE) {
                        mBloodVelocityPlotter.DrawBySubSegOffLine(iDrawStartIdx, iDrawSize);
                    }
                }
                showBloodVelocityForSingleVpk(inputData);
                return;
            }

            //------------------------------------------------------------------
            // check if data available
            //------------------------------------------------------------------

            if (!SystemConfig.mBoolProcessorPart2Selected) {

                initResultValue();

                mBloodVelocityPlotter.DrawBySubSegOffLine(iDrawStartIdx, iDrawSize);
                return;
            }

            //------------------------------------------------------------------
            // for Heart Rate
            //------------------------------------------------------------------
            //iHR = (int) MainActivity.mBVSignalProcessorPart2Selected.getHRAverage();
            iHR = inputData.HR;
            mHRValueTextView.setTextColor(Color.BLACK);
            greenLightImg.setVisibility(View.INVISIBLE);
            if (mTBtnRec.isEnabled()) {
                mTBtnRec.setTextColor(Color.BLACK);
            } else {
                mTBtnRec.setTextColor(Color.GRAY);
            }

            if (iHR <= 0) {
                mHRValueTextView.setText("--");
            } else {
                strHR = String.valueOf(iHR) + " ";
                mHRValueTextView.setText(strHR);
            }

            //----------------------------------------------------------------------------------
            // for Vpk
            //-----------------------------------------------------------------------------------
            //if (MainActivity.mBVSignalProcessorPart2Selected.getPeakVelocityAverage() <= 0) {
            if (inputData.Vpk <= 0) {
                //Log.d(TAG,"VPK : "+mStrFailed );
                mVpkValueTextView.setText("--");
            } else {
                //doubleVpkAfterAngleAfterCali = MainActivity.mBVSignalProcessorPart2Selected.mDoubleVpkMeterAverageAfterAngleAfterCali;
                doubleVpkAfterAngleAfterCali = inputData.Vpk;
                // if(SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_0_SNSI_GM) {
                //     strVpk2 = String.format("%.2f", doubleVpkAfterAngleAfterCali * 2.0);
                //}else {
                strVpk2 = String.format(Locale.US, "%.2f", doubleVpkAfterAngleAfterCali);
                //}
                mVpkValueTextView.setText(strVpk2);
            }

            //---------------------------------------------------------------
            // for VTI
            //---------------------------------------------------------------
            //if(MainActivity.mBVSignalProcessorPart2Selected.getVTIAverage() <= 0) {
            if (inputData.VTI <= 0) {
                Log.d(TAG, "VTI : " + mStrFailed);
            } else {
                //doubleVtiAfterAngleAfterCali = MainActivity.mBVSignalProcessorPart2Selected.mDoubleVtiCmAverageAfterAngleAfterCali;
                doubleVtiAfterAngleAfterCali = inputData.VTI;
                //if(SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_0_SNSI_GM) {
                //    strVTI2 = String.format("%.2f", doubleVtiAfterAngleAfterCali * 2.0);
                //}else {
                strVTI2 = String.format(Locale.US, "%.2f", doubleVtiAfterAngleAfterCali);
                // }
                mVTIValueTextView.setText(strVTI2);
            }

            //-------------------------------------------------
            // for Stroke Volume
            //--------------------------------------------------
            //if(MainActivity.mBVSignalProcessorPart2Selected.mDoubleStrokeVolumeAverage <= 0) {
            if (inputData.SV <= 0) {
                mSVValueTextView.setText("--");
            } else {
                //doubleSVAngle = MainActivity.mBVSignalProcessorPart2Selected.mDoubleStrokeVolumeAverageAfterUserAngle;
                //doubleSVAfterAngleAfterCali = MainActivity.mBVSignalProcessorPart2Selected.mDoubleStrokeVolumeAverageAfterAngleAfterCali;
                doubleSVAfterAngleAfterCali = inputData.SV;
                //doubleSVOri = MainActivity.mBVSignalProcessorPart2Selected.mDoubleStrokeVolumeAverageOri;

                //if(SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_0_SNSI_GM) {
                //    strSV2 = String.format("%.2f", doubleSVAfterAngleAfterCali * 2.0);
                //}else{
                strSV2 = String.format(Locale.US, "%.2f", doubleSVAfterAngleAfterCali);
                //}
                mSVValueTextView.setText(strSV2);
            }

            mOnlineAngleTestTV.setText(String.format("%s", SystemConfig.rxAngle));

            if (UserManagerCommon.mDoubleUserPulmDiameterCm <= 0.0) {
                /*SystemConfig.mFragment.mTextViewSVRemark2Value.setText("0");*/
            } else {
                doubleRadius = UserManagerCommon.mDoubleUserPulmDiameterCm / 2;
                bigDecimal = new BigDecimal(doubleRadius * doubleRadius * Math.PI);
                bigDecimal = bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP);// 小數後面1位, 四捨五入
                strCSArea = bigDecimal.doubleValue() + " ";
                /*SystemConfig.mFragment.mTextViewSVRemark2Value.setText(strCSArea);*/
                //userInfoSettingUiManager = null;
            }

            //-------------------------------------------------
            // for Cardiac Output
            //--------------------------------------------------
            //if(MainActivity.mBVSignalProcessorPart2Selected.mDoubleCOAverage < 0) {
            if (inputData.CO <= 0) {
                mCOValueTextView.setText("--");
            } else {
                //doubleCOAngle = MainActivity.mBVSignalProcessorPart2Selected.mDoubleCOAverageAfterUserAngle;
                //doubleCOAfterAngleAfterCali = MainActivity.mBVSignalProcessorPart2Selected.mDoubleCOAverageAfterAngleAfterCali;
                doubleCOAfterAngleAfterCali = inputData.CO;
                //doubleCOOri = MainActivity.mBVSignalProcessorPart2Selected.mDoubleCOAverageOri;

                //strCardiacOutput = String.format("%.2f", doubleCOAngle);
                //if(SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_0_SNSI_GM) {
                //   strCO2 = String.format("%.2f", doubleCOAfterAngleAfterCali * 2.0);
                //}else{
                strCO2 = String.format(Locale.US, "%.2f", doubleCOAfterAngleAfterCali);
                //}
                mCOValueTextView.setText(strCO2);
            }

            //-------------------------------------------------
            // for Debug Information
            //--------------------------------------------------

            /*if (MainActivity.mBVSignalProcessorPart2Selected != null) {
                if (SystemConfig.mEnumUltrasoundUIState == SystemConfig.ENUM_UI_STATE.ULTRASOUND_UI_STATE_OFFLINE) {
                    mBloodVelocityPlotter.DrawBySubSegOffLine(iDrawStartIdx, iDrawSize);
                }
            }*/

        } catch (Exception ex1) {
            ex1.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugStr("UiManBV.showResultBVCommon.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }

    public void invisibleAllSurfaceViews() {
        int iVar;

        /*mSurfaceViewUltrasound.setVisibility(View.INVISIBLE);
        for (iVar = 0 ; iVar < SystemConfig.INT_SURFACE_VIEWS_ON_LINE_MAX_SIZE ; iVar++){
            mSurfaceViewsOnline[iVar].setVisibility(View.GONE);
        }*/
        invisibleAllTimeScaleSurface();
    }

    public void invisibleAllTimeScaleSurface() {
        mSurfaceViewTimeScaleUpSpace.setVisibility(View.GONE);
        mSurfaceViewTimeScaleDownSpace.setVisibility(View.GONE);
        mSurfaceViewTimeScaleUp.setVisibility(View.GONE);
        mSurfaceViewTimeScaleDown.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SystemConfig.mIntPowerLevel != -1 && SystemConfig.mIntPowerLevel != 0) {
            MainActivity.updatePowerLevel(SystemConfig.mIntPowerLevel, mUserInfoTextView, mActivity);
        }
    }

    @Override
    public void onDestroy() {
        //----------- for Audio Player Controller -------
        //if (MainActivity.mAudioPlayerController!=null){
        //    MainActivity.mAudioPlayerController.stopThread();
        //}

        if (MainActivity.mIsNotifyEnabled) {
            tryEndAction(true);
            if (mHandler != null) {
                mHandler.removeCallbacks(mRunnable);
                mHandler = null;
            }
        }

        super.onDestroy();
    }

    private View.OnClickListener btnNotifyOnClick = view -> {
        String strDebug;

        if (SystemConfig.mIntSoundEnabled == SystemConfig.INT_SOUND_ENABLED_YES) {
            MainActivity.mAudioPlayerController.clearAllMsg();
            //SystemConfig.mAudioPlayerController.putMsgForAudioOpen(SystemConfig.mIntUltrasoundSamplerate);
            MainActivity.mAudioPlayerController.putMsgForAudioOpen(SystemConfig.mIntUltrasoundSamplerate);
        }

        enableTryAction();
/*
            if (SystemConfig.mBoolLongStartEnabled) {
                SystemConfig.mBoolLongStartFirst = true;
            }

            SystemConfig.mMyEventLogger.appendDebugStr("****************************", "");
            strDebug = "**** New Try : " + SystemConfig.mMyEventLogger.getDateStr() + " ***";
            SystemConfig.mMyEventLogger.appendDebugStr(strDebug, "");
*/
/*
            mBtnTry.setVisibility(View.GONE);
            mBtnTryStop.setVisibility(View.VISIBLE);
            if (!SystemConfig.mBoolLongStartEnabled) {
                mBtnTryStart.setVisibility(View.VISIBLE);
            } else {
                mBtnTryStart.setVisibility(View.GONE);
            }
            mBtnStop.setVisibility(View.GONE);
            SystemConfig.mActivity.invisibleMeasureMenu();
        */
    };


    /**
     * Stopping Broadcast receiver to broadcast notify characteristics
     *
     * @param gattCharacteristic Input Bluetooth GATT characteristic.
     */
    void stopBroadcastDataNotify(BluetoothGattCharacteristic gattCharacteristic) {
        if (gattCharacteristic != null) {
            if ((gattCharacteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                BluetoothLeService.setCharacteristicNotification(
                        gattCharacteristic, false);
            }
        }
    }


    private void enableTryAction() {
        //    try {
        if (BluetoothLeService.getConnectionState() != BluetoothLeService.STATE_CONNECTED) {
            Log.d(TAG, "connection state!=BluetoothLeService.STATE_CONNECTED");
            return;
        }

        // Keep screen on
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        switchDrawSurfaceViews(false);
        MainActivity.mDoublePER = 0.0;
        MainActivity.mRawDataProcessor.prepareStartOnLine();
        MainActivity.mBVSignalProcessorPart1.prepareStart();

        mBloodVelocityPlotter.prepareStartOnLine();
        MainActivity.mSignalProcessController.putSignalProcessControllerMsgForPrepareCmd();


        if (SystemConfig.mEnumUltrasoundSubUIState == SystemConfig.ENUM_SUB_UI_STATE.RAW_DATA) {
            // Do nothing
        } else if ((SystemConfig.mEnumUltrasoundSubUIState == SystemConfig.ENUM_SUB_UI_STATE.BLOOD_FLOW_VELOCITY)
                || (SystemConfig.mEnumUltrasoundSubUIState == SystemConfig.ENUM_SUB_UI_STATE.AUX_INFO)) {

            initResultValue();
            mSurfaceViewScale.setVisibility(View.VISIBLE);
        } else {
            // Do nothing
        }

        SystemConfig.mEnumTryState = SystemConfig.ENUM_TRY_STATE.STATE_TRY;


        //SystemConfig.mUltrasoundComm.notifyStartCharacteristic();
       /* if (SystemConfig.isHeartIO2){
            BluetoothGattCharacteristic gattChara = mApplication.getBluetoothgattRuncharacteristic();
            BluetoothLeService.write2Char3(1,gattChara);
        }else{*/
        BluetoothLeService.setCharacteristicNotification(mApplication.getBluetoothgattDatacharacteristic(), //mDataNotifyCharacteristic,
                true);
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        BluetoothLeService.mBluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
//        }

        //SystemConfig.mMyEventLogger.appendDebugStr("Send Notify Start");
        // mBoolNotifyUsedByDataComm = true;
        //SystemConfig.mUltrasoundComm.mBoolRxData = true;





        /*} catch (Exception e) {
            SystemConfig.mMyEventLogger.appendDebugStr("Fragment.enableTryAct.Exception","");
            SystemConfig.mMyEventLogger.appendDebugStr(e.toString(),"");
        }*/
    }

    private void tryStopAction() {

        MainActivity.mRawDataProcessor.mEnumUltrasoundAttributeReceiveState = RawDataProcessor.ENUM_RAW_DATA_RX_STATE.RECEIVE_STATE_END;
        MainActivity.mRawDataProcessor.mEnumUltrasoundOneDataReceiveState = RawDataProcessor.ENUM_RAW_DATA_ONE_DATA_RX_STATE.RECEIVE_STATE_END;
        GIS_Log.e("Leslie","tryStopAction");

        if (SystemConfig.isHeartIO2) {
            for (EcgSegView ecgSegView : ecgSegViews) {
                ecgSegView.isRunning = false;
            }
            if (SystemConfig.saveFile && MainActivity.mRawDataProcessor.openfile_status == 1) {
                MainActivity.mRawDataProcessor.closeDataFile();
                MainActivity.mRawDataProcessor.openfile_status = 0;
            }
        }
        tryEndAction(true);

        if (SystemConfig.mIntSoundEnabled == SystemConfig.INT_SOUND_ENABLED_YES) {
            //SystemConfig.mAudioPlayerController.closeAudio();
            MainActivity.mAudioPlayerController.putMsgForAudioClose();
        }
        if (MainActivity.mRawDataProcessor.mBoolRxError) {
            Log.d(TAG, "Lost Count = " + MainActivity.mRawDataProcessor.mIntLostCount);
        }
    }

    public void tryEndAction(boolean boolNotifyStop) {
        SystemConfig.mEnumTryState = SystemConfig.ENUM_TRY_STATE.STATE_TRY_STOP;
        if (boolNotifyStop) {
            //SystemConfig.mUltrasoundComm.notifyStopCharacteristic();
            /*
            if (SystemConfig.isHeartIO2){
                BluetoothGattCharacteristic gattChara = mApplication.getBluetoothgattRuncharacteristic();
                BluetoothLeService.write2Char3(0,gattChara);
            }else {*/
            stopBroadcastDataNotify(mNotifyCharacteristic);
//            }
        }
        if (getActivity() != null) {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        /*
        SystemConfig.mUltrasoundComm.mBoolRxData = false;
        mBoolNotifyUsedByDataComm = false;
        */
    }

    private void startRecord() {
        int iVar;

        // try {

        if (BluetoothLeService.getConnectionState() != BluetoothLeService.STATE_CONNECTED) {
                /*
                mTextViewBleState.setBackgroundColor(Color.RED);
                SystemConfig.mMyEventLogger.appendDebugStr("Star but Disconnected","");
                mBtnTry.setVisibility(View.VISIBLE);
                mBtnTryStop.setVisibility(View.GONE);
                mBtnTryStart.setVisibility(View.GONE);
                mBtnLongStart.setVisibility(View.GONE);
                mBtnTryStop.setVisibility(View.GONE);
                */
            return;
        }

        switchDrawSurfaceViews(false);

        MainActivity.mRawDataProcessor.prepareStartOnLine();
        MainActivity.mBVSignalProcessorPart1.prepareStart();
        mBloodVelocityPlotter.prepareStartOnLine();

        MainActivity.mSignalProcessController.putSignalProcessControllerMsgForPrepareCmd();

        SystemConfig.mEnumStartState = SystemConfig.ENUM_START_STATE.STATE_START;

        if (SystemConfig.isHeartIO2) {
            if (SystemConfig.saveFile && MainActivity.mRawDataProcessor.openfile_status == 0) {
                MainActivity.mRawDataProcessor.createDataFile(mActivity);
                MainActivity.mRawDataProcessor.openfile_status = 1;
                MainActivity.mRawDataProcessor.nPacketStored = 0;
            }
        }

        // for update Power level flag
            /*
            mBoolNotifyUsedByDataComm = true;
*/
        //SystemConfig.mUltrasoundComm.notifyStartCharacteristic();
        BluetoothLeService.setCharacteristicNotification(mApplication.getBluetoothgattDatacharacteristic(), //mDataNotifyCharacteristic,
                true);
/*
            SystemConfig.mUltrasoundComm.mBoolRxData = true;
*/
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        BluetoothLeService.mBluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);

      //  } catch (Exception e) {
       //     SystemConfig.mMyEventLogger.appendDebugStr("enableStartAction.Exception","");
       //     SystemConfig.mMyEventLogger.appendDebugStr(e.toString(),"");
       // }
    }

    private void stopRecord(){
        MainActivity.mRawDataProcessor.mEnumUltrasoundAttributeReceiveState = RawDataProcessor.ENUM_RAW_DATA_RX_STATE.RECEIVE_STATE_END;
        MainActivity.mRawDataProcessor.mEnumUltrasoundOneDataReceiveState = RawDataProcessor.ENUM_RAW_DATA_ONE_DATA_RX_STATE.RECEIVE_STATE_END;
        GIS_Log.e("Leslie","stopRecord");

        recEndAction();
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

        //SystemConfig.mUltrasoundComm.notifyStopCharacteristic();
//        if (!SystemConfig.isHeartIO2){
            stopBroadcastDataNotify(mNotifyCharacteristic);
//        }
        //SystemConfig.mMyEventLogger.appendDebugStr("Send Notify Stop");
        //SystemConfig.mUltrasoundComm.mBoolRxData = false;
        //mBoolNotifyUsedByDataComm = false;
    }

    public void switchDrawSurfaceViews(boolean boolMainSurfaceView) {
        int iVar;

        if(boolMainSurfaceView){
            for (iVar = 0; iVar < SystemConfig.INT_SURFACE_VIEWS_ON_LINE_USE_SIZE; iVar++) {
                mSurfaceViewsOnline[iVar].setVisibility(View.GONE);
                if (SystemConfig.isHeartIO2){
                    ecgSegViews[iVar].setVisibility(View.GONE);
                }
            }
            mSurfaceViewUltrasound.setVisibility(View.VISIBLE);
            if (SystemConfig.isHeartIO2){
                ecgViewOffline.setVisibility(View.VISIBLE);
            }
        }else {
            mSurfaceViewUltrasound.setVisibility(View.GONE);
            if (SystemConfig.isHeartIO2){
                ecgViewOffline.setVisibility(View.GONE);
            }
            for (iVar = 0; iVar < SystemConfig.INT_SURFACE_VIEWS_ON_LINE_USE_SIZE; iVar++) {
                mSurfaceViewsOnline[iVar].setVisibility(View.VISIBLE);
                if (SystemConfig.isHeartIO2){
                    ecgSegViews[iVar].setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public void putUiMsg(int iMsgId){
        Message uiMessage;

        //try {
            uiMessage = new Message();
            uiMessage.what = iMsgId;
            mHandlerUiMsg.sendMessage(uiMessage);
       // } catch (Exception ex1) {
            //SystemConfig.mMyEventLogger.appendDebugStr("Fragment.putUiMsg.Exception", "");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(), "");
        //}
    }

    public View.OnTouchListener mSvUltrasoundOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //Log.d(TAG,"onTouch event.getX:"+event.getX()+"  event.getY: "+event.getY());
            int iEventAction, iCurSubSeg, iSubSegIdx;
            float fCurVpkPosY, fDiffY;
            float fSvHeight;

            try {
                if ((SystemConfig.mEnumTryState == SystemConfig.ENUM_TRY_STATE.STATE_TRY)
                        || (SystemConfig.mEnumStartState == SystemConfig.ENUM_START_STATE.STATE_START)){
                    return true;
                }

                if(MainActivity.mRawDataProcessor.mIntDataNextIndex == 0){
                    return true;
                }

                //Log.d(TAG,"onTouch event.getAction="+event.getAction());
                iEventAction = event.getAction();
                switch (iEventAction) {
                    case MotionEvent.ACTION_DOWN:
                        mFloatSvOnTouchStartX = event.getX();
                        mFloatSvOnTouchStartY = event.getY();
                        mFloatSvOnTouch2PointX = event.getX();
                        fSvHeight = mSurfaceViewUltrasound.getHeight();
                        mBoolSvUltrasoundTouchAtUpper = mFloatSvOnTouchStartY >= (fSvHeight / 2.0);
                        if(!mBoolSvUltrasoundTouchAtUpper) {
                            if (SystemConfig.mIntVTIModeIdx == SystemConfig.INT_VTI_MODE_2_TWO_POINT) {
                                mBloodVelocityPlotter.setTimeScalePoint(mFloatSvOnTouch2PointX, false, true);
                            }
                        }
                        if (needReDrawScale){
                            mBloodVelocityPlotter.drawFreqScaleByHolder(mBloodVelocityPlotter.mSurfaceHolderScale);
                            needReDrawScale = false;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!mBoolSvUltrasoundTouchAtUpper){
                            if (SystemConfig.mIntVTIModeIdx == SystemConfig.INT_VTI_MODE_2_TWO_POINT) {
                                mFloatSvOnTouch2PointX = event.getX();
                                mBloodVelocityPlotter.setTimeScalePoint(mFloatSvOnTouch2PointX, false, true);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mBoolSvUltrasoundTouchAtUpper) {
                            mFloatSvOnTouchEndX = event.getX();
                            mFloatSvOnTouchStartY = event.getY();
                            mFloatShiftX = mFloatSvOnTouchEndX - mFloatSvOnTouchStartX;
                            if (mFloatShiftX < -5) {
                                mFloatShiftX = -1 * mFloatShiftX;
                                if (SystemConfig.mEnumUltrasoundSubUIState == SystemConfig.ENUM_SUB_UI_STATE.RAW_DATA) {
                                /*rawDataUiManager.MoveNextOnTouchAction(mFloatShiftX);*/
                                } else if ((SystemConfig.mEnumUltrasoundSubUIState == SystemConfig.ENUM_SUB_UI_STATE.BLOOD_FLOW_VELOCITY)
                                        || (SystemConfig.mEnumUltrasoundSubUIState == SystemConfig.ENUM_SUB_UI_STATE.AUX_INFO)){
                                    MoveNextOnTouchAction(mFloatShiftX);
                                } else {

                                }
                                mFloatSvOnTouchStartX = mFloatSvOnTouchEndX;
                            } else if (mFloatShiftX > 5) {
                                if (SystemConfig.mEnumUltrasoundSubUIState == SystemConfig.ENUM_SUB_UI_STATE.RAW_DATA) {
                                /*rawDataUiManager.MoveBackOnTouchAction(mFloatShiftX);*/
                                } else if ((SystemConfig.mEnumUltrasoundSubUIState == SystemConfig.ENUM_SUB_UI_STATE.BLOOD_FLOW_VELOCITY)
                                        || (SystemConfig.mEnumUltrasoundSubUIState == SystemConfig.ENUM_SUB_UI_STATE.AUX_INFO)){
                                    MoveBackOnTouchAction(mFloatShiftX);
                                }
                                mFloatSvOnTouchStartX = mFloatSvOnTouchEndX;
                            }else {
                                iCurSubSeg = mBloodVelocityPlotter.getSubSegIdxFromSurfaceView(mFloatSvOnTouchStartX);
                                if (MainActivity.mBVSignalProcessorPart2Selected != null) {
                                    MainActivity.mBVSignalProcessorPart2Selected.toggleHRPeriodDiscardedState(iCurSubSeg);
                                    mBloodVelocityPlotter.drawBVSignalByChangeDiscarededState();
                                }
                            }
                            if(SystemConfig.mIntTimeScaleEnabled == SystemConfig.INT_TIME_SCALE_ENABLED_YES){
                                mBloodVelocityPlotter.setTimeScalePoint(mFloatTimeScalePosCur, true, true);
                                iSubSegIdx = mBloodVelocityPlotter.getTimeScaleSubSegmentIdx(mFloatTimeScalePosCur);
                                //setTimeScaleInfoCur(iSubSegIdx);
                            }
                        }else if(SystemConfig.mIntVTIModeIdx == SystemConfig.INT_VTI_MODE_2_TWO_POINT) {
                            mFloatSvOnTouchStartY = event.getY();
                            mFloatSvOnTouch2PointX = event.getX();
                            mBloodVelocityPlotter.setTimeScalePoint(mFloatSvOnTouch2PointX, false, true);
                            iCurSubSeg = mBloodVelocityPlotter.getSubSegIdxFromSurfaceView(mFloatSvOnTouch2PointX);
                            double doubleRatio = (double)mFloatSvOnTouchStartY / (double)mSurfaceViewUltrasound.getHeight();
                            if(doubleRatio > 0.5){
                                if (MainActivity.mBVSignalProcessorPart2Selected != null) {
                                    MainActivity.mBVSignalProcessorPart2Selected.toggleHRPeriodDiscardedState(iCurSubSeg);
                                    mBloodVelocityPlotter.drawBVSignalByChangeDiscarededState();
                                }
                            }else{
                                MainActivity.mBVSignalProcessorPart2Selected.changeVTIScopeBy2PointsSel(iCurSubSeg);
                                mBloodVelocityPlotter.drawBVSignalByChangeDiscarededState();
                            }
                        }
                        v.performClick();
                        break;
                    default:
                        break;
                }
            }catch(Exception ex1){
                //SystemConfig.mMyEventLogger.appendDebugStr("mSvUltrasoundOnTouch.Exception", "");
                ex1.printStackTrace();
            }
            return true;

        }

        protected void MoveActionAfterCorrect() {
            int iDrawStartIdx, iDrawSize;

            try {
                iDrawStartIdx = SystemConfig.mIntSVDrawStartBloodVelocity;
                iDrawSize = SystemConfig.mIntSVDrawSizeBloodVelocity;

                /*UiManager.mIntYMultiFactor = Integer.parseInt(SystemConfig.mFragment.mEditTextYMultiValue.getText().toString());*/
                if (SystemConfig.mEnumUltrasoundSubUIState == SystemConfig.ENUM_SUB_UI_STATE.BLOOD_FLOW_VELOCITY) {
                    mBloodVelocityPlotter.DrawBySubSegOffLine(iDrawStartIdx, iDrawSize);
                } else {
                    mBloodVelocityPlotter.DrawBySubSegOffLine(iDrawStartIdx, iDrawSize);
                    //mStrokeVolumePlotter.offlineDrawBySubSeg(iDrawStartIdx, iDrawSize);
                }

            } catch (Exception e) {
                //String strMsg = e.toString();
                //Log.d("ITRI", strMsg);
                e.printStackTrace();
            }
        }

        protected void MoveNextOnTouchAction(float fMoveX) {
            //int iDrawStartIdx, iDrawSize;
            float fSvXMaxSize, fSvSubSegMaxSize;
            int iMoveSubSegs;

            try {

                fSvXMaxSize = mSurfaceViewUltrasound.getWidth();
                //fSvSubSegMaxSize = (float) SystemConfig.mFragment.mBloodVelocitySignalProcessor.getCurSubSegSize();
                fSvSubSegMaxSize = (float) SystemConfig.mIntSVDrawSizeBloodVelocity;
                iMoveSubSegs = (int) ((fSvSubSegMaxSize * fMoveX) / fSvXMaxSize);

                correctMoveNextParamWithParam(iMoveSubSegs);
                //correctYMultiParam();
                MoveActionAfterCorrect();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        protected void MoveBackOnTouchAction(float fMoveX) {
            int iDrawStartIdx, iDrawSize;
            float fSvXMaxSize, fSvSubSegMaxSize;
            int iMoveSubSegs;

            try {
                fSvXMaxSize = mSurfaceViewUltrasound.getWidth();
                fSvSubSegMaxSize = (float) SystemConfig.mIntSVDrawSizeBloodVelocity;
                iMoveSubSegs = (int) ((fSvSubSegMaxSize * fMoveX) / fSvXMaxSize);

                correctMoveBackParamWithParam(iMoveSubSegs);
                //correctYMultiParam();
                MoveActionAfterCorrect();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        protected void correctMoveNextParamWithParam(int iMoveSubSeg) {
            int iDrawStartIdx=0, iDrawSize=SystemConfig.mIntSVDrawSizeBloodVelocity ;

            try {
                iDrawStartIdx = SystemConfig.mIntSVDrawStartBloodVelocity + iMoveSubSeg;
                if ((iDrawStartIdx + iDrawSize) > MainActivity.mBVSignalProcessorPart1.getCurSubSegSize()) {
                    iDrawStartIdx = MainActivity.mBVSignalProcessorPart1.getCurSubSegSize() - iDrawSize;
                }
            } catch (Exception ex1) {
                iDrawStartIdx = 0;
                iDrawSize = SystemConfig.mIntSVDrawSizeBloodVelocity;
            } finally {
                correctPointsParam(iDrawStartIdx, iDrawSize);
            }
        }
        protected void correctMoveBackParamWithParam(int iMoveSubSegs) {
            int iDrawStartIdx = 0, iDrawSize = SystemConfig.mIntSVDrawSizeBloodVelocity;

            try {
                iDrawStartIdx = SystemConfig.mIntSVDrawStartBloodVelocity - iMoveSubSegs;
                if (iDrawStartIdx < 0) {
                    iDrawStartIdx = 0;
                }
            } catch (Exception ex1) {
                iDrawStartIdx = 0;
                iDrawSize = SystemConfig.mIntSVDrawSizeBloodVelocity;
            } finally {
                correctPointsParam(iDrawStartIdx, iDrawSize);
            }
        }
    };

    protected void correctPointsParam(int iDrawStartIdx, int iDrawSize) {
        int iCurDataSize;

        iCurDataSize = MainActivity.mBVSignalProcessorPart1.getCurSubSegSize();

        if (iDrawStartIdx + iDrawSize > iCurDataSize) {
            if (iDrawStartIdx >= (iCurDataSize - 1)) {
                iDrawStartIdx = iCurDataSize - 1;
                iDrawSize = 1;
            } else {
                iDrawSize = iCurDataSize - iDrawStartIdx;
            }
        }

        SystemConfig.mIntSVDrawStartBloodVelocity = iDrawStartIdx;
        SystemConfig.mIntSVDrawSizeBloodVelocity = iDrawSize;
    }

    private boolean saveToDatabase(){
//        dataInfo mDataInfo;
        Date date;
        SimpleDateFormat simpleDateFormat;
        String strDate;

        try {
//            mDataInfo = MainActivity.mSignalProcessController.getResultDataAfterSignalProcess();

            currentResult.userId = UserManagerCommon.mUserInfoCur.userID;
            currentResult.fileName = MainActivity.mRawDataProcessor.mStrCurFileName;
            simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.TAIWAN);
            date = new Date();
            currentResult.createdDate = simpleDateFormat.format(date);

            MainActivity.mIwuSQLHelper.addDataInfoToDB(currentResult);
            return true;
        } catch (Exception ex1) {
            ex1.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugStr("Fragment.trySaveToDataBase.Exception", "");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(), "");
            return false;
        }
    }

    public void updateWithServiceDiscoveryFragment() {
        //  getActivity().unregisterReceiver(mGattConnectReceiver);
        SystemConfig.mEnumUltrasoundSubUIState = SystemConfig.ENUM_SUB_UI_STATE.SERVICE_DISCOVERY;
        FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
        ServiceDiscoveryFragment serviceDiscoveryFragment = new ServiceDiscoveryFragment();
        //fragmentManager.beginTransaction().remove(getFragmentManager().
        //        findFragmentByTag(Constants.PROFILE_SCANNING_FRAGMENT_TAG)).commit();
        fragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, serviceDiscoveryFragment,
                        //.replace(R.id.container, serviceDiscoveryFragment,
                        Constants.SERVICE_DISCOVERY_FRAGMENT_TAG)
                .commit();
    }

    private void showBloodVelocityForSingleVpk(dataInfo inputData){
        mVpkValueTextView.setText(String.format(
                        getResources().getString(R.string.singleVpkResult),inputData.Vpk,inputData.VTI));

    }

    private static void showMessageDialog(final String title, final Context ctx){
        AlertDialog alertIncorrectDialog = new AlertDialog.Builder(ctx).create();
        alertIncorrectDialog.setTitle(title);
        alertIncorrectDialog.setButton(Dialog.BUTTON_POSITIVE,
                ctx.getString(R.string.alert_message_exit_ok),
                (dialogInterface, i) -> {
                    // Do nothing.
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
