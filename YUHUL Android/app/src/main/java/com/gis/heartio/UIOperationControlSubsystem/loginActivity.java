package com.gis.heartio.UIOperationControlSubsystem;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.gis.heartio.BuildConfig;
import com.gis.heartio.GIS_Log;
import com.gis.heartio.R;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.IwuSQLHelper;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.LoginDatabaseAdapter;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.SystemConfig;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;


public class loginActivity extends AppCompatActivity {
    private static final String TAG = "loginActivity";
    private TextInputEditText mAccountEditText, mPWEditText;
    private CheckBox mRememberIDCheckbox;
    private IwuSQLHelper mHelper;
    String storedPassword = "";
    LoginDatabaseAdapter loginDatabaseAdapter;

    /* OTA update參數 2023/4/10 by Doris */
//    private static final int version = BuildConfig.VERSION_CODE;
//    boolean updateFlag = false;
//    Thread otaRequest, otaDownload;
//    private DownloadManager downloadManager;

    private SharedPreferences sharedPref;
    private boolean adminPoliciesAccepted;

    private boolean getAdminPoliciesAccepted(){
        return this.sharedPref.getBoolean("adminPoliciesAccepted",false);
    }

    private void setAdminPoliciesAccepted(boolean accepted){
        this.sharedPref.edit().putBoolean("adminPoliciesAccepted", accepted).apply();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try{
            // delay 1 second
            Thread.sleep(1200);

        } catch(InterruptedException e){
            e.printStackTrace();

        }
        setTheme(R.style.myAppThemeNoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.sharedPref = getSharedPreferences("policies", Context.MODE_PRIVATE);

        /* OTA update 2023/4/10 by Doris */
//        (checkWifiIsEnable(wifiManager)){
//            WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
//            if   otaRequest = new otaRequest();
//            otaRequest.start();
//            new Thread(() -> runOnUiThread(() -> {
//                try {
//                    otaRequest.join();
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//                if (updateFlag){
//                    getUpdateDialog();
//                }
//            })).start();
//
//            Log.i("updateFlag: ", String.valueOf(updateFlag));
//        }

        mHelper = new IwuSQLHelper(this);
        mHelper.tryAddDefaultAdmin();
        loginDatabaseAdapter = new LoginDatabaseAdapter(getApplicationContext());

        mRememberIDCheckbox = findViewById(R.id.rememberIdCheckBox);
        Typeface regularTypeface = Typeface.createFromAsset(getAssets(),"Lato-Regular.ttf");
        mRememberIDCheckbox.setTypeface(regularTypeface);

        SharedPreferences setting = getSharedPreferences("admin",MODE_PRIVATE);

        mAccountEditText = findViewById(R.id.accountEditText);
        mPWEditText = findViewById(R.id.pwEditTexit);

        if(SystemConfig.mTestMode) {
            mPWEditText.setText("admin");
        }

        if (!setting.getString("PREF_ADMINID","").equals("")){
            mRememberIDCheckbox.setChecked(true);
            mAccountEditText.setText(setting.getString("PREF_ADMINID",""));
        }else{
            mRememberIDCheckbox.setChecked(false);
        }

        Button mSignUpBtn = findViewById(R.id.signUpBtn);
        mSignUpBtn.setOnClickListener(view -> {
            /*Intent intent = new Intent(loginActivity.this, termOfServiceActivity.class);
            startActivity(intent);*/
            showCheckAdminPWDialog();
            //showTermsOfServiceDialog("");

        });

        Button mLoginBtn = findViewById(R.id.loginBtn);
        Typeface loginFont = Typeface.createFromAsset(getAssets(), "Lato-Bold.ttf");
        mLoginBtn.setTypeface(loginFont);
        mLoginBtn.setOnClickListener(view -> {
            checkValidation();
            //goToMainActivity("");
        });
    }

    @Override
    protected void onDestroy() {
        mHelper.closeDatabase();
        super.onDestroy();
    }

    private void checkValidation(){
        String inputID, inputPassword;

        inputID = mAccountEditText.getText().toString().trim();
        inputPassword = mPWEditText.getText().toString().trim();

        GIS_Log.d(TAG,"input ID: "+inputID+"  , input password: "+inputPassword);

        try {
            loginDatabaseAdapter = loginDatabaseAdapter.open();

            if (inputID.equals("")||inputPassword.equals("")){
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle(getString(R.string.filled_all));
                alertDialog.setButton(Dialog.BUTTON_POSITIVE,"OK", (dialogInterface, i) -> {
                    // Do nothing.
                });
                alertDialog.show();
                return;
            }
            if (!inputID.equals("")){
                storedPassword = loginDatabaseAdapter.getSingleEntry(inputID);
                if (storedPassword.equals("NOT EXIST")){
                    AlertDialog alertUserNotExistDialog = new AlertDialog.Builder(this).create();
                    alertUserNotExistDialog.setTitle(getString(R.string.account_not_exist));
                    alertUserNotExistDialog.setButton(Dialog.BUTTON_POSITIVE,"OK", (dialogInterface, i) -> {
                        // Do nothing.
                    });
                    alertUserNotExistDialog.show();
                    return;
                }
                if (inputPassword.equals(storedPassword)){

                    SharedPreferences setting = getSharedPreferences("admin",MODE_PRIVATE);
                    if (mRememberIDCheckbox.isChecked()){
                        setting.edit().putString("PREF_ADMINID",inputID).commit();
                    }else{
                        setting.edit().putString("PREF_ADMINID","").commit();
                    }
                    if (inputID.equals("admin")&&!getAdminPoliciesAccepted()){
                        showTermsOfServiceDialog(inputID);
                    }else{
                        goToMainActivity(inputID);
                    }

                }else{
                    AlertDialog alertIncorrectDialog = new AlertDialog.Builder(this).create();
                    alertIncorrectDialog.setTitle(getString(R.string.incorrect_username_password));
                    alertIncorrectDialog.setButton(Dialog.BUTTON_POSITIVE,"OK", (dialogInterface, i) -> {
                        // Do nothing.
                    });
                    alertIncorrectDialog.show();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void showCheckAdminPWDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this,R.style.MyAdminCheckDialogTheme);
        //AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        //alertDialog.setTitle(getString(R.string.action_change_password));
        alertDialog.setTitle(getString(R.string.input_pw_admin));
        final EditText pwEditText = new EditText(this);

        pwEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());


        pwEditText.setHint(getString(R.string.hint_input_password));
        pwEditText.setHintTextColor(0X88FFFFFF);
        pwEditText.setTextColor(0XFFFFFFFF);
        //pwEditText.setPadding(20,10,20,0);

        LinearLayout ll=new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        //ll.setBackgroundColor(0XFF2F51CE);

        ll.addView(pwEditText);

        float dpi = getResources().getDisplayMetrics().density;
        ll.setPadding((int)(20*dpi),0,(int)(20*dpi),0);
        alertDialog.setView(ll);
        alertDialog.setPositiveButton(getString(R.string.apply),
                (dialog, id) -> {
                    if (!pwEditText.getText().toString().equals(loginDatabaseAdapter.getSingleEntry("admin"))){
                        //showErrorDialog(getString(R.string.msg_original_pw_error));
                        GIS_Log.d(TAG,"admin password error");
                        Toast.makeText(loginActivity.this,
                                getString(R.string.admin_pw_error),
                                Toast.LENGTH_SHORT).show();
                    }else{
                        showTermsOfServiceDialog("");
                    }
                    dialog.cancel();
                });
        alertDialog.setNegativeButton(getString(R.string.cancel),
                (dialog, id) -> dialog.cancel());

        AlertDialog alert11 = alertDialog.create();
        alert11.show();

        final Button applyBtn = alert11.getButton(AlertDialog.BUTTON_POSITIVE);
        //applyBtn.setBackgroundResource(R.drawable.block_b);
        //applyBtn.setTextColor(Color.WHITE);
        applyBtn.setBackgroundResource(R.drawable.block_g2);
        applyBtn.setTextColor(Color.BLACK);
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

    private void showTermsOfServiceDialog(final String inputID){
        /*PrivacyPolicyDialog dialog = new PrivacyPolicyDialog(this,
                "https://localhost/terms",
                "https://localhost/privacy");*/
        myPrivacyPolicyDialog dialog = new myPrivacyPolicyDialog(this,
                "https://drive.google.com/open?id=1Qz5EFFCUJnOH14uZnqoFjRTVRF109ryY",
                getString(R.string.privacy_policy_url));

        dialog.addPoliceLine(getString(R.string.this_app_requires));
        dialog.addPoliceLine(getString(R.string.all_details_about_use_of_data));
        dialog.setOnClickListener(new myPrivacyPolicyDialog.OnClickListener() {
            @Override
            public void onAccept(Boolean isFirstTime) {
                GIS_Log.d(TAG, "Policies accepted");
                if (inputID.equals("admin")){
                    goToMainActivity(inputID);
                    loginActivity.this.setAdminPoliciesAccepted(true);
                    finish();
                }else{
                    Intent intent = new Intent(loginActivity.this, registerActivity.class);
                    startActivity(intent);
                }

            }

            @Override
            public void onCancel() {
                GIS_Log.d(TAG, "Policies not accepted");
                if (inputID.equals("admin")){
                    loginActivity.this.setAdminPoliciesAccepted(false);
                }
                //finish();
            }
        });


        dialog.setTitleTextColor(Color.parseColor("#222222"));
        dialog.setAcceptButtonColor(ContextCompat.getColor(this, R.color.colorAccent));

        dialog.setTitle(getString(R.string.term_of_serivce));
        dialog.setTermsOfServiceSubtitle(getString(R.string.terms_of_service_subtitle));

        dialog.setAcceptText(getString(R.string.agree));
        dialog.setCancelText(getString(R.string.disagree));

        dialog.show();


    }

    private void goToMainActivity(String inputAdmin){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("adminID",inputAdmin);
        intent.putExtra("adminPW",storedPassword);
        startActivity(intent);
        finish();
    }

    /* OTA update function --start-- 2023/4/10 by Doris */
//    class otaRequest extends Thread{
//        @Override
//        public void run() {
//            super.run();
//            // 建立OkHttpClient
//            OkHttpClient client = new OkHttpClient().newBuilder()
//                    .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
//                    .build();
//
//            Request request = new Request.Builder()
//                    .url("http://ec2-54-252-152-206.ap-southeast-2.compute.amazonaws.com/apk.php?latest_version")
//                    .build();
//
//            // 建立Call
//            Call call = client.newCall(request);
//
//            //同步
//            try {
//                Response response = call.execute();
//                String result = response.body().string();
//
//                JSONObject jObject = new JSONObject(result);
//                if(jObject.getInt("version") > version){
//                    Log.i(TAG, "need to update apk");
//                    updateFlag = true;
//                }
//
//            } catch (IOException | JSONException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
//
//    class otaDownload extends Thread{
//        @Override
//        public void run() {
//            clearDownloadFile();
//            DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http://ec2-54-252-152-206.ap-southeast-2.compute.amazonaws.com/apk.php?get_version"));
//            //設定通知提示
//            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
//            request.setTitle("下載");
//            request.setDescription("正在下載新版APP");
//            request.setAllowedOverRoaming(false);
//            //下載後存在哪
//            request.setDestinationInExternalFilesDir(loginActivity.this, Environment.DIRECTORY_DOWNLOADS, "/temp.apk");
//            downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
//            long id = downloadManager.enqueue(request);
//            downloadManager.getUriForDownloadedFile(id);
//
//            CompleteReceiver completeReceiver = new CompleteReceiver();
//            /*register download success broadcast */
//            registerReceiver(completeReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
//        }
//    }
//
//    class CompleteReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            updateFlag = false;
//            // get complete download id
//            long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
//            Toast.makeText(loginActivity.this, "下載完成~", Toast.LENGTH_LONG).show();
//
//            // Intent to open apk
//            Intent intentAPK = new Intent(Intent.ACTION_VIEW, downloadManager.getUriForDownloadedFile(completeDownloadId));
//            intentAPK.setDataAndType(downloadManager.getUriForDownloadedFile(completeDownloadId), "application/vnd.android.package-archive");
//            intentAPK.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            startActivity(intentAPK);
//        }
//    }
//
//    public void clearDownloadFile(){
//        String directory = "/storage/emulated/0/Android/data/com.gis.heartio/files/Download";
//        File fileDir = new File(directory);
//        if (fileDir.exists()) {
//            File[] listFiles = fileDir.listFiles();
//            Log.d("listFiles", String.valueOf(listFiles.length));
//            for (File listFile : listFiles) {
//                if (!listFile.delete()) {
//                    Log.e("Unable to delete file", String.valueOf(listFile));
//                }
//            }
//        }
//    }
//
//    private boolean checkWifiIsEnable(WifiManager wifiManager){
//        Log.d("Wifi", String.valueOf(wifiManager.isWifiEnabled()));
//        return wifiManager.isWifiEnabled();
//    }
//
//    public void getUpdateDialog() {
//        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(loginActivity.this);
//        builder.setTitle("Detected new version")
//                .setMessage("Want to update?")
//                .setPositiveButton("GO", (dialogInterface, i) -> {
//                    Toast.makeText(loginActivity.this, "開始下載", Toast.LENGTH_SHORT).show();
//                    otaDownload = new otaDownload();
//                    otaDownload.start();
//                    try {
//                        otaDownload.join();
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                })
//                .setNegativeButton("NO", (dialogInterface, i) -> dialogInterface.dismiss())
//                .create()
//                .show();
//    }
    /* OTA update function --end-- 2023/4/12 by Doris */
}
