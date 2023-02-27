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

    @Column(name= "retry_count", columnDefinition="integer")
    private int retryCount=0;

    private String headersJson;
    private String bodyJson;

    public Message(JsonNode body, JsonNode headers, HttpMethod method, String url, LocalDateTime triggerTime) {
        this.body = body;
        this.headers = headers;
        this.method = method;
        this.url = url;
        this.triggerTime = triggerTime;
    }


    public void setHeadersJson(String headersJson) {
        this.headersJson=headersJson;
    }

    public void setBodyJson(String bodyJson) {
        this.bodyJson=bodyJson;
    }
}