package com.walterade.callcrusher.injection.module;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.walterade.callcrusher.activity.MainView;
import com.walterade.callcrusher.injection.component.PerActivity;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Walter on 10/10/17.
 */

@Module
public class MainActivityModule {
    private final FragmentActivity activity;

    public MainActivityModule(FragmentActivity activity) {
        this.activity = activity;
    }

    @Provides
    public FragmentActivity provideActivity() {
        return activity;
    }

    @Provides
    @PerActivity
    @Named("activity")
    Context provideContext() {
        return activity;
    }

    @Provides
    @PerActivity
    MainView provideMainView() {return (MainView) activity;}

    @Provides
    @PerActivity
    FragmentManager provideFragmentManager() {
        return activity.getSupportFragmentManager();
    }
}