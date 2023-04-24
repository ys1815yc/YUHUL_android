package com.gis.heartio.SignalProcessSubsystem.SupportSubsystem;

import com.gis.heartio.SignalProcessSubsysII.utilities.wuDopplerInfo;
import com.gis.heartio.UIOperationControlSubsystem.MainActivity;

import java.util.Arrays;

/**
 * Created by Cavin on 2017/12/29.
 */
public class SystemConfig {

    //*************************************************
    //************** Debug Control   ******************
    //**************************************************
    public static final boolean mBoolDebugPlotterDrawLine = true;
    public static boolean mBoolDebugLogAllSeqNo = false;
    public static boolean mBoolDebugCharacteristic = false;
    public static boolean mBoolDebugGainControl = false;
    public static boolean mBoolDebugPowerLevelChangeHistory = false;

    public static boolean mTestMode = true;
    public static boolean isHeartIO2 = false;
    public static boolean saveFile = true;
    public static boolean vtiSegPlot = true;
    public static boolean darkMode = false;
    public static boolean isYuhul = true;

    public static double rxAngle = 55.0;

    //*************************************************
    //************** For Dr. Wu Doppler ****************
    //**************************************************
    public static double[][] mDopplerVFOutput;
    public static wuDopplerInfo mDopplerInfo;

    public static final int vtiLengthUS = 30;
    public static final int vtiLengthECG = 4*vtiLengthUS;


    //******************************************************************
    //************** Try/Start Button Control Flag   *********
    //******************************************************************
    public enum ENUM_TRY_STATE {STATE_TRY, STATE_TRY_STOP}
    public static ENUM_TRY_STATE mEnumTryState;
    public enum ENUM_START_STATE {STATE_START, STATE_STOP}
    public static ENUM_START_STATE mEnumStartState;



    //*******************************************************************
    //************** VPK Mode Control Flag ******************************
    //*******************************************************************
    //--------- Single VPK Parameter -------------------
    public static int INT_SINGLE_VPK_ENABLED_NO = 0;
    public static int INT_SINGLE_VPK_ENABLED_YES = 1;
    public static String STR_SINGLE_VPK_ENABLED_NO = "Disabled";
    public static String STR_SINGLE_VPK_ENABLED_YES = "Enabled";
    public static int mIntSingleVpkEnabled = INT_SINGLE_VPK_ENABLED_NO;
    public static int mIntSingleVpkAvg;
    public static double mDoubleSingleVpkAvg;
    //----------- Vpk Algorithm  -----------------------
    public static int INT_VPK_ALGORITHM_0_SNSI_GM = 0;
    public static int INT_VPK_ALGORITHM_1_WU_NEW = 1;
    public static int INT_VPK_ALGORITHM_2_SNR_AMP = 2;
    public static int INT_VPK_ALGORITHM_3_SNR_POWER = 3;
    public static String STR_VPK_ALGORITHM_0_SNSI_GM = "SNSI_GM";
    public static String STR_VPK_ALGORITHM_1_WU_NEW = "WU_NEW";
    public static String STR_VPK_ALGORITHM_2_SNR_AMP = "SNR_AMP";
    public static String STR_VPK_ALGORITHM_3_SNR_POWER = "SNR_POWER";
    public static int mIntVpkAlgorithm = INT_VPK_ALGORITHM_0_SNSI_GM;
    //********************************************************
    //************** Filter Enable parameter *****************
    //********************************************************
    public static int INT_FILTER_ENABLED_NO = 0;
    public static int INT_FILTER_ENABLED_YES = 1;
    public static String STR_FILTER_ENABLED_NO = "Disabled";
    public static String STR_FILTER_ENABLED_YES = "Enabled";
    public static int mIntFilterDataEnabled = INT_FILTER_ENABLED_YES;

    //********************************************************
    //************** Sound enable parameter *****************
    //********************************************************
    public static final int INT_SOUND_ENABLED_NO = 0;
    public static final int INT_SOUND_ENABLED_YES = 1;
    public static int mIntSoundEnabled = INT_SOUND_ENABLED_YES;

    //**********************************************************
    //*************  Time Scale ***************************
    //**********************************************************
    public static int INT_TIME_SCALE_ENABLED_NO = 0;
    public static int INT_TIME_SCALE_ENABLED_YES = 1;
    public static String STR_TIME_SCALE_ENABLED_NO = "Disabled";
    public static String STR_TIME_SCALE_ENABLED_YES = "Enabled";
    public static int mIntTimeScaleEnabled = INT_TIME_SCALE_ENABLED_NO;
    //-----The date for file created in mini second--------------------
    public static long mLongOpenWavFileDateMiniSecs;

    //**********************************************************
    //*************  Calibration ***************************
    //**********************************************************
    public static int INT_CALIBRATION_TYPE_0_NO_CALI = 0;
    public static int INT_CALIBRATION_TYPE_1_TABLE = 1;
    public static String STR_CALIBRATION_TYPE_0_NO_CALI = "No Calibration";
    public static String STR_CALIBRATION_TYPE_1_TABLE = "Cali.By.Table";
    public static int mIntCalibrationAdjustType = INT_CALIBRATION_TYPE_0_NO_CALI;

    //**********************************************************
    //*************  Show VTI Enabled  ***************************
    //**********************************************************
    public static int INT_DRAW_VTI_ENABLED_YES = 0;
    public static int INT_DRAW_VTI_ENABLED_NO = 1;
    public static String STR_DRAW_VTI_ENABLED_0_YES = "Yes";
    public static String STR_DRAW_VTI_ENABLED_1_NO = "No";
    public static int mIntDrawVTIEnabled = INT_DRAW_VTI_ENABLED_YES;
//    public static int mIntDrawVTIEnabled = INT_DRAW_VTI_ENABLED_NO;
    //*********************************************************************
    //*************  Special HR, 3 wave in one HR are connected  **********
    //*********************************************************************
    public static int INT_HR_SPECIAL_YES = 0;
    public static int INT_HR_SPECIAL_NO = 1;
    public static String STR_HR_SPECIAL_0_YES = "Yes";
    public static String STR_HR_SPECIAL_1_NO = "No";
    public static int mIntHRSpecial = INT_HR_SPECIAL_YES;
    public static double DOUBLE_PHANTOM_DIFF_SUM_BASE_RATIO_NORMAL = 0.8;
    public static double DOUBLE_PHANTOM_DIFF_SUM_BASE_RATIO_HR_SPECIAL = 0.4;

    //**********************************************************
    //*************  Gain Control ***************************
    //**********************************************************
    public static int INT_GAIN_CONTROL_ENABLED_NO = 0;
    public static int INT_GAIN_CONTROL_ENABLED_YES = 1;
    public static String STR_GAIN_CONTROL_ENABLED_NO = "Disabled";
    public static String STR_GAIN_CONTROL_ENABLED_YES = "Enabled";
    public static int mIntGainControlEnabled = INT_GAIN_CONTROL_ENABLED_YES;
    //public static int mIntGainControlEnabled = INT_GAIN_CONTROL_ENABLED_NO;
    //--------------------------------------------------------------------------
    public static double DOUBLE_GAIN_CONTROL_HIGH_THRESHOLD_RATIO = (double)9 / (double)10;
    public static double DOUBLE_GAIN_CONTROL_LOW_THRESHOLD_RATIO = (double)8 / (double)10;
    public static double DOUBLE_GAIN_CONTROL_TARGET_VALUE_RATIO = (double)7 / (double)10;
    public static int INT_GAIN_CONTROL_TOP_VALUE = 3600;
    public static int INT_GAIN_CONTROL_LEVEL_CNT = 8;
    public static int INT_GAIN_CHANGE_INTERVAL_MSEC = 2000;     // for adjust interval
    public static int mIntGainLevelCommandSetting =  INT_GAIN_CONTROL_LEVEL_CNT;
    public static int mIntGainControlHighThreshold,mIntGainControlLowThreshold, mIntGainControlTargetValue;
    public static int[] mIntGainLevelMapVer1;
    public static int mIntGainLevel;    // mIntGainCommand;
    public static boolean mBoolGainLevelLog = false;

    //**********************************************************
    //*************  Power Consume Test ************************
    //**********************************************************
    public enum ENUM_POWER_CONSUME_TEST_STATE {TEST_STATE_START, TEST_STATE_STOP}
    public static ENUM_POWER_CONSUME_TEST_STATE mEnumPowerConsumeTestState = ENUM_POWER_CONSUME_TEST_STATE.TEST_STATE_STOP;

