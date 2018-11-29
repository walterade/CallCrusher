package com.walterade.callcrusher.mvp.data.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.walterade.callcrusher.application.CallCrusherApplication;
import com.walterade.callcrusher.manager.DatabaseManager;

import java.util.Date;
import java.util.List;


@Entity(tableName = "blocked_calls")
public class BlockedCall extends PhoneCall {

    @ColumnInfo(name = "block_date")
    private long blockDate;


    @Dao
    public interface BlockedCallDao {
        @Query("SELECT * from blocked_calls where id = :id")
        BlockedCall get(int id);

        @Query("SELECT * from blocked_calls where phone_number = :number")
        BlockedCall getByPhoneNumber(String number);

        @Query("SELECT b.*, a.last_call_date, c.block_count from blocked_calls b left join " +
                "(SELECT phone_number, MAX(call_date) as last_call_date FROM incoming_calls GROUP BY phone_number) a on b.phone_number = a.phone_number " +
                "left join " +
                "(SELECT phone_number, count(*) as block_count FROM incoming_calls WHERE call_type = " + IncomingCall.CALLTYPE_BLOCK + " GROUP BY phone_number) c on b.phone_number = c.phone_number " +
                "ORDER BY block_date desc")
        LiveData<List<BlockedCallInfo>> getAllLive();

        @Query("SELECT b.*, a.last_call_date, c.block_count from blocked_calls b left join " +
                "(SELECT phone_number, MAX(call_date) as last_call_date FROM incoming_calls GROUP BY phone_number) a on b.phone_number = a.phone_number " +
                "left join " +
                "(SELECT phone_number, count(*) as block_count FROM incoming_calls WHERE call_type = " + IncomingCall.CALLTYPE_BLOCK + " GROUP BY phone_number) c on b.phone_number = c.phone_number " +
                "ORDER BY block_date desc")
        List<BlockedCallInfo> getAll();

        @Query("SELECT Count(*) from blocked_calls where block_date > :time")
        LiveData<Integer> getCountSince(long time);

        @Insert
        long insert(BlockedCall blockedCall);

        @Delete
        int delete(BlockedCall blockedCall);

        @Query("DELETE from blocked_calls WHERE phone_number = :number")
        int delete(String number);
    }

    public static boolean isBlocked(String phoneNumber) {
        phoneNumber = IncomingCall.normalizeNumber(phoneNumber);
        BlockedCall call = CallCrusherApplication.getComponent().getDatabase().blockedCallDao().getByPhoneNumber(phoneNumber);
        return call != null;
    }

    public static int unblock(String phoneNumber) {
        phoneNumber = IncomingCall.normalizeNumber(phoneNumber);
        return DatabaseManager.getDatabase().blockedCallDao().delete(phoneNumber);
    }

    public static void block(String phoneNumber) {
        if (!isBlocked(phoneNumber)) {
            BlockedCall call = new BlockedCall();
            call.setPhoneNumber(phoneNumber);
            call.setBlockDate(new Date().getTime());
            DatabaseManager.getDatabase().blockedCallDao().insert(call);
        }
    }

    public static List<BlockedCallInfo> getAll() {
        return DatabaseManager.getDatabase().blockedCallDao().getAll();
    }

    public static LiveData<List<BlockedCallInfo>> getAllLive() {
        MediatorLiveData<List<BlockedCallInfo>> mlive = new MediatorLiveData<>();
        LiveData<List<BlockedCallInfo>> live = CallCrusherApplication.getComponent().getDatabase().blockedCallDao().getAllLive();

        mlive.addSource(live, mlive::postValue);

        return mlive;

    }

    public static LiveData<Integer> getCountSince(long time) {
        return DatabaseManager.getDatabase().blockedCallDao().getCountSince(time);
    }

    public long getBlockDate() {
        return blockDate;
    }

    public void setBlockDate(long blockDate) {
        this.blockDate = blockDate;
    }

}
