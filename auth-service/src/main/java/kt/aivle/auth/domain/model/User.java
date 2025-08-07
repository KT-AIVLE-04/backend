package kt.aivle.auth.domain.model;

import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class User extends BaseEntity {

    private String provider;
    private String providerId;

    @Column(unique = true)
    private String email;
    private String name;
    private String password;
    private String phoneNumber;

    private int loginFailCount = 0;
    private boolean locked = false;

    @Builder
    public User(String provider, String providerId, String email, String name,
                String password, String phoneNumber, int loginFailCount, boolean locked) {
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
        this.name = name;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.loginFailCount = loginFailCount;
        this.locked = locked;
    }

    public void increaseLoginFailCount() {
        this.loginFailCount++;
        if (this.loginFailCount >= 5) {
            this.locked = true;
        }
    }

    public void resetLoginFailCount() {
        this.loginFailCount = 0;
    }

    public void unlock() {
        this.locked = false;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
}
