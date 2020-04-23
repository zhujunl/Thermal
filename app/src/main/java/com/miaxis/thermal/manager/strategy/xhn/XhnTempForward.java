package com.miaxis.thermal.manager.strategy.xhn;

import android.serialport.api.SerialPort;
import android.util.Log;

import com.miaxis.thermal.app.App;
import com.miaxis.thermal.util.DataUtils;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class XhnTempForward {

    private XhnTempForward() {
    }

    public static XhnTempForward getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final XhnTempForward instance = new XhnTempForward();
    }

    /**
     * ================================ 静态内部类单例 ================================
     **/

    private SerialPort serialPort;
    private OutputStream outputStream;

    public void open() {
        try {
            serialPort = new SerialPort();
            FileDescriptor fileDescriptor = serialPort.open("/dev/ttyS1", 9600, 0);
            outputStream = new FileOutputStream(fileDescriptor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (outputStream != null) {
                outputStream.close();
                outputStream = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void forward(float temperature) {
        App.getInstance().getThreadExecutor().execute(() -> {
            if (outputStream == null) return;
            try {
                Log.e("asd", "转发温度" + temperature);
                byte[] floatBytes = getFloatBytes(temperature);
                String s = DataUtils.ByteArrToHex(floatBytes);
                Log.e("asd", s);
                outputStream.write(floatBytes);
                outputStream.flush();
            } catch (Exception e) {
                Log.e("asd", "出错");
                e.printStackTrace();
            }
        });
    }

    private static int floorMod(int x, int y) {
        int r = x - floorDiv(x, y) * y;
        return r;
    }

    private static int floorDiv(int x, int y) {
        int r = x / y;
        if ((x ^ y) < 0 && (r * y != x)) {
            r--;
        }
        return r;
    }

    private byte[] getFloatBytes(float value) {
        int data = Math.round(value * 10);
        int value1 = data / 100;
        int value2 = floorMod(data, 100) / 10;
        int value3 = floorMod(data, 10);
        byte[] bytes = new byte[6];
        bytes[0] = (byte) 0xA5;
        bytes[1] = (byte) (value1 & 0xff);
        bytes[2] = (byte) (value2 & 0xff);
        bytes[3] = (byte) (value3 & 0xff);
        bytes[4] = (byte) 0;
        bytes[5] = (byte) ((value1 + value2 + value3) & 0xff);
        return bytes;
    }

    public static byte[] getBytes(float data) {
        int intBits = Float.floatToIntBits(data);
        return getBytes(intBits);
    }

    public static byte[] getBytes(int data) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (data & 0xff);
        bytes[1] = (byte) ((data & 0xff00) >> 8);
        bytes[2] = (byte) ((data & 0xff0000) >> 16);
        bytes[3] = (byte) ((data & 0xff000000) >> 24);
        return bytes;
    }

}
