package com.hobbyswap.controller;

import com.hobbyswap.model.ChatMessage;
import com.hobbyswap.model.User;
import com.hobbyswap.repository.ChatMessageRepository;
import com.hobbyswap.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ChatController {
    @Autowired private ChatMessageRepository chatMessageRepository;
    @Autowired private UserService userService; // 需要用來撈所有使用者列表
    @Autowired private SimpMessagingTemplate messagingTemplate; // ▼ 核心工具：用來寄信給特定人

    // 1. 進入頁面：撈出歷史訊息
    @GetMapping("/chat")
    public String chatPage(Model model) {
        // 把資料庫裡的所有訊息抓出來，放進 "history" 變數傳給網頁
        model.addAttribute("history", chatMessageRepository.findAllByOrderByTimestampAsc());
        return "chat";
    }

    // 2. 接收並存檔
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(ChatMessage chatMessage, Principal principal) {
        if (principal != null) {
            chatMessage.setSender(principal.getName());
        }
        chatMessage.setTimestamp(LocalDateTime.now()); // 確保有時間

        // 關鍵：儲存到資料庫！
        return chatMessageRepository.save(chatMessage);
    }

    @GetMapping("/private-chat")
    public String privateChatPage(@RequestParam(required = false) String recipient, Model model, Principal principal) {
        String currentUser = principal.getName();

        // 傳送當前登入者名字
        model.addAttribute("currentUser", currentUser);

        // 撈出所有使用者 (排除自己)，顯示在左側列表
        List<User> allUsers = userService.findAllUsers(); // 假設您的 UserService 有這個方法，如果沒有請用 UserRepository.findAll()
        allUsers.removeIf(u -> u.getEmail().equals(currentUser)); // 移除自己
        model.addAttribute("users", allUsers);

        // 如果有點選某人，就撈出跟他的對話紀錄
        if (recipient != null && !recipient.isEmpty()) {
            model.addAttribute("currentRecipient", recipient);
            model.addAttribute("history", chatMessageRepository.findConversation(currentUser, recipient));
        }

        return "private-chat";
    }

    // 2. 接收私訊並轉發
    @MessageMapping("/chat.sendPrivate")
    public void sendPrivateMessage(ChatMessage msg, Principal principal) {
        msg.setSender(principal.getName());
        msg.setTimestamp(LocalDateTime.now());

        // 存入資料庫
        chatMessageRepository.save(msg);

        // A. 寄給「收件人」 (路徑會變成 /user/{recipient}/queue/private)
        messagingTemplate.convertAndSendToUser(
                msg.getRecipient(),
                "/queue/private",
                msg
        );

        // B. 寄回給「自己」 (這樣您的畫面上才看得到自己剛打的字)
        messagingTemplate.convertAndSendToUser(
                msg.getSender(),
                "/queue/private",
                msg
        );
    }
}