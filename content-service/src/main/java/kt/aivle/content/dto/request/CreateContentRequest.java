package kt.aivle.content.dto.request;

import org.springframework.web.multipart.MultipartFile;

public record CreateContentRequest(
        Long userId,
        Long storeId,
        MultipartFile file) {
}