package common.dto;

public class ApiResponse<T> {
    private boolean success;
    private int statusCode;
    private String message;
    private T data;
}
