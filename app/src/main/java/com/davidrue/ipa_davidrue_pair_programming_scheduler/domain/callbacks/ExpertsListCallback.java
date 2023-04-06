package com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.callbacks;

import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.Expert;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.Skill;
import java.util.List;

public interface ExpertsListCallback {
  void onSuccess(List<Expert> experts);
  void onFailure(Exception e);
}
