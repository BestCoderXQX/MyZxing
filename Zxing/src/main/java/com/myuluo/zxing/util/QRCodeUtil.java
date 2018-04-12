package com.myuluo.zxing.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Environment;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Myuluo on 2016/4/13.
 */
public class QRCodeUtil {

    private static String CacheDir = Environment.getExternalStorageDirectory().getAbsolutePath();// 系统路径
    private static final String FolderName = "/QrCode/";// 文件夹名称
    private static String imgName = "QrCode_";// 二维码名称前缀

    /**
     * 生成二维码Bitmap
     *
     * @param url       内容
     * @param widthPix  图片宽度
     * @param heightPix 图片高度
     * @param logoBm    二维码中心的Logo图标（可以为null）
     * @param filePath  用于存储二维码图片的文件路径
     * @return 返回生成的二维码所保存的文件路径
     */
    public static String createQRImage(String url, int widthPix, int heightPix, Bitmap logoBm, String filePath) {
        try {
            if (url == null || "".equals(url) || url.length() < 1) {
                return null;
            }
            //配置参数
            Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            //容错级别
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            // 图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, widthPix, heightPix, hints);
            int[] pixels = new int[widthPix * heightPix];
            // 下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (int y = 0; y < heightPix; y++) {
                for (int x = 0; x < widthPix; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * widthPix + x] = 0xff000000;// 决定二维码的颜色
                    } else {
                        pixels[y * widthPix + x] = 0xffffffff;// 白色背景，如果设置为0x00ffffff，背景将会变透明
                        // 视音频imageView中没有颜色的位置默认使用的是白色的替代的，但是保存成png后，没有颜色的位置使用黑色替代，所以会看到生成的图片是全黑的
                    }
                }
            }
            // 生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, widthPix, 0, 0, widthPix, heightPix);
            // 如果传入的logo为null，则默认没有logo
            if (logoBm != null) {
                bitmap = addLogo(bitmap, logoBm);
            }
            // 如果传入的路径为null，保存到默认路径
            if (filePath == null) {
                filePath = saveToSDCard();
            }
            // 必须使用compress方法将bitmap保存到文件中再进行读取。直接返回的bitmap是没有任何压缩的，内存消耗巨大！
            // bitmap.compress()：把位图的压缩信息写入到一个指定的输出流中，如果返回true（已经成功压缩到输出流）
            boolean isBitmap = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(filePath));
            // 判断是否保存成功，保存成功则将二维码的存储路径返回
            if (bitmap != null && isBitmap) {
                return filePath;
            }
            return null;
        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 在二维码中间添加Logo图案
     */
    private static Bitmap addLogo(Bitmap src, Bitmap logo) {
        if (src == null) {
            return null;
        }

        if (logo == null) {
            return src;
        }

        // 获取生成的二维码图片的宽高
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        // 获取logo图片的宽高
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();

        if (srcWidth == 0 || srcHeight == 0) {
            return null;
        }

        if (logoWidth == 0 || logoHeight == 0) {
            return src;
        }
        //logo大小为二维码整体大小的1/5
        float scaleFactor = srcWidth * 1.0f / 5 / logoWidth;
        // 创建一个宽width、高height的新Bitmap对象，创建空白的画布
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            /**
             * 在指定点绘制位图：
             *      参数1：位图Bitmap对象，代表图像资源
             *      参数二：图像显示时，距离窗口左边的位置
             *      参数三：图像显示时，距离窗口上边的位置
             *      参数四：画笔
             */
            canvas.drawBitmap(src, 0, 0, null);// 将二维码绘制到画布上
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);// 坐标为中心点，对Canvas执行缩放变换
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);// 将logo绘制到画布上
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }
        return bitmap;
    }

    /**
     * 将生成的二维码保存到SD卡中
     *
     * @return 返回二维码的储存路径
     */
    public static String saveToSDCard() {
        File dir = new File(CacheDir + FolderName);
        if (!dir.exists()) {// 判断文件夹是否存在，不存在则创建文件夹
            dir.mkdirs();
        }
        File jpgFile = new File(dir, imgName + System.currentTimeMillis() + ".jpg");
        return jpgFile.getAbsolutePath();
    }
}
