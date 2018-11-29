package com.walterade.callcrusher.mvp.ui.animations;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.walterade.callcrusher.utils.AndroidUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Walter on 12/23/17.
 */

public class AnimatoryManager {
    public static final String ANIMATORY_ROOT = "ANIMATORY_ROOT";
    public static boolean debug = false;
    public final static int DEBUG_DURATION = 10000;
    public final static int DURATION = 200;

    static HashMap<String, Animator> animations = new HashMap<>();
    static HashMap<String, Animator> nextAnimations = new HashMap<>();
    static HashMap<View, BouncyButtonTouchListener> bouncyButtons = new HashMap<>();
    static LinkedHashMap<View, View> rootViews = new LinkedHashMap<>();


    public interface OnAnimationStartListener {
        void onAnimationStart();
    }
    public interface OnAnimationEndListener {
        void onAnimationEnd();
    }


    public static abstract class Animatory {
        protected final Animator animator;

        public Animatory(Animator animator) {
            this.animator = animator;
        }
        public Animatory combine(Animatory a) {
            AnimatorSet set = new AnimatorSet();
            set.playTogether(animator, a.animator);
            return Animatory.newInstance(set);
        }
        public Animatory append(Animatory a) {
            AnimatorSet set = new AnimatorSet();
            set.playSequentially(animator, a.animator);
            return Animatory.newInstance(set);
        }

        public static Animatory newInstance(Context c, int a) {
            return new Animatory(AnimatorInflater.loadAnimator(c, a)) {};
        }
        public static Animatory newInstance(Animator a) {
            return new Animatory(a) {};
        }
        public static Animatory newInstance(Animator a, Object applyTo) {
            Animatory animatory = newInstance(a);
            animatory.animator.setTarget(applyTo);
            return animatory;
        }

    }


    public static class BouncyButtonTouchListener implements View.OnTouchListener {
        ArrayList<View> views = new ArrayList<>();
        final Object target;

        public BouncyButtonTouchListener(Object target, View... applyTo) {
            this.target = target;
            Collections.addAll(views, applyTo);
            //for (View v : views)
            //    v.setClickable(true);
        }

        void cancel() {
            for (View v : views)
                AnimatoryManager.cancel(this.target, v);
        }

