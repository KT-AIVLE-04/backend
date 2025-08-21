package kt.aivle.sns.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class BuildCookie {

    @Value("${cdn.cookie-domain}")
    private String cookieDomain;

    @Value("${cdn.ttl}")
    private int cookieTtl;

    public String buildCfCookieHeader(String nameEqValue, String cookiePath) {
        int i = nameEqValue.indexOf('=');
        String name = nameEqValue.substring(0, i);
        String value = nameEqValue.substring(i + 1);

        String normalizedPath = cookiePath.startsWith("/") ? cookiePath : "/" + cookiePath;

        ResponseCookie base = ResponseCookie.from(name, value)
                .domain(cookieDomain)
                .path(normalizedPath)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(Duration.ofSeconds(cookieTtl))
                .build();

        return base.toString() + "; Partitioned";
    }
}
