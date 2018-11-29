package com.walterade.callcrusher.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.arch.lifecycle.LifecycleService;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneNumberUtils;
import android.widget.RemoteViews;

import com.walterade.callcrusher.R;
import com.walterade.callcrusher.activity.MainActivity;
import com.walterade.callcrusher.application.CallCrusherApplication;
import com.walterade.callcrusher.event.CallCrushStateChangeEvent;
import com.walterade.callcrusher.manager.DatabaseManager;
import com.walterade.callcrusher.manager.SettingsManager;
import com.walterade.callcrusher.mvp.data.model.BlockedCall;
import com.walterade.callcrusher.mvp.data.model.BlockedCallInfo;
import com.walterade.callcrusher.mvp.data.model.CallInfo;
import com.walterade.callcrusher.mvp.data.model.IncomingCall;
import com.walterade.callcrusher.receiver.IncomingCallReceiver;
import com.walterade.callcrusher.utils.AndroidUtils;
import com.walterade.callcrusher.utils.DateUtils;
import com.walterade.callcrusher.utils.IntentUtils;
import com.walterade.callcrusher.utils.RxBus;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static android.content.IntentFilter.SYSTEM_HIGH_PRIORITY;


public class IncomingCallService extends LifecycleService {

    private static final int NOTIFICATION_ID = 100;
    private static final String CHANNEL_ID = "Call Crusher";
    private static final String ACTION_UNBLOCK = "unblock";
    private static final String ACTION_BLOCK = "block";

