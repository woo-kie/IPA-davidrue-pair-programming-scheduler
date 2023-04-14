package com.davidrue.ipa_davidrue_pair_programming_scheduler.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.R;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.data.SettingsController;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.data.SignInController;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.BaseActivity;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.ToolBarHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

public class SettingsActivity extends BaseActivity {
  private TextInputEditText mMeetingTitleInput;
  private TextInputEditText mMeetingDescriptionInput;

  private MaterialAutoCompleteTextView mStartTimeDropdown;
  private MaterialAutoCompleteTextView mEndTimeDropdown;

  private SettingsController settingsController;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);
    settingsController = new SettingsController(this);
    ToolBarHelper.setUpToolbar(this, "Settings", true, false);

    MaterialButton logout = findViewById(R.id.logout);

    // Initialize the input fields
    mMeetingTitleInput = findViewById(R.id.meeting_title_input);
    mMeetingDescriptionInput = findViewById(R.id.meeting_description_input);

    // Set the initial values for the input fields
    mMeetingTitleInput.setText(settingsController.getMeetingTitleFromSharedPrefs());
    mMeetingDescriptionInput.setText(settingsController.getMeetingDescriptionFromSharedPrefs());

    mStartTimeDropdown = findViewById(R.id.start_time_dropdown);
    mEndTimeDropdown = findViewById(R.id.end_time_dropdown);

    // Set up the ArrayAdapter and populate the dropdowns
    ArrayAdapter<CharSequence> startTimeAdapter = ArrayAdapter.createFromResource(this, R.array.start_times, R.layout.dropdown_menu_item);
    startTimeAdapter.setDropDownViewResource(R.layout.dropdown_menu_item);
    mStartTimeDropdown.setAdapter(startTimeAdapter);

    ArrayAdapter<CharSequence> endTimeAdapter = ArrayAdapter.createFromResource(this, R.array.end_times, R.layout.dropdown_menu_item);
    endTimeAdapter.setDropDownViewResource(R.layout.dropdown_menu_item);
    mEndTimeDropdown.setAdapter(endTimeAdapter);

    // Set the initial values for the dropdowns
    mStartTimeDropdown.setText(settingsController.getMeetingStartTimeFromSharedPrefs(), false);
    mEndTimeDropdown.setText(settingsController.getMeetingEndTimeFromSharedPrefs(), false);

    logout.setOnClickListener(l -> {
      settingsController.clearSharedPrefs();
      SignInController.getInstance().logout(this);
    });

  }

  @Override
  protected void onPause() {
    super.onPause();
    if(SignInController.isSignedIn(this)){
      saveInputs();
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    if(SignInController.isSignedIn(this)){
      saveInputs();
    }
  }
  private void saveInputs() {
    // Save the input fields' values
    String meetingTitle = mMeetingTitleInput.getText().toString();
    String meetingDescription = mMeetingDescriptionInput.getText().toString();
    String meetingStartTime = mStartTimeDropdown.getText().toString();
    String meetingEndTime = mEndTimeDropdown.getText().toString();
    settingsController.writeMeetingTitleToSharedPrefs(meetingTitle);
    settingsController.writeMeetingDescriptionToSharedPrefs(meetingDescription);
    settingsController.writeMeetingStartTimeToSharedPrefs(meetingStartTime);
    settingsController.writeMeetingEndTimeToSharedPrefs(meetingEndTime);
  }

}