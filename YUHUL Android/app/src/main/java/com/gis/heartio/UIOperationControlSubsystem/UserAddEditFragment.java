package com.gis.heartio.UIOperationControlSubsystem;

import static com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.SystemConfig.cloudToken;
import static com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.Utilitys.getToken;
import static com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.Utilitys.isNetOnline;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.gis.heartio.GIS_Log;
import com.gis.heartio.R;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.IwuSQLHelper;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.userInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

//* {@link UserAddEditFragment.OnFragmentInteractionListener} interface
/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link UserAddEditFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
/* 改寫編輯使用者畫面&重新定義資料庫欄位 by Doris 2024/05/08 */
public class UserAddEditFragment extends Fragment {
    private static final String TAG = "UserAddEditFragment";

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private EditText mUserNameText, mPhoneNumberText, mBirthdayText, mJobText, mPulmDiaEditText, mNationalityText, mOperationText, mDiseaseText;
//    private EditText mUserIDEditText, mFirstNameEditText, mLastNameEditText;
//    private EditText mPulmDiaEditText, mPhoneNumber, mBirthday;
//    private EditText mAgeEditText;
    private RadioGroup mGenderGroup, mMarryGroup;
    private TextView mHospitalText, mUserIDEditText;
    private Spinner mHospitalSpinner;
    private int intGender = -1;
    private int intMarry = -1;
    private int mbirthdayYear, mbirthdayMonth, mbirthdayDay;
    private boolean isBirthday = false;
    private String selectBirthdayDateStr = null;
//    private final String[] selectHospitalStr = new String[]{"選擇醫院"};
    static ArrayList<String> selectHospitalStr = new ArrayList<>();
    private static IwuSQLHelper mHelper;
    private String inputUserPriID = null;
    private AppCompatActivity mActivity = null;
    private WifiManager wifiManager;
    private static boolean getHospitalList = false;
    static ArrayAdapter<String> adapter;

    /*    private OnFragmentInteractionListener mListener;*/

    public UserAddEditFragment() {
        // Required empty public constructor
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment UserAddEditFragment.
     */

    public static UserAddEditFragment newInstance() {
        return new UserAddEditFragment();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserAddEditFragment.
     */
    public static UserAddEditFragment newInstance(String param1, String param2) {
        UserAddEditFragment fragment = new UserAddEditFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        if (!getHospitalList){
            Thread getHospitalThread = new getHospitalRequest();
            getHospitalThread.start();
        }
//        getHospital();
        if (getArguments() != null) {
            inputUserPriID = getArguments().getString(ARG_PARAM1);
        }
        mActivity = (AppCompatActivity)getActivity();
        mHelper = new IwuSQLHelper(mActivity);
    }

    @Override
    public void onDestroy() {
        mHelper.closeDatabase();
        super.onDestroy();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView =inflater.inflate(R.layout.fragment_user_add_edit, container, false);

        if (mActivity!=null){
            Objects.requireNonNull(mActivity.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            mActivity.getSupportActionBar().setDisplayShowHomeEnabled(true);
            setHasOptionsMenu(true);
        }
        Log.d(TAG, "onCreateView");

        Button resetBtn = rootView.findViewById(R.id.resetBtn);
        Button applyBtn = rootView.findViewById(R.id.userApplyBtn);

        mUserIDEditText = rootView.findViewById(R.id.userIDeditText);
        mUserNameText = rootView.findViewById(R.id.userName);
        mPhoneNumberText = rootView.findViewById(R.id.phoneNumber);
        mBirthdayText = rootView.findViewById(R.id.birthday);
        mJobText = rootView.findViewById(R.id.job);
        mPulmDiaEditText = rootView.findViewById(R.id.pulmDiaeditText);
        mNationalityText = rootView.findViewById(R.id.nationality);
        mOperationText = rootView.findViewById(R.id.operation);
        mDiseaseText = rootView.findViewById(R.id.disease);
        mHospitalSpinner = rootView.findViewById(R.id.hospital);
        mHospitalText = rootView.findViewById(R.id.hospitalText);

//        selectHospitalStr.add("test0");
//        selectHospitalStr.add("test1");
//        selectHospitalStr.add("test2");
        Log.d(TAG, String.valueOf(selectHospitalStr));
//        String text = mHospitalSpinner.getSelectedItem().toString();
        adapter = new ArrayAdapter<>(mActivity,android.R.layout.simple_spinner_item, selectHospitalStr);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);

        mHospitalSpinner.setAdapter(adapter);
//        mHospitalSpinner.setPrompt("選擇醫院");
        mHospitalSpinner.setSelection(0, false);

        mHospitalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                Log.d(TAG, "選擇監聽");
                String selectedItem = parent.getItemAtPosition(position).toString();
                if(!(selectedItem.equals("選擇醫院"))){
                    Log.d(TAG, "selectedItem" + selectedItem);
                    mHospitalText.setText(selectedItem);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "監聽不到");
            }
        });

