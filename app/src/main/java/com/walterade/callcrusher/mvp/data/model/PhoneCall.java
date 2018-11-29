package com.walterade.callcrusher.mvp.data.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;
import android.telephony.PhoneNumberUtils;

import com.walterade.callcrusher.R;
import com.walterade.callcrusher.utils.ContactUtils;
import com.walterade.callcrusher.utils.PhoneUtils;

import java.util.Locale;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import rx.functions.Action1;


class PhoneCall {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "phone_number")
    protected String phoneNumber;

    @ColumnInfo(name = "display_phone_number")
    private String displayPhoneNumber;

    @Ignore
    private String callerName;

    @Ignore
    private CallInfo info;

    @Ignore
    private boolean isLoading;
    @Ignore
    private boolean isLoaded;


    public String toString() {
        String s = PhoneNumberUtils.formatNumber(phoneNumber, Locale.getDefault().getCountry());
        if (callerName != null) s = callerName + " (" + s + ")";
        return s;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.displayPhoneNumber = phoneNumber;
        phoneNumber = IncomingCall.normalizeNumber(phoneNumber);
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getDisplayPhoneNumber() {
        return displayPhoneNumber;
    }

    public void setDisplayPhoneNumber(String displayPhoneNumber) {
        this.displayPhoneNumber = displayPhoneNumber;
    }

    public String getCallerName() {
        return callerName;
    }

    public void setCallerName(String callerName) {
        this.callerName = callerName;
    }


    public String getCallerId(Context c, Action1<CallInfo> runInBackground, Action1<CallInfo> onSuccess, Action1<Exception> onError) {
        if (isLoaded)
            return callerName;
        else {
            isLoading = true;
            getCallerInfo(c, phoneNumber, runInBackground, (info) -> {
                if (info != null)
                    callerName = info.callerName;
                isLoaded = true;
                isLoading = false;
                onSuccess.call(info);
            }, onError);
        }

        return null;
    }


    public static void getCallerInfo(Context c, String phoneNumber, Action1<CallInfo> runInBackground, Action1<CallInfo> onSuccess, Action1<Exception> onError) {
        Single.create((e) -> {

            ContactUtils.ContactInfo info = PhoneUtils.getCallerId(c,
                    c.getString(R.string.opencnam_account_sid),
                    c.getString(R.string.opencnam_auth_token),
                    phoneNumber
            );

            CallInfo callInfo = new CallInfo();
            callInfo.isDirty = false;
            callInfo.isLoaded = true;
            callInfo.isBlocked = BlockedCall.isBlocked(phoneNumber);

            if (info != null) {
                callInfo.callerName = info.name;
                callInfo.imageUri = info.imageUri;
                callInfo.isContact = info.isContact;
            }

            if (runInBackground != null) runInBackground.call(callInfo);
            e.onSuccess(callInfo);

        }).subscribeOn(Schedulers.io())/*.observeOn(AndroidSchedulers.mainThread())*/.subscribe(
                (e)-> onSuccess.call((CallInfo) e),
                (e)-> {if (onError != null) onError.call((Exception) e);}
        );
    }

    public CallInfo getInfo(Context c, Action1<CallInfo> onSuccess, Action1<Exception> onError, boolean refresh) {
        if (!refresh && info != null) {
            return info;
        }
        isLoading = true;
        IncomingCall.getCallerInfo(c, phoneNumber,
                null,
                (info) -> {
                    isLoading = false;
                    isLoaded = true;
                    this.info = info;
                    onSuccess.call(info);
                },
                (e) -> {
                    isLoading = false;
                    isLoaded = true;
                    onError.call(e);
                }
        );
        return null;
    }

    public CallInfo getInfo() {
        return info;
    }

    public void dispose() {
        info = null;
    }

    public void setInfo(CallInfo info) {
        this.info = info;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }
}
