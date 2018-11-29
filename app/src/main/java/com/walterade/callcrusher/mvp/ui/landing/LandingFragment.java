package com.walterade.callcrusher.mvp.ui.landing;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.walterade.callcrusher.R;
import com.walterade.callcrusher.injection.component.FragmentSubComponent;
import com.walterade.callcrusher.mvp.data.model.BlockedCallInfo;
import com.walterade.callcrusher.mvp.data.model.IncomingCall;
import com.walterade.callcrusher.mvp.ui.adapters.LandingAdapter;
import com.walterade.callcrusher.mvp.ui.base.BaseFragment;
import com.walterade.callcrusher.utils.IntentUtils;
import com.walterade.callcrusher.utils.PhoneUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Walter on 1/31/18.
 */

public class LandingFragment extends BaseFragment implements LandingView, ViewPager.OnPageChangeListener, BottomNavigationView.OnNavigationItemSelectedListener {
    public static final String TAG = "LandingFragment";

    Handler handler;

    @BindView(R.id.pager) ViewPager pager;
    @BindView(R.id.bottom_nav) BottomNavigationView bottomNav;

    @Inject LandingPresenter presenter;
    @Inject LandingAdapter landingAdapter;
    @Inject IncomingCallsFragment incomingCallsFragment;
    @Inject BlockedNumbersFragment blockedNumbersFragment;

    ArrayList<CallsInfo> callsInfos = new ArrayList<>();
    CallsInfo incomingCallsInfo = new CallsInfo();
    CallsInfo blockedCallsInfo = new CallsInfo();
    private int oldPosition = -1;


    class CallsInfo {
        int menuId;
        int newItems;
        Fragment fragment;
        int tab;
        String title;
        Drawable icon;
    }

    public static LandingFragment newInstance() {
        return new LandingFragment();
    }


    @Override
    protected void inject(FragmentSubComponent component) {
        component.inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.frg_landing, container, false);
        ButterKnife.setDebug(true);
        ButterKnife.bind(this, v);

        buildTabs();
        pager.setAdapter(landingAdapter);
        pager.addOnPageChangeListener(this);
        bottomNav.setOnNavigationItemSelectedListener(this);
        handler = new Handler();

