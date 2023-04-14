package com.davidrue.ipa_davidrue_pair_programming_scheduler.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.R;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.data.MeetingSlotFinder.Duration;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.data.SettingsController;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.data.SignInController;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.BaseActivity;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.ToolBarHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

 /** An activity that allows users to modify the settings related to scheduling pair programming meetings.
     * Users can configure the meeting title, description, start and end times, duration, and choose
     * whether to open the meeting in Google Calendar.
    */
public class SettingsActivity extends BaseActivity {
  private TextInputEditText mMeetingTitleInput;
  private TextInputEditText mMeetingDescriptionInput;

  private MaterialAutoCompleteTextView mStartTimeDropdown;
  private MaterialAutoCompleteTextView mEndTimeDropdown;

  private SettingsController settingsController;

  private MaterialButtonToggleGroup mDurationToggleGroup;
  private MaterialButton mButtonFifteen;
  private MaterialButton mButtonThirty;
  private MaterialButton mButtonSixty;
  private MaterialButton mButtonNinety;

  private SwitchMaterial mOpenInGoogleCalendarSwitch;

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
    mDurationToggleGroup = findViewById(R.id.duration_toggle_group);
    mButtonFifteen = findViewById(R.id.fifteenButton);
    mButtonThirty = findViewById(R.id.thirtyButton);
    mButtonSixty = findViewById(R.id.hourButton);
    mButtonNinety = findViewById(R.id.ninetyButton);

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
    setDurationToggleSelection(Duration.valueOf(settingsController.getMeetingDurationFromSharedPrefs()));

    mOpenInGoogleCalendarSwitch = findViewById(R.id.switch_open_in_google_calendar);
    mOpenInGoogleCalendarSwitch.setChecked(settingsController.getOpenInGoogleCalendarFromSharedPrefs());

    mOpenInGoogleCalendarSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
      settingsController.writeOpenInGoogleCalendarToSharedPrefs(isChecked);
    });

    logout.setOnClickListener(l -> {
      settingsController.clearSharedPrefs();
      SignInController.getInstance().logout(this);
    });

    mDurationToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
      if (isChecked) {
        switch (checkedId) {
          case R.id.fifteenButton:
            settingsController.writeMeetingDurationToSharedPrefs(Duration.FIFTEEN);
            break;
          case R.id.thirtyButton:
            settingsController.writeMeetingDurationToSharedPrefs(Duration.THIRTY);
            break;
          case R.id.hourButton:
            settingsController.writeMeetingDurationToSharedPrefs(Duration.HOUR);
            break;
          case R.id.ninetyButton:
            settingsController.writeMeetingDurationToSharedPrefs(Duration.NINETY);
            break;
        }
      }
    });

  }

  @Override
  protected void onPause() {
    super.onPause();
    if(SignInController.isSignedIn(this)){
      saveInputs();
      saveDurationToggleGroupState();
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    if(SignInController.isSignedIn(this)){
      saveInputs();
      saveDurationToggleGroupState();
    }
  }

  private void setDurationToggleSelection(Duration duration) {
    switch (duration) {
      case FIFTEEN:
        mDurationToggleGroup.check(R.id.fifteenButton);
        break;
      case THIRTY:
        mDurationToggleGroup.check(R.id.thirtyButton);
        break;
      case HOUR:
        mDurationToggleGroup.check(R.id.hourButton);
        break;
      case NINETY:
        mDurationToggleGroup.check(R.id.ninetyButton);
        break;
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

  private void saveDurationToggleGroupState() {
    int checkedId = mDurationToggleGroup.getCheckedButtonId();
    switch (checkedId) {
      case R.id.fifteenButton:
        settingsController.writeMeetingDurationToSharedPrefs(Duration.FIFTEEN);
        break;
      case R.id.thirtyButton:
        settingsController.writeMeetingDurationToSharedPrefs(Duration.THIRTY);
        break;
      case R.id.hourButton:
        settingsController.writeMeetingDurationToSharedPrefs(Duration.HOUR);
        break;
      case R.id.ninetyButton:
        settingsController.writeMeetingDurationToSharedPrefs(Duration.NINETY);
        break;
    }
  }

}