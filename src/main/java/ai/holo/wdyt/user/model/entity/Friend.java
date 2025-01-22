package ai.holo.wdyt.user.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "user_friend")
@Getter
@Setter
@NoArgsConstructor
public class Friend {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", nullable = false)
    private User friend;
    @Column(name = "created_at")
    private LocalDateTime createdAt;


    public Friend(Long userId, User friend) {
        this.userId = userId;
        this.friend = friend;
        createdAt = LocalDateTime.now();
    }
}
