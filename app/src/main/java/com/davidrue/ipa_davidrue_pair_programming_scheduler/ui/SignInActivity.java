package com.davidrue.ipa_davidrue_pair_programming_scheduler.ui;

import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.R;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.data.SignInController;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.BaseActivity;

public class SignInActivity extends BaseActivity {

  private SignInController signInController;
  private TextView loggedState;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sign_in);

    signInController = SignInController.getInstance();
    signInController.initialize(this);

    Button login = this.findViewById(R.id.btn_login);

    login.setOnClickListener(log -> signInController.displaySignIn(this));
    // MeetingSlotFinder meetingSlotFinder = new MeetingSlotFinder();
    // login.setOnClickListener(log -> meetingSlotFinder.doSomething());

    loggedState = this.findViewById(R.id.login_state);
  }

  @Override
  protected void onResume() {
    super.onResume();
    //signInController.onResume(this);
    loggedState.setText("Login State: " + signInController.getUserNameFromSharedPrefs(this));
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    signInController.onResult(this, requestCode, data);
  }

  @Override
  protected void onStart() {
    super.onStart();
    //signInController.onStart(this);
  }

}