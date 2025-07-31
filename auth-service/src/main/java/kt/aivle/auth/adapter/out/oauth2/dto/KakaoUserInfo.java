package kt.aivle.auth.adapter.out.oauth2.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoUserInfo implements OAuth2UserInfo {

    public KakaoUserInfo(Map<String, Object> attributes) {
        this.id = attributes.get("id") instanceof Long ? (Long) attributes.get("id") :
                Long.valueOf(attributes.get("id").toString());
        this.connectedAt = (String) attributes.get("connected_at");

        Map<String, Object> propertiesMap = (Map<String, Object>) attributes.get("properties");
        if (propertiesMap != null) {
            this.properties = new KakaoProperties(
                    (String) propertiesMap.get("nickname"),
                    (String) propertiesMap.get("profile_image"),
                    (String) propertiesMap.get("thumbnail_image")
            );
        }

        Map<String, Object> kakaoAccountMap = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccountMap != null) {
            Map<String, Object> profileMap = (Map<String, Object>) kakaoAccountMap.get("profile");
            KakaoProfile profile = null;
            if (profileMap != null) {
                profile = new KakaoProfile(
                        (String) profileMap.get("nickname"),
                        (String) profileMap.get("thumbnail_image_url"),
                        (String) profileMap.get("profile_image_url")
                );
            }

            this.kakaoAccount = new KakaoAccount(
                    (Boolean) kakaoAccountMap.get("profile_needs_agreement"),
                    profile,
                    (Boolean) kakaoAccountMap.get("email_needs_agreement"),
                    (Boolean) kakaoAccountMap.get("is_email_valid"),
                    (Boolean) kakaoAccountMap.get("is_email_verified"),
                    (String) kakaoAccountMap.get("email")
            );
        }
    }
    @JsonProperty("id")
    private Long id;              // 카카오 고유 ID

    @JsonProperty("connected_at")
    private String connectedAt;

    @JsonProperty("properties")
    private KakaoProperties properties;

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Override
    public String getProviderId() {
        return id != null ? id.toString() : null;
    }

    @Override
    public String getEmail() {
        return kakaoAccount != null ? kakaoAccount.getEmail() : null;
    }

    @Override
    public String getName() {
        return properties != null ? properties.getNickname() : null;
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KakaoProperties {
        @JsonProperty("nickname")
        private String nickname;

        @JsonProperty("profile_image")
        private String profileImage;

        @JsonProperty("thumbnail_image")
        private String thumbnailImage;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KakaoAccount {
        @JsonProperty("profile_needs_agreement")
        private Boolean profileNeedsAgreement;

        @JsonProperty("profile")
        private KakaoProfile profile;

        @JsonProperty("email_needs_agreement")
        private Boolean emailNeedsAgreement;

        @JsonProperty("is_email_valid")
        private Boolean isEmailValid;

        @JsonProperty("is_email_verified")
        private Boolean isEmailVerified;

        @JsonProperty("email")
        private String email;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KakaoProfile {
        @JsonProperty("nickname")
        private String nickname;

        @JsonProperty("thumbnail_image_url")
        private String thumbnailImageUrl;

        @JsonProperty("profile_image_url")
        private String profileImageUrl;
    }
} 