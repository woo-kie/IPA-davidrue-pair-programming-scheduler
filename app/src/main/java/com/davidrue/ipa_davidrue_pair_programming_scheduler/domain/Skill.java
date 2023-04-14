package com.davidrue.ipa_davidrue_pair_programming_scheduler.domain;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a skill with its associated information, including ID, name, type, and the list of users (experts) who possess the skill.
 * Is used to parse data between the ui and the data layer.
 */
public class Skill implements Parcelable {

  // Many fields not used, would be important for future implementation
  private int id;
  private String name;
  private String type;
  private List<Integer> users;

  public Skill(int id, String name, String type, List<Integer> users) {
    this.id = id;
    this.name = name;
    this.type = type;
    this.users = users;
  }

  protected Skill(Parcel in) {
    id = in.readInt();
    name = in.readString();
    type = in.readString();
    users = new ArrayList<>();
    in.readList(users, Integer.class.getClassLoader());
  }

  public static final Creator<Skill> CREATOR = new Creator<Skill>() {
    @Override
    public Skill createFromParcel(Parcel in) {
      return new Skill(in);
    }

    @Override
    public Skill[] newArray(int size) {
      return new Skill[size];
    }
  };

  public int getID() {
    return id;
  }

  public void setID(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public List<Integer> getUsers() {
    return users;
  }

  public void setUsers(List<Integer> users) {
    this.users = users;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeInt(id);
    parcel.writeString(name);
    parcel.writeString(type);
    parcel.writeList(users);
  }
}
