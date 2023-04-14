package com.davidrue.ipa_davidrue_pair_programming_scheduler.data;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import androidx.annotation.Nullable;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.BuildConfig;
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
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;
import java.util.Collections;
import java.util.Objects;

public class SignInController {

  private static SignInController INSTANCE;
  private static final String TAG = "SignInController: ";
  private static final int RC_SIGN_IN = 1282;
  private static final int RC_REQUEST_PERMISSION = 1283;

  private GoogleSignInOptions googleSignInOptions;

  private GoogleSignInClient googleSignInClient;

  private HttpTransport httpTransport;

  private JsonFactory jsonFactory;

  private GoogleAccountCredential credential;


  public static SignInController getInstance(){
    if(INSTANCE == null){
      INSTANCE = new SignInController();
    }
    return INSTANCE;
  }

  public void initialize(Activity activity) {
    googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestScopes(new Scope(CalendarScopes.CALENDAR))
        .requestEmail()
        .build();

    googleSignInClient = GoogleSignIn.getClient(activity, googleSignInOptions);

    AuthorizationController.getInstance().initialize(activity);
  }

  public void displaySignIn(Activity activity){
    try {
      Intent signInIntent = googleSignInClient.getSignInIntent();
      activity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }catch (Exception e){
      Log.d(TAG, "Error displaying sing-in screen - " + e.getMessage());
    }
  }

  public void onResult(Activity activity, int requestCode, @Nullable Intent data){
    if (requestCode == RC_SIGN_IN) {
      try {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        handleSignInResult(task, activity);
      }catch (Exception e){
        Log.d("Something went wrong parsing the result of the sign in: ", e.getMessage());
      }
    }
  }

  private void handleSignInResult(Task<GoogleSignInAccount> completedTask, Activity activity) {
    try {
      GoogleSignInAccount account = completedTask.getResult(ApiException.class);
      writeUserNameToSharedPrefs(account, activity);
      AuthorizationController.getInstance().initializeCalendarService(account);
      activity.startActivity(new Intent(activity, SkillSearchActivity.class));

    } catch (ApiException e) {
      // The ApiException status code indicates the detailed failure reason.
      // Please refer to the GoogleSignInStatusCodes class reference for more information.
      Log.d(TAG, "signInResult:failed code=" + e.getStatusCode() + e);
      if (e.getStatusCode() == 7) {
        // Probably no internet connection
      }
    }
  }

  public void logout(Activity activity) {
    // Important: Always revoke before sign-out, revoking requires the user to be signed in.
    googleSignInClient.revokeAccess()
        .addOnSuccessListener(activity, task -> {
          Log.d(TAG, "Successfully revoked access");
        }).addOnFailureListener(activity, exception -> Log.d(TAG, "FAILURE FAILURE Revoke"));

    googleSignInClient.signOut()
        .addOnSuccessListener(activity, task -> {
          Log.d(TAG, "Successfully Signed out");
        }).addOnFailureListener(activity, e -> Log.d(TAG, "FAILURE FAILURE Sign Out"));
    clearUserNameInSharedPrefs(activity);
    // Start the sign in activity
    if (!(activity instanceof SignInActivity)) {
      activity.startActivity(new Intent(activity, SignInActivity.class));
    }
  }
  public static void writeUserNameToSharedPrefs(GoogleSignInAccount account, Activity activity) {
    SharedPreferences.Editor editor = activity.getSharedPreferences("LOGGED_IN_USER",
        Context.MODE_PRIVATE).edit();
    editor.putString("userId", account.getEmail());
    editor.commit();
  }

  public static void clearUserNameInSharedPrefs(Activity activity) {
    SharedPreferences.Editor editor = activity.getSharedPreferences("LOGGED_IN_USER",
        Context.MODE_PRIVATE).edit();
    editor.remove("userId");
    editor.commit();
  }

  public static String getUserNameFromSharedPrefs(Activity activity) {
    SharedPreferences sharedPreferences = activity.getSharedPreferences("LOGGED_IN_USER",
        Context.MODE_PRIVATE);

    return sharedPreferences.getString("userId", "Not Found");
  }

  public static GoogleSignInAccount getSignedInAccount(Activity activity) {
    if (isSignedIn(activity)) {
      return GoogleSignIn.getLastSignedInAccount(activity);
    } else {
      return null;
    }
  }

  public void recursiveLogin(Activity activity){
    if (isSignedIn(activity)) {
      String savedEmail = getUserNameFromSharedPrefs(activity);
      if (!savedEmail.equals("Not Found")) {
        // Attempt to sign in the user automatically
        googleSignInClient.silentSignIn()
            .addOnSuccessListener(activity, googleSignInAccount -> {
              // Handle successful silent sign-in
              AuthorizationController.getInstance().maybeInitCalendarService(googleSignInAccount);
              if (activity instanceof SignInActivity) {
                activity.startActivity(new Intent(activity, SkillSearchActivity.class));
              }
            })
            .addOnFailureListener(activity, e -> {
              // Handle failed silent sign-in
              logout(activity);
            });
      } else {
        // If no saved user, then log out and go to the SignInActivity
        logout(activity);
      }
    }
  }

  public static boolean isSignedIn(Activity activity) {
    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
    if (account != null) {
      if (Objects.equals(account.getEmail(), getUserNameFromSharedPrefs(activity))) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

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
