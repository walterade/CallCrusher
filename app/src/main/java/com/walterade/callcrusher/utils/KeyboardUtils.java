package com.walterade.callcrusher.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Walter on 11/23/16.
 */
public class KeyboardUtils {
    public static void hide(View focus) {
        InputMethodManager imm = (InputMethodManager)focus.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
    }

    public static void hide(Activity a) {
        if (a != null) {
            InputMethodManager imm = (InputMethodManager) a.getSystemService(Context.INPUT_METHOD_SERVICE);
            View v = a.getCurrentFocus();
            if (v == null) v = a.getWindow().getCurrentFocus();
            if (v == null) v = a.getWindow().getDecorView();
            if (v != null) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                v.clearFocus();
            }
        }
    }
}
