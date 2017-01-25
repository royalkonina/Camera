package com.example.user.camera;


import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class SavingImageActivity extends AppCompatActivity {

  public static final String TAG_INPUT_NAME_DIALOG = "TAG_INPUT_NAME_DIALOG";
  public static final File PHOTO_DIRECTORY = Environment.getExternalStorageDirectory();

  private ImageView photoImageView;
  private Button saveButton;
  private Button cancelButton;
  private byte[] photo;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.a_saving_screen);
    getSupportActionBar().hide();

    photo = restorePhotoFromFile(MainActivity.PHOTO_TEMP_FILENAME);
    photoImageView = (ImageView) findViewById(R.id.iv_photo);

    setImageToBitmap();

    saveButton = (Button) findViewById(R.id.b_save);
    saveButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        InputNameDialogFragment inputNameDF = new InputNameDialogFragment();
        inputNameDF.setOnNameChosenListener(new OnNameChosenListener() {
          @Override
          public void onNameChosen(String name) {
            savePhotoToFile(name);
            notifyGallery(name);
            finish();
          }
        });
        inputNameDF.show(getSupportFragmentManager(), TAG_INPUT_NAME_DIALOG);
      }
    });

    cancelButton = (Button) findViewById(R.id.b_cancel);
    cancelButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        SavingImageActivity.this.finish();
      }
    });
  }

  private void notifyGallery(String filename) {
    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
    File f = new File(Environment.getExternalStorageDirectory(), filename);
    Uri contentUri = Uri.fromFile(f);
    mediaScanIntent.setData(contentUri);
    getApplicationContext().sendBroadcast(mediaScanIntent);
  }

  private void setImageToBitmap() {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeByteArray(photo, 0, photo.length, options);
    options.inJustDecodeBounds = false;
    Display display = getWindowManager().getDefaultDisplay();
    options.inSampleSize = calculateInSampleSize(options, display.getWidth(), display.getHeight());
    Log.d("display", String.valueOf(display.getWidth()));
    Log.d("size", String.valueOf(options.inSampleSize));
    photoImageView.setImageBitmap(BitmapFactory.decodeByteArray(photo, 0, photo.length, options));
  }

  private byte[] restorePhotoFromFile(String filename) {
    File photoFile = new File(PHOTO_DIRECTORY, filename);
    byte[] picture = new byte[(int) photoFile.length()];
    try {
      FileInputStream fis = new FileInputStream(photoFile);
      fis.read(picture);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return picture;
  }

  private void savePhotoToFile(String filename) {
    File photoFile = new File(PHOTO_DIRECTORY, filename);
    if (photoFile.exists()) {
      photoFile.delete();
    }
    try {
      FileOutputStream fos = new FileOutputStream(photoFile.getPath());
      fos.write(photo);
      fos.close();
    } catch (java.io.IOException e) {
      Log.e("savePhotoToFile", "Exception in savePhotoToFile", e);
    }
  }

  public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

      final int halfHeight = height / 2;
      final int halfWidth = width / 2;
      while ((halfHeight / inSampleSize) >= reqHeight
              && (halfWidth / inSampleSize) >= reqWidth) {
        inSampleSize *= 2;
      }
    }

    return inSampleSize;
  }

  @Override
  protected void onDestroy() {
    File tempFile = new File(PHOTO_DIRECTORY, MainActivity.PHOTO_TEMP_FILENAME);
    if (tempFile.exists()) {
      tempFile.delete();
    }
    super.onDestroy();
  }
}
