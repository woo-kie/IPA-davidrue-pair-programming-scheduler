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
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.ExpertsListCallback;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.RecyclerViewInterface;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.ToolBarHelper;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.ui.meetings.MeetingSlotsActivity;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ExpertsActivity extends AppCompatActivity implements RecyclerViewInterface {

  private final SkillsAndExpertsApiController skillsAndExpertsApiController = SkillsAndExpertsApiController.initialize(this);

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

  private void setupExperts(ExpertsActivity activity, List<Skill> skills, RecyclerView recyclerView){
    skillsAndExpertsApiController.getExperts(this, new ExpertsListCallback() {
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
        // Handle the error here
      }
    });
  }

  public List<Expert> getExpertsWithSkills(List<Expert> experts, List<Skill> requiredSkills) {
    List<Expert> expertsWithSkills = new ArrayList<>();
    List<Integer> requiredSkillIds = requiredSkills.stream().map(Skill::getID).collect(Collectors.toList());

    System.out.println("Required Skill IDs: " + requiredSkillIds); // Debugging: Print the required skill IDs

    for (Expert expert : experts) {// Debugging: Print each expert's skill IDs
      // Check if the user's skill IDs contain all the required skill IDs
      if (expert.getSkills().containsAll(requiredSkillIds)) {
        expertsWithSkills.add(expert);
      }
    }

    return expertsWithSkills;
  }

  @Override
  public void onItemClick(int position) {
    Intent intent = new Intent(ExpertsActivity.this, MeetingSlotsActivity.class);
    intent.putExtra("EXPERT", expertsWithSkills.get(position).getEmail());
    startActivity(intent);
  }
}