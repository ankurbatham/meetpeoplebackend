package com.meetThePeople.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class FileUtil {
    
    @Value("${app.file.upload.profile-pictures}")
    private String profilePicturesPath;
    
    @Value("${app.file.upload.voice-messages}")
    private String voiceMessagesPath;
    
    @Value("${app.file.upload.images}")
    private String imagesPath;
    
    // Default constructor for manual instantiation
    public FileUtil() {
        this.profilePicturesPath = "./uploads/profile-pictures/";
        this.voiceMessagesPath = "./uploads/voice-messages/";
        this.imagesPath = "./uploads/images/";
    }
    
    public String saveProfilePicture(MultipartFile file) throws IOException {
        return saveFile(file, profilePicturesPath, "profile");
    }
    
    public String saveVoiceMessage(MultipartFile file) throws IOException {
        return saveFile(file, voiceMessagesPath, "voice");
    }
    
    public String saveImage(MultipartFile file) throws IOException {
        return saveFile(file, imagesPath, "image");
    }
    
    private String saveFile(MultipartFile file, String directoryPath, String prefix) throws IOException {
        // Create directory if it doesn't exist
        Path directory = Paths.get(directoryPath);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String filename = prefix + "_" + UUID.randomUUID().toString() + extension;
        Path filePath = directory.resolve(filename);
        
        // Save file
        Files.copy(file.getInputStream(), filePath);
        
        return filePath.toString();
    }
    
    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            return false;
        }
    }
    
    public boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }
} 