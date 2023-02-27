package com.example.task_scheduler.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@JsonTest
@SpringJUnitConfig
public class MessageInputTest {


    private JacksonTester<MessageInput> json;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        JacksonTester.initFields(this, objectMapper);
    }

        @Test
    public void testSerialize() throws Exception {
        MessageInput messageInput = new MessageInput();
        messageInput.setBody("{\"name\":\"John\",\"age\":30}");
        messageInput.setHeaders("{\"Content-Type\":\"application/json\"}");
        messageInput.setUrl("http://example.com");
        messageInput.setHttpMethod("POST");
        messageInput.setStatus("PENDING");

        JsonContent<MessageInput> jsonContent = json.write(messageInput);

        assertThat(jsonContent).hasJsonPathStringValue("$.body");
        assertThat(jsonContent).hasJsonPathStringValue("$.headers");
        assertThat(jsonContent).hasJsonPathStringValue("$.url");
        assertThat(jsonContent).extractingJsonPathStringValue("$.body").isEqualTo("{\"name\":\"John\",\"age\":30}");
        assertThat(jsonContent).extractingJsonPathStringValue("$.headers").isEqualTo("{\"Content-Type\":\"application/json\"}");
        assertThat(jsonContent).extractingJsonPathStringValue("$.url").isEqualTo("http://example.com");
    }

    @Test
    public void testDeserialize() throws Exception {
        MessageInput messageInput = new MessageInput();
        messageInput.setBody("{\"name\":\"John\",\"age\":30}");
        messageInput.setHeaders("{\"Content-Type\":\"application/json\"}");
        messageInput.setUrl("http://example.com");
        messageInput.setHttpMethod("POST");
        messageInput.setStatus("PENDING");

        String jsonStr = json.write(messageInput).getJson();
        MessageInput deserialized = json.parse(jsonStr).getObject();

        assertThat(deserialized.getBody()).isEqualTo("{\"name\":\"John\",\"age\":30}");
        assertThat(deserialized.getHeaders()).isEqualTo("{\"Content-Type\":\"application/json\"}");
        assertThat(deserialized.getUrl()).isEqualTo("http://example.com");
        assertThat(deserialized.getHttpmethod()).isEqualTo("POST");
        assertThat(deserialized.getStatus()).isEqualTo("PENDING");
    }


}
