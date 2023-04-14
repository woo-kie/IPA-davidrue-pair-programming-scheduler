package com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers;

import android.app.Activity;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * ErrorDialog is a utility class that provides a method to show an error dialog with a given
 * exception message. This can be used to display errors consistently across the application.
 */
public class ErrorDialog {

  /**
   * Shows an error dialog with the provided exception message.
   *
   * @param exception the exception containing the error message to be displayed
   * @param activity  the activity in which the error dialog should be displayed
   */
  public static void showError(Exception exception, Activity activity){
    new MaterialAlertDialogBuilder(activity)
        .setTitle(activity.getResources().getString(R.string.title_error_dialog))
        .setMessage(exception.getMessage())
        .setPositiveButton(activity.getResources().getString(R.string.back),
            (dialog, which) -> dialog.dismiss())
        .show();
  }

}