        ImageButton birthdayDateButton = rootView.findViewById(R.id.birthdayDateButton);
        birthdayDateButton.setOnClickListener(view -> {
            final Calendar c = Calendar.getInstance();
            int chooseYear, chooseMonth, chooseDay;
            chooseYear = c.get(Calendar.YEAR);
            chooseMonth = c.get(Calendar.MONTH);
            chooseDay = c.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(mActivity, (view1, year, month, day) -> {
                mbirthdayYear = year;
                mbirthdayMonth = month;
                mbirthdayDay = day;
                selectBirthdayDateStr = String.valueOf(year) + "-"
                        + String.valueOf(month + 1) + "-"
                        + String.valueOf(day);
//                Log.d(TAG, selectBirthdayDateStr);
                mBirthdayText.setText(selectBirthdayDateStr);
                isBirthday = true;
            }, chooseYear, chooseMonth, chooseDay).show();
        });

//        mAgeEditText = rootView.findViewById(R.id.ageEditTextNumber);
//        mFirstNameEditText = rootView.findViewById(R.id.firstnameeditText);
//        mLastNameEditText = rootView.findViewById(R.id.lastnameeditText);

        if (mActivity.getSupportActionBar()!=null){
            GIS_Log.d(TAG,"inputUserPriID = "+inputUserPriID);
            if (inputUserPriID==null){
                mActivity.getSupportActionBar().setTitle(getString(R.string.title_add_user));
                resetBtn.setVisibility(View.VISIBLE);
                applyBtn.setText(R.string.create);

                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle("个人信息收集/使用 同意书")
                        .setMessage("\n佰晨泰佑（上海）智能科技有限公司（下称本公司）业务服务管理系统需要收集与使用用户的个人信息，根据《中华人民共和国网络安全法》等相关法规，特告知如下。\n" +
                                "请仔细阅读并理解文本全部内容后，决定是否同意并使用本公司服务管理系统；如您仍决定使用本公司服务管理系统的各项服务，则视同您同意执行本同意书。\n" +
                                "\n\n" +
                                "第1条\t定义\n" +
                                "1.1 “用户”：使用本公司服务管理系统的自然人。\n" +
                                "1.2 “本公司”：运营及管理服务管理系统的佰晨泰佑（上海）智能科技有限公司。\n" +
                                "1.3 “服务管理系统”：需要收集和使用个人信息的本公司服务管理系统，该系统的功能范围可能不断扩大。\n" +
                                "1.4 “个人信息”：指通过本公司的产品采集到的，以电子或其他方式记录的能够单独或与其他信息结合的各种信息，包括与确定自然人相关的生物特征、位置、行为、健康数据等信息，如姓名、出生日期、身份证号、个人账号、手机号码、指纹、心率、每博量、心输出量等。\n" +
                                "\n\n" +
                                "第2条\t个人信息内容和目的\n" +
                                "2.1 个人信息内容 见1.4条\n" +
                                "2.2 收集/利用的目的\n" +
                                "   1） 识别个人信息主体\n" +
                                "   2） 用户使用本公司产品所获得的数据信息的收集、分析\n" +
                                "   3） 为用户提供数据分析、建议的服务\n" +
                                "   4） 本仪器测量数据仅供参考，不作为医疗诊断依据，如有不适，请到医院就诊。\n" +
                                "2.3 个人信息收集最小化\n" +
                                "   本公司不收集超出个人信息必须范围的个人敏感信息，例如财产、生活应是等个人敏感信息。\n" +
                                "2.4 用户享有对个人信息的访问、更正、删除个人信息、注销账户等权利。\n" +
                                "2.5 本公司收集的个人信息不会用于直接商业营销。\n" +
                                "\n\n" +
                                "第3条\t个人信息的存储和使用时间\n" +
                                "3.1 本公司与用户服务关系存续期间。\n" +
                                "3.2 从个人信息收集日开始一直到服务关系终止为止。\n" +
                                "3.3 本公司获取的个人所有信息，除了以上提及的收集/使用目的或法律规定的使用目的以外，不会用于其他用途。依据相关法规，如无正当的理由继续保存个人信息，或个人信息收集使用目的已经达成，本公司会立即删除用户相关个人信息。用户如要求删除个人信息，且相关法规规定不要求保存此类个人信息时，自接收用户删除要求后，本公司会及时删除所有个人信息。\n" +
                                "\n\n" +
                                "第4条\t拒绝同意权利\n" +
                                "4.1 用户可拒绝接受收集和利用个人信息，但因此而无法实现与本公司的正常服务提供，甚至导致服务关系无法正常履行，所带来的负面后果由用户承担，请审慎留意。\n" +
                                "4.2 用户同意签署此同意书，即表示已仔细阅读并理解以上所有事项，同意相关个人信息的收集和使用。\n" +
                                "\n\n" +
                                "第5条\t同意书的效力和变更\n" +
                                "5.1 本同意书从用户同意之日起开始生效。\n" +
                                "   如本用户已经使用本公司业务服务管理系统的，则同意书的签署同时视为对之前已经发生的个人信息收集与利用的同意。\n" +
                                "5.2 本公司保留运营或者营业上有重要原因时，对本同意书进行修改的权利。但即使修改也会在《中华人民共和国网络安全法》及其相关法规允许的范围内进行修订。\n" +
                                "5.3 如对本同意书进行修订，本公司将提前至少7天将修订内容、实施时间、修订原因公布在公司服务管理系统首页。\n" +
                                "5.4 用户有全力拒绝同意修订。在用户拒绝同意修订的情况下，本公司有权终止相关服务，禁止用户登录。\n" +
                                "\n\n" +
                                "第6条\t争议的解决与联系方式\n" +
                                "6.1 本同意书的订立、执行和争议的解决均适用中华人民共和国法律。\n" +
                                "6.2 如双方就本同意书内容或者其执行发生任何争议，双方应尽量友好协商解决；协商不成时，任何一方可由本公司所在地有管辖权的人民法院提起诉讼。\n" +
                                "6.3 本公司保留对同意书的解释和修改权利。\n" +
                                "6.4 本公司未形式本协议的任何权利或规定，不构成对前述权利之放弃。\n" +
                                "6.5 如本同意书中的任何条款完全或部分无效，本同意书的其余条款仍有效并且有约束力。\n" +
                                "6.6 若您对本同意书的内容、执行有其他疑问、建议、意见等，欢迎通过下列方式与本公司联系：https://www.be-plus.com.cn\n" +
                                "\n\n" +
                                "第7条\t附则\n" +
                                "7.1 本同意书通过用户登录点击确认方式签署，具有与纸质签署同等法律效力。\n\n")
                        .setPositiveButton("同意", (dialog, which) -> {
                            Toast.makeText(mActivity, "已授權同意", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("不同意", (dialog, which) -> {
                            Toast.makeText(mActivity, "不同意即無法使用本產品", Toast.LENGTH_SHORT).show();
                            mActivity.onBackPressed();
                        });

                AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false); // 禁止點擊 AlertDialog 以外的區域取消
                dialog.setCancelable(false); // 禁止按 [手機返回鍵] 取消
                dialog.show();
            }else{
                mActivity.getSupportActionBar().setTitle(getString(R.string.title_edit_user));
                resetBtn.setVisibility(View.GONE);
                applyBtn.setText(R.string.apply);
            }
        }

        mGenderGroup = rootView.findViewById(R.id.genderGroup);
        mGenderGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if(checkedId == R.id.femaleRadioButton){
                intGender = userInfo.FEMALE;
            }else if(checkedId == R.id.maleRadioButton){
                intGender = userInfo.MALE;
            }
        });

