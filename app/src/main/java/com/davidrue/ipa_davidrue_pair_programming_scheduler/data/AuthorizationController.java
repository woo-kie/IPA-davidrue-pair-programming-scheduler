package com.davidrue.ipa_davidrue_pair_programming_scheduler.data;

import android.app.Activity;
import android.util.Log;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import java.util.Collections;

public class AuthorizationController {

  private static final String TAG = "Authorization Log: ";
  private static final String APPLICATION_NAME = "Pair Programming Scheduler";
  private static final String SCOPES = CalendarScopes.CALENDAR;

  public static final int RC_REQUEST_PERMISSIONS = 5254;

  private static AuthorizationController INSTANCE;

  private Calendar calendarService;

  private HttpTransport httpTransport;

  private JsonFactory jsonFactory;

  private GoogleAccountCredential credential;

  public static AuthorizationController getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new AuthorizationController();
    }
    return INSTANCE;
  }

  public void initialize(Activity activity) {
    try {
      this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
      jsonFactory = GsonFactory.getDefaultInstance();
      credential =
          GoogleAccountCredential.usingOAuth2(activity, Collections.singleton(SCOPES));

      calendarService =
          new com.google.api.services.calendar.Calendar.Builder(httpTransport, jsonFactory,
              credential)
              .setApplicationName(APPLICATION_NAME).build();

    } catch (Exception e) {
      Log.d(TAG, e.getMessage());
    }
  }

  public void initializeCalendarService(GoogleSignInAccount account) {
    credential.setSelectedAccount(account.getAccount());

    calendarService =
        new Calendar.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName(APPLICATION_NAME).build();
  }

  public void maybeInitCalendarService(GoogleSignInAccount account) {
    if (credential == null || credential.getSelectedAccount() == null) {
      initializeCalendarService(account);
    }
  }

  public Calendar getCalendarService() {
    return calendarService;
  }

  public boolean isAuthorized(Activity activity) {
    if (credential == null) {
      return false;
    }
    if (credential.getSelectedAccount() != null
        || GoogleSignIn.hasPermissions(SignInController.getSignedInAccount(activity), new Scope(SCOPES))) {
      return true;
    } else {
      return false;
    }
  }
}
