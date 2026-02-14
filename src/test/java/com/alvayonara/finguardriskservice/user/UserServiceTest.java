package com.alvayonara.finguardriskservice.user;

import static com.alvayonara.finguardriskservice.user.UserTestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alvayonara.finguardriskservice.security.JwtUtil;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService test")
class UserServiceTest {
  @Mock private UserRepository userRepository;
  @Mock private RefreshTokenRepository refreshTokenRepository;
  @Mock private JwtUtil jwtUtil;
  @InjectMocks private UserService userService;
  private User existingUser;
  private User newUser;

  @BeforeEach
  void setUp() {
    existingUser =
        User.builder()
            .id(USER_ID_1)
            .userUid(USER_UID_1)
            .anonymousId(EXISTING_ANONYMOUS_ID)
            .createdAt(TEST_CREATED_AT)
            .build();
    newUser =
        User.builder()
            .id(USER_ID_2)
            .userUid(USER_UID_2)
            .anonymousId(NEW_ANONYMOUS_ID)
            .createdAt(LocalDateTime.now())
            .build();
  }

  @Test
  @DisplayName("Should return existing user when anonymous ID exists")
  void shouldReturnExistingUserWhenAnonymousIdExists() {
    when(userRepository.findByAnonymousId(EXISTING_ANONYMOUS_ID))
        .thenReturn(Mono.just(existingUser));
    when(jwtUtil.generateAccessToken(anyString(), anyList())).thenReturn("test-access-token");
    when(jwtUtil.generateRefreshToken()).thenReturn("test-refresh-token");
    when(jwtUtil.getRefreshTokenExpirationSeconds()).thenReturn(604800L);
    when(refreshTokenRepository.save(any(RefreshToken.class)))
        .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    StepVerifier.create(userService.createOrGetAnonymousUser(EXISTING_ANONYMOUS_ID))
        .assertNext(
            authResponse -> {
              assertNotNull(authResponse);
              assertEquals(USER_UID_1, authResponse.userUid());
              assertNotNull(authResponse.accessToken());
              assertNotNull(authResponse.refreshToken());
            })
        .verifyComplete();
    verify(userRepository, times(1)).findByAnonymousId(EXISTING_ANONYMOUS_ID);
  }

