package com.digy.CameraHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;


import com.digy.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class CameraImageInput implements CameraImageListener {

    private static String TAG = "CameraImageInput";
    private Context _context;
    private String _cameraId;
    private boolean _initialized = false;
    private CameraImageHelper _cameraImageHelper;
    private List<ImageInputConsumer> _childsFrameListener = new ArrayList<>();
    private Map<Integer, ImageInputConsumer> _imageconsumer = new HashMap<Integer, ImageInputConsumer>();

    private static Map<String, CameraImageInput> _instance = new HashMap<String, CameraImageInput>();

    // size of input frame
    public static int _widthSize = 640;
    public static int _heightSize = 480;

    /**
     * @param context
     * @param cameraId 0 ~ 3
     * @return
     */
    public static CameraImageInput getInstance(Context context, String cameraId, String rotation) {
        //Logger.I(TAG, "CameraImageInput getInstance, cameraid=" + cameraId);
        if (Integer.parseInt(cameraId) < 0 || Integer.parseInt(cameraId) > 5) {
            Logger.E(TAG, "CameraId is out of range, please check cameraid :: " + cameraId);
            return null;
        }
        CameraImageInput input = _instance.get(cameraId);
        if (input == null) {
            CameraImageInput newInput = new CameraImageInput(context, cameraId, _widthSize, _heightSize);
            _instance.put(cameraId, newInput);
        }
        return _instance.get(cameraId);
    }

    public static CameraImageInput getInstance(Context context, String cameraId, int width, int height) {
        if (Integer.parseInt(cameraId) < 0 || Integer.parseInt(cameraId) > 5) {
            Logger.E(TAG, "CameraId is out of range, please check cameraid :: " + cameraId);
            return null;
        }
        CameraImageInput input = _instance.get(cameraId);
        if (input == null) {
            CameraImageInput newInput = new CameraImageInput(context, cameraId, width, height);
            _instance.put(cameraId, newInput);
        }
        return _instance.get(cameraId);
    }

    private CameraImageInput(Context context, String cameraId, int width, int height) {
        this._context = context;
        this._cameraId = cameraId;
        _cameraImageHelper = new CameraImageHelper(_context, cameraId, this, width, height);
        Logger.D(TAG, "CameraImageInput::init, cameraId=" + cameraId + ", w=" + width + ", h=" + height);
    }

    public void addFrameListener(ImageInputConsumer consumer) {
        Logger.D(TAG, "before addFrameListener..." + _childsFrameListener.size());
        _childsFrameListener.add(consumer);
        Logger.D(TAG, "after addFrameListener..." + _childsFrameListener.size());
    }

    public void start() {
        if (!_initialized) {
            this._cameraImageHelper.openCamera();
            Logger.D(TAG, "start...");
            _initialized = true;
        }
    }

    public void close() {
        if (_initialized) {
            if (_childsFrameListener.size() == 1) {
                _cameraImageHelper.stopCamera();
                _initialized = false;
            }
            if (_childsFrameListener.size() > 0) {
                _childsFrameListener.remove(_childsFrameListener.size() - 1);
            }
            Logger.D(TAG, "close..." + _childsFrameListener.size());
        }
    }

    @Override
    public void onCameraFrame(Image frame) {
        if (frame == null) {
            Logger.W(TAG, "onCameraFrame frame is null");
            return;
        }
        List<ImageInputConsumer> tempListener = new ArrayList<ImageInputConsumer>();
        tempListener.addAll(_childsFrameListener);

        for (ImageInputConsumer consumer : tempListener) {
            consumer.onConsumerImage(frame);
        }
    }
}