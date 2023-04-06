package com.davidrue.ipa_davidrue_pair_programming_scheduler.domain;

import java.util.List;

public class Expert {
  private int id;
  private String email;
  private String name;
  private List<Integer> skills;
  private boolean lead;

  public Expert(int id, String email, String name, List<Integer> skills, boolean lead) {
    this.id = id;
    this.email = email;
    this.name = name;
    this.skills = skills;
    this.lead = lead;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Integer> getSkills() {
    return skills;
  }

  public void setSkills(List<Integer> skills) {
    this.skills = skills;
  }

  public boolean isLead() {
    return lead;
  }

  public void setLead(boolean lead) {
    this.lead = lead;
  }
}
