package ai.holo.wdyt.user.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "user")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "email", unique = true)
    private String email;
    @Column(name = "name")
    private String name;
    @Column(name = "apple_id")
    private String appleId;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "robot_id", referencedColumnName = "id")
    private Robot robot;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public User(String email, String name, String appleId) {
        this.email = email;
        this.name = name;
        this.appleId = appleId;
        createdAt = LocalDateTime.now();
    }
}
