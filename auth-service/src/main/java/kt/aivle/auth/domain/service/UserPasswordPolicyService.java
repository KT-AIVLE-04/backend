package kt.aivle.auth.domain.service;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class UserPasswordPolicyService {

    // 허용 특수문자 집합 ( () < > “ ‘ ; 는 제외 )
    private static final String SPECIAL_CHARS = "!@#$%^&*_-+=|\\/?.,:[]{}";
    private static final Pattern SPECIAL_PATTERN = Pattern.compile("[" + Pattern.quote(SPECIAL_CHARS) + "]");
    private static final Pattern UPPER_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWER_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");

    public boolean isValid(String email, String password) {
        // 1. 길이 및 조합 체크
        if (!isValidLengthAndCombination(password)) return false;

        // 2. 연속문자(3자리 이상) 체크
        if (hasSequentialChars(password, 3)) return false;

        // 3. 아이디와 동일(완전일치) 불가
        if (email != null && email.equals(password)) return false;
        return true;
    }

    private boolean isValidLengthAndCombination(String pw) {
        int length = pw.length();
        boolean hasUpper = UPPER_PATTERN.matcher(pw).find();
        boolean hasLower = LOWER_PATTERN.matcher(pw).find();
        boolean hasDigit = DIGIT_PATTERN.matcher(pw).find();
        boolean hasSpecial = SPECIAL_PATTERN.matcher(pw).find();

        int kinds = 0;
        if (hasUpper || hasLower) kinds++; // 영문(대/소문자 중 하나만 있으면 1종)
        if (hasDigit) kinds++;
        if (hasSpecial) kinds++;

        // 2종(영문+숫자, 영문+특수, 숫자+특수) 10자리 이상
        if (kinds == 2 && length >= 10) return true;
        // 3종(영문+숫자+특수) 8자리 이상
        if (kinds == 3 && length >= 8) return true;

        return false;
    }

    private boolean hasSequentialChars(String pw, int limit) {
        for (int i = 0; i <= pw.length() - limit; i++) {
            boolean ascending = true, descending = true;
            for (int j = 0; j < limit - 1; j++) {
                char c1 = pw.charAt(i + j), c2 = pw.charAt(i + j + 1);
                if (c2 != c1 + 1) ascending = false;
                if (c2 != c1 - 1) descending = false;
            }
            if (ascending || descending) return true;
        }
        return false;
    }
}
