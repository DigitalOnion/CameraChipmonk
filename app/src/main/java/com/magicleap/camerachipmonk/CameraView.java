package com.magicleap.camerachipmonk;

import android.content.Context;
import android.hardware.camera2.CameraDevice;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public interface CameraView {

    Context getContext();

    AppCompatActivity getActivity();
}
