package com.example.Chat_Application.Repository;

import com.example.Chat_Application.Entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long> {



    @Query("SELECT cm FROM ChatMessage cm WHERE cm.type = 'PRIVATE_MESSAGE' AND " +
            "((cm.sender = :user1 AND cm.recipient = :user2) OR (cm.sender = :user2 AND cm.recipient = :user1)) "
            +
            "ORDER BY cm.timeStamp ASC")
    List<ChatMessage> findPrivateMessagesBetweenTwoUsers(@Param("user1") String user1,
                                                         @Param("user2") String user2);

}
