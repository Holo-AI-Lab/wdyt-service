package ai.holo.wdyt.user.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "user_friend")
@Getter
@Setter
@NoArgsConstructor
public class UserFriend {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_l_id")
    private Long userLId;
    @Column(name = "user_r_id")
    private Long userRId;
    @Column(name = "user_l_r_id")
    private String userLRId;
    @Column(name = "status")
    private String status;
}
