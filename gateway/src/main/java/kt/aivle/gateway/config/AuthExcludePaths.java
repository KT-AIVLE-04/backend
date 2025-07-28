package kt.aivle.gateway.config;

import java.util.List;

public class AuthExcludePaths {
    public static final List<String> EXCLUDE_PATHS = List.of(
        "/api/auth/login",
        "/api/auth/signup",
        "/api/auth/refresh",
        "/api/auth/logout",
        "/api/auth/oauth2/google",
        "/api/auth/oauth2/callback"
    );
}
