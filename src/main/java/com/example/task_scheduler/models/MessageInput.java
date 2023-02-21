package com.example.task_scheduler.models;

import com.example.task_scheduler.enums.MessageStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpMethod;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageInput {

    private String url;
    private HttpMethod httpmethod;

    private JsonNode headers;
    private JsonNode body;

    private MessageStatus status;
    LocalDateTime triggerTime;


    public MessageInput(String url, HttpMethod httpmethod, JsonNode headers, JsonNode body, MessageStatus status,
                        LocalDateTime triggerTime) {
        this.url = url;
        this.httpmethod = httpmethod;
        this.headers = headers;
        this.body = body;
        this.triggerTime = triggerTime;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HttpMethod getHttpmethod() {
        return httpmethod;
    }

    public void setHttpmethod(HttpMethod httpmethod) {
        this.httpmethod = httpmethod;
    }

    public JsonNode getHeaders() {
        return headers;
    }

    public void setHeaders(JsonNode headers) {
        this.headers = headers;
    }

    public JsonNode getBody() {
        return body;
    }

    public void setBody(JsonNode body) {
        this.body = body;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public LocalDateTime getTriggerTime() {
        return triggerTime;
    }

    public void setTriggerTime(LocalDateTime triggerTime) {
        this.triggerTime = triggerTime;
    }
}