package kt.aivle.auth.adapter.out.oauth2;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import kt.aivle.auth.adapter.out.oauth2.dto.GoogleTokenResponse;
import kt.aivle.auth.adapter.out.oauth2.dto.GoogleUserInfo;
import kt.aivle.auth.application.port.out.OAuth2Port;
import kt.aivle.auth.properties.OAuth2Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleOAuth2Adapter implements OAuth2Port {

    private final OAuth2Properties oauth2Properties;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String exchangeCodeForToken(String code) {
        try {
            OAuth2Properties.Registration google = oauth2Properties.getGoogle();
            OAuth2Properties.Provider googleProvider = oauth2Properties.getGoogleProvider();
            
            // 토큰 요청 파라미터 구성
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", google.getClientId());
            params.add("client_secret", google.getClientSecret());
            params.add("code", code);
            params.add("grant_type", "authorization_code");
            params.add("redirect_uri", google.getRedirectUri());

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            log.info("Google 토큰 교환 요청 시작 - Token URI: {}", googleProvider.getTokenUri());
            
            // Google Token API 호출
            ResponseEntity<String> response = restTemplate.exchange(
                googleProvider.getTokenUri(),
                HttpMethod.POST,
                request,
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                GoogleTokenResponse tokenResponse = objectMapper.readValue(response.getBody(), GoogleTokenResponse.class);
                log.info("Google 토큰 교환 성공");
                return tokenResponse.getAccessToken();
            } else {
                log.error("Google 토큰 교환 실패 - Status: {}, Body: {}", 
                    response.getStatusCode(), response.getBody());
                throw new RuntimeException("Google 토큰 교환 실패");
            }
        } catch (Exception e) {
            log.error("Google 토큰 교환 중 오류 발생", e);
            throw new RuntimeException("Google 토큰 교환 실패", e);
        }
    }

    @Override
    public GoogleUserInfo getUserInfo(String accessToken) {
        try {
            OAuth2Properties.Provider googleProvider = oauth2Properties.getGoogleProvider();
            
            // HTTP 헤더 설정 (Bearer Token)
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(headers);

            log.info("Google 사용자 정보 조회 시작 - UserInfo URI: {}", googleProvider.getUserInfoUri());
            
            // Google UserInfo API 호출
            ResponseEntity<String> response = restTemplate.exchange(
                googleProvider.getUserInfoUri(),
                HttpMethod.GET,
                request,
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                GoogleUserInfo userInfo = objectMapper.readValue(response.getBody(), GoogleUserInfo.class);
                log.info("Google 사용자 정보 조회 성공 - Email: {}, Name: {}", 
                    userInfo.getEmail(), userInfo.getName());
                return userInfo;
            } else {
                log.error("Google 사용자 정보 조회 실패 - Status: {}, Body: {}", 
                    response.getStatusCode(), response.getBody());
                throw new RuntimeException("Google 사용자 정보 조회 실패");
            }
        } catch (Exception e) {
            log.error("Google 사용자 정보 조회 중 오류 발생", e);
            throw new RuntimeException("Google 사용자 정보 조회 실패", e);
        }
    }

    @Override
    public String generateAuthUrl() {
        OAuth2Properties.Registration google = oauth2Properties.getGoogle();
        OAuth2Properties.Provider googleProvider = oauth2Properties.getGoogleProvider();
        
        String scopes = String.join("%20", google.getScope());
        
        return String.format(
            "%s?client_id=%s&redirect_uri=%s&response_type=code&scope=%s",
            googleProvider.getAuthorizationUri(),
            google.getClientId(),
            google.getRedirectUri(),
            scopes
        );
    }
} 