package ai.holo.wdyt.wardrobe.model.entity;

import lombok.Getter;

@Getter
public enum WardrobeItemCategory {
    TOPS("tops"),
    BOTTOMS("bottoms"),
    DRESSES("dresses"),
    JUMPSUITS("jumpsuits"),
    OUTERWEAR("outerwear"),
    FOOTWEAR("footwear"),
    SWIMWEAR("swimwear"),
    ACCESSORIES("accessories");

    private final String displayName;

    WardrobeItemCategory(String displayName) {
        this.displayName = displayName;
    }
}
