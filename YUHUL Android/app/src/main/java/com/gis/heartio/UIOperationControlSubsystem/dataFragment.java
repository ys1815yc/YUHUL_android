package com.gis.heartio.UIOperationControlSubsystem;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import androidx.core.app.Fragment;
//import androidx.core.app.FragmentTransaction;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
//import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.gis.CommonUtils.Constants;
import com.gis.heartio.GIS_Log;
import com.gis.heartio.R;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.IwuSQLHelper;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.SystemConfig;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.Utilitys;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.dataInfo;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Utils;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link dataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class dataFragment extends Fragment {
    private static final String TAG = "dataFragement";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private List<dataInfo> dataList = new ArrayList<>();
    private IwuSQLHelper mHelper;
    private MainActivity mActivity;
    private ListView lv = null;

    public static final int HR = 0;
    public static final int VPK = 1;
    public static final int VTI = 2;
    public static final int SV = 3;
    public static final int CO = 4;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    protected Typeface mTfLight;

    private static final int CHART_TYPE_BARCHART = 0;
    private static final int CHART_TYPE_LINECHART = 1;
    private static int chartType = CHART_TYPE_LINECHART;
    //private static int chartType = CHART_TYPE_BARCHART;
    private int selectedIdx = 0;

    public static dataFragment newInstance() {
        return new dataFragment();
    }

    public dataFragment() {
        // Required empty public constructor

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment dataFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static dataFragment newInstance(String param1, String param2) {
        dataFragment fragment = new dataFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
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
        final View rootView =inflater.inflate(R.layout.fragment_item_data, container, false);
        setHasOptionsMenu(true);
        if (mActivity.getSupportActionBar()!=null){
            mActivity.getSupportActionBar().setTitle(getString(R.string.title_historical_data));
        }
        if (dataList.size()==0){
            queryUserDataToList(UserManagerCommon.mUserInfoCur.userID);
        }

        lv = rootView.findViewById(R.id.dataListView);
        //reloadListView();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.currentFragmentTag = Constants.DATA_MANAGER_TAG;
        reloadListView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        if (SystemConfig.mTestMode){
            inflater.inflate(R.menu.data_menu,menu);
//            menu.removeItem(R.id.action_export);
        }else {
            inflater.inflate(R.menu.update_to_cloud_menu,menu);
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
            case R.id.update_data:
                Log.d(TAG,"update_data");
                return true;
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
                    String arrStr[] = {curCSV.getString(0),
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
//                    GIS_Log.d(TAG,"create CSV file");
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

            alertDialog.setButton(Dialog.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Do nothing.
                }
            });
            alertDialog.show();
        }
    }

    private class LineChartDataAdapter extends ArrayAdapter<LineData>{

        public LineChartDataAdapter(@NonNull Context context, @NonNull List<LineData> objects) {
            super(context, 0, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final LineData data = getItem(position);

            final ViewHolder holder;

            if (convertView == null) {

                holder = new ViewHolder();

                convertView = LayoutInflater.from(getContext()).inflate(
                        R.layout.listitem_linechart, null);
                holder.chart = convertView.findViewById(R.id.linechart);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            YAxis leftAxis = holder.chart.getAxisLeft();
            leftAxis.setTypeface(mTfLight);
            leftAxis.setLabelCount(5, false);
            leftAxis.setSpaceTop(15f);

            YAxis rightAxis = holder.chart.getAxisRight();
            rightAxis.setTypeface(mTfLight);
            rightAxis.setLabelCount(5, false);
            rightAxis.setSpaceTop(15f);


            if (data!=null){
                 /*for (int i=0;i<labels.length;i++){
            }*/
                String[] labels = data.getDataSetLabels();

                if (!labels[0].equals("HR")){
//                    data.setValueFormatter(new IValueFormatter() {
//                        @Override
//                        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
//                            return new DecimalFormat("###,###,##0.00").format(value);
//                        }
//                    });
                    data.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            return new DecimalFormat("###,###,##0.00").format(value);
                        }
                    });
                    if (labels[0].equals("Vpk")){
                        holder.chart.getDescription().setText(getString(R.string.vpk_unit));
                        rightAxis.setAxisMinimum(0);
                        leftAxis.setAxisMinimum(0);
                        rightAxis.setAxisMaximum(2.5f);
                        leftAxis.setAxisMaximum(2.5f);
                    }else if (labels[0].equals("CO")){
                        holder.chart.getDescription().setText(getString(R.string.co_unit));
                        rightAxis.setAxisMinimum(0);
                        leftAxis.setAxisMinimum(0);
                        rightAxis.setAxisMaximum(30);
                        leftAxis.setAxisMaximum(30);
                    }else if (labels[0].equals("VTI")){
                        holder.chart.getDescription().setText(getString(R.string.cm_unit));
                    }else{
                        holder.chart.getDescription().setText(getString(R.string.ml_unit));
                        rightAxis.setAxisMinimum(0);
                        leftAxis.setAxisMinimum(0);
                        rightAxis.setAxisMaximum(160);
                        leftAxis.setAxisMaximum(160);
                    }

                }else {
//                    data.setValueFormatter(new IValueFormatter() {
//                        @Override
//                        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
//                            return new DecimalFormat("###,###,##0").format(value);
//                        }
//                    });
                    data.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            return new DecimalFormat("###,###,##0").format(value);
                        }
                    });
                    rightAxis.setAxisMinimum(30);
                    leftAxis.setAxisMinimum(30);
                    rightAxis.setAxisMaximum(180);
                    leftAxis.setAxisMaximum(180);
                    holder.chart.getDescription().setText(getString(R.string.hr_unit));
                }
            }



            holder.chart.getDescription().setEnabled(true);
            holder.chart.animateX(1000);
            holder.chart.setOnChartValueSelectedListener(mOCValueSelectedListner);
            // set data
            holder.chart.setOnChartGestureListener(new OnChartGestureListener() {
                @Override
                public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

                }

                @Override
                public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

                }

                @Override
                public void onChartLongPressed(MotionEvent me) {
                    if (me == null){
                        GIS_Log.d(TAG,"me == null");
                        return;
                    }
                    //final int idx = (int)me.getX()-1;
                    final float rightX = holder.chart.getHighestVisibleX();

                    //final int idx = (int)me.getAxisValue((int)me.getX());
                    GIS_Log.d(TAG,"selectedIdx = "+selectedIdx+"  dataList.size="+dataList.size());
                    AlertDialog alertDialog = new AlertDialog.Builder(mActivity).create(); //Read Update
                    alertDialog.setTitle(dataList.get(selectedIdx).createdDate);
                    alertDialog.setMessage(" Vpk = " + String.format("%.2f",dataList.get(selectedIdx).Vpk)
                            +"  ,VTI= "+String.format("%.2f",dataList.get(selectedIdx).VTI)
                            + " \nSV="+String.format("%.2f",dataList.get(selectedIdx).SV)
                            +" ,HR= "+dataList.get(selectedIdx).HR);
                    final long deleteID = dataList.get(selectedIdx).Id;
                    final String deleteFilename = dataList.get(selectedIdx).fileName;
                    alertDialog.setButton( Dialog.BUTTON_POSITIVE, getString(R.string.spectrum), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (dataList.get(selectedIdx).fileName!=null && !dataList.get(selectedIdx).fileName.equals("")){
                                updateWithOfflineFragment(dataFragment.this, dataList.get(selectedIdx).fileName);
                            }
                        }
                    });

                    alertDialog.setButton( Dialog.BUTTON_NEGATIVE, getString(R.string.action_del), new DialogInterface.OnClickListener()    {
                        public void onClick(DialogInterface dialog, int which) {
                            //String strMsg = "Are you sure to delete the data ?";
                            showDialogForDelete(getString(R.string.alert_msg_sure_del_data), deleteID, deleteFilename);
                        }
                    });
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Do nothing.
                        }
                    });
                    alertDialog.show();  //<-- See This!
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

            holder.chart.setData(data);
            holder.chart.invalidate();// refresh

            return convertView;
        }

        private class ViewHolder {

            LineChart chart;
        }
    }

    private class ChartDataAdapter extends ArrayAdapter<BarData> {

        public ChartDataAdapter(Context context, List<BarData> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final BarData data = getItem(position);

            ViewHolder holder = null;

            if (convertView == null) {

                holder = new ViewHolder();

                convertView = LayoutInflater.from(getContext()).inflate(
                        R.layout.listitem_barchart, null);
                holder.chart = (BarChart) convertView.findViewById(R.id.chart);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            // apply styling
            if (data!=null){
                data.setValueTypeface(mTfLight);
                data.setValueTextColor(Color.BLACK);
            }
            holder.chart.getDescription().setEnabled(false);
            holder.chart.setDrawGridBackground(false);

            XAxis xAxis = holder.chart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setAxisMinimum(0);
            xAxis.setTypeface(mTfLight);
            xAxis.setDrawGridLines(false);
            // Set xAxis as int value
            xAxis.setGranularity(1f);
            //xAxis.setValueFormatter(new DayAxisValueFormatter(holder.chart));

            YAxis leftAxis = holder.chart.getAxisLeft();
            leftAxis.setTypeface(mTfLight);
            leftAxis.setLabelCount(5, false);
            leftAxis.setSpaceTop(15f);

            YAxis rightAxis = holder.chart.getAxisRight();
            rightAxis.setTypeface(mTfLight);
            rightAxis.setLabelCount(5, false);
            rightAxis.setSpaceTop(15f);

            String[] labels = data.getDataSetLabels();
            if (labels[0].equals("Vpk")){
//                data.setValueFormatter(new IValueFormatter() {
//                    @Override
//                    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
//                        return new DecimalFormat("###,###,##0.00").format(value);
//                    }
//                });
                data.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value){
                        return new DecimalFormat("###,###,##0.00").format(value);
                    }
                });
            }else{
//                data.setValueFormatter(new IValueFormatter() {
//                    @Override
//                    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
//                        return new DecimalFormat("###,###,##0.0").format(value);
//                    }
//                });
                data.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return new DecimalFormat("###,###,##0.0").format(value);
                    }
                });
            }
            /*if (labels[0].equals("HR")){
                rightAxis.setAxisMinimum(30);
                leftAxis.setAxisMinimum(30);
                rightAxis.setAxisMaximum(230);
                leftAxis.setAxisMaximum(230);
            }else if (labels[0].equals("Vpk")){
                rightAxis.setAxisMinimum(0);
                leftAxis.setAxisMinimum(0);
                rightAxis.setAxisMaximum(2.5f);
                leftAxis.setAxisMaximum(2.5f);
                rightAxis.setGranularity(1.2f);
                leftAxis.setGranularity(1.2f);
                holder.chart.setFitBars(true);
                data.setValueFormatter(new IValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                        return new DecimalFormat("###,###,##0.00").format(value);
                    }
                });
            }else if (labels[0].equals("CO")){
                holder.chart.setFitBars(true);
            }else{
                holder.chart.setFitBars(true);
            }*/


            // set data
            holder.chart.setData(data);
            holder.chart.setFitBars(true);

            holder.chart.animateY(700);
            holder.chart.setOnChartValueSelectedListener(mOCValueSelectedListner);

            // do not forget to refresh the chart
            holder.chart.invalidate();

            return convertView;
        }

        private class ViewHolder {

            BarChart chart;
        }
    }

    OnChartValueSelectedListener mOCValueSelectedListner =new OnChartValueSelectedListener() {
        @Override
        public void onValueSelected(Entry e, Highlight h) {
            if (e==null){
                return;
            }
            //RectF bounds = new RectF();
            //holder.chart.getBarBounds(e,bounds);

            final int idx = (int)e.getX()-1;
            selectedIdx = idx;
            GIS_Log.d(TAG, "data _id :"+ dataList.get(idx).Id+" date: "+dataList.get(idx).createdDate + " Vpk = " + dataList.get(idx).Vpk
                    +"  ,VTI= "+dataList.get(idx).VTI + " ,SV="+dataList.get(idx).SV
                    +" ,HR= "+dataList.get(idx).HR +" ,Path= "+dataList.get(idx).fileName);

        }

        @Override
        public void onNothingSelected() {

        }
    };

    private void showDialogForDelete(String strMsg, final long deleteID, final String deleteFilename){
        new AlertDialog.Builder(mActivity)
                .setTitle(strMsg)
                //.setMessage(strMsg)
                .setPositiveButton(R.string.alert_message_exit_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        deleteDataSelected(deleteID, deleteFilename);
                        queryUserDataToList(UserManagerCommon.mUserInfoCur.userID);
                        // initial listview and adapter for refresh.
                        reloadListView();
                    }
                })
                .setNegativeButton(R.string.alert_message_exit_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing;
                    }
                })
                .show();
    }
    private void reloadListView(){
        //if (dataList.size()>0){
            if (chartType == CHART_TYPE_BARCHART){
                ArrayList<BarData> bDataList = new ArrayList<>();
                for (int i=0;i<5;i++){
                    bDataList.add(getBarData(dataList,i));
                }
                ChartDataAdapter cdA = new ChartDataAdapter(mActivity.getApplicationContext(),bDataList);
                lv.setAdapter(cdA);
                cdA.notifyDataSetChanged();
            }else if(chartType == CHART_TYPE_LINECHART){
                ArrayList<LineData> lDataList = new ArrayList<>();
                for (int i=0;i<5;i++){
                    lDataList.add(getLineData(dataList,i));
                }
                LineChartDataAdapter lcdA = new LineChartDataAdapter(mActivity.getApplicationContext(),lDataList);
                lv.setAdapter(lcdA);
                lcdA.notifyDataSetChanged();
            }
        //}

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
        }catch(Exception ex2){
            ex2.printStackTrace();
        }finally{
            mHelper.mDBWrite.endTransaction();
        }


    }

    private void updateWithOfflineFragment(final Fragment fragment, String filename) {
        if (fragment!=null&&getActivity()!=null){
            SystemConfig.mEnumUltrasoundUIState = SystemConfig.ENUM_UI_STATE.ULTRASOUND_UI_STATE_OFFLINE;
            SystemConfig.mEnumUltrasoundSubUIState = SystemConfig.ENUM_SUB_UI_STATE.BLOOD_FLOW_VELOCITY;
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            offlineFragment offFrag;
            if (filename!=null){
                Long selectID = dataList.get(selectedIdx).Id;
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



    public long dayBetween(String date1,String date2,String pattern)
    {
        // Default pattern = "yyyy/MM/dd HH:mm:ss";
        if (pattern.equals("")){
            pattern = "yyyy/MM/dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        Date Date1 = null,Date2 = null;
        long retDays = -1;
        try{
            Date1 = sdf.parse(date1);
            Date2 = sdf.parse(date2);
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        if (Date1!=null&&Date2!=null){
            retDays = (Date2.getTime() - Date1.getTime())/(24*60*60*1000);
        }
        return retDays;
    }

    private LineData getLineData(List<dataInfo> inputList, int itemType){
        ArrayList<Entry> entries = new ArrayList<>();
        LineDataSet ldSet = null;
        // long days = 0;
        if (inputList.size()>0){
            switch (itemType){
                case HR:
                    for (int i=0;i<inputList.size();i++){
                        //days = dayBetween("2016/01/01 00:00:00", inputList.get(i).createdDate,"yyyy/MM/dd HH:mm:ss");
                        entries.add(new Entry(i+1,inputList.get(i).HR));
                    }
                    ldSet = new LineDataSet(entries, getString(R.string.hr_title));
                    //ldSet.setColor(Color.BLUE);
                    ldSet.setColor(ColorTemplate.VORDIPLOM_COLORS[HR]);
                    break;
                case VPK:
                    for (int i=0;i<inputList.size();i++){
                        //days = dayBetween("2016/01/01 00:00:00", inputList.get(i).createdDate,"yyyy/MM/dd HH:mm:ss");
                        entries.add(new Entry(i+1,(float)inputList.get(i).Vpk));
                    }
                    ldSet = new LineDataSet(entries,getString(R.string.vpk_title));
                    //ldSet.setColor(Color.RED);
                    ldSet.setColor(ColorTemplate.VORDIPLOM_COLORS[VPK]);
                    break;
                case VTI:
                    for (int i=0;i<inputList.size();i++){
                        //days = dayBetween("2016/01/01 00:00:00", inputList.get(i).createdDate,"yyyy/MM/dd HH:mm:ss");
                        entries.add(new Entry(i+1,(float)inputList.get(i).VTI));
                    }
                    ldSet = new LineDataSet(entries,getString(R.string.vti_title));
                    //ldSet.setColor(Color.BLACK);
                    ldSet.setColor(ColorTemplate.VORDIPLOM_COLORS[VTI]);
                    break;
                case SV:
                    for (int i=0;i<inputList.size();i++){
                        //days = dayBetween("2016/01/01 00:00:00", inputList.get(i).createdDate,"yyyy/MM/dd HH:mm:ss");
                        entries.add(new Entry(i+1,(float)inputList.get(i).SV));
                    }
                    ldSet = new LineDataSet(entries,getString(R.string.sv_title));
                    //ldSet.setColor(Color.BLACK);
                    ldSet.setColor(ColorTemplate.VORDIPLOM_COLORS[SV]);
                    break;
                case CO:
                    for (int i=0;i<inputList.size();i++){
                        //days = dayBetween("2016/01/01 00:00:00", inputList.get(i).createdDate,"yyyy/MM/dd HH:mm:ss");
                        entries.add(new Entry(i+1,(float)inputList.get(i).CO));
                    }
                    ldSet = new LineDataSet(entries,getString(R.string.co_title));
                    //ldSet.setColor(Color.BLACK);
                    ldSet.setColor(ColorTemplate.VORDIPLOM_COLORS[CO]);
                    break;
            }
            if (ldSet != null){
                GIS_Log.d(TAG,"itemType="+itemType +"  set data style");
                //ldSet.setColor(Color.BLACK);
                //ldSet.setCircleColor(Color.BLACK);
                //ldSet.setCircleRadius(4.5f);
                ldSet.setDrawCircles(false);
                ldSet.setLineWidth(2.5f);
                ldSet.setHighLightColor(Color.rgb(244, 117, 117));

                ldSet.setDrawValues(true);
                ldSet.setValueTextSize(9f);
                ldSet.setDrawFilled(false);
                ldSet.setFormSize(15.f);

                ldSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                //ldSet.setCubicIntensity(0.2f);
                //ldSet.setColors(ColorTemplate.VORDIPLOM_COLORS);

                if (Utils.getSDKInt() >= 18) {
                    // fill drawable only supported on api level 18 and above
                    Drawable drawable = ContextCompat.getDrawable(mActivity, R.drawable.fade_blue);
                    ldSet.setFillDrawable(drawable);
                }
                else {
                    ldSet.setFillColor(Color.BLUE);
                }
            }
            ArrayList<ILineDataSet> sets = new ArrayList<>();
            sets.add(ldSet);

            LineData lD = new LineData(sets);
            return lD;
        }
        return null;

    }


    private BarData getBarData(List<dataInfo> inputList,int itemType){
        ArrayList<BarEntry> entries = new ArrayList<>();
        BarDataSet bdSet = null;
       // long days = 0;
        switch (itemType){
            case HR:
                for (int i=0;i<inputList.size();i++){
                    //days = dayBetween("2016/01/01 00:00:00", inputList.get(i).createdDate,"yyyy/MM/dd HH:mm:ss");
                    entries.add(new BarEntry(i+1,inputList.get(i).HR));
                }
                bdSet = new BarDataSet(entries,"HR");
                break;
            case VPK:
                for (int i=0;i<inputList.size();i++){
                    //days = dayBetween("2016/01/01 00:00:00", inputList.get(i).createdDate,"yyyy/MM/dd HH:mm:ss");
                    entries.add(new BarEntry(i+1,(float)inputList.get(i).Vpk));
                }
                bdSet = new BarDataSet(entries,"Vpk");
                break;
            case VTI:
                for (int i=0;i<inputList.size();i++){
                    //days = dayBetween("2016/01/01 00:00:00", inputList.get(i).createdDate,"yyyy/MM/dd HH:mm:ss");
                    entries.add(new BarEntry(i+1,(float)inputList.get(i).VTI));
                }
                bdSet = new BarDataSet(entries,"VTI");
                break;
            case SV:
                for (int i=0;i<inputList.size();i++){
                    //days = dayBetween("2016/01/01 00:00:00", inputList.get(i).createdDate,"yyyy/MM/dd HH:mm:ss");
                    entries.add(new BarEntry(i+1,(float)inputList.get(i).SV));
                }
                bdSet = new BarDataSet(entries,"SV");
                break;
            case CO:
                for (int i=0;i<inputList.size();i++){
                    //days = dayBetween("2016/01/01 00:00:00", inputList.get(i).createdDate,"yyyy/MM/dd HH:mm:ss");
                    entries.add(new BarEntry(i+1,(float)inputList.get(i).CO));
                }
                bdSet = new BarDataSet(entries,"CO");
                break;
        }
        if (bdSet != null){
            bdSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
            bdSet.setBarShadowColor(Color.rgb(203,203,203));
        }
        ArrayList<IBarDataSet> sets = new ArrayList<>();
        sets.add(bdSet);

        BarData bD = new BarData(sets);
        bD.setBarWidth(0.9f);

        return bD;
    }

    private void queryUserDataToList(String userID){
        dataList.clear();
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
    }

    private void queryAllDataToList(){
        dataList.clear();
        Cursor cursor = mHelper.mDBWrite.rawQuery("select *  from "+IwuSQLHelper.STR_TABLE_DATA,null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                dataInfo tmpData = new dataInfo();
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
    }

}
