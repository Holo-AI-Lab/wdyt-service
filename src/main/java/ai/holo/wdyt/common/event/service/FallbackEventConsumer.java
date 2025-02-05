package ai.holo.wdyt.common.event.service;

import ai.holo.wdyt.common.event.Event;
import ai.holo.wdyt.common.event.model.EventLog;
import ai.holo.wdyt.common.event.repository.EventLogRepository;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Slf4j
@Component
public class FallbackEventConsumer {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final EventLogRepository eventLogRepository;
    private final EventSerializer eventSerializer;
    private final PlatformTransactionManager transactionManager;
    private final String eventMaxRetryCount;
    private final String eventDelayInMinutes;
    private final static int MAX_ALLOWED_EVENT_FOR_EACH_ITERATION = 50;

    public FallbackEventConsumer(ApplicationEventPublisher applicationEventPublisher,
                                 EventLogRepository eventLogRepository,
                                 EventSerializer eventSerializer,
                                 @Qualifier("transactionManager") PlatformTransactionManager transactionManager,
                                 @Value("${application.event.max.retry.count}") String eventMaxRetryCount,
                                 @Value("${application.event.delay.minutes.after.produce}") String eventDelayInMinutes) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.eventLogRepository = eventLogRepository;
        this.eventSerializer = eventSerializer;
        this.transactionManager = transactionManager;
        this.eventMaxRetryCount = eventMaxRetryCount;
        this.eventDelayInMinutes = eventDelayInMinutes;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    @SchedulerLock(name = "Scheduler_scheduledFallbackEventConsumer",
            lockAtLeastFor = "PT4M", lockAtMostFor = "PT10M")
    @Transactional
    public void consumeFailedEvents() {
        log.info("Scheduled Fallback event consumer started...");

        int maxRetryCount = Integer.parseInt(eventMaxRetryCount);
        int eventDelayInMinutesAfterProducing = Integer.parseInt(eventDelayInMinutes);
        LocalDateTime maxProducedDate = getMaxProduceDateWithTheDelayOnConfig(eventDelayInMinutesAfterProducing);

        AtomicInteger eventCount = new AtomicInteger();
        Stream<EventLog> unprocessedEventsStream = eventLogRepository.getUnprocessedEvents(maxRetryCount, maxProducedDate);
        unprocessedEventsStream.forEach(eventLog -> {
            if(eventCount.intValue() < MAX_ALLOWED_EVENT_FOR_EACH_ITERATION) {
                processEvent(eventLog);
                sleep500Milis();
            }
            eventCount.getAndIncrement();
        });

        log.info(String.format("Scheduled Fallback event consumer finished. %s events has been tried to be consumed.", eventCount));
    }

    private void sleep500Milis() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            log.error("Error on sleeping thread", e);
        }
    }

    private LocalDateTime getMaxProduceDateWithTheDelayOnConfig(int eventDelayAfterProduce) {
        return LocalDateTime.now().minusMinutes(eventDelayAfterProduce);
    }

    private void processEvent(EventLog eventLog) {
        executeInNewTransaction(() -> {
            try {
                Event event = eventSerializer.deserialize(eventLog) ;
                event.setFallback(true);
                event.setEventId(UUID.fromString(eventLog.getId()));
                applicationEventPublisher.publishEvent(event);
            } catch (Exception e) {
                logError(eventLog, e);
            } finally {
                eventLog.setRetryCount(eventLog.getRetryCount() + 1);
                eventLogRepository.save(eventLog);
            }
        });
    }

    private void logError(EventLog eventLog, Exception e) {
        log.error(String.format("Error on fallback event consume, event id: %s", eventLog.getId()), e);
    }

    private void executeInNewTransaction(Runnable runnable) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                                        @Override
                                        protected void doInTransactionWithoutResult(TransactionStatus status) {
                                            runnable.run();
                                        }
                                    }
        );
    }
}
