package com.alvayonara.finguardriskservice.user.preference;

import static org.mockito.Mockito.*;

import com.alvayonara.finguardriskservice.user.User;
import com.alvayonara.finguardriskservice.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class UserPreferenceServiceTest {
    @Mock
    private UserRepository userRepository;
    private UserPreferenceService userPreferenceService;

    @BeforeEach
    void setUp() {
        userPreferenceService = new UserPreferenceService(userRepository);
    }

    @ParameterizedTest
    @CsvSource({
            "USD, en,   USD, en",
            "EUR, fr,   EUR, fr",
            "   ,    ,  USD, en",
            "GBP,    ,  GBP, en",
            "   , ja,   USD, ja"
    })
    void get_returnsPreferenceWithDefaults(
            String storedCurrency,
            String storedLanguage,
            String expectedCurrency,
            String expectedLanguage) {
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .preferredCurrency(blankToNull(storedCurrency))
                .preferredLanguage(blankToNull(storedLanguage))
                .build();

        when(userRepository.findById(userId)).thenReturn(Mono.just(user));

        StepVerifier.create(userPreferenceService.get(userId))
                .expectNextMatches(
                        response ->
                                expectedCurrency.trim().equals(response.getCurrency())
                                        && expectedLanguage.trim().equals(response.getLanguage()))
                .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource({
            "USD, en,   EUR, fr,   EUR, fr",
            "USD, en,      ,   ,   USD, en",
            "USD, en,   GBP,    ,  GBP, en",
            "USD, en,      , ja,   USD, ja"
    })
    void update_appliesPartialUpdates(
            String initialCurrency,
            String initialLanguage,
            String requestCurrency,
            String requestLanguage,
            String expectedCurrency,
            String expectedLanguage) {
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .preferredCurrency(blankToNull(initialCurrency))
                .preferredLanguage(blankToNull(initialLanguage))
                .build();

        UserPreferenceRequest request = new UserPreferenceRequest();
        request.setCurrency(blankToNull(requestCurrency));
        request.setLanguage(blankToNull(requestLanguage));

        when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(user));

        StepVerifier.create(userPreferenceService.update(userId, request)).verifyComplete();

        verify(userRepository).save(argThat(saved -> expectedCurrency.trim().equals(saved.getPreferredCurrency())
                && expectedLanguage.trim().equals(saved.getPreferredLanguage())));
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}

