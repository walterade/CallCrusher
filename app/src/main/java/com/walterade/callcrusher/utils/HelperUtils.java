package com.walterade.callcrusher.utils;

import android.content.Context;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Walter on 11/15/16.
 */

public class HelperUtils {
    public interface OnLinkClickListener {
        void onLinkClicked(View v, String url);
    }

    public static String plural(String word, int count) {
        if (count != 1) {
            if (word.toLowerCase().endsWith("y") && !(
                    word.toLowerCase().endsWith("ay") ||
                    word.toLowerCase().endsWith("ey") ||
                    word.toLowerCase().endsWith("iy") ||
                    word.toLowerCase().endsWith("oy") ||
                    word.toLowerCase().endsWith("uy")
                )
            ) word = word.substring(0, word.length() - 1) + "ies";
            else word += "s";
        }
        word = word.replace("#", String.valueOf(count));
        return word;
    }

    private static void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span, final OnLinkClickListener clickListener)
    {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        ClickableSpan clickable = new ClickableSpan() {
            public void onClick(View view) {
                // Do something with span.getURL() to handle the link click...
                if (clickListener != null) clickListener.onLinkClicked(view, span.getURL());
            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

    public static void setTextViewHTML(TextView text, String html, OnLinkClickListener clickListener)
    {
        CharSequence sequence = Html.fromHtml(html);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for(URLSpan span : urls) {
            makeLinkClickable(strBuilder, span, clickListener);
        }
        text.setText(strBuilder);
        text.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public static float dpFromPx(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

}
