package com.walterade.callcrusher.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.walterade.callcrusher.mvp.data.model.BlockedCall;
import com.walterade.callcrusher.mvp.data.model.IncomingCall;

/**
 * Created by Walter on 1/25/18.
 */

@Database(entities = {BlockedCall.class, IncomingCall.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "calls";

    public abstract BlockedCall.BlockedCallDao blockedCallDao();
    public abstract IncomingCall.IncomingCallDao incomingCallDao();

}