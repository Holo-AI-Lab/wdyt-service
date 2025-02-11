package ai.holo.wdyt.deeplink.repository;

import ai.holo.wdyt.deeplink.model.entity.ClientFingerprint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientFingerprintRepository extends JpaRepository<ClientFingerprint, Long> {
    List<ClientFingerprint> findByUserFingerprint(String userFingerprint);
}
