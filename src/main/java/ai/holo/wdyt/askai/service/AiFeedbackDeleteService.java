package ai.holo.wdyt.askai.service;

import ai.holo.wdyt.askai.repository.AiFeedbackOrderRepository;
import ai.holo.wdyt.askai.repository.AiFeedbackRepository;
import ai.holo.wdyt.common.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AiFeedbackDeleteService {
    private final S3Service s3Service;
    private final AiFeedbackRepository aiFeedbackRepository;
    private final AiFeedbackOrderRepository aiFeedbackOrderRepository;

    public AiFeedbackDeleteService(S3Service s3Service,
                                   AiFeedbackRepository aiFeedbackRepository,
                                   AiFeedbackOrderRepository aiFeedbackOrderRepository) {
        this.s3Service = s3Service;
        this.aiFeedbackRepository = aiFeedbackRepository;
        this.aiFeedbackOrderRepository = aiFeedbackOrderRepository;
    }

    @Transactional
    public void deleteAllByUserId(Long userId) {
        aiFeedbackOrderRepository.deleteAllByUserId(userId);
        aiFeedbackRepository.deleteAllByUserId(userId);
        deleteS3Directory(userId);
    }

    private void deleteS3Directory(Long userId) {
        String path = String.format("%d/", userId);
        s3Service.deleteDirectory(path);
    }
}
