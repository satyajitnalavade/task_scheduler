package com.example.task_scheduler.service;

import com.example.task_scheduler.entities.Message;
import com.example.task_scheduler.enums.MessageStatus;
import com.example.task_scheduler.models.MessageInput;
import com.example.task_scheduler.repository.MessageRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class MessageService<objectMapper> {

    private final MessageRepository messageRepository;
    private RestTemplate restTemplate;

    @Autowired
    public MessageService(MessageRepository messageRepository, RestTemplate restTemplate) {
        this.messageRepository = messageRepository;
        }

    public Message CreateMessage(MessageInput messageInput){
       Message message = new Message(messageInput.getBody(), messageInput.getHeaders(), messageInput.getHttpmethod(),
               messageInput.getUrl(), messageInput.getTriggerTime());
        message = messageRepository.save(message);
       return message;
    }

    public void processDelayedMessage(){
        List<Message> delayedMessages = messageRepository.findByTriggerTimeBeforeAndStatus(Instant.now(),
                MessageStatus.PENDING);
        for(Message delayedMessage : delayedMessages) {
            CompletableFuture.runAsync(() -> processMessage(delayedMessage));
        }
    }

    private void processMessage(Message message) {
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(createRequestEntity(message), String.class);
            message.setStatus(MessageStatus.COMPLETE);
            message.setRetryCount(message.getRetryCount() + 1);
            messageRepository.save(message);
        } catch (Exception e) {
            if (message.getRetryCount() >= 3) {
                message.setStatus(MessageStatus.FAILED);
            } else {
                message.setStatus(MessageStatus.PENDING);
                message.setRetryCount(message.getRetryCount() + 1);
                message.setTriggerTime(LocalDateTime.now().plus(Duration.ofSeconds(message.getRetryCount() * 60)));
            }
            messageRepository.save(message);
        }
    }

    private RequestEntity<JsonNode> createRequestEntity(Message message) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode headerNode = message.getHeaders();
        Map<String, String> headerMap = objectMapper.convertValue(headerNode, new TypeReference<Map<String, String>>() {});
        headers.setAll(headerMap);
        URI url = URI.create(message.getUrl());

        return new RequestEntity<JsonNode>(
                message.getBody(),headers,
                message.getMethod(),URI.create(message.getUrl()));
    }



}