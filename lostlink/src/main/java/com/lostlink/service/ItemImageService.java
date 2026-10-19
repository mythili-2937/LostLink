
package com.lostlink.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class ItemImageService {

    private final Path uploadDirectory;

    public ItemImageService(
            @Value("${lostlink.upload-dir}") String uploadDir)
            throws IOException {

        uploadDirectory = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();

        Files.createDirectories(uploadDirectory);
    }

    public String saveImage(MultipartFile file)
            throws IOException {

        if (file == null || file.isEmpty()) {
            return null;
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "Image must be 5 MB or smaller."
            );
        }

        String contentType = file.getContentType();
    if (contentType == null ||
        !Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "image/bmp"
        ).contains(contentType.toLowerCase())) {

            throw new IllegalArgumentException(
                    "Only JPG, PNG and WebP images are allowed."
            );
        }

        String extension;

        try (InputStream input = file.getInputStream()) {
            byte[] signature = input.readNBytes(12);

           
if (isJpeg(signature)) {
    extension = ".jpg";
} else if (isPng(signature)) {
    extension = ".png";
} else if (isWebp(signature)) {
    extension = ".webp";
} else if (isGif(signature)) {
    extension = ".gif";
} else if (isBmp(signature)) {
    extension = ".bmp";
} else {
    throw new IllegalArgumentException(
        "Unsupported image. Use JPG, PNG, WebP, GIF or BMP."
    );
}

        }

        String filename = UUID.randomUUID() + extension;

        Path destination = uploadDirectory
                .resolve(filename)
                .normalize();

        try (InputStream input = file.getInputStream()) {
            Files.copy(input, destination,
                    StandardCopyOption.REPLACE_EXISTING);
        }

        return filename;
    }

    private boolean isJpeg(byte[] bytes) {
        return bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF;
    }

    private boolean isPng(byte[] bytes) {
        return bytes.length >= 8
                && (bytes[0] & 0xFF) == 0x89
                && bytes[1] == 'P'
                && bytes[2] == 'N'
                && bytes[3] == 'G';
    }

    private boolean isWebp(byte[] bytes) {
        return bytes.length >= 12
                && new String(bytes, 0, 4,
                    java.nio.charset.StandardCharsets.US_ASCII)
                    .equals("RIFF")
                && new String(bytes, 8, 4,
                    java.nio.charset.StandardCharsets.US_ASCII)
                    .equals("WEBP");
    }

    public Path getImagePath(String filename) {

        if (filename == null
        || !filename.matches(
            "[0-9a-fA-F-]{36}\\.(jpg|png|webp|gif|bmp)")) {
    throw new IllegalArgumentException(
        "Invalid image filename."
    );

        }

        Path path = uploadDirectory.resolve(filename).normalize();

        if (!path.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException(
                    "Invalid image path."
            );
        }

        return path;
    }

    public void deleteImage(String filename) throws IOException {
        if (filename != null) {
            Files.deleteIfExists(getImagePath(filename));
        }
    }
    
private boolean isGif(byte[] bytes) {
    if (bytes.length < 6) return false;

    String header = new String(
        bytes, 0, 6,
        java.nio.charset.StandardCharsets.US_ASCII
    );

    return header.equals("GIF87a")
            || header.equals("GIF89a");
}

private boolean isBmp(byte[] bytes) {
    return bytes.length >= 2
            && bytes[0] == 'B'
            && bytes[1] == 'M';
}

}
