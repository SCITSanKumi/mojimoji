package com.sangkeumi.mojimoji.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;

@Service
public class LocalImageStorageService {
    @Value("${upload.path.image}")
    private String imagePath;

    /**
     * base64 인코딩된 이미지 데이터를 받아서 로컬 서버에 저장하고, 저장된 파일의 경로를 반환합니다.
     *
     * @param base64Image base64 인코딩된 이미지 데이터
     * @param fileName 저장할 파일 이름 (확장자는 자동으로 .png로 지정)
     * @return 저장된 파일의 전체 경로
     * @throws IOException 이미지 저장 중 예외 발생 시
     */
    public String saveImageLocallyFromBase64(String base64Image, String fileName) throws IOException {
        // 저장 디렉토리가 존재하지 않으면 생성
        Path storagePath = Paths.get(imagePath, "thumbnail_images");
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
        }

        // 파일 확장자는 기본적으로 .png로 지정
        String extension = ".png";
        Path filePath = storagePath.resolve(fileName + extension);

        // base64 문자열을 디코드하여 파일로 저장
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        Files.write(filePath, imageBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        return filePath.toString();
    }
}
