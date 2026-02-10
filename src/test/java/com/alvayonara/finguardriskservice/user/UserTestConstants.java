package com.alvayonara.finguardriskservice.user;

import java.time.LocalDateTime;

public final class UserTestConstants {

  private UserTestConstants() {
    throw new UnsupportedOperationException("Utility class");
  }

  // Anonymous IDs
  public static final String EXISTING_ANONYMOUS_ID = "existing-anonymous-id";
  public static final String NEW_ANONYMOUS_ID = "new-anonymous-id";
  public static final String TEST_ANONYMOUS_ID = "test-anonymous-id";
  public static final String ERROR_USER_ID = "error-user";
  public static final String EMPTY_USER_ID = "empty-user";

  public static final String[] VALID_ANONYMOUS_IDS = {
    "user-123",
    "anonymous-456",
    "test-id-789",
    "abc",
    "user_with_underscore",
    "12345",
    "very-long-anonymous-id-with-many-characters-123456789"
  };

  // User IDs (Database)
  public static final Long USER_ID_1 = 1L;
  public static final Long USER_ID_2 = 2L;
  public static final Long USER_ID_100 = 100L;
  public static final Long USER_ID_999 = 999L;
  public static final Long USER_ID_1000 = 1000L;
  public static final Long USER_ID_5000 = 5000L;

  // User UIDs (Application)
  public static final String USER_UID_1 = "usr_test001";
  public static final String USER_UID_2 = "usr_test002";
  public static final String USER_UID_100 = "usr_test100";
  public static final String USER_UID_999 = "usr_test999";
  public static final String USER_UID_1000 = "usr_test1000";
  public static final String USER_UID_5000 = "usr_test5000";

  public static final String DATABASE_ERROR_MESSAGE = "Database connection error";
  public static final String SAVE_ERROR_MESSAGE = "Save failed";
  public static final String SERVICE_ERROR_MESSAGE = "Service error";

  public static final LocalDateTime TEST_CREATED_AT = LocalDateTime.of(2026, 1, 1, 0, 0);

  public static final String ANONYMOUS_USER_ENDPOINT = "/v1/users/anonymous";

  public static final int BAD_REQUEST = 400;
  public static final int SERVER_ERROR = 500;
}
