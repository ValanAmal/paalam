package com.example.filesharing.controller;

import com.example.filesharing.model.FileEntity;
import com.example.filesharing.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/")
    public String listFiles(Model model) {
        model.addAttribute("files", fileService.getAllFiles());
        return "index";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                           RedirectAttributes redirectAttributes) {
        try {
            fileService.storeFile(file, "anonymous");
            redirectAttributes.addFlashAttribute("message", 
                "File uploaded successfully: " + file.getOriginalFilename());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Failed to upload file: " + e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        try {
            FileEntity fileEntity = fileService.getFile(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

            Path path = Paths.get(fileEntity.getFilePath());
            Resource resource = new UrlResource(path.toUri());

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileEntity.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + fileEntity.getFileName() + "\"")
                .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/delete/{fileId}")
    public String deleteFile(@PathVariable Long fileId, 
                           RedirectAttributes redirectAttributes) {
        try {
            fileService.deleteFile(fileId);
            redirectAttributes.addFlashAttribute("message", "File deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Failed to delete file: " + e.getMessage());
        }
        return "redirect:/";
    }
} 