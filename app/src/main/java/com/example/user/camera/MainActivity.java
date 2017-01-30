package com.example.user.camera;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  public static final String PHOTO_TEMP_FILENAME = "temp_photo.jpg";
  public static final int MY_PERMISSIONS_REQUEST_CAMERA = 125;
  public static final int ROTATION_DELTA = 10;
  private static final int FOCUS_AREA_SIZE = 300;

  private ImageButton flashButton;
  private Camera camera;
  private CameraPreview cameraPreview;
  private FrameLayout preview;
  private ImageButton captureButton;
  private ImageButton swapCameraButton;
  private OrientationEventListener orientationEventListener;

  private int lastCheckedOrientation = getRequestedOrientation();
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

    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
    } else {
      setupCamera();
    }
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

    preview.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
          focusOnTouch(motionEvent);
        }
        return true;
      }
    });

    orientationEventListener = new OrientationChangeListener(this);
    orientationEventListener.enable();
  }

  private void focusOnTouch(MotionEvent event) {
    if (camera != null) {
      Camera.Parameters parameters = camera.getParameters();
      if (parameters.getMaxNumMeteringAreas() > 0) {
        Rect rect = calculateFocusArea(event.getX(), event.getY());
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        List<Camera.Area> meteringAreas = new ArrayList<>();
        meteringAreas.add(new Camera.Area(rect, 800));
        parameters.setFocusAreas(meteringAreas);
        camera.setParameters(parameters);
      }
      camera.autoFocus(new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean b, Camera camera) {
          //without that empty callback autoFocus doesn't work ?_?
          Log.d("autoFocusOnTap", String.valueOf(b));
        }
      });
    }
  }

  private Rect calculateFocusArea(float x, float y) {
    int left = (int) (x / preview.getWidth() * 2000 - 1000);
    int top = (int) (y / preview.getHeight() * 2000 - 1000);
    return new Rect(left, top, Math.min(left + FOCUS_AREA_SIZE, 1000), Math.min(top + FOCUS_AREA_SIZE, 1000));
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
    setCameraParameters();
    cameraPreview = new CameraPreview(this, camera);
    preview.addView(cameraPreview);
    flashButton.setEnabled(cameraID == 0);
    if (Camera.getNumberOfCameras() < 2) {
      swapCameraButton.setEnabled(false);
    }
  }

  private void setCameraParameters() {
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

    @Override
    public void onOrientationChanged(int orientation) {
      int newOrientation = (((orientation + 2 * ROTATION_DELTA) / 90) * 90) % 360; //rounding to nearest 0-90-180-270 values
      if (Math.abs(orientation - lastCheckedOrientation) >= 90 - ROTATION_DELTA && lastCheckedOrientation != newOrientation) {
        int rotation = -(newOrientation - 270); //that's because initial orientation is 270
        if (newOrientation - lastCheckedOrientation == 270) {
          rotation += 360;
        } else if (lastCheckedOrientation - newOrientation == 270) {
          rotation -= 360;
        }
        ObjectAnimator.ofFloat(captureButton, "rotation", (int) captureButton.getRotation(), rotation).setDuration(500).start();
        ObjectAnimator.ofFloat(swapCameraButton, "rotation", (int) captureButton.getRotation(), rotation).setDuration(500).start();
        ObjectAnimator.ofFloat(flashButton, "rotation", (int) captureButton.getRotation(), rotation).setDuration(500).start();
        lastCheckedOrientation = newOrientation;
      }
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
    switch (requestCode) {
      case MY_PERMISSIONS_REQUEST_CAMERA: {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          // permission was granted, yay!
          setupCamera();
        } else {
          // permission denied, boo!
          Toast.makeText(this, "Sorry, this permission is necessary", Toast.LENGTH_SHORT).show();
          finish();
        }
        return;
      }
    }
  }
}