        mMarryGroup = rootView.findViewById(R.id.marryGroup);
        mMarryGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if(checkedId == R.id.marriedRadioButton){
                intMarry = 1;
            }else if(checkedId == R.id.unmarriedRadioButton){
                intMarry = 0;
            }
        });

        mPulmDiaEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (mPulmDiaEditText.getText().length() == 0 && mBirthdayText.getText().length() != 0) {
                    try {
                        if (!isBirthday){
                            Calendar c = Calendar.getInstance();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                            c.setTime(dateFormat.parse(mBirthdayText.getText().toString()));
                            mbirthdayYear = c.get(Calendar.YEAR);
                            mbirthdayMonth = c.get(Calendar.MONTH)+1;
                            mbirthdayDay = c.get(Calendar.DAY_OF_MONTH);
//                            Log.d(TAG, "mbirthdayYear = " + mbirthdayYear);
//                            Log.d(TAG, "mbirthdayMonth = " + mbirthdayMonth);
//                            Log.d(TAG, "mbirthdayDay = " + mbirthdayDay);
                        }
                        Calendar todayTime = Calendar.getInstance();
                        int countAge = todayTime.get(Calendar.YEAR) - mbirthdayYear;
                        if(todayTime.get(Calendar.MONTH)+1-mbirthdayMonth < 0){
                            countAge--;
                        }
                        Log.d(TAG, "Age = " + countAge);
                        if (countAge > 0 && intGender != -1) {
                            mPulmDiaEditText.setText(String.valueOf(getPADiaByGenderAge(intGender, countAge)));
                        }

                    } catch (Exception ex1) {
                        ex1.printStackTrace();
                    }
                }
            }
        });

        if (inputUserPriID!=null){
            userInfo editUser = IwuSQLHelper.getUserInfoFromPrimaryID(inputUserPriID, mHelper);
//            Log.d(TAG, "inputUserPriID = " + inputUserPriID);
            mUserIDEditText.setText(editUser.userID);
            mUserNameText.setText(editUser.name);
            mPhoneNumberText.setText(editUser.phoneNumber);
            mJobText.setText(editUser.job);
            mNationalityText.setText(editUser.nationality);
            mOperationText.setText(editUser.operation);
            mDiseaseText.setText(editUser.disease);
//            mFirstNameEditText.setText(editUser.firstName);
//            mLastNameEditText.setText(editUser.lastName);
            mPulmDiaEditText.setText(String.valueOf(editUser.pulmDiameter));
            mBirthdayText.setText(editUser.birthday);
            mHospitalText.setText(editUser.hospital);
            Log.d(TAG, "editUser.hospital = " + editUser.hospital);
            Log.d(TAG, "editUser.userID = " + editUser.userID);

            if (editUser.gender == userInfo.FEMALE){
                mGenderGroup.check(R.id.femaleRadioButton);
            } else {
                mGenderGroup.check(R.id.maleRadioButton);
            }
            if (editUser.marry == 1){
                mMarryGroup.check(R.id.marriedRadioButton);
            } else {
                mMarryGroup.check(R.id.unmarriedRadioButton);
            }
        }

        resetBtn.setOnClickListener(view -> {
//            mUserIDEditText.setText("");
            mUserNameText.setText("");
            mPhoneNumberText.setText("");
            mBirthdayText.setText("");
            mJobText.setText("");
            mNationalityText.setText("");
            mOperationText.setText("");
            mDiseaseText.setText("");
//            mFirstNameEditText.setText("");
//            mLastNameEditText.setText("");
            mGenderGroup.clearCheck();
            mMarryGroup.clearCheck();
//            mAgeEditText.setText("");
            mPulmDiaEditText.setText("");
            mHospitalText.setText("所選醫院");
            intGender = -1;
            intMarry = -1;
        });


        applyBtn.setOnClickListener(view -> {
            if (isInputComplete()){
                userInfo addUser = new userInfo();

//                addUser.firstName = mFirstNameEditText.getText().toString();
//                addUser.lastName = mLastNameEditText.getText().toString();
                addUser.name = mUserNameText.getText().toString();
//                addUser.userID = mUserIDEditText.getText().toString();
                addUser.pulmDiameter = Double.parseDouble(mPulmDiaEditText.getText().toString());
                addUser.gender = intGender;
                addUser.marry = intMarry;
//                addUser.age = Integer.parseInt(mAgeEditText.getText().toString());
                addUser.birthday = mBirthdayText.getText().toString();
                addUser.phoneNumber = mPhoneNumberText.getText().toString();
                addUser.job = mJobText.getText().toString();
                addUser.nationality = mNationalityText.getText().toString();
                addUser.operation = mOperationText.getText().toString();
                addUser.disease = mDiseaseText.getText().toString();
                addUser.hospital = mHospitalSpinner.getSelectedItem().toString();

                if (inputUserPriID==null){
                    addUser.userID = generateID();
                    if (!isUserExist(addUser.userID)){
                        addUserInfoToDB(addUser);
                        if (getUserCount()==1){
                            UserManagerCommon.initUserUltrasoundParameter(addUser);
                            MainActivity.mRawDataProcessor.updateStrBaseFolder();
                        }
                        /* 串接新增使用者API 2024/07/23 by Doris */
                        if (isNetOnline(mActivity)){
                            getToken(mActivity);
                            OkHttpClient client = new OkHttpClient().newBuilder()
                                    .build();
                            MediaType mediaType = MediaType.parse("application/json");
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("idNumber", addUser.userID);
                                jsonObject.put("name", addUser.name);
                                jsonObject.put("phone", addUser.phoneNumber);
                                jsonObject.put("birthday", addUser.birthday);
                                jsonObject.put("job", addUser.job);
                                jsonObject.put("sex", String.valueOf(addUser.gender));
                                jsonObject.put("maritalStatus", String.valueOf(addUser.marry));
                                jsonObject.put("pulmonary", String.valueOf(addUser.pulmDiameter));
                                jsonObject.put("nation", addUser.nationality);
                                jsonObject.put("operation", addUser.operation);
                                jsonObject.put("disease", addUser.disease);
                                jsonObject.put("hospital", addUser.hospital);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

//                            FormBody formBody = new FormBody.Builder()
//                                    .add("idNumber", addUser.userID)
//                                    .add("name", addUser.name)
//                                    .add("phone", addUser.phoneNumber)
//                                    .add("birthday", "2001-7-23")
//                                    .add("job", addUser.job)
//                                    .add("sex", String.valueOf(addUser.gender))
//                                    .add("maritalStatus", String.valueOf(addUser.marry))
//                                    .add("pulmonary", String.valueOf(addUser.pulmDiameter))
//                                    .add("nation", addUser.nationality)
//                                    .add("operation", addUser.operation)
//                                    .add("disease", addUser.disease)
//                                    .add("hospital", addUser.hospital)
//
//                                    .build();

                            RequestBody formBody = RequestBody.create(mediaType, jsonObject.toString());
                            Request request = new Request.Builder()
                                    .url("http://139.196.4.88:8081/beplus/medical/patient/add")
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

                                        mActivity.runOnUiThread(()->{
                                            if (status.equals("true")){
                                                Toast.makeText(mActivity, "使用者資料上傳成功", Toast.LENGTH_SHORT).show();
                                            }else {
                                                Toast.makeText(mActivity, "使用者資料上傳失敗：" + message, Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                        Log.d(TAG, "success = "+status);
                                    } catch (IOException | JSONException e) {
                                        mActivity.runOnUiThread(()->{
                                            Toast.makeText(mActivity, "伺服器連線異常", Toast.LENGTH_SHORT).show();
                                        });
                                        e.printStackTrace();
                                    }
                                }
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    //當連線失敗
                                    e.printStackTrace();
                                }
                            });
                        }

                        mActivity.onBackPressed();
                    }else{
                        showInputErrorDialog(getString(R.string.msg_id_existed));
                    }
                }else{
                    addUser.userCount = Integer.parseInt(inputUserPriID);
                    addUser.userID = IwuSQLHelper.getUserInfoFromPrimaryID(inputUserPriID, mHelper).userID;
                    updateUserInfoToDB(addUser);
                    SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);
                    int currentUserID_primary = sharedPref.getInt(IwuSQLHelper.KEY_CURRENT_USER,1);
                    if (currentUserID_primary == addUser.userCount){
                        UserManagerCommon.initUserUltrasoundParameter(addUser);
                        MainActivity.mRawDataProcessor.updateStrBaseFolder();
                    }

                    /* 串接編輯使用者API 2024/07/23 by Doris */
                    if (isNetOnline(mActivity)){
                        getToken(mActivity);
                        OkHttpClient client = new OkHttpClient().newBuilder()
                                .build();
                        MediaType mediaType = MediaType.parse("application/json");
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("idNumber", addUser.userID);
                            jsonObject.put("name", addUser.name);
                            jsonObject.put("phone", addUser.phoneNumber);
                            jsonObject.put("birthday", addUser.birthday);
                            jsonObject.put("job", addUser.job);
                            jsonObject.put("sex", String.valueOf(addUser.gender));
                            jsonObject.put("maritalStatus", String.valueOf(addUser.marry));
                            jsonObject.put("pulmonary", String.valueOf(addUser.pulmDiameter));
                            jsonObject.put("nation", addUser.nationality);
                            jsonObject.put("operation", addUser.operation);
                            jsonObject.put("disease", addUser.disease);
                            jsonObject.put("hospital", addUser.hospital);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

//                            FormBody formBody = new FormBody.Builder()
//                                    .add("idNumber", addUser.userID)
//                                    .add("name", addUser.name)
//                                    .add("phone", addUser.phoneNumber)
//                                    .add("birthday", "2001-7-23")
//                                    .add("job", addUser.job)
//                                    .add("sex", String.valueOf(addUser.gender))
//                                    .add("maritalStatus", String.valueOf(addUser.marry))
//                                    .add("pulmonary", String.valueOf(addUser.pulmDiameter))
//                                    .add("nation", addUser.nationality)
//                                    .add("operation", addUser.operation)
//                                    .add("disease", addUser.disease)
//                                    .add("hospital", addUser.hospital)
//
//                                    .build();

                        RequestBody formBody = RequestBody.create(mediaType, jsonObject.toString());
                        Request request = new Request.Builder()
                                .url("http://139.196.4.88:8081/beplus/medical/patient/edit")
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
                                    if (status.equals("false")){
//                                            AlertDialog alertDialog = new AlertDialog.Builder(mActivity).create();
//                                            alertDialog.setTitle("上傳雲端失敗");
//                                            alertDialog.setMessage(jObject.getString("message"));
//                                            alertDialog.setButton(Dialog.BUTTON_POSITIVE,"OK", (dialogInterface, i) -> {
//                                                // Do nothing.
//                                            });
//                                            alertDialog.show();
//                                            Looper.prepare();
//                                            Toast.makeText(mActivity, "上傳雲端失敗\n"+ jObject.getString("message"), Toast.LENGTH_LONG).show();
//                                            Log.d(TAG, jObject.getString("message"));
//                                            Looper.loop();

//                                            JSONObject tokenResult = jObject.getJSONObject("result");
//                                            cloudToken = tokenResult.getString("token");
                                    }
                                    Log.d(TAG, "success = "+status);
                                    Log.d(TAG, "message = "+jObject.getString("message"));
//                        Log.d(TAG, "token = "+cloudToken);
                                } catch (IOException | JSONException e) {
//                                        Looper.prepare();
//                                        Toast.makeText(mActivity, "伺服器連線異常", Toast.LENGTH_SHORT).show();
//                                        Looper.loop();
                                    e.printStackTrace();
                                }
                            }
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                //當連線失敗
                                e.printStackTrace();
                            }
                        });
                    }
                    mActivity.onBackPressed();
                }
            }
        });
        return rootView;
    }

    // 用onStart監聽是否已經取得醫院list並更改UI畫面
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        while (true){
            if (getHospitalList){
                adapter.notifyDataSetChanged();
                Log.d(TAG, "notifyDataSetChanged");
                break;
            }
        }
    }

    /* To get hospital name API from cloud 2024/05/20 by Doris */
    private static class getHospitalRequest extends Thread{
//                @Override
        public void run() {
            super.run();
            // 建立OkHttpClient
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                    .build();

            Request request = new Request.Builder()
                    .url("http://139.196.4.88:8081/beplus/sys/api/getAllSysDepart")
                    .addHeader("X-Access-Token", cloudToken)
                    .build();

            // 建立Call
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response){
                    //連線成功，取得回傳response
                    try {
                        String result = response.body().string();
                        Log.i(TAG, "hospital = " + result);
                        selectHospitalStr.add("選擇醫院");

//                        JSONObject jObject = new JSONObject(result);
                        JSONArray hospitalArray = new JSONArray(result);
                        for (int i=0; i<hospitalArray.length(); i++){
                            JSONObject jObject = hospitalArray.getJSONObject(i);
                            Log.d(TAG, jObject.getString("id"));
                            selectHospitalStr.add(jObject.getString("departName"));
                        }
                        getHospitalList = true;
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    //當連線失敗
                    e.printStackTrace();
                }
            });
        }
    }

    // To get hospital name API from cloud
    private void getHospital(){
            // 建立OkHttpClient
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                    .build();

            Request request = new Request.Builder()
                    .url("http://47.116.220.134:8081/beplus/sys/api/getAllSysDepart")
                    .addHeader("X-Access-Token", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3MTU0NDc0NzgsInVzZXJuYW1lIjoibW9iaWxlIn0.r_QbSzl0vQS91hthQkVlO--nD8WfA1nnQ_hNarrGtec")
                    .build();

            // 建立Call
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response){
                    //連線成功，取得回傳response
                    try {
                        String result = response.body().string();
                        Log.i(TAG, "hospital = " + result);
//                        selectHospitalStr.add("選擇醫院");

//                        JSONObject jObject = new JSONObject(result);
                        JSONArray hospitalArray = new JSONArray(result);
                        for (int i=0; i<hospitalArray.length(); i++){
                            JSONObject jObject = hospitalArray.getJSONObject(i);
                            Log.d(TAG, jObject.getString("id"));
                            selectHospitalStr.add(jObject.getString("departName"));
                        }



                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    //當連線失敗
                    e.printStackTrace();
                }
            });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        GIS_Log.d(TAG,"onCreateOptionMenu");
        inflater.inflate(R.menu.fake_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private String generateID() {
        Random tmpRand = new Random();
        return String.valueOf(tmpRand.nextInt(2147483647));
    }


    private boolean isInputComplete(){
//        if (mUserIDEditText.getText().toString().equalsIgnoreCase("")){
//            showInputErrorDialog(getString(R.string.msg_id_cannot_empty));
//            return false;
//        }

        if (mUserNameText.getText().toString().equalsIgnoreCase("")){
            showInputErrorDialog("User name can't be empty");
            return false;
        }
//        if (mFirstNameEditText.getText().toString().equalsIgnoreCase("")){
//            showInputErrorDialog(getString(R.string.msg_first_name_cannot_empty));
//            return false;
//        }

//        if (mLastNameEditText.getText().toString().equalsIgnoreCase("")){
//            showInputErrorDialog(getString(R.string.msg_last_name_cannot_empty));
//            return false;
//        }

        if (mPhoneNumberText.getText().toString().equalsIgnoreCase("")){
            showInputErrorDialog("請輸入電話號碼");
            return false;
        }

        if (mBirthdayText.getText().toString().equalsIgnoreCase("")){
            showInputErrorDialog("請輸入生日");
            return false;
        }

        if (mJobText.getText().toString().equalsIgnoreCase("")){
            showInputErrorDialog("請輸入職業");
            return false;
        }

        if (mOperationText.getText().toString().equalsIgnoreCase("")){
            showInputErrorDialog("請輸入手術");
            return false;
        }

        if (mDiseaseText.getText().toString().equalsIgnoreCase("")){
            showInputErrorDialog("請輸入疾病");
            return false;
        }

        if (mNationalityText.getText().toString().equalsIgnoreCase("")){
            showInputErrorDialog("請輸入民族");
            return false;
        }

        if(mGenderGroup.getCheckedRadioButtonId() == -1){
            showInputErrorDialog(getString(R.string.msg_gender_cannot_empty));
            return false;
        }

        if(mMarryGroup.getCheckedRadioButtonId() == -1){
            showInputErrorDialog("請選擇婚姻狀態");
            return false;
        }

//        String ageStr = mAgeEditText.getText().toString();
//        if(ageStr.isEmpty()){
//            showInputErrorDialog(getString(R.string.msg_age_cannot_empty));
//            return false;
//        }

        String pulmDiaStr = mPulmDiaEditText.getText().toString();

        if(pulmDiaStr.isEmpty() || pulmDiaStr.indexOf(".") == 0){
            showInputErrorDialog(getString(R.string.msg_pulm_dia_not_number));
            return false;
        }

        return true;
    }

    private void showInputErrorDialog(String msg){
        if (getActivity()!=null){
            new AlertDialog.Builder(getActivity())
                    .setTitle(msg)
                    .setPositiveButton("OK", (dialogInterface, i) -> {
                        // Do nothing.
                    }).show();
        }
    }

    private void addUserInfoToDB(userInfo inputUser){
        ContentValues cv = new ContentValues();
        cv.put(IwuSQLHelper.KEY_USER_ID_NUMBER, inputUser.userID);
        cv.put(IwuSQLHelper.KEY_USER_NAME, inputUser.name);
        cv.put(IwuSQLHelper.KEY_USER_PHONE_NUMBER, inputUser.phoneNumber);
        cv.put(IwuSQLHelper.KEY_USER_BIRTHDAY, inputUser.birthday);
        cv.put(IwuSQLHelper.KEY_USER_HOSPITAL, inputUser.hospital);
        cv.put(IwuSQLHelper.KEY_USER_JOB, inputUser.job);
        cv.put(IwuSQLHelper.KEY_USER_NATION, inputUser.nationality);
        cv.put(IwuSQLHelper.KEY_USER_OPERATION, inputUser.operation);
        cv.put(IwuSQLHelper.KEY_USER_DISEASE, inputUser.disease);
//        cv.put(IwuSQLHelper.KEY_USER_FIRST_NAME, inputUser.firstName);
//        cv.put(IwuSQLHelper.KEY_USER_LAST_NAME, inputUser.lastName);
//        cv.put(IwuSQLHelper.KEY_USER_HEIGHT, inputUser.height);
        cv.put(IwuSQLHelper.KEY_USER_PULM_DIAMETER, inputUser.pulmDiameter);
//        cv.put(IwuSQLHelper.KEY_USER_HEIGHT_TO_CSA, inputUser.heightToCSA);
//        cv.put(IwuSQLHelper.KEY_USER_ANGLE, inputUser.angle);
//        cv.put(IwuSQLHelper.KEY_USER_BLE_MAC, inputUser.bleMac);
        cv.put(IwuSQLHelper.KEY_USER_GENDER, inputUser.gender);
        cv.put(IwuSQLHelper.KEY_USER_MARRY, inputUser.marry);
//        cv.put(IwuSQLHelper.KEY_USER_AGE, inputUser.age);

        long lnReturn = mHelper.mDBWrite.insert(IwuSQLHelper.STR_TABLE_USER, "", cv);
        inputUser.userCount = (int)lnReturn;

    }

    private int getUserCount(){
        Cursor cursor = mHelper.mDBWrite.rawQuery("select *  from "+IwuSQLHelper.STR_TABLE_USER,null);
        int userCount = cursor.getCount();
        cursor.close();
        return userCount;
    }

    private boolean isUserExist(String userID){
        boolean isExist = false;
        String[] columns, userIds;
        Log.d(TAG, "isUserExist");
        columns = new String[]{IwuSQLHelper.KEY_USER_PRIMARY, IwuSQLHelper.KEY_USER_ID_NUMBER,
                IwuSQLHelper.KEY_USER_NAME, IwuSQLHelper.KEY_USER_PHONE_NUMBER,
                IwuSQLHelper.KEY_USER_BIRTHDAY, IwuSQLHelper.KEY_USER_HOSPITAL,
                IwuSQLHelper.KEY_USER_JOB, IwuSQLHelper.KEY_USER_NATION,
                IwuSQLHelper.KEY_USER_OPERATION, IwuSQLHelper.KEY_USER_DISEASE,
                IwuSQLHelper.KEY_USER_PULM_DIAMETER, IwuSQLHelper.KEY_USER_GENDER, IwuSQLHelper.KEY_USER_MARRY};
        String selection = IwuSQLHelper.KEY_USER_ID_NUMBER + "=?";
        userIds = new String[1];
        userIds[0] = userID;
        Cursor c = mHelper.mDBWrite.query(
                IwuSQLHelper.STR_TABLE_USER,
                columns,
                selection,
                userIds,
                null,
                null,
                null
        );
        int total = c.getCount();
        if (total>0){
            isExist = true;
        }
        c.close();
        return isExist;
    }

    private void updateUserInfoToDB(userInfo updateInfo){
        ContentValues cv = new ContentValues();
        String strID, strWhere;
        cv.put(IwuSQLHelper.KEY_USER_ID_NUMBER, updateInfo.userID);
        cv.put(IwuSQLHelper.KEY_USER_NAME, updateInfo.name);
        cv.put(IwuSQLHelper.KEY_USER_PHONE_NUMBER, updateInfo.phoneNumber);
        cv.put(IwuSQLHelper.KEY_USER_BIRTHDAY, updateInfo.birthday);
        cv.put(IwuSQLHelper.KEY_USER_HOSPITAL, updateInfo.hospital);
        cv.put(IwuSQLHelper.KEY_USER_JOB, updateInfo.job);
        cv.put(IwuSQLHelper.KEY_USER_NATION, updateInfo.nationality);
        cv.put(IwuSQLHelper.KEY_USER_OPERATION, updateInfo.operation);
        cv.put(IwuSQLHelper.KEY_USER_DISEASE, updateInfo.disease);
        cv.put(IwuSQLHelper.KEY_USER_PULM_DIAMETER, updateInfo.pulmDiameter);
        cv.put(IwuSQLHelper.KEY_USER_GENDER, updateInfo.gender);
        cv.put(IwuSQLHelper.KEY_USER_MARRY, updateInfo.marry);

        strID = String.valueOf(updateInfo.userCount);
        strWhere = "_id="+strID;
        GIS_Log.d(TAG,"strWhere="+strWhere);
        mHelper.mDBWrite.beginTransaction();
        mHelper.mDBWrite.update(IwuSQLHelper.STR_TABLE_USER,cv,strWhere,null);
        mHelper.mDBWrite.setTransactionSuccessful();
        mHelper.mDBWrite.endTransaction();
    }

    private double getPADiaByGenderAge(int inputGender, int inputAge){
        if (inputGender == userInfo.FEMALE){
            if (inputAge < 45 && inputAge > 0){
                return 23.5;
            } else if (inputAge >= 45 && inputAge <55) {
                return 23.8;
            } else {
                return 24.0;
            }
        } else {
            if (inputAge < 45 && inputAge > 0){
                return 25.7;
            } else if (inputAge >= 45 && inputAge < 55) {
                return 25.6;
            } else {
                return 25.2;
            }
        }
    }
}
