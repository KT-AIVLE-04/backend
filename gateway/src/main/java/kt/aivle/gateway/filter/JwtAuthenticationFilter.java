package kt.aivle.gateway.filter;

import static kt.aivle.common.config.AuthExcludePaths.EXCLUDE_PATHS;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import kt.aivle.common.jwt.JwtUtils;
import kt.aivle.gateway.exception.GatewayErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtils jwtUtils;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final String BLACKLIST_PREFIX = "blacklist:access_token:";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        log.debug("🔐 인증 필터 실행: {} {}", method, path);

        // 인증 예외 경로면 그냥 통과
        if (isAuthExcludedPath(path)) {
            log.debug("인증 예외 경로 통과: {}", path);
            return chain.filter(exchange);
        }

        // 1. Authorization 헤더 확인
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Authorization 헤더 누락: {}", path);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, GatewayErrorCode.MISSING_AUTH_HEADER.name());
        }
        String token = authHeader.substring(7);

        // 2. 토큰 검증
        Claims claims;
        try {
            claims = jwtUtils.validateToken(token);
            log.debug("토큰 검증 성공: {}", path);
        } catch (ExpiredJwtException e) {
            log.warn("토큰 만료: {}", path);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, GatewayErrorCode.EXPIRED_TOKEN.name());
        } catch (Exception e) {
            log.warn("토큰 검증 실패: {}", path);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, GatewayErrorCode.INVALID_TOKEN.name());
        }

        // 3. jti 추출
        String jti;
        try {
            jti = jwtUtils.getJti(claims);
        } catch (Exception e) {
            log.warn("JTI 추출 실패: {}", path);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, GatewayErrorCode.INVALID_TOKEN.name());
        }
        String key = BLACKLIST_PREFIX + jti;

        // 4. 블랙리스트 체크
        return redisTemplate.hasKey(key)
                .flatMap(isBlacklisted -> {
                    if (Boolean.TRUE.equals(isBlacklisted)) {
                        log.warn("블랙리스트 토큰: {}", path);
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, GatewayErrorCode.BLACKLISTED_TOKEN.name()));
                    }
                    log.debug("인증 완료: {}", path);
                    return chain.filter(exchange);
                });
    }

    // 인증 예외 경로 (AntPathMatcher 사용)
    private boolean isAuthExcludedPath(String path) {
        log.debug("인증 예외 경로 체크: {} (패턴들: {})", path, EXCLUDE_PATHS);
        
        boolean isExcluded = EXCLUDE_PATHS.stream().anyMatch(pattern -> {
            boolean matches = pathMatcher.match(pattern, path);
            log.debug("  패턴 '{}' vs 경로 '{}' -> 매칭: {}", pattern, path, matches);
            return matches;
        });
        
        if (isExcluded) {
            String matchedPattern = EXCLUDE_PATHS.stream()
                .filter(pattern -> pathMatcher.match(pattern, path))
                .findFirst().orElse("");
            log.debug("인증 예외 경로 매칭: {} -> {}", path, matchedPattern);
        } else {
            log.debug("인증 예외 경로 아님: {}", path);
        }
        
        return isExcluded;
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
