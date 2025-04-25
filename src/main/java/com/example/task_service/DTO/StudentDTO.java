package com.example.task_service.DTO;

public class StudentDTO {
    private String id;
    private String name;
    private byte[] photo;

    public StudentDTO(String id, String name, byte[] photo) {
        this.id = id;
        this.name = name;
        this.photo = photo;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }
}