package com.walterade.callcrusher.injection.component;

import com.walterade.callcrusher.activity.MainActivity;
import com.walterade.callcrusher.database.AppDatabase;
import com.walterade.callcrusher.injection.module.AndroidModule;
import com.walterade.callcrusher.injection.module.ApplicationModule;
import com.walterade.callcrusher.injection.module.DataModule;
import com.walterade.callcrusher.injection.module.MainActivityModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Walter on 10/12/17.
 */

@Singleton
@Component(modules = {
        AndroidModule.class,
        ApplicationModule.class,
        DataModule.class
})

public interface ApplicationComponent {
    MainActivitySubComponent plus(MainActivityModule module);
    AppDatabase getDatabase();
    void inject(MainActivity mainActivity);
}