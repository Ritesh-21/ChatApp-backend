package com.example.Chat_Application.JWT;

import com.example.Chat_Application.Repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // ✅ FIXED: Skip JWT filter for public endpoints
        String path = request.getRequestURI();

        // Skip ALL /auth endpoints (signup, login, verify, etc.)
        if (path.contains("/auth/")) {  // ✅ Changed to contains
            filterChain.doFilter(request, response);
            return;
        }

        // Skip WebSocket and STOMP endpoints
        if (path.startsWith("/ws") ||
                path.startsWith("/app") ||
                path.startsWith("/topic") ||
                path.startsWith("/queue") ||
                path.startsWith("/user")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip H2 console
        if (path.startsWith("/h2-console")) {
            filterChain.doFilter(request, response);
            return;
        }

        Long userId = null;
        String jwtToken = null;

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7);
        }

        //If JWT token is null, need to check cookie
        if(jwtToken == null){
            Cookie[] cookies = request.getCookies();
            if(cookies != null){
                for (Cookie cookie : cookies) {
                    if("JWT".equals(cookie.getName())){
                        jwtToken = cookie.getValue();
                        break;
                    }
                }
            }
        }

        if(jwtToken == null){
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ Try-catch for JWT extraction
        try {
            userId = jwtService.extractUserId(jwtToken);
        } catch (Exception e) {
            // Invalid JWT token, continue without authentication
            System.out.println("Invalid JWT token: " + e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if(userId != null && SecurityContextHolder.getContext().getAuthentication() == null){

            // ✅ Better error handling for missing user
            var userOptional = userRepository.findById(userId);

            if(userOptional.isEmpty()) {
                // User not found, continue without authentication
                System.out.println("User not found for ID: " + userId);
                filterChain.doFilter(request, response);
                return;
            }

            var userDetails = userOptional.get();

            if(jwtService.isTokenValid(jwtToken, userDetails)){

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null,
                                Collections.emptyList());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        }

        filterChain.doFilter(request, response);

    }
}