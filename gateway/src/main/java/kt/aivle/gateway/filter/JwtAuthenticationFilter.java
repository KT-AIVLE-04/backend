package kt.aivle.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import kt.aivle.common.exception.BusinessException;
import kt.aivle.gateway.config.ExcludePaths;
import kt.aivle.gateway.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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

        // 1. 토큰 추출 (HTTP 헤더에서)
        String token = extractToken(exchange);
        if (token == null) {
            throw new BusinessException(INVALID_TOKEN);
        }

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

    private String extractToken(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        
        // HTTP: Authorization 헤더에서 token 추출
        String token = extractTokenFromHeader(request);
        log.info("[JWT Filter] HTTP token extracted: {}", token != null ? "SUCCESS" : "FAILED");
        return token;
    }



    private String extractTokenFromHeader(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}