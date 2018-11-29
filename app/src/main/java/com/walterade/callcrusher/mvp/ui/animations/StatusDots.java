package com.walterade.callcrusher.mvp.ui.animations;

import android.os.Handler;
import android.text.TextUtils;

/**
 * Created by Walter on 11/14/17.
 */

public class StatusDots {

    public interface OnStatusDotUpdateListener {
        void onStatusDotUpdate(String status);
    }

    Handler statusDotHandler = new Handler();
    private int statusDotCount;
    StringBuilder dots;
    boolean enable;
    private String status;
    private OnStatusDotUpdateListener listener;

    public StatusDots() {
        createDots(4);
    }

    public StatusDots(int dots) {
        createDots(dots);
    }

    public void setOnStatusDotUpdateListener(OnStatusDotUpdateListener listener) {
        this.listener = listener;
    }

    void createDots(int dots) {
        this.dots = new StringBuilder();
        while (dots-- > 0)
            this.dots.append(".");
    }

    public void setStatus(String status) {
        if (TextUtils.isEmpty(status)) {
            setEnabled(false);
            return;
        }

        if (!status.equals(this.status) || !this.enable) {
            this.status = status;
            updateStatus();
            setEnabled(true);
        }
    }

    public void setEnabled(boolean enable) {
        if (this.enable != enable) {
            this.enable = enable;
            if (!enable) {
                if (status != null) onStatusUpdate(status);
                statusDotHandler.removeCallbacksAndMessages(null);
            }
            else showStatusDots(true);
        }
    }

    private void showStatusDots(boolean reset) {
        if (reset) {
            statusDotHandler.removeCallbacksAndMessages(null);
        }

        if (enable) {
            updateStatus();

            statusDotHandler.postDelayed(() -> {
                if (enable) {
                    if (++statusDotCount > dots.length()) statusDotCount = 1;
                    updateStatus();
                    showStatusDots(false);
                }
            }, 500);
        }
        else
            statusDotCount = 0;
    }

    void updateStatus() {
        onStatusUpdate(getStatus(status));
    }

    final void onStatusUpdate(String status) {
        if (listener != null) listener.onStatusDotUpdate(status);
    }

    String getStatus(String status) {
        return status + dots.substring(0, statusDotCount);
    }

    public void stop(String status) {
        setStatus(status);
        setEnabled(false);
    }


}
