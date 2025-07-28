package kt.aivle.common.config;

import java.util.List;

public class AuthExcludePaths {
    public static final List<String> EXCLUDE_PATHS = List.of(
        "/api/auth/login",
        "/api/auth/signup", 
        "/api/auth/refresh",
        "/api/auth/logout",
        "/api/oauth2/authorization/**",
        "/api/login/oauth2/code/**"
    );
} 