    //**********************************************************
    //*************  Power Control   ***************************
    //**********************************************************
    public static int mIntPowerLevel;
    public static boolean mBoolGetFirstPowerLevelEnable = true;


    //******************************************************************
    //*************  BloodVelocity Surface View Display   **************
    //******************************************************************
    public static int INT_SURFACE_VIEWS_ON_LINE_MAX_SIZE = 32;
    public static int INT_SURFACE_VIEWS_ON_LINE_USE_SIZE = 27;
    public static final int INT_SVIEW_ONLINE_DRAW_SUBSEG_SIZE = 12;   // 0.1*8000/64=12
    public static int mIntBVSvMaxScaleIdxForDisplay = 25;
    public static double mDoubleBVSvMaxScaleForReal;
    public static double mDoubleBVSvMaxScaleForDisplay;

    //***************************************************
    //*************  UI Operation State    **************
    //***************************************************
    public enum ENUM_UI_STATE{ULTRASOUND_UI_STATE_NONE,ULTRASOUND_UI_STATE_OFFLINE,ULTRASOUND_UI_STATE_ONLINE}
    public static ENUM_UI_STATE mEnumUltrasoundUIState=ENUM_UI_STATE.ULTRASOUND_UI_STATE_NONE;

    public enum ENUM_SUB_UI_STATE {APP_START, ABOUT, CAREGIVER_MANAGER, CAREGIVER_ADD_EDIT, USER_MANAGER, USER_ADD_EDIT, DATA_MANAGER, DATA_MANAGER_TREND,
                                    LONG_START_SETTING, SYSTEM_SETTING, SCAN_DEVICE, SERVICE_DISCOVERY, PROFILE_CONTROL, BLOOD_FLOW_VELOCITY, AUX_INFO, RAW_DATA}
    public static ENUM_SUB_UI_STATE mEnumUltrasoundSubUIState = ENUM_SUB_UI_STATE.APP_START;

    //***************************************************
    //*************  Device Type           **************
    //***************************************************
    public enum ENUM_DEVICE_TYPE {ITRI_8K_LE_ONLINE, ITRI_8K_BE_ONLINE, ITRI_8K_OFFLINE , ITRI_44K, USCOM_44K ,USCOM_8K}
    public static ENUM_DEVICE_TYPE mEnumDeviceType ;

    //**********************************************************************************
    // ***************** Noise & Signal Learning   *************************************
    //**********************************************************************************
    public static final double DOUBLE_NOISE_SIGNAL_LEARN_START_SEC = 0.0;//Leslie modified 1.0;   // for DC offset
    public static final double DOUBLE_NOISE_SIGNAL_LEARN_END_SEC = 0.0;//Leslie modified 3.0;
    public static final double DOUBLE_SIGNAL_SORT_RATIO = 0.95;
    public static final double DOUBLE_NOISE_SORT_RATIO = 0.05;
    public static int mIntStartIdxNoiseLearn, mIntEndIdxNoiseLearn;
    public static double mDoubleNoiseBaseLearned, mDoubleSignalBaseLearned, mDoubleSNRLearned;

    public static double mDoubleNoiseRangeWu, mDoubleNoiseStrengthWu, mDoubleSignalStrengthWu, mDoubleSNRLearnedWu, mDoubleSignalStrengthMaxWu;
    public static double mDoubleSignalMaxForLearnWuNew, mDoubleSignalMaxForAllWuNew;
    public static double mDoubleSignalMinForLearnWuNew, mDoubleSignalMinForAllWuNew;
    public static boolean mBoolSignalMaxForLearnWuNewOK;
    public static double mDoubleSignalMaxForLearnAfterNormWuNew, mDoubleSignalMinForLearnAfterNormWuNew;
    public static boolean mBoolDCOffsetEnable = false;

    //**********************************************************************************
    // ***********  (Frequency Envelop) MaxIdx Estimate ; 一個時間點最大有效之頻移指標 <= 128 ****
    //**********************************************************************************
    //--------------MaxIdx Reference Level -------------------------------------------------
    public static final double DOUBLE_MAXIDX_REFERENCE_FROM_SNR_BY_NOISE_MULTI = 10.0;
    public static final double DOUBLE_MAXIDX_REFERENCE_FROM_SNR_BY_DEVIDED_RATIO = 3.0;
    //--------------BreakLow Parameter -----------------------------------------------------
    public static final int INT_MAXIDX_BREAK_LOW_WINDOWS_SIZE_DEFAULT_ITRI = 12;
    public static final int INT_MAXIDX_BREAK_LOW_WINDOWS_SIZE_DEFAULT_8K_USCOM = 6;
    public static boolean mBoolMaxIdxBreakLow;
    //--------------MaxIdx Window-----------------------------------------------------
    public static final int INT_MAX_IDX_WINDOW_SIZE_ITRI = 10;
    public static final int INT_MAX_IDX_WINDOW_SIZE_USCOM = 10;
    public static int mIntMaxIdxWindowSizeBySNR;
    //-------------- MaxIdx Parameters ------------------------------------
    public static int mIntVtiMaxIdxSmoothLeftSize, mIntVtiMaxIdxSmoothRightSize;
    //jaufa, N1, -, public static boolean mBoolMaxIdxMovingAverageForVTI = true;
    //jaufa, N1, +,
    public static boolean mBoolMaxIdxMovingAverageForVTI = false;
    //**********************************************************************************
    // ***************** for BV Parameter
    //**********************************************************************************
    //--------- for Reselect Type -----------------------
    public enum ENUM_BV_RESULT_RESELECT_TYPE {NONE, VPK_RESELECT, VTI_RESELECT}
    public static ENUM_BV_RESULT_RESELECT_TYPE mEnumBVResultReselectType = ENUM_BV_RESULT_RESELECT_TYPE.VTI_RESELECT;
    public static int INT_HR_USED_MAX_CNT_BY_RESELECTED_VPK_DEFAULT = 6;   // 自動選取時，最多5個最高Vpk之HR
    public static int INT_HR_USED_MAX_CNT_BY_RESELECTED_VTI_DEFAULT = 3;   // 自動選取時，最多2個最高VTI之HR

    //--------- for HR tolerance tolerance Limitation default -----------------------
    public static double DOUBLE_HR_USED_TORLANCE_RATIO_MAX = 0.15;

    //-------------------------------------------------------------------------------
    // ------  Group Parameter : 依心跳範圍分為多個Group    ------------------------
    //----------------------------------------------------------------------------

    //------------ Group Count -------------------------------------
    public static int INT_HR_GROUP_CNT = 1;

    //* Jaufa, 180805, For One Group Only
    //public static int INT_HR_GROUP_CNT = 1;



