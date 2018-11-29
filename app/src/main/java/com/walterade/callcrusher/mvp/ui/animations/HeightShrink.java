package com.walterade.callcrusher.mvp.ui.animations;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by Walter on 12/28/17.
 */

public class HeightShrink extends AnimatoryManager.Animatory {

    public HeightShrink(View applyTo) {
        super(newInstance(applyTo));
    }

    public static Animator newInstance(View applyTo) {
        int height = AnimatoryManager.checkViewHeight(applyTo, 0);

        ValueAnimator a = ValueAnimator.ofInt(height, 0);
        a.addUpdateListener(valueAnimator -> {
            int val = (Integer) valueAnimator.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = applyTo.getLayoutParams();
            layoutParams.height = val;
            applyTo.setLayoutParams(layoutParams);
        });

        a.setInterpolator(new AccelerateDecelerateInterpolator());

        return a;
    }
}

