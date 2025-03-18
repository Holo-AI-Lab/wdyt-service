package ai.holo.wdyt.contactus.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "contact_us")
@Getter
@Setter
@NoArgsConstructor
public class ContactUs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "email")
    private String email;
    @Column(name = "name")
    private String name;
    @Column(name = "subject")
    private String subject;
    @Column(name = "message")
    private String message;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public ContactUs(String email, String name, String subject, String message) {
        this.email = email;
        this.name = name;
        this.subject = subject;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }
}
