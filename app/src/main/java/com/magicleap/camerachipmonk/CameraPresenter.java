package com.magicleap.camerachipmonk;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Size;
import android.view.Surface;

import java.util.ArrayList;
import java.util.List;

public class CameraPresenter extends CameraDevice.StateCallback {
    public static final int MY_PERMISSION_REQUEST_CAMERA = 1;

    public static final int ERROR_ID_INVALID_SIZE = 0;
    public static final int ERROR_ID_NO_ACCESS_TO_CAMERA = 1;
    public static final int ERROR_ID_FAILED_CAMERA_CONFIGURATION = 2;

    private static final int MAX_PREVIEW_WIDTH = 1920;      // max preview width - Camera2 API
    private static final int MAX_PREVIEW_HEIGHT = 1080;     // max preview height - Camera2 API
    private static final int MAX_IMAGES = 2;

    private MainView mainView;

    private String selectedCameraId = null;
    private CameraCharacteristics selectedCameraCharacteristics = null;
    private StreamConfigurationMap selectedConfMap = null;
    private CameraCaptureSession selectedSession = null;
    private Size[] selectedSizes = null;
    private Surface previewSurface = null;
    private ImageReader imageReader = null;
    private CaptureRequest.Builder  previewRequestBuilder = null;

    public CameraPresenter(MainView mainView) {
        this.mainView = mainView;
    }

    private static final String noSelectedCameraMessage = "There is no Selected Camera. Try OpenCamera(Integer cameraFacing).";

    /**
     * openCamera
     * chooses among the available cameras the one matching the CameraFacing (front, back...)
     * the result is a CameraDevice received in the StateCallback's onOpened.
     * The method handles the request for CAMERA permission in case the user has not granted it
     */
    public void openCamera(final Activity activity, final Integer cameraFacing) {
        CameraManager manager;
        manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);

        try {
            // Check for Camera permission
            if(ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                // chooses among all available cameras
                for(String cameraId : manager.getCameraIdList()) {
                    CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                    Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                    if(facing!=null && facing.equals(cameraFacing)) {
                        // configure the camera
                        selectedCameraId = cameraId;
                        selectedCameraCharacteristics = characteristics;
                        selectedConfMap = characteristics
                                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                        selectedSizes = selectedConfMap.getOutputSizes(ImageFormat.JPEG);
                    }
                }
                if(selectedCameraId == null) {
                    throw new NullPointerException(noSelectedCameraMessage);
                } else {
                    // openCamera will take the selectedCameraId and get the corresponding
                    // CameraDevice to the onOpened callback
                    manager.openCamera(selectedCameraId, this, null);
                }
            }
            else if(ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.CAMERA)) {
                // no camera permissions, handle it
                new AlertDialog.Builder(activity)
                        .setMessage(R.string.camera_permission_rationale)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                requestCameraPermission(activity);
                            }
                        })
                        .create().show();
            } else {
                requestCameraPermission(activity);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void requestCameraPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[] {Manifest.permission.CAMERA},
                MY_PERMISSION_REQUEST_CAMERA);
    }


    /**
     * onOpened
     * @param cameraDevice
     *
     * onOpened is a method from CameraDevice.StateCallback. it delivers the cameraDevice
     * once it is opened.
     * in the example we do:
     *   - choose the size
     *   - instantiate an image reader with the size and type (JPEG)
     *   - obtain a surface for photo shooting from the Image Reader
     *   - obtain the surface from the TextureView for preview
     *   - create a CaptureSession. The result from it will be delivered
     *     at the CameraCaptureSession.StateCallback
     */
    @Override
    public void onOpened(@NonNull CameraDevice cameraDevice) {
        // Select appropriate size from selectedSizes.
        Size chosenSize = null;
        for(Size size: selectedSizes) {
            if(size.getHeight() >= 600 && size.getHeight() <= 720) {
                chosenSize = size;
                break;
            }
        }
        if(chosenSize == null) {
            mainView.onCameraError(ERROR_ID_INVALID_SIZE);
            return;
        }

        imageReader = ImageReader.newInstance(
                chosenSize.getWidth(), chosenSize.getHeight(),
                ImageFormat.JPEG, MAX_IMAGES);
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader imageReader) {
                Image image = imageReader.acquireLatestImage();
                CameraPresenter.this.mainView.onImageReady(image);
            }
        }, null);

        SurfaceTexture surfaceTexture = mainView.getTextureView().getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(chosenSize.getWidth(), chosenSize.getHeight());
        previewSurface = new Surface(surfaceTexture);

        List<Surface> surfaces = new ArrayList<>();
        surfaces.add(imageReader.getSurface());
        surfaces.add(previewSurface);

        try {
            cameraDevice.createCaptureSession(surfaces, new CaptureSessionCallback(), null);
        } catch (CameraAccessException e) {
            mainView.onCameraError(ERROR_ID_NO_ACCESS_TO_CAMERA);
        }
    }

    @Override
    public void onDisconnected(@NonNull CameraDevice cameraDevice) { }

    @Override
    public void onError(@NonNull CameraDevice cameraDevice, int i) { }

    private class CaptureSessionCallback extends CameraCaptureSession.StateCallback {

        /**
         * onConfigured
         * @param cameraCaptureSession
         *
         * receives the CameraCaptureSession to be configured ready to receive requests by:
         *  - create the Preview RequestBuilder
         *  - launch the request to the camera session. This starts showing the video on screen
         */
        @Override
        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
            selectedSession = cameraCaptureSession;
            try {
                // Request for Preview - it does not need a CameraCaptureSession.CaptureCallback since the stream is only shown
                previewRequestBuilder = cameraCaptureSession.getDevice().createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                previewRequestBuilder.addTarget(previewSurface);
                previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                cameraCaptureSession.setRepeatingRequest(previewRequestBuilder.build(), null, null);

            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
            mainView.onCameraError(ERROR_ID_FAILED_CAMERA_CONFIGURATION);
        }
    }

    public void shoot() {
        try {
            // Lock Focus
            previewRequestBuilder = selectedSession.getDevice().createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(previewSurface);
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
            selectedSession.capture(previewRequestBuilder.build(),
                    new CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                                       @NonNull CaptureRequest request,
                                                       @NonNull TotalCaptureResult result) {
                            try {
                                CaptureRequest.Builder  captureRequestBuilder =
                                        selectedSession.getDevice()
                                                .createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                                captureRequestBuilder.addTarget(imageReader.getSurface());
                                captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                                selectedSession.stopRepeating();
                                selectedSession.abortCaptures();
                                selectedSession.capture(captureRequestBuilder.build(),
                                        new CameraCaptureSession.CaptureCallback() {
                                            @Override
                                            public void onCaptureCompleted(
                                                    @NonNull CameraCaptureSession session,
                                                    @NonNull CaptureRequest request,
                                                    @NonNull TotalCaptureResult result) {
                                                //super.onCaptureCompleted(session, request, result);
                                                // Unlock Focus when the capture is completed
                                                try {
                                                    previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
                                                    selectedSession.capture(previewRequestBuilder.build(), null, null);

                                                    // restart the preview
                                                    selectedSession.setRepeatingRequest(previewRequestBuilder.build(), null, null);
                                                } catch (CameraAccessException e) {
                                                    e.printStackTrace();
                                                }

                                            }
                                        },
                                        null);

                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }

                        }
                    },
                    null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }



}
