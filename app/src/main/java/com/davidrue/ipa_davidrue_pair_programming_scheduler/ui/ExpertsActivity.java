package com.davidrue.ipa_davidrue_pair_programming_scheduler.ui;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.R;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.data.SkillsAndExpertsApiController;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.databinding.ActivityExpertsBinding;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.databinding.ActivitySkillSearchBinding;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.Expert;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.Skill;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.callbacks.ExpertsListCallback;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.callbacks.SkillsListCallback;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ExpertsActivity extends AppCompatActivity {

  private final SkillsAndExpertsApiController skillsAndExpertsApiController = SkillsAndExpertsApiController.initialize(this);
  private ActivityExpertsBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_experts);
    binding = ActivityExpertsBinding.inflate(getLayoutInflater());
    RecyclerView recyclerView = findViewById(R.id.expertsRecyclerView);
    List<Skill> skills = getIntent().getParcelableArrayListExtra("SKILLS");
    setupExperts(this, skills, recyclerView);
  }

  private void setupExperts(Context context, List<Skill> skills, RecyclerView recyclerView){
    skillsAndExpertsApiController.getExperts(this, new ExpertsListCallback() {
      @Override
      public void onSuccess(List<Expert> expertsResponse) {
        if(!expertsResponse.isEmpty()){
          List<Expert> expertsWithSkills = getExpertsWithSkills(expertsResponse, skills).stream()
              .sorted(Comparator.comparing(Expert::isLead).reversed()
                  .thenComparing(e -> e.getSkills().size(), Comparator.reverseOrder()))
              .collect(Collectors.toList());


          ExpertsRecyclerViewAdapter adapter = new ExpertsRecyclerViewAdapter(context, expertsWithSkills);
          recyclerView.setAdapter(adapter);
          recyclerView.setLayoutManager(new LinearLayoutManager(context));
        }else{

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
        System.out.println(expert.getName() + " has all required skills"); // Debugging: Print the expert's name if they have all required skills
      }
    }

    return expertsWithSkills;
  }

}