    //------------ 各個群組負責的心跳範圍 --------------------------
    public static double[] DOUBLE_HR_PERIOD_MSEC_MIN;       // 心跳週期最小時間 millisecond
    public static double[] DOUBLE_HR_PERIOD_MSEC_MAX;      // 心跳週期最大時間 millisecond
    public static double[] mDoubleArrayHRAverageMax;    // 心跳數最大值 次/分
    public static double[] mDoubleArrayHRAverageMin;    // 心跳數最小值 次/分
    //------------ ITRI Group Parameter -------------------------------------
    public static double[] DOUBLE_HR_PEAK_INTEGRAL_WINDOW_MSEC_RATIO_MODE_PEAK_ITRI;
    public static double[] DOUBLE_HR_PEAK_FEATURE_WINDOW_MSEC_RATIO_MODE_PEAK_ITRI;
    public static double[] DOUBLE_HR_BOTTOM_INTEGRAL_WINDOW_MSEC_RATIO_MODE_PEAK_ITRI;
    public static double[] DOUBLE_HR_BOTTOM_FEATURE_WINDOW_MSEC_RATIO_MODE_PEAK_ITRI;
    public static double[] DOUBLE_VPK_PEAK_S1_INTEGRAL_WINDOW_MSEC_RATIO_ITRI;
    public static double[] DOUBLE_VPK_PEAK_S1_FEATURE_WINDOW_MSEC_RATIO_ITRI;
    public static double[] DOUBLE_PHANTOM_PEAK_INTEGRAL_WINDOW_MSEC_RATIO_ITRI;
    public static double[] DOUBLE_PHANTOM_PEAK_FEATURE_WINDOW_MSEC_RATIO_ITRI;
    public static double[] DOUBLE_VTI_START_INTEGRAL_WINDOW_MSEC_RATIO_ITRI;
    public static double[] DOUBLE_VTI_START_FEATURE_WINDOW_MSEC_RATIO_ITRI;
    public static double[] DOUBLE_VTI_END_INTEGRAL_WINDOW_MSEC_RATIO_ITRI;
    public static double[] DOUBLE_VTI_END_FEATURE_WINDOW_MSEC_RATIO_ITRI;
    //public static double[] DOUBLE_VTI_START_END_INTEGRAL_WINDOW_MSEC_RATIO_ITRI;
    //public static double[] DOUBLE_VTI_START_END_FEATURE_WINDOW_MSEC_RATIO_ITRI;
    //----------USCOM Group Parameter --------------------------------------------
    public static double[] DOUBLE_HR_PEAK_INTEGRAL_WINDOW_MSEC_RATIO_USCOM;
    public static double[] DOUBLE_HR_PEAK_FEATURE_WINDOW_MSEC_RATIO_USCOM;
    //public static double[] DOUBLE_HR_PEAK_HALF_INTEGRAL_WINDOW_MSEC_RATIO_USCOM;
    //public static double[] DOUBLE_HR_PEAK_HALF_FEATURE_WINDOW_MSEC_RATIO_USCOM;
    public static double[] DOUBLE_VPK_PEAK_S1_INTEGRAL_WINDOW_MSEC_RATIO_USCOM;
    public static double[] DOUBLE_VPK_PEAK_S2_INTEGRAL_WINDOW_MSEC_RATIO_USCOM;
    public static double[] DOUBLE_VPK_PEAK_S1_FEATURE_WINDOW_MSEC_RATIO_USCOM;
    public static double[] DOUBLE_VPK_PEAK_S2_FEATURE_WINDOW_MSEC_RATIO_USCOM;
    public static double[] DOUBLE_PHANTOM_PEAK_INTEGRAL_WINDOW_MSEC_RATIO_USCOM;
    public static double[] DOUBLE_PHANTOM_PEAK_FEATURE_WINDOW_MSEC_RATIO_USCOM;
    public static double[] DOUBLE_HR_BOTTOM_INTEGRAL_WINDOW_MSEC_RATIO_USCOM;
    public static double[] DOUBLE_HR_BOTTOM_FEATURE_WINDOW_MSEC_RATIO_USCOM;
    public static double[] DOUBLE_VTI_START_INTEGRAL_WINDOW_MSEC_RATIO_USCOM;
    public static double[] DOUBLE_VTI_START_FEATURE_WINDOW_MSEC_RATIO_USCOM;
    public static double[] DOUBLE_VTI_END_INTEGRAL_WINDOW_MSEC_RATIO_USCOM;
    public static double[] DOUBLE_VTI_END_FEATURE_WINDOW_MSEC_RATIO_USCOM;
    //----------------------------------------------------------------------------
    // for VTI smooth Parameter
    //----------------------------------------------------------------------------
    public static final int INT_VTI_MAX_IDX_DIFF_SMOOTH_LEFT_SIZE_ITRI = 12;
    public static final int INT_VTI_MAX_IDX_DIFF_SMOOTH_RIGHT_SIZE_ITRI = 12;
    public static final int INT_VTI_MAX_IDX_DIFF_SMOOTH_LEFT_SIZE_USCOM = 6;
    public static final int INT_VTI_MAX_IDX_DIFF_SMOOTH_RIGHT_SIZE_USCOM = 6;
    //--------- VTI Algorithm Mode ------------------------------------------------
    public static final String STR_VTI_MODE_0_STRONGEST = "Strong Peak";
    public static final String STR_VTI_MODE_1_FULL = "Full Select";
    public static final String STR_VTI_MODE_2_TWO_POINT = "Two Point Select";
    public static final int INT_VTI_MODE_0_STRONGEST =0;
    public static final int INT_VTI_MODE_1_FULL =1;
    public static final int INT_VTI_MODE_2_TWO_POINT =2;
    public static int mIntVTIModeIdx = INT_VTI_MODE_0_STRONGEST;

    //----------------------------------------------------------------------------
    // for BV/HR Result Record
    //----------------------------------------------------------------------------
    public static final int INT_HR_RESULT_IDX1_CNT= 60; // Cavin change from 50 to 60
    public static final int INT_HR_RESULT_IDX2_CNT= 16;
    public static final int INT_HR_RESULT_HR_START_IDX = 0;
    public static final int INT_HR_RESULT_HR_END_IDX = 1;
    public static final int INT_HR_RESULT_INTERVAL_IDX = 2;
    //public static final int INT_HR_RESULT_VPK_REF_IDX = 3;
    public static final int INT_HR_RESULT_VPK_IDX =4;
    public static final int INT_HR_RESULT_VPK_VALUE_IDX =5;
    public static final int INT_HR_RESULT_VTI_START_IDX =6;
    public static final int INT_HR_RESULT_VTI_END_IDX =7;
    public static final int INT_HR_RESULT_HR_DISCARDED_IDX = 8;
    public static final int INT_HR_RESULT_IS_PHANTOM_IDX = 9;
    public static final int INT_HR_RESULT_VTI_INTERVAL_IDX = 10;
    public static final int INT_HR_RESULT_VTI_AREA_IDX = 11;
    public static final int INT_HR_RESULT_VTI_VALUE_IDX = 12;
    public static final int INT_HR_RESULT_HR_AREA_IDX = 13;
    public static final int INT_HR_RESULT_PHANTOM_PEAK_CNT_IDX = 14;
    public static final int INT_HR_RESULT_VTI_START_END_PRE_SEL_IDX = 15;
    public static final int INT_HR_RESULT_DISCARDED_STATE_NO = 0;
    public static final int INT_HR_RESULT_DISCARDED_STATE_YES = 1;
    public static boolean mBoolProcessorPart2Selected;
    public static int mIntPart2TestIdx = -1; ///     // -1 : All

    //----------------------------------------------------------------------------
    // jaufa, N1, 181002, for VTI/HR/SV/CO/ VPK Result Record
    //----------------------------------------------------------------------------
    public static final int DOUBLE_HR_BLOOD_RESULT_IDX1_CNT= 2;
    public static final int DOUBLE_HR_BLOOD_RESULT_IDX2_CNT= 7;

    public static final int DOUBLE_HR_BLOOD_RESULT_HR_IDX = 0;
    public static final int DOUBLE_HR_BLOOD_RESULT_VPK_IDX = 1;
    public static final int DOUBLE_HR_BLOOD_RESULT_VPK_NoCali_IDX = 2;
    public static final int DOUBLE_HR_BLOOD_RESULT_VTI_IDX = 3;
    public static final int DOUBLE_HR_BLOOD_RESULT_SV_IDX = 4;
    public static final int DOUBLE_HR_BLOOD_RESULT_CO_IDX = 5;

    public static final int DOUBLE_HR_BLOOD_RESULT_ERR_IDX = 6;

    public static final int DOUBLE_HR_BLOOD_RESULT_HRErr_STATE = 0x01;
    public static final int DOUBLE_HR_BLOOD_RESULT_VPKErr_STATE = 0x02;
    public static final int DOUBLE_HR_BLOOD_RESULT_VTIErr_STATE = 0x04;
    public static final int DOUBLE_HR_BLOOD_RESULT_SVErr_STATE = 0x08;
    public static final int DOUBLE_HR_BLOOD_RESULT_COErr_STATE = 0x10;
    public static final int DOUBLE_HR_BLOOD_RESULT_SNRErr_STATE = 0x20;

    public static final int DOUBLE_HR_BLOOD_RESULT_ORI_STATE = 0;
    public static final int DOUBLE_HR_BLOOD_RESULT_SEL_STATE = 1;



