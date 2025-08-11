package kt.aivle.content.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Optional;

@Configuration
@EnableJpaRepositories(basePackages = "kt.aivle.content.repository")
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaConfig {

    /**
     * JPA Auditing을 위한 현재 사용자 제공
     * 실제 프로젝트에서는 SecurityContext에서 사용자 정보를 가져옴
     */
    @org.springframework.context.annotation.Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAware<String>() {
            @Override
            public Optional<String> getCurrentAuditor() {
                // 실제로는 Spring Security의 SecurityContextHolder에서 사용자 정보 추출
                // return Optional.of(SecurityContextHolder.getContext().getAuthentication().getName());

                // 임시로 기본값 반환 (실제 구현 시 수정 필요)
                return Optional.of("system");
            }
        };
    }
}