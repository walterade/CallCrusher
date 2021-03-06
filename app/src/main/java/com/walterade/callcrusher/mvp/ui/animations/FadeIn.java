package com.walterade.callcrusher.mvp.ui.animations;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;

/**
 * Created by Walter on 12/28/17.
 */

public class FadeIn extends AnimatoryManager.Animatory {

    public FadeIn(View applyTo) {
        super(newInstance(applyTo));
    }

    public static Animator newInstance(View applyTo) {
        Animator a = ObjectAnimator.ofPropertyValuesHolder(applyTo,
                PropertyValuesHolder.ofFloat("alpha", 0, 1)
        );
        return a;
    }
}

