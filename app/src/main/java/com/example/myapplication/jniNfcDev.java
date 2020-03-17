package com.example.myapplication;

import android.util.Log;

import java.util.Arrays;

public class jniNfcDev {
    static {
        System.loadLibrary("NfcDevCtl");
    }

    private static final String TAG = "jniNfcDev";

    private native int NFC_OpenDevice();

    private native int NFC_CloseDevice();

    private native int NFC_Get_IDNum(int[] arr);

    private int Array0[] = new int[1024];

    public void jniOpenNfc() {
        Log.e("dxx", "jniOpenNfc");
        int ret = NFC_OpenDevice(); // 0 -success ; errorno- fail
        if (ret == 0)
            Log.e("dxx", "success");
        else
            Log.e("dxx", "error no is :" + ret);

    }

    public void jniCloseNfc() {
        Log.e("dxx", "jniCloseNfc");
        int ret = NFC_CloseDevice(); // 0 -success ; errorno- fail
        if (ret == 0)
            Log.e("dxx", "success");
        else
            Log.e("dxx", "error no is :" + ret);
    }


    public int jni_Get_IDNum(int[] arr) {
        Log.e("dxx", "NFC_Get_IDNum");

        //cardNum[0] = 0x1;
        int ret = NFC_Get_IDNum(arr); // 0 -success ; errorno- fail

        // for(int i = 0; i < 1024;i++)
        Log.d("dxx", "arr:" + Arrays.toString(arr));

        return 0;
    }

}
