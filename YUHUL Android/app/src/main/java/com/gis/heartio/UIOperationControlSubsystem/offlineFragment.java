package com.gis.heartio.UIOperationControlSubsystem;

import static com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.IwuSQLHelper.KEY_DATA_TABLE_DEVICE_MAC_ADDR;
import static com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.SystemConfig.cloudToken;
import static com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.Utilitys.getToken;
import static com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.Utilitys.isNetOnline;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.gis.heartio.GIS_DataController;
import com.gis.heartio.GIS_Log;
import com.gis.heartio.R;
import com.gis.heartio.SignalProcessSubsysII.utilities.Doppler;
import com.gis.heartio.SignalProcessSubsystem.BVSignalProcessController;
import com.gis.heartio.SignalProcessSubsystem.BVSignalProcessorPart1;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.IwuSQLHelper;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.SystemConfig;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.Utilitys;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.dataInfo;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.dataInfoTestMode;
import com.opencsv.CSVWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import yogesh.firzen.filelister.FileListerDialog;


/**
 * Created by Cavin on 2017/12/15.
 */

public class offlineFragment extends Fragment {
    private final String TAG = "offlineFragment";

    private static final String ARG_FILENAME = "filename";
    private static final String ARG_ID = "id";

    private boolean uploadData = false;
    private boolean uploadWav = false;

    boolean mBoolOfflineSoundDataExist = false;

    public static float mFloatTimeScalePosCur;

    private Button  calculateButton;
    private ImageButton mPlaySoundBtn, mPauseSoundBtn;
    private TextView mDataInfoTextView;
    //private Switch mVTISwitch, mFilterSwitch, mWindowSwitch, mDCOffsetSwitch, mSingleVpkSwitch;
    private SwitchCompat mVTISwitch, mFilterSwitch, mWindowSwitch, mDCOffsetSwitch, mSingleVpkSwitch;
    private SwitchCompat mTestSwitch, mInverseSwitch, mFourthLevelSwitch;
    public final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1234;
    private String openFilePath = "";
    private String inputFileName = "";
    public String currentFilename = "";
    private long inputID = 0;
    private boolean needReDrawScale = false;

    // ECG view
    public EcgView mECGOffline;
    public View mVECGLeftSpace;
    public boolean hasECG = false;

    public SurfaceView mSVUltrasoundOffline, mSVScaleOffline, mSVSpaceScaleOffline;
    public SurfaceView mSurfaceViewTimeScaleDownSpace, mSurfaceViewTimeScaleDown;
    public SurfaceView mSurfaceViewTimeScaleUpSpace, mSurfaceViewTimeScaleUp;
    private boolean mBoolSvUltrasoundTouchAtUpper;

    private TextView mHRValueTextView, mVTIValueTextView,
            mVpkValueTextView, mSVValueTextView, mCOValueTextView;

    private TextView mSNRTextView;

    private EditText rxEditText;

    private LinearLayout resultVti, resultVpk;

    public static MyPlotterBloodVelocity mBVPOffline;

    public float mFloatSvOnTouchStartX;
    public float mFloatSvOnTouchStartY;
    public float mFloatSvOnTouchEndX;
    public float mFloatSvOnTouch2PointX;
    private AppCompatActivity mActivity = null;

    /*public static final int UI_MSG_ID_SHOW_BV_SV_OFFLINE = 0;*/
    private static IwuSQLHelper mHelper;

    // Cavin add for two point
    public static int twoPointStart, twoPointEnd, lastPoint = -1;


    public static offlineFragment newInstance() {
        return new offlineFragment();
    }

    public static offlineFragment newInstance(String inputName, long id){
        offlineFragment fragment = new offlineFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILENAME, inputName); // wav file
        args.putLong(ARG_ID,id); // _id
//        Log.d("inputName ", inputName);
//        Log.d("id ", String.valueOf(id));
        fragment.setArguments(args);
        return fragment;
    }

    public offlineFragment(){
        MainActivity.offFrag = this;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (AppCompatActivity)getActivity();


        if (getArguments() != null) {
            inputFileName = getArguments().getString(ARG_FILENAME);
            inputID = getArguments().getLong(ARG_ID);
            needReDrawScale = true;
        }
        mHelper = new IwuSQLHelper(mActivity);
    }

    @Override
    public void onDestroy() {
        mHelper.closeDatabase();
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =inflater.inflate(R.layout.fragment_item_offline, container, false);

        if (mActivity!=null){
            Objects.requireNonNull(mActivity.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            mActivity.getSupportActionBar().setDisplayShowHomeEnabled(true);
            setHasOptionsMenu(true);
        }

        // Init User Info
        TextView mUserInfoTextView = rootView.findViewById(R.id.userInfoTextView);
        String userInfoStr = UserManagerCommon.mUserInfoCur.userID+ "   " +
                                UserManagerCommon.mUserInfoCur.firstName + "  "+
                                UserManagerCommon.mUserInfoCur.lastName+ "   PA Dia.  "+
                                UserManagerCommon.mDoubleUserPulmDiameterCm;

        mUserInfoTextView.setText(userInfoStr);

        mDataInfoTextView = rootView.findViewById(R.id.dataInfoTextView);

        initSurfaceViews(rootView);

        resultVti = rootView.findViewById(R.id.resultVTIOfflineLinearLayout);
        resultVpk = rootView.findViewById(R.id.resultVpkOfflineLinearLayout);

        mHRValueTextView = rootView.findViewById(R.id.hrValueOfflineTextView);
        if (!SystemConfig.isYuhul || SystemConfig.mTestMode){
            mVTIValueTextView = rootView.findViewById(R.id.VTIValueOfflineTextView);
            mVpkValueTextView = rootView.findViewById(R.id.VpkValueOfflineTextView);
        }

        mSVValueTextView = rootView.findViewById(R.id.SVValueOfflineTextView);
        mCOValueTextView = rootView.findViewById(R.id.COValueOfflineTextView);

        mSNRTextView = rootView.findViewById(R.id.SNRTextView);

        mVTISwitch = rootView.findViewById(R.id.vtiSwitch);
        mVTISwitch.setChecked(true);
        mVTISwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b){
                SystemConfig.mIntDrawVTIEnabled = SystemConfig.INT_DRAW_VTI_ENABLED_YES;
            }else{
                SystemConfig.mIntDrawVTIEnabled = SystemConfig.INT_DRAW_VTI_ENABLED_NO;
            }

        });
        mVTISwitch.setEnabled(false);

        mDCOffsetSwitch = rootView.findViewById(R.id.dcoffsetSwitch);
        mDCOffsetSwitch.setChecked(SystemConfig.mBoolDCOffsetEnable);
        mDCOffsetSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            SystemConfig.mBoolDCOffsetEnable = b;
            if (!openFilePath.equals("")){
                enableBloodVelocityStartFromFileAction(openFilePath);
            }
        });
        mDCOffsetSwitch.setEnabled(false);

        mFilterSwitch = rootView.findViewById(R.id.filterSwitch);
        mFilterSwitch.setChecked(SystemConfig.mIntFilterDataEnabled == SystemConfig.INT_FILTER_ENABLED_YES);
        mFilterSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b){
                SystemConfig.mIntFilterDataEnabled = SystemConfig.INT_FILTER_ENABLED_YES;
            }else {
                SystemConfig.mIntFilterDataEnabled = SystemConfig.INT_FILTER_ENABLED_NO;
            }
            enableBloodVelocityStartFromFileAction(openFilePath);
        });
