package com.digy.CameraHelper;

import android.graphics.Bitmap;
import android.media.Image;


public interface CameraImageListener {
    void onCameraFrame(Image frame);
}
