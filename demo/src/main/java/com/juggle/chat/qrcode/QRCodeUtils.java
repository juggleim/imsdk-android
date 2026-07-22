package com.juggle.chat.qrcode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.juggle.chat.qrcode.client.DecodeFormatManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;

public class QRCodeUtils {
    /**
     * Generate a QR code with a logo.
     *
     * @param text the text to encode
     * @param w the QR code width
     * @param h the QR code height
     * @param logo the center logo; pass null to omit it
     * @return the generated QR code bitmap
     */
    public static Bitmap generateImage(String text, int w, int h, Bitmap logo) {
        if (TextUtils.isEmpty(text)) {
            return null;
        }
        try {
            Bitmap scaleLogo = getScaleLogo(logo, w, h);

            int offsetX = w / 2;
            int offsetY = h / 2;

            int scaleWidth = 0;
            int scaleHeight = 0;
            if (scaleLogo != null) {
                scaleWidth = scaleLogo.getWidth();
                scaleHeight = scaleLogo.getHeight();
                offsetX = (w - scaleWidth) / 2;
                offsetY = (h - scaleHeight) / 2;
            }
            Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            // Error correction level
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            // Set the blank margin width.
            hints.put(EncodeHintType.MARGIN, 0);
            BitMatrix bitMatrix =
                    new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, w, h, hints);
            int[] pixels = new int[w * h];
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    if (x >= offsetX
                            && x < offsetX + scaleWidth
                            && y >= offsetY
                            && y < offsetY + scaleHeight) {
                        int pixel = scaleLogo.getPixel(x - offsetX, y - offsetY);
                        if (pixel == 0) {
                            if (bitMatrix.get(x, y)) {
                                pixel = 0xff000000;
                            } else {
                                pixel = 0xffffffff;
                            }
                        }
                        pixels[y * w + x] = pixel;
                    } else {
                        if (bitMatrix.get(x, y)) {
                            pixels[y * w + x] = 0xff000000;
                        } else {
                            pixels[y * w + x] = 0xffffffff;
                        }
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Bitmap getScaleLogo(Bitmap logo, int w, int h) {
        if (logo == null) return null;
        Matrix matrix = new Matrix();
        float scaleFactor =
                Math.min(w * 1.0f / 5 / logo.getWidth(), h * 1.0f / 5 / logo.getHeight());
        matrix.postScale(scaleFactor, scaleFactor);
        return Bitmap.createBitmap(logo, 0, 0, logo.getWidth(), logo.getHeight(), matrix, true);
    }

    /**
     * Recognize an image.
     *
     * @param path a file:// or http:// resource
     */
    public static String analyzeImage(final String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        if (path.startsWith("http://")) {
            return analyzeBitmap(getImage(path));
        } else {
            /** First check the image size; if it is too large, downsample it to avoid OOM. */
            Bitmap mBitmap;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true; // Get original size first.
            BitmapFactory.decodeFile(path, options);
            int sampleSize = (int) (options.outHeight / (float) 400);
            if (sampleSize <= 0) sampleSize = 1;
            options.inSampleSize = sampleSize;
            options.inJustDecodeBounds = false; // Get the resized dimensions.
            mBitmap = BitmapFactory.decodeFile(path, options);
            mBitmap = zoomImg(mBitmap, mBitmap.getWidth() * 3, mBitmap.getHeight() * 3);
            return analyzeBitmap(mBitmap);
        }
    }

    /**
     * Convert a network http:// image to a bitmap.
     *
     * @param path the http:// URL
     * @return Bitmap
     */
    private static Bitmap getImage(String path) {
        Bitmap bitmap = null;
        try {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static Bitmap zoomImg(Bitmap bm, int newWidth, int newHeight) {
        // Get the image dimensions.
        int width = bm.getWidth();
        int height = bm.getHeight();
        // Compute the scale ratio.
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // Create the scaling matrix.
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // Create the scaled image.
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }

    /**
     * Decode a QR code bitmap.
     *
     * @param bitmap the bitmap to decode
     * @return the decoded text
     */
    public static String analyzeBitmap(Bitmap bitmap) {
        MultiFormatReader multiFormatReader = new MultiFormatReader();

        // Decode parameters.
        Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>(2);
        // Supported barcode formats.
        Vector<BarcodeFormat> decodeFormats = new Vector<BarcodeFormat>();
        if (decodeFormats.isEmpty()) {
            decodeFormats = new Vector<BarcodeFormat>();

            // Use all supported scan formats here.
            decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
            decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
            decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);
        }
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        // Use UTF-8 as the character encoding.
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        // Configure decode parameters.
        multiFormatReader.setHints(hints);

        // Start decoding the image resource.
        Result rawResult = null;
        String result = null;
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            rawResult =
                    multiFormatReader.decodeWithState(
                            new BinaryBitmap(
                                    new HybridBinarizer(
                                            new RGBLuminanceSource(width, height, pixels))));
            result = rawResult.getText();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
