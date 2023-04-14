package com.davidrue.ipa_davidrue_pair_programming_scheduler.data;

import android.app.Activity;
import androidx.annotation.NonNull;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.Expert;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.Skill;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.ExpertsListCallback;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.SkillsListCallback;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * The SkillsAndExpertsApiController is a singleton controller responsible for fetching the list of skills and
 * experts from our sample JSON-API server. This class handles making HTTP requests, deserializing the response,
 * and managing the flow of data back to the caller through the provided callbacks.
 */
public class SkillsAndExpertsApiController {
  private static final String URL_ALL_SKILLS = "https://shy-gold-fossa-belt.cyclic.app/skills";
  private static final String URL_ALL_USERS = "https://shy-gold-fossa-belt.cyclic.app/users";

  private static final Gson gson = new Gson();

  private static OkHttpClient client;
  private static SkillsAndExpertsApiController INSTANCE;

  /**
   * Initializes the controller and its instance.
   *
   * @return an instance of SkillsAndExpertsApiController
   */
  public static SkillsAndExpertsApiController getInstance(){
    if(INSTANCE == null){
      INSTANCE = new SkillsAndExpertsApiController();
      client = new OkHttpClient();
    }
    return INSTANCE;
  }

  /**
   * Retrieves a list of skills from the API.
   *
   * @param activity the activity from which this method is called
   * @param callback callback for handling the list of skills
   */
  public void getSkills(Activity activity, SkillsListCallback callback) {
    Request request = new Request.Builder().url(URL_ALL_SKILLS).build();

    Type skillListType = new TypeToken<List<Skill>>() {}.getType();

    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(@NonNull Call call, @NonNull IOException e) {
        activity.runOnUiThread(() -> callback.onFailure(e));
      }

      @Override
      public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
        if (response.isSuccessful()) {
          assert response.body() != null;
          String responseData = response.body().string();

          List<Skill> skillsList = gson.fromJson(responseData, skillListType);

          activity.runOnUiThread(() -> {
            callback.onSuccess(skillsList);
          });
        } else {
          activity.runOnUiThread(() -> callback.onFailure(new Exception("Skills Request not successful")));
        }
      }
    });
  }

  /**
   * Retrieves a list of experts from the API.
   *
   * @param activity the activity from which this method is called
   * @param callback callback for handling the list of experts
   */
  public void getExperts(Activity activity, ExpertsListCallback callback) {
    Request request = new Request.Builder().url(URL_ALL_USERS).build();
    Type expertsListType = new TypeToken<List<Expert>>() {}.getType();
    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(@NonNull Call call, @NonNull IOException e) {
        activity.runOnUiThread(() -> callback.onFailure(e));
      }

      @Override
      public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
        if (response.isSuccessful()) {
          assert response.body() != null;
          String responseData = response.body().string();
          List<Expert> expertsList = gson.fromJson(responseData, expertsListType);

          activity.runOnUiThread(() -> callback.onSuccess(expertsList));
        } else {
          activity.runOnUiThread(() -> callback.onFailure(new Exception("Experts Request not successful")));
        }
      }
    });
  }
}
