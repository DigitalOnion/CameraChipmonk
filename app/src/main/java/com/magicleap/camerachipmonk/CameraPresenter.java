package com.magicleap.camerachipmonk;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;


public class CameraPresenter
        extends CameraDevice.StateCallback
        implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int MY_PERMISSION_REQUEST_CAMERA = 1234;

    private CameraView view;
    private CameraManager manager;

    private String selectedCameraId;

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
            return;
        }

        if(ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            manager.openCamera(selectedCameraId, this, null);
        }
    }

    private void openCameraWhenAllowed() {
        if (ContextCompat.checkSelfPermission(view.getActivity(),
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(view.getActivity(),
                    Manifest.permission.READ_CONTACTS)) {
                new AlertDialog.Builder(view.getContext())
                        .setMessage(R.string.camera_permission_rationale)
                        .setPositiveButton(android.R.string.yes, null)
                        .create();
            } else {
                ActivityCompat.requestPermissions(view.getActivity(),
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSION_REQUEST_CAMERA);
            }
        } else {
            openCamera(0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
        case MY_PERMISSION_REQUEST_CAMERA:
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera(0);
                return;
            }
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
