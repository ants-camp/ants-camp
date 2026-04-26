package io.antcamp.notificationservice.infrastructure.client.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.dockerjava.api.model.ExposedPort;
import io.antcamp.notificationservice.application.port.RestartPort;
import io.antcamp.notificationservice.application.port.RollbackPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class DockerOperationClient implements RestartPort, RollbackPort {

    private final DockerClient dockerClient;
    private final Environment environment;

    @Override
    public void restart(String job) {
        String containerName = toContainerName(job);
        String containerId = findContainerId(containerName);
        dockerClient.restartContainerCmd(containerId).withTimeout(30).exec();
        log.info("컨테이너 재시작 완료: {}", containerName);
    }

    @Override
    public void rollback(String job) {
        String rollbackImage = getRollbackImage(job);
        String containerName = toContainerName(job);

        String containerId = findContainerId(containerName);
        InspectContainerResponse info = dockerClient.inspectContainerCmd(containerId).exec();

        ensureImageExists(rollbackImage);

        dockerClient.stopContainerCmd(containerId).withTimeout(30).exec();
        dockerClient.removeContainerCmd(containerId).exec();

        ExposedPort[] exposedPorts = info.getConfig().getExposedPorts();
        String newContainerId = dockerClient.createContainerCmd(rollbackImage)
                .withName(containerName)
                .withEnv(info.getConfig().getEnv())
                .withHostConfig(info.getHostConfig())
                .withExposedPorts(exposedPorts != null ? exposedPorts : new ExposedPort[0])
                .exec()
                .getId();

        for (Map.Entry<String, ContainerNetwork> entry : info.getNetworkSettings().getNetworks().entrySet()) {
            if (!"bridge".equals(entry.getKey())) {
                dockerClient.connectToNetworkCmd()
                        .withContainerId(newContainerId)
                        .withNetworkId(Objects.requireNonNull(entry.getValue().getNetworkID()))
                        .exec();
            }
        }

        dockerClient.startContainerCmd(newContainerId).exec();
        log.info("컨테이너 롤백 완료: container={}, image={}", containerName, rollbackImage);
    }

    private String findContainerId(String containerName) {
        return dockerClient.listContainersCmd()
                .withNameFilter(List.of("/" + containerName))
                .exec()
                .stream()
                .findFirst()
                .map(Container::getId)
                .orElseThrow(() -> new IllegalArgumentException("컨테이너를 찾을 수 없습니다: " + containerName));
    }

    private String getRollbackImage(String job) {
        String envKey = "ROLLBACK_IMAGE_" + job.replace("antcamp-", "").replace("-", "_").toUpperCase(Locale.ROOT);
        String image = environment.getProperty(envKey);
        if (image == null || image.isBlank()) {
            throw new IllegalStateException("롤백 이미지 미설정: " + envKey);
        }
        return image;
    }

    private void ensureImageExists(String image) {
        try {
            dockerClient.inspectImageCmd(image).exec();
            log.info("롤백 이미지 로컬 확인: {}", image);
        } catch (NotFoundException e) {
            log.info("롤백 이미지 풀링 시작: {}", image);
            try {
                dockerClient.pullImageCmd(image)
                        .exec(new PullImageResultCallback())
                        .awaitCompletion(10, TimeUnit.MINUTES);
                log.info("롤백 이미지 풀링 완료: {}", image);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("이미지 풀링 중단: " + image, ie);
            }
        }
    }

    private String toContainerName(String job) {
        return job;
    }
}
