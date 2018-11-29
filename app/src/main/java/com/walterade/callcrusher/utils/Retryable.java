package com.walterade.callcrusher.utils;

import timber.log.Timber;

/**
 * Created by Walter on 3/21/18.
 */

public class Retryable {
    private final int retries;

    public static void retry(int retries, RetryAction action, RetryFailure failure) {
        new Retryable(retries, action, failure);
    }

    public interface RetryAction {
        boolean run();
    }
    public interface RetryFailure {
        void onFailure();
    }

    public Retryable(int retries, RetryAction action, RetryFailure failure) {
        this.retries = retries;
        boolean ok = false;
        int r = 0;

        do {
            try {
                ok = action.run();
            } catch (Exception e) {
                Timber.e(e, "Retry %s failed after %s tries", action.toString(),r + 1);
            }
        } while (!ok && (++r < retries));

        if (ok && r > 1) Timber.e("Retry %s success after %s tries", action.toString(), r + 1);

        if (!ok && failure != null) failure.onFailure();
    }
}