    private IncomingCallReceiver incomingCallReceiver;
    private LiveData<List<IncomingCall>> incoming;
    private LiveData<List<BlockedCallInfo>> blocked;
    private NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
    CompositeSubscription events = new CompositeSubscription();
    private IncomingCall lastCall;
    private boolean notificationShown;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent != null && intent.getAction() != null) {
            String phoneNumber = intent.getStringExtra("phoneNumber");
            String action = intent.getAction();
            Single.create((e) -> {
                switch (action) {
                    case ACTION_BLOCK:
                        BlockedCall.block(phoneNumber);
                        break;
                    case ACTION_UNBLOCK:
                        BlockedCall.unblock(phoneNumber);
                        break;
                }
            }).subscribeOn(Schedulers.io()).subscribe();
        }

        return Service.START_STICKY;
    }



    @Override
    public void onCreate() {
        super.onCreate();

        //so we can keep getting the incoming calls broadcast even if activity or application is killed
        registerIncomingCallReceiver();

        registerEvents();

        setUpBuilder();

        incoming = DatabaseManager.getDatabase(this).incomingCallDao().getAllLive();
        blocked = DatabaseManager.getDatabase().blockedCallDao().getAllLive();
        incoming.observe(this, incomingCalls -> {
            if (incomingCalls != null && incomingCalls.size() > 0) loadNotification(incomingCalls.get(0), false, true);
        });
        blocked.observe(this, blockList -> {
            loadNotification(lastCall, true, true);
        });

        if (new SettingsManager(this).getCallsCrushState() != SettingsManager.CRUSH_STATE_ALLOW)
            loadNotification(null, false, false);
    }

    private void registerEvents() {
        events.add(RxBus.getInstance().register(CallCrushStateChangeEvent.class, event -> {
            switch (event.state) {
                case SettingsManager.CRUSH_STATE_ALLOW:
                    removeNotification();
                    break;
                default:
                    if (!AndroidUtils.isServiceRunningInForeground(this, IncomingCallService.class)) {
                        CallCrusherApplication.getInstance().startCallCrusherService();
                        return;
                    }
                    int icon = getCallsStateIcon(event.state);
                    builder.setSmallIcon(icon);
                    sendNotification();
            }
        }));
    }

    int getCallsStateIcon(int state) {
        switch (state) {
            case SettingsManager.CRUSH_STATE_BLOCK:
                return R.drawable.ic_block_24dp;
            case SettingsManager.CRUSH_STATE_CRUSH:
                return R.drawable.ic_smash;
            case SettingsManager.CRUSH_STATE_ALLOW:
            default:
                return R.drawable.ic_block_disabled_24dp;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterIncomingCallReceiver();
        removeNotification();
        events.clear();
        incoming.removeObservers(this);
        incoming = null;
        blocked.removeObservers(this);
        blocked = null;
        builder.setContent(null);
        builder.setContentIntent(null);
        builder.setCustomContentView(null);
        builder = null;
        if (lastCall != null) {
            lastCall.dispose();
            lastCall = null;
        }
    }


    void setUpBuilder() {
        builder.setStyle(new NotificationCompat.DecoratedCustomViewStyle());
        builder.setOnlyAlertOnce(true);
        builder.setOngoing(true);
        builder.setCategory(NotificationCompat.CATEGORY_CALL);
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);

        // create the pending intent and add to the notification
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);
    }

    void registerIncomingCallReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        intentFilter.setPriority(SYSTEM_HIGH_PRIORITY - 1);
        incomingCallReceiver = new IncomingCallReceiver();
        registerReceiver(incomingCallReceiver, intentFilter);
    }

    void unregisterIncomingCallReceiver() {
        if (incomingCallReceiver != null) {
            unregisterReceiver(incomingCallReceiver);
            incomingCallReceiver = null;
        }
    }

    private void loadNotification(IncomingCall call, boolean refresh, boolean ifVisible) {
        if (ifVisible && !notificationShown) return;

        if (call != null) {
            showNotification(call);
            call.getInfo(this, (info) -> showNotification(call), (e) -> showNotification(call), refresh);
        }
        else showNotification(null);
    }

    synchronized private void showNotification(IncomingCall call) {
        // create the notification

        RemoteViews rv;
        int callType;

        if (call != null) {
            lastCall = call;

            rv = new RemoteViews(getPackageName(), R.layout.not_incoming);
            int id = 0;
            String info;
            callType = call.getCallType();

            if (call.getInfo() != null && call.getInfo().isContact && callType == IncomingCall.CALLTYPE_SUSPICIOUS)
                callType = IncomingCall.CALLTYPE_SAFE;

            switch (callType) {
                case IncomingCall.CALLTYPE_SUSPICIOUS:
                    id = R.drawable.ic_suspicious_24dp;
                    info = "Suspicious call on ";
                    break;
                case IncomingCall.CALLTYPE_BLOCK:
                    id = android.R.drawable.presence_busy;
                    info = "Blocked call on ";
                    break;
                case IncomingCall.CALLTYPE_SAFE:
                default:
                    id = android.R.drawable.presence_online;
                    info = "Safe call on ";
                    break;
            }
            rv.setImageViewResource(R.id.call_type, id);
            if (call.getInfo() != null && call.getInfo().callerName != null)
                rv.setTextViewText(R.id.number, call.getInfo().callerName);
            else rv.setTextViewText(R.id.number, PhoneNumberUtils.formatNumber(call.getPhoneNumber(), Locale.getDefault().getCountry()));
            rv.setTextViewText(R.id.date, info + DateUtils.format(getString(R.string.date_format), call.getCallDate()));
        }
        else rv = new RemoteViews(getPackageName(), R.layout.not_waiting);

        if (call != null) {
            Intent i;
            PendingIntent pi;
            CallInfo info = call.getInfo();
            builder.mActions.clear();

            if (info != null) {
                i = new Intent(info.isBlocked ? ACTION_UNBLOCK : ACTION_BLOCK, null, this, IncomingCallService.class);
                i.putExtra("phoneNumber", call.getPhoneNumber());
                pi = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.addAction(R.drawable.ic_block, info.isBlocked ? "Unblock" : "Block", pi);
            }

            try {
                i = IntentUtils.getWebBrowser("http://www.google.com/search?q=" + URLEncoder.encode(call.getPhoneNumber(), "utf-8"));
                pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.addAction(R.drawable.ic_search, "Lookup...", pi);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        int state = new SettingsManager(this).getCallsCrushState();
        builder.setSmallIcon(getCallsStateIcon(state));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            builder.setCustomContentView(rv);
        else builder.setContent(rv);

        // send the notification
        sendNotification();
    }

    private void sendNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (manager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT);
                manager.createNotificationChannel(channel);
                builder.setChannelId(CHANNEL_ID);
            }
            manager.notify(NOTIFICATION_ID, builder.build());
        }

        startForeground(NOTIFICATION_ID, builder.build());
        notificationShown = true;
    }

    private void removeNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(NOTIFICATION_ID);
        }
        stopForeground(true);
        notificationShown = false;
    }

}
