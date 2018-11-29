package com.walterade.callcrusher.manager;

import android.arch.persistence.room.Room;
import android.content.Context;

import com.walterade.callcrusher.R;
import com.walterade.callcrusher.database.AppDatabase;
import com.walterade.callcrusher.mvp.data.model.BlockedCall;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;


public class DatabaseManager {

    private static AppDatabase db;

    public static AppDatabase getDatabase() {
        return db;
    }

    public static AppDatabase getDatabase(Context c) {
        if (db == null) {
            if (c.getResources().getBoolean(R.bool.debug))
                db = Room.inMemoryDatabaseBuilder(c.getApplicationContext(),
                        AppDatabase.class).
                        fallbackToDestructiveMigration().
                        build();
            else
                db = Room.databaseBuilder(c.getApplicationContext(),
                        AppDatabase.class, AppDatabase.DATABASE_NAME).
                        fallbackToDestructiveMigration().
                        build();

            setupBlockedCalls();

        }
        return db;
    }

    static void setupBlockedCalls() {
        Single.create((e) -> {
            BlockedCall.block("253-950-1212");
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    public static void dispose() {
        if (db != null) {
            db.close();
            db = null;
        }
    }

}
