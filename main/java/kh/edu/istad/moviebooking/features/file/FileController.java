package kh.edu.istad.moviebooking.features.file;

import kh.edu.istad.moviebooking.features.file.dto.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FileUploadResponse uploadImage(@RequestPart("file") MultipartFile file) {

        String url = fileStorageService.uploadImage(file);

        return new FileUploadResponse(url);
    }
}