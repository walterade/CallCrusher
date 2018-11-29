package com.walterade.callcrusher.mvp.ui.landing;

import android.arch.lifecycle.LiveData;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;

import com.walterade.callcrusher.event.PhoneRingingEvent;
import com.walterade.callcrusher.event.PhoneRingingStoppedEvent;
import com.walterade.callcrusher.injection.component.PerLandingFragment;
import com.walterade.callcrusher.manager.SettingsManager;
import com.walterade.callcrusher.mvp.data.model.BlockedCall;
import com.walterade.callcrusher.mvp.data.model.BlockedCallInfo;
import com.walterade.callcrusher.mvp.data.model.IncomingCall;
import com.walterade.callcrusher.mvp.ui.base.BasePresenter;
import com.walterade.callcrusher.utils.RxBus;
import com.walterade.callcrusher.utils.RxBusUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import javax.inject.Inject;

import rx.Single;
import rx.schedulers.Schedulers;

@PerLandingFragment
public class LandingPresenter extends BasePresenter<LandingView> {

    Handler handler = new Handler();
    RxBusUtils.DisposableSubscriptions events = new RxBusUtils.DisposableSubscriptions();
    LiveData<List<BlockedCallInfo>> blocked;
    LiveData<List<IncomingCall>> incoming;
    LiveData<Integer> newBlocked;
    LiveData<Integer> newIncoming;

    @Inject
    SettingsManager settings;


    @Inject
    public LandingPresenter() {
    }

    @Override
    public void attachView(LandingView landingView) {
        super.attachView(landingView);
        loadCalls();

        events.add(RxBus.getInstance().register(PhoneRingingEvent.class, (event -> {
            getView().showPhoneRinging(event.incomingCallId);
        })));

        events.add(RxBus.getInstance().register(PhoneRingingStoppedEvent.class, (event -> {
            getView().hidePhoneRinging();
        })));
    }

    @Override
    public void detachView() {

        handler.removeCallbacksAndMessages(null);

        if (blocked != null) {
            blocked.removeObservers((Fragment) getView());
            blocked = null;
        }
        if (incoming != null) {
            incoming.removeObservers((Fragment) getView());
            incoming = null;
        }
        if (newIncoming != null) {
            newIncoming.removeObservers((Fragment) getView());
            newIncoming = null;
        }
        if (newBlocked != null) {
            newBlocked.removeObservers((Fragment) getView());
            newBlocked = null;
        }

        events.dispose();

        super.detachView();
    }

    public void loadCalls() {

        if (!isViewAttached()) return;

        blocked = BlockedCall.getAllLive();
        blocked.observe((Fragment) getView(), entries -> {
            getView().setBlockedCalls(blocked.getValue());
            getView().refreshIncomingCalls();
        });

        incoming = IncomingCall.getAllLive();
        incoming.observe((Fragment) getView(), entries -> getView().setIncomingCalls(incoming.getValue()));

        updateNewCallsCountObsevers();

        Single.create((e) -> {
            getView().setIncomingCalls(IncomingCall.getAll());
            getView().setBlockedCalls(BlockedCall.getAll());
        })
        .subscribeOn(Schedulers.io()).subscribe();
    }

    void updateNewCallsCountObsevers() {
        long time = settings.getBlockedCallsLastViewTime();
        if (newBlocked != null) newBlocked.removeObservers((Fragment) getView());
        newBlocked = BlockedCall.getCountSince(time);
        newBlocked.observe((Fragment) getView(), count -> getView().setNewBlockedCount(count));

        time = settings.getIncomingCallsLastViewTime();
        if (newIncoming != null) newIncoming.removeObservers((Fragment) getView());
        newIncoming = IncomingCall.getCountSince(time);
        newIncoming.observe((Fragment) getView(), count -> getView().setNewIncomingCount(count));
    }

    public void unblockNumber(String phoneNumber) {
        Single.create((e) -> BlockedCall.unblock(phoneNumber)).subscribeOn(Schedulers.io()).subscribe();
    }

    public void unblockClicked(IncomingCall call) {
        unblockNumber(call.getPhoneNumber());
    }

    public void blockClicked(IncomingCall call, boolean isPhoneRinging) {
        if (isPhoneRinging) getView().endCall();
        Single.create((e) -> BlockedCall.block(call.getPhoneNumber())).subscribeOn(Schedulers.io()).subscribe();
    }

    public void searchClicked(IncomingCall call) {
        try {
            getView().showWebBrowser("http://www.google.com/search?q=" + URLEncoder.encode(call.getPhoneNumber(), "UTF-8" ));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void addContactClicked(IncomingCall call) {
        getView().showAddContact("", call.getDisplayPhoneNumber());
    }

    public void newBlockedCallAdded() {
        getView().showNewBlockedCall();
    }

    public void newIncomingCallAdded() {
        getView().showNewIncomingCall();
    }

    public void pageSelected(int oldPosition, int position) {

        if (isViewAttached()) {
            getView().setBottomNavigationTab(position);
            int[] positions = {oldPosition, position};

            for (int pos : positions)
                switch (pos) {
                    case 0:
                        settings.setIncomingCallsLastViewTime(System.currentTimeMillis());
                        break;
                    case 1:
                        settings.setBlockedCallsLastViewTime(System.currentTimeMillis());
                        break;
                }

            updateNewCallsCountObsevers();
        }
    }

    public void incomingCallSwiped(IncomingCall call, int position) {
        getView().showIncomingCallRemoved(call, position);
    }

    public void removeIncomingCall(IncomingCall call) {
        Single.create((e) -> IncomingCall.delete(call)).subscribeOn(Schedulers.io()).subscribe();
    }

    public void blockedCallSwiped(BlockedCallInfo call, int position) {
        getView().showBlockedCallRemoved(call, position);
    }

    public void removeBlockedCall(BlockedCallInfo call) {
        unblockNumber(call.getPhoneNumber());
    }

    public void undoIncomingCallRemovalClicked(IncomingCall call, int position) {
        getView().undoIncomingCallRemoval(call, position);
    }

    public void undoBlockedCallRemovalClicked(BlockedCallInfo call, int position) {
        getView().undoBlockedCallRemoval(call, position);
    }

    public void phoneNumberClicked(IncomingCall call) {
        getView().showPhoneCall(call);
    }

    public void saveState(Bundle b) {
        if (b != null) {
            int tab = b.getInt("tab", 0);
            settings.setCurrentTab(tab);
        }
    }
    public void restoreState() {
        int tab = settings.getCurrentTab();
        getView().setBottomNavigationTab(tab);
    }
}
