package com.miaxis.thermal.manager.strategy.xh;

import android.app.Application;

import com.android.xhapimanager.XHApiManager;
import com.miaxis.thermal.manager.GpioManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class XhGpioStrategy implements GpioManager.GpioStrategy {

    private static final String GPIO5 = "/sys/class/xh_custom/xh_custom_gpio/device/gpio5";

    private Application context;
    private XHApiManager xhApiManager;

    @Override
    public void init(Application application) {
        this.context = application;
        xhApiManager = new XHApiManager();
    }

    @Override
    public void resetGpio() {
        xhApiManager.XHSetGpioValue(0, 0);
        xhApiManager.XHSetGpioValue(1, 0);
        xhApiManager.XHSetGpioValue(2, 0);
        xhApiManager.XHSetGpioValue(3, 0);
        xhApiManager.XHSetGpioValue(4, 0);
    }

    @Override
    public void controlWhiteLed(boolean status) {
        xhApiManager.XHSetGpioValue(0, status ? 1 : 0);
    }

    @Override
    public void controlGreenLed(boolean status) {
        xhApiManager.XHSetGpioValue(1, status ? 1 : 0);
    }

    @Override
    public void controlRedLed(boolean status) {
        xhApiManager.XHSetGpioValue(2, status ? 1 : 0);
    }

    @Override
    public void setStatusBar(boolean show) {
        xhApiManager.XHShowOrHideStatusBar(show);
    }

    @Override
    public void openGate(boolean open) {
        RootCommand("echo " + (open ? 1 : 0) + " > " + GPIO5);
    }

    public void setGpio(int gpio, int value) {
        xhApiManager.XHSetGpioValue(gpio, value);
    }

    private void RootCommand(String cmd) {
        Process process = null;
        DataOutputStream os = null;
        DataInputStream is = null;
        try {
            process = Runtime.getRuntime().exec("/system/xbin/su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();

            int aa = process.waitFor();
            is = new DataInputStream(process.getInputStream());

            byte[] buffer = new byte[is.available()];
            is.read(buffer);

            //String out = new String(buffer);
            // Log.d(TAG, out + aa);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (is != null) {
                    is.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
    }

}
