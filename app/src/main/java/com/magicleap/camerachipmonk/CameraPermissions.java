package com.magicleap.camerachipmonk;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

public class CameraPermissions implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int MY_PERMISSION_REQUEST_CAMERA = 1234;

    public interface CameraPermissionsCallback {
        
    }

    AppCompatActivity activity = null;

    public CameraPermissions(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void requestCameraPermission() {
        if(ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {

            if(ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.CAMERA)) {
                new AlertDialog.Builder(activity)
                        .setMessage(R.string.camera_permission_rationale)
                        .setPositiveButton(android.R.string.yes, null)
                        .create();
            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSION_REQUEST_CAMERA);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int i, @NonNull String[] strings, @NonNull int[] ints) {

    }
}
