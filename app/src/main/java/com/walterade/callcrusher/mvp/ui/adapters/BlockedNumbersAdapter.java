package com.walterade.callcrusher.mvp.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.ListUpdateCallback;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.walterade.callcrusher.R;
import com.walterade.callcrusher.injection.component.PerLandingFragment;
import com.walterade.callcrusher.mvp.data.model.BlockedCallInfo;
import com.walterade.callcrusher.mvp.ui.landing.LandingPresenter;
import com.walterade.callcrusher.utils.AndroidUtils;
import com.walterade.callcrusher.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

@PerLandingFragment
public class BlockedNumbersAdapter extends RecyclerView.Adapter<BlockedNumbersAdapter.BlockedCallViewHolder> {

    private ArrayList<BlockedCallInfo> blockedCalls = new ArrayList<>();

    Context context;
    String dateFormat;
    @Inject public LandingPresenter presenter;
    @Inject public BlockedNumbersAdapter() {}

    public void setBlockedCalls(List<BlockedCallInfo> calls) {
        DiffCallback diff = new BlockedNumbersAdapter.DiffCallback(calls, this.blockedCalls);
        DiffUtil.DiffResult dr = DiffUtil.calculateDiff(diff);
        this.blockedCalls.clear();
        this.blockedCalls.addAll(calls);
        dr.dispatchUpdatesTo(this);
        dr.dispatchUpdatesTo(new UpdateCallBack());
    }

    public void removeItem(int position) {
        presenter.blockedCallSwiped(blockedCalls.get(position), position);
        blockedCalls.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        context = recyclerView.getContext();
        dateFormat = context.getString(R.string.date_format);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        dateFormat = null;
        blockedCalls.clear();
        presenter = null;
        context = null;
    }


    @NonNull
    @Override
    public BlockedCallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.itm_blocked_number, parent, false);
        return new BlockedCallViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BlockedCallViewHolder holder, int position) {
        BlockedCallInfo call = blockedCalls.get(position);
        String name = call.getCallerId(context,null, (n) -> AndroidUtils.runOnUIThread(this::notifyDataSetChanged), null);
        String phone = PhoneNumberUtils.formatNumber(call.getPhoneNumber(), Locale.getDefault().getCountry());
        String info = name != null ? phone : "";

        if (call.getLastCallDate() != 0) {
            if (!info.isEmpty()) info += "\n";
            info += "Last called you on " + DateUtils.format(dateFormat, call.getLastCallDate());
        }
        if (call.getBlockCount() != 0) {
            if (!info.isEmpty()) info += "\n";
            info += "Blocked " + call.getBlockCount() + " time" + ((call.getBlockCount() != 1 ? "s" : ""));
        }

        holder.phoneNumber.setText(name != null ? name : phone);
        holder.info.setVisibility(TextUtils.isEmpty(info) ? View.GONE : View.VISIBLE);
        holder.infoIcon.setVisibility(holder.info.getVisibility());
        holder.info.setText(info);

    }

    @Override
    public int getItemCount() {
        return blockedCalls.size();
    }

    public void restoreItem(BlockedCallInfo call, int position) {
        blockedCalls.add(position, call);
        notifyItemInserted(position);
    }


    public class BlockedCallViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.number) TextView phoneNumber;
        @BindView(R.id.info) TextView info;
        @BindView(R.id.info_icon) View infoIcon;
        @BindView(R.id.unblock) ImageView unblock;
        @BindView(R.id.foreground) public View foreground;
        @BindView(R.id.background) public View background;
        @BindView(R.id.delete_left) public View deleteLeft;
        @BindView(R.id.delete_right) public View deleteRight;

        public BlockedCallViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            unblock.setOnClickListener(v -> {
                int i = getAdapterPosition();
                if (i == -1) i = getOldPosition();
                if (i != -1)
                    removeItem(i);
            });
        }

    }

    public class UpdateCallBack implements ListUpdateCallback {
        @Override
        public void onInserted(int position, int count) {
            if (position == 0)
                presenter.newBlockedCallAdded();
        }

        @Override
        public void onRemoved(int position, int count) {

        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {

        }

        @Override
        public void onChanged(int position, int count, Object payload) {

        }
    }

    public class DiffCallback extends DiffUtil.Callback{

        List<BlockedCallInfo> oldCalls;
        List<BlockedCallInfo> newCalls;

        public DiffCallback(List<BlockedCallInfo> newCalls, List<BlockedCallInfo> oldCalls) {
            this.newCalls = newCalls;
            this.oldCalls = oldCalls;
        }

        @Override
        public int getOldListSize() {
            return oldCalls.size();
        }

        @Override
        public int getNewListSize() {
            return newCalls.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldCalls.get(oldItemPosition).getId() == newCalls.get(newItemPosition).getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldCalls.get(oldItemPosition).equals(newCalls.get(newItemPosition));
        }

        @Nullable
        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            //you can return particular field for changed item.
            return super.getChangePayload(oldItemPosition, newItemPosition);
        }
    }

}
