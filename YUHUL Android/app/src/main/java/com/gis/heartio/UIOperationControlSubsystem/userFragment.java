package com.gis.heartio.UIOperationControlSubsystem;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
//import android.support.annotation.Nullable;
//import androidx.core.app.Fragment;
//import androidx.core.app.FragmentTransaction;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gis.CommonUtils.Constants;
import com.gis.heartio.GIS_Log;
import com.gis.heartio.R;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.IwuSQLHelper;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.userInfo;

import java.util.ArrayList;

/**
 * Created by Cavin on 2018/2/21.
 */

public class userFragment extends Fragment {
    public static final String TAG = "userFragment";

    private static ArrayList<userInfo> mUserList;
    private userListCursorAdapter mUserListCursorAdapter;
    private Cursor mCursorUserList;
    private IwuSQLHelper mHelper;
    private MainActivity mActivity;

    // GUI elements
    private ListView mUserListView;

    public static userFragment newInstance() {
        return new userFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        mHelper = new IwuSQLHelper(mActivity);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView =inflater.inflate(R.layout.fragment_item_user, container, false);
        ((AppCompatActivity)mActivity).getSupportActionBar().setTitle(getString(R.string.title_user));

        if (mActivity!=null){
            mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mActivity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        mUserListView = rootView.findViewById(R.id.userListView);

        setHasOptionsMenu(true);

        updateUserCursor();
        mUserListCursorAdapter = new userListCursorAdapter(mActivity,mCursorUserList,0);
        mUserListView.setAdapter(mUserListCursorAdapter);

        /*final SimpleCursorAdapter adapter = new SimpleCursorAdapter(mActivity,R.layout.listitem_user, mCursorUserList,
                new String[]{IwuSQLHelper.KEY_USER_ID_NUMBER, IwuSQLHelper.KEY_USER_FIRST_NAME, IwuSQLHelper.KEY_USER_LAST_NAME},
                new int[]{R.id.idTextView,R.id.firstNameTextView,R.id.lastNameTextView},0);
        mUserListView.setAdapter(adapter);*/
        //adapter.notifyDataSetChanged();
        mUserListCursorAdapter.notifyDataSetChanged();
        mUserListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, final View view, int i, long l) {
                if (mUserListCursorAdapter.getCount()>0){
                    final Cursor cursor = (Cursor) mUserListCursorAdapter.getItem(i);
                    final String selectStr = cursor.getString(0);
                    final int selectedIdx = i;
                    GIS_Log.d(TAG,"Selected String = "+selectStr);
                    AlertDialog alertDialog = new AlertDialog.Builder(mActivity).create(); //Read Update
                    alertDialog.setTitle("Action");
                    //alertDialog.setMessage("Upgrade Text Here");
                    alertDialog.setButton( Dialog.BUTTON_POSITIVE, getString(R.string.action_edit), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            updateWithAddEditUserFragment(userFragment.this,selectStr);
                        }
                    });

                    alertDialog.setButton( Dialog.BUTTON_NEGATIVE, getString(R.string.action_del), new DialogInterface.OnClickListener()    {
                        public void onClick(DialogInterface dialog, int which) {
                            String deleteID = cursor.getString(cursor.getColumnIndexOrThrow(IwuSQLHelper.KEY_USER_ID_NUMBER));
                            String strMsg = getString(R.string.msg_sure_del_user) +deleteID;
                            showDialogForDelete(strMsg, deleteID);
                        }
                    });
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.action_select), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putInt(IwuSQLHelper.KEY_CURRENT_USER, Integer.parseInt(selectStr));
                            editor.commit();
                            mUserListCursorAdapter.notifyDataSetChanged();
                            UserManagerCommon.initUserUltrasoundParameter(IwuSQLHelper.getUserInfoFromPrimaryID(selectStr,mHelper));
                            //MainActivity.mRawDataProcessor.initStrBaseFolder();
                            MainActivity.mRawDataProcessor.updateStrBaseFolder();
                        }
                    });
                    alertDialog.show();  //<-- See This!
                }
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.user_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add:
                updateWithAddEditUserFragment(this,null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onDestroy() {
        mHelper.closeDatabase();
        mCursorUserList.close();
        super.onDestroy();
    }

    private void updateWithAddEditUserFragment(final Fragment fragment, String userID) {
        if (fragment!=null){
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            UserAddEditFragment uAEFrag;
            if (userID!=null){
                uAEFrag = UserAddEditFragment.newInstance(userID,null);
            }else {
                uAEFrag = UserAddEditFragment.newInstance();
            }


            transaction.replace(R.id.frame_layout, uAEFrag,
                    Constants.USER_ADD_EDIT_TAG);
            transaction.addToBackStack(fragment.getClass().getName());

            transaction.commit();
        }
    }

    /**
        * Holder class for the list view view widgets
        */
    /*static class userViewHolder {
        TextView firstName;
        TextView lastName;
        TextView userID;
    }*/

    private class userListCursorAdapter extends CursorAdapter{
        private LayoutInflater mInflator;
        private Cursor cursor;

        public userListCursorAdapter(Context context, Cursor cursor, int flags){
            super(context,cursor, flags);
            mUserList = new ArrayList<userInfo>();
            this.cursor = cursor;
            //mInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

       /* @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            final userViewHolder mViewHolder;

            if (view == null){
                view = mInflator.inflate(R.layout.listitem_user, viewGroup,false);
                mViewHolder = new userViewHolder();
                mViewHolder.firstName = view.findViewById(R.id.firstnameeditText);
                mViewHolder.lastName = view.findViewById(R.id.lastnameeditText);
                mViewHolder.userID = view.findViewById(R.id.idTextView);
                view.setTag(mViewHolder);
            }else{
                mViewHolder = (userViewHolder)view.getTag();
            }
            return view;
        }*/
       public int getCount(){
           return cursor.getCount();
       }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            View view = LayoutInflater.from(context).inflate(R.layout.listitem_user, viewGroup, false);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView firstName = view.findViewById(R.id.firstNameTextView);
            String strFirstName = cursor.getString(cursor.getColumnIndexOrThrow(IwuSQLHelper.KEY_USER_FIRST_NAME));
            firstName.setText(strFirstName);
            TextView lastName = view.findViewById(R.id.lastNameTextView);
            String strLastName = cursor.getString(cursor.getColumnIndexOrThrow(IwuSQLHelper.KEY_USER_LAST_NAME));
            lastName.setText(strLastName);
            TextView userID = view.findViewById(R.id.idTextView);
            String strUserID = cursor.getString(cursor.getColumnIndexOrThrow(IwuSQLHelper.KEY_USER_ID_NUMBER));
            userID.setText(strUserID);

            ImageView currentUserIV = view.findViewById(R.id.selectUserImageView);
            //String strSelected = cursor.getString(cursor.getColumnIndexOrThrow(IwuSQLHelper.KEY_CURRENT_USER));
            SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);
            int currentUserID_primary = sharedPref.getInt(IwuSQLHelper.KEY_CURRENT_USER,1);
            if (currentUserID_primary==cursor.getInt(cursor.getColumnIndexOrThrow(IwuSQLHelper.KEY_USER_PRIMARY))){
                currentUserIV.setImageDrawable(ContextCompat.getDrawable(mActivity,R.drawable.checkbox_checked));
            }else{
                currentUserIV.setImageDrawable(ContextCompat.getDrawable(mActivity,R.drawable.checkbox_unchecked));
            }

        }


    }

    private void deleteUserSelected(String deleteID){
        String strWhere;
        long lnReturn;

        //strWhere = "_id=" + UserManagerCommon.mUserInfoCur.mIntUserID;
        strWhere = IwuSQLHelper.KEY_USER_ID_NUMBER+" = '"+deleteID+"'";
        mHelper.mDBWrite.beginTransaction();
        GIS_Log.d(TAG,"strWhere="+strWhere);
        try {
            lnReturn =mHelper.mDBWrite.delete(IwuSQLHelper.STR_TABLE_USER, strWhere, null);
            mHelper.mDBWrite.setTransactionSuccessful();
        }catch(Exception ex2){
            ex2.printStackTrace();
        }finally{
            mHelper.mDBWrite.endTransaction();
        }

    }

    private void showDialogForDelete(String strMsg, final String deleteID){
        new AlertDialog.Builder(mActivity)
                .setTitle(strMsg)
                //.setMessage(strMsg)
                .setPositiveButton(R.string.alert_message_exit_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteUserSelected(deleteID);
                        //sendSelfMsg(INT_USER_MANAGER_SELF_MSG_ID_SHOW_FROM_DELETE);
                        if (UserManagerCommon.mUserInfoCur != null && UserManagerCommon.mUserInfoCur.userID != null){
                            if (UserManagerCommon.mUserInfoCur.userID.equals(deleteID)){
                                mUserList = IwuSQLHelper.getAllUserInfo(mHelper);
                                if (mUserList.size() > 0){
                                    UserManagerCommon.initUserUltrasoundParameter(mUserList.get(0));

                                    SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putInt(IwuSQLHelper.KEY_CURRENT_USER, mUserList.get(0).userCount);
                                    editor.commit();
                                }else{
                                    UserManagerCommon.mBoolUserSelected = false;
                                    UserManagerCommon.initUserUltrasoundParameter(null);
                                }
                            }
                        }
                        updateUserCursor();
                        // initial listview and adapter for refresh.
                        mUserListCursorAdapter = new userListCursorAdapter(mActivity,mCursorUserList,0);
                        mUserListView.setAdapter(mUserListCursorAdapter);
                        mUserListCursorAdapter.notifyDataSetChanged();

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

    private void updateUserCursor(){
        String[] mStrCursorQueryFields = new String[]{IwuSQLHelper.KEY_USER_PRIMARY, IwuSQLHelper.KEY_USER_ID_NUMBER, IwuSQLHelper.KEY_USER_FIRST_NAME
                , IwuSQLHelper.KEY_USER_LAST_NAME, IwuSQLHelper.KEY_USER_HEIGHT_TO_CSA, IwuSQLHelper.KEY_USER_HEIGHT
                , IwuSQLHelper.KEY_USER_PULM_DIAMETER, IwuSQLHelper.KEY_USER_ANGLE, IwuSQLHelper.KEY_USER_BLE_MAC};
        mCursorUserList = mHelper.mDBWrite.query(IwuSQLHelper.STR_TABLE_USER,
                                                        mStrCursorQueryFields, null, null,
                                                        null, null,null);
    }
}
