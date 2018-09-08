package com.magicleap.camerachipmonk;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraDevice;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;

public class MainActivity extends AppCompatActivity
        implements CameraView {

    private CameraPresenter presenter = null;

    private TextureView texture = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        presenter = new CameraPresenter(this);
        texture = findViewById(R.id.texture);
        //presenter.openCamera();
    }

    public void onClickBtnShoot(View viewButton) {
        presenter.openCamera(0);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public AppCompatActivity getActivity() {
        return this;
    }
}
