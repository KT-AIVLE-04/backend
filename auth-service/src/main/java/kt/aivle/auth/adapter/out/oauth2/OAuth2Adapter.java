package kt.aivle.auth.adapter.out.oauth2;

import static kt.aivle.common.code.CommonResponseCode.BAD_REQUEST;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import kt.aivle.auth.adapter.out.oauth2.dto.GoogleTokenResponse;
import kt.aivle.auth.adapter.out.oauth2.dto.GoogleUserInfo;
import kt.aivle.auth.adapter.out.oauth2.dto.KakaoTokenResponse;
import kt.aivle.auth.adapter.out.oauth2.dto.KakaoUserInfo;
import kt.aivle.auth.adapter.out.oauth2.dto.OAuth2UserInfo;
import kt.aivle.auth.application.port.out.OAuth2Port;
import kt.aivle.auth.properties.OAuth2Properties;
import kt.aivle.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2Adapter implements OAuth2Port<OAuth2UserInfo> {

    private final OAuth2Properties oauth2Properties;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String exchangeCodeForToken(String provider, String code) {
        try {
            OAuth2Properties.Registration registration = getRegistration(provider);
            OAuth2Properties.Provider providerConfig = getProvider(provider);
            
            // 토큰 요청 파라미터 구성
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", registration.getClientId());
            params.add("client_secret", registration.getClientSecret());
            params.add("code", code);
            params.add("redirect_uri", registration.getRedirectUri());

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            log.info("{} 토큰 교환 요청 시작 - Token URI: {}", provider, providerConfig.getTokenUri());
            
            // OAuth2 Token API 호출
            ResponseEntity<String> response = restTemplate.exchange(
                providerConfig.getTokenUri(),
                HttpMethod.POST,
                request,
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                String accessToken = switch (provider.toLowerCase()) {
                    case "google" -> objectMapper.readValue(response.getBody(), GoogleTokenResponse.class).getAccessToken();
                    case "kakao" -> objectMapper.readValue(response.getBody(), KakaoTokenResponse.class).getAccessToken();
                    default -> throw new BusinessException(BAD_REQUEST, "지원하지 않는 OAuth2 제공자: " + provider);
                };
                log.info("{} 토큰 교환 성공", provider);
                return accessToken;
            } else {
                log.error("{} 토큰 교환 실패 - Status: {}, Body: {}", 
                    provider, response.getStatusCode(), response.getBody());
                throw new RuntimeException(provider + " 토큰 교환 실패");
            }
        } catch (Exception e) {
            log.error("{} 토큰 교환 중 오류 발생", provider, e);
            throw new RuntimeException(provider + " 토큰 교환 실패", e);
        }
    }

    @Override
    public OAuth2UserInfo getUserInfo(String provider, String accessToken) {
        try {
            OAuth2Properties.Provider providerConfig = getProvider(provider);
            
            // HTTP 헤더 설정 (Bearer Token)
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(headers);

            log.info("{} 사용자 정보 조회 시작 - UserInfo URI: {}", provider, providerConfig.getUserInfoUri());
            
            // OAuth2 UserInfo API 호출
            ResponseEntity<String> response = restTemplate.exchange(
                providerConfig.getUserInfoUri(),
                HttpMethod.GET,
                request,
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                try {
                    OAuth2UserInfo userInfo = switch (provider.toLowerCase()) {
                        case "kakao" -> objectMapper.readValue(response.getBody(), KakaoUserInfo.class);
                        case "google" -> objectMapper.readValue(response.getBody(), GoogleUserInfo.class);
                        default -> throw new BusinessException(BAD_REQUEST, "지원하지 않는 OAuth2 제공자: " + provider);
                    };
                    log.info("{} 사용자 정보 조회 성공 - ProviderId: {}, Email: {}, Name: {}",
                            provider, userInfo.getProviderId(), userInfo.getEmail(), userInfo.getName());
                    return userInfo;
                } catch (Exception e) {
                    log.error("{} 사용자 정보 파싱 실패", provider, e);
                    throw new RuntimeException(provider + " 사용자 정보 파싱 실패", e);
                }
            } else {
                log.error("{} 사용자 정보 조회 실패 - Status: {}, Body: {}", 
                    provider, response.getStatusCode(), response.getBody());
                throw new RuntimeException(provider + " 사용자 정보 조회 실패");
            }
        } catch (Exception e) {
            log.error("{} 사용자 정보 조회 중 오류 발생", provider, e);
            throw new RuntimeException(provider + " 사용자 정보 조회 실패", e);
        }
    }

    @Override
    public String generateAuthUrl(String provider) {
        OAuth2Properties.Registration registration = getRegistration(provider);
        OAuth2Properties.Provider providerConfig = getProvider(provider);
        
        String scopes = String.join("%20", registration.getScope());
        
        return String.format(
            "%s?client_id=%s&redirect_uri=%s&response_type=code&scope=%s",
            providerConfig.getAuthorizationUri(),
            registration.getClientId(),
            registration.getRedirectUri(),
            scopes
        );
    }

    private OAuth2Properties.Registration getRegistration(String provider) {
        OAuth2Properties.Registration registration = oauth2Properties.getRegistration(provider);
        if (registration == null) {
            throw new BusinessException(BAD_REQUEST, "지원하지 않는 OAuth2 제공자: " + provider);
        }
        return registration;
    }

    private OAuth2Properties.Provider getProvider(String provider) {
        OAuth2Properties.Provider providerConfig = oauth2Properties.getProvider(provider);
        if (providerConfig == null) {
            throw new BusinessException(BAD_REQUEST, "지원하지 않는 OAuth2 제공자: " + provider);
        }
        return providerConfig;
    }



    private OAuth2UserInfo createUserInfo(String provider, Map<String, Object> attributes) {
        return switch (provider.toLowerCase()) {
            case "kakao" -> new KakaoUserInfo(attributes);
            case "google" -> new GoogleUserInfo(attributes);
            default -> throw new BusinessException(BAD_REQUEST, "지원하지 않는 OAuth2 제공자: " + provider);
        };
    }
    
    /**
     * Map 형태의 attributes를 OAuth2UserInfo로 변환
     * Spring Security OAuth2에서 받은 attributes를 처리할 때 사용
     */
    @Override
    public OAuth2UserInfo createUserInfoFromAttributes(String provider, Map<String, Object> attributes) {
        return createUserInfo(provider, attributes);
    }
} 