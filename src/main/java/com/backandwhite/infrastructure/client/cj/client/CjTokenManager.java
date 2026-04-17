package com.backandwhite.infrastructure.client.cj.client;

import com.backandwhite.infrastructure.client.cj.dto.CjAccessTokenDataDto;
import com.backandwhite.infrastructure.db.postgres.entity.CjTokenEntity;
import com.backandwhite.infrastructure.db.postgres.repository.CjTokenJpaRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Manages the CJ Dropshipping token lifecycle with DB persistence:
 * <ul>
 * <li>Persists accessToken, refreshToken and expiration dates in the cj_tokens
 * table</li>
 * <li>On startup, loads the last token from DB (survives restarts)</li>
 * <li>If the token is still valid, reuses it without calling CJ</li>
 * <li>If it is close to expiring (&lt;30 min), performs proactive refresh</li>
 * <li>If it is expired, requests a new token</li>
 * <li>Respects the CJ 5-minute cooldown for new tokens</li>
 * <li>Automatically refreshes every hour (via @Scheduled)</li>
 * </ul>
 */
@Log4j2
@Component
public class CjTokenManager {

    private static final String SINGLETON_ID = "SINGLETON";
    private static final DateTimeFormatter CJ_DATE_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    /** CJ only allows requesting a new token every 5 minutes */
    private static final long TOKEN_REQUEST_COOLDOWN_SECONDS = 5L * 60;
    /** Margin for proactive refresh: 30 minutes before expiry */
    private static final long PROACTIVE_REFRESH_MINUTES = 30;

    private final CjAuthClient cjAuthClient;
    private final CjTokenJpaRepository tokenRepository;
    private final ReentrantLock lock = new ReentrantLock();

    // In-memory cache synchronized with the DB
    private String cachedAccessToken;
    private String cachedRefreshToken;
    private Instant accessTokenExpiry;
    private Instant refreshTokenExpiry;
    private Instant lastTokenRequestTime;
    private boolean loadedFromDb = false;

    public CjTokenManager(CjAuthClient cjAuthClient, CjTokenJpaRepository tokenRepository) {
        this.cjAuthClient = cjAuthClient;
        this.tokenRepository = tokenRepository;
    }

