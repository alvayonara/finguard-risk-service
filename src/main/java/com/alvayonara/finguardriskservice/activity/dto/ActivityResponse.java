package com.alvayonara.finguardriskservice.activity.dto;

import com.alvayonara.finguardriskservice.activity.ActivityType;
import java.util.List;

public record ActivityResponse(
    InsightActivityData insight,
    List<ActivityItem> items,
    String nextCursorTime,
    Long nextCursorId) {
  public record ActivityItem(ActivityType type, Object data) {}
}
