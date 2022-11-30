package com.gis.heartio.UIOperationControlSubsystem;

import android.app.Dialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
//import androidx.core.app.Fragment;
import android.os.Bundle;
//import androidx.core.app.FragmentTransaction;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.gis.BLEConnectionServices.BluetoothLeService;
import com.gis.CommonUtils.Constants;
import com.gis.heartio.R;
import com.gis.heartio.SupportSubsystem.LoginDatabaseAdapter;
import com.gis.heartio.SupportSubsystem.SystemConfig;
import com.gis.heartio.heartioApplication;

import java.sql.SQLException;
import java.util.Objects;

/**
 * Created by Cavin on 2017/12/15.
 */

public class MoreFragment extends Fragment {
    private static final String TAG = "MoreFragment";
    LoginDatabaseAdapter loginDatabaseAdapter;

    LinearLayout mBatteryLinearLayout;
    TextView m25PercentText;
    TextView mLowBatteryText;

    private heartioApplication mApplication;
    private AppCompatActivity mActivity = null;

    public static MoreFragment newInstance() {
        return new MoreFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = (heartioApplication) getActivity().getApplication();
        mActivity = (AppCompatActivity)getActivity();

        loginDatabaseAdapter = new LoginDatabaseAdapter(getContext());
        try {
            loginDatabaseAdapter = loginDatabaseAdapter.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView =inflater.inflate(R.layout.fragment_item_more, container, false);
        final String[] func = {
              //  getString(R.string.action_data_manager),
                getString(R.string.action_admin_manager),
                getString(R.string.action_about),
                getString(R.string.logout)};
        final String[] func2 = {
           //     getString(R.string.action_data_manager),
                getString(R.string.action_change_password),
                getString(R.string.action_about),
                getString(R.string.logout)};
        /*if (getActivity()!=null&&((AppCompatActivity)getActivity()).getSupportActionBar()!=null){
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getString(R.string.title_more));
        }*/

        if (mActivity!=null){
            Objects.requireNonNull(mActivity.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            mActivity.getSupportActionBar().setDisplayShowHomeEnabled(true);
            setHasOptionsMenu(true);
        }

        mBatteryLinearLayout = rootView.findViewById(R.id.battery_linearlayout);
        m25PercentText = rootView.findViewById(R.id.persent25_text);
        mLowBatteryText = rootView.findViewById(R.id.low_battery_text);
        mBatteryLinearLayout.setVisibility(View.GONE);
        m25PercentText.setVisibility(View.GONE);
        mLowBatteryText.setVisibility(View.GONE);

        updateBatteryStatus();
        getFirmwareVersion();
        //getMACAddress();

        ListView moreList = rootView.findViewById(R.id.more_list);
        final MoreFragment mFragment = this;
        moreList.setOnItemClickListener((arg0, arg1, position, arg3) -> {

            switch (position){
                /*case 0:     //Data Manager
                    //Toast.makeText(getActivity(), func[position], Toast.LENGTH_SHORT).show();
                    updateWithDataFragment(mFragment);
                    break;*/
                /*case 1:     //User Manager
                    updateWithUserFragment(mFragment);
                    break;*/
                case 0:
                    if (MainActivity.currentAdminID.equals("admin")){
                        updateWithAdminFragment(mFragment);
                    }else {
                        if (!MainActivity.currentAdminID.equals("")){
                            showChangePWDialog();
                        }
                    }

                    break;
                case 1:     //About
                    String pkgName = getActivity().getPackageName();
                    String verName;
                    int verCode;
                    if (pkgName!=null){
                        try {
                            PackageInfo pkgInfo = getActivity().getPackageManager().getPackageInfo(pkgName,0);
                            verName = pkgInfo.versionName;
                            verCode = pkgInfo.versionCode;
                            showAboutDialog(verName+"("+verCode+")");
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 2:
                    showLogoutDialog();
                    break;
            }
        });
        if (getActivity()!=null){
            if (MainActivity.currentAdminID!=null){
                ArrayAdapter<String> adapter;
                if (MainActivity.currentAdminID.equals("admin")){
                    adapter = new ArrayAdapter<>(getActivity(), R.layout.listitem_mysimpleitem, func);
                    //new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, func);
                }else{
                    adapter = new ArrayAdapter<>(getActivity(), R.layout.listitem_mysimpleitem, func2);
                            //new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, func2);
                }
                moreList.setAdapter(adapter);
            }

        }

        return rootView;
    }

    @Override
    public void onDestroy() {
        loginDatabaseAdapter.close();
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG,"onCreateOptionMenu");
        inflater.inflate(R.menu.fake_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void showAboutDialog(String verName){
        if (getActivity()!=null){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.MyDialogTheme);
            builder.setTitle(getString(R.string.action_about));
            StringBuilder msg = new StringBuilder();
            msg.append(getString(R.string.about_version_title_2) + verName+"\n");
            if (BluetoothLeService.getConnectionState()==BluetoothLeService.STATE_CONNECTED){
                getFirmwareVersion();
                try{
                    // delay 0.02 second
                    Thread.sleep(80);
                    //Log.d("More","Firmware Version: "+ MainActivity.fwVersion);
                    msg.append("Firmware Version: "+MainActivity.fwVersion+"\n");
                    getMACAddress();
                    Thread.sleep(80);
                    getUDI();
                    Thread.sleep(80);
                    msg.append("UDI: \n"+ MainActivity.UDIStr+"\n");

                } catch(InterruptedException e){
                    e.printStackTrace();

                }


            }

            builder.setMessage(msg.toString());

            builder.setNegativeButton(getString(R.string.close_btn), (dialog, which) -> dialog.cancel());


            AlertDialog alert = builder.create();
            alert.show();

            final Button closeBtn = alert.getButton(AlertDialog.BUTTON_NEGATIVE);
            closeBtn.setBackgroundResource(R.drawable.block_b);
            closeBtn.setTextColor(Color.WHITE);
            closeBtn.setTextSize(18f);
            closeBtn.setScaleX(0.60f);
            closeBtn.setScaleY(0.60f);

        }

    }

    private void showLogoutDialog(){
        if (getActivity()!=null&&getContext()!=null){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(),R.style.MyDialogTheme);
            builder.setTitle(getString(R.string.msg_sure_want_logout));
            builder.setPositiveButton(getString(R.string.alert_message_yes), (dialog, which) -> {
                if (BluetoothLeService.getConnectionState() == BluetoothLeService.STATE_CONNECTED){
                    BluetoothLeService.disconnect();
                }
                Intent intent = new Intent(getActivity(), loginActivity.class);
                startActivity(intent);
                getActivity().finish();
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
        }

    }

    private void showErrorDialog(String title){
        if (getContext()!=null){
            AlertDialog alertIncorrectDialog = new AlertDialog.Builder(getContext(),R.style.MyDialogTheme).create();
            alertIncorrectDialog.setTitle(title);
            alertIncorrectDialog.setButton(Dialog.BUTTON_POSITIVE,
                    getString(R.string.alert_message_exit_ok),
                    (dialogInterface, i) -> {
                        // Do nothing.
                    });
            alertIncorrectDialog.show();

            final Button okBtn = alertIncorrectDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            okBtn.setBackgroundResource(R.drawable.block_b);
            okBtn.setTextColor(Color.WHITE);
            okBtn.setTextSize(18f);
            okBtn.setScaleX(0.60f);
            okBtn.setScaleY(0.60f);
        }

    }

    private void showChangePWDialog(){
        if (getContext()!=null){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext(),R.style.MyDialogTheme);
            alertDialog.setTitle(getString(R.string.action_change_password));
            final EditText oldPass = new EditText(getContext());
            final EditText newPass = new EditText(getContext());
            final EditText confirmPass = new EditText(getContext());


            oldPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
            newPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
            confirmPass.setTransformationMethod(PasswordTransformationMethod.getInstance());

            oldPass.setHint(getString(R.string.hint_old_password));
            newPass.setHint(getString(R.string.hint_new_password));
            confirmPass.setHint(getString(R.string.hint_confirm_password));

            //oldPass.setPadding(20,10,20,0);
            //newPass.setPadding(20,10,20,0);
            //confirmPass.setPadding(20,10,20,0);

            LinearLayout ll=new LinearLayout(getContext());
            ll.setOrientation(LinearLayout.VERTICAL);

            ll.addView(oldPass);

            ll.addView(newPass);
            ll.addView(confirmPass);
            float dpi = getResources().getDisplayMetrics().density;
            ll.setPadding((int)(20*dpi),0,(int)(20*dpi),0);
            alertDialog.setView(ll);
            alertDialog.setPositiveButton(getString(R.string.apply),
                    (dialog, id) -> {
                        if (!oldPass.getText().toString().equals(MainActivity.currentAdminPW)){
                            showErrorDialog(getString(R.string.msg_original_pw_error));
                        }else if(!newPass.getText().toString().equals(confirmPass.getText().toString())){
                            showErrorDialog(getString(R.string.msg_confirm_pw_error));
                        }else{
                            Log.d(TAG,"Old passowrd: "+oldPass.getText().toString()+"   , new passowrd:  "+newPass.getText().toString());
                            loginDatabaseAdapter.updateEntry(MainActivity.currentAdminID,newPass.getText().toString());
                            showErrorDialog(getString(R.string.msg_change_pw_successfully));
                        }
                        dialog.cancel();
                    });
            alertDialog.setNegativeButton(getString(R.string.cancel),
                    (dialog, id) -> dialog.cancel());

            AlertDialog alert11 = alertDialog.create();
            alert11.show();

            final Button applyBtn = alert11.getButton(AlertDialog.BUTTON_POSITIVE);
            applyBtn.setBackgroundResource(R.drawable.block_b);
            applyBtn.setTextColor(Color.WHITE);
            applyBtn.setTextSize(18f);
            applyBtn.setScaleX(0.60f);
            applyBtn.setScaleY(0.60f);

            final Button cancelBtn = alert11.getButton(AlertDialog.BUTTON_NEGATIVE);
            cancelBtn.setBackgroundResource(R.drawable.block_g2);
            cancelBtn.setTextColor(Color.parseColor("#804A4A4A"));
            cancelBtn.setTextSize(18f);
            cancelBtn.setScaleX(0.60f);
            cancelBtn.setScaleY(0.60f);
            cancelBtn.setPadding(100,5,100,5);
        }

    }

    /*private void updateWithUserFragment(final Fragment fragment) {
        if (fragment!=null){
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            userFragment uFrag = userFragment.newInstance();
        *//*fragmentManager.beginTransaction().remove(getFragmentManager().
                findFragmentByTag(Constants.MORE_FRAGMENT_TAG)).commit();*//*
            transaction.replace(R.id.frame_layout, uFrag,
                    Constants.USER_MANAGER_TAG);
            transaction.addToBackStack(fragment.getClass().getName());

            transaction.commit();
        }
    }*/

    private void updateWithAdminFragment(final Fragment fragment) {
        if (fragment!=null&&getActivity()!=null){
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            adminFragment uFrag = adminFragment.newInstance();
            MainActivity.currentFragmentTag = Constants.ADMIN_MANAGER_TAG;
            transaction.replace(R.id.frame_layout, uFrag,
                    Constants.ADMIN_MANAGER_TAG);
            transaction.addToBackStack(fragment.getClass().getName());
            transaction.commit();
        }
    }

    private void updateWithDataFragment(final Fragment fragment){
        if (fragment!=null&&getActivity()!=null){
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            data2Fragment uFrag = data2Fragment.newInstance();
            MainActivity.currentFragmentTag = Constants.DATA_MANAGER_TAG;
            transaction.replace(R.id.frame_layout, uFrag,
                    Constants.DATA_MANAGER_TAG);
            transaction.addToBackStack(fragment.getClass().getName());

            transaction.commit();
        }
    }

    private void getFirmwareVersion(){
        if (BluetoothLeService.getConnectionState()==BluetoothLeService.STATE_CONNECTED){
            BluetoothGattCharacteristic fwCharacteristic = mApplication.getBluetoothgattFirmwareVersioncharacteristic();
            if (fwCharacteristic !=null){
                BluetoothLeService.readCharacteristic(fwCharacteristic);
            }
        }
    }

    private void getMACAddress(){
        if (BluetoothLeService.getConnectionState()==BluetoothLeService.STATE_CONNECTED){
            BluetoothGattCharacteristic macCharacteristic = mApplication.getBluetoothgattMacAddrcharacteristic();
            if (macCharacteristic !=null){
                //Log.d("More","read MAC address.");
                BluetoothLeService.readCharacteristic(macCharacteristic);
            }
        }
    }

    private void getUDI(){
        if (BluetoothLeService.getConnectionState()==BluetoothLeService.STATE_CONNECTED){
            BluetoothGattCharacteristic udiCharacteristic = mApplication.getBluetoothgattUdiParacharacteristic();
            if (udiCharacteristic !=null){
                Log.d("More","read UDI.");
                BluetoothLeService.readCharacteristic(udiCharacteristic);
            }
        }
    }
    private void updateBatteryStatus(){
        if (BluetoothLeService.getConnectionState()==BluetoothLeService.STATE_CONNECTED){
            mBatteryLinearLayout.setVisibility(View.VISIBLE);
            if (mActivity != null){
                int powerLevelNow = SystemConfig.mIntPowerLevel;
                switch (powerLevelNow){
                    case 0:
                    case 1: // <3.5v
                    case 2:  // <3.6v
                    case 3: // <3.7v && >3.6v
                        mBatteryLinearLayout.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.battery_25_layerlist));
                        mLowBatteryText.setVisibility(View.VISIBLE);
                        m25PercentText.setVisibility(View.VISIBLE);
                        break;
                    case 4:  //  <3.9v && >3.8v
                        mBatteryLinearLayout.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.battery_50_layerlist));
                        mLowBatteryText.setVisibility(View.GONE);
                        m25PercentText.setVisibility(View.GONE);
                        break;
                    case 5:   //  <4.0v  && > 3.9v
                        mBatteryLinearLayout.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.battery_75_layerlist));
                        mLowBatteryText.setVisibility(View.GONE);
                        m25PercentText.setVisibility(View.GONE);
                        break;
                    case 6:    //  < 4.1v  &&　> 4.0v
                    case  7:   //   > 4.1v
                        mBatteryLinearLayout.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.battery_full_layerlist));
                        mLowBatteryText.setVisibility(View.GONE);
                        m25PercentText.setVisibility(View.GONE);
                        break;

                    default:
                        mBatteryLinearLayout.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.battery_50_layerlist));
                        mLowBatteryText.setVisibility(View.GONE);
                        m25PercentText.setVisibility(View.GONE);
                        break;
                }
            }
        }else{
            mBatteryLinearLayout.setVisibility(View.GONE);
        }
    }

}
