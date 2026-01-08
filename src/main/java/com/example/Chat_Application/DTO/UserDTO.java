package com.example.Chat_Application.DTO;

import lombok.Data;

@Data
public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private boolean isOnline;
}
