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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
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

    @Value("${app.frontend-url:https://chat-app-frontend-blue-omega.vercel.app}")
    private String frontendUrl;

    @PostMapping("/signup")
    public ResponseEntity<UserDTO> signup(@RequestBody RegisterRequestDTO registerRequestDTO){
        return ResponseEntity.ok(authenticationService.signup(registerRequestDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequestDTO loginRequestDTO){

        LoginResponseDTO loginResponseDTO = authenticationService.login(loginRequestDTO);

        ResponseCookie responseCookie = ResponseCookie.from("JWT", loginResponseDTO.getToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(1*60*60)
                .sameSite("None")
                .build();

        Map<String, Object> response = new HashMap<>();
        response.put("token", loginResponseDTO.getToken());
        response.put("user", loginResponseDTO.getUserDTO());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(){
        return authenticationService.logout();
    }

    @GetMapping("/verify")
    public RedirectView verifyEmail(@RequestParam(required = false) String token) {

        System.out.println("🔍 Verify endpoint hit! Token: " + token);
        System.out.println("🌐 Frontend URL: " + frontendUrl);

        if (token == null || token.trim().isEmpty()) {
            System.out.println("❌ Token is null or empty!");
            String redirectUrl = frontendUrl + "/login?verified=false&error=missing_token";
            System.out.println("↪️ Redirecting to: " + redirectUrl);
            return new RedirectView(redirectUrl);
        }

        Optional<User> userOpt = userRepository.findByVerificationToken(token);

        if (userOpt.isEmpty()) {
            System.out.println("❌ User not found for token: " + token);
            String redirectUrl = frontendUrl + "/login?verified=false&error=invalid_token";
            System.out.println("↪️ Redirecting to: " + redirectUrl);
            return new RedirectView(redirectUrl);
        }

        User user = userOpt.get();
        System.out.println("✅ User found: " + user.getEmail());

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        System.out.println("✅ User verified successfully: " + user.getEmail());
        String redirectUrl = frontendUrl + "/login?verified=true";
        System.out.println("↪️ Redirecting to: " + redirectUrl);

        return new RedirectView(redirectUrl);
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