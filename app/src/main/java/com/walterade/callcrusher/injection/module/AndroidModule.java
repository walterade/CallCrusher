package com.walterade.callcrusher.injection.module;

import android.content.Context;
import android.content.res.Resources;

import com.walterade.callcrusher.application.CallCrusherApplication;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Walter on 10/12/17.
 */

@Module
public class AndroidModule {
    private CallCrusherApplication application;

    public AndroidModule(CallCrusherApplication application) {
        this.application = application;
    }

    @Provides
    @Singleton
    public Context provideContext() {
        return application.getApplicationContext();
    }

    @Provides
    @Singleton
    public Resources provideResources() {
        return application.getResources();
    }

}