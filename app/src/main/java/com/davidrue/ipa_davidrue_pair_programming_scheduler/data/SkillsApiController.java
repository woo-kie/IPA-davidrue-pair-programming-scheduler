package com.davidrue.ipa_davidrue_pair_programming_scheduler.data;

import android.app.Activity;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.Skill;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.SkillsArrayCallback;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SkillsApiController {
  private static final String URL_ALL_SKILLS = "https://shy-gold-fossa-belt.cyclic.app/skills";
  private static final String URL_ALL_USERS = "https://shy-gold-fossa-belt.cyclic.app/users";
  private static final Gson gson = new Gson();
  private static OkHttpClient client;
  private static SkillsApiController INSTANCE;

  public static SkillsApiController initialize(Activity activity){
    if(INSTANCE == null){
      INSTANCE = new SkillsApiController();
      client = new OkHttpClient();
    }
    return INSTANCE;
  }

  public void getSkills(Activity activity, SkillsArrayCallback callback) {
    Request request = new Request.Builder().url(URL_ALL_SKILLS).build();

    Type skillListType = new TypeToken<List<Skill>>() {}.getType();

    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        activity.runOnUiThread(() -> callback.onFailure(e));
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        if (response.isSuccessful()) {
          String responseData = response.body().string();
          Gson gson = new Gson();
          List<Skill> skillsList = gson.fromJson(responseData, skillListType);

          // Convert the list of skills to an array

          activity.runOnUiThread(() -> {
            callback.onSuccess(skillsList);
            Log.d("TAG", "Response: " + responseData);
          });
        } else {
          activity.runOnUiThread(() -> callback.onFailure(new Exception("Request not successful")));
        }
      }
    });
  }
}
