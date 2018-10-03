package org.student;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class Timestamp {

  @Id
  @GeneratedValue
  long id;

  @Column
  LocalDateTime timestamp;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Timestamp timestamp1 = (Timestamp) o;

    if (id != timestamp1.id) {
      return false;
    }
    return timestamp != null ? timestamp.equals(timestamp1.timestamp) : timestamp1.timestamp == null;
  }

  @Override
  public int hashCode() {
    int result = (int) (id ^ (id >>> 32));
    result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
    return result;
  }
}