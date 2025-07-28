package kt.aivle.common.jwt;

import static kt.aivle.common.code.CommonResponseCode.INTERNAL_SERVER_ERROR;
import static kt.aivle.common.code.CommonResponseCode.INVALID_TOKEN;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import kt.aivle.common.exception.BusinessException;
import kt.aivle.common.exception.InfraException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtUtils {

    private final JwtProperties jwtProperties;
    private Key key;

    public JwtUtils(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    protected void init() {
        String secret = jwtProperties.getSecret();
        log.info("🔑 JWT_SECRET 길이: {}, 끝 3글자: '{}'", secret != null ? secret.length() : 0, secret != null && secret.length() > 0 ? secret.substring(Math.max(0, secret.length() - 3)) : "");
        try {
            byte[] keyBytes = Base64.getDecoder().decode(secret);
            this.key = Keys.hmacShaKeyFor(keyBytes);
            log.info("JWT_SECRET Base64 디코딩 성공! Key 생성 완료");
        } catch (IllegalArgumentException e) {
            log.error("JWT_SECRET Base64 디코딩 실패", e);
            throw new InfraException(INTERNAL_SERVER_ERROR, "JWT_SECRET Base64 디코딩 실패");
        }
    }

    // JWT AccessToken 발급
    public JwtDto generateAccessToken(Long userId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getExpirationMs());
        String jti = UUID.randomUUID().toString();

        String token = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .setId(jti)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return new JwtDto(token, expiry.getTime());
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

    public boolean isExpired(Claims claims) {
        return claims.getExpiration().getTime() <= System.currentTimeMillis();
    }

    public String getJti(Claims claims) {
        return claims.getId();
    }

    public long getExpiration(Claims claims) {
        return claims.getExpiration().getTime();
    }
}