package kt.aivle.analytics.adapter.in.web.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;

public record PostMetricsRequest(
    @NotNull(message = "게시글 ID를 입력해주세요.")
    Long socialPostId,
    
    @NotNull(message = "시작 날짜를 입력해주세요.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate startDate,
    
    @NotNull(message = "종료 날짜를 입력해주세요.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate endDate
) {}
