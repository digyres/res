package com.digy.CameraHelper;

import android.graphics.Bitmap;
import android.media.Image;


public interface ImageInputConsumer {
    void onConsumerImage(Image frame);
}
