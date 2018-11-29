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
import com.walterade.callcrusher.mvp.data.model.BlockedCallInfo;
import com.walterade.callcrusher.mvp.ui.adapters.BlockedNumbersAdapter;
import com.walterade.callcrusher.utils.AndroidUtils;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;


public class BlockedNumbersFragment extends Fragment implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    @BindView(R.id.block_list)
    RecyclerView blockList;

    @BindView(R.id.empty)
    View empty;

    @Inject
    BlockedNumbersAdapter blockedNumbersAdapter;

    @Inject
    public BlockedNumbersFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frg_blocked_numbers, container, false);
        ButterKnife.bind(this, v);

        LinearLayoutManager lm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        blockList.setLayoutManager(lm);
        blockList.setAdapter(blockedNumbersAdapter);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, this) {
            @Override
            public View onGetForegroundView(RecyclerView.ViewHolder viewHolder, float dX) {
                BlockedNumbersAdapter.BlockedCallViewHolder vh = (BlockedNumbersAdapter.BlockedCallViewHolder) viewHolder;
                vh.deleteLeft.setVisibility(dX > 0 ? View.VISIBLE : View.GONE);
                vh.deleteRight.setVisibility(dX < 0 ? View.VISIBLE : View.GONE);
                return vh.foreground;
            }
        };
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(blockList);

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        blockList.setAdapter(null);
        blockList.setLayoutManager(null);
        blockList = null;
        blockedNumbersAdapter = null;
        empty = null;
    }

    public void setBlockedCalls(List<BlockedCallInfo> calls) {
        AndroidUtils.runOnUIThread(() -> {
            boolean isEmpty = calls.isEmpty();
            if (empty != null) empty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            if (blockedNumbersAdapter != null)
                blockedNumbersAdapter.setBlockedCalls(calls);
        });
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (blockedNumbersAdapter != null)
            blockedNumbersAdapter.removeItem(position);
    }

    public void smoothScrollToBlockListPosition(int position) {
        if (blockList != null)
            blockList.smoothScrollToPosition(position);
    }

    public void restoreItem(BlockedCallInfo call, int position) {
        if (blockedNumbersAdapter != null)
            blockedNumbersAdapter.restoreItem(call, position);
    }
}
