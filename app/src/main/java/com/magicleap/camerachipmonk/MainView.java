package com.magicleap.camerachipmonk;

import android.hardware.camera2.CameraDevice;
import android.media.Image;
import android.util.Size;
import android.view.TextureView;

import java.net.Socket;

public interface MainView {
    public void onCameraError(int errorId);
    public void onImageReady(Image image);

    public TextureView getTextureView();
}
