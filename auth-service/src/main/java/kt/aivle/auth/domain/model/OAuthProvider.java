package kt.aivle.auth.domain.model;

import java.util.logging.Logger;

public enum OAuthProvider {
    KAKAO, GOOGLE;
    
    private static final Logger logger = Logger.getLogger(OAuthProvider.class.getName());

    public static OAuthProvider fromUrl(String requestUri) {
        if (requestUri.contains("google")) {
            return GOOGLE;
        } else if (requestUri.contains("kakao")) {
            return KAKAO;
        }
        logger.warning("Unsupported OAuth provider requested. URI: " + requestUri);
        throw new IllegalArgumentException("지원하지 않는 OAuth provider입니다.");
    }
} 