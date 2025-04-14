package com.example.filesharing.service;

import com.example.filesharing.model.FileEntity;
import com.example.filesharing.repository.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FileService {

    private final FileRepository fileRepository;
    private final Path fileStorageLocation;

    public FileService(FileRepository fileRepository, @Value("${file.upload-dir:uploads}") String uploadDir) {
        this.fileRepository = fileRepository;
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create upload directory!", ex);
        }
    }

    public FileEntity storeFile(MultipartFile file, String uploadedBy) {
        try {
            String fileName = file.getOriginalFilename();
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation);

            FileEntity fileEntity = new FileEntity();
            fileEntity.setFileName(fileName);
            fileEntity.setFileType(file.getContentType());
            fileEntity.setFilePath(targetLocation.toString());
            fileEntity.setFileSize(file.getSize());
            fileEntity.setUploadedBy(uploadedBy);
            fileEntity.setUploadDateTime(LocalDateTime.now());

            return fileRepository.save(fileEntity);
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + file.getOriginalFilename(), ex);
        }
    }

    public Optional<FileEntity> getFile(Long fileId) {
        return fileRepository.findById(fileId);
    }

    public List<FileEntity> getAllFiles() {
        return fileRepository.findAll();
    }

    public void deleteFile(Long fileId) {
        Optional<FileEntity> fileEntity = fileRepository.findById(fileId);
        if (fileEntity.isPresent()) {
            try {
                Files.deleteIfExists(Paths.get(fileEntity.get().getFilePath()));
                fileRepository.deleteById(fileId);
            } catch (IOException ex) {
                throw new RuntimeException("Error deleting file", ex);
            }
        }
    }
} 