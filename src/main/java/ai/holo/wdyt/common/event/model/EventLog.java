package ai.holo.wdyt.common.event.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_log")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EventLog {
    @Id
    private String id;
    @Column(name = "event")
    private String event;
    @Column(name = "payload")
    private String payload;
    @Column(name = "created_date")
    @CreatedDate
    private LocalDateTime createdDate;
    @Column(name = "produced_by")
    private Long producedById;
    @Column(name = "retry_count")
    private Integer retryCount;
}
