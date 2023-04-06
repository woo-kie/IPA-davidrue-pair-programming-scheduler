package com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.callbacks;

import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.Skill;
import java.util.List;

public interface SkillsListCallback {
  void onSuccess(List<Skill> skills);
  void onFailure(Exception e);
}
