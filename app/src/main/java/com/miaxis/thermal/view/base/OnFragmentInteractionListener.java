package com.miaxis.thermal.view.base;

import androidx.fragment.app.Fragment;

import com.miaxis.thermal.view.presenter.UpdatePresenter;

public interface OnFragmentInteractionListener {
    void setRoot(Fragment fragment);
    void backToRoot();
    void replaceFragment(Fragment fragment);
    void backToStack(Class<? extends Fragment> fragment);
//    void addFragment(Fragment lastFragment, Fragment fragment);
    void showWaitDialog(String message);
    void dismissWaitDialog();
    void showResultDialog(String message);
    void dismissResultDialog();
    void exitApp();
    void updateApp(UpdatePresenter.OnCheckUpdateResultListener listener);
}
