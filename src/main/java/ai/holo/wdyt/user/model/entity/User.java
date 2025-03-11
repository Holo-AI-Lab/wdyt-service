package ai.holo.wdyt.user.model.entity;

import ai.holo.wdyt.user.model.dto.UserSelectedStyle;
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
    @Column(name = "username")
    private String username;
    @Column(name = "profile_picture")
    private String profilePicture;
    @Column(name = "apple_id")
    private String appleId;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "robot_id", referencedColumnName = "id")
    private Robot robot;
    @Column(name = "is_style_adapted")
    private boolean isStyleAdapted;
    // Used for push notifications
    @Column(name = "device_token")
    private String deviceToken;
    @Column(name = "timezone")
    private String timezone;
    @Convert(converter = UserSelectedStyleConverter.class)
    @Column(name = "user_selected_style")
    private UserSelectedStyle selectedStyle;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "received_feedbacks")
    private int receivedFeedbacks;
    @Column(name = "given_feedbacks")
    private int givenFeedbacks;

    public User(String email, String name, String appleId) {
        this.email = email;
        this.name = name;
        this.appleId = appleId;
        isStyleAdapted = true;
        createdAt = LocalDateTime.now();
    }

    public void increaseReceivedFeedbacks() {
        receivedFeedbacks++;
    }

    public void increaseGivenFeedbacks() {
        givenFeedbacks++;
    }
}
