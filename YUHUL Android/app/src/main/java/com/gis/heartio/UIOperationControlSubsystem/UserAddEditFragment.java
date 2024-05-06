package com.gis.heartio.UIOperationControlSubsystem;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.gis.heartio.GIS_Log;
import com.gis.heartio.R;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.IwuSQLHelper;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.userInfo;

import java.util.Objects;

//* {@link UserAddEditFragment.OnFragmentInteractionListener} interface
/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link UserAddEditFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserAddEditFragment extends Fragment {
    private static final String TAG = "UserAddEditFragment";

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private EditText mUserNameText, mUserIDEditText, mPhoneNumberText, mBirthdayText, mJobText, mPulmDiaEditText, mNationalityText, mOperationText, mDiseaseText;
//    private EditText mUserIDEditText, mFirstNameEditText, mLastNameEditText;
//    private EditText mPulmDiaEditText, mPhoneNumber, mBirthday;
//    private EditText mAgeEditText;
    private RadioGroup mGenderGroup, mMarryGroup;
    private Spinner mHospitalSpinner;
    private int intGender = -1;
    private int intMarry = -1;
    private static IwuSQLHelper mHelper;
    private String inputUserPriID = null;
    private AppCompatActivity mActivity = null;

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

        String text = mHospitalSpinner.getSelectedItem().toString();
//        mAgeEditText = rootView.findViewById(R.id.ageEditTextNumber);
//        mFirstNameEditText = rootView.findViewById(R.id.firstnameeditText);
//        mLastNameEditText = rootView.findViewById(R.id.lastnameeditText);

        if (mActivity.getSupportActionBar()!=null){
            GIS_Log.d(TAG,"inputUserPriID = "+inputUserPriID);
            if (inputUserPriID==null){
                mActivity.getSupportActionBar().setTitle(getString(R.string.title_add_user));
                resetBtn.setVisibility(View.VISIBLE);
                applyBtn.setText(R.string.create);
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
                if (mPulmDiaEditText.getText().length() == 0) {
                    try {
                        //mBirthdayText type = androidx.appcompat.widget.AppCompatEditText
                        String birthday = mBirthdayText.getText().toString();
                        Log.d(TAG, "birthday = " + birthday);
                        if(!birthday.isEmpty()){
                            int inputAge = getAge(); //not yet
                            if (inputAge > 0 && intGender != -1) {
                                mPulmDiaEditText.setText(String.valueOf(getPADiaByGenderAge(intGender, inputAge)));
                            }
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
            mBirthdayText.setText(String.valueOf(editUser.birthday));

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
            mUserIDEditText.setText("");
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
            intGender = -1;
        });


        applyBtn.setOnClickListener(view -> {
            if (isInputComplete()){
                userInfo addUser = new userInfo();

//                addUser.firstName = mFirstNameEditText.getText().toString();
//                addUser.lastName = mLastNameEditText.getText().toString();
                addUser.name = mUserNameText.getText().toString();
                addUser.userID = mUserIDEditText.getText().toString();
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
                    if (!isUserExist(addUser.userID)){
                        addUserInfoToDB(addUser);
                        if (getUserCount()==1){
                            UserManagerCommon.initUserUltrasoundParameter(addUser);
                            MainActivity.mRawDataProcessor.updateStrBaseFolder();
                        }

                        mActivity.onBackPressed();
                    }else{
                        showInputErrorDialog(getString(R.string.msg_id_existed));
                    }
                }else{
                    addUser.userCount = Integer.parseInt(inputUserPriID);
                    updateUserInfoToDB(addUser);
                    SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);
                    int currentUserID_primary = sharedPref.getInt(IwuSQLHelper.KEY_CURRENT_USER,1);
                    if (currentUserID_primary == addUser.userCount){
                        UserManagerCommon.initUserUltrasoundParameter(addUser);
                        MainActivity.mRawDataProcessor.updateStrBaseFolder();
                    }
                    mActivity.onBackPressed();
                }
            }
        });
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        GIS_Log.d(TAG,"onCreateOptionMenu");
        inflater.inflate(R.menu.fake_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    private boolean isInputComplete(){
        if (mUserIDEditText.getText().toString().equalsIgnoreCase("")){
            showInputErrorDialog(getString(R.string.msg_id_cannot_empty));
            return false;
        }

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
    // create by Doris 2024/05/02
    private int getAge(){
        int age = 40;
        return age;
    }
}
