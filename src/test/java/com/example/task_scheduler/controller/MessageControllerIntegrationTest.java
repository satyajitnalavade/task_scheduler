package com.example.task_scheduler.controller;


import com.example.task_scheduler.entities.Message;
import com.example.task_scheduler.enums.MessageStatus;
import com.example.task_scheduler.models.MessageInput;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(MessageController.class)
public class MessageControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    public void testCreateMessage() throws Exception {
        // create a test message
        MessageInput messageInput = new MessageInput();
        messageInput.setUrl("http://example.com");
        messageInput.setHttpmethod(HttpMethod.POST.toString());
        messageInput.setBody("{\"name\":\"John\",\"age\":30}");
        messageInput.setHeaders("{\"Content-Type\":\"application/json\"}");
        messageInput.setStatus(MessageStatus.PENDING.toString());
        String jsonBody = objectMapper.writeValueAsString(messageInput);

        // perform a POST request to create the message
        MvcResult result = mockMvc.perform(post("/messages/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isCreated())
                .andReturn();

        // verify the response body
        MockHttpServletResponse response = result.getResponse();
        Message message = objectMapper.readValue(response.getContentAsString(), Message.class);
        assertThat(message.getId()).isNotNull();
        assertThat(message.getMethod().toString()).isEqualTo("POST");
        assertThat(message.getUrl()).isEqualTo("https://example.com");
        assertThat(message.getHeaders()).isEqualTo(objectMapper.readTree("{\"Content-Type\":\"application/json\"}"));
        assertThat(message.getBody()).isEqualTo(objectMapper.readTree("{\"name\":\"John\",\"age\":30}"));
        assertThat(message.getTriggerTime()).isNull();
        assertThat(message.getStatus()).isEqualTo(MessageStatus.PENDING);
        assertThat(message.getRetryCount()).isEqualTo(0);
    }

}