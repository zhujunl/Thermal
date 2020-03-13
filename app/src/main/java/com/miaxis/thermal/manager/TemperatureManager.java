package com.miaxis.thermal.manager;

import android.serialport.api.SerialPort;
import android.util.Log;

import com.miaxis.thermal.util.DataUtils;

import org.zz.api.MXFaceInfoEx;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class TemperatureManager {

    private TemperatureManager() {
    }

    public static TemperatureManager getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final TemperatureManager instance = new TemperatureManager();
    }

    /**
     * ================================ 静态内部类单例写法 ================================
     **/

    private static final byte[] COMMAND_FRAME = new byte[]{(byte) 0xA5, (byte) 0x35, (byte) 0xF1, (byte) 0xCB};
    private static final byte[] COMMAND_SEARCH = new byte[]{(byte) 0xA5, (byte) 0x55, (byte) 0x01, (byte) 0xFB};

    private SerialPort serialPort;
    private InputStream inputStream;
    private OutputStream outputStream;

    private ReceiveThread receiveThread;

    private volatile boolean running = false;
    private float temperature = 0;

    public void open() {
        try {
            serialPort = new SerialPort();
            FileDescriptor fileDescriptor = serialPort.open("/dev/ttyS1", 115200, 0);
            inputStream = new FileInputStream(fileDescriptor);
            outputStream = new FileOutputStream(fileDescriptor);
            running = true;
            Log.e("asd", "开启串口");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        Log.e("asd", "关闭串口");
        try {
            if (inputStream != null) {
                inputStream.close();
                inputStream = null;
            }
            if (outputStream != null) {
                outputStream.close();
                outputStream = null;
            }
            running = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendSerialPort(String data) {
        if (outputStream == null) {
            return;
        }
        try {
            byte[] sendData = DataUtils.HexToByteArr(data);
            outputStream.write(sendData);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public float readFrame(MXFaceInfoEx faceInfo, int width, int height) {
        if (inputStream == null || outputStream == null) {
            return 0f;
        }
        try {
            outputStream.write(COMMAND_FRAME);
            outputStream.flush();
            long t = System.currentTimeMillis();
            int sumLength = 1548;
            byte[] readData = new byte[sumLength];
            int length = 0;
            int hasRead = 0;//已经读取的字节数
            while ((length = inputStream.read(readData, hasRead, sumLength - hasRead)) > 0) {
                hasRead = hasRead + length;
                if (hasRead >= sumLength) {
                    break;
                }
            }
            if (readData[0] != 0x5A || readData[1] != 0x5A) {
                return -1f;
            }
            int x = faceInfo.x;
            int y = faceInfo.y;
            double xf1 = (double) x / width;
            double xf2 = (double) (x + faceInfo.width) / width;
            double yf1 = (double) y / height;
            double yf2 = (double) (y + faceInfo.height) / height;
            List<Float> temperatureList = new ArrayList<>();
            for (int i = 1; i <= 768; i++) {
                int index = i * 2 + 7;
                float tem = (float) (readData[index] * 256 + readData[index - 1]) / 100;
                temperatureList.add(tem);
            }
//            for (int i = 1; i <= 32; i++) {
//                double ip = (double) i / 32;
//                if (ip >= xf1 && ip <= xf2) {
//                    for (int j = 1; j <= 24; j++) {
//                        double jp = (double) j / 24;
//                        if (jp >= yf1 && jp <= yf2) {
//                            int position = (j - 1) * 32 + i;
//                            int index = position * 2 + 7;
//                            float temperature = (float) (readData[index + 1] * 256 + readData[index]);
//                            temperatureList.add(temperature);
//                        }
//                    }
//                }
//            }
            float max = 0f;
            for (Float temperature : temperatureList) {
                if (temperature > max) {
                    max = temperature;
                }
            }
            return max;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0f;
    }

    public float readTemperature() {
        if (inputStream == null || outputStream == null) {
            return 0f;
        }
        byte[] readData = new byte[10];
        try {
            outputStream.write(COMMAND_SEARCH);
            outputStream.flush();
//            Log.e("asd", "发送时间：" + System.currentTimeMillis());
            int size = inputStream.read(readData);
            if (size > 0) {
//                Log.e("asd", "接收时间：" + System.currentTimeMillis());
                float temperature = (readData[2] + 256 * readData[3]) / 100f;
                Log.e("sad", "温度" + temperature);
                //四舍五入
                temperature = (float) Math.round(temperature * 10) / 10;
                return temperature;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0f;
    }

    private void startReceiveThread() {
        receiveThread = new ReceiveThread();
        receiveThread.start();
    }

    private class ReceiveThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (running) {
                if (inputStream == null || outputStream == null) {
                    return;
                }
                byte[] readData = new byte[10];
                try {
                    sendSerialPort("A55501FB");
                    Log.e("asd", "发送时间：" + System.currentTimeMillis());
                    int size = inputStream.read(readData);
                    if (size > 0) {
                        Log.e("asd", "接收时间：" + System.currentTimeMillis());
                        String readString = DataUtils.ByteArrToHex(readData, 0, size);
                        float temperature = (readData[2] + 256 * readData[3]) / 100f;
                        Log.e("sad", readString + "------" + temperature);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

//    public interface On

}
