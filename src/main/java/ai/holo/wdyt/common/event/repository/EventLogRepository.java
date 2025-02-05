package ai.holo.wdyt.common.event.repository;

import ai.holo.wdyt.common.event.model.EventLog;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.hibernate.jpa.QueryHints.*;

public interface EventLogRepository extends JpaRepository<EventLog, String> {

    @QueryHints(value = {
            @QueryHint(name = HINT_FETCH_SIZE, value = "100"),
            @QueryHint(name = HINT_CACHEABLE, value = "false"),
            @QueryHint(name = HINT_READONLY, value = "true")
    })
    @Query("select p from EventLog p where p.retryCount < :maxRetryCount and p.createdDate < :maxProducedDate order by p.createdDate asc")
    Stream<EventLog> getUnprocessedEvents(int maxRetryCount, LocalDateTime maxProducedDate);
}
