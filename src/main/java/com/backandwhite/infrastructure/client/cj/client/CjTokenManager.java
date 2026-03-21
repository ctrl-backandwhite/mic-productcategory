package com.backandwhite.infrastructure.client.cj.client;

import com.backandwhite.infrastructure.client.cj.dto.CjAccessTokenDataDto;
import com.backandwhite.infrastructure.db.postgres.entity.CjTokenEntity;
import com.backandwhite.infrastructure.db.postgres.repository.CjTokenJpaRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Gestiona el ciclo de vida del token de CJ Dropshipping con persistencia en
 * DB:
 * <ul>
 * <li>Persiste accessToken, refreshToken y fechas de expiración en tabla
 * cj_tokens</li>
 * <li>Al iniciar, carga el último token de la DB (sobrevive a reinicios)</li>
 * <li>Si el token está vigente, lo reutiliza sin llamar a CJ</li>
 * <li>Si le falta poco para vencer (&lt;30 min), hace refresh proactivo</li>
 * <li>Si está vencido, solicita un token nuevo</li>
 * <li>Respeta el cooldown de 5 minutos de CJ para nuevos tokens</li>
 * <li>Refresca automáticamente cada hora (vía @Scheduled)</li>
 * </ul>
 */
@Log4j2
@Component
public class CjTokenManager {

    private static final String SINGLETON_ID = "SINGLETON";
    private static final DateTimeFormatter CJ_DATE_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    /** CJ solo permite solicitar un token nuevo cada 5 minutos */
    private static final long TOKEN_REQUEST_COOLDOWN_SECONDS = 5 * 60;
    /** Margen para refresh proactivo: 30 minutos antes del vencimiento */
    private static final long PROACTIVE_REFRESH_MINUTES = 30;

    private final CjAuthClient cjAuthClient;
    private final CjTokenJpaRepository tokenRepository;
    private final ReentrantLock lock = new ReentrantLock();

    // Caché en memoria sincronizada con la DB
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
     * Devuelve un access token válido.
     * 1. Si hay token en caché y es válido → lo retorna.
     * 2. Si le falta poco para vencer → intenta refresh proactivo.
     * 3. Si está vencido → solicita uno nuevo (respetando cooldown).
     */
    public String getValidAccessToken() {
        lock.lock();
        try {
            // Cargar de DB en el primer acceso
            if (!loadedFromDb) {
                loadFromDatabase();
                loadedFromDb = true;
            }

            // Token válido y lejos de vencer → reusar
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

            // Token vencido → intentar refresh si hay refresh token válido
            if (cachedRefreshToken != null && !isRefreshTokenExpired()) {
                log.info("Access token expired, attempting refresh...");
                try {
                    return doRefresh();
                } catch (Exception e) {
                    log.warn("Refresh failed ({}), requesting new token...", e.getMessage());
                }
            }

            // Sin token válido → solicitar nuevo
            log.info("No valid access token available, requesting new token...");
            return doNewToken();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Tarea programada: se ejecuta cada hora (3_600_000 ms).
     * Refresca el token proactivamente y loguea el estado.
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

    // ── Persistencia DB ─────────────────────────────────────────

    /**
     * Carga el token persistido de la base de datos al caché en memoria.
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
     * Persiste el estado actual del token en la base de datos.
     */
    private void saveToDatabase() {
        try {
            CjTokenEntity entity = CjTokenEntity.builder()
                    .id(SINGLETON_ID)
                    .accessToken(cachedAccessToken)
                    .refreshToken(cachedRefreshToken)
                    .accessTokenExpiry(accessTokenExpiry)
                    .refreshTokenExpiry(refreshTokenExpiry)
                    .lastTokenRequestTime(lastTokenRequestTime)
                    .build();
            tokenRepository.save(entity);
            log.debug("CJ token persisted to DB");
        } catch (Exception e) {
            log.error("Failed to persist CJ token to DB: {}", e.getMessage());
            // No lanzar — el token en memoria sigue funcionando
        }
    }

    // ── Métodos internos ────────────────────────────────────────

    private String doRefresh() {
        CjAccessTokenDataDto data = cjAuthClient.refreshAccessToken(cachedRefreshToken);
        storeTokenData(data);
        return cachedAccessToken;
    }

    private String doNewToken() {
        // Cooldown: CJ solo permite pedir token cada 5 minutos
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
        // Persistir en DB
        saveToDatabase();
    }

    private void logTokenStatus() {
        long minutesLeft = accessTokenExpiry != null
                ? ChronoUnit.MINUTES.between(Instant.now(), accessTokenExpiry)
                : -1;
        long refreshMinutesLeft = refreshTokenExpiry != null
                ? ChronoUnit.MINUTES.between(Instant.now(), refreshTokenExpiry)
                : -1;

        log.info("  AccessToken  : {}...{} | expires: {} ({} min left)",
                maskStart(), maskEnd(), accessTokenExpiry, minutesLeft);
        log.info("  RefreshToken : {}...{} | expires: {} ({} min left)",
                maskStart(cachedRefreshToken), maskEnd(cachedRefreshToken),
                refreshTokenExpiry, refreshMinutesLeft);
    }

    private boolean isExpired() {
        if (accessTokenExpiry == null)
            return true;
        return Instant.now().isAfter(accessTokenExpiry);
    }

    private boolean isAboutToExpire() {
        if (accessTokenExpiry == null)
            return false;
        return Instant.now().plus(PROACTIVE_REFRESH_MINUTES, ChronoUnit.MINUTES)
                .isAfter(accessTokenExpiry);
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
