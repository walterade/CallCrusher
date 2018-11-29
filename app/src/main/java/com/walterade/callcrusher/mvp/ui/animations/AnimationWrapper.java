package com.walterade.callcrusher.mvp.ui.animations;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.Animation;

/**
 * Created by Walter on 12/28/17.
 */

public class AnimationWrapper extends AnimatoryManager.Animatory {

    public AnimationWrapper(View applyTo, Animation animation) {
        super(newInstance(applyTo, animation));
    }

    public static Animator newInstance(View applyTo, Animation animation) {
        ValueAnimator a = ValueAnimator.ofFloat(0, 1);
        a.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                animation.setFillAfter(true);
                animation.setDuration(animator.getDuration());
                applyTo.setAnimation(animation);
                animation.start();
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                applyTo.clearAnimation();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                animation.cancel();
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });

        a.setDuration(animation.getDuration());

        return a;
    }
}

