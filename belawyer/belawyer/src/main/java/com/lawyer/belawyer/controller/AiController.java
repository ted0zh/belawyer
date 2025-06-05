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
import jakarta.annotation.PostConstruct;
//import lombok.Value;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${spring.ai.ollama.model}")
    private String aiModelName;


    public AiController(OllamaChatModel chatModel, @Value("${spring.ai.ollama.model}") String modelName) {

        this.chatClient = ChatClient.builder(chatModel)
                .defaultOptions(OllamaOptions.builder().model(modelName).temperature(0.4).build())
                        .build();



        System.out.println(">> ChatClient created with model: " + modelName);
    }

    @PostConstruct
    public void printModelName() {
        System.out.println(">> AI Model loaded = " + aiModelName);
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
