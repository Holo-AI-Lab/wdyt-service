package ai.holo.wdyt.wardrobe.model.entity;

import ai.holo.wdyt.askai.model.entity.TagConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private Category category;
    @Column(name = "liked")
    private boolean liked;
    @Convert(converter = TagConverter.class)
    @Column(columnDefinition = "JSON")
    private Map<String, List<String>> tags = new HashMap<>();
}
