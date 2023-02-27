package com.example.task_scheduler.controller;

import com.example.task_scheduler.entities.Message;
import com.example.task_scheduler.mapper.MessageInputMapper;
import com.example.task_scheduler.models.MessageInput;
import com.example.task_scheduler.models.ScheduleMessageInput;
import com.example.task_scheduler.service.MessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;
    private final MessageInputMapper messageInputMapper;

    public MessageController(MessageService messageService, MessageInputMapper messageInputMapper) {
        this.messageService = messageService;
        this.messageInputMapper = messageInputMapper;
    }


    @PostMapping("/create")
    public ResponseEntity<String> myEndpoint(@RequestBody ScheduleMessageInput input) throws JsonProcessingException {
        // Map the ScheduleMessageInput to MessageInput using the mapper
        MessageInput messageInput = messageInputMapper.toMessageInput(input);
        Message createdMessage = messageService.createMessage(messageInput);
        return ResponseEntity.status(HttpStatus.CREATED).body("message-id: " + createdMessage.getId());
    }

    @PostMapping("/process-delayed")
    public ResponseEntity<String> processDelayedMessage() {
        messageService.processDelayedMessage();
        return ResponseEntity.ok().build();
    }


}
