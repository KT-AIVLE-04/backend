package kt.aivle.common.config;

import java.util.List;

import org.springframework.util.AntPathMatcher;

public class AuthExcludePaths {
    private static final ThreadLocal<AntPathMatcher> pathMatcher = ThreadLocal.withInitial(AntPathMatcher::new);

    public static final List<String> EXCLUDE_PATHS = List.of(
        "/api/auth/login",
        "/api/auth/signup", 
        "/api/auth/refresh",
        "/api/auth/logout",
        "/api/oauth2/authorization/**",
        "/api/login/oauth2/code/**"
    );
    public static boolean isPatternMatch(String path) {
        return EXCLUDE_PATHS.stream().anyMatch(pattern -> pathMatcher.get().match(pattern, path));
    }
} 