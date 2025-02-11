package ai.holo.wdyt.deeplink.repository;

import ai.holo.wdyt.deeplink.model.entity.ReferralLink;
import ai.holo.wdyt.user.model.entity.Friend;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReferralLinkRepository extends JpaRepository<ReferralLink, Long> {
    Optional<ReferralLink> findByNonce(String nonce);
}
