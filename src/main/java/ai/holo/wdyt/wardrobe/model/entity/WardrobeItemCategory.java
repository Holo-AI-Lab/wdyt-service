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

    public static WardrobeItemCategory fromValue(String label) {
        for (WardrobeItemCategory category : WardrobeItemCategory.values()) {
            if (category.getDisplayName().equalsIgnoreCase(label)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown category: " + label);
    }
}
