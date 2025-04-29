package com.example.task_service.DTO;

public class ApiResponse {

    private String status;
    private int statusCode;
    private String message;
    private Object response;

    public ApiResponse(String status, int statusCode, String message, Object response) {
        this.status = status;
        this.statusCode = statusCode;
        this.message = message;
        this.response = response;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }

    // Added getData method
    public Object getData() {
        return this.response;
    }
}