//        mFilterSwitch.setEnabled(false);

        mSingleVpkSwitch = rootView.findViewById(R.id.singleVpkSwitch);
        mSingleVpkSwitch.setChecked(SystemConfig.mIntSingleVpkEnabled == SystemConfig.INT_SINGLE_VPK_ENABLED_YES);
        mSingleVpkSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b){
                SystemConfig.mIntSingleVpkEnabled = SystemConfig.INT_SINGLE_VPK_ENABLED_YES;
                mVpkValueTextView.setTextSize(12);
            }else{
                SystemConfig.mIntSingleVpkEnabled = SystemConfig.INT_SINGLE_VPK_ENABLED_NO;
                mVpkValueTextView.setTextSize(18);
            }
            enableBloodVelocityStartFromFileAction(openFilePath);
        });
        if (SystemConfig.isYuhul){
            resultVpk.setVisibility(View.GONE);
            resultVti.setVisibility(View.GONE);
        }

        mTestSwitch = rootView.findViewById(R.id.testSwitch);
        mTestSwitch.setChecked(Doppler.cavinTest);
        mTestSwitch.setOnCheckedChangeListener((compoundButton, b) ->{
            Doppler.cavinTest = b;
            enableBloodVelocityStartFromFileAction(openFilePath);
        });

        mInverseSwitch = rootView.findViewById(R.id.inverseSwitchOffline);
        mInverseSwitch.setChecked(BVSignalProcessorPart1.isInverseFreq);
        mInverseSwitch.setOnCheckedChangeListener(((compoundButton, b) -> {
            BVSignalProcessorPart1.isInverseFreq = b;
            enableBloodVelocityStartFromFileAction(openFilePath);
        }));

        mFourthLevelSwitch = rootView.findViewById(R.id.fourthLevel);
        mFourthLevelSwitch.setChecked(BVSignalProcessorPart1.isFourthLevel);
        mFourthLevelSwitch.setOnCheckedChangeListener(((compoundButton, b) -> {
            BVSignalProcessorPart1.isFourthLevel = b;
            MainActivity.mBVSignalProcessorPart1.initButterworthFilterForUS();
            enableBloodVelocityStartFromFileAction(openFilePath);
        }));

        mWindowSwitch = rootView.findViewById(R.id.windowSwitch);
        mWindowSwitch.setChecked(!SystemConfig.mStrSTFTWindowType.equals(SystemConfig.STFT_WINDOW_TYPE_NONE));
        mWindowSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b){
                SystemConfig.mStrSTFTWindowType = SystemConfig.STFT_WINDOW_TYPE_HANNING;
            }else{
                SystemConfig.mStrSTFTWindowType = SystemConfig.STFT_WINDOW_TYPE_NONE;
            }
            enableBloodVelocityStartFromFileAction(openFilePath);
        });
        mWindowSwitch.setEnabled(false);

        Button openFileButton = rootView.findViewById(R.id.openfile_button);
        if (getContext()!=null){
            final FileListerDialog fileListerDialog = //FileListerDialog.createFileListerDialog(getContext(),R.style.MyFilesListDialogTheme);
            FileListerDialog.createFileListerDialog(getContext());
            //fileListerDialog.setDefaultDir(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
            //String mStrBaseFolder = Utilities.getUserBaseFilePath(mActivity,UserManagerCommon.mUserInfoCur.userID);
            String mStrDownloadFolder = Utilitys.getDownloadBaseFilePath(mActivity);

            fileListerDialog.setDefaultDir(mStrDownloadFolder);
            //fileListerDialog.setFileFilter(FileListerDialog.FILE_FILTER.AUDIO_ONLY);

            openFileButton.setOnClickListener(view -> fileListerDialog.show());

            fileListerDialog.setOnFileSelectedListener((file, path) -> {
                //your code here
                GIS_Log.d(TAG,"selected file path:"+path);
                openFilePath = path;
                currentFilename = file.getName();
                mDataInfoTextView.setText(currentFilename);
                enableBloodVelocityStartFromFileAction(path);
                //File wavFile = new File(path);
                //WavFile waveFile = new WavFile(path);
            });

        }

        Button spiderButton = rootView.findViewById(R.id.spiderButton);
        if (getContext()!=null){
            final FileListerDialog dirListerDialog = //FileListerDialog.createFileListerDialog(getContext(),R.style.MyFilesListDialogTheme);
                    FileListerDialog.createFileListerDialog(getContext());

            //String mStrBaseFolder = Utilities.getAppDataFilePath(mActivity);
            String mStrBaseFolder = Utilitys.getDownloadBaseFilePath(mActivity);
            dirListerDialog.setDefaultDir(mStrBaseFolder);
            dirListerDialog.setFileFilter(FileListerDialog.FILE_FILTER.DIRECTORY_ONLY);

            spiderButton.setOnClickListener(view -> dirListerDialog.show());

            dirListerDialog.setOnFileSelectedListener((file, path) -> {
                //your code here
                File directory = new File(path);
                File[] files = directory.listFiles();
                List<dataInfoTestMode> singleDataInfoList;
                List<dataInfo> dataInfoList;
//                if(SystemConfig.mIntSingleVpkEnabled == SystemConfig.INT_SINGLE_VPK_ENABLED_YES) {
                    singleDataInfoList = new ArrayList<>();
//                }else {
                    dataInfoList = new ArrayList<>();
//                }

                for (int i = 0; i < files.length; i++)
                {
                    String wavPath = path+File.separator+files[i].getName();
                    // jaufa, +, '181121, Prepare For SingleMode Data
                    if(SystemConfig.mIntSingleVpkEnabled == SystemConfig.INT_SINGLE_VPK_ENABLED_YES){
                        dataInfoTestMode tmpDataSingleMode = getResultFromFileSingleMode(wavPath);
                        if (tmpDataSingleMode != null) {
                            tmpDataSingleMode.fileName = files[i].getName();
                            tmpDataSingleMode.userId = directory.getName();
                            tmpDataSingleMode.createdDate = getCreateDateStrFromFilename(tmpDataSingleMode.fileName);

                            singleDataInfoList.add(tmpDataSingleMode);
                        }
                    }else{
                        dataInfo tmpData = getResultFromFile(wavPath);
                        if (tmpData != null) {
                            tmpData.fileName = files[i].getName();
                            tmpData.userId = directory.getName();
                            tmpData.createdDate = getCreateDateStrFromFilename(tmpData.fileName);
                            dataInfoList.add(tmpData);
                            GIS_Log.d(TAG, "HR:" + tmpData.HR + ", Vpk: " + tmpData.Vpk + ", VTI: " + tmpData.VTI +
                                    ", SV:" + tmpData.SV + "/n CO: " + tmpData.CO + ", Filename: " +
                                    tmpData.fileName + ", Result:" + tmpData.result + ", UserID: " + tmpData.userId + " ,CreateDate: " + tmpData.createdDate);
                        }
                    }
                }

                if (singleDataInfoList.size()>0){
                    exportDirSingleMode(singleDataInfoList, path);
                }

                if (dataInfoList.size()>0){
//                    if(SystemConfig.mIntSingleVpkEnabled == SystemConfig.INT_SINGLE_VPK_ENABLED_YES) {
//
//                    }else{
                        exportDir(dataInfoList, path);
//                    }
                }
//                    openFilePath = path;
//                    currentFilename = file.getName();
//                    mDataInfoTextView.setText(currentFilename);
//                    enableBloodVelocityStartFromFileAction(path);
                //File wavFile = new File(path);
                //WavFile waveFile = new WavFile(path);
            });

        }
        Button updateButton = rootView.findViewById(R.id.updateButton);


        rxEditText = rootView.findViewById(R.id.rxAngleEditTextNumber);
        //rxEditText.setText(""+SystemConfig.rxAngle);
        rxEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() > 0){
                    double inputValue = Double.parseDouble(editable.toString());
                    if (inputValue > 0 && inputValue<90){
                        SystemConfig.rxAngle = inputValue;
                    }
                }

            }
        });
        RadioGroup mVTIModeGroup = rootView.findViewById(R.id.VTIModeGroup);

        if (SystemConfig.mTestMode){
            openFileButton.setVisibility(View.VISIBLE);
//            mSNRTextView.setVisibility(View.VISIBLE);
            mSNRTextView.setVisibility(View.GONE);
            mWindowSwitch.setVisibility(View.VISIBLE);
            mFilterSwitch.setVisibility(View.VISIBLE);
            mSingleVpkSwitch.setVisibility(View.VISIBLE);
            mTestSwitch.setVisibility(View.VISIBLE);
            mInverseSwitch.setVisibility(View.VISIBLE);
            mFourthLevelSwitch.setVisibility(View.VISIBLE);
            mDCOffsetSwitch.setVisibility(View.VISIBLE);
            updateButton.setVisibility(View.VISIBLE);
            spiderButton.setVisibility(View.VISIBLE);
            rxEditText.setVisibility(View.VISIBLE);
            mVTISwitch.setVisibility(View.VISIBLE);
            mVTIModeGroup.setVisibility(View.VISIBLE);
            resultVpk.setVisibility(View.VISIBLE);
            resultVti.setVisibility(View.VISIBLE);

        }else{
            openFileButton.setVisibility(View.GONE);
            mSNRTextView.setVisibility(View.GONE);
            mWindowSwitch.setVisibility(View.GONE);
            mFilterSwitch.setVisibility(View.GONE);
            mSingleVpkSwitch.setVisibility(View.GONE);
            mTestSwitch.setVisibility(View.GONE);
            mInverseSwitch.setVisibility(View.GONE);
            mFourthLevelSwitch.setVisibility(View.GONE);
            mDCOffsetSwitch.setVisibility(View.GONE);
            updateButton.setVisibility(View.GONE);
            spiderButton.setVisibility(View.GONE);
            rxEditText.setVisibility(View.GONE);
            mVTISwitch.setVisibility(View.GONE);
            mVTIModeGroup.setVisibility(View.GONE);
        }



        mPlaySoundBtn = rootView.findViewById(R.id.playSoundBtn);
        mPauseSoundBtn = rootView.findViewById(R.id.pauseSoundBtn);

        mPlaySoundBtn.setEnabled(false);

        mPlaySoundBtn.setOnClickListener(btnSoundOnClick);
        mPauseSoundBtn.setOnClickListener(btnSoundPauseListener);

