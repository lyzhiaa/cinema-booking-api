package kh.edu.istad.moviebooking.features.file;

import kh.edu.istad.moviebooking.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageServiceImpl
        implements FileStorageService {

    private static final String UPLOAD_DIR = "uploads/images";

    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    @Override
    public String uploadImage(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required");
        }

        String contentType = file.getContentType();

        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {

            throw new BadRequestException("Only JPEG, PNG and WEBP images are allowed");
        }

        try {

            Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();

            Files.createDirectories(uploadPath);

            String extension = getExtension(file.getOriginalFilename());

            String fileName = UUID.randomUUID() + extension;

            Path targetPath = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(),
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return "/uploads/images/" + fileName;

        } catch (IOException e) {

            throw new BadRequestException("Failed to upload image");
        }
    }

    private String getExtension(String originalFilename) {

        if (originalFilename == null) {
            return "";
        }

        int index = originalFilename.lastIndexOf('.');

        if (index < 0) {
            return "";
        }

        return originalFilename.substring(index).toLowerCase();
    }
}