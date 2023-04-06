package com.davidrue.ipa_davidrue_pair_programming_scheduler.ui;

import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.R;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.data.SkillsAndExpertsApiController;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.databinding.ActivitySkillSearchBinding;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.Skill;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.callbacks.SkillsListCallback;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SkillSearchActivity extends AppCompatActivity {
  private final SkillsAndExpertsApiController skillsAndExpertsApiController = SkillsAndExpertsApiController.initialize(this);
  private List<Skill> selectedSkills = new ArrayList<>();
  private ActivitySkillSearchBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivitySkillSearchBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    updateSearchButton();
    setUpSkillSearch();
    binding.searchExpertsButton.setOnClickListener(search -> searchExperts());

  }

  private void setUpSkillSearch(){
    skillsAndExpertsApiController.getSkills(this, new SkillsListCallback() {
      @Override
      public void onSuccess(List<Skill> skillsResponse) {
        initializeAdapter(skillsResponse);
      }
      @Override
      public void onFailure(Exception e) {
        // Handle the error here
      }
    });
  }

  private void initializeAdapter(List<Skill> skills){
    List<String> skillsAsString = skills.stream().map(Skill::getName).sorted().collect(Collectors.toList());
    ArrayAdapter adapter = new ArrayAdapter(this, R.layout.item_drop_down, skillsAsString);
    binding.autoCompleteSkills.setAdapter(adapter);
    binding.autoCompleteSkills.setOnItemClickListener((parent, view, position, id) -> {
      String selectedSkill = (String) parent.getItemAtPosition(position);
      if (selectedSkills.contains(findSkill(selectedSkill, skills))) {
        Toast.makeText(this, "You have already selected this skill :)", Toast.LENGTH_LONG).show();
      } else if (selectedSkills.size() >= 10){
        Toast.makeText(this, "Please do not select more than 10 skills", Toast.LENGTH_LONG).show();
      }
      else {
        addSkillChip(selectedSkill, skills);
        binding.autoCompleteSkills.setText("");
      }

    });
  }

  private void addSkillChip(String selectedSkill, List<Skill> skills){
    Skill matchingSkill = findSkill(selectedSkill, skills);
    selectedSkills.add(matchingSkill);
    updateSearchButton();
    binding.chipGroup.addView(getChip(matchingSkill));
  }

  private Chip getChip(Skill skill){
    Chip chip = new Chip(this);
    chip.setText(skill.getName());
    chip.setCloseIconVisible(true);
    chip.setOnCloseIconClickListener(chippy -> {
      ((ChipGroup) chippy.getParent()).removeView(chippy);
      selectedSkills.remove(skill);
      updateSearchButton();
    });

    return chip;
  }

  private Skill findSkill(String skillName, List<Skill> skills){
    Skill foundSkill = null;

    for (Skill skill : skills) {
      if (skill.getName().equals(skillName)) {
        foundSkill = skill;
        break;
      }
    }

    if (foundSkill != null) {
      return foundSkill;
    }
    return null;
  }

  private void updateSearchButton(){
    binding.searchExpertsButton.setEnabled(selectedSkills.size() > 0);
    binding.searchExpertsButton.setClickable(selectedSkills.size() > 0);
  }

  private void searchExperts(){
    if(!selectedSkills.isEmpty()){
      Intent intent = new Intent(SkillSearchActivity.this, ExpertsActivity.class);
      intent.putParcelableArrayListExtra("SKILLS", new ArrayList<>(selectedSkills));
      startActivity(intent);
    }else{
      Toast.makeText(this, "I do not understand how you got here :(", Toast.LENGTH_LONG).show();
    }
  }
}