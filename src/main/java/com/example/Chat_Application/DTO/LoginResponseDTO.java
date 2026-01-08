package com.example.Chat_Application.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDTO {

    private String token;
    private UserDTO userDTO;
}
