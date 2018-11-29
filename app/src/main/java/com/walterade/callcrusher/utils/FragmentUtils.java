package com.walterade.callcrusher.utils;

import android.animation.Animator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * Created by Walter on 11/16/16.
 */

public class FragmentUtils {
    private static ArrayList<Fragment> child = new ArrayList<>();
    private static int enterAnimId;
    private static int exitAnimId;
    private static int popEnterAnimId;
    private static int popExitAnimId;
    private static Animation enterAnimation;
    private static Animation exitAnimation;
    private static Animation popEnterAnimation;
    private static Animation popExitAnimation;
    private static Animator enterAnimator;
    private static Animator exitAnimator;
    private static Animator popEnterAnimator;
    private static Animator popExitAnimator;
    private static boolean isAnimationSticky;
    private static int nextPopExitAnimId = -1;
    private static Animation nextPopExitAnimation;
    private static Animator nextPopExitAnimator;
    private static ArrayList<String> fragmentShowing = new ArrayList<>();

    public interface FragmentAnimationController {
        void setPopAnimation(int enterAnimId, int exitAnimId);
        void setAnimation(Animation enterAnimation, Animation exitAnimation, Animation popEnterAnimation, Animation popExitAnimation);
        void setAnimator(Animator enterAnimator, Animator exitAnimator, Animator popEnterAnimator, Animator popExitAnimator);
    }

    public static void setNextPopAnimation(int exitAnimId) {
        nextPopExitAnimId = exitAnimId;
    }
    public static void setNextPopAnimation(Animation exitAnimation) {
        nextPopExitAnimation = exitAnimation;
    }
    public static void setNextPopAnimator(Animator exitAnimator) {
        nextPopExitAnimator = exitAnimator;
    }
    public static void setAnimation(int enterAnim, int exitAnim, int popEnterAnim, int popExitAnim) {
        setAnimation(enterAnim, exitAnim, popEnterAnim, popExitAnim, true);
    }
    public static void nextAnimation(int enterAnim, int exitAnim, int popEnterAnim, int popExitAnim) {
        setAnimation(enterAnim, exitAnim, popEnterAnim, popExitAnim, false);
    }
    public static void nextAnimation(Animation enterAnim, Animation exitAnim, Animation popEnterAnim, Animation popExitAnim) {
        enterAnimation = enterAnim;
        exitAnimation = exitAnim;
        popEnterAnimation = popEnterAnim;
        popExitAnimation = popExitAnim;
        setAnimation(enterAnim != null ? -2 : 0, exitAnim != null ? -2 : 0, popEnterAnim != null ? -2 : 0, popExitAnim != null ? -2 : 0);
    }
    public static void nextAnimator(Animator enterAnim, Animator exitAnim, Animator popEnterAnim, Animator popExitAnim) {
        enterAnimator = enterAnim;
        exitAnimator = exitAnim;
        popEnterAnimator = popEnterAnim;
        popExitAnimator = popExitAnim;
        setAnimation(enterAnim != null ? -2 : 0, exitAnim != null ? -2 : 0, popEnterAnim != null ? -2 : 0, popExitAnim != null ? -2 : 0);
    }
    public static void removeAnimation() {
        enterAnimId = 0;
        exitAnimId = 0;
        popEnterAnimId = 0;
        popExitAnimId = 0;
        isAnimationSticky = false;
    }
    public static void setAnimation(int enterAnim, int exitAnim, int popEnterAnim, int popExitAnim, boolean isSticky) {
        enterAnimId = enterAnim;
        exitAnimId = exitAnim;
        popEnterAnimId = popEnterAnim;
        popExitAnimId = popExitAnim;
        isAnimationSticky = isSticky;
    }

    public static void fragmentShown(Fragment f) {
        fragmentShowing.remove(f.getTag());
    }
    public static boolean isFragmentShowing(String tag) {
        return fragmentShowing.contains(tag);
    }

    public static Fragment get(FragmentActivity activity, String tag) {
        Fragment f = activity.getSupportFragmentManager().findFragmentByTag(tag);
        return f;
    }


    public static Fragment show(FragmentActivity activity, Fragment f, int containerId, String tag) {
        /*
        int count = activity.getSupportFragmentManager().getBackStackEntryCount();
        if (count > 0) {
            String tagTop = activity.getSupportFragmentManager().getBackStackEntryAt(count - 1).getName();
            if (tag != null && tagTop != null && tag.equals(tagTop)) {
                return activity.getSupportFragmentManager().(tag);
            }
        }
        */
        clear(activity);

        //clear is not immediate so we cannot remove old one without creating a random popstack crash
        return show(activity, f, containerId, true, tag, false, false);
    }

