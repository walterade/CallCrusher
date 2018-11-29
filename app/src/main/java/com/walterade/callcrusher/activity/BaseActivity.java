package com.walterade.callcrusher.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.walterade.callcrusher.application.CallCrusherApplication;
import com.walterade.callcrusher.injection.component.ApplicationComponent;
import com.walterade.callcrusher.injection.component.MainActivitySubComponent;
import com.walterade.callcrusher.injection.module.MainActivityModule;
import com.walterade.callcrusher.mvp.ui.base.BaseFragment;
import com.walterade.callcrusher.utils.FragmentUtils;

import java.util.List;

public abstract class BaseActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {

    MainActivitySubComponent component;
    protected Bundle savedInstanceState;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        inject(CallCrusherApplication.getComponent());
        component = CallCrusherApplication.getComponent().plus(new MainActivityModule(this));
        this.savedInstanceState = savedInstanceState;
        super.onCreate(savedInstanceState);
    }

    public MainActivitySubComponent getComponent() {
        return component;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void inject(ApplicationComponent component) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        CallCrusherApplication.getInstance().activityStarted(this);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        CallCrusherApplication.getInstance().activityStopped(this);
    }

    @Override
    public void onBackPressed() {

        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        boolean backPressed = false;

        if (fragments.size() > 0) {
            Fragment f = fragments.get(fragments.size() - 1);
            if (f instanceof BaseFragment)
                if (((BaseFragment)f).onBackPressed()) backPressed = true;
        }

        if (!backPressed && !goBack()) super.onBackPressed();
    }

    public boolean goBack() {
        if (!FragmentUtils.goBack(this)) {
            supportFinishAfterTransition();
        }
        return true;
    }

    @Override
    public void onBackStackChanged() {
        Log.d("hmm", "hmm");
    }
}
