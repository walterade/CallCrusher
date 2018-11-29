package com.walterade.callcrusher.injection.component;

import com.walterade.callcrusher.injection.module.FragmentModule;
import com.walterade.callcrusher.mvp.ui.landing.BlockedNumbersFragment;
import com.walterade.callcrusher.mvp.ui.landing.IncomingCallsFragment;
import com.walterade.callcrusher.mvp.ui.landing.LandingFragment;

import dagger.Subcomponent;

/**
 * Created by Walter on 10/10/17.
 */

@PerLandingFragment
@Subcomponent(modules={FragmentModule.class})
public interface FragmentSubComponent {
    void inject(LandingFragment landingFragment);
    void inject(IncomingCallsFragment incomingCallsFragment);
    void inject(BlockedNumbersFragment blockedNumbersFragment);
}
