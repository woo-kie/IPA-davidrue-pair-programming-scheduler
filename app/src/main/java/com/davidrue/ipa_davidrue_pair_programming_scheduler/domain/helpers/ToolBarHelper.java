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

public class ToolBarHelper {

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
