package com.example.task_scheduler.service;

import com.example.task_scheduler.entities.Message;
import com.example.task_scheduler.enums.MessageStatus;
import com.example.task_scheduler.models.MessageInput;
import com.example.task_scheduler.repository.MessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class MessageServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ObjectMapper objectMapper;

    private MessageService<Message> messageService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        messageService = new MessageService<>(messageRepository, restTemplate,objectMapper);
    }

    @Test
    public void testProcessMessage_success() throws JsonProcessingException {
        // Setup
        Message message = new Message();
        message.setId(1L);
        message.setUrl("https://example.com");
        message.setMethod(HttpMethod.POST);
        message.setHeaders(new ObjectMapper().readTree("{\"Content-Type\":\"application/json\"}"));
        message.setBody(new ObjectMapper().readTree("{\"name\":\"John\",\"age\":30}"));
        message.setStatus(MessageStatus.PENDING);
        message.setRetryCount(1);
        ResponseEntity<String> responseEntity = new ResponseEntity<>("", HttpStatus.OK);
        Optional<Message> optionalMessage = Optional.of(message);
        when(messageRepository.findByIdForUpdate(any(Long.class))).thenReturn(optionalMessage);
        when(restTemplate.exchange(any(RequestEntity.class), eq(String.class))).thenReturn(responseEntity);

        // Execute
        messageService.processMessage(message);

        assertEquals(MessageStatus.COMPLETE, message.getStatus());
        assertEquals(2, message.getRetryCount());

        // Verify
        verify(restTemplate, times(1)).exchange(any(RequestEntity.class), eq(String.class));
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    public void testProcessMessage_fail() throws JsonProcessingException {
        // Setup
        Message message = new Message();
        message.setUrl("https://example.com");
        message.setMethod(HttpMethod.POST);
        message.setHeaders(new ObjectMapper().readTree("{\"Content-Type\":\"application/json\"}"));
        message.setBody(new ObjectMapper().readTree("{\"name\":\"John\",\"age\":30}"));
        message.setStatus(MessageStatus.PENDING);
        message.setRetryCount(3);
        Optional<Message> optionalMessage = Optional.of(message);
        when(messageRepository.findByIdForUpdate(any())).thenReturn(optionalMessage);
        ResponseEntity<String> responseEntity = new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.exchange(any(RequestEntity.class), eq(String.class))).thenThrow(new RuntimeException("Internal Server Error"));

        // Execute
        messageService.processMessage(message);

        assertEquals(MessageStatus.FAILED, message.getStatus());
        // Verify
        verify(restTemplate, times(1)).exchange(any(RequestEntity.class), eq(String.class));
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    public void testProcessMessage_retry_Pending() throws JsonProcessingException {
        // Setup
        Message message = new Message();
        message.setId(1L);
        message.setUrl("https://example.com");
        message.setMethod(HttpMethod.POST);
        message.setHeaders(new ObjectMapper().readTree("{\"Content-Type\":\"application/json\"}"));
        message.setBody(new ObjectMapper().readTree("{\"name\":\"John\",\"age\":30}"));
        message.setStatus(MessageStatus.PENDING);
        message.setRetryCount(2);
        message.setTriggerTime(LocalDateTime.now().minus(Duration.ofSeconds(30)));

        ResponseEntity<String> responseEntity = new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR);
        Optional<Message> optionalMessage = Optional.of(message);
        when(messageRepository.findByIdForUpdate(any())).thenReturn(optionalMessage);

        when(restTemplate.exchange(any(RequestEntity.class), eq(String.class))).thenThrow(new RuntimeException("Internal Server Error"));

        // Execute
        messageService.processMessage(message);

        assertEquals(MessageStatus.PENDING, message.getStatus());
        assertEquals(3, message.getRetryCount());
        // Verify
        verify(restTemplate, times(1)).exchange(any(RequestEntity.class), eq(String.class));
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    public void testCreateMessage_success() throws JsonProcessingException {
        // Setup
        MessageInput messageInput = new MessageInput("https://example.com",
                "POST",
                "{\"Content-Type\":\"application/json\"}",
                "{\"name\":\"John\",\"age\":30}",
                "PENDING",
                LocalDateTime.now().minus(Duration.ofSeconds(30)));

        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message savedMessage = invocation.getArgument(0);
            savedMessage.setId(1L);
            return savedMessage;
        });

        // Call createMessage()
        Message result = messageService.CreateMessage(messageInput);

        // Verify the message was saved with the correct data
        assertEquals(1L, result.getId());
        assertEquals(messageInput.getUrl(), result.getUrl());
        assertEquals(messageInput.getHttpmethod(), result.getMethod().toString());
        assertEquals(messageInput.getHeaders(), new ObjectMapper().writeValueAsString(result.getHeaders()));
        assertEquals(messageInput.getBody(), new ObjectMapper().writeValueAsString(result.getBody()));
        assertEquals(MessageStatus.PENDING, result.getStatus());
        assertEquals(messageInput.getTriggerTime(), result.getTriggerTime());

        verify(messageRepository).save(argThat(message ->
                message.getUrl().equals(messageInput.getUrl()) &&
                        message.getStatus().equals(MessageStatus.PENDING) &&
                        message.getTriggerTime().equals(messageInput.getTriggerTime())
        ));

    }



}
