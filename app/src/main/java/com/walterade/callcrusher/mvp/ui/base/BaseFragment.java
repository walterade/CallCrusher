package com.walterade.callcrusher.mvp.ui.base;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.TimeInterpolator;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.walterade.callcrusher.R;
import com.walterade.callcrusher.activity.BaseActivity;
import com.walterade.callcrusher.injection.component.FragmentSubComponent;
import com.walterade.callcrusher.injection.module.FragmentModule;
import com.walterade.callcrusher.mvp.ui.animations.AnimatoryManager;
import com.walterade.callcrusher.utils.FragmentUtils;


/**
 * Created by Walter on 10/11/17.
 */

public abstract class BaseFragment extends Fragment implements FragmentUtils.FragmentAnimationController {

    SparseArray<AnimIds> animIds = new SparseArray<>();
    boolean onAfterGoneCalled = false;
    protected Bundle savedInstanceState;
    private boolean beforeVisibleCalled;
    private boolean onHiddenCalled;


    private class AnimIds {
        int enterAnimId;
        int exitAnimId;
        Animation enterAnimation;
        Animation exitAnimation;
        Animator enterAnimator;
        Animator exitAnimator;
    }

    private class AfterGoneAnimationListener implements Animation.AnimationListener {
        private final boolean runAfterGone;
        private final boolean enter;

