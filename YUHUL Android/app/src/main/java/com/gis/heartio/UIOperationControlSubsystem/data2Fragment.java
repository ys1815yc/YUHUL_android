package com.gis.heartio.UIOperationControlSubsystem;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
//import androidx.core.app.Fragment;
//import androidx.core.app.FragmentTransaction;
//import android.support.v7.app.ActionBar;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.gis.CommonUtils.Constants;
import com.gis.heartio.GIS_Log;
import com.gis.heartio.R;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.IwuSQLHelper;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.SystemConfig;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.Utilitys;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.dataInfo;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Utils;
import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.DecoDrawEffect;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import info.hoang8f.android.segmented.SegmentedGroup;

public class data2Fragment extends SampleFragment {
    private static final String TAG = "data2Fragment";
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private List<dataInfo> dataList = new ArrayList<>();
    private List<dataInfo> dataListInPeriod = new ArrayList<>();

    private IwuSQLHelper mHelper;
    private MainActivity mActivity;

    public static final int HR = 0;
    public static final int VPK = 1;
    public static final int VTI = 2;
    public static final int SV = 3;
    public static final int CO = 4;

    private String mParam1;
    private String mParam2;

    protected Typeface mTfLight;

    private int mBackIndex;
    private int mBackVpkIndex;
    private int mBackVtiIndex;
    private int mBackSvIndex;
    private int mBackCoIndex;
    private int mSeriesHRIndex;
    private int mSeriesVpkIndex;
    private int mSeriesVtiIndex;
    private int mSeriesSvIndex;
    private int mSeriesCoIndex;

    private LineChart mChart;
    private int currentDataIndex = 0;
    private int currentDataType = HR;
    private int currentSegmentDays = 1;

    private int mYear, mMonth, mDay;
    private String selectDateStr = null;

    private TextView mDtTextView;

    public static data2Fragment newInstance() {
        return new data2Fragment();
    }

    public data2Fragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mActivity = (MainActivity) getActivity();
        if (mActivity!=null){
            mTfLight = Typeface.createFromAsset(mActivity.getAssets(), "OpenSans-Light.ttf");
        }
        mHelper = new IwuSQLHelper(mActivity);

