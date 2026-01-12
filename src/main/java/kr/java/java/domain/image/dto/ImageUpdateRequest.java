package kr.java.java.domain.image.dto;

import org.springframework.web.multipart.MultipartFile;

public record ImageUpdateRequest(
        Long id,
        String fileUrl,
        Integer sortOrder,
        MultipartFile file
) {
}
