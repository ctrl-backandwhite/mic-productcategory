package com.backandwhite.infrastructure.client.cj.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.backandwhite.infrastructure.client.cj.dto.CjAccessTokenDataDto;
import com.backandwhite.infrastructure.db.postgres.entity.CjTokenEntity;
import com.backandwhite.infrastructure.db.postgres.repository.CjTokenJpaRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CjTokenManagerTest {

    @Mock
    private CjAuthClient cjAuthClient;

    @Mock
    private CjTokenJpaRepository tokenRepository;

    private CjTokenManager tokenManager;

    @BeforeEach
    void setUp() {
        tokenManager = new CjTokenManager(cjAuthClient, tokenRepository);
    }

    @Test
    void getValidAccessToken_noDbToken_requestsNew() {
        when(tokenRepository.findById("SINGLETON")).thenReturn(Optional.empty());
        CjAccessTokenDataDto newToken = new CjAccessTokenDataDto("new-access", future(2), "new-refresh", future(30));
        when(cjAuthClient.requestNewToken()).thenReturn(newToken);
        when(tokenRepository.save(any(CjTokenEntity.class))).thenAnswer(i -> i.getArgument(0));

        String accessToken = tokenManager.getValidAccessToken();

        assertThat(accessToken).isEqualTo("new-access");
        verify(cjAuthClient).requestNewToken();
    }

    @Test
    void getValidAccessToken_validTokenInDb_reusesIt() {
        CjTokenEntity entity = CjTokenEntity.builder().id("SINGLETON").accessToken("db-access")
                .refreshToken("db-refresh").accessTokenExpiry(Instant.now().plus(2, ChronoUnit.HOURS))
                .refreshTokenExpiry(Instant.now().plus(30, ChronoUnit.DAYS)).build();
        when(tokenRepository.findById("SINGLETON")).thenReturn(Optional.of(entity));

        String accessToken = tokenManager.getValidAccessToken();

        assertThat(accessToken).isEqualTo("db-access");
        verify(cjAuthClient, never()).requestNewToken();
    }

    @Test
    void getValidAccessToken_expiredToken_refreshesUsingRefreshToken() {
        CjTokenEntity entity = CjTokenEntity.builder().id("SINGLETON").accessToken("expired-access")
                .refreshToken("valid-refresh").accessTokenExpiry(Instant.now().minus(1, ChronoUnit.HOURS))
                .refreshTokenExpiry(Instant.now().plus(30, ChronoUnit.DAYS)).build();
        when(tokenRepository.findById("SINGLETON")).thenReturn(Optional.of(entity));
        CjAccessTokenDataDto refreshed = new CjAccessTokenDataDto("refreshed-access", future(2), "refreshed-refresh",
                future(30));
        when(cjAuthClient.refreshAccessToken("valid-refresh")).thenReturn(refreshed);
        when(tokenRepository.save(any(CjTokenEntity.class))).thenAnswer(i -> i.getArgument(0));

        String accessToken = tokenManager.getValidAccessToken();

        assertThat(accessToken).isEqualTo("refreshed-access");
        verify(cjAuthClient).refreshAccessToken("valid-refresh");
    }

    @Test
    void getValidAccessToken_aboutToExpire_proactiveRefresh() {
        CjTokenEntity entity = CjTokenEntity.builder().id("SINGLETON").accessToken("expiring-access")
                .refreshToken("valid-refresh").accessTokenExpiry(Instant.now().plus(15, ChronoUnit.MINUTES))
                .refreshTokenExpiry(Instant.now().plus(30, ChronoUnit.DAYS)).build();
        when(tokenRepository.findById("SINGLETON")).thenReturn(Optional.of(entity));
        CjAccessTokenDataDto refreshed = new CjAccessTokenDataDto("proactive-access", future(2), "proactive-refresh",
                future(30));
        when(cjAuthClient.refreshAccessToken("valid-refresh")).thenReturn(refreshed);
        when(tokenRepository.save(any(CjTokenEntity.class))).thenAnswer(i -> i.getArgument(0));

        String accessToken = tokenManager.getValidAccessToken();

        assertThat(accessToken).isEqualTo("proactive-access");
    }

    @Test
    void getValidAccessToken_aboutToExpire_refreshFails_returnsOldToken() {
        CjTokenEntity entity = CjTokenEntity.builder().id("SINGLETON").accessToken("expiring-access")
                .refreshToken("valid-refresh").accessTokenExpiry(Instant.now().plus(15, ChronoUnit.MINUTES))
                .refreshTokenExpiry(Instant.now().plus(30, ChronoUnit.DAYS)).build();
        when(tokenRepository.findById("SINGLETON")).thenReturn(Optional.of(entity));
        when(cjAuthClient.refreshAccessToken("valid-refresh")).thenThrow(new RuntimeException("Refresh failed"));

        String accessToken = tokenManager.getValidAccessToken();

        assertThat(accessToken).isEqualTo("expiring-access");
    }

    @Test
    void getValidAccessToken_expiredBothTokens_requestsNew() {
        CjTokenEntity entity = CjTokenEntity.builder().id("SINGLETON").accessToken("expired-access")
                .refreshToken("expired-refresh").accessTokenExpiry(Instant.now().minus(2, ChronoUnit.HOURS))
                .refreshTokenExpiry(Instant.now().minus(1, ChronoUnit.HOURS)).build();
        when(tokenRepository.findById("SINGLETON")).thenReturn(Optional.of(entity));
        CjAccessTokenDataDto newToken = new CjAccessTokenDataDto("brand-new", future(2), "brand-refresh", future(30));
        when(cjAuthClient.requestNewToken()).thenReturn(newToken);
        when(tokenRepository.save(any(CjTokenEntity.class))).thenAnswer(i -> i.getArgument(0));

        String accessToken = tokenManager.getValidAccessToken();

        assertThat(accessToken).isEqualTo("brand-new");
        verify(cjAuthClient).requestNewToken();
    }

    @Test
    void getValidAccessToken_cachedOnSecondCall() {
        when(tokenRepository.findById("SINGLETON")).thenReturn(Optional.empty());
        CjAccessTokenDataDto newToken = new CjAccessTokenDataDto("new-access", future(2), "new-refresh", future(30));
        when(cjAuthClient.requestNewToken()).thenReturn(newToken);
        when(tokenRepository.save(any(CjTokenEntity.class))).thenAnswer(i -> i.getArgument(0));

        tokenManager.getValidAccessToken();
        String secondCall = tokenManager.getValidAccessToken();

        assertThat(secondCall).isEqualTo("new-access");
        verify(cjAuthClient, times(1)).requestNewToken();
    }

    @Test
    void scheduledRefresh_noToken_requestsNew() {
        when(tokenRepository.findById("SINGLETON")).thenReturn(Optional.empty());
        CjAccessTokenDataDto newToken = new CjAccessTokenDataDto("scheduled-access", future(2), "scheduled-refresh",
                future(30));
        when(cjAuthClient.requestNewToken()).thenReturn(newToken);
        when(tokenRepository.save(any(CjTokenEntity.class))).thenAnswer(i -> i.getArgument(0));

        tokenManager.scheduledRefresh();

        verify(cjAuthClient).requestNewToken();
    }

    private String future(int days) {
        return Instant.now().plus(days, ChronoUnit.DAYS).toString();
    }
}
