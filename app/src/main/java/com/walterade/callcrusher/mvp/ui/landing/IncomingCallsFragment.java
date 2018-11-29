package com.walterade.callcrusher.mvp.ui.landing;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.walterade.callcrusher.R;
import com.walterade.callcrusher.injection.component.PerLandingFragment;
import com.walterade.callcrusher.mvp.data.model.IncomingCall;
import com.walterade.callcrusher.mvp.ui.adapters.IncomingCallAdapter;
import com.walterade.callcrusher.utils.AndroidUtils;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

@PerLandingFragment
public class IncomingCallsFragment extends Fragment implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {
    @BindView(R.id.call_history)
    RecyclerView callHistory;

    @BindView(R.id.empty)
    View empty;

    @Inject
    IncomingCallAdapter incomingCallAdapter;

    @Inject
    public IncomingCallsFragment() {}


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frg_incoming_calls, container, false);
        ButterKnife.bind(this, v);

        LinearLayoutManager lm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        callHistory.setLayoutManager(lm);
        callHistory.setAdapter(incomingCallAdapter);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, this) {
            @Override
            public View onGetForegroundView(RecyclerView.ViewHolder viewHolder, float dX) {
                IncomingCallAdapter.CallHistoryViewHolder vh = (IncomingCallAdapter.CallHistoryViewHolder) viewHolder;
                vh.deleteLeft.setVisibility(dX > 0 ? View.VISIBLE : View.GONE);
                vh.deleteRight.setVisibility(dX < 0 ? View.VISIBLE : View.GONE);
                return vh.foreground;
            }
        };
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(callHistory);

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        callHistory.setAdapter(null);
        callHistory.setLayoutManager(null);
        callHistory = null;
        incomingCallAdapter = null;
        empty = null;
    }

    public void setIncomingCalls(List<IncomingCall> calls) {
        AndroidUtils.runOnUIThread(()  -> {
            boolean isEmpty = calls.isEmpty();
            if (empty != null) empty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            if (incomingCallAdapter != null)
                incomingCallAdapter.setIncomingCalls(calls);
        });
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (incomingCallAdapter != null)
            incomingCallAdapter.removeItem(position);
    }

    public void setPhoneRinging(long incomingCallId) {
        if (incomingCallAdapter != null)
            incomingCallAdapter.setPhoneRinging(incomingCallId);
    }

    public void refreshIncomingCallAdapter() {
        if (incomingCallAdapter != null)
            incomingCallAdapter.refresh();
    }

    public void smoothScrollToCallHistoryPosition(int position) {
        if (callHistory != null)
            callHistory.smoothScrollToPosition(position);
    }
    
    public void restoreItem(IncomingCall call, int position) {
        if (incomingCallAdapter != null)
            incomingCallAdapter.restoreItem(call, position);
    }
}
