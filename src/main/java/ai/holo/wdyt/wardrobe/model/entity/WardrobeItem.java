package ai.holo.wdyt.wardrobe.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "wardrobe_item")
@Getter
@Setter
@NoArgsConstructor
public class WardrobeItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "image_path")
    private String imagePath;
    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    private WardrobeItemCategory category;
    @Column(name = "liked")
    private boolean liked;
    @Convert(converter = WardrobeTagConverter.class)
    @Column(columnDefinition = "JSON")
    private Tags tags;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wardrobe_id", nullable = false)
    private Wardrobe wardrobe;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
