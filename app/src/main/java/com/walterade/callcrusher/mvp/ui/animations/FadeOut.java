package com.walterade.callcrusher.mvp.ui.animations;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;

/**
 * Created by Walter on 12/28/17.
 */

public class FadeOut extends AnimatoryManager.Animatory {

    public FadeOut(View applyTo) {
        super(newInstance(applyTo));
    }

    public static Animator newInstance(View applyTo) {
        float alpha = applyTo.getAlpha();
        Animator a = ObjectAnimator.ofPropertyValuesHolder(applyTo,
                PropertyValuesHolder.ofFloat("alpha",  alpha, 0)
        );
        return a;
    }
}