    public static Fragment push(FragmentActivity activity, Fragment f, int containerId, String tag) {
        return show(activity, f, containerId, true, tag);
    }

    public static void pop(FragmentActivity activity) {
        goBack(activity);
    }

    public static Fragment replace(FragmentActivity activity, Fragment f, int containerId, String tag) {
        return show(activity, f, containerId, false, tag, true, true);
    }

    public static void push(Fragment parent, Fragment f, int containerId, String tag) {
        pushChild(parent, f, containerId, tag);
    }
    public static void pushChild(Fragment parent, Fragment f, int containerId, String tag) {
        showChild(parent, f, containerId, true, tag, false);
    }

    public static void pop(Fragment parent) {
        popChild(parent);
    }
    public static void popChild(Fragment parent) {
        goBack((FragmentActivity) parent.getActivity());
        /*goBackChild(parent);*/
    }

    public static Fragment replaceChild(Fragment parent, Fragment f, int containerId, String tag) {
        return showChild(parent, f, containerId, false, tag, true);
    }


    // Base functions ///////////////////

    public static Fragment show(FragmentActivity activity, Fragment f, int containerId, boolean addToBackstack, String tag) {
        return show(activity, f, containerId, addToBackstack, tag, true, true);
    }

    public static Fragment show(FragmentActivity activity, Fragment f, int containerId, boolean addToBackstack, String tag, boolean replace, boolean removeOldOne) {
        FragmentManager fm = activity.getSupportFragmentManager();
        FragmentTransaction t = fm.beginTransaction();
        Fragment oldFragment = (tag != null) ? fm.findFragmentByTag(tag) : null;

        fragmentShowing.add(tag);

        if (oldFragment != null && removeOldOne) {
            t.remove(oldFragment);
            //f = oldFragment;
        }

        if (f instanceof FragmentAnimationController) {
            ((FragmentAnimationController) f).setAnimation(enterAnimation, exitAnimation, popEnterAnimation, popExitAnimation);
            ((FragmentAnimationController) f).setAnimator(enterAnimator, exitAnimator, popEnterAnimator, popExitAnimator);
            ((FragmentAnimationController) f).setPopAnimation(popEnterAnimId, popExitAnimId);
            t.setCustomAnimations(enterAnimId, exitAnimId, -1, -1);
        }
        else t.setCustomAnimations(enterAnimId, exitAnimId, popEnterAnimId, popExitAnimId);

        if (addToBackstack) {
            int i = fm.getBackStackEntryCount() - 1;

            if (tag != null && i >= 0 && tag.equals(fm.getBackStackEntryAt(i).getName())) {
                try {
                    fm.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                } catch (IllegalStateException e) {}
            }

            t.addToBackStack(tag);
        }

        if (replace) {
            if (!addToBackstack) fm.popBackStack(); //FIXME: pop the stack if there is one because I'm replacing it / not sure if this works in every case
            if (fm.getBackStackEntryCount() > 0) {
                //FragmentManager.BackStackEntry entry = fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1);
                oldFragment = fm.getFragments().get(fm.getFragments().size() - 1);
                if (isInContainer(oldFragment, containerId)) {
                    //applyPopAnimations(oldFragment);
                    applyNextPopExitAnimation(oldFragment);
                    t.hide(oldFragment);
                }
            }
            t.add(containerId, f, tag);
            //FIXME: t.replace(containerId, f, tag);
        }
        else
            t.add(containerId, f, tag);

        if (!isAnimationSticky) removeAnimation();

        commitTransactionAllowingStateLoss(t);
        return oldFragment;
    }

    private static boolean isInContainer(Fragment f, int containerId) {
        View v = f.getView();

        if (v != null) {
            ViewGroup vg = (ViewGroup) v.getParent();
            return containerId == vg.getId();
        }

        return false;
    }

