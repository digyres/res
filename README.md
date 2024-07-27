# CameraHelper

Camera2 api를 사용하여 카메라를 제어하는 모듈. 

한 프로세스 내 여러 activity/service에서 한 카메라를 제어 할 수 있도록 singleton 적용.

한 프로세스 내 여러 activity/service에서 한 카메라의 image를 공유 가능.



#### 구조

>CameraHelper
>
>>CameraImageHelper.java
>>
>>Camera2 api를 사용하여 카메라 제어.
>
>>CameraImageListener.java
>>
>>CameraImageHelper에서 카메라 Image를 공유.
>
>>CameraImageInput.java
>>
>>CameraImageHelper를 사용하여 singleton으로 구성.
>
>>ImageInputConsumer.java
>>
>>CameraImageInput에서 activity/service에 카메라 Image를 공유.




#### example

1) ImageInputConsumer 선언 or implement
   ```
   public class MainActivity extends AppCompatActivity implements ImageInputConsumer {}
   ```
   or
   ```
   ImageInputConsumer imageInputConsumer = new ImageInputConsumer;
   ```
2) ImageInputConsumer 재정의
   ```
   imageInputConsumer = new ImageInputConsumer() {
            @Override
            public void onConsumerImage(Image frame) {
                ...
            }
        };
   ```
3) CameraImageInput 선언 및 초기화
   ```
   CameraImageInput cameraImageInput = new CameraImageInput;
   ```
   ```
   cameraImageInput[i] = CameraImageInput.getInstance(
                    this,
                    String.valueOf(i),
                    640,
                    480);

   if (cameraImageInput != null) {
       Logger.I(TAG, "addFrameListener");
       cameraImageInput.addFrameListener(imageInputConsumer);
   } else {
       Logger.I(TAG, "addFrameListener is null");
   }
   ```
4) Camera start/close
   ```
   cameraImageInput.start();
   ```

   ```
   if (cameraImageInput != null) {
      cameraImageInput.close();
      cameraImageInput = null;
   }
   ```
