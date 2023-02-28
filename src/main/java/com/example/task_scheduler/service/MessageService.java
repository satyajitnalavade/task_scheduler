package com.example.task_scheduler.service;

import com.example.task_scheduler.entities.Message;
import com.example.task_scheduler.enums.MessageStatus;
import com.example.task_scheduler.models.MessageInput;
import com.example.task_scheduler.repository.MessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
//@Transactional
public class MessageService {

    private final Logger logger = LoggerFactory.getLogger(MessageService.class);

    private final MessageRepository messageRepository;
    private final RestTemplate restTemplate;
    private RedisTemplate<String, Object> redisTemplate;

    public MessageService(MessageRepository messageRepository, RestTemplate restTemplate, RedisTemplate<String, Object> redisTemplate) {
        this.messageRepository = messageRepository;
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public Message createMessage(MessageInput messageInput) throws JsonProcessingException {
        Message message = new Message();
        message.setHeaders(new ObjectMapper().readTree(messageInput.getHeaders()));
        message.setBody(new ObjectMapper().readTree(messageInput.getBody()));
        message.setMethod(messageInput.getHttpMethodEnum());
        message.setUrl(messageInput.getUrl());
        message.setTriggerTime(messageInput.getTriggerTime());

        String headersJson = new ObjectMapper().writeValueAsString(message.getHeaders());
        String bodyJson = new ObjectMapper().writeValueAsString(message.getBody());
        message.setHeadersJson(headersJson);
        message.setBodyJson(bodyJson);

        return messageRepository.save(message);

    }



   // @Transactional
    public void processDelayedMessage() {
        List<Message> delayedMessages = messageRepository.findByTriggerTimeBeforeAndStatus(LocalDateTime.now(),
                    MessageStatus.PENDING);

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Message msg : delayedMessages) {
            futures.add(CompletableFuture.runAsync(() -> processMessage(msg))
                    .exceptionally(ex -> {
                        logger.error("Exception occurred while processing message with id {}: {}", msg.getId(), ex.getMessage());
                        // Handle exceptions here
                        return null;
                    }));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    /*public List<Message> getByTriggerTimeBetweenAndStatus(LocalDateTime start, LocalDateTime end, MessageStatus status) {
        return messageRepository.findByTriggerTimeBetweenAndStatus(start, end, status);
    }*/

    public List<Message> getByTriggerTimeBeforeAndStatus(LocalDateTime end, MessageStatus messageStatus) {
        return messageRepository.findByTriggerTimeBeforeAndStatus(end,messageStatus );
    }


   // @Transactional
    void processMessage(Message message) {
        logger.info("Processing message: {}", message);
        // Generate a unique lock key for the message
        String lockKey = "message:" + message.getId();
        // Try to acquire the lock on the message
        String lockValue = UUID.randomUUID().toString();

        boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, Duration.ofSeconds(30));

        if (!lockAcquired) {
            // Another instance is already processing this message
            logger.info("Message already being processed by another instance: {}", message.getId());
            return;
        }

        try {
            Optional<Message> messageOptional = messageRepository.findByIdForUpdate(message.getId());
            // Check again if the message is NEW in case another instance of the processor has updated the status
            if (messageOptional.isPresent() && (messageOptional.get().getStatus() == MessageStatus.PENDING)) {
                 message = messageOptional.get();
                ResponseEntity<String> responseEntity = restTemplate.exchange(createRequestEntity(message), String.class);
                logger.info("Response entity: {}", responseEntity.getBody());
                message.setStatus(MessageStatus.COMPLETE);
                message.setRetryCount(message.getRetryCount() + 1);
                messageRepository.save(message);
            }
        }
        catch (Exception e) {
            if (message.getRetryCount() >= 3) {
                message.setStatus(MessageStatus.FAILED);
            } else {
                message.setStatus(MessageStatus.PENDING);
                message.setRetryCount(message.getRetryCount() + 1);
                long delayInSeconds = (long) message.getRetryCount() * 60;
                message.setTriggerTime(LocalDateTime.now().plus(Duration.ofSeconds(delayInSeconds)));
            }
            messageRepository.save(message);
        } finally {
            // Release the lock on the message
            String lockValueInRedis = (String) redisTemplate.opsForValue().get(lockKey);
            if (lockValue.equals(lockValueInRedis)) {
                redisTemplate.delete(lockKey);
            }
        }
    }


    private RequestEntity<JsonNode> createRequestEntity(Message message) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode headerNode = message.getHeaders();
        if (headerNode != null && headerNode.size() != 0) {
            Map<String, String> headerMap = objectMapper.convertValue(headerNode, new TypeReference<Map<String, String>>() {
            });
            headers.setAll(headerMap);
        }
        URI url = URI.create(message.getUrl());

        JsonNode bodyNode = message.getBody();
        if (bodyNode != null && bodyNode.size() != 0) {
            return new RequestEntity<>(bodyNode, headers, message.getMethod(), url);
        } else {
            return new RequestEntity<>(null, headers, message.getMethod(), url);
        }
    }


    public Optional<Message> findByIdForUpdate(Long id) {
        return messageRepository.findByIdForUpdate(id);
    }
}
