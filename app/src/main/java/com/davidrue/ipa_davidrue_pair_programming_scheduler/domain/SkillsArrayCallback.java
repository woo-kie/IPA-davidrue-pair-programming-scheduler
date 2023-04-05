package com.davidrue.ipa_davidrue_pair_programming_scheduler.domain;

import java.util.List;

public interface SkillsArrayCallback {
  void onSuccess(List<Skill> skills);
  void onFailure(Exception e);
}