    //**********************************************************************************
    //**************     for Plotter Mode    ****************************
    //**********************************************************************************
    //--------- BV SurfaceView DrawType ------------------------------------------------
    public enum BLOOD_VELOCITY_PLOTTER_DRAW_TYPE_ENUM {TYPE_LINE, TYPE_POINT}
    public static final  BLOOD_VELOCITY_PLOTTER_DRAW_TYPE_ENUM mBloodVelocityPlotterDrawType = BLOOD_VELOCITY_PLOTTER_DRAW_TYPE_ENUM.TYPE_LINE;
    //--------- BV Surface Plotter Type/Mode -------------------------------------------
    public static final String STR_PLOTTER_MODE_0_BLOOD_VELOCITY = "Blood Velocity";
    public static final String STR_PLOTTER_MODE_1_HR_PEAK = "HR Peak";
    public static final String STR_PLOTTER_MODE_2_HR_BOTTOM = "HR Bottom";
    public static final String STR_PLOTTER_MODE_3_PHANTOM_PEAK = "Phantom Peak";
    public static final String STR_PLOTTER_MODE_4_VTI_START = "VTI Start";
    public static final String STR_PLOTTER_MODE_5_VTI_END = "VTI End";
    public static final int INT_PLOTTER_MODE_IDX_0_BLOOD_VELOCITY =0;
    public static final int INT_PLOTTER_MODE_IDX_1_HR_PEAK =1;
    public static final int INT_PLOTTER_MODE_IDX_2_HR_BOTTOM=2;
    public static final int INT_PLOTTER_MODE_IDX_3_PHANTOM_PEAK =3;
    public static final int INT_PLOTTER_MODE_IDX_4_VTI_START=4;
    public static final int INT_PLOTTER_MODE_IDX_5_VTI_END=5;
    public static int mIntPlotterModeIdx = INT_PLOTTER_MODE_IDX_0_BLOOD_VELOCITY;
    //---------- SurfaceView parameter -------------------------------------
    public static final int  INT_SV_DRAW_SIZE_DEFAULT_RAW_DATA= 10240;
    public static final int  INT_SV_DRAW_SIZE_DEFAULT_BLOOD_VELOCITY = 384;// 3 sec //512;  // 4sec
    public static int mIntSVDrawSizeBloodVelocity = INT_SV_DRAW_SIZE_DEFAULT_BLOOD_VELOCITY;
    public static int mIntSVDrawSizeRawData = INT_SV_DRAW_SIZE_DEFAULT_RAW_DATA;
    public static int mIntSVDrawStartBloodVelocity, mIntSVDrawStartRawData;
    //---------- SurfaceView GrayLevel Ratio for Huang/SNR Mode ----------------------------
    public static double DOUBLE_SV_GRAYLEVEL_DRAW_STEP_RATIO_HUANG_SNR_AMP = 0.965;
    public static double DOUBLE_SV_GRAYLEVEL_DRAW_STEP_RATIO_GM = 0.95;
    public static double mDoubleSViewGrayLevelStepRatio;
    //--------- BV WU_NEW DrawType ------------------------------------------------
    public enum ENUM_PLOTTER_GRAY_MODE_FOR_WU_NEW {WU_NEW_DIFF_MAX_NOISE, WU_NEW_RATIO_MAX_NOISE, WU_NEW_DIFF_MAX_MIN, WU_NEW_RATIO_MAX_MIN}
    public static ENUM_PLOTTER_GRAY_MODE_FOR_WU_NEW mEnumPlotterGrayModeForWuNew = ENUM_PLOTTER_GRAY_MODE_FOR_WU_NEW.WU_NEW_RATIO_MAX_MIN;
    public static double mDoubleDivideParamForPlotterGrayMaxWuNew = 250.0;

    //**********************************************************************************
    // BLE Communication Packet content
    //**********************************************************************************
    public static final int INT_ULTRASOUND_DATA_1DATA_BYTES_16PCM=2 ;
    public static final int INT_ULTRASOUND_SAMPLING_RATE_8K = 8000 ;
    public static final int INT_ULTRASOUND_SAMPLING_RATE_44K = 44000 ;
    public static final int INT_ULTRASOUND_START_ONLINE_SEC = 14 ;
    public static final int INT_ULTRASOUND_DATA_MAX_SIS_ITRI_8K = INT_ULTRASOUND_SAMPLING_RATE_8K
                                * INT_ULTRASOUND_DATA_1DATA_BYTES_16PCM * INT_ULTRASOUND_START_ONLINE_SEC;
    public static final int INT_ULTRASOUND_DATA_MAX_SIS = INT_ULTRASOUND_SAMPLING_RATE_44K
                                * INT_ULTRASOUND_DATA_1DATA_BYTES_16PCM * 10;     // for both online and offline

    // For ECG
    public static final int INT_ECG_SAMPLING_RATE = 500;
    //public static final int INT_ECG_DATA_MAX_SIZE = INT_ECG_SAMPLING_RATE * INT_ULTRASOUND_START_ONLINE_SEC;
    public static final int INT_ECG_DATA_MAX_SIZE = INT_ECG_SAMPLING_RATE * 20;
    public static final int INT_HEARTIO2_1DATA_BYTES = 40;
    public static final int INT_HEARTIO2_DATA_COUNT = 6;
    public static final int INT_HEARTIO2_1PACKET_PAYLOAD_BYTES = INT_HEARTIO2_1DATA_BYTES * INT_HEARTIO2_DATA_COUNT;//240;//120;

    public static final int INT_ULTRASOUND_1PACKET_PAYLOAD_BYTES_ITRI=488;  // 486+1+1
    public static final int INT_ULTRASOUND_DATA_GAIN_BYTES_ITRI=1;
    public static final int INT_ULTRASOUND_DATA_SEQ_BYTES_ITRI=1;
    public static int mIntPacketDataByteSize = INT_ULTRASOUND_1PACKET_PAYLOAD_BYTES_ITRI
                                            - INT_ULTRASOUND_DATA_GAIN_BYTES_ITRI
                                            -INT_ULTRASOUND_DATA_SEQ_BYTES_ITRI;
    public static final int INT_HEARTIO2_US_DATA_SIZE = 16;
    public static final int mIntPacketDataByteSizeHeartIO2 = INT_HEARTIO2_US_DATA_SIZE * INT_HEARTIO2_DATA_COUNT;//48;
    public static int mInt1DataBytes = INT_ULTRASOUND_DATA_1DATA_BYTES_16PCM ;
    public static int mIntUltrasoundSamplerate ; // Used in offline mode

    //**********************************************************************************
    // for Doppler & ShortTimeFFT parameter
    //**********************************************************************************
    //-----------  for ultrasound tx parameter --------------------------
    public static final double DOUBLE_ULTRASOUND_SPEED_FOR_BODY_METER_PERSEC = 1540;
    public static final double DOUBLE_ULTRASOUND_SENSOR_WAVE_FREQ_USCOM = 2200000 ; //2.2 Mega
    public static double DOUBLE_DEVICE_FREQ_0_1p0M = 1000000;
    public static double DOUBLE_DEVICE_FREQ_1_1p5M = 1500000;
    public static double DOUBLE_DEVICE_FREQ_2_2p0M = 2000000;
    public static double DOUBLE_DEVICE_FREQ_3_2p5M = 2500000;
    public static double[] mDoubleDeviceFreqMapItri;
    public static int mIntDeviceFreqIdxItri = 3;
    public static double mDoubleSensorWaveFreq;
    //------ shortTimeFFt window & shift size --------------------------------------------
    public static final int INT_BLOOD_VELOCITY_STFT_WINDOW_SIZE_512 = 512 ;
    public static final int INT_BLOOD_VELOCITY_STFT_WINDOW_SIZE_256 = 256 ;
    public static final int INT_BLOOD_VELOCITY_STFT_WINDOW_SHIFT_SIZE_64 = 64 ;
    public static final int INT_BLOOD_VELOCITY_STFT_WINDOW_SHIFT_SIZE_256 = 256 ;
    public static int mIntSTFTWindowSize;
    public static int mIntSTFTWindowShiftSize;
    public static double mDoubleSubSegTimesliceMsec;
    public static int mIntFreqIdxsMaxSize;
    //------ shortTimeFFt window type --------------------------------------------
    public static final String STFT_WINDOW_TYPE_NONE = "";
    public static final String STFT_WINDOW_TYPE_HANNING = "Hanning";
    public static String mStrSTFTWindowType;

    //**********************************************************************************
    // for Raw Data parameter
    //**********************************************************************************
    public static int mIntUltrasoundSamplesMaxSize;
    public static int mIntUltrasoundSamplesMaxSizeForRun;
    public static int mIntRawPacketCntsOnLine;
    public static int mIntRawPacketCntsOnLineForRun;


