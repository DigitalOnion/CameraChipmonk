package com.magicleap.camerachipmonk;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraDevice;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Socket;
import java.nio.ByteBuffer;

import static android.hardware.camera2.CameraMetadata.LENS_FACING_FRONT;

public class MainActivity extends AppCompatActivity
        implements MainView, HostCallback, TextureView.SurfaceTextureListener {

    private HttpServerThread httpServerThread = null;
    private Socket responseSocket;

    private CameraPresenter presenter = null;
    private TextureView textureView = null;

    TextView httpLog;
    TextView ipInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(this);

        // Camera logic (Camera2 API)
        presenter = new CameraPresenter(this);

        httpLog = findViewById(R.id.http_log);
        ipInfo = findViewById(R.id.ip_info);

        // HTTP light server - uses a ServerSocket
        httpServerThread = new HttpServerThread();
        httpServerThread.setHostCallback(this);
        httpServerThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        httpServerThread.close();
    }

    public static final String HTTP_ADDRESS_CLIP = "HTTP_ADDRESS_CLIPBOARD";
    public void onClickBtnClipboard(View view) {
        CharSequence address = ipInfo.getText();
        ClipboardManager clipManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(HTTP_ADDRESS_CLIP, address);
        clipManager.setPrimaryClip(clip);
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
        Toast.makeText(this, "I've got the image", Toast.LENGTH_SHORT).show();

//        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//        byte[] bytes = new byte[buffer.capacity()];
//        buffer.get(bytes);
//        Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
//        ImageView imageView = findViewById(R.id.test_image);
//
//        imageView.setImageBitmap(bitmapImage);

        httpServerThread.generateHttpResponse(image, responseSocket);
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

    /**
     * From HostCallback
     */

    @Override
    public void logHttpEvent(String httpEvent) {
        String log = httpEvent + '\n' + httpLog.getText().toString();
        if(log.length() > 1000) {
            log = log.substring(1, 1000);
        }
        httpLog.setText(log);
    }

    @Override
    public void onIpAddressKnown(String ipAddress) {
        ipInfo.setText(ipAddress);
    }

    @Override
    public void onHttpRequestReceived(final Socket socket) {
        responseSocket = socket;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                presenter.shoot();
            }
        });
    }

    @Override
    public ContentResolver getHostContentResolver() {
        return this.getContentResolver();
    }
}
