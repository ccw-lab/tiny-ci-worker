package com.ccwlab.worker.controller;

import com.ccwlab.worker.message.Work;
import com.ccwlab.worker.service.DockerService;
import com.ccwlab.worker.service.WorkService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;


@Controller
public class WorkerController {
    Logger logger = LoggerFactory.getLogger(WorkerController.class);

    @Autowired
    DockerService dockerService;

    @Autowired
    StreamBridge streamBridge;

    @Autowired
    WorkService workService;

    @PostConstruct
    void set(){
        this.dockerService.dockerClient();
    }

    Executor executor = Executors.newFixedThreadPool(16);

    @Bean
    Consumer<Work> commandFromController() {
        return work -> {
            this.executor.execute(() -> {
                this.workService.process(work);
                logger.debug("Work: " + work.toString());
            });
        };
    }
}


