package com.example.user.camera;


import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class SavingImageActivity extends AppCompatActivity {

  public static final String TAG_INPUT_NAME_DIALOG = "TAG_INPUT_NAME_DIALOG";

  private ImageView photoImageView;
  private Button saveButton;
  private Button cancelButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.a_saving_screen);
    getSupportActionBar().hide();

    photoImageView = (ImageView) findViewById(R.id.iv_photo);
    byte[] photo = getIntent().getByteArrayExtra(MainActivity.EXTRA_PHOTO);
    photoImageView.setImageBitmap(BitmapFactory.decodeByteArray(photo, 0, photo.length));

    saveButton = (Button) findViewById(R.id.b_save);
    saveButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        InputNameDialogFragment inputNameDF = new InputNameDialogFragment();
        inputNameDF.setOnNameChosenListener(new OnNameChosenListener() {
          @Override
          public String onNameChosen() {
            return null;
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
}
