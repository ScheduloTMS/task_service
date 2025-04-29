package com.example.task_service.DTO;

public class StudentDTO {

    private String userId;  // It's better to use `userId` instead of `id` to align with other parts of your system
    private String name;
    private String photoUrl;  // Storing photo as URL (if it's being stored remotely) or byte[] for image data

    // Constructor
    public StudentDTO(String userId, String name, String photoUrl) {
        this.userId = userId;
        this.name = name;
        this.photoUrl = photoUrl;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    // If you prefer to keep byte[] for photo (less common), you can keep `photo` as byte[]
    public byte[] getPhoto() {
        return photoUrl != null ? photoUrl.getBytes() : null;  // Convert URL to byte[] if needed
    }

    public void setPhoto(byte[] photo) {
        this.photoUrl = new String(photo);
    }
}
