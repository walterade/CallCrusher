package com.walterade.callcrusher.injection.component;

import com.walterade.callcrusher.activity.MainActivity;
import com.walterade.callcrusher.injection.module.FragmentModule;
import com.walterade.callcrusher.injection.module.MainActivityModule;

import dagger.Subcomponent;

/**
 * Created by Walter on 10/10/17.
 */

@PerActivity
@Subcomponent(modules={MainActivityModule.class})
public interface MainActivitySubComponent {
    FragmentSubComponent plus(FragmentModule module);
    void inject(MainActivity mainActivity);
}
