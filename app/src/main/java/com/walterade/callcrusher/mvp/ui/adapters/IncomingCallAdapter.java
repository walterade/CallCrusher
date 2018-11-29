package com.walterade.callcrusher.mvp.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.ListUpdateCallback;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.walterade.callcrusher.R;
import com.walterade.callcrusher.injection.component.PerLandingFragment;
import com.walterade.callcrusher.mvp.data.model.CallInfo;
import com.walterade.callcrusher.mvp.data.model.IncomingCall;
import com.walterade.callcrusher.mvp.ui.landing.LandingPresenter;
import com.walterade.callcrusher.utils.AndroidUtils;
import com.walterade.callcrusher.utils.DateUtils;
import com.walterade.callcrusher.widget.BouncyTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

@PerLandingFragment
public class IncomingCallAdapter extends RecyclerView.Adapter<IncomingCallAdapter.CallHistoryViewHolder> {

    private ArrayList<IncomingCall> incomingCalls = new ArrayList<>();
    private HashMap<String, CallInfo> callInfo = new HashMap<>();
    private String dateFormat;
    @Inject
    public LandingPresenter presenter;
    private Context context;
    private long phoneRingingIncomingCallId;


    @Inject public IncomingCallAdapter() {}


    public void setIncomingCalls(List<IncomingCall> calls) {
        DiffCallback diff = new DiffCallback(calls, incomingCalls);
        DiffUtil.DiffResult dr = DiffUtil.calculateDiff(diff);
        incomingCalls.clear();
        incomingCalls.addAll(calls);
        dr.dispatchUpdatesTo(this);
        dr.dispatchUpdatesTo(new UpdateCallBack());
        setAllDirty();
    }

    void setAllDirty() {
        for (CallInfo ci : callInfo.values())
            ci.isDirty = true;
    }

    public void refresh() {
        setAllDirty();
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        presenter.incomingCallSwiped(incomingCalls.get(position), position);
        incomingCalls.remove(position);
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
        callInfo.clear();
        incomingCalls.clear();
        presenter = null;
        context = null;
    }

