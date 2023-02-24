package com.example.task_scheduler.controller;

import com.example.task_scheduler.entities.Message;
import com.example.task_scheduler.models.MessageInput;
import com.example.task_scheduler.service.MessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<Message> createMessage(@RequestBody MessageInput message) throws JsonProcessingException {
        Message createdMessage = messageService.CreateMessage(message);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMessage);
    }
}