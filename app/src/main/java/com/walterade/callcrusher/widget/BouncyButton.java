package com.walterade.callcrusher.widget;

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Walter on 12/27/17.
 */

public class BouncyButton extends android.support.v7.widget.AppCompatButton {
    private boolean isAnimating;

    public BouncyButton(Context context) {
        super(context);
    }

    public BouncyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BouncyButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAnimationStart() {
        super.onAnimationStart();
        isAnimating = true;
    }

    @Override
    protected void onAnimationEnd() {
        super.onAnimationEnd();
        isAnimating = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!isAnimating)
                    animate().scaleX(.75F).scaleY(.75F).start();
                return true;

            case MotionEvent.ACTION_UP:
                if (!isAnimating)
                    animate().scaleX(1F).scaleY(1F).setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            performClick();
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    }).start();
                return false;

            default:
                return super.onTouchEvent(event);
        }
    }

}
