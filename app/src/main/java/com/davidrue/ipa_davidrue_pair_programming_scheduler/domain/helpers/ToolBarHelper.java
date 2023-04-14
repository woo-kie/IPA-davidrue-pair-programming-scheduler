package com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.R;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.ui.SettingsActivity;
import java.util.ArrayList;

/**
 * The ToolBarHelper class is a utility class for setting up the toolbar in activities.
 * It helps to easily customize the toolbar's text, back button, and settings button visibility.
 */
public class ToolBarHelper {

  /**
   * Sets up the toolbar with the provided text, back button, and settings button visibility.
   *
   * @param activity      the activity in which the toolbar should be set up
   * @param text          the text to be displayed in the toolbar
   * @param showBack      a boolean indicating whether to show the back button
   * @param showSettings  a boolean indicating whether to show the settings button
   */
  public static void setUpToolbar(Activity activity, String text, boolean showBack, boolean showSettings){
    ImageView backButton = activity.findViewById(R.id.back_button);
    ImageView settingsButton = activity.findViewById(R.id.settings_button);
    TextView toolbarText = activity.findViewById(R.id.toolbar_text);

    toolbarText.setText(text);

    backButton.setVisibility(showBack ? View.VISIBLE : View.GONE);
    settingsButton.setVisibility(showSettings ? View.VISIBLE : View.GONE);
    backButton.setOnClickListener(back -> {
      activity.onBackPressed();
    });

    settingsButton.setOnClickListener(settings -> {
      Intent intent = new Intent(activity, SettingsActivity.class);
      activity.startActivity(intent);
    });
  }

}
