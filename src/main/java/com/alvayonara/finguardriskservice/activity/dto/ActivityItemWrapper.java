package com.alvayonara.finguardriskservice.activity.dto;

import com.alvayonara.finguardriskservice.activity.ActivityType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ActivityItemWrapper implements Comparable<ActivityItemWrapper> {
  private final ActivityType type;
  private final Object data;
  private final LocalDateTime timestamp;
  private final Long id;


  @Override
  public int compareTo(ActivityItemWrapper other) {
    int timeCompare = other.timestamp.compareTo(this.timestamp);
    if (timeCompare != 0) {
      return timeCompare;
    }
    return other.id.compareTo(this.id);
  }
}