        void dispose() {
            cancel();
            views.clear();
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (!view.isClickable()) return false;

            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    for (View v : views)
                        if (!AnimatoryManager.isAnimating(target, v))
                            AnimatoryManager.buttonDown(target, v, null);

                    view.setPressed(true);
                    return true;

                case MotionEvent.ACTION_UP:
                    view.setClickable(false);
                    for (View v : views)
                        AnimatoryManager.buttonUp(target, v, () -> {
                            if (views.indexOf(v) == 0) {
                                view.setClickable(true);
                                view.performClick();
                                view.setPressed(false);
                            }
                        });
                    return true;

                case MotionEvent.ACTION_CANCEL:
                    for (View v : views) {
                        AnimatoryManager.cancel(target, v);
                        AnimatoryManager.buttonUp(target, v, () -> view.setPressed(false));
                    }

                default:
                    return false;
            }
        }
    }


    public static void cancel(Object target) {
        String[] keys = new String[animations.size()];
        animations.keySet().toArray(keys);

        for (String key : keys)
            if (key.contains(target.hashCode() + ":"))
                animations.remove(key).cancel();

        removeBouncyButtonsByTarget(target);
    }

    public static void cancelByParent(View parent) {
        /*String[] keys = new String[animations.size()];
        animations.keySet().toArray(keys);

        for (String key : keys)
            animations.get(key)
            if (key.contains(target.hashCode() + ":"))
                animations.remove(key).cancel();
                */

        View[] views = new View[10];
        BouncyButtonTouchListener[] listeners = bouncyButtons.values().toArray(new BouncyButtonTouchListener[bouncyButtons.size()]);
        for (int i = 0; i < listeners.length; i++) {
            BouncyButtonTouchListener listener = listeners[i];
            int length = listener.views.size();
            if (length > views.length) views = new View[length + 10];
            listener.views.toArray(views);
            for (int t = 0; t < length; t++) {
                View v = views[t];
                if (isDescendantView(parent, v))
                    cancelBouncyButton(v);
            }
        }
    }

    public static boolean isDescendantView(View parent, View child) {
        while (child.getParent() instanceof View && (child = (View)child.getParent()) != null && child != parent);
        return child == parent;
    }

    public static void cancel(Object target, View v) {
        Animator a = animations.remove(target.hashCode() + ":" + v.hashCode());
        if (a != null) a.cancel();
    }

    public static boolean isAnimating(Object target, View v) {
        return animations.containsKey(target.hashCode() + ":" + v.hashCode());
    }

    public static boolean isAnimating(Object target) {
            for (String key : animations.keySet())
            if (key.contains(target.hashCode() + ":")) return true;

        return false;
    }

    public static void animating(Object target, View v, Animator animator) {
        animations.put(target.hashCode() + ":" + v.hashCode(), animator);
    }

    public static void notAnimating(Object target, View v) {
        animations.remove(target.hashCode() + ":" + v.hashCode());
        Animator a = getNextAnimation(target, v);
        if (a != null) a.start();
    }

    public static void setDuration(Animator a, float scale) {
        a.setDuration((long) (DURATION * scale));
    }
    public static void setDuration(Animation a, float scale) {
        a.setDuration((long) (DURATION * scale));
    }

    private static void nextAnimation(Object target, View view, Animator a, OnAnimationStartListener start) {
        if (start != null)
            a.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    start.onAnimationStart();
                }

                @Override
                public void onAnimationEnd(Animator animator) {

                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
        a.setDuration(debug ? DEBUG_DURATION : a.getDuration() <= 0 ? DURATION : a.getDuration());
        nextAnimations.put(target.hashCode() + ":" + view.hashCode(), a);
    }
    private static Animator getNextAnimation(Object target, View view) {
        return nextAnimations.remove(target.hashCode() + ":" + view.hashCode());
    }
    public static boolean checkAnimating(Object target, View v) {
        if (isAnimating(target, v)) return true;
        animating(target, v, null);
        return false;
    }

    public static int measureViewHeight(View v) {
        v.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        return v.getMeasuredHeight();
    }

    public static int checkViewHeight(View v, int height) {
        if (height == 0) {
            height = v.getHeight();
            if (height == 0) {
                height = v.getMeasuredHeight();
                if (height == 0) {
                    v.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    height = v.getMeasuredHeight();
                }
            }
        }
        return height;
    }

    public static int checkViewWidth(View v, int width) {
        if (width == 0) {
            width = v.getHeight();
            if (width == 0) {
                width = v.getMeasuredWidth();
                if (width == 0) {
                    v.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    width = v.getMeasuredWidth();
                }
            }
        }
        return width;
    }


    public static void applyBouncyButton(Object target, View toucher, View animated) {
        BouncyButtonTouchListener listener = new BouncyButtonTouchListener(target, animated);
        toucher.setOnTouchListener(listener);
        bouncyButtons.put(toucher, listener);
    }
    public static void applyBouncyButton(View toucher) {
        Object rootTarget = findRootView(toucher);
        applyBouncyButton(rootTarget, toucher);
    }
    public static void applyBouncyButton(Object target, View toucher) {
        BouncyButtonTouchListener listener;
        if ((listener = bouncyButtons.get(toucher)) != null) {
            if (!listener.views.contains(toucher)) listener.views.add(toucher);
        } else {
            listener = new BouncyButtonTouchListener(target, toucher);
            toucher.setOnTouchListener(listener);
            bouncyButtons.put(toucher, listener);
        }
    }
    public static void addViewToBouncyButton(Object target, View toucher, View... applyTo) {
        BouncyButtonTouchListener listener;
        if ((listener = bouncyButtons.get(toucher)) != null) {
            listener.views.addAll(Arrays.asList(applyTo));
        }
        else {
            applyBouncyButton(target, toucher);
            addViewToBouncyButton(target, toucher, applyTo);
        }
    }
    public static void cancelBouncyButton(View toucher) {
        BouncyButtonTouchListener listener;
        if ((listener = bouncyButtons.get(toucher)) != null)
            listener.cancel();
    }
    public static void removeBouncyButton(View toucher) {
        BouncyButtonTouchListener listener;
        if ((listener = bouncyButtons.remove(toucher)) != null) {
            toucher.setOnTouchListener(null);
            listener.dispose();
        }
    }
    public static void removeBouncyButtonsByTarget(Object target) {
        Map.Entry<View, BouncyButtonTouchListener>[] set = new Map.Entry[bouncyButtons.size()];
        bouncyButtons.entrySet().toArray(set);

        for (Map.Entry<View, BouncyButtonTouchListener> e : set)
            if (e.getValue().target == target)
                removeBouncyButton(e.getKey());
    }

    public static void setAnimatoryRoot(View root) {
        root.setTag(ANIMATORY_ROOT);
    }

    static View findRootView(View v) {
        View root = v;
        View rootOf = v;

        do {
            root = rootViews.get(v);
            if (root != null) return root;
            root = v;
        } while ((v.getParent() instanceof View) && ((v = (View) v.getParent()) != null) && !ANIMATORY_ROOT.equals(v.getTag()));

        v = rootOf;
        do {
            if (rootViews.containsKey(v)) break;
            rootViews.put(v, root);
        } while ((v.getParent() instanceof View) && ((v = (View) v.getParent()) != null) && !ANIMATORY_ROOT.equals(v.getTag()));

        while (rootViews.size() > 5)
            rootViews.remove(rootViews.keySet().iterator().next());

        return root;
    }

    public static void animate(Context c, Object target, View applyTo, int animId, OnAnimationEndListener listener) {
        set(target, applyTo, AnimationWrapper.newInstance(applyTo, AnimationUtils.loadAnimation(c, animId)), listener);
    }

    public static boolean showMenu(Object target, View menu, Drawable menuBackground, int menuHeight, Animator... animators) {
        return showMenu(target, menu, menuBackground, menuHeight, null, animators);
    }

    public static boolean showMenu(Object target, View menu, Drawable menuBackground, int menuHeight, OnAnimationEndListener listener, Animator... animators) {
        if (checkAnimating(target, menu)) return false;

        menuHeight = checkViewHeight(menu, menuHeight);
        Drawable background = menuBackground.mutate();
        ValueAnimator fadeIn = ValueAnimator.ofInt(0, 255);
        ValueAnimator menuShow = ValueAnimator.ofInt(-menuHeight, 0);
        AnimatorSet set = crateAnimatorSet(target, menu, listener);
        ArrayList<Animator> anims = new ArrayList<>();

        fadeIn.addUpdateListener(valueAnimator -> {
            int v = (int) valueAnimator.getAnimatedValue();
            background.setAlpha(v);
            menu.setAlpha(v/255f);
        });

        menuShow.addUpdateListener(valueAnimator -> {
            int v = (int) valueAnimator.getAnimatedValue();
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) menu.getLayoutParams();
            layoutParams.topMargin = v;
            menu.setLayoutParams(layoutParams);
        });

        anims.add(fadeIn);
        anims.add(menuShow);
        Collections.addAll(anims, animators);
        set.playTogether(anims);
        set.start();

        animating(target, menu, set);
        return true;
    }

    public static boolean hideMenu(Object target, View menu, Drawable menuBackground, int menuHeight, Animator... animators) {
        return hideMenu(target, menu, menuBackground, menuHeight, null, animators);
    }

    public static boolean hideMenu(Object target, View menu, Drawable menuBackground, int menuHeight, OnAnimationEndListener listener, Animator... animators) {
        if (checkAnimating(target, menu)) return false;

        menuHeight = checkViewHeight(menu, menuHeight);
        Drawable background = menuBackground.mutate();
        ValueAnimator fadeOut = ValueAnimator.ofInt(255, 0);
        ValueAnimator menuHide = ValueAnimator.ofInt(0, -menuHeight);
        AnimatorSet set = crateAnimatorSet(target, menu, listener);
        ArrayList<Animator> anims = new ArrayList<>();

        fadeOut.addUpdateListener(valueAnimator -> {
            int v = (int) valueAnimator.getAnimatedValue();
            background.setAlpha(v);
            menu.setAlpha(v/255f);
        });

        menuHide.addUpdateListener(valueAnimator -> {
            int v = (int) valueAnimator.getAnimatedValue();
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) menu.getLayoutParams();
            layoutParams.topMargin = v;
            menu.setLayoutParams(layoutParams);
        });

        anims.add(fadeOut);
        anims.add(menuHide);
        Collections.addAll(anims, animators);
        set.playTogether(anims);
        set.start();

        animating(target, menu, set);
        return true;
    }

    public static boolean run(Object target, View applyTo, Class<? extends Animatory> animatory, OnAnimationEndListener listener) {
        try {
            Animator a = animatory.getConstructor(View.class).newInstance(applyTo).animator;

            if (checkAnimating(target, applyTo)) {
                return false;
            }

            apply(target, applyTo, a, listener);
            a.start();
            animating(target, applyTo, a);
            return true;

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean add(Object target, View applyTo, Animatory animatory, OnAnimationEndListener listener) {
        if (checkAnimating(target, applyTo)) {
            nextAnimation(target, applyTo, animatory.animator, null);
            return false;
        }

        apply(target, applyTo, animatory.animator, listener);
        AndroidUtils.runOnUIThread(animatory.animator::start);
        animating(target, applyTo, animatory.animator);
        return true;
    }

    public static boolean add(Object target, View applyTo, Class<? extends Animatory> animatory, OnAnimationEndListener listener) {
        try {
            Animator a = animatory.getConstructor(View.class).newInstance(applyTo).animator;

            if (checkAnimating(target, applyTo)) {
                nextAnimation(target, applyTo, a, null);
                return false;
            }

            apply(target, applyTo, a, listener);
            AndroidUtils.runOnUIThread(a::start);
            animating(target, applyTo, a);
            return true;

            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

        return false;
    }

    public static void set(Object target, View applyTo, Animator a, OnAnimationEndListener listener) {
        if (checkAnimating(target, applyTo))
            cancel(target, applyTo);

        apply(target, applyTo, a, listener);
        a.start();
        animating(target, applyTo, a);
    }

    public static void set(Object target, View applyTo, Class<? extends Animatory> animatory, OnAnimationEndListener listener) {
        try {
            Animator a = animatory.getConstructor(View.class).newInstance(applyTo).animator;
            set(target, applyTo, a, listener);

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void after(Object target, View applyTo, Animator animator, OnAnimationStartListener start, OnAnimationEndListener end) {
        if (isAnimating(target, applyTo)) {
            nextAnimation(target, applyTo, animator, start);
        }
        else {
            if (start != null) start.onAnimationStart();
            set(target, applyTo, animator, end);
        }
    }

    public static boolean after(Object target, View applyTo, OnAnimationStartListener start) {
        Animator a = ValueAnimator.ofInt(0, 0);

        if (isAnimating(target, applyTo)) {
            nextAnimation(target, applyTo, a, start);
            return false;
        }
        else set(target, applyTo, a, null);
        return true;
    }


    public static boolean buttonDown(Object target, View button, OnAnimationEndListener listener) {
        if (checkAnimating(target, button)) return false;

        PropertyValuesHolder x = PropertyValuesHolder.ofFloat("scaleX", .95f);
        PropertyValuesHolder y = PropertyValuesHolder.ofFloat("scaleY", .95f);
        Animator a = ObjectAnimator.ofPropertyValuesHolder(button, x, y).setDuration(debug ? DEBUG_DURATION : DURATION/4);
        apply(target, button, a, listener);
        a.start();

        animating(target, button, a);
        return true;
    }

    public static boolean buttonUp(Object target, View button, OnAnimationEndListener listener) {

        PropertyValuesHolder x = PropertyValuesHolder.ofFloat("scaleX", 1f);
        PropertyValuesHolder y = PropertyValuesHolder.ofFloat("scaleY", 1f);
        Animator a = ObjectAnimator.ofPropertyValuesHolder(button, x, y);
        apply(target, button, a, listener);
        a.setDuration(debug ? DEBUG_DURATION : DURATION/2);

        if (!checkAnimating(target, button)) {
            a.start();
            animating(target, button, a);
            return true;
        }
        else {
            nextAnimation(target, button, a, null);
            return false;
        }

    }

    private static AnimatorSet crateAnimatorSet(Object target, View v, OnAnimationEndListener listener) {
        AnimatorSet set = new AnimatorSet();
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {}

            @Override
            public void onAnimationEnd(Animator animator) {
                notAnimating(target, v);
                if (listener != null) listener.onAnimationEnd();
            }

            @Override
            public void onAnimationCancel(Animator animator) {notAnimating(target, v);}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });
        set.setDuration(debug ? DEBUG_DURATION : DURATION);
        return set;
    }

    public static Animator create(ValueAnimator animator, ValueAnimator.AnimatorUpdateListener onUpdate) {
        return create(animator, onUpdate, null);
    }

    public static Animator create(ValueAnimator animator, ValueAnimator.AnimatorUpdateListener onUpdate, OnAnimationEndListener onEnd) {
        animator.addUpdateListener(onUpdate);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {}

            @Override
            public void onAnimationEnd(Animator animator) {
                if (onEnd != null) onEnd.onAnimationEnd();
            }

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });
        return animator;
    }

    private static void apply(Object target, View view, Animator a, OnAnimationEndListener listener) {
        a.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                notAnimating(target, view);
                if (listener != null) listener.onAnimationEnd();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        a.setDuration(debug ? DEBUG_DURATION : a.getDuration() <= 0 ? DURATION : a.getDuration());
    }
}
