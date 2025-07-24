package kt.aivle.common.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import kt.aivle.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static kt.aivle.common.code.CommonResponseCode.INVALID_TOKEN;

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
        byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.getSecret());
        this.key = Keys.hmacShaKeyFor(keyBytes);
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
            throw new BusinessException(INVALID_TOKEN, "유효하지 않은 토큰입니다.");
        }
    }

    public void validateToken(String token) {
        Claims claims = parseClaimsAllowExpired(token);
        if (claims.getExpiration().getTime() < System.currentTimeMillis()) {
            throw new BusinessException(INVALID_TOKEN, "토큰이 만료되었습니다.");
        }
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