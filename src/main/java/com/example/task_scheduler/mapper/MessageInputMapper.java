package com.example.task_scheduler.mapper;

import com.example.task_scheduler.models.MessageInput;
import com.example.task_scheduler.models.ScheduleMessageInput;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MessageInputMapper {

    public MessageInput toMessageInput(ScheduleMessageInput input) throws JsonProcessingException {
        MessageInput messageInput = new MessageInput();
        messageInput.setUrl(input.getUrl());
        messageInput.setHttpMethod(input.getHttpMethod());
        messageInput.setHeaders(new ObjectMapper().writeValueAsString(input.getHeaders()));
        messageInput.setBody(new ObjectMapper().writeValueAsString(input.getBody()));
        messageInput.setStatus(input.getStatus());
        messageInput.setTriggerTime(LocalDateTime.parse(input.getTriggerTime()));
        return messageInput;
    }
}
