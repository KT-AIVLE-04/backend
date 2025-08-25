package kt.aivle.analytics.application.port.in.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder
public abstract class BaseQueryRequest {
    private LocalDate date; // 특정 날짜, null이면 현재 날짜
    
    // 생성자 추가
    protected BaseQueryRequest(LocalDate date) {
        this.date = date;
    }

    
    public LocalDate getEffectiveDate() {
        return date != null ? date : LocalDate.now();
    }

}
