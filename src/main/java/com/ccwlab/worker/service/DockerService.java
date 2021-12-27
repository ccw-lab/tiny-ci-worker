package com.ccwlab.worker.service;

import com.github.dockerjava.api.DockerClient;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.function.Consumer;

@Service
public class DockerService {
    Logger logger = LoggerFactory.getLogger(DockerService.class);
    private DockerClient dockerClient;

    @Value("${docker.uri}")
    private String uri;

    public DockerClient dockerClient(){
        if(this.dockerClient != null)
            return this.dockerClient;
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(uri)
                .build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .build();
        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
        this.dockerClient = dockerClient;
        return dockerClient;
    }

    public void pullImage(String imageNameWithTag, Consumer<PullResponseItem> callback) throws InterruptedException {
        		var pullImageResultCallback = new PullImageResultCallback(){
			@Override
			public void onNext(PullResponseItem item) {
				super.onNext(item);
                callback.accept(item);
				logger.debug("[pullImageCmd] onNext: " + item.toString());
			}
		};
		dockerClient.pullImageCmd("pulling image " + imageNameWithTag)
                .withRepository(imageNameWithTag)
                .exec(pullImageResultCallback);
		pullImageResultCallback.awaitCompletion();
    }

    public String createContainer(String imageNameWithTag, String containerId
			, String src
			, String dest
			, String scriptPathOnLinux) throws InterruptedException {
        		var res= dockerClient.createContainerCmd("create a container with " + imageNameWithTag)
                        .withName(containerId)
				.withImage(imageNameWithTag)
				.withAttachStderr(true)
				.withAttachStdout(true)
				.withHostConfig(HostConfig
						.newHostConfig()
						.withMemory(4096000000L)
						.withBinds(new Bind(src,new Volume(dest)))
				)
				.withCmd("sh", scriptPathOnLinux).withWorkingDir(dest)
				.exec();
                return res.getId();
    }

    public void startContainer(String containerId, Consumer<Frame> callback) throws InterruptedException {
        dockerClient.startContainerCmd("start a container " + containerId)
                .withContainerId(containerId)
                .exec();
		WaitContainerResultCallback waitContainerResultCallback = new WaitContainerResultCallback();
		dockerClient.waitContainerCmd("wait a container " + containerId)
                .withContainerId(containerId)
                .exec(waitContainerResultCallback);

		var logContainerResultCallback = new LogContainerResultCallback(){
			@Override
			public void onNext(Frame item) {
				super.onNext(item);
                callback.accept(item);
				logger.debug("[logContainerCmd] onNext "+item.toString());
			}
		};
		dockerClient.logContainerCmd("print logs").withContainerId(containerId)
				.withStdErr(true)
				.withFollowStream(true)
				.withStdOut(true)
				.exec(logContainerResultCallback);
		logContainerResultCallback.awaitCompletion();
		waitContainerResultCallback.awaitCompletion();
    }

    public void removeContainer(String containerId){
		dockerClient.removeContainerCmd("remove newone").withContainerId(containerId).exec();
    }
}