        return v;
    }


    private void buildCallsInfo() {
        CallsInfo ci = incomingCallsInfo;
        ci.fragment = incomingCallsFragment;
        ci.menuId = R.id.menu_item_incoming;
        ci.title = "Incoming Calls";
        ci.icon = ContextCompat.getDrawable(getContext(), android.R.drawable.sym_call_incoming);
        callsInfos.add(ci);

        ci = blockedCallsInfo;
        ci.fragment = blockedNumbersFragment;
        ci.menuId = R.id.menu_item_blocked;
        ci.title = "Block List";
        ci.icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_block).mutate();
        ci.icon.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
        callsInfos.add(ci);
    }

    private void buildTabs() {
        buildCallsInfo();

        int tab = 0;

        for (CallsInfo ci : callsInfos) {
            landingAdapter.add(ci.fragment, ci.title);
            ci.tab = tab++;
        }
    }

    private void showNewBadge(int index) {

        if (getActivity() != null) {

            CallsInfo ci = callsInfos.get(index);
            BottomNavigationMenuView m = (BottomNavigationMenuView) bottomNav.getChildAt(0);
            BottomNavigationItemView mi = (BottomNavigationItemView) m.getChildAt(ci.tab);
            TextView badge = mi.findViewById(R.id.badge);

            if (badge == null) {
                badge = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.vw_badge, m, false);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(badge.getLayoutParams());
                lp.gravity = Gravity.CENTER_HORIZONTAL;
                lp.leftMargin = getResources().getDimensionPixelOffset(R.dimen.badge_margin_left);
                lp.topMargin = getResources().getDimensionPixelOffset(R.dimen.badge_margin_top);
                mi.addView(badge, lp);
            }

            badge.setText(ci.newItems > 9 ? "9+" : String.valueOf(ci.newItems));
            badge.setVisibility(ci.newItems != 0 ? View.VISIBLE : View.GONE);
        }
    }

    void refreshBadges() {
        for (int i = 0; i < callsInfos.size(); i++)
            showNewBadge(i);
    }


    @Override
    public boolean onBackPressed() {
        if (pager.getCurrentItem() > 0) {
            pager.setCurrentItem(pager.getCurrentItem() - 1, true);
            return true;
        }
        return false;
    }

    @Override
    public void onAttachPresenter() {
        presenter.attachView(this);
        presenter.restoreState();
        presenter.pageSelected(oldPosition, pager.getCurrentItem());
        oldPosition = pager.getCurrentItem();
    }

    @Override
    public void onDetachPresenter() {
        presenter.detachView();
        handler.removeCallbacksAndMessages(this);
        Bundle b = new Bundle();
        b.putInt("tab", pager.getCurrentItem());
        presenter.saveState(b);
    }

    @Override
    protected void onBeforeVisible() {
        incomingCallsFragment.refreshIncomingCallAdapter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        for (CallsInfo ci : callsInfos) {
            ci.fragment = null;
            ci.icon = null;
            ci.title = null;
        }

        callsInfos.clear();
        presenter = null;
        incomingCallsInfo = null;
        blockedCallsInfo = null;
        handler = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        landingAdapter.clear();
        landingAdapter = null;
        pager.setAdapter(null);
        pager.clearOnPageChangeListeners();
        pager = null;
        bottomNav = null;
        incomingCallsFragment = null;
        blockedNumbersFragment = null;
    }

    @Override
    public void setBlockedCalls(List<BlockedCallInfo> calls) {
        if (blockedNumbersFragment != null)
            blockedNumbersFragment.setBlockedCalls(calls);
    }

    @Override
    public void setIncomingCalls(List<IncomingCall> calls) {
        if (incomingCallsFragment != null)
            incomingCallsFragment.setIncomingCalls(calls);
    }

    @Override
    public void showIncomingCallDialog(IncomingCall call) {

    }

    @Override
    public void showAddBlockedCallDialog() {

    }

    @Override
    public void showAddContact(String name, String phoneNumber) {
        if (getActivity() != null)
            IntentUtils.addContact(getActivity(), name, phoneNumber);
    }

    @Override
    public void showWebBrowser(String url) {
        if (getActivity() != null)
            IntentUtils.showWebBrowser(getActivity(), url);
    }

    @Override
    public void showNewBlockedCall() {
        if (handler != null)
            handler.postDelayed(() -> {if (blockedNumbersFragment != null) blockedNumbersFragment.smoothScrollToBlockListPosition(0);}, 500);
    }

    @Override
    public void showNewIncomingCall() {
        if (handler != null)
            handler.postDelayed(() -> {if (incomingCallsFragment != null) incomingCallsFragment.smoothScrollToCallHistoryPosition(0);}, 500);
    }

    @Override
    public void refreshIncomingCalls() {
        if (incomingCallsFragment != null)
            incomingCallsFragment.refreshIncomingCallAdapter();
    }

    @Override
    public void showPhoneRinging(long incomingCallId) {
        if (incomingCallsFragment != null)
            incomingCallsFragment.setPhoneRinging(incomingCallId);
    }

    @Override
    public void hidePhoneRinging() {
        if (incomingCallsFragment != null)
            incomingCallsFragment.setPhoneRinging(0);
    }

    @Override
    public void endCall() {
        if (getActivity() != null)
            PhoneUtils.endCall(getActivity());
    }

    @Override
    public void setNewBlockedCount(int count) {

        if (pager != null) {
            if (pager.getCurrentItem() == blockedCallsInfo.tab) count = 0;

            blockedCallsInfo.newItems = count;
            refreshBadges();
        }
    }

    @Override
    public void setNewIncomingCount(int count) {

        if (pager != null) {
            if (pager.getCurrentItem() == incomingCallsInfo.tab) count = 0;

            incomingCallsInfo.newItems = count;
            refreshBadges();
        }
    }

    @Override
    public void maximizeIncomingCalls() {

    }

    @Override
    public void maximizeBlockedCalls() {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        presenter.pageSelected(oldPosition, position);
        oldPosition = position;
    }

    @Override
    public void setBottomNavigationTab(int position) {
        CallsInfo ci = callsInfos.get(position);
        ci.newItems = 0;
        refreshBadges();
        if (bottomNav.getSelectedItemId() != ci.menuId)
            bottomNav.setSelectedItemId(ci.menuId);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int index = -1;

        for (int i = 0; i < callsInfos.size(); i++)
            if (callsInfos.get(i).menuId == item.getItemId()) {
                index = i;
                break;
            }

        if (index >= 0)
            pager.setCurrentItem(index, true);

        return true;
    }

    @Override
    public void showIncomingCallRemoved(IncomingCall call, int position) {
        if (getView() != null) {
            Snackbar snackbar = Snackbar
                    .make(getView(), call.toString() + " removed from incoming calls", getResources().getInteger(R.integer.snackbar_duration));
            snackbar.setAction("UNDO", view -> presenter.undoIncomingCallRemovalClicked(call, position));
            snackbar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    if (event != DISMISS_EVENT_ACTION)
                        presenter.removeIncomingCall(call);
                }
            });
            TextView tv = snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
            tv.setMaxLines(5);
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    @Override
    public void showBlockedCallRemoved(BlockedCallInfo call, int position) {
        if (getView() != null) {
            Snackbar snackbar = Snackbar
                    .make(getView(), call.toString() + " removed from block list", getResources().getInteger(R.integer.snackbar_duration));
            snackbar.setAction("UNDO", view -> presenter.undoBlockedCallRemovalClicked(call, position));
            snackbar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                if (event != DISMISS_EVENT_ACTION)
                    presenter.removeBlockedCall(call);
                }
            });
            TextView tv = snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
            tv.setMaxLines(5);
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }

    }

    @Override
    public void undoIncomingCallRemoval(IncomingCall call, int position) {
        if (incomingCallsFragment != null)
            incomingCallsFragment.restoreItem(call, position);
    }

    @Override
    public void undoBlockedCallRemoval(BlockedCallInfo call, int position) {
        if (blockedNumbersFragment != null)
            blockedNumbersFragment.restoreItem(call, position);
    }

    @Override
    public void showPhoneCall(IncomingCall call) {
        if (getContext() != null)
            IntentUtils.phoneCall(getContext(), call.getPhoneNumber());
    }

}