    //*************************************************
    //**************    System  Control *************
    //**************************************************
    //---------- SystemSetting Function   -----------------------------------------
    public static int INT_SYSTEM_SETTING_FUNCTION_0_NORMAL = 0;
    public static int INT_SYSTEM_SETTING_FUNCTION_1_SAFETY_TEST = 1;
    public static int INT_SYSTEM_SETTING_FUNCTION_2_POWER_CONSUME_TEST = 2;
    public static String STR_SYSTEM_SETTING_FUNCTION_0_NORMAL = "Normal Mode";
    public static String STR_SYSTEM_SETTING_FUNCTION_1_SAFETY_TEST = "Safety Test";
    public static String STR_SYSTEM_SETTING_FUNCTION_2_POWER_CONSUME_TEST = "Power Consume Test";
    public static int mIntSystemSettingFunction = INT_SYSTEM_SETTING_FUNCTION_0_NORMAL;
    //---------- Display parameter   -----------------------------------------
    public static boolean mBoolEngineerMode = true;
    //---------- Buffer MaxSize   -----------------------------------------
    public static int mIntSystemMaxSubSegSize, mIntSystemMaxMaxIdxWindowSize;
    public static final int mIntSystemMaxTotalFreqSeqsCnt = INT_BLOOD_VELOCITY_STFT_WINDOW_SIZE_512 / 2 + 1;

    public static final int mIntEcgSegCnt = 4;  // 500 / 125

    /* 聲音AI參數 2022/12/14 by Doris */
    public static int continuePA = 0;
    public static int continueNotPA = 0;
    public static short voiceIndex = 0;
    public static String[] voiceCategory = new String[]{"BG", "BG", "BG", "BG", "BG", "BG", "BG", "BG", "BG", "BG"};

    public static void initItriDeviceCommon(){
        //
        // common part for online and offline
        //

        //try{

        mInt1DataBytes = INT_ULTRASOUND_DATA_1DATA_BYTES_16PCM;

        mDoubleSensorWaveFreq = mDoubleDeviceFreqMapItri[mIntDeviceFreqIdxItri];

        mIntVtiMaxIdxSmoothLeftSize = INT_VTI_MAX_IDX_DIFF_SMOOTH_LEFT_SIZE_ITRI;
        mIntVtiMaxIdxSmoothRightSize = INT_VTI_MAX_IDX_DIFF_SMOOTH_RIGHT_SIZE_ITRI;

        //}catch(Exception ex1) {
        //SystemConfig.mMyEventLogger.appendDebugStr("initItriDeviceCommon.Exception", "");
        //}
    }

    public static void initItriDevice8KCommon(){

        //try {

            initItriDeviceCommon();

            mIntSTFTWindowSize = INT_BLOOD_VELOCITY_STFT_WINDOW_SIZE_256;
            mIntSTFTWindowShiftSize = INT_BLOOD_VELOCITY_STFT_WINDOW_SHIFT_SIZE_64;
            mIntFreqIdxsMaxSize = mIntSTFTWindowSize / 2 + 1;

            mIntUltrasoundSamplerate = INT_ULTRASOUND_SAMPLING_RATE_8K; // Used in offline mode

            mDoubleSubSegTimesliceMsec = (((double) mIntSTFTWindowShiftSize) * (double) 1000) / (double) mIntUltrasoundSamplerate;

            mIntUltrasoundSamplesMaxSize = INT_ULTRASOUND_DATA_MAX_SIS_ITRI_8K / mInt1DataBytes;

            //if(mEnumUltrasoundUIState == ENUM_UI_STATE.ULTRASOUND_UI_STATE_ONLINE) {
            //    setVelocityDrawSizeToItri8KOnLine();
            //}else{
            //    setVelocityDrawSizeToItri8KOffLine();
            //}
            //mIntSVBloodVelocityDrawSize = 20;

            //UltrasoundAudioPlayer.openPlayAudio(AUDIO_SAMPLING_RATE_8K, AudioFormat.ENCODING_PCM_8BIT);

            //mIntMulHRMinLowWindowMilliSec = INT_HR_MINLOW_WINDOW_MILLISEC_ITRI_8K;

            mIntMaxIdxWindowSizeBySNR = INT_MAX_IDX_WINDOW_SIZE_ITRI;

        //}catch(Exception ex1){
        //SystemConfig.mMyEventLogger.appendDebugStr("initItriDevice8KCommon.Exception","");
        //}
    }


    public static void initItriDevice8KOnlineLE(){

        //try{
        initItriDevice8KCommon();
        mEnumDeviceType = ENUM_DEVICE_TYPE.ITRI_8K_LE_ONLINE;

        //}catch(Exception ex1) {
        //SystemConfig.mMyEventLogger.appendDebugStr("initItriDevice8KOnline.Exception", "");
        //}
    }

    public static void initItriDevice8KOnlineBE(){

        //try{
        initItriDevice8KCommon();
        mEnumDeviceType = ENUM_DEVICE_TYPE.ITRI_8K_BE_ONLINE;

        //}catch(Exception ex1) {
        //SystemConfig.mMyEventLogger.appendDebugStr("initItriDevice8KOnlineBE.Exception", "");
        //}
    }

    public static void initItriDevice8KOffline(){

        //try{
        initItriDevice8KCommon();

        mEnumDeviceType = ENUM_DEVICE_TYPE.ITRI_8K_OFFLINE;

        //}catch(Exception ex1) {
            //SystemConfig.mMyEventLogger.appendDebugStr("initItriDevice8KOffline.Exception", "");
        //}
    }

    public static void initItriDevice44K(){

        //try{
        initItriDeviceCommon();

        //mIntUltrasound1DataBytes = ULTRASOUND_DATA_1DATA_BYTES_16PCM;

        //mIntSTFTWindowSize = INT_BLOOD_VELOCITY_STFT_WINDOW_SIZE_256;
        mIntSTFTWindowSize = INT_BLOOD_VELOCITY_STFT_WINDOW_SIZE_512;
        mIntSTFTWindowShiftSize = INT_BLOOD_VELOCITY_STFT_WINDOW_SHIFT_SIZE_256;
        mIntFreqIdxsMaxSize = mIntSTFTWindowSize/2 +1;

        mIntUltrasoundSamplerate = INT_ULTRASOUND_SAMPLING_RATE_44K; // Used in offline mode

        mDoubleSubSegTimesliceMsec = (((double)mIntSTFTWindowShiftSize) * (double)1000) / (double) mIntUltrasoundSamplerate;

        mIntUltrasoundSamplesMaxSize = INT_ULTRASOUND_DATA_MAX_SIS / mInt1DataBytes;

        //mIntMulHRMinLowWindowMilliSec = INT_HR_MINLOW_WINDOW_MILLISEC_ITRI_44K;

        mIntMaxIdxWindowSizeBySNR = INT_MAX_IDX_WINDOW_SIZE_ITRI;

        mEnumDeviceType = ENUM_DEVICE_TYPE.ITRI_44K;

        //}catch(Exception ex1) {
        //SystemConfig.mMyEventLogger.appendDebugStr("initItriDevice44K.Exception", "");
        //}
    }


    public static void initUScomDeviceCommon(){

        // can be canceled
        //mIntType2LongWindowMsec = 2000;

        // try{

        mInt1DataBytes = INT_ULTRASOUND_DATA_1DATA_BYTES_16PCM;

        //?mIntHeartRateSTFTWindowSize = HR_STFT_WINDOW_SIZE_OTHER;

        mDoubleSensorWaveFreq = DOUBLE_ULTRASOUND_SENSOR_WAVE_FREQ_USCOM;

        mIntUltrasoundSamplesMaxSize = INT_ULTRASOUND_DATA_MAX_SIS / mInt1DataBytes;

        mIntMaxIdxWindowSizeBySNR = INT_MAX_IDX_WINDOW_SIZE_USCOM;

        mIntVtiMaxIdxSmoothLeftSize = INT_VTI_MAX_IDX_DIFF_SMOOTH_LEFT_SIZE_USCOM;
        mIntVtiMaxIdxSmoothRightSize = INT_VTI_MAX_IDX_DIFF_SMOOTH_RIGHT_SIZE_USCOM;

        //}catch(Exception ex1) {
        //    SystemConfig.mMyEventLogger.appendDebugStr("initUscomDeviceCommon.Exception", "");
        //}
    }

