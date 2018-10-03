package org.student;

import java.time.LocalDateTime;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "timestamp", schema = "testdb")
public class TimestampEntity {

  private LocalDateTime time;

  @Basic
  @Column(name = "time", nullable = true, length = -1)
  public LocalDateTime getTime() {
    return time;
  }

  public void setTime(LocalDateTime time) {
    this.time = time;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TimestampEntity that = (TimestampEntity) o;

    if (time != null ? !time.equals(that.time) : that.time != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return time != null ? time.hashCode() : 0;
  }

  private Long id;

  @Id
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
}
