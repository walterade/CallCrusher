package com.walterade.callcrusher.mvp.ui.animations;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.OvershootInterpolator;

/**
 * Created by Walter on 12/28/17.
 */

public class BounceIn extends AnimatoryManager.Animatory {

    public BounceIn(View applyTo) {
        super(newInstance(applyTo));
    }

    public static Animator newInstance(View applyTo) {
        float height = AnimatoryManager.checkViewHeight(applyTo, 0);

        Animator a = ObjectAnimator.ofPropertyValuesHolder(applyTo,
                PropertyValuesHolder.ofFloat("translationY", height, 0)
        );

        a.setInterpolator(new OvershootInterpolator());

        return a;
    }
}

