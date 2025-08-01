package kt.aivle.store.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Store extends BaseEntity {

    private Long userId;
    private String name;
    private String address;
    private String phoneNumber;
    private String businessNumber;
    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    private Industry industry;

    @Builder
    public Store(Long userId, String name, String address, String phoneNumber,
                 String businessNumber, Double latitude, Double longitude, Industry industry) {
        this.userId = userId;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.businessNumber = businessNumber;
        this.latitude = latitude;
        this.longitude = longitude;
        this.industry = industry;
    }

    public void update(String name, String address, String phoneNumber,
                       Double latitude, Double longitude,
                       Industry industry) {
        if (name != null) this.name = name;
        if (address != null) this.address = address;
        if (phoneNumber != null) this.phoneNumber = phoneNumber;
        if (latitude != null) this.latitude = latitude;
        if (longitude != null) this.longitude = longitude;
        if (industry != null) this.industry = industry;
    }
}