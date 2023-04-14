package com.davidrue.ipa_davidrue_pair_programming_scheduler.data;

import android.content.Intent;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.R;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.ErrorDialog;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.ui.SkillSearchActivity;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.ui.meetings.MeetingSlotsActivity;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.TimePeriod;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class MeetingSlotsController {

  private MeetingSlotsActivity meetingSlotsActivity;

  private SettingsController settingsController;
  private String expert;

  public void initialize(MeetingSlotsActivity activity){
    settingsController = new SettingsController(activity);
    expert = activity.getIntent().getStringExtra("EXPERT");
    meetingSlotsActivity = activity;
    ProgressBar progressBar = meetingSlotsActivity.findViewById(R.id.progressBar);
    Date now = new Date();
    Date nextWeek = new Date(now.getTime() + 7 * (1000 * 60 * 60 * 24));
    RecyclerView recyclerView = meetingSlotsActivity.findViewById(R.id.meetingsRecyclerView);
    TimePeriod window = new TimePeriod().setStart(new DateTime(now)).setEnd(new DateTime(nextWeek));
    MeetingSlotFinder.findAvailableMeetingSlots(meetingSlotsActivity, expert, window, recyclerView, progressBar);
  }

  public void onClick(TimePeriod time){
    if(settingsController.getOpenInGoogleCalendarFromSharedPrefs()){
      openCalendar(time, expert);
    }else{
      addEventCalendarApi(time, expert);
    }
  }

  public void openCalendar(TimePeriod time, String expert) {
    Intent insertGoogleCalendar = new Intent(Intent.ACTION_INSERT).setData(Events.CONTENT_URI);
    insertGoogleCalendar.putExtra(CalendarContract.Events.TITLE, settingsController.getMeetingTitleFromSharedPrefs()) // Simple title
        .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false)
        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, time.getStart().getValue())
        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, time.getEnd().getValue())
        .putExtra(CalendarContract.Events.DESCRIPTION, settingsController.getMeetingDescriptionFromSharedPrefs()) // Description
        .putExtra(Intent.EXTRA_EMAIL, expert);

    meetingSlotsActivity.startActivity(insertGoogleCalendar);
  }

  public void addEventCalendarApi(TimePeriod time, String expert){
    try {
      CalendarApiController.addMeeting(meetingSlotsActivity, time, expert).get();
      Toast.makeText(meetingSlotsActivity, "Success", Toast.LENGTH_LONG).show();
      meetingSlotsActivity.startActivity(new Intent(meetingSlotsActivity, SkillSearchActivity.class));
    } catch (ExecutionException e) {
      ErrorDialog.showError(e, meetingSlotsActivity);
    } catch (InterruptedException e) {
      ErrorDialog.showError(e, meetingSlotsActivity);
    }
  }
}