    @NonNull
    @Override
    public CallHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.itm_incoming_call, parent, false);
        return new CallHistoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CallHistoryViewHolder holder, int position) {
        int id;
        IncomingCall call = incomingCalls.get(position);
        int callType = call.getCallType();
        CallInfo info = getInfo(call);
        boolean isRinging = call.getId() == phoneRingingIncomingCallId;
        String phoneNumber = PhoneNumberUtils.formatNumber(call.getPhoneNumber(), Locale.getDefault().getCountry());

        holder.phoneNumber.setText(phoneNumber);
        holder.date.setText(DateUtils.format(dateFormat, call.getCallDate()));
        holder.incoming.setVisibility(isRinging ? View.VISIBLE : View.INVISIBLE);

        if (info.isLoaded) {
            if (info.callerName != null) holder.phoneNumber.setText(info.callerName);
            holder.block.setText(info.isBlocked ? "Unblock" : "Block");
            holder.addContact.setVisibility((!info.isContact && !isRinging) ? View.VISIBLE : View.INVISIBLE);
            if (info.isContact && callType == IncomingCall.CALLTYPE_SUSPICIOUS)
                callType = IncomingCall.CALLTYPE_SAFE;
        }

        switch (callType) {
            case IncomingCall.CALLTYPE_SUSPICIOUS:
                id = R.drawable.ic_suspicious_48dp;
                break;
            case IncomingCall.CALLTYPE_BLOCK:
                id = android.R.drawable.presence_busy;
                break;
            case IncomingCall.CALLTYPE_SAFE:
            default:
                id = android.R.drawable.presence_online;
                break;
        }

        holder.callType.setImageResource(id);
    }

    @Override
    public int getItemCount() {
        return incomingCalls.size();
    }

    @SuppressLint("CheckResult")
    private CallInfo getInfo(IncomingCall call) {

        CallInfo info = callInfo.get(call.getPhoneNumber());
        if (info == null) info = new CallInfo();

        if (info.isDirty) {
            String phoneNumber = call.getPhoneNumber();
            callInfo.put(phoneNumber, info);

            call.setCallerName(null);
            IncomingCall.getCallerInfo(context, phoneNumber, (info1) -> {
                CallInfo ci = callInfo.get(phoneNumber);
                if (ci != null) {
                    if (info1 != null) {
                        call.setCallerName(info1.callerName);
                        ci.isContact = info1.isContact;
                        ci.callerName = info1.callerName;
                        ci.imageUri = info1.imageUri;
                        ci.isBlocked = info1.isBlocked;
                    }
                    ci.isLoaded = true;
                    ci.isDirty = false;
                }
            }, (name) -> AndroidUtils.runOnUIThread(this::notifyDataSetChanged), null);

        }
        else call.setCallerName(info.callerName);

        return info;
    }

    public void setPhoneRinging(long incomingCallId) {
        AndroidUtils.runOnUIThread(() -> {
            if (phoneRingingIncomingCallId != incomingCallId) {
                phoneRingingIncomingCallId = incomingCallId;
                notifyDataSetChanged();
            }
        });
    }

    public void restoreItem(IncomingCall call, int position) {
        incomingCalls.add(position, call);
        notifyItemInserted(position);
    }

    public class CallHistoryViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.number) BouncyTextView phoneNumber;
        @BindView(R.id.date) TextView date;
        @BindView(R.id.call_type) ImageView callType;
        @BindView(R.id.block) TextView block;
        @BindView(R.id.search) TextView search;
        @BindView(R.id.add_contact) TextView addContact;
        @BindView(R.id.incoming) ImageView incoming;
        @BindView(R.id.foreground) public View foreground;
        @BindView(R.id.background) public View background;
        @BindView(R.id.delete_left) public View deleteLeft;
        @BindView(R.id.delete_right) public View deleteRight;


        int position() {
            int i = getAdapterPosition();
            if (i == -1) i = getOldPosition();
            return i;
        }

        public CallHistoryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            addContact.setVisibility(View.INVISIBLE);
            incoming.setVisibility(View.INVISIBLE);
            addContact.getCompoundDrawablesRelative()[0].mutate().setColorFilter(addContact.getCurrentTextColor(), PorterDuff.Mode.SRC_IN);
            block.getCompoundDrawablesRelative()[0].mutate().setColorFilter(block.getCurrentTextColor(), PorterDuff.Mode.SRC_IN);
            search.getCompoundDrawablesRelative()[0].mutate().setColorFilter(search.getCurrentTextColor(), PorterDuff.Mode.SRC_IN);

            block.setOnClickListener(v -> {
                int i = position();
                if (i != -1) {
                    IncomingCall call = incomingCalls.get(i);
                    CallInfo info = getInfo(call);
                    if (!info.isBlocked)
                        presenter.blockClicked(call, phoneRingingIncomingCallId == call.getId());
                    else presenter.unblockClicked(call);
                }
            });
            search.setOnClickListener(v -> {
                int i = position();
                if (i != -1) presenter.searchClicked(incomingCalls.get(i));
            });
            addContact.setOnClickListener(v -> {
                int i = position();
                if (i != -1) presenter.addContactClicked(incomingCalls.get(i));
            });
            phoneNumber.setOnClickListener(V -> {
                int i = position();
                if (i != -1) presenter.phoneNumberClicked(incomingCalls.get(i));
            });
        }

    }

    public class UpdateCallBack implements ListUpdateCallback {
        @Override
        public void onInserted(int position, int count) {
            if (position == 0)
                presenter.newIncomingCallAdded();
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

        List<IncomingCall> oldCalls;
        List<IncomingCall> newCalls;

        public DiffCallback(List<IncomingCall> newCalls, List<IncomingCall> oldCalls) {
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
            return super.getChangePayload(oldItemPosition, newItemPosition);
        }
    }
}
