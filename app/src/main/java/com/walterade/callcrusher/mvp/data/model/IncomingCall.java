package com.walterade.callcrusher.mvp.data.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.telephony.PhoneNumberUtils;

import com.walterade.callcrusher.manager.DatabaseManager;

import java.util.List;


@Entity(tableName = "incoming_calls")
public class IncomingCall extends PhoneCall {

    public static final int CALLTYPE_SAFE = 0;
    public static final int CALLTYPE_SUSPICIOUS = 1;
    public static final int CALLTYPE_BLOCK = 2;
    public static final int CALLTYPE_BLOCK_ERROR = 3;

    @ColumnInfo(name = "call_date")
    private long callDate;

    @ColumnInfo(name = "call_type")
    private int callType;


    @Dao
    public interface IncomingCallDao {
        @Query("SELECT * from incoming_calls " +
                "where id = :id")
        IncomingCall get(int id);

        @Query("SELECT * from incoming_calls ORDER BY call_date desc")
        LiveData<List<IncomingCall>> getAllLive();

        @Query("SELECT * from incoming_calls ORDER BY call_date desc")
        List<IncomingCall> getAll();

        @Query("SELECT Count(*) from incoming_calls WHERE call_date > :time")
        LiveData<Integer> getCountSince(long time);

        @Insert
        long insert(IncomingCall call);

        @Delete
        void delete(IncomingCall call);
        }


    public static String normalizeNumber(String number) {
        return PhoneNumberUtils.getStrippedReversed(number);
    }

    public static long add(IncomingCall call) {
        long id = DatabaseManager.getDatabase().incomingCallDao().insert(call);
        call.setId(id);
        return id;
    }

    public static void delete(IncomingCall call) {
        DatabaseManager.getDatabase().incomingCallDao().delete(call);
    }

    public static List<IncomingCall> getAll() {
        return DatabaseManager.getDatabase().incomingCallDao().getAll();
    }

    public static LiveData<List<IncomingCall>> getAllLive() {
        MediatorLiveData<List<IncomingCall>> mlive = new MediatorLiveData<>();
        LiveData<List<IncomingCall>> live = DatabaseManager.getDatabase().incomingCallDao().getAllLive();

        mlive.addSource(live, mlive::postValue);

        return mlive;
    }

    public static LiveData<Integer> getCountSince(long time) {
        return DatabaseManager.getDatabase().incomingCallDao().getCountSince(time);
    }

    public long getCallDate() {
        return callDate;
    }

    public void setCallDate(long callDate) {
        this.callDate = callDate;
    }

    public int getCallType() {
        return callType;
    }

    public void setCallType(int callType) {
        this.callType = callType;
    }

}
