package com.walterade.callcrusher.application;

import android.app.Application;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.content.ContextCompat;

import com.walterade.callcrusher.BuildConfig;
import com.walterade.callcrusher.activity.BaseActivity;
import com.walterade.callcrusher.event.ApplicationTerminatedEvent;
import com.walterade.callcrusher.injection.component.ApplicationComponent;
import com.walterade.callcrusher.injection.component.DaggerApplicationComponent;
import com.walterade.callcrusher.injection.module.AndroidModule;
import com.walterade.callcrusher.manager.DatabaseManager;
import com.walterade.callcrusher.manager.SettingsManager;
import com.walterade.callcrusher.service.IncomingCallService;
import com.walterade.callcrusher.utils.ExceptionHandler;
import com.walterade.callcrusher.utils.RxBus;

import java.util.ArrayList;


public class CallCrusherApplication extends Application {

    private static CallCrusherApplication instance;
    private static ApplicationComponent component;
    private ArrayList<String> activityStack = new ArrayList<>();

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        if (BuildConfig.DEBUG) {

            //Timber.plant(new Timber.DebugTree());

            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            /*if (!LeakCanary.isInAnalyzerProcess(this)) {
                // Normal app init code...
                LeakCanary.install(this);
            }*/
            ExceptionHandler.install(this, "walter.ademiluyi@gmail.com", "Call Crusher Exception");
        }

        initComponent();
        initDatabase();
        startCallCrusherService();
    }

    private void initComponent() {
        component = DaggerApplicationComponent.builder()
                .androidModule(new AndroidModule(this))
                .build();
    }

    private void initDatabase() {
        DatabaseManager.getDatabase(this);
    }

    private void disposeDatabase() {
        DatabaseManager.dispose();
    }

    public void startCallCrusherService() {
        Intent i = new Intent(this, IncomingCallService.class);
        stopService(i);
        if (new SettingsManager(this).getCallsCrushState() != SettingsManager.CRUSH_STATE_ALLOW)
            ContextCompat.startForegroundService(this, i);
        else startService(i);
    }

    public static ApplicationComponent getComponent() {
        return component;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        instance = null;
        component = null;
        activityStack.clear();
        disposeDatabase();
        RxBus.getInstance().post(new ApplicationTerminatedEvent());
    }

    public static CallCrusherApplication getInstance() {
        return instance;
    }

    public void activityStarted(BaseActivity activity) {
        activityStack.add(activity.getComponentName().getClassName());
    }

    public void activityStopped(BaseActivity activity) {
        activityStack.remove(activity.getComponentName().getClassName());
    }

    public boolean isActivityStarted(Class activityClassName) {
        return activityStack.contains(activityClassName.getName());
    }

}
