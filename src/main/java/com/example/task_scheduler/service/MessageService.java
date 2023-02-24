package com.example.task_scheduler.service;

import com.example.task_scheduler.entities.Message;
import com.example.task_scheduler.enums.MessageStatus;
import com.example.task_scheduler.models.MessageInput;
import com.example.task_scheduler.repository.MessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class MessageService {

    private MessageRepository messageRepository;
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;


    @Autowired
    public MessageService(MessageRepository messageRepository, RestTemplate restTemplate) {
        this.messageRepository = messageRepository;
        this.restTemplate = restTemplate;

    }

    @Transactional
    public Message CreateMessage(MessageInput messageInput) throws JsonProcessingException {
        Message message = new Message();
        message.setHeaders(new ObjectMapper().readTree(messageInput.getHeaders()));
        message.setBody(new ObjectMapper().readTree(messageInput.getBody()));
        message.setMethod(messageInput.getHttpMethodEnum());
        message.setUrl(messageInput.getUrl());
        message.setTriggerTime(messageInput.getTriggerTime());

        message = messageRepository.save(message);
        return message;
    }

    @Transactional
    public void processDelayedMessage() {
        List<Message> delayedMessages = getByTriggerTimeBeforeAndStatus(LocalDateTime.now(),
                MessageStatus.PENDING);
        for (Message delayedMessage : delayedMessages) {
            CompletableFuture.runAsync(() -> processMessage(delayedMessage));
        }
    }

    public List<Message> getByTriggerTimeBetweenAndStatus(LocalDateTime start, LocalDateTime end, MessageStatus status) {
        return messageRepository.findByTriggerTimeBetweenAndStatus(start, end, status);
    }

    public List<Message> getByTriggerTimeBeforeAndStatus(LocalDateTime end, MessageStatus messageStatus) {
        return messageRepository.findByTriggerTimeBeforeAndStatus(end, messageStatus);
    }


    @Transactional
    void processMessage(Message message) {
        try {

            Optional<Message> messageOptional = messageRepository.findByIdForUpdate(message.getId());
            // Check again if the message is NEW in case another instance of the processor has updated the status
            if (messageOptional.isPresent() && (messageOptional.get().getStatus() == MessageStatus.PENDING)) {
                 message = messageOptional.get();


                ResponseEntity<String> responseEntity = restTemplate.exchange(createRequestEntity(message), String.class);
                message.setStatus(MessageStatus.COMPLETE);
                message.setRetryCount(message.getRetryCount() + 1);
                messageRepository.save(message);
            }

        }
        catch (OptimisticLockingFailureException | PessimisticLockingFailureException ex) {
            // handle optimistic locking failure
            throw new IllegalStateException("Failed to acquire lock on message row.", ex);
        } catch (Exception e) {
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


    public Optional<Message> findByIdForUpdate(Long id) {
        return messageRepository.findByIdForUpdate(id);
    }
}
