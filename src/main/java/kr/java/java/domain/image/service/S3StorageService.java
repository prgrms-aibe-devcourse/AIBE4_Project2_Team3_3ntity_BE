package kr.java.java.domain.image.service;

import kr.java.java.domain.image.exception.ImageErrorCode;
import kr.java.java.domain.image.exception.ImageException;
import kr.java.java.global.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService {
    private final S3Client s3Client;

    @Value("${supabase.storage.bucket}")
    private String bucket;

    @Value("${supabase.storage.url}")
    private String supabaseUrl;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

    public String uploadFile(MultipartFile file, String domain){
        if (file == null || file.isEmpty()) return null;

        validateImageFile(file);

        String fileName = FileUtil.createFileName(file.getOriginalFilename());
        String key = domain + "/" + fileName;

        try {
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build(), RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (Exception e) {
            throw new ImageException(ImageErrorCode.UPLOAD_FAILED);
        }

        String fileUrl = String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, bucket, key);

        return fileUrl;
    }

    public void deleteFile(String fileUrl) {
        String searchString = bucket + "/";
        int bucketIndex = fileUrl.lastIndexOf(searchString);

        if(bucketIndex != -1){
            String key = fileUrl.substring(bucketIndex + searchString.length());
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
        }
    }

    public String uploadProfileImage(MultipartFile file){
        return uploadFile(file, "profiles");
    }

    private void validateImageFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new ImageException(ImageErrorCode.INVALID_FILE_EXTENSION);
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ImageException(ImageErrorCode.INVALID_FILE_EXTENSION);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ImageException(ImageErrorCode.INVALID_FILE_EXTENSION);
        }
    }
}
