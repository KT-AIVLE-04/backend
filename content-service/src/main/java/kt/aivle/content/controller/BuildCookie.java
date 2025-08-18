package kt.aivle.content.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class BuildCookie {

    @Value("${cdn.domain}")
    private String cdnDomain;

    @Value("${cdn.ttl}")
    private int cookieTtl;

    public ResponseCookie buildCfCookie(String nameEqValue, String thumbPrefix) {
        int i = nameEqValue.indexOf('=');
        String name = nameEqValue.substring(0, i);
        String value = nameEqValue.substring(i + 1);
        return ResponseCookie.from(name, value)
                .domain(cdnDomain)
                .path(thumbPrefix)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(Duration.ofSeconds(cookieTtl))
                .build();
    }
}
