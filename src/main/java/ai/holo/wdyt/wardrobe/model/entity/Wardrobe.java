package ai.holo.wdyt.wardrobe.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity(name = "wardrobe")
@Getter
@Setter
@NoArgsConstructor
public class Wardrobe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "wardrobe_id")
    private List<WardrobeItem> items = new ArrayList<>();

    public Wardrobe(Long userId) {
        this.userId = userId;
    }
}
