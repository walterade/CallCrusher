package com.walterade.callcrusher.mvp.ui.animations;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by Walter on 12/28/17.
 */

public class HeightGrow extends AnimatoryManager.Animatory {

    public HeightGrow(View applyTo) {
        super(newInstance(applyTo));
    }

    public static Animator newInstance(View applyTo) {
        applyTo.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        ValueAnimator a = ValueAnimator.ofInt(applyTo.getHeight(), applyTo.getMeasuredHeight());
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

