package kt.aivle.sns.application.service.oauth;

import kt.aivle.sns.domain.model.SnsType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class OAuthTemplateService {

    private static final String SUCCESS_TEMPLATE_PATH = "templates/oauth-success.html";
    private static final String ERROR_TEMPLATE_PATH = "templates/oauth-error.html";

    public String createSuccessHtml(SnsType snsType) {
        try {
            String template = loadTemplate(SUCCESS_TEMPLATE_PATH);
            String snsName = getSnsDisplayName(snsType);
            
            return template
                    .replace("{snsName}", snsName);
                    
        } catch (Exception e) {
            log.error("Failed to create success HTML template", e);
            return createFallbackSuccessHtml(snsType);
        }
    }

    public String createErrorHtml(SnsType snsType, String errorMessage) {
        try {
            String template = loadTemplate(ERROR_TEMPLATE_PATH);
            String snsName = getSnsDisplayName(snsType);
            
            return template
                    .replace("{snsName}", snsName)
                    .replace("{errorMessage}", errorMessage != null ? errorMessage : "알 수 없는 오류");
                    
        } catch (Exception e) {
            log.error("Failed to create error HTML template", e);
            return createFallbackErrorHtml(snsType, errorMessage);
        }
    }

    private String loadTemplate(String templatePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(templatePath);
        return resource.getContentAsString(StandardCharsets.UTF_8);
    }

    private String getSnsDisplayName(SnsType snsType) {
        return switch (snsType) {
            case youtube -> "YouTube";
            // case instagram -> "Instagram";
            // case facebook -> "Facebook";
            default -> snsType.name().toUpperCase();
        };
    }

    private String createFallbackSuccessHtml(SnsType snsType) {
        String snsName = getSnsDisplayName(snsType);
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>계정 연동 완료</title>
            </head>
            <body>
                <h2>✅ %s 계정 연동이 완료되었습니다!</h2>
                <p>이 창을 닫고 원래 페이지로 돌아가세요.</p>
                <script>
                    setTimeout(() => window.close(), 3000);
                </script>
            </body>
            </html>
            """.formatted(snsName);
    }

    private String createFallbackErrorHtml(SnsType snsType, String errorMessage) {
        String snsName = getSnsDisplayName(snsType);
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>계정 연동 실패</title>
            </head>
            <body>
                <h2>❌ %s 계정 연동에 실패했습니다</h2>
                <p>오류: %s</p>
                <button onclick="window.close()">창 닫기</button>
            </body>
            </html>
            """.formatted(snsName, errorMessage != null ? errorMessage : "알 수 없는 오류");
    }
}
