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
import io.antcamp.notificationservice.domain.exception.DockerOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
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
        log.info("재시작 시작: job={}", job);
        String containerId = findContainerId(containerName);
        log.info("컨테이너 조회 완료: container={}, id={}", containerName, containerId);
        try {
            dockerClient.restartContainerCmd(containerId).withTimeout(30).exec();
            log.info("재시작 완료: container={}", containerName);
        } catch (Exception e) {
            log.error("재시작 실패: container={}", containerName);
            throw e;
        }
    }

    @Override
    public void rollback(String job) {
        String rollbackImage = getRollbackImage(job);
        String containerName = toContainerName(job);
        log.info("롤백 시작: job={}, image={}", job, rollbackImage);

        String containerId = findContainerId(containerName);
        log.info("컨테이너 조회 완료: container={}, id={}", containerName, containerId);
        InspectContainerResponse info = dockerClient.inspectContainerCmd(containerId).exec();

        ensureImageExists(rollbackImage);

        String originalImage = info.getConfig().getImage();
        ExposedPort[] exposedPorts = info.getConfig().getExposedPorts();

        try {
            dockerClient.stopContainerCmd(containerId).withTimeout(30).exec();
            log.info("컨테이너 중지 완료: {}", containerName);
        } catch (Exception e) {
            log.error("컨테이너 중지 실패: {}", containerName);
            throw e;
        }

        try {
            dockerClient.removeContainerCmd(containerId).exec();
            log.info("컨테이너 제거 완료: {}", containerName);
        } catch (Exception e) {
            log.error("컨테이너 제거 실패: {}", containerName);
            throw e;
        }

        String newContainerId = createContainer(containerName, rollbackImage, originalImage, info, exposedPorts);

        for (Map.Entry<String, ContainerNetwork> entry : info.getNetworkSettings().getNetworks().entrySet()) {
            if (!"bridge".equals(entry.getKey())) {
                try {
                    String networkId = entry.getValue().getNetworkID();
                    if (networkId == null) {
                        log.warn("NetworkID null, 네트워크 연결 스킵: network={}", entry.getKey());
                        continue;
                    }
                    dockerClient.connectToNetworkCmd()
                            .withContainerId(newContainerId)
                            .withNetworkId(networkId)
                            .exec();
                    log.info("네트워크 연결 완료: container={}, network={}", containerName, entry.getKey());
                } catch (Exception e) {
                    log.error("네트워크 연결 실패: container={}, network={}", containerName, entry.getKey());
                    throw e;
                }
            }
        }

        try {
            dockerClient.startContainerCmd(newContainerId).exec();
            log.info("컨테이너 롤백 완료: container={}, image={}", containerName, rollbackImage);
        } catch (Exception e) {
            log.error("컨테이너 시작 실패: container={}, image={}", containerName, rollbackImage);
            throw e;
        }
    }

    private String createContainer(String containerName, String targetImage, String fallbackImage,
                                   InspectContainerResponse info, ExposedPort[] exposedPorts) {
        try {
            String id = dockerClient.createContainerCmd(targetImage)
                    .withName(containerName)
                    .withEnv(info.getConfig().getEnv())
                    .withHostConfig(info.getHostConfig())
                    .withExposedPorts(exposedPorts != null ? exposedPorts : new ExposedPort[0])
                    .exec()
                    .getId();
            log.info("컨테이너 생성 완료: container={}, image={}", containerName, targetImage);
            return id;
        } catch (Exception e) {
            log.error("롤백 이미지로 컨테이너 생성 실패, 원본 이미지로 복구 시도: container={}, image={}", containerName, targetImage);
            try {
                String id = dockerClient.createContainerCmd(fallbackImage)
                        .withName(containerName)
                        .withEnv(info.getConfig().getEnv())
                        .withHostConfig(info.getHostConfig())
                        .withExposedPorts(exposedPorts != null ? exposedPorts : new ExposedPort[0])
                        .exec()
                        .getId();
                log.warn("원본 이미지로 복구 완료: container={}, image={}", containerName, fallbackImage);
                return id;
            } catch (Exception recovery) {
                log.error("복구 실패 — 서비스 다운 상태: container={}", containerName);
                throw recovery;
            }
        }
    }

    private String findContainerId(String containerName) {
        return dockerClient.listContainersCmd()
                .withNameFilter(List.of("/" + containerName))
                .exec()
                .stream()
                .findFirst()
                .map(Container::getId)
                .orElseThrow(() -> {
                    log.warn("컨테이너를 찾을 수 없음: container={}", containerName);
                    return DockerOperationException.containerNotFound();
                });
    }

    private String getRollbackImage(String job) {
        String envKey = "ROLLBACK_IMAGE_" + job.replace("antcamp-", "").replace("-", "_").toUpperCase(Locale.ROOT);
        String image = environment.getProperty(envKey);
        if (image == null || image.isBlank()) {
            log.error("롤백 이미지 미설정: envKey={}", envKey);
            throw DockerOperationException.rollbackImageNotConfigured();
        }
        return image;
    }

    //이미지 사용 가능한지 확인
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
                log.error("이미지 풀링 중단: image={}", image, ie);
                throw DockerOperationException.operationFailed();
            }
        }
    }

    private String toContainerName(String job) {
        return job;
    }
}
