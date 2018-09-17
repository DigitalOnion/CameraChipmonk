package com.magicleap.camerachipmonk;

import android.hardware.camera2.CameraDevice;
import android.util.Size;
import android.view.TextureView;

public interface MainView {
    public void onCameraDeviceReady(CameraDevice cameraDevice);
    public void onCameraError(int errorId);

    public TextureView getTextureView();
    public Size getPreviewSize();
}
