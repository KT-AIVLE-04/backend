package kt.aivle.auth.adapter.out.oauth2.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoUserInfo implements OAuth2UserInfo {
    
    public KakaoUserInfo(Map<String, Object> attributes) {
        this.id = attributes.get("id") instanceof Long ? (Long) attributes.get("id") : 
                 Long.valueOf(attributes.get("id").toString());
        this.connectedAt = (String) attributes.get("connected_at");
        
        // Properties 설정
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        if (properties != null) {
            this.properties = new KakaoProperties();
            this.properties.setNickname((String) properties.get("nickname"));
            this.properties.setProfileImage((String) properties.get("profile_image"));
            this.properties.setThumbnailImage((String) properties.get("thumbnail_image"));
        }
        
        // KakaoAccount 설정
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount != null) {
            this.kakaoAccount = new KakaoAccount();
            this.kakaoAccount.setEmail((String) kakaoAccount.get("email"));
            this.kakaoAccount.setEmailNeedsAgreement((Boolean) kakaoAccount.get("email_needs_agreement"));
            this.kakaoAccount.setIsEmailValid((Boolean) kakaoAccount.get("is_email_valid"));
            this.kakaoAccount.setIsEmailVerified((Boolean) kakaoAccount.get("is_email_verified"));
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
    @Setter
    public static class KakaoProperties {
        @JsonProperty("nickname")
        private String nickname;
        
        @JsonProperty("profile_image")
        private String profileImage;
        
        @JsonProperty("thumbnail_image")
        private String thumbnailImage;
    }
    
    @Getter
    @Setter
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
    @Setter
    public static class KakaoProfile {
        @JsonProperty("nickname")
        private String nickname;
        
        @JsonProperty("thumbnail_image_url")
        private String thumbnailImageUrl;
        
        @JsonProperty("profile_image_url")
        private String profileImageUrl;
    }
} 