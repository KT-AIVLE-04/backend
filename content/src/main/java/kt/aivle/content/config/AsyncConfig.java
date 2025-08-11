package kt.aivle.content.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 비동기 처리 설정
 *
 * 썸네일 생성, 영상 메타데이터 추출 등
 * 시간이 오래 걸리는 작업을 비동기로 처리하기 위한 설정
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 파일 처리용 스레드 풀
     * 썸네일 생성, 영상 압축 등에 사용
     */
    @Bean(name = "fileProcessingExecutor")
    public Executor fileProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);        // 기본 스레드 수
        executor.setMaxPoolSize(5);         // 최대 스레드 수
        executor.setQueueCapacity(100);     // 대기 큐 크기
        executor.setThreadNamePrefix("FileProcessing-");
        executor.setKeepAliveSeconds(60);   // 유휴 스레드 유지 시간
        executor.initialize();
        return executor;
    }

    /**
     * 썸네일 생성용 스레드 풀
     * 이미지/영상 썸네일 생성 전용
     */
    @Bean(name = "thumbnailExecutor")
    public Executor thumbnailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("Thumbnail-");
        executor.setKeepAliveSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * 일반 비동기 작업용 스레드 풀
     * 알림, 로그 처리 등에 사용
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("Async-");
        executor.setKeepAliveSeconds(60);
        executor.initialize();
        return executor;
    }
}