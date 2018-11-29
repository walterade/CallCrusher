package com.walterade.callcrusher.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.walterade.callcrusher.R;
import com.walterade.callcrusher.event.CallCrushStateChangeEvent;
import com.walterade.callcrusher.injection.component.ApplicationComponent;
import com.walterade.callcrusher.manager.SettingsManager;
import com.walterade.callcrusher.mvp.data.model.BlockedCall;
import com.walterade.callcrusher.mvp.ui.landing.LandingFragment;
import com.walterade.callcrusher.utils.AndroidUtils;
import com.walterade.callcrusher.utils.FragmentUtils;
import com.walterade.callcrusher.utils.RxBus;
import com.walterade.callcrusher.widget.BouncyTextView;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends BaseActivity implements MainView {

    private static final int MY_PERMISSIONS_REQUEST = 1;
    boolean hasContacts = false;
    boolean hasPhone = false;

    @BindView(R.id.crush_calls) BouncyTextView crushCalls;
    @BindView(R.id.allow_calls) BouncyTextView allowCalls;
    @BindView(R.id.block_calls) BouncyTextView blockCalls;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.content) View content;
    @Inject SettingsManager settings;
    private Snackbar snackbar;
    private AlertDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        checkForPermissions();

        updateCrushCallsState();

        FragmentUtils.push(this, LandingFragment.newInstance(), R.id.content, LandingFragment.TAG);
    }

    @Override
    protected void inject(ApplicationComponent component) {
        component.inject(this);
    }

    @OnClick(R.id.crush_calls)
    public void onCrushCallsClicked() {
        settings.setCallsCrushState(SettingsManager.CRUSH_STATE_CRUSH);
        updateCrushCallsUI();
    }

    @OnClick(R.id.allow_calls)
    public void onAllowCallsClicked() {
        settings.setCallsCrushState(SettingsManager.CRUSH_STATE_ALLOW);
        updateCrushCallsUI();
    }

    @OnClick(R.id.block_calls)
    public void onBlockCallsClicked() {
        settings.setCallsCrushState(SettingsManager.CRUSH_STATE_BLOCK);
        updateCrushCallsUI();
    }

    @OnClick(R.id.add_blocked_number)
    public void onBlockCallsAddClicked() {
        if (dialog != null) dialog.dismiss();

        dialog = new AlertDialog.Builder(this)
            .setTitle("Add number to block list")
            .setView(R.layout.dlg_add_blocked_number)
            .setPositiveButton("Add", (dialog1, which) -> {})
            .setCancelable(true)
            .show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (dialog.getWindow() != null) {
                TextInputLayout til = dialog.getWindow().findViewById(R.id.phone_layout);
                TextInputEditText tv = dialog.getWindow().findViewById(R.id.phone);
                String phone = tv.getText().toString();
                if (!TextUtils.isEmpty(phone)) {
                    try {
                        phone = PhoneNumberUtils.formatNumber(phone, Locale.getDefault().getCountry());
                        if (!TextUtils.isEmpty(phone)) {
                            til.setError(null);
                            final String fp = phone;
                            Single.create((e) -> BlockedCall.block(fp)).subscribeOn(Schedulers.io()).subscribe();
                            dialog.dismiss();
                            dialog = null;
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                til.setError("Please enter a valid phone number.");
            }
            else {
                dialog.dismiss();
                dialog = null;
            }
        });
    }

    void updateCrushCallsUI() {
        int state = settings.getCallsCrushState();
        updateCrushCallsState();
        showCrushToast(state);
        RxBus.getInstance().post(new CallCrushStateChangeEvent(state));
    }

    void updateCrushCallsState() {
        int state = settings.getCallsCrushState();

        int allowCallsColor = Color.LTGRAY;
        int blockCallsColor = Color.LTGRAY;
        int crushCallsColor = Color.LTGRAY;

        switch (state) {
            case SettingsManager.CRUSH_STATE_ALLOW:
                allowCallsColor = AndroidUtils.getThemeColor(this, R.attr.colorPrimary);
                break;
            case SettingsManager.CRUSH_STATE_BLOCK:
                blockCallsColor = AndroidUtils.getThemeColor(this, R.attr.colorPrimary);
                break;
            case SettingsManager.CRUSH_STATE_CRUSH:
                crushCallsColor = Color.RED;
                break;
        }

        allowCalls.getCompoundDrawablesRelative()[0].setColorFilter(allowCallsColor, PorterDuff.Mode.SRC_ATOP);
        blockCalls.getCompoundDrawablesRelative()[0].setColorFilter(blockCallsColor, PorterDuff.Mode.SRC_ATOP);
        crushCalls.getCompoundDrawablesRelative()[0].setColorFilter(crushCallsColor, PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
                else {
                }
        }
    }

    @Override
    public boolean hasAccessToContacts() {
        return hasContacts;
    }

    @Override
    public boolean hasAccessToPhone() {
        return hasPhone;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        snackbar = null;
        crushCalls = null;
        toolbar = null;
        content = null;
        settings = null;
        dialog = null;
    }

    void showCrushToast(int crushState) {
        if (snackbar != null) snackbar.dismiss();

        snackbar = Snackbar.make(content, "", getResources().getInteger(R.integer.snackbar_duration));
        snackbar.getView().setPadding(0,0,0,0);
        ((ViewGroup)snackbar.getView()).removeAllViews();
        ((ViewGroup)snackbar.getView()).addView(LayoutInflater.from(getApplicationContext()).inflate(R.layout.sb_call_crusher, null));
        TextView tv = snackbar.getView().findViewById(R.id.message);
        ImageView iv = snackbar.getView().findViewById(R.id.crush);

        String message;
        int iconColor = Color.WHITE;
        int icon;

        switch (crushState) {

            case SettingsManager.CRUSH_STATE_BLOCK:
                message = "Call blocking enabled. Suspicious calls will be allowed.";
                icon = R.drawable.ic_block_36dp;
                break;

            case SettingsManager.CRUSH_STATE_CRUSH:
                message = "Call Crusher activated. Only calls from your contacts will be allowed.";
                iconColor = Color.RED;
                icon = R.drawable.ic_smash;
                break;

            case SettingsManager.CRUSH_STATE_ALLOW:
            default:
                icon = R.drawable.ic_block_disabled_36dp;
                message = "Call blocking disabled. All callers will be allowed.";
                break;
        }

        tv.setText(message);
        tv.setMaxLines(5);
        iv.setColorFilter(iconColor, PorterDuff.Mode.SRC_ATOP);
        iv.setImageResource(icon);
        snackbar.show();

    }

    void checkForPermissions() {
        hasPhone = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;
        hasContacts = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;

        if (!hasContacts || !hasPhone) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)
                    ) {

                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setMessage(R.string.permission_message);
                alert.setTitle(R.string.permission_required_title);
                alert.show();

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.READ_CONTACTS,
                                Manifest.permission.CALL_PHONE
                        },
                        MY_PERMISSIONS_REQUEST);
            }
        }
    }

}
