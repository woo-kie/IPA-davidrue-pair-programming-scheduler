package com.davidrue.ipa_davidrue_pair_programming_scheduler.data;

import android.app.Activity;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.ErrorDialog;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.calendar.Calendar.Freebusy;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.FreeBusyCalendar;
import com.google.api.services.calendar.model.FreeBusyRequest;
import com.google.api.services.calendar.model.FreeBusyRequestItem;
import com.google.api.services.calendar.model.FreeBusyResponse;
import com.google.api.services.calendar.model.TimePeriod;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CalendarApiController is a class to interact with the Google Calendar API.
 */
public class CalendarApiController {
  public static final int REQUEST_AUTHORIZATION = 3200;
  public static final String TAG = "CalendarApi: ";
  private static final ExecutorService EXEC_SERVICE = Executors.newSingleThreadExecutor();
  private static final ListeningExecutorService FUTURE_EXECUTOR_SERVICE = MoreExecutors.listeningDecorator(EXEC_SERVICE);

  private static ListeningExecutorService getExecutor() {
    ExecutorService execService = Executors.newSingleThreadExecutor();
    return MoreExecutors.listeningDecorator(execService);
  }

  /**
   * Returns the free/busy status of two users' calendars.
   *
   * @param activity   the calling activity.
   * @param otherUserId the other user's email address.
   * @param window     the time period to check for availability.
   * @return a future containing a map of free/busy information.
   */
  public static ListenableFuture<Map<String, FreeBusyCalendar>> getFreeBusy(Activity activity,
      String otherUserId, TimePeriod window) {
    AuthorizationController.getInstance().initialize(activity);
    AuthorizationController.getInstance().maybeInitializeCalendarService(SignInController.getSignedInAccount(activity));
    if (AuthorizationController.getInstance().isAuthorized(activity)) {
      return Futures.transform(getFreeBusyPrep(activity, otherUserId, window), response ->
          response.getCalendars(), getExecutor());
    } else {
      return Futures.immediateFailedFuture(new Exception("Not Signed in"));
    }
  }

  /**
   * Adds a meeting to both users' Google Calendar.
   *
   * @param activity   the calling activity.
   * @param time       the time period for the meeting.
   * @param otherUserId the other user's email address.
   * @return a future containing the created event.
   */
  public static ListenableFuture<Event> addMeeting(Activity activity, TimePeriod time, String otherUserId) {
    return FUTURE_EXECUTOR_SERVICE.submit(() -> {
      SettingsController settingsController = new SettingsController(activity);
      String title = settingsController.getMeetingTitleFromSharedPrefs();
      String description = settingsController.getMeetingDescriptionFromSharedPrefs();

      Event event = new Event().setSummary(title).setDescription(description);

      EventDateTime startDateTime = new EventDateTime().setDateTime(time.getStart()).setTimeZone("UTC+1");
      EventDateTime endDateTime = new EventDateTime().setDateTime(time.getEnd()).setTimeZone("UTC+1");

      event.setStart(startDateTime).setEnd(endDateTime);

      EventAttendee[] attendees = new EventAttendee[]{
          new EventAttendee().setEmail(SignInController.getUserNameFromSharedPrefs(activity)),
          new EventAttendee().setEmail(otherUserId),
      };

      event.setAttendees(Arrays.asList(attendees));

      String calendarId = "primary";

      try {
        return AuthorizationController.getInstance().getCalendarService()
            .events().insert(calendarId, event).execute();
      } catch (IOException ioException) {
        ErrorDialog.showError(ioException, activity);
      }
      return null;
    });
  }

  private static ListenableFuture<FreeBusyResponse> getFreeBusyPrep(Activity activity,
      String userId, TimePeriod window) {
    FreeBusyRequestItem home = new FreeBusyRequestItem().setId(
        SignInController.getUserNameFromSharedPrefs(activity));
    FreeBusyRequestItem external = new FreeBusyRequestItem().setId(userId);
    List<FreeBusyRequestItem> items = Arrays.asList(home, external);

    ExecutorService execService = Executors.newSingleThreadExecutor();
    ListeningExecutorService futureExecutorService = MoreExecutors.listeningDecorator(execService);

    return futureExecutorService.submit(() -> {
      try {
        FreeBusyRequest request = new FreeBusyRequest().setItems(items)
            .setTimeMin(window.getStart())
            .setTimeMax(window.getEnd()).setTimeZone("UTC+1");
        Freebusy.Query query = AuthorizationController.getInstance().getCalendarService().freebusy()
            .query(request);

        System.out.println(query.execute());
        return query.execute();
      } catch (UserRecoverableAuthIOException userRecoverableAuthIOException) {
        activity.startActivityForResult(
            userRecoverableAuthIOException.getIntent(), REQUEST_AUTHORIZATION);
      } catch (UnknownHostException unknownHostException) {
        // Network Connectivity Problems
        ErrorDialog.showError(unknownHostException, activity);
      } catch (Exception e) {
        ErrorDialog.showError(e, activity);
      }
      return null;
    });
  }
}