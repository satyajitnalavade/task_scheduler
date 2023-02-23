package com.example.task_scheduler.service;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class MessageHandlerScheduler {

    private final TaskScheduler taskScheduler;
    private final MessageProcessor messageProcessor;

    public MessageHandlerScheduler(TaskScheduler taskScheduler, MessageProcessor messageProcessor) {
        this.taskScheduler = taskScheduler;
        this.messageProcessor = messageProcessor;
    }

    @PostConstruct
    public void scheduleTask() {
        taskScheduler.scheduleAtFixedRate(messageProcessor, 60000);
    }
}