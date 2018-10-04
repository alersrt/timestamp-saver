package org.student;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "timestamp")
public class TimestampEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "time")
  private LocalDateTime time;

  public TimestampEntity() {
  }

  public TimestampEntity(LocalDateTime time) {
    this.time = time;
  }

  public LocalDateTime getTime() {
    return time;
  }

  public void setTime(LocalDateTime time) {
    this.time = time;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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
}
