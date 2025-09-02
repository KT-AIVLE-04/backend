package kt.aivle.gateway.config;

import java.util.List;

import org.springframework.util.AntPathMatcher;

public class ExcludePaths {
    private static final ThreadLocal<AntPathMatcher> pathMatcher = ThreadLocal.withInitial(AntPathMatcher::new);

    public static final List<String> EXCLUDE_PATHS = List.of(
            // auth
            "/api/auth/login",
            "/api/auth/signup",
            "/api/auth/refresh",
            "/api/auth/logout",
            "/api/auth/*/login",
            "/api/auth/oauth2/authorization/**",
            "/api/auth/oauth2/code/**",
            "/api/sns/oauth/**/callback/**",

            // WebSocket
            "/api/analytics/report",

            // swagger
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/v3/api-docs/**",
            "/api/auth/v3/api-docs/**",
            "/api/stores/v3/api-docs/**",
            "/api/shorts/v3/api-docs/**",
            "/api/contents/v3/api-docs/**",
            "/api/sns/v3/api-docs/**",
            "/api/analytics/v3/api-docs/**"
    );

    public static boolean isPatternMatch(String path) {
        return EXCLUDE_PATHS.stream().anyMatch(pattern -> pathMatcher.get().match(pattern, path));
    }
}