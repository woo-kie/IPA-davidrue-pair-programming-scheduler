package com.davidrue.ipa_davidrue_pair_programming_scheduler.data;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.ui.meetings.MeetingSlotsActivity;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.ui.meetings.MeetingsRecyclerViewAdapter;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.TimePeriod;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class MeetingSlotFinder {

  private static SettingsController settingsController;
  public static void findAvailableMeetingSlots(MeetingSlotsActivity activity, String otherUserId,
      TimePeriod window, Duration duration, RecyclerView recyclerView, ProgressBar progressBar) {

    settingsController = new SettingsController(activity);
    activity.runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));

    ListenableFuture<List<TimePeriod>> freeSlotsFuture = getFreeMeetingSlots(activity, otherUserId,
        window, duration);
    freeSlotsFuture.addListener(() -> {
      if (freeSlotsFuture.isDone() && !freeSlotsFuture.isCancelled()) {
        try {
            List<TimePeriod> result = freeSlotsFuture.get().stream().filter(
                MeetingSlotFinder::isInWorkingHours).collect(
                Collectors.toList());


          Comparator<TimePeriod> comparator = Comparator.comparing(
              timePeriod -> timePeriod.getStart().getValue());
          result.sort(comparator);
          activity.runOnUiThread(() -> {
            MeetingsRecyclerViewAdapter adapter = new MeetingsRecyclerViewAdapter(activity, result, activity);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(activity));
            progressBar.setVisibility(View.GONE);
          });

        } catch (Exception e) {
          e.printStackTrace();
          Log.d("MeetingSlotFinder: ", "Execution Exception " + e.getMessage());
        }
      }
    }, getExecutor());
  }


  public static ListeningExecutorService getExecutor() {
    ExecutorService execService = Executors.newSingleThreadExecutor();
    return MoreExecutors.listeningDecorator(execService);
  }

  public static boolean isInWorkingHours(TimePeriod slot) {

    // check if slot is in working hours
    SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");
    int endWorkingHours;
    int startWorkingHours;

    try {
      startWorkingHours = Objects.requireNonNull(
          hourFormat.parse(settingsController.getMeetingStartTimeFromSharedPrefs())).getHours();

      endWorkingHours = Objects.requireNonNull(
          hourFormat.parse(settingsController.getMeetingEndTimeFromSharedPrefs())).getHours();

    }catch (ParseException e){
      e.printStackTrace();
      endWorkingHours = 21;
      startWorkingHours = 6;
    }

    Date start = new Date(slot.getStart().getValue());
    Date end = new Date(slot.getEnd().getValue());
    Calendar calendarStart = Calendar.getInstance();
    Calendar calendarEnd = Calendar.getInstance();

    calendarStart.setTime(start);
    calendarEnd.setTime(end);

    int dayOfWeek = calendarStart.get(Calendar.DAY_OF_WEEK);
    int hourStart = calendarStart.get(Calendar.HOUR_OF_DAY);
    int hourEnd = calendarEnd.get(Calendar.HOUR_OF_DAY);


    return (dayOfWeek != Calendar.SUNDAY && dayOfWeek != Calendar.SATURDAY) &&
        (hourStart >= startWorkingHours && hourEnd <= endWorkingHours) && (hourStart < endWorkingHours && hourEnd > startWorkingHours) && !(hourStart >= 12
        && hourEnd <= 13);
  }

  public static ListenableFuture<List<TimePeriod>> getFreeMeetingSlots(Activity activity,
      String otherUserId, TimePeriod window, Duration duration) {
    return Futures.transform(
        Futures.transform(getBothCalendarsAsList(activity, otherUserId, window),
            busySlots -> transformBusyToFreeSlots(busySlots, window), getExecutor()),
        freeSlots -> getMeetingSlotsOfDuration(freeSlots, duration), getExecutor());
  }


  public static List<TimePeriod> transformBusyToFreeSlots(List<TimePeriod> busySlots,
      TimePeriod window) {

    List<Long> datesAsArray = getTimeStampsAsArray(busySlots);
    Set<Long> uniqueDatesAsSet = new HashSet<>();

    long max = Math.max(Collections.max(datesAsArray), window.getEnd().getValue());
    long min = Math.min(Collections.min(datesAsArray), window.getStart().getValue());

    uniqueDatesAsSet.addAll(datesAsArray);

    Collections.sort(datesAsArray);

    long first = datesAsArray.get(0);
    long last = datesAsArray.get(datesAsArray.size() - 1);

    List<State> statusSlots = labelSlotsAsFreeBusy(datesAsArray, busySlots);

    List<TimePeriod> freeSlots = new ArrayList<>();
    freeSlots.add(new TimePeriod().setStart(new DateTime(min)).setEnd(new DateTime(first)));
    long tempStartTime = min;

    State previous = State.FREE;

    for (int pos = 0; pos < datesAsArray.size(); pos++) {
      State current = statusSlots.get(pos);
      long currentTime = datesAsArray.get(pos);

      if (previous == current) {

      } else if (Objects.equals(current, State.BUSY) && Objects.equals(previous, State.FREE)) {
        freeSlots.add(new TimePeriod().setStart(new DateTime(tempStartTime))
            .setEnd(new DateTime(currentTime)));
      } else {
        tempStartTime = currentTime;
      }

      previous = current;
    }

    freeSlots.add(new TimePeriod().setStart(new DateTime(last)).setEnd(new DateTime(max)));

    return new ArrayList<>(new HashSet<>(freeSlots));
  }

  private static List<TimePeriod> getMeetingSlotsOfDuration(List<TimePeriod> freeSlots,
      Duration duration) {

    List<TimePeriod> meetingSlots = new ArrayList<>();
    System.out.println("FREE SLOTS " + freeSlots);
    for (TimePeriod timePeriod : freeSlots) {
      meetingSlots.addAll(getMeetingSlotsFromTimePeriod(timePeriod, duration));
    }
    return meetingSlots;
  }

  private static List<TimePeriod> getMeetingSlotsFromTimePeriod(TimePeriod timePeriod,
      Duration duration) {

    List<TimePeriod> results = new ArrayList<>();

    LocalDateTime time = dateTimeToLocalDateTime(timePeriod.getStart());

    LocalDateTime current = time.truncatedTo(ChronoUnit.HOURS)
        .plusMinutes(15 * (time.getMinute() / (15L * duration.slots))).plusMinutes(15 * duration.slots);

    while (localDateTimeToLong(current) < timePeriod.getEnd().getValue()) {
      System.out.println(current);
      DateTime end = new DateTime(localDateTimeToLongPlusDuration(current, duration));
      if (localDateTimeToLong(dateTimeToLocalDateTime(end)) <= timePeriod.getEnd().getValue()) {
        results.add(new TimePeriod().setStart(
                new DateTime(localDateTimeToLong(current)))
            .setEnd(end));
      }
      current = current.plusMinutes(duration.getDuration() * 15L);
    }

    return results;
  }

  private static LocalDateTime dateTimeToLocalDateTime(DateTime dateTime) {
    Date temp = new Date(dateTime.getValue());
    return temp.toInstant().atZone(ZoneId.of("UTC+1")).toLocalDateTime();
  }

  private static Long localDateTimeToLong(LocalDateTime localDateTime) {
    return localDateTime.atZone(ZoneId.of("UTC+1")).toInstant().toEpochMilli();
  }

  private static Long localDateTimeToLongPlusDuration(LocalDateTime localDateTime,
      Duration duration) {
    return localDateTime.plusMinutes(
        duration.getDuration() * 15L).atZone(ZoneId.of("UTC+1")).toInstant().toEpochMilli();
  }


  private static List<State> labelSlotsAsFreeBusy(List<Long> timeSlots,
      List<TimePeriod> busyTimePeriods) {
    List<State> labelSlots = new ArrayList<>();

    for (long current : timeSlots) {
      labelSlots.add(getFreeBusyStatus(current, busyTimePeriods));
    }
    return labelSlots;
  }


  private static State getFreeBusyStatus(long current, List<TimePeriod> busyTimePeriods) {

    boolean isBusy = busyTimePeriods.stream().anyMatch(timePeriod -> isBusy(timePeriod, current));
    return isBusy ? State.BUSY : State.FREE;
  }

  private static boolean isBusy(TimePeriod timePeriod, long current) {
    return timePeriod.getStart().getValue() <= current && timePeriod.getEnd().getValue() > current;
  }


  private static ListenableFuture<List<TimePeriod>> getBothCalendarsAsList(Activity activity,
      String otherUserId, TimePeriod window) {
    List<TimePeriod> times = new ArrayList<>();

    return Futures.transform(CalendarApiController.getFreeBusy(activity, otherUserId, window),
        bothCalendarsAsMap -> {
          times.addAll(Objects.requireNonNull(
              bothCalendarsAsMap.get(SignInController.getUserNameFromSharedPrefs(activity))).getBusy());
          times.addAll(Objects.requireNonNull(bothCalendarsAsMap.get(otherUserId)).getBusy());
          return times;

        }, getExecutor());
  }

  private static List<Long> getTimeStampsAsArray(List<TimePeriod> timePeriods) {

    List<Long> results = new ArrayList<>();

    for (TimePeriod timePeriod : timePeriods) {
      results.add(timePeriod.getStart().getValue());
      results.add(timePeriod.getEnd().getValue());
    }

    return new ArrayList<>(new HashSet<>(results));
  }

  private enum State {
    FREE, BUSY,
  }

  public enum Duration {
    FIFTEEN(1), THIRTY(2), HOUR(4), NINETY(6);

    private final int slots;

    Duration(int slots) {
      this.slots = slots;
    }

    int getDuration() {
      return slots;
    }
  }
}
