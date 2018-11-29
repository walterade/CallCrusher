package com.walterade.callcrusher.injection.module;

import com.walterade.callcrusher.BuildConfig;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Walter on 10/12/17.
 */

@Module
public class ApplicationModule {
    @Provides
    @Singleton
    @Named("isDebug")
    public boolean provideIsDebug() {
        return BuildConfig.DEBUG;
    }
}