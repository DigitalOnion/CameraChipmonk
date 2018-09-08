package com.magicleap.camerachipmonk;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;

import java.util.HashMap;

public class CameraPresenter extends CameraDevice.StateCallback  {

    private CameraView view = null;
    private CameraManager manager;

    String selectedCameraId = null;

    public CameraPresenter(CameraView view) {
        this.view = view;
        Context context = view.getContext();
        manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    public void openCamera(Integer cameraFacing) {

        try {
            for(String cameraId : manager.getCameraIdList()) {

                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                int facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if(facing == cameraFacing) {
                    selectedCameraId = cameraId;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        if(ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if(ActivityCompat.shouldShowRequestPermissionRationale( view.getContext(),
                    Manifest.permission.CAMERA))
        } else {
            manager.openCamera(selectedCameraId, this, null);
        }
    }

    @Override
    public void onOpened(@NonNull CameraDevice cameraDevice) {

    }

    @Override
    public void onDisconnected(@NonNull CameraDevice cameraDevice) {

    }

    @Override
    public void onError(@NonNull CameraDevice cameraDevice, int i) {

    }



}
