package com.example.task_scheduler.controller;

import com.example.task_scheduler.models.ScheduleMessageInput;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ScheduleMessageControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void scheduleMessage() throws Exception {
        ScheduleMessageInput input = new ScheduleMessageInput();
        input.setUrl("http://example.com");
        input.setHttpMethod("POST");
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        input.setHeaders(headers);
        Map<String, String> body = new HashMap<>();
        body.put("name", "John Doe");
        body.put("age", "30");
        input.setBody(body);
        input.setStatus("PENDING");
        input.setTriggerTime("2023-02-28T10:00:00");

        ResponseEntity<String> response = restTemplate.postForEntity("/messages/create", input, String.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }
}
