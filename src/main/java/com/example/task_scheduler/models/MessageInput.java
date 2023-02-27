package com.example.task_scheduler.models;

import com.example.task_scheduler.enums.MessageStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpMethod;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageInput {

    private String url;
    private String httpmethod;

    private String headers;
    private String body;

    private String status;
    LocalDateTime triggerTime;


    public MessageInput(String url, String httpmethod, String headers, String body, String status, LocalDateTime triggerTime) {
        this.url = url;
        this.httpmethod = httpmethod;
        this.headers = headers;
        this.body = body;
        this.status = status;
        this.triggerTime = triggerTime;
    }

    public MessageInput() {
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHttpmethod() {
        return httpmethod;
    }

    public void setHttpMethod(String httpmethod) {
        this.httpmethod = httpmethod;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTriggerTime() {
        return triggerTime;
    }

    public void setTriggerTime(LocalDateTime triggerTime) {
        this.triggerTime = triggerTime;
    }

    public HttpMethod getHttpMethodEnum() {
        return HttpMethod.valueOf(this.httpmethod.toUpperCase());
    }

    public MessageStatus getStatusEnum() {
        return MessageStatus.valueOf(this.status.toUpperCase());
    }


}