    public static void initUScomDevice8K(){

        //try{
        initUScomDeviceCommon();

        mIntUltrasoundSamplerate = INT_ULTRASOUND_SAMPLING_RATE_8K; // Used in offline mode

        mIntSTFTWindowSize = INT_BLOOD_VELOCITY_STFT_WINDOW_SIZE_256;
        mIntSTFTWindowShiftSize = INT_BLOOD_VELOCITY_STFT_WINDOW_SHIFT_SIZE_64;
        mIntFreqIdxsMaxSize = mIntSTFTWindowSize/2 +1;

        mDoubleSubSegTimesliceMsec = (((double)mIntSTFTWindowShiftSize) * (double)1000) / (double)INT_ULTRASOUND_SAMPLING_RATE_8K;

        mEnumDeviceType = ENUM_DEVICE_TYPE.USCOM_8K;

        //}catch(Exception ex1) {
        //    SystemConfig.mMyEventLogger.appendDebugStr("initUscomDevice8K.Exception", "");
        //}
    }

    public static void initUScomDevice44K(){

        try{
            initUScomDeviceCommon();

        mIntUltrasoundSamplerate = INT_ULTRASOUND_SAMPLING_RATE_44K; // Used in offline mode

        mIntSTFTWindowSize = INT_BLOOD_VELOCITY_STFT_WINDOW_SIZE_512;
        mIntSTFTWindowShiftSize = INT_BLOOD_VELOCITY_STFT_WINDOW_SHIFT_SIZE_256;
        mIntFreqIdxsMaxSize = mIntSTFTWindowSize/2 +1;

        mDoubleSubSegTimesliceMsec = (((double)mIntSTFTWindowShiftSize) * (double)1000) / (double)INT_ULTRASOUND_SAMPLING_RATE_44K;

        mEnumDeviceType = ENUM_DEVICE_TYPE.USCOM_44K;

        }catch(Exception ex1) {
            ex1.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugStr("initUscomDevice44K.Exception", "");
        }
    }

