package ai.holo.wdyt.askai.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "ai_feedback_order")
@Getter
@Setter
@NoArgsConstructor
public class AiFeedbackOrder {
    @Id
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "last_order")
    private int lastOrder;

    public AiFeedbackOrder(Long userId) {
        this.userId = userId;
        this.lastOrder = 0;
    }

    public int incrementOrder() {
        return ++lastOrder;
    }
}
