package com.jet.im.kit.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Pair;
import android.util.Size;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;

import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.log.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings("unused")
public class ImageUtils {
    private final static int DEFAULT_THUMBNAIL_WIDTH = 600;
    private final static int DEFAULT_THUMBNAIL_HEIGHT = 600;

    public static int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    @NonNull
    public static Bitmap rotate(@NonNull Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static int calculateInSampleSize(@NonNull InputStream input, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inDither = true;
        BitmapFactory.decodeStream(input, null, options);
        return calculateInSampleSize(options.outWidth, options.outHeight, width, height);
    }

    public static int calculateInSampleSize(@NonNull String filePath, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inDither = true;
        BitmapFactory.decodeFile(filePath, options);
        return calculateInSampleSize(options.outWidth, options.outHeight, width, height);
    }

    private static int calculateInSampleSize(int width, int height, int reqWidth, int reqHeight) {
        // Raw height and width of image
        int inSampleSize = 1;
        Logger.d("++ width=%s, height=%s, reqWidth=%s, reqHeight=%s", width, height, reqWidth, reqHeight);
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        Logger.d("++ inSampleSize=%s", inSampleSize);
        return inSampleSize;
    }

    @NonNull
    public static Bitmap getBitmap(@NonNull String path, int width, int height) throws IOException {
        ExifInterface exif = new ExifInterface(path);
        int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int exifDegree = exifOrientationToDegrees(exifOrientation);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = calculateInSampleSize(path, width, height);
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        if (exifDegree != 0.0f) {
            Bitmap originBitmap = bitmap;
            bitmap = rotate(bitmap, exifDegree);
            originBitmap.recycle();
        }
        return bitmap;
    }

    @SuppressWarnings("deprecation")
    @Nullable
    public static Bitmap getThumbnailBitmap(@NonNull String path, @NonNull String mimeType) throws IOException {
        if (mimeType.startsWith(StringSet.image)) {
            return getBitmap(path, DEFAULT_THUMBNAIL_WIDTH, DEFAULT_THUMBNAIL_HEIGHT);
        } else if (mimeType.startsWith(StringSet.video)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return ThumbnailUtils.createVideoThumbnail(new File(path), new Size(DEFAULT_THUMBNAIL_WIDTH, DEFAULT_THUMBNAIL_HEIGHT), null);
            } else {
                return ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
            }
        }
        return null;
    }

    @NonNull
    public static Pair<Integer, Integer> getDimensions(@NonNull String path, @NonNull String mimeType) {
        int width = 0, height = 0;
        if (mimeType.startsWith(StringSet.image)) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inSampleSize = calculateInSampleSize(path, DEFAULT_THUMBNAIL_WIDTH, DEFAULT_THUMBNAIL_HEIGHT);
            BitmapFactory.decodeFile(path, options);
            width = options.outWidth;
            height = options.outHeight;
        } else if (mimeType.startsWith(StringSet.video)) {
            Bitmap bitmap = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    bitmap = ThumbnailUtils.createVideoThumbnail(new File(path), new Size(DEFAULT_THUMBNAIL_WIDTH, DEFAULT_THUMBNAIL_HEIGHT), null);
                } catch (IOException e) {
                    Logger.w(e);
                }
            } else {
                bitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
            }
            if (bitmap != null) {
                width = bitmap.getWidth();
                height = bitmap.getHeight();
            }
        }
        return new Pair<>(width, height);
    }

    @Nullable
    public static Drawable resize(@NonNull Resources resources, @Nullable Drawable drawable, @DimenRes int width, @DimenRes int height) {
        if (drawable == null) {
            return null;
        }

        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(bitmap, (int) resources.getDimension(width), (int) resources.getDimension(height), false);

        drawable = new BitmapDrawable(resources, bitmapResized);
        return drawable;
    }
}
