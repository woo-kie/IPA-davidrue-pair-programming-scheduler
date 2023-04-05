package com.davidrue.ipa_davidrue_pair_programming_scheduler.domain;

import java.util.List;

public class Skill {
  private int ID;

  private String name;
  private SkillType type;
  private List<Integer> users;

  public Skill(int ID, String name, SkillType type, List<Integer> users) {
    this.ID = ID;
    this.name = name;
    this.type = type;
    this.users = users;
  }

  public int getID() {
    return ID;
  }

  public void setID(int ID) {
    this.ID = ID;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public SkillType getType() {
    return type;
  }

  public void setType(SkillType type) {
    this.type = type;
  }

  public List<Integer> getUsers() {
    return users;
  }

  public void setUsers(List<Integer> users) {
    this.users = users;
  }
}

enum SkillType {
  SOFT,
  LEAD,
  SPECIALITY
}

