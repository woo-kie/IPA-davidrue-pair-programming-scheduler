package com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.R;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.data.SignInController;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * BaseActivity is an abstract class that extends AppCompatActivity and provides common functionality
 * to most activities in the application. It handles silent sign-ins, checks for network connectivity,
 * and displays an alert dialog to guide the user to resolve any connectivity issues.
 */
public abstract class BaseActivity extends AppCompatActivity {

  private SignInController signInController;

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
    signInController.silentSignIn(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    checkForConnectivity();
    signInController.silentSignIn(this);
  }

  /**
   * Checks for network connectivity and displays an alert dialog if there are any issues.
   * The dialog allows users to directly open their settings to fix the issue.
   */
  private void checkForConnectivity() {
    if (!signInController.isNetworkAvailable(this)) {
      new MaterialAlertDialogBuilder(this)
          .setTitle(getResources().getString(R.string.title_network_issues))
          .setMessage(getResources().getString(R.string.supporting_text))
          .setNeutralButton(getResources().getString(R.string.back),
              (dialog, which) -> dialog.dismiss())
          .setPositiveButton(getResources().getString(R.string.settings),
              (dialog, which) -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)))
          .show();
    }
  }
}