    /**
     * Returns a valid access token. 1. If there is a cached token and it is valid →
     * returns it. 2. If it is close to expiring → attempts proactive refresh. 3. If
     * it is expired → requests a new one (respecting cooldown).
     */
    public String getValidAccessToken() {
        lock.lock();
        try {
            // Load from DB on first access
            if (!loadedFromDb) {
                loadFromDatabase();
                loadedFromDb = true;
            }

            // Valid token far from expiry → reuse
            if (cachedAccessToken != null && !isExpired()) {
                if (isAboutToExpire()) {
                    log.info("Access token about to expire in <{} min, attempting proactive refresh...",
                            PROACTIVE_REFRESH_MINUTES);
                    try {
                        doRefresh();
                    } catch (Exception e) {
                        log.warn("Proactive refresh failed ({}), continuing with current token", e.getMessage());
                    }
                } else {
                    long minutesLeft = ChronoUnit.MINUTES.between(Instant.now(), accessTokenExpiry);
                    log.debug("Using persisted CJ access token (expires in {} min)", minutesLeft);
                }
                return cachedAccessToken;
            }

            // Token expired → attempt refresh if refresh token is valid
            if (cachedRefreshToken != null && !isRefreshTokenExpired()) {
                log.info("Access token expired, attempting refresh...");
                try {
                    return doRefresh();
                } catch (Exception e) {
                    log.warn("Refresh failed ({}), requesting new token...", e.getMessage());
                }
            }

            // No valid token → request new one
            log.info("No valid access token available, requesting new token...");
            return doNewToken();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Scheduled task: runs every hour (3_600_000 ms). Proactively refreshes the
     * token and logs its status.
     */
    @Scheduled(fixedRate = 3_600_000, initialDelay = 10_000)
    public void scheduledRefresh() {
        lock.lock();
        try {
            log.info("══════ CJ Token Scheduled Refresh ══════");

            if (!loadedFromDb) {
                loadFromDatabase();
                loadedFromDb = true;
            }

            if (cachedAccessToken == null) {
                log.info("No token in DB, requesting initial token...");
                doNewToken();
                logTokenStatus();
                log.info("══════════════════════════════════════════");
                return;
            }

            logTokenStatus();

            if (cachedRefreshToken != null && !isRefreshTokenExpired()) {
                log.info("Refreshing access token using refresh token...");
                try {
                    doRefresh();
                    log.info("Token refreshed successfully and persisted to DB.");
                } catch (Exception e) {
                    log.warn("Refresh failed ({}), requesting new token...", e.getMessage());
                    doNewToken();
                }
            } else {
                log.info("Refresh token expired or unavailable, requesting new token...");
                doNewToken();
            }

            logTokenStatus();
            log.info("══════════════════════════════════════════");
        } finally {
            lock.unlock();
        }
    }

    // ── DB Persistence ─────────────────────────────────────────

    /**
     * Loads the persisted token from the database into the in-memory cache.
     */
    private void loadFromDatabase() {
        try {
            tokenRepository.findById(SINGLETON_ID).ifPresentOrElse(entity -> {
                this.cachedAccessToken = entity.getAccessToken();
                this.cachedRefreshToken = entity.getRefreshToken();
                this.accessTokenExpiry = entity.getAccessTokenExpiry();
                this.refreshTokenExpiry = entity.getRefreshTokenExpiry();
                this.lastTokenRequestTime = entity.getLastTokenRequestTime();

                if (cachedAccessToken != null && !isExpired()) {
                    long minutesLeft = ChronoUnit.MINUTES.between(Instant.now(), accessTokenExpiry);
                    log.info("Loaded valid CJ token from DB (expires in {} min)", minutesLeft);
                } else if (cachedAccessToken != null) {
                    log.info("Loaded expired CJ token from DB, will need renewal");
                } else {
                    log.info("No token data in DB row");
                }
            }, () -> log.info("No CJ token found in DB, will request on first API call"));
        } catch (Exception e) {
            log.warn("Could not load CJ token from DB: {}. Will request new token.", e.getMessage());
        }
    }

    /**
     * Persists the current token state to the database.
     */
    private void saveToDatabase() {
        try {
            CjTokenEntity entity = CjTokenEntity.builder().id(SINGLETON_ID).accessToken(cachedAccessToken)
                    .refreshToken(cachedRefreshToken).accessTokenExpiry(accessTokenExpiry)
                    .refreshTokenExpiry(refreshTokenExpiry).lastTokenRequestTime(lastTokenRequestTime).build();
            tokenRepository.save(entity);
            log.debug("CJ token persisted to DB");
        } catch (Exception e) {
            log.error("Failed to persist CJ token to DB: {}", e.getMessage());
            // Do not throw — in-memory token still works
        }
    }

    // ── Internal methods ────────────────────────────────────────

    private String doRefresh() {
        CjAccessTokenDataDto data = cjAuthClient.refreshAccessToken(cachedRefreshToken);
        storeTokenData(data);
        return cachedAccessToken;
    }

    private String doNewToken() {
        // Cooldown: CJ only allows requesting a token every 5 minutes
        if (lastTokenRequestTime != null) {
            long elapsed = ChronoUnit.SECONDS.between(lastTokenRequestTime, Instant.now());
            if (elapsed < TOKEN_REQUEST_COOLDOWN_SECONDS) {
                long remaining = TOKEN_REQUEST_COOLDOWN_SECONDS - elapsed;
                if (cachedAccessToken != null) {
                    log.warn("Token request cooldown active ({} s remaining). Reusing cached token.", remaining);
                    return cachedAccessToken;
                }
                log.warn("Token request cooldown active ({} s remaining) and no cached token. Waiting is required.",
                        remaining);
                throw new RuntimeException(
                        "CJ token cooldown: must wait " + remaining + "s before requesting a new token");
            }
        }
        lastTokenRequestTime = Instant.now();
        CjAccessTokenDataDto data = cjAuthClient.requestNewToken();
        storeTokenData(data);
        return cachedAccessToken;
    }

    private void storeTokenData(CjAccessTokenDataDto data) {
        this.cachedAccessToken = data.getAccessToken();
        this.cachedRefreshToken = data.getRefreshToken();
        this.accessTokenExpiry = parseToInstant(data.getAccessTokenExpiryDate());
        this.refreshTokenExpiry = parseToInstant(data.getRefreshTokenExpiryDate());
        // Persist in DB
        saveToDatabase();
    }

    private void logTokenStatus() {
        long minutesLeft = accessTokenExpiry != null
                ? ChronoUnit.MINUTES.between(Instant.now(), accessTokenExpiry)
                : -1;
        long refreshMinutesLeft = refreshTokenExpiry != null
                ? ChronoUnit.MINUTES.between(Instant.now(), refreshTokenExpiry)
                : -1;

        log.info("  AccessToken  : {}...{} | expires: {} ({} min left)", maskStart(), maskEnd(), accessTokenExpiry,
                minutesLeft);
        log.info("  RefreshToken : {}...{} | expires: {} ({} min left)", maskStart(cachedRefreshToken),
                maskEnd(cachedRefreshToken), refreshTokenExpiry, refreshMinutesLeft);
    }

    private boolean isExpired() {
        if (accessTokenExpiry == null)
            return true;
        return Instant.now().isAfter(accessTokenExpiry);
    }

    private boolean isAboutToExpire() {
        if (accessTokenExpiry == null)
            return false;
        return Instant.now().plus(PROACTIVE_REFRESH_MINUTES, ChronoUnit.MINUTES).isAfter(accessTokenExpiry);
    }

    private boolean isRefreshTokenExpired() {
        if (refreshTokenExpiry == null)
            return true;
        return Instant.now().isAfter(refreshTokenExpiry);
    }

    private Instant parseToInstant(String dateStr) {
        if (dateStr == null || dateStr.isBlank())
            return null;
        try {
            // Try ISO offset format first: 2026-04-03T17:09:34+08:00
            return OffsetDateTime.parse(dateStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant();
        } catch (Exception e1) {
            try {
                // Fallback: legacy CJ format yyyy/MM/dd HH:mm:ss (assume UTC)
                return LocalDateTime.parse(dateStr, CJ_DATE_FMT).toInstant(ZoneOffset.UTC);
            } catch (Exception e2) {
                log.warn("Could not parse CJ date '{}', treating as null", dateStr);
                return null;
            }
        }
    }

    private String maskStart() {
        return maskStart(cachedAccessToken);
    }

    private String maskEnd() {
        return maskEnd(cachedAccessToken);
    }

    private String maskStart(String token) {
        if (token == null || token.length() <= 8)
            return "****";
        return token.substring(0, 4);
    }

    private String maskEnd(String token) {
        if (token == null || token.length() <= 8)
            return "****";
        return token.substring(token.length() - 4);
    }
}
