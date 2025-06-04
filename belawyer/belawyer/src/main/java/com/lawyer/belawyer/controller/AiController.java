package com.lawyer.belawyer.controller;
//
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@CrossOrigin(origins = "http://localhost:3000")
//@RequestMapping("/api/v1")
//public class AiController {
//
//    private final ChatClient chatClient;
//    public AiController(ChatClient.Builder chatClientBuilder) {
//        this.chatClient = chatClientBuilder.build();
//    }
//
//    @GetMapping("/ai")
//    public String generation(@RequestParam String userInput) {
//        return this.chatClient.prompt()
//                .user(userInput)
//                .call()
//                .content();
//    }
//
//}
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
public class AiController {

    private ChatClient chatClient;

    public AiController(OllamaChatModel chatModel) {
        this.chatClient = ChatClient.create(chatModel);
    }

    @GetMapping("/ai")
    public ResponseEntity<String> getAnswer(@RequestParam String message) {

        ChatResponse chatResponse = chatClient
                .prompt(message)
                .call()
                .chatResponse();

        System.out.println(chatResponse.getMetadata().getModel());

        String response = chatResponse.getResult().getOutput().getText();

        return ResponseEntity.ok(response);
    }
}
