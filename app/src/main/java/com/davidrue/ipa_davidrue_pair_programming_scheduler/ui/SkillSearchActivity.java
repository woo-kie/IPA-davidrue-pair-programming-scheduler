package com.davidrue.ipa_davidrue_pair_programming_scheduler.ui;

import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.R;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.data.SkillsApiController;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.databinding.ActivitySkillSearchBinding;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.Skill;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.SkillsArrayCallback;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.io.IOException;
import java.nio.file.StandardWatchEventKinds;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SkillSearchActivity extends AppCompatActivity {
  private final SkillsApiController skillsApiController = SkillsApiController.initialize(this);
  private AutoCompleteTextView autoCompleteTextView;
  private List<Skill> selectedSkills = new ArrayList<>();
  private ActivitySkillSearchBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivitySkillSearchBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    setUpSkillSearch();


  }

  private void setUpSkillSearch(){
    skillsApiController.getSkills(this, new SkillsArrayCallback() {
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
    List<String> skillsAsString = skills.stream().map(Skill::getName).collect(Collectors.toList());
    ArrayAdapter adapter = new ArrayAdapter(this, R.layout.item_drop_down, skillsAsString);
    binding.autoCompleteSkills.setAdapter(adapter);
    binding.autoCompleteSkills.setOnItemClickListener((parent, view, position, id) -> {
      String selectedSkill = (String) parent.getItemAtPosition(position);
      if (selectedSkills.contains(findSkill(selectedSkill, skills))) {
        Toast.makeText(this, "You have already selected that skill :)", Toast.LENGTH_SHORT).show();
      } else {
        addSkillChip(selectedSkill, skills);
      }

    });
  }

  private void addSkillChip(String selectedSkill, List<Skill> skills){
    Skill matchingSkill = findSkill(selectedSkill, skills);
    selectedSkills.add(matchingSkill);
    binding.chipGroup.addView(getChip(matchingSkill));
  }

  private Chip getChip(Skill skill){
    Chip chip = new Chip(this);
    chip.setText(skill.getName());
    chip.setCloseIconVisible(true);

    chip.setOnCloseIconClickListener(chippy -> {
      ((ChipGroup) chippy.getParent()).removeView(chippy);
      selectedSkills.remove(skill);
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

}