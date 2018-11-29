package com.walterade.callcrusher.widget;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.walterade.callcrusher.R;

public class CustomSnackbar extends BaseTransientBottomBar<CustomSnackbar> {

    /**
     * Constructor for the transient bottom bar.
     *
     * @param parent The parent for this transient bottom bar.
     * @param content The content view for this transient bottom bar.
     * @param callback The content view callback for this transient bottom bar.
     */
    private CustomSnackbar(ViewGroup parent, View content, ContentViewCallback callback) {
        super(parent, content, callback);
    }

    public static CustomSnackbar make(@NonNull ViewGroup parent, @LayoutRes int id, @Duration int duration) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View content = inflater.inflate(id, parent, false);
        final ContentViewCallback viewCallback = new ContentViewCallback(content);
        final CustomSnackbar customSnackbar = new CustomSnackbar(parent, content, viewCallback);

        customSnackbar.getView().setPadding(0, 0, 0, 0);
        customSnackbar.setDuration(duration);
        return customSnackbar;
    }

    public CustomSnackbar setText(CharSequence text) {
        TextView textView = getView().findViewById(R.id.snackbar_text);
        textView.setText(text);
        return this;
    }

    public CustomSnackbar setAction(CharSequence text, final View.OnClickListener listener) {
        Button actionView = getView().findViewById(R.id.snackbar_action);
        actionView.setText(text);
        actionView.setVisibility(View.VISIBLE);
        actionView.setOnClickListener(view -> {
            listener.onClick(view);
            // Now dismiss the Snackbar
            dismiss();
        });
        return this;
    }

    private static class ContentViewCallback implements BaseTransientBottomBar.ContentViewCallback {

        private View content;

        public ContentViewCallback(View content) {
            this.content = content;
        }

        @Override
        public void animateContentIn(int delay, int duration) {
            content.setScaleY(0f);
            ViewCompat.animate(content).scaleY(1f).setDuration(duration).setStartDelay(delay);
        }

        @Override
        public void animateContentOut(int delay, int duration) {
            content.setScaleY(1f);
            ViewCompat.animate(content).scaleY(0f).setDuration(duration).setStartDelay(delay);
        }
    }
}