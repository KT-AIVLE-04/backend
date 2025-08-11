package kt.aivle.gateway.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import kt.aivle.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static kt.aivle.common.code.CommonResponseCode.INVALID_TOKEN;

@Slf4j
@Component
public class JwtUtils {

    // yml 값 직접 주입
    @Value("${jwt.secret}")
    private String secret;

    private Key key;

    @PostConstruct
    protected void init() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims parseClaimsAllowExpired(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", token, e);
            throw new BusinessException(INVALID_TOKEN, "유효하지 않은 토큰입니다.");
        }
    }

    public Claims validateToken(String token) {
        Claims claims = parseClaimsAllowExpired(token);
        if (claims.getExpiration().getTime() < System.currentTimeMillis()) {
            throw new BusinessException(INVALID_TOKEN, "토큰이 만료되었습니다.");
        }
        return claims;
    }

    public String getJti(Claims claims) {
        return claims.getId();
    }
}
