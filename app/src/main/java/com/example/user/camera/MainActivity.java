package com.example.user.camera;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  private Camera camera;
  private CameraPreview cameraPreview;
  private FrameLayout preview;
  private ImageButton captureButton;
  private ImageButton swapCameraButton;
  private ImageButton flashButton;
  private OrientationEventListener orientationEventListener;

  public static final String PHOTO_TEMP_FILENAME = "temp_photo.jpg";
  private int cameraID = 0;
  private String flashMode = Camera.Parameters.FLASH_MODE_OFF;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.a_camera);
    getSupportActionBar().hide();

    flashButton = (ImageButton) findViewById(R.id.b_flash);
    captureButton = (ImageButton) findViewById(R.id.b_capture);
    preview = (FrameLayout) findViewById(R.id.camera_preview);
    swapCameraButton = (ImageButton) findViewById(R.id.b_swap_camera);

    setupCamera();

    captureButton.setOnClickListener(capturePictureListener);
    swapCameraButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        preview.removeView(cameraPreview);
        cameraPreview = null;
        releaseCamera();
        cameraID = (cameraID + 1) % 2; //swaps between 0 and 1 camera
        setupCamera();
      }
    });

    flashButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        switch (flashMode) {
          case Camera.Parameters.FLASH_MODE_OFF:
            flashButton.setImageResource(R.drawable.camera_flash_auto_32);
            flashMode = Camera.Parameters.FLASH_MODE_AUTO;
            break;
          case Camera.Parameters.FLASH_MODE_AUTO:
            flashMode = Camera.Parameters.FLASH_MODE_ON;
            flashButton.setImageResource(R.drawable.camera_flash_on_32);
            break;
          case Camera.Parameters.FLASH_MODE_ON:
            flashButton.setImageResource(R.drawable.camera_flash_off_32);
            flashMode = Camera.Parameters.FLASH_MODE_OFF;
            break;
        }
        Camera.Parameters params = camera.getParameters();
        params.setFlashMode(flashMode);
        camera.setParameters(params);
      }
    });

    orientationEventListener = new OrientationChangeListener(this);
    orientationEventListener.enable();
  }

  View.OnClickListener capturePictureListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      capturePhoto();
    }
  };

  Camera.PictureCallback cameraCallback = new Camera.PictureCallback() {
    @Override
    public void onPictureTaken(byte[] picture, Camera camera) {
      saveTempPhoto(picture);
      Intent intent = new Intent(MainActivity.this, SavingImageActivity.class);
      startActivity(intent);
    }
  };

  private void capturePhoto() {
    camera.autoFocus(new Camera.AutoFocusCallback() {
      @Override
      public void onAutoFocus(boolean b, Camera camera) {
        camera.takePicture(null, null, cameraCallback);
      }
    });
  }

  private void setupCamera() {
    camera = getCameraInstance();
    configureCamera();
    cameraPreview = new CameraPreview(this, camera);
    preview.addView(cameraPreview);
    flashButton.setEnabled(cameraID == 0);
    if (Camera.getNumberOfCameras() < 2) {
      swapCameraButton.setEnabled(false);
    }
  }

  private void configureCamera() {
    Camera.Parameters parameters = camera.getParameters();
    List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
    Camera.Size size = sizes.get(0);
    for (int i = 0; i < sizes.size(); i++) {
      if (sizes.get(i).width > size.width)
        size = sizes.get(i);
    }
    parameters.setPictureSize(size.width, size.height);
    parameters.setJpegQuality(100);
    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
    camera.setParameters(parameters);
  }

  private void saveTempPhoto(byte[] picture) {
    File photoFile = new File(Environment.getExternalStorageDirectory(), PHOTO_TEMP_FILENAME);
    if (photoFile.exists()) {
      photoFile.delete();
    }
    try {
      FileOutputStream fos = new FileOutputStream(photoFile.getPath());
      fos.write(picture);
      fos.close();
    } catch (java.io.IOException e) {
      Log.e("saveTempPhoto", "Exception in saveTempPhoto", e);
    }
  }


  private Camera getCameraInstance() {
    Camera c = null;
    try {
      c = Camera.open(cameraID);
    } catch (Exception e) {
      // Camera is not available (in use or does not exist)
    }
    return c;
  }

  private void releaseCamera() {
    if (camera != null) {
      camera.release();
      camera = null;
    }
  }

  @Override
  protected void onDestroy() {
    releaseCamera();
    orientationEventListener.disable();
    orientationEventListener = null;
    super.onDestroy();
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    int action = event.getAction();
    int keyCode = event.getKeyCode();
    switch (keyCode) {
      case KeyEvent.KEYCODE_VOLUME_UP:
        if (action == KeyEvent.ACTION_DOWN) {
          capturePictureListener.onClick(captureButton);
        }
        return true;
      case KeyEvent.KEYCODE_VOLUME_DOWN:
        if (action == KeyEvent.ACTION_DOWN) {
          capturePictureListener.onClick(captureButton);
        }
        return true;
      default:
        return super.dispatchKeyEvent(event);
    }
  }

  class OrientationChangeListener extends OrientationEventListener {

    public OrientationChangeListener(Context context) {
      super(context);
    }

    public OrientationChangeListener(Context context, int rate) {
      super(context, rate);
    }

    @Override
    public void onOrientationChanged(int orientation) {
      captureButton.setRotation(orientation);
      swapCameraButton.setRotation(orientation);
      flashButton.setRotation(orientation);
    }
  }
}
