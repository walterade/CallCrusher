package com.walterade.callcrusher.mvp.ui.landing;

import com.walterade.callcrusher.mvp.data.model.BlockedCallInfo;
import com.walterade.callcrusher.mvp.data.model.IncomingCall;
import com.walterade.callcrusher.mvp.ui.base.BaseView;

import java.util.List;

public interface LandingView extends BaseView {
    void maximizeIncomingCalls();
    void maximizeBlockedCalls();
    void setBlockedCalls(List<BlockedCallInfo> calls);
    void setIncomingCalls(List<IncomingCall> calls);
    void showIncomingCallDialog(IncomingCall call);
    void showAddBlockedCallDialog();
    void showAddContact(String name, String phoneNumber);
    void showWebBrowser(String url);
    void showNewBlockedCall();
    void showNewIncomingCall();
    void refreshIncomingCalls();
    void showPhoneRinging(long incomingCallId);
    void hidePhoneRinging();
    void endCall();
    void setNewIncomingCount(int count);
    void setNewBlockedCount(int count);
    void setBottomNavigationTab(int position);
    void showIncomingCallRemoved(IncomingCall call, int position);
    void showBlockedCallRemoved(BlockedCallInfo call, int position);
    void undoIncomingCallRemoval(IncomingCall call, int position);
    void undoBlockedCallRemoval(BlockedCallInfo call, int position);
    void showPhoneCall(IncomingCall call);
}

