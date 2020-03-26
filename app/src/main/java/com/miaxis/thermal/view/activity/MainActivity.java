package com.miaxis.thermal.view.activity;

import android.os.Handler;

import androidx.fragment.app.Fragment;
import com.afollestad.materialdialogs.MaterialDialog;
import com.miaxis.thermal.R;
import com.miaxis.thermal.data.dao.AppDatabase;
import com.miaxis.thermal.databinding.ActivityMainBinding;
import com.miaxis.thermal.manager.GpioManager;
import com.miaxis.thermal.manager.HeartBeatManager;
import com.miaxis.thermal.manager.PersonManager;
import com.miaxis.thermal.manager.RecordManager;
import com.miaxis.thermal.manager.WatchDogManager;
import com.miaxis.thermal.manager.WebServerManager;
import com.miaxis.thermal.view.base.BaseActivity;
import com.miaxis.thermal.view.base.BaseViewModelFragment;
import com.miaxis.thermal.view.base.OnFragmentInteractionListener;
import com.miaxis.thermal.view.fragment.PreludeFragment;

public class MainActivity extends BaseActivity<ActivityMainBinding> implements OnFragmentInteractionListener {

    private MaterialDialog waitDialog;
    private MaterialDialog resultDialog;
    private MaterialDialog quitDialog;

    private String root;

    @Override
    protected int setContentView() {
        return R.layout.activity_main;
    }

    @Override
    protected void initData() {
    }

    @Override
    protected void initView() {
        initDialog();
        replaceFragment(PreludeFragment.newInstance());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GpioManager.getInstance().setStatusBar(true);
        WebServerManager.getInstance().stopServer();
        HeartBeatManager.getInstance().stopHeartBeat();
        WatchDogManager.getInstance().stopANRWatchDog();
        AppDatabase.getInstance().close();
    }

    @Override
    public void onBackPressed() {
        Fragment visibleFragment = getVisibleFragment();
        if (visibleFragment != null) {
            BaseViewModelFragment fragment = (BaseViewModelFragment) visibleFragment;
            fragment.onBackPressed();
        }
    }

    /** OnFragmentInteractionListener方法区 **/

    @Override
    public void setRoot(Fragment fragment) {
        root = fragment.getClass().getName();
        replaceFragment(fragment);
        WatchDogManager.getInstance().startANRWatchDog();
        new Thread(() -> {
//            GpioManager.getInstance().setStatusBar(false);
            HeartBeatManager.getInstance().startHeartBeat();
            PersonManager.getInstance().init();
            RecordManager.getInstance().init();
            WebServerManager.getInstance().startServer();
        }).start();
    }

    @Override
    public void backToRoot() {
        getSupportFragmentManager().popBackStack(root, 0);
    }

    @Override
    public void replaceFragment(Fragment fragment) {
        hideInputMethod();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.cl_container, fragment)
                .addToBackStack(fragment.getClass().getName())
                .commit();
    }

    @Override
    public void backToStack(Class<? extends Fragment> fragment) {
        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackEntryCount > 1) {
            if (fragment != null) {
                getSupportFragmentManager().popBackStack(fragment.getName(), 0);
            } else {
                getSupportFragmentManager().popBackStack();
            }
        } else {
            exitApp();
        }
    }

    @Override
    public void showWaitDialog(String message) {
        waitDialog.getContentView().setText(message);
        waitDialog.show();
    }

    @Override
    public void dismissWaitDialog() {
        if (waitDialog.isShowing()) {
            waitDialog.dismiss();
        }
    }

    @Override
    public void showResultDialog(String message) {
        resultDialog.getContentView().setText(message);
        resultDialog.show();
    }

    @Override
    public void dismissResultDialog() {
        if (resultDialog.isShowing()) {
            resultDialog.dismiss();
        }
    }

    @Override
    public void exitApp() {
        quitDialog.show();
    }

    /** OnFragmentInteractionListener方法区 **/

    private void initDialog() {
        waitDialog = new MaterialDialog.Builder(this)
                .progress(true, 100)
                .content("请稍后")
                .cancelable(false)
                .autoDismiss(false)
                .build();
        quitDialog = new MaterialDialog.Builder(this)
                .title("确认退出?")
                .positiveText("确认")
                .onPositive((dialog, which) -> {
                    finish();
                    System.exit(0);
                })
                .negativeText("取消")
                .build();
        resultDialog = new MaterialDialog.Builder(this)
                .content("")
                .positiveText("确认")
                .build();
    }

}