//        RadioButton mStrongPeakModeRB = rootView.findViewById(R.id.strongPeakMode);
//        RadioButton mTwoPointModeRB = rootView.findViewById(R.id.twoPointMode);

        if (SystemConfig.mIntVTIModeIdx == SystemConfig.INT_VTI_MODE_0_STRONGEST){
//            SystemConfig.mIntVTIModeIdx = SystemConfig.INT_VTI_MODE_0_STRONGEST;
            mVTIModeGroup.check(R.id.strongPeakMode);
        }else{
            mVTIModeGroup.check(R.id.twoPointMode);
        }

        updateButton.setOnClickListener(view -> {
            if (SystemConfig.mIntVTIModeIdx == SystemConfig.INT_VTI_MODE_0_STRONGEST) {
                updateBloodVelocityData(MainActivity.mSignalProcessController.getResultDataAfterSignalProcess());
            } else{
                updateBloodVelocityData(BVSignalProcessController.getResultFromTwoPointWu(twoPointStart,twoPointEnd));
            }
        });

        mVTIModeGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            if (i == R.id.strongPeakMode){
                offlineFragment.twoPointStart = 0;
                offlineFragment.twoPointEnd = 0;
                offlineFragment.lastPoint = -1;
                if (SystemConfig.mIntVTIModeIdx!=SystemConfig.INT_VTI_MODE_0_STRONGEST){
                    SystemConfig.mIntVTIModeIdx = SystemConfig.INT_VTI_MODE_0_STRONGEST;
                    if (!openFilePath.equalsIgnoreCase("")){
                        enableBloodVelocityStartFromFileAction(openFilePath);
                    }
                }
                mSurfaceViewTimeScaleUp.setVisibility(View.INVISIBLE);
                mSurfaceViewTimeScaleUpSpace.setVisibility(View.INVISIBLE);
            } else if (i == R.id.twoPointMode){
                if (SystemConfig.mIntVTIModeIdx!=SystemConfig.INT_VTI_MODE_2_TWO_POINT){
                    SystemConfig.mIntVTIModeIdx = SystemConfig.INT_VTI_MODE_2_TWO_POINT;
                    if (!openFilePath.equalsIgnoreCase("")){
                        enableBloodVelocityStartFromFileAction(openFilePath);
                    }
                }
                mSurfaceViewTimeScaleUp.setVisibility(View.VISIBLE);
                mSurfaceViewTimeScaleUpSpace.setVisibility(View.VISIBLE);
            } else{
                SystemConfig.mIntVTIModeIdx = SystemConfig.INT_VTI_MODE_0_STRONGEST;
            }
        });

        this.calculateButton = rootView.findViewById(R.id.calculateBtn);
        calculateButton.setOnClickListener(btnCalculateOnClick);
//        calculateButton.setEnabled(false);
        calculateButton.setVisibility(View.INVISIBLE);

        if (ContextCompat.checkSelfPermission(mActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(mActivity,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }



        //resetSurfaceView();

        /*if (!inputFileName.equals("")){
            String mStrBaseFolder = Utilities.getUserBaseFilePath(mActivity,UserManagerCommon.mUserInfoCur.userID);
            openFilePath = mStrBaseFolder + File.separator +inputFileName;
            mDataInfoTextView.setText(inputFileName);
            enableBloodVelocityStartFromFileAction(openFilePath);
        }*/

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.update_to_cloud_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.update_data:
//                Log.d(TAG, "click upload~");
                AlertDialog alertDialog = new AlertDialog.Builder(mActivity,R.style.MyDialogTheme).create(); //Read Update
                alertDialog.setTitle("資料上傳");
                alertDialog.setMessage("確定上傳該筆資料?");
                alertDialog.setButton(Dialog.BUTTON_POSITIVE, "上傳", (dialog, id) -> {
                    uploadData();
//                    if (uploadSuccess){
//                        Toast.makeText(mActivity, "上傳成功", Toast.LENGTH_SHORT).show();
//                    }else {
//                        Toast.makeText(mActivity, "上傳失敗", Toast.LENGTH_SHORT).show();
//                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.cancel), (dialog, id) -> {
                    // Do nothing.
                });
                alertDialog.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* Add upload function UI 2024/06/12 by Doris */
    /* Finish upload function with API 2024/07/30 by Doris */
    @SuppressLint("Range")
    private void uploadData(){
//        List<dataInfo> uploadDataInfo = new ArrayList<>();
        dataInfo uploadDataInfo = new dataInfo();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursorCourses = db.rawQuery("select *  from "+IwuSQLHelper.STR_TABLE_DATA+" where " + IwuSQLHelper.KEY_DATA_TABLE_FILE_NAME +" = ?", new String[]{inputFileName});
//        Cursor cursorCourses = db.rawQuery("select *  from "+IwuSQLHelper.STR_TABLE_DATA, null);
//        Log.d(TAG, String.valueOf(cursorCourses));

        while (cursorCourses.moveToNext()){
//            String data = cursorCourses.getString(cursorCourses.getColumnIndex("HR"));
            uploadDataInfo.userId = cursorCourses.getString(cursorCourses.getColumnIndex(IwuSQLHelper.KEY_DATA_TABLE_USER_ID));
            uploadDataInfo.createdDate = cursorCourses.getString(cursorCourses.getColumnIndex(IwuSQLHelper.KEY_DATA_TABLE_CREATED_DATE));
            uploadDataInfo.HR = Integer.parseInt(cursorCourses.getString(cursorCourses.getColumnIndex(IwuSQLHelper.KEY_DATA_TABLE_HR)));
            uploadDataInfo.CO = Double.parseDouble(cursorCourses.getString(cursorCourses.getColumnIndex(IwuSQLHelper.KEY_DATA_TABLE_CO)));
            uploadDataInfo.SV = Double.parseDouble(cursorCourses.getString(cursorCourses.getColumnIndex(IwuSQLHelper.KEY_DATA_TABLE_SV)));
            uploadDataInfo.Vpk = Double.parseDouble(cursorCourses.getString(cursorCourses.getColumnIndex(IwuSQLHelper.KEY_DATA_TABLE_VPK)));
            uploadDataInfo.VTI = Double.parseDouble(cursorCourses.getString(cursorCourses.getColumnIndex(IwuSQLHelper.KEY_DATA_TABLE_VTI)));
            uploadDataInfo.fileName = cursorCourses.getString(cursorCourses.getColumnIndex(IwuSQLHelper.KEY_DATA_TABLE_FILE_NAME));
            uploadDataInfo.macAddress = cursorCourses.getString(cursorCourses.getColumnIndex(KEY_DATA_TABLE_DEVICE_MAC_ADDR));
        }

//        Log.d(TAG, uploadDataInfo.toString());
        cursorCourses.close();

        /* 串接上傳使用者數據API 2024/07/29 by Doris */

        if (isNetOnline(mActivity)){
            getToken(mActivity);
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("clientId", uploadDataInfo.macAddress);
                jsonObject.put("hr", uploadDataInfo.HR);
                jsonObject.put("sv", uploadDataInfo.SV);
                jsonObject.put("co", uploadDataInfo.CO);
                jsonObject.put("indexTime", uploadDataInfo.createdDate.replace('/', '-'));
                jsonObject.put("fileName", uploadDataInfo.fileName);
//                jsonObject.put("patientPhone", uploadDataInfo.userId);
                jsonObject.put("userId", uploadDataInfo.userId);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //上傳數據
            RequestBody formBody = RequestBody.create(mediaType, jsonObject.toString());
            Request request = new Request.Builder()
                    .url("http://139.196.4.88:8081/beplus/medical/patientHeart/add")
                    .addHeader("X-Access-Token", cloudToken)
//                                    .addHeader("Content-Type", "application/json")
//                                    .addHeader("Authorization", "••••••")
                    .post(formBody)
                    .build();


            // 建立Call
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response){
                    //連線成功，取得回傳response
                    try {
                        String result = response.body().string();

                        JSONObject jObject = new JSONObject(result);
                        String status = jObject.getString("success");
                        String message = jObject.getString("message");
                        if (status.equals("true")){
                            uploadData = true;
                        }else {
                            mActivity.runOnUiThread(()->{
                                Toast.makeText(mActivity, "上傳失敗：" + message, Toast.LENGTH_SHORT).show();
                            });
                            return;
                        }
                        Log.d(TAG, "success = "+status);
                        Log.d(TAG, "message = "+message);
//                        Log.d(TAG, "uploadWav = " + uploadWav);

//                        Log.d(TAG, "token = "+cloudToken);
                    } catch (IOException | JSONException e) {
                        mActivity.runOnUiThread(()->{
                            Toast.makeText(mActivity, "執行上傳動作失敗", Toast.LENGTH_SHORT).show();
                        });
                        e.printStackTrace();
                    }
                }
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    mActivity.runOnUiThread(()->{
                        Toast.makeText(mActivity, "伺服器連線異常，未執行上傳動作", Toast.LENGTH_SHORT).show();
                    });
                    //當連線失敗
                    e.printStackTrace();
                }
            });

            //上傳file
            RequestBody fileBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("files",uploadDataInfo.fileName,
                            RequestBody.create(MediaType.parse("application/octet-stream"),
                                    new File(openFilePath)))
                    .addFormDataPart("userId",uploadDataInfo.userId)
                    .build();

            Request fileRequest = new Request.Builder()
                    .url("http://139.196.4.88:8081/beplus/medical/patientHeart/upload")
                    .method("POST", fileBody)
                    .addHeader("X-Access-Token", cloudToken)
//                    .addHeader("Authorization", "••••••")
                    .build();
