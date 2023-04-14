package com.davidrue.ipa_davidrue_pair_programming_scheduler.ui.experts;

import android.content.Intent;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.R;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.data.SkillsAndExpertsApiController;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.Expert;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.Skill;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.BaseActivity;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.ErrorDialog;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.ExpertsListCallback;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.RecyclerViewInterface;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.ToolBarHelper;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.ui.meetings.MeetingSlotsActivity;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Displays a list of experts with the required skills, allowing the user to select an expert for a meeting.
 */
public class ExpertsActivity extends AppCompatActivity implements RecyclerViewInterface {

  private List<Expert> expertsWithSkills;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_experts);
    RecyclerView recyclerView = findViewById(R.id.expertsRecyclerView);
    List<Skill> skills = getIntent().getParcelableArrayListExtra("SKILLS");
    setupExperts(this, skills, recyclerView);
    ToolBarHelper.setUpToolbar(this, "Experts", true, true);
  }

  /**
   * Sets up the list of experts based on the provided skills and populates the recycler view.
   *
   * @param activity    the current activity
   * @param skills      the list of required skills
   * @param recyclerView the recycler view to be populated with the experts
   */
  private void setupExperts(ExpertsActivity activity, List<Skill> skills, RecyclerView recyclerView){
    SkillsAndExpertsApiController.getInstance().getExperts(this, new ExpertsListCallback() {
      @Override
      public void onSuccess(List<Expert> expertsResponse) {
        if(!expertsResponse.isEmpty()){
          expertsWithSkills = getExpertsWithSkills(expertsResponse, skills).stream()
              .sorted(Comparator.comparing(Expert::isLead).reversed()
                  .thenComparing(e -> e.getSkills().size(), Comparator.reverseOrder()))
              .collect(Collectors.toList());

          if (!expertsWithSkills.isEmpty()){
            ExpertsRecyclerViewAdapter adapter = new ExpertsRecyclerViewAdapter(activity, expertsWithSkills, activity);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(activity));
          }else{
            activity.findViewById(R.id.experts_no_results).setVisibility(View.VISIBLE);
          }

        }
      }
      @Override
      public void onFailure(Exception e) {
        ErrorDialog.showError(e, activity);
      }
    });
  }

  /**
   * Filters the list of experts based on the required skills.
   *
   * @param experts        the list of all available experts
   * @param requiredSkills the list of required skills
   * @return a list of experts with the required skills
   */
  public List<Expert> getExpertsWithSkills(List<Expert> experts, List<Skill> requiredSkills) {
    List<Expert> expertsWithSkills = new ArrayList<>();
    List<Integer> requiredSkillIds = requiredSkills.stream().map(Skill::getID).collect(Collectors.toList());
    for (Expert expert : experts) {
      if (new HashSet<>(expert.getSkills()).containsAll(requiredSkillIds)) {
        expertsWithSkills.add(expert);
      }
    }

    return expertsWithSkills;
  }

  /**
   * Handles the click event on an expert item, launching the MeetingSlotsActivity with the selected expert's email.
   *
   * @param position the position of the clicked expert in the list
   */
  @Override
  public void onItemClick(int position) {
    Intent intent = new Intent(ExpertsActivity.this, MeetingSlotsActivity.class);
    intent.putExtra("EXPERT", expertsWithSkills.get(position).getEmail());
    startActivity(intent);
  }
}