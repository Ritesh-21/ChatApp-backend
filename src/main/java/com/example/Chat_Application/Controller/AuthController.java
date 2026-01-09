package com.example.Chat_Application.Controller;


import com.example.Chat_Application.DTO.LoginRequestDTO;
import com.example.Chat_Application.DTO.LoginResponseDTO;
import com.example.Chat_Application.DTO.RegisterRequestDTO;
import com.example.Chat_Application.DTO.UserDTO;
import com.example.Chat_Application.Entity.User;
import com.example.Chat_Application.Repository.UserRepository;
import com.example.Chat_Application.Service.AuthenticationService;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(
        origins = {
                "http://localhost:5173",
                "https://chat-app-frontend-blue-omega.vercel.app"
        },
        allowCredentials = "true",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
public class AuthController {


    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<UserDTO> signup(@RequestBody RegisterRequestDTO registerRequestDTO){
        return ResponseEntity.ok(authenticationService.signup(registerRequestDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody LoginRequestDTO loginRequestDTO){

        LoginResponseDTO loginResponseDTO = authenticationService.login(loginRequestDTO);
        ResponseCookie responseCookie = ResponseCookie.from("JWT", loginResponseDTO.getToken())
                .httpOnly(true)
                .secure(true)  // ✅ Changed to true for Railway (HTTPS)
                .path("/")
                .maxAge(1*60*60) //1 Hour
                .sameSite("None")  // ✅ Changed to None for cross-origin (Vercel to Railway)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(loginResponseDTO.getUserDTO());
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(){
        return authenticationService.logout();
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        Optional<User> userOpt = userRepository.findByVerificationToken(token);

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid verification token");
        }

        User user = userOpt.get();
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        return ResponseEntity.ok("Email verified successfully! You can now login.");
    }

    @GetMapping("/getonlineusers")
    public ResponseEntity<Map<String, Object>> getOnlineUsers() {
        return ResponseEntity.ok(authenticationService.getOnlineUsers());
    }

    @GetMapping("/getcurrentuser")
    public ResponseEntity<?> getCurrentUser(Authentication authentication){

        if(authentication == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("USER NOT AUTHORIZED");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow(()
                -> new RuntimeException("User not found"));

        return ResponseEntity.ok(convertToUserDTO(user));
    }

    public UserDTO convertToUserDTO(User user){
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(user.getEmail());
        userDTO.setUsername(user.getUsername());
        userDTO.setId(user.getId());

        return userDTO;
    }

    // 🔧 TEMPORARY DEBUG ENDPOINTS - Kaam ho jaye to delete kar dena

    @GetMapping("/debug/users")
    public ResponseEntity<?> debugGetAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/debug/verify-user")
    public ResponseEntity<?> debugVerifyUser(@RequestParam String email) {
        return userRepository.findByEmail(email).map(user -> {
            user.setEmailVerified(true);
            user.setVerificationToken(null);
            userRepository.save(user);
            return ResponseEntity.ok("✅ User verified: " + email);
        }).orElse(ResponseEntity.badRequest().body("❌ User not found: " + email));
    }

    @DeleteMapping("/debug/delete-user")
    public ResponseEntity<?> debugDeleteUser(@RequestParam String email) {
        return userRepository.findByEmail(email).map(user -> {
            userRepository.delete(user);
            return ResponseEntity.ok("✅ User deleted: " + email);
        }).orElse(ResponseEntity.badRequest().body("❌ User not found: " + email));
    }

}
