package ai.holo.wdyt.wardrobe.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity(name = "draft_wardrobe_item")
@Getter
@Setter
@NoArgsConstructor
public class DraftWardrobeItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "ai_feedback_id")
    private Long aiFeedbackId;
    @Column(name = "name")
    private String name;
    @Column(name = "content")
    private String content;
    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    private WardrobeItemCategory category;
    @Column(name = "sub_category")
    private String subCategory;
    @Column(name = "season")
    private String season;
    @Column(name = "colors")
    @Convert(converter = ColorConverter.class)
    private List<Color> colors;
    @Column(name = "image_path")
    private String imagePath;
    @Column(name = "extraction_type")
    @Enumerated(EnumType.STRING)
    private WardrobeItemExtractionType extractionType = WardrobeItemExtractionType.AUTOMATIC;

    public DraftWardrobeItem(Long userId, Long aiFeedbackId, String name, String content, WardrobeItemCategory category, String subCategory,
                             List<Color> colors, String season, String imagePath) {
        this.userId = userId;
        this.aiFeedbackId = aiFeedbackId;
        this.name = name;
        this.content = content;
        this.category = category;
        this.subCategory = subCategory;
        this.colors = colors;
        this.season = season;
        this.imagePath = imagePath;
    }
}
