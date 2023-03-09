package com.gis.heartio.UIOperationControlSubsystem;

import android.app.Dialog;

import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.gis.CommonUtils.Constants;
import com.gis.heartio.R;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.IwuSQLHelper;
import com.gis.heartio.SignalProcessSubsystem.SupportSubsystem.LoginDatabaseAdapter;

import java.sql.SQLException;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link adminFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link adminFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class adminFragment extends Fragment {
    private static final String TAG = "adminFragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ListView mAdminListView;
    private SimpleCursorAdapter mSimpleCursorAdapter;
    LoginDatabaseAdapter loginDatabaseAdapter;
    private Cursor mCursor;

    private AppCompatActivity mActivity = null;

    public static adminFragment newInstance() {

        Bundle args = new Bundle();

        adminFragment fragment = new adminFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public adminFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment adminFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static adminFragment newInstance(String param1, String param2) {
        adminFragment fragment = new adminFragment();
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
        if (getActivity()!=null&&((AppCompatActivity)getActivity()).getSupportActionBar()!=null){
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getString(R.string.action_admin_manager));
        }
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
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_item_admin, container, false);
        mAdminListView = rootView.findViewById(R.id.adminListView);

        if (mActivity!=null){
            Objects.requireNonNull(mActivity.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            mActivity.getSupportActionBar().setDisplayShowHomeEnabled(true);
            setHasOptionsMenu(true);
        }

        updateAdminCursor();

        mSimpleCursorAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.listitem_admin, mCursor,
                new String[]{IwuSQLHelper.KEY_ADMIN_ID},
                new int[]{R.id.adminIDTextView},0);
        mAdminListView.setAdapter(mSimpleCursorAdapter);

        mAdminListView.setOnItemClickListener((adapterView, view, i, l) -> {
            if (mSimpleCursorAdapter.getCount()>0){
                final Cursor cursor = (Cursor) mSimpleCursorAdapter.getItem(i);
                final String selectStr = cursor.getString(0);
                final String selectID = cursor.getString(1);
                Log.d(TAG,"Selected admin   "+selectStr+" ID = "+selectID);
                if (getActivity()!=null){
                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create(); //Read Update
                    alertDialog.setTitle(R.string.action_change_password);
                    //alertDialog.setMessage("Upgrade Text Here");
                    alertDialog.setButton( Dialog.BUTTON_POSITIVE, getString(R.string.cancel), (dialog, which) -> {

                    });
                    if (selectID.equals("admin")){
                        alertDialog.setButton( Dialog.BUTTON_NEGATIVE, getString(R.string.alert_message_yes), (dialog, which) -> showChangePWDialog());
                    }else{
                        alertDialog.setTitle(R.string.title_action);
                        alertDialog.setButton( Dialog.BUTTON_NEGATIVE, getString(R.string.action_del), (dialog, which) -> {
                            //String deleteID = cursor.getString(cursor.getColumnIndexOrThrow(IwuSQLHelper.KEY_USER_ID_NUMBER));
                            String strMsg = "Are you sure to delete account ID = " +selectID;
                            showDialogForDelete(strMsg, selectID);
                        });
                    }

                    alertDialog.show();  //<-- See This!

                    final Button actionBtn = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                    actionBtn.setBackgroundResource(R.drawable.block_b);
                    actionBtn.setTextColor(Color.WHITE);
                    actionBtn.setTextSize(18f);
                    actionBtn.setScaleX(0.60f);
                    actionBtn.setScaleY(0.60f);
                    actionBtn.setPadding(100, 5, 100, 5 );

                    final Button cancelBtn = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    cancelBtn.setBackgroundResource(R.drawable.block_g2);
                    cancelBtn.setTextColor(Color.parseColor("#804A4A4A"));
                    cancelBtn.setTextSize(18f);
                    cancelBtn.setScaleX(0.60f);
                    cancelBtn.setScaleY(0.60f);
                    cancelBtn.setPadding(100,5,100,5);
                }

            }
        });
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG,"onCreateOptionMenu");
        inflater.inflate(R.menu.fake_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void showDialogForDelete(String strMsg, final String deleteID){
        if (getActivity()!=null){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(strMsg)
                    //.setMessage(strMsg)
                    .setPositiveButton(R.string.alert_message_exit_ok, (dialog, which) -> {
                        int deletePriID = loginDatabaseAdapter.deleteEntry(deleteID);
                        //sendSelfMsg(INT_USER_MANAGER_SELF_MSG_ID_SHOW_FROM_DELETE);
                        Log.d(TAG,"The user "+deletePriID+"  had been deleted.");
                        updateAdminCursor();
                        // initial listview and adapter for refresh.
                        mSimpleCursorAdapter = new SimpleCursorAdapter(getActivity(),
                                R.layout.listitem_admin, mCursor,
                                new String[]{IwuSQLHelper.KEY_ADMIN_ID},
                                new int[]{R.id.adminIDTextView},0);
                        mAdminListView.setAdapter(mSimpleCursorAdapter);
                        mSimpleCursorAdapter.notifyDataSetChanged();

                    })
                    .setNegativeButton(R.string.alert_message_exit_cancel, (dialog, which) -> {
                        //do nothing;
                    });

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

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.currentFragmentTag = Constants.ADMIN_MANAGER_TAG;
    }

    @Override
    public void onDestroy() {
        mCursor.close();
        loginDatabaseAdapter.close();
        super.onDestroy();
    }

    private void updateAdminCursor(){

        mCursor = loginDatabaseAdapter.getDatabaseInstance().query(IwuSQLHelper.STR_TABLE_ADMIN,
                new String[]{IwuSQLHelper.KEY_ADMIN_PRIMARY,IwuSQLHelper.KEY_ADMIN_ID},
                null,null,null,null,null);
    }

    private void showErrorDialog(String title){
        if (getContext()!=null){
            AlertDialog alertIncorrectDialog = new AlertDialog.Builder(getContext()).create();
            alertIncorrectDialog.setTitle(title);
            alertIncorrectDialog.setButton(Dialog.BUTTON_POSITIVE,"OK", (dialogInterface, i) -> {
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
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
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
                            Log.d(TAG,"Old password: "+oldPass.getText().toString()+"   , new password:  "+newPass.getText().toString());
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

/*    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }*/

/*    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }*/

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /*private class adminListCursorAdapter extends CursorAdapter {
        //private LayoutInflater mInflator;
        private Cursor cursor;

        public adminListCursorAdapter(Context context, Cursor cursor, int flags){
            super(context,cursor, flags);
            //mAdminList = new ArrayList<userInfo>();
            this.cursor = cursor;
            //mInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount(){
            return cursor.getCount();
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            //Log.d(TAG,"newView................");
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
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            int currentUserID_primary = sharedPref.getInt(IwuSQLHelper.KEY_CURRENT_USER,1);
            Log.d(TAG,"currentUserID_primary="+currentUserID_primary);
            if (currentUserID_primary==cursor.getInt(cursor.getColumnIndexOrThrow(IwuSQLHelper.KEY_USER_PRIMARY))){
                currentUserIV.setImageDrawable(getResources().getDrawable(R.drawable.checkbox_checked));
            }else{
                currentUserIV.setImageDrawable(getResources().getDrawable(R.drawable.checkbox_unchecked));
            }

        }


    }*/

}
