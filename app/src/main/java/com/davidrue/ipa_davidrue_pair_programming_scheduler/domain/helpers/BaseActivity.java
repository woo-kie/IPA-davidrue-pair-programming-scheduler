package com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.R;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.data.SignInController;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public abstract class BaseActivity extends AppCompatActivity {

  SignInController signInController;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    signInController = SignInController.getInstance();
    signInController.initialize(this);
  }

  @Override
  protected void onStart() {
    super.onStart();
    checkForConnectivity();
    signInController.recursiveLogin(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    checkForConnectivity();
    signInController.recursiveLogin(this);
  }

  private void checkForConnectivity() {
    if (!signInController.isNetworkAvailable(this)) {
      new MaterialAlertDialogBuilder(this)
          .setTitle(getResources().getString(R.string.title))
          .setMessage(getResources().getString(R.string.supporting_text))
          .setNeutralButton(getResources().getString(R.string.back),
              (dialog, which) -> dialog.dismiss())
          .setPositiveButton(getResources().getString(R.string.settings),
              (dialog, which) -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)))
          .show();
    }
  }
}