        public AfterGoneAnimationListener(boolean enter, boolean isRemoving) {
            this.enter = enter;
            this.runAfterGone = !enter && isRemoving;
            onTransitionStart(enter);
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            onTransitionEnd(enter);
            if (runAfterGone) callOnAfterGone();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    private class AfterGoneAnimatorListener implements Animator.AnimatorListener {
        private final boolean runAfterGone;
        private final boolean enter;

        public AfterGoneAnimatorListener(boolean enter, boolean isRemoving) {
            this.enter = enter;
            this.runAfterGone = !enter && isRemoving;
        }

        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            onTransitionEnd(enter);
            if (runAfterGone) callOnAfterGone();
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        BaseActivity baseActivity = (BaseActivity) getActivity();
        inject(baseActivity.getComponent().plus(new FragmentModule(this)));
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setPopAnimation(int enterAnimId, int exitAnimId) {
        AnimIds ids = getAnimIds();
        if (ids == null) ids = new AnimIds();
        if (enterAnimId >= 0) ids.enterAnimId = enterAnimId;
        if (exitAnimId >= 0) ids.exitAnimId = exitAnimId;
        putAnimIds(ids);
    }

    @Override
    public void setAnimator(Animator enterAnimator, Animator exitAnimator, Animator popEnterAnimator, Animator popExitAnimator) {
        AnimIds ids = getAnimIds();
        if (ids == null) ids = new AnimIds();
        if (enterAnimator != null) ids.enterAnimator = enterAnimator;
        if (exitAnimator != null) ids.exitAnimator = exitAnimator;
        if (popEnterAnimator != null) ids.enterAnimator = popEnterAnimator;
        if (popExitAnimator != null) ids.exitAnimator = popExitAnimator;
        putAnimIds(ids);
    }

    @Override
    public void setAnimation(Animation enterAnimation, Animation exitAnimation, Animation popEnterAnimation, Animation popExitAnimation) {
        AnimIds ids = getAnimIds();
        if (ids == null) ids = new AnimIds();
        if (enterAnimation != null) ids.enterAnimation = enterAnimation;
        if (exitAnimation != null) ids.exitAnimation = exitAnimation;
        if (popEnterAnimation != null) ids.enterAnimation = popEnterAnimation;
        if (popExitAnimation != null) ids.exitAnimation = popExitAnimation;
        putAnimIds(ids);
    }

    AnimIds getAnimIds() {
        int c = (getFragmentManager() != null) ? getFragmentManager().getBackStackEntryCount() : 0;
        c++;
        c = 0;
        AnimIds ids = animIds.get(c);
        while(ids == null && --c > 0)
            ids = animIds.get(c);
        if (ids == null) ids = new AnimIds();
        return ids;
    }
    void putAnimIds(AnimIds ids) {
        int c = (getFragmentManager() != null) ? getFragmentManager().getBackStackEntryCount() : 0;
        c++;
        c = 0;
        animIds.put(c, ids);
    }

    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {

        Animator a;
        AnimIds ids = getAnimIds();
        int enterAnimId = ids.enterAnimId;
        int exitAnimId = ids.exitAnimId;

        if (nextAnim == -1) {
            if (!enter)
                nextAnim = exitAnimId;
            else nextAnim = enterAnimId;

            if (nextAnim == 0) {
                a = new Animator() {
                    @Override
                    public long getStartDelay() {
                        return 0;
                    }

                    @Override
                    public void setStartDelay(long l) {}

                    @Override
                    public Animator setDuration(long l) {
                        return null;
                    }

                    @Override
                    public long getDuration() {
                        return 0;
                    }

                    @Override
                    public void setInterpolator(TimeInterpolator timeInterpolator) {}

                    @Override
                    public boolean isRunning() {
                        return false;
                    }
                };

                a.addListener(new AfterGoneAnimatorListener(enter, isRemoving()));
                return a;
            }
        }

        if (nextAnim != 0) {
            a = getAnimator(nextAnim, enter);
            if (getResources().getBoolean(R.bool.debug_animations))
                a.setDuration(AnimatoryManager.DEBUG_DURATION);

            a.addListener(new AfterGoneAnimatorListener(enter, isRemoving()));
            return a;
        }
        else {
            onTransitionStart(enter);
            onTransitionEnd(enter);
            if (!enter && isRemoving())
                callOnAfterGone();
        }

        return super.onCreateAnimator(transit, enter, nextAnim);
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {

        Animation a;
        AnimIds ids = getAnimIds();
        int enterAnimId = ids.enterAnimId;
        int exitAnimId = ids.exitAnimId;

        if (nextAnim == -1) {
            if (!enter)
                nextAnim = exitAnimId;
            else nextAnim = enterAnimId;

            if (nextAnim == 0) {

                a = new Animation() {};
                a.setAnimationListener(new AfterGoneAnimationListener(enter, isRemoving()));
                return a;
            }
        }

        if (nextAnim != 0) {
            try {

                if (nextAnim > 0) a = getAnimation(nextAnim, enter);
                else a = new Animation() {};

                if (getResources().getBoolean(R.bool.debug_animations))
                    a.setDuration(AnimatoryManager.DEBUG_DURATION);

                a.setAnimationListener(new AfterGoneAnimationListener(enter, isRemoving()));
                return a;
            } catch (Exception e) {

            }
        }
        else {
            onTransitionStart(enter);
            onTransitionEnd(enter);
            if (!enter && isRemoving())
                callOnAfterGone();
        }

        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    Animation getAnimation(int animId, boolean enter) {
        if (animId > 0)
            return AnimationUtils.loadAnimation(getContext(), animId);
        else return enter ? getAnimIds().enterAnimation : getAnimIds().exitAnimation;
    }

    Animator getAnimator(int animatorId, boolean enter) {
        if (animatorId > 0)
            return AnimatorInflater.loadAnimator(getContext(), animatorId);
        else return enter ? getAnimIds().enterAnimator : getAnimIds().exitAnimator;
    }

    public boolean onBackPressed() {
        return false;
    }
    protected abstract void inject(FragmentSubComponent component);
    protected void onBeforeVisible() {}
    protected void onHidden() {}
    protected void onTransitionStart(boolean enter) {}
    protected void onTransitionEnd(boolean enter) {}
    protected void onAfterGone() {}

    void callOnAfterGone() {
        if (onAfterGoneCalled) return;
        onAfterGoneCalled = true;
        onAfterGone();
        savedInstanceState = null;
    }

    @Override
    public void onDestroyView() {
        callOnHidden();
        super.onDestroyView();
    }

    @Override
    public void onStart() {
        super.onStart();
        callOnBeforeVisible();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (onHiddenCalled)
            callOnBeforeVisible();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!isRemoving()) callOnHidden();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.savedInstanceState = savedInstanceState;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) callOnBeforeVisible();
        else if (!isRemoving())
            //we dont need onHidden called when Fragment isRemoving because we are calling onHidden in onDestroyView
            callOnHidden();
    }

    void callOnHidden() {
        if (beforeVisibleCalled) {
            AnimatoryManager.cancelByParent(getView());
            onDetachPresenter();
            onHidden();
            beforeVisibleCalled = true;
            onHiddenCalled = true;
        }
    }

    void callOnBeforeVisible() {
        FragmentUtils.fragmentShown(this);
        onAttachPresenter();
        onBeforeVisible();
        beforeVisibleCalled = true;
        onHiddenCalled = false;
    }

    public void onDetachPresenter() {}
    public void onAttachPresenter() {}
}
