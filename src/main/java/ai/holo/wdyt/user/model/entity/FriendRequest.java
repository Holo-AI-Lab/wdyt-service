package ai.holo.wdyt.user.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "user_friend_request")
@Getter
@Setter
@NoArgsConstructor
public class FriendRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "friend_id")
    private Long friendId;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public FriendRequest(Long userId, Long friendId) {
        this.userId = userId;
        this.friendId = friendId;
        createdAt = LocalDateTime.now();
    }
}
