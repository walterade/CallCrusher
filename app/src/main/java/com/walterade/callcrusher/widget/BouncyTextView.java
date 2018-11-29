package com.walterade.callcrusher.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.walterade.callcrusher.mvp.ui.animations.AnimatoryManager;


/**
 * Created by Walter on 12/27/17.
 */

public class BouncyTextView extends android.support.v7.widget.AppCompatTextView {

    public BouncyTextView(Context context) {
        super(context);
    }

    public BouncyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BouncyTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    void init() {
        AnimatoryManager.applyBouncyButton(this, this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        AnimatoryManager.removeBouncyButton(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        init();
    }
    /*
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!Animations.isAnimating(this, this))
                    Animations.buttonDown(this, this, null);
                return true;

            case MotionEvent.ACTION_UP:
                Animations.buttonUp(this, this, this::performClick);
                return false;

            case MotionEvent.ACTION_CANCEL:
                Animations.cancel(this, this);
                Animations.buttonUp(this, this, null);

            default:
                return super.onTouchEvent(event);
        }
    }*/


}
