package com.example.Chat_Application.Controller;

import com.example.Chat_Application.Entity.ChatMessage;
import com.example.Chat_Application.Repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @GetMapping("/private")  // ✅ FIXED: Added missing annotation
    public ResponseEntity<List<ChatMessage>> getPrivateMessages(
            @RequestParam String user1,
            @RequestParam String user2) {

        List<ChatMessage> messages = chatMessageRepository
                .findPrivateMessagesBetweenTwoUsers(user1, user2);
        return ResponseEntity.ok(messages);
    }
}
