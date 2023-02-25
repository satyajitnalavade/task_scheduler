package com.example.task_scheduler.controller;

import com.example.task_scheduler.entities.Message;
import com.example.task_scheduler.enums.MessageStatus;
import com.example.task_scheduler.models.MessageInput;
import com.example.task_scheduler.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MessageControllerTest {

    @Mock
    private MessageService messageService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    private MessageController messageController;

    @BeforeEach
    void setUp() {
        messageController = new MessageController(messageService);
        mockMvc = MockMvcBuilders.standaloneSetup(messageController).build();
    }

    @Test
    void testCreateMessage() throws Exception {
        MessageInput messageInput = new MessageInput();
        messageInput.setUrl("http://example.com");
        messageInput.setHttpmethod(HttpMethod.POST.toString());
        messageInput.setBody("{\"name\":\"John\",\"age\":30}");
        messageInput.setHeaders("{\"Content-Type\":\"application/json\"}");
        messageInput.setStatus(MessageStatus.PENDING.toString());

        Message message = new Message();
        message.setId(1L);
        message.setUrl("https://example.com");
        message.setMethod(HttpMethod.POST);
        message.setHeaders(new ObjectMapper().readTree("{\"Content-Type\":\"application/json\"}"));
        message.setBody(new ObjectMapper().readTree("{\"name\":\"John\",\"age\":30}"));
        message.setHeadersJson(new ObjectMapper().readTree("{\"Content-Type\":\"application/json\"}").toString());
        message.setBodyJson(new ObjectMapper().readTree("{\"name\":\"John\",\"age\":30}").toString());

        message.setStatus(MessageStatus.PENDING);
        message.setRetryCount(1);
        given(messageService.createMessage(any(MessageInput.class))).willReturn(message);
        String jsonBody = objectMapper.writeValueAsString(messageInput);

        mockMvc.perform(post("/messages/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.url").value("https://example.com"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(jsonPath("$.method").value("POST"))
                .andExpect(jsonPath("$.bodyJson").value("{\"name\":\"John\",\"age\":30}"))
                .andExpect(jsonPath("$.headersJson").value("{\"Content-Type\":\"application/json\"}"));
    }
}
