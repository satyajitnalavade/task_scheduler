package com.example.task_scheduler.service;

import com.example.task_scheduler.entities.Message;
import com.example.task_scheduler.enums.MessageStatus;
import com.example.task_scheduler.models.MessageInput;
import com.example.task_scheduler.repository.MessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MessageServiceTest {
    @Mock
    private MessageRepository messageRepository;

    @Mock
    private RestTemplate restTemplate;

    @Autowired
    private MessageService messageService;

    @Test
    public void processMessage_shouldSetStatusToComplete_whenRequestIsSuccessful() throws Exception {
        // Arrange
        Message message = new Message();
        message.setId(1L);
        message.setMethod(HttpMethod.GET);
        message.setUrl("https://www.example.com");
        message.setHeaders(new ObjectMapper().createObjectNode());
        message.setBody(new ObjectMapper().createObjectNode());
        message.setStatus(MessageStatus.PENDING);
        message.setRetryCount(0);
        message.setTriggerTime(LocalDateTime.now());

        ResponseEntity<String> responseEntity = new ResponseEntity<>("OK", HttpStatus.OK);
        when(restTemplate.exchange(any(RequestEntity.class), eq(String.class))).thenReturn(responseEntity);

        // Act
        messageService.processMessage(message);

        // Assert
        assertEquals(MessageStatus.COMPLETE, message.getStatus());
        verify(messageRepository).save(message);
    }

    @Test
    public void processMessage_shouldSetStatusToFailed_whenRequestFails() throws Exception {
        // Arrange
        Message message = new Message();
        message.setId(1L);
        message.setMethod(HttpMethod.GET);
        message.setUrl("https://www.example.com");
        message.setHeaders(new ObjectMapper().createObjectNode());
        message.setBody(new ObjectMapper().createObjectNode());
        message.setStatus(MessageStatus.PENDING);
        message.setRetryCount(2);
        message.setTriggerTime(LocalDateTime.now());

        when(restTemplate.exchange(any(RequestEntity.class), eq(String.class))).thenThrow(new RestClientException("Error"));

        // Act
        messageService.processMessage(message);

        // Assert
        assertEquals(MessageStatus.FAILED, message.getStatus());
        verify(messageRepository).save(message);
    }

    @Test
    void processMessage_shouldInvokeMyServiceClient() {
        Message message = new Message();
        message.setUrl("http://example.com");
        message.setMethod(HttpMethod.GET);
        message.setHeaders(new ObjectMapper().createObjectNode());
        when(restTemplate.exchange(any(RequestEntity.class), eq(String.class))).thenReturn(new ResponseEntity<>("", HttpStatus.OK));
        messageService.processMessage(message);
        verify(restTemplate, times(1)).exchange(any(RequestEntity.class), eq(String.class));
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme