package com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers;

import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.Skill;
import java.util.List;

/**
 * SkillsListCallback is an interface used for receiving the results of asynchronous
 * API requests to obtain a list of all skills. (SkillsAndExpertsApiController.java)
 * Implement this interface to handle the success or failure of the API call.
 */
public interface SkillsListCallback {
  void onSuccess(List<Skill> skills);
  void onFailure(Exception e);
}
