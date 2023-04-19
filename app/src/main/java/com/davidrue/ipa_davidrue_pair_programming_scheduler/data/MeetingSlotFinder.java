package com.davidrue.ipa_davidrue_pair_programming_scheduler.data;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.R;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.ErrorDialog;
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
import java.time.Instant;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * The MeetingSlotFinder class helps users find available meeting slots
 * for pair programming sessions with the selected expert.
 */
public class MeetingSlotFinder {

  private static final String TAG = "MeetingSlotFinder: ";
  private static SettingsController settingsController;
  private static List<TimePeriod> results;

  /**
   * Finds available meeting slots and updates the UI.
   *
   * @param activity      the activity to run UI updates on
   * @param otherUserId   the other user's ID
   * @param window        the time window to search for available slots
   * @param recyclerView  the recycler view to display the results
   * @param progressBar   the progress bar to indicate searching progress
   */
  public static void findAvailableMeetingSlots(
      MeetingSlotsActivity activity,
      String otherUserId,
      TimePeriod window,
      RecyclerView recyclerView,
      ProgressBar progressBar) {

    settingsController = new SettingsController(activity);
    // Shows the "loading"-icon until the asynchronous tasks are done.
    activity.runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));

    ListenableFuture<List<TimePeriod>> freeSlotsFuture = getFreeMeetingSlots(
        activity,
        otherUserId,
        window,
        Duration.valueOf(settingsController.getMeetingDurationFromSharedPrefs()));

    freeSlotsFuture.addListener(() -> {
      if (freeSlotsFuture.isDone() && !freeSlotsFuture.isCancelled()) {
        try {

          // Filtering the result by working hours and off hours
          List<TimePeriod> result = freeSlotsFuture.get().stream()
              .filter(MeetingSlotFinder::isInWorkingHours)
              .collect(Collectors.toList());

          // Sorting results by start time
          Comparator<TimePeriod> comparator = Comparator.comparing(
              timePeriod -> timePeriod.getStart().getValue());
          result.sort(comparator);
          results = result;

          // Populating recycler view with results
          activity.runOnUiThread(() -> {
            MeetingsRecyclerViewAdapter adapter = new MeetingsRecyclerViewAdapter(
                activity,
                result,
                activity);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(activity));
            progressBar.setVisibility(View.GONE);
            if(adapter.getItemCount() == 0){
              activity.findViewById(R.id.meetings_no_results).setVisibility(View.VISIBLE);
            }
          });

        } catch (Exception e) {
          activity.runOnUiThread(()->{
            progressBar.setVisibility(View.GONE);
            activity.findViewById(R.id.meetings_no_results).setVisibility(View.VISIBLE);
          });
        }
      }
    }, getExecutor());
  }

  /**
   * Checks if a time slot is within working hours.
   *
   * @param slot the time slot to check
   * @return true if the slot is within working hours, false otherwise
   */
  public static boolean isInWorkingHours(TimePeriod slot) {
    SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");
    int endWorkingHours;
    int startWorkingHours;

    // Setup until return statement, so the hours of each instance can be compared.
    try {
      startWorkingHours = Objects.requireNonNull(
          hourFormat.parse(settingsController.getMeetingStartTimeFromSharedPrefs())).getHours();

      endWorkingHours = Objects.requireNonNull(
          hourFormat.parse(settingsController.getMeetingEndTimeFromSharedPrefs())).getHours();

    } catch (ParseException e) {
      /* This should never happen, if it does a default value will be set.
         It's better if the user has some sort of result instead of an error. */
      Log.d(TAG, e.getMessage());
      endWorkingHours = 21;
      startWorkingHours = 7;
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
        (hourStart >= startWorkingHours && hourEnd <= endWorkingHours) &&
        (hourStart < endWorkingHours && hourEnd > startWorkingHours) &&
        !(hourStart >= 12 && hourEnd <= 13);
  }

  /**
   * Gets all free meeting slots for both users.
   *
   * @param activity    the activity to run UI updates on
   * @param otherUserId the other user's ID
   * @param window      the time window to search for available slots
   * @param duration    the desired meeting duration
   * @return a future containing a list of free meeting slots
   */
  public static ListenableFuture<List<TimePeriod>> getFreeMeetingSlots(
      Activity activity,
      String otherUserId,
      TimePeriod window,
      Duration duration) {
    return Futures.transform(
        Futures.transform(getBothCalendarsAsList(activity, otherUserId, window),
            busySlots -> transformBusyToFreeSlots(busySlots, window), getExecutor()),
        freeSlots -> getMeetingSlotsOfDuration(freeSlots, duration), getExecutor());
  }

  /**
   * Transforms a list of busy time slots into a list of free time slots.
   *
   * @param busySlots the list of busy time slots
   * @param window    the time window to search for free slots
   * @return the list of free time slots
   */
  public static List<TimePeriod> transformBusyToFreeSlots(List<TimePeriod> busySlots,
      TimePeriod window) {

    // This list only contains unique timestamps
    List<Long> datesAsArray = getTimeStampsAsArray(busySlots);

    long max = Math.max(Collections.max(datesAsArray), window.getEnd().getValue());
    long min = Math.min(Collections.min(datesAsArray), window.getStart().getValue());

    Collections.sort(datesAsArray);

    long first = datesAsArray.get(0);
    long last = datesAsArray.get(datesAsArray.size() - 1);

    List<State> statusSlots = labelSlotsAsFreeBusy(datesAsArray, busySlots);

    List<TimePeriod> freeSlots = new ArrayList<>();


    // Always starts as "FREE" and ends as "BUSY", this way we can find available timeslots
    // between now and the next meeting and between the last meeting and the end of the search window.
    freeSlots.add(new TimePeriod().setStart(new DateTime(min)).setEnd(new DateTime(first)));
    long tempStartTime = min;
    State previous = State.FREE;

    for (int pos = 0; pos < datesAsArray.size(); pos++) {
      State current = statusSlots.get(pos);
      long currentTime = datesAsArray.get(pos);

      if (previous != current) {
        // We always capture the switch from "FREE" to "BUSY"
        if (Objects.equals(current, State.BUSY) && Objects.equals(previous, State.FREE)) {
          freeSlots.add(new TimePeriod().setStart(new DateTime(tempStartTime))
              .setEnd(new DateTime(currentTime)));
        }else {
          tempStartTime = currentTime;
      }
      }
      previous = current;
    }

    // As mentioned above, adds the last slot. Between endLastMeeting and endSearchWindow.
    freeSlots.add(new TimePeriod().setStart(new DateTime(last)).setEnd(new DateTime(max)));

    return new ArrayList<>(new HashSet<>(freeSlots));
  }

  private static List<TimePeriod> getMeetingSlotsOfDuration(List<TimePeriod> freeSlots,
      Duration duration) {
    List<TimePeriod> meetingSlots = new ArrayList<>();
    for (TimePeriod timePeriod : freeSlots) {
      meetingSlots.addAll(getMeetingSlotsFromTimePeriod(timePeriod, duration));
    }
    return meetingSlots;
  }

  private static List<TimePeriod> getMeetingSlotsFromTimePeriod(TimePeriod timePeriod,
      Duration duration) {

    List<TimePeriod> results = new ArrayList<>();

    LocalDateTime time = dateTimeToLocalDateTime(timePeriod.getStart());

    // Rounds up to the next 15-minute mark on the clock. Leaves room for improvement ->
    // should handle roundUp and roundDown differently.
    LocalDateTime current = time.truncatedTo(ChronoUnit.HOURS)
        .plusMinutes(15 * (time.getMinute() / (15L * duration.slots))).plusMinutes(15L * duration.slots);

    while (localDateTimeToLong(current) < timePeriod.getEnd().getValue()) {
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
    Instant instant = Instant.ofEpochMilli(dateTime.getValue());
    return instant.atZone(ZoneId.of("UTC+1")).toLocalDateTime();
  }

  private static Long localDateTimeToLong(LocalDateTime localDateTime) {
    return localDateTime.atZone(ZoneId.of("UTC+1")).toInstant().toEpochMilli();
  }

  private static Long localDateTimeToLongPlusDuration(LocalDateTime localDateTime,
      Duration duration) {
    return localDateTime
        .plusMinutes(duration.getDuration() * 15L)
        .atZone(ZoneId.of("UTC+1"))
        .toInstant()
        .toEpochMilli();
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

  /**
   * Gets the time period at the specified position in the results list.
   *
   * @param position the position of the time period in the results list
   * @return the time period at the specified position
   */
  public static TimePeriod getPosition(int position) {
    return results.get(position);
  }

  /**
   * Creates and returns a listening executor service to handle futures.
   *
   * @return a listening executor service
   */
  public static ListeningExecutorService getExecutor() {
    ExecutorService execService = Executors.newSingleThreadExecutor();
    return MoreExecutors.listeningDecorator(execService);
  }

  private enum State {
    FREE,
    BUSY
  }

  /**
   * Represents meeting duration in 15-minute increments.
   */
  public enum Duration {
    FIFTEEN(1),
    THIRTY(2),
    HOUR(4),
    NINETY(6);

    private final int slots;

    Duration(int slots) {
      this.slots = slots;
    }

    int getDuration() {
      return slots;
    }
  }
}
