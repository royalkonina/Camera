package com.example.user.camera;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

  private Camera camera;
  private CameraPreview cameraPreview;
  private FrameLayout preview;
  private ImageButton captureButton;
  public static final String EXTRA_PHOTO = "EXTRA_PHOTO";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.a_camera);
    getSupportActionBar().hide();

    camera = getCameraInstance();
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
            Intent intent = new Intent(MainActivity.this, SavingImageActivity.class);
            intent.putExtra(EXTRA_PHOTO, picture);
            startActivity(intent);
          }
        });
      }
    });

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
