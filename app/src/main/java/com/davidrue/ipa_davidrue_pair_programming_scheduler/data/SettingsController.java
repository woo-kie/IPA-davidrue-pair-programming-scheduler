package com.davidrue.ipa_davidrue_pair_programming_scheduler.data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class SettingsController {

  public Activity activity;

  public SettingsController(Activity activity){
    this.activity = activity;
  }
  public void writeMeetingTitleToSharedPrefs(String title) {
    getEditor().putString("title", title).apply();
  }

  public void writeMeetingDescriptionToSharedPrefs(String description) {
    getEditor().putString("description", description).apply();
  }

  public void writeMeetingStartTimeToSharedPrefs(String startTime) {
    getEditor().putString("startTime", startTime).apply();
  }

  public void writeMeetingEndTimeToSharedPrefs(String endTime) {
    getEditor().putString("endTime", endTime).apply();
  }

  public String getMeetingTitleFromSharedPrefs() {
    return getSharedPrefs().getString("title", "Pair Programming Session");
  }

  public String getMeetingDescriptionFromSharedPrefs() {
    return getSharedPrefs().getString("description", "Hello, would you have time for a 1:1 session? I need some help :)");
  }

  public String getMeetingStartTimeFromSharedPrefs() {
    return getSharedPrefs().getString("startTime", "09:00");
  }

  public String getMeetingEndTimeFromSharedPrefs() {
    return getSharedPrefs().getString("endTime", "17:00");
  }

  public void clearSharedPrefs(){
    getEditor().clear().commit();
  }

  private SharedPreferences.Editor getEditor(){
    return activity.getSharedPreferences("MEETINGS", Context.MODE_PRIVATE).edit();
  }

  private SharedPreferences getSharedPrefs(){
    return activity.getSharedPreferences("MEETINGS", Context.MODE_PRIVATE);
  }
}
