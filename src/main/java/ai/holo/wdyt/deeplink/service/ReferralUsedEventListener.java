package ai.holo.wdyt.deeplink.service;

import ai.holo.wdyt.common.event.service.EventConsumer;
import ai.holo.wdyt.deeplink.model.event.ReferralUsedEvent;
import ai.holo.wdyt.user.service.FriendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@EventConsumer
@Async
@Slf4j
public class ReferralUsedEventListener {
    private final FriendService friendService;

    public ReferralUsedEventListener(FriendService friendService) {
        this.friendService = friendService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReferralUsed(ReferralUsedEvent event) {
        // handle event
        log.info("Handling Referral Used Event for referringUserId: {} and invitedUserId: {}", event.getReferringUserId(), event.getInvitedUserId());
        boolean alreadyFriends = friendService.createFriends(event.getReferringUserId(), event.getInvitedUserId());
        if (alreadyFriends) {
            log.info("Users are already friends");
        }
    }
}
