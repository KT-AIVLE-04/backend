package kt.aivle.auth.adapter.out.oauth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleUserInfo {
    private String id;
    private String email;
    
    @JsonProperty("verified_email")
    private Boolean verifiedEmail;
    
    private String name;
    
    @JsonProperty("given_name")
    private String givenName;
    
    @JsonProperty("family_name")
    private String familyName;
    
    private String picture;
    
    @JsonProperty("locale")
    private String locale;
} 