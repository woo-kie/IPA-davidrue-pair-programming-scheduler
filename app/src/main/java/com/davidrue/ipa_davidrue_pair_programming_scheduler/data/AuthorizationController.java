package com.davidrue.ipa_davidrue_pair_programming_scheduler.data;

import android.app.Activity;
import android.util.Log;

import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.ErrorDialog;
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

/**
 * AuthorizationController is a singleton class responsible for handling the
 * authorization for accessing Google Calendar API.
 */
public class AuthorizationController {

  private static final String TAG = "Authorization Log: ";
  private static final String APPLICATION_NAME = "Pair Programming Scheduler";
  private static final String SCOPES = CalendarScopes.CALENDAR;

  private static AuthorizationController instance;

  private Calendar calendarService;
  private HttpTransport httpTransport;
  private JsonFactory jsonFactory;
  private GoogleAccountCredential credential;

  private AuthorizationController() {
  }

  /**
   * Returns the instance of the AuthorizationController singleton.
   *
   * @return the AuthorizationController instance.
   */
  public static AuthorizationController getInstance() {
    if (instance == null) {
      instance = new AuthorizationController();
    }
    return instance;
  }

  /**
   * Initializes the authorization components.
   *
   * @param activity the calling activity.
   */
  public void initialize(Activity activity) {
    try {
      httpTransport = GoogleNetHttpTransport.newTrustedTransport();
      jsonFactory = GsonFactory.getDefaultInstance();
      credential = GoogleAccountCredential.usingOAuth2(activity, Collections.singleton(SCOPES));

      calendarService = new Calendar.Builder(httpTransport, jsonFactory, credential)
          .setApplicationName(APPLICATION_NAME).build();
    } catch (Exception e) {
      ErrorDialog.showError(e, activity);
    }
  }

  /**
   * Initializes the calendar service with the given GoogleSignInAccount.
   *
   * @param account the GoogleSignInAccount.
   */
  public void initializeCalendarService(GoogleSignInAccount account) {
    credential.setSelectedAccount(account.getAccount());

    calendarService = new Calendar.Builder(httpTransport, jsonFactory, credential)
        .setApplicationName(APPLICATION_NAME).build();
  }

  /**
   * Initializes the calendar service if not already initialized.
   *
   * @param account the GoogleSignInAccount.
   */
  public void maybeInitializeCalendarService(GoogleSignInAccount account) {
    // OnDestroy, GarbageCollector or just token expiry date could cause
    // the credential to be invalid. This tries to seamlessly authorize signed-in users.
    if (credential == null || credential.getSelectedAccount() == null) {
      initializeCalendarService(account);
    }
  }

  /**
   * Returns the calendar service instance.
   *
   * @return the calendar service instance.
   */
  public Calendar getCalendarService() {
    return calendarService;
  }

  /**
   * Checks if the user is authorized.
   *
   * @param activity the calling activity.
   * @return true if authorized, false otherwise.
   */
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
