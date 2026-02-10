package com.alvayonara.finguardriskservice.user;

import static com.alvayonara.finguardriskservice.user.UserTestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alvayonara.finguardriskservice.user.dto.AnonymousUserRequest;
import com.alvayonara.finguardriskservice.user.dto.UserResponse;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(UserController.class)
@DisplayName("UserController test")
class UserControllerTest {

  @Autowired private WebTestClient webTestClient;

  @MockBean private UserService userService;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(USER_ID_1);
    testUser.setAnonymousId(TEST_ANONYMOUS_ID);
    testUser.setCreatedAt(LocalDateTime.now());
  }

  @Test
  @DisplayName("Should create anonymous user successfully with valid request")
  void shouldCreateAnonymousUserSuccessfullyWithValidRequest() {
    when(userService.createOrGetAnonymousUser(TEST_ANONYMOUS_ID)).thenReturn(Mono.just(testUser));

    webTestClient
        .post()
        .uri(ANONYMOUS_USER_ENDPOINT)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new AnonymousUserRequest(TEST_ANONYMOUS_ID))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(UserResponse.class)
        .value(
            response -> {
              assertNotNull(response);
              assertEquals(USER_ID_1, response.getUserId());
            });

    verify(userService, times(1)).createOrGetAnonymousUser(TEST_ANONYMOUS_ID);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "user-123",
        "anonymous-456",
        "test-id-789",
        "abc",
        "user_with_underscore",
        "12345",
        "very-long-anonymous-id-with-many-characters-123456789"
      })
  @DisplayName("Should accept various valid anonymous ID formats")
  void shouldAcceptVariousValidAnonymousIdFormats(String anonymousId) {
    User user = new User();
    user.setId(USER_ID_100);
    user.setAnonymousId(anonymousId);
    user.setCreatedAt(LocalDateTime.now());

    when(userService.createOrGetAnonymousUser(anonymousId)).thenReturn(Mono.just(user));

    webTestClient
        .post()
        .uri(ANONYMOUS_USER_ENDPOINT)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new AnonymousUserRequest(anonymousId))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(UserResponse.class)
        .value(
            response -> {
              assertNotNull(response);
              assertEquals(USER_ID_100, response.getUserId());
            });

    verify(userService, times(1)).createOrGetAnonymousUser(anonymousId);
  }

  @ParameterizedTest
  @MethodSource("provideUserIdScenarios")
  @DisplayName("Should return correct user ID for different scenarios")
  void shouldReturnCorrectUserIdForDifferentScenarios(String anonymousId, Long expectedUserId) {
    User user = new User();
    user.setId(expectedUserId);
    user.setAnonymousId(anonymousId);
    user.setCreatedAt(LocalDateTime.now());

    when(userService.createOrGetAnonymousUser(anonymousId)).thenReturn(Mono.just(user));

    webTestClient
        .post()
        .uri(ANONYMOUS_USER_ENDPOINT)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new AnonymousUserRequest(anonymousId))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(UserResponse.class)
        .value(
            response -> {
              assertNotNull(response);
              assertEquals(expectedUserId, response.getUserId());
            });

    verify(userService, times(1)).createOrGetAnonymousUser(anonymousId);
  }

  private static Stream<Arguments> provideUserIdScenarios() {
    return Stream.of(
        Arguments.of("user-1", USER_ID_1),
        Arguments.of("user-2", USER_ID_2),
        Arguments.of("user-100", USER_ID_100),
        Arguments.of("user-999", USER_ID_999),
        Arguments.of("new-user", USER_ID_1000),
        Arguments.of("existing-user", USER_ID_5000));
  }

  @Test
  @DisplayName("Should handle service error gracefully")
  void shouldHandleServiceErrorGracefully() {
    when(userService.createOrGetAnonymousUser(anyString()))
        .thenReturn(Mono.error(new RuntimeException(SERVICE_ERROR_MESSAGE)));

    webTestClient
        .post()
        .uri(ANONYMOUS_USER_ENDPOINT)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new AnonymousUserRequest(ERROR_USER_ID))
        .exchange()
        .expectStatus()
        .is5xxServerError();

    verify(userService, times(1)).createOrGetAnonymousUser(ERROR_USER_ID);
  }

  @Test
  @DisplayName("Should handle empty service response")
  void shouldHandleEmptyServiceResponse() {
    when(userService.createOrGetAnonymousUser(EMPTY_USER_ID)).thenReturn(Mono.empty());

    webTestClient
        .post()
        .uri(ANONYMOUS_USER_ENDPOINT)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new AnonymousUserRequest(EMPTY_USER_ID))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .isEmpty();

    verify(userService, times(1)).createOrGetAnonymousUser(EMPTY_USER_ID);
  }

  @ParameterizedTest
  @MethodSource("provideInvalidRequestBodies")
  @DisplayName("Should validate request body structure")
  void shouldValidateRequestBodyStructure(String requestBody, int expectedStatus) {
    webTestClient
        .post()
        .uri(ANONYMOUS_USER_ENDPOINT)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(requestBody)
        .exchange()
        .expectStatus()
        .value(status -> assertTrue(status >= BAD_REQUEST));
  }

  private static Stream<Arguments> provideInvalidRequestBodies() {
    return Stream.of(
        Arguments.of("{}", BAD_REQUEST),
        Arguments.of("{\"invalidField\": \"value\"}", BAD_REQUEST),
        Arguments.of("{\"anonymousId\": null}", BAD_REQUEST),
        Arguments.of("invalid-json", BAD_REQUEST));
  }

  @Test
  @DisplayName("Should return valid JSON response")
  void shouldReturnValidJsonResponse() {
    when(userService.createOrGetAnonymousUser("json-test")).thenReturn(Mono.just(testUser));

    webTestClient
        .post()
        .uri(ANONYMOUS_USER_ENDPOINT)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new AnonymousUserRequest("json-test"))
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.userId")
        .isNotEmpty()
        .jsonPath("$.userId")
        .isNumber();

    verify(userService, times(1)).createOrGetAnonymousUser("json-test");
  }

  @Test
  @DisplayName("Should accept APPLICATION_JSON content type")
  void shouldAcceptApplicationJsonContentType() {
    when(userService.createOrGetAnonymousUser("content-type-test")).thenReturn(Mono.just(testUser));

    webTestClient
        .post()
        .uri(ANONYMOUS_USER_ENDPOINT)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new AnonymousUserRequest("content-type-test"))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(UserResponse.class)
        .value(
            response -> {
              assertNotNull(response);
              assertEquals(USER_ID_1, response.getUserId());
            });

    verify(userService, times(1)).createOrGetAnonymousUser("content-type-test");
  }

  @Test
  @DisplayName("Should use correct endpoint path")
  void shouldUseCorrectEndpointPath() {
    when(userService.createOrGetAnonymousUser("path-test")).thenReturn(Mono.just(testUser));

    webTestClient
        .post()
        .uri(ANONYMOUS_USER_ENDPOINT)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new AnonymousUserRequest("path-test"))
        .exchange()
        .expectStatus()
        .isOk();

    webTestClient
        .post()
        .uri("/users/anonymous")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new AnonymousUserRequest("path-test"))
        .exchange()
        .expectStatus()
        .isNotFound();

    verify(userService, times(1)).createOrGetAnonymousUser("path-test");
  }
}
