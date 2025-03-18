package ai.holo.wdyt.deeplink.repository;

import ai.holo.wdyt.deeplink.model.entity.ReferralLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReferralLinkRepository extends JpaRepository<ReferralLink, Long> {
    Optional<ReferralLink> findByNonce(String nonce);
}
