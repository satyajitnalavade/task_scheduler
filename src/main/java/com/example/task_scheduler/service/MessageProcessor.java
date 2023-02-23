package com.example.task_scheduler.service;

import com.example.task_scheduler.entities.Message;
import com.example.task_scheduler.enums.MessageStatus;
import com.example.task_scheduler.repository.MessageRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class MessageProcessor implements Runnable{
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProcessor.class);
    private MessageRepository messageRepository;
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    public MessageProcessor(MessageRepository messageRepository, RestTemplate restTemplate) {
        this.messageRepository = messageRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    @Scheduled(fixedDelayString = "6000")
    @Transactional
    public void run() {
        LOGGER.info("Starting message processing");

        List<Message> messages = messageRepository.findByTriggerTimeBetweenAndStatus(LocalDateTime.now().minusMinutes(2), LocalDateTime.now(), MessageStatus.PENDING);

        for (Message message : messages) {
            CompletableFuture.runAsync(() -> ProcessMessage(message));
        }

        LOGGER.info("Message processing complete");
    }



    @Transactional
    void ProcessMessage(Message message) {
        try {
            LOGGER.info("Processing message: {}", message);
            // Use select ... for update to ensure only one instance of the processor reads a message at a time
            Optional<Message> messageOptional = messageRepository.findByIdForUpdate(message.getId());
            // Check again if the message is NEW in case another instance of the processor has updated the status
            if (messageOptional.isPresent() && (messageOptional.get().getStatus() == MessageStatus.PENDING)) {
                message = messageOptional.get();
                ResponseEntity<String> response = restTemplate.exchange(createRequestEntity(message), String.class);

                if (response.getStatusCode() == HttpStatus.OK) {
                    message.setStatus(MessageStatus.COMPLETE);
                    message.setRetryCount(message.getRetryCount() + 1);
                } else {
                    LOGGER.warn("API response returned status code {}", response.getStatusCodeValue());
                    message.setStatus(MessageStatus.FAILED);
                }
                messageRepository.save(message);
            } else {
                LOGGER.info("Message status is {}, skipping processing", message.getStatus());
            }

        }
        catch (OptimisticLockingFailureException | PessimisticLockingFailureException ex) {
            // handle optimistic locking failure
            throw new IllegalStateException("Failed to acquire lock on message row.", ex);
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
        }
    }

    private RequestEntity<JsonNode> createRequestEntity(Message message) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode headerNode = message.getHeaders();
        Map<String, String> headerMap = objectMapper.convertValue(headerNode, new TypeReference<Map<String, String>>() {
        });
        headers.setAll(headerMap);
        URI url = URI.create(message.getUrl());

        return new RequestEntity<>(
                message.getBody(), headers,
                message.getMethod(), URI.create(message.getUrl()));
    }



}
