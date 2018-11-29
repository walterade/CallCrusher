package com.walterade.callcrusher.mvp.ui.base;

/**
 * Created by Walter on 9/28/17.
 */

/**
 * Every presenter in the app must either implement this interface or extend BasePresenter
 * indicating the MvpView type that wants to be attached with.
 */
public interface Presenter<V extends BaseView> {

    void attachView(V mvpView);

    void detachView();
}