        // Very important!! for MPAndroid initial.
        Utils.init(getContext());

    }

    @Override
    public void onDestroy() {
        mHelper.closeDatabase();
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView =inflater.inflate(R.layout.fragment_item_data2, container, false);
        if (mActivity!=null){
            mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            mActivity.getSupportActionBar().setDisplayShowHomeEnabled(false);
        }
        mDtTextView = rootView.findViewById(R.id.dtTextView);
        setHasOptionsMenu(true);
        updateActionBarTitle(null);
        /*if (mActivity.getSupportActionBar()!=null){
            //mActivity.getSupportActionBar().setTitle(getString(R.string.title_historical_data));
            TextView customView = (TextView)
                    LayoutInflater.from(mActivity).inflate(R.layout.actionbar_custom_title_view_centered,
                            null);
            ActionBar.LayoutParams params = new
                    ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                    ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
            mActivity.getSupportActionBar().setCustomView(customView, params);
        }*/
        if (dataList.size()==0){
            queryUserDataToList(UserManagerCommon.mUserInfoCur.userID);
            if (dataList.size()>0){

            }

        }

        SegmentedGroup timePeriodSG = rootView.findViewById(R.id.timePeriodSegmented);
        timePeriodSG.check(R.id.dayButton);
        timePeriodSG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                String currentDateStr = getCurrentDateStr();
                if (i == R.id.dayButton){
                    currentSegmentDays = 1;
                } else if (i == R.id.weekButton){
                    currentSegmentDays = 7;
                } else if (i == R.id.monthButton){
                    currentSegmentDays = 30;
                } else {
                    currentSegmentDays = 1;
                }
                if (selectDateStr==null){
                    getPeriodData(currentDateStr,currentSegmentDays,null);
                }else{
                    getPeriodData(selectDateStr,currentSegmentDays,"yyyy/MM/dd");
                }

                setData(currentDataType);
                currentDataIndex = 0;
                setupEvents();
                updateActionBarTitle(selectDateStr);
            }
        });

        mChart = rootView.findViewById(R.id.dataChart);
        mChart.setPinchZoom(false);
        mChart.getAxisRight().setEnabled(false);
        //mChart.getXAxis().setEnabled(false);

        Legend mLegend = mChart.getLegend();
        mLegend.setTextSize(13);
        //mLegend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setEnabled(false);
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xAxis.setTextSize(10f);
        //xAxis.setCenterAxisLabels(true);

        //xAxis.setValueFormatter(new DayAxisValueFormatter(mChart));

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
//        MyMarkerView mv = new MyMarkerView(mActivity, R.layout.custom_marker_view);
//        mv.setChartView(mChart); // For bounds control
//        mChart.setMarker(mv); // Set the marker to the chart
//        xAxis.setGranularity(1f); // one hour
//        xAxis.setValueFormatter(new IAxisValueFormatter() {
//            private SimpleDateFormat mFormat = new SimpleDateFormat("MM/dd HH:mm",Locale.getDefault());
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//                long millis = TimeUnit.HOURS.toMillis((long)value);
//                return mFormat.format(new Date(millis));
//            }
//        });

        mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e==null){
                    return;
                }
                currentDataIndex = (int)e.getX()-1;
                setupEvents();
            }

            @Override
            public void onNothingSelected() {

            }
        });

        mChart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartLongPressed(MotionEvent me) {
                if (me==null){
                    GIS_Log.d(TAG,"me == null");
                    return;
                }
                //final int idx = (int)me.getX()-1;
                //final float rightX = mChart.getHighestVisibleX();

                //final int idx = (int)me.getAxisValue((int)me.getX());
                GIS_Log.d(TAG,"currentDataIndex = "+currentDataIndex+"  dataListInPeriod.size="+dataListInPeriod.size());

                AlertDialog alertDialog = new AlertDialog.Builder(mActivity,R.style.MyDialogTheme).create(); //Read Update
                alertDialog.setTitle(dataListInPeriod.get(currentDataIndex).createdDate);
                alertDialog.setMessage("Vpk :  " + String.format("%.2f",dataListInPeriod.get(currentDataIndex).Vpk)
                        +"  ,VTI:  "+String.format("%.2f",dataListInPeriod.get(currentDataIndex).VTI)
                        + " \nSV:  "+String.format("%.2f",dataListInPeriod.get(currentDataIndex).SV)
                        +"  ,HR:  "+dataListInPeriod.get(currentDataIndex).HR
                        +"  ,CO:  "+String.format("%.2f",dataListInPeriod.get(currentDataIndex).CO));
                final long deleteID = dataListInPeriod.get(currentDataIndex).Id;
                final String deleteFilename = dataListInPeriod.get(currentDataIndex).fileName;
                alertDialog.setButton( Dialog.BUTTON_POSITIVE, getString(R.string.spectrum), (dialog, which) -> {
                    if (dataListInPeriod.get(currentDataIndex).fileName!=null && !dataListInPeriod.get(currentDataIndex).fileName.equals("")){
                        updateWithOfflineFragment(data2Fragment.this, dataListInPeriod.get(currentDataIndex).fileName);
                    }
                });

//                alertDialog.setButton( Dialog.BUTTON_NEUTRAL, "上傳", (dialog, which) -> {
//                    showDialogForUpload();
//                });

                alertDialog.setButton( Dialog.BUTTON_NEGATIVE, getString(R.string.action_del), (dialog, which) -> {
                    //String strMsg = "Are you sure to delete the data ?";
                    showDialogForDelete(getString(R.string.alert_msg_sure_del_data), deleteID, deleteFilename);
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.cancel), (dialog, id) -> {
                    // Do nothing.
                });


                alertDialog.show();  //<-- See This!
                final Button spectrumBtn = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                spectrumBtn.setBackgroundResource(R.drawable.block_b);
                spectrumBtn.setTextColor(Color.WHITE);
                spectrumBtn.setTextSize(18f);
                spectrumBtn.setScaleX(0.60f);
                spectrumBtn.setScaleY(0.60f);

                final Button delBtn = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                delBtn.setBackgroundResource(R.drawable.block_g2);
                delBtn.setTextColor(Color.parseColor("#804A4A4A"));
                //delBtn.setBackgroundResource(R.drawable.block_b);
                //delBtn.setTextColor(Color.WHITE);
                delBtn.setTextSize(18f);
                delBtn.setScaleX(0.60f);
                delBtn.setScaleY(0.60f);
                delBtn.setPadding(20,5,20,5);

                final Button uploadBtn = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                uploadBtn.setBackgroundResource(R.drawable.block_g2);
                uploadBtn.setTextColor(Color.parseColor("#804A4A4A"));
                //cancelBtn.setBackgroundResource(R.drawable.block_b);
                //cancelBtn.setTextColor(Color.WHITE);
                uploadBtn.setTextSize(18f);
                uploadBtn.setScaleX(0.60f);
                uploadBtn.setScaleY(0.60f);
                uploadBtn.setPadding(20,5,20,5);
            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {

            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {

            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {

            }
        });

        ImageButton dateButton = rootView.findViewById(R.id.dateButton);
        dateButton.setOnClickListener(view -> {
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(mActivity, (view1, year, month, day) -> {
                //String format = getString(R.string.set_date) + setDateFormat(year,month,day);
                //dateText.setText(format);
                currentDataIndex = 0;
                selectDateStr = String.valueOf(year) + "/"
                        + String.valueOf(month + 1) + "/"
                        + String.valueOf(day);
                GIS_Log.d(TAG,selectDateStr);
                updateActionBarTitle(selectDateStr);
                updateDataAndViews();
            }, mYear,mMonth, mDay).show();
        });

        updateDataAndViews();
        return rootView;
    }

    private void updateActionBarTitle(String inputDateStr){
        if (mActivity.getSupportActionBar()!=null){
            if (inputDateStr==null){
                //mActivity.getSupportActionBar().
                setTitle(getActionBarTitleStr(currentSegmentDays,getCurrentDateStr(),"yyyy/MM/dd HH:mm:ss"));
            }else {
                //mActivity.getSupportActionBar().
                setTitle(getActionBarTitleStr(currentSegmentDays,inputDateStr,"yyyy/MM/dd"));
            }

        }
    }

    public void setTitle(String title){
        if ((AppCompatActivity)getActivity()!=null&&((AppCompatActivity)getActivity()).getSupportActionBar()!=null){
            //((AppCompatActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
            //((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            TextView textView = new TextView(getActivity());
            textView.setText(title);
            textView.setTextSize(20);
            textView.setTypeface(null, Typeface.BOLD);
            textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(getResources().getColor(R.color.black,getActivity().getTheme()));
            ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            ((AppCompatActivity)getActivity()).getSupportActionBar().setCustomView(textView);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (SystemConfig.mTestMode){
            inflater.inflate(R.menu.data_menu,menu);
//            menu.removeItem(R.id.action_export);
        }else {
//            inflater.inflate(R.menu.update_to_cloud_menu,menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_export:
                // TODO: export data to csv file.
                exportDB();
                return true;
//            case R.id.update_data:
//                Log.d(TAG,"update_data");
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void exportDB(){

        String exportDirPath = Utilitys.getUserBaseFilePath(mActivity,UserManagerCommon.mUserInfoCur.userID);
        File exportDir = new File(exportDirPath,"");
        if (!exportDir.exists()){
            exportDir.mkdir();
        }
        SimpleDateFormat df;

        //df = new SimpleDateFormat("yyyyMMdd_HHmmssSSS");
        df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String strDate = df.format(new Date());
        String filename = UserManagerCommon.mUserInfoCur.userID+"_"+ strDate +".csv";
        File exportFile = new File(exportDir,filename);
        try {
            if (exportFile.createNewFile()){
                GIS_Log.d(TAG,"Create file "+filename+" successfully.");
                CSVWriter csvWriter = new CSVWriter(new FileWriter(exportFile));
                SQLiteDatabase db = mHelper.getReadableDatabase();
                Cursor curCSV = db.rawQuery("select *  from "+IwuSQLHelper.STR_TABLE_DATA+" where userID = ?",new String[] {UserManagerCommon.mUserInfoCur.userID});
                csvWriter.writeNext(curCSV.getColumnNames());
                while (curCSV.moveToNext()){
                    String[] arrStr = {curCSV.getString(0),
                            curCSV.getString(1),
                            curCSV.getString(2),
                            curCSV.getString(3),
                            curCSV.getString(4),
                            curCSV.getString(5),
                            curCSV.getString(6),
                            curCSV.getString(7),
                            curCSV.getString(8),
                            curCSV.getString(9)};
                    csvWriter.writeNext(arrStr);
                }
                csvWriter.close();
                curCSV.close();

                showAlertDialog(getString(R.string.find_value_success_toast),
                        getString(R.string.alert_msg_save_csv_to)+exportFile.getAbsolutePath());

            }else{
                GIS_Log.d(TAG,"file "+filename+" existed.");
                showAlertDialog(getString(R.string.alert_title_failure),null);
            }

        }catch(Exception sqlEx){
            sqlEx.printStackTrace();
        }

    }

    private void showAlertDialog(String title, String msg){
        if (getContext()!=null){
            AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
            alertDialog.setTitle(title);
            if (msg!=null){
                alertDialog.setMessage(msg);
            }

            alertDialog.setButton(Dialog.BUTTON_POSITIVE,"OK", (dialogInterface, i) -> {
                // Do nothing.
            });
            alertDialog.show();
        }
    }

    private String getCurrentDateStr(){
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",Locale.getDefault());
        return format.format(date);
    }

    private String getShortDateTimeStr(String inputStr){
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",Locale.getDefault());
        Date date = null;
        try {
            date = inputFormat.parse(inputStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat format = new SimpleDateFormat("MM/dd HH:mm",Locale.getDefault());
        return format.format(date);
    }

    private String getActionBarTitleStr(int periodDays,String date, String dateFormat){

        SimpleDateFormat format = new SimpleDateFormat(dateFormat,Locale.getDefault());
        String outputStr="";
        try {
            Date newDate = format.parse(date);
            format = new SimpleDateFormat("MM/dd");
            if (periodDays == 1){
                outputStr = format.format(newDate);
            }else if (periodDays==7){
                Calendar cal = Calendar.getInstance();
                cal.setTime(newDate);
                cal.add(Calendar.DATE, -7);
                outputStr = format.format(cal.getTime())+" - " +format.format(newDate);
            }else if (periodDays == 30){
                Calendar cal = Calendar.getInstance();
                cal.setTime(newDate);
                cal.add(Calendar.DATE, -30);
                outputStr = format.format(cal.getTime())+" - " +format.format(newDate);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return outputStr;
    }


    private void queryUserDataToList(String userID){
        dataList.clear();
        try{
            Cursor cursor = mHelper.mDBWrite.rawQuery("select *  from "+IwuSQLHelper.STR_TABLE_DATA+" where userID = ?",new String[] {userID});
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast()){
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
                    cursor.moveToNext();
                }
            }
            cursor.close();
        }catch (Exception ex1){
            ex1.printStackTrace();
        }

    }

    @Override
    protected void setupEvents() {
        final DecoView hrDecoView = getHRDecoView();
        final DecoView vpkDecoView = getVpkDecoView();
        final DecoView vtiDecoView = getVtiDecoView();
        final DecoView svDecoView = getSvDecoView();
        final DecoView coDecoView = getCoDecoView();

        final View view = getView();
        if (hrDecoView == null || hrDecoView.isEmpty() || view == null
                || vpkDecoView == null || vpkDecoView.isEmpty()
                || vtiDecoView == null || vtiDecoView.isEmpty()
                || svDecoView == null || svDecoView.isEmpty()
                || coDecoView == null || coDecoView.isEmpty()
                || dataListInPeriod.size()==0
                ) {
            return;
        }

        if (SystemConfig.isYuhul && !SystemConfig.mTestMode){
            vpkDecoView.setVisibility(View.GONE);
            vtiDecoView.setVisibility(View.GONE);
        }

        String sDtStr = getShortDateTimeStr(dataListInPeriod.get(currentDataIndex).createdDate);
        mDtTextView.setText(sDtStr);
        final TextView textHR = view.findViewById(R.id.HrText);
        final int showBackDuration = 100;
        final int fadeDuration = 0;
        final int spiralDuration = 0;
        final int spiralDelay = 0;
        final int delayMS = 500;
        hrDecoView.addEvent(new DecoEvent.Builder(DecoEvent.EventType.EVENT_SHOW, true)
                .setIndex(mBackIndex)
                .setDuration(showBackDuration)
                .build());
        // circle...

        hrDecoView
                //.addEvent(new DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_OUT)
                .addEvent(new DecoEvent.Builder(DecoEvent.EventType.EVENT_SHOW, true)
                .setIndex(mSeriesHRIndex)
                .setFadeDuration(fadeDuration)
                .setDuration(spiralDuration)
                .setDelay(spiralDelay)
                .build());

        hrDecoView.addEvent(new DecoEvent.Builder(dataListInPeriod.get(currentDataIndex).HR).setIndex(mSeriesHRIndex).setDelay(delayMS).build());

        if (!SystemConfig.isYuhul || SystemConfig.mTestMode){
            final TextView textVpk = view.findViewById(R.id.VpkText);
            vpkDecoView.addEvent(new DecoEvent.Builder(DecoEvent.EventType.EVENT_SHOW, true)
                    .setIndex(mBackVpkIndex)
                    .setDuration(showBackDuration)
                    .build());
            // circle...
            vpkDecoView
                    //.addEvent(new DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_OUT)
                    .addEvent(new DecoEvent.Builder(DecoEvent.EventType.EVENT_SHOW, true)
                            .setIndex(mSeriesVpkIndex)
                            .setFadeDuration(fadeDuration)
                            .setDuration(spiralDuration)
                            .setDelay(spiralDelay)
                            .build());

            vpkDecoView.addEvent(new DecoEvent.Builder((float) dataListInPeriod.get(currentDataIndex).Vpk).setIndex(mSeriesVpkIndex).setDelay(delayMS).build());

            // Vti
            final TextView textVti = view.findViewById(R.id.VtiText);
            vtiDecoView.addEvent(new DecoEvent.Builder(DecoEvent.EventType.EVENT_SHOW, true)
                    .setIndex(mBackVtiIndex)
                    .setDuration(showBackDuration)
                    .build());
            // circle...
            vtiDecoView.addEvent(new DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_OUT)
                    .setIndex(mSeriesVtiIndex)
                    .setFadeDuration(fadeDuration)
                    .setDuration(spiralDuration)
                    .setDelay(spiralDelay)
                    .build());

            vtiDecoView.addEvent(new DecoEvent.Builder((float) dataListInPeriod.get(currentDataIndex).VTI).setIndex(mSeriesVtiIndex).setDelay(delayMS).build());

        }


        // SV
        final TextView textSv = view.findViewById(R.id.svText);
        svDecoView.addEvent(new DecoEvent.Builder(DecoEvent.EventType.EVENT_SHOW, true)
                .setIndex(mBackSvIndex)
                .setDuration(showBackDuration)
                .build());
        // circle...
        svDecoView.addEvent(new DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_OUT)
                .setIndex(mSeriesSvIndex)
                .setFadeDuration(fadeDuration)
                .setDuration(spiralDuration)
                .setDelay(spiralDelay)
                .build());

        svDecoView.addEvent(new DecoEvent.Builder((float) dataListInPeriod.get(currentDataIndex).SV).setIndex(mSeriesVtiIndex).setDelay(delayMS).build());

        // CO
        final TextView textCo = view.findViewById(R.id.CoText);
        coDecoView.addEvent(new DecoEvent.Builder(DecoEvent.EventType.EVENT_SHOW, true)
                .setIndex(mBackCoIndex)
                .setDuration(showBackDuration)
                .build());
        // circle...
        coDecoView.addEvent(new DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_OUT)
                .setIndex(mSeriesSvIndex)
                .setFadeDuration(fadeDuration)
                .setDuration(spiralDuration)
                .setDelay(spiralDelay)
                .build());

        coDecoView.addEvent(new DecoEvent.Builder((float) dataListInPeriod.get(currentDataIndex).CO).setIndex(mSeriesCoIndex).setDelay(delayMS).build());
    }

    @Override
    protected void createTracks() {
        final DecoView hrDecoView = getHRDecoView();
        DecoView vpkDecoView = getVpkDecoView();
        DecoView vtiDecoView = getVtiDecoView();
        final DecoView svDecoView = getSvDecoView();
        final DecoView coDecoView = getCoDecoView();
        final View view = getView();
        if (hrDecoView == null || view == null
                || vpkDecoView == null
                || vtiDecoView == null
                || svDecoView == null
                || coDecoView == null
                ) {
            return;
        }

        if (SystemConfig.isYuhul && !SystemConfig.mTestMode){
            vpkDecoView.setVisibility(View.GONE);
            vtiDecoView.setVisibility(View.GONE);
        }
        final float lineWidth = 20f;

        hrDecoView.setOnClickListener(view1 -> {
            if (currentDataType!=HR){
                currentDataType = HR;
                setData(HR);
            }
        });
        final float HrSeriesMax = 200f;

        SeriesItem arcBackTrack = new SeriesItem.Builder(Color.argb(255, 228, 228, 228))
                .setRange(0, HrSeriesMax, HrSeriesMax)
                .setInitialVisibility(false)
                .setLineWidth(lineWidth)
                .setChartStyle(SeriesItem.ChartStyle.STYLE_DONUT)
                .build();

        mBackIndex = hrDecoView.addSeries(arcBackTrack);

        //SeriesItem seriesItem1 = new SeriesItem.Builder(Color.argb(255, 255, 165, 0))
        SeriesItem seriesItem1 = new SeriesItem.Builder(ColorTemplate.VORDIPLOM_COLORS[HR])
                .setRange(0, HrSeriesMax, 0)
                .setInitialVisibility(false)
                //.setLineWidth(getDimension(mTrackWidth[mStyleIndex]))
                .setLineWidth(lineWidth)
              //  .setInset(new PointF(-inset, -inset))
                .setSpinClockwise(true)
                .setCapRounded(true)
                .setChartStyle(SeriesItem.ChartStyle.STYLE_DONUT)
                .build();

        mSeriesHRIndex = hrDecoView.addSeries(seriesItem1);

        final TextView textHR = (TextView) view.findViewById(R.id.HrText);
        if (textHR != null) {
            textHR.setText("");
            addProgressListener(seriesItem1, textHR, "HR\n%.0f bpm");
        }
        vpkDecoView.setOnClickListener(view12 -> {
            if (currentDataType!=VPK){
                currentDataType = VPK;
                setData(VPK);
            }
        });
        final float VpkSeriesMax = 2.0f;

        SeriesItem arcVpkBackTrack = new SeriesItem.Builder(Color.argb(255, 228, 228, 228))
                .setRange(0, VpkSeriesMax, VpkSeriesMax)
                .setInitialVisibility(false)
                .setLineWidth(lineWidth)
                .setChartStyle(SeriesItem.ChartStyle.STYLE_DONUT)
                .build();

        mBackVpkIndex = vpkDecoView.addSeries(arcVpkBackTrack);
        //SeriesItem seriesVpkItem1 = new SeriesItem.Builder(Color.argb(255, 255, 51, 51))
        SeriesItem seriesVpkItem1 = new SeriesItem.Builder(ColorTemplate.VORDIPLOM_COLORS[VPK])
                .setRange(0, VpkSeriesMax, 0)
                .setInitialVisibility(false)
                //.setLineWidth(getDimension(mTrackWidth[mStyleIndex]))
                .setLineWidth(lineWidth)
                //  .setInset(new PointF(-inset, -inset))
                .setSpinClockwise(true)
                .setCapRounded(true)
                .setChartStyle(SeriesItem.ChartStyle.STYLE_DONUT)
                .build();

        mSeriesVpkIndex = vpkDecoView.addSeries(seriesVpkItem1);

        final TextView textVpk = view.findViewById(R.id.VpkText);
        if (textVpk != null) {
            textVpk.setText("");
            addProgressListener(seriesVpkItem1, textVpk, "Vpk\n%.2f m/s");
        }

        // Vti
        vtiDecoView.setOnClickListener(view13 -> {
            if (currentDataType!=VTI){
                currentDataType = VTI;
                setData(VTI);
            }
        });
        final float VtiSeriesMax = 30.0f;

        SeriesItem arcVtiBackTrack = new SeriesItem.Builder(Color.argb(255, 228, 228, 228))
                .setRange(0, VtiSeriesMax, VtiSeriesMax)
                .setInitialVisibility(false)
                .setLineWidth(lineWidth)
                .setChartStyle(SeriesItem.ChartStyle.STYLE_DONUT)
                .build();

        mBackVtiIndex = vtiDecoView.addSeries(arcVtiBackTrack);

        //SeriesItem seriesVtiItem1 = new SeriesItem.Builder(Color.argb(255, 196, 196, 128))
        SeriesItem seriesVtiItem1 = new SeriesItem.Builder(ColorTemplate.VORDIPLOM_COLORS[VTI])
                .setRange(0, VtiSeriesMax, 0)
                .setInitialVisibility(false)
                //.setLineWidth(getDimension(mTrackWidth[mStyleIndex]))
                .setLineWidth(lineWidth)
                //  .setInset(new PointF(-inset, -inset))
                .setSpinClockwise(true)
                .setCapRounded(true)
                .setChartStyle(SeriesItem.ChartStyle.STYLE_DONUT)
                .build();

        mSeriesVtiIndex = vtiDecoView.addSeries(seriesVtiItem1);

        final TextView textVti = view.findViewById(R.id.VtiText);
        if (textVti != null) {
            textVti.setText("");
            addProgressListener(seriesVtiItem1, textVti, "Vti\n%.2f cm");
        }

        // SV
        svDecoView.setOnClickListener(view14 -> {
            if (currentDataType!=SV){
                currentDataType = SV;
                setData(SV);
            }
        });
        final float svSeriesMax = 100.0f;

        SeriesItem arcSvBackTrack = new SeriesItem.Builder(Color.argb(255, 228, 228, 228))
                .setRange(0, svSeriesMax, svSeriesMax)
                .setInitialVisibility(false)
                .setLineWidth(lineWidth)
                .setChartStyle(SeriesItem.ChartStyle.STYLE_DONUT)
                .build();

        mBackSvIndex = svDecoView.addSeries(arcSvBackTrack);

        //SeriesItem seriesSvItem1 = new SeriesItem.Builder(Color.argb(255, 29, 118, 210))
        SeriesItem seriesSvItem1 = new SeriesItem.Builder(ColorTemplate.VORDIPLOM_COLORS[SV])
                .setRange(0, svSeriesMax, 0)
                .setInitialVisibility(false)
                //.setLineWidth(getDimension(mTrackWidth[mStyleIndex]))
                .setLineWidth(lineWidth)
                //  .setInset(new PointF(-inset, -inset))
                .setSpinClockwise(true)
                .setCapRounded(true)
                .setChartStyle(SeriesItem.ChartStyle.STYLE_DONUT)
                .build();

        mSeriesSvIndex = svDecoView.addSeries(seriesSvItem1);

        final TextView textSv = view.findViewById(R.id.svText);
        if (textSv != null) {
            textSv.setText("");
            addProgressListener(seriesSvItem1, textSv, "SV\n%.2f ml");
        }

        // CO
        coDecoView.setOnClickListener(view15 -> {
            if (currentDataType!=CO){
                currentDataType = CO;
                setData(CO);
            }
        });
        final float coSeriesMax = 10.0f;

        SeriesItem arcCoBackTrack = new SeriesItem.Builder(Color.argb(255, 228, 228, 228))
                .setRange(0, coSeriesMax, coSeriesMax)
                .setInitialVisibility(false)
                .setLineWidth(lineWidth)
                .setChartStyle(SeriesItem.ChartStyle.STYLE_DONUT)
                .build();

        mBackCoIndex = coDecoView.addSeries(arcCoBackTrack);

        //SeriesItem seriesCoItem1 = new SeriesItem.Builder(Color.argb(255, 255, 64, 129))
        SeriesItem seriesCoItem1 = new SeriesItem.Builder(ColorTemplate.VORDIPLOM_COLORS[CO])
                .setRange(0, coSeriesMax, 0)
                .setInitialVisibility(false)
                //.setLineWidth(getDimension(mTrackWidth[mStyleIndex]))
                .setLineWidth(lineWidth)
                //  .setInset(new PointF(-inset, -inset))
                .setSpinClockwise(true)
                .setCapRounded(true)
                .setChartStyle(SeriesItem.ChartStyle.STYLE_DONUT)
                .build();

        mSeriesCoIndex = coDecoView.addSeries(seriesCoItem1);

        final TextView textCo = view.findViewById(R.id.CoText);
        if (textCo != null) {
            textCo.setText("");
            addProgressListener(seriesCoItem1, textCo, "CO\n%.2f lpm");
        }
    }

    private void getPeriodData(String inputDate,int inputDays, String inputPattern){
        if (inputDate==null||inputDate.equals("")){
            inputDate = getCurrentDateStr();
        }
        dataListInPeriod.clear();
        if (dataList.size()==0){
            queryUserDataToList(UserManagerCommon.mUserInfoCur.userID);
            if (dataList.size()>0){

            }else{
                GIS_Log.d(TAG,"This user has no data.");
                return;
            }
        }

        for (int i = 0;i<dataList.size();i++){
            int retDays = (int)dayBetween(dataList.get(i).createdDate,inputDate,inputPattern);
            if (retDays<inputDays && retDays>=0){
                dataListInPeriod.add(dataList.get(i));
            }
        }
    }

    public long dayBetween(String date1,String date2,String pattern)
    {
        // Default pattern = "yyyy/MM/dd HH:mm:ss";
        if (pattern==null|| pattern.equals("")){
            pattern = "yyyy/MM/dd HH:mm:ss";
        }
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat sdf2 = new SimpleDateFormat(pattern, Locale.getDefault());
        Date Date1 = null,Date2 = null;
        long retDays = -1;
        try{
            Date1 = sdf1.parse(date1);
            Date2 = sdf2.parse(date2);
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        if (Date1!=null&&Date2!=null){
            retDays = (Date2.getTime() - Date1.getTime())/(24*60*60*1000);
        }
        return retDays;
    }

    public long getDateTimeMS(String dateTimeStr){
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",Locale.getDefault());
        long outputMS = 0;
        try {
            Date inputDate = inputFormat.parse(dateTimeStr);
            outputMS = inputDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return outputMS;
    }

    private void setData(int dataType){
        GIS_Log.d(TAG,"dataType = "+dataType+"  ,dataListInPeriod.size()="+dataListInPeriod.size());
        //if (dataListInPeriod.size()>0){
            ArrayList<Entry> yVals = new ArrayList<>();
            for (int i=0;i<dataListInPeriod.size();i++){
                long tmpMS = getDateTimeMS(dataListInPeriod.get(i).createdDate);
                switch (dataType){
                    case HR:
                        yVals.add(new Entry(i+1,dataListInPeriod.get(i).HR));
//                        yVals.add(new Entry(tmpMS,dataListInPeriod.get(i).HR));
                        break;
                    case VPK:
                        yVals.add(new Entry( i+1,(float) dataListInPeriod.get(i).Vpk));
//                        yVals.add(new Entry( tmpMS,(float) dataListInPeriod.get(i).Vpk));
                        break;
                    case VTI:
                        yVals.add(new Entry(i+1,(float)dataListInPeriod.get(i).VTI));
//                        yVals.add(new Entry(tmpMS,(float)dataListInPeriod.get(i).VTI));
                        break;
                    case SV:
                        yVals.add(new Entry(i+1,(float)dataListInPeriod.get(i).SV));
//                        yVals.add(new Entry(tmpMS,(float)dataListInPeriod.get(i).SV));
                        break;
                    case CO:
                        yVals.add(new Entry(i+1,(float)dataListInPeriod.get(i).CO));
//                        yVals.add(new Entry(tmpMS,(float)dataListInPeriod.get(i).CO));
                        break;

                }
            }
            LineDataSet dataSet;
            String dataLabel = "";
            String unitDescription = "";
            switch (dataType){
                case HR:
                    dataLabel = getString(R.string.hr_title);
                    unitDescription = getString(R.string.hr_unit);
                    break;
                case VPK:
                    dataLabel = getString(R.string.vpk_title);
                    unitDescription = getString(R.string.vpk_unit);
                    break;
                case VTI:
                    dataLabel = getString(R.string.vti_title);
                    unitDescription = getString(R.string.cm_unit);
                    break;
                case SV:
                    dataLabel = getString(R.string.sv_title);
                    unitDescription = getString(R.string.ml_unit);
                    break;
                case CO:
                    dataLabel = getString(R.string.co_title);
                    unitDescription = getString(R.string.co_unit);
                    break;

            }
            if (mChart.getData() !=null &&
                    mChart.getData().getDataSetCount()>0){
                dataSet = (LineDataSet)mChart.getData().getDataSetByIndex(0);
                if (yVals.size()>0){
                    dataSet.setColor(ColorTemplate.VORDIPLOM_COLORS[dataType]);
                    dataSet.setValues(yVals);
                    dataSet.setLabel(dataLabel);
                    //dataSet.setFormSize(20);
                    dataSet.setDrawValues(false);
                    /*if (!dataLabel.equals("HR")) {
                        mChart.getData().setValueFormatter(new IValueFormatter() {
                            @Override
                            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                                return new DecimalFormat("###,###,##0.00").format(value);
                            }
                        });
                    }else{
                        mChart.getData().setValueFormatter(new IValueFormatter() {
                            @Override
                            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                                return new DecimalFormat("###,###,##0").format(value);
                            }
                        });
                    }*/

                    mChart.getDescription().setText(unitDescription);
                    mChart.getData().notifyDataChanged();
                }else{
                    mChart.setData(null);
                }
                mChart.notifyDataSetChanged();


            }else {
                if (yVals.size()>0){
                    dataSet = new LineDataSet(yVals,dataLabel);
                    dataSet.setLineWidth(2f);
                    dataSet.setCircleRadius(3f);
                    dataSet.setColor(ColorTemplate.VORDIPLOM_COLORS[dataType]);
                    dataSet.setHighLightColor(Color.rgb(244, 117, 117));
                    dataSet.setDrawValues(false);
                    LineData data = new LineData(dataSet);
                    /*if (!dataLabel.equals("HR")) {
                        data.setValueFormatter(new IValueFormatter() {
                            @Override
                            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                                return new DecimalFormat("###,###,##0.00").format(value);
                            }
                        });
                    }else{
                        data.setValueFormatter(new IValueFormatter() {
                            @Override
                            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                                return new DecimalFormat("###,###,##0").format(value);
                            }
                        });
                    }*/
                    data.setValueTextColor(Color.BLACK);
                    data.setValueTextSize(9f);
                    mChart.setData(data);
                    mChart.getDescription().setText(unitDescription);
                }

            }
            mChart.invalidate();
       // }

    }

    private void updateWithOfflineFragment(final Fragment fragment, String filename) {
        if (fragment!=null&&getActivity()!=null){
            SystemConfig.mEnumUltrasoundUIState = SystemConfig.ENUM_UI_STATE.ULTRASOUND_UI_STATE_OFFLINE;
            SystemConfig.mEnumUltrasoundSubUIState = SystemConfig.ENUM_SUB_UI_STATE.BLOOD_FLOW_VELOCITY;
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            offlineFragment offFrag;
            if (filename!=null){
                Long selectID = dataListInPeriod.get(currentDataIndex).Id;
                offFrag = offlineFragment.newInstance(filename,selectID);
            }else {
                offFrag = offlineFragment.newInstance();
            }

            MainActivity.currentFragmentTag = Constants.FILEOPEN_TAG;
            transaction.replace(R.id.frame_layout, offFrag,
                    Constants.FILEOPEN_TAG);
            transaction.addToBackStack(fragment.getClass().getName());

            transaction.commit();
        }
    }
    private void showDialogForDelete(String strMsg, final long deleteID, final String deleteFilename){
        new AlertDialog.Builder(mActivity)
                .setTitle(strMsg)
                //.setMessage(strMsg)
                .setPositiveButton(R.string.alert_message_exit_ok, (dialog, which) -> {

                    deleteDataSelected(deleteID, deleteFilename);
                    currentDataIndex = 0;
                    mChart.highlightValue(null);

                    updateDataAndViews();
                })
                .setNegativeButton(R.string.alert_message_exit_cancel, (dialog, which) -> {
                    //do nothing;
                })
                .show();
    }


    private void updateDataAndViews(){
        queryUserDataToList(UserManagerCommon.mUserInfoCur.userID);
        // initial listview and adapter for refresh.
        //reloadListView();
        if (selectDateStr==null){
            getPeriodData(getCurrentDateStr(),currentSegmentDays,null);
        }else{
            getPeriodData(selectDateStr,currentSegmentDays,"yyyy/MM/dd");
        }
        setData(currentDataType);
        setupEvents();
    }

    private void deleteDataSelected(final long deleteID, final String deleteFilename){
        String strWhere;
        long lnReturn;

        //strWhere = "_id=" + UserManagerCommon.mUserInfoCur.mIntUserID;
        strWhere = IwuSQLHelper.KEY_DATA_TABLE_PRIMARY+" = '"+deleteID+"'";
        mHelper.mDBWrite.beginTransaction();
        GIS_Log.d(TAG,"strWhere="+strWhere);
        try {
            lnReturn =mHelper.mDBWrite.delete(IwuSQLHelper.STR_TABLE_DATA, strWhere, null);
            mHelper.mDBWrite.setTransactionSuccessful();
        }catch(Exception ex2){
            ex2.printStackTrace();
        }finally{
            mHelper.mDBWrite.endTransaction();
        }
        // Delete wav file
        String deleteFilePath = Utilitys.getUserBaseFilePath(mActivity,UserManagerCommon.mUserInfoCur.userID);
        deleteFilePath += File.separator + deleteFilename;
        File deleteFile = new File(deleteFilePath);
        if (deleteFile.exists()){
            if (deleteFile.delete()){
                GIS_Log.d(TAG,deleteFilePath+" had been deleted.");
            }else{
                GIS_Log.d(TAG,deleteFilePath+"  delete file fail.");
            }
        }

    }
}
