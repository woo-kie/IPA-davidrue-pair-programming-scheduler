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

/**
 The SignInActivity is an activity class responsible for handling the user sign-in process
 using Google authentication. It displays a sign-in button and initializes the
 SignInController to manage the sign-in flow and onActivityResult.
 */
public class SignInActivity extends BaseActivity {

  private SignInController signInController;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sign_in);

    signInController = SignInController.getInstance();
    signInController.initialize(this);

    Button login = this.findViewById(R.id.btn_login);

    login.setOnClickListener(log -> signInController.displaySignIn(this));
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    signInController.onResult(this, requestCode, data);
  }

  @Override
  protected void onStart() {
    super.onStart();
  }

}