package kr.java.java.domain.image.service;

import kr.java.java.domain.image.exception.ImageErrorCode;
import kr.java.java.domain.image.exception.ImageException;
import kr.java.java.global.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3StorageService {
    private final S3Client s3Client;

    @Value("${supabase.storage.bucket}")
    private String bucket;

    @Value("${supabase.storage.url}")
    private String supabaseUrl;

    public String uploadFile(MultipartFile file){
        if (file == null || file.isEmpty()) return null;

        String fileName = FileUtil.createFileName(file.getOriginalFilename());
        try {
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build(), RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (Exception e) {
            throw new ImageException(ImageErrorCode.UPLOAD_FAILED);
        }

        String fileUrl = String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, bucket, fileName);

        return fileUrl;
    }

    public void deleteFile(String fileUrl) {
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(fileName).build());
    }

    public String uploadProfileImage(MultipartFile file){
        return uploadFile(file);
    }
}
