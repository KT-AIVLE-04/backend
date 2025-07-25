package kt.aivle.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import kt.aivle.common.jwt.JwtUtils;
import kt.aivle.gateway.exception.GatewayErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static kt.aivle.gateway.config.AuthExcludePaths.EXCLUDE_PATHS;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtils jwtUtils;
    private final ReactiveStringRedisTemplate redisTemplate;

    private static final String BLACKLIST_PREFIX = "blacklist:access_token:";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 인증 예외 경로면 그냥 통과
        if (isAuthExcludedPath(path)) {
            return chain.filter(exchange);
        }

        // 1. Authorization 헤더 확인
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, GatewayErrorCode.MISSING_AUTH_HEADER.name());
        }
        String token = authHeader.substring(7);

        // 2. 토큰 검증
        Claims claims;
        try {
            claims = jwtUtils.validateToken(token);
        } catch (ExpiredJwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, GatewayErrorCode.EXPIRED_TOKEN.name());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, GatewayErrorCode.INVALID_TOKEN.name());
        }

        // 3. jti 추출
        String jti;
        try {
            jti = jwtUtils.getJti(claims);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, GatewayErrorCode.INVALID_TOKEN.name());
        }
        String key = BLACKLIST_PREFIX + jti;

        // 4. 블랙리스트 체크
        return redisTemplate.hasKey(key)
                .flatMap(isBlacklisted -> {
                    if (Boolean.TRUE.equals(isBlacklisted)) {
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, GatewayErrorCode.BLACKLISTED_TOKEN.name()));
                    }
                    return chain.filter(exchange);
                });
    }

    // 인증 예외 경로 (추가 필요하면 여기에)
    private boolean isAuthExcludedPath(String path) {
        return EXCLUDE_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
