package com.walterade.callcrusher.injection.module;

import android.content.Context;

import com.walterade.callcrusher.database.AppDatabase;
import com.walterade.callcrusher.manager.DatabaseManager;
import com.walterade.callcrusher.manager.SettingsManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Walter on 10/11/17.
 */

@Module
public class DataModule {

    @Provides
    @Singleton
    AppDatabase provideAppDatabase(Context context) {
        return DatabaseManager.getDatabase(context);
    }

    @Provides
    @Singleton
    SettingsManager provideSettingsManager(Context context) {
        return new SettingsManager(context);
    }
}
