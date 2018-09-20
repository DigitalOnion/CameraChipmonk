package com.magicleap.camerachipmonk;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraDevice;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import java.nio.ByteBuffer;

import static android.hardware.camera2.CameraMetadata.LENS_FACING_FRONT;

public class MainActivity extends AppCompatActivity
        implements MainView, TextureView.SurfaceTextureListener {

    private CameraPresenter presenter = null;

    private TextureView textureView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(this);

        presenter = new CameraPresenter(this);

    }

    public void onClickBtnShoot(View viewButton) {
        presenter.shoot();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CameraPresenter.MY_PERMISSION_REQUEST_CAMERA:
                presenter.openCamera(this, LENS_FACING_FRONT);
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onImageReady(Image image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        image.close();
        Toast.makeText(this, "I've got the image", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCameraError(int errorId) {
        switch (errorId) {
            case CameraPresenter.ERROR_ID_INVALID_SIZE:
                Toast.makeText(this, R.string.error_invalid_size, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public TextureView getTextureView() {
        textureView = findViewById(R.id.textureView);
        return textureView;
    }


    /**
     *  From: TextureView.SurfaceTextureListener
     */

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        presenter.openCamera(this, LENS_FACING_FRONT);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) { /* IGNORE */ }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        /* TODO IMPLEMENT CASE */
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) { /* IGNORE */ }
}
