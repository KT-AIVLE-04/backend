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
public class GoogleUserInfo implements OAuth2UserInfo {
    
    public GoogleUserInfo(Map<String, Object> attributes) {
        this.sub = (String) attributes.get("sub");
        this.email = (String) attributes.get("email");
        this.name = (String) attributes.get("name");
        this.givenName = (String) attributes.get("given_name");
        this.familyName = (String) attributes.get("family_name");
        this.picture = (String) attributes.get("picture");
        this.emailVerified = (Boolean) attributes.get("email_verified");
    }
    @JsonProperty("sub")
    private String sub;          // Google 고유 ID
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("given_name")
    private String givenName;
    
    @JsonProperty("family_name")
    private String familyName;
    
    @JsonProperty("picture")
    private String picture;
    
    @JsonProperty("email_verified")
    private Boolean emailVerified;
    
    @Override
    public String getProviderId() {
        return sub;
    }
    
    @Override
    public String getProvider() {
        return "google";
    }
} 