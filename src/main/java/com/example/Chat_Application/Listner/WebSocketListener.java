package com.example.Chat_Application.Listner;

import com.example.Chat_Application.Entity.ChatMessage;
import com.example.Chat_Application.Service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;

@Component
public class WebSocketListener {

    @Autowired
    private UserService userService;

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    private static final Logger logger = LoggerFactory.getLogger(WebSocketListener.class);

    @EventListener
    public void handleWebsocketConnectListener(SessionConnectedEvent event){
        logger.info("Connected to websocket");
    }

    @EventListener
    public void handleWebsocketDisconnectListener(SessionDisconnectEvent event){
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // ✅ IMPROVED: Better null checking
        if(headerAccessor.getSessionAttributes() == null) {
            logger.warn("Session attributes are null during disconnect");
            return;
        }

        Object usernameObj = headerAccessor.getSessionAttributes().get("username");

        if(usernameObj == null) {
            logger.debug("Username not found in session attributes - likely a failed connection attempt");
            return;
        }

        String username = usernameObj.toString();

        try {
            // ✅ ADDED: Check if user exists before updating status
            if(!userService.userExists(username)) {
                logger.warn("User {} does not exist in database, skipping disconnect handling", username);
                return;
            }

            userService.setUserOnlineStatus(username, false);
            logger.info("User disconnected from websocket: {}", username);

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setType(ChatMessage.MessageType.LEAVE);
            chatMessage.setSender(username);
            chatMessage.setTimeStamp(LocalDateTime.now());
            chatMessage.setContent("");

            messagingTemplate.convertAndSend("/topic/public", chatMessage);

        } catch (Exception e) {
            logger.error("Error handling disconnect for user: {}", username, e);
        }
    }
}