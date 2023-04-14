package com.davidrue.ipa_davidrue_pair_programming_scheduler.data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.data.MeetingSlotFinder.Duration;

/**
 * The SettingsController class is responsible for managing the meeting-related user-settings stored
 * in SharedPreferences. This includes reading and writing settings such as meeting
 * title_network_issues, description, start and end times, duration, and the option to open a
 * selected meeting in Google Calendar.
 */
public class SettingsController {

  private final Activity activity;

  public SettingsController(Activity activity) {
    this.activity = activity;
  }

  // Read Settings

  /**
   * Get the meeting title_network_issues from SharedPreferences.
   *
   * @return The meeting title_network_issues as a String.
   */
  public String getMeetingTitleFromSharedPrefs() {
    return getSharedPrefs().getString("title_network_issues", "Pair Programming Session");
  }

  /**
   * Get the meeting description from SharedPreferences.
   *
   * @return The meeting description as a String.
   */
  public String getMeetingDescriptionFromSharedPrefs() {
    return getSharedPrefs().getString("description",
        "Hello, would you have time for a 1:1 session? :)");
  }

  /**
   * Get the meeting start time from SharedPreferences.
   *
   * @return The meeting start time as a String.
   */
  public String getMeetingStartTimeFromSharedPrefs() {
    return getSharedPrefs().getString("startTime", "09:00");
  }

  /**
   * Get the meeting end time from SharedPreferences.
   *
   * @return The meeting end time as a String.
   */
  public String getMeetingEndTimeFromSharedPrefs() {
    return getSharedPrefs().getString("endTime", "17:00");
  }

  /**
   * Get the meeting duration from SharedPreferences.
   *
   * @return The meeting duration as a String.
   */
  public String getMeetingDurationFromSharedPrefs() {
    return getSharedPrefs().getString("duration", Duration.THIRTY.name());
  }

  /**
   * Get the 'open in Google Calendar' preference from SharedPreferences.
   *
   * @return true if the preference is set, false otherwise.
   */
  public boolean getOpenInGoogleCalendarFromSharedPrefs() {
    return getSharedPrefs().getBoolean("open_in_google_calendar", false);
  }

  // Write Settings

  /**
   * Write the meeting title_network_issues to SharedPreferences.
   *
   * @param title The meeting title_network_issues as a String.
   */
  public void writeMeetingTitleToSharedPrefs(String title) {
    getEditor().putString("title_network_issues", title).apply();
  }

  /**
   * Write the meeting description to SharedPreferences.
   *
   * @param description The meeting description as a String.
   */
  public void writeMeetingDescriptionToSharedPrefs(String description) {
    getEditor().putString("description", description).apply();
  }

  /**
   * Write the meeting start time to SharedPreferences.
   *
   * @param startTime The meeting start time as a String.
   */
  public void writeMeetingStartTimeToSharedPrefs(String startTime) {
    getEditor().putString("startTime", startTime).apply();
  }

  /**
   * Write the meeting end time to SharedPreferences.
   *
   * @param endTime The meeting end time as a String.
   */
  public void writeMeetingEndTimeToSharedPrefs(String endTime) {
    getEditor().putString("endTime", endTime).apply();
  }

  /**
   * Write the meeting duration to SharedPreferences.
   *
   * @param duration The meeting duration as a Duration enum value.
   */
  public void writeMeetingDurationToSharedPrefs(Duration duration) {
    getEditor().putString("duration", duration.name()).apply();
  }

  /**
   * Write the 'open in Google Calendar' preference to SharedPreferences.
   *
   * @param openInGoogleCalendar The preference value as a boolean.
   */
  public void writeOpenInGoogleCalendarToSharedPrefs(boolean openInGoogleCalendar) {
    getEditor().putBoolean("open_in_google_calendar", openInGoogleCalendar).apply();
  }

  // Utility methods

  /**
   * Clear all values in SharedPreferences.
   */
  public void clearSharedPrefs() {
    getEditor().clear().commit();
  }

  /**
   * Get a SharedPreferences.Editor instance.
   *
   * @return A SharedPreferences.Editor instance for editing user settings (preferences).
   */
  private SharedPreferences.Editor getEditor() {
    return activity.getSharedPreferences("MEETINGS", Context.MODE_PRIVATE).edit();
  }

  /**
   * Get a SharedPreferences instance.
   *
   * @return A SharedPreferences instance for retrieving user settings (preferences).
   */
  private SharedPreferences getSharedPrefs() {
    return activity.getSharedPreferences("MEETINGS", Context.MODE_PRIVATE);
  }
}