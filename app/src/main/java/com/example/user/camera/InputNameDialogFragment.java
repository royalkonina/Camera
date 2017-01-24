package com.example.user.camera;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class InputNameDialogFragment extends DialogFragment {
  private OnNameChosenListener onNameChosenListener;

  public void setOnNameChosenListener(OnNameChosenListener onNameChosenListener) {
    this.onNameChosenListener = onNameChosenListener;
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.df_input_name, container);
    return v;
  }
}
