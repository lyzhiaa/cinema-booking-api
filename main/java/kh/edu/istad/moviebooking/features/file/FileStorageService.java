package kh.edu.istad.moviebooking.features.file;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String uploadImage(MultipartFile file);
}