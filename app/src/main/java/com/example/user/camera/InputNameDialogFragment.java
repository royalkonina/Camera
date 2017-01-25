package com.example.user.camera;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class InputNameDialogFragment extends DialogFragment {
  private OnNameChosenListener onNameChosenListener;
  private Button nameChosenButton;
  private Button nameCanceledButton;
  private EditText fileNameEditText;

  public void setOnNameChosenListener(OnNameChosenListener onNameChosenListener) {
    this.onNameChosenListener = onNameChosenListener;
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.df_input_name, container);
    nameCanceledButton = (Button) v.findViewById(R.id.b_name_canceled);
    nameChosenButton = (Button) v.findViewById(R.id.b_name_chosen);
    fileNameEditText = (EditText) v.findViewById(R.id.et_filename);
    nameChosenButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (fileNameEditText.length() > 0) {
          onNameChosenListener.onNameChosen(fileNameEditText.getText().toString() + ".jpg");
        } else {
          fileNameEditText.setError("Filename can't be empty");
        }
      }
    });
    nameCanceledButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        dismiss();
      }
    });
    return v;
  }

}
