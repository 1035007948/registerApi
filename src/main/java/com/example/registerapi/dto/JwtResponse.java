package com.example.registerapi.dto;

public class JwtResponse {

    private String token;
    private String type = "Bearer";
    private String username;
    private Long id;

    public JwtResponse(String token, String username, Long id) {
        this.token = token;
        this.username = username;
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
