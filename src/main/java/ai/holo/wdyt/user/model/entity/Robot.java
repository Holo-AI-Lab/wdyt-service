package ai.holo.wdyt.user.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "wdyt_robot")
@Getter
@Setter
@NoArgsConstructor
public class Robot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "robot_source_id", unique = true)
    private Long robot_source_id;
    @Column(name = "name")
    private String name;
    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @Column(name = "birthday")
    private LocalDateTime birthday;
    @Column(name = "head_image_url")
    private String headImageUrl;
    @Column(name = "avatar_url")
    private String avatarUrl;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Robot(Long robot_source_id ,String name, Gender gender, LocalDateTime birthday,
                 String headImageUrl, String avatarUrl) {
        this.robot_source_id = robot_source_id;
        this.name = name;
        this.gender =  gender;
        this.birthday = birthday;
        this.headImageUrl = headImageUrl;
        this.avatarUrl = avatarUrl;
        this.createdAt = LocalDateTime.now();
    }
}