  @Test
  @DisplayName("Should create new user when anonymous ID does not exist")
  void shouldCreateNewUserWhenAnonymousIdDoesNotExist() {
    when(userRepository.findByAnonymousId(NEW_ANONYMOUS_ID)).thenReturn(Mono.empty());
    when(userRepository.save(any(User.class))).thenReturn(Mono.just(newUser));
    when(jwtUtil.generateAccessToken(anyString(), anyList())).thenReturn("test-access-token");
    when(jwtUtil.generateRefreshToken()).thenReturn("test-refresh-token");
    when(jwtUtil.getRefreshTokenExpirationSeconds()).thenReturn(604800L);
    when(refreshTokenRepository.save(any(RefreshToken.class)))
        .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    StepVerifier.create(userService.createOrGetAnonymousUser(NEW_ANONYMOUS_ID))
        .assertNext(
            authResponse -> {
              assertNotNull(authResponse);
              assertEquals(USER_UID_2, authResponse.userUid());
              assertNotNull(authResponse.accessToken());
              assertNotNull(authResponse.refreshToken());
            })
        .verifyComplete();
    verify(userRepository, times(1)).findByAnonymousId(NEW_ANONYMOUS_ID);
    verify(userRepository, times(1)).save(any(User.class));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "user-123",
        "anonymous-456",
        "test-789",
        "abc123xyz",
        "user_with_underscore",
        "user-with-dashes-and-numbers-123"
      })
  @DisplayName("Should handle various valid anonymous ID formats")
  void shouldHandleVariousValidAnonymousIdFormats(String anonymousId) {
    User testUser =
        User.builder()
            .id(USER_ID_100)
            .userUid(USER_UID_100)
            .anonymousId(anonymousId)
            .createdAt(LocalDateTime.now())
            .build();
    when(userRepository.findByAnonymousId(anonymousId)).thenReturn(Mono.empty());
    when(userRepository.save(any(User.class))).thenReturn(Mono.just(testUser));
    when(jwtUtil.generateAccessToken(anyString(), anyList())).thenReturn("test-access-token");
    when(jwtUtil.generateRefreshToken()).thenReturn("test-refresh-token");
    when(jwtUtil.getRefreshTokenExpirationSeconds()).thenReturn(604800L);
    when(refreshTokenRepository.save(any(RefreshToken.class)))
        .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    StepVerifier.create(userService.createOrGetAnonymousUser(anonymousId))
        .assertNext(
            authResponse -> {
              assertNotNull(authResponse);
              assertEquals(USER_UID_100, authResponse.userUid());
              assertNotNull(authResponse.accessToken());
              assertNotNull(authResponse.refreshToken());
            })
        .verifyComplete();
    verify(userRepository, times(1)).findByAnonymousId(anonymousId);
    verify(userRepository, times(1)).save(any(User.class));
  }

  @ParameterizedTest
  @MethodSource("provideUserScenarios")
  @DisplayName("Should handle different user scenarios")
  void shouldHandleDifferentUserScenarios(String anonymousId, boolean userExists, Long expectedId) {
    User user =
        User.builder()
            .id(expectedId)
            .userUid("usr_" + expectedId)
            .anonymousId(anonymousId)
            .createdAt(LocalDateTime.now())
            .build();
    when(userRepository.findByAnonymousId(anonymousId))
        .thenReturn(userExists ? Mono.just(user) : Mono.empty());
    when(userRepository.save(any(User.class))).thenReturn(Mono.just(user));
    when(jwtUtil.generateAccessToken(anyString(), anyList())).thenReturn("test-access-token");
    when(jwtUtil.generateRefreshToken()).thenReturn("test-refresh-token");
    when(jwtUtil.getRefreshTokenExpirationSeconds()).thenReturn(604800L);
    when(refreshTokenRepository.save(any(RefreshToken.class)))
        .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    StepVerifier.create(userService.createOrGetAnonymousUser(anonymousId))
        .assertNext(
            authResponse -> {
              assertNotNull(authResponse);
              assertEquals("usr_" + expectedId, authResponse.userUid());
              assertNotNull(authResponse.accessToken());
              assertNotNull(authResponse.refreshToken());
            })
        .verifyComplete();
    verify(userRepository, times(1)).findByAnonymousId(anonymousId);
  }

  private static Stream<Arguments> provideUserScenarios() {
    return Stream.of(
        Arguments.of("existing-user-1", true, USER_ID_1),
        Arguments.of("new-user-1", false, USER_ID_2),
        Arguments.of("existing-user-2", true, 3L),
        Arguments.of("new-user-2", false, 4L),
        Arguments.of("user-with-special-chars-!@#", false, 5L));
  }

  @Test
  @DisplayName("Should handle repository error gracefully")
  void shouldHandleRepositoryErrorGracefully() {
    when(userRepository.findByAnonymousId(anyString()))
        .thenReturn(Mono.error(new RuntimeException(DATABASE_ERROR_MESSAGE)));
    when(userRepository.save(any(User.class))).thenReturn(Mono.just(newUser));
    StepVerifier.create(userService.createOrGetAnonymousUser(TEST_ANONYMOUS_ID))
        .expectErrorMatches(
            throwable ->
                throwable instanceof RuntimeException
                    && throwable.getMessage().equals(DATABASE_ERROR_MESSAGE))
        .verify();
    verify(userRepository, times(1)).findByAnonymousId(TEST_ANONYMOUS_ID);
  }

  @Test
  @DisplayName("Should handle save error when creating new user")
  void shouldHandleSaveErrorWhenCreatingNewUser() {
    when(userRepository.findByAnonymousId(ERROR_USER_ID)).thenReturn(Mono.empty());
    when(userRepository.save(any(User.class)))
        .thenReturn(Mono.error(new RuntimeException(SAVE_ERROR_MESSAGE)));
    StepVerifier.create(userService.createOrGetAnonymousUser(ERROR_USER_ID))
        .expectErrorMatches(
            throwable ->
                throwable instanceof RuntimeException
                    && throwable.getMessage().equals(SAVE_ERROR_MESSAGE))
        .verify();
    verify(userRepository, times(1)).findByAnonymousId(ERROR_USER_ID);
    verify(userRepository, times(1)).save(any(User.class));
  }

  @ParameterizedTest
  @MethodSource("provideTimestampScenarios")
  @DisplayName("Should preserve created timestamp for existing users")
  void shouldPreserveCreatedTimestampForExistingUsers(
      String anonymousId, LocalDateTime originalTimestamp) {
    User user =
        User.builder()
            .id(USER_ID_1)
            .userUid(USER_UID_1)
            .anonymousId(anonymousId)
            .createdAt(originalTimestamp)
            .build();
    when(userRepository.findByAnonymousId(anonymousId)).thenReturn(Mono.just(user));
    when(userRepository.save(any(User.class))).thenReturn(Mono.just(user));
    when(jwtUtil.generateAccessToken(anyString(), anyList())).thenReturn("test-access-token");
    when(jwtUtil.generateRefreshToken()).thenReturn("test-refresh-token");
    when(jwtUtil.getRefreshTokenExpirationSeconds()).thenReturn(604800L);
    when(refreshTokenRepository.save(any(RefreshToken.class)))
        .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    StepVerifier.create(userService.createOrGetAnonymousUser(anonymousId))
        .assertNext(
            authResponse -> {
              assertNotNull(authResponse);
              assertEquals(USER_UID_1, authResponse.userUid());
              assertNotNull(authResponse.accessToken());
              assertNotNull(authResponse.refreshToken());
            })
        .verifyComplete();
  }

  private static Stream<Arguments> provideTimestampScenarios() {
    return Stream.of(
        Arguments.of("user-1", LocalDateTime.of(2025, 1, 1, 10, 0)),
        Arguments.of("user-2", LocalDateTime.of(2025, 6, 15, 14, 30)),
        Arguments.of("user-3", LocalDateTime.of(2026, 2, 1, 8, 45)),
        Arguments.of("user-4", LocalDateTime.now().minusDays(7)));
  }
}
