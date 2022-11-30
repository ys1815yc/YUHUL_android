package com.gis.heartio.UIOperationControlSubsystem;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.gis.heartio.R;
import com.gis.heartio.SupportSubsystem.LoginDatabaseAdapter;
import com.google.android.material.textfield.TextInputEditText;

import java.sql.SQLException;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class registerActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private TextInputEditText userIDEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText confirmPWEditText;
    private String username;
    private String password;
    private String confirmPassword;
    LoginDatabaseAdapter loginDatabaseAdapter;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = () -> hide();
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = (view, motionEvent) -> {
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS);
        }
        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        loginDatabaseAdapter = new LoginDatabaseAdapter(getApplicationContext());
        try {
            loginDatabaseAdapter = loginDatabaseAdapter.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        userIDEditText = findViewById(R.id.userIDEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPWEditText = findViewById(R.id.confirmPwEditText);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        Button cancelBtn = findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(view -> finish());


        // Set up the user interaction to manually show or hide the system UI.
        /*mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });*/

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
       // findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        //delayedHide(100);
    }

    @Override
    protected void onDestroy() {
        loginDatabaseAdapter.close();
        super.onDestroy();
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public void submit(View view){
        username = userIDEditText.getText().toString();
        password = passwordEditText.getText().toString();
        confirmPassword = confirmPWEditText.getText().toString();

        if (username.equals("")||password.equals("")||confirmPassword.equals("")){
            AlertDialog alertDialog = new AlertDialog.Builder(this,R.style.MyDialogTheme).create();
            alertDialog.setTitle(getString(R.string.filled_all));
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", (dialogInterface, i) -> {

            });
            alertDialog.show();
            final Button okBtn = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            okBtn.setBackgroundResource(R.drawable.block_b);
            okBtn.setTextColor(Color.WHITE);
            okBtn.setTextSize(18f);
            okBtn.setScaleX(0.60f);
            okBtn.setScaleY(0.60f);
        }else{
            if (password.equals(confirmPassword)){
                int receiveOk = loginDatabaseAdapter.insertEntry(username,password,null,null);
                AlertDialog submitAlertDialog = new AlertDialog.Builder(this,R.style.MyDialogTheme).create();
                if (receiveOk!=LoginDatabaseAdapter.retFail){
                    submitAlertDialog.setTitle(getString(R.string.successful));
                    submitAlertDialog.setMessage(getString(R.string.signin_now));
                    submitAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", (dialogInterface, i) -> {
                        Intent intent = new Intent(registerActivity.this, loginActivity.class);
                        startActivity(intent);
                        finish();
                    });


                }else{
                    submitAlertDialog.setTitle(getString(R.string.alert_title_failure));
                    submitAlertDialog.setMessage(getString(R.string.accunt_exist_or_invalid_password));
                    submitAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", (dialogInterface, i) -> {

                    });

                }
                submitAlertDialog.show();
                final Button okBtn = submitAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                okBtn.setBackgroundResource(R.drawable.block_b);
                okBtn.setTextColor(Color.WHITE);
                okBtn.setTextSize(18f);
                okBtn.setScaleX(0.60f);
                okBtn.setScaleY(0.60f);

            }else{
                AlertDialog pwConfirmErrAlertDialog = new AlertDialog.Builder(this,R.style.MyDialogTheme).create();
                pwConfirmErrAlertDialog.setTitle(getString(R.string.confirm_password_not_the_same));
                pwConfirmErrAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", (dialogInterface, i) -> {

                });
                pwConfirmErrAlertDialog.show();

                final Button okBtn = pwConfirmErrAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                okBtn.setBackgroundResource(R.drawable.block_b);
                okBtn.setTextColor(Color.WHITE);
                okBtn.setTextSize(18f);
                okBtn.setScaleX(0.60f);
                okBtn.setScaleY(0.60f);
            }
        }
    }
}
