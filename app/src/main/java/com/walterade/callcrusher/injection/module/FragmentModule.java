package com.walterade.callcrusher.injection.module;

import com.walterade.callcrusher.mvp.ui.adapters.LandingAdapter;
import com.walterade.callcrusher.mvp.ui.base.BaseFragment;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Walter on 10/10/17.
 */

@Module
public class FragmentModule {
    private final BaseFragment fragment;

    public FragmentModule(BaseFragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    LandingAdapter provideLandingAdapter() {
        return new LandingAdapter(fragment.getChildFragmentManager());
    }
}