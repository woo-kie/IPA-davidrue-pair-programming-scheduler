package com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers;

import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.Expert;
import java.util.List;

/**
 * ExpertsListCallback is an interface used for receiving the results of asynchronous
 * API requests to obtain a list of experts. (SkillsAndExpertsApiController.java)
 * Implement this interface to handle the success or failure of the API call.
 */
public interface ExpertsListCallback {
  void onSuccess(List<Expert> experts);
  void onFailure(Exception e);
}
