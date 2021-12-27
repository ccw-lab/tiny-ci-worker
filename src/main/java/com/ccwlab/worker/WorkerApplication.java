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

//		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
//				.withDockerHost("tcp://127.0.0.1:23750")
//				.build();
//		DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
//				.dockerHost(config.getDockerHost())
//				.build();
//		DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
//		logger.debug(dockerClient.listImagesCmd().exec().size()+"");
//
//		var pullImageResultCallback = new PullImageResultCallback(){
//			@Override
//			public void onNext(PullResponseItem item) {
//				super.onNext(item);
//				logger.debug("[pullImageCmd] onNext: " + item.toString());
//			}
//		};
//		dockerClient.pullImageCmd("pull image").withRepository("centos:centos7").exec(pullImageResultCallback);
//		pullImageResultCallback.awaitCompletion();
//		var res= dockerClient.createContainerCmd("newone")
//				.withImage("centos:centos8")
//				.withAttachStderr(true)
//				.withAttachStdout(true)
//				.withHostConfig(HostConfig
//						.newHostConfig()
//						.withBinds(new Bind("/root",new Volume("/tmp")))
//				)
//				.withCmd("sh", "/tmp/hello.sh")
//				.exec();
//		dockerClient.startContainerCmd("newone").withContainerId(res.getId()).exec();
//		WaitContainerResultCallback callback = new WaitContainerResultCallback();
//		dockerClient.waitContainerCmd("wait a container").withContainerId(res.getId()).exec(callback);
//		callback.awaitCompletion();
//
//		var logContainerResultCallback = new LogContainerResultCallback(){
//			@Override
//			public void onNext(Frame item) {
//				super.onNext(item);
//				logger.debug("[logContainerCmd] onNext "+item.toString());
//			}
//		};
//		dockerClient.logContainerCmd("newone").withContainerId(res.getId())
//				.withStdErr(true)
//				.withStdOut(true)
//				.withTailAll()
//				.exec(logContainerResultCallback);
//		logContainerResultCallback.awaitCompletion();
//
//		dockerClient.removeContainerCmd("remove newone").withContainerId(res.getId()).exec();
		SpringApplication.run(WorkerApplication.class, args);
	}

}
