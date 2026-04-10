package com.example.registerapi.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class UserUpdateDto {

    @Email(message = "邮箱格式不正确")
    private String email;

    public UserUpdateDto() {
    }

    public UserUpdateDto(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
