package com.davidrue.ipa_davidrue_pair_programming_scheduler.data;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.annotation.Nullable;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.ErrorDialog;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.ui.SignInActivity;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.ui.SkillSearchActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.services.calendar.CalendarScopes;
import java.util.Objects;

/**
 * The SignInController class is responsible for managing user authentication and sign-in operations
 * with Google. It also handles sign-out and silent sign-in, while managing user-related data in
 * SharedPreferences.
 */
public class SignInController {

  private static SignInController INSTANCE;
  private static final int RC_SIGN_IN = 1282;

  private GoogleSignInClient googleSignInClient;

  /**
   * Returns an instance of SignInController.
   *
   * @return an instance of SignInController
   */
  public static SignInController getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new SignInController();
    }
    return INSTANCE;
  }

  private static void onSuccess(Void task) {
  }

  /**
   * Initializes the GoogleSignInClient and AuthorizationController.
   *
   * @param activity the activity from which this method is called
   */
  public void initialize(Activity activity) {
    GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(
        GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestScopes(new Scope(CalendarScopes.CALENDAR))
        .requestEmail()
        .build();

    googleSignInClient = GoogleSignIn.getClient(activity, googleSignInOptions);
    AuthorizationController.getInstance().initialize(activity);
  }

  /**
   * Starts the sign-in process by displaying the sign-in UI.
   *
   * @param activity the activity from which this method is called
   */
  public void displaySignIn(Activity activity) {
    try {
      Intent signInIntent = googleSignInClient.getSignInIntent();
      activity.startActivityForResult(signInIntent, RC_SIGN_IN);
    } catch (Exception e) {
      ErrorDialog.showError(e, activity);
    }
  }

  /**
   * Handles the sign-in result by processing the returned data.
   *
   * @param activity the activity from which this method is called
   * @param requestCode the request code of the sign-in activity
   * @param data the returned intent containing sign-in data
   */
  public void onResult(Activity activity, int requestCode, @Nullable Intent data) {
    if (requestCode == RC_SIGN_IN) {
      try {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        handleSignInResult(task, activity);
      } catch (Exception e) {
        ErrorDialog.showError(e, activity);
      }
    }
  }

  /**
   * Handles the sign-in result by processing the completed task. Initializes calendarService for
   * CalendarApiRequests & writes loggedIn user to SharedPreferences.
   *
   * @param completedTask the task containing the sign-in result
   * @param activity the activity from which this method is called
   */
  private void handleSignInResult(Task<GoogleSignInAccount> completedTask, Activity activity) {
    try {
      GoogleSignInAccount account = completedTask.getResult(ApiException.class);
      writeUserNameToSharedPrefs(account, activity);
      AuthorizationController.getInstance().initializeCalendarService(account);
      activity.startActivity(new Intent(activity, SkillSearchActivity.class));

    } catch (ApiException e) {
      // The ApiException status code indicates the detailed failure reason.
      // Refer to the GoogleSignInStatusCodes class reference for more information.
      switch (e.getStatusCode()){
        case 12501:
          ErrorDialog.showError(new Exception("Cancelled Sign-in"), activity);
          break;
        case 10:
          ErrorDialog.showError(new Exception("Developer Error, check configuration!"), activity);
        case 7:
          ErrorDialog.showError(new Exception("Connectivity / Access Issues"), activity);
        default:
          ErrorDialog.showError(e, activity);
      }
    }
  }

  /**
   * Logs out the user and revokes all access + redirected to SignInActivity.
   *
   * @param activity the activity from which this method is called
   */
  public void logout(Activity activity) {
    // Important: Always revoke before sign-out, revoking requires the user to be signed in.
    googleSignInClient.revokeAccess()
        .addOnSuccessListener(activity, SignInController::onSuccess)
        .addOnFailureListener(activity, exception -> ErrorDialog.showError(exception, activity));

    googleSignInClient.signOut()
        .addOnSuccessListener(activity, SignInController::onSuccess)
        .addOnFailureListener(activity, exception -> ErrorDialog.showError(exception, activity));
    clearUserNameInSharedPrefs(activity);
    // Start the sign in activity
    if (!(activity instanceof SignInActivity)) {
      activity.startActivity(new Intent(activity, SignInActivity.class));
    }
  }

  /**
   * Writes the user's email to SharedPreferences.
   *
   * @param account the GoogleSignInAccount containing user's email
   * @param activity the activity from which this method is called
   */
  public static void writeUserNameToSharedPrefs(GoogleSignInAccount account, Activity activity) {
    SharedPreferences.Editor editor = activity.getSharedPreferences("LOGGED_IN_USER",
        Context.MODE_PRIVATE).edit();
    editor.putString("userId", account.getEmail());
    // Has to be .commit(), due to race condition onResume() -> isSignedIn()
    editor.commit();
  }

  /**
   * Clears the user's email from SharedPreferences.
   *
   * @param activity the activity from which this method is called
   */
  public static void clearUserNameInSharedPrefs(Activity activity) {
    SharedPreferences.Editor editor = activity.getSharedPreferences("LOGGED_IN_USER",
        Context.MODE_PRIVATE).edit();
    editor.remove("userId");
    // Has to be .commit(), due to race condition onResume() -> isSignedIn()
    editor.commit();
  }

  /**
   * Retrieves the user's email from SharedPreferences.
   *
   * @param activity the activity from which this method is called
   * @return the user's email as a string
   */
  public static String getUserNameFromSharedPrefs(Activity activity) {
    SharedPreferences sharedPreferences = activity.getSharedPreferences("LOGGED_IN_USER",
        Context.MODE_PRIVATE);

    return sharedPreferences.getString("userId", "Not Found");
  }

  /**
   * Returns the GoogleSignInAccount of the signed-in user, if there is one.
   *
   * @param activity the activity from which this method is called
   * @return the GoogleSignInAccount of the signed-in user or null
   */
  public static GoogleSignInAccount getSignedInAccount(Activity activity) {
    if (isSignedIn(activity)) {
      return GoogleSignIn.getLastSignedInAccount(activity);
    } else {
      return null;
    }
  }

  /**
   * Performs a silent sign-in, if the user is signed in and the saved email matches.
   *
   * @param activity the activity from which this method is called
   */
  public void silentSignIn(Activity activity) {
    if (isSignedIn(activity)) {
      String savedEmail = getUserNameFromSharedPrefs(activity);
      if (!savedEmail.equals("Not Found")) {
        // Attempt to sign in the user automatically
        googleSignInClient.silentSignIn()
            .addOnSuccessListener(activity, googleSignInAccount -> {
              // Handle successful silent sign-in
              AuthorizationController.getInstance()
                  .maybeInitializeCalendarService(googleSignInAccount);
              if (activity instanceof SignInActivity) {
                activity.startActivity(new Intent(activity, SkillSearchActivity.class));
              }
            })
            .addOnFailureListener(activity, e -> {
              // Handle failed silent sign-in
              ErrorDialog.showError(e, activity);
              logout(activity);
            });
      } else {
        // If no saved user, then log out and go to the SignInActivity
        logout(activity);
      }
    }
  }

  /**
   * Checks if the user is signed in and the saved email matches the signed-in user's email.
   *
   * @param activity the activity from which this method is called
   * @return true if the user is signed in and the saved email matches, false otherwise
   */
  public static boolean isSignedIn(Activity activity) {
    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
    if (account != null) {
      return Objects.equals(account.getEmail(), getUserNameFromSharedPrefs(activity));
    } else {
      return false;
    }
  }

  /**
   * Checks if the network and Google Play Services are available.
   *
   * @param activity the activity from which this method is called
   * @return true if the network & play services are available, false otherwise
   */
  public boolean isNetworkAvailable(Activity activity) {
    ConnectivityManager connectivityManager
        = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo =
        connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
    GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
    int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(activity);

    return activeNetworkInfo != null && activeNetworkInfo.isConnected()
        && resultCode == ConnectionResult.SUCCESS;
  }
}
