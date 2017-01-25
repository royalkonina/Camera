package com.example.user.camera;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
  public static final String PHOTO_TEMP_FILENAME = "temp_photo.jpg";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.a_camera);
    getSupportActionBar().hide();

    camera = getCameraInstance();
    configureCamera();
    cameraPreview = new CameraPreview(this, camera);

    preview = (FrameLayout) findViewById(R.id.camera_preview);
    preview.addView(cameraPreview);

    captureButton = (ImageButton) findViewById(R.id.b_capture);
    captureButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        camera.takePicture(null, null, new Camera.PictureCallback() {
          @Override
          public void onPictureTaken(byte[] picture, Camera camera) {
            saveTempPhoto(picture);
            Intent intent = new Intent(MainActivity.this, SavingImageActivity.class);
            startActivity(intent);
          }
        });
      }
    });

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
      c = Camera.open();
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
    super.onDestroy();
  }

}
