package com.miaxis.thermal.manager.strategy.xh;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.serialport.api.SerialPort;
import android.util.Log;

import com.miaxis.thermal.manager.ConfigManager;
import com.miaxis.thermal.manager.TemperatureManager;
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

public class XhTemperatureStrategy implements TemperatureManager.TemperatureStrategy {

    private static final byte[] COMMAND_FRAME = new byte[]{(byte) 0xA5, (byte) 0x35, (byte) 0xF1, (byte) 0xCB};
    private static final byte[] COMMAND_SEARCH = new byte[]{(byte) 0xA5, (byte) 0x55, (byte) 0x01, (byte) 0xFB};

    private SerialPort serialPort;
    private InputStream inputStream;
    private OutputStream outputStream;

    @Override
    public void open() {
        try {
            serialPort = new SerialPort();
            FileDescriptor fileDescriptor = serialPort.open("/dev/ttyS1", 115200, 0);
            inputStream = new FileInputStream(fileDescriptor);
            outputStream = new FileOutputStream(fileDescriptor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            if (inputStream != null) {
                inputStream.close();
                inputStream = null;
            }
            if (outputStream != null) {
                outputStream.close();
                outputStream = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void readTemperature(TemperatureManager.TemperatureListener listener) {
        if (listener != null) {
            if (ConfigManager.getInstance().getConfig().isHeatMap()) {
                readTemperatureFromFrame(listener);
            } else {
                listener.onTemperature(readTemperatureFromSearch());
                listener.onHeatMap(null);
            }
        }
    }

    public float readTemperatureFromSearch() {
        if (inputStream == null || outputStream == null) {
            return 0f;
        }
        byte[] readData = new byte[10];
        try {
            outputStream.write(COMMAND_SEARCH);
            outputStream.flush();
            int size = inputStream.read(readData);
            if (size > 0) {
                float temperature = (readData[2] + 256 * readData[3]) / 100f;
                //四舍五入
                temperature = (float) Math.round(temperature * 10) / 10;
                return temperature;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0f;
    }

    public void readTemperatureFromFrame(TemperatureManager.TemperatureListener listener) {
        float temperature = readTemperatureFromSearch();
        if (inputStream == null || outputStream == null) {
            listener.onTemperature(0f);
            listener.onHeatMap(null);
            return;
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
                listener.onTemperature(0f);
                listener.onHeatMap(null);
                return;
            }
            List<Float> temperatureList = new ArrayList<>();
            for (int i = 1; i <= 768; i++) {
                int index = i * 2 + 7;
                float tem = (float) (readData[index] * 256 + readData[index - 1]) / 100;
                temperatureList.add(tem);
            }
            Bitmap bmp = Bitmap.createBitmap(32, 24, Bitmap.Config.RGB_565);
            for (int i = 0; i < 32; i++) {
                for (int j = 0; j < 24; j++) {
                    bmp.setPixel(i, j, getHeatMapColor(temperatureList.get(i * 32 + j)));
                }
            }
//            float max = 0f;
//            for (Float temperature : temperatureList) {
//                if (temperature > max) {
//                    max = temperature;
//                }
//            }
            listener.onTemperature(temperature);
            listener.onHeatMap(bmp);
        } catch (IOException e) {
            e.printStackTrace();
            listener.onTemperature(0f);
            listener.onHeatMap(null);
        }
    }

    private int getHeatMapColor(float temperature) {
        if (temperature < 20) {
            return Color.parseColor("#008CFF");
        } else if (temperature < 21) {
            return Color.parseColor("#0080FF");
        } else if (temperature < 22) {
            return Color.parseColor("#0073FF");
        } else if (temperature < 23) {
            return Color.parseColor("#0066FF");
        } else if (temperature < 24) {
            return Color.parseColor("#0059FF");
        } else if (temperature < 25) {
            return Color.parseColor("#004DFF");
        } else if (temperature < 26) {
            return Color.parseColor("#0040FF");
        } else if (temperature < 27) {
            return Color.parseColor("#0033FF");
        } else if (temperature < 28) {
            return Color.parseColor("#0026FF");
        } else if (temperature < 29) {
            return Color.parseColor("#001AFF");
        } else if (temperature < 30) {
            return Color.parseColor("#FF8C00");
        } else if (temperature < 31) {
            return Color.parseColor("#FF8000");
        } else if (temperature < 32) {
            return Color.parseColor("#FF7300");
        } else if (temperature < 33) {
            return Color.parseColor("#FF6600");
        } else if (temperature < 34) {
            return Color.parseColor("#FF5900");
        } else if (temperature < 35) {
            return Color.parseColor("#FF4D00");
        } else if (temperature < 36) {
            return Color.parseColor("#FF4000");
        } else if (temperature < 37) {
            return Color.parseColor("#FF3300");
        } else if (temperature < 38) {
            return Color.parseColor("#FF2600");
        } else if (temperature < 39) {
            return Color.parseColor("#FF1A00");
        } else if (temperature < 40) {
            return Color.parseColor("#FF0D00");
        } else {
            return Color.parseColor("#FF0000");
        }
    }

    private float rectifyDeviation(float value) {
        if (value < 300) value += 38;
        else {
            if (value < 310 && value >= 300) value += 30;
            if (value < 320 && value >= 310) value += 20;
            if (value < 330 && value >= 320) value += 15;
        }
        if (value < 337 && value >= 330) {
            value += 26;
        }
        else if (value <= 340 && value > 337) value += 23;
        else if (value <= 342 && value > 340) value += 22;
        else if (value <= 344 && value > 342) value += 21.8;
        else if (value <= 346 && value > 344) value += 21.6;
        else if (value <= 348 && value > 346) value += 21.4;
        else if (value <= 350 && value > 348) value += 21.2;
        else if (value <= 352 && value > 350) value += 19.8;
        else if (value <= 354 && value > 352) value += 19.4;
        else if (value <= 356 && value > 354) value += 19;
        else if (value <= 358 && value > 356) value += 19;
        else if (value <= 360 && value > 358) value += 18;
        else if (value <= 362 && value > 360) value += 18;
//        else if (value <= 364 && value > 362) value += 23;
//        else if (value <= 366 && value > 364) value += 23;
//        else if (value <= 368 && value > 366) value += 23;
//        else if (value <= 370 && value > 368) value += 23;
//        else if (value <= 372 && value > 370) value += 23;
//        else if (value <= 374 && value > 372) value += 23;


        return (float) value / 10;
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

}
