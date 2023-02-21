package com.example.task_scheduler.entities;

import com.example.task_scheduler.enums.MessageStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonNodeBinaryType;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.http.HttpMethod;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "messages")
@TypeDef(name = "jsonb", typeClass = JsonNodeBinaryType.class)
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime triggerTime;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private HttpMethod method;

    @Column(name = "headers", columnDefinition = "jsonb")
    @Type(type="jsonb")
    private JsonNode headers;

    @Column(name = "body", columnDefinition = "jsonb")
    @Type(type="jsonb")
    private JsonNode body;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageStatus status = MessageStatus.PENDING;

    @Column(name= "retry_count", columnDefinition="integer default 3")
    private int retryCount;


    public Message(JsonNode body, JsonNode headers, HttpMethod method, String url, LocalDateTime triggerTime) {
        this.body = body;
        this.headers = headers;
        this.method = method;
        this.url = url;
        this.triggerTime = triggerTime;
    }





}