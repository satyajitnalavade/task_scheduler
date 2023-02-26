package com.example.task_scheduler.controller;

import com.example.task_scheduler.entities.Message;
import com.example.task_scheduler.models.MessageInput;
import com.example.task_scheduler.service.MessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/create")
    public ResponseEntity<Message> createMessage(@RequestBody MessageInput messageInput) throws JsonProcessingException {
        Message createdMessage = messageService.createMessage(messageInput);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMessage);
    }

    @GetMapping("/process-delayed")
    public ResponseEntity<String> processDelayedMessage(Message message) {
        messageService.processDelayedMessage();
        return ResponseEntity.ok().build();
    }

}