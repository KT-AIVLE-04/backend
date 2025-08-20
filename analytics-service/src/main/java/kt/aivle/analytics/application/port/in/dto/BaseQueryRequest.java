package kt.aivle.analytics.application.port.in.dto;

import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder
public abstract class BaseQueryRequest {
    private Date date; // 특정 날짜, null이면 현재 날짜
    
    // 생성자 추가
    protected BaseQueryRequest(Date date) {
        this.date = date;
    }
    
    // 공통 편의 메서드
    public boolean isCurrentDate() {
        return date == null;
    }
    
    public Date getEffectiveDate() {
        return date != null ? date : new Date();
    }
    
    // 날짜 검증 메서드
    public boolean isValidDate() {
        return date == null || !date.after(new Date());
    }
}
