package kt.aivle.auth.adapter.out.oauth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoUserInfo {
    private Long id;
    private KakaoAccount kakaoAccount;
    
    @Getter
    @Setter
    public static class KakaoAccount {
        private String email;
        private Profile profile;
        private String emailNeedsAgreement;
        private String profileNeedsAgreement;
        private String profileNicknameNeedsAgreement;
        private String profileImageNeedsAgreement;
        private String nameNeedsAgreement;
        private String name;
        private String phoneNumberNeedsAgreement;
        private String phoneNumber;
        private String ageRangeNeedsAgreement;
        private String ageRange;
        private String birthdayNeedsAgreement;
        private String birthday;
        private String genderNeedsAgreement;
        private String gender;
        private String ciNeedsAgreement;
        private String ci;
        private String ciAuthenticatedAt;
    }
    
    @Getter
    @Setter
    public static class Profile {
        private String nickname;
        private String thumbnailImageUrl;
        private String profileImageUrl;
        private String isDefaultImage;
    }
} 