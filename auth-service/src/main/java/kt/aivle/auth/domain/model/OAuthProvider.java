package kt.aivle.auth.domain.model;

public enum OAuthProvider {
    KAKAO, GOOGLE;
    
    public static OAuthProvider fromUrl(String requestUri) {
        if (requestUri.contains("google")) {
            return GOOGLE;
        } else if (requestUri.contains("kakao")) {
            return KAKAO;
        }
        throw new IllegalArgumentException("지원하지 않는 OAuth provider: " + requestUri);
    }
} 