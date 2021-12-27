package com.ccwlab.worker.service;

import com.ccwlab.worker.GithubUtil;
import com.ccwlab.worker.message.CiFile;
import com.ccwlab.worker.message.Report;
import com.ccwlab.worker.message.Result;
import com.ccwlab.worker.message.Work;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class WorkService {
    Logger logger = LoggerFactory.getLogger(WorkService.class);
    @Autowired
    DockerService dockerService;

    @Value("${docker.workspace}")
    String workspace;

    @Autowired
    StreamBridge streamBridge;

    List<Path> directoriesToClean = new ArrayList<>();

    @Autowired
    GithubUtil githubUtil;
    public void process(Work work) {
        logger.debug("process: " + work.toString());
        var workspace = Paths.get(this.workspace);
        var uuid = UUID.randomUUID();
        var repositoryPath = workspace.resolve(uuid.toString());
        logger.debug("path: "+ repositoryPath.toString());
        try {
            var result = clone(work, repositoryPath);
            if(result)
                streamBridge.send("reporting-out-0", new Report(Result.PROGRESS, work.getId(), "Cloning is successful."));
            else {
                streamBridge.send("reporting-out-0", new Report(Result.FAILED, work.getId(), "A clone failure by exitCode."));
                return;
            }
            var checkoutResult = checkout(work.getCommitId(), repositoryPath);
            if(checkoutResult)
                streamBridge.send("reporting-out-0", new Report(Result.PROGRESS, work.getId(), "Checking out from " + work.getCommitId() + " is successful."));
            else {
                streamBridge.send("reporting-out-0", new Report(Result.FAILED, work.getId(), "A checkout failure by exitCode."));
                return;
            }

            var ciFilePath = repositoryPath.resolve("ci.json");
            var ciFile = objectMapper.readValue(ciFilePath.toFile(), CiFile.class);
            var imageNameWithTag = ciFile.getImage();
            var linuxWorkingDirectory = "/tmp";
            streamBridge.send("reporting-out-0", new Report(Result.PROGRESS, work.getId(), "Creating a command script which a user requested."));
            var scriptName = this.createScript(work, repositoryPath, ciFile);
            streamBridge.send("reporting-out-0", new Report(Result.PROGRESS, work.getId(), "Pulling " + imageNameWithTag));
            this.dockerService.pullImage(imageNameWithTag, (item) -> {
                streamBridge.send("reporting-out-0", new Report(Result.PROGRESS, work.getId(), item.getStatus()));
            });
            var containerId = this.dockerService.createContainer(imageNameWithTag, uuid.toString(), repositoryPath.toAbsolutePath().toString(), linuxWorkingDirectory, linuxWorkingDirectory + "/" + scriptName);
            this.dockerService.startContainer(containerId, frame -> {
                streamBridge.send("reporting-out-0", new Report(Result.PROGRESS, work.getId(), frame.toString()));
            });
            this.dockerService.removeContainer(containerId);
            streamBridge.send("reporting-out-0", new Report(Result.PROGRESS, work.getId(), "Removing a container" + containerId));
            streamBridge.send("reporting-out-0", new Report(Result.SUCCESS, work.getId(), "A job has just been finished. A work id is " + work.getId() + "."));
        }catch(Exception e){
            logger.warn("ex", e);
            streamBridge.send("reporting-out-0", new Report(Result.FAILED, work.getId(),e.toString()));
        }finally {
            directoriesToClean.add(repositoryPath);
        }
    }

    private boolean clone(Work work, Path repositoryPath) throws IOException, InterruptedException {
        var accessToken = work.getAccessToken();
        var gitHub= githubUtil.get(accessToken);
        var repository = gitHub.getRepositoryById(work.getRepositoryId());
        var url = repository.getHttpTransportUrl().replace("https://", "https://"+ accessToken + "@");
        var builder = new ProcessBuilder();
        builder.command("git", "clone", url, repositoryPath.toAbsolutePath().toString());
        logger.debug(builder.toString());
        logger.debug("git " + "clone " + url + " " + repositoryPath.toAbsolutePath().toString());
        streamBridge.send("reporting-out-0", new Report(Result.PROGRESS, work.getId(), "[Clone] " + repository.getHttpTransportUrl().toString()));
        var process = builder.start();
        var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuffer buf = new StringBuffer();
        String line = null;
        while((line = reader.readLine()) != null){
            buf.append(line);
            logger.debug("[line]" + line);
            streamBridge.send("reporting-out-0", new Report(Result.PROGRESS, work.getId(), "[line]" + line));
        }
        int exitCode = process.waitFor();
        return exitCode == 0;
    }

    private boolean checkout(String commitId, Path repositoryPath) throws IOException, InterruptedException {
        var processBuilder = new ProcessBuilder();
        processBuilder.directory(repositoryPath.toFile());
        processBuilder.command("git", "checkout", commitId);
        var process = processBuilder.start();
        int exitCode = process.waitFor();
        return exitCode == 0;
    }

    @Autowired
    ObjectMapper objectMapper;

    private String createScript(Work work, Path repositoryPath, CiFile ciFile) throws IOException {
        StringBuilder sb = new StringBuilder();
        ciFile.getCmd().forEach(cmd -> {
            sb.append(cmd+"\n");
        });
        var command = sb.toString();
        var script = repositoryPath.resolve(repositoryPath.getFileName().toString());
        Files.write(script, command.getBytes(StandardCharsets.UTF_8));
        return repositoryPath.getFileName().toString();
    }
}
