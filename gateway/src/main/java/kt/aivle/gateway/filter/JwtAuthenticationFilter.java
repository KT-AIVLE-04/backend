package kt.aivle.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import kt.aivle.common.exception.BusinessException;
import kt.aivle.gateway.config.ExcludePaths;
import kt.aivle.gateway.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static kt.aivle.gateway.exception.GatewayErrorCode.*;

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
        if (ExcludePaths.isPatternMatch(path)) {
            return chain.filter(exchange);
        }

        // 1. Authorization 헤더 확인
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(INVALID_TOKEN);
        }

        String token = authHeader.substring(7);

        // 2. 토큰 검증
        Claims claims;
        try {
            claims = jwtUtils.validateToken(token);
        } catch (ExpiredJwtException e) {
            throw new BusinessException(EXPIRED_TOKEN);
        } catch (Exception e) {
            throw new BusinessException(INTERNAL_ERROR, e);
        }

        // 3. jti 추출
        String jti;
        try {
            jti = jwtUtils.getJti(claims);
        } catch (Exception e) {
            throw new BusinessException(INVALID_TOKEN, e);
        }
        String key = BLACKLIST_PREFIX + jti;

        // 4. 블랙리스트 체크
        return redisTemplate.hasKey(key)
                .flatMap(isBlacklisted -> {
                    if (Boolean.TRUE.equals(isBlacklisted)) {
                        return Mono.error(new BusinessException(BLACKLISTED_TOKEN));
                    }

                    String userId = claims.getSubject();

                    ServerHttpRequest mutatedRequest = exchange.getRequest()
                            .mutate()
                            .header("X-USER-ID", userId)
                            .build();

                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .request(mutatedRequest)
                            .build();

                    return chain.filter(mutatedExchange);
                });
    }

    @Override
    public int getOrder() {
        return -1;
    }
}