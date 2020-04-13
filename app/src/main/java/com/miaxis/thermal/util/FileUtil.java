package com.miaxis.thermal.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by tang.yf on 2018/11/15.
 */

public class FileUtil {

    private static final String PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "miaxis";
    public static final String MAIN_PATH = PATH + File.separator + "thermal";
    public static final String LICENCE_PATH = PATH + File.separator + "FaceId_ST" + File.separator + "st_lic.txt";
//    public static final String MODEL_PATH = MAIN_PATH + File.separator + "zzFaceModel";
    public static final String ADVERTISE = MAIN_PATH + File.separator + "advertise";
    public static final String FACE_IMAGE_PATH = MAIN_PATH + File.separator + "recordImage";
    public static final String FACE_STOREHOUSE_PATH = MAIN_PATH + File.separator + "faceStorehouse";

    public static void initDirectory() {
        File path = new File(FileUtil.PATH);
        if (!path.exists()) {
            path.mkdirs();
        }
        path = new File(FileUtil.MAIN_PATH);
        if (!path.exists()) {
            path.mkdirs();
        }
//        path = new File(FileUtil.MODEL_PATH);
//        if (!path.exists()) {
//            path.mkdirs();
//        }
        path = new File(FileUtil.ADVERTISE);
        if (!path.exists()) {
            path.mkdirs();
        }
        path = new File(FileUtil.FACE_IMAGE_PATH);
        if (!path.exists()) {
            path.mkdirs();
        }
        path = new File(FileUtil.FACE_STOREHOUSE_PATH);
        if (!path.exists()) {
            path.mkdirs();
        }
    }

    public static String readLicence(String licencePath) {
        File lic = new File(licencePath);
        return readFileToString(lic);
    }

    public static void copyAssetsFile(Context context, String fileSrc, String fileDst) {
        InputStream is = null;
        FileOutputStream os = null;
        try {
            is = context.getAssets().open(fileSrc);
            File file = new File(fileDst);
            os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = is.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
            os.getFD().sync();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String readFileToString(File file) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String readLine;
            while ((readLine = br.readLine()) != null) {
                sb.append(readLine);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    /**
     * 根据byte数组生成文件
     *
     * @param bytes 生成文件用到的byte数组
     */
    public static void createFileWithByte(byte[] bytes, String filePath) {
        /**
         * 创建File对象，其中包含文件所在的目录以及文件的命名
         */
        File file = new File(filePath);
        // 创建FileOutputStream对象
        FileOutputStream outputStream = null;
        // 创建BufferedOutputStream对象
        BufferedOutputStream bufferedOutputStream = null;
        try {
            // 如果文件存在则删除
            if (file.exists()) {
                file.delete();
            }
            // 在文件系统中根据路径创建一个新的空文件
            file.createNewFile();
            // 获取FileOutputStream对象
            outputStream = new FileOutputStream(file);
            // 获取BufferedOutputStream对象
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            // 往文件所在的缓冲输出流中写byte数据
            bufferedOutputStream.write(bytes);
            // 刷出缓冲输出流，该步很关键，要是不执行flush()方法，那么文件的内容是空的。
            bufferedOutputStream.flush();
        } catch (Exception e) {
            // 打印异常信息
            e.printStackTrace();
        } finally {
            // 关闭创建的流对象
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    public static Bitmap openImage(String path) {
        Bitmap bitmap = null;
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path));
            bitmap = BitmapFactory.decodeStream(bis);
            bis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                baos.flush();
                baos.close();
                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.NO_WRAP);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static void saveBitmap(Bitmap bitmap, String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream out = new FileOutputStream(file);
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.getFD().sync();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeBytesToFile(byte[] bfile, String filePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            if (!dir.exists() || !dir.isDirectory()) {//判断文件目录是否存在
                dir.mkdirs();
            }
            file = new File(filePath, fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
            fos.flush();
            fos.getFD().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static void deleteImg(String path) {
        try {
            File f = new File(path);
            if (!f.delete()) {
                Log.e("asd", "删除失败" + path);
            }
        } catch (Exception e) {
            Log.e("asd", "删除失败 路径：" + path + "\r\n" + e.getMessage());
        }
    }

    public static String pathToBase64(String path) {
        try {
            byte[] bytes = toByteArray(path);
            String str = Base64.encodeToString(bytes, Base64.NO_WRAP);
            bytes = null;
            System.gc();
            return str;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static byte[] toByteArray(String filename) throws Exception {

        File f = new File(filename);
        if (!f.exists()) {
            throw new FileNotFoundException(filename);
        }

        BufferedInputStream in = null;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream((int) f.length())) {
            in = new BufferedInputStream(new FileInputStream(f));
            int buf_size = 1024;
            byte[] buffer = new byte[buf_size];
            int len = 0;
            while (-1 != (len = in.read(buffer, 0, buf_size))) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static void deleteDirectory(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    public static byte[] readSDFile(String path, String fileName) {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + path + File.separator + fileName);
        byte[] b = null;
        try {
            @SuppressWarnings("resource")
            FileInputStream inputStream = new FileInputStream(file);
            b = new byte[inputStream.available()];
            inputStream.read(b);
        } catch(Exception ex)  {
            return null;
        }
        return b;
    }

    public static Bitmap getImageFromAssetsFile(Context context, String fileName) {
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

}
