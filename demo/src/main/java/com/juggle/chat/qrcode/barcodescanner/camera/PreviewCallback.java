package com.juggle.chat.qrcode.barcodescanner.camera;


import com.juggle.chat.qrcode.barcodescanner.SourceData;

/** Callback for camera previews. */
public interface PreviewCallback {
    void onPreview(SourceData sourceData);

    void onPreviewError(Exception e);
}
