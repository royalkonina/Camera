package com.example.user.camera;


import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{

  private Camera camera;
  private SurfaceHolder surfaceHolder;

  public CameraPreview(Context context, Camera camera) {
    super(context);
    this.camera = camera;
    surfaceHolder = getHolder();
    surfaceHolder.addCallback(this);
  }


  @Override
  public void surfaceCreated(SurfaceHolder surfaceHolder) {
    try {
      camera.setPreviewDisplay(surfaceHolder);
      camera.startPreview();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int w, int h) {
    //needed if activity can change orientation
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    //need to release a camera in activity
  }
}
