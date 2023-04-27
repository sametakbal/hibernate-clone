package org.sametakbal.entity;

import org.hibernate.annotation.Column;
import org.hibernate.annotation.Id;
import org.hibernate.annotation.Table;

import java.util.Date;

@Table(name = "category")
public class Category {


  public Category() {
  }

  public Category(Integer id, String name, Date lastUpdate) {
  this.id = id;
  this.name = name;
  this.lastUpdate = lastUpdate;
  }

  @Id
  @Column(name = "category_id")
  private Integer id;
  @Column(name = "name")
  private String name;
  @Column(name = "last_update")
  private Date lastUpdate;

  public Integer getId() {
  return id;
  }

  public void setId(Integer id) {
  this.id = id;
  }

  public String getName() {
  return name;
  }

  public void setName(String name) {
  this.name = name;
  }

  public Date getLastUpdate() {
  return lastUpdate;
  }

  public void setLastUpdate(Date lastUpdate) {
  this.lastUpdate = lastUpdate;
  }

  @Override
  public String toString() {
    return "Category{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", lastUpdate=" + lastUpdate +
            '}';
  }
}
