package com.legacyvault.legacyvault;

import com.legacyvault.legacyvault.model.Beneficiary;
import com.legacyvault.legacyvault.model.BeneficiaryAccessToken;
import com.legacyvault.legacyvault.repository.BeneficiaryAccessTokenRepository;
import com.legacyvault.legacyvault.service.BeneficiaryAccessService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BeneficiaryAccessServiceTest {

    private BeneficiaryAccessTokenRepository accessTokenRepository;
    private BeneficiaryAccessService accessService;

    @BeforeEach
    void setUp() {

        accessTokenRepository =
                mock(BeneficiaryAccessTokenRepository.class);

        accessService =
                new BeneficiaryAccessService(
                        accessTokenRepository
                );
    }

    // TEST 1
    // Verified beneficiary should receive an access token
    @Test
    void shouldGenerateAccessTokenForVerifiedBeneficiary() {

        Beneficiary beneficiary =
                mock(Beneficiary.class);

        when(beneficiary.isVerified())
                .thenReturn(true);

        String token =
                accessService.generateAccessToken(
                        beneficiary
                );

        assertNotNull(token);
        assertFalse(token.isBlank());

        verify(accessTokenRepository)
                .save(any(BeneficiaryAccessToken.class));
    }


    // TEST 2
    // Unverified beneficiary must NOT receive token
    @Test
    void shouldRejectUnverifiedBeneficiary() {

        Beneficiary beneficiary =
                mock(Beneficiary.class);

        when(beneficiary.isVerified())
                .thenReturn(false);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                accessService.generateAccessToken(
                                        beneficiary
                                )
                );

        assertEquals(
                409,
                exception.getStatusCode().value()
        );

        verify(
                accessTokenRepository,
                never()
        ).save(any());
    }


    // TEST 3
    // Blank token should be rejected
    @Test
    void shouldRejectBlankToken() {

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                accessService.validateToken("")
                );

        assertEquals(
                401,
                exception.getStatusCode().value()
        );
    }


    // TEST 4
    // Invalid token should be rejected
    @Test
    void shouldRejectInvalidToken() {

        when(accessTokenRepository
                .findByTokenHash(anyString()))
                .thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                accessService.validateToken(
                                        "invalid-token"
                                )
                );

        assertEquals(
                401,
                exception.getStatusCode().value()
        );
    }


    // TEST 5
    // Used token should be rejected
    @Test
    void shouldRejectUsedToken() {

        BeneficiaryAccessToken accessToken =
                mock(BeneficiaryAccessToken.class);

        when(accessToken.isUsed())
                .thenReturn(true);

        when(accessTokenRepository
                .findByTokenHash(anyString()))
                .thenReturn(Optional.of(accessToken));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                accessService.validateToken(
                                        "test-token"
                                )
                );

        assertEquals(
                401,
                exception.getStatusCode().value()
        );
    }


    // TEST 6
    // Expired token should be rejected
    @Test
    void shouldRejectExpiredToken() {

        BeneficiaryAccessToken accessToken =
                mock(BeneficiaryAccessToken.class);

        when(accessToken.isUsed())
                .thenReturn(false);

        when(accessToken.getExpiresAt())
                .thenReturn(
                        LocalDateTime.now()
                                .minusMinutes(1)
                );

        when(accessTokenRepository
                .findByTokenHash(anyString()))
                .thenReturn(Optional.of(accessToken));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                accessService.validateToken(
                                        "expired-token"
                                )
                );

        assertEquals(
                401,
                exception.getStatusCode().value()
        );
    }


    // TEST 7
    // Token belonging to unverified beneficiary should fail
    @Test
    void shouldRejectTokenForUnverifiedBeneficiary() {

        Beneficiary beneficiary =
                mock(Beneficiary.class);

        BeneficiaryAccessToken accessToken =
                mock(BeneficiaryAccessToken.class);

        when(accessToken.isUsed())
                .thenReturn(false);

        when(accessToken.getExpiresAt())
                .thenReturn(
                        LocalDateTime.now()
                                .plusMinutes(10)
                );

        when(accessToken.getBeneficiary())
                .thenReturn(beneficiary);

        when(beneficiary.isVerified())
                .thenReturn(false);

        when(accessTokenRepository
                .findByTokenHash(anyString()))
                .thenReturn(Optional.of(accessToken));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                accessService.validateToken(
                                        "valid-looking-token"
                                )
                );

        assertEquals(
                401,
                exception.getStatusCode().value()
        );
    }


    // TEST 8
    // Valid token should pass validation
    @Test
    void shouldValidateValidToken() {

        Beneficiary beneficiary =
                mock(Beneficiary.class);

        BeneficiaryAccessToken accessToken =
                mock(BeneficiaryAccessToken.class);

        when(accessToken.isUsed())
                .thenReturn(false);

        when(accessToken.getExpiresAt())
                .thenReturn(
                        LocalDateTime.now()
                                .plusMinutes(10)
                );

        when(accessToken.getBeneficiary())
                .thenReturn(beneficiary);

        when(beneficiary.isVerified())
                .thenReturn(true);

        when(accessTokenRepository
                .findByTokenHash(anyString()))
                .thenReturn(Optional.of(accessToken));

        BeneficiaryAccessToken result =
                accessService.validateToken(
                        "valid-token"
                );

        assertSame(
                accessToken,
                result
        );
    }


    // TEST 9
    // Mark token as used
    @Test
    void shouldMarkTokenAsUsed() {

        BeneficiaryAccessToken accessToken =
                mock(BeneficiaryAccessToken.class);

        accessService.markTokenUsed(
                accessToken
        );

        verify(accessToken)
                .setUsed(true);

        verify(accessToken)
                .setUsedAt(any(LocalDateTime.class));

        verify(accessTokenRepository)
                .save(accessToken);
    }
}