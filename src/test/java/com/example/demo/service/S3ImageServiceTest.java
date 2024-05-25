package com.example.demo.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;

import com.example.demo.service.community.post.S3ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class S3ImageServiceTest {

    @Mock
    private AmazonS3 amazonS3;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private S3ImageService s3Service; // 이 클래스는 uploadImageToS3 메서드를 포함하는 클래스입니다.

    @BeforeEach
    public void setup() {
        s3Service = new S3ImageService(amazonS3);
    }

    @DisplayName("이미지 업로드 성공 테스트")
    @Test
    public void testUploadImageToS3_Success() throws IOException {
        // Given
        String originalFilename = "testImage.jpg";
        String extention = ".jpg";
        String s3FileName = UUID.randomUUID().toString().substring(0, 10) + originalFilename;
        byte[] bytes = "test image content".getBytes();

        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(bytes));
        when(amazonS3.getUrl(any(),any())).thenReturn(new URL("https://"+s3Service.bucketName+".s3.amazonaws.com/" + s3FileName));

        // When
        String resultUrl = s3Service.uploadImageToS3(multipartFile);

        // Then
        verify(amazonS3, times(1)).putObject(any(PutObjectRequest.class));
        assertEquals("https://"+s3Service.bucketName+".s3.amazonaws.com/" + s3FileName, resultUrl);
    }

    @Test
    public void testUploadImageToS3_Exception() throws IOException {
        // Given
        String originalFilename = "testImage.jpg";
        byte[] bytes = "test image content".getBytes();

        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(bytes));
        doThrow(new RuntimeException("S3 error")).when(amazonS3).putObject(any(PutObjectRequest.class));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            s3Service.uploadImageToS3(multipartFile);
        });

        verify(amazonS3, times(1)).putObject(any(PutObjectRequest.class));
    }
}
