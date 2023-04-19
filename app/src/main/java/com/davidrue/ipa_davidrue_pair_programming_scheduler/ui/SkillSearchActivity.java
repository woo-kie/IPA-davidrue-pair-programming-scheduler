package com.davidrue.ipa_davidrue_pair_programming_scheduler.ui;

import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.os.Bundle;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.R;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.data.SkillsAndExpertsApiController;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.databinding.ActivitySkillSearchBinding;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.Skill;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.BaseActivity;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.ErrorDialog;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.SkillsListCallback;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.ToolBarHelper;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.ui.experts.ExpertsActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 SkillSearchActivity is an activity class responsible for displaying a skill search interface
 to the user. It fetches a list of skills from the API and allows the user to select skills.
 Users can then search for experts based on their selected skills.
 The activity navigates to ExpertsActivity to display the search results.
 */
public class SkillSearchActivity extends BaseActivity {
  private final List<Skill> selectedSkills = new ArrayList<>();
  private ActivitySkillSearchBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivitySkillSearchBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    updateSearchButton();
    setUpSkillSearch();
    binding.searchExpertsButton.setOnClickListener(search -> searchExperts());
    ToolBarHelper.setUpToolbar(this, "Select Skills", false, true);
  }

  /**
   Sets up the skill search functionality by fetching the list of skills from the API and
   initializing the adapter.
   */
  private void setUpSkillSearch(){
    SkillsAndExpertsApiController.getInstance().getSkills(this, new SkillsListCallback() {
      @Override
      public void onSuccess(List<Skill> skillsResponse) {
        initializeAdapter(skillsResponse);
      }
      @Override
      public void onFailure(Exception e) {
        ErrorDialog.showError(e, SkillSearchActivity.this);
      }
    });
  }

  /**
   Initializes the ArrayAdapter with the fetched skills and sets up an item click listener
   for the AutoCompleteTextView.
   */
  private void initializeAdapter(List<Skill> skills){
    List<String> skillsAsString = skills.stream().map(Skill::getName).sorted().collect(Collectors.toList());
    ArrayAdapter adapter = new ArrayAdapter(this, R.layout.dropdown_menu_item, skillsAsString);
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
    // Add skill to the list of required skills.
    Skill matchingSkill = findSkill(selectedSkill, skills);
    selectedSkills.add(matchingSkill);
    updateSearchButton();
    binding.chipGroup.addView(getChip(matchingSkill));
  }

  private Chip getChip(Skill skill){
    // Creates a Chip for the Chip Group using the Skill.
    Chip chip = new Chip(this);
    chip.setText(skill.getName());
    chip.setCloseIconVisible(true);
    chip.setClickable(false);
    chip.setCloseIcon(getResources().getDrawable(R.drawable.baseline_close_24));
    chip.setTextSize(14);
    chip.setChipBackgroundColorResource(R.color.main_pink);
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

    return foundSkill;
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
      Toast.makeText(this, "Check Internet Connection...", Toast.LENGTH_LONG).show();
    }
  }
}