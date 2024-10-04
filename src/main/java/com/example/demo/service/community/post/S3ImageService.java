package com.example.demo.service.community.post;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3ImageService {

    private final AmazonS3 amazonS3;
    private final List<String> allowedExtentionList = Arrays.asList("jpg", "jpeg", "png",
            "gif", "pdf", "ppt", "word");
    @Value("${cloud.aws.s3.bucketName}")
    public String bucketName;

    /**
     * 주어진 이미지를 S3에 업로드합니다.
     *
     * @param image 업로드할 이미지 파일
     * @return S3에 업로드된 이미지의 URL
     * @throws IllegalArgumentException 이미지가 비어있거나 파일 이름이 null일 경우
     */
    public String upload(MultipartFile image) {
        if(image.isEmpty() || Objects.isNull(image.getOriginalFilename())){
            throw new IllegalArgumentException();
        }
        return this.uploadImage(image);
    }

    /**
     * 실제로 이미지를 S3에 업로드합니다.
     *
     * @param image 업로드할 이미지 파일
     * @return S3에 업로드된 이미지의 URL
     * @throws IllegalArgumentException 이미지 파일 확장자가 유효하지 않거나 I/O 오류가 발생한 경우
     */
    private String uploadImage(MultipartFile image) {
        this.validateImageFileExtension(image.getOriginalFilename());
        try {
            return this.uploadImageToS3(image);
        } catch (IOException e) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * 이미지 파일의 확장자를 검증합니다.
     *
     * @param filename 검증할 파일 이름
     * @throws IllegalArgumentException 파일 확장자가 유효하지 않은 경우
     */
    private void validateImageFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new IllegalArgumentException();
        }

        String extension = filename.substring(lastDotIndex + 1).toLowerCase();

        if (!allowedExtentionList.contains(extension)) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * S3에 이미지를 업로드하는 메소드입니다.
     *
     * @param image 업로드할 이미지 파일
     * @return S3에 업로드된 이미지의 URL
     * @throws IOException I/O 오류가 발생한 경우
     */
    public String uploadImageToS3(MultipartFile image) throws IOException {
        String originalFilename = image.getOriginalFilename(); // 원본 파일 명
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")); // 확장자 명

        String s3FileName = UUID.randomUUID().toString().substring(0, 10) + originalFilename; // 변경된 파일 명

        InputStream is = image.getInputStream();
        byte[] bytes = IOUtils.toByteArray(is);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/" + extension);
        metadata.setContentLength(bytes.length);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        try {
            PutObjectRequest putObjectRequest =
                    new PutObjectRequest(bucketName, s3FileName, byteArrayInputStream, metadata)
                            .withCannedAcl(CannedAccessControlList.PublicRead);
            amazonS3.putObject(putObjectRequest); // S3에 이미지 저장
        } catch (SdkClientException e) {
            throw new SdkClientException(e.getMessage(), e);
        } finally {
            byteArrayInputStream.close();
            is.close();
        }

        return amazonS3.getUrl(bucketName, s3FileName).toString();
    }

    /**
     * S3에서 이미지를 삭제합니다.
     *
     * @param imageAddress 삭제할 이미지의 S3 URL
     * @throws IllegalArgumentException 이미지 삭제 중 오류가 발생한 경우
     */
    public void deleteImageFromS3(String imageAddress) {
        String key = getKeyFromImageAddress(imageAddress);
        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key));
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * 이미지 주소에서 S3 키를 추출합니다.
     *
     * @param imageAddress 이미지의 S3 URL
     * @return S3 키
     * @throws IllegalArgumentException URL 형식이 잘못되었거나 인코딩 오류가 발생한 경우
     */
    private String getKeyFromImageAddress(String imageAddress) {
        try {
            URL url = new URL(imageAddress);
            String decodingKey = URLDecoder.decode(url.getPath(), "UTF-8");
            return decodingKey.substring(1); // 맨 앞의 '/' 제거
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            throw new IllegalArgumentException();
        }
    }
}
