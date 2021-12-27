package com.ccwlab.worker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;

import java.io.Closeable;
import java.io.IOException;

@SpringBootApplication
//@EnableBinding({WorkerInputProcessor.class, WorkerOutputProcessor.class})
public class WorkerApplication {

	public static void main(String[] args) throws InterruptedException {
		Logger logger =  LoggerFactory.getLogger(WorkerApplication.class);
		SpringApplication.run(WorkerApplication.class, args);
	}

}
