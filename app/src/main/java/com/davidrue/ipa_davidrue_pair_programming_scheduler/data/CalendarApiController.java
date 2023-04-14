package com.davidrue.ipa_davidrue_pair_programming_scheduler.data;

import android.app.Activity;
import android.util.Log;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.Calendar.Freebusy;
import com.google.api.services.calendar.model.Event;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CalendarApiController {

  public static final int REQUEST_AUTHORIZATION = 3200;
  public static final String TAG = "CalendarApi: ";
  private static final ExecutorService execService = Executors.newSingleThreadExecutor();
  private static final ListeningExecutorService futureExecutorService = MoreExecutors.listeningDecorator(
      execService);

  public static ListeningExecutorService getExecutor() {
    ExecutorService execService = Executors.newSingleThreadExecutor();
    return MoreExecutors.listeningDecorator(
        execService);
  }

  public static ListenableFuture<Map<String, FreeBusyCalendar>> getFreeBusy(Activity activity,
      String otherUserId, TimePeriod window) {

    if (AuthorizationController.getInstance().isAuthorized(activity)) {
      return Futures.transform(getFreeBusyPrep(activity, otherUserId, window), response ->
          response.getCalendars(), getExecutor());
    } else {
      return Futures.immediateFailedFuture(new Exception("Not Signed in"));
    }

  }

  private static ListenableFuture<FreeBusyResponse> getFreeBusyPrep(Activity activity,
      String userId, TimePeriod window) {
    FreeBusyRequestItem home = new FreeBusyRequestItem().setId(
        SignInController.getUserNameFromSharedPrefs(activity));
    FreeBusyRequestItem external = new FreeBusyRequestItem().setId(userId);
    List<FreeBusyRequestItem> items = Arrays.asList(home, external);

    String accountId = SignInController.getUserNameFromSharedPrefs(activity);
    if (accountId == null || accountId.isEmpty() || accountId.equals("Not Found")) {
      System.out.println(accountId);
    }

    ExecutorService execService = Executors.newSingleThreadExecutor();
    ListeningExecutorService futureExecutorService = MoreExecutors.listeningDecorator(
        execService);

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
        // Internet connection Error
      } catch (Error e){
        System.out.println(e);
      }
      return null;
    });
  }
}