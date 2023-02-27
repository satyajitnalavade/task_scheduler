package com.example.task_scheduler.controller;

import com.example.task_scheduler.models.ScheduleMessageInput;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@EnableTransactionManagement
public class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testMyEndpoint() throws Exception {
        // Set up the request body using ScheduleMessageInput class
        ScheduleMessageInput input = new ScheduleMessageInput();
        input.setUrl("https://example.com");
        input.setHttpMethod("GET");
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        input.setHeaders(headers);
        Map<String, String> body = new HashMap<>();
        body.put("key", "value");
        input.setBody(body);
        input.setStatus("PENDING");
        input.setTriggerTime("2022-12-31T23:59:59");

        // Send the request and verify the response
        mockMvc.perform(post("/messages/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(input)))
                .andExpect(status().isCreated());
    }

    @Test
    public void testProcessDelayedMessage() throws Exception {
        mockMvc.perform(post("/messages/process-delayed"))
                .andExpect(status().isOk());
    }

}