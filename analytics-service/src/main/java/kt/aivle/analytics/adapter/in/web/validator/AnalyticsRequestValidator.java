package kt.aivle.analytics.adapter.in.web.validator;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import kt.aivle.analytics.domain.model.SnsType;

@Component
public class AnalyticsRequestValidator {
    
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("\\d+");
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MIN_PAGE_SIZE = 1;
    
    /**
     * Post ID 검증
     */
    public boolean isValidPostId(String postId) {
        return StringUtils.hasText(postId) && NUMERIC_PATTERN.matcher(postId).matches();
    }
    
    /**
     * Account ID 검증
     */
    public boolean isValidAccountId(String accountId) {
        return StringUtils.hasText(accountId) && NUMERIC_PATTERN.matcher(accountId).matches();
    }
    
    /**
     * User ID 검증
     */
    public boolean isValidUserId(String userId) {
        return StringUtils.hasText(userId) && NUMERIC_PATTERN.matcher(userId).matches();
    }
    
    /**
     * 페이지네이션 파라미터 검증
     */
    public boolean isValidPagination(Integer page, Integer size) {
        return page != null && page >= 0 && 
               size != null && size >= MIN_PAGE_SIZE && size <= MAX_PAGE_SIZE;
    }
    
    /**
     * 날짜 범위 검증
     */
    public boolean isValidDateRange(String startDate, String endDate) {
        return StringUtils.hasText(startDate) && StringUtils.hasText(endDate);
    }

    /**
     * SNS 타입 검증
     */
    public boolean isValidSnsType(String snsType) {
        if (!StringUtils.hasText(snsType)) {
            return false;
        }
        
        try {
            SnsType.valueOf(snsType.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
