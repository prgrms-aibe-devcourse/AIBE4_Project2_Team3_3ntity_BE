package kr.java.java.domain.auth.dto;

public class LoginRequest {

    private String code;
    private String role;

    public LoginRequest(String code, String role) {
        this.code = code;
        this.role = role;
    }
}