//            Response response = client.newCall(request).execute();

            // 建立Call
            Call fileCall = client.newCall(fileRequest);
            fileCall.enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response){
                    //連線成功，取得回傳response
                    try {
                        String result = response.body().string();

                        JSONObject jObject = new JSONObject(result);
                        String status = jObject.getString("success");
                        String message = jObject.getString("message");
                        if (status.equals("true")){
                            uploadWav = true;
                        }
                        mActivity.runOnUiThread(()->{
                            if (uploadWav && uploadData){
                                Toast.makeText(mActivity, "上傳成功", Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(mActivity, "上傳失敗：" + message, Toast.LENGTH_SHORT).show();
                            }
                        });
//                        uploadSuccess = true;
                        Log.d(TAG, "success = "+status);
                        Log.d(TAG, "message = "+message);
//                        Toast.makeText(mActivity, "上傳成功", Toast.LENGTH_SHORT).show();
//                        Log.d(TAG, "token = "+cloudToken);
                    } catch (IOException | JSONException e) {
                        mActivity.runOnUiThread(()->{
                            Toast.makeText(mActivity, "執行上傳動作失敗", Toast.LENGTH_SHORT).show();
                        });
                        e.printStackTrace();
                    }
                }
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    mActivity.runOnUiThread(()->{
                        Toast.makeText(mActivity, "伺服器連線異常，未執行上傳動作", Toast.LENGTH_SHORT).show();
                    });
                    //當連線失敗
                    e.printStackTrace();
                }
            });
        }else {
            Toast.makeText(mActivity, "請連接網路", Toast.LENGTH_SHORT).show();
        }

        Log.d(TAG, uploadDataInfo.userId);
        Log.d(TAG, uploadDataInfo.createdDate.replace('/', '-'));
        Log.d(TAG, String.valueOf(uploadDataInfo.HR));
        Log.d(TAG, String.valueOf(uploadDataInfo.CO));
        Log.d(TAG, String.valueOf(uploadDataInfo.SV));
        Log.d(TAG, String.valueOf(uploadDataInfo.Vpk));
        Log.d(TAG, String.valueOf(uploadDataInfo.VTI));
        Log.d(TAG, uploadDataInfo.macAddress);
        Log.d(TAG, uploadDataInfo.fileName);
    }

    private void initSurfaceViews(View rootView){
        mECGOffline = rootView.findViewById(R.id.vEcgView);
        mVECGLeftSpace = rootView.findViewById(R.id.vEcgLeftSpace);

        mSVSpaceScaleOffline = rootView.findViewById(R.id.svSpaceScaleOffline);
        mSVSpaceScaleOffline.setZOrderOnTop(true);
        mSVScaleOffline = rootView.findViewById(R.id.svScaleOffline);
        mSVScaleOffline.setZOrderOnTop(true);
        mSVUltrasoundOffline = rootView.findViewById(R.id.svUltrasoundOffline);
        mSVUltrasoundOffline.setZOrderOnTop(true);

        mSVUltrasoundOffline.setOnTouchListener(mSvUltrasoundOnTouchListener);

        mSurfaceViewTimeScaleDown = rootView.findViewById(R.id.svTimeScaleDownOffline);
        mSurfaceViewTimeScaleDown.setZOrderOnTop(true);
        mSurfaceViewTimeScaleDown.setOnTouchListener(mSvTimeScaleOnTouchListener);

        mBVPOffline = new MyPlotterBloodVelocity(mSVUltrasoundOffline, this, mSVScaleOffline);

        mSurfaceViewTimeScaleDownSpace = rootView.findViewById(R.id.svTimeScaleDownSpaceOffline);
        mSurfaceViewTimeScaleDownSpace.setZOrderOnTop(true);
        mSurfaceViewTimeScaleUp = rootView.findViewById(R.id.svTimeScaleUpOffline);
        mSurfaceViewTimeScaleUp.setZOrderOnTop(true);
        mSurfaceViewTimeScaleUpSpace = rootView.findViewById(R.id.svTimeScaleUpSpaceOffline);
        mSurfaceViewTimeScaleUpSpace.setZOrderOnTop(true);
    }

    private void showECGView(boolean isVisible){
        if (isVisible){
            mECGOffline.setVisibility(View.VISIBLE);
            mVECGLeftSpace.setVisibility(View.VISIBLE);
        }else {
            mECGOffline.setVisibility(View.GONE);
            mVECGLeftSpace.setVisibility(View.GONE);
        }


    }

    /*private void resetSurfaceView(){
        mSVUltrasoundOffline.setVisibility(View.GONE);
        mSVScaleOffline.setVisibility(View.GONE);
        mSVSpaceScaleOffline.setVisibility(View.GONE);

        mSVUltrasoundOffline.setVisibility(View.VISIBLE);
        mSVScaleOffline.setVisibility(View.VISIBLE);
        mSVSpaceScaleOffline.setVisibility(View.VISIBLE);
    }*/

    public void switchPlayPauseBtn(boolean isPlaying){
        if (isPlaying){
            mPlaySoundBtn.setVisibility(View.GONE);
            mPauseSoundBtn.setVisibility(View.VISIBLE);
        }else {
            mPlaySoundBtn.setVisibility(View.VISIBLE);
            mPauseSoundBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        /*if (!openFilePath.equalsIgnoreCase("")){
        }*/

        if (!inputFileName.equals("")){
            String mStrBaseFolder = Utilitys.getUserBaseFilePath(mActivity,UserManagerCommon.mUserInfoCur.userID);
            Log.d(TAG, mStrBaseFolder);
            if (openFilePath.equals("")){
                openFilePath = mStrBaseFolder + File.separator +inputFileName;
            }
            if (!currentFilename.equals("")){
                mDataInfoTextView.setText(currentFilename);
            }else {
                mDataInfoTextView.setText(inputFileName);
            }
            enableBloodVelocityStartFromFileAction(openFilePath);
        }
    }



    @Override
    public void onPause() {
        if (MainActivity.mAudioPlayerController.isPlaying){
            MainActivity.mAudioPlayerController.closeAudio();
            switchPlayPauseBtn(false);
        }
        needReDrawScale = true;
        super.onPause();
    }

    private final View.OnClickListener btnSoundPauseListener = view -> {

        MainActivity.mAudioPlayerController.closeAudio();
        switchPlayPauseBtn(false);
    };

    private final View.OnClickListener btnSoundOnClick = view -> {

        if(!mBoolOfflineSoundDataExist){
            return;
        }
        switchPlayPauseBtn(true);

        try {
//            new playAudioTask().execute();

            new Thread(()-> {
                MainActivity.mAudioPlayerController.openAudio(SystemConfig.mIntUltrasoundSamplerate);
                MainActivity.mAudioPlayerController.dataToSoundByAllSegment(MainActivity.mRawDataProcessor.mByteArrayWavDataOffLine, MainActivity.mRawDataProcessor.mByteArrayWavDataOffLine.length);
                MainActivity.mAudioPlayerController.closeAudio();

                mActivity.runOnUiThread(() ->{
                    switchPlayPauseBtn(false);
                });
            }).start();
        } catch (Exception ex1) {
            ex1.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugStr("Fragment.btnSoundOnClick.Exception", "");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(), "");
        }
    };

    private final View.OnClickListener btnCalculateOnClick = view -> {
        try {
            if (SystemConfig.mEnumUltrasoundUIState == SystemConfig.ENUM_UI_STATE.ULTRASOUND_UI_STATE_ONLINE) {
                if ((SystemConfig.mEnumTryState == SystemConfig.ENUM_TRY_STATE.STATE_TRY)
                        || (SystemConfig.mEnumStartState == SystemConfig.ENUM_START_STATE.STATE_START)) {
                    return;
                }
            }

            if (MainActivity.mBVSignalProcessorPart2Selected == null) {
                return;
            }

            SystemConfig.mBoolProcessorPart2Selected = true;

            // jaufa, -, 181025
            //MainActivity.mBVSignalProcessorPart2Selected.processResultBloodSignalByRecalculate();
            // Cavin -200903
            //showResultBloodVelocityCommon(MainActivity.mSignalProcessController.getResultDataAfterSignalProcess());
            dataInfo tmpData = MainActivity.mSignalProcessController.getResultDataAfterSignalProcessByWu();
            showResultBloodVelocityCommon(tmpData);

            //?enableBloodVelocityStartFromCalculateAction();
        } catch (Exception ex1) {
            ex1.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugStr("Fragment.btnCalculateOnClick.Exception", "");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(), "");
        }
    };

    public View.OnTouchListener mSvUltrasoundOnTouchListener = new View.OnTouchListener() {
        private float mFloatShiftX,  mFloatShiftY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int iEventAction, iCurSubSeg, iSubSegIdx;
            float fSvHeight;

            //try {
                if ((SystemConfig.mEnumTryState == SystemConfig.ENUM_TRY_STATE.STATE_TRY)
                        || (SystemConfig.mEnumStartState == SystemConfig.ENUM_START_STATE.STATE_START)){
                    return true;
                }

                if(MainActivity.mRawDataProcessor.mIntDataNextIndex == 0){
                    return true;
                }

                iEventAction = event.getAction();
                switch (iEventAction) {
                    case MotionEvent.ACTION_DOWN:
                        mFloatSvOnTouchStartX = event.getX();
                        mFloatSvOnTouchStartY = event.getY();
                        mFloatSvOnTouch2PointX = event.getX();
                        fSvHeight = mSVUltrasoundOffline.getHeight();
                        mBoolSvUltrasoundTouchAtUpper = mFloatSvOnTouchStartY >= (fSvHeight / 2.0);
                        if(!mBoolSvUltrasoundTouchAtUpper) {
                            if (SystemConfig.mIntVTIModeIdx == SystemConfig.INT_VTI_MODE_2_TWO_POINT) {
                                mBVPOffline.setTimeScalePoint(mFloatSvOnTouch2PointX, false, true);
                            }
                        }
                        if (needReDrawScale){
                            //mBVPOffline.drawFreqScale(mSVScaleOffline);
                            mBVPOffline.drawFreqScaleByHolder(mBVPOffline.mSurfaceHolderScale);
                            needReDrawScale = false;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!mBoolSvUltrasoundTouchAtUpper){
                            if (SystemConfig.mIntVTIModeIdx == SystemConfig.INT_VTI_MODE_2_TWO_POINT) {
                                mFloatSvOnTouch2PointX = event.getX();
                                mBVPOffline.setTimeScalePoint(mFloatSvOnTouch2PointX, false, true);
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
                                iCurSubSeg = mBVPOffline.getSubSegIdxFromSurfaceView(mFloatSvOnTouchStartX);
                                if (MainActivity.mBVSignalProcessorPart2Selected != null) {
                                    MainActivity.mBVSignalProcessorPart2Selected.toggleHRPeriodDiscardedState(iCurSubSeg);
                                    mBVPOffline.drawBVSignalByChangeDiscarededState();
                                }
                            }
                            if(SystemConfig.mIntTimeScaleEnabled == SystemConfig.INT_TIME_SCALE_ENABLED_YES){
                                mBVPOffline.setTimeScalePoint(mFloatTimeScalePosCur, true, true);
                                iSubSegIdx = mBVPOffline.getTimeScaleSubSegmentIdx(mFloatTimeScalePosCur);
                                setTimeScaleInfoCur(iSubSegIdx);
                            }
                        }else if(SystemConfig.mIntVTIModeIdx == SystemConfig.INT_VTI_MODE_2_TWO_POINT){
                            mFloatSvOnTouchStartY = event.getY();
                            mFloatSvOnTouch2PointX = event.getX();
                            mBVPOffline.setTimeScalePoint(mFloatSvOnTouch2PointX,false,true);
                            iCurSubSeg = mBVPOffline.getSubSegIdxFromSurfaceView(mFloatSvOnTouch2PointX);
//                            setTimeScaleInfoCur(iSubSegIdxCur);
                            double doubleRatio = (double) mFloatSvOnTouchStartY / (double) mSVUltrasoundOffline.getHeight();
                            if (doubleRatio > 0.5) {
                                if (MainActivity.mBVSignalProcessorPart2Selected != null) {
                                    MainActivity.mBVSignalProcessorPart2Selected.toggleHRPeriodDiscardedState(iCurSubSeg);
                                    mBVPOffline.drawBVSignalByChangeDiscarededState();
                                }
                            } else {
                                GIS_Log.d(TAG,"lastPoint = "+ lastPoint+"");
                                GIS_Log.d(TAG,"iCurSubSeg = "+ iCurSubSeg+"");
                              if (lastPoint != -1) {
                                    if (Math.abs(iCurSubSeg - lastPoint)<90){
                                        if (lastPoint > iCurSubSeg){
                                            twoPointStart = iCurSubSeg;
                                            twoPointEnd = lastPoint;
                                        }else{
                                            twoPointStart = lastPoint;
                                            twoPointEnd = iCurSubSeg;
                                        }
                                        showResultBloodVelocity(BVSignalProcessController.getResultFromTwoPointWu(twoPointStart,twoPointEnd));
                                        //Doppler.exportIPC(mActivity);
                                    }
                                }
                                lastPoint = iCurSubSeg;
                                MainActivity.mBVSignalProcessorPart2Selected.changeVTIScopeBy2PointsSel(iCurSubSeg);
                                mBVPOffline.drawBVSignalByChangeDiscarededState();

                            }
                        }
                        v.performClick();
                        break;
                    default:
                        break;
                }
            //}catch(Exception ex1){
                //SystemConfig.mMyEventLogger.appendDebugStr("mSvUltrasoundOnTouch.Exception", "");
             //   ex1.printStackTrace();
            //}
            return true;

        }




        protected void MoveActionAfterCorrect() {
            int iDrawStartIdx, iDrawSize;

            try {
                iDrawStartIdx = SystemConfig.mIntSVDrawStartBloodVelocity;
                iDrawSize = SystemConfig.mIntSVDrawSizeBloodVelocity;

                /*UiManager.mIntYMultiFactor = Integer.parseInt(SystemConfig.mFragment.mEditTextYMultiValue.getText().toString());*/
                if (SystemConfig.mEnumUltrasoundSubUIState == SystemConfig.ENUM_SUB_UI_STATE.BLOOD_FLOW_VELOCITY) {
                    mBVPOffline.DrawBySubSegOffLine(iDrawStartIdx, iDrawSize);
                } else {
                    mBVPOffline.DrawBySubSegOffLine(iDrawStartIdx, iDrawSize);
                    //mStrokeVolumePlotter.offlineDrawBySubSeg(iDrawStartIdx, iDrawSize);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        protected void MoveNextOnTouchAction(float fMoveX) {
            //int iDrawStartIdx, iDrawSize;
            float fSvXMaxSize, fSvSubSegMaxSize;
            int iMoveSubSegs;

            //try {

                fSvXMaxSize = mSVUltrasoundOffline.getWidth();
                //fSvSubSegMaxSize = (float) SystemConfig.mFragment.mBloodVelocitySignalProcessor.getCurSubSegSize();
                fSvSubSegMaxSize = (float) SystemConfig.mIntSVDrawSizeBloodVelocity;
                iMoveSubSegs = (int) ((fSvSubSegMaxSize * fMoveX) / fSvXMaxSize);

                correctMoveNextParamWithParam(iMoveSubSegs);
                //correctYMultiParam();
                MoveActionAfterCorrect();

            //} catch (Exception e) {

            //}
        }


        protected void MoveBackOnTouchAction(float fMoveX) {
            int iDrawStartIdx, iDrawSize;
            float fSvXMaxSize, fSvSubSegMaxSize;
            int iMoveSubSegs;

            //try {
                fSvXMaxSize = mSVUltrasoundOffline.getWidth();
                fSvSubSegMaxSize = (float) SystemConfig.mIntSVDrawSizeBloodVelocity;
                iMoveSubSegs = (int) ((fSvSubSegMaxSize * fMoveX) / fSvXMaxSize);

                correctMoveBackParamWithParam(iMoveSubSegs);
                //correctYMultiParam();
                MoveActionAfterCorrect();

            //} catch (Exception e) {

           // }
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



    public View.OnTouchListener mSvTimeScaleOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int iEventAction;

           // try {

                if (SystemConfig.mEnumUltrasoundSubUIState != SystemConfig.ENUM_SUB_UI_STATE.BLOOD_FLOW_VELOCITY) {
                    return true;
                }

                if ((SystemConfig.mEnumTryState == SystemConfig.ENUM_TRY_STATE.STATE_TRY)
                        || (SystemConfig.mEnumStartState == SystemConfig.ENUM_START_STATE.STATE_START)){
                    return true;
                }

                iEventAction = event.getAction();
                switch (iEventAction) {
                    case MotionEvent.ACTION_DOWN:
                        mFloatSvOnTouchStartX = event.getX();
                        mFloatSvOnTouchStartY = event.getY();
                        //putUiMsg(UI_MSG_ID_TIME_SCALE_TOUCH_DOWN);
                        processTimeScaleTouchEvent(MainActivity.UI_MSG_ID_TIME_SCALE_TOUCH_DOWN);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mFloatSvOnTouchStartX = event.getX();
                        mFloatSvOnTouchStartY = event.getY();
                        //putUiMsg(UI_MSG_ID_TIME_SCALE_TOUCH_MOVE);
                        processTimeScaleTouchEvent(MainActivity.UI_MSG_ID_TIME_SCALE_TOUCH_MOVE);
                        break;
                    case MotionEvent.ACTION_UP:
                        mFloatSvOnTouchStartX = event.getX();
                        mFloatSvOnTouchStartY = event.getY();
                        //putUiMsg(UI_MSG_ID_TIME_SCALE_TOUCH_UP);
                        processTimeScaleTouchEvent(MainActivity.UI_MSG_ID_TIME_SCALE_TOUCH_UP);
                        v.performClick();
                        break;
                    default:
                        break;
                }
                return true;
            //}catch(Exception ex1){
                //SystemConfig.mMyEventLogger.appendDebugStr("mSvTimeScaleOnTouch.Exception","");
            //    ex1.printStackTrace();
            //    return true;
            //}
        }
    };

    private void processTimeScaleTouchEvent(int iEventAction){
        int iSubSegIdxCur;

        try {

            if(iEventAction == MainActivity.UI_MSG_ID_TIME_SCALE_TOUCH_DOWN) {
                mBVPOffline.drawBVSvByTimeScaleTouchDown(mFloatSvOnTouchStartX);
                iSubSegIdxCur = mBVPOffline.getTimeScaleSubSegmentIdx(mFloatTimeScalePosCur);
                setTimeScaleInfoCur(iSubSegIdxCur);
            }else if(iEventAction == MainActivity.UI_MSG_ID_TIME_SCALE_TOUCH_MOVE) {
                mBVPOffline.drawBVSvByTimeScaleTouchMove(mFloatSvOnTouchStartX);
                iSubSegIdxCur = mBVPOffline.getTimeScaleSubSegmentIdx(mFloatSvOnTouchStartX);
                setTimeScaleInfoCur(iSubSegIdxCur);
            }else if(iEventAction == MainActivity.UI_MSG_ID_TIME_SCALE_TOUCH_UP) {
                mBVPOffline.drawBVSvByTimeScaleTouchUp(mFloatSvOnTouchStartX);
                iSubSegIdxCur = mBVPOffline.getTimeScaleSubSegmentIdx(mFloatSvOnTouchStartX);
                setTimeScaleInfoCur(iSubSegIdxCur);
            }

        }catch(Exception ex1){
            //SystemConfig.mMyEventLogger.appendDebugStr("procTimeScaleTouchEvent.Exception","");
            ex1.printStackTrace();
        }
    }

    // initial offline plot parameter and UI component result value.
    private void prepareStart(){
        SystemConfig.mIntSVDrawStartBloodVelocity = 0;
        initResultValue();
    }

    private void initResultValue(){
        mHRValueTextView.setText("--");
        mCOValueTextView.setText("--");
        mSVValueTextView.setText("--");
        if (!SystemConfig.isYuhul){
            mVTIValueTextView.setText("--");
            mVpkValueTextView.setText("--");
        }

    }

    private void exportDirSingleMode(List<dataInfoTestMode> inputList,String inputPath){

        String exportDirPath = inputPath;
        File exportDir = new File(exportDirPath,"");
        if (!exportDir.exists()){
            exportDir.mkdir();
        }
        SimpleDateFormat df;

        //df = new SimpleDateFormat("yyyyMMdd_HHmmssSSS");
        df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String strDate = df.format(new Date());
        String filename = inputList.get(0).userId+"_"+ strDate +".csv";
        File exportFile = new File(exportDir,filename);
        try {
            if (exportFile.createNewFile()){
                GIS_Log.d(TAG,"Create file "+filename+" successfully.");
                CSVWriter csvWriter = new CSVWriter(new FileWriter(exportFile));


                SQLiteDatabase db = mHelper.getReadableDatabase();
                Cursor curCSV = db.rawQuery("select *  from "+IwuSQLHelper.STR_TABLE_DATA+" where userID = ?",new String[] {UserManagerCommon.mUserInfoCur.userID});
                //csvWriter.writeNext(curCSV.getColumnNames());
                String[] strTitle = {   "_id ",
                                        "Result ",
                                        "UseID ",
                                        "CreateDate",
                                        "Vpk_Calibration ",
                                        "Vpk_Theory ",
                                        "Vpk_Raw ",
                                        "SNR ",
                                        "SignalBase ",
                                        "NoiseBase ",
                                        "FileName " };
                csvWriter.writeNext(strTitle);
                for (int i=0;i<inputList.size();i++){
                    // while (curCSV.moveToNext()){
                    String[] arrStr = {""+i,
                            inputList.get(i).result,
                            inputList.get(i).userId,
                            inputList.get(i).createdDate,
                            ""+inputList.get(i).Vpk_Calibration,
                            ""+inputList.get(i).Vpk_Theory,
                            ""+inputList.get(i).Vpk_Raw,
                            ""+inputList.get(i).SNR,
                            ""+inputList.get(i).SignalBase,
                            ""+inputList.get(i).NoiseBase,
                            inputList.get(i).fileName};
                    csvWriter.writeNext(arrStr);
                }
                csvWriter.close();
                curCSV.close();

                //showAlertDialog(getString(R.string.find_value_success_toast),
                //        getString(R.string.alert_msg_save_csv_to)+exportFile.getAbsolutePath());

            }else{
                GIS_Log.d(TAG,"file "+filename+" existed.");
                //showAlertDialog(getString(R.string.alert_title_failure),null);
            }

        }catch(Exception sqlEx){
            sqlEx.printStackTrace();
        }

    }
    private void exportDir(List<dataInfo> inputList,String inputPath){

        String exportDirPath = inputPath;
        File exportDir = new File(exportDirPath,"");
        if (!exportDir.exists()){
            exportDir.mkdir();
        }
        SimpleDateFormat df;

        //df = new SimpleDateFormat("yyyyMMdd_HHmmssSSS");
        df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String strDate = df.format(new Date());
        String filename = inputList.get(0).userId+"_"+ strDate +".csv";
        File exportFile = new File(exportDir,filename);
        try {
            if (exportFile.createNewFile()){
                GIS_Log.d(TAG,"Create file "+filename+" successfully.");
                CSVWriter csvWriter = new CSVWriter(new FileWriter(exportFile));


                SQLiteDatabase db = mHelper.getReadableDatabase();
                Cursor curCSV = db.rawQuery("select *  from "+IwuSQLHelper.STR_TABLE_DATA+" where userID = ?",new String[] {UserManagerCommon.mUserInfoCur.userID});
                //csvWriter.writeNext(curCSV.getColumnNames());
                //jaufa, +, 190102, Template
                String[] strTitle = {   "_id ",
                        "Result ",
                        "UseID ",
                        "CreateDate ",
                        "HR ",
                        "Vpk ",
                        "VTI ",
                        "SV ",
                        "CO ",
                        "ErrCode ",
                        "FileName ",
                        "tmpValue"};
                csvWriter.writeNext(strTitle);
                for (int i=0;i<inputList.size();i++){
               // while (curCSV.moveToNext()){
                    String[] arrStr = {""+i,
                            inputList.get(i).result,
                            inputList.get(i).userId,
                            inputList.get(i).createdDate,
                            ""+inputList.get(i).HR,
                            ""+inputList.get(i).Vpk,
                            ""+inputList.get(i).VTI,
                            ""+inputList.get(i).SV,
                            ""+inputList.get(i).CO,
                            ""+inputList.get(i).ErrCode,
                            ""+inputList.get(i).fileName,
                            ""+inputList.get(i).tmpValue};
                    csvWriter.writeNext(arrStr);
                }
                csvWriter.close();
                curCSV.close();

                //showAlertDialog(getString(R.string.find_value_success_toast),
                //        getString(R.string.alert_msg_save_csv_to)+exportFile.getAbsolutePath());

            }else{
                GIS_Log.d(TAG,"file "+filename+" existed.");
                //showAlertDialog(getString(R.string.alert_title_failure),null);
            }

        }catch(Exception sqlEx){
            sqlEx.printStackTrace();
        }

    }

    public String getCreateDateStrFromFilename(String inputFileName){
        String outputDate = "";
        SimpleDateFormat simpleDateFormat,df;

        df = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.getDefault());
        simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        if (inputFileName.length()<18){
            return outputDate;
        }
        String dateStr = inputFileName.substring(3,18);
        try {
            Date inputDate = df.parse(dateStr);
            assert inputDate != null;
            outputDate = simpleDateFormat.format(inputDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return outputDate;
    }

    public dataInfoTestMode getResultFromFileSingleMode(String inputPath){
        boolean boolSuccess;
        dataInfoTestMode resultData = new dataInfoTestMode();
        try {
            if(checkFileTypeAndInit(inputPath)) {
                MainActivity.mRawDataProcessor.prepareStartOffLine();
                boolSuccess = MainActivity.mRawDataProcessor.setDataFromWavFile(inputPath);

                if (boolSuccess) {
                    MainActivity.mSignalProcessController.processSignalAfterDataRxOffLine();
                    //MainActivity.mBVSignalProcessorPart1.processMaxIdxByMovingAverageForSingleVpk();

//                    if (SystemConfig.mTestMode){
                        // Cavin Added to show SNR  for testing 2018/08/08
//                        String SNRStr = "SNR="+ String.format("%.1f", SystemConfig.mDoubleSNRLearned)+
//                                "\nNoiseBase="+ String.valueOf(SystemConfig.mDoubleNoiseBaseLearned)+
//                                "\nSignalBase="+ String.valueOf(SystemConfig.mDoubleSignalBaseLearned);
//                        mSNRTextView.setText(SNRStr);
//                    }

                    dataInfo tmpData = MainActivity.mSignalProcessController.getResultDataAfterSignalProcess();

                    //resultData.Vpk_Calibration = MainActivity.mBVSignalProcessorPart2Selected.mDoubleVpkMeterAverageAfterAngleAfterCali;

                    //resultData.Vpk_Theory = MainActivity.mBVSignalProcessorPart2Selected.mDoubleVpkMeterAverageOri;
                    resultData.Vpk_Theory = tmpData.Vpk;
                    resultData.Vpk_Raw = SystemConfig.mDoubleSingleVpkAvg / MainActivity.mBVSignalProcessorPart1.mDoubleFreqGap;
                    resultData.SNR = SystemConfig.mDoubleSNRLearned;
                    resultData.NoiseBase = SystemConfig.mDoubleNoiseBaseLearned;
                    resultData.SignalBase = SystemConfig.mDoubleSignalBaseLearned;


                }

            }else{
                resultData = null;
            }

        } catch (Exception ex1) {
            ex1.printStackTrace();
            resultData = null;
        }
        return resultData;
    }

    public dataInfo getResultFromFile(String inputPath){
        boolean boolSuccess;
        dataInfo resultData;
        try {
            if(checkFileTypeAndInit(inputPath)) {
                MainActivity.mRawDataProcessor.prepareStartOffLine();
                boolSuccess = MainActivity.mRawDataProcessor.setDataFromWavFile(inputPath);

                if (boolSuccess) {

                    MainActivity.mSignalProcessController.processSignalAfterDataRxOffLine();

                    if (SystemConfig.mTestMode){
                        // Cavin Added to show SNR  for testing 2018/08/08
//                        String SNRStr = "SNR="+ String.format("%.1f", SystemConfig.mDoubleSNRLearned)+
//                                "\nNoiseBase="+ String.valueOf(SystemConfig.mDoubleNoiseBaseLearned)+
//                                "\nSignalBase="+ String.valueOf(SystemConfig.mDoubleSignalBaseLearned);
//                        mSNRTextView.setText(SNRStr);
                    }

                }
                // For Wu algorithm test 20201022
                resultData = MainActivity.mSignalProcessController.getResultDataAfterSignalProcessWu201022();
                //resultData = MainActivity.mSignalProcessController.getResultDataAfterSignalProcess();
            }else{
                resultData = null;
            }

        } catch (Exception ex1) {
            ex1.printStackTrace();
            resultData = null;
        }
        return resultData;
    }

    public void enableBloodVelocityStartFromFileAction(String inputPath){
        boolean boolSuccess;

        mBoolOfflineSoundDataExist = true;
        try {
            if(checkFileTypeAndInit(inputPath)) {
                MainActivity.mRawDataProcessor.prepareStartOffLine();
                /*boolSuccess = mOffLineRawDataUiManager.getRawDataFromFile();*/
                if (MainActivity.mRawDataProcessor.isFileTxtType(inputPath)){
                    hasECG = true; //ECG心電圖
                    showECGView(true);
                    boolSuccess = MainActivity.mRawDataProcessor.setDataFromTxtFile(inputPath);
                }else {
                    hasECG = false;
                    showECGView(false);
                    boolSuccess = MainActivity.mRawDataProcessor.setDataFromWavFile(inputPath);
                }

                if (boolSuccess) {

                    MainActivity.mSignalProcessController.processSignalAfterDataRxOffLine();

                    prepareStart();
                    mBVPOffline.prepareStartOffLine();
                    //showResultBloodVelocity(MainActivity.mSignalProcessController.getResultDataAfterSignalProcess());

                    //Leslie
                    //showResultBloodVelocity(MainActivity.mSignalProcessController.getResultDataAfterSignalProcessWu201022());
                    showResultBloodVelocity(new GIS_DataController().getMeasureResult());
                    //Leslie End_20230308

                    if (SystemConfig.mTestMode){
                        // Cavin Added to show SNR  for testing 2018/08/08
                        String SNRStr = "SNR="+ String.format(Locale.US,"%.1f", SystemConfig.mDoubleSNRLearned)+
                                "\nNoiseBase="+ SystemConfig.mDoubleNoiseBaseLearned +
                                "\nSignalBase="+ SystemConfig.mDoubleSignalBaseLearned;
                        mSNRTextView.setText(SNRStr);

                        mFilterSwitch.setEnabled(true);
                        mWindowSwitch.setEnabled(true);
                        mDCOffsetSwitch.setEnabled(true);
                    }

                    // set UI status.
//                    calculateButton.setEnabled(true);
                    calculateButton.setVisibility(View.INVISIBLE);
                    mVTISwitch.setEnabled(true);
                    mPlaySoundBtn.setEnabled(true);

                }
            }

        } catch (Exception ex1) {
            ex1.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugStr("Fragment.enableBVStartFromFileAction.Exception", "");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(), "");
        }
    }

    private void updateBloodVelocityData(dataInfo inputData){
        ContentValues cv = new ContentValues();
        String strID, strWhere;
        //cv.put(IwuSQLHelper.KEY_DATA_TABLE_USER_ID, inputData.userId);
        //cv.put(IwuSQLHelper.KEY_DATA_TABLE_CREATED_DATE, inputData.createdDate);
        cv.put(IwuSQLHelper.KEY_DATA_TABLE_HR, inputData.HR);
        cv.put(IwuSQLHelper.KEY_DATA_TABLE_RESULT, inputData.result);
        cv.put(IwuSQLHelper.KEY_DATA_TABLE_VPK, inputData.Vpk);
        cv.put(IwuSQLHelper.KEY_DATA_TABLE_VTI, inputData.VTI);
        cv.put(IwuSQLHelper.KEY_DATA_TABLE_SV, inputData.SV);
        cv.put(IwuSQLHelper.KEY_DATA_TABLE_CO, inputData.CO);
        //cv.put(IwuSQLHelper.KEY_DATA_TABLE_FILE_NAME, inputFileName);

        strID = String.valueOf(inputID);
        strWhere = "_id="+strID;
        GIS_Log.d(TAG,"strWhere="+strWhere);
        mHelper.mDBWrite.beginTransaction();
        mHelper.mDBWrite.update(IwuSQLHelper.STR_TABLE_DATA,cv,strWhere,null);
        mHelper.mDBWrite.setTransactionSuccessful();
        mHelper.mDBWrite.endTransaction();
    }


    protected void showResultBloodVelocity(dataInfo inputData) {

        correctShowParam();
        showResultBloodVelocityCommon(inputData);

        if (MainActivity.mBVSignalProcessorPart2Selected != null) {
            mBVPOffline.DrawBySubSegOffLine(SystemConfig.mIntSVDrawStartBloodVelocity, SystemConfig.mIntSVDrawSizeBloodVelocity);
        }
    }

    // 最後秀出資料的function
    protected void showResultBloodVelocityCommon(dataInfo inputData) {
        final int iDrawStartIdx, iDrawSize, iHR;
        double doubleCardiacOutput, doubleHR, doubleRadius;
        String strHR, strPeakVelocity, strVTI, strStrokeVolume, strCardiacOutput, strCSArea, strRemark;
        String strVpk2, strVTI2, strSV2, strCO2, strVpkOri, strVTIOri, strSVOri, strCOOri;
        BigDecimal bigDecimal;
        double doubleVpkAngle=0.0, doubleVpkAfterAngleAfterCali, doubleVpkOri;
        double doubleVtiAngle, doubleVtiAfterAngleAfterCali, doubleVtiOri;
        double doubleSVAngle, doubleSVAfterAngleAfterCali, doubleSVOri;
        double doubleCOAngle, doubleCOAfterAngleAfterCali, doubleCOOri;

        try {
            iDrawStartIdx = SystemConfig.mIntSVDrawStartBloodVelocity;
            iDrawSize = SystemConfig.mIntSVDrawSizeBloodVelocity;
            //userInfoSettingUiManager = new UserInfoSettingUiManager(SystemConfig.mFragment);

            //------------------------------------------------------------------
            // show common data
            //------------------------------------------------------------------

            /*strRemark = "[Vpk Remark 1] : " + " Doppler Angle = " + String.valueOf(UserManagerCommon.mUserInfoCur.mIntAngle) + " " + "Deg.";
            SystemConfig.mFragment.mTextViewVpkRemark1.setText(strRemark);

            bigDecimal = new BigDecimal(UserManagerCommon.mDoubleCosineUserAngle);
            bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);// 小數後面1位, 四捨五入
            strRemark = "[Vpk Remark 2] : " + "Cosine(" + String.valueOf(UserManagerCommon.mUserInfoCur.mIntAngle) + ") = " + bigDecimal.doubleValue();
            SystemConfig.mFragment.mTextViewVpkRemark2.setText(strRemark);

            UiManager.updateShowDebugBloodVelocitySpinner();
*/
            //------------------------------------------------------------------
            // check if single vpk
            //------------------------------------------------------------------
            if (SystemConfig.mIntSingleVpkEnabled == SystemConfig.INT_SINGLE_VPK_ENABLED_YES) {
//jaufa, -, '181121                MainActivity.mBVSignalProcessorPart1.processMaxIdxByMovingAverageForSingleVpk();
                if (MainActivity.mBVSignalProcessorPart2Selected != null) {
                    if (SystemConfig.mEnumUltrasoundUIState == SystemConfig.ENUM_UI_STATE.ULTRASOUND_UI_STATE_OFFLINE) {
                        mBVPOffline.DrawBySubSegOffLine(iDrawStartIdx, iDrawSize);
                    }
                }
                showBloodVelocityForSingleVpk(inputData);
                return;
            }
            //------------------------------------------------------------------
            // check if data available
            //------------------------------------------------------------------
            if (!SystemConfig.mBoolProcessorPart2Selected) {
                GIS_Log.d(TAG,"SystemConfig.mBoolProcessorPart2Selected == false");
                initResultValue();
                mBVPOffline.DrawBySubSegOffLine(iDrawStartIdx, iDrawSize);
                return;
            }

            //------------------------------------------------------------------
            // for Heart Rate
            //------------------------------------------------------------------
            //iHR = (int) MainActivity.mBVSignalProcessorPart2Selected.getHRAverage();
            iHR = inputData.HR;
//            if (iHR < 0) {
            /* 當SV & CO顯示「--」，把HR也隱藏成「--」 2024/3/28 by Doris*/
            if (iHR < 0 || inputData.SV <= 0 || inputData.CO <= 0) {
                mHRValueTextView.setText("--");
            } else {
                strHR = String.valueOf(iHR) + " ";
                mHRValueTextView.setText(strHR);
            }

            //----------------------------------------------------------------------------------
            // for Vpk
            //-----------------------------------------------------------------------------------
            //if (MainActivity.mBVSignalProcessorPart2Selected.getPeakVelocityAverage() <= 0) {
            if (!SystemConfig.isYuhul || SystemConfig.mTestMode){
                if(inputData.Vpk <= 0){
                    mVpkValueTextView.setText("--");
                } else {
                    //doubleVpkAngle = MainActivity.mBVSignalProcessorPart2Selected.mDoubleVpkMeterAverageAfterUserAngle;
                    //doubleVpkAfterAngleAfterCali = MainActivity.mBVSignalProcessorPart2Selected.mDoubleVpkMeterAverageAfterAngleAfterCali;
                    doubleVpkAfterAngleAfterCali = inputData.Vpk;
                    //doubleVpkOri = MainActivity.mBVSignalProcessorPart2Selected.mDoubleVpkMeterAverageOri;

                    //strPeakVelocity = String.format("%.2f", doubleVpkAngle);
                    //strVpkOri = String.format("%.2f", doubleVpkOri);
                    //if(SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_0_SNSI_GM) {
                    //    strVpk2 = String.format("%.2f", doubleVpkAfterAngleAfterCali * 2.0);
                    // }else {
                    strVpk2 = String.format(Locale.US,"%.2f", doubleVpkAfterAngleAfterCali);
                    //}
                    mVpkValueTextView.setText(strVpk2);
                }
            }


            //---------------------------------------------------------------
            // for VTI
            //---------------------------------------------------------------
            //if(MainActivity.mBVSignalProcessorPart2Selected.getVTIAverage() <= 0) {
            if (!SystemConfig.isYuhul || SystemConfig.mTestMode){
                if(inputData.VTI <= 0) {
                    mVTIValueTextView.setText("--");
                }else {
                    //doubleVtiAngle =  MainActivity.mBVSignalProcessorPart2Selected.mDoubleVtiCmAverageAfterUserAngle;
                    //doubleVtiAfterAngleAfterCali = MainActivity.mBVSignalProcessorPart2Selected.mDoubleVtiCmAverageAfterAngleAfterCali;
                    doubleVtiAfterAngleAfterCali = inputData.VTI;
                    //doubleVtiOri = MainActivity.mBVSignalProcessorPart2Selected.mDoubleVtiCmAverageOri;

                    //strVTI = String.format("%.2f", doubleVtiAngle);
                    //if(SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_0_SNSI_GM) {
                    //    strVTI2 = String.format("%.2f", doubleVtiAfterAngleAfterCali * 2.0);
                    //}else {
                    strVTI2 = String.format(Locale.US,"%.2f", doubleVtiAfterAngleAfterCali);
                    //}
                    //strVTIOri = String.format("%.2f", doubleVtiOri);
                    mVTIValueTextView.setText(strVTI2);
                }
            }

            //-------------------------------------------------
            // for Stroke Volume
            //--------------------------------------------------
            //if(MainActivity.mBVSignalProcessorPart2Selected.mDoubleStrokeVolumeAverage <= 0) {
            if(inputData.SV <= 0) {
                /*SystemConfig.mFragment.mTextViewSVValue.setText(mStrFailed);*/
                mSVValueTextView.setText("--");
            }else {
                //doubleSVAngle = MainActivity.mBVSignalProcessorPart2Selected.mDoubleStrokeVolumeAverageAfterUserAngle;
                //doubleSVAfterAngleAfterCali = MainActivity.mBVSignalProcessorPart2Selected.mDoubleStrokeVolumeAverageAfterAngleAfterCali;
                doubleSVAfterAngleAfterCali = inputData.SV;
                //doubleSVOri = MainActivity.mBVSignalProcessorPart2Selected.mDoubleStrokeVolumeAverageOri;

                //strStrokeVolume = String.format("%.2f", doubleSVAngle);
                //strSVOri = String.format("%.2f", doubleSVOri);
                //if(SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_0_SNSI_GM) {
                //    strSV2 = String.format("%.2f", doubleSVAfterAngleAfterCali * 2.0);
                //}else{
                    strSV2 = String.format(Locale.US,"%.2f", doubleSVAfterAngleAfterCali);
                //}
                mSVValueTextView.setText(strSV2);
            }

            if(UserManagerCommon.mDoubleUserPulmDiameterCm <= 0.0) {
                /*SystemConfig.mFragment.mTextViewSVRemark2Value.setText("0");*/
            }else {
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
            if(inputData.CO <= 0) {
                /*SystemConfig.mFragment.mTextViewCOValue.setText(mStrFailed);*/
                mCOValueTextView.setText("--");
            }else {
                //doubleCOAngle = MainActivity.mBVSignalProcessorPart2Selected.mDoubleCOAverageAfterUserAngle;
                //doubleCOAfterAngleAfterCali = MainActivity.mBVSignalProcessorPart2Selected.mDoubleCOAverageAfterAngleAfterCali;
                doubleCOAfterAngleAfterCali = inputData.CO;
                //doubleCOOri = MainActivity.mBVSignalProcessorPart2Selected.mDoubleCOAverageOri;

                //strCardiacOutput =  String.format("%.2f", doubleCOAngle);
                //strCOOri = String.format("%.2f", doubleCOOri);
                //if(SystemConfig.mIntVpkAlgorithm == SystemConfig.INT_VPK_ALGORITHM_0_SNSI_GM) {
                //    strCO2 = String.format("%.2f", doubleCOAfterAngleAfterCali * 2.0);
                //}else{
                    strCO2 = String.format(Locale.US,"%.2f", doubleCOAfterAngleAfterCali);
                //}
                mCOValueTextView.setText(strCO2);
            }
            rxEditText.setText(""+SystemConfig.rxAngle);
            //-------------------------------------------------
            // for Debug Information
            //--------------------------------------------------
            if (MainActivity.mBVSignalProcessorPart2Selected != null) {
                if (SystemConfig.mEnumUltrasoundUIState == SystemConfig.ENUM_UI_STATE.ULTRASOUND_UI_STATE_OFFLINE) {
                    //mBVPOffline.DrawBySubSegOffLine(iDrawStartIdx, iDrawSize);
                }
            }
        } catch (Exception ex1) {
            ex1.printStackTrace();
            //SystemConfig.mMyEventLogger.appendDebugStr("UiManBV.showResultBVCommon.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
        }
    }

    /*private static void putUiMsg(int iMsgId){
        Message uiMessage;

        uiMessage = new Message();
        uiMessage.what = iMsgId;
        mHandlerUiMsg.sendMessage(uiMessage);
    }*/

    private void correctShowParam() {
        int iDrawStartIdx = 0, iDrawSize = SystemConfig.mIntSVDrawSizeBloodVelocity;
        int iCurSubSegs;

        try {
            iDrawStartIdx = SystemConfig.mIntSVDrawStartBloodVelocity;
            iCurSubSegs = MainActivity.mBVSignalProcessorPart1.getCurSubSegSize();
            if (iDrawStartIdx >= iCurSubSegs) {
                iDrawStartIdx = 0;
            }
            if (iDrawSize == 0) {
                iDrawSize = SystemConfig.mIntSVDrawSizeBloodVelocity;
            }
        } catch (Exception ex1) {
            ex1.printStackTrace();
            iDrawStartIdx = 0;
            iDrawSize = SystemConfig.mIntSVDrawSizeBloodVelocity;
        } finally {
            correctPointsParam(iDrawStartIdx, iDrawSize);
        }
    }

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

    public boolean checkFileTypeAndInit(String inputFile) {
        String strNoPathFileName;
        SystemConfig.ENUM_DEVICE_TYPE enumDeviceType;
        strNoPathFileName = inputFile.substring(inputFile.lastIndexOf('/') + 1);
        //strNoPathFileName = SystemConfig.mFragment.mSpinnerFileList.getSelectedItem().toString();

        /*SystemConfig.mFragment.mStrSelectedFile = inputFile;//MainActivity.mRawDataProcessor.mStrBaseFolder + File.separator + strNoPathFileName;*/
        if (MainActivity.mRawDataProcessor.isFileWaveType(strNoPathFileName)) {
            enumDeviceType = MainActivity.mRawDataProcessor.checkDeviceType(strNoPathFileName);
            if (enumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_LE_ONLINE) {
                SystemConfig.initItriDevice8KOnlineLE();
            }else if (enumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_BE_ONLINE) {
                SystemConfig.initItriDevice8KOnlineBE();
            }else if (enumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_8K_OFFLINE) {
                SystemConfig.initItriDevice8KOffline();
            }else if (enumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.ITRI_44K) {
                SystemConfig.initItriDevice44K();
            }else if (enumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.USCOM_8K) {
                SystemConfig.initUScomDevice8K();
            }else if (enumDeviceType == SystemConfig.ENUM_DEVICE_TYPE.USCOM_44K) {
                SystemConfig.initUScomDevice44K();
            } else {
                SystemConfig.initItriDevice44K();
            }
            GIS_Log.d(TAG, String.valueOf(enumDeviceType));
            return true;
        }else if (MainActivity.mRawDataProcessor.isFileTxtType(strNoPathFileName)){
            GIS_Log.d(TAG,"It is a text file!!!!!!");
            SystemConfig.initItriDevice8KOnlineBE();
            return true;
        } else{
            return false;
        }
    }

    private void setTimeScaleInfoCur(int iSubSegIdx) {
        Calendar calendar;
        SimpleDateFormat df;
        String strDate, strDate2, strMonth;
        long longMiniSecGap;
        int iMonth, iDay;

//        try {
            /*longMiniSecGap = (MainActivity.mBVSignalProcessorPart1.mIntSTFFTNextSubSegIdx - iSubSegIdx) * 8;   // i subseg = 8 minisec

            df = new SimpleDateFormat("yyyyMMdd_HHmmssSSS");
            calendar = Calendar.getInstance();
            calendar.setTimeInMillis(SystemConfig.mLongOpenWavFileDateMiniSecs - longMiniSecGap);
            strDate = df.format(calendar.getTime());
            iMonth = Integer.parseInt(strDate.substring(4, 6)) - 1;
            if (iMonth < 10) {
                strMonth = "0" + String.valueOf(iMonth);
            } else {
                strMonth = String.valueOf(iMonth);
            }
            strDate2 = strDate.substring(0, 4) + strMonth + strDate.substring(6);
            mTextViewTimeScaleValue.setText(strDate2);*/
//        } catch (Exception ex1) {
            //SystemConfig.mMyEventLogger.appendDebugStr("setTimeScaleInfoCur.Exception","");
            //SystemConfig.mMyEventLogger.appendDebugStr(ex1.toString(),"");
//        }
    }

    private void showBloodVelocityForSingleVpk(dataInfo inputData){
        mVpkValueTextView.setText(String.format(
                getResources().getString(R.string.singleVpkResult),inputData.Vpk,inputData.VTI));
    }
}