    public static void systemInit() {
        int iMax, iVar, iVar2;
        double doubleMax, doubleMin;

        try {

            mDoubleBVSvMaxScaleForDisplay = (mIntBVSvMaxScaleIdxForDisplay + 1) * 0.1;
            if(mIntVpkAlgorithm == INT_VPK_ALGORITHM_0_SNSI_GM ){
                mStrSTFTWindowType = STFT_WINDOW_TYPE_NONE;
                //mStrSTFTWindowType = STFT_WINDOW_TYPE_HANNING;
                mDoubleBVSvMaxScaleForReal = mDoubleBVSvMaxScaleForDisplay / 2.0;
            }else if(mIntVpkAlgorithm == INT_VPK_ALGORITHM_1_WU_NEW){
                mStrSTFTWindowType = STFT_WINDOW_TYPE_HANNING;
                mDoubleBVSvMaxScaleForReal = mDoubleBVSvMaxScaleForDisplay;
            }else{
                mStrSTFTWindowType = STFT_WINDOW_TYPE_HANNING;
                mDoubleBVSvMaxScaleForReal = mDoubleBVSvMaxScaleForDisplay;
            }

            mDoubleDeviceFreqMapItri = new double[4];
            mDoubleDeviceFreqMapItri[0] = DOUBLE_DEVICE_FREQ_0_1p0M;
            mDoubleDeviceFreqMapItri[1] = DOUBLE_DEVICE_FREQ_1_1p5M;
            mDoubleDeviceFreqMapItri[2] = DOUBLE_DEVICE_FREQ_2_2p0M;
            mDoubleDeviceFreqMapItri[3] = DOUBLE_DEVICE_FREQ_3_2p5M;

            //-------------------------------------
            //--- for Gain Control
            //-------------------------------------
            mIntGainLevelMapVer1 = new int[]{750, 390, 300, 200, 120, 75, 47, 30};

            /*mIntLongStartDutyTimeSec = ULTRASOUND_START_ONLINE_SEC;*/

            mIntRawPacketCntsOnLine = getPacketCntFormDataSize(SystemConfig.INT_ULTRASOUND_DATA_MAX_SIS_ITRI_8K);

            //-------------------------------------------------------
            // for mIntSystemMaxSubSegSize
            //-------------------------------------------------------
            mIntSystemMaxSubSegSize = INT_ULTRASOUND_DATA_MAX_SIS / INT_BLOOD_VELOCITY_STFT_WINDOW_SHIFT_SIZE_64;

            //-------------------------------------------------------
            // for mIntSystemMaxMaxIdxWindowSize
            //-------------------------------------------------------
            iMax = INT_MAX_IDX_WINDOW_SIZE_ITRI;
            if (iMax < INT_MAX_IDX_WINDOW_SIZE_USCOM) {
                iMax = INT_MAX_IDX_WINDOW_SIZE_USCOM;
            }
            mIntSystemMaxMaxIdxWindowSize = iMax;

            //-------------------------------------------------------
            // for mIntSystemMaxHRMinLHWindowSize
            //-------------------------------------------------------
            iMax = INT_ULTRASOUND_SAMPLING_RATE_44K;
            doubleMin = (((double) INT_BLOOD_VELOCITY_STFT_WINDOW_SHIFT_SIZE_64) * (double) 1000) / (double) iMax;

            //-------------------------------------------------------
            // for mIntSystemMaxTotalFreqSeqsCnt
            //-------------------------------------------------------
            /*mIntSystemMaxTotalFreqSeqsCnt = INT_BLOOD_VELOCITY_STFT_WINDOW_SIZE_512 / 2 + 1;*/

            //-------------------------------------------------------
            // for mBloodVelocitySignalProcessor initialize
            //-------------------------------------------------------
            DOUBLE_HR_PERIOD_MSEC_MIN = new double[INT_HR_GROUP_CNT];
            DOUBLE_HR_PERIOD_MSEC_MAX = new double[INT_HR_GROUP_CNT];
            mDoubleArrayHRAverageMin = new double[INT_HR_GROUP_CNT];
            mDoubleArrayHRAverageMax = new double[INT_HR_GROUP_CNT];
            //mDoubleHRPeakIntegralWindowMsecRatio = new double[INT_HR_GROUP_CNT];
            //mDoubleHRPeakFeaturalWindowMsecRatio = new double[INT_HR_GROUP_CNT];
            //mDoubleVpkPeakS1IntegralWindowMsecRatio = new double[INT_HR_GROUP_CNT];
            //mDoubleVpkPeakS1FeaturalWindowMsecRatio = new double[INT_HR_GROUP_CNT];
            //mDoubleVpkPeakS2IntegralWindowMsecRatio = new double[INT_HR_GROUP_CNT];
            //mDoubleVpkPeakS2FeaturalWindowMsecRatio = new double[INT_HR_GROUP_CNT];
            //mDoublePhantomPeakIntegralWindowMsecRatio = new double[INT_HR_GROUP_CNT];
            //mDoublePhantomPeakFeaturalWindowMsecRatio = new double[INT_HR_GROUP_CNT];
            //mDoubleHRBottomIntegralWindowMsecRatio = new double[INT_HR_GROUP_CNT];
            //mDoubleHRBottomFeaturalWindowMsecRatio = new double[INT_HR_GROUP_CNT];
            //mDoubleVTIBottomIntegralWindowMsecRatio = new double[INT_HR_GROUP_CNT];
            //mDoubleVTIBottomFeaturalWindowMsecRatio = new double[INT_HR_GROUP_CNT];
            DOUBLE_HR_PEAK_INTEGRAL_WINDOW_MSEC_RATIO_MODE_PEAK_ITRI = new double[INT_HR_GROUP_CNT];
            DOUBLE_HR_PEAK_FEATURE_WINDOW_MSEC_RATIO_MODE_PEAK_ITRI = new double[INT_HR_GROUP_CNT];
            DOUBLE_HR_BOTTOM_INTEGRAL_WINDOW_MSEC_RATIO_MODE_PEAK_ITRI = new double[INT_HR_GROUP_CNT];
            DOUBLE_HR_BOTTOM_FEATURE_WINDOW_MSEC_RATIO_MODE_PEAK_ITRI = new double[INT_HR_GROUP_CNT];
            //DOUBLE_HR_PEAK_INTEGRAL_WINDOW_MSEC_RATIO_MODE_BOTTOM_ITRI = new double[INT_HR_GROUP_CNT];
            //DOUBLE_HR_PEAK_FEATURE_WINDOW_MSEC_RATIO_MODE_BOTTOM_ITRI = new double[INT_HR_GROUP_CNT];
            //DOUBLE_HR_BOTTOM_INTEGRAL_WINDOW_MSEC_RATIO_MODE_BOTTOM_ITRI = new double[INT_HR_GROUP_CNT];
            //DOUBLE_HR_BOTTOM_FEATURE_WINDOW_MSEC_RATIO_MODE_BOTTOM_ITRI = new double[INT_HR_GROUP_CNT];
            DOUBLE_VPK_PEAK_S1_INTEGRAL_WINDOW_MSEC_RATIO_ITRI = new double[INT_HR_GROUP_CNT];
            DOUBLE_VPK_PEAK_S1_FEATURE_WINDOW_MSEC_RATIO_ITRI = new double[INT_HR_GROUP_CNT];
            //DOUBLE_VPK_PEAK_S2_INTEGRAL_WINDOW_MSEC_RATIO_ITRI = new double[INT_HR_GROUP_CNT];
            //DOUBLE_VPK_PEAK_S2_FEATURE_WINDOW_MSEC_RATIO_ITRI = new double[INT_HR_GROUP_CNT];
            DOUBLE_PHANTOM_PEAK_INTEGRAL_WINDOW_MSEC_RATIO_ITRI = new double[INT_HR_GROUP_CNT];
            DOUBLE_PHANTOM_PEAK_FEATURE_WINDOW_MSEC_RATIO_ITRI = new double[INT_HR_GROUP_CNT];
            DOUBLE_VTI_START_INTEGRAL_WINDOW_MSEC_RATIO_ITRI = new double[INT_HR_GROUP_CNT];
            DOUBLE_VTI_START_FEATURE_WINDOW_MSEC_RATIO_ITRI = new double[INT_HR_GROUP_CNT];
            DOUBLE_VTI_END_INTEGRAL_WINDOW_MSEC_RATIO_ITRI = new double[INT_HR_GROUP_CNT];
            DOUBLE_VTI_END_FEATURE_WINDOW_MSEC_RATIO_ITRI = new double[INT_HR_GROUP_CNT];
            //DOUBLE_VTI_START_END_INTEGRAL_WINDOW_MSEC_RATIO_ITRI = new double[INT_HR_GROUP_CNT];
            //DOUBLE_VTI_START_END_FEATURE_WINDOW_MSEC_RATIO_ITRI = new double[INT_HR_GROUP_CNT];
            //-------------------------------------------------------------------------------
            DOUBLE_HR_PEAK_INTEGRAL_WINDOW_MSEC_RATIO_USCOM = new double[INT_HR_GROUP_CNT]; // = 300;  // for HR =80 --> 750 * 0.4 280
            DOUBLE_HR_PEAK_FEATURE_WINDOW_MSEC_RATIO_USCOM = new double[INT_HR_GROUP_CNT];
            //DOUBLE_HR_PEAK_HALF_INTEGRAL_WINDOW_MSEC_RATIO_USCOM = new double[INT_HR_GROUP_CNT]; // = 300;  // for HR =80 --> 750 * 0.4 280
            //DOUBLE_HR_PEAK_HALF_FEATURE_WINDOW_MSEC_RATIO_USCOM = new double[INT_HR_GROUP_CNT];
            DOUBLE_VPK_PEAK_S1_INTEGRAL_WINDOW_MSEC_RATIO_USCOM = new double[INT_HR_GROUP_CNT];
            DOUBLE_VPK_PEAK_S2_INTEGRAL_WINDOW_MSEC_RATIO_USCOM = new double[INT_HR_GROUP_CNT];
            DOUBLE_VPK_PEAK_S1_FEATURE_WINDOW_MSEC_RATIO_USCOM = new double[INT_HR_GROUP_CNT];
            DOUBLE_VPK_PEAK_S2_FEATURE_WINDOW_MSEC_RATIO_USCOM = new double[INT_HR_GROUP_CNT];
            DOUBLE_PHANTOM_PEAK_INTEGRAL_WINDOW_MSEC_RATIO_USCOM = new double[INT_HR_GROUP_CNT];
            DOUBLE_PHANTOM_PEAK_FEATURE_WINDOW_MSEC_RATIO_USCOM = new double[INT_HR_GROUP_CNT];
            DOUBLE_HR_BOTTOM_INTEGRAL_WINDOW_MSEC_RATIO_USCOM = new double[INT_HR_GROUP_CNT];
            DOUBLE_HR_BOTTOM_FEATURE_WINDOW_MSEC_RATIO_USCOM = new double[INT_HR_GROUP_CNT];
            DOUBLE_VTI_START_INTEGRAL_WINDOW_MSEC_RATIO_USCOM = new double[INT_HR_GROUP_CNT];
            DOUBLE_VTI_START_FEATURE_WINDOW_MSEC_RATIO_USCOM = new double[INT_HR_GROUP_CNT];
            DOUBLE_VTI_END_INTEGRAL_WINDOW_MSEC_RATIO_USCOM = new double[INT_HR_GROUP_CNT];
            DOUBLE_VTI_END_FEATURE_WINDOW_MSEC_RATIO_USCOM = new double[INT_HR_GROUP_CNT];
            //DOUBLE_VTI_START_END_INTEGRAL_WINDOW_MSEC_RATIO_USCOM = new double[INT_HR_GROUP_CNT];
            //DOUBLE_VTI_START_END_FEATURE_WINDOW_MSEC_RATIO_USCOM = new double[INT_HR_GROUP_CNT];

            //---HR Period Setting for every group -----------------------
            //---- Group0 : 500-->750 msec
            //*jaufa, 180619, -: Remark
            for (iVar = 0; iVar < INT_HR_GROUP_CNT; iVar++) {
                if (iVar == 0) {
                    DOUBLE_HR_PERIOD_MSEC_MIN[iVar] = 480;
                } else {
                    DOUBLE_HR_PERIOD_MSEC_MIN[iVar] = DOUBLE_HR_PERIOD_MSEC_MIN[iVar - 1] * 1.1;
                }
                DOUBLE_HR_PERIOD_MSEC_MAX[iVar] = DOUBLE_HR_PERIOD_MSEC_MIN[iVar] * 1.1;
            }
            //---HR Min/Max Setting for every group ------------------------
            for (iVar = 0; iVar < INT_HR_GROUP_CNT; iVar++) {
                mDoubleArrayHRAverageMin[iVar] = 60000 / DOUBLE_HR_PERIOD_MSEC_MAX[iVar];
                mDoubleArrayHRAverageMax[iVar] = 60000 / DOUBLE_HR_PERIOD_MSEC_MIN[iVar];
            }


            //jaufa, 180619, -: Remark */
            /*jaufa, 180619, +: Mark HR_GROUP=10, PERIOD = 1.1 , ini = 480
            double jDoubleHrPeriodMsecMin = 0;
            for (iVar = 0; iVar < INT_HR_GROUP_CNT; iVar++) {
                if (iVar == 0) {
                    jDoubleHrPeriodMsecMin = 480;
                } else {
                    jDoubleHrPeriodMsecMin *= 1.1;
                }
                DOUBLE_HR_PERIOD_MSEC_MIN[iVar] =  jDoubleHrPeriodMsecMin * 0.95;
                DOUBLE_HR_PERIOD_MSEC_MAX[iVar] = jDoubleHrPeriodMsecMin * 1.15;
            }
            //---HR Min/Max Setting for every group ------------------------
            for (iVar = 0; iVar < INT_HR_GROUP_CNT; iVar++) {
                mDoubleArrayHRAverageMin[iVar] = 60000 / DOUBLE_HR_PERIOD_MSEC_MAX[iVar];
                mDoubleArrayHRAverageMax[iVar] = 60000 / DOUBLE_HR_PERIOD_MSEC_MIN[iVar];
            }
            //*jaufa, 180619, +: Mark HR_GROUP=10, PERIOD = 1.1 , ini = 400  */
            //---ITRI Group ----------------------------
            for (iVar = 0; iVar < INT_HR_GROUP_CNT; iVar++) {
                //-- HR Peak Mode ------
                DOUBLE_HR_PEAK_INTEGRAL_WINDOW_MSEC_RATIO_MODE_PEAK_ITRI[iVar] = 0.47;  //jaufa, 180607, # ori: 0.2
                DOUBLE_HR_PEAK_FEATURE_WINDOW_MSEC_RATIO_MODE_PEAK_ITRI[iVar] = 0.98;   //jaufa, 180607, # ori: 0.9
                DOUBLE_HR_BOTTOM_INTEGRAL_WINDOW_MSEC_RATIO_MODE_PEAK_ITRI[iVar] = 0.3; //jaufa, 180607, # ori: 0.2
                DOUBLE_HR_BOTTOM_FEATURE_WINDOW_MSEC_RATIO_MODE_PEAK_ITRI[iVar] = 0.6; //jaufa, 180607, # ori: 0.35
                //-- HR Bottom Mode -------
                //DOUBLE_HR_PEAK_INTEGRAL_WINDOW_MSEC_RATIO_MODE_BOTTOM_ITRI[iVar] = 0.3;
                //DOUBLE_HR_PEAK_FEATURE_WINDOW_MSEC_RATIO_MODE_BOTTOM_ITRI[iVar] = 0.4;
                //DOUBLE_HR_BOTTOM_INTEGRAL_WINDOW_MSEC_RATIO_MODE_BOTTOM_ITRI[iVar] = 0.25;
                //DOUBLE_HR_BOTTOM_FEATURE_WINDOW_MSEC_RATIO_MODE_BOTTOM_ITRI[iVar] = 0.7;
                //---- Phantom Peak ----------
                DOUBLE_PHANTOM_PEAK_INTEGRAL_WINDOW_MSEC_RATIO_ITRI[iVar] = 0.18; //jaufa, 180612, ori: 0.1// 120/1000 = 0.12
                DOUBLE_PHANTOM_PEAK_FEATURE_WINDOW_MSEC_RATIO_ITRI[iVar] = 0.12;  //jaufa, 180612, ori: 0.1// 120/1000 = 0.12
                //---- VPK -------------------
                DOUBLE_VPK_PEAK_S1_INTEGRAL_WINDOW_MSEC_RATIO_ITRI[iVar] = 0.25;    //jaufa, 180612, ori: 0.25
                DOUBLE_VPK_PEAK_S1_FEATURE_WINDOW_MSEC_RATIO_ITRI[iVar] = 0.35; //jaufa, 180612, ori: 0.35
                //DOUBLE_VPK_PEAK_S2_INTEGRAL_WINDOW_MSEC_RATIO_ITRI[iVar] = 0.1;
                //DOUBLE_VPK_PEAK_S2_FEATURE_WINDOW_MSEC_RATIO_ITRI[iVar] = 0.1;

                DOUBLE_VTI_START_INTEGRAL_WINDOW_MSEC_RATIO_ITRI[iVar] = 0.1;   //jaufa, 180612, ori: 0.1
                DOUBLE_VTI_START_FEATURE_WINDOW_MSEC_RATIO_ITRI[iVar] = 0.1;    //jaufa, 180612, ori: 0.1
                //DOUBLE_VTI_END_INTEGRAL_WINDOW_MSEC_RATIO_ITRI[iVar] = 0.25;
                //DOUBLE_VTI_END_FEATURE_WINDOW_MSEC_RATIO_ITRI[iVar] = 0.25;
                DOUBLE_VTI_END_INTEGRAL_WINDOW_MSEC_RATIO_ITRI[iVar] = 0.12;    //jaufa, 180612, ori: 0.12
                DOUBLE_VTI_END_FEATURE_WINDOW_MSEC_RATIO_ITRI[iVar] = 0.12;     //jaufa, 180612, ori: 0.12
            }
            //--USCOM Group Parameter------------------------------------------------------------
            for (iVar = 0; iVar < INT_HR_GROUP_CNT; iVar++) {
                DOUBLE_HR_PEAK_INTEGRAL_WINDOW_MSEC_RATIO_USCOM[iVar] = 0.3;
                DOUBLE_HR_PEAK_FEATURE_WINDOW_MSEC_RATIO_USCOM[iVar] = 0.4;
                DOUBLE_HR_BOTTOM_INTEGRAL_WINDOW_MSEC_RATIO_USCOM[iVar] = 0.25;
                DOUBLE_HR_BOTTOM_FEATURE_WINDOW_MSEC_RATIO_USCOM[iVar] = 0.7;
                DOUBLE_PHANTOM_PEAK_INTEGRAL_WINDOW_MSEC_RATIO_USCOM[iVar] = 0.12;
                DOUBLE_PHANTOM_PEAK_FEATURE_WINDOW_MSEC_RATIO_USCOM[iVar] = 0.12;
                DOUBLE_VPK_PEAK_S1_INTEGRAL_WINDOW_MSEC_RATIO_USCOM[iVar] = 0.25;
                DOUBLE_VPK_PEAK_S1_FEATURE_WINDOW_MSEC_RATIO_USCOM[iVar] = 0.25;
                //DOUBLE_VPK_PEAK_S2_INTEGRAL_WINDOW_MSEC_RATIO_USCOM[iVar] = 0.2;
                //DOUBLE_VPK_PEAK_S2_FEATURE_WINDOW_MSEC_RATIO_USCOM[iVar] = 0.2;
                DOUBLE_VTI_START_INTEGRAL_WINDOW_MSEC_RATIO_USCOM[iVar] = 0.1;
                DOUBLE_VTI_START_FEATURE_WINDOW_MSEC_RATIO_USCOM[iVar] = 0.1;
                DOUBLE_VTI_END_INTEGRAL_WINDOW_MSEC_RATIO_USCOM[iVar] = 0.25;
                DOUBLE_VTI_END_FEATURE_WINDOW_MSEC_RATIO_USCOM[iVar] = 0.25;
                /*DOUBLE_VTI_START_END_INTEGRAL_WINDOW_MSEC_RATIO_USCOM[iVar] = 0.12;
                DOUBLE_VTI_START_END_FEATURE_WINDOW_MSEC_RATIO_USCOM[iVar] = 0.12;*/
            }

/*
            if (mBVSignalProcessorPart1 == null) {
                mBVSignalProcessorPart1 = new BVSignalProcessorPart1();
            }
            if (mBVSignalProcessorPart2Array == null) {
                mBVSignalProcessorPart2Array = new BVSignalProcessorPart2[INT_HR_GROUP_CNT];
                for (iVar = 0; iVar < INT_HR_GROUP_CNT; iVar++) {
                    SystemConfig.mBVSignalProcessorPart2Array[iVar] = new BVSignalProcessorPart2(iVar);
                }
            }

            if (SystemConfig.mRawDataProcessor == null) {
                SystemConfig.mRawDataProcessor = new RawDataProcessor();
                SystemConfig.mRawDataProcessor.initRawDataProcessor();
            }

            //----------- for BVSignalProcessorController -------
            if (SystemConfig.mSignalProcessController == null) {
                SystemConfig.mSignalProcessController = new BVSignalProcessController();
                SystemConfig.mSignalProcessController.startThread(10);
            }

            //----------- for Audio Player Controller -------
            if (SystemConfig.mAudioPlayerController == null) {
                SystemConfig.mAudioPlayerController = new MyAudioPlayerController();
                SystemConfig.mAudioPlayerController.startThread(10);
            }
            */
        }catch(Exception ex1) {
            //SystemConfig.mMyEventLogger.appendDebugStr("systemInit.Exception", "");
        }
    }

