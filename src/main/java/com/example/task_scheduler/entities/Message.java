package com.example.task_scheduler.entities;

import com.example.task_scheduler.enums.MessageStatus;
import org.springframework.http.HttpMethod;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime triggerTime;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private HttpMethod method;

    @ElementCollection
    @CollectionTable(name = "message_header",joinColumns = @JoinColumn(name = "message_id"))
    @MapKeyColumn(name = "header_param_key")
    @Column(name = "header_param_value")
    private Map<String,String> header = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "message_body",joinColumns = @JoinColumn(name = "message_id"))
    @MapKeyColumn(name = "body_param_key")
    @Column(name = "body_param_value")
    private Map<String,String> body = new HashMap<>();

    private MessageStatus status = MessageStatus.PENDING;

}