package ai.holo.wdyt.subscription.service;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UserCreditUpdaterScheduledJob {
    private final UserCreditService userCreditService;

    public UserCreditUpdaterScheduledJob(UserCreditService userCreditService) {
        this.userCreditService = userCreditService;
    }

    // Schedule for every day at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    @SchedulerLock(name = "Scheduler_scheduledUpdateUserCreditsLock", lockAtLeastFor = "PT5M", lockAtMostFor = "PT55M")
    @Transactional
    public void updateUserCredits() {
        log.info("Starting update user credits job at {}", System.currentTimeMillis());
        userCreditService.markExpiredCreditsAsInvalid();
        userCreditService.markConsumedCreditsToInvalid();
        userCreditService.renewFreemiumCredits();
        log.info("Finished update user credits job at {}", System.currentTimeMillis());
    }
}