    public static Fragment showChild(Fragment parent, Fragment f, int containerId, boolean addToBackStack, String tag, boolean replace) {
        FragmentManager fm = parent.getChildFragmentManager();
        FragmentTransaction t = fm.beginTransaction();
        Fragment oldFragment = (tag != null) ? fm.findFragmentByTag(tag) : null;

        if (oldFragment != null) {
            t.remove(oldFragment);
        }

        if (f instanceof FragmentAnimationController) {
            ((FragmentAnimationController) f).setAnimation(enterAnimation, exitAnimation, popEnterAnimation, popExitAnimation);
            ((FragmentAnimationController) f).setAnimator(enterAnimator, exitAnimator, popEnterAnimator, popExitAnimator);
            ((FragmentAnimationController) f).setPopAnimation(popEnterAnimId, popExitAnimId);
            t.setCustomAnimations(enterAnimId, exitAnimId, -1, -1);
        }
        else t.setCustomAnimations(enterAnimId, exitAnimId, popEnterAnimId, popExitAnimId);
        if (!isAnimationSticky) removeAnimation();

        if (addToBackStack) {
            child.add(f);
            int i = fm.getBackStackEntryCount() - 1;

            if (tag != null && i >= 0 && tag.equals(fm.getBackStackEntryAt(i).getName())) {
                fm.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }

            t.addToBackStack(tag);
        }

        if (replace)
            t.replace(containerId, f, tag);
        else
            t.add(containerId, f, tag);

        commitTransactionAllowingStateLoss(t);
        return oldFragment;
    }

    public static void commitTransactionAllowingStateLoss(FragmentTransaction t) {
        try {t.commitAllowingStateLoss();}
        catch (IllegalStateException e) {} //activity has been destroyed error on android 4.3 samsung verizon sch-I535
    }


    public static boolean goBack(FragmentActivity activityCompat) {
        if (child.size() > 0) {
            if (goBackChild(child.get(child.size() - 1).getParentFragment())) return true;
        }

        FragmentManager fm = activityCompat.getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 1) {
            try {
                activityCompat.getSupportFragmentManager().popBackStackImmediate();
            } catch (IllegalStateException e) {}
            return true;
        }
        return false;
    }

    public static boolean goBackChild(Fragment parent) {
        if (child.size() > 0) {
            child.remove(child.size() - 1);
            try {
                parent.getChildFragmentManager().popBackStack();
            } catch (IllegalStateException e) {}
            return true;
        }
        return false;
    }

    public static void remove(Fragment f) {
        if (f.getActivity() != null) {
            FragmentTransaction ft = f.getActivity().getSupportFragmentManager().beginTransaction();
            ft.remove(f).commit();
        }
    }
    public static void removeChild(Fragment f) {
        FragmentTransaction ft = f.getParentFragment().getFragmentManager().beginTransaction();
        ft.remove(f);
        commitTransactionAllowingStateLoss(ft);
    }

    public static void showChild(Fragment f) {
        FragmentTransaction ft = f.getParentFragment().getFragmentManager().beginTransaction();
        ft.show(f);
//        commitTransactionAllowingStateLoss(ft);
    }

    public static void hideChild(Fragment f) {
        FragmentTransaction ft = f.getParentFragment().getFragmentManager().beginTransaction();
        ft.hide(f);
//        commitTransactionAllowingStateLoss(ft);
    }

    public static void clear(FragmentActivity activityCompat) {
        FragmentManager fm = activityCompat.getSupportFragmentManager();

        if (fm.getBackStackEntryCount() > 0) {

            int size = fm.getFragments().size();

            if (size > 0) {
                Fragment f = fm.getFragments().get(size - 1);
                if (f != null) applyNextPopExitAnimation(f);
            }

            FragmentManager.BackStackEntry first = fm.getBackStackEntryAt(0);

            for (int i = size - 2; i >= 0; i--) {
                Fragment f = fm.getFragments().get(i);
                if (f != null && f instanceof FragmentAnimationController)
                    ((FragmentAnimationController) f).setPopAnimation(-1, 0);
            }

            try {
                fm.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            } catch (IllegalStateException e) {
                Timber.e(e, "Clear backstack problem");
            }
        }
    }

    static void applyNextPopExitAnimation(Fragment f) {
        if (f instanceof FragmentAnimationController) {
            ((FragmentAnimationController) f).setAnimation(null, null, null, nextPopExitAnimation);
            ((FragmentAnimationController) f).setAnimator(null, null, null, nextPopExitAnimator);
            ((FragmentAnimationController) f).setPopAnimation(-1, nextPopExitAnimId);
            nextPopExitAnimId = -1;
        }
    }

    public static boolean inBackStack(FragmentActivity activity, String tag) {
        FragmentManager fm = activity.getSupportFragmentManager();

        for(int i = 0; i < activity.getSupportFragmentManager().getBackStackEntryCount(); i++)
            if (tag.equals(fm.getBackStackEntryAt(i).getName())) return true;

        return false;
    }

}
