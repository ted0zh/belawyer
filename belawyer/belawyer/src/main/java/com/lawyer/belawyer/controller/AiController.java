package com.lawyer.belawyer.controller;

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



    public AiController(OllamaChatModel chatModel, @Value("${spring.ai.ollama.model}") String modelName) {

        this.chatClient = ChatClient.builder(chatModel)
                .defaultOptions(OllamaOptions.builder()
                        .model(modelName)
                        .temperature(0.4)
                        .build())
                        .build();



        System.out.println(">> ChatClient created with model: " + modelName);
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
