package com.walterade.callcrusher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.walterade.callcrusher.activity.MainActivity;
import com.walterade.callcrusher.application.CallCrusherApplication;
import com.walterade.callcrusher.event.PhoneRingingEvent;
import com.walterade.callcrusher.event.PhoneRingingStoppedEvent;
import com.walterade.callcrusher.manager.SettingsManager;
import com.walterade.callcrusher.mvp.data.model.BlockedCall;
import com.walterade.callcrusher.mvp.data.model.IncomingCall;
import com.walterade.callcrusher.utils.ContactUtils;
import com.walterade.callcrusher.utils.PhoneUtils;
import com.walterade.callcrusher.utils.RxBus;

import java.util.Date;

import rx.Single;
import rx.schedulers.Schedulers;


public class IncomingCallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context c, Intent intent) {

        if (intent.getAction() != null)
            switch (intent.getAction()) {
                case Intent.ACTION_BOOT_COMPLETED:
                    CallCrusherApplication.getInstance().startCallCrusherService();
                    break;
                default:
                    try {

                        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                        String number = intent.getExtras() != null ? intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER) : null;

                        if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {
                            if (number != null) checkIncomingCall(c, number);
                        } else {
                            RxBus.getInstance().post(new PhoneRingingStoppedEvent());
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
    }


    void checkIncomingCall(Context c, String number) {


        Single.create((e) -> {

            //everyone is suspicious until proven normal :)
            int callType = IncomingCall.CALLTYPE_SUSPICIOUS;

            boolean blocked;
            IncomingCall call = new IncomingCall();
            int callsCrushState = new SettingsManager(c).getCallsCrushState();

            switch (callsCrushState) {

                case SettingsManager.CRUSH_STATE_BLOCK:
                    if (BlockedCall.isBlocked(number)) {
                        blocked = PhoneUtils.endCall(c);
                        callType = blocked ? IncomingCall.CALLTYPE_BLOCK : IncomingCall.CALLTYPE_BLOCK_ERROR;
                    } else {
                        //normal calls are numbers not in your contacts
                        if (ContactUtils.isNumberInContacts(c, number))
                            callType = IncomingCall.CALLTYPE_SAFE;
                    }
                    break;

                case SettingsManager.CRUSH_STATE_CRUSH:
                    if (BlockedCall.isBlocked(number) ||
                        !ContactUtils.isNumberInContacts(c, number)) {
                        blocked = PhoneUtils.endCall(c);
                        callType = blocked ? IncomingCall.CALLTYPE_BLOCK : IncomingCall.CALLTYPE_BLOCK_ERROR;
                    } else callType = IncomingCall.CALLTYPE_SAFE;
                    break;

                case SettingsManager.CRUSH_STATE_ALLOW:
                default:
                    break;
            }

            call.setCallDate(new Date().getTime());
            call.setPhoneNumber(number);
            call.setCallType(callType);
            IncomingCall.add(call);

            RxBus.getInstance().post(new PhoneRingingEvent(call.getId()));

            if (callType != IncomingCall.CALLTYPE_SAFE)
                if (!CallCrusherApplication.getInstance().isActivityStarted(MainActivity.class)) {
                    Intent i = new Intent(c, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    c.startActivity(i);
                }

        })
        .subscribeOn(Schedulers.io()).subscribe();

    }

}