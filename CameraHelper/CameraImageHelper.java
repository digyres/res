package com.digy.CameraHelper;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;


import com.digy.util.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class CameraImageHelper {

    public static final String TAG = "CamHelper";

    private CameraImageListener _imageListener;

    Context _context;
    String _cameraId = "";
    CameraManager mCameraManager;
    CameraDevice mCamera;
    CameraCaptureSession mCaptureSession;
    CaptureRequest mCaptureRequest;
    CaptureRequest.Builder mRequestBuilder;
    ImageReader mImageReader;
    int _width = 640, _height = 480;

    /**
     * BackGroundThread for camera2setting
     */
    private HandlerThread backgroundThread = null;
    private Handler backgroundHandler = null;

    long _frameCounter = 0;

    public CameraImageHelper(Context context, String cameraId, CameraImageListener listener, int width, int height) {

        this._context = context;
        this._imageListener = listener;
        this._cameraId = cameraId;
        this.mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        this._width = width;
        this._height = height;
        Logger.I(TAG, "CameraDevice.StateCallback::init, cameraId=" + this._cameraId);
    }

    public void setNewFrameListener(CameraImageListener listener) {
        this._imageListener = listener;
    }

    public void close() {

    }

    public String getCameraId() {
        return this._cameraId;
    }

    public CameraCaptureSession getCameraCaptureSession() {
        return this.mCaptureSession;
    }

    public CaptureRequest getCaptureRequest() {
        return this.mCaptureRequest;
    }

    public ImageReader getImageReader() {
        return this.mImageReader;
    }

    /************************************************
     * setup camera proc                            *
     ************************************************/
    CameraDevice.StateCallback mCameraDeviceCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            Logger.I(TAG, "CameraDevice.StateCallback::onOpened");
            mCamera = cameraDevice;
            createCaptureSession(cameraDevice);
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
//            Logger.I(TAG, "CameraDevice.StateCallback::onDisconnected");

            if (mCamera != null) mCamera.close();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            Logger.E(TAG, "CameraDevice.StateCallback::onError, code=" + i);
            if (mCamera != null) {
                mCamera.close(); // null point exception
                mCamera = null;
            }
            //Error 발생 시 카메라 재 연결 시도..
            openCamera();
        }
    };

    /************************************************
     * setup camera session                         *
     ************************************************/
    CameraCaptureSession.StateCallback mSesstionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
//            Logger.I(TAG, "CameraCaptureSession.StateCallback::onConfigured");
            mCaptureSession = cameraCaptureSession;
            try {
                mCaptureRequest = mRequestBuilder.build();
                mCaptureSession.setRepeatingBurst(Collections.singletonList(mCaptureRequest), mCaptrueCallback, null);

            } catch (Exception e) {
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
//            Logger.E(TAG, "CameraCaptureSession.StateCallback::onConfigureFailed");
        }
    };

    /************************************************
     * setup camera callback                        *
     ************************************************/
    CameraCaptureSession.CaptureCallback mCaptrueCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
//            Logger.I(TAG, "CameraCaptureSession.CaptureCallback::onCaptureProgressed");
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
//            Logger.I(TAG, "CameraCaptureSession.CaptureCallback::onCaptureCompleted");
        }
    };

    public void openCamera() {
        String camIdx = getCameraId();
        Logger.I(TAG, "openCamera(), camera id=" + camIdx);
        startBackgroundThread();

        try {
            if (ActivityCompat.checkSelfPermission(_context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Logger.E(TAG, "Camera permission is not granted. Please check the camera permission.");
                return;
            }
            if (camIdx != null && mCameraDeviceCallback != null) {
                mCameraManager.openCamera(camIdx, mCameraDeviceCallback, backgroundHandler);
                Logger.I(TAG, "openCamera():OK");
            } else {
                Logger.I(TAG, "openCamera(): Check Camera idx..");
            }
        } catch (CameraAccessException e) {
            Logger.E(TAG, "CameraAccessException:" + e.getMessage());
        } catch (Exception e) {
            /**
             * error 발생 시 카메라 인덱스가 사라지는 경우가 발생하여 연결 될 때 까지 openCamera 시도
             * 차후 cameramanager.getcameraidlist!=0 으로 loop 돌리는것 고려 필요.
             */
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            openCamera();
            Logger.E(TAG, "Unknown Exception:" + e.toString());
        }
    }

    public void stopCamera() {
        Logger.I(TAG, "stopCamera");
        try {
            if (mCaptureSession != null) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (mCamera != null) {
                mCamera.close();
                mCamera = null;
            }

            if (mImageReader != null) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (Exception e) {
            Logger.E(TAG, "Unknown Exception: " + e.getMessage());
        }
        stopBackgroundThread();
    }

    public void createCaptureSession(CameraDevice cameraDevice) {
        //
        try {
            ArrayList<Surface> targetSurfaces = new ArrayList<>();

            mImageReader = ImageReader.newInstance(_width, _height, ImageFormat.YUV_420_888, 2);
            mImageReader.setOnImageAvailableListener(mImageListener, null);

            mRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            targetSurfaces.add(mImageReader.getSurface());
            mRequestBuilder.addTarget(mImageReader.getSurface());

            mRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            mRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            Logger.I(TAG, "Test builder Set!!");

            cameraDevice.createCaptureSession(targetSurfaces, mSesstionStateCallback, backgroundHandler);

        } catch (Exception e) {
            Logger.E(TAG, "createCaptureSession():Exception" + e);
        }
    }

    ImageReader.OnImageAvailableListener mImageListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();
            if (image == null) {
                Logger.W(TAG, "imageReader::acquireLatestImage returns null, cameraId=" + getCameraId() + " ,frame=" + _frameCounter);
                return;
            }

            if (_imageListener != null) {
                _imageListener.onCameraFrame(image);
            }
            image.close();
        }
    };

    private void startBackgroundThread() {
        stopBackgroundThread();
        backgroundThread = new HandlerThread("cameraHandler");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (backgroundThread == null)
            return;
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

}