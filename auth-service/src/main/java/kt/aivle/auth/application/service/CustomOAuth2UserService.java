package kt.aivle.auth.application.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import kt.aivle.auth.domain.model.OAuth2UserPrincipal;
import kt.aivle.auth.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final EntityManager entityManager;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        
        log.info("OAuth2 Login: {}, attributes: {}", registrationId, oAuth2User.getAttributes());
        
        // Google OAuth2 사용자 정보 추출
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getAttribute("sub");
        
        // 기존 사용자 확인 또는 새 사용자 생성
        User user = findOrCreateUser(registrationId, providerId, email, name);
        
        return new OAuth2UserPrincipal(user, oAuth2User.getAttributes(), userNameAttributeName);
    }
    
    private User findOrCreateUser(String provider, String providerId, String email, String name) {
        // 기존 OAuth 사용자 확인
        Optional<User> existingUser = entityManager
                .createQuery("SELECT u FROM User u WHERE u.provider = :provider AND u.providerId = :providerId", User.class)
                .setParameter("provider", provider)
                .setParameter("providerId", providerId)
                .getResultList()
                .stream()
                .findFirst();
        
        if (existingUser.isPresent()) {
            return existingUser.get();
        }
        
        // 같은 이메일의 일반 회원 확인
        Optional<User> emailUser = entityManager
                .createQuery("SELECT u FROM User u WHERE u.email = :email AND u.provider IS NULL", User.class)
                .setParameter("email", email)
                .getResultList()
                .stream()
                .findFirst();
        
        if (emailUser.isPresent()) {
            // 기존 회원을 OAuth 회원으로 연동
            User user = emailUser.get();
            User updatedUser = User.builder()
                    .provider(provider)
                    .providerId(providerId)
                    .email(user.getEmail())
                    .name(user.getName())
                    .password(user.getPassword())
                    .phoneNumber(user.getPhoneNumber())
                    .build();
            
            entityManager.merge(updatedUser);
            return updatedUser;
        }
        
        // 새 OAuth 사용자 생성
        User newUser = User.builder()
                .provider(provider)
                .providerId(providerId)
                .email(email)
                .name(name)
                .password(passwordEncoder.encode("oauth_user_" + System.currentTimeMillis()))
                .build();
        
        entityManager.persist(newUser);
        return newUser;
    }
} 