    private static int getPacketDataByteSizeItri(){
        return (SystemConfig.INT_ULTRASOUND_1PACKET_PAYLOAD_BYTES_ITRI
                -SystemConfig.INT_ULTRASOUND_DATA_GAIN_BYTES_ITRI-SystemConfig.INT_ULTRASOUND_DATA_SEQ_BYTES_ITRI);
    }

    private static int getPacketDataByteSizeUscom() {
        return (mIntSTFTWindowSize * INT_ULTRASOUND_DATA_1DATA_BYTES_16PCM * 8);
    }


    public static int getPacketCntFormDataSize(int iDataSize){
        int iValue, iValue2, iPacketCnt;

        if (isHeartIO2){
            iValue = mIntPacketDataByteSizeHeartIO2;
        }else{
            iValue = (SystemConfig.INT_ULTRASOUND_1PACKET_PAYLOAD_BYTES_ITRI - SystemConfig.INT_ULTRASOUND_DATA_GAIN_BYTES_ITRI - SystemConfig.INT_ULTRASOUND_DATA_SEQ_BYTES_ITRI) / 2;
        }

        iPacketCnt = iDataSize / iValue;
        iValue2 = iDataSize % iValue;
        if (iValue2 != 0) {
            iPacketCnt++;
        }
        return iPacketCnt;